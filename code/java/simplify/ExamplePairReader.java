package simplify;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ExamplePairReader implements Iterator<ExamplePair>{
	// indicates whether we have a single file with parsed pairs (use dataScanner) or separate aligned files
	// (use normalScanner and simpleScanner)
	private boolean combinedDataFile;
	
	private DataReader dataReader;
	
	private DataReader normalReader;
	private DataReader simpleReader;

	private Scanner alignScanner;
	
	private ExamplePair next;
	
	/**
	 * @param dataFile File containing parsed pairs, simple then normal, one per line (will contain twice
	 *                 as many lines as other files
	 * @param alignFile the alignment file (alignment indices are of the form <normal_index>-<simple_index>)
	 */
	public ExamplePairReader(String dataFile, String alignFile){
		// open up all of the readers
		try{
			dataReader = new DataReader(dataFile);
			alignScanner = new Scanner(new File(alignFile));
		}catch(FileNotFoundException e){
			throw new RuntimeException("Problems opening file\n" + e);
		}
		
		combinedDataFile = true;
		
		// read a line to prime things
		next = readNextExample();
	}
	
	/**
	 * Initialize it with the parsed files separated
	 * 
	 * @param normalParseFile the normal sentences, parsed
	 * @param simpleParseFile the corresponding simple sentences, parsed
	 * @param alignFile the alignment file
	 */
	public ExamplePairReader(String normalParseFile, String simpleParseFile, String alignFile){
		// open up all of the readers
		try{
			normalReader = new DataReader(normalParseFile);
			simpleReader = new DataReader(simpleParseFile);
			alignScanner = new Scanner(new File(alignFile));
		}catch(FileNotFoundException e){
			throw new RuntimeException("Problems opening file\n" + e);
		}
		
		combinedDataFile = false;
		
		// read a line to prime things
		next = readNextExample();
	}
	
	// returns null if we're at the end of the file
	private ExamplePair readNextExample(){
		if( (combinedDataFile && dataReader.hasNext()) ||
			(!combinedDataFile && normalReader.hasNext())){
				
			ParseTree normalTree;
			ParseTree simpleTree;
			
			if( combinedDataFile ){
				simpleTree = dataReader.next();
								
				if( !dataReader.hasNext() ){
					throw new RuntimeException("Data file contains odd number of lines");
				}
			
				normalTree = dataReader.next();
			}else{
				if( !simpleReader.hasNext() ){
					throw new RuntimeException("Mismatched simple file");
				}
				
				if( !normalReader.hasNext() ){
					throw new RuntimeException("Mismatched normal file");
				}
			
				
				simpleTree = simpleReader.next();
				normalTree = normalReader.next();
			}
			
			if( !alignScanner.hasNextLine() ){
				throw new RuntimeException("Mismatched alignment file");
			}
			
			String alignLine = alignScanner.nextLine();
			
			if( normalTree == null || 
				simpleTree == null ){				
				// read another example
				return readNextExample();
			}else{
				try {
					return new ExamplePair(normalTree, simpleTree, new Alignment(alignLine));
				} catch (BadAlignmentException e) {
					//System.out.println("Skipping problem alignment");
					
					// read the next one
					return readNextExample();
				}
			}
		}else{
			// make sure we're also done with the other files
			if( !combinedDataFile ){
				if( normalReader.hasNext() ){
					throw new RuntimeException("Mismatched normal file");
				}
			
				if( simpleReader.hasNext() ){
					throw new RuntimeException("Mismatched simple file");
				}
			}

			if( alignScanner.hasNextLine() ){
				throw new RuntimeException("Mismatched alignment file");
			}
			
			return null;
		}
	}

	public boolean hasNext() {
		return next != null;
	}

	public ExamplePair next() {
		ExamplePair temp = next;
		next = readNextExample();
		return temp;
	}

	public void remove() {
		// optional and won't implement
	}
}