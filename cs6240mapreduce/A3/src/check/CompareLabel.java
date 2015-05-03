package check;

import java.io.IOException;
import java.util.ArrayList;
/**
 * Comparing all labels between the check file and predict file, and calculating 
 * how many flights we predict correctly;  
 * 
 * @param input_File_Path1	A file path related to the file predicted by a Predict_Model.
 * @param input_File_Path2	The file path for a given file having all accuracy info; 
 * 
 * @param ArrayList<String> tem1	An ArrayList to store the label fetching from a predict file;
 * @param ArrayList<String> tem2	An ArrayList to store the label fetching from a check file;
 * @param lines						The total number of flights info;
 * @param predict_accuracy			Accuracy rate; 
 * @param trueInstance		The number of labels the Model predicted correctly;
 * @param falseInstance		The number of labels the Model predicted incorrectly;
 * 
 * @throws ArithmeticException
 * @throws IndexOutOfBoundsException
 * 
 * @author GANG Liu
 * @data 02-27-2015
 *
 */

public class CompareLabel{
	//set input file path and arguments 
        String input_File_Path1;
        String input_File_Path2;
        ArrayList<String> tem1 = new ArrayList<String>();
        ArrayList<String> tem2 = new ArrayList<String>();
        int trueInstance;
        int falseInstance;
        int lines;
        double predict_accuracy;
        
        //Initialization
        public CompareLabel(String input_File_Path1, String input_File_Path2){
        	this.input_File_Path1 = input_File_Path1;
        	this.input_File_Path2 = input_File_Path2;
        }

        //comparing all labels between the two given files 
        //and recording how many flights we predicted correctly 
        //and how many flights are not;
        public void comLab() throws IOException{
            FetchLabel ppc1 = new FetchLabel(input_File_Path1);
            FetchLabel ppc2 = new FetchLabel(input_File_Path2);
    		ppc1.getLabel();
    		ppc2.getLabel();
    		tem1 = ppc1.ToSting();
    		tem2 = ppc2.ToSting();
      
            lines = tem1.size();
            try{
    	        for(int k=0; k<lines; k++){
    	            if(tem1.get(k).equals(tem2.get(k))){
    	                trueInstance++;
    	            }else falseInstance++;
    	        }
    	        predict_accuracy = ((double)trueInstance/(double)(lines))*100;
         
            }catch(ArithmeticException e){
                e.printStackTrace();
            }catch(IndexOutOfBoundsException ie){
            	System.out.println("INDEX OUT OF BOUNDARY, PLEASE CHECK THE INPUT PATH");
                System.out.println("==============================");
            }
        }
        
        // return the number of flights info the Model predicted correctly
        public int getCorrectInfo(){
        	return trueInstance;
        }
        
        //return the number of flights info the Model predicted incorrectly
        public int getInCorrectInfo(){
        	return falseInstance;
        }
	    
        //return the accuracy rate the Model reach out
        public double getAccuracyInfo(){
        	return predict_accuracy;
        }
    }