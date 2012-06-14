package simplify;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import simplify.readability.*;

import simplify.align.WordAlignmentVisualizer;

public class DataAnalyzer {
	
	public static void main(String[] args){
		//new WordAlignmentVisualizer(PARSE_FILE, ALIGN_FILE, "/Volumes/Data/drk04747/research/simplify/word_align/dave.temp", 20);
		//rewordDeleteMatch();
		//analyzeConstituents(Data.PARSE_FILE, Data.ALIGN_FILE);
		//analyzeInsertionsDeletions(Data.PARSE_FILE, Data.ALIGN_FILE);
		analyzeReadability(Data.PARSE_FILE, Data.ALIGN_FILE);
	}
	
	public static void analyzeReadability(String dataFile, String alignFile){
		ExamplePairReader reader = new ExamplePairReader(dataFile, alignFile);
		
		ReadabilityStats[] stats = ReadabilityMeasures.getStats(reader);
		
		System.out.println("Normal Flesch:\t" + ReadabilityMeasures.getFlesch(stats[0]));
		System.out.println("Simple Flesch:\t" + ReadabilityMeasures.getFlesch(stats[1]));
		System.out.println("Normal Flesch-Kincaid:\t" + ReadabilityMeasures.getFleschKincaid(stats[0]));
		System.out.println("Simple Flesch-Kincaid:\t" + ReadabilityMeasures.getFleschKincaid(stats[1]));
	}
	
	public static void analyzeWordAlignment(String dataFile, String alignFile){
		ExamplePairReader reader = new ExamplePairReader(dataFile, alignFile);

		// - how many words are just aligned to themselves
		// - how many words are aligned to themselves and other words
		// - how many words are unaligned
		// - how many words are aligned to other words, but have an occurrence of that same word
		//   in the sentence
		
		int total = 0;
		int alignedToSelf = 0;
		int alignedToSelfPlus = 0;
		int unaligned = 0;
		int unalignedWrong = 0;
		int alignedToWrong = 0;
		int alignedToWrongMultiple = 0;
		
		HashMapCounter<String> wrong = new HashMapCounter<String>();
		HashMapCounter<String> uWrong = new HashMapCounter<String>();
		HashMapCounter<String> wrong2 = new HashMapCounter<String>();
		HashMapCounter<String> wrongMulti = new HashMapCounter<String>();
		
		while( reader.hasNext() ){
			ExamplePair pair = reader.next();
			
			ParseTree normalTree = pair.getNormal();
			ParseTree simpleTree = pair.getSimple();
			
			ArrayList<Word> simpleWords = simpleTree.getWords();
			ArrayList<Word> normalWords = normalTree.getWords();
			
			for( Word w: normalTree.getWords() ){
				total++;
								
				ArrayList<ParseTreeEntry> aligned = w.getAlignment();
				
				if( aligned.size() == 0 ){
					unaligned++;
					
					// see if it should have been aligned
					if( wordCount(simpleWords, w) > 0 &&
						wordCount(normalWords, w) <= wordCount(simpleWords, w) ){
						unalignedWrong++;
						uWrong.increment(w.getLabel());
					}
				}else if( aligned.size() == 1 ){
					// see if it's aligned to itself
					if( aligned.get(0).getLabel().equalsIgnoreCase(w.getLabel())){
						alignedToSelf++;
					}else if( wordCount(simpleWords, w) > 0 &&
							  wordCount(normalWords, w) <= wordCount(simpleWords, w) ){
						alignedToWrong++;
						wrong.increment(w.getLabel());
						wrong2.increment(w.getLabel() + " -> " + aligned.get(0).getLabel());
						
						System.out.println();
						System.out.println(w.getLabel());
						System.out.println(normalTree.textString());
						System.out.println(simpleTree.textString());						
					}
				}else{
					// this word is aligned to more than one word
					boolean foundWord = false;
					
					for( ParseTreeEntry e: aligned ){
						if( e.getLabel().equalsIgnoreCase(w.getLabel()) ){
							foundWord = true;
						}
					}
					
					if( foundWord ){
						alignedToSelfPlus++;
					}else if( wordCount(simpleWords, w) > 0 &&
							  wordCount(normalWords, w) <= wordCount(simpleWords, w) ){
						alignedToWrongMultiple++;
						wrongMulti.increment(w.getLabel());
					}
				}
			}
		}
		
		System.out.println("Total words: " + total);
		System.out.println("Aligned: " + (total - unaligned) + " (" + ((double)(total-unaligned)/total) + ")");
		System.out.println("Unaligned: " + unaligned + " (" + ((double)unaligned/total) + ")");
		System.out.println("Unaligned wrong: " + unalignedWrong + " (" + ((double)unalignedWrong/total) + ")");
		System.out.println("Self-aligned: " + alignedToSelf + " (" + ((double)alignedToSelf/total) + ")");
		System.out.println("Self-aligned plus: " + alignedToSelfPlus + " (" + ((double)alignedToSelfPlus/total) + ")");
		System.out.println("Wrong-aligned: " + alignedToWrong + " (" + ((double)alignedToWrong/total) + ")");
		System.out.println("Wrong-aligned mult: " + alignedToWrongMultiple + " (" + ((double)alignedToWrongMultiple/total) + ")");
	
		System.out.println();
		System.out.println("-------------------------------");
		System.out.println("Wrong words");
		printSortedKeyValue(wrong, 50);
		System.out.println("-------------------------------");
		
		System.out.println();
		System.out.println("-------------------------------");
		System.out.println("Unaligned wrong words");
		printSortedKeyValue(uWrong, 50);
		System.out.println("-------------------------------");
		
		System.out.println();
		System.out.println("-------------------------------");
		System.out.println("Wrong words with alignment");
		printSortedKeyValue(wrong2, 50);
		System.out.println("-------------------------------");
		
		System.out.println();
		System.out.println("-------------------------------");
		System.out.println("Wrong words multi-aligned");
		printSortedKeyValue(wrongMulti, 50);
		System.out.println("-------------------------------");
	}
	
