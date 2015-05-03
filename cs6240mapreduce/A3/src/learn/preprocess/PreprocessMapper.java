package learn.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class PreprocessMapper extends
		Mapper<LongWritable, Text, IntWritable, Text> {

	protected String hederConfPath = null;
	protected String avgFilePath = null;
	protected List<Integer> numericIndexList = new ArrayList<Integer>();
	protected List<Integer> nominalIndexList = new ArrayList<Integer>();
	protected List<Integer> indexList = new ArrayList<Integer>();
	protected List<String> feature = new ArrayList<String>();
	protected Map<String, String> hMap = new HashMap<String, String>();
	protected int yesCounter = 0;
	protected int noCounter = 0;
	protected int keyCounter = 0;
	
	/*
	 * read the header configuration file and classify numeric and nominal
	 * feature
	 * 
	 * @author: Chenjin
	 * 
	 * @date: 2-25-2015
	 */
	@Override
	protected void setup(Context context) {
		hederConfPath = context.getConfiguration().get("headerConf");
		avgFilePath = context.getConfiguration().get("avgFile");
		readHeaderConf(hederConfPath);
		readAvgFile(avgFilePath);

		// TEST
		// for(int index : numericIndexList) {
		// System.out.println("numeric: " + index);
		// }
		// for(int index : nominalcIndexList) {
		// System.out.println("nominalc: " + index);
		// }
		//System.out.println(hMap.get("54"));
	}

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		String line = value.toString();
		CSVParser parser = CSVParser.parse(line, CSVFormat.DEFAULT);
		
		
		// Read csv file line by line
		for (CSVRecord record : parser) {
			String cancleFlg = record.get(47);

			// filer the flight data which is cancelled
			if (cancleFlg.equals("1"))
				return;

			// catch the needed column
			indexList = new ArrayList<Integer>(numericIndexList); // copy numeric index
			indexList.addAll(nominalIndexList); // merge numeric index and nominal index											// 
			Collections.sort(indexList); // sort list

			// get target needed column
			String str = "";
			for (int index : indexList) {
				String cloVal = record.get(index);

				if (cloVal.isEmpty()) {
					if(nominalIndexList.contains(index)) { // find out missing nominal feature
						return;
					}
					
					// find out missing numeric feature
					Set<String> keys = hMap.keySet();
					for(String k : keys) {  
						if(Integer.parseInt(k) == index) {
							cloVal = hMap.get(k);    // substitute column value with corresponding mean value
							str = str + cloVal + ",";
						}
					}				
						
				} else { //none empty data field
//					if(record.get(44).equals("1"))  
//						yesCounter++;
//					if(record.get(44).equals("0")) 
//						noCounter++;
					if(index == 44) {
						if(Integer.parseInt(cloVal) == 1) 
							yesCounter++;
						else
							noCounter++;
					}
						
					//combine all columns
					str = str + cloVal + ",";
					}
			}
			context.write(new IntWritable(keyCounter), new Text(str));
			keyCounter++;
		}

	}
	
	/*Use to write the header of model.m,
	 * call at the end of the task
	 * 
	 * @quthor: Chenjin
	 * @date: 3-01-2015
	 */
	@Override
	public void cleanup(Context context) {
		String modelPath = context.getConfiguration().get("modelPath");
		System.out.println("=**************=" + modelPath + "=************8=");
		WriteLog writeLog = new WriteLog(modelPath);
		
		System.out.println("$$$$$$$$$$$CLEAN UP$$$$$$$$$$$$$$$$");
		
		for(int i : indexList) {
			writeLog.setFieldOne("@attribute");
			if(nominalIndexList.contains(i)) {
				
				if(i == 44) {
					writeLog.setFieldTwo("CLASS_LABEL");
					writeLog.setFieldThree("{0,1}");
				} else {	
					writeLog.setFieldTwo(feature.get(i));
					writeLog.setFieldThree("nominal");
					}
				writeLog.setFieldFour("");
				writeLog.log();
			} else {
				writeLog.setFieldTwo(feature.get(i));
				writeLog.setFieldThree("real");
				writeLog.setFieldFour(hMap.get("54"));
				writeLog.log();
			}			
		}
		
		//write others
		writeLog.setFieldFour("");
		
		writeLog.setFieldOne("@record_num");
		writeLog.setFieldTwo(Integer.toString(keyCounter));
		writeLog.setFieldThree("");
		writeLog.log();
		
		writeLog.setFieldOne("@true_num");
		writeLog.setFieldTwo(Integer.toString(yesCounter));
		writeLog.setFieldThree("");
		writeLog.log();
		
		writeLog.setFieldOne("@false_num");
		writeLog.setFieldTwo(Integer.toString(noCounter));
		writeLog.setFieldThree("");
		writeLog.log();
		
	}

	public void readHeaderConf(String filePath) {
		File file = new File(filePath);
		String line = null;
		int lineNum = -1;

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			// read header conf file line by line
			while ((line = br.readLine()) != null) {
				lineNum++;

				String[] lineArr = line.split(",");
				feature.add(lineArr[0]);
				String type = lineArr[1];

				// classify the column number of nominal feature and numeric
				// feature
				if (type.equals("c"))
					nominalIndexList.add(lineNum);
				if (type.equals("n"))
					numericIndexList.add(lineNum);
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
	 * Read the mean file computed by the first MR and store the value to hash
	 * map
	 */
	public void readAvgFile(String avgFilePath) {
		File file = new File(avgFilePath);
		String line = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			// read line by line, and store the column number and its mean into
			// hashmap
			while ((line = br.readLine()) != null) {
				String[] data = line.trim().split("	");
				hMap.put(data[0], data[1]);
				//System.out.println(hMap.toString());
			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
