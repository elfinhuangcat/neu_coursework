package mapred_multi_nodes;

import java.io.IOException;

/**
 * Both the master and slave should have a copy of this java file.
 * This is the protocol of their communication.
 * @author yaxin
 *
 */
class MSProtocol {
    private static final String MSGSEP = " ";
    
    /**
     * The sender should send the returned String out using socket connection.
     * @param masterConfig the config item for the master machine
     * @param pKeyPath the master's private key path in slave machine (copied)
     * @param jarPath the jar file path in slave machine (copied)
     * @param splitPath the split path in slave machine (copied)
     * @param mapClsName user-specified Mapper name
     * @param keyClsName output key class name
     * @param valClsName output value class name
     * @param mapResultPath the map result should be sent back to the master in this path
     * @return A string request, which should be sent out by the sender.
     */
    protected static String sendMapRequest(
            ConfigItem masterConfig,
            String pKeyPath, String jarPath, String splitPath, 
            String mapClsName, String keyClsName, String valClsName,
            String mapResultPath) {
        return (jarPath + MSGSEP + mapClsName + MSGSEP + splitPath + 
                MSGSEP + keyClsName + MSGSEP + valClsName + MSGSEP +
                mapResultPath + MSGSEP + pKeyPath +
                MSGSEP + masterConfig.getLoginUser() + MSGSEP +
                masterConfig.getIpAddr());
    }
    
    /**
     * Map request parser.
     * @param mapReq map request sent by master
     * @return a parsed MasterRequest object.
     * @author yaxin
     */
    protected static MasterRequest parseMapRequest(String mapReq) {
        String[] content = mapReq.split(MSGSEP);
        return new MasterRequest(content);
    }
    
    /**
     * Terminate the slave (listener).
     * @return string message which should be sent out by the sender.
     * @author yaxin
     */
    protected static String terminateListener() {
        return "BYE";
    }
    
    /**
     * Send the message to notify the master that the map task is successfully done.
     * @return string message which should be sent out by the sender.
     * @author yaxin
     */
    protected static String mapTaskDone() {
        return "OK";
    }
    
    /**
     * Send the message to notify the master that the map task failed.
     * @return string message which should be sent out by the sender.
     * @author yaxin
     */
    protected static String mapTaskFailed() {
        return "FAILED";
    }
    
    /**
     * See if the map task is done based on the ACK.
     * @param ack ACK message sent by the slave.
     * @return true if the ACK means it is done, otherwise false.
     * @author yaxin
     */
    protected static boolean isMapTaskDone(String ack) {
        if (ack.equals(mapTaskDone())) return true;
        else return false;
    }
    
    /**
     * Executes the command to scp a file from src machine to dest machine.
     * (Designed to be used by master)
     * @param slaveConfig the configuration item of the destination slave
     * @param filePath the path to the file to be copied
     * @param destFilePath the destination file path
     * @param log the log object to write the logs
     * @author yaxin
     */
    protected static void scpFile(ConfigItem slaveConfig,
            String filePath, String destFilePath, Log log) {
        try {
            String[] command = new String[] {"sh","-c","scp -i "+ 
                    slaveConfig.getPrivateKeyPath() + 
                    " -o \"StrictHostKeyChecking no\" " + 
                    filePath + " " +
                    slaveConfig.getLoginUser() + "@" + slaveConfig.getIpAddr() + ":" +
                    destFilePath};
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            log.writeLog("Command executed complete: " + command[2]);
        } catch (IOException e) {
            log.writeLog("ERROR - cannot scp file to destination.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            log.writeLog("ERROR - the scp is interrupted.");
            e.printStackTrace();
        }
    }
    
    /**
     * 
     * @param pKeyPath private key path in this machine to login to another machine
     * @param filepath the path of the file to be copied in this machine
     * @param loginUser login user name for the dest machine
     * @param destIp ip address of the dest machine
     * @param destFilePath destination file path
     */
    protected static void scpFile(String pKeyPath, String filepath,
            String loginUser, String destIp, String destFilePath) {
        String[] command = new String[] {"sh","-c","scp -i "+ 
                pKeyPath + 
                " -o \"StrictHostKeyChecking no\" " + 
                filepath + " " +
                loginUser + "@" + destIp + ":" +
                destFilePath};
        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
        } catch (IOException | InterruptedException e) {
            System.out.println("ERROR - error occurred during scp file.");
            e.printStackTrace();
        }    
    }
}

class MasterRequest {
    private String jarPath; // The jar path in slave machine (copied)
    private String mapClsName;
    private String splitPath; // The split path in slave machine. ( copied)
    private String keyClsName;
    private String valClsName;
    private String mapResultPathInMaster; // the result should sent back to this path in master
    private String privateKeyPath; // the master's private key path in slave machine (copied)
    private String loginUserName; // the master's login user name
    private String ipAddr; // the master's ip addr
    

    /**
     * To use this constructor, you have to promise your req contains the content 
     * as the same order as what listed in req.
     * @param req 0: jarPath 1: mapClsName 2: splitPath 3: keyClsName 
     *            4: valClsName 5: mapResultPath 6: privateKeyPath
     *            7: loginUserName 8: ipAddr
     */
    protected MasterRequest(String[] req) {
        this.jarPath = req[0];
        this.mapClsName = req[1];
        this.splitPath = req[2];
        this.keyClsName = req[3];
        this.valClsName = req[4];
        this.mapResultPathInMaster = req[5];
        this.privateKeyPath = req[6];
        this.loginUserName = req[7];
        this.ipAddr = req[8];
    }
    

    protected MasterRequest() {
        this.jarPath = null;
        this.mapClsName = null;
        this.splitPath = null;
        this.keyClsName = null;
        this.valClsName = null;
        this.mapResultPathInMaster = null;
        this.privateKeyPath = null;
        this.loginUserName = null;
        this.ipAddr = null;
    }
    protected String getJarPath() {
        return jarPath;
    }
    protected void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }
    protected String getMapClsName() {
        return mapClsName;
    }
    protected void setMapClsName(String mapClsName) {
        this.mapClsName = mapClsName;
    }
    protected String getSplitPath() {
        return splitPath;
    }
    protected void setSplitPath(String splitPath) {
        this.splitPath = splitPath;
    }
    protected String getKeyClsName() {
        return keyClsName;
    }
    protected void setKeyClsName(String keyClsName) {
        this.keyClsName = keyClsName;
    }
    protected String getValClsName() {
        return valClsName;
    }
    protected void setValClsName(String valClsName) {
        this.valClsName = valClsName;
    }

    protected String getPrivateKeyPath() {
        return privateKeyPath;
    }

    protected void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    protected String getLoginUserName() {
        return loginUserName;
    }

    protected void setLoginUserName(String loginUserName) {
        this.loginUserName = loginUserName;
    }

    protected String getIpAddr() {
        return ipAddr;
    }

    protected void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }


    public String getMapResultPathInMaster() {
        return mapResultPathInMaster;
    }


    public void setMapResultPathInMaster(String mapResultPathInMaster) {
        this.mapResultPathInMaster = mapResultPathInMaster;
    }
    
}