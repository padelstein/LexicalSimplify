package simplify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Scanner;

public class DataReader implements Iterator<ParseTree>{
	private Scanner scanner;

	private ParseTree next;
	private boolean hasNext = true;
	
	public DataReader(String dataFile){
		try{
			scanner = new Scanner(new File(dataFile));
		}catch(FileNotFoundException e){
			throw new RuntimeException("Problems opening file: " + dataFile + "\n" + e);
		}
	
		next = readNext();
	}
	
	public DataReader(InputStream stream){
		scanner = new Scanner(stream);
		
		// read a line to prime things
		next = readNext();
	}
	
	private ParseTree readNext(){
		if( !scanner.hasNextLine() ){
			hasNext = false;
			return null;
		}else{
			String nextLine = scanner.nextLine();
			
			// check to see if it's just a parse tree line or if it has other metadata
			String[] parts = nextLine.split("\\t");
			
			if( parts.length != 1 && parts.length != 3 ){
				// bad file
				throw new RuntimeException("Bad line found: " + nextLine);
			}
			
			String parseTreeLine;
			
			if( parts.length == 1 ){
				parseTreeLine = parts[0];
			}else{
				parseTreeLine = parts[2];
			}
			
			if( nextLine.equals("(())") ){
				System.out.println("Skipping (()))");
				return null;
			}else{
				try {
					return new ParseTree(parseTreeLine);
				} catch (MalformedTreeException e) {
					System.out.println("Skipping problem tree pair");
					System.out.println(e);
			
					// read the next one
					return null;
				}
			}
		}
	}
	
	@Override
	public boolean hasNext() {
		return hasNext;
	}

	/**
	 * Returns the next parse tree from the file.  If it encounters a problem parse tree
	 * or (()), it returns null, but does return something to keep consistency.
	 */
	public ParseTree next(){
		ParseTree temp = next;
		next = readNext();
		return temp;
	}

	@Override
	public void remove() {
		//optional and not implementing
	}

}
