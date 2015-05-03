package check;

import java.io.IOException;
/**
 * Check the accuracy of the file predicted by a Predict_Model.
 * 
 * @param predict_File_Path	A file path related to the file predicted by a Predict_Model.
 * @param check_File_Path	The file path for a given file having all accuracy info. 
 * @param log_File_Path		A file path for LOG file to track predicting details 
 * 
 * @param trueInstance		The number of flight's info. without delaying;
 * @param falseInstance		The number of flight's info having a delay;
 * @param accuracy			The accuracy rate that the Predict_Model can reach out;
 * 
 * @throws IOException
 * 
 * @author GANG Liu
 * @data 02-27-2015
 *
 */


public class MainCheck{
    public static void main(String[] args) throws IOException{
    	//set input and output path;
        String predict_File_Path=args[0];
        String check_File_Path=args[1];
        String outputFilePath=args[2];
        
        int trueInstance;
        int falseInstance;
        double accuracy ;
        
        //invoking CompareLable Class to track comparing info
        CompareLabel cl = new CompareLabel(predict_File_Path, check_File_Path);
        cl.comLab();
        trueInstance = cl.getCorrectInfo();
        falseInstance = cl.getInCorrectInfo();
        accuracy = cl.getAccuracyInfo();
        
        //invoking writeToLog Class to write down comparing info into a Log file
        // and print it through console
		WriteLog writeToLog = new WriteLog(outputFilePath, trueInstance, falseInstance,  accuracy);
        writeToLog.log();
        writeToLog.print();
    }
}