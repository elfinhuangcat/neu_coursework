package mapred_single;
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

import mapred_single.Configuration;
import mapred_single.Mapper;
import mapred_single.Reducer;
import mapred_single.Context;

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
	
	private final Integer PORT = 4444;
	private final String SplitDir = "/tmp/splits/";
	private final String MapResults = "/tmp/mapoutput/";
	
	private boolean isComplete;
	private boolean isSuccessful;
	@SuppressWarnings("unused")
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
	 * 
	 * @author Yaxin
	 */	
	public void submit() {
		File outputDir = new File(this.outputDirPath);
		if (outputDir.exists()) {
			System.out.println("ERROR - output dir exists.");
			this.isComplete = true;
			this.isSuccessful = false;
			System.exit(1);
		}
		
		
		File inputFile = new File(this.inputFilePath);
		if (!inputFile.isFile()) {
			System.out.println("ERROR - cannot find input file.");
			this.isComplete = true;
			this.isSuccessful = false;
		}
		//TODO: create split dir
		//TODO: split input file
		
		//Now we use the input file directly
		String response = this.sendMapToSlave();
		System.out.println("INFO - sendMapToSlave() complete.");
		if (response == null) {
			this.isComplete = true;
			this.isSuccessful = false;
			System.out.println("ERROR - MAP FAILED");
			System.exit(1);
		}
		else {
			// Do reduce
			System.out.println("INFO - ready to do reduce.");
			this.doReduce(response);
			this.isComplete = true;
			this.isSuccessful = true;
			System.out.println("INFO - job successfully done.");
		}
	}
	
	// TODO: new method
	/*
	private void splitFiles() throws Exception {
		String inputfile = this.inputFilePath;
		String outputfile = this.SplitDir;
		File file = new File(inputfile);
		
		long megaBytes = file.length()/(1024*1024)/ (this.conf.getslaves().size());
		SplitFile split = new SplitFile(inputfile, megaBytes,outputfile);
	}
	*/
	
	/**
	 * @author Gang
	 * @maintainer Yaxin
	 * @return path to the map result
	 */
	private String sendMapToSlave() {
		// Create socket communication
		Socket socket;
		DataInputStream distream;
		PrintStream dostream;
		try {
			socket = new Socket("127.0.0.1", this.PORT);
			System.out.println("INFO - Conntected to slave. Ready to send request.");
			distream = new DataInputStream(socket.getInputStream());
			dostream = new PrintStream(socket.getOutputStream());

			// Send request to let slave do the map.
			// info[0] - path to jar
			// info[1] - class name
			// info[2] - path to input split
			// info[3] - output key class name
			// info[4] - output value class name			
			dostream.println(this.jarPath + " " + this.mapperClass.getName()+ " " +
			    this.inputFilePath + " " + this.outputKeyClass.getName() + " " + 
				this.outputValueClass.getName());
			System.out.println("INFO - request sent to slave. Wait for map results.");
			@SuppressWarnings("deprecation")
			String path = distream.readLine();
			System.out.println("INFO - map result got. Path: " + path);
			dostream.println("BYE"); // End connection
			
			distream.close();
			dostream.close();
			socket.close();

			// Get server response (path to intermediate result file)
			return path;						
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @author Yaxin
	 * @param mapResultPath path to the map output file
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void doReduce(String mapResultPath) {
		try {			
			Context reduceContext = new Context();
			Reducer reducer = this.reducerClass.newInstance();
			BufferedReader br = new BufferedReader(new FileReader(mapResultPath));
			String line = null;
			HashMap mapResult = null;
			if (this.outputValueClass.equals(String.class)) {
				// String
				mapResult = new HashMap<String, ArrayList<String>>();
				while ((line = br.readLine()) != null) {
					String[] content = line.split(SEP);
					if (mapResult.containsKey(content[0])) {
						((ArrayList<String>) mapResult.get(content[0])).add(content[1]);
					}
					else {
						ArrayList<String> values = new ArrayList();
						values.add(content[1]);
						mapResult.put(content[0], values);
					}
				}
			}
			else if (this.outputValueClass.equals(Long.class)) {
				// Long
				mapResult = new HashMap<String, ArrayList<Long>>();
				Long value;
				while ((line = br.readLine()) != null) {
					String[] content = line.split(SEP);
					value = Long.parseLong(content[1]);
					if (mapResult.containsKey(content[0])) {
						((ArrayList<Long>) mapResult.get(content[0])).add(value);
					}
					else {
						ArrayList<Long> values = new ArrayList();
						values.add(value);
						mapResult.put(content[0], values);
					}
				}
			}
			else if (this.outputValueClass.equals(Integer.class)) {
				// Long
				mapResult = new HashMap<String, ArrayList<Long>>();
				Integer value;
				while ((line = br.readLine()) != null) {
					String[] content = line.split(SEP);
					value = Integer.parseInt(content[1]);
					if (mapResult.containsKey(content[0])) {
						((ArrayList<Integer>) mapResult.get(content[0])).add(value);
					}
					else {
						ArrayList<Integer> values = new ArrayList();
						values.add(value);
						mapResult.put(content[0], values);
					}
				}
			}
			else if (this.outputValueClass.equals(Double.class)) {
				// Long
				mapResult = new HashMap<String, ArrayList<Double>>();
				Double value;
				while ((line = br.readLine()) != null) {
					String[] content = line.split(SEP);
					value = Double.parseDouble(content[1]);
					if (mapResult.containsKey(content[0])) {
						((ArrayList<Double>) mapResult.get(content[0])).add(value);
					}
					else {
						ArrayList<Double> values = new ArrayList();
						values.add(value);
						mapResult.put(content[0], values);
					}
				}
			}
			else {
				System.out.println("ERROR - outputValue type is illegal.");
				System.exit(1);
			}
			br.close();
			File mapResultFile = new File(mapResultPath);
			
			mapResultFile.delete(); // Delete intermediate files.
			
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}
}