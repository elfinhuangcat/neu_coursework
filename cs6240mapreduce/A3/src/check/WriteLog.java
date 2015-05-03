package check;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
/**
 * To write predicting info. into a Log file and pint it through console at same time;   
 * 
 * @param outPath	A file path for LOG file to track predicting details 
 * 
 * @param accuracyInfo		Accuracy rate; 
 * @param correctInfo		The number of labels the Model predicted correctly;
 * @param inCorrectInfo		The number of labels the Model predicted correctly;
 * 
 * @throws Exception
 * 
 * @author GANG Liu
 * @data 02-27-2015
 *
 */
public class WriteLog{
	String outPath;
	int correctInfo;
	int inCorrectInfo;
	double accuracyInfo;
	
	//Initialization
	protected WriteLog(String outPath, int correctInfo, int inCorrectInfo
			,double accuracyInfo){
		this.outPath=outPath;
		this.correctInfo=correctInfo;
		this.inCorrectInfo=inCorrectInfo;
		this.accuracyInfo=accuracyInfo;
	}
	
	//Write all info. into a Log file
	public void log(){
	    try {
	        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter
	        		(new FileOutputStream(outPath, false)));
	        bw.write("Correctly Labeled Instances: "+ correctInfo);
	        bw.newLine();
	        bw.write("Incorrectly Labeled Instances: "+ inCorrectInfo);
	        bw.newLine();
	        bw.write("Accuracy: "+ accuracyInfo +"%");
	        bw.close();
	    } catch (Exception e) {
	    	System.out.println("CAN NOT WRITE DOWN RECORDES INTO LOG FILE, "
	    			+ "PLEASE DOUBLE CHECK YOUR OUTPUT FILE PATH");
	    	System.out.println("==============================");
	    }
	}
	
	//Pint info.
	public void print(){
        System.out.println("Correctly Labeled Instances: "+ correctInfo);
        System.out.println("Incorrectly Labeled Instances: "+ inCorrectInfo);
        System.out.println("Accuracy: "+ accuracyInfo +"%");
	}

}