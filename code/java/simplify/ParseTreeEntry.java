package simplify;

import java.util.ArrayList;

public abstract class ParseTreeEntry {
	private WordSpan wordSpan = new WordSpan();
	private WordSpan alignmentSpan = new WordSpan();
	private ArrayList<ParseTreeEntry> alignment = new ArrayList<ParseTreeEntry>(2);
	
	abstract public String getLabel();
	
	public WordSpan getWordSpan(){
		return wordSpan;
	}
	
	public WordSpan getAlignmentSpan(){
		return alignmentSpan;
	}
	
	public ArrayList<ParseTreeEntry> getAlignment(){
		return alignment;
	}
	
	public void addAlignment(ParseTreeEntry e){
		alignment.add(e);
	}
}
