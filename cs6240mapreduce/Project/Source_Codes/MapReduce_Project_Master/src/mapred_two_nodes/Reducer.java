package mapred_two_nodes;
import java.util.List;

public class Reducer<KEYIN,VALUEIN,KEYOUT,VALUEOUT> {
	/**
	 * Constructs a reducer and starts the process.
	 */
	public Reducer() {
		// for user to extend
	}
	
	/**
	 * This method is called once for each key.
	 * @param key key for the values
	 * @param values a list of values corresponding to the given key
	 * @param context the result can be written to this context object
	 */
	public void reduce(KEYIN key, List<VALUEIN> values, Context<KEYOUT, VALUEOUT> context) {
		// for user to implement
	}
}