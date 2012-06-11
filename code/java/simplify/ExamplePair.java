package simplify;

import java.util.ArrayList;

public class ExamplePair {
	private ParseTree normal;
	private ParseTree simple;
	private Alignment alignment;
	
	// align lines are of the form:
	// <normal_index>-<simple_index> ...
	public ExamplePair(String normalTreeString, String simpleTreeString, String alignLine) 
	  throws MalformedTreeException, BadAlignmentException{
		normal = new ParseTree(normalTreeString);
		simple = new ParseTree(simpleTreeString);
		
		// add in the word alignment
		alignment = new Alignment(alignLine);
		
		pushDownAlignment();
	}
	
	public ExamplePair(ParseTree normalTree, ParseTree simpleTree, Alignment alignment) throws BadAlignmentException{
		normal = normalTree;
		simple = simpleTree;
		this.alignment = alignment;
		
		pushDownAlignment();
	}
	
	private void pushDownAlignment() throws BadAlignmentException{
		addWordAlignments();
		
		// we can only do this after we've assigned the alignments to the words in the trees
		normal.calculateAlignedSpans();
		simple.calculateAlignedSpans();
		
		// this can only be done after the spans have been calculated
		addConstituentAlignments();
	}
			
	// for now, this will just add word alignments
	private void addWordAlignments() throws BadAlignmentException{
		ArrayList<Word> normalWords = normal.getWords();
		ArrayList<Word> simpleWords = simple.getWords();
		
		for( AlignPair align: alignment ){
			if( align.getNormalIndex() >= normalWords.size() ||
				align.getSimpleIndex() >= simpleWords.size() ){
				throw new BadAlignmentException("Alignment index > number of words");
			}
			
			// add the alignment
			normalWords.get(align.getNormalIndex()).addAlignment(simpleWords.get(align.getSimpleIndex()));
			simpleWords.get(align.getSimpleIndex()).addAlignment(normalWords.get(align.getNormalIndex()));
		}
	}
	
	/**
	 * Pre: addWordAlignments() must have been called
	 * 
	 */
	private void addConstituentAlignments(){
		// for all constituents in normal parse tree: n
		//   for all constituents in simple parse tree: s
		//     if two things pass these checks, align them
		//
		// add something to try and avoid things with one child all being aligned (in particular, the parts of speech)

		// compare
		for( Constituent n: normal.getConstituents()){
			for( Constituent s: simple.getConstituents() ){
				// check to see if these constituents should be aligned

				// the conditions for alignment are:
				// 1. only allow parts of speech to be aligned to other parts of speech and vice versa
				// 2. The words that simple is aligned to (align span) are fully contained in the span
				//    of the normal constituent
				// 3. The words that normal is aligned to (align span) are fully contained in the span
				//    of the simple constituent
				
				if( n.isPos() == s.isPos() &&
					n.getWordSpan().contains(s.getAlignmentSpan()) &&
					s.getWordSpan().contains(n.getAlignmentSpan()) ){
					n.getAlignment().add(s);
					s.getAlignment().add(n);
					
					/*System.out.println("Adding:");
					System.out.println(n.toString(true) + ", " + n.isPos());
					System.out.println(s.toString(true) + ", " + s.isPos());
					System.out.println();*/
				}
			}
		}
	}


	public boolean isEqual(){
		return normal.textString().equalsIgnoreCase(simple.textString());
	}

	/**
	 * Determines the number of words that must be deleted to turn the normal tree text
	 * into the simple text.  Returns -1 if cannot be done
	 * 
	 * @return Number of deletions to turn normal into simple.  -1 if it can't happen with deletions.
	 */
	public ArrayList<Word> deletionMatch(){
		return numDeletions(normal.getWords(), simple.getWords());
	}

	/**
	 * Determines the which words need to be deleted to turn sent1 into sent2.  Returns nullt if it cannot be done. 
	 * 
	 * @param sent1
	 * @param sent2
	 * @return The words deleted to turn sent1 into sent2.  null if it can't be done.
	 */
	private static ArrayList<Word> numDeletions(ArrayList<Word> sent1, ArrayList<Word> sent2){
		ArrayList<Word> deletions = new ArrayList<Word>();
		int index2 = 0;

		for( int index1 = 0; index1 < sent1.size(); index1++ ){
			if( index2 == sent2.size() ){
				// we've reached the end of the simple words, so just delete the remaining normal words
				deletions.add(sent1.get(index1));
			}else if( sent1.get(index1).equalsIgnoreCase(sent2.get(index2)) ){
				// this normal word matches, so just move simpleIndex along
				index2++;
			}else{
				// this normal word doesn't match, so delete it
				deletions.add(sent1.get(index1));
			}
		}

		if( index2 == sent2.size() ){
			// we got to the end with deletions only
			return deletions;
		}else{
			// we couldn't get to the end with deletions only
			return null;
		}
	}

	public ArrayList<Word> insertionMatch(){
		return numDeletions(simple.getWords(), normal.getWords());
	}

	public ArrayList<String> rewordMatch(){
		ArrayList<Word> normalWords = normal.getWords();
		ArrayList<Word> simpleWords = simple.getWords();

		ArrayList<String> reword = new ArrayList<String>();
		
		if( normalWords.size() == simpleWords.size() ){			
			for( int i = 0; i < normalWords.size(); i++ ){
				if( !normalWords.get(i).equalsIgnoreCase(simpleWords.get(i))){
					reword.add(normalWords.get(i).getLabel() + " -> " + simpleWords.get(i).getLabel());
				}
			}

			return reword;
		}else{
			return null;
		}
	}

	public int rewordDeleteMatch(){
		ArrayList<Word> normalWords = normal.getWords();
		ArrayList<Word> simpleWords = simple.getWords();

		final int BIG_NUM = 100000;

		if( normalWords.size() < simpleWords.size() ){
			return -1;
		}else{
			int[][] num = new int[normalWords.size()+1][simpleWords.size()+1];

			for( int i = 0; i < num.length; i++ ){
				num[i][0] = i;
			}

			for( int i = 0; i < num[0].length; i++ ){
				num[0][i] = BIG_NUM;
			}

			// num[i][j] = max{
			//                 num[i-1][j-1]      if the ith word == the jth word
			//                 1 + num[i-1][j-1]  if they're not equal (reword)
			//                 1 + num[i-1][j]              (deletion)
			//                 }


			for( int i = 1; i < num.length; i++){
				for( int j = 1; j < num[0].length; j++ ){
					if( i < j ){
						// we can't recover from this
						num[i][j] = BIG_NUM;
					}else{
						int bestScore = num[i-1][j-1];

						if( !normalWords.get(i-1).equalsIgnoreCase(simpleWords.get(j-1)) ){
							bestScore++; // we had to reword
						}

						// see if it would be better to just delete the normal word

						bestScore = (bestScore < 1 + num[i-1][j]) ? bestScore : 1 + num[i-1][j];
						num[i][j] = bestScore;
					}
				}
			}

			return num[normalWords.size()][simpleWords.size()];
		}
	}
	
	public boolean simpleEqualsNormal(){
		return normal.textString().equalsIgnoreCase(simple.textString());
	}

	public ParseTree getNormal() {
		return normal;
	}

	public ParseTree getSimple() {
		return simple;
	}
	
	public Alignment getAlignment(){
		return alignment;
	}

	public String toString(){
		return normal.textString() + "\n" + simple.textString();
	}
	
	public String debugString(){
		return normal.debugString() + "\n" + simple.debugString() + "\n" + alignment;
	}
}
