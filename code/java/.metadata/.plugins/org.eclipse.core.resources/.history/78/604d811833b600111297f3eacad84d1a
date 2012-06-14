package simplify.lm;

import java.util.ArrayList;
import java.util.HashMap;

import simplify.HashMapCounter;
import simplify.ParseTree;
import simplify.Word;

public class POSFeatureExtractor implements FeatureExtractor {

	private HashMap<String, String> coarseTags = new HashMap<String, String>();
	
	public POSFeatureExtractor(){
		initializeCourseTags();
	}
	
	private void initializeCourseTags(){
		// determiner mappings
		coarseTags.put("DT", "DET");
		coarseTags.put("PDT", "DET");
		
		// adjective mappings
		coarseTags.put("JJ", "ADJ");
		coarseTags.put("JJR", "ADJ");
		coarseTags.put("JJS", "ADJ");
		

		coarseTags.put("NN", "N");
		coarseTags.put("NNS", "N");
		coarseTags.put("NP", "N");
		coarseTags.put("NPS", "N");
		coarseTags.put("PRP", "N");
		coarseTags.put("FW", "N");
		
		// adverb mappings
		coarseTags.put("RB", "ADV");
		coarseTags.put("RBR", "ADV");
		coarseTags.put("RBS", "ADV");
		
		// verb mappings
		coarseTags.put("VB", "V");
		coarseTags.put("VBN", "V");
		coarseTags.put("VBG", "V");
		coarseTags.put("VBP", "V");
		coarseTags.put("VBZ", "V");
		coarseTags.put("MD", "V");
		
		// wh word mappings
		coarseTags.put("WDT", "WH");
		coarseTags.put("WP", "WH");
		coarseTags.put("WP$", "WH");
		coarseTags.put("WRB", "WH");
	}
	
	public ArrayList<Feature> getFeatures(ParseTree tree) {
		ArrayList<Feature> features = new ArrayList<Feature>();
		
		ArrayList<Word> words = tree.getWords();
		
		HashMapCounter<String> unigramCounter = new HashMapCounter<String>();
		HashMapCounter<String> bigramCounter = new HashMapCounter<String>();
		
		// collect the POS unigram and bigram counts
		for( int i = 0; i < words.size(); i++ ){
			String pos = words.get(i).getPos();
			
			if( i != 0 ){
				// collect bigram counts
				String prevPos = words.get(i-1).getPos();
				
				bigramCounter.increment(prevPos + " " + pos);
				
				if( coarseTags.containsKey(pos) ||
					coarseTags.containsKey(prevPos) ){
					String prev = coarseTags.containsKey(prevPos) ? coarseTags.get(prevPos) : prevPos;
					String cur = coarseTags.containsKey(pos) ? coarseTags.get(pos) : pos;
					
					bigramCounter.increment(prev + " " + cur);
				}
			}
			
			unigramCounter.increment(pos);
			
			if( coarseTags.containsKey(pos) ){
				unigramCounter.increment(coarseTags.get(pos));
			}
		}
		
		// calculate the length normalized frequency and generate a feature
		for( String uni: unigramCounter.keySet() ){
			features.add(new DoubleFeature(uni, ((double)unigramCounter.get(uni))/words.size()));
		}
		
		for( String bigram: bigramCounter.keySet() ){
			features.add(new DoubleFeature(bigram, ((double)bigramCounter.get(bigram))/(words.size()-1)));
		}
		
		return features;
	}
}
