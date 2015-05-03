package predict1;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
 * @param 0: the header configuration file
 * @param 1: model.m file path
 * @param 2: input path of initial data file
 * @param 3: result output file path
 * 
 * @author: Chenjin Hou
 * @date: 3-02-2015
 */

public class PredictDriver {
	// argument I will use in mapper, like model, header file

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		if (args.length != 4) {
			System.err.println("Incorrect number of arguments.");
			System.exit(-1);
		}
		System.out.println(Arrays.toString(args));

		Configuration conf = new Configuration();

		// set param pass to mapper
		conf.set("headerConf", args[0]);
		conf.set("model.m", args[1]);

		Job jobForPredict = new Job(conf, "predict");
		jobForPredict.setJarByClass(PredictDriver.class);

		FileInputFormat.addInputPath(jobForPredict, new Path(args[2]));
		FileOutputFormat.setOutputPath(jobForPredict, new Path(args[3]));

		jobForPredict.setMapperClass(PredictMapper.class);
		jobForPredict.setReducerClass(PredictReducer.class);

		jobForPredict.setOutputKeyClass(IntWritable.class);
		jobForPredict.setOutputValueClass(Text.class);

		System.out.println("Starting predict job");
		jobForPredict.waitForCompletion(true);

	}

}
