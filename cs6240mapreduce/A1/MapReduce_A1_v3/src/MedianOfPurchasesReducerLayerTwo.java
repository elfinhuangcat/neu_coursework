import java.util.ArrayList;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MedianOfPurchasesReducerLayerTwo
    extends Reducer<Text, DoubleWritable, Text, DoubleWritable>{
	
	public void reduce(Text key, 
			Iterable<DoubleWritable> values, 
	        Context context) 
	        throws IOException, InterruptedException {
		/*****************************************************
		 * This method takes a category name and the corresponding
		 * sorted price list to compute the median of the value list.
		 * The result will be written to the context as 
		 * category-price pair.
		 * 
		 * Given: <Text> key - name of current category
		 *        <Iterable<DoubleWritable>> values 
		 *        - An Iterable over the list of prices.
		 *        <Context> context - stores the result.
		 * Returns: void
		 *****************************************************/
		// Copy the Iterable values to a list, to facilitate the median 
		// computation:
		ArrayList<Double> list = new ArrayList<Double>();
		for (DoubleWritable value : values) {
    		list.add(value.get());
    	}
		// The median of value list:
		DoubleWritable median = new DoubleWritable(medianOfSortedList(list));
		// Write to the context:
		context.write(key, median);
	}
	
	public Double medianOfSortedList(ArrayList<Double> list) {
    	/*************************************************
    	 * This method returns the median of the given list.
    	 * 
    	 * Given: <LinkedList<Double>> list
    	 *        - A list of SORTED Double values.
    	 * Return: <Double> - The median of the given list.
    	 *************************************************/
    	// 1. Sort the given list
    	// Collections.sort(list);
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