	public static int wordCount(ArrayList<Word> words, Word w){
		int count = 0;
		
		for( Word e: words ){
			if( e.getLabel().equalsIgnoreCase(w.getLabel()) ){
				count++;
			}
		}
		
		return count;
	}
	
	public static void analyzeConstituents(String parseFile, String alignFile){
		ExamplePairReader reader = new ExamplePairReader(parseFile, alignFile);

		int totalConstituents = 0;
		
		// the number of constituents aligned to a constituent of the same type
		HashMapCounter<String> equalCounts = new HashMapCounter<String>();
		int equalCountsTotal = 0;
		
		// the number of constituents aligned to a constituent of a different type
		HashMapCounter<String> changeCounts = new HashMapCounter<String>();
		int changeCountsTotal = 0;
		
		// the number of unaligned constituents
		HashMapCounter<String> unalignedCounts = new HashMapCounter<String>();
		int unalignedCountsTotal = 0;
		
		// the number of multi-aligned constituents
		HashMapCounter<String> multiAlignedCounts = new HashMapCounter<String>();
		int multiAlignedCountsTotal = 0;
		
		while( reader.hasNext() ){
			ExamplePair pair = reader.next();
					
			for( Constituent c: pair.getNormal().getConstituents()){
				totalConstituents++;
				
				ArrayList<ParseTreeEntry> alignment = c.getAlignment();
				
				if( alignment.size() == 0 ){
					unalignedCounts.increment(c.getLabel());
					unalignedCountsTotal++;
				}else if( alignment.size() > 1 ){
					multiAlignedCounts.increment(c.getLabel());
					multiAlignedCountsTotal++;
				}else{
					String alignedConst = ((Constituent)alignment.get(0)).getLabel();
					
					if( alignedConst.equals(c.getLabel()) ){
						equalCounts.increment(c.getLabel());
						equalCountsTotal++;
					}else{
						changeCounts.increment(alignedConst + " -> " + c.getLabel());
						changeCountsTotal++;
					}
				}
			}
		}
		
		System.out.println("Total constituents: " + totalConstituents);
		System.out.println("Unaligned: " + unalignedCountsTotal + " (" + ((double)unalignedCountsTotal/totalConstituents) + ")");
		System.out.println("Multiply aligned: " + multiAlignedCountsTotal + " (" + ((double)multiAlignedCountsTotal/totalConstituents) + ")");
		System.out.println("Number aligned to same type: " + equalCountsTotal + " (" + ((double)equalCountsTotal/totalConstituents) + ")");
		System.out.println("Number aligned to different type: " + changeCountsTotal + " (" + ((double)changeCountsTotal/totalConstituents) + ")");
		printSortedKeyValue(changeCounts, 50);
		System.out.println("-------------------------------");
		
	}
	
