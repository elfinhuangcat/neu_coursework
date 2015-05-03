package single;

import java.io.IOException;

// Our simple single node library
import mapred_two_nodes.*;

public class MainDriver {
	/**
	 * 
	 * @param args[0] configuration file path
	 * @param args[1] input file path
	 * @param args[2] output dir path
	 * @param args[3] user jar path
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Job job = new Job (new Configuration(args[0]));
		job.setJar(args[3]);
		job.setMapperClass(MyMapper.class);
		job.setReducerClass(MyReducer.class);
		job.setOutputDirPath(args[2]);
		job.setInputFilePath(args[1]);
		job.setOutputKeyClass(String.class);
		job.setOutputValueClass(Double.class);
		job.submit();
		System.out.println("Job complete? " + job.isComplete());
		System.out.println("Job success? " + job.isSuccessful());
	}	
}
