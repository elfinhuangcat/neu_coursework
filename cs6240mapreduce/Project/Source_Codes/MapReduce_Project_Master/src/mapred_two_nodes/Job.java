package mapred_two_nodes;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/*
Single node simple MapReduce API.

OutputKey Type Restriction:
String
OutputValue Type Restriction:
Double, Integer, Long, String
*/

/**
 * 
 * @author Yaxin, Gang
 *
 */
public class Job {
	protected static final String SEP = "\t";
	
	// intermediate map results dir in master
	private final String MAP_RESULT_DIR = "/home/ec2-user/mapResult";
	// Slave workspace directory
	private final String SLAVE_ROOT_DIR = MSProtocol.getSlaveRootDir();
	
	private boolean isComplete;
	private boolean isSuccessful;
	private Configuration conf;
	@SuppressWarnings("rawtypes")
	private Class<? extends Mapper> mapperClass;
	@SuppressWarnings("rawtypes")
	private Class<? extends Reducer> reducerClass;
	private Class<?> outputKeyClass;
	private Class<?> outputValueClass;
	private String inputFilePath;
	private String outputDirPath;
	private String jarPath;
	
	/**
	 * Constructor which takes the configuration object to create a new job.
	 * @param conf
	 */
	public Job(Configuration conf) {
		this.isComplete = false;
		this.isSuccessful = false;
		this.conf = conf;
	}
	/** 
	 * Check if the job is complete.
	 * @return long
	 */
	public boolean isComplete() {
		return this.isComplete;
	}
	/**
	 * Check if the job is successfully complete.
	 * @return boolean
	 */
	public boolean isSuccessful() {
		return this.isSuccessful;}
	
	/**
	 * Set the jar by the path to the jar file.
	 * @param jar the path to the jar file
	 */
	public void setJar(String jar){
		this.jarPath = jar;
	}
	
	/**
	 * Set the Mapper class.
	 * @param cls the Mapper to use
	 */
	@SuppressWarnings("rawtypes")
	public void setMapperClass(Class<? extends Mapper> cls) {
		this.mapperClass = cls;
	}
	/**
	 * Set the Reducer of the job.
	 * @param cls the Reducer to use
	 */
	@SuppressWarnings("rawtypes")
	public void setReducerClass(Class<? extends Reducer> cls) {
		this.reducerClass = cls;
	}
	
	/**
	 * Set the key class for the job output data.
	 * @param theClass the class of the output key
	 */
	public void setOutputKeyClass(Class<?> theClass) {
		this.outputKeyClass = theClass;
	}
	/**
	 * Set the value class for the job output data.
	 * @param theClass the class of the output value
	 */
	public void setOutputValueClass(Class<?> theClass) {
		this.outputValueClass = theClass;
	}
	
	/**
	 * Set the path of the input file.
	 * @param path path to the input file
	 */
	public void setInputFilePath(String path) {
		this.inputFilePath = path;
	}
	
	/**
	 * Set the path to the directory where all output files resides in.
	 * @param path path to the output directory
	 */
	public void setOutputDirPath(String path) {
		this.outputDirPath = path;
	}
	
	/**
	 * Submit the job to the cluster and return immediately.
	 * @author Yaxin
	 */	
	public void submit() {
		// Check type settings:
		this.checkKeyAndValueTypes();
		// Check user-entered output directory existence
		File outputDir = new File(this.outputDirPath);
		if (outputDir.exists()) {
			System.out.println("ERROR - output dir exists.");
			this.isComplete = true;
			this.isSuccessful = false;
			System.exit(1);
		}
		outputDir.mkdirs();
		// Check user-entered input file existence
		File inputFile = new File(this.inputFilePath);
		if (!inputFile.isFile()) {
			System.out.println("ERROR - cannot find input file.");
			this.isComplete = true;
			this.isSuccessful = false;
		}
		// Create map result temp directory
		File mapDir = new File(this.MAP_RESULT_DIR);
		mapDir.mkdirs();
		
		//TODO: create split dir
		//TODO: split input file
		
		//Now we use the input file directly
		System.out.println("INFO - ready to send file to slave and let it do the map.");
		boolean response = this.sendMapToSlave();
		if (response) {
			System.out.println("INFO - sendMapToSlave() complete.");
			System.out.println("INFO - ready to do reduce.");
			this.doReduce();
			this.isComplete = true;
			this.isSuccessful = true;
			//TODO delete the map result folder
			//mapDir.delete();
			System.out.println("INFO - job successfully done.");
		}
		else {
			this.isComplete = true;
			this.isSuccessful = false;
			System.out.println("ERROR - MAP FAILED");
			System.exit(1);
		}
	}
	