	public static void analyzeInsertionsDeletions(String parseFile, String alignFile){
		ExamplePairReader reader = new ExamplePairReader(parseFile, alignFile);

		int totalConstituents = 0;
		
		HashMapCounter<String> deletedCounts = new HashMapCounter<String>();
		int deletedNodes = 0;
		
		HashMapCounter<String> insertedCounts = new HashMapCounter<String>();
		int insertedNodes = 0;
		
		int strictCopy = 0;
		int strictReorder = 0;
		
		while( reader.hasNext() ){
			ExamplePair pair = reader.next();
					
			for( Constituent normalConstituent: pair.getNormal().getConstituents()){
				if( !normalConstituent.isPos() ){
					totalConstituents++;
				}
				
				ArrayList<ParseTreeEntry> alignment = normalConstituent.getAlignment();
				
				if( alignment.size() == 1 ){
					Constituent simpleConstituent = (Constituent)alignment.get(0);
	
					// see if this is a strict deletion
					deletedNodes += addStrictDeleted(normalConstituent, simpleConstituent, deletedCounts);
					
					// see if this is a strick insertion
					insertedNodes += addStrictInserted(normalConstituent, simpleConstituent, insertedCounts);
					
					if( isStrictlyAligned(normalConstituent, simpleConstituent) ){
						// see if they're in the same order
						ArrayList<ParseTreeNode> normalChildren = normalConstituent.getParseTreeNode().getActualChildren();
						ArrayList<ParseTreeNode> simpleChildren = simpleConstituent.getParseTreeNode().getActualChildren();
						
						boolean sameOrder = true;
						
						for( int i = 0; i < normalChildren.size() && sameOrder; i++ ){
							if( normalChildren.get(i).getLabel().getAlignment().get(0) !=
								simpleChildren.get(i).getLabel() ){
								sameOrder = false;
							}
						}
						
						if( sameOrder ){
							strictCopy++;
						}else{
							strictReorder++;
						}
						
					}
								
				}
			}
		}
		
		System.out.println("Total constituents (non-POS): " + totalConstituents);
		System.out.println("Number of strict copies: " + strictCopy + " (" + ((double)strictCopy/totalConstituents) + ")");
		System.out.println("Number of strict reorders: " + strictReorder + " (" + ((double)strictReorder/totalConstituents) + ")");
		System.out.println("Number of strictly deleted constituents: " + deletedNodes + " (" + ((double)deletedNodes/totalConstituents) + ")");
		printSortedKeyValue(deletedCounts, 50);
		System.out.println("-------------------------------");
		System.out.println("Number of strictly inserted constituents: " + insertedNodes + " (" + ((double)insertedNodes/totalConstituents) + ")");
		printSortedKeyValue(insertedCounts, 50);
		System.out.println("-------------------------------");
		
	}
	
	private static boolean isStrictlyAligned(Constituent normalConstituent, Constituent simpleConstituent){
		// see how many
		//
		if( normalConstituent.getParseTreeNode().getActualChildren().size() != 
			simpleConstituent.getParseTreeNode().getActualChildren().size() ){
			return false;
		}else{

			boolean allSimpleAligned = true;

			for( ParseTreeNode simpleChild: simpleConstituent.getParseTreeNode().getChildren() ){
				if( simpleChild.isTerminal() ){
					allSimpleAligned = false;
				}else{
					ArrayList<ParseTreeEntry> childAlignment = simpleChild.getLabel().getAlignment();

					if( childAlignment.size() != 1 ||
							((Constituent)childAlignment.get(0)).getParseTreeNode().getParent() != normalConstituent.getParseTreeNode() ){
						allSimpleAligned = false;
					}
				}
			}

			return allSimpleAligned;
		}
	}
	
	private static int addStrictDeleted(Constituent normalConstituent, Constituent simpleConstituent, HashMapCounter<String> deletedCounts){
		int count = 0;
		
		// for now, we'll classify a deletion as all simple children are aligned to children
		// of the normal node, but there are some normal nodes not aligned
		boolean allSimpleAligned = true;
		HashSet<ParseTreeNode> normChildAligned = new HashSet<ParseTreeNode>();
		
		for( ParseTreeNode simpleChild: simpleConstituent.getParseTreeNode().getChildren() ){
			if( simpleChild.isTerminal() ){
				allSimpleAligned = false;
			}else{
				ArrayList<ParseTreeEntry> childAlignment = simpleChild.getLabel().getAlignment();
			
				if( childAlignment.size() == 1 &&
					((Constituent)childAlignment.get(0)).getParseTreeNode().getParent() == normalConstituent.getParseTreeNode() ){
					normChildAligned.add(((Constituent)childAlignment.get(0)).getParseTreeNode());
				}else{
					allSimpleAligned = false;
				}
			}
		}
		
		if( allSimpleAligned ){
			// any nodes not aligned are deleted
			for( ParseTreeNode normalChild: normalConstituent.getParseTreeNode().getChildren() ){
				if( !normChildAligned.contains(normalChild)){
					count++;
					deletedCounts.increment(normalConstituent.getLabel() + "(" + normalChild.getLabelString() + ")");
				}
			}
		}
		
		return count;
	}
	
