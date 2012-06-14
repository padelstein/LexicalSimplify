package simplify;

public class WordSpan{
	private int minIndex = -1;
	private int maxIndex = -1;
	
	public WordSpan(){
	}
	
	public WordSpan(int minIndex, int maxIndex){
		this.minIndex = minIndex;
		this.maxIndex = maxIndex;
	}

	public int getMinIndex() {
		if( minIndex == -1 ){
			throw new RuntimeException("Tried to access uninitialized minimum index");
		}
		
		return minIndex;
	}

	public void setMinIndex(int minIndex) {
		this.minIndex = minIndex;
	}

	public int getMaxIndex() {
		if( maxIndex == -1 ){
			throw new RuntimeException("Tried to access uninitialized maximum index");
		}
		
		return maxIndex;
	}

	public void setMaxIndex(int maxIndex) {
		this.maxIndex = maxIndex;
	}
	
	/**
	 * A span "contains" another span if:
	 * 	- both spans are non-empty
	 * 	- it's min index is less than or equal to the input span's min index
	 * 	- it's max index is greater than or equal to the input span's max index
	 * 
	 * @param otherSpan
	 * @return Whether or not this span contains the other span
	 */
	public boolean contains(WordSpan otherSpan){
		if( isEmpty() || otherSpan.isEmpty() ){
			return false;
		}else{
			return minIndex <= otherSpan.minIndex && maxIndex >= otherSpan.maxIndex;
		}
	}
	
	public boolean isEmpty(){
		return minIndex == -1 || maxIndex == -1;
	}
	
	public String toString(){
		if( minIndex == -1 &&
			maxIndex == -1 ){
			return "{}";
		}else if( minIndex == -1 ||
				  maxIndex == -1 ){
			throw new RuntimeException("Called toString on partially initialized WordSpan");
		}else{
			return "{" + minIndex + ", " + maxIndex + "}";
		}
	}
}
