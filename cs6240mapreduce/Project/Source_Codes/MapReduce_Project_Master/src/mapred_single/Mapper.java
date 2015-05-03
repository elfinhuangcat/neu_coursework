package mapred_single;

/**
 * 
 * @author Yaxin
 *
 * @param <KEYIN>
 * @param <VALUEIN>
 * @param <KEYOUT>
 * @param <VALUEOUT>
 */
public class Mapper<KEYIN,VALUEIN,KEYOUT,VALUEOUT> {
	/**
	 * Constructs a mapper and starts the map process.
	 */
	public Mapper() {}
	
	/**
	 * Called once for each key/value pair in the input split.
	 * @param key
	 * @param value
	 * @param context
	 */
	public void map(KEYIN key, VALUEIN value, Context<KEYOUT, VALUEOUT> context) {}
		
}

