package multi;

import java.util.Collections;
import java.util.List;

import mapred_multi_nodes.Context;
import mapred_multi_nodes.Reducer;

public class MyReducer extends Reducer<String, Double, String, Double> {
	@Override
	public void reduce(String key, List<Double> values, 
			Context<String, Double> context) {
		context.write(key, findMedian(values));
	}
	
	public Double findMedian(List<Double> values) {
		Collections.sort(values);
		int size = values.size();
		return values.get(size/2);
	}
}
