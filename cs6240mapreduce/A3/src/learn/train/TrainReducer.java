package learn.train;
/**
 * @author Yaxin Huang
 * @created Feb 26, 2015
 */
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;


public class TrainReducer extends
        Reducer<Text, IntWritable, Text, DoubleWritable> {
    private int TRUE_COUNT = 0;
    private int FALSE_COUNT = 0;
    @Override
    protected void setup(final Context context) 
    		throws IOException, InterruptedException {
        super.setup(context);
        final Configuration conf = context.getConfiguration();
        TRUE_COUNT = conf.getInt(PropertyNames.TRUE_NUM, 0);
        FALSE_COUNT = conf.getInt(PropertyNames.FALSE_NUM, 0);
    }

    @Override
    public void reduce(Text key, Iterable<IntWritable> values, Context context)
            throws IOException, InterruptedException {
    	/********************************************************
    	 * @param key: String "<label> <feature_name> <feature_val>"
    	 * @param values: A list of ones. Serve to count the occurrence.
    	 * @param context: The reducer context.
    	 */
    	double count = 0;
    	for (IntWritable value : values) {
    		count += (double)value.get();
    	}
    	if (key.toString().split(PropertyNames.KEY_DELIMITER)[0].equals(
    			PropertyNames.TRUE_LABEL)) {
    		context.write(key, new DoubleWritable(Math.log(count/TRUE_COUNT)));
    	}
    	else {
    		context.write(key, new DoubleWritable(Math.log(count/FALSE_COUNT)));
    	}
    }
}