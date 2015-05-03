package myutil;
import java.util.ArrayList;

/**
 * The instance of this class is to store evaluation results.
 * @author elfin
 *
 */
public class ResultList {
	ArrayList<Result> resultList;
	public ResultList() {
		this.resultList = new ArrayList<Result>();
	}
	public ArrayList<Result> getResultList() {
		return resultList;
	}
	public void addResult(String identifier, double errorRate) {
		this.resultList.add(new Result(identifier, errorRate));
	}
}

