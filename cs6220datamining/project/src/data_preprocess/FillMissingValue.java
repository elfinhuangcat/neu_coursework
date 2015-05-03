package data_preprocess;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

public class FillMissingValue {
	public FillMissingValue() {
		// do nothing
	}
	
	/**
	 * 
	 * @param data the data to be preprocessed (with missing values)
	 * @return the data instances with missing values replaced by mode or mean
	 * @throws Exception If the input data instances cannot be found
	 * @author yaxin
	 */
	public static Instances fillMissingValues(Instances data) throws Exception {
		ReplaceMissingValues replacer = new ReplaceMissingValues();
		replacer.setInputFormat(data);
		return Filter.useFilter(data, replacer);
	}
}