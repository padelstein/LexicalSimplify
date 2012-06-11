package simplify.lm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LM {
	private HashMap<String, Double> probs[];
	
	public LM(String arpaFile){
		loadARPA(arpaFile);
	}
	
	@SuppressWarnings("unchecked")
	private void loadARPA(String arpaFile){
		Scanner scanner;
		
		try{
			scanner = new Scanner(new File(arpaFile));
		}catch(FileNotFoundException e){
			throw new RuntimeException("Problems opening file\n" + e);
		}
		
		String section = null;
		
		int ngramSizes = 0;
		int currentNgram = 0;
		
		while( scanner.hasNextLine() ){
			String line = scanner.nextLine();
			
			//System.out.println(line);
			
			if( line.equals("") ||
				line.equals("\\end\\")){
				// skip it
			}else if( line.equals("\\data\\") ){
				section = "data";
			}else if( line.matches(".*[0-9]-grams:") ){
				section = line;
				
				// figure out what n-gram it is
				Matcher m = Pattern.compile(".*([0-9])-grams:").matcher(line);
				System.out.println(m.matches());
				
				currentNgram = Integer.parseInt(m.group(1));
				
				System.out.println("Loading " + currentNgram + "-grams");
				
				if( currentNgram == 1 ){
					// initialize the tables
					if( ngramSizes == 0 ){
						throw new RuntimeException("Failed to find ngram size from arpa file: " + arpaFile);
					}
					
					probs = (HashMap<String, Double>[])new HashMap[ngramSizes];
					
					for( int i = 0; i < ngramSizes; i++ ){
						probs[i] = new HashMap<String, Double>();
					}
				}
			
			}else if( section != null ){
				if( !section.equals("data") ){
					String[] parts = line.split("\\t+");
					
					probs[currentNgram-1].put(parts[1], Double.parseDouble(parts[0]));
				}else{
					// see if we can figure out the size of the model
					Matcher m = Pattern.compile("ngram (\\d)=.*").matcher(line);
					
					if( m.matches() ){
						ngramSizes = Integer.parseInt(m.group(1));
					}
				}
			}
		}
	}
	
	public boolean contains(String ngram){
		int length = countWords(ngram);
		return length-1 < probs.length ? probs[length-1].containsKey(ngram) : false;	
	}
	
	public double getLogProb(String ngram){
		int length = countWords(ngram);
		return contains(ngram) ? probs[length-1].get(ngram) : Double.NEGATIVE_INFINITY;
	}
	
	private int countWords(String ngram){
		return ngram.split("\\s+").length;
	}
	
	public Iterable<String> getTrigrams(){
		return probs[2].keySet();
	}
	
	public Iterable<String> getBigrams(){
		return probs[1].keySet();
	}
	
	public Iterable<String> getUnigrams(){
		return probs[0].keySet();
	}
}
