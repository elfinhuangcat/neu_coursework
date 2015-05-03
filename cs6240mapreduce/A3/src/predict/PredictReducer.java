package predict1;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PredictReducer extends
		Reducer<IntWritable, Text, Text, NullWritable> {
	
	@Override
	public void reduce(IntWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
		NullWritable nw = NullWritable.get();
		for(Text value : values) {
			context.write(value, nw);
		}
	}

}
