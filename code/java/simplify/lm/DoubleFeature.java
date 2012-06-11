package simplify.lm;

public class DoubleFeature implements Feature {
	private String label;
	private double value;
	
	public DoubleFeature(String label, double value){
		this.label = label;
		this.value = value;
	}
	
	public String getLabel(){
		return label;
	}
	
	public String getValue(){
		return Double.toString(value);
	}
}
