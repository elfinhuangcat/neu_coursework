package learn.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class AvgMapper extends
		Mapper<LongWritable, Text, IntWritable, IntWritable> {

	protected String filePath = null;
	protected List<Integer> numericIndexList = new ArrayList<Integer>();

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
		filePath = context.getConfiguration().get("headerConf");
		readHeaderConfNumeric(filePath);
	}

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {

		String line = value.toString();
		CSVParser parser = CSVParser.parse(line, CSVFormat.DEFAULT);
		
		//Read csv file line by line
		for (CSVRecord record : parser) {
			String cancleFlg = record.get(47);

			// filer the flight data which is cancelled
			if (cancleFlg.equals("1"))
				return;
			/*
			 * handle numeric feature so that the reduce can calculate the the
			 * mean for each numeric feature
			 */
			for (int index : numericIndexList) {
				//skip missing data
				if(record.get(index).isEmpty()) {
					return;
				}else {
					int content = Integer.parseInt(record.get(index));
					context.write(new IntWritable(index), new IntWritable(content));
				}				
			}
		}
	}

	public void readHeaderConfNumeric(String filePath) {
		File file = new File(filePath);
		String line = null;
		int lineNum = -1;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));

			// read header conf file line by line
			while ((line = br.readLine()) != null) {
				lineNum++;

				String[] lineArr = line.split(",");
				String feature = lineArr[0];
				String type = lineArr[1];

				// classify the column number of nominal feature and numeric
				// feature
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

}

