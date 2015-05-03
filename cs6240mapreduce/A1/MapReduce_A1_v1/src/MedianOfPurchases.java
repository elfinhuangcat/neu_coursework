/*******************************************************
* MapReduce A1 - v1
* 
* The built jar should be run in the same folder as the
* input file and the input file is named "purchases4.txt".
* 
* This program takes in an input file which is specified 
* in the argument and calculates the median of purchases 
* for each category of transaction.
* 
* Usage: 
* java -jar <name_of_JAR> <input_file_name>
* 
* This program is targeted to Linux system.
* 
* The Input file is a tab separated, 6 columns text file.
* Here is the head of an example input:
Date            time    City            Category    Sale    CC
2012-01-01    09:00    San Jose    Men's Clothing    214.05    Amex
2012-01-01    09:00    Fort Worth    Women's Clothing    153.57    Visa
2012-01-01    09:00    San Diego    Music    66.08    Cash
2012-01-01    09:00    Pittsburgh    Pet Supplies    493.51    Discover
2012-01-01    09:00    Omaha    Children's Clothing    235.63    MasterCard
2012-01-01    09:00    Stockton    Men's Clothing    247.18    MasterCard
2012-01-01    09:00    Austin    Cameras    379.6    Visa
2012-01-01    09:00    New York    Consumer Electronics    296.8    Cash
2012-01-01    09:00    Corpus Christi    Toys    25.38    Discover

* Input file format:
* Column 1 - Date: (String) yyyy-mm-dd
* Column 2 - time: (String) hh:mm
* Column 3 - City: (String) The name of a city
* Column 4 - Category: (String) free form text
* Column 5 - Sale: (Double, in Dollar) 
* Column 6 - CC: (String) Name of the credit card
* 
* Author: Yaxin Huang
********************************************************/
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;

public class MedianOfPurchases {
    public static void main(String[] args) throws IOException {
        //     args[0]: input file path

    	String outputFilePath = "output_v1.txt";
    	long start = System.currentTimeMillis();
    	new MedianOfPurchases().run(args[0], outputFilePath);
    	long end = System.currentTimeMillis();
    	System.out.println("#---END: " + ((double)(end - start))/1000 
    			+ " seconds.");
    }
    
    public void run(String inputFilePath, String outputFilePath) 
    		throws IOException {
    	/*************************************************
    	 * This method calls other methods to compute the 
    	 * median of each category sale prices.
    	 * 
    	 * Given: <String[]> args 
    	 *        - args[0]: Input data file path
    	 *        - args[1]: Output data directory path.
    	 * Return: void
    	 * Output: A file under the directory specified in 
    	 *         args[1]. The file contains the median of 
    	 *         sale prices of each category in the 
    	 *         input data.
    	 *************************************************/
    	
    	// map: Key = Category <String>
    	//      Value = a list of sales <Double>
    	HashMap<String, LinkedList<Double>> map =
    	    this.readDataForCategoryAndSale(inputFilePath);
    	HashMap<String, Double> resultMap = medianOfCategories(map);
    	outputResult(outputFilePath, resultMap);
    	
    }
    
    public HashMap<String, Double> medianOfCategories(HashMap<String, 
    		LinkedList<Double>> map) {
    	/*************************************************
    	 * This method returns the median of every key value
    	 * (Category) in the given HashMap.
    	 * 
    	 * Given: <HashMap<String, LinkedList<Double>>> map
    	 *        - A HashMap storing the names of category and
    	 *          the corresponding sale prices.
    	 * Return: <HashMap<String, Double>> 
    	 *         - A HashMap storing the names of category and
    	 *           the median of corresponding sale prices.
    	 *************************************************/
    	HashMap<String, Double> resultMap = new HashMap<String, Double>();
    	for (Map.Entry<String, LinkedList<Double>> entry : map.entrySet()) {
    	    String category = entry.getKey();
    	    LinkedList<Double> priceList = entry.getValue();
    	    resultMap.put(category, medianOfList(priceList));
    	}
    	return resultMap;
    }
    
