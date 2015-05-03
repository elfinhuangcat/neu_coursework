package slave_multi_nodes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import mapred_multi_nodes.Context;
import mapred_multi_nodes.Mapper;

/**
 * @see http://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
 * @author Gang, Yaxin
 *
 */
public class Listener {
    private String jarPath;
    private String splitPath; // path to input split
    private String masterPKeyPath;
    private String masterUserName;
    private String masterIP;
    private String scpDestFilePath; // the destination path of scp from slave to master
    
    
    @SuppressWarnings("rawtypes")
    private Mapper mapObj;    
    private Class <?> outputKeyCls;
    private Class <?> outputValCls;
    private final int PORT = 4444;
    // Slave workspace directory
    private final String ROOTDIR = "/home/ec2-user/"; // We hard-coded it here.
    protected Listener() {
        this.jarPath = null;
        this.mapObj = null;
        this.splitPath = null;
        this.setOutputKeyCls(null);
        this.setOutputValCls(null);
        this.masterPKeyPath = null;
        this.masterUserName = null;
        this.masterIP = null;
        this.scpDestFilePath = null;
    }
    public static void main(String[] args) {
        Listener slave = new Listener();
        slave.listen();
    }
    @SuppressWarnings("deprecation")
    /**
     * Receives requests from master and does the job according to the request.
     * @author yaxin
     */
    private void listen() {
        ServerSocket server;
        Socket socket;
        String str;
        DataInputStream distream;
        PrintStream dostream;
        try {
            //register the server at the port "4444"
            server = new ServerSocket(PORT);
            System.out.println("INFO - Slave is ready, waiting for requests.");
            System.out.println("************************************************");
            socket = server.accept();

            distream = new DataInputStream(socket.getInputStream());
            dostream = new PrintStream(socket.getOutputStream());
            
            while (true) {
                System.out.println("INFO - Listening....");
                // str is the request
                str = distream.readLine();
                
                System.out.println("INFO - request from master: " + str);

                if (str.trim().equals(MSProtocol.terminateListener()))
                    break;
                
                // 1. Parse the request:
                this.parseRequestFromMaster(str);
                // 2. Invoke map and send the message back.
                boolean result = this.invokeMapProcess();
                System.out.println("INFO - map task done? : " + result);
                if (result) {
                    dostream.println(MSProtocol.mapTaskDone());
                }
                else {
                    dostream.println(MSProtocol.mapTaskFailed());
                }
                System.out.println("INFO - map result path sent back to master.");
            }
            socket.close();
            server.close();
            distream.close();
            dostream.close();
        }

        catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }
    
    /**
     * Parses the request from master and initializes the Listener's fields.
     * @param request
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @author yaxin
     */
    @SuppressWarnings("rawtypes")
    private void parseRequestFromMaster(String request) 
            throws IOException, ClassNotFoundException, 
            InstantiationException, IllegalAccessException {
        MasterRequest masterReq = MSProtocol.parseMapRequest(request);
        this.jarPath = masterReq.getJarPath();
        this.splitPath = masterReq.getSplitPath();
        this.setOutputKeyCls(Class.forName(masterReq.getKeyClsName()));
        this.setOutputValCls(Class.forName(masterReq.getValClsName()));
        this.masterPKeyPath = masterReq.getPrivateKeyPath();
        this.masterIP = masterReq.getIpAddr();
        this.masterUserName = masterReq.getLoginUserName();
        this.scpDestFilePath = masterReq.getMapResultPathInMaster();
        
        JarFile jarFile = new JarFile(this.jarPath);
        Enumeration<JarEntry> e = jarFile.entries();

        URL[] urls = { new URL("jar:file:" + this.jarPath +"!/") };
        URLClassLoader cl = URLClassLoader.newInstance(urls);
        
        String className = null; // Store the class name of user's Mapper
        // Look for map class in the jar file
        while (e.hasMoreElements()) {
            JarEntry je = (JarEntry) e.nextElement();
            // -6 because ".class"
            className = je.getName().substring(0, je.getName().length()-6);
            className = className.replace('/', '.');
            if (je.getName().endsWith(".class") && className.equals(masterReq.getMapClsName())) {
                break;
            }
        }
        System.out.println("INFO - user-defined Mapper <" + className + "> found.");
        this.mapObj = (Mapper) cl.loadClass(className).newInstance();
        jarFile.close();
    }
    
    
    /**
     * Runs the map process.
     * @return true if the map task is successfully done, otherwise false.
     * @throws IOException 
     * @author yaxin
     */    
    @SuppressWarnings("unchecked")
    private boolean invokeMapProcess() {
        @SuppressWarnings("rawtypes")
        Context mapContext = new Context();
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(this.splitPath));
            String line;
            Integer lineCount = 0;
            System.out.println("INFO - map process starting..");
            while ((line=br.readLine()) != null) {
                this.mapObj.map(lineCount.toString(), line, mapContext);
                lineCount += 1;
            }
            br.close();
            System.out.println("INFO - done with doing map. Ready to output results to a file.");        
            File splitFile = new File(this.splitPath);
            splitFile.deleteOnExit();
                    
            // Creates a file to store the map result
            File dir = new File(this.ROOTDIR + "/tmp/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File mapOutputFile = new File(dir.getAbsolutePath()
                    + "/mapresult");
            if (!mapOutputFile.exists()) {
                mapOutputFile.createNewFile();
            }
            
            
            // Outputs the map result to the file
            @SuppressWarnings("rawtypes")
            HashMap mapResults = mapContext.getResults();
            BufferedWriter bw = new BufferedWriter(new FileWriter(mapOutputFile, false));
            for (Object key : mapResults.keySet()) {
                @SuppressWarnings("rawtypes")
                List values = (List) mapResults.get(key);
                for (Object val : values) {
                    //System.out.println("OUTPUT key = " + key.toString() + ", value = " + val.toString());
                    bw.write(key.toString() + "\t" + val.toString() + "\n");
                }
            }
            bw.close();
            System.out.println("INFO - intermediate map result at: " + 
                mapOutputFile.getAbsolutePath());
            
            // Scp the map result back
            MSProtocol.scpFile(this.masterPKeyPath, 
                    mapOutputFile.getAbsolutePath(), 
                    this.masterUserName, this.masterIP, 
                    this.scpDestFilePath);
            // After the file is sent back to master, delete it.
            mapOutputFile.delete();
            dir.delete(); // The dir stores the map result on slave.
            // Delete the private key:
            File masterPKey = new File(this.masterPKeyPath);
            masterPKey.delete();
            // Delete the user.jar file
            File userJarFile = new File(this.jarPath);
            userJarFile.delete();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR - Cannot find the input split.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return true;
    }
    public Class <?> getOutputKeyCls() {
        return outputKeyCls;
    }
    public void setOutputKeyCls(Class <?> outputKeyCls) {
        this.outputKeyCls = outputKeyCls;
    }
    public Class <?> getOutputValCls() {
        return outputValCls;
    }
    public void setOutputValCls(Class <?> outputValCls) {
        this.outputValCls = outputValCls;
    }
}