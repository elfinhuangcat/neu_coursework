package main;
/*******************************************
 * This is the file defining the Reducer class.
 * @author Yaxin Huang
 */
import java.io.IOException;
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
         * @author Yaxin Huang
         *************************************************/
        
        // Copy the Iterable values to a list to facilitate soring.
        LinkedList<Double> list = new LinkedList<Double>();
        for (DoubleWritable value : values) {
            list.add(value.get());
        }
        
        // Write the result category-median pair to the context:
        context.write(key, 
                // Median:
                new DoubleWritable(new Median().medianOfList(list)));
    }
}