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
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

//import simplify.LexicalSubSurvey.Worker;

/**
 * A class to create lexical simplification HITs
 * Patrick Adelstein, Teddy Pendergast, David Kauchack, Middlebury College Summer 2012
 */
public class LexicalSubSurvey{

	// pointers to the data
	private static String DATADIR = "/home/padelstein/LexicalSimplify/data/Parsed.aligned";
	private static String PARSED = DATADIR + "/sentences.parsed";
	private static String ALIGN = DATADIR + "/normal-simple.berkeley.align";

	private RequesterService service;

	// Define the properties of the HIT to be created.
	private String contextGivenTitle = "Suggest a Simpler Word in the Sentence";
	private String noTargetGivenTitle = "Fill in the Blank with a Simpler Word";
	private String noContextGivenTitle = "Suggest a Simpler Word";
	private String contextGivenDescription = 
		"Replace a word with a simple substitute in the given sentence.";
	private String noTargetGivenDescription = 
		"Suggest a simple word in the given sentence.";
	private String noContextGivenDescription = 
		"Replace a word with a simple substitute.";
	private int numAssignments = 10;
	private double reward = 00.02;
	
	// define the writers for storage of HIT IDs
	private PrintWriter noContextpr;
	private PrintWriter contextpr;
	private PrintWriter noTargetpr;
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
	private ArrayList<OurHIT> contextHITs = new ArrayList<OurHIT>();
	private ArrayList<OurHIT> noContextHITs = new ArrayList<OurHIT>();
	
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
			noTargetpr.println(hit.getHITId());
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
			OurHIT currentHIT = new OurHIT(hitId, wordToSense);
//			hitIdtoType.put(hitId, HITtype);
			
//			if ( typeId1 == null ){
//				typeId1 = HITtype;
//			} else if ( typeId2 == null ){
//				typeId2 = HITtype;
//			} else if ( typeId3 == null ){
//				typeId3 = HITtype;
//			}
				
			Assignment[] answers = service.getAllAssignmentsForHIT(hitId);
			
			ArrayList<String>  compareList = null;
			// Print out the HITId and the URL to view the HIT.
			System.out.println("Retrieved HIT: " + hitId + " " + currentHIT.getHITtype() );
			
//			System.out.println( "# of hits available: " + answers.length );
			PrintWriter answerOutput = null;
			
			//uses hittypeId to check what kind of hit we are looking at and sets the outputstream accordingly.
			if ( currentHIT.getHITtype().equals("25D2JE1M7PKKF8JGAZQAK04LZYTXQE") ){
				answerOutput = noContextAnswerOutput;
				noContextHITs.add(currentHIT);
				compareList = noContextAnswers;
			} else if ( currentHIT.getHITtype().equals("20ASTLB3L0FBPWA8FU5JZEVE5SUJV7") ){
				answerOutput = contextAnswerOutput;
				contextHITs.add(currentHIT);
				compareList = contextAnswers;
				HITindex++;
			} else {
				return;
			}
						
//			answerOutput.println("<br /><br />");
//			answerOutput.println(question);
			
			//WordToSense maps the focus word to the context, simple word, 
//			int wordStart = question.substring(question.indexOf("<b", question.indexOf("Task:") + 35)).indexOf(">") + question.indexOf("<b", question.indexOf("Task:") + 35) + 1;
//			int wordEnd = question.indexOf("</b>", question.indexOf("Task:") + 17 );
//			String word = ""; 
//			if (wordStart < 0 || wordEnd <0){
//				for (String key: wordToSense.keySet()){
//					String firstHalf = wordToSense.get(key)[0];
//					if (question.indexOf(firstHalf) > 0){
//						word = key;
//					}
//				}
//			} else
//				word = question.substring(wordStart, wordEnd);
//			
//			word = word.trim();
//			
//			System.out.println("word: " + word);
			
			Map<String, Integer> frequencyCounter = new HashMap<String, Integer>();
			//frequencyCounter keeps track of each words frequency over all the assignments.
//			answerOutput.println( currentHIT.getQuestion() + hitId);
//			System.out.println("word: " + word);
//			answerOutput.print(hitId + "\t" + HITtype + "\t" + "word: " + word + "\t");

