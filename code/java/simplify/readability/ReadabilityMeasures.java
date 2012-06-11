package simplify.readability;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import simplify.DataReader;
import simplify.ExamplePair;
import simplify.ExamplePairReader;

public class ReadabilityMeasures {
	//return 206.835f - (1.015f * wordsPerSentence(stats)) - (84.6f * syllablesPerWords(stats));
	
	public static ReadabilityStats[] getStats(ExamplePairReader reader){
		ReadabilityStats[] pair = new ReadabilityStats[2];

		ReadabilityStats simpleStats = new ReadabilityStats();
		ReadabilityStats normalStats = new ReadabilityStats();
		
		while( reader.hasNext() ){
			ExamplePair ex = reader.next();
			
			normalStats.addWordLine(ex.getNormal().getWords());
			simpleStats.addWordLine(ex.getSimple().getWords());
		}
		
		pair[0] = normalStats;
		pair[1] = simpleStats;
		
		return pair;
	}
	
	public static ReadabilityStats getStats(Scanner scanner){
		ReadabilityStats stats = new ReadabilityStats();
		
		while( scanner.hasNextLine() ){
			stats.addLine(scanner.nextLine());
		}
		
		return stats;
	}
	
	public static double getFlesch(ReadabilityStats stats){
		double wordsPerSentence = (double)stats.getNumWords() / stats.getNumSentences();
		double syllablesPerWord = (double)stats.getNumSyllables() / stats.getNumWords();
		
		
		return 206.835 - (1.015 * wordsPerSentence) - (84.6 * syllablesPerWord);
	}
	
	public static double getFleschKincaid(ReadabilityStats stats){
		double wordsPerSentence = (double)stats.getNumWords() / stats.getNumSentences();
		double syllablesPerWord = (double)stats.getNumSyllables() / stats.getNumWords();
		
		return 0.39 * wordsPerSentence + 11.8*syllablesPerWord -15.59;
	}
	
	
	public static void main(String[] args) throws IOException{
		Scanner scanner = new Scanner(new File(
				"/Users/dkauchak/research/simplify/data/50/testing/simple.txt"));
		
		ReadabilityStats stats = ReadabilityMeasures.getStats(scanner);
		
		System.out.println("Flesh: " + getFlesch(stats));
		System.out.println("FleshKincaid: " + getFleschKincaid(stats));
		
		/*if( args.length != 1 ){
			System.err.println("simplify.readability.ReadabilityMeasures <file>");
			System.exit(1);
		}else{
			Scanner scanner = new Scanner(new File(args[0]));
			ReadabilityStats stats = ReadabilityMeasures.getStats(scanner);
			
			System.out.println("Flesh:\t" + getFlesch(stats));
			System.out.println("FleshKincaid:\t" + getFleschKincaid(stats));
		}*/
	}
}
