package classifier;

import data_preprocess.FillMissingValue;
import data_preprocess.MyResampler;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.Logistic;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.NBTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;



import java.util.*;

import myutil.Log;

/**
 *
 * Bagging Multiple performs the bagging on
 * 1. K N N
 * 2. Naive Bays
 * 3. Decision Tree
 * 4. Naive Bays Decision Tree
 * 5. Logistic Regression
 *
 * Usage:
 * for training :-
 *
 *  trainBagging( train.arff, logfile, true)  => for cross validation
 *  trainBagging( train.arff, logfile, false) => without cross validation
 *
 *
 * for testing :-
 *  testBagging( test.arff, logfile)
 *
 * Created by jalpanranderi on 4/29/15.
 */
public class Bagging_Multiple {


    public static final String CLASS_POSITIVE = "<=50K";
    public static final String CLASS_NEGATIVE = ">50K";

    private static List<Classifier> sBaseLearners;


    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Usage : <logfile> <train.arff> <test.arff>");
            System.exit(1);
        }

        String path = args[0];
        trainBagging(args[1],path, true);
//        trainBagging(args[1], path, false);
        testBagging(args[2], path);
    }

    /**
     * Test bagging
     * @param testFile Test.arff file
     * @param path Logfile path
     * @throws Exception
     */
    public static void testBagging(String testFile, String path) {

        try {

            // 1. Read test data
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(testFile);
            Instances testData = source.getDataSet();
            testData.setClassIndex(testData.numAttributes() - 1);


            if (sBaseLearners == null) {
                String str_message = "Train is not done. First train Bagging\n";
                System.out.printf(str_message);
                Log.addLogToThisPath(str_message, path);

            } else {
                Log.addLogToThisPath("Testing : test.arff ", path);
                Result result = startTesting(testData, path);
                Log.addLogToThisPath(result.toString(), path);
            }
        }catch(Exception e ){
            Log.addLogToThisPath("Unable to read File "+testFile,path);
            System.out.println("Unable to read Testing file");
        }

    }

    /**
     * start testing the bagging on given test data
     * @param data Instances Testing data
     * @return Result {total_error, true pos, true neg, false pos, false neg}
     * @throws Exception
     */
    private static Result startTesting(Instances data, String path) throws Exception {
        int unclassified = 0;
        int error = 0;
        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;

        String log = String.format("Total instances: %d\n", data.numInstances());
        Log.addLogToThisPath(log, path);



        for (int t_index = 0; t_index < data.numInstances(); t_index++) {
            Instance test_instance = data.instance(t_index);

            List<Double> answer = new ArrayList<Double>();
            for (Classifier c : sBaseLearners) {
                answer.add(c.classifyInstance(test_instance));
            }

            String correctClass = getClass(test_instance);

            double major_vote = getMajorVote(answer);



            if (major_vote == -1) {
                unclassified++;
            } else if (isMissClassified(major_vote, test_instance)) {
                error++;

                if(correctClass.equals(CLASS_POSITIVE)){
                    // correct class is positive and it classified as negative
                    // so it is false negative
                    fn++;
                }else{
                    // correct class is negative and it classified as positive
                    // so it is false positive
                    fp++;
                }


            }else if(correctClass.equals(CLASS_POSITIVE)){
                // correct class is true and classified as true
                // so it is true positive
                tp++;
            }else {
                // correct class is negative and classified as false
                // so it is true negative
                tn++;
            }

        }

        return new Result(error + unclassified, tp, tn, fn, fp);
    }


    /**
     * get the instance class attribute value
     * @param instance Instance
     * @return Double 0 , 1
     */
    private static double getClassValue(Instance instance) {
        return instance.classValue();
    }


    /**
     * get the actual class attribute string value
     * @param instance Instance
     * @return >50K, <=50K
     */
    private static String getClass(Instance instance) {
        return instance.stringValue(instance.numAttributes() - 1);
    }


    /**
     * Train the base learner using  given the training file
     * @param trainFile Traing File
     * @param path Logfile
     * @throws Exception
     */
    public static void trainBagging(String trainFile, String path, boolean isCrossValidation)  {

        String str_message = String.format("Training : Bagging\n Cross-Validation :%s\n",isCrossValidation);
        Log.addLogToThisPath(str_message, path);

        try {

            // 1. Read training data
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(trainFile);
            Instances trainingSet = source.getDataSet();
            trainingSet.setClassIndex(trainingSet.numAttributes() - 1);


            // Fill missing value
            trainingSet = FillMissingValue.fillMissingValues(trainingSet);


            // 3. Balance data set
            trainingSet = MyResampler.resampleToBalance(trainingSet);


            // 4. Train
            if (isCrossValidation) {
                performCrossValidation(path, trainingSet);
            } else {
                sBaseLearners = trainClassifiers(trainingSet, path);
            }

        }catch (Exception e){
            Log.addLogToThisPath("Unable to read file "+trainFile,path);
            System.out.println("Unable to read file"+trainFile);
        }
    }


    /**
     * perform the cross validation to training the classifiers
     * @param path Logfile path
     * @param trainingSet Instances training set
     * @throws Exception
     */
    private static void performCrossValidation(String path, Instances trainingSet) throws Exception {
        double avgError = 0;
        double avgTPR = 0;
        double avgTNR = 0;

        // 1. Generate sets
        Random random = new Random();
        Instances randTrain = new Instances(trainingSet);
        randTrain.randomize(random);


        // 2.1 set folds
        int folds = 10;
        randTrain.stratify(folds);


        // 3. starts training
        for (int i = 0; i < folds; i++) {
            Instances train = randTrain.trainCV(folds, i);
            Instances test = randTrain.testCV(folds, i);

            String log = String.format("Iteration : %d\n", i);
            Log.addLogToThisPath(log, path);


            // Train
            sBaseLearners = trainClassifiers(train,path);

            // Test
            Log.addLogToThisPath("Testing : ",path);
            Result result = startTesting(test, path);


            avgError = avgError + result.mTotalError;
            avgTNR = avgTNR + result.getTrueNegativeRate();
            avgTPR = avgTPR + result.getTruePositiveRate();
        }

        avgError = avgError / folds;
        avgTNR = avgTNR / folds;
        avgTPR = avgTPR / folds;

        String log = String.format("avg_error: %.0f\navg_TNR: %.2f\navg_TPR: %.2f\n", avgError, avgTNR, avgTPR);
        Log.addLogToThisPath(log, path);
    }


    /**
     * true if instance is miss classified
     * @param major_vote Double classifier's majority vote
     * @param test_instance Test Instance
     * @return Boolean
     */
    private static boolean isMissClassified(double major_vote, Instance test_instance) {
        return major_vote != getClassValue(test_instance);
    }


    /**
     * Determine the majority vote for the given classifier
     * @param answer
     * @return -1 : can not answer -error
     * class_one = major vote as class one
     * class_two = major vote as class two
     */
    private static double getMajorVote(List<Double> answer) {
        HashMap<Double, Integer> count = new HashMap<Double, Integer>();
        for (double d : answer) {
            if (count.containsKey(d)) {
                count.put(d, count.get(d) + 1);
            } else {
                count.put(d, 1);
            }
        }



        // sort the map
        List<Map.Entry<Double, Integer>> list = sortByComparator(count);
        if (list.size() == 2) {
            double class_one = list.get(0).getValue();
            double class_two = list.get(1).getValue();

            if (class_one == class_two) {
                return -1;
            } else if (class_one > class_two) {
                return list.get(0).getKey();
            } else {
                return list.get(1).getKey();
            }
        } else {
            return list.get(0).getKey();
        }


    }


    /**
     * sort the hashmap by value
     * @param unsortMap unsorted HashMap
     * @return HashMap
     */
    private static List<Map.Entry<Double, Integer>> sortByComparator(Map<Double, Integer> unsortMap) {

        // Convert Map to List
        List<Map.Entry<Double, Integer>> list =
                new LinkedList<Map.Entry<Double, Integer>>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<Double, Integer>>() {
            public int compare(Map.Entry<Double, Integer> o1,
                               Map.Entry<Double, Integer> o2) {
                return -1 * (o1.getValue()).compareTo(o2.getValue());
            }
        });

        return list;
    }


    /**
     * Train the classifier on the given training set
     * @param train Training set
     * @param path Log file path
     * @return List<Classifiers>
     * @throws Exception
     */
    private static List<Classifier> trainClassifiers(Instances train, String path) throws Exception {


        List<Classifier> list = new ArrayList<Classifier>();


        // Build classifiers
        IBk knn = new IBk();
        knn.setKNN(1);
        list.add(knn);

        
        J48 tree = new J48();
        tree.setOptions(new String[]{"-C", "0.35", "-S", "-A"});
        list.add(tree);



        NaiveBayes nb = new NaiveBayes();
        nb.setOptions(new String[] {"-D"});
        list.add(nb);


        NBTree nbTree = new NBTree();
        list.add(nbTree);


        Logistic lr = new Logistic();
        list.add(lr);



        List<Instances> bags = getBags(train, list.size());

        for (int i = 0; i < bags.size(); i++) {
            Classifier c = list.get(i);
            Instances bag = bags.get(i);
            c.buildClassifier(bag);
            Log.addLogToThisPath(Arrays.toString(c.getOptions())+"\n", path);
        }

        return list;
    }


    /**
     * Create bags for the classifiers
     * @param train Training set
     * @param total_no_of_bags double total number of bags
     * @return List<Instances> representing the bag as each item
     */
    private static List<Instances> getBags(Instances train, double total_no_of_bags) {

        // separate the instances
        HashMap<String, Instances> map = readAndSeparateClass(train);

        Instances bagPositive = map.get(CLASS_POSITIVE);
        Instances bagNegative = map.get(CLASS_NEGATIVE);

        int unit_size_pos = (int) (bagPositive.numInstances() / total_no_of_bags);
        int unit_size_neg = (int) (bagNegative.numInstances() / total_no_of_bags);


        int prevCountPos = 0;
        int prevCountNeg = 0;

        // generate bags
        List<Instances> bags = new ArrayList<Instances>();
        for(int i = 0; i < total_no_of_bags; i++){

            Instances bag = new Instances(bagPositive, prevCountPos, Math.abs(unit_size_pos));
            prevCountPos = prevCountPos +  unit_size_pos;

            for(int j = 0; j < unit_size_neg; j++ ){
                bag.add(bagNegative.instance(j + prevCountNeg));
            }

            prevCountNeg = prevCountNeg + unit_size_neg;
            bags.add(bag);

        }

        return bags;
    }

    /**
     * separate the given training file into positive and negative classes
     * @param trainFile Training file
     * @return
     */
    public static HashMap<String, Instances> readAndSeparateClass(Instances trainFile){


        Instances classTrue = null;
        Instances classFalse = null;

        for(int i = 0; i < trainFile.numInstances(); i++){
            Instance instance = trainFile.instance(i);
            String instanceClass = getClass(instance);

            if(instanceClass.equals(CLASS_POSITIVE)){
                if(classTrue == null) {
                    classTrue = new Instances(trainFile, i, i);
                }else{
                    classTrue.add(instance);
                }
            }else{
                if(classFalse == null){
                    classFalse = new Instances(trainFile, i, i);
                }else{
                    classFalse.add(instance);
                }
            }
        }


        HashMap<String, Instances> map = new HashMap<String, Instances>();
        map.put(CLASS_POSITIVE, classTrue);
        map.put(CLASS_NEGATIVE, classFalse);

        return map;
    }


    /**
     * Result holder
     * it saves the meta data about the training and testing results.
     */
    public static class Result{
        public int mTotalError;
        public int mTruePositive;
        public int mTrueNegative;
        public int mFalseNegative;
        public int mFalsePositive;

        public Result(int totalError, int tp, int tn, int fn, int fp) {
            this.mTotalError = totalError;
            this.mTruePositive = tp;
            this.mTrueNegative = tn;
            this.mFalseNegative = fn;
            this.mFalsePositive = fp;
        }

        /**
         * @return True Positive rate
         */
        public double getTruePositiveRate(){
            return (double)mTruePositive / (double) (mTruePositive + mFalseNegative);
        }

        /**
         * @return True Negative Rate
         */
        public double getTrueNegativeRate(){
            return (double)mTrueNegative / (double) (mFalsePositive + mTrueNegative);
        }

        @Override
        public String toString() {
            return String.format("Result: Error: %d\nTrue Positive Rate: %.2f\nTrue Negative Rate: %.2f",
                    mTotalError, getTruePositiveRate() , getTrueNegativeRate()) ;
        }
    }
}
