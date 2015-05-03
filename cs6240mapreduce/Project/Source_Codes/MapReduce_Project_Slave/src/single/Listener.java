package single;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import mapred_single.*;

/**
 * 
 * @author Gang, Yaxin
 *
 */
public class Listener {
	/**
	 * @reference http://stackoverflow.com/questions/11016092/how-to-load-classes-at-runtime-from-a-folder-or-jar
	 * @param args
	 */
	private String jarPath;
	private Mapper mapObj;
	private String splitPath; // path to input split
	private Class <?> outputKeyCls;
	private Class <?> outputValCls;
	private int PORT = 4444;
	protected Listener() {
		this.jarPath = null;
		this.mapObj = null;
		this.splitPath = null;
		this.outputKeyCls = null;
		this.outputValCls = null;
	}
	public static void main(String args[]) {
		Listener slave = new Listener();
		slave.listen();
	}
	@SuppressWarnings("deprecation")
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
				System.out.println("Listening....");
				// str is the request
				str = distream.readLine();
				
				System.out.println("INFO - request from master: " + str);

				if (str.trim().equals("BYE"))
					break;
				
				// 1. Parse the request:
				this.parseRequestFromMaster(str);
				// 2. Invoke map and send the message back.
				dostream.println(this.invokeMapProcess());
				System.out.println("INFO - map result path sent back to master.");
				break;
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
	 * 
	 * @param request
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	private void parseRequestFromMaster(String request) 
			throws IOException, ClassNotFoundException, 
			InstantiationException, IllegalAccessException {
		// info[0] - path to jar
		// info[1] - map class name
		// info[2] - path to input split
		// info[3] - output key class name
		// info[4] - output value class name
		String[] info = request.split(" ");
		System.out.println("INFO - request after split: \n" + Arrays.toString(info));
		this.jarPath = info[0];
		this.splitPath = info[2];
		this.outputKeyCls = Class.forName(info[3]);
		this.outputValCls = Class.forName(info[4]);
		
		JarFile jarFile = new JarFile(this.jarPath);
		Enumeration<JarEntry> e = jarFile.entries();

		URL[] urls = { new URL("jar:file:" + this.jarPath +"!/") };
		URLClassLoader cl = URLClassLoader.newInstance(urls);
		
		String className = null;
		while (e.hasMoreElements()) {
			JarEntry je = (JarEntry) e.nextElement();
			// -6 because ".class"
			className = je.getName().substring(0, je.getName().length()-6);
			className = className.replace('/', '.');
			if (je.getName().endsWith(".class") && className.equals(info[1])) {
				break;
			}
		}
		System.out.println("INFO - user-defined Mapper <" + className + "> found.");
		this.mapObj = (Mapper) cl.loadClass(className).newInstance();
		jarFile.close();
	}
	
	
	/**
	 * @return the message sent back to master (path to the map result file)
	 * @throws IOException 
	 */	
	@SuppressWarnings("unchecked")
	private String invokeMapProcess() throws IOException {
		@SuppressWarnings("rawtypes")
		Context mapContext = new Context();
		@SuppressWarnings("rawtypes")
		BufferedReader br = new BufferedReader(new FileReader(this.splitPath));
		String line;
		Integer lineCount = 0;
		System.out.println("INFO - map process starting..");
		while ((line=br.readLine()) != null) {
			this.mapObj.map(lineCount.toString(), line, mapContext);
			lineCount += 1;
		}
		br.close();
				
		//TODO: delete the split for future implementation
				
		// output the map result to a file
		File dir = new File("/tmp/mapoutput/");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File mapOutputFile = new File(dir.getAbsolutePath()
				+ "/mapresult");
		if (mapOutputFile.exists()) {
			mapOutputFile.createNewFile();
		}
		
		@SuppressWarnings("rawtypes")
		HashMap mapResults = mapContext.getResults();
		BufferedWriter bw = new BufferedWriter(new FileWriter(mapOutputFile, false));
		for (Object key : mapResults.keySet()) {
			List values = (List) mapResults.get(key);
			for (Object val : values) {
				//System.out.println("OUTPUT key = " + key.toString() + ", value = " + val.toString());
				bw.write(key.toString() + "\t" + val.toString() + "\n");
			}
		}
		bw.close();
		System.out.println("INFO - intermediate map result at: " + 
		    mapOutputFile.getAbsolutePath());
		return mapOutputFile.getAbsolutePath();
	}
}