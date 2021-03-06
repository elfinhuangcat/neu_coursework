import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MedianOfPurchasesMapper 
    extends Mapper<LongWritable, Text, TupleWritable, DoubleWritable> {
	// Class Mapper<KEYIN,VALUEIN,KEYOUT,VALUEOUT>    
    
    @Override
    public void map(LongWritable key, Text value, Context context) 
        throws IOException, InterruptedException {
    	/*************************************************
    	 * This method takes the input <key,value> pairs and 
    	 * returns <(category,price),char> pair. This can avoid 
    	 * sorting again in the reducer because the shuffle function
    	 * will sort the key pairs before handing it over to 
    	 * reducer.
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
        	System.out.println("Omit because of info not complete: \n" 
                + "\t" + line);
        }
        else {
        	if (contents[3].equals("Category") 
        			&& contents[4].equals("Sale")) {
        		return; // Omit the column names line.
        	}
        	else {
        		try {
    	            double sale = Double.parseDouble(contents[4]);
    	            context.write(new TupleWritable(contents[3], sale), 
    	            		      new DoubleWritable(0));
    	    	}
    	    	catch (NullPointerException ne) {
    	    		System.out.println("Omit because Sale is not" + 
    	    	        "a number: \n" + "\t" + line);
    	    	}
    	    	catch (NumberFormatException nfe) {
    	    		System.out.println("Omit because Sale is not" + 
        	    	        "a number: \n" + "\t" + line);
    	    	}
        	}
        }
    }
}