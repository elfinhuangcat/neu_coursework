package classifier;
import java.util.Arrays;
import java.util.Random;

import myutil.Log;

import data_preprocess.FillMissingValue;
import data_preprocess.MyResampler;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.NBTree;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
public class DiffCls_Ada {	

	private int fold;
	private double seed;
	private String logFilePath;
	private Instances balancedData;
	private Instances cleanedTestData;

	public DiffCls_Ada(String logFilePath) {
		this.fold = 10;
		this.seed = System.currentTimeMillis();
		this.logFilePath = logFilePath;
		this.balancedData = null;
		this.cleanedTestData = null;
	}
	
	/**
	 * This is the method to be used when run the final test set.
	 * @param logFilePath
	 * @param trainFilePath
	 * @param testFilePath
	 * @param adaOptions
	 * @return test error rate.
	 */
	public static double runTestOnAdaBoost(String logFilePath, String trainFilePath, 
			String testFilePath, String adaOptions) {
		DiffCls_Ada myAdaboost = new DiffCls_Ada(logFilePath);
		// 1. Preprocess training data
		myAdaboost.readTrainingData(trainFilePath);
		// 2. Preprocess testing data
		myAdaboost.readTestingData(testFilePath);
		
		// 3. run
		return myAdaboost.runAdaBoost(adaOptions);
	}

	/**
	 * Runs the CROSS VALIDATION of adaboost on given options.
	 * @param logFilePath path to log file
	 * @param trainFilePath train.arff
	 * @param adaOptions something like : "-I 10 -W \"weka.classifiers.trees.J48 -C 0.25\""
	 * @return the average error rate
	 */
	public static double run(String logFilePath, String trainFilePath, 
			String adaOptions) {
		DiffCls_Ada myAdaboost = new DiffCls_Ada(logFilePath);
		myAdaboost.readTrainingData(trainFilePath);
		return myAdaboost.runAdaCVProcess(adaOptions);
	}

