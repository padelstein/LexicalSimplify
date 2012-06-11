package simplify.lm;

import java.util.ArrayList;

import simplify.ConstituentInfo;
import simplify.HashMapCounter;
import simplify.ParseTree;
import simplify.Word;

public class ParseTreeFeatureExtractor implements FeatureExtractor{

	public ArrayList<Feature> getFeatures(ParseTree tree) {
		ArrayList<Word> words = tree.getWords();
		ArrayList<ConstituentInfo> constituentInfo = tree.getConstituentInformation();
		
		// - count the number of NPs, VPs and PPs
		// - count the size of each of these
		// - length of the longest NP
		int longestNP = 0;
		HashMapCounter<String> numCounter = new HashMapCounter<String>();
		HashMapCounter<String> branchCounter = new HashMapCounter<String>();
		
		for( ConstituentInfo info: constituentInfo ){
			numCounter.increment(info.getLabel());
			branchCounter.increment(info.getLabel(), info.getBranch());
			
			if( info.getLabel().equals("NP") &&
				info.getBranch() > longestNP ){
				longestNP = info.getBranch();
			}
		}
		
		ArrayList<Feature> features = new ArrayList<Feature>();
		
		// ratio of the height of the tree to the number of words
		features.add(new DoubleFeature("HEIGHTRATIO", ((double)tree.height())/words.size()));
		
		ArrayList<String> trackMe = new ArrayList<String>();
		
		trackMe.add("NP");
		trackMe.add("PP");
		trackMe.add("VP");
		
		for( String constituent: trackMe ){	
			if( numCounter.containsKey(constituent) ){
				features.add(new DoubleFeature(constituent + "-COUNT", ((double)numCounter.get(constituent))/words.size()));
			}else{
				features.add(new DoubleFeature(constituent + "-COUNT", 0));
			}
			
			if( branchCounter.containsKey(constituent) ){
				features.add(new DoubleFeature(constituent + "-SIZE", ((double)branchCounter.get(constituent))/numCounter.get(constituent)));
			}else{
				features.add(new DoubleFeature(constituent + "-SIZE", 0));
			}
		}
		
		// add the ratio of the different constituents we're tracking
		for( String c1: trackMe ){
			for( String c2: trackMe ){
				if( !c1.equals(c2) ){
					if( numCounter.containsKey(c1) &&
							numCounter.containsKey(c2) ){
						features.add(new DoubleFeature(c1 + "-vs-" + c2, ((double)numCounter.get(c1))/numCounter.get(c2)));
					}else{
						features.add(new DoubleFeature(c1 + "-vs-" + c2, 0));
					}
				}
			}
		}
		
		features.add(new DoubleFeature("LARGEST-NP", longestNP));

		return features;
	}

}
