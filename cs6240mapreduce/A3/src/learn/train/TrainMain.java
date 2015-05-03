package learn.train;
/**
 * @author Yaxin Huang
 * @created Feb 25, 2015
 */

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;

/****************************************
 * This class configures the mapreduce job
 * @author Yaxin Huang
 */
public class TrainMain extends Configured implements Tool {
	public final String TEMP_OUTPUT_DIR = "temp-output";

    public static void main(String[] args) throws Exception {
    	/***********************************************
         * @param args[0] new-data.csv path
         * @param args[1] model.m path
         */
    	// 1. Run the MapReduce job
    	TrainMain classObj = new TrainMain();
    	int res = ToolRunner.run(new Configuration(), classObj, args);
        if (res != 0) System.exit(res);
    	// 2. Combine the results to produce model.m
        classObj.combineResults(args[1], classObj.TEMP_OUTPUT_DIR);
        
    }
    
    @Override
    /***********************************************
     * @param args[0] new-data.csv path
     * @param args[1] model.m path
     */
	public int run(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        Configuration conf = new Configuration();
        // 1. Parse the pregenerated model.m and new-data.csv
        //    to get needed properties:
        setPropertiesFromPrevWork(args[1], conf);

        // 2. Set up the MapReduce job:

        @SuppressWarnings("deprecation")
		Job job = new Job(conf);
        job.setJarByClass(TrainMain.class);
        job.setJobName("Flight Delay 15 mins? -- Naive Bayes");

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(TEMP_OUTPUT_DIR));

        job.setMapperClass(TrainMapper.class);
        job.setReducerClass(TrainReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        if (job.waitForCompletion(true)) {
        	long endTime = System.currentTimeMillis();
            System.out.println("Training Time(ms): " + (endTime - startTime));
            return 0;
        }
        else return 1;       
	}
    
    public static void setPropertiesFromPrevWork(String modelPath, 
    		Configuration conf) {
    	/**************************************************
    	 * @param modelPath: Path to pre-generated model.m
    	 * @param conf: The job configuration object
    	 * 
    	 * @effect Set the configuration.
    	 * Basically add these properties to configuration:
    	 * (1) feature_list:
    	 *     A list of:
    	 *     <feature_name><flist_delimiter><feature_type>
    	 *     (But for class label it is:
    	 *      <feature_name><flist_delimiter><feature_name>)
    	 * (2) true count
    	 * (3) false count
    	 */
    	try {
    		BufferedReader modelBr = new BufferedReader(
    				new FileReader(modelPath));
    		String line = null;
    		StringBuilder featureNames = new StringBuilder();
    		while ((line = modelBr.readLine()) != null) {
    			if (line.startsWith(mytools.StaticConstants.ATTRIBUTE_PREFIX)) {
    				// Starts with "@attribute"
    				String[] content = line.split(" ");
    				
    				if (content[mytools.StaticConstants.ATTRI_NAME_INDEX].equals(
    						mytools.StaticConstants.CLASS_LABEL_NAME)) {
    					// Class label
    					featureNames.append(
    							mytools.StaticConstants.CLASS_LABEL_NAME
        								+ PropertyNames.FLIST_DELIMITER
        								+ mytools.StaticConstants.CLASS_LABEL_NAME
        								+ ",");
    					continue;
    				}
    				
    				// Mark down the feature names & feature value type
    				featureNames.append(
    						content[mytools.StaticConstants.ATTRI_NAME_INDEX]
    								+ PropertyNames.FLIST_DELIMITER 
    								+ content[mytools.StaticConstants.ATTRI_TYPE_INDEX]
    								+ ",");
    				
    				if (content[mytools.StaticConstants.ATTRI_TYPE_INDEX].equals(
    						mytools.StaticConstants.REAL_MARK)) {
    					// If it is a real value type, mark down the mean
    					// Key = feature name
    					// Value = mean
    					conf.setDouble(content[mytools.StaticConstants.ATTRI_NAME_INDEX], 
    							Double.parseDouble(
    									content[mytools.StaticConstants.ATTRI_MEAN_INDEX]));
    				}
    			}
    			else if (line.equals("\n")) {
    				continue;
    			}
    			else if (line.startsWith(mytools.StaticConstants.RECORD_NUM_PREFIX)) {
    				conf.setInt(PropertyNames.RECORD_NUM, 
    						Integer.parseInt(line.split(" ")[1]));
    			}
    			else if (line.startsWith(mytools.StaticConstants.TRUE_NUM_PREFIX)) {
    				conf.setInt(PropertyNames.TRUE_NUM, 
    						Integer.parseInt(line.split(" ")[1]));
    			}
    			else if (line.startsWith(mytools.StaticConstants.FALSE_NUM_PREFIX)) {
    				conf.setInt(PropertyNames.FALSE_NUM, 
    						Integer.parseInt(line.split(" ")[1]));
    			}
    		}//END OF WHILE
    		
    		// delete the last comma ","
    		featureNames.deleteCharAt(featureNames.length()-1);
    		// Mark down the list of feature names
    		conf.setStrings(PropertyNames.FEATURE_LIST, featureNames.toString());
    		modelBr.close();
    		
    		// Set the output delimiter:
    		conf.set("mapreduce.textoutputformat.separator", " ");
    		
    		// Append the prior probabilities to model.m:
    		FileWriter modelWr = new FileWriter(modelPath, true);
    		double trueCount = (double)conf.getInt(PropertyNames.TRUE_NUM, 0);
    		double falseCount = (double)conf.getInt(PropertyNames.FALSE_NUM, 0);
    		modelWr.write("\n" + mytools.StaticConstants.PRIOR_PREFIX + ":\n"
    				+ "1 " + Math.log(trueCount / (trueCount+falseCount)) + "\n"
    				+ "0 " + Math.log(falseCount / (trueCount+falseCount)) + "\n\n");
    		modelWr.close();
    		
    	} catch (FileNotFoundException fileNotFoundExp) {
    		System.out.println("The pre-generated model.m does not exist.");
    		System.exit(1);
    	} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
    }
    
    public void combineResults(String modelPath, String tempOutputDir) 
    		throws IOException {
    	/**********************************************
    	 * @param modelPath: path to the model.m file
    	 * @param tempOutputDir: path to the temp-output folder
    	 * 
    	 * @effect combines the reduce output with the model.m
    	 *         & deletes the temp-output
    	 */
    	FileWriter modelWr = new FileWriter(modelPath, true);
    	File tempOutputs = new File(tempOutputDir);
    	
    	modelWr.write("@conditional_probability:\n");
    	
    	for (final File fileEntry : tempOutputs.listFiles()) {
    		if (fileEntry.getName().startsWith("part")) {
    		    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
    		    String line = null;
    		    while((line = br.readLine()) != null) {
    			    String[] content = line.split("\t");
    			    modelWr.write(content[0] + " " + content[1] + "\n");
    		    }
    		    br.close();
    		}
    	}
    	
    	// Clean up
    	modelWr.close();
    	for (final File fileEntry : tempOutputs.listFiles()) {
    		fileEntry.delete();
    	}
    	tempOutputs.delete();
    }
}