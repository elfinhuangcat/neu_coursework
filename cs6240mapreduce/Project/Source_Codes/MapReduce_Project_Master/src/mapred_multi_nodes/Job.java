package mapred_multi_nodes;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    protected static final String SEP = "\t"; // Reduce result separator
    private final String MAP_RESULT_DIR_NAME = "temp_map_results";
    private final String SPLIT_DIR_NAME = "temp_splits";
    private final String LOG_FILE_NAME = "loglog.txt";
    
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
    
    private Log log;
    
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
        // Create the log object:
        this.log = new Log(this.conf.getMaster().getRootDir() + 
                "/" + this.LOG_FILE_NAME);
        this.log.writeLog("==================================================\n"
                + "INFO - NEW JOB IS STARTING ~!!");
        // Check type settings:
        this.checkKeyAndValueTypes();        
        // Check input file & output dir existence:
        this.checkInputOutputAndCreate();
        
        // Create splits directory
        File splitDir = new File(this.conf.getMaster().getRootDir() + 
                "/" + this.SPLIT_DIR_NAME);
        splitDir.mkdirs();
        
        // Create map result temp directory (slaves should send back results to this dir)
        File mapDir = new File(this.conf.getMaster().getRootDir() + 
                "/" + this.MAP_RESULT_DIR_NAME);
        mapDir.mkdirs();
        
        this.splitInputFile();
        
        this.log.writeLog("INFO - ready to send file to slave and let it do the map.");
        
        boolean response = this.sendMapToSlave();
        if (response) {
            this.log.writeLog("INFO - sendMapToSlave() complete.\n"
                    + "INFO - ready to do reduce.");
            this.doReduce();
            this.isComplete = true;
            this.isSuccessful = true;
            this.log.writeLog("INFO - job successfully done.");
        }
        else {
            this.isComplete = true;
            this.isSuccessful = false;
            this.log.writeLog("ERROR - MAP (sendMapToSlave) FAILED");
            System.exit(1);
        }
        
        // Delete intermediate files and folders:
        for (File mapResultFile : mapDir.listFiles()) {
            mapResultFile.delete();
        }
        for (File splitFile : splitDir.listFiles()) {
            splitFile.delete();
        }
    }
    
    /**
     * Splits the input file and put it into the SPLIT_DIR
     * @author yaxin
     */
    private void splitInputFile() {
        File inputFile = new File(this.inputFilePath);
        // Split the file to the size that the number of splits is the number of slaves
        long bytes = (long) ((double) inputFile.length() / this.conf.getSlaves().size());
        try {
            new SplitFile(this.inputFilePath, 
                    bytes, // split size
                    // output the splits to this dir:
                    this.conf.getMaster().getRootDir() + "/" + this.SPLIT_DIR_NAME + "/");
        } catch (Exception e) {
            this.log.writeLog("ERROR - File Split: Cannot find the given dirs.");
            e.printStackTrace();
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
     * This method checks the user input file existence, and 
     * checks the output directory existence. If the output directory does 
     * not exists, then creates it. Otherwise exit with error.
     * @author yaxin
     */
    private void checkInputOutputAndCreate() {
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
    }
    
    /**
     * @author Gang, Chenjin
     * @maintainer Yaxin
     * @return path to the map result
     */
    private boolean sendMapToSlave() {
        List<Boolean> slaveJobDoneFlags = new ArrayList<Boolean>();
        for (int counter = 0; counter < this.conf.getSlaves().size(); ++counter) {
            slaveJobDoneFlags.add(new Boolean(false));
        }
        
        for (int i = 0; i < this.conf.getSlaves().size(); ++i) {
            //build several sockets using threads
            ConfigItem slaveConfig = this.conf.getSlaves().get(i);
            String splitFileName = new Integer(i + 1).toString();
            Thread requestThread = new MapRequestThread(slaveJobDoneFlags, i,
                    slaveConfig, this.conf.getMaster(),
                    this.jarPath, slaveConfig.getRootDir() + "/user.jar",
                    this.SPLIT_DIR_NAME + "/" + splitFileName,
                    slaveConfig.getRootDir() + "/" + splitFileName,
                    slaveConfig.getRootDir() + "/" + "key.pem",
                    this.mapperClass.getName(), this.outputKeyClass.getName(), this.outputValueClass.getName(),
                    this.MAP_RESULT_DIR_NAME + "/" + splitFileName, this.log);
            requestThread.start();
        }
        while (!this.arrayListAllTrue(slaveJobDoneFlags)) {
            // wait
        }
        return true;
    }
    
    /**
     * See if the elements in the Boolean list are all true
     * @param list a Boolean List
     * @return true if all the elements are true. Otherwise false
     * @author yaxin
     */
    private boolean arrayListAllTrue(List<Boolean> list) {
        for (Boolean element : list) {
            if (!element.booleanValue()) {
                return false;
            }
        }
        return true;
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
            File mapResultDir = new File(this.conf.getMaster().getRootDir() + 
                    "/" + this.MAP_RESULT_DIR_NAME);
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
    
    /**
     * This method reads the map result and then do reduce.
     * @param mapResult The HashMap containing all the map results
     * @param file the output file
     */
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