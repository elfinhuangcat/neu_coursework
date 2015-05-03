package mapred_single;

import java.util.ArrayList;
import java.util.HashMap;

public class Context<KEYOUT,VALUEOUT> {
	/**
	 * Stores the result of maps/reduces.
	 */
	private HashMap<KEYOUT,ArrayList<VALUEOUT>> results;
	
	/**
	 * Initializes all fields of the Context object.
	 */
	public Context() {
		this.results = new HashMap<KEYOUT,ArrayList<VALUEOUT>>();
	}
		
	public HashMap<KEYOUT, ArrayList<VALUEOUT>> getResults() {
		return results;
	}

	/**
	 * Writes map results.
	 * @param key the map output key
	 * @param value the map output value
	 */
	public void write(KEYOUT key, VALUEOUT value) {
		if (results.containsKey(key)) {
			//TRY IF THIS WORKS:
			results.get(key).add(value);
		}
		else {
			ArrayList<VALUEOUT> values = new ArrayList<VALUEOUT>();
			values.add(value);
			results.put(key, values);
		}
	}
}