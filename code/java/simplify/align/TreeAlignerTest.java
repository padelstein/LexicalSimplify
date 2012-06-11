package simplify.align;

import simplify.ExamplePairReader;

public class TreeAlignerTest {
	private static final String LABEL = "/Volumes/Data/drk04747/research/simplify/data/cohnetal_test/sentence";

	public static void main(String[] args){
		cohnExampleTest();
	}
	
	public static void cohnExampleTest(){
		ExamplePairReader reader = new ExamplePairReader(LABEL + ".parsed", LABEL + ".align");
		
		while( reader.hasNext() ){
			System.out.println(reader.next().debugString());
		}
	}
}
