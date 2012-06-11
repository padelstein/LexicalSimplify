package simplify;

public class AlignPair {
	private int normalIndex;
	private int simpleIndex;
	
	public AlignPair(String pair) throws BadAlignmentException{
		String[] parts = pair.split("-");
		
		// the alignment pairs are of the form <norma_index>-<simple_index>			
		if( parts.length != 2 ){
			throw new BadAlignmentException("Bad alignment part: " + pair);
		}
		
		normalIndex = Integer.parseInt(parts[0]);
		simpleIndex = Integer.parseInt(parts[1]);
		
		if( normalIndex < 0 ||
			simpleIndex < 0 ){
			throw new BadAlignmentException("Found alignment index < 0: " + pair);
		}
	}
	
	public AlignPair(int normalIndex, int simpleIndex) throws BadAlignmentException{
		if( normalIndex < 0 ||
				simpleIndex < 0 ){
			throw new BadAlignmentException("Found alignment index < 0: " + normalIndex + "-" + simpleIndex);
		}
		
		this.normalIndex = normalIndex;
		this.simpleIndex = simpleIndex;
	}

	public int getNormalIndex() {
		return normalIndex;
	}

	public void setNormalIndex(int normalIndex) {
		this.normalIndex = normalIndex;
	}

	public int getSimpleIndex() {
		return simpleIndex;
	}

	public void setSimpleIndex(int simpleIndex) {
		this.simpleIndex = simpleIndex;
	}
	
	public String toString(){
		return normalIndex + "-" + simpleIndex;
	}
	
	public boolean equals(Object o){
		if( !(o instanceof AlignPair) ){
			return false;
		}else{
			return ((AlignPair)o).normalIndex == normalIndex &&
			       ((AlignPair)o).simpleIndex == simpleIndex;
		}
	}
}