			for (Assignment answer: currentHIT.getHITassignments() ){
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
					currentWorker.addAnswer(answerText, currentHIT.getHITtype(), HITindex);
				} else {
					currentWorker = new Worker(workerID);
					workers.add(currentWorker);
					currentWorker.addAnswer(answerText, currentHIT.getHITtype(), HITindex);
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
			for (String text: currentHIT.getFrequencyCounter().keySet()){
				int freq = currentHIT.getFrequencyCounter().get(text);
				if (freq == currentHIT.highestFreq){
					compareList.add(text.trim());
				}
			}

		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}
	
	public void getHITs() throws IOException
	{
		
		
		try 
		{

			HIT[] HITs = service.getAllReviewableHITs(null);
			
			int i = 1;
			
			for ( HIT current: HITs){
				System.out.println( i + " " + current.getHITId() );
				i++;
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
		q += "      <br/><form name='mturk_form' method='post' id='mturk_form' action='https://www.mturk.com/mturk/externalSubmit' style=\"padding-top:10px\">";
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
		q += "      <br/><form name='mturk_form' method='post' id='mturk_form' action='https://www.mturk.com/mturk/externalSubmit' style=\"padding-top:10px\">";
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
		
		if (args.length >=2){
			// Create an instance of this class.
			LexicalSubSurvey app = new LexicalSubSurvey();
			
			for (int j = 1; j<args.length; j++){
				try {
					File inputFile = new File(args[j]);
					

					if (args[0].equals("-add")){

						String[] parts = {"NN", "NNS", "JJ", "JJR", "JJS", "RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
						ArrayList<String> pos = new ArrayList<String>();
						for (int i=0; i<parts.length; i++){
							pos.add(parts[i]);
						}
						
						
						
						ExamplePairReader reader = new ExamplePairReader(PARSED, ALIGN);
						BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))); // typical file name: "sub.simple.first100"
						app.noContextpr = new PrintWriter(new FileOutputStream(new File("NoContextGivenIDs")));
						app.noTargetpr = new PrintWriter(new FileOutputStream(new File("NoTargetGivenIDs")));
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
							
							ArrayList<String> simpleWordsList = new ArrayList<String>();
								for (int i=0; i<SimpleWordList.simpleList.length; i++){
									simpleWordsList.add(SimpleWordList.simpleList[i]);
								}
							
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
									String wordAfterFocus = normalWords.get(n+1).getWord();
									String target = normal.getWord();
									if ( !( wordAfterFocus.length() == 1 && wordAfterFocus.compareTo("A") < 0 ) ){
										target += " ";
									}
									String secondPart = "";
									sense = wordToSense.get(normal.getWord())[1];
									String POS = codeToPOS.get(normal.getPos());
									
									for ( int i = 0; i < normalWords.size(); i++ ){
										String currentWord = normalWords.get(i).getWord();
										String nextWord = "";
										if ( i+1 < normalWords.size() ){
											nextWord = normalWords.get(i+1).getWord();
										}
										if ( i < n ){
											firstPart += currentWord;
											if ( !( nextWord.length() == 1 && nextWord.compareTo("A") < 0 )){
												firstPart += " ";
											}
										}
										if ( i > n ){
											secondPart += currentWord;
											if ( !( nextWord.length() == 1 && nextWord.compareTo("A") < 0 )){
												secondPart += " ";
											}
										}
									}
									
									app.createContextGivenSurvey(firstPart, target, secondPart);
									app.createNoTargetGivenSurvey(firstPart, target, secondPart);
									app.createNoContextGivenSurvey(firstPart, target, secondPart, sense, POS);
								}
							}
							

//							input = in.readLine();
						}
						app.contextpr.close();
						app.noTargetpr.close();
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
						// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
						BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
						BufferedReader in = null;
						if (args[args.length-2].equals("-i")) // this tag denotes the input file which contains the Hit data in the form context \t wordIndex \t normalWord \t simpleWord \t sense
								in = new BufferedReader(new InputStreamReader(new FileInputStream(args[args.length-1])));
						String hitId = "";
						
						app.noContextAnswerOutput = new PrintWriter(new FileOutputStream(new File("noContextAnswerOutput.cleaned")));
						app.contextAnswerOutput = new PrintWriter(new FileOutputStream(new File("contextAnswerOutput.cleaned")));
						app.workerOutput = new PrintWriter(new FileOutputStream(new File("workerOutput")));
						app.substitutionOutput = new PrintWriter(new FileOutputStream(new File("allSubstitionsOnly")));
						
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
						
						
						for (hitId = fileReader.readLine(); hitId !=null; hitId = fileReader.readLine()){
							System.out.println(hitId);
							app.getAssignmentsHIT(hitId, wordToSense);
						}
						
						//prints out each workers Id and the answers to the questions
						for (Worker i: app.workers){
							app.workerOutput.println(i.workerId + "\t");
							app.workerOutput.println(i.question1Answers.toString());
							app.workerOutput.println(i.question2Answers.toString());
							app.workerOutput.println(i.question3Answers.toString());
						}
						
						//Checks the for the percentage of words that match up between the context given and no context given answers
						int matching = 0;
						for( int i = 0; i< app.noContextAnswers.size(); i ++){
							if (app.contextAnswers.get(i).equals(app.noContextAnswers.get(i)))
								matching++;
							if (i > 0)
								if(app.contextAnswers.get(i).equals(app.noContextAnswers.get(i- 1)))
									matching++;
							if (i < app.noContextAnswers.size() - 1)
								if(app.contextAnswers.get(i).equals(app.noContextAnswers.get(i + 1)))
									matching++;
						}
						
//						for (some HITs) {
//						boolean topAnswers = false;
//						for (String text: currentHIT.getFrequencyCounter().keySet()){
//							int freq = currentHIT.getFrequencyCounter().get(text);
//							if (freq >=3){
//								if(!topAnswers)
//									answerOutput.print("Top submissions were: ");
//								answerOutput.print(text + ": " + freq + " ");
//								topAnswers = true;
//							} else
//								wordList += text + ": " + freq + " ";
//						}
//						if (!topAnswers){
//							answerOutput.print("No majority submission, all submissions are: " + wordList);
//						}
//					
						
//						System.out.print("Percentage hits with same top submission: " +  matching*100.0 / app.contextAnswers.size());
						
						//closing all printWriters
						app.workerOutput.close();
						app.noContextAnswerOutput.close();
						app.contextAnswerOutput.close();
						app.substitutionOutput.close();
						
						//Writes out the data so that it can be imported into excel using csv format.
						
//						PrintWriter statData = new PrintWriter(new FileOutputStream(new File("statData.csv")));
//						
//						for (String hitIdKey: app.hitTopSubCount.keySet()){
//							statData.println(hitIdKey +"," + app.hitTopSubCount.get(hitIdKey));
//						}
//						statData.close();
						
						break;
//						app.getHITs();
					}else if (args[0].equals("-getEntropyData")){
						System.out.println("gathering data...");
						// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
						BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
						app.noContextAnswerOutput = new PrintWriter(new FileOutputStream(new File("noContextEntropy.data")));
						app.contextAnswerOutput = new PrintWriter(new FileOutputStream(new File("contextEntropy.data")));
						
						if (fileReader != null){
							String input = fileReader.readLine();
							while (input != null){
								String[] words = input.split("\t");
								ArrayList<String> answers = new ArrayList<String>(10);
								String typeID = "";
								for ( int i = 3; i < 13; i++)
								{
									answers.add(words[i].trim() );
								}

								typeID = words[1];

								OurHIT currentHIT = new OurHIT(words[0], typeID, words[2], answers);

								System.out.println(currentHIT.targetWord);
//								System.out.println(app.contextHITs.toString());

								if ( typeID.equals("20ASTLB3L0FBPWA8FU5JZEVE5SUJV7") )
								{
									app.contextHITs.add(currentHIT);
								} else if ( typeID.equals("25D2JE1M7PKKF8JGAZQAK04LZYTXQE") )
								{
									app.noContextHITs.add(currentHIT);
								}

								input = fileReader.readLine();
							}
						}
						
						
						
						app.noContextAnswerOutput.close();
						app.contextAnswerOutput.close();

						break;
					}else {
						System.err.println("No valid options were provided");
						System.out.println(usageError);
						break;
					}
					
				} catch (IOException e){
					System.err.println("Could not find the file: \"" + args[j] + "\"");
					System.err.println("Please provide a valid file name");
				}

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

}