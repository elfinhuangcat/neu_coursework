package multi;

import mapred_multi_nodes.Context;
import mapred_multi_nodes.Mapper;

public class MyMapper extends Mapper<String, String, String, Double> {
	
	private final int CATEGORY_IND = 3;
	private final int SALE_IND = 4;
	public MyMapper() {
		super();
	}
	@Override
	public void map(String key, String value, Context<String, Double> context) {
		if (!value.contains("Category")) {
			try {
				String[] content = value.split("\t");
				if (content.length != 6) {
					return;
				}
				Double sale = Double.parseDouble(content[SALE_IND]);
				context.write(content[CATEGORY_IND], sale);
			}
			catch (NullPointerException ne) {
	    		System.out.println("Omit because Sale is not" + 
	    	        "a number: \n" + "\t" + value);
	    	}
	    	catch (NumberFormatException nfe) {
	    		System.out.println("Omit because Sale is not" + 
    	    	        "a number: \n" + "\t" + value);
	    	}
		}
	}
}
