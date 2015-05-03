package learn.preprocess;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class WriteLog{
	//String outPath;
	private String fieldOne;
	private String fieldTwo;
	private String fieldThree;
	private String fieldFour;
	private String modelPath;
	
	public WriteLog() {
		super();
	}
	public WriteLog(String modelPath) {
		super();
		this.modelPath = modelPath;
		System.out.println("=============CONSTRUCTOR: model path"
				+ ": " + this.modelPath);
	}
	protected WriteLog(String fieldOne, String fieldTwo, String fieldThree){
		//this.outPath=outPath;
		this.fieldOne = fieldOne;
		this.fieldTwo = fieldTwo;
		this.fieldThree = fieldThree;
	}

	
	public String getFieldOne() {
		return fieldOne;
	}
	public void setFieldOne(String fieldOne) {
		this.fieldOne = fieldOne;
	}
	public String getFieldTwo() {
		return fieldTwo;
	}
	public void setFieldTwo(String fieldTwo) {
		this.fieldTwo = fieldTwo;
	}
	public String getFieldThree() {
		return fieldThree;
	}
	public void setFieldThree(String fieldThree) {
		this.fieldThree = fieldThree;
	}
	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
	}
	public String getModelPath() {
		return this.modelPath;
	}
	
	public String getFieldFour() {
		return fieldFour;
	}
	public void setFieldFour(String fieldFour) {
		this.fieldFour = fieldFour;
	}
	/*
	 * write fields data to model.m
	 */
	public void log(){
		BufferedWriter bw = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(this.modelPath, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {	    	
	    	bw = new BufferedWriter(new OutputStreamWriter(fos));
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    try {
	    	bw.write(fieldOne +" "+ fieldTwo + " "+fieldThree + " " + fieldFour);
	        bw.newLine();
	        bw.close();
	    } catch (Exception e) {
	    	System.out.println("CAN NOT WRITE DOWN RECORDES INTO LOG FILE, "
	    			+ "PLEASE DOUBLE CHECK YOUR OUTPUT FILE PATH");
	    	System.out.println("==============================");
	    }
	}
	
	public void print(){
        System.out.println(fieldOne +" "+ fieldTwo + " "+fieldThree);
	}

}