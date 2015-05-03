package main;
/****************************************************
 * This is the file defining the Mapper class.
 * @author Yaxin Huang
 */
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MedianOfPurchasesMapper 
    extends Mapper<LongWritable, Text, Text, DoubleWritable> {
    // Class Mapper<KEYIN,VALUEIN,KEYOUT,VALUEOUT>    
    
    @Override
    public void map(LongWritable key, Text value, Context context) 
        throws IOException, InterruptedException {
        /*************************************************
         * This method takes the input <key,value> pairs and 
         * returns <category,sale> pairs for the reduce function
         * to deal with.
         * 
         * Given: <LongWritable> key - The offset of the current
         *                             line within input file.
         *        <Text> value - The text content of the current
         *                       line.
         *        <Context> context - The reducer will then read
         *                            key-values pairs from it.
         * Returns: void.
         *************************************************/
        
        String line = value.toString();
        String[] contents = line.split("\t");
        if (contents.length != 6) {
            System.out.println("Skip: " + line + "\n[Column != 6]");
        }
        else {

            try {
                double sale = Double.parseDouble(contents[4]);
                context.write(new Text(contents[3]), 
                        new DoubleWritable(sale));
            }
            catch (NullPointerException ne) {
                System.out.println("Skip: " + line + "\n[Sale is not"
                        + " a number]");
            }
            catch (NumberFormatException nfe) {
                System.out.println("Skip: " + line + "\n[Sale is not"
                        + " a number]");
            }
        }
    }
}