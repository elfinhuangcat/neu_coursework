package learn.train;
/**
 * @author Yaxin Huang
 * @created Feb 26, 2015
 */
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class TrainMapper extends
        Mapper<LongWritable, Text, Text, IntWritable> {
	
	private String[] featureList = null;
	@Override
    protected void setup(final Context context) 
    		throws InterruptedException, IOException {
        super.setup(context);
        final Configuration conf = context.getConfiguration();
        
        // Each element is: "<FeatureName>_<FeatureType>"
        featureList = conf.getStrings(PropertyNames.FEATURE_LIST);
    }

    @Override
    public void map(LongWritable key, Text value, Context context) 
    		throws IOException, InterruptedException {
    	/*************************************
    	 * @param key: Line offset
    	 * @param value: Line string
    	 * @param context: the mapper context
    	 */
    	
    	int labelInd = this.getLabelIndex();
    	
    	String[] record = value.toString().split(",");		
		for (int i = 0; i < (record.length-1); ++i) {
			// The last element is "\t"
			// For each feature, record the true count and false count
			String[] featureDetail = featureList[i].split(
			    	PropertyNames.FLIST_DELIMITER);
			    
			if (featureDetail[1].equals(
					mytools.StaticConstants.REAL_MARK)) {
				// Real value:
				double meanOfFeature = context.getConfiguration().getDouble(
						featureDetail[0], 0);
				if (Double.parseDouble(record[i]) < meanOfFeature) {
					// Less than mean:
					String compositekey = (record[labelInd]  //label
							+ PropertyNames.KEY_DELIMITER
							+ featureDetail[0]               //feature name
							+ PropertyNames.KEY_DELIMITER +
							"less");                         // less
					context.write(new Text(compositekey), new IntWritable(1));
				}
				else {
					// More than mean:
					String compositekey = (record[labelInd]  // label
							+ PropertyNames.KEY_DELIMITER
							+ featureDetail[0]               //feature name
							+ PropertyNames.KEY_DELIMITER
							+ "more");                       // more
					context.write(new Text(compositekey), new IntWritable(1));
				}
			}
			else if (featureDetail[1].equals(
					mytools.StaticConstants.NOMINAL_MARK)) {
				// Nominal value:
				String compositekey = (record[labelInd] // label
                        + PropertyNames.KEY_DELIMITER
						+ featureDetail[0]              //feature name
						+ PropertyNames.KEY_DELIMITER
						+ record[i]);               //feature value
				context.write(new Text(compositekey), new IntWritable(1));
			}				
				// Class label doesn't write anything to reducer.				
		}//End of one record
	}//End of records in parser.
    
    public int getLabelIndex() {
    	/****************************************
    	 * @returns the index of the label column
    	 */
    	
    	for (int i = 0; i < this.featureList.length; ++i) {
    		if (this.featureList[i].endsWith(mytools.StaticConstants.CLASS_LABEL_NAME)) {
    			return i;
    		}
    	}
    	// Error in model.m:
    	System.out.println("Cannot find class label.");
    	return Integer.MIN_VALUE;
    }
}