    public Double medianOfList(LinkedList<Double> list) {
    	/*************************************************
    	 * This method returns the median of the given list.
    	 * 
    	 * Given: <LinkedList<Double>> list
    	 *        - A list of Double values.
    	 * Return: <Double> - The median of the given list.
    	 *************************************************/
    	// 1. Sort the given list
    	Collections.sort(list);
    	// 2. size is even or odd?
    	if (list.size() % 2 == 1) {
    		Double median = list.get((int)(list.size()/2));
    		return median;
    	}
    	else {
    		Double median = (list.get((int)(list.size()/2)) 
    		    + list.get((int)(list.size()/2) - 1)) / 2;
    		return median;
    	}
    }
    
    public HashMap<String, LinkedList<Double>> readDataForCategoryAndSale 
    (String pathToData) throws IOException {
    	/*************************************************
    	 * This method reads a file specified in the given 
    	 * argument `pathToData`, extracts the category and
    	 * sale information and return the information as 
    	 * HashMap.
    	 * 
    	 * Given: <String> pathToData - the path to input data
    	 * Returns: <HashMap<String, LinkedList<Double>>>
    	 *          - a HashMap, whose keys are category names
    	 *            and values are corresponding sale price
    	 *            stored in a LinkedList.
    	 *************************************************/
        BufferedReader in = new BufferedReader(new FileReader(pathToData));
        HashMap<String, LinkedList<Double>> map 
            = new HashMap<String, LinkedList<Double>>();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            // For each line we take the category and sale
        	String[] contents = line.split("\t");
        	if (contents.length == 6) {
        		// This line contains enough columns of data
        	    if (contents[3].equals("Category") 
        	    		&& contents[4].equals("Sale")) {
        		    // Omit the first line
        		    continue;
        	    }
        	    else {
        		    // This is not the first line.
        	    	// Check Category and Sale validity.
        	    	// Category free form text: No need to check?
        	    	/*
        	    	 * parseDouble() throws:
                       NullPointerException - if the string is null
                       NumberFormatException - if the string does not 
                       contain a parsable double.
        	    	 */
        	    	try {
        	            double sale = Double.parseDouble(contents[4]);
        	            if (map.containsKey(contents[3])) {
        	            	map.get(contents[3]).add(sale);
        	            }
        	            else {
        	            	LinkedList<Double> valueList = new LinkedList<Double>();
        	            	valueList.add(sale);
        	            	map.put(contents[3], valueList);
        	            }
        	    	}
        	    	catch (NullPointerException ne) {
        	    		System.out.println("--Omit because Sale is not " + 
        	    	        "a number: \n" + "\t" + line);
        	    	}
        	    	catch (NumberFormatException nfe) {
        	    		System.out.println("--Omit because Sale is not " + 
            	    	        "a number: \n" + "\t" + line);
        	    	}
        	        
        	    }
        	}
        	else {
        		// The line does not contain enough information
        		System.out.println("--Omit because of info not complete: \n" 
        		+"\t"+ line);
        	}
        }
        in.close();
        // Return an unsorted list
        return map;
    }
    
    public void outputResult(String filename, 
    		HashMap<String, Double> resultMap) {
    	/*************************************************
    	 * This method outputs the given result map to the 
    	 * given directory as result.txt
    	 * 
    	 * Given: <String> directory - the path to the directory
    	 *                             to store the result.txt;
    	 *        <HashMap<String,Double>> resultMap
    	 *        - The HashMap storing all categories and the
    	 *          median of corresponding sale prices. 
    	 * Returns: void
    	 * Output: The HashMap will be output to directory/result.txt
    	 *************************************************/
    	try {
    		File result = new File(filename);
    		if (result.exists()) {
    			result = new File(filename.substring(0, filename.length()-4) 
    		             + System.currentTimeMillis() + ".txt");
    		}
    		result.createNewFile();
    		FileWriter resultWriter = new FileWriter(result);
    		BufferedWriter out = new BufferedWriter(resultWriter);
    		out.write("Category\tSale (Median)\n");
    		for (Map.Entry<String, Double> entry : resultMap.entrySet()) {
    			out.write(entry.getKey() + "\t" + entry.getValue() + "\n");
    		}
    		out.close();
    		System.out.println("FINISHED: The result is stored in " 
    		+ result.getName());
    	}
    	catch (IOException ioe) {
    		System.out.println("ERROR: Cannot create output file.");
    	}
    }
}