package simplify;

import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

/**
 * Class for grabbing the underlying text from a parse tree.
 * 
 * @author drk04747
 * @version 5/30/2011
 */

public class ParseTreeToText {

	public static void main(String[] args){
		if( args.length < 1 || args.length > 2){
			System.err.println("ParseTreeText <parse_file> [output_file]");
			System.exit(1);
		}
		
		PrintWriter out = null;
		
		if( args.length == 1 ){
			out = new PrintWriter(System.out);
		}else{
			try {
				out = new PrintWriter(new FileWriter(args[1]));
			} catch (IOException e) {
				System.err.println("Couldn't open file: " + args[1] + "\n" + e);
				System.exit(1);
			}
		}
		
		printText(args[0], out);
		out.close();
	}
	
	/**
	 * Given a file full of parse trees output the underlying text to "out"
	 * 
	 * @param parseFile The file containing the parse files
	 * @param out the Writer to print the text to
	 */
	public static void printText(String parseFile, PrintWriter out){
		try {
			Scanner scanner = new Scanner(new File(parseFile));
		
			String parseLine = null;
			
			while( scanner.hasNext() ){
				try {
					parseLine = scanner.nextLine();
					
					ParseTree tree = new ParseTree(parseLine);
					
					ArrayList<Word> words = tree.getWords();
					
					StringBuffer buffer = new StringBuffer();
					
					// if you want to do any processing based on the words, e.g. lowercase,
					// change based on pose (e.g. pos=CD -> %NUMBER%) this is the place to do it
					// don't change the number of words, though!
					//
					// if you need to change the number of words, do this BEFORE parsing the data.
					for( Word w: words ){
						buffer.append(w.getLabel() + " ");
					}
					
					// print out the text (minus the trailing whitespace)
					
					if( buffer.length() == 0 ){
						out.println("");
					}else{
						out.println(buffer.substring(0, buffer.length()-1));
					}
				} catch (MalformedTreeException e) {
					// if we have a problem parsing the tree, output a warning,
					// but try and do some simple regex parsing to grab the text
					
					System.err.println("WARNING, couldn't parse: \n" + parseLine + "\n" + e);
					
					// remove any string that starts with a (
					parseLine = parseLine.replaceAll("\\([^ ]*", "");
					
					// remove all left and right parens
					parseLine = parseLine.replaceAll("\\(", "");
					parseLine = parseLine.replaceAll("\\)", "");
					
					// remove all extra whitespace
					parseLine = parseLine.replaceAll("\\s+", " ");
					parseLine = parseLine.trim();
					
					out.println(parseLine);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't open file: " + parseFile + "\n" + e);
			System.exit(2);
		}
	}
}
