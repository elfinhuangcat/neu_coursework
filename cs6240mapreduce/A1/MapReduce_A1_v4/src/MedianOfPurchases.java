/******************************************************
 * This is A1_v4 and each call to mapper will also
 * compute the Fibonacci of N(input by user as second 
 * argument). The version 4 is a slight modification of 
 * version 2.
 * 
 * The built jar should be kept in the same directory
 * as the input file.
 * Usage:
 * export HADOOP_CLASSPATH=<nameofjar.jar>
 * hadoop MedianOfPurchases <input_data_name> N
 * 
 * (N will be used to compute Fibonacci of N)
 * 
 * This program is designed to run in Linux system.
 ******************************************************/


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class MedianOfPurchases extends Configured implements Tool {
        public static long N = 5;	
	public static void main(String[] args) throws Exception {
        // args[0]: input data path
	// args[1]: N
	if (args[1] != null) {
		N = Long.parseLong(args[1]);
	}
        int res = ToolRunner.run(new Configuration(), new MedianOfPurchases(), args);
        System.exit(res);
    }
	
	public int run(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		// When implementing tool
        Configuration conf = this.getConf();
 
        // Create job
        @SuppressWarnings("deprecation")
		Job job = new Job(conf, "Median Of Purchases");
        job.setJarByClass(MedianOfPurchases.class);
 
        // Setup MapReduce job
        // Do not specify the number of Reducer
        job.setMapperClass(MedianOfPurchasesMapper.class);
		job.setReducerClass(MedianOfPurchasesReducer.class);
 
        // Specify key / value		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
 
        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);
 
        // Output
        FileOutputFormat.setOutputPath(job, new Path("MapReduce_A1_v4/output/"));
        job.setOutputFormatClass(TextOutputFormat.class);

		
		if (job.waitForCompletion(true)) {
			// Exit normally
			long end = System.currentTimeMillis();
			System.out.println("#--END: " + ((double)(end - start))/1000
					+ " seconds.");
			return 0;
		}
		else {
			return 1;
		}
	}
}
