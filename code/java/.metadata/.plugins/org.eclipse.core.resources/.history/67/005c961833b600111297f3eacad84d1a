package simplify.align;

import java.io.*;

import java.util.ArrayList;
import java.util.Scanner;

import simplify.AlignPair;
import simplify.Alignment;
import simplify.BadAlignmentException;
import simplify.ExamplePairReader;
import simplify.ExamplePair;

public class AlignmentEvaluator {
	private static final String LABEL = "/Volumes/Data/drk04747/research/simplify/word_align/labeled_data/dave.6.8/dave.6.8";
	
	public static void main(String[] args){
		evaluate(LABEL + ".berkeley.align", LABEL + ".hand.align", LABEL + ".normal", LABEL + ".simple");
	}
	
	public static void evaluate(String proposedAlignFile, String correctAlignFile, String normalFile, String simpleFile){
		double precision = 0.0;
		double recall = 0.0;
		double f1 = 0.0;
		
		double nonEqualPrecision = 0.0;
		double nonEqualRecall = 0.0;
		double nonEqualF1 = 0.0;
		
		int count = 0;
		int nonEqualCount = 0;
		
		try {
			Scanner correctScanner = new Scanner(new File(correctAlignFile));
			ExamplePairReader reader = new ExamplePairReader(normalFile, simpleFile, proposedAlignFile);
			
			while( reader.hasNext() ){
				try {
					ExamplePair pair = reader.next();
					Alignment correct = new Alignment(correctScanner.nextLine());
					
					if( !correct.isBadSentenceAlignment() ){
						double p = precision(pair.getAlignment(), correct);
						double r = recall(pair.getAlignment(), correct);
						double f = (2*p*r)/(p+r);
						
						precision += p;
						recall += r; 
						f1 += f;
						
						count++;
						
						if( !pair.simpleEqualsNormal() ){
							nonEqualPrecision += p;
							nonEqualRecall += r;
							nonEqualF1 += f;
							
							nonEqualCount++;
						}
					}
				} catch (BadAlignmentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("-------All pairs with correct sentence alignments-------");
		System.out.println("Precision:\t" + (precision/count));
		System.out.println("Recall:\t" + (recall/count));
		System.out.println("F1:\t\t" + (f1/count));
		System.out.println();
		
		System.out.println("-------All pairs with correct sentence alignments that are NOT equal-------");
		System.out.println("Precision:\t" + (nonEqualPrecision/nonEqualCount));
		System.out.println("Recall:\t" + (nonEqualRecall/nonEqualCount));
		System.out.println("F1:\t\t" + (nonEqualF1/nonEqualCount));
	}
	
	/**
	 * For all of the alignments proposed, how many are right (i.e. found in correct)?
	 * 
	 * @param proposed
	 * @param correct
	 * @return
	 */
	public static double precision(Alignment proposed, Alignment correct){
		int correctCount = 0;
		
		for( AlignPair p: proposed ){
			if( correct.contains(p) ){
				correctCount++;
			}
		}
		
		return ((double)correctCount)/proposed.size();
	}
	
	/**
	 * For all of the correct alignments, how many were found in the proposed alignment?
	 * 
	 * @param proposed
	 * @param correct
	 * @return
	 */
	public static double recall(Alignment proposed, Alignment correct){
		int correctCount = 0;
		
		for( AlignPair p: correct ){
			if( proposed.contains(p) ){
				correctCount++;
			}
		}
		
		return ((double)correctCount)/correct.size();
	}
}
