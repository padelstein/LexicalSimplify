package simplify;

import java.util.ArrayList;
import java.util.Iterator;

public class Alignment implements Iterable<AlignPair>{
	public static String BAD_SENTENCE_ALIGNMENT = "BAD_SENTENCE_ALIGNMENT";
	
	private ArrayList<AlignPair> alignment;
	boolean badSentenceAlignment = false;
	
	public Alignment(Alignment align){
		alignment = new ArrayList<AlignPair>(align.size());
		
		for( AlignPair p: align){
			alignment.add(p);
		}
	}
	
	public Alignment(String alignLine) throws BadAlignmentException{
		alignment = new ArrayList<AlignPair>();
		
		if( alignLine.equals(BAD_SENTENCE_ALIGNMENT)){
			badSentenceAlignment = true;
		}else{
			readAlignment(alignLine);
		}
	}
	
	private void readAlignment(String alignLine) throws BadAlignmentException{
		String[] alignments = alignLine.split("\\s+");

		for( int i = 0; i < alignments.length; i++ ){
			alignment.add(new AlignPair(alignments[i]));
		}
	}
	
	public AlignPair get(int index){
		return alignment.get(index);
	}
	
	public boolean contains(AlignPair pair){
		return alignment.contains(pair);
	}
	
	public boolean isBadSentenceAlignment(){
		return badSentenceAlignment;
	}
	
	public void add(AlignPair pair){
		if( !contains(pair) ){
			int index = 0;

			while( index < alignment.size() &&
					(alignment.get(index).getNormalIndex() < pair.getNormalIndex() ||
							(alignment.get(index).getNormalIndex() == pair.getNormalIndex() &&
									alignment.get(index).getSimpleIndex() < pair.getSimpleIndex()))){
				index++;
			}

			if( index == alignment.size() ){
				// it's the last thing, so just add it at the end
				alignment.add(pair);
			}else{
				// insert it
				alignment.add(index, pair);
			}
		}
	}
	
	public void remove(AlignPair pair){
		alignment.remove(pair);
	}
	
	public int size(){
		return alignment.size();
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		for( AlignPair p: alignment ){
			buffer.append(p);
			buffer.append(" ");
		}
		
		if( buffer.length() == 0 ){
			return "";
		}else{
			return buffer.substring(0, buffer.length()-1);
		}
	}

	@Override
	public Iterator<AlignPair> iterator() {
		// TODO Auto-generated method stub
		return alignment.iterator();
	}
}
