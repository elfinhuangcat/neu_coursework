import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MedianOfPurchasesReducer
    extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
    
    @Override
    public void reduce(Text key, Iterable<DoubleWritable> values, 
        Context context) 
        throws IOException, InterruptedException {
    	/*************************************************
    	 * This method takes a category-prices pair from the
    	 * result computed by mapper and write the result 
    	 * category-median_of_prices pair to the context.
    	 * 
    	 * Given: <Text> key - Name of category
    	 *        <Iterable<DoubleWritable>> values
    	 *        - the sale prices of the given category
    	 *        <Context> context - The final result will be
    	 *                            retrieved here.
    	 * Returns: void.
    	 *************************************************/
    	
    	// Copy the Iterable values to a list to facilitate soring.
    	LinkedList<Double> list = new LinkedList<Double>();
    	for (DoubleWritable value : values) {
    		list.add(value.get());
    	}
    	// Compute the median of the list
    	Double median = medianOfList(list);
    	
    	// Write the result category-median pair to the context:
    	context.write(key, new DoubleWritable(median));
    }
    
    public Double medianOfList(LinkedList<Double> list) {
    	/*************************************************
    	 * This method returns the median of the given list.
    	 * 
    	 * Given: <LinkedList<Double>> list
    	 *        - A list of Double values.
    	 * Return: <Double> - The median of the given list.
    	 *************************************************/
    	// 1. Sort the given list
    	Collections.sort(list);
    	// 2. size is even or odd?
    	if (list.size() % 2 == 1) {
    		Double median = list.get((int)(list.size()/2));
    		return median;
    	}
    	else {
    		Double median = (list.get((int)(list.size()/2)) 
    		    + list.get((int)(list.size()/2) - 1)) / 2;
    		return median;
    	}
    }
}