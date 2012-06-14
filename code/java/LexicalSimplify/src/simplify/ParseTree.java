package simplify;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseTree {
	public static void main(String[] args){
		DataReader reader = new DataReader("/Volumes/Data/drk04747/temp/sentences.simple");
		
		while( reader.hasNext() ){
			reader.next();
		}
	}
	
	private static Pattern treePattern = Pattern.compile("\\s*\\(\\s*(\\(.*\\))\\s*\\)\\s*");
	private ParseTreeNode top = null;
	
	public ParseTree(String treeString) throws MalformedTreeException{
		Matcher m = treePattern.matcher(treeString);
		
		// splice off the beginning and trailing parentheses
		if( treeString.equals("(())")){
			// this is an empty tree
			top = null;
		}
		//else if( !treeString.matches("\\(\\s*.*\\s*\\)") ){
		else if( !m.matches()){
			// not the right format
			throw new MalformedTreeException("Malformed tree: " + treeString);
		}else{
			top = new ParseTreeNode(m.group(1), null);
		}
				
		// set the indices of the leaves
		setWordIndices();
		
		// set the spans (min and max) for the constituents
		top.calculateSpans();
	}
	
	private void setWordIndices(){
		ArrayList<Word> words = getWords();
		
		for( int i = 0; i < words.size(); i++ ){
			words.get(i).setIndex(i);
		}
	}
	
	// we need to make this a separate method since the word alignments aren't assigned until after the
	// tree has been constructed (by the ExamplePair class)
	public void calculateAlignedSpans(){
		top.calculateAlignedSpans();
	}
	
	public int height(){
		if( top == null ){
			return 0;
		}else{
			return top.height();
		}
	}
		
	public ArrayList<ConstituentInfo> getConstituentInformation(){
		if( top == null ){
			return new ArrayList<ConstituentInfo>();
		}else{
			return top.getConstituentInformation();
		}
	}
	
	public String toString(){
		if( top == null ){
			return "(())";
		}else{
			return "( " + top.toString() + " )";
		}
	}
	
	public String textString(){
		if( top == null ){
			return "";
		}else{
			return top.toString(false).replaceAll("\\s+", " ").trim();
		}
	}
	
	public String debugString(){
		if( top == null ){
			return "(())";
		}else{
			return top.toString(true, true);
		}
	}
	
	public ArrayList<Word> getWords(){
		if( top == null ){
			return new ArrayList<Word>();
		}else{
			return top.getWords();
		}
	}
	
	public ArrayList<Constituent> getConstituents(){
		if( top == null ){
			return new ArrayList<Constituent>();
		}else{
			return top.getConstituents();
		}
	}
}
