package mapred_multi_nodes;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.List;

/**
 * 
 * @author Gang, Chenjin
 *
 */
public class MapRequestThread extends Thread {
    // Make sure all the fields are initialized before you start the thread.
    List<Boolean> slaveJobDoneFlags;
    int slaveInd;
    ConfigItem slaveConfig;
    ConfigItem masterConfig;
    String jarPathInMaster;
    String jarPathInSlave; // jarPath in slave's machine
    String inputSplitPathInMaster;
    String inputSplitPathInSlave; // input split path in slave's machine
    String masterPKeyPathInSlave; // master machine's private key path in slave (for scp)
    String mapClsName; // User-specified map class name
    String outputKeyClass; // user-specified output key 
    String outputValueClass; // user-specified output value

    String resultPathInMaster; // map task result send back to the path in master machine.
    Log log;
    
    protected MapRequestThread(List<Boolean> slaveJobDoneFlags, int slaveInd,
            ConfigItem slaveConfig, ConfigItem masterConfig,
            String jarPathInMaster, String jarPathInSlave,
            String inputSplitPathInMaster, String inputSplitPathInSlave,
            String masterPKeyPathInSlave,
            String mapClsName,String outputKeyClass,String outputValueClass,
            String resultPathInMaster, Log log) {
        super();
        this.slaveJobDoneFlags = slaveJobDoneFlags;
        this.slaveInd = slaveInd; // Starting from 0
        this.slaveConfig = slaveConfig;
        this.masterConfig = masterConfig;
        this.jarPathInMaster = jarPathInMaster;
        this.jarPathInSlave = jarPathInSlave;
        this.inputSplitPathInMaster = inputSplitPathInMaster;
        this.inputSplitPathInSlave = inputSplitPathInSlave;
        this.masterPKeyPathInSlave = masterPKeyPathInSlave;
        this.mapClsName = mapClsName;
        this.outputKeyClass = outputKeyClass;
        this.outputValueClass = outputValueClass;
        this.resultPathInMaster = resultPathInMaster;
        this.log = log;
    }

    protected MapRequestThread() {
        super();
    }

    /**
     * @author Gang, Chenjin
     * @maintainer yaxin
     */
    @Override
    public void run(){
        Socket socket;
        DataInputStream distream;
        PrintStream dostream;
        try{
            socket = new Socket(this.slaveConfig.getIpAddr(), 
                    this.slaveConfig.getPort());
            this.log.writeLog("INFO - Conntected to slave : "+"<"+ 
                    this.slaveConfig.getIpAddr() +" : "+
                    this.slaveConfig.getPort() +">" + ". Ready to send request.");
            distream = new DataInputStream(socket.getInputStream());
            dostream = new PrintStream(socket.getOutputStream());

            // Scp the input file ,user.jar and private key to slave:
            MSProtocol.scpFile(this.slaveConfig, this.inputSplitPathInMaster,
                    this.inputSplitPathInSlave, this.log);
            MSProtocol.scpFile(this.slaveConfig, this.jarPathInMaster,
                    this.jarPathInSlave, this.log);
            MSProtocol.scpFile(this.slaveConfig, this.masterConfig.getPrivateKeyPath(),
                    this.masterPKeyPathInSlave, this.log);
            
            // Send request to let slave do the map.
            dostream.println(MSProtocol.sendMapRequest(
                    this.masterConfig, this.masterPKeyPathInSlave,
                    this.jarPathInSlave, this.inputSplitPathInSlave,
                    this.mapClsName, this.outputKeyClass, this.outputValueClass,
                    this.resultPathInMaster));
            this.log.writeLog("INFO - Request Sent:\n" +  // Write the log to debug.
                    MSProtocol.sendMapRequest(
                            this.masterConfig, this.masterPKeyPathInSlave,
                            this.jarPathInSlave, this.inputSplitPathInSlave,
                            this.mapClsName, this.outputKeyClass, this.outputValueClass,
                            this.resultPathInMaster));
            this.log.writeLog("INFO - Wait for map results.");
            @SuppressWarnings("deprecation")
            String response = distream.readLine();
            if (MSProtocol.isMapTaskDone(response)) {
                this.log.writeLog("INFO - map task in slave(" + 
                        this.slaveConfig.getHostname() + ") succeeded.");
                this.slaveJobDoneFlags.set(this.slaveInd, new Boolean(true));
            }
            else {
                this.log.writeLog("ERROR - map task in slave(" + 
                        this.slaveConfig.getHostname() + ") failed.");
            }    
            dostream.println(MSProtocol.terminateListener()); // End connection
            distream.close();
            dostream.close();
            socket.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected ConfigItem getSlaveConfig() {
        return slaveConfig;
    }

    protected void setSlaveConfig(ConfigItem slaveConfig) {
        this.slaveConfig = slaveConfig;
    }

    protected ConfigItem getMasterConfig() {
        return masterConfig;
    }

    protected void setMasterConfig(ConfigItem masterConfig) {
        this.masterConfig = masterConfig;
    }

    protected String getJarPathInMaster() {
        return jarPathInMaster;
    }

    protected void setJarPathInMaster(String jarPathInMaster) {
        this.jarPathInMaster = jarPathInMaster;
    }

    protected String getJarPathInSlave() {
        return jarPathInSlave;
    }

    protected void setJarPathInSlave(String jarPathInSlave) {
        this.jarPathInSlave = jarPathInSlave;
    }

    protected String getInputSplitPathInMaster() {
        return inputSplitPathInMaster;
    }

    protected void setInputSplitPathInMaster(String inputSplitPathInMaster) {
        this.inputSplitPathInMaster = inputSplitPathInMaster;
    }

    protected String getInputSplitPathInSlave() {
        return inputSplitPathInSlave;
    }

    protected void setInputSplitPathInSlave(String inputSplitPathInSlave) {
        this.inputSplitPathInSlave = inputSplitPathInSlave;
    }

    protected String getMasterPKeyPathInSlave() {
        return masterPKeyPathInSlave;
    }

    protected void setMasterPKeyPathInSlave(String masterPKeyPathInSlave) {
        this.masterPKeyPathInSlave = masterPKeyPathInSlave;
    }

    protected String getMapClsName() {
        return mapClsName;
    }

    protected void setMapClsName(String mapClsName) {
        this.mapClsName = mapClsName;
    }

    protected String getOutputKeyClass() {
        return outputKeyClass;
    }

    protected void setOutputKeyClass(String outputKeyClass) {
        this.outputKeyClass = outputKeyClass;
    }

    protected String getOutputValueClass() {
        return outputValueClass;
    }

    protected void setOutputValueClass(String outputValueClass) {
        this.outputValueClass = outputValueClass;
    }

    protected String getResultPathInMaster() {
        return resultPathInMaster;
    }

    protected void setResultPathInMaster(String resultPathInMaster) {
        this.resultPathInMaster = resultPathInMaster;
    }

    protected Log getLog() {
        return log;
    }

    protected void setLog(Log log) {
        this.log = log;
    }

    protected List<Boolean> getSlaveJobDoneFlags() {
        return slaveJobDoneFlags;
    }

    protected void setSlaveJobDoneFlags(List<Boolean> slaveJobDoneFlags) {
        this.slaveJobDoneFlags = slaveJobDoneFlags;
    }
}