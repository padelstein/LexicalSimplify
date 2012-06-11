package simplify.lm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import simplify.Data;
import simplify.ParseTree;
import simplify.Word;
import simplify.readability.Syllable;

public class SurfaceFeatureExtractor implements FeatureExtractor {
	private HashSet<String> be850;
	private HashSet<String> be1500;
	
	public SurfaceFeatureExtractor(){
		be850 = loadDictionary(Data.BE850);
		be1500 = loadDictionary(Data.BE1500);
	}
	
	private HashSet<String> loadDictionary(String file){
		HashSet<String> dict = new HashSet<String>();
		
		try {
			Scanner scanner = new Scanner(new File(file));
			
			while( scanner.hasNext() ){
				dict.add(scanner.next().toLowerCase());
			}
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Problems reading dictionary: " + file);
		}
		
		return dict;
	}
	
	public ArrayList<Feature> getFeatures(ParseTree tree) {
		ArrayList<Feature> features = new ArrayList<Feature>();
		
		ArrayList<Word> words = tree.getWords();
		
		// number of tokens
		//features.add(new DoubleFeature("NUMWORDS", words.size()));
		
		// number of words containing at least one letter
		// average number of syllables per word
		// average number of characters per word
		// longest word in characters
		// most number of syllables of any word
		// number of BE850 words
		// ratio of BE850 words
		// number of BE1500 words
		// ratio of BE1500 words
		
		int lexicalWords = 0; // number of words containing at least one letter
		int totalSyllables = 0; // average number of syllables per word
		int totalChars = 0; // average number of characters per word
		int longestWord = 0; // longest word in characters
		int mostSyllables = 0; // most number of syllables of any word
		int numBE850 = 0; // number of BE850 words
		int numBE1500 = 0; // number of BE1500 words
		
		int[] syllableCount = new int[5];
		
		for( Word w: words ){
			String word = w.getWord();
			
			if( word.matches(".*[a-zA-Z].*") ){
				lexicalWords++;
				
				int numSyllables = Syllable.syllable(word);
				
				if( numSyllables <= 5 ){
					syllableCount[numSyllables-1]++;
				}
				
				totalSyllables += numSyllables;
				
				if( numSyllables > mostSyllables ){
					mostSyllables = numSyllables;
				}
				
				totalChars += word.length();
				
				if( word.length() > longestWord ){
					longestWord = word.length();
				}
				
				if( be850.contains(word.toLowerCase()) ){
					numBE850++;
				}
				
				if( be1500.contains(word.toLowerCase()) ){
					numBE1500++;
				}
			}
		}
		
		// number of words containing at least one letter
		//features.add(new DoubleFeature("NUMLEXWORDS", lexicalWords));
		// total number of syllables
		//features.add(new DoubleFeature("SYLL", totalSyllables));
		// average number of syllables per word
		features.add(new DoubleFeature("AVGSYLL", (double)totalSyllables/lexicalWords));
		// total number of characters
		//features.add(new DoubleFeature("CHARS", totalChars));
		// average number of characters per word
		features.add(new DoubleFeature("AVGCHARS", (double)totalChars/lexicalWords));
		// longest word in characters
		//features.add(new DoubleFeature("LONGCHARS", longestWord));
		// most number of syllables of any word
		//features.add(new DoubleFeature("LONGSYLL", mostSyllables));
		// number of BE850 words
		for( int i = 0; i < syllableCount.length; i++ ){
			features.add(new DoubleFeature("SYLLABLES" + i, syllableCount[i]));
		}
		
		//features.add(new DoubleFeature("NUMBE850", numBE850));
		// ratio of BE850 words
		features.add(new DoubleFeature("RATIOBE850", (double)numBE850/lexicalWords));
		// number of BE1500 words
		//features.add(new DoubleFeature("NUMBE1500", numBE1500));
		// ratio of BE1500 words
		features.add(new DoubleFeature("RATIOBE1500", (double)numBE1500/lexicalWords));
		
		return features;
	}

}
