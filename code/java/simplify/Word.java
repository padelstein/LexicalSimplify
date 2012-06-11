package simplify;

public class Word extends ParseTreeEntry{
	private String word;
	private String pos;
	private int index = -1;
	
	public Word(String word, String pos){
		this.word = word;
		this.pos = pos;
	}

	public String getLabel() {
		return word;
	}
	
	public String getWord(){
		return word;
	}
	
	public String getPos(){
		return pos;
	}
	
	public int getIndex(){
		if( index < 0 ){
			throw new RuntimeException("Asked for word index before initializing indices");
		}
		
		return index;
	}
	
	public void setIndex(int index){
		this.index = index;
	}
	
	
	public boolean equals(Word w){
		return word.equals(w.word);
	}
	
	public boolean equalsIgnoreCase(Word w){
		return word.equalsIgnoreCase(w.word);
	}
}