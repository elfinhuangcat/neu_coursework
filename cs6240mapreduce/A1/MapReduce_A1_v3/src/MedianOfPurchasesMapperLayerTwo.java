import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MedianOfPurchasesMapperLayerTwo 
    extends Mapper<LongWritable, Text, Text, DoubleWritable> {
	// Class Mapper<KEYIN,VALUEIN,KEYOUT,VALUEOUT>
	
	public void map(LongWritable key, Text value, Context context) 
	    throws IOException, InterruptedException {
		/*************************************************
    	 * This method takes the input <key,value> pairs and 
    	 * returns category-price pair. 
    	 * 
    	 * Given: <LongWritable> key - The offset of the current
    	 *                             line within input file.
    	 *        <Text> value - The text content of the current
    	 *                       line.
    	 *        <Context> context - The reducer will then read
    	 *                            category-price pairs from it.
    	 * Returns: void.
    	 *************************************************/
		String[] line = value.toString().split("\t");
		context.write(new Text(line[0]), 
				new DoubleWritable(Double.parseDouble(line[1])));
	}
}