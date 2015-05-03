package classifier;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import myutil.Log;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.NBTree;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import data_preprocess.FillMissingValue;
import data_preprocess.MyResampler;
/**
 * This class provides the method to run different classifiers 
 * according to user-entered argument. It runs without any 
 * data preprocessing, cost matrix, bagging or boosting.
 * @author yaxin
 *
 */
public class DiffClassifier {
	private String logFilePath;
	private int fold; // Cross validation fold num
	private int seed; // Random seed for reordering the training data
	private Classifier model;

	public DiffClassifier() {
		this.fold = 10;
		this.seed = (int) System.currentTimeMillis();
		this.model = null;
		this.logFilePath = null;
	}
	public DiffClassifier(String logFilePath) {
		this.fold = 10;
		this.seed = (int) System.currentTimeMillis();
		this.model = null;
		this.logFilePath = logFilePath;
	}
	public DiffClassifier(int fold, int seed, String logFilePath) {
		this.fold = fold;
		this.seed = seed;
		this.model = null;
		this.logFilePath = logFilePath;
	}

	public static void main(String[] args) throws IOException {
		String trainArffPath = "/home/elfin/workspace/OngoingProjects/DataMining_Project/data/train.arff";
		String logFilePath = "/home/elfin/workspace/OngoingProjects/DataMining_Project/data/log";
		File logFile = new File(logFilePath);
		if (logFile.exists()) {
			logFile.delete();
		}
		logFile.createNewFile();
		String[] arguments = {logFilePath, trainArffPath, "-knn", "-K", "3"};
		try {
			DiffClassifier.run(arguments);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/***************************************************
	 * This method first preprocess the data and then evaluate the 
	 * model by using 10-fold cross validation
	 * @param args[0] log file path
	 * @param args[1] train arff path
	 * @param args[2] choice of classifier: ["-c45", "-knn", "-logr", "-nb", "-nbt", "-smo"]
	 * @param args[after 2] options passed to the classifier.
	 * @see For available options please check the correspongding weka doc.
	 */
	public static double run(String[] args) throws Exception {
		DiffClassifier classifyTask = new DiffClassifier(args[0]);
		// Read training data
		DataSource trainSource = new DataSource(args[1]);
		Instances trainData = trainSource.getDataSet();
		trainData.setClassIndex(trainData.numAttributes() - 1);
		
		// Replace missing values:
		Instances missingReplacedData = FillMissingValue.fillMissingValues(trainData);

		// Resample:
		Instances balancedTrainData = MyResampler.resampleToBalance(missingReplacedData);

		// Build and evaluate the classifier using cross validation:
		return classifyTask.buildAndEvaluate(balancedTrainData, 
				Arrays.copyOfRange(args, 2, args.length));

	}

	/**
	 * Build a classifier according to the input options and evaluation through cross validation.
	 * @param trainData the training instances. It should be without missing value and balanced.
	 * @param args[0] choice of classifier: ["-c45", "-knn", "-logr", "-nb", "-nbt", "-smo"]
	 * @param args[after 0] options passed to the classifier.
	 */
	public double buildAndEvaluate(Instances trainData, String[] args) {
		this.setModel(args[0], Arrays.copyOfRange(args, 1, args.length));
		Log.addLogToThisPath("Options: " + Arrays.toString(this.model.getOptions()) + 
				"\n", this.logFilePath);

		// Randomize data
		Random rand = new Random(this.seed);
		trainData.randomize(rand);

		double sumErrorRate = 0;
		for (int n = 0; n < this.fold; ++n) {
			Instances trainFold = trainData.trainCV(this.fold, n);
			Instances testFold = trainData.testCV(this.fold, n);
			try {
				this.model.buildClassifier(trainFold);

				// Evaluate:
				Evaluation eval = new Evaluation(trainFold);
				eval.evaluateModel(this.model, testFold);
				Log.addLogToThisPath("INFO - Statistics for " + (n+1) + "th fold: \n"
						+ "Error rate: " + eval.errorRate() + "\nConfusion Matrix:\n", 
						this.logFilePath);
				for (int i = 0; i < eval.confusionMatrix().length; ++i) {
					for (int j = 0; j < eval.confusionMatrix()[i].length; ++j) {
						Log.addLogToThisPath(eval.confusionMatrix()[i][j] + "\t", this.logFilePath);
					}
					Log.addLogToThisPath("\n", this.logFilePath);
				}
				sumErrorRate += eval.errorRate();
			} catch (Exception e) {
				Log.addLogToThisPath("ERROR - cannot load the train/test fold.\n", this.logFilePath);
				e.printStackTrace();
			}
		}
		Log.addLogToThisPath("************* Average Error Rate: " + 
				sumErrorRate / this.fold + " *************\n", this.logFilePath);
		return (sumErrorRate / this.fold);
	}


	public int getFold() {
		return fold;
	}
	public void setFold(int fold) {
		this.fold = fold;
	}
	public int getSeed() {
		return seed;
	}
	public void setSeed(int seed) {
		this.seed = seed;
	}
	public Classifier getModel() {
		return model;
	}

	/**
	 * @param modelChoice one of: ["-c45", "-knn", "-logr", "-nb", "-nbt", "-smo"]
	 * @param options the options of this model
	 */
	public void setModel(String modelChoice, String[] options) {
		if (modelChoice.equals("-c45")) {
			this.model = new J48();
			Log.addLogToThisPath("====================\nClassifier Name: " +
					"C45 Decision Tree\n", this.logFilePath);
		}
		else if (modelChoice.equals("-knn")) {
			this.model = new IBk();
			Log.addLogToThisPath("====================\nClassifier Name: " +
					"KNN\n", this.logFilePath);
		}
		else if (modelChoice.equals("-logr")) {
			this.model = new SimpleLogistic();
			Log.addLogToThisPath("====================\nClassifier Name: " +
					"Logistic Regression\n", this.logFilePath);
		}
		else if (modelChoice.equals("-nb")) {
			this.model = new NaiveBayes();
			Log.addLogToThisPath("====================\nClassifier Name: " +
					"Naive Bayes\n", this.logFilePath);
		}
		else if (modelChoice.equals("-nbt")) {
			this.model = new NBTree();
			Log.addLogToThisPath("====================\nClassifier Name: " +
					"Naive Bayes Decision Tree\n", this.logFilePath);
		}
		else if (modelChoice.equals("-smo")) {
			this.model = new SMO();
			Log.addLogToThisPath("====================\nClassifier Name: " +
					"SMO\n", this.logFilePath);
		}
		else {
			Log.addLogToThisPath("ERROR - wrong choice of classifier.\n", this.logFilePath);
			System.exit(1);
		}
		try {
			this.model.setOptions(options);
		} catch (Exception e) {
			Log.addLogToThisPath("ERROR - model options failed to be set.\n", this.logFilePath);
			e.printStackTrace();
			System.exit(1);
		}
	}

}