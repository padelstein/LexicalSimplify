package simplify.readability;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ReadabilityStatsOld {
	private int numWords = 0;
	private int numSentences = 0;
	private final int numTextLines = 0;
	private final int numBlankLines = 0;
	private int numSyllables = 0;
	private int numComplexWords = 0;

	private final Map<String, Integer> uniqueWords = new HashMap<String, Integer>();

	@Override
	public String toString() {
		return String.format("Stats:[words: %d, sentences: %d, text: %d, blank: %d, syllables: %d, complex: %d]",
				this.numWords, this.numSentences, this.numTextLines, this.numBlankLines, this.numSyllables,
				this.numComplexWords);
	}

	public void addWord(final String s) {
		final Integer i = this.uniqueWords.get(s);
		this.uniqueWords.put(s, i == null ? 1 : 1 + i.intValue());
		this.numWords++;
	}

	public int getNumBlankLines() {
		return this.numBlankLines;
	}

	public int getNumSentences() {
		return this.numSentences;
	}

	public int getNumTextLines() {
		return this.numTextLines;
	}

	public int getNumWords() {
		return this.numWords;
	}

	public int getNumComplexWords() {
		return this.numComplexWords;
	}

	public int getNumSyllables() {
		return this.numSyllables;
	}

	public Map<String, Integer> getUniqueWords() {
		return Collections.unmodifiableMap(this.uniqueWords);
	}

	public void setNumWords(int numWords) {
		this.numWords = numWords;
	}

	public void setNumSentences(int numSentences) {
		this.numSentences = numSentences;
	}

	public void setNumSyllables(int numSyllables) {
		this.numSyllables = numSyllables;
	}

	public void setNumComplexWords(int numComplexWords) {
		this.numComplexWords = numComplexWords;
	}
}