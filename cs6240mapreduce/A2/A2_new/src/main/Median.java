package main;

import java.util.Collections;
import java.util.List;

public class Median {
	public Median() {
		super();
	}
	
	public Double medianOfList(List<Double> list) {
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
}