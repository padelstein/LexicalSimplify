package simplify;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.requester.Comparator;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.requester.Locale;
import com.amazonaws.mturk.requester.QualificationRequirement;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

//import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
//import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

//import simplify.LexicalSubSurvey.Worker;

/**
 * A class to create and analyze lexical simplification HITs
 * Patrick Adelstein, Teddy Pendergast, David Kauchack, Middlebury College Summer 2012
 */
public class LexicalSubSurvey
{

	// pointers to the data
	private static String DATADIR = "/home/padelstein/LexicalSimplify/data/Parsed.aligned";
	private static String PARSED = DATADIR + "/sentences.parsed";
	private static String ALIGN = DATADIR + "/normal-simple.berkeley.align";

	private RequesterService service;

	// Define the properties of the HIT to be created.
	private String contextGivenTitle = "Suggest a Simpler Word in the Sentence";
	private String noTargetGivenTitle = "Fill in the Blank with a Simpler Word";
	private String noContextGivenTitle = "Suggest a Simpler Word";
	private String partialContextTitle = "Suggest a Simpler Word in the given Context";
	private String contextGivenDescription = 
		"Replace a word with a simple substitute in the given sentence.";
	private String noTargetGivenDescription = 
		"Suggest a simple word in the given sentence.";
	private String noContextGivenDescription = 
		"Replace a word with a simple substitute.";
	private String partialContextDescription = 
		"Replace a word with a simple substitute with partial context provided.";
	private int numAssignments = 10;
	private double reward = 00.02;

	// define the writers for storage of HIT IDs
	private PrintWriter noContextpr;
	private PrintWriter contextpr;
	private PrintWriter partialContextpr;
	private PrintWriter noContextAnswerOutput;
	private PrintWriter contextAnswerOutput;
	private PrintWriter workerOutput;
	private PrintWriter substitutionOutput;

	private QualificationRequirement acceptanceRate = new QualificationRequirement("000000000000000000L0", Comparator.GreaterThanOrEqualTo, 95, null, false);
	private QualificationRequirement location = new QualificationRequirement("00000000000000000071", Comparator.EqualTo, null, new Locale("US"), false);
	private QualificationRequirement[] requirements = {acceptanceRate, location};

	ArrayList<String> noContextAnswers = new ArrayList<String>();
	ArrayList<String> contextAnswers= new ArrayList<String>();
	ArrayList<String> noTargetAnswers= new ArrayList<String>() ;;

	Map<String, String> hitIdtoType = new HashMap<String, String>();
	private ArrayList<SentenceHIT> contextHITs = new ArrayList<SentenceHIT>();
	private ArrayList<SentenceHIT> contextHITs1 = new ArrayList<SentenceHIT>();
	private ArrayList<SentenceHIT> contextHITs2 = new ArrayList<SentenceHIT>();
	private ArrayList<SentenceHIT> noContextHITs = new ArrayList<SentenceHIT>();
	private ArrayList<SentenceHIT> noContextHITs1 = new ArrayList<SentenceHIT>();
	private ArrayList<SentenceHIT> noContextHITs2 = new ArrayList<SentenceHIT>();

	private ArrayList<Worker> workers = new ArrayList<Worker>();
	private int HITindex = 0;

	/**
	 * Constructor
	 */
	public LexicalSubSurvey()
	{
		service = new RequesterService(new PropertiesClientConfig());
	}

