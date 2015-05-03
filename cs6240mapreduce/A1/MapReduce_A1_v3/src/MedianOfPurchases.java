/***************************************************************
 * This is A1_v3. This version uses MapReduce to calculate the
 * median of each category's sale prices. Different from v2, 
 * version 3 uses (category,salePrice) as a composite key to
 * let the shuffling process do the sorting job. 
 * 
 * Usage: 
 * export HADOOP_CLASSPATH=<NAME_OF_JAR>
 * hadoop MedianOfPurchases <input_data_file_name>
 * 
 * The built jar should be kept in the same folder as the input
 * data file which should be named as "purchases4.txt".
 * 
 * This program is designed to run in Linux system.
 ***************************************************************/


import java.io.File;

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
	
	public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new MedianOfPurchases(), args);
        System.exit(res);
    }
	
	public int run(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		// When implementing tool
        Configuration conf = this.getConf();
 
        // Create job
        @SuppressWarnings("deprecation")
		Job job = new Job(conf, "Median Of Purchases v3 Phase 1");
        job.setJarByClass(MedianOfPurchases.class);
 
        // Setup MapReduce job
        // Do not specify the number of Reducer
        job.setMapperClass(MedianOfPurchasesMapper.class);
		job.setReducerClass(MedianOfPurchasesReducerLayerOne.class);
 
        // Specify key / value		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(DoubleWritable.class);
		job.setMapOutputKeyClass(TupleWritable.class);
		job.setMapOutputValueClass(DoubleWritable.class);
 
        // Input
        FileInputFormat.addInputPath(job, new Path(args[0]));
        job.setInputFormatClass(TextInputFormat.class);
 
        // Output
        FileOutputFormat.setOutputPath(job, new Path("MapReduce_A1_v3/temp_output/"));
        job.setOutputFormatClass(TextOutputFormat.class);

		
		if (job.waitForCompletion(true)) {
			// Exit normally
			@SuppressWarnings("deprecation")
			Job secondJob = new Job();
			secondJob.setJarByClass(MedianOfPurchases.class);
			secondJob.setJobName("Median of Purchases v3 Phase 2");
			
			FileInputFormat.addInputPath(secondJob, 
					new Path("MapReduce_A1_v3/temp_output/part-r-00000"));
			FileOutputFormat.setOutputPath(secondJob, 
					new Path("MapReduce_A1_v3/output"));
			
			secondJob.setMapperClass(MedianOfPurchasesMapperLayerTwo.class);
			secondJob.setReducerClass(MedianOfPurchasesReducerLayerTwo.class);
			
			secondJob.setOutputKeyClass(Text.class);
			secondJob.setOutputValueClass(DoubleWritable.class);
			
			if (secondJob.waitForCompletion(true)) {
				// Exit normally
				long end = System.currentTimeMillis();
				System.out.println("#--END: " + ((double)(end - start))/1000
						+ " seconds.");
				// Delete intermediate file:
				File intermediateFile = new File("MapReduce_A1_v3/temp_output/part-r-00000");
				intermediateFile.delete();
                                File intermediateSuccess = new File("MapReduce_A1_v3/temp_output/_SUCCESS");
                                intermediateSuccess.delete();
				// Delete intermediate folder:
				File intermediateDirectory = new File("MapReduce_A1_v3/temp_output/");
				intermediateDirectory.delete();
				
				return 0;
			}
			else {
				return 1;
			}
		}
		else {
			return 1;
		}
	}
}
