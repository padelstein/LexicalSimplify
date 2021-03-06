package simplify;

import java.io.*;
import java.util.ArrayList;

import simplify.lm.POSFeatureExtractor;

public class Test {
	
	private static String DATADIR = "/home/padelstein/LexicalSimplify/data/Parsed.aligned";
	private static String PARSED = DATADIR + "/sentences.parsed";
	private static String ALIGN = DATADIR + "/normal-simple.berkeley.align";
	
	
	public static void main(String[] argv) throws IOException{
		ExamplePairReader reader = new ExamplePairReader(PARSED, ALIGN);
				
		String[] parts = {"NN", "NNS", "JJ", "JJR", "JJS", "RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
		ArrayList<String> pos = new ArrayList<String>();
		for (int i=0; i<parts.length; i++){
			pos.add(parts[i]);
		}
		
		PrintWriter pr = new PrintWriter(new FileOutputStream("normalToSimple"));
		
		int cnt = 0;
		while( reader.hasNext() ){
		// while(cnt < 100){
			ExamplePair p = reader.next();
			Alignment align = p.getAlignment();
			ArrayList<Word> normalWords = p.getNormal().getWords();
			ArrayList<Word> simpleWords = p.getSimple().getWords();
			
			boolean hasPair = false;
			
			for( AlignPair pair: align){
				int n = pair.getNormalIndex();
				int s = pair.getSimpleIndex();
				Word normal = normalWords.get(n);
				Word simple = simpleWords.get(s);
				
				if( !normal.getWord().toLowerCase().equals(simple.getWord().toLowerCase()) && pos.contains(normal.getPos()) && normal.getPos().equals(simple.getPos())){
					cnt++;
					pr.println( p.getNormal().textString() + "\t" + n + "\t" + normal.getWord() + "\t " + simple.getWord());
				}
			}
			
			
			
		}
	pr.close();
	}
}