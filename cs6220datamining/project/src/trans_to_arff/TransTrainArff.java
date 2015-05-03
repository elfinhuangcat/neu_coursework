package trans_to_arff;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
public class TransTrainArff {

	private final static String HEADER = "@relation Census-Income-Train\n\n"
			+ "@attribute age real\n"
			+ "@attribute workclass {Private, Self-emp-not-inc, Self-emp-inc, "
			+ "Federal-gov, Local-gov, State-gov, Without-pay, Never-worked}\n"
			+ "@attribute fnlwgt real\n"
			+ "@attribute education {Bachelors, Some-college, 11th, HS-grad, "
			+ "Prof-school, Assoc-acdm, Assoc-voc, 9th, 7th-8th, 12th, Masters, "
			+ "1st-4th, 10th, Doctorate, 5th-6th, Preschool}\n"
			+ "@attribute education-num real\n"
			+ "@attribute marital-status {Married-civ-spouse, Divorced, "
			+ "Never-married, Separated, Widowed, Married-spouse-absent, "
			+ "Married-AF-spouse}\n"
			+ "@attribute occupation {Tech-support, Craft-repair, Other-service, "
			+ "Sales, Exec-managerial, Prof-specialty, Handlers-cleaners, "
			+ "Machine-op-inspct, Adm-clerical, Farming-fishing, "
			+ "Transport-moving, Priv-house-serv, Protective-serv, "
			+ "Armed-Forces}\n"
			+ "@attribute relationship: {Wife, Own-child, Husband, Not-in-family, "
			+ "Other-relative, Unmarried}\n"
			+ "@attribute race {White, Asian-Pac-Islander, Amer-Indian-Eskimo, "
			+ "Other, Black}\n"
			+ "@attribute sex {Female, Male}\n"
			+ "@attribute capital-gain real\n"
			+ "@attribute capital-loss real\n"
			+ "@attribute hours-per-week real\n"
			+ "@attribute native-country {United-States, Cambodia, England, "
			+ "Puerto-Rico, Canada, Germany, Outlying-US(Guam-USVI-etc), India, "
			+ "Japan, Greece, South, China, Cuba, Iran, Honduras, Philippines, "
			+ "Italy, Poland, Jamaica, Vietnam, Mexico, Portugal, Ireland, "
			+ "France, Dominican-Republic, Laos, Ecuador, Taiwan, Haiti, "
			+ "Columbia, Hungary, Guatemala, Nicaragua, Scotland, Thailand, "
			+ "Yugoslavia, El-Salvador, Trinadad&Tobago, Peru, Hong, "
			+ "Holand-Netherlands}\n"
			+ "@attribute CLASS {>50K, <=50K}\n"
			+ "@data\n";
	
	public static void main(String[] args) throws IOException {
		/*******************************************
		 * @param args[0] input file path
		 * @param args[1] output arff file path
		 */
		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		BufferedWriter writer = new BufferedWriter(new FileWriter(args[1], false));
		writer.write(HEADER);
		writer.flush();
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.write(line + "\n");
		}
		writer.flush();
		reader.close();
		writer.close();
	}
}