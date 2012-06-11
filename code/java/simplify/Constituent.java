package simplify;

public class Constituent extends ParseTreeEntry{
	private String label;
	private boolean pos;
	private ParseTreeNode node;
	
	public Constituent(String label, boolean pos, ParseTreeNode node){
		this.label = label;
		this.pos = pos;
		this.node = node;
	}

	public String getLabel() {
		return label;
	}
	
	public boolean isPos(){
		return pos;
	}
	
	public ParseTreeNode getParseTreeNode(){
		return node;
	}
	
	public String toString(){
		return label;
	}
	
	public String toString(boolean printSpance){
		return label + "[W:" + getWordSpan() + " A:" + getAlignmentSpan() + "]";
	}
}
