package simplify;

public class ConstituentInfo {
	private String label;
	private int branch;
	
	public ConstituentInfo(String label, int branch){
		this.label = label;
		this.branch = branch;
	}

	public String getLabel() {
		return label;
	}

	public int getBranch() {
		return branch;
	}
}
