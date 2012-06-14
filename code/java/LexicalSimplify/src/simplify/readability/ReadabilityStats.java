package simplify.readability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import simplify.Word;

public class ReadabilityStats {
	private static final String END_SENTENCE = "[.!?]";
	
	private int numLines = 0;
	private int numSentences = 0;
	private int numSyllables = 0;
	private int numWords = 0;
	
	public void addLine( String line ){
		addLine(Arrays.asList(line.trim().split("\\s+")));
	}
	
	public void addWordLine( List<Word> words ){
		List<String> s = new ArrayList<String>(words.size());
		
		for( Word w: words ){
			s.add(w.getWord());
		}
		
		addLine(s);
	}
	
	public void addLine(List<String> words){		
		numLines++;
		numWords += words.size();
		
		for( String w: words ){
			
			if( w.matches(END_SENTENCE) ){
				numSentences++;
			}else{
				numSyllables += Syllable.syllable(w);
			}
		}
	}

	public int getNumLines() {
		return numLines;
	}

	public int getNumSyllables() {
		return numSyllables;
	}

	public int getNumWords() {
		return numWords;
	}
	
	public int getNumSentences() {
		return numSentences;
	}
}