	// a method to create a HIT with context and the target word provided
	public void createContextGivenSurvey(String firstSentence, String word, String secondSentence) throws FileNotFoundException
	{
		try 
		{
			HIT hit = service.createHIT
			(
					null,
					contextGivenTitle,
					contextGivenDescription,
					null,
					contextGivenSub(firstSentence, word, secondSentence),
					reward,
					(long)300,
					(long)432000, (long)172800, numAssignments,
					"", requirements, null
			);

			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
			contextpr.println(hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());

		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}

	// a method to create a HIT with the context given but the target word omitted
	public void createNoTargetGivenSurvey(String firstSentence, String word, String secondSentence) throws FileNotFoundException
	{
		try 
		{
			HIT hit = service.createHIT
			(
					null,
					noTargetGivenTitle,
					noTargetGivenDescription,
					null,
					noTargetGivenSub(firstSentence, word, secondSentence),
					reward,
					(long)300,
					(long)432000, (long)172800, numAssignments,
					"", requirements, null);

			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
//			noTargetpr.println(hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());

		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}

	// a method to create a HIT with target word given but the context omitted
	public void createNoContextGivenSurvey(String firstSentence, String word, String secondSentence, String sense, String POS) throws FileNotFoundException
	{
		try 
		{
			HIT hit = service.createHIT
			(
					null,
					noContextGivenTitle,
					noContextGivenDescription,
					null,
					noContextGivenSub(
							firstSentence, word, secondSentence, sense, POS),
							reward,
							(long)300,
							(long)432000, (long)172800, numAssignments,
							"", requirements, null);


			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
			noContextpr.println(hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());


		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}
	
	private void createPartialContextGivenSurvey(String partialFirst, String target, String partialSecond) {
		try 
		{
			HIT hit = service.createHIT
			(
					null,
					partialContextTitle,
					partialContextDescription,
					null,
					partialContextSub(
							partialFirst, target, partialSecond),
							reward,
							(long)300,
							(long)432000, (long)172800, numAssignments,
							"", requirements, null);


			// Print out the HITId and the URL to view the HIT.
			System.out.println("Created HIT: " + hit.getHITId());
			partialContextpr.println(hit.getHITId());
			System.out.println("HIT location: ");
			System.out.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());


		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}


	// deletes all HITs we have IDs stored for in NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
	public void deleteHIT(String hitId) throws IOException
	{
		try 
		{
			hitId.trim();
			service.disableHIT
			(
					hitId
			);

			// Print out the HITId and the URL to view the HIT.
			System.out.println("Removed HIT: " + hitId);

		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}

	//  gets the results for all the HITs we have IDs stored for in NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs
	public void getAssignmentsHIT(String hitId, Map<String, String[]> wordToSense) throws IOException
	{
		try 
		{
			SentenceHIT currentHIT = new SentenceHIT(hitId, wordToSense);

			ArrayList<String>  compareList = null;
			// Print out the HITId and the URL to view the HIT.
			System.out.println("Retrieved HIT: " + hitId + " " + currentHIT.typeID );

			//uses hittypeId to check what kind of hit we are looking at
			if ( currentHIT.typeID.equals("25D2JE1M7PKKF8JGAZQAK04LZYTXQE") ){
				noContextHITs.add(currentHIT);
				compareList = noContextAnswers;
			} else if ( currentHIT.typeID.equals("20ASTLB3L0FBPWA8FU5JZEVE5SUJV7") ){
				contextHITs.add(currentHIT);
				compareList = contextAnswers;
				HITindex++;
			} else {
				return;
			}

			for (Assignment answer: currentHIT.assignments ){
				String workerID = answer.getWorkerId();
				Worker currentWorker = null;
				boolean alreadyDocumentedWorker = false;
				String answerText = answer.getAnswer();

				// checks if we have already documented the worker
				for ( Worker i: workers ){
					if ( i.workerId.equals(workerID) ){
						currentWorker = i;
						alreadyDocumentedWorker = true;
						break;
					} 
				}
				// adds the answer to the worker
				if ( alreadyDocumentedWorker ){
					currentWorker.addAnswer(answerText, currentHIT.typeID, HITindex);
				} else {
					currentWorker = new Worker(workerID);
					workers.add(currentWorker);
					currentWorker.addAnswer(answerText, currentHIT.typeID, HITindex);
				}
			}

			if (wordToSense != null){
				//				answerOutput.println("The original target word:" + word);
				//				answerOutput.println("The ideal substitution would be: " + wordToSense.get(word)[2]);
			}

			/* 
			 * uses frequency counter to check for the word with the highest frequency and it prints out all words with frequencies >= three
			 * otherwise prints out a list of all submissions and their frequencies. Also takes the most frequent word and records the normal word
			 * and they frequent word for comparison.
			 */
			for ( String text: currentHIT.frequencyCounter.keySet() ){
				int freq = currentHIT.frequencyCounter.get(text);
				if ( freq == currentHIT.highestFreq ){
					compareList.add(text.trim());
				}
			}

		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}

	// retrieves HITs from amazon and processes them
	public void getHITs(Map<String, String[]> wordToSense) throws IOException
	{
		try 
		{
			HIT[] HITs = service.getAllReviewableHITs(null);
			for ( HIT amazonHIT: HITs )
			{
				SentenceHIT currentHIT = new SentenceHIT(amazonHIT.getHITId(), wordToSense);
				ArrayList<String>  compareList = null;
				System.out.println("Retrieved HIT: " + currentHIT.ID + " " + currentHIT.typeID );

				//uses hittypeId to check what kind of hit we are looking at
				if ( currentHIT.typeID.equals("25D2JE1M7PKKF8JGAZQAK04LZYTXQE") ){
					noContextHITs.add(currentHIT);
					compareList = noContextAnswers;
				} else if ( currentHIT.typeID.equals("20ASTLB3L0FBPWA8FU5JZEVE5SUJV7") ){
					contextHITs.add(currentHIT);
					compareList = contextAnswers;
					HITindex++;
				} else {
					continue;
				}

				for (Assignment answer: currentHIT.assignments ){
					String workerID = answer.getWorkerId();
					Worker currentWorker = null;
					boolean alreadyDocumentedWorker = false;
					String answerText = answer.getAnswer();

					// checks if we have already documented the worker
					for ( Worker i: workers ){
						if ( i.workerId.equals(workerID) ){
							currentWorker = i;
							alreadyDocumentedWorker = true;
							break;
						} 
					}
					// adds the answer to the worker
					if ( alreadyDocumentedWorker ){
						currentWorker.addAnswer(answerText, currentHIT.typeID, HITindex);
					} else {
						currentWorker = new Worker(workerID);
						workers.add(currentWorker);
						currentWorker.addAnswer(answerText, currentHIT.typeID, HITindex);
					}

					for (String text: currentHIT.frequencyCounter.keySet())
					{
						int freq = currentHIT.frequencyCounter.get(text);
						if (freq == currentHIT.highestFreq)
						{
							compareList.add(text.trim());
						}
					}
				}
			}
		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}

	// approves all hits we have IDs stored for in NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs
	public void approveHIT(String hitId) throws IOException
	{
		try 
		{
			hitId.trim();
			HIT currentHIT = service.getHIT(hitId);
			Assignment[] answers = service.getAssignmentsForHIT
			(
					hitId,
					currentHIT.getMaxAssignments()
			);

			// Print out the HITId and the URL to view the HIT.
			System.out.println("Retrieved HIT: " + hitId);
			for (Assignment answer: answers){
				answer.setAssignmentStatus(AssignmentStatus.Approved);
				System.out.println("Approved HIT: " + hitId);
				service.approveAssignment(answer.getAssignmentId(), "Accepted");
			}



		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}

	public void fillHitList(File input, int numAssignments) throws IOException{
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
		if (fileReader != null){
			String inputLine = fileReader.readLine();
			while (inputLine != null){
				String[] words = inputLine.split("\t");
				ArrayList<String> answers = new ArrayList<String>(10);
				String typeID = "";
				for ( int i = 3; i < 3 + numAssignments ; i++)
				{
					if ( i < words.length )
					{
						answers.add(words[i].trim());
					}
				}

				typeID = words[1];

				SentenceHIT currentHIT = new SentenceHIT(words[0], typeID, words[2], answers);

				if ( typeID.equals("20ASTLB3L0FBPWA8FU5JZEVE5SUJV7") )
				{
					contextHITs.add(currentHIT);
				} else if ( typeID.equals("25D2JE1M7PKKF8JGAZQAK04LZYTXQE") )
				{
					noContextHITs.add(currentHIT);
				}

				inputLine = fileReader.readLine();
			}
		}
	}
	
	public void splitHITs()
	{
		contextHITs1.clear();
		contextHITs2.clear();
		noContextHITs1.clear();
		noContextHITs2.clear();
		
		Random hitSplitter = new Random();
		
		for (int n = 0 ; n < 24 ; n++)
		{
			SentenceHIT currentContextHIT = contextHITs.get(n);
			SentenceHIT currentNoContextHIT = noContextHITs.get(n);
			
			ArrayList<String> currentContextAnswers = new ArrayList<String>(currentContextHIT.answers);
			ArrayList<String> currentContextAnswers1 = new ArrayList<String>();
			ArrayList<String> currentContextAnswers2 = new ArrayList<String>();
			ArrayList<String> currentNoContextAnswers = new ArrayList<String>(currentNoContextHIT.answers);
			ArrayList<String> currentNoContextAnswers1 = new ArrayList<String>();
			ArrayList<String> currentNoContextAnswers2 = new ArrayList<String>();
			
			
			
			for (int i =0; i<24; i++){
				currentContextAnswers1.add(currentContextAnswers.remove(hitSplitter.nextInt(48-i*2)));
				currentContextAnswers2.add(currentContextAnswers.remove(hitSplitter.nextInt((48- i*2) - 1)));
				currentNoContextAnswers1.add(currentNoContextAnswers.remove(hitSplitter.nextInt(48-i*2)));
				currentNoContextAnswers2.add(currentNoContextAnswers.remove(hitSplitter.nextInt((48- i*2) - 1)));
			}
			// used quick and dirty OurHIT constructor for speed
			SentenceHIT currentContextHIT1 = new SentenceHIT(currentContextAnswers1);
			SentenceHIT currentContextHIT2 = new SentenceHIT(currentContextAnswers2);
			contextHITs1.add(currentContextHIT1);
			contextHITs2.add(currentContextHIT2);
			SentenceHIT currentNoContextHIT1 = new SentenceHIT(currentNoContextAnswers1);
			SentenceHIT currentNoContextHIT2 = new SentenceHIT(currentNoContextAnswers2);
			noContextHITs1.add(currentNoContextHIT1);
			noContextHITs2.add(currentNoContextHIT2);
			
		}
	}
	
	public double getMirrorPearsonCoeff(ArrayList<SentenceHIT> list1, ArrayList<SentenceHIT> list2)
	{
		double answer = 0;
		double[] array1 = new double[list1.size()];
		double[] array2 = new double[list1.size()];
		
		for (int i = 0 ; i < list1.size() ; i++)
		{
			array1[i] = list1.get(i).entropy;
			array2[i] = list2.get(i).entropy;
		}
//		
//		PearsonsCorrelation corr = new PearsonsCorrelation();
//
//		answer = corr.correlation(array1, array2);
		
		return answer;
	}
	public double getMirrorSpearmanCoeff(ArrayList<SentenceHIT> list1, ArrayList<SentenceHIT> list2)
	{
		double answer = 0;
		double[] array1 = new double[list1.size()];
		double[] array2 = new double[list1.size()];
		
		for (int i = 0 ; i < list1.size() ; i++)
		{
			array1[i] = list1.get(i).entropy;
			array2[i] = list2.get(i).entropy;
		}
		
//		SpearmansCorrelation corr = new SpearmansCorrelation();
//
//		answer = corr.correlation(array1, array2);
		
		return answer;
	}
	public double getSpearmanCoeff()
	{
		double answer = 0;
		double[] array1 = new double[contextHITs.size()];
		double[] array2 = new double[contextHITs.size()];
		
		for (int i = 0 ; i < contextHITs.size() ; i++)
		{
			array1[i] = contextHITs.get(i).entropy;
			array2[i] = noContextHITs.get(i).entropy;
		}
		
//		SpearmansCorrelation corr = new SpearmansCorrelation();
//
//		answer = corr.correlation(array1, array2);
		
		return answer;
	}
	public double getPearsonCoeff()
	{
		double answer = 0;
		double[] array1 = new double[contextHITs.size()];
		double[] array2 = new double[contextHITs.size()];
		
		for (int i = 0 ; i < contextHITs.size() ; i++)
		{
			array1[i] = contextHITs.get(i).entropy;
			array2[i] = noContextHITs.get(i).entropy;
		}
		
//		PearsonsCorrelation corr = new PearsonsCorrelation();
//
//		answer = corr.correlation(array1, array2);
		
		return answer;
	}
	

	public static double round(double value) {
		long factor = (long) Math.pow(10, 4);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}

	// splits HITs and calculates pearson and spearman coefficients on their entropies
	public void runEntropySampling(PrintWriter output) {
		HashMap<Double, Double> pearsonContextFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> spearmanContextFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> pearsonNoContextFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> spearmanNoContextFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> pearsonFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> spearmanFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> entropyContextFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> entropyNoContextFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> entropyAvgContextFreq = new HashMap<Double, Double>();
		HashMap<Double, Double> entropyAvgNoContextFreq = new HashMap<Double, Double>();
		double pearsonContextMean = 0;
		double pearsonNoContextMean = 0;
		double spearmanContextMean = 0;
		double spearmanNoContextMean = 0;
		double pearsonMean = 0;
		double spearmanMean = 0;
		double pearsonContextSD = 0;
		double pearsonNoContextSD = 0;
		double spearmanContextSD = 0;
		double spearmanNoContextSD = 0;
		double pearsonSD = 0;
		double spearmanSD = 0;
		double samples = 5000;

		for ( int n = 0 ; n < (int)samples ; n++)
		{
			System.out.println(n);
			splitHITs();
			double entropy =0;
			double avgEntropy = 0;
			int i;
			for (i = 0; i<contextHITs1.size(); i ++){
				entropy = Math.round((Math.abs(contextHITs1.get(i).entropy - noContextHITs2.get(i).entropy))*1000)/1000.0;
				avgEntropy += entropy;
				if (entropyContextFreq.containsKey(entropy)){
					entropyContextFreq.put(entropy, entropyContextFreq.get(entropy) + 1);
				}else {
					entropyContextFreq.put(entropy, (double) 1);
				} 
			}
			avgEntropy = Math.round((avgEntropy / contextHITs1.size())*1000)/1000.0;
			if (entropyAvgContextFreq.containsKey(entropy)){
				entropyAvgContextFreq.put(entropy, entropyAvgContextFreq.get(entropy) + 1);
			}else {
				entropyAvgContextFreq.put(entropy, (double) 1);
			}
//			avgEntropy = 0;
//			for (i = 0; i<noContextHITs1.size(); i ++){
//				entropy = Math.round((Math.abs(noContextHITs1.get(i).entropy - noContextHITs2.get(i).entropy))*1000)/1000.0;
//				avgEntropy += entropy;
//				if (entropyNoContextFreq.containsKey(entropy)){
//					entropyNoContextFreq.put(entropy, entropyNoContextFreq.get(entropy) + 1);
//				}else {
//					entropyNoContextFreq.put(entropy, (double) 1);
//				} 
//			}
//			avgEntropy = Math.round((avgEntropy / noContextHITs1.size())*1000)/1000.0;
//			if (entropyAvgNoContextFreq.containsKey(entropy)){
//				entropyAvgNoContextFreq.put(entropy, entropyAvgNoContextFreq.get(entropy) + 1);
//			}else {
//				entropyAvgNoContextFreq.put(entropy, (double) 1);
//			}
//			double coeff = round( getMirrorPearsonCoeff(contextHITs1, contextHITs2) );
//			if (pearsonContextFreq.containsKey(coeff)){
//				pearsonContextFreq.put(coeff, pearsonContextFreq.get(coeff) + 1);
//			}else {
//				pearsonContextFreq.put(coeff, (double) 1);
//			} 
//			coeff = round( getMirrorSpearmanCoeff(contextHITs1, contextHITs2) );
//			if (spearmanContextFreq.containsKey(coeff)){
//				spearmanContextFreq.put(coeff, spearmanContextFreq.get(coeff) + 1);
//			}else {
//				spearmanContextFreq.put(coeff, (double) 1);
//			} 
//			coeff = round( getMirrorPearsonCoeff(noContextHITs1, noContextHITs2) );
//			if (pearsonNoContextFreq.containsKey(coeff)){
//				pearsonNoContextFreq.put(coeff, pearsonNoContextFreq.get(coeff) + 1);
//			}else {
//				pearsonNoContextFreq.put(coeff, (double) 1);
//			} 
//			coeff = round( getMirrorSpearmanCoeff(noContextHITs1, noContextHITs2) );
//			if (spearmanNoContextFreq.containsKey(coeff)){
//				spearmanNoContextFreq.put(coeff, spearmanNoContextFreq.get(coeff) + 1);
//			}else {
//				spearmanNoContextFreq.put(coeff, (double) 1);
//			} 
//			coeff = round( getMirrorSpearmanCoeff(contextHITs1, noContextHITs1) );
//			if ( spearmanFreq.containsKey(coeff)){
//				spearmanFreq.put(coeff, spearmanFreq.get(coeff) + 1);
//			} else {
//				spearmanFreq.put(coeff, (double) 1);
//			} 
//			coeff = round( getMirrorPearsonCoeff(contextHITs1, noContextHITs1) );
//			if ( pearsonFreq.containsKey(coeff)){
//				pearsonFreq.put(coeff, pearsonFreq.get(coeff) + 1);
//			} else {
//				pearsonFreq.put(coeff, (double) 1);
//			}
		}
		output.println("Entropy Context-No Context");
		for (double coefficient : entropyContextFreq.keySet() )
		{
			output.printf(coefficient + ", %.8f", entropyContextFreq.get(coefficient));
			output.println();
		}
		output.println("Entropy Avg Context-No Context");
		for (double coefficient : entropyAvgContextFreq.keySet() )
		{
			output.printf(coefficient + ", %.8f", entropyAvgContextFreq.get(coefficient));
			output.println();
		}
//		output.println("Entropy NoContext");
//		for (double coefficient : entropyNoContextFreq.keySet() )
//		{
//			output.printf(coefficient + ", %.8f", entropyNoContextFreq.get(coefficient));
//			output.println();
//		}
//		output.println("Entropy AvgNoContext");
//		for (double coefficient : entropyAvgNoContextFreq.keySet() )
//		{
//			output.printf(coefficient + ", %.8f", entropyAvgNoContextFreq.get(coefficient));
//			output.println();
//		}
//		output.println("Pearson Context");
//		for (double coefficient : pearsonContextFreq.keySet() )
//		{
//			pearsonContextFreq.put(coefficient, pearsonContextFreq.get(coefficient)/samples);
//			output.printf(coefficient + ", %.8f", pearsonContextFreq.get(coefficient));
//			output.println();
//			pearsonContextMean += coefficient * pearsonContextFreq.get(coefficient);
//		}
//		output.println("Pearson No Context");
//		for (double coefficient : pearsonNoContextFreq.keySet() )
//		{
//			pearsonNoContextFreq.put(coefficient, pearsonNoContextFreq.get(coefficient)/samples);
//			output.printf(coefficient + ", %.8f", pearsonNoContextFreq.get(coefficient));
//			output.println();
//			pearsonNoContextMean += coefficient * pearsonNoContextFreq.get(coefficient);
//		}
//		output.println("Spearman Context");
//		for (double coefficient : spearmanContextFreq.keySet() )
//		{
//			spearmanContextFreq.put(coefficient, spearmanContextFreq.get(coefficient)/samples);
//			output.printf(coefficient + ", %.8f", spearmanContextFreq.get(coefficient));
//			output.println();
//			spearmanContextMean += coefficient * spearmanContextFreq.get(coefficient);
//		}
//		output.println("Spearman No Context");
//		for (double coefficient : spearmanNoContextFreq.keySet() )
//		{
//			spearmanNoContextFreq.put(coefficient, spearmanNoContextFreq.get(coefficient)/samples);
//			output.printf(coefficient + ", %.8f", spearmanNoContextFreq.get(coefficient));
//			output.println();
//			spearmanNoContextMean += coefficient * spearmanNoContextFreq.get(coefficient);
//		}
//		output.println("Spearman Basic");
//		for (double coefficient : spearmanFreq.keySet() )
//		{
//			spearmanFreq.put(coefficient, spearmanFreq.get(coefficient)/samples);
//			output.printf(coefficient + ", %.8f", spearmanFreq.get(coefficient));
//			output.println();
//			spearmanMean += coefficient * spearmanFreq.get(coefficient);
//		}
//		output.println("Pearson Basic");
//		for (double coefficient : pearsonFreq.keySet() )
//		{
//			pearsonFreq.put(coefficient, pearsonFreq.get(coefficient)/samples);
//			output.printf(coefficient + ", %.8f", pearsonFreq.get(coefficient));
//			output.println();
//			pearsonMean += coefficient * pearsonFreq.get(coefficient);
//		}
//		// calculating standard deviations
//		for (double coefficient : pearsonContextFreq.keySet() )
//		{
//			pearsonContextSD += pearsonContextFreq.get(coefficient) * Math.pow(coefficient - pearsonContextMean, 2);
//		}
//		for (double coefficient : pearsonNoContextFreq.keySet() )
//		{
//			pearsonNoContextSD += pearsonNoContextFreq.get(coefficient) * Math.pow(coefficient - pearsonNoContextMean, 2);
//		}
//		for (double coefficient : spearmanContextFreq.keySet() )
//		{
//			spearmanContextSD += spearmanContextFreq.get(coefficient) * Math.pow(coefficient - spearmanContextMean, 2);
//		}
//		for (double coefficient : spearmanNoContextFreq.keySet() )
//		{
//			spearmanNoContextSD += spearmanNoContextFreq.get(coefficient) * Math.pow(coefficient - spearmanNoContextMean, 2);
//		}
//		for (double coefficient : pearsonFreq.keySet() )
//		{
//			pearsonSD += pearsonFreq.get(coefficient) * Math.pow(coefficient - pearsonMean, 2);
//		}
//		for (double coefficient : spearmanFreq.keySet() )
//			spearmanSD += spearmanFreq.get(coefficient) * Math.pow(coefficient - spearmanMean, 2);
//		{
//		}
//		System.out.println("Pearson Basic mean = " + pearsonMean + " SD = " + Math.sqrt(pearsonSD));
//		System.out.println("Spearman Basic mean = " + spearmanMean + " SD = " + Math.sqrt(spearmanSD));
//		System.out.println("Pearson Context mean = " + pearsonContextMean + " SD = " + Math.sqrt(pearsonContextSD));
//		System.out.println("Pearson No Context mean = " + pearsonNoContextMean + " SD = " + Math.sqrt(pearsonNoContextSD));
//		System.out.println("Spearman Context mean = " + spearmanContextMean + " SD = " + Math.sqrt(spearmanContextSD));
//		System.out.println("Spearman No Context mean = " + spearmanNoContextMean + " SD = " + Math.sqrt(spearmanNoContextSD));
	}
	
	public void runSimilaritySamplingData(PrintWriter output)
	{
		double samples = 1000;
		for ( int n = 0 ; n < (int)samples ; n++)
		{
			System.out.println(n);
			splitHITs();
			FrequencyCounter<Double> contextFreq = new FrequencyCounter<Double>();
			FrequencyCounter<Double> noContextFreq = new FrequencyCounter<Double>();
			
			for ( int i = 0; i < 24; i ++ )
			{
				contextFreq.add( getCosineSimilarity(contextHITs1.get(i), contextHITs2.get(i)) );
				noContextFreq.add( getCosineSimilarity(noContextHITs1.get(i), noContextHITs2.get(i)) );
			}

			
		}
	}

	public double getCosineSimilarity(SentenceHIT contextHit, SentenceHIT noContextHit){
		Map<String, Integer> contextFreq = contextHit.frequencyCounter;
		Map<String, Integer> noContextFreq = noContextHit.frequencyCounter;
		double contextMagnitude = 0;
		double noContextMagnitude = 0;
		double cosineIndicator = 0;
		if (contextHit.targetWord.equals(noContextHit.targetWord)){
			for (String contextSubmission: contextFreq.keySet()){
//				noContextMagnitude = 0;
				for (String noContextSubmission: noContextFreq.keySet()){
					if (contextSubmission.equals(noContextSubmission)){
						double contextSubFreq = contextFreq.get(contextSubmission);
						double noContextSubFreq = noContextFreq.get(noContextSubmission);
						cosineIndicator += contextSubFreq * noContextSubFreq;

						contextMagnitude += contextFreq.get(contextSubmission)* contextFreq.get(contextSubmission);
						noContextMagnitude += noContextFreq.get(noContextSubmission)* noContextFreq.get(noContextSubmission);
					}
				}
			}
			noContextMagnitude = Math.sqrt(noContextMagnitude);
			contextMagnitude = Math.sqrt(contextMagnitude);
			double cosineDivisor = noContextMagnitude*contextMagnitude;
			return cosineIndicator/cosineDivisor;
		}
		return 0;
		//	
	}

	public double getOverlap(SentenceHIT contextHit, SentenceHIT noContextHit){
		Map<String, Integer> contextFreq = contextHit.frequencyCounter;
		Map<String, Integer> noContextFreq = noContextHit.frequencyCounter;
		int weightMatched=0;
		if (contextHit.targetWord.equals(noContextHit.targetWord)){
			for (String contextSubmission: contextFreq.keySet()){
//				noContextMagnitude = 0;
				for (String noContextSubmission: noContextFreq.keySet()){
					if (contextSubmission.equals(noContextSubmission)){
						double contextSubFreq = contextFreq.get(contextSubmission);
						double noContextSubFreq = noContextFreq.get(noContextSubmission);
						for (int i =0; i < Math.min(contextSubFreq, noContextSubFreq); i++){
							// do match
							weightMatched++;
						}

					}
				}
			}
			return weightMatched/(double)Math.min(contextHit.answers.size(), noContextHit.answers.size());
		}
		return 0;
	}
	// HTML for a HIT with context and the target word provided
 	public static String contextGivenSub(String firstPartQuestion, String word, String secondPartQuestion) {
		String q = "";
		q += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		q += "<HTMLQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\">";
		q += "	<HTMLContent><![CDATA[";
		q += "<!DOCTYPE html>";
		q += "<html>";
		q += "  <head>";
		q += "    <script type=\'text/javascript\' src=\'https://s3.amazonaws.com/mturk-public/externalHIT_v1.js\'></script>";
		q += "  </head>";
		q += "	<body>";
		q += "		<u><b><span style=\"font-size:25px;\">Instructions:</span></b></u><br /><br />";
		q += "		Enter a <i>simpler</i> word in the box below that could be substituted for the red, bold word in the sentence.";
		q += "		A <i>simpler</i> word is one that would be understood by more people or people with a lower reading level (e.g. children). <br/> <br/>";
		q += "		Make sure that the simple word you enter fits in the context of the sentence.";
		q += "		For example, given the sentence:<br/> <br/>" ;
		q += "		My horse was <span style=\"color:red;\">galloping</span> through the forest. <br/> <br/>";
		q += "		Entering <b>run</b> would NOT be appropriate, however <b>running</b> would be.<br/>";
		q += "      <div id=\"test\"></div>";
		q += "		<hr />";
		q += "		<br /><u><b><span style=\"font-size:25px;\">Task:</span></b></u><br />";
		q += "		<br /><span style = \"font-size:20px;\">" + firstPartQuestion + "<b style=\"color:red;\">" + word + "</b>" + secondPartQuestion + "</span>";
		q += "      <br/><form name='mturk_form' method='post' id='mturk_form' onsubmit=\"return validateForm()\" action='https://www.mturk.com/mturk/externalSubmit' style=\"padding-top:10px\">";
		q += "      <input type=\'hidden\' value=\'\' name =\'assignmentId\' id=\'assignmentId\'/>";
		q += "      <input type=\"text\" name=\"HITAnswer\" id=\"answer\"/>";
		q += "      <input type=\"submit\" value=\"Submit\" id=\"submit_button\"/>";
		q += "      </form>";
		q += "    <script language='Javascript'>turkSetAssignmentID();</script>";
		q += "    <script type=\'text/javascript\'>";
		q += "      if (document.getElementById(\"assignmentId\").value == \"ASSIGNMENT_ID_NOT_AVAILABLE\") {";
		q += "        document.getElementById(\"submit_button\").disabled = true;";
		q += "        document.getElementById(\"answer\").disabled = true; } ";
		q += "      else {document.getElementById(\"submit_button\").disabled = false;";
		q += "		  document.getElementById(\"answer\").disabled = false; }";
		q += "		function validateForm(){";
		q += "			if (document.getElementById(\"answer\").value == null || document.getElementById(\"answer\").value.trim() == \"\"){";
		q += " 				alert(\"Please provide a word.\"";
		q += " 				return false;";
		q += " 			}";
		q += " 		}";
		q += "    </script>";
		q += "  </body>";
		q += "</html>]]>";
		q += "  </HTMLContent>";
		q += "  <FrameHeight>500</FrameHeight>";
		q += "</HTMLQuestion>";
		return q;
	}
 	
 	public static String partialContextSub(String firstPartQuestion, String word, String secondPartQuestion){
 		String q = "";
		q += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		q += "<HTMLQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\">";
		q += "	<HTMLContent><![CDATA[";
		q += "<!DOCTYPE html>";
		q += "<html>";
		q += "  <head>";
		q += "    <script type=\'text/javascript\' src=\'https://s3.amazonaws.com/mturk-public/externalHIT_v1.js\'></script>";
		q += "  </head>";
		q += "	<body>";
		q += "		<u><b><span style=\"font-size:25px;\">Instructions:</span></b></u><br /><br />";
		q += "		";
		q += "		<hr />";
		q += "		<br /><u><b><span style=\"font-size:25px;\">Task:</span></b></u><br />";
		q += "		<br /><span style = \"font-size:20px;\">" + firstPartQuestion + "<b style=\"color:red;\">" + word + "</b>" + secondPartQuestion + "</span>";
		q += "      <br/><form name='mturk_form' method='post' id='mturk_form' onsubmit=\"return validateForm()\" action='https://www.mturk.com/mturk/externalSubmit' style=\"padding-top:10px\">";
		q += "      <input type=\'hidden\' value=\'\' name =\'assignmentId\' id=\'assignmentId\'/>";
		q += "      <input type=\"text\" name=\"HITAnswer\" id=\"answer\"/>";
		q += "      <input type=\"submit\" value=\"Submit\" id=\"submit_button\"/>";
		q += "      </form>";
		q += "    <script language='Javascript'>turkSetAssignmentID();</script>";
		q += "    <script type=\'text/javascript\'>";
		q += "      if (document.getElementById(\"assignmentId\").value == \"ASSIGNMENT_ID_NOT_AVAILABLE\") {";
		q += "        document.getElementById(\"submit_button\").disabled = true;";
		q += "        document.getElementById(\"answer\").disabled = true; } ";
		q += "      else {document.getElementById(\"submit_button\").disabled = false;";
		q += "		  document.getElementById(\"answer\").disabled = false; }";
		q += "		function validateForm(){";
		q += "			if (document.getElementById(\"answer\").value == null || document.getElementById(\"answer\").value.trim() == \"\"){";
		q += " 				alert(\"Please provide a word.\"";
		q += " 				return false;";
		q += " 			}";
		q += " 		}";
		q += "    </script>";
		q += "  </body>";
		q += "</html>]]>";
		q += "  </HTMLContent>";
		q += "  <FrameHeight>500</FrameHeight>";
		q += "</HTMLQuestion>";
		return q;
 	}

	// HTML for a HIT with the context given but the target word omitted
	public static String noTargetGivenSub(String firstPartQuestion, String word, String secondPartQuestion) {
		String q = "";
		q += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		q += "<HTMLQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\">";
		q += "	<HTMLContent><![CDATA[";
		q += "<!DOCTYPE html>";
		q += "<html>";
		q += "  <head>";
		q += "    <script type=\'text/javascript\' src=\'https://s3.amazonaws.com/mturk-public/externalHIT_v1.js\'></script>";
		q += "  </head>";
		q += "	<body>";
		q += "		<u><b><span style=\"font-size:25px;\">Instructions:</span></b></u><br /><br />";
		q += "		Enter a <i>simple</i> word in the box below that could be inserted for the space in the sentence below.";
		q += "		A <i>simple</i> word is one that would be understood by more people or people with a lower reading level (e.g. children). <br/> <br/>";
		q += "		Make sure that the simple word you enter fits in the context of the sentence.";
		q += "		For example, given the sentence:<br/> <br/>";
		q += "		My horse was _________ through the forest. <br/> <br/>";
		q += "		<b>run</b> would NOT be appropriate, but <b>running</b> would be.";
		q += "      <div id=\"test\"></div>";
		q += "		<hr />";
		q += "		<u><b><span style=\"font-size:25px;\">Task:</span></b></u><br /><br />";
		q += "      <form name='mturk_form' method='post' id='mturk_form' action='https://www.mturk.com/mturk/externalSubmit'>";
		q += "		<br /><span style = \"font-size:20px;\">" + firstPartQuestion + "<input type=\"text\" name=\"HITAnswer\" id =\"answer\"/> " + secondPartQuestion + "</span>";
		q += "      <br/><input type=\'hidden\' value=\'\' name =\'assignmentId\' id=\'assignmentId\'/>";
		q += "      <input type=\"submit\" value=\"Submit\" id=\"submit_button\" style=\"margin-top:10px\"/>";
		q += "      </form>";
		q += "    <script language='Javascript'>turkSetAssignmentID();</script>";
		q += "    <script type=\'text/javascript\'>";
		q += "      if (document.getElementById(\"assignmentId\").value == \"ASSIGNMENT_ID_NOT_AVAILABLE\") {";
		q += "        document.getElementById(\"submit_button\").disabled = true;";
		q += "        document.getElementById(\"answer\").disabled = true; } ";
		q += "      else {document.getElementById(\"submit_button\").disabled = false;";
		q += "		  document.getElementById(\"answer\").disabled = false; }";
		q += "    </script>";
		q += "  </body>";
		q += "</html>]]>";
		q += "  </HTMLContent>";
		q += "  <FrameHeight>500</FrameHeight>";
		q += "</HTMLQuestion>";
		return q;
	}

	// HTML for a HIT with target word given but the context omitted
	public static String noContextGivenSub(String firstPartQuestion, String word, String secondPartQuestion, String sense, String POS) {
		String q = "";
		q += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		q += "<HTMLQuestion xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2011-11-11/HTMLQuestion.xsd\">";
		q += "	<HTMLContent><![CDATA[";
		q += "<!DOCTYPE html>";
		q += "<html>";
		q += "  <head>";
		q += "    <script type=\'text/javascript\' src=\'https://s3.amazonaws.com/mturk-public/externalHIT_v1.js\'></script>";
		q += "  </head>";
		q += "	<body>";
		q += "		<u><b><span style=\"font-size:25px;\">Instructions:</span></b></u><br /><br />";
		q += "		Below is a word with its part of speech and definition. ";
		q += "		Enter a <i>simpler</i> word in the box below that has the same meaning as the given word.";
		q += "		A <i>simpler</i> word is one that would be understood by more people or people with a lower reading level (e.g. children).<br/><br/>";
		q += "		Make sure that the simple word that you enter has the same tense as the provided word.";
		q += "		For example, given the word:<br/><br/>";
		q += "		galloping (VERB): go at galloping speed<br/><br/>";
		q += "		<b>run</b> would NOT be appropriate, but <b>running</b> would be.";
		q += "      <div id=\"test\"></div>";
		q += "		<hr />";
		q += "		<u><b><span style=\"font-size:25px;\">Task:</span></b></u><br />";
		q += "		<br /><span style = \"font-size:20px;\"><b>" + word + "</b>: (" + POS + ") " + sense + "</span>";
		q += "      <br/><form name='mturk_form' method='post' id='mturk_form' onsubmit=\"return validateForm()\" action='https://www.mturk.com/mturk/externalSubmit' style=\"padding-top:10px\">";
		q += "      <input type=\'hidden\' value=\'\' name =\'assignmentId\' id=\'assignmentId\'/>";
		q += "      <input type=\"text\" name=\"HITAnswer\" id=\"answer\"/>";
		q += "      <input type=\"submit\" value=\"Submit\" id=\"submit_button\"/>";
		q += "      </form>";
		q += "    <script language='Javascript'>turkSetAssignmentID();</script>";
		q += "    <script type=\'text/javascript\'>";
		q += "      if (document.getElementById(\"assignmentId\").value == \"ASSIGNMENT_ID_NOT_AVAILABLE\") {";
		q += "        document.getElementById(\"submit_button\").disabled = true;";
		q += "        document.getElementById(\"answer\").disabled = true; } ";
		q += "      else {document.getElementById(\"submit_button\").disabled = false;";
		q += "		  document.getElementById(\"answer\").disabled = false; }";
		q += "		function validateForm(){";
		q += "			if (document.getElementById(\"answer\").value == null || document.getElementById(\"answer\").value.trim() == \"\"){";
		q += " 				alert(\"Please provide a word.\"";
		q += " 				return false;";
		q += " 			}";
		q += " 		}";
		q += "    </script>";
		q += "  </body>";
		q += "</html>]]>";
		q += "  </HTMLContent>";
		q += "  <FrameHeight>500</FrameHeight>";
		q += "</HTMLQuestion>";
		return q;
	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{

		String usageError = "Please provide a valid option. Such as: " +
		"\n -add FILENAME [more files...]           *creates new HITs from the data provided in the given file(s)* " +
		"\n -delete FILENAME [more files...]        *deletes all of the HITs with IDs matching those given in the file(s)*" +
		"\n -approveAll FILENAME [more files...]    *approves all the assignments for all HITs with IDs in the given file(s)*" +
		"\n -getAnswers FILENAME [more files...]    *retrieves all assignments for the HITs matching the IDs in the given file(s) outputs them to the originals filename with \'Results\' appended*";

		if (args.length >=1){
			// Create an instance of this class.
			LexicalSubSurvey app = new LexicalSubSurvey();
			File inputFile = null;

			try {
				if (args.length>1)
					inputFile = new File(args[1]);


				if (args[0].equals("-add")){

					String[] parts = {"NN", "NNS", "JJ", "JJR", "JJS", "RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
					ArrayList<String> pos = new ArrayList<String>();
					for (int i=0; i<parts.length; i++){
						pos.add(parts[i]);
					}



					ExamplePairReader reader = new ExamplePairReader(PARSED, ALIGN);
					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))); // typical file name: "sub.simple.first100"
					app.noContextpr = new PrintWriter(new FileOutputStream(new File("NoContextGivenIDs")));
					app.partialContextpr = new PrintWriter(new FileOutputStream(new File("partialContextIDs")));
					app.contextpr = new PrintWriter(new FileOutputStream(new File("ContextGivenIDs")));

					Map<String, String> codeToPOS = new HashMap<String, String>(14);
					codeToPOS.put("NN", "Noun");
					codeToPOS.put("NNS", "Noun");
					codeToPOS.put("JJ", "Adjective");
					codeToPOS.put("JJR", "Adjective");
					codeToPOS.put("JJS", "Adjective");
					codeToPOS.put("RB", "Adverb");
					codeToPOS.put("RBR", "Adverb");
					codeToPOS.put("RBS", "Adverb");
					codeToPOS.put("VB", "Verb");
					codeToPOS.put("VBD", "Verb");
					codeToPOS.put("VBG", "Verb");
					codeToPOS.put("VBN", "Verb");
					codeToPOS.put("VBP", "Verb");
					codeToPOS.put("VBZ", "Verb");


					String input = in.readLine();
					Map<String, String[]> wordToSense = new HashMap<String, String[]>(25);
					String focusWord = "";
					String sense = "";
					String context = "";
					String simpleWord;
					while (input != null){
						StringTokenizer splitter = new StringTokenizer(input, "\t");
						context = splitter.nextToken();
						splitter.nextToken();
						focusWord = splitter.nextToken();
						simpleWord = splitter.nextToken();
						sense = splitter.nextToken();

						String[] wordAssociations = {context, sense, simpleWord};

						wordToSense.put(focusWord, wordAssociations);

						input = in.readLine();
					}

					for( int k = 0; k <1000; k++ ){ // for counted input
						//						while ( input != null ){ // for text input
						//						while ( reader.hasNext() ){ // for ExamplePairReader input	
						ExamplePair p = reader.next();
						Alignment align = p.getAlignment();
						ArrayList<Word> normalWords = p.getNormal().getWords();
						ArrayList<Word> simpleWords = p.getSimple().getWords();

						// creates object = list of simple words
						SimpleWordsList simpleWordsList = new SimpleWordsList();

						for( AlignPair pair: align){
							int n = pair.getNormalIndex();
							int s = pair.getSimpleIndex();
							Word normal = normalWords.get(n);
							Word simple = simpleWords.get(s);
							boolean diffWords = !normal.getWord().toLowerCase().equals( simple.getWord().toLowerCase() );
							boolean normWordSimplePOS = pos.contains( normal.getPos() );
							boolean posEqual = normal.getPos().equals( simple.getPos() );
							boolean normalIsAlreadySimple = simpleWordsList.contains( normal.getWord() );
							boolean doWeHaveSense = wordToSense.containsKey(normal.getWord());
							if (doWeHaveSense)
								context = wordToSense.get(normal.getWord())[0];
							boolean contextMatch = context.equals(p.getNormal().textString());

							if( diffWords && normWordSimplePOS && posEqual && !normalIsAlreadySimple && doWeHaveSense && contextMatch){
								String firstPart = "";
								String partialFirst = "";
								String wordAfterFocus = normalWords.get(n+1).getWord();
								String target = normal.getWord();
								if ( !( wordAfterFocus.length() == 1 && wordAfterFocus.compareTo("A") < 0 ) ){
									target += " ";
								}
								String secondPart = "";
								String partialSecond = "";
								sense = wordToSense.get(normal.getWord())[1];
								String POS = codeToPOS.get(normal.getPos());

								for ( int i = 0; i < normalWords.size(); i++ ){
									String currentWord = normalWords.get(i).getWord();
									String nextWord = "";
									if ( i+1 < normalWords.size() ){
										nextWord = normalWords.get(i+1).getWord();
									}
									if ( i < n ){
										if (i > n - 3)
											partialFirst += currentWord;
										firstPart += currentWord;
										if ( !( nextWord.length() == 1 && nextWord.compareTo("A") < 0 )){
											firstPart += " ";
											if (i > n - 3)
												partialFirst += " ";
										}
									}
									if ( i > n ){
										if (i < n + 3)
											partialSecond += currentWord;
										secondPart += currentWord;
										if ( !( nextWord.length() == 1 && nextWord.compareTo("A") < 0 )){
											secondPart += " ";
											if (i < n + 3)
												partialSecond += " ";
										}
									}
								}

								app.createContextGivenSurvey(firstPart, target, secondPart);
								app.createPartialContextGivenSurvey(partialFirst, target, partialSecond);
								app.createNoContextGivenSurvey(firstPart, target, secondPart, sense, POS);
							}
						}


						// input = in.readLine();
					}
					app.contextpr.close();
					app.partialContextpr.close();
					app.noContextpr.close();

				}else if (args[0].equals("-delete")){
					System.out.println("deleting");
					// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
					BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
					String hitId = "";

					for (hitId = fileReader.readLine(); hitId !=null; hitId = fileReader.readLine()){
						System.out.println(hitId);
						app.deleteHIT(hitId);
					}
				}else if (args[0].equals("-approveAll")){
					System.out.println("approving");
					// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
					BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
					String hitId = "";

					for (hitId = fileReader.readLine(); hitId !=null; hitId = fileReader.readLine()){
						System.out.println(hitId);
						app.approveHIT(hitId);
					}
				}else if (args[0].equals("-getAnswers")){
					System.out.println("retrieving");
//					 app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/contextAnswerOutput.cleaned"));
//					 app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/noContextAnswerOutput.cleaned"));
					// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
					// BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
					BufferedReader in = null;
					if (args[args.length-2].equals("-i")) // this tag denotes the input file which contains the Hit data in the form context \t wordIndex \t normalWord \t simpleWord \t sense
						in = new BufferedReader(new InputStreamReader(new FileInputStream(args[args.length-1])));

					app.noContextAnswerOutput = new PrintWriter(new FileOutputStream(new File("noContextAnswerOutput")));
					app.contextAnswerOutput = new PrintWriter(new FileOutputStream(new File("contextAnswerOutput")));
					app.workerOutput = new PrintWriter(new FileOutputStream(new File("workerOutput")));
					app.substitutionOutput = new PrintWriter(new FileOutputStream(new File("SubstitionsOnly")));

					//wordToSense maps each word to the rest of the data associated with it: simple word, context and sense.
					Map<String, String[]> wordToSense = null;
					if (in != null){
						String input = in.readLine();
						wordToSense = new HashMap<String, String[]>(25);
						String focusWord = "";
						String sense = "";
						String context = "";
						String simpleWord;
						while (input != null){
							StringTokenizer splitter = new StringTokenizer(input, "\t");
							context = splitter.nextToken();
							int firstHalf = Integer.parseInt(splitter.nextToken());
							context = context.substring(0, firstHalf);
							focusWord = splitter.nextToken();
							simpleWord = splitter.nextToken();
							sense = splitter.nextToken();

							String[] wordAssociations = {context, sense, simpleWord};

							wordToSense.put(focusWord, wordAssociations);

							input = in.readLine();
						}
					}

					// retrieves the HITs from amazon and processes them in our system
					app.getHITs(wordToSense);

					//prints out each workers Id and the answers to the questions
					for (Worker i: app.workers){
						app.workerOutput.println(i.workerId + "\t");
						app.workerOutput.println(i.question1Answers.toString());
						app.workerOutput.println(i.question2Answers.toString());
						app.workerOutput.println(i.question3Answers.toString());
					}

					PrintWriter answerOutput = new PrintWriter(new FileOutputStream(new File("Hit output formatted")));

					for (SentenceHIT currentHIT: app.contextHITs) {
						String wordList = "";
						boolean topAnswers = false;
						for (String text: currentHIT.frequencyCounter.keySet()){
							int freq = currentHIT.frequencyCounter.get(text);
							if (freq >=3){
								if(!topAnswers)
									answerOutput.print("Top submissions were: ");
								answerOutput.print(text + ": " + freq + " ");
								topAnswers = true;
								if (freq ==currentHIT.highestFreq)
									app.substitutionOutput.println(currentHIT.targetWord + "\t" + text);
							} else
								wordList += text + ": " + freq + " ";
						}
						if (!topAnswers){
							answerOutput.print("No majority submission, all submissions are: " + wordList);
						}
						answerOutput.println();
						app.contextAnswerOutput.println(currentHIT.toString());
					}

					for (SentenceHIT currentHIT: app.noContextHITs) {
						String wordList = "";
						boolean topAnswers = false;
						for (String text: currentHIT.frequencyCounter.keySet()){
							int freq = currentHIT.frequencyCounter.get(text);
							if (freq >=3){
								if(!topAnswers)
									answerOutput.print("Top submissions were: ");
								answerOutput.print(text + ": " + freq + " ");
								topAnswers = true;
								if (freq ==currentHIT.highestFreq)
									app.substitutionOutput.println(currentHIT.targetWord + "\t" + text);
							} else
								wordList += text + ": " + freq + " ";
						}
						if (!topAnswers){
							answerOutput.print("No majority submission, all submissions are: " + wordList);
						}
						answerOutput.println();
						app.noContextAnswerOutput.println(currentHIT.toString());
					}

					answerOutput.close();

					//	System.out.print("Percentage hits with same top submission: " +  matching*100.0 / app.contextAnswers.size());

					// closing all printWriters
					app.workerOutput.close();
					app.noContextAnswerOutput.close();
					app.contextAnswerOutput.close();
					app.substitutionOutput.close();

					//Writes out the data so that it can be imported into excel using csv format.
				}else if (args[0].equals("-getEntropyData")){
					System.out.println("gathering data...");
					// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
					
//					for (int i = 5 ; i < 51 ; i += 5)
//					{
//						app.contextHITs.clear();
//						app.noContextHITs.clear();
					app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/contextAnswerOutput.cleaned"), 50);
					app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/noContextAnswerOutput.cleaned"), 50);
//						PrintWriter noContextEntropyOut = new PrintWriter(new FileOutputStream(new File("noContextEntropy"+i+".data.csv")));
//						PrintWriter contextEntropyOut = new PrintWriter(new FileOutputStream(new File("contextEntropy"+i+".data.csv")));
						PrintWriter samplingData = new PrintWriter(new FileOutputStream(new File("entropyContext-NoContext.data.csv")));
//						
						
						
//						for ( OurHIT currentHIT : app.contextHITs)
//						{
//							contextEntropyOut.println(currentHIT.targetWord + "," + currentHIT.entropy);
//						}
//						for ( OurHIT currentHIT : app.noContextHITs)
//						{
//							noContextEntropyOut.println(currentHIT.targetWord + "," + currentHIT.entropy);
//						}
//						
//						System.out.println(i + "\t" + app.getPearsonCoeff() + "\t" + app.getSpearmanCoeff() );
						
//						if ( i == 50 )
//						{
							app.runEntropySampling(samplingData);
//						}
							
//							app.splitHITs();
//							for (int k = 0 ; k < 24 ; k++)
//							{
//								mirrorData.println(app.contextHITs1.get(k).targetWord + "," + app.contextHITs1.get(k).entropy + "," 
//										+ app.contextHITs2.get(k).targetWord + "," + app.contextHITs2.get(k).entropy);
//							}
//							System.out.println("no context");
//							for (int j = 0 ; j < 24 ; j++)
//							{
//								mirrorData.println(app.noContextHITs1.get(j).targetWord + "," + app.noContextHITs1.get(j).entropy + "," 
//														+ app.noContextHITs2.get(j).targetWord + "," + app.noContextHITs2.get(j).entropy);
//							}
//							System.out.println("25-25 context Spearman : " + app.getMirrorSpearmanCoeff(app.contextHITs1, app.contextHITs2));
//							System.out.println("25-25 context Pearson : " + app.getMirrorPearsonCoeff(app.contextHITs1, app.contextHITs2));
//							System.out.println("25-25 no context Spearman : " + app.getMirrorSpearmanCoeff(app.noContextHITs1, app.noContextHITs2));
//							System.out.println("25-25 no context Pearson : " + app.getMirrorPearsonCoeff(app.noContextHITs1, app.noContextHITs2));
//						}
						samplingData.close();
//						noContextEntropyOut.close();
//						contextEntropyOut.close();
//					}

				} else if (args[0].equals("-checkSimilarity")){
					app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/contextAnswerOutput.cleaned"), 50);
					app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/noContextAnswerOutput.cleaned"), 50);
					PrintWriter similarity = new PrintWriter(new FileOutputStream(new File("similarityData.csv")));

					double overlapIndicator = 0;
					double overlapDivisor = 0;
					double sorensen = 0;
					double jaccardDivisor = 0;
					double jaccardIndicator = 0;
					double cosineIndicator = 0;
					double cosineDivisor = 0;
					double cosineTotal = 0;
					double kLIndicator = 0;
					double kLIndicatorNoContext = 0;
					int topMatch = 0;


					for (SentenceHIT contextHit: app.contextHITs){
						for (SentenceHIT noContextHit: app.noContextHITs){
							Map<String, Integer> contextFreq = contextHit.frequencyCounter;
							Map<String, Integer> noContextFreq = noContextHit.frequencyCounter;
							int weightMatched=0;
							int matched = 0;
							double contextMagnitude = 0;
							double noContextMagnitude = 0;
							cosineIndicator = 0;
							double indivKL = 0;
							if (contextHit.targetWord.equals(noContextHit.targetWord)){
								sorensen += contextHit.answers.size() + noContextHit.answers.size();
								for (String contextSubmission: contextFreq.keySet()){
//									noContextMagnitude = 0;
									for (String noContextSubmission: noContextFreq.keySet()){
										if (contextSubmission.equals(noContextSubmission)){
											double contextSubFreq = contextFreq.get(contextSubmission);
											double noContextSubFreq = noContextFreq.get(noContextSubmission);
											if (contextSubFreq == contextHit.highestFreq && noContextSubFreq == noContextHit.highestFreq)
												topMatch++;
											matched++;
											cosineIndicator += contextSubFreq * noContextSubFreq;
//											kLIndicator += (contextSubFreq/contextHit.answers.size())* Math.log((contextSubFreq/contextHit.answers.size())/(noContextSubFreq/noContextHit.answers.size()));
//											indivKL += (contextSubFreq/contextHit.answers.size())* Math.log((contextSubFreq/contextHit.answers.size())/(noContextSubFreq/noContextHit.answers.size()));
//											kLIndicatorNoContext += (noContextSubFreq/noContextHit.answers.size())* Math.log((noContextSubFreq/noContextHit.answers.size())/(contextSubFreq/contextHit.answers.size()));
											for (int i =0; i < Math.min(contextSubFreq, noContextSubFreq); i++){
												// do match
												weightMatched++;
												overlapIndicator++;
												overlapDivisor++;
											}

											contextMagnitude += contextFreq.get(contextSubmission)* contextFreq.get(contextSubmission);
											noContextMagnitude += noContextFreq.get(noContextSubmission)* noContextFreq.get(noContextSubmission);
										}
									}
								}
								similarity.println("jaccard, " + contextHit.targetWord + ", " + matched/(double)(contextFreq.keySet().size() + noContextFreq.keySet().size() - matched));
								jaccardDivisor += contextFreq.keySet().size() + noContextFreq.keySet().size() - matched;

								jaccardIndicator += matched;
								noContextMagnitude = Math.sqrt(noContextMagnitude);
								contextMagnitude = Math.sqrt(contextMagnitude);
								for (int k =0; k< Math.min(contextHit.answers.size(), noContextHit.answers.size()) - weightMatched; k++){
									//Do no match
									overlapDivisor++;
								}
								similarity.println("Kullback-Liebler, " + contextHit.targetWord + ", " + indivKL);
								cosineDivisor = noContextMagnitude*contextMagnitude;
								similarity.println("Cosine, " + contextHit.targetWord + ", " + cosineIndicator/cosineDivisor);
								cosineTotal += cosineIndicator/cosineDivisor;
								similarity.println("Overlap, " + contextHit.targetWord + ", " + weightMatched/(double)Math.min(contextHit.answers.size(), noContextHit.answers.size()));
								break;
							}
							//									contextMagnitude += contextFreq.get(contextSubmission)* contextFreq.get(contextSubmission);
						}
					}

					System.out.println("Overlap: " + overlapIndicator/overlapDivisor);
					//						System.out.println((2.0*overlapIndicator)/sorensen);
					System.out.println("Jaccard: " + jaccardIndicator/jaccardDivisor);
					System.out.println("Cosine: " + cosineTotal/app.contextHITs.size());
					System.out.println("Kullback-Liebler from context: " + kLIndicator/app.contextHITs.size());
					System.out.println("Kullback-Liebler from no context: " + kLIndicatorNoContext/app.noContextHITs.size());
					System.out.println("percentage of top submissions that match: " + topMatch/app.noContextHITs.size());
					similarity.close();
				}else if (args[0].equals("-checkSimilarityChange")){
						System.out.println("gathering data...");
						// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
						
						for (int i = 5 ; i < 51 ; i += 5)
						{
							app.contextHITs.clear();
							app.noContextHITs.clear();
						app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/contextAnswerOutput.cleaned"), 50);
						app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/noContextAnswerOutput.cleaned"), 50);
//							PrintWriter noContextEntropyOut = new PrintWriter(new FileOutputStream(new File("noContextEntropy"+i+".data.csv")));
							PrintWriter contextEntropyOut = new PrintWriter(new FileOutputStream(new File("CosineSimilarity"+i+".data.csv")));
							PrintWriter samplingData = new PrintWriter(new FileOutputStream(new File("Overlap" + i +".data.csv")));
//							
							
							
//							for ( OurHIT currentHIT : app.contextHITs)
//							{
//								contextEntropyOut.println(currentHIT.targetWord + "," + currentHIT.entropy);
//							}
//							for ( OurHIT currentHIT : app.noContextHITs)
//							{
//								noContextEntropyOut.println(currentHIT.targetWord + "," + currentHIT.entropy);
//							}
//							
//							System.out.println(i + "\t" + app.getPearsonCoeff() + "\t" + app.getSpearmanCoeff() );
							
//							if ( i == 50 )
//							{
								app.runEntropySampling(samplingData);
//							}
								
//								app.splitHITs();
//								for (int k = 0 ; k < 24 ; k++)
//								{
//									mirrorData.println(app.contextHITs1.get(k).targetWord + "," + app.contextHITs1.get(k).entropy + "," 
//											+ app.contextHITs2.get(k).targetWord + "," + app.contextHITs2.get(k).entropy);
//								}
//								System.out.println("no context");
//								for (int j = 0 ; j < 24 ; j++)
//								{
//									mirrorData.println(app.noContextHITs1.get(j).targetWord + "," + app.noContextHITs1.get(j).entropy + "," 
//															+ app.noContextHITs2.get(j).targetWord + "," + app.noContextHITs2.get(j).entropy);
//								}
//								System.out.println("25-25 context Spearman : " + app.getMirrorSpearmanCoeff(app.contextHITs1, app.contextHITs2));
//								System.out.println("25-25 context Pearson : " + app.getMirrorPearsonCoeff(app.contextHITs1, app.contextHITs2));
//								System.out.println("25-25 no context Spearman : " + app.getMirrorSpearmanCoeff(app.noContextHITs1, app.noContextHITs2));
//								System.out.println("25-25 no context Pearson : " + app.getMirrorPearsonCoeff(app.noContextHITs1, app.noContextHITs2));
//							}
							samplingData.close();
//							noContextEntropyOut.close();
//							contextEntropyOut.close();
						}
				} else if (args[0].equals("-StandardStat")){
					PrintWriter statData = new PrintWriter(new FileOutputStream(new File("statData.csv")));
					app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/contextAnswerOutput.cleaned"), 50);
					app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/noContextAnswerOutput.cleaned"), 50);

					for (SentenceHIT contextHit: app.contextHITs){
						statData.println(contextHit.typeID +", " + contextHit.targetWord + ", " + contextHit.highestFreq);
					}
					
					for (SentenceHIT noContextHit: app.noContextHITs){
						statData.println(noContextHit.typeID +", " + noContextHit.targetWord + ", " + noContextHit.highestFreq);
					}
					statData.close();
				}else if (args[0].equals("-selfCheck")){
					File output = new File(args[1] + "SelfSimilarity.csv");
					app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/contextAnswerOutput.cleaned"), 50);
					app.fillHitList(new File("/home/ependergast/LexicalSimplify/code/java/LexicalSimplify/noContextAnswerOutput.cleaned"), 50);
					PrintWriter similarity = new PrintWriter(new FileOutputStream(output));

					System.out.print("Progress");
					for (int x = 0; x<10000; x++){
						if (x % 500 == 0)
							System.out.print(" . ");
					
					double overlapIndicator = 0;
					double overlapDivisor = 0;
					double sorensen = 0;
					double jaccardDivisor = 0;
					double jaccardIndicator = 0;
					double cosineIndicator = 0;
					double cosineDivisor = 0;
					double cosineTotal = 0;
					int topMatch = 0;
					Random hitSplitter = new Random();
					ArrayList<SentenceHIT> data = new ArrayList<SentenceHIT>();
					if (args[2].equals("context"))
						data = app.contextHITs;
					else
						data = app.noContextHITs;
//					for (int x = 0; x<1000; x++){
					for (SentenceHIT currentHit: data){
						ArrayList<String> currentAnswers = new ArrayList<String>(currentHit.answers);
						ArrayList<String> current1Answers = new ArrayList<String>();
						ArrayList<String> current2Answers = new ArrayList<String>();
						for (int i =0; i<24; i++){
							current1Answers.add(currentAnswers.remove(hitSplitter.nextInt(48-i*2)));
							current2Answers.add(currentAnswers.remove(hitSplitter.nextInt((48- i*2) - 1)));
						}
						SentenceHIT currentHit1 = new SentenceHIT(currentHit.ID +"1", currentHit.typeID, currentHit.targetWord, current1Answers);
						SentenceHIT currentHit2 = new SentenceHIT(currentHit.ID +"2", currentHit.typeID, currentHit.targetWord, current2Answers);
						Map<String, Integer> current1Freq = currentHit1.frequencyCounter;
						Map<String, Integer> current2Freq = currentHit2.frequencyCounter;
						int weightMatched=0;
						int matched = 0;
						double current1Magnitude = 0;
						double current2Magnitude = 0;
						cosineIndicator = 0;
						double indivKL = 0;
						if (currentHit1.targetWord.equals(currentHit2.targetWord)){
							sorensen += currentHit1.answers.size() + currentHit2.answers.size();
							for (String currentSubmission: current1Freq.keySet()){
//								noContextMagnitude = 0;
								for (String current2Submission: current2Freq.keySet()){
									if (currentSubmission.equals(current2Submission)){
										double current1SubFreq = current1Freq.get(currentSubmission);
										double current2SubFreq = current2Freq.get(current2Submission);
										if (current1SubFreq == currentHit1.highestFreq && current2SubFreq == currentHit2.highestFreq)
											topMatch++;
										matched++;
										cosineIndicator += current1SubFreq * current2SubFreq;
//										kLIndicator += (contextSubFreq/contextHit.answers.size())* Math.log((contextSubFreq/contextHit.answers.size())/(noContextSubFreq/noContextHit.answers.size()));
//										indivKL += (contextSubFreq/contextHit.answers.size())* Math.log((contextSubFreq/contextHit.answers.size())/(noContextSubFreq/noContextHit.answers.size()));
//										kLIndicatorNoContext += (noContextSubFreq/noContextHit.answers.size())* Math.log((noContextSubFreq/noContextHit.answers.size())/(contextSubFreq/contextHit.answers.size()));
										for (int j =0; j < Math.min(current1SubFreq, current2SubFreq); j++){
											// do match
											weightMatched++;
											overlapIndicator++;
											overlapDivisor++;
										}

										current1Magnitude += current1Freq.get(currentSubmission)* current1Freq.get(currentSubmission);
										current2Magnitude += current2Freq.get(current2Submission)* current2Freq.get(current2Submission);
									}
								}
							}
//							similarity.println("jaccard, " + currentHit1.targetWord + ", " + matched/(double)(current1Freq.keySet().size() + current2Freq.keySet().size() - matched));
							jaccardDivisor += current1Freq.keySet().size() + current2Freq.keySet().size() - matched;

							jaccardIndicator += matched;
							current2Magnitude = Math.sqrt(current2Magnitude);
							current1Magnitude = Math.sqrt(current1Magnitude);
							for (int k =0; k< Math.min(currentHit1.answers.size(), currentHit2.answers.size()) - weightMatched; k++){
								//Do no match
								overlapDivisor++;
//							}
//							similarity.println("Kullback-Liebler, " + currentHit1.targetWord + ", " + indivKL);
							cosineDivisor = current2Magnitude*current1Magnitude;
//							similarity.println("Cosine, " + currentHit1.targetWord + ", " + cosineIndicator/cosineDivisor);
							cosineTotal += cosineIndicator/cosineDivisor;
//							similarity.println("Overlap, " + currentHit1.targetWord + ", " + weightMatched/(double)Math.min(currentHit1.answers.size(), currentHit2.answers.size()));
						}
					}
					}
					similarity.println("Overlap:, " + (double)Math.round((overlapIndicator/overlapDivisor)*1000)/1000.0);
					//						similarity.println((2.0*overlapIndicator)/sorensen);
//					similarity.println("Jaccard:, " + jaccardIndicator/jaccardDivisor);
					similarity.println("Cosine:,  " + (double)Math.round((cosineTotal/data.size())*1000)/1000.0);
//					System.out.println("percentage of top submissions that match: " + topMatch/data.size());
				}
					similarity.close();
					
				}else {
					System.err.println("No valid options were provided");
					System.out.println(usageError);
				}
			}catch (IOException e){
				System.err.println("Could not find the file: \"" + args[1] + "\"");
				System.err.println("Please provide a valid file name");
			}


		}else
			System.out.println(usageError);
	}

	private class Worker
	{
		public ArrayList<String> question1Answers = new ArrayList<String>(26); //the hitTypeId is stored at index 0
		public ArrayList<String> question2Answers = new ArrayList<String>(26);
		public ArrayList<String> question3Answers = new ArrayList<String>(26);

		public String workerId;

		public Worker(String id){
			workerId = id;
			setTypeId(1, "25D2JE1M7PKKF8JGAZQAK04LZYTXQE");
			setTypeId(2, "20ASTLB3L0FBPWA8FU5JZEVE5SUJV7");
			setTypeId(3, "2ZZ2NJQ2172ZFWI1AMBLKVEBU27XPN");
		}

		public void addAnswer(String text, String hitTypeId, int HITindex){
			if (hitTypeId.equals(question1Answers.get(0))){
				question1Answers.add( /*HITindex,*/ text );
			}else if (hitTypeId.equals(question2Answers.get(0))){
				question2Answers.add( /*HITindex,*/ text );
			}else if (hitTypeId.equals(question3Answers.get(0))){
				question3Answers.add( /*HITindex,*/ text );
			}
		}

		public void setTypeId(int question, String typeId){
			switch (question){
			case 1:
				question1Answers.add(0, typeId);
				break;
			case 2:
				question2Answers.add(0, typeId);
				break;
			case 3:
				question3Answers.add(0, typeId);
				break;
			}
		}

	}

	private class FrequencyCounter<K>
    {
        public HashMap<K, Double> frequency = new HashMap<K, Double>();
        public double samples = 0;
        public double mean = 0;
        public double sd = 0;
       
        public void add(K key)
        {
            if (frequency.containsKey(key)){
                frequency.put(key, frequency.get(key) + 1);
            }else {
                frequency.put(key, (double) 1);
            }
            samples++;
        }
       
        public void calcStats()
        {
            for (K key : frequency.keySet() )
            {
                frequency.put(key, frequency.get(key)/samples);
                mean += (Double)key * frequency.get(key);
            }
            // calculating standard deviations
            for (K key : frequency.keySet() )
            {
                sd += frequency.get(key) * Math.pow((Double)key - mean, 2);
            }
            sd = Math.sqrt(sd);
        }
   
        public void printCSV(PrintWriter out)
        {
            for ( K key : frequency.keySet() )
            {
                out.printf(key + ", %.8f", frequency.get(key) );
                out.println();
            }
        }
    }
}
