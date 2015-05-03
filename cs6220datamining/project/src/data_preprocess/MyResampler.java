package data_preprocess;

import java.util.Arrays;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.supervised.instance.Resample;

/**
 * @see http://weka.sourceforge.net/doc.dev/weka/filters/supervised/instance/Resample.html for valid options.
 * @author yaxin
 */
public class MyResampler {
	private int randomSeed;
	private double percentage;
	private double bias;
	public MyResampler() {
		this.randomSeed = (int) System.currentTimeMillis();
		this.percentage = 100;
		this.bias = 1;
	}
	public MyResampler(int randomSeed, double percentage, double bias) {
		this.randomSeed = randomSeed;
		this.percentage = percentage;
		this.bias = bias;
	}

	/**
	 * This method returns a balanced dataset. It assumes that the data has 
	 * only two classes and the class label column has already been set.
	 * @param data the data instances to be resampled
	 * @return the balanced data instances
	 * @author yaxin
	 */
	public static Instances resampleToBalance(Instances data) {	
		MyResampler myResampler = new MyResampler(
				(int) System.currentTimeMillis(), // seed
				MyResampler.computePercentageOfResampledData(data, 
						MyResampler.computeNumOfFirstClass(data)), // percentage
						1); // bias
		// 2. resampleData(data)
		return myResampler.resampleData(data);
	}

	public static double computePercentageOfResampledData(Instances data, 
			int numOfFirstClass) {
		int numOfSecondClass = data.numInstances() - numOfFirstClass;;
		return 100 * ((double)(data.numInstances() + 
				Math.abs(numOfFirstClass - numOfSecondClass)) 
				/ data.numInstances());
	}

	/**
	 * This method returns the number of instances whose label equals to the 
	 * first instance's label.
	 * @param data the data instances
	 * @return the number of instances whose label equals to the first instance's label.
	 */
	public static int computeNumOfFirstClass(Instances data) {
		int numOfFirstClass = 0;
		// We just pick the first intance's class value to be our first class.
		// Then another class value will be the second class.
		double labelOfFirstClass = data.instance(0).classValue();
		for (int i = 0; i < data.numInstances(); ++i) {
			if (data.instance(i).classValue() == labelOfFirstClass) {
				++numOfFirstClass;
			}
		}
		return numOfFirstClass;
	}


	/**
	 * This method returns the resampled data set given the original data.
	 * @param trainData the original data (usually the training dataset)
	 * @return the dataset balanced by the parameters set before using this method.
	 */
	public Instances resampleData(Instances trainData) {
		Resample resampler = new Resample();
		resampler.setBiasToUniformClass(this.bias);
		resampler.setRandomSeed(this.randomSeed);
		resampler.setSampleSizePercent(this.percentage);
		System.out.println("INFO - Resampler options: " + Arrays.toString(resampler.getOptions()));
		try {
			resampler.setInputFormat(trainData);
			Instances newTrainData = Resample.useFilter(trainData, resampler);
			return newTrainData;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	// Getters and Setters
	public int getRandomSeed() {
		return randomSeed;
	}
	public void setRandomSeed(int randomSeed) {
		this.randomSeed = randomSeed;
	}
	public double getPercentage() {
		return percentage;
	}
	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}
	public double getBias() {
		return bias;
	}
	public void setBias(double bias) {
		this.bias = bias;
	}

	/**
	 * This is the test code. Should not be in actual use.
	 * @param args[0] training data path
	 * @author yaxin
	 */
	public static void main(String[] args) {
		DataSource trainSource;
		try {
			trainSource = new DataSource(args[0]);
			Instances trainData = trainSource.getDataSet();
			trainData.setClassIndex(trainData.numAttributes() - 1);
			System.out.println("Num of instances in train set: " + 
					trainData.numInstances());
			System.out.println("Num of first class instances: " + 
					MyResampler.computeNumOfFirstClass(trainData));


			Instances newTrainData = MyResampler.resampleToBalance(trainData);
			System.out.println("======After resampling======");
			System.out.println("Num of instances in train set: " + newTrainData.numInstances());
			System.out.println("Num of first class instances: " + MyResampler.computeNumOfFirstClass(newTrainData));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}