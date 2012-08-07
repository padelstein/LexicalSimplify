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
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;

public class DataCollection {

	private RequesterService service;

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

	// constructor
	public DataCollection()
	{
		service = new RequesterService(new PropertiesClientConfig());
	}

	// compiles worker data for single HIT
	public void fillWorkerArray(OurHIT currentHIT)
	{
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
				currentWorker.addAnswer(currentHIT.typeID, answerText);
			} else {
				currentWorker = new Worker(workerID);
				workers.add(currentWorker);
				currentWorker.addAnswer(currentHIT.typeID, answerText);
			}

		}
	}

	// retrieves HITs from amazon and processes them
	public void getHITs(Map<String, String[]> wordToSense, File contextIDfile, File partialContextIDfile, File noContextIDfile, int numAnnotations) throws IOException
	{
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
				// instantiates current HITs
				OurHIT currentContextHIT = new OurHIT(contextID, wordToSense, numAnnotations);
				OurHIT currentPartialContextHIT = new OurHIT(partialContextID, wordToSense, numAnnotations);
				OurHIT currentNoContextHIT = new OurHIT(noContextID, wordToSense, numAnnotations);

				//*********************************
				ArrayList<String>  contextCompareList = currentContextHIT.answers;
				ArrayList<String>  partialContextCompareList = currentPartialContextHIT.answers;
				ArrayList<String>  noContextCompareList = currentNoContextHIT.answers;
				//*********************************

				System.out.println("Retrieved HITs: " + currentContextHIT.ID + ", " + currentPartialContextHIT.ID + ", " + currentNoContextHIT.ID );

				// fills HIT arrays with current HITs
				contextHITs.add(currentContextHIT);
				partialContextHITs.add(currentPartialContextHIT);
				noContextHITs.add(currentNoContextHIT);

				// compiles worker data for current HITs
				fillWorkerArray(currentContextHIT);
				fillWorkerArray(currentPartialContextHIT);
				fillWorkerArray(currentNoContextHIT);

				for (String text: currentContextHIT.frequencyCounter.keySet())
				{
					int freq = currentContextHIT.frequencyCounter.get(text);
					if (freq == currentContextHIT.highestFreq)
					{
						contextCompareList.add(text.trim());
					}
				}

				for (String text: currentPartialContextHIT.frequencyCounter.keySet())
				{
					int freq = currentPartialContextHIT.frequencyCounter.get(text);
					if (freq == currentPartialContextHIT.highestFreq)
					{
						partialContextCompareList.add(text.trim());
					}
				}

				for (String text: currentNoContextHIT.frequencyCounter.keySet())
				{
					int freq = currentNoContextHIT.frequencyCounter.get(text);
					if (freq == currentNoContextHIT.highestFreq)
					{
						noContextCompareList.add(text.trim());
					}
				}

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

	// fills the HIT arrays if we have a file of cleaned annotations
	public void fillHitList(File input, int numAssignments) throws IOException{
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(input)));
		if (fileReader != null){
			String inputLine = fileReader.readLine();
			while (inputLine != null){
				String[] words = inputLine.split("\t");
				ArrayList<String> answers = new ArrayList<String>(10);
				String typeID = "";
				for ( int i = 3; i < 3 + numAssignments; i++)
				{
					if ( i < words.length )
					{
						answers.add(words[i].trim());
					}
				}

				typeID = words[1];

				OurHIT currentHIT = new OurHIT(words[0], typeID, words[2], answers, 50);

				if ( typeID.equals("") )
				{
					contextHITs.add(currentHIT);
				} else if ( typeID.equals("") )
				{
					partialContextHITs.add(currentHIT);
				} else if ( typeID.equals("") )
				{
					noContextHITs.add(currentHIT);
				}

				inputLine = fileReader.readLine();
			}
		}
	}

	// randomly splits each target word's annotations in half
	public void splitHITs()
	{
		contextHITs1.clear();
		contextHITs2.clear();
		partialContextHITs1.clear();
		partialContextHITs2.clear();
		noContextHITs1.clear();
		noContextHITs2.clear();

		Random hitSplitter = new Random();

		int numHITs = contextHITs.size();

		for (int n = 0 ; n < numHITs ; n++)
		{
			OurHIT currentContextHIT = contextHITs.get(n);
			OurHIT currentPartialContextHIT = partialContextHITs.get(n);
			OurHIT currentNoContextHIT = noContextHITs.get(n);

			// context
			ArrayList<String> currentContextAnswers = new ArrayList<String>(currentContextHIT.answers);
			ArrayList<String> currentContextAnswers1 = new ArrayList<String>();
			ArrayList<String> currentContextAnswers2 = new ArrayList<String>();

			// partial-context
			ArrayList<String> currentPartialContextAnswers = new ArrayList<String>(currentPartialContextHIT.answers);
			ArrayList<String> currentPartialContextAnswers1 = new ArrayList<String>();
			ArrayList<String> currentPartialContextAnswers2 = new ArrayList<String>();

			// no-context
			ArrayList<String> currentNoContextAnswers = new ArrayList<String>(currentNoContextHIT.answers);
			ArrayList<String> currentNoContextAnswers1 = new ArrayList<String>();
			ArrayList<String> currentNoContextAnswers2 = new ArrayList<String>();



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

			// quick and dirty OurHIT constructor for speed
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

	// takes context ID, partial-context ID, and no-context ID files in that order
	public static void main(String[] args) throws IOException {

		DataCollection app = new DataCollection();

		try {
			int samples = 10;

			File contextIDfile = new File(args[0]);
			File partialContextIDfile = new File(args[1]);
			File noContextIDfile = new File(args[2]);
			File senseFile = new File(args[3]);

			PrintWriter entropyDistributions = new PrintWriter(new FileOutputStream(new File("NewExperimentData/entropy.distributions.data.csv")));
			PrintWriter entropyCorrelationDistributions = new PrintWriter(new FileOutputStream(new File("NewExperimentData/entropy_correlation.distributions.data.csv")));
			PrintWriter similarityDistributions = new PrintWriter(new FileOutputStream(new File("NewExperimentData/similarity.distributions.data.csv")));
			PrintWriter standardData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/standard.data.csv")));
			PrintWriter contextData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/context.data.csv")));
			PrintWriter partialContextData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/partialContext.data.csv")));
			PrintWriter noContextData = new PrintWriter(new FileOutputStream(new File("NewExperimentData/noContext.data.csv")));

			// create 25 annotations HITs to get word by word data
			app.getHITs(new HashMap<String, String[]>(), contextIDfile, partialContextIDfile, noContextIDfile, 25);

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

			app.getHITs(app.getWordToSense(senseFile), contextIDfile, partialContextIDfile, noContextIDfile, 50);

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

			for (int i = 0 ; i < samples ; i++)
			{
				// creates a random split of every HIT's annotations
				// these are stored as OurHITs in the following ArrayLists
				// contextHITs1, contextHITs2,
				// partialContextHITs1, partialContextHITs2,
				// noContextHITs1, and noContextHITs2
				app.splitHITs();
				System.out.println("split the HITs");

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

				for (int j = 0 ; j < numHITs ; j++)
				{	
					// getting randomly split context HITs, retrieving entropy
					OurHIT contextHIT1 = app.contextHITs1.get(j);
					OurHIT contextHIT2 = app.contextHITs2.get(j);
					double contextEntropy1 = contextHIT1.entropy;
					double contextEntropy2 = contextHIT2.entropy;
					double contextEntropyDiff = contextEntropy1 - contextEntropy2;

					// getting randomly split partial-context HITs, retrieving entropy
					OurHIT partialContextHIT1 = app.partialContextHITs1.get(j);
					OurHIT partialContextHIT2 = app.partialContextHITs2.get(j);
					double partialContextEntropy1 = partialContextHIT1.entropy;
					double partialContextEntropy2 = partialContextHIT1.entropy;
					double partialContextEntropyDiff = partialContextEntropy1 - partialContextEntropy2;

					// getting randomly split no-context HITs, retrieving entropy
					OurHIT noContextHIT1 = app.noContextHITs1.get(j);
					OurHIT noContextHIT2 = app.noContextHITs2.get(j);
					double noContextEntropy1 = noContextHIT1.entropy;
					double noContextEntropy2 = noContextHIT1.entropy;
					double noContextEntropyDiff = noContextEntropy1 - noContextEntropy2;

					// inter-environment entropy differences
					double topEntropyDiff = contextEntropy1 - partialContextEntropy1;
					double basicEntropyDiff = contextEntropy1 - noContextEntropy1;
					double bottomEntropyDiff = partialContextEntropy1 - noContextEntropy1;

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
					double contextCosine = app.getCosine(contextHIT1, contextHIT2);
					double partialContextCosine = app.getCosine(partialContextHIT1, partialContextHIT2);
					double noContextCosine = app.getCosine(noContextHIT1, noContextHIT2);
					double topCosine = app.getCosine(contextHIT1, partialContextHIT1);
					double basicCosine = app.getCosine(contextHIT1, noContextHIT1);
					double bottomCosine = app.getCosine(partialContextHIT1, noContextHIT1);

					// adding to cosine distributions
					contextCosineFreq.add(contextCosine);
					partialContextCosineFreq.add(partialContextCosine);
					noContextCosineFreq.add(noContextCosine);
					topCosineFreq.add(topCosine);
					basicCosineFreq.add(basicCosine);
					bottomCosineFreq.add(bottomCosine);

					// get overlap coefficients
					double contextOverlap = app.getOverlap(contextHIT1, contextHIT2);
					double partialContextOverlap = app.getOverlap(partialContextHIT1, partialContextHIT2);
					double noContextOverlap = app.getOverlap(noContextHIT1, noContextHIT2);
					double topOverlap = app.getOverlap(contextHIT1, partialContextHIT1);
					double basicOverlap = app.getOverlap(contextHIT1, noContextHIT1);
					double bottomOverlap = app.getOverlap(partialContextHIT1, noContextHIT1);

					// adding to overlap distributions
					contextOverlapFreq.add(contextOverlap);
					partialContextOverlapFreq.add(partialContextOverlap);
					noContextOverlapFreq.add(noContextOverlap);
					topOverlapFreq.add(topOverlap);
					basicOverlapFreq.add(basicOverlap);
					bottomOverlapFreq.add(bottomOverlap);

					// adding similarities for determining mean of this random split
					split_contextCosine.add(contextCosine);
					split_partialContextCosine.add(partialContextCosine);
					split_noContextCosine.add(noContextCosine);
					split_topCosine.add(topCosine);
					split_basicCosine.add(basicCosine);
					split_bottomCosine.add(bottomCosine);

					split_contextOverlap.add(contextOverlap);
					split_partialContextOverlap.add(partialContextOverlap);
					split_noContextOverlap.add(noContextOverlap);
					split_topOverlap.add(topOverlap);
					split_basicOverlap.add(basicOverlap);
					split_bottomOverlap.add(bottomOverlap);

				}

				// adding to Pearson distributions
				contextPearsonFreq.add(app.getPearsonCoeff(app.contextHITs1, app.contextHITs2));
				partialContextPearsonFreq.add(app.getPearsonCoeff(app.partialContextHITs1, app.partialContextHITs2));
				noContextPearsonFreq.add(app.getPearsonCoeff(app.noContextHITs1, app.noContextHITs2));
				topPearsonFreq.add(app.getPearsonCoeff(app.contextHITs1, app.partialContextHITs1));
				basicPearsonFreq.add(app.getPearsonCoeff(app.contextHITs1, app.noContextHITs1));
				bottomPearsonFreq.add(app.getPearsonCoeff(app.partialContextHITs1, app.noContextHITs1));

				// adding to Spearman distributions
				contextSpearmanFreq.add(app.getSpearmanCoeff(app.contextHITs1, app.contextHITs2));
				partialContextSpearmanFreq.add(app.getSpearmanCoeff(app.partialContextHITs1, app.partialContextHITs2));
				noContextSpearmanFreq.add(app.getSpearmanCoeff(app.noContextHITs1, app.noContextHITs2));
				topSpearmanFreq.add(app.getSpearmanCoeff(app.contextHITs1, app.partialContextHITs1));
				basicSpearmanFreq.add(app.getSpearmanCoeff(app.contextHITs1, app.noContextHITs1));
				bottomSpearmanFreq.add(app.getSpearmanCoeff(app.partialContextHITs1, app.noContextHITs1));

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
				contextMeanEntropyFreq.add(split_contextEntropy1.mean);
				partialContextMeanEntropyFreq.add(split_partialContextEntropy1.mean);
				noContextMeanEntropyFreq.add(split_noContextEntropy1.mean);

				// adding to mean difference distributions
				contextEntropyMeanOfDiffFreq.add(split_contextDiff.mean);
				partialContextEntropyMeanOfDiffFreq.add(split_partialContextDiff.mean);
				noContextEntropyMeanOfDiffFreq.add(split_noContextDiff.mean);
				topEntropyMeanOfDiffFreq.add(split_topDiff.mean);
				basicEntropyMeanOfDiffFreq.add(split_basicDiff.mean);
				bottomEntropyMeanOfDiffFreq.add(split_bottomDiff.mean);

				// adding to difference of mean distributions
				contextEntropyDiffOfMeanFreq.add(split_contextEntropy1.mean - split_contextEntropy2.mean);
				partialContextEntropyDiffOfMeanFreq.add(split_partialContextEntropy1.mean - split_partialContextEntropy2.mean);
				noContextEntropyDiffOfMeanFreq.add(split_noContextEntropy1.mean - split_noContextEntropy2.mean);
				topEntropyDiffOfMeanFreq.add(split_contextEntropy1.mean - split_partialContextEntropy1.mean);
				basicEntropyDiffOfMeanFreq.add(split_contextEntropy1.mean - split_noContextEntropy1.mean);
				bottomEntropyDiffOfMeanFreq.add(split_partialContextEntropy1.mean - split_noContextEntropy1.mean);

				// adding to mean cosine distributions
				contextMeanCosineFreq.add(split_contextCosine.mean);
				partialContextMeanCosineFreq.add(split_partialContextCosine.mean);
				noContextMeanCosineFreq.add(split_noContextCosine.mean);
				// inter-environment mean cosine
				topMeanCosineFreq.add(split_topCosine.mean);
				basicMeanCosineFreq.add(split_basicCosine.mean);
				bottomMeanCosineFreq.add(split_bottomCosine.mean);

				// adding to mean overlap distributions
				contextMeanOverlapFreq.add(split_contextOverlap.mean);
				partialContextMeanOverlapFreq.add(split_partialContextOverlap.mean);
				noContextMeanOverlapFreq.add(split_noContextOverlap.mean);
				// inter-environment mean overlap
				topMeanOverlapFreq.add(split_topOverlap.mean);
				basicMeanOverlapFreq.add(split_basicOverlap.mean);
				bottomMeanOverlapFreq.add(split_bottomOverlap.mean);
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
			entropyDistributions.println("context entropy");
			contextEntropyFreq.printCSV(entropyDistributions);

			entropyDistributions.println("partial-context entropy");
			partialContextEntropyFreq.printCSV(entropyDistributions);

			entropyDistributions.println("no-context entropy");
			noContextEntropyFreq.printCSV(entropyDistributions);

			// printing mean entropy distributions
			entropyDistributions.println("mean context entropy");
			contextMeanEntropyFreq.printCSV(entropyDistributions);

			entropyDistributions.println("mean partial-context entropy");
			partialContextMeanEntropyFreq.printCSV(entropyDistributions);

			entropyDistributions.println("mean no-context entropy");
			noContextMeanEntropyFreq.printCSV(entropyDistributions);

			// printing entropy differences distributions
			entropyDistributions.println("context entropy diff");
			contextEntropyDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("partial-context entropy diff");
			partialContextEntropyDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("no-context entropy diff");
			noContextEntropyDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("top entropy diff");
			topEntropyDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("basic entropy diff");
			basicEntropyDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("bottom entropy diff");
			bottomEntropyDiffFreq.printCSV(entropyDistributions);

			// printing differences of mean entropy distributions
			entropyDistributions.println("context entropy means of diffs");
			contextEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

			entropyDistributions.println("partial-context entropy means of diffs");
			partialContextEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

			entropyDistributions.println("no-context entropy means of diffs");
			noContextEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

			entropyDistributions.println("top entropy means of diffs");
			topEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

			entropyDistributions.println("basic entropy means of diffs");
			basicEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

			entropyDistributions.println("bottom entropy means of diffs");
			bottomEntropyDiffOfMeanFreq.printCSV(entropyDistributions);

			// printing mean of entropy differences distributions
			entropyDistributions.println("context entropy diff of means");
			contextEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("partial-context entropy diff of means");
			partialContextEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("no-context entropy diff of means");
			noContextEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("top entropy diff of means");
			topEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("basic entropy diff of means");
			basicEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

			entropyDistributions.println("bottom entropy diff of means");
			bottomEntropyMeanOfDiffFreq.printCSV(entropyDistributions);

			// printing Pearson distributions
			entropyCorrelationDistributions.println("pearson context");
			contextPearsonFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("pearson partial-context");
			partialContextPearsonFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("pearson no-context");
			noContextPearsonFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("pearson top");
			topPearsonFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("pearson basic");
			basicPearsonFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("pearson bottom");
			bottomPearsonFreq.printCSV(entropyCorrelationDistributions);

			// printing Spearman distributions
			entropyCorrelationDistributions.println("Spearman context");
			contextSpearmanFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("Spearman partial-context");
			partialContextSpearmanFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("Spearman no-context");
			noContextSpearmanFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("Spearman top");
			topSpearmanFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("Spearman basic");
			basicSpearmanFreq.printCSV(entropyCorrelationDistributions);

			entropyCorrelationDistributions.println("Spearman bottom");
			bottomSpearmanFreq.printCSV(entropyCorrelationDistributions);

			// printing cosine distributions
			similarityDistributions.println("Cosine context");
			contextCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Cosine partial-context");
			partialContextCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Cosine no-context");
			noContextCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Cosine top");
			topCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Cosine basic");
			basicCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Cosine bottom");
			bottomCosineFreq.printCSV(similarityDistributions);

			// printing mean cosine distributions
			similarityDistributions.println("Mean Cosine context");
			contextMeanCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Cosine partial-context");
			partialContextMeanCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Cosine no-context");
			noContextMeanCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Cosine top");
			topMeanCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Cosine basic");
			basicMeanCosineFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Cosine bottom");
			bottomMeanCosineFreq.printCSV(similarityDistributions);

			// printing overlap distributions
			similarityDistributions.println("Overlap context");
			contextOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Overlap partial-context");
			partialContextOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Overlap no-context");
			noContextOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Overlap top");
			topOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Overlap basic");
			basicOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Overlap bottom");
			bottomOverlapFreq.printCSV(similarityDistributions);

			// printing mean overlap distributions
			similarityDistributions.println("Mean Overlap context");
			contextMeanOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Overlap partial-context");
			partialContextMeanOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Overlap no-context");
			noContextMeanOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Overlap top");
			topMeanOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Overlap basic");
			basicMeanOverlapFreq.printCSV(similarityDistributions);

			similarityDistributions.println("Mean Overlap bottom");
			bottomMeanOverlapFreq.printCSV(similarityDistributions);

			entropyDistributions.close();
			entropyCorrelationDistributions.close();
			similarityDistributions.close();


		}catch(IOException e){
			System.err.println(e.getLocalizedMessage());
		}
	}

	// stores all the answers and HITs a worker has annotated
	private class Worker
	{
		public HashMap<String, ArrayList<String>> idToAnswer = new HashMap<String, ArrayList<String>>();

		public String workerId;
		public int numAnnotated = 0;

		public Worker(String id){
			workerId = id;
		}

		public void addAnswer(String hitTypeID, String text){
			if (idToAnswer.containsKey(hitTypeID))
			{
				idToAnswer.get(hitTypeID).add(text);
			} else {
				idToAnswer.put(hitTypeID, new ArrayList<String>());
			}
			numAnnotated++;
		}
	}

	// general class that creates frequency distributions
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
