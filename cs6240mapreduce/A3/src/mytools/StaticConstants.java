package mytools;

public class StaticConstants {
	//@prefixes
	public static final String ATTRIBUTE_PREFIX = "@attribute";
	public static final String RECORD_NUM_PREFIX = "@record_num";
	public static final String TRUE_NUM_PREFIX = "@true_num";
	public static final String FALSE_NUM_PREFIX = "@false_num";
	public static final String PRIOR_PREFIX = "@prior";
	public static final String CONDITION_PROB_PREFIX = "@conditional_probability";
	
	//class label identifier.
	public static final String CLASS_LABEL_NAME = "CLASS_LABEL";
	
	//value type identifier
	public static final String REAL_MARK = "real";
	public static final String NOMINAL_MARK = "nominal";
	
	//@attribute indices:
	public static final int ATTRI_PREFIX_INDEX = 0;
	public static final int ATTRI_NAME_INDEX = 1;
	public static final int ATTRI_TYPE_INDEX = 2;
	public static final int ATTRI_MEAN_INDEX = 3;
	
	// Paths
	public static final String MODEL_PATH = "data/model.m";
	public static final String MODEL_TEMP_PATH = "data/model.temp";
	public static final String HEADER_PATH = "data/header";
	public static final String TRAIN_DATA_PATH = "data/traindir";
}

