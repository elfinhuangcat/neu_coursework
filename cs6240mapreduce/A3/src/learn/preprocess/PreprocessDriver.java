package learn.preprocess;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*************************************
 * @param: the header configuration file
 * @param: input data file
 * @param: mean data output file directory
 * @param: our final data output file
 * @param: model.temp path
 * @author: Chenjin
 * @date: 2-25-2015
 */
public class PreprocessDriver {

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		System.out.println(Arrays.toString(args)); 		
		if (args.length != 5) {
			System.err.println("Incorrect number of arguments.");
			System.exit(1);
		}
		
		
		Configuration conf = new Configuration();
		
		//set param pass to mapper
		conf.set("headerConf", args[0]);
		conf.set("avgFile", args[2] + "/part-r-00000");
		
		//Job1 configuration, job1 computer mean of numeric column 
		Job job1 = new Job(conf, "average");

		job1.setJarByClass(PreprocessDriver.class);

		job1.setOutputKeyClass(IntWritable.class);
		job1.setOutputValueClass(IntWritable.class);

    	job1.setMapperClass(AvgMapper.class);
		job1.setReducerClass(AvgReducer.class);

		FileInputFormat.addInputPath(job1, new Path(args[1]));
		FileOutputFormat.setOutputPath(job1, new Path(args[2]));
		
	    System.out.println("Starting Job 1");
	    job1.waitForCompletion(true);

		//Job2 configuration
	    conf.set("modelPath", args[4]);
		Job job2 = new Job(conf, "preprocess");	
		job2.setJarByClass(PreprocessDriver.class);

		job2.setOutputKeyClass(IntWritable.class);
		job2.setOutputValueClass(Text.class);

		job2.setMapperClass(PreprocessMapper.class);
		job2.setReducerClass(PreprocessReducer.class);

		FileInputFormat.addInputPath(job2, new Path(args[1]));
		FileOutputFormat.setOutputPath(job2, new Path(args[3]));
		
	    System.out.println("Starting Job 2");
	    job2.waitForCompletion(true);
	}
}