	private static int addStrictInserted(Constituent normalConstituent, Constituent simpleConstituent, HashMapCounter<String> insertedCounts){
		int count = 0;
		
		// for now, we'll classify a deletion as all simple children are aligned to children
		// of the normal node, but there are some normal nodes not aligned
		boolean allNormalAligned = true;
		HashSet<ParseTreeNode> simpleChildAligned = new HashSet<ParseTreeNode>();

		for( ParseTreeNode normalChild: normalConstituent.getParseTreeNode().getChildren() ){
			if( normalChild.isTerminal() ){
				allNormalAligned = false;
			}else{
				ArrayList<ParseTreeEntry> childAlignment = normalChild.getLabel().getAlignment();

				if( childAlignment.size() == 1 &&
						((Constituent)childAlignment.get(0)).getParseTreeNode().getParent() == simpleConstituent.getParseTreeNode() ){
					simpleChildAligned.add(((Constituent)childAlignment.get(0)).getParseTreeNode());
				}else{
					allNormalAligned = false;
				}
			}
		}
			
		if( allNormalAligned ){
			// any nodes not aligned are deleted
			for( ParseTreeNode simpleChild: simpleConstituent.getParseTreeNode().getChildren() ){
				if( !simpleChildAligned.contains(simpleChild)){
					count++;
					insertedCounts.increment(normalConstituent.getLabel() + "(" + simpleChild.getLabelString() + ")");
				}
			}
		}
		
		return count;
	}

		
	public static void analyzeWords(String dataFile, String alignFile){
		ExamplePairReader reader = new ExamplePairReader(dataFile, alignFile);
		
		int total = 0;
		int equal = 0;
		
		// deletion counting variables
		HashMapCounter<Integer> deletionCounts = new HashMapCounter<Integer>();
		int deletionOnly = 0;
		HashMapCounter<String> deletedWords = new HashMapCounter<String>();
		
		// insertion counting variables
		HashMapCounter<Integer> insertionCounts = new HashMapCounter<Integer>();
		int insertionOnly = 0;
		HashMapCounter<String> insertedWords = new HashMapCounter<String>();
		
		// reword counting variables
		HashMapCounter<Integer> rewordCounts = new HashMapCounter<Integer>();
		int rewordOnly = 0;
		
		HashMapCounter<String> changedWords = new HashMapCounter<String>();
		
		// reword-delete variables
		HashMapCounter<Integer> rewordDeleteCounts = new HashMapCounter<Integer>();
		int rewordDelete = 0;
				
		while( reader.hasNext() ){
			ExamplePair pair = reader.next();
			total++;
			
			ArrayList<Word> deleted = pair.deletionMatch();
			ArrayList<Word> inserted = pair.insertionMatch();
			ArrayList<String> reworded = pair.rewordMatch();
			int rewordDeleteNum = pair.rewordDeleteMatch();

			if( pair.isEqual() ){
				equal++;
			}else if(deleted != null &&
					deleted.size() > 0 ){
				deletionOnly++;
				deletionCounts.increment(deleted.size());

				if( deleted.size() < 3 ){
					for( Word d: deleted ){
						deletedWords.increment(d.getLabel());
					}
				}
			}else if( inserted != null &&
					inserted.size() > 0 ){
				insertionOnly++;
				insertionCounts.increment(inserted.size());

				if( inserted.size() < 3 ){
					for( Word i: inserted){
						insertedWords.increment(i.getLabel());
					}
				}
			}else if( reworded != null &&
					reworded.size() > 0 ){
				rewordOnly++;
				rewordCounts.increment(reworded.size());

				if( reworded.size() < 3 ){
					for( String r: reworded ){
						changedWords.increment(r);
					}
				}
			}else if( rewordDeleteNum > 0 ){						
				rewordDelete++;
				rewordDeleteCounts.increment(rewordDeleteNum);
			}
		}


		System.out.println("Equal: " + equal + " (" + ((double)equal/total) + ")");


		System.out.println("Deletion only: " + deletionOnly + " (" + ((double)deletionOnly/total) + ")");
		System.out.println("-------------------------------");

		//ArrayList<Integer> counts = new ArrayList<Integer>();
		//counts.addAll(deletionCounts.keySet());
		//Collections.sort(counts);

		for( int i = 1; i < 5; i++ ){
			int num = deletionCounts.get(i);
			System.out.println(i + ":\t" + num + " (" + ((double)num/total) + ")");
		}

		System.out.println("-------------------------------");

		System.out.println("Insertion only: " + insertionOnly + " (" + ((double)insertionOnly/total) + ")");
		System.out.println("-------------------------------");

		for( int i = 1; i < 5; i++ ){
			int num = insertionCounts.get(i);
			System.out.println(i + ":\t" + num + " (" + ((double)num/total) + ")");
		}

		System.out.println("-------------------------------");


		System.out.println("Reword only: " + rewordOnly + " (" + ((double)rewordOnly/total) + ")");
		System.out.println("-------------------------------");

		for( int i = 1; i < 5; i++ ){
			int num = rewordCounts.get(i);
			System.out.println(i + ":\t" + num + " (" + ((double)num/total) + ")");
		}

		System.out.println("-------------------------------");


		System.out.println("Reword-Delete only: " + rewordDelete + " (" + ((double)rewordDelete/total) + ")");
		System.out.println("-------------------------------");

		for( int i = 1; i < 6; i++ ){
			int num = rewordDeleteCounts.get(i);
			System.out.println(i + ":\t" + num + " (" + ((double)num/total) + ")");
		}

		System.out.println("-------------------------------");

		int remaining = total - equal - deletionOnly - insertionOnly - rewordOnly;
		System.out.println("Remaining: " + remaining + " (" + ((double)remaining/total) + ")");
		System.out.println("Total: " + total);


		System.out.println();
		System.out.println("-------------------------------");
		System.out.println("Deleted words");
		printSortedKeyValue(deletedWords, 50);
		System.out.println("-------------------------------");

		System.out.println();
		System.out.println("-------------------------------");
		System.out.println("Inserted words");
		printSortedKeyValue(insertedWords, 50);
		System.out.println("-------------------------------");


		System.out.println();
		System.out.println("-------------------------------");
		System.out.println("Changed words");
		printSortedKeyValue(changedWords, 50);
		System.out.println("-------------------------------");
	}
	
