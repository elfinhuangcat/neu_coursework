package learn.preprocess;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class AvgReducer extends
		Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {
	
	@Override
	public void reduce(IntWritable key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
		
		/*
		 * calculate mean
		 */
		int sum = 0;
		int count = 0;
		for(IntWritable val : values) {
			sum += val.get();
			count++;
		}
		
		IntWritable avg = new IntWritable(sum/count);
		context.write(key, avg);
	}

}

