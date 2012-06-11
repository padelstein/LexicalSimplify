package simplify;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class representing a syntactic parse tree.  The parse tree is a recursive structure.
 * A parse tree consists of a label and, if it is not a terminal, children that are also
 * parse trees.
 * 
 * @author Dave Kauchak
 * @version 2/26/2011
 *
 */
public class ParseTreeNode {
	private static Pattern tagSplitPattern = Pattern.compile("(\\S+)\\s+(.+?)");
	
	private boolean terminal; // whether or not this is a terminal node in the tree
	private ParseTreeEntry label; // the label of this node of the tree, e.g. S, NP, VP, etc.
	
	private ParseTreeNode parent;
	private ArrayList<ParseTreeNode> children; // the sub parse trees of this node if it's a non-terminal
			
	/**
	 * Construct a new parse tree from a single line, parenthesized version of the parse tree.
	 * 
	 * @param treeString A single line containing a parenthesized version of a parse tree
	 */
	public ParseTreeNode(String treeString, ParseTreeNode parent) throws MalformedTreeException{
		// removing any leading or trailing whitespace
		treeString = treeString.trim();
		
		if( treeString.charAt(0) != '(' ||
			treeString.charAt(treeString.length()-1) != ')' ){
			throw new MalformedTreeException("\'" + treeString + "\'");
		}
		
		this.parent = parent;
		treeString = treeString.substring(1, treeString.length()-1);
		
		// if this still had parenthesis in it, it's a non-terminal
		if( treeString.contains("(") ){
			Matcher m = tagSplitPattern.matcher(treeString);
		
			// make sure this subtree is of the proper form
			if( m.matches() ){
				label = new Constituent(m.group(1), false, this);
				String childrenString = m.group(2);
				
				terminal = false;
				children = new ArrayList<ParseTreeNode>();
				
				int leftParenCount = 0;
				int start = 0;
				
				// find the subchildren and recursively create them
				for( int i = 0; i < childrenString.length(); i++ ){
					if( childrenString.charAt(i) == '(' ){
						leftParenCount++;
					}else if( childrenString.charAt(i) == ')' ){
						leftParenCount--;
						
						if( leftParenCount == 0 ){
							children.add(new ParseTreeNode(childrenString.substring(start, i+1), this));
							
							// the i+1th character should be a space (or we're off the end)
							start = i+2;
							
						}else if( leftParenCount < 0 ){
							throw new MalformedTreeException(treeString);
						}
					}
				}
			}else{
				throw new MalformedTreeException(treeString);
			}
			
		}else{
			// this is a part of speech
			// when we get down the the part of speech tag, create the POS node as a non-terminal
			// and then link in the terminal node
			terminal = false;
			
			Matcher m = tagSplitPattern.matcher(treeString);
			
			if( m.matches() ){
				label = new Constituent(m.group(1), true, this);
				children = new ArrayList<ParseTreeNode>(1);
				children.add(new ParseTreeNode(new Word(m.group(2), getLabelString()), this));
			}else{
				throw new MalformedTreeException("Malformed tree leaf: " + treeString);
			}
		}  
	}
	
	/**
	 * Construct a new parse tree leave without any children
	 * 
	 * @param label the constituent label for this node (or the word if it's a terminal)
	 */
	public ParseTreeNode(Word w, ParseTreeNode parent){//, boolean terminal){
		terminal = true;
		this.parent = parent;
		label = w;
	}
	
	/**
	 * Adds a child to this TreeNode from left to right
	 * 
	 * @param newChild the child to be added.
	 */
	// For now, don't allow this.  If you want to add it back in, make sure to properly handle setting
	// the parent.
	//public void addChild(ParseTreeNode newChild){
	//	children.add(newChild);
	//}
	
	/**
	 * Get the children of this parse tree.
	 * 
	 * @return an iterable object over the children parse trees
	 */
	public Iterable<ParseTreeNode> getChildren(){
		return children;
	}
	
	/**
	 * Get the children of this parse tree.
	 * 
	 * @return an iterable object over the children parse trees
	 */
	public ArrayList<ParseTreeNode> getActualChildren(){
		return children;
	}
	
	public ParseTreeNode getParent(){
		return parent;
	}
	
	public int height(){
		if( terminal ){
			return 0;
		}else{
			int max = 0;
			
			for( ParseTreeNode t: children ){
				int height = t.height();
				
				if( height > max ){
					max = height;
				}
			}
			
			return max;
		}
	}
	
	public ArrayList<ConstituentInfo> getConstituentInformation(){
		ArrayList<ConstituentInfo> list = new ArrayList<ConstituentInfo>();
		getConstituentInformation(list);
		return list;
	}
	
	private void getConstituentInformation(ArrayList<ConstituentInfo> list){
		// don't process leaves or POS tags
		if( !terminal &&
			!children.get(0).isTerminal() ){
			list.add(new ConstituentInfo(label.getLabel(), children.size()));
			
			for( ParseTreeNode t: children ){
				t.getConstituentInformation(list);
			}
		}
	}
	
	/**
	 * Get the constituent labels of the children of this parse tree.
	 * 
	 * @return an ArrayList of the labels
	 */
	public ArrayList<String> getChildrenLabels(){
		ArrayList<String> labels = new ArrayList<String>(children.size());
		
		if( !terminal ){		
			for(ParseTreeNode t: children){
				labels.add(t.getLabelString());
			}
		}
		
		return labels;
	}
	
	/**
	 * Get the child at index of this parse tree
	 * 
	 * @param index the index of the child to obtain
	 * @return the child at index
	 */
	public ParseTreeNode getChild(int index){
		return children.get(index);
	}
	
	/**
	 * Get the number of children/sub-trees for this parse tree
	 * 
	 * @return the number of children/sub-trees
	 */
	public int numChildren(){
		return children.size();
	}
	