	public void readTrainingData(String trainFilePath) {
		// Read the training data :
		DataSource source = null;
		Instances trainData = null;
		try {
			source = new DataSource(trainFilePath);
			trainData = source.getDataSet();
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - cannot read the training data.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
		trainData.setClassIndex(trainData.numAttributes() - 1);

		// Fill the missing values and resample:
		try {
			Instances missValueReplacedData = FillMissingValue.fillMissingValues(trainData);
			this.balancedData = MyResampler.resampleToBalance(missValueReplacedData);

		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - Data preprocessing error.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * 
	 * @param adaOptions Something like :
	 * "-I 10 -W \"weka.classifiers.trees.J48 -C 0.25\""
	 * @return the average error rate of this classifier
	 */
	public double runAdaCVProcess(String adaOptions) {
		// Use cross validation to run the adaboost:
		AdaBoostM1 adaboost = new AdaBoostM1();
		try {
			Log.addLogToThisPath(Arrays.toString(weka.core.Utils.splitOptions(adaOptions)) + "\n", this.logFilePath);
			adaboost.setOptions(weka.core.Utils.splitOptions(adaOptions));
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - cannot set adaboost options.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}

		// Randomize the order of training data
		Random rand = new Random((long) this.seed);
		Instances randData = new Instances(this.balancedData);
		randData.randomize(rand);
		randData.stratify(this.fold); // To make each fold contains almost equal number of each class.

		double sumTrainErrorRate = 0;
		double sumTrainTPR =0;
		double sumTrainTNR = 0;
		double sumTestErrorRate = 0;
		double sumTestTPR = 0;
		double sumTestTNR = 0;

		for (int iter = 0; iter < this.fold; ++iter) {
			Instances trainData = randData.trainCV(this.fold, iter);
			Instances testData = randData.testCV(this.fold, iter);
			try {
				adaboost.buildClassifier(trainData);
			} catch (Exception e) {
				Log.addLogToThisPath("ERROR - fail to build the adaboost classifier.\n", this.logFilePath);
				e.printStackTrace();
			}
			Log.addLogToThisPath("INFO - Current setting for Adaboost: " + 
					Arrays.toString(adaboost.getOptions()) + "\n", this.logFilePath);
			// Evaluation:
			try {
				// Evaluation on TRAIN data:
				Evaluation evalTrain = new Evaluation(trainData);
				evalTrain.evaluateModel(adaboost, trainData);				
				Log.addLogToThisPath("===TRAIN Error Rate of Fold " + (iter+1) + ": " + evalTrain.errorRate() + "===\n", this.logFilePath);
				sumTrainErrorRate += evalTrain.errorRate();
				Log.addLogToThisPath("===TRAIN True Positive Rate of Fold " + (iter+1) + ": " + evalTrain.truePositiveRate(1) + "===\n", this.logFilePath);
				sumTrainTPR += evalTrain.truePositiveRate(1);
				Log.addLogToThisPath("===TRAIN True Negative Rate of Fold " + (iter+1) + ": " + evalTrain.trueNegativeRate(1) + "===\n", this.logFilePath);
				sumTrainTNR += evalTrain.trueNegativeRate(1);
				
				// Evaluation on TEST data:
				Evaluation eval = new Evaluation(trainData);
				eval.evaluateModel(adaboost, testData);
				Log.addLogToThisPath("===TEST Error Rate of Fold " + (iter+1) + ": " + eval.errorRate() + "===\n", this.logFilePath);
				Log.addLogToThisPath("===TEST True Positive Rate of Fold " + (iter+1) + ": " + eval.truePositiveRate(1) + "===\n", this.logFilePath);
				Log.addLogToThisPath("===TEST True Negative Rate of Fold " + (iter+1) + ": " + eval.trueNegativeRate(1) + "===\n", this.logFilePath);
				sumTestErrorRate += eval.errorRate();
				sumTestTPR += eval.truePositiveRate(1);
				sumTestTNR += eval.trueNegativeRate(1);
				
				// Confusion matrix
				for (int i = 0; i < eval.confusionMatrix().length; ++i) {
					for (int j = 0; j < eval.confusionMatrix()[i].length; ++j) {
						Log.addLogToThisPath(eval.confusionMatrix()[i][j] + "\t", this.logFilePath);
					}
					Log.addLogToThisPath("\n", this.logFilePath);
				}
				
			} catch (Exception e) {
				Log.addLogToThisPath("ERROR - fail to create the evaluation.\n", this.logFilePath);
				e.printStackTrace();
			}
		}
		Log.addLogToThisPath("*******************AVERAGE RESULTS*******************\n", this.logFilePath);
		Log.addLogToThisPath("TRAIN error rate: " + sumTrainErrorRate/this.fold, this.logFilePath);
		Log.addLogToThisPath("TRAIN TPR: " + sumTrainTPR/this.fold, this.logFilePath);
		Log.addLogToThisPath("TRAIN TNR: " + sumTrainTNR/this.fold, this.logFilePath);
		Log.addLogToThisPath("TEST error rate: " + sumTestErrorRate/this.fold, this.logFilePath);
		Log.addLogToThisPath("TEST TPR: " + sumTestTPR/this.fold, this.logFilePath);
		Log.addLogToThisPath("TEST TNR: " + sumTestTNR/this.fold, this.logFilePath);
		Log.addLogToThisPath("*******************AVERAGE RESULTS*******************\n", this.logFilePath);
		return sumTestErrorRate / this.fold;
	}
	
	
	
	public double runAdaBoost(String options) {
		AdaBoostM1 adaboost = new AdaBoostM1();
		try {
			Log.addLogToThisPath(Arrays.toString(weka.core.Utils.splitOptions(options)) + "\n", this.logFilePath);
			adaboost.setOptions(weka.core.Utils.splitOptions(options));
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - cannot set adaboost options.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
		try {
			adaboost.buildClassifier(this.balancedData);
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - cannot build classifier.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			// Evaluation on training set:
			Evaluation evalTrain = new Evaluation(this.balancedData);
			evalTrain.evaluateModel(adaboost, this.balancedData);
			Log.addLogToThisPath("===TRAIN Error Rate: " + evalTrain.errorRate() + "===\n", this.logFilePath);
			Log.addLogToThisPath("===TRAIN True Positive Rate: " + evalTrain.truePositiveRate(1) + "===\n", this.logFilePath);
			Log.addLogToThisPath("===TRAIN True Negative Rate: " + evalTrain.trueNegativeRate(1) + "===\n", this.logFilePath);
			
			// Evaluation on testing set:
			Evaluation evalTest = new Evaluation(this.balancedData);
			evalTest.evaluateModel(adaboost, this.cleanedTestData);
			Log.addLogToThisPath("===TEST Error Rate: " + evalTest.errorRate() + "===\n", this.logFilePath);
			Log.addLogToThisPath("===TEST True Positive Rate: " + evalTest.truePositiveRate(1) + "===\n", this.logFilePath);
			Log.addLogToThisPath("===TEST True Negative Rate: " + evalTest.trueNegativeRate(1) + "===\n", this.logFilePath);
			
			return evalTest.errorRate();
			
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - evaluation error.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
		return 1;
		
	}
	
	/**
	 * 
	 * @param testFilePath test.arff
	 */
	public void readTestingData(String testFilePath) {	
		DataSource source = null;
		Instances testData = null;
		try {
			source = new DataSource(testFilePath);
			testData = source.getDataSet();
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - cannot read the testing data.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
		testData.setClassIndex(testData.numAttributes() - 1);

		// Fill the missing values:
		try {
			this.cleanedTestData = FillMissingValue.fillMissingValues(testData);
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - Testing data preprocessing error.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static Classifier buildModel(String modelChoice, 
			String logFilePath) {
		if (modelChoice.equals("-c45")) {
			return new J48();
		}
		else if (modelChoice.equals("-knn")) {
			return new IBk();
		}
		else if (modelChoice.equals("-logr")) {
			return new Logistic();
		}
		else if (modelChoice.equals("-nb")) {
			return new NaiveBayes();
		}
		else if (modelChoice.equals("-nbt")) {
			return new NBTree();
		}
		else if (modelChoice.equals("-smo")) {
			return new SMO();
		}
		else {
			Log.addLogToThisPath("ERROR - wrong choice of " +
					"classifier.\n", logFilePath);
			return null;
		}
	}

	public static void setClsOptions(String[] options, Classifier model, 
			String logFilePath) {
		try {
			model.setOptions(options);
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - Cannot set options for the base " +
					"classifier in Adaboost.\n", logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
	}
}