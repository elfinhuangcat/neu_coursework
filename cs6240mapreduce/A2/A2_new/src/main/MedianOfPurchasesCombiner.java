package main;
/*******************************************
 * This is the file defining the Combiner class.
 * @author Yaxin Huang
 */
import java.io.IOException;
import java.util.LinkedList;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MedianOfPurchasesCombiner
    extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {
    
    @Override
    public void reduce(Text key, Iterable<DoubleWritable> values, 
        Context context) 
        throws IOException, InterruptedException {
        /*************************************************
         * The combiner computes the intermediate medians.
         * 
         * Given: <Text> key - Name of category
         *        <Iterable<DoubleWritable>> values
         *        - the sale prices of the given category
         *        <Context> context - The intermediates will be
         *                            written to context.
         * Returns: void.
         *************************************************/
        
        // Copy the Iterable values to a list to facilitate sorting.
        LinkedList<Double> list = new LinkedList<Double>();
        for (DoubleWritable value : values) {
            list.add(value.get());
            if (list.size() % MedianOfPurchases.binSize 
                    == (MedianOfPurchases.binSize - 1)) {
                // If the list contains enough elements (decided by binSize)
                // then compute the intermediate median and write to the 
                // context.
                context.write(key, 
                        // median: 
                        new DoubleWritable(new Median().medianOfList(list)));
                // Empty the list:
                list.clear();
            }
        }
        
        if (list.size() > 0) {
            // The last intermediate median:
            context.write(key, 
                    // median:
                    new DoubleWritable(new Median().medianOfList(list)));
            // Empty the list:
            list.clear();
        }
    }
    
    
}