	/**
	 * Checks if this parse tree is a terminal node or not
	 * 
	 * @return whether or not this parse tree is a terminal node
	 */
	public boolean isTerminal(){
		return terminal;
	}
	
	public ParseTreeEntry getLabel(){
		return label;
	}
	
	/**
	 * Get the constituent label for this parse tree.  If it is a terminal,
	 * this is the word.
	 * 
	 * @return the constituent label
	 */
	public String getLabelString(){
		return label.getLabel();
	}
	
	/**
	 * Get a string representation of this parse tree in parenthesized form
	 */
	public String toString(boolean printConstituents){
		return toString(printConstituents, false);
	}
	
	/**
	 * Get a string representation of this parse tree in parenthesized form
	 */
	public String toString(boolean printConstituents, boolean printSpans){
		if( terminal ){
			return label.getLabel();
		}else{
			StringBuffer buffer = new StringBuffer();
	
			if( printConstituents ){
				buffer.append("(");
				buffer.append(((Constituent)label).toString(printSpans));				
			}
			
			for(ParseTreeNode child: children){
				buffer.append(" ");
				buffer.append(child.toString(printConstituents, printSpans));
			}
			
			if( printConstituents ){
				buffer.append(")");
			}
			
			return buffer.toString();
		}
	}
	
	public String toString(){
		return toString(true);
	}
	
	public ArrayList<Word> getWords(){
		ArrayList<Word> words = new ArrayList<Word>();
		getWordsHelper(words);
		return words;
	}
	
	private void getWordsHelper(ArrayList<Word> words){
		if( terminal ){
			words.add((Word)label);
		}else{
			for( ParseTreeNode child: children ){
				child.getWordsHelper(words);
			}
		}
	}
	
	public ArrayList<Constituent> getConstituents(){
		ArrayList<Constituent> constituents = new ArrayList<Constituent>();
		
		getConstituentsHelper(constituents);
		
		return constituents;
	}
	
	private void getConstituentsHelper(ArrayList<Constituent> constituents){
		// for now, don't add the leaves (i.e. words
		if( !terminal ){
			// add this nodes constituent and then recurse
			constituents.add((Constituent)label);
			
			for( ParseTreeNode child: children ){
				child.getConstituentsHelper(constituents);
			}
		}
	}
	
	public void calculateSpans(){
		calculateMinSpan();
		calculateMaxSpan();
	}
	
	private int calculateMinSpan(){
		int minSpan;
		
		if( terminal ){
			minSpan = ((Word)label).getIndex();			
		}else{
			// the minimum span will be the index of the left-most child
			minSpan = children.get(0).calculateMinSpan();
			
			// still need to recurse on the other children, though, so they also get values
			for( int i = 1; i < children.size(); i++ ){
				children.get(i).calculateMinSpan();
			}
		}
		
		label.getWordSpan().setMinIndex(minSpan);
		
		return minSpan;
	}
	
	private int calculateMaxSpan(){
		int maxSpan;
		
		if( terminal ){
			maxSpan = ((Word)label).getIndex();
		}else{
			// the maximum span will be the index of the right-most child
			maxSpan = children.get(children.size()-1).calculateMaxSpan();
			
			// still need to recurse on the other children, though, so they also get values
			for( int i = 0; i < children.size()-1; i++ ){
				children.get(i).calculateMaxSpan();
			}
		}
		
		label.getWordSpan().setMaxIndex(maxSpan);
		
		return maxSpan;
	}
	
	public void calculateAlignedSpans(){
		calculateAlignedMinSpan();
		calculateAlignedMaxSpan();
	}
	
	private int calculateAlignedMinSpan(){
		int minSpan = -1;
		
		if( terminal ){
			ArrayList<ParseTreeEntry> aligned = ((Word)label).getAlignment();
			
			if( aligned.size() > 0 ){
				minSpan = Integer.MAX_VALUE;
				
				for( ParseTreeEntry entry: aligned){
					if( ((Word)entry).getIndex() < minSpan ){
						minSpan = ((Word)entry).getIndex();
					}
				}
			}
		}else{
			// get the minimum span from the children that are not -1 (i.e. aligned)
			for( ParseTreeNode child: children ){
				int childMin = child.calculateAlignedMinSpan();
				
				if( childMin != -1 ){
					// this child is aligned
					if( minSpan == -1 ){
						// first aligned child we've found, so this is the best we've got
						minSpan = childMin;
					}else if( childMin < minSpan ){
						// the child's minimum is less than the best found
						minSpan = childMin;
					}
				}
			}
		}
				
		label.getAlignmentSpan().setMinIndex(minSpan);
		
		return minSpan;
	}
	
	private int calculateAlignedMaxSpan(){
		int maxSpan = -1;
		
		if( terminal ){
			ArrayList<ParseTreeEntry> aligned = ((Word)label).getAlignment();
			
			if( aligned.size() > 0 ){
				maxSpan = 0;
				
				for( ParseTreeEntry entry: aligned){
					if( ((Word)entry).getIndex() > maxSpan ){
						maxSpan = ((Word)entry).getIndex();
					}
				}
			}
		}else{
			// get the minimum span from the children that are not -1 (i.e. aligned)
			for( ParseTreeNode child: children ){
				int childMax = child.calculateAlignedMaxSpan();
				
				if( childMax != -1 ){
					// this child is aligned
					if( childMax > maxSpan ){
						// handles both the case of:
						// 1. only aligned child we've found so far, so maxSpan == -1
						// 2. highest aligned child so far
						maxSpan = childMax;
					}
				}
			}
		}
				
		label.getAlignmentSpan().setMaxIndex(maxSpan);
		
		return maxSpan;
	}
}