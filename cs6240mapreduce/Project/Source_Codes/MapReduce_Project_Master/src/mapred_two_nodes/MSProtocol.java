package mapred_two_nodes;

import java.io.IOException;

/**
 * Both the master and slave should have a copy of this java file.
 * This is the protocol of their communication.
 * @author yaxin
 *
 */
class MSProtocol {
	private static final String MSGSEP = " ";
	private static final String SLAVE_ROOT_DIR = "/home/ec2-user/";
	
	/**
	 * 
	 * @param jarPath path to the user jar file in slave (already copied to slave machine)
	 * @param mapClsName name of user-defined Mapper class
	 * @param splitPath path to the input split in slave (scpied)
	 * @param keyClsName name of the output key class
	 * @param valClsName name of the output value class
	 * @param dirPath slave should send back result to this dir of the master
	 * @param fileName slave should send back the result of this file name
	 * @param pKeyPath the master's private key path in slave machine
	 * @param userName the master's login user name
	 * @param ipAddr the master's ip addr
	 * @return A string request, which should be sent out by the sender.
	 * @author yaxin
	 * */
	 
	protected static String sendMapRequest(String jarPath, String mapClsName, 
			String splitPath, String keyClsName, String valClsName,
			String dirPath, String fileName, String pKeyPath, 
			String userName, String ipAddr) {
		return (jarPath + MSGSEP + mapClsName + MSGSEP + splitPath + 
				MSGSEP + keyClsName + MSGSEP + valClsName + MSGSEP +
				dirPath + MSGSEP + fileName + MSGSEP + pKeyPath +
				MSGSEP + userName + MSGSEP + ipAddr);
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
		if (ack.equals(mapTaskDone()))  {
			System.out.println("INFO - check result: map task succeeded!");
			return true;
		}
		else return false;
	}
	
	/**
	 * Executes the command to scp a file from src machine to dest machine.
	 * @param pKeyPath the path to private key which can allow ssh login
	 * @param filePath the path to the file to be copied
	 * @param destUser the destination machine login user name
	 * @param destIP the destination machine IP address
	 * @param destDirPath the destination directory path
	 * @param destFileName the destination file name
	 * @author yaxin
	 */
	protected static void scpFile(String pKeyPath, String filePath, 
			String destUser, String destIP, String destDirPath, String destFileName) {
		try {
			Process p = Runtime.getRuntime().exec(new String[]
					{"sh","-c","scp -i "+ pKeyPath + 
					" -o \"StrictHostKeyChecking no\" " + 
					filePath + " " +
					destUser + "@" + destIP + ":" +
					destDirPath + "/" + destFileName});
			p.waitFor();
		} catch (IOException e) {
			System.out.println("ERROR - cannot scp file to destination.");
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String getSlaveRootDir() {
		return SLAVE_ROOT_DIR;
	}
}

class MasterRequest {
	private String jarPath;
	private String mapClsName;
	private String splitPath;
	private String keyClsName;
	private String valClsName;
	private String dirPath; // the master dir to receive map results
	private String fileName; //the map result file name.
	private String privateKeyPath; // the master's private key
	private String loginUserName; // the master's login user name
	private String ipAddr; // the master's ip addr
	

	/**
	 * To use this constructor, you have to promise your req contains the content 
	 * as the same order as what listed in req.
	 * @param req 0: jarPath 1: mapClsName 2: splitPath 3: keyClsName 
	 *            4: valClsName 5: dirPath 6: fileName 7: privateKeyPath
	 *            8: loginUserName 9: ipAddr
	 */
	protected MasterRequest(String[] req) {
		this.jarPath = req[0];
		this.mapClsName = req[1];
		this.splitPath = req[2];
		this.keyClsName = req[3];
		this.valClsName = req[4];
		this.dirPath = req[5];
		this.fileName = req[6];
		this.privateKeyPath = req[7];
		this.loginUserName = req[8];
		this.ipAddr = req[9];
	}
	
	/**
	 * 
	 * @param jarPath path to the user jar file in slave (already copied to slave machine)
	 * @param mapClsName name of user-defined Mapper class
	 * @param splitPath path to the input split in slave (scpied)
	 * @param keyCls name of the output key class
	 * @param valCls name of the output value class
	 * @param dirPath slave should send back result to this dir of the master
	 * @param fileName slave should send back the result of this file name
	 * @param pKeyPath the master's private key path in slave machine
	 * @param userName the master's login user name
	 * @param ipAddr the master's ip addr
	 */
	protected MasterRequest(String jarPath, String mapClsName, String splitPath,
			String keyCls, String valCls, String dirPath, String fileName, 
			String pKeyPath, String userName, String ipAddr) {
		this.jarPath = jarPath;
		this.mapClsName = mapClsName;
		this.splitPath = splitPath;
		this.keyClsName = keyCls;
		this.valClsName = valCls;
		this.dirPath = dirPath;
		this.fileName = fileName;
		this.privateKeyPath = pKeyPath;
		this.loginUserName = userName;
		this.ipAddr = ipAddr;
	}
	protected MasterRequest() {
		this.jarPath = null;
		this.mapClsName = null;
		this.splitPath = null;
		this.keyClsName = null;
		this.valClsName = null;
		this.dirPath = null;
		this.fileName = null;
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
	protected String getDirPath() {
		return dirPath;
	}
	protected void setDirPath(String dirPath) {
		this.dirPath = dirPath;
	}
	protected String getFileName() {
		return fileName;
	}
	protected void setFileName(String fileName) {
		this.fileName = fileName;
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
	
}