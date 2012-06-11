package simplify.lm;

public class LMExperimenter {
	private static final String SIMPLE_LM = "/Volumes/Data/drk04747/research/simplify/lm/data/lms/simple.lm";
	private static final String NORMAL_LM = "/Volumes/Data/drk04747/research/simplify/lm/data/lms/normal.lm";
	
	//private static final String SIMPLE_WORDS1 = ""
	
	public static void main(String[] args){
		//System.out.println("\\".matches("\\\\"));
		
		examineTrigramOverlap();
	}
	
	private static void examineTrigramOverlap(){
		LM simpleLM = new LM(SIMPLE_LM);
		LM normalLM = new LM(NORMAL_LM);
		
		int triOverlap = 0;
		int biOverlap = 0;
		int uniOverlap = 0;
		
		for( String trigram: simpleLM.getTrigrams() ){
			//System.out.println(trigram);
			if( normalLM.contains(trigram) ){
				triOverlap++;
			}
		}
				
		for( String bigram: simpleLM.getBigrams() ){
			if( normalLM.contains(bigram) ){
				biOverlap++;
			}
		}		
		
		for( String unigram: simpleLM.getUnigrams() ){
			if( normalLM.contains(unigram) ){
				uniOverlap++;
			}
		}
		
		System.out.println("unigram overlap: " + uniOverlap);
		System.out.println("bigram overlap: " + biOverlap);
		System.out.println("trigram overlap: " + triOverlap);
	}
}
