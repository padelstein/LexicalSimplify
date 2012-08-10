// USAGE
// to get data from amazon use argument line: -amazon [contextIDfile] [partialContextIDfile] [noContextIDfile] [wordToSenseFile]
// to get sampling data from cleaned annotations use argument line: -cleaned [contextFile] [partialContextFile] [noContextFile] [wordToSenseFile]

package simplify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.service.exception.ServiceException;

public class DataCollection {

	// arrayLists for storing HITs and the half-HITs for random sampling
	private ArrayList<OurHIT> contextHITs = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> contextHITs1 = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> contextHITs2 = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> partialContextHITs = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> partialContextHITs1 = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> partialContextHITs2 = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> noContextHITs = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> noContextHITs1 = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> noContextHITs2 = new ArrayList<OurHIT>();

	// stores all the workers that have provided annotations
	private ArrayList<Worker> workers = new ArrayList<Worker>();

	/** 
	 * Basic constructor
	 */
	public DataCollection()
	{
	}

	/** Compiles worker data for single given HIT
	 * 
	 * @param an OurHIT
	 */
	public void fillWorkerArray(OurHIT currentHIT)
	{
		for (Assignment assignment: currentHIT.assignments ){
			String workerID = assignment.getWorkerId();
			Worker currentWorker = null;
			boolean alreadyDocumentedWorker = false;
			String answerText = null;
			if( assignment.getAssignmentStatus().equals(AssignmentStatus.Approved) )
			{
				int textStart = assignment.getAnswer().indexOf("<FreeText>");
				int textEnd = assignment.getAnswer().indexOf("</FreeText>");
				answerText = assignment.getAnswer().substring(textStart + 10, textEnd).toLowerCase();
				answerText = answerText.trim();
			}
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
				currentWorker.addAnswer(currentHIT.typeID, answerText);
			} else {
				currentWorker = new Worker(workerID);
				workers.add(currentWorker);
				currentWorker.addAnswer(currentHIT.typeID, answerText);
			}

		}
	}

	/** Retrieves HITs directly from Amazon Mechanical Turk database and processes them
	 * 
	 * @param Word to sense map
	 * @param Context HIT Ids file
	 * @param Partial context HIT Ids file
	 * @param No context HIT Ids file
	 * @param Number of annotations
	 * @throws IOException
	 */
	public void getHITsFromAmazon(Map<String, String[]> wordToSense, File contextIDfile, File partialContextIDfile, File noContextIDfile, int numAnnotations) throws IOException
	{
		// instantiate the ID file readers
		BufferedReader contextIDreader = new BufferedReader(new InputStreamReader(new FileInputStream( contextIDfile )));
		BufferedReader partialContextIDreader = new BufferedReader(new InputStreamReader(new FileInputStream( partialContextIDfile )));
		BufferedReader noContextIDreader = new BufferedReader(new InputStreamReader(new FileInputStream( noContextIDfile )));

		contextHITs.clear();
		partialContextHITs.clear();
		noContextHITs.clear();

		try 
		{
			String contextID = contextIDreader.readLine();
			String partialContextID = partialContextIDreader.readLine();
			String noContextID = noContextIDreader.readLine();

			while ( contextID != null )
			{
				// instantiates current HITs using Amazon service 
				OurHIT currentContextHIT = new OurHIT(contextID, wordToSense, numAnnotations);
				OurHIT currentPartialContextHIT = new OurHIT(partialContextID, wordToSense, numAnnotations);
				OurHIT currentNoContextHIT = new OurHIT(noContextID, wordToSense, numAnnotations);

				// used below to compute frequency of most frequent annotation
				ArrayList<String>  contextAnswers = currentContextHIT.answers;
				ArrayList<String>  partialContextAnswers = currentPartialContextHIT.answers;
				ArrayList<String>  noContextAnswers = currentNoContextHIT.answers;

				System.out.println("Retrieved HITs: " + currentContextHIT.ID + ", " + currentPartialContextHIT.ID + ", " + currentNoContextHIT.ID );

				// fills HIT arrays with current HITs
				contextHITs.add(currentContextHIT);
				partialContextHITs.add(currentPartialContextHIT);
				noContextHITs.add(currentNoContextHIT);

				// compiles worker data for current HITs
				fillWorkerArray(currentContextHIT);
				fillWorkerArray(currentPartialContextHIT);
				fillWorkerArray(currentNoContextHIT);

				// computes frequency of most frequent annotation
				for (String text: currentContextHIT.frequencyCounter.keySet())
				{
					int freq = currentContextHIT.frequencyCounter.get(text);
					if (freq == currentContextHIT.highestFreq)
					{
						contextAnswers.add(text.trim());
					}
				}

				for (String text: currentPartialContextHIT.frequencyCounter.keySet())
				{
					int freq = currentPartialContextHIT.frequencyCounter.get(text);
					if (freq == currentPartialContextHIT.highestFreq)
					{
						partialContextAnswers.add(text.trim());
					}
				}

				for (String text: currentNoContextHIT.frequencyCounter.keySet())
				{
					int freq = currentNoContextHIT.frequencyCounter.get(text);
					if (freq == currentNoContextHIT.highestFreq)
					{
						noContextAnswers.add(text.trim());
					}
				}
				// set-up readers for next set of HITs
				contextID = contextIDreader.readLine();
				partialContextID = partialContextIDreader.readLine();
				noContextID = noContextIDreader.readLine();

			}
		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}

	
	/**Prints out the top submissions for each hit where a top submissions is one having 10 or more, uses the given list of hits and print writer
	 * 
	 * @param List of HITs
	 * @param PrintWriter to output to
	 */
	public void getTopSubs(ArrayList<OurHIT> hitList, PrintWriter output){
		for (OurHIT currentHIT: hitList) {
			output.println(currentHIT.targetWord + "\t");
			String wordList = "";
			boolean topAnswers = false;
			for (String text: currentHIT.frequencyCounter.keySet()){
				int freq = currentHIT.frequencyCounter.get(text);
				if (freq >=3){
					if(!topAnswers)
						output.print("Top submissions were: ");
					output.print(text + ": " + freq + " ");
					topAnswers = true;
				} else
					wordList += text + ": " + freq + " ";
			}
			if (!topAnswers){
				output.print("No majority submission, all submissions are: " + wordList);
			}
			output.println();
		}
	}
	
	/**  Fills the HIT arrays (contextHITs, partialContextHIts and noContextHITs) using annotations in the given files 
	 *  
	 * @param Word to sense map
	 * @param File containing context HIT annotations
	 * @param File containing partial context HIT annotations
	 * @param File containing no context HIT annotations
	 * @param Number of annotations
	 * @throws IOException
	 */
	public void getHITsFromFiles(Map<String, String[]> wordToSense, File contextFile, File partialContextFile, File noContextFile, int numAnnotations) throws IOException
	{
		// instantiate the annotation file readers
		BufferedReader contextReader = new BufferedReader(new InputStreamReader(new FileInputStream( contextFile )));
		BufferedReader partialContextReader = new BufferedReader(new InputStreamReader(new FileInputStream( partialContextFile )));
		BufferedReader noContextReader = new BufferedReader(new InputStreamReader(new FileInputStream( noContextFile )));

		contextHITs.clear();
		partialContextHITs.clear();
		noContextHITs.clear();

		try 
		{
			// read the first lines from the files
			String contextLine = contextReader.readLine();
			String partialContextLine = partialContextReader.readLine();
			String noContextLine = noContextReader.readLine();

			while ( contextLine != null )
			{
				// split the lines on tabs, lines are of the form
				// ID \t typeID \t targetWord \t annotation1 \t annotation2 ... \t annotation50
				String[] contextWords = contextLine.split("\t");
				String[] partialContextWords = partialContextLine.split("\t");
				String[] noContextWords = noContextLine.split("\t");

				// set up the answer arrays
				ArrayList<String> contextAnswers = new ArrayList<String>(numAnnotations);
				ArrayList<String> partialContextAnswers = new ArrayList<String>(numAnnotations);
				ArrayList<String> noContextAnswers = new ArrayList<String>(numAnnotations);

				// grab the IDs
				String contextID = contextWords[0];
				String partialContextID = partialContextWords[0];
				String noContextID = noContextWords[0];

				// grab the type IDs
				String contextTypeID = contextWords[1];
				String partialContextTypeID = partialContextWords[1];
				String noContextTypeID = noContextWords[1];

				// grab the target words
				String contextTargetWord = contextWords[2];
				String partialContextTargetWord = partialContextWords[2];
				String noContextTargetWord = noContextWords[2];

				// grab the annotations
				for ( int i = 3; i < 3 + numAnnotations; i++)
				{
					if ( i < contextWords.length )
					{
						contextAnswers.add(contextWords[i].trim());
					}
					if ( i < partialContextWords.length )
					{
						partialContextAnswers.add(partialContextWords[i].trim());
					}
					if ( i < noContextWords.length )
					{
						noContextAnswers.add(noContextWords[i].trim());
					}
				}

				// instantiates current HITs
				OurHIT currentContextHIT = new OurHIT(contextID, contextTypeID, contextTargetWord, contextAnswers, numAnnotations);
				OurHIT currentPartialContextHIT = new OurHIT(partialContextID, partialContextTypeID, partialContextTargetWord, partialContextAnswers, numAnnotations);
				OurHIT currentNoContextHIT = new OurHIT(noContextID, noContextTypeID, noContextTargetWord, noContextAnswers, numAnnotations);

				System.out.println("Retrieved HITs: " + currentContextHIT.targetWord + ", " + currentPartialContextHIT.targetWord + ", " + currentNoContextHIT.targetWord );

				// fills HIT arrays with current HITs
				contextHITs.add(currentContextHIT);
				partialContextHITs.add(currentPartialContextHIT);
				noContextHITs.add(currentNoContextHIT);

				// determine frequency of most common annotation
				for (String text: currentContextHIT.frequencyCounter.keySet())
				{
					int freq = currentContextHIT.frequencyCounter.get(text);
					if (freq == currentContextHIT.highestFreq)
					{
						contextAnswers.add(text.trim());
					}
				}

				for (String text: currentPartialContextHIT.frequencyCounter.keySet())
				{
					int freq = currentPartialContextHIT.frequencyCounter.get(text);
					if (freq == currentPartialContextHIT.highestFreq)
					{
						partialContextAnswers.add(text.trim());
					}
				}

				for (String text: currentNoContextHIT.frequencyCounter.keySet())
				{
					int freq = currentNoContextHIT.frequencyCounter.get(text);
					if (freq == currentNoContextHIT.highestFreq)
					{
						noContextAnswers.add(text.trim());
					}
				}
				// set-up readers for next HIT
				contextLine = contextReader.readLine();
				partialContextLine = partialContextReader.readLine();
				noContextLine = noContextReader.readLine();

			}
		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}

	/**
	 * Randomly splits each target word's annotations in half taken from the values in the ArrayLists contextHITs, partialContextHITs and noContextHITs
	 * Used for random sampling of data
	 */
	public void splitHITs()
	{
		// clear the the arrays that hold the randomly split HITs
		contextHITs1.clear();
		contextHITs2.clear();
		partialContextHITs1.clear();
		partialContextHITs2.clear();
		noContextHITs1.clear();
		noContextHITs2.clear();

		Random hitSplitter = new Random();

		int numHITs = contextHITs.size();

		// iterate through every HIT
		for (int n = 0 ; n < numHITs ; n++)
		{
			// retrieve the HIT from each environment
			OurHIT currentContextHIT = contextHITs.get(n);
			OurHIT currentPartialContextHIT = partialContextHITs.get(n);
			OurHIT currentNoContextHIT = noContextHITs.get(n);

			// context answers
			ArrayList<String> currentContextAnswers = new ArrayList<String>(currentContextHIT.answers);
			ArrayList<String> currentContextAnswers1 = new ArrayList<String>();
			ArrayList<String> currentContextAnswers2 = new ArrayList<String>();

			// partial-context answers
			ArrayList<String> currentPartialContextAnswers = new ArrayList<String>(currentPartialContextHIT.answers);
			ArrayList<String> currentPartialContextAnswers1 = new ArrayList<String>();
			ArrayList<String> currentPartialContextAnswers2 = new ArrayList<String>();

			// no-context answers
			ArrayList<String> currentNoContextAnswers = new ArrayList<String>(currentNoContextHIT.answers);
			ArrayList<String> currentNoContextAnswers1 = new ArrayList<String>();
			ArrayList<String> currentNoContextAnswers2 = new ArrayList<String>();

			// we randomly split 50 annotations into two sets of 25
			for (int i = 0 ; i < 25; i++){
				// context
				currentContextAnswers1.add(currentContextAnswers.remove(hitSplitter.nextInt(50-i*2)));
				currentContextAnswers2.add(currentContextAnswers.remove(hitSplitter.nextInt((50- i*2) - 1)));
				// partial-context
				currentPartialContextAnswers1.add(currentPartialContextAnswers.remove(hitSplitter.nextInt(50-i*2)));
				currentPartialContextAnswers2.add(currentPartialContextAnswers.remove(hitSplitter.nextInt((50- i*2) - 1)));
				// no-context
				currentNoContextAnswers1.add(currentNoContextAnswers.remove(hitSplitter.nextInt(50-i*2)));
				currentNoContextAnswers2.add(currentNoContextAnswers.remove(hitSplitter.nextInt((50- i*2) - 1)));
			}

			// quick and dirty OurHIT constructors for speed
			// then add them to the global arrays
			OurHIT currentContextHIT1 = new OurHIT(currentContextAnswers1);
			OurHIT currentContextHIT2 = new OurHIT(currentContextAnswers2);
			contextHITs1.add(currentContextHIT1);
			contextHITs2.add(currentContextHIT2);

			OurHIT currentPartialContextHIT1 = new OurHIT(currentPartialContextAnswers1);
			OurHIT currentPartialContextHIT2 = new OurHIT(currentPartialContextAnswers2);
			partialContextHITs1.add(currentPartialContextHIT1);
			partialContextHITs2.add(currentPartialContextHIT2);

			OurHIT currentNoContextHIT1 = new OurHIT(currentNoContextAnswers1);
			OurHIT currentNoContextHIT2 = new OurHIT(currentNoContextAnswers2);
			noContextHITs1.add(currentNoContextHIT1);
			noContextHITs2.add(currentNoContextHIT2);

		}
	}

	/** Calculates and returns the Pearson coefficient for the two given lists of HITs
	 * 
	 * @param First list of HITs
	 * @param Second list of HITs
	 * @return a double representing the Pearson coefficient between the two HITs
	 */
	public double getPearsonCoeff(ArrayList<OurHIT> list1, ArrayList<OurHIT> list2)
	{
		double answer = 0;
		double[] array1 = new double[list1.size()];
		double[] array2 = new double[list1.size()];

		for (int i = 0 ; i < list1.size() ; i++)
		{
			array1[i] = list1.get(i).entropy;
			array2[i] = list2.get(i).entropy;
		}

		PearsonsCorrelation corr = new PearsonsCorrelation();
		answer = corr.correlation(array1, array2);
		return answer;
	}

	/** Calculates and returns the Spearman coefficient for the two given lists of HITs
	 * 
	 * @param First list of HITs
	 * @param Second list of HITs
	 * @return a double representing the Spearman coefficient between the two HITs
	 */
	public double getSpearmanCoeff(ArrayList<OurHIT> list1, ArrayList<OurHIT> list2)
	{
		double answer = 0;
		double[] array1 = new double[list1.size()];
		double[] array2 = new double[list1.size()];

		for (int i = 0 ; i < list1.size() ; i++)
		{
			array1[i] = list1.get(i).entropy;
			array2[i] = list2.get(i).entropy;
		}

		SpearmansCorrelation corr = new SpearmansCorrelation();
		answer = corr.correlation(array1, array2);
		return answer;
	}

	/** Calculates and returns the overlap coefficient for the two given HITs
	 * 
	 * @param First HIT
	 * @param Second HIT
	 * @return a double representing the overlap similarity between the two HITs
	 */
	public double getOverlap(OurHIT contextHit, OurHIT noContextHit){
		Map<String, Integer> contextFreq = contextHit.frequencyCounter;
		Map<String, Integer> noContextFreq = noContextHit.frequencyCounter;
		int weightMatched=0;
		if (contextHit.targetWord.equals(noContextHit.targetWord)){
			for (String contextSubmission: contextFreq.keySet()){
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

	/** Calculates and return the Cosine similarity coefficient for the two given HITs
	 * 
	 * @param First HIT
	 * @param Second HIT
	 * @return a double representing the cosine similarity between the two HITs
	 */
	public double getCosine(OurHIT contextHit, OurHIT noContextHit){
		Map<String, Integer> contextFreq = contextHit.frequencyCounter;
		Map<String, Integer> noContextFreq = noContextHit.frequencyCounter;
		double contextMagnitude = 0;
		double noContextMagnitude = 0;
		double cosineIndicator = 0;
		if (contextHit.targetWord.equals(noContextHit.targetWord)){
			for (String contextSubmission: contextFreq.keySet()){
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
	}

	/** Turns the file containing information for the HITs into the a word to sense map
	 * 
	 * @param File that has the HIT information: Sentence, target index, target word, simple word, word sense
	 * @return A word to sense map
	 * @throws IOException
	 */
	public Map<String, String[]> getWordToSense(File senseFile) throws IOException
	{	
		BufferedReader senseReader = new BufferedReader(new InputStreamReader(new FileInputStream( senseFile )));
		String input = senseReader.readLine();
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

			input = senseReader.readLine();
		}
		return wordToSense;
	}

	/** Rounds a double to 4 decimal places
	 * 
	 * @param double to be rounded
	 * @return The rounded double
	 */
	public static double round(double value) {
		long factor = (long) Math.pow(10, 4);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	// takes context ID, partial-context ID, and no-context ID files in that order
	public static void main(String[] args) throws IOException {

		DataCollection app = new DataCollection();

		try {
			int samples = 10000;

			String flag = args[0];
			File contextFile = new File(args[1]);
			File partialContextFile = new File(args[2]);
			File noContextFile = new File(args[3]);
			File senseFile = new File(args[4]);

			PrintWriter entropyDistributions = new PrintWriter(new FileOutputStream(new File("NewExperimentData/entropy.distributions.data.csv")));
			PrintWriter entropyCorrelationDistributions = new PrintWriter(new FileOutputStream(new File("NewExperimentData/entropy_correlation.distributions.data.csv")));
			PrintWriter similarityDistributions = new PrintWriter(new FileOutputStream(new File("NewExperimentData/similarity.distributions.data.csv")));

			PrintWriter standardData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/standard.data.csv")));

			PrintWriter contextData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/context.data.csv")));
			PrintWriter partialContextData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/partialContext.data.csv")));
			PrintWriter noContextData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/noContext.data.csv")));

			PrintWriter workerData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/worker.data.csv")));

			// create 25 annotation HITs to get word by word data
			if (flag.equals("-cleaned"))
			{
				app.getHITsFromFiles(app.getWordToSense(senseFile), contextFile, partialContextFile, noContextFile, 25);
			} else if ( flag.equals("-amazon"))
			{
				app.getHITsFromAmazon(app.getWordToSense(senseFile), contextFile, partialContextFile, noContextFile, 25);
			} else {
				System.out.println("incorrect flag, use either -cleaned or -amazon");
			}

			int numHITs = app.contextHITs.size();

			// print the data word by word
			standardData.println("target word, context entropy, partial-context entropy, no-context entropy, , top cosine, basic cosine, bottom cosine, , top overlap, basic overlap, bottom overlap");
			for (int k = 0 ; k < numHITs ; k++)
			{
				OurHIT contextHIT = app.contextHITs.get(k);
				OurHIT partialContextHIT = app.partialContextHITs.get(k);
				OurHIT noContextHIT = app.noContextHITs.get(k);

				if ( contextHIT.targetWord.equals(partialContextHIT.targetWord) && contextHIT.targetWord.equals(noContextHIT.targetWord) )
				{
					standardData.println(contextHIT.targetWord + ","
							+ contextHIT.entropy + "," + partialContextHIT.entropy + "," + noContextHIT.entropy + ", ," 
							+ app.getCosine(contextHIT, partialContextHIT) + "," + app.getCosine(contextHIT, noContextHIT) + "," + app.getCosine(contextHIT, noContextHIT) + ", ,"
							+ app.getOverlap(contextHIT, partialContextHIT) + "," + app.getOverlap(contextHIT, noContextHIT) + "," + app.getOverlap(contextHIT, noContextHIT) );
				}

			}
			standardData.close();

			// create 50 annotation HITs to get sampling data
			if (flag.equals("-cleaned"))
			{
				app.getHITsFromFiles(app.getWordToSense(senseFile), contextFile, partialContextFile, noContextFile, 50);
			} else if ( flag.equals("-amazon"))
			{
				app.getHITsFromAmazon(app.getWordToSense(senseFile), contextFile, partialContextFile, noContextFile, 50);
			} else {
				System.out.println("incorrect flag, use either -cleaned or -amazon");
			}

			// printing worker data
			if ( !app.workers.isEmpty() )
			{
				for (Worker worker : app.workers)
				{
					workerData.println(worker.toString());
				}
			}
			workerData.close();

			for ( OurHIT hit : app.contextHITs)
			{
				contextData.println(hit.toString());
			}
			for ( OurHIT hit : app.partialContextHITs)
			{
				partialContextData.println(hit.toString());
			}
			for ( OurHIT hit : app.noContextHITs)
			{
				noContextData.println(hit.toString());
			}

			contextData.close();
			partialContextData.close();
			noContextData.close();

			// entropies of all target words
			FrequencyCounter contextEntropyFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextEntropyFreq = app.new FrequencyCounter();
			FrequencyCounter noContextEntropyFreq = app.new FrequencyCounter();

			// mean entropies of all target words
			FrequencyCounter contextMeanEntropyFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextMeanEntropyFreq = app.new FrequencyCounter();
			FrequencyCounter noContextMeanEntropyFreq = app.new FrequencyCounter();

			// differences in entropies of all target words
			FrequencyCounter contextEntropyDiffFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextEntropyDiffFreq = app.new FrequencyCounter();
			FrequencyCounter noContextEntropyDiffFreq = app.new FrequencyCounter();
			// inter-environment differences
			FrequencyCounter topEntropyDiffFreq = app.new FrequencyCounter();
			FrequencyCounter basicEntropyDiffFreq = app.new FrequencyCounter();
			FrequencyCounter bottomEntropyDiffFreq = app.new FrequencyCounter();

			// differences of means in entropies of all target words
			FrequencyCounter contextEntropyDiffOfMeanFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextEntropyDiffOfMeanFreq = app.new FrequencyCounter();
			FrequencyCounter noContextEntropyDiffOfMeanFreq = app.new FrequencyCounter();
			// inter-environment differences of means
			FrequencyCounter topEntropyDiffOfMeanFreq = app.new FrequencyCounter();
			FrequencyCounter basicEntropyDiffOfMeanFreq = app.new FrequencyCounter();
			FrequencyCounter bottomEntropyDiffOfMeanFreq = app.new FrequencyCounter();

			// means of differences of entropies of all target words
			FrequencyCounter contextEntropyMeanOfDiffFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextEntropyMeanOfDiffFreq = app.new FrequencyCounter();
			FrequencyCounter noContextEntropyMeanOfDiffFreq = app.new FrequencyCounter();
			// inter-environment means of differences
			FrequencyCounter topEntropyMeanOfDiffFreq = app.new FrequencyCounter();
			FrequencyCounter basicEntropyMeanOfDiffFreq = app.new FrequencyCounter();
			FrequencyCounter bottomEntropyMeanOfDiffFreq = app.new FrequencyCounter();

			// Pearson correlations
			FrequencyCounter contextPearsonFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextPearsonFreq = app.new FrequencyCounter();
			FrequencyCounter noContextPearsonFreq = app.new FrequencyCounter();
			// inter-environment correlations
			FrequencyCounter topPearsonFreq = app.new FrequencyCounter();
			FrequencyCounter basicPearsonFreq = app.new FrequencyCounter();
			FrequencyCounter bottomPearsonFreq = app.new FrequencyCounter();

			// Spearman correlations
			FrequencyCounter contextSpearmanFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextSpearmanFreq = app.new FrequencyCounter();
			FrequencyCounter noContextSpearmanFreq = app.new FrequencyCounter();
			// inter-environment correlations
			FrequencyCounter topSpearmanFreq = app.new FrequencyCounter();
			FrequencyCounter basicSpearmanFreq = app.new FrequencyCounter();
			FrequencyCounter bottomSpearmanFreq = app.new FrequencyCounter();

			// Cosine similarities
			FrequencyCounter contextCosineFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextCosineFreq = app.new FrequencyCounter();
			FrequencyCounter noContextCosineFreq = app.new FrequencyCounter();
			// inter-environment cosine
			FrequencyCounter topCosineFreq = app.new FrequencyCounter();
			FrequencyCounter basicCosineFreq = app.new FrequencyCounter();
			FrequencyCounter bottomCosineFreq = app.new FrequencyCounter();

			// Mean Cosine similarities
			FrequencyCounter contextMeanCosineFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextMeanCosineFreq = app.new FrequencyCounter();
			FrequencyCounter noContextMeanCosineFreq = app.new FrequencyCounter();
			// inter-environment mean cosine
			FrequencyCounter topMeanCosineFreq = app.new FrequencyCounter();
			FrequencyCounter basicMeanCosineFreq = app.new FrequencyCounter();
			FrequencyCounter bottomMeanCosineFreq = app.new FrequencyCounter();

			// Overlap similarities
			FrequencyCounter contextOverlapFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextOverlapFreq = app.new FrequencyCounter();
			FrequencyCounter noContextOverlapFreq = app.new FrequencyCounter();
			// inter-environment Overlap
			FrequencyCounter topOverlapFreq = app.new FrequencyCounter();
			FrequencyCounter basicOverlapFreq = app.new FrequencyCounter();
			FrequencyCounter bottomOverlapFreq = app.new FrequencyCounter();

			// Mean Overlap similarities
			FrequencyCounter contextMeanOverlapFreq = app.new FrequencyCounter();
			FrequencyCounter partialContextMeanOverlapFreq = app.new FrequencyCounter();
			FrequencyCounter noContextMeanOverlapFreq = app.new FrequencyCounter();
			// inter-environment mean Overlap
			FrequencyCounter topMeanOverlapFreq = app.new FrequencyCounter();
			FrequencyCounter basicMeanOverlapFreq = app.new FrequencyCounter();
			FrequencyCounter bottomMeanOverlapFreq = app.new FrequencyCounter();

			// run the sampling stuff if the flag is correct
			if (flag.equals("-cleaned"))
			{
				for (int i = 0 ; i < samples ; i++)
				{
					System.out.println("sample #:" + i);
					// creates a random split of every HIT's annotations
					// these are stored as OurHITs in the following ArrayLists
					// contextHITs1, contextHITs2,
					// partialContextHITs1, partialContextHITs2,
					// noContextHITs1, and noContextHITs2
					app.splitHITs();

					// these following FrequencyCounters are for determining means for this random split
					// take both from 1 and 2 because we want to know the difference in mean entropy
					FrequencyCounter split_contextEntropy1 = app.new FrequencyCounter();
					FrequencyCounter split_partialContextEntropy1 = app.new FrequencyCounter();
					FrequencyCounter split_noContextEntropy1 = app.new FrequencyCounter();
					FrequencyCounter split_contextEntropy2 = app.new FrequencyCounter();
					FrequencyCounter split_partialContextEntropy2 = app.new FrequencyCounter();
					FrequencyCounter split_noContextEntropy2 = app.new FrequencyCounter();

					FrequencyCounter split_contextDiff = app.new FrequencyCounter();
					FrequencyCounter split_partialContextDiff = app.new FrequencyCounter();
					FrequencyCounter split_noContextDiff = app.new FrequencyCounter();
					FrequencyCounter split_topDiff = app.new FrequencyCounter();
					FrequencyCounter split_basicDiff = app.new FrequencyCounter();
					FrequencyCounter split_bottomDiff = app.new FrequencyCounter();

					FrequencyCounter split_contextCosine = app.new FrequencyCounter();
					FrequencyCounter split_partialContextCosine = app.new FrequencyCounter();
					FrequencyCounter split_noContextCosine = app.new FrequencyCounter();
					FrequencyCounter split_topCosine = app.new FrequencyCounter();
					FrequencyCounter split_basicCosine = app.new FrequencyCounter();
					FrequencyCounter split_bottomCosine = app.new FrequencyCounter();

					FrequencyCounter split_contextOverlap = app.new FrequencyCounter();
					FrequencyCounter split_partialContextOverlap = app.new FrequencyCounter();
					FrequencyCounter split_noContextOverlap = app.new FrequencyCounter();
					FrequencyCounter split_topOverlap = app.new FrequencyCounter();
					FrequencyCounter split_basicOverlap = app.new FrequencyCounter();
					FrequencyCounter split_bottomOverlap = app.new FrequencyCounter();

					// determining metrics for this random split
					for (int j = 0 ; j < numHITs ; j++)
					{	
						// getting randomly split context HITs, retrieving entropy
						OurHIT contextHIT1 = app.contextHITs1.get(j);
						OurHIT contextHIT2 = app.contextHITs2.get(j);
						double contextEntropy1 = round(contextHIT1.entropy);
						double contextEntropy2 = round(contextHIT2.entropy);
						double contextEntropyDiff = round(contextEntropy1 - contextEntropy2);

						// getting randomly split partial-context HITs, retrieving entropy
						OurHIT partialContextHIT1 = app.partialContextHITs1.get(j);
						OurHIT partialContextHIT2 = app.partialContextHITs2.get(j);
						double partialContextEntropy1 = round(partialContextHIT1.entropy);
						double partialContextEntropy2 = round(partialContextHIT2.entropy);
						double partialContextEntropyDiff = round(partialContextEntropy1 - partialContextEntropy2);

						// getting randomly split no-context HITs, retrieving entropy
						OurHIT noContextHIT1 = app.noContextHITs1.get(j);
						OurHIT noContextHIT2 = app.noContextHITs2.get(j);
						double noContextEntropy1 = round(noContextHIT1.entropy);
						double noContextEntropy2 = round(noContextHIT2.entropy);
						double noContextEntropyDiff = round(noContextEntropy1 - noContextEntropy2);

						// inter-environment entropy differences
						double topEntropyDiff = round(contextEntropy1 - partialContextEntropy1);
						double basicEntropyDiff = round(contextEntropy1 - noContextEntropy1);
						double bottomEntropyDiff = round(partialContextEntropy1 - noContextEntropy1);

						// add entropies and diffs for determining means for this random split
						split_contextEntropy1.add(contextEntropy1);
						split_partialContextEntropy1.add(partialContextEntropy1);
						split_noContextEntropy1.add(noContextEntropy1);
						split_contextEntropy2.add(contextEntropy2);
						split_partialContextEntropy2.add(partialContextEntropy2);
						split_noContextEntropy2.add(noContextEntropy2);

						split_contextDiff.add(contextEntropyDiff);
						split_partialContextDiff.add(partialContextEntropyDiff);
						split_noContextDiff.add(noContextEntropyDiff);
						split_topDiff.add(topEntropyDiff);
						split_basicDiff.add(basicEntropyDiff);
						split_bottomDiff.add(bottomEntropyDiff);

						// adding to entropy distributions
						contextEntropyFreq.add(contextEntropy1);
						partialContextEntropyFreq.add(partialContextEntropy1);
						noContextEntropyFreq.add(noContextEntropy1);

						// adding to entropy diff distributions
						contextEntropyDiffFreq.add(contextEntropyDiff);
						partialContextEntropyDiffFreq.add(partialContextEntropyDiff);
						noContextEntropyDiffFreq.add(noContextEntropyDiff);
						topEntropyDiffFreq.add(topEntropyDiff);
						basicEntropyDiffFreq.add(basicEntropyDiff);
						bottomEntropyDiffFreq.add(bottomEntropyDiff);

						// get cosine coefficients
						double contextCosine = round(app.getCosine(contextHIT1, contextHIT2));
						double partialContextCosine = round(app.getCosine(partialContextHIT1, partialContextHIT2));
						double noContextCosine = round(app.getCosine(noContextHIT1, noContextHIT2));
						double topCosine = round(app.getCosine(contextHIT1, partialContextHIT1));
						double basicCosine = round(app.getCosine(contextHIT1, noContextHIT1));
						double bottomCosine = round(app.getCosine(partialContextHIT1, noContextHIT1));

						// adding to cosine distributions
						contextCosineFreq.add(contextCosine);
						partialContextCosineFreq.add(partialContextCosine);
						noContextCosineFreq.add(noContextCosine);
						topCosineFreq.add(topCosine);
						basicCosineFreq.add(basicCosine);
						bottomCosineFreq.add(bottomCosine);
						
						split_contextCosine.add(contextCosine);
						split_partialContextCosine.add(partialContextCosine);
						split_noContextCosine.add(noContextCosine);
						split_topCosine.add(topCosine);
						split_basicCosine.add(basicCosine);
						split_bottomCosine.add(bottomCosine);

						// get overlap coefficients
						double contextOverlap = round(app.getOverlap(contextHIT1, contextHIT2));
						double partialContextOverlap = round(app.getOverlap(partialContextHIT1, partialContextHIT2));
						double noContextOverlap = round(app.getOverlap(noContextHIT1, noContextHIT2));
						double topOverlap = round(app.getOverlap(contextHIT1, partialContextHIT1));
						double basicOverlap = round(app.getOverlap(contextHIT1, noContextHIT1));
						double bottomOverlap = round(app.getOverlap(partialContextHIT1, noContextHIT1));

						// adding to overlap distributions
						contextOverlapFreq.add(contextOverlap);
						partialContextOverlapFreq.add(partialContextOverlap);
						noContextOverlapFreq.add(noContextOverlap);
						topOverlapFreq.add(topOverlap);
						basicOverlapFreq.add(basicOverlap);
						bottomOverlapFreq.add(bottomOverlap);

						split_contextOverlap.add(contextOverlap);
						split_partialContextOverlap.add(partialContextOverlap);
						split_noContextOverlap.add(noContextOverlap);
						split_topOverlap.add(topOverlap);
						split_basicOverlap.add(basicOverlap);
						split_bottomOverlap.add(bottomOverlap);

					}

					// adding to Pearson distributions
					contextPearsonFreq.add(round(app.getPearsonCoeff(app.contextHITs1, app.contextHITs2)));
					partialContextPearsonFreq.add(round(app.getPearsonCoeff(app.partialContextHITs1, app.partialContextHITs2)));
					noContextPearsonFreq.add(round(app.getPearsonCoeff(app.noContextHITs1, app.noContextHITs2)));
					topPearsonFreq.add(round(app.getPearsonCoeff(app.contextHITs1, app.partialContextHITs1)));
					basicPearsonFreq.add(round(app.getPearsonCoeff(app.contextHITs1, app.noContextHITs1)));
					bottomPearsonFreq.add(round(app.getPearsonCoeff(app.partialContextHITs1, app.noContextHITs1)));

					// adding to Spearman distributions
					contextSpearmanFreq.add(round(app.getSpearmanCoeff(app.contextHITs1, app.contextHITs2)));
					partialContextSpearmanFreq.add(round(app.getSpearmanCoeff(app.partialContextHITs1, app.partialContextHITs2)));
					noContextSpearmanFreq.add(round(app.getSpearmanCoeff(app.noContextHITs1, app.noContextHITs2)));
					topSpearmanFreq.add(round(app.getSpearmanCoeff(app.contextHITs1, app.partialContextHITs1)));
					basicSpearmanFreq.add(round(app.getSpearmanCoeff(app.contextHITs1, app.noContextHITs1)));
					bottomSpearmanFreq.add(round(app.getSpearmanCoeff(app.partialContextHITs1, app.noContextHITs1)));

					// calculating stats for this random split's means
					split_contextEntropy1.calcStats();
					split_partialContextEntropy1.calcStats();
					split_noContextEntropy1.calcStats();
					split_contextEntropy2.calcStats();
					split_partialContextEntropy2.calcStats();
					split_noContextEntropy2.calcStats();

					split_contextDiff.calcStats();
					split_partialContextDiff.calcStats();
					split_noContextDiff.calcStats();
					split_topDiff.calcStats();
					split_basicDiff.calcStats();
					split_bottomDiff.calcStats();

					split_contextCosine.calcStats();
					split_partialContextCosine.calcStats();
					split_noContextCosine.calcStats();
					split_topCosine.calcStats();
					split_basicCosine.calcStats();
					split_bottomCosine.calcStats();

					split_contextOverlap.calcStats();
					split_partialContextOverlap.calcStats();
					split_noContextOverlap.calcStats();
					split_topOverlap.calcStats();
					split_basicOverlap.calcStats();
					split_bottomOverlap.calcStats();

					// adding to mean entropy distributions
					contextMeanEntropyFreq.add(round(split_contextEntropy1.mean));
					partialContextMeanEntropyFreq.add(round(split_partialContextEntropy1.mean));
					noContextMeanEntropyFreq.add(round(split_noContextEntropy1.mean));

					// adding to mean difference distributions
					contextEntropyMeanOfDiffFreq.add(round(split_contextDiff.mean));
					partialContextEntropyMeanOfDiffFreq.add(round(split_partialContextDiff.mean));
					noContextEntropyMeanOfDiffFreq.add(round(split_noContextDiff.mean));
					topEntropyMeanOfDiffFreq.add(round(split_topDiff.mean));
					basicEntropyMeanOfDiffFreq.add(round(split_basicDiff.mean));
					bottomEntropyMeanOfDiffFreq.add(round(split_bottomDiff.mean));

					// adding to difference of mean distributions
					contextEntropyDiffOfMeanFreq.add(round(split_contextEntropy1.mean - split_contextEntropy2.mean));
					partialContextEntropyDiffOfMeanFreq.add(round(split_partialContextEntropy1.mean - split_partialContextEntropy2.mean));
					noContextEntropyDiffOfMeanFreq.add(round(split_noContextEntropy1.mean - split_noContextEntropy2.mean));
					topEntropyDiffOfMeanFreq.add(round(split_contextEntropy1.mean - split_partialContextEntropy1.mean));
					basicEntropyDiffOfMeanFreq.add(round(split_contextEntropy1.mean - split_noContextEntropy1.mean));
					bottomEntropyDiffOfMeanFreq.add(round(split_partialContextEntropy1.mean - split_noContextEntropy1.mean));

					// adding to mean cosine distributions
					contextMeanCosineFreq.add(round(split_contextCosine.mean));
					partialContextMeanCosineFreq.add(round(split_partialContextCosine.mean));
					noContextMeanCosineFreq.add(round(split_noContextCosine.mean));
					// inter-environment mean cosine
					topMeanCosineFreq.add(round(split_topCosine.mean));
					basicMeanCosineFreq.add(round(split_basicCosine.mean));
					bottomMeanCosineFreq.add(round(split_bottomCosine.mean));

					// adding to mean overlap distributions
					contextMeanOverlapFreq.add(round(split_contextOverlap.mean));
					partialContextMeanOverlapFreq.add(round(split_partialContextOverlap.mean));
					noContextMeanOverlapFreq.add(round(split_noContextOverlap.mean));
					// inter-environment mean overlap
					topMeanOverlapFreq.add(round(split_topOverlap.mean));
					basicMeanOverlapFreq.add(round(split_basicOverlap.mean));
					bottomMeanOverlapFreq.add(round(split_bottomOverlap.mean));
				}

				// calculating stats for entropy
				contextEntropyFreq.calcStats();
				partialContextEntropyFreq.calcStats();
				noContextEntropyFreq.calcStats();

				// calculating stats for mean entropies
				contextMeanEntropyFreq.calcStats();
				partialContextMeanEntropyFreq.calcStats();
				noContextMeanEntropyFreq.calcStats();

				// calculating stats for differences in entropies
				contextEntropyDiffFreq.calcStats();
				partialContextEntropyDiffFreq.calcStats();
				noContextEntropyDiffFreq.calcStats();
				// inter-environment
				topEntropyDiffFreq.calcStats();
				basicEntropyDiffFreq.calcStats();
				bottomEntropyDiffFreq.calcStats();

				// calculating stats for differences of entropy means 
				contextEntropyDiffOfMeanFreq.calcStats();
				partialContextEntropyDiffOfMeanFreq.calcStats();
				noContextEntropyDiffOfMeanFreq.calcStats();
				// inter-environment
				topEntropyDiffOfMeanFreq.calcStats();
				basicEntropyDiffOfMeanFreq.calcStats();
				bottomEntropyDiffOfMeanFreq.calcStats();

				// calculating stats for mean of entropy differences
				contextEntropyMeanOfDiffFreq.calcStats();
				partialContextEntropyMeanOfDiffFreq.calcStats();
				noContextEntropyMeanOfDiffFreq.calcStats();
				// inter-environment
				topEntropyMeanOfDiffFreq.calcStats();
				basicEntropyMeanOfDiffFreq.calcStats();
				bottomEntropyMeanOfDiffFreq.calcStats();

				// calculating stats for Pearson correlations
				contextPearsonFreq.calcStats();
				partialContextPearsonFreq.calcStats();
				noContextPearsonFreq.calcStats();
				// inter-environment
				topPearsonFreq.calcStats();
				basicPearsonFreq.calcStats();
				bottomPearsonFreq.calcStats();

				// calculating stats for Spearman correlations
				contextSpearmanFreq.calcStats();
				partialContextSpearmanFreq.calcStats();
				noContextSpearmanFreq.calcStats();
				// inter-environment
				topSpearmanFreq.calcStats();
				basicSpearmanFreq.calcStats();
				bottomSpearmanFreq.calcStats();

				// calculating stats for cosine similarities
				contextCosineFreq.calcStats();
				partialContextCosineFreq.calcStats();
				noContextCosineFreq.calcStats();
				// inter-environment
				topCosineFreq.calcStats();
				basicCosineFreq.calcStats();
				bottomCosineFreq.calcStats();

				// calculating stats for mean cosine similarities
				contextMeanCosineFreq.calcStats();
				partialContextMeanCosineFreq.calcStats();
				noContextMeanCosineFreq.calcStats();
				// inter-environment
				topMeanCosineFreq.calcStats();
				basicMeanCosineFreq.calcStats();
				bottomMeanCosineFreq.calcStats();

				// calculating stats for overlap similarities
				contextOverlapFreq.calcStats();
				partialContextOverlapFreq.calcStats();
				noContextOverlapFreq.calcStats();
				// inter-environment
				topOverlapFreq.calcStats();
				basicOverlapFreq.calcStats();
				bottomOverlapFreq.calcStats();

				// calculating stats for mean overlap similarities
				contextMeanOverlapFreq.calcStats();
				partialContextMeanOverlapFreq.calcStats();
				noContextMeanOverlapFreq.calcStats();
				// inter-environment
				topMeanOverlapFreq.calcStats();
				basicMeanOverlapFreq.calcStats();
				bottomMeanOverlapFreq.calcStats();

				// printing entropy distributions
				entropyDistributions.println("context entropy," + contextEntropyFreq.mean + "," + contextEntropyFreq.sd);
				contextEntropyFreq.printCSV(entropyDistributions);

				entropyDistributions.println("partial-context entropy," + partialContextEntropyFreq.mean + "," + partialContextEntropyFreq.sd);
				partialContextEntropyFreq.printCSV(entropyDistributions);

				entropyDistributions.println("no-context entropy," + noContextEntropyFreq.mean + "," + noContextEntropyFreq.sd);
				noContextEntropyFreq.printCSV(entropyDistributions);

				// printing mean entropy distributions
				entropyDistributions.println("mean context entropy," + contextMeanEntropyFreq.mean + "," + contextMeanEntropyFreq.sd);
				contextMeanEntropyFreq.printCSV(entropyDistributions);

				entropyDistributions.println("mean partial-context entropy," + partialContextMeanEntropyFreq.mean + "," + partialContextMeanEntropyFreq.sd);
				partialContextMeanEntropyFreq.printCSV(entropyDistributions);

				entropyDistributions.println("mean no-context entropy," + noContextMeanEntropyFreq.mean + "," + noContextMeanEntropyFreq.sd);
				noContextMeanEntropyFreq.printCSV(entropyDistributions);

				// printing entropy differences distributions
				entropyDistributions.println("context entropy diff," + contextEntropyDiffFreq.mean + "," + contextEntropyDiffFreq.sd);
				contextEntropyDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("partial-context entropy diff," + partialContextEntropyDiffFreq.mean + "," + partialContextEntropyDiffFreq.sd);
				partialContextEntropyDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("no-context entropy diff," + noContextEntropyDiffFreq.mean + "," + noContextEntropyDiffFreq.sd);
				noContextEntropyDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("top entropy diff," + topEntropyDiffFreq.mean + "," +  topEntropyDiffFreq.sd);
				topEntropyDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("basic entropy diff," + basicEntropyDiffFreq.mean + "," + basicEntropyDiffFreq.sd);
				basicEntropyDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("bottom entropy diff," + bottomEntropyDiffFreq.mean + "," + bottomEntropyDiffFreq.sd);
				bottomEntropyDiffFreq.printCSV(entropyDistributions);

				// printing differences of mean entropy distributions
				entropyDistributions.println("context entropy means of diffs," + contextEntropyDiffOfMeanFreq.mean + "," + contextEntropyDiffOfMeanFreq.sd);
				contextEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

				entropyDistributions.println("partial-context entropy means of diffs," + partialContextEntropyDiffOfMeanFreq.mean + "," + partialContextEntropyDiffOfMeanFreq.sd);
				partialContextEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

				entropyDistributions.println("no-context entropy means of diffs," + noContextEntropyDiffOfMeanFreq.mean + "," + noContextEntropyDiffOfMeanFreq.sd);
				noContextEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

				entropyDistributions.println("top entropy means of diffs," + topEntropyDiffOfMeanFreq.mean + "," + topEntropyDiffOfMeanFreq.sd);
				topEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

				entropyDistributions.println("basic entropy means of diffs," + basicEntropyDiffOfMeanFreq.mean + "," + basicEntropyDiffOfMeanFreq.sd);
				basicEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

				entropyDistributions.println("bottom entropy means of diffs," + bottomEntropyDiffOfMeanFreq.mean + "," + bottomEntropyDiffOfMeanFreq.sd);
				bottomEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

				// printing mean of entropy differences distributions
				entropyDistributions.println("context entropy diff of means," + contextEntropyMeanOfDiffFreq.mean + "," + contextEntropyMeanOfDiffFreq.sd);
				contextEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("partial-context entropy diff of means," + partialContextEntropyMeanOfDiffFreq.mean + "," + partialContextEntropyMeanOfDiffFreq.sd);
				partialContextEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("no-context entropy diff of means," + noContextEntropyMeanOfDiffFreq.mean + "," + noContextEntropyMeanOfDiffFreq.sd);
				noContextEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("top entropy diff of means," + topEntropyMeanOfDiffFreq.mean + "," + topEntropyMeanOfDiffFreq.sd);
				topEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("basic entropy diff of means," + basicEntropyMeanOfDiffFreq.mean + "," + basicEntropyMeanOfDiffFreq.sd);
				basicEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

				entropyDistributions.println("bottom entropy diff of means," + bottomEntropyMeanOfDiffFreq.mean + "," + bottomEntropyMeanOfDiffFreq.sd);
				bottomEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

				// printing Pearson distributions
				entropyCorrelationDistributions.println("pearson context," + contextPearsonFreq.mean + "," + contextPearsonFreq.sd);
				contextPearsonFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("pearson partial-context," + partialContextPearsonFreq.mean + "," + partialContextPearsonFreq.sd);
				partialContextPearsonFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("pearson no-context," + noContextPearsonFreq.mean + "," + noContextPearsonFreq.sd);
				noContextPearsonFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("pearson top," + topPearsonFreq.mean + "," + topPearsonFreq.sd);
				topPearsonFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("pearson basic," + basicPearsonFreq.mean + "," + basicPearsonFreq.sd);
				basicPearsonFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("pearson bottom," + bottomPearsonFreq.mean + "," + bottomPearsonFreq.sd);
				bottomPearsonFreq.printCSV(entropyCorrelationDistributions);

				// printing Spearman distributions
				entropyCorrelationDistributions.println("Spearman context," + contextSpearmanFreq.mean + "," + contextSpearmanFreq.sd);
				contextSpearmanFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("Spearman partial-context," + partialContextSpearmanFreq.mean + "," + partialContextSpearmanFreq.sd);
				partialContextSpearmanFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("Spearman no-context," + noContextSpearmanFreq.mean + "," + noContextSpearmanFreq.sd);
				noContextSpearmanFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("Spearman top," + topSpearmanFreq.mean + "," + topSpearmanFreq.sd);
				topSpearmanFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("Spearman basic," + basicSpearmanFreq.mean + "," + basicSpearmanFreq.sd);
				basicSpearmanFreq.printCSV(entropyCorrelationDistributions);

				entropyCorrelationDistributions.println("Spearman bottom," + bottomSpearmanFreq.mean + "," + bottomSpearmanFreq.sd);
				bottomSpearmanFreq.printCSV(entropyCorrelationDistributions);

				// printing cosine distributions
				similarityDistributions.println("Cosine context," + contextCosineFreq.mean + "," + contextCosineFreq.sd);
				contextCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Cosine partial-context," + partialContextCosineFreq.mean + "," + partialContextCosineFreq.sd);
				partialContextCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Cosine no-context," + noContextCosineFreq.mean + "," + noContextCosineFreq.sd);
				noContextCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Cosine top," + topCosineFreq.mean + "," + topCosineFreq.sd);
				topCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Cosine basic," + basicCosineFreq.mean + "," + basicCosineFreq.sd);
				basicCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Cosine bottom," + bottomCosineFreq.mean + "," + bottomCosineFreq.sd);
				bottomCosineFreq.printCSV(similarityDistributions);

				// printing mean cosine distributions
				similarityDistributions.println("Mean Cosine context," + contextMeanCosineFreq.mean + "," + contextMeanCosineFreq.sd);
				contextMeanCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Cosine partial-context," + partialContextMeanCosineFreq.mean + "," + partialContextMeanCosineFreq.sd);
				partialContextMeanCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Cosine no-context," + noContextMeanCosineFreq.mean + "," + noContextMeanCosineFreq.sd);
				noContextMeanCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Cosine top," + topMeanCosineFreq.mean + "," + topMeanCosineFreq.sd);
				topMeanCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Cosine basic," + basicMeanCosineFreq.mean + "," + basicMeanCosineFreq.sd);
				basicMeanCosineFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Cosine bottom," + bottomMeanCosineFreq.mean + "," + bottomMeanCosineFreq.sd);
				bottomMeanCosineFreq.printCSV(similarityDistributions);

				// printing overlap distributions
				similarityDistributions.println("Overlap context," + contextOverlapFreq.mean + "," + contextOverlapFreq.sd);
				contextOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Overlap partial-context," + partialContextOverlapFreq.mean + "," + partialContextOverlapFreq.sd);
				partialContextOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Overlap no-context," + noContextOverlapFreq.mean + "," + noContextOverlapFreq.sd);
				noContextOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Overlap top," + topOverlapFreq.mean + "," + topOverlapFreq.sd);
				topOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Overlap basic," + basicOverlapFreq.mean + "," + basicOverlapFreq.sd);
				basicOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Overlap bottom," + bottomOverlapFreq.mean + "," + bottomOverlapFreq.sd);
				bottomOverlapFreq.printCSV(similarityDistributions);

				// printing mean overlap distributions
				similarityDistributions.println("Mean Overlap context," + contextMeanOverlapFreq.mean + "," + contextMeanOverlapFreq.sd);
				contextMeanOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Overlap partial-context," + partialContextMeanOverlapFreq.mean + "," + partialContextMeanOverlapFreq.sd);
				partialContextMeanOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Overlap no-context," + noContextMeanOverlapFreq.mean + "," + noContextMeanOverlapFreq.sd);
				noContextMeanOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Overlap top," + topMeanOverlapFreq.mean + "," + topMeanOverlapFreq.sd);
				topMeanOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Overlap basic," + basicMeanOverlapFreq.mean + "," + basicMeanOverlapFreq.sd);
				basicMeanOverlapFreq.printCSV(similarityDistributions);

				similarityDistributions.println("Mean Overlap bottom," + bottomMeanOverlapFreq.mean + "," + bottomMeanOverlapFreq.sd);
				bottomMeanOverlapFreq.printCSV(similarityDistributions);
			}
			entropyDistributions.close();
			entropyCorrelationDistributions.close();
			similarityDistributions.close();


		}catch(IOException e){
			System.err.println(e.getLocalizedMessage());
		}
	}

	/**
	 * Stores all the answers and HITs a worker has annotated
	 */
	private class Worker
	{
		public HashMap<String, ArrayList<String>> idToAnswer = new HashMap<String, ArrayList<String>>();

		public String workerId;
		public int numAnnotated = 0;

		public Worker(String id){
			workerId = id;
		}

		/** Adds an answer to this workers list of answers
		 * 
		 * @param HIT Type Id
		 * @param answer to be added
		 */
		public void addAnswer(String hitTypeID, String text){
			if (idToAnswer.containsKey(hitTypeID))
			{
				idToAnswer.get(hitTypeID).add(text);
			} else {
				idToAnswer.put(hitTypeID, new ArrayList<String>());
			}
			numAnnotated++;
		}

		/** 
		 * Returns a string consisting of the workers Id and a list of his answers
		 */
		public String toString()
		{
			String out = workerId;

			for (String typeId : idToAnswer.keySet())
			{
				out += "\n" + typeId;
				for ( String answer : idToAnswer.get(typeId) )
				{
					out += "\t" + answer;
				}

			}

			return out;
		}
	}

	/** 
	 * General class that creates frequency distributions
	 */
	private class FrequencyCounter
	{
		public HashMap<Double, Double> frequency = new HashMap<Double, Double>();
		public double samples = 0;
		public double mean = 0;
		public double sd = 0;

		public void add(Double key)
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
			for (Double key : frequency.keySet() )
			{
				frequency.put(key, frequency.get(key)/samples);
				mean += key * frequency.get(key);
			}
			// calculating standard deviations
			for (Double key : frequency.keySet() )
			{
				sd += frequency.get(key) * Math.pow(key - mean, 2);
			}
			sd = Math.sqrt(sd);
		}

		public void printCSV(PrintWriter out)
		{
			for ( Double key : frequency.keySet() )
			{
				out.printf(key + ", %.8f", frequency.get(key) );
				out.println();
			}
		}
	}
}