	private static void printSortedKeyValue(HashMapCounter<String> counter, int num){
		ArrayList<Map.Entry<String, Integer>> words = counter.sortedEntrySet();
		
		for( int i = 0; i < num && i < words.size(); i++ ){
			System.out.println(words.get(i).getKey() + ":\t" + words.get(i).getValue());
		}
	}
	
	
	public static int rewordDeleteMatch(){
		ArrayList<String> normalWords = new ArrayList<String>(Arrays.asList("e e e a b c d".split("\\s+")));
		ArrayList<String> simpleWords = new ArrayList<String>(Arrays.asList("a b c d".split("\\s+")));
		
		final int BIG_NUM = 100000;
		
		if( normalWords.size() < simpleWords.size() ){
			return -1;
		}else{
			int[][] num = new int[normalWords.size()+1][simpleWords.size()+1];
		
			for( int i = 0; i < num.length; i++ ){
				num[i][0] = i;
			}
			
			for( int i = 0; i < num[i].length; i++ ){
				num[0][i] = BIG_NUM;
			}
			
			// num[i][j] = max{
			//                 num[i-1][j-1]      if the ith word == the jth word
			//                 1 + num[i-1][j-1]  if they're not equal (reword)
			//                 1 + num[i-1][j]              (deletion)
			//                 }


			for( int i = 1; i < num.length; i++){
				for( int j = 1; j < num[0].length; j++ ){
					if( i < j ){
						// we can't recover from this
						num[i][j] = BIG_NUM;
					}else{
						int bestScore = num[i-1][j-1];

						if( !normalWords.get(i-1).equalsIgnoreCase(simpleWords.get(j-1)) ){
							bestScore++; // we had to reword
						}

						// see if it would be better to just delete the normal word

						bestScore = (bestScore < 1 + num[i-1][j]) ? bestScore : 1 + num[i-1][j];
						num[i][j] = bestScore;
					}
					
					System.out.print(num[i][j] + " ");
				}
				
				System.out.println();
			}

			return num[normalWords.size()][simpleWords.size()];
		} 
	}
}
