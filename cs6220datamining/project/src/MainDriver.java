import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import trans_to_arff.TransTestArff;
import trans_to_arff.TransTrainArff;
import myutil.Log;
import myutil.ResultList;
import classifier.Bagging_Multiple;
import classifier.DiffClassifier;
import classifier.DiffCls_Ada;

public class MainDriver {
	/**
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws IOException {
		if (args[0].equals("-arff")) {
			// args[1] = train data path
			// args[2] = dest train.arff path
			// args[3] = test data path
			// args[4] = dest test.arff path
			TransTrainArff.main(Arrays.copyOfRange(args, 1, 3));
			TransTestArff.main(Arrays.copyOfRange(args, 3, args.length));
			System.out.println("Transformation to ARFF succeeded!");
		}
		else if (args[0].equals("-bag")) {
			// BAGGING
			// args[1] = train.arff path
			// args[2] = test.arff path
			//Bagging_Multiple bagging = new Bagging_Multiple();
			File logfile = new File("bagging_log");
			if (!logfile.exists()) {
				logfile.createNewFile();
			}
			try {
				Bagging_Multiple.trainBagging(args[1], logfile.getAbsolutePath(), false);
				Bagging_Multiple.testBagging(args[2], logfile.getAbsolutePath());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		else if (args[0].equals("-boo")) {
			// BOOSTING
			// args[2] = train.arff path
			// args[3] = test.arff path
			File logfile = new File("bagging_log");
			if (!logfile.exists()) {
				logfile.createNewFile();
			}
			if (args[1].equals("-c45")) {
				DiffCls_Ada.runTestOnAdaBoost(logfile.getAbsolutePath(), 
						args[2], args[3], "-I 10 -W \"weka.classifiers.trees.J48\" -- -C 0.35 -S -A");
			}
			else if (args[1].equals("-knn")) {
				DiffCls_Ada.runTestOnAdaBoost(logfile.getAbsolutePath(), 
						args[2], args[3], "-I 10 -W \"weka.classifiers.lazy.IBk\" -- -K 1");
			}
			else if (args[1].equals("-nb")) {
				DiffCls_Ada.runTestOnAdaBoost(logfile.getAbsolutePath(), 
						args[2], args[3], "-I 10 -W \"weka.classifiers.bayes.NaiveBayes\" -- -D");
			}
			else if (args[1].equals("-nbt")) {
				DiffCls_Ada.runTestOnAdaBoost(logfile.getAbsolutePath(), 
						args[2], args[3], "-I 10 -W \"weka.classifiers.trees.NBTree\"");
			}
		}
		else if (args[0].equals("-allclstest")) {
			// args[1] = log file path
			// args[2] = train arff path
			File logFile = new File(args[1]);
			if (logFile.exists()) {
				logFile.delete();
			}
			logFile.createNewFile();

			MainDriver.runAllClassifiers(args[1], args[2]);
		}
		else if (args[0].equals("-c45boosttest")) {
			// args[1] = log file path
			// args[2] = train arff path
			File logFile = new File(args[1]);
			if (logFile.exists()) {
				logFile.delete();
			}
			logFile.createNewFile();

			MainDriver.runC45BoostingTest(args[1], args[2]);
		}
		else if (args[0].equals("-knnboosttest")) {
			// args[1] = log file path
			// args[2] = train arff path
			File logFile = new File(args[1]);
			if (logFile.exists()) {
				logFile.delete();
			}
			logFile.createNewFile();

			MainDriver.runKNNBoostingTest(args[1], args[2]);
		}
		else if (args[0].equals("-nbboosttest")) {
			// args[1] = log file path
			// args[2] = train arff path
			File logFile = new File(args[1]);
			if (logFile.exists()) {
				logFile.delete();
			}
			logFile.createNewFile();

			MainDriver.runNBBoostingTest(args[1], args[2]);
		}
		else if (args[0].equals("-nbtboosttest")) {
			// args[1] = log file path
			// args[2] = train arff path
			File logFile = new File(args[1]);
			if (logFile.exists()) {
				logFile.delete();
			}
			logFile.createNewFile();

			MainDriver.runNBTreeBoostingTest(args[1], args[2]);
		}
		/*
		File logFile = new File("/home/elfin/workspace/OngoingProjects/DataMining_Project/adalog");
		if (logFile.exists()) {
			logFile.delete();
		}
		logFile.createNewFile();
		
		DiffCls_Ada.runTestOnAdaBoost(logFile.getAbsolutePath(), "/home/elfin/workspace/OngoingProjects/DataMining_Project/data/train.arff", 
				"/home/elfin/workspace/OngoingProjects/DataMining_Project/data/test.arff", "-I 10 -W \"weka.classifiers.trees.J48\" -- -C 0.25 -S -A");

		MainDriver.runAllBoosting("/home/elfin/workspace/OngoingProjects/DataMining_Project/adalog", "/home/elfin/workspace/OngoingProjects/DataMining_Project/data/train.arff");
		
		*/
		
	}
	
	public static void runC45BoostingTest(String logFilePath, String trainArffPath) {
		// 1.1  C4.5, iteration = 10:
		Log.addLogToThisPath("=====Adaboost iteration = 10 , C4.5 [-C 0.25 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 10 -W \"weka.classifiers.trees.J48\" -- -C 0.25 -S -A");
		Log.addLogToThisPath("=====Adaboost iteration = 10 , C4.5 [-C 0.35 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 10 -W \"weka.classifiers.trees.J48\" -- -C 0.35 -S -A");
		Log.addLogToThisPath("=====Adaboost iteration = 10 , C4.5 [-C 0.45 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 10 -W \"weka.classifiers.trees.J48\" -- -C 0runAllBoosting.45 -S -A");
		// 1.2  C4.5, iteration = 15:
		Log.addLogToThisPath("=====Adaboost iteration = 15 , C4.5 [-C 0.25 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 15 -W \"weka.classifiers.trees.J48\" -- -C 0.25 -S -A");
		Log.addLogToThisPath("=====Adaboost iteration = 15 , C4.5 [-C 0.35 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 15 -W \"weka.classifiers.trees.J48\" -- -C 0.35 -S -A");
		Log.addLogToThisPath("=====Adaboost iteration = 15 , C4.5 [-C 0.45 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 15 -W \"weka.classifiers.trees.J48\" -- -C 0.45 -S -A");
		// 1.3 C4.5, iteration = 20:
		Log.addLogToThisPath("=====Adaboost iteration = 20 , C4.5 [-C 0.25 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 20 -W \"weka.classifiers.trees.J48\" -- -C 0.25 -S -A");
		Log.addLogToThisPath("=====Adaboost iteration = 20 , C4.5 [-C 0.35 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 20 -W \"weka.classifiers.trees.J48\" -- -C 0.35 -S -A");
		Log.addLogToThisPath("=====Adaboost iteration = 20 , C4.5 [-C 0.45 -S -A] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 20 -W \"weka.classifiers.trees.J48\" -- -C 0.45 -S -A");
	}
	
	public static void runKNNBoostingTest(String logFilePath, String trainArffPath) {		
		// 2.1 KNN, iteration = 10:	
		Log.addLogToThisPath("=====Adaboost itreation = 10, KNN [-K 1] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 10 -W \"weka.classifiers.lazy.IBk\" -- -K 1");
		Log.addLogToThisPath("=====Adaboost itreation = 10, KNN [-K 3] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 10 -W \"weka.classifiers.lazy.IBk\" -- -K 3");
		// 2.2 KNN, iteration = 15:
		Log.addLogToThisPath("=====Adaboost itreation = 15, KNN [-K 1] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 15 -W \"weka.classifiers.lazy.IBk\" -- -K 1");
		Log.addLogToThisPath("=====Adaboost itreation = 15, KNN [-K 3] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 15 -W \"weka.classifiers.lazy.IBk\" -- -K 3");
		// 2.3 KNN, iteration = 20:
		Log.addLogToThisPath("=====Adaboost itreation = 20, KNN [-K 1] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 20 -W \"weka.classifiers.lazy.IBk\" -- -K 1");
		Log.addLogToThisPath("=====Adaboost itreation = 20, KNN [-K 3] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 20 -W \"weka.classifiers.lazy.IBk\" -- -K 3");
	}
	
	public static void runNBBoostingTest(String logFilePath, String trainArffPath) {	
		// 3. Naive Bayes:
		Log.addLogToThisPath("=====Adaboost itreation = 10, NaiveBayes [-D] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 10 -W \"weka.classifiers.bayes.NaiveBayes\" -- -D");
		Log.addLogToThisPath("=====Adaboost itreation = 15, NaiveBayes [-D] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 15 -W \"weka.classifiers.bayes.NaiveBayes\" -- -D");
		Log.addLogToThisPath("=====Adaboost itreation = 20, NaiveBayes [-D] =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 20 -W \"weka.classifiers.bayes.NaiveBayes\" -- -D");
	}
	
	public static void runNBTreeBoostingTest(String logFilePath, String trainArffPath) {	
		// 4. NB Tree
		Log.addLogToThisPath("=====Adaboost itreation = 10, NBTree =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 10 -W \"weka.classifiers.trees.NBTree\"");
		Log.addLogToThisPath("=====Adaboost itreation = 15, NBTree =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 15 -W \"weka.classifiers.trees.NBTree\"");
		Log.addLogToThisPath("=====Adaboost itreation = 20, NBTree =====\n", logFilePath);
		DiffCls_Ada.run(logFilePath, trainArffPath, 
				"-I 20 -W \"weka.classifiers.trees.NBTree\"");
		
	}

	/**
	 * This is the method to run cross validation on all classifiers to see 
	 * which has the better average performance (lower error rate).
	 * @param logFilePath the path to the log file
	 * @param trainArffPath the path to the training set arff file.
	 */
	public static void runAllClassifiers(String logFilePath, String trainArffPath) {
		double errorRate = 0;
		ResultList results = new ResultList();
		try {
			// 1. C4.5 decision tree
			// 1.1 Laplace smoothing is applied. Vary the pruning confidence:
			Log.addLogToThisPath("### C4.5 smoothing applied, vary pruning confidence:\n", logFilePath);
			double[] pruningConf = {0.10, 0.15, 0.20, 0.25, 0.30, 0.35, 0.40, 0.45, 0.50};
			for (Double threshold : pruningConf) {
				errorRate = DiffClassifier.run(new String[] {logFilePath, trainArffPath, 
						"-c45", "-C", threshold.toString(), "-A"});
				Log.addLogToThisPath("*************[-c45 -C " + threshold.toString() + 
						" -A] " + errorRate + " *************\n", logFilePath);
				results.addResult("[-c45 -C " + threshold.toString() + " -A] ", errorRate);
			}
			// 1.2 Laplace smoothing is applied. Use reduced error pruning.
			Log.addLogToThisPath("### C4.5 smoothing applied, use reduced error pruning:\n", logFilePath);
			errorRate = DiffClassifier.run(new String[] {logFilePath, trainArffPath, 
					"-c45", "-R", "-A"}); 
			Log.addLogToThisPath("*************[-c45 -R -A] " + errorRate + " *************\n", logFilePath);
			results.addResult("[-c45 -R -A] ", errorRate);

			// 1.3 Turn off subtree raising. Laplace smoothing is applied. Vary the pruning confidence:
			Log.addLogToThisPath("### C4.5 smoothing applied, TURN OFF SUBTREE RAISING, " +
					"vary pruning confidence:\n", logFilePath);
			for (Double threshold : pruningConf) {
				errorRate = DiffClassifier.run(new String[] {logFilePath, trainArffPath, 
						"-c45", "-C", threshold.toString(), "-S", "-A"});
				Log.addLogToThisPath("*************[-c45 -C " + threshold.toString() + 
						" -S -A] " + errorRate + " *************\n", logFilePath);
				results.addResult("[-c45 -C " + threshold.toString() + 
						" -S -A] ", errorRate);
			}

			// 1.4 Turn off subtree raising. Laplace smoothing is applied. Use reduced error pruning.
			Log.addLogToThisPath("### C4.5 smoothing applied, TURN OFF SUBTREE RAISING," +
					" use reduced error pruning:\n", logFilePath);
			errorRate = DiffClassifier.run(new String[] {logFilePath, trainArffPath, 
					"-c45", "-R", "-S", "-A"}); 
			Log.addLogToThisPath("*************[-c45 -R -S -A] " + errorRate + " *************\n", logFilePath);
			results.addResult("[-c45 -R -S -A] ", errorRate);

			// 2. KNN: with varied K value
			Log.addLogToThisPath("### KNN: Vary K ###\n", logFilePath);
			for (Integer k = 1; k < 21; ++k) {
				errorRate = DiffClassifier.run(new String[] {logFilePath, trainArffPath, 
						"-knn", "-K", k.toString()}); 
				Log.addLogToThisPath("*************[-c45 -K " + k.toString() +
						"] " + errorRate + " *************\n", logFilePath);
				results.addResult("[-c45 -K " + k.toString() + "] ", errorRate);
			}

			// 3. Logistic Regression: varying the ridge estimator
			Log.addLogToThisPath("### Logistic Regression: Default settings ###\n", logFilePath);
			errorRate = DiffClassifier.run(new String[] {logFilePath, 
					trainArffPath, "-logr"}); 
			Log.addLogToThisPath("*************[-logr] " + 
					errorRate + " *************\n", logFilePath);
			results.addResult("[-logr] ", errorRate);

			// 4. Naive Bayes: How to process the numeric features
			// 4.1 Default setting: Guassian Distribution
			Log.addLogToThisPath("### Naive Bayes: Default settings ###\n", logFilePath);
			errorRate = DiffClassifier.run(new String[] {logFilePath, 
					trainArffPath, "-nb"}); 
			Log.addLogToThisPath("*************[-nb] " + 
					errorRate + " *************\n", logFilePath);
			results.addResult("[-nb] ", errorRate);
			
			// 4.2 -D: Use supervised discretization to process numeric attributes
			Log.addLogToThisPath("### Naive Bayes: Discretize the numeric features ###\n", logFilePath);
			errorRate = DiffClassifier.run(new String[] {logFilePath, 
					trainArffPath, "-nb", "-D"}); 
			Log.addLogToThisPath("*************[-nb -D] " + 
					errorRate + " *************\n", logFilePath);
			results.addResult("[-nb -D] ", errorRate);

			// 5. NBTree: No options
			Log.addLogToThisPath("### NaiveBayes Decision Tree ###\n", logFilePath);
			errorRate = DiffClassifier.run(new String[] {logFilePath, 
					trainArffPath, "-nbt"}); 
			Log.addLogToThisPath("*************[-nbt] " + 
					errorRate + " *************\n", logFilePath);
			results.addResult("[-nbt] ", errorRate);

			// ================================================================
			// 6. SMO: 
			// 6.1 PolyKernel 1st order: vary C
			Log.addLogToThisPath("### SMO ###\n", logFilePath);

			String[] options = weka.core.Utils.splitOptions("-C 0.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0\"");
			String[] prefix = new String[] {logFilePath, trainArffPath, "-smo"};
			/*
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 2.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 4.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/

			// 6.2 SMO PolyKernel 1st: vay P
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-16 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-8 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-4 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 1.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/

			// 6.2 SMO PolyKernel 2nd order: Vary C
			/*
			options = weka.core.Utils.splitOptions("-C 0.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 2.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 2.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 2.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 2.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 4.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 2.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/

			// 6.3. SMO PolyKernel 2nd: Vary P
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-16 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 2.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 2.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-8 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 2.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-4 "
					+ "-K \"weka.classifiers.functions.supportVector.PolyKernel -E 2.0\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/
			// 6.4 SMO RBFkernel : gamma = 0.01, vary C
			/*
			options = weka.core.Utils.splitOptions("-C 0.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.01\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			 */
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.01\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 2.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.01\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 4.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.01\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/
			// 6.5 SMO RBFkernel : gamma = 0.01, vary P
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-16 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.01\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.01\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-8 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.01\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-4 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.01\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/
			// 6.6 RBFkernel : gamma = 0.1 vary C
			/*
			options = weka.core.Utils.splitOptions("-C 0.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 2.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 4.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/

			// 6.7 SMO RBFkernel : gamma = 0.1, vary P
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-16 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-8 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-4 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 0.1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/

			// 6.8 RBFkernel : gamma = 1 vary C
			/*
			options = weka.core.Utils.splitOptions("-C 0.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 2.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 4.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/
			// 6.9 SMO RBFkernel : gamma = 1, vary P
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-16 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);

			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-8 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			/*
			options = weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-4 "
					+ "-K \"weka.classifiers.functions.supportVector.RBFKernel -G 1\"");
			errorRate = DiffClassifier.run(MainDriver.concatenateTwoStringArrays(prefix, options)); 
			Log.addLogToThisPath("*************[-smo " + Arrays.toString(options) + " ] " +
					errorRate + " *************\n", logFilePath);
			results.addResult("[-smo " + Arrays.toString(options) + " ] ", errorRate);
			*/
			// OUTPUT THE SUMMARY:
			Log.addLogToThisPath("**********************SUMMARY**********************\n", logFilePath);
			for (int counter = 0; counter < results.getResultList().size(); ++ counter) {
				Log.addLogToThisPath(results.getResultList().get(counter).getIdentifier() + 
						"\t" + results.getResultList().get(counter).getErrorRate(), logFilePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Concatenates two string arrays. The first will be put in front and the 
	 * second will be put after the first.
	 * @param arr1 the first String array
	 * @param arr2 the second String array
	 * @return the concatenated String array
	 */
	public static String[] concatenateTwoStringArrays(String[] arr1, String[] arr2) {
		String[] newArr = new String[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, newArr, 0, arr1.length);
		System.arraycopy(arr2, 0, newArr, arr1.length, arr2.length);
		return newArr;
	}

	/*
	public static void main(String[] args) throws Exception {
		if (args[0].equals("-transtrain")) {
			if (args.length != 3) {
				//TODO: print usage error
				System.exit(1);
			}
			TransTrainArff.main(Arrays.copyOfRange(args, 1, 3));
		}
		else if (args[0].equals("-transtest")) {
			if (args.length != 3) {
				//TODO: print usage error
				System.exit(1);
			}
			TransTestArff.main(Arrays.copyOfRange(args, 1, 3));
		}
		else if (args[0].equals("-diffcls")) {
			DiffClassifier.run(Arrays.copyOfRange(args, 1, args.length));
		}
		else {
			// TODO: print usage error
			System.exit(1);
		}
	}
	 */
}