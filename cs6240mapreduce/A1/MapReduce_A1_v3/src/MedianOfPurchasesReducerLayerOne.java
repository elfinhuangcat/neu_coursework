import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MedianOfPurchasesReducerLayerOne
    extends Reducer<TupleWritable, DoubleWritable, Text, DoubleWritable> {
    
    @Override
    public void reduce(TupleWritable key, Iterable<DoubleWritable> values, 
        Context context) 
        throws IOException, InterruptedException {
    	/*************************************************
    	 * This method takes a category&price-num pair from the
    	 * result computed by mapper and decomposes the pair by
    	 * writing multiple category-price pairs to the context.
    	 * 
    	 * Given: <Text> key - Name of category
    	 *        <Iterable<DoubleWritable>> values
    	 *        - the sale prices of the given category
    	 *        <Context> context - The final result will be
    	 *                            retrieved here.
    	 * Returns: void.
    	 *************************************************/
        // Decompose the category-pairs and write them to context:
    	context.write(key.getCategory(), key.getSale());
    }   
}