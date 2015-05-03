package check;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
/**
 * To fetch labels from a given file;  
 * 
 * @param inputFile	The file path of a given file;
 * 
 * @param ArrayList<String> temp	An arrayList to store labels we fetched;
 * 
 * @throws FileNotFoundException
 * 
 * @author GANG Liu
 * @data 02-27-2015
 *
 */
public class FetchLabel {
    String inputFile;
    ArrayList<String> temp = new ArrayList<String>();
    
    //Initialization
    public FetchLabel(String inputFile){
        this.inputFile = inputFile;
    }
    
    //Fetching labels for given file
    //and storing the label into an arrayList;
    public void getLabel() throws IOException {
        File f=new File(inputFile);
        BufferedReader br = null;
        String cLine = null;

        try { br = new BufferedReader(new FileReader(f));
            while((cLine = br.readLine()) != null){
                String [] str = cLine.split(",");
                String lable = str[46];
                temp.add(lable);
            }
             br.close();
         }catch (FileNotFoundException e) {
             System.out.println("CAN NOT FIND THE INPUT FILE!");
             System.out.println("==============================");
         }
         }
    
    //Return the label list;
    public ArrayList<String> ToSting(){
        return temp;
    } 
}