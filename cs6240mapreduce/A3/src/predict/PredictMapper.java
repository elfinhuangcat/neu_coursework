package predict1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class PredictMapper extends
		Mapper<LongWritable, Text, IntWritable, Text> {

	String probPath = null;
	String hederConfPath = null;

	double mean;
	double probTrue;
	double probFalse;
	double predictYesProb;
	double predictNoProb;
	int lableIndex;
	// readHeaderConf method will assign values to this
	// List<String> featureList = new ArrayList<String>();
	Map<String, String> featureIndexHashMap = new HashMap<String, String>();
	// use to store the tarning prbo
	Map<String, String> modelHashMap = new HashMap<String, String>();

	/*
	 * set model.m file path, read model.m file and store data to hashmap
	 */
	@Override
	protected void setup(Context context) {
		probPath = context.getConfiguration().get("model.m");
		hederConfPath = context.getConfiguration().get("headerConf");
		readHeaderConfGetNeededFeatureAndIndex(hederConfPath);
		readModel(probPath);

		// TEST
		// Set<String> keys1 = featureIndexHashMap.keySet();

		//
		// System.out.println("featureIndexHashMap");
		// for(String k1 : keys1) {
		// System.out.println(k1 + " : " + featureIndexHashMap.get(k1));
		// }
		//
		// System.out.println("modelHashMap");
		// Set<String> keys2 = modelHashMap.keySet();
		// for(String k2 : keys2) {
		// System.out.println(k2 + " : " + modelHashMap.get(k2));
		// }
		// System.out.println("modelHashMap.size" + modelHashMap.size());
	}

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String line = value.toString().trim();
		String[] lineArr = line.split(",");
		CSVParser parser = CSVParser.parse(line, CSVFormat.DEFAULT);
		int recordNum = 0;

		List<Double> targetNoProb = new ArrayList<Double>();
		List<Double> targetYesProb = new ArrayList<Double>();
		String targetFieldName;
		String targetFieldValue;
		String targetIndex;
		String recordStr = "";

		// read column by column except the lable feature and distance feature
		for (CSVRecord record : parser) {
			Set<String> keys = featureIndexHashMap.keySet();

			// get the feature name and value according to our filter rule
			for (String k : keys) {
				targetFieldName = k;
				targetIndex = featureIndexHashMap.get(k);
				targetFieldValue = record.get(Integer.parseInt(targetIndex));

				// TEST
				// System.out.println(targetFieldName + " : "
				// +targetFieldValue);

				// missing data
				if (targetFieldValue.isEmpty())
					targetFieldValue = "0";

				// deal with distance feature
				if (targetFieldName.equals("Distance")) {
					if (Double.parseDouble(targetFieldValue) >= mean)
						targetFieldValue = "more";
					else
						targetFieldValue = "less";
				}

				// add the needed feature to list
				targetNoProb.add(findProb("0", targetFieldName,
						targetFieldValue));
				targetYesProb.add(findProb("1", targetFieldName,
						targetFieldValue));

			}

			// TEST
			// System.out.println("NO list");
			// for (double p : targetNoProb) {
			// System.out.println(p);
			// }
			// System.out.println("YES list");
			// for (double p : targetYesProb) {
			// System.out.println(p);
			// }
			//
			// System.out.println("dis more: " + findProb("0", "Distance",
			// "more"));

			// sum yes and no prob list
			double yesSum = 0;
			double noSum = 0;
			for (int i = 0; i < targetNoProb.size(); i++) {
				noSum += targetNoProb.get(i);
//				System.out.println(targetNoProb.get(i));
			}
			for (int i = 0; i < targetYesProb.size(); i++) {
				yesSum += targetYesProb.get(i);
			}

			// calculation prob
			predictNoProb = noSum + probFalse;
			predictYesProb = yesSum + probTrue;

			// TEST
//			System.out.println("lableIndex " + lableIndex);
//			System.out.println("predictNoProb " + predictNoProb);
//			System.out.println("predictYesProb " + predictYesProb);

			
			//handle output line
//			Iterator iterator = record.iterator();
//			int index = 0;
//			while (iterator.hasNext()) {
//				String str = (String) iterator.next();
//
//				if (index == lableIndex) {
//					//TEST
////					System.out.println("recordStr " + recordStr);
//					
//					if (predictNoProb >= predictYesProb)
//						str = "0";
//					else
//						str = "1";
//				}
//				recordStr += str + ",";
//				index++;
//			}
			
			//handle initial input line
			for(int i = 0; i < lineArr.length; i++) {
				
				//when get the label column, substitute with predicted prob
				if(i == lableIndex+2) {
					//TEST
					System.out.println("recordStr " + recordStr);
					
					if (predictNoProb >= predictYesProb) {
						lineArr[i] = "0";
					} else {
						lineArr[i] = "1";
					}
				}
				
				recordStr += lineArr[i] + ",";
			}
			context.write(new IntWritable(recordNum), new Text(recordStr));
		}
	}

	/*
	 * Read data from the model.m file and store distance mean value and store
	 * the probability of when target feature is true and store the probability
	 * of when target feature is false and store all predicted probability of
	 * each field in a hashmap
	 * 
	 * author: Chenjin date: 3-01-2015
	 * 
	 * @param: the path of model.m file
	 */
	public void readModel(String probPath) {
		FileReader inputFile;
		try {
			inputFile = new FileReader(probPath);
			BufferedReader reader = new BufferedReader(inputFile);
			String line;

			// read file line by line
			while ((line = reader.readLine()) != null) {

				// set distance mean value
				if (line.startsWith("@attribute Distance")) {
					String data[] = line.trim().split(" "); // spilt by space
					mean = Double.parseDouble(data[3]);
				}

				// set target yes probability and no probability
				if (line.startsWith("@prior")) {
					String[] probTrueStr = reader.readLine().trim().split(" "); // spilt
																				// by
																				// space
					probTrue = Double.parseDouble(probTrueStr[1]);

					String[] probFalseStr = reader.readLine().trim().split(" "); // spilt
																					// by
																					// space
					probFalse = Double.parseDouble(probFalseStr[1]);

				}

				// read probability data
				if (line.startsWith("@conditional_probability")) {
					String aLine;

					while ((line = reader.readLine()) != null) {
						String[] data = line.trim().split(" ");
						String mapKey = data[0] + "-" + data[1] + "-" + data[2];
						String mapValue = data[3];
						modelHashMap.put(mapKey, mapValue);
					}
				}
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * read header configuration file and store the name of the features we
	 * needed
	 * 
	 * @param: header configuration file
	 * 
	 * author: Chenjin date: 3-01-2015
	 */
	public void readHeaderConfGetNeededFeatureAndIndex(String filePath) {
		File file = new File(filePath);
		String line = null;
		int lineNum = 0;

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			// read header conf file line by line
			while ((line = br.readLine()) != null) {
				String[] lineArr = line.split(",");
				String feature = lineArr[0];
				String type = lineArr[1];
				if (!type.equals("r")) {
					featureIndexHashMap.put(feature, Integer.toString(lineNum));
				}
				lineNum++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// store lable index and remove it from hash table
		lableIndex = Integer.parseInt(featureIndexHashMap.get("ArrDel15"));

		featureIndexHashMap.remove("ArrDel15");
	}

	/*
	 * find the probability for the given three parts of key
	 * 
	 * @param: a column name
	 * 
	 * author: Chenjin date: 3-01-2015
	 */
	public double findProb(String str1, String str2, String str3) {
		double prob = 0;
		Set<String> keys = modelHashMap.keySet();
		String k = str1 + "-" + str2 + "-" + str3;

		// get keys in hash map
		for (String key : keys) {
			// find target key
			if (key.equals(k)) {
				String probStr = modelHashMap.get(key);
				prob = Double.parseDouble(probStr);
			}
		}
		return prob;
	}
}
