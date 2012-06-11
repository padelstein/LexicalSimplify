package simplify.align;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import simplify.ExamplePairReader;
import simplify.ExamplePair;

/**
 * A temporary class to fix my alignment mistake :(
 * 
 * @author drk04747
 *
 */
public class AlignmentFixerUpper {
	private static final String PARSE_FILE = "/Volumes/Data/drk04747/research/simplify/data/50/training/sentences.parsed";
	private static final String ALIGN_FILE = "/Volumes/Data/drk04747/research/simplify/data/50/align/normal-simple.berkeley.align";
	private static final String LABEL = "/Volumes/Data/drk04747/research/simplify/word_align/dave.6.8";
	
	public static void main(String[] args){
		ExamplePairReader mainReader = new ExamplePairReader(PARSE_FILE, ALIGN_FILE);
		ExamplePairReader labeledReader = new ExamplePairReader(LABEL + ".normal", LABEL + ".simple", LABEL + ".hand.align");
		
		PrintWriter out = null;
		
		try {
			out = new PrintWriter(new FileOutputStream(LABEL + ".berkeley.align"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while( labeledReader.hasNext() ){
			ExamplePair next = labeledReader.next();
			
			// find this pair
			ExamplePair p = mainReader.next();
			
			while( !p.getNormal().toString().equals(next.getNormal().toString()) ){
				p = mainReader.next();
			}
			
			out.println(p.getAlignment());
		}
		
		out.close();
	}
}
