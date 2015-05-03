import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import check.MainCheck;
import predict.PredictDriver;
import learn.preprocess.PreprocessDriver;
import learn.train.TrainMain;

/******************************************************
* @author Yaxin Huang
* @created Mar 1, 2015
* 
* We need the jar stays in the same directory as the "header" file.
* 
* The model.m will be output to the directory /data as the jar.
* 
* Because when run in the AWS cluster, you need to provide absolute path,
* so please keep the jar in a folder and keep everything else in a subfolder
* called "data", just like this:
* 
* s3://<your_bucket_name>/
*     MapReduce_A3.jar
*     data/
*         header
*         data.csv
*         predict.csv
*         check.csv
* 
* When use the jar, please use a parameter to indicate if you are 
* running in your own machine or running in AWS cluster.
*/

public class NaiveBayesMain {
	public static final String MEAN_OUTPUT_DIR = "meanoutput";
	public static void main(String[] args) {
		/**********************************************************
		 * Usage:
		 * Running on your machine(may provide relative path):
		 * (1)hadoop jar MapReduce_A3.jar -s -learn <train.csv>
		 * (2)hadoop jar MapReduce_A3.jar -s -predict <model.m> <predict.csv>
		 * (3)hadoop jar MapReduce_A3.jar -s -check <predict.csv> <check.csv> 
		 * 
		 * Running in AWS cluster:
		 * We suggest you use the setup method here:
		 * https://piazza.com/class/i4ppp5auv1n7k5?cid=94
		 * And when you come to the step where you need to provide arguments for 
		 * running our jar:
		 * (1) -c -learn <train.csv> <path_to_your_s3_bucket>
		 *     (So here train.csv is a relative path starting from your bucket)
		 * (2) -c -predict <model.m> <predict.csv> <path_to_your_s3_bucket>
		 * (3) -c -check <predict.csv> <check.csv> <path_to_your_s3_bucket>
		 * 
		 * PLASE NOTE THAT: <path_to_your_s3_bucket> must end with a "/"
		 */
		
		if (args[0].equals("-s")) {
			// Running standalone
			if (args[1].equals("-learn")) {
				long start = System.currentTimeMillis();
				if (args.length != 3) {
					printUsageError();
					System.exit(1);
				}
				
				// If model.m exists, delete it:
				File modelFile = new File(mytools.StaticConstants.MODEL_PATH);
				if (modelFile.exists()) {
					modelFile.delete();
				}
				
				String[] preprocessArgs = {mytools.StaticConstants.HEADER_PATH,
						args[2],
						MEAN_OUTPUT_DIR, 
						mytools.StaticConstants.TRAIN_DATA_PATH,
						mytools.StaticConstants.MODEL_TEMP_PATH};
				// Preprocess:
				try {
					PreprocessDriver.main(preprocessArgs);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// Remove the intermediate files:
				File meanOutputDir = new File(MEAN_OUTPUT_DIR);
				for (File fileEntry : meanOutputDir.listFiles()) {
					fileEntry.delete();
				}
				meanOutputDir.delete();
				
				// Combine the result in model.m (because they could be
				// run in several map).
				try {
					rebuildModelAfterPreProcess(
							mytools.StaticConstants.MODEL_PATH,
							mytools.StaticConstants.MODEL_TEMP_PATH, 
							mytools.StaticConstants.HEADER_PATH);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				// Train:
				String[] trainArgs = {mytools.StaticConstants.TRAIN_DATA_PATH,
						mytools.StaticConstants.MODEL_PATH};
				try {
					TrainMain.main(trainArgs);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Remove the generated Training files:
				File trainDir = new File(mytools.StaticConstants.TRAIN_DATA_PATH);
				for (File fileEntry : trainDir.listFiles()) {
					fileEntry.delete();
				}
				trainDir.delete();
				long end = System.currentTimeMillis();
				System.out.println("LEARNING TIME(ms): " + (end-start));
			}
			else if (args[1].equals("-predict")) {
				long start = System.currentTimeMillis();
				// Predict
				if (args.length != 4) {
					printUsageError();
					System.exit(1);
				}

				String[] predictArgs = {
					mytools.StaticConstants.HEADER_PATH, // header
					args[2],                             // model.m
					args[3],                             // predict.csv					
				    "data/predicted-dir"		         // result output file
				};
				try {
					PredictDriver.main(predictArgs);
				} catch (ClassNotFoundException | IOException
						| InterruptedException e) {
					System.out.println("==============================="
							+"\n ERROR IN PREDICT DRIVER\n");
					e.printStackTrace();
				}
				long end = System.currentTimeMillis();
				System.out.println("PREDICT TIME(ms): " + (end-start));
				
			}
			else if (args[1].equals("-check")) {
				long start = System.currentTimeMillis();
				if (args.length != 4) {
					printUsageError();
					System.exit(1);
				}
				String[] checkArgs = {args[2], args[3], "log"};
				try {
					MainCheck.main(checkArgs);
				} catch (IOException e) {
					System.out.println("====================="
							+ "Check Failed==============");
					e.printStackTrace();
				}
				long end = System.currentTimeMillis();
				System.out.println("CHECK TIME(ms): " + (end-start));
			}
			else printUsageError();
		}
		else if (args[0].equals("-c")) {
			// AWS cluster
			if (args[1].equals("-learn")) {
				if (args.length != 4 || (!args[3].endsWith("/"))) {
					printUsageError();
					System.exit(1);
				}
				
				String s3bucketPath = args[3];							
				String[] preprocessArgs = {
						s3bucketPath + mytools.StaticConstants.HEADER_PATH,
						s3bucketPath + args[2],
						s3bucketPath + MEAN_OUTPUT_DIR, 
						s3bucketPath + mytools.StaticConstants.TRAIN_DATA_PATH,
						s3bucketPath + mytools.StaticConstants.MODEL_TEMP_PATH};
				// Preprocess:
				try {
					PreprocessDriver.main(preprocessArgs);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
							
				// Combine the result in model.m (because they could be
				// run in several map).
				try {
					rebuildModelAfterPreProcess(
							s3bucketPath + mytools.StaticConstants.MODEL_PATH,
							s3bucketPath + mytools.StaticConstants.MODEL_TEMP_PATH, 
							s3bucketPath + mytools.StaticConstants.HEADER_PATH);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
							
				// Train:
				String[] trainArgs = {
						s3bucketPath + mytools.StaticConstants.TRAIN_DATA_PATH,
						s3bucketPath + mytools.StaticConstants.MODEL_PATH};
				try {
					TrainMain.main(trainArgs);
				} catch (Exception e) {
					e.printStackTrace();
				}
							
			}
			else if (args[1].equals("-predict")) {
				// Predict
				if (args.length != 5 || (!args[4].endsWith("/"))) {
					printUsageError();
					System.exit(1);
				}
				String s3bucketPath = args[4];
				String[] predictArgs = {
				    s3bucketPath + mytools.StaticConstants.HEADER_PATH, // header
				    s3bucketPath + args[2],                             // model.m
				    s3bucketPath + args[3],                             // predict.csv
				    s3bucketPath + "data/predicted-dir"		         // result output file
				};
				try {
					PredictDriver.main(predictArgs);
				} catch (ClassNotFoundException | IOException
						| InterruptedException e) {
					System.out.println("==============================="
							+"\n ERROR IN PREDICT DRIVER\n");
					e.printStackTrace();
				}			
			}
			else if (args[1].equals("-check")) {
				if (args.length != 5) {
					printUsageError();
					System.exit(1);;
				}
				String[] checkArgs = {args[2], args[3], args[4] + "log"};
				try {
					MainCheck.main(checkArgs);
				} catch (IOException e) {
					System.out.println("====================="
							+ "Check Failed==============");
					e.printStackTrace();
				}
			}
			else printUsageError();
		}
		else printUsageError();
	}
	
	public static void printUsageError() {
		System.out.println("Usage:"
				+ "Running on your machine(may provide relative path):\n"
				+ "(1)hadoop jar MapReduce_A3.jar -s -learn <train.csv>\n"
				+ "(2)hadoop jar MapReduce_A3.jar -s -predict <model.m> <predict.csv>\n"
				+ "(3)hadoop jar MapReduce_A3.jar -s -check <predict.csv> <check.csv> \n"
				+ "\nRunning in AWS cluster:\n"
				+ "We suggest you use the setup method here:\n"
				+ "https://piazza.com/class/i4ppp5auv1n7k5?cid=94\n"
				+ "And when you come to the step where you need to provide "
				+ "arguments for running our jar:\n"
				+ "(1) -c -learn <train.csv> <path_to_your_s3_bucket>\n"
				+ "(So here train.csv is a relative path starting from your bucket)\n"
				+ "(2) -c -predict <model.m> <predict.csv> <path_to_your_s3_bucket>\n"
				+ "(3) -c -check <predict.csv> <check.csv> <path_to_your_s3_bucket>\n"
				+ "\nPLASE NOTE THAT: <path_to_your_s3_bucket> must end with a \"/\"\n");
	}
	
	public static void rebuildModelAfterPreProcess(String modelPath, 
			String modelTempPath,
			String headerPath) throws IOException {
		PrintWriter modelWrite = new PrintWriter(modelPath, "UTF-8");
		
		System.out.println("===========model.m CREATED================");
		BufferedReader tempModelRead = new BufferedReader(new FileReader(modelTempPath));
		BufferedReader headerRead = new BufferedReader(new FileReader(headerPath));
		
		final String CLASS_LABEL = "ArrDel15";
		
		ArrayList<FeatureInfo> features = new ArrayList<FeatureInfo>();
		int trueCount = 0;
		int falseCount = 0;
		int allCount = 0;
		
		
		String line = null;
		while ((line = headerRead.readLine())!=null) {
			String[] content = line.split(",");
			if (content[1].equals("n")) {
				//numeric
				features.add(new FeatureInfo(content[0],
						mytools.StaticConstants.REAL_MARK, null));
			}
			else if (content[1].equals("c")) {
				if (content[0].equals(CLASS_LABEL)) {
					features.add(new FeatureInfo(content[0], 
							mytools.StaticConstants.CLASS_LABEL_NAME, "{0,1}"));
				}
				else {
					features.add(new FeatureInfo(content[0], 
							mytools.StaticConstants.NOMINAL_MARK, null));
				}
			}
			// else, it is a feature to be removed.
		}
		headerRead.close();
		
		System.out.println("============HEADER READ IN COMBINING=============");
		
		// Process the temp model file:
		line = null;
		while ((line=tempModelRead.readLine()) != null) {
			String[] content = line.split(" ");
			if (content[0].startsWith(mytools.StaticConstants.ATTRIBUTE_PREFIX)) {
				if (content[2].equals(mytools.StaticConstants.REAL_MARK)) {
					for(int k = 0; k < features.size(); ++k) {
						if (features.get(k).getFeatureName().equals(content[1])) {
							features.get(k).setValue(content[3]);
							System.out.println("=====SET REAL VALUE MEAN====");
						}
					}
				}
			}
			else if (content[0].startsWith(mytools.StaticConstants.RECORD_NUM_PREFIX)) {
				allCount += Integer.parseInt(content[1]);
			}
			else if (content[0].startsWith(mytools.StaticConstants.TRUE_NUM_PREFIX)) {
				trueCount += Integer.parseInt(content[1]);
			}
			else if (content[0].startsWith(mytools.StaticConstants.FALSE_NUM_PREFIX)) {
				falseCount += Integer.parseInt(content[1]);
			}
		}
		
		System.out.println("============TEMP MODEL READ COMPLETE==============");
		
		tempModelRead.close();
		
		// Output the real model.m
		for (int k = 0; k < features.size(); ++k) {
			if (features.get(k).getType().equals(mytools.StaticConstants.NOMINAL_MARK)) {
				modelWrite.println(mytools.StaticConstants.ATTRIBUTE_PREFIX + " "
						+ features.get(k).getFeatureName() + " "
						+ mytools.StaticConstants.NOMINAL_MARK);
			}
			else {
				modelWrite.println(mytools.StaticConstants.ATTRIBUTE_PREFIX + " "
						+ features.get(k).getFeatureName() + " "
						+ features.get(k).getType() + " " 
						+ features.get(k).getValue());
			}
		}
		modelWrite.println();
		modelWrite.println(mytools.StaticConstants.RECORD_NUM_PREFIX
				+ " " + Integer.toString(allCount));
		modelWrite.println(mytools.StaticConstants.TRUE_NUM_PREFIX
				+ " " + Integer.toString(trueCount));
		modelWrite.println(mytools.StaticConstants.FALSE_NUM_PREFIX
				+ " " + Integer.toString(falseCount));
		modelWrite.close();
		
		// Delete the temp model file
		File tempModelFile = new File(modelTempPath);
		tempModelFile.delete();
		
		System.out.println("==============COMBINE model.m COMPLETED============");
	}
}

class FeatureInfo {
	// This class serves to store the feature information we need in model.m
	private String featureName;
	private String type;
	private String value;
	
	public FeatureInfo() {
		this.featureName = null;
		this.type = null;
		this.value = null;
	}
	public FeatureInfo(String featureName, String type, String value) {
		this.featureName = featureName;
		this.type = type;
		this.value = value;
	}
	public String getFeatureName() {
		return featureName;
	}
	public void setFeatureName(String featureName) {
		this.featureName = featureName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}