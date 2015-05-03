package myutil;

public class Result extends Object {
	private String identifier;
	private double errorRate;
	
	public Result() {
		super();
		this.identifier = null;
		this.errorRate = 0;
	}
	
	public Result(String identifier, double errorRate) {
		this.identifier = identifier;
		this.errorRate = errorRate;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public double getErrorRate() {
		return errorRate;
	}

	public void setErrorRate(double errorRate) {
		this.errorRate = errorRate;
	}
	
	
}