	/**
	 * Checks the output key and value types. If any of them do not satisfy the 
	 * restriction, prints error message and exit.
	 * @author yaxin
	 */
	private void checkKeyAndValueTypes() {
		if (!this.outputKeyClass.equals(String.class)) {
			System.out.println("ERROR - output key class must be String.");
			this.isComplete = true;
			this.isSuccessful = false;
			System.exit(1);
		}
		if (!(this.outputValueClass.equals(String.class) || 
				this.outputValueClass.equals(Double.class) ||
				this.outputValueClass.equals(Integer.class) ||
				this.outputValueClass.equals(Long.class))) {
			System.out.println("ERROR - output value class must be among"
					+ " String, Long, Integer, Double.");
			this.isComplete = true;
			this.isSuccessful = false;
			System.exit(1);
		}
	}
	
	/**
	 * @author Gang
	 * @maintainer Yaxin
	 * @return path to the map result
	 */
	private boolean sendMapToSlave() {
		// Create socket communication
		Socket socket;
		DataInputStream distream;
		PrintStream dostream;
		String fileName = "1"; //Map result file name
		String jarName = "user.jar"; // User jar file name.
		String pKeyName = "pKey.pem";
		try {
			// TODO Now we only have one element in the slave list in config
			socket = new Socket(this.conf.getSlaves().get(0).getIpAddr(), 
					this.conf.getSlaves().get(0).getPort());
			System.out.println("INFO - Conntected to slave. Ready to send request.");
			distream = new DataInputStream(socket.getInputStream());
			dostream = new PrintStream(socket.getOutputStream());
			
			// Scp the input file ,user.jar and private key to slave:
			MSProtocol.scpFile(this.conf.getSlaves().get(0).getPrivateKeyPath(), 
					this.inputFilePath, //input split
					this.conf.getSlaves().get(0).getLoginUser(), 
					this.conf.getSlaves().get(0).getIpAddr(), 
					this.SLAVE_ROOT_DIR, fileName);
			MSProtocol.scpFile(this.conf.getSlaves().get(0).getPrivateKeyPath(), 
					this.jarPath, // user jar file
					this.conf.getSlaves().get(0).getLoginUser(), 
					this.conf.getSlaves().get(0).getIpAddr(), 
					this.SLAVE_ROOT_DIR, jarName);
			MSProtocol.scpFile(this.conf.getSlaves().get(0).getPrivateKeyPath(), 
					this.conf.getMaster().getPrivateKeyPath(), // private key
					this.conf.getSlaves().get(0).getLoginUser(), 
					this.conf.getSlaves().get(0).getIpAddr(), 
					this.SLAVE_ROOT_DIR, pKeyName);
			// Send request to let slave do the map.
			dostream.println(MSProtocol.sendMapRequest(
					this.SLAVE_ROOT_DIR +  "/" + jarName, 
					this.mapperClass.getName(), 
					this.SLAVE_ROOT_DIR + "/" + fileName, 
					this.outputKeyClass.getName(), 
					this.outputValueClass.getName(), 
					this.MAP_RESULT_DIR, fileName, 
					this.SLAVE_ROOT_DIR + "/" + pKeyName,
					this.conf.getMaster().getLoginUser(),
					this.conf.getMaster().getIpAddr()));
			System.out.println("INFO - request sent to slave. Wait for map results.");
			@SuppressWarnings("deprecation")
			String response = distream.readLine();
			if (MSProtocol.isMapTaskDone(response)) {
				dostream.println(MSProtocol.terminateListener());
				distream.close();
				dostream.close();
				socket.close();
				return true;
			}
			else {
				distream.close();
				dostream.close();
				socket.close();
				return false;
			}					
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return false; //Should not reach this line
	}
	
	/**
	 * Let the master do the reduce job.
	 * @author yaxin
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doReduce() {
		try {			
			Context reduceContext = new Context();
			Reducer reducer = this.reducerClass.newInstance();
			
			// Materialize the hashmap according to value type.
			HashMap mapResult = null;
			if (this.outputValueClass.equals(String.class)) {
				mapResult = new HashMap<String, ArrayList<String>>();
			}
			else if (this.outputValueClass.equals(Long.class)) {
				mapResult = new HashMap<String, ArrayList<Long>>();
			}
			else if (this.outputValueClass.equals(Integer.class)) {
				mapResult = new HashMap<String, ArrayList<Integer>>();
			}
			else {
				mapResult = new HashMap<String, ArrayList<Double>>();
			}
			File mapResultDir = new File(this.MAP_RESULT_DIR);
			for (File resultFile : mapResultDir.listFiles()) {
				this.readMapResultFromFile(mapResult, resultFile);
			}
			
			System.out.println("INFO - reducer starting..");

			for (Object ikey : mapResult.keySet()) {
				((Reducer) reducer).reduce(ikey, (List) mapResult.get(ikey), 
						reduceContext);
			}
			
			// output result
			File outputDir = new File(this.outputDirPath);
			outputDir.mkdir();
			File outputFile = new File(this.outputDirPath + "/part-0000");
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			
			for (Object ikey : reduceContext.getResults().keySet()) {
				for (Object ival : (List)(reduceContext.getResults().get(ikey))) {
					bw.write(ikey.toString() + SEP + ival.toString() + "\n");
				}
			}
			bw.close();
			System.out.println("INFO - reduce complete. Result dir path: " + 
			    outputDir.getAbsolutePath());
			
		} catch (FileNotFoundException e) {
			System.out.println("ERROR - map result not found.");
			this.isComplete = true;
			this.isSuccessful = false;
			e.printStackTrace();
			System.exit(1);
		} catch (IOException|SecurityException|InstantiationException|IllegalAccessException e) {			
			this.isComplete = true;
			this.isSuccessful = false;
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void readMapResultFromFile(HashMap mapResult, File file) {
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			if (this.outputValueClass.equals(String.class)) {
				// String
				while ((line = br.readLine()) != null) {
					String[] content = line.split(SEP);
					if (mapResult.containsKey(content[0])) {
						((ArrayList<String>) mapResult.get(content[0])).add(content[1]);
					}
					else {
						ArrayList<String> values = new ArrayList<String>();
						values.add(content[1]);
						mapResult.put(content[0], values);
					}
				}
			}
			else if (this.outputValueClass.equals(Long.class)) {
				// Long
				Long value;
				while ((line = br.readLine()) != null) {
					String[] content = line.split(SEP);
					value = Long.parseLong(content[1]);
					if (mapResult.containsKey(content[0])) {
						((ArrayList<Long>) mapResult.get(content[0])).add(value);
					}
					else {
						ArrayList<Long> values = new ArrayList<Long>();
						values.add(value);
						mapResult.put(content[0], values);
					}
				}
			}
			else if (this.outputValueClass.equals(Integer.class)) {
				// Long
				Integer value;
				while ((line = br.readLine()) != null) {
					String[] content = line.split(SEP);
					value = Integer.parseInt(content[1]);
					if (mapResult.containsKey(content[0])) {
						((ArrayList<Integer>) mapResult.get(content[0])).add(value);
					}
					else {
						ArrayList<Integer> values = new ArrayList<Integer>();
						values.add(value);
						mapResult.put(content[0], values);
					}
				}
			}
			else {
				// Double
				Double value;
				while ((line = br.readLine()) != null) {
					String[] content = line.split(SEP);
					value = Double.parseDouble(content[1]);
					if (mapResult.containsKey(content[0])) {
						((ArrayList<Double>) mapResult.get(content[0])).add(value);
					}
					else {
						ArrayList<Double> values = new ArrayList<Double>();
						values.add(value);
						mapResult.put(content[0], values);
					}
				}
				//TODO delete the intermediate result file
				//file.delete();
				br.close();
			}
		} catch (IOException e) {
			this.isComplete = true;
			this.isSuccessful = false;
			e.printStackTrace();
			System.exit(1);
		}
	}
}