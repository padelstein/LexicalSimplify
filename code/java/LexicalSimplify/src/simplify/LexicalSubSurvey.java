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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
	private int numAssignments = 50;
	private double reward = 00.02;

	// define the writers for storage of HIT IDs
	private PrintWriter noContextpr;
	private PrintWriter contextpr;
	private PrintWriter partialContextpr;

	private QualificationRequirement acceptanceRate = new QualificationRequirement("000000000000000000L0", Comparator.GreaterThanOrEqualTo, 95, null, false);
	private QualificationRequirement location = new QualificationRequirement("00000000000000000071", Comparator.EqualTo, null, new Locale("US"), false);
	private QualificationRequirement[] requirements = {acceptanceRate, location};

	ArrayList<String> noContextAnswers = new ArrayList<String>();
	ArrayList<String> contextAnswers= new ArrayList<String>();
	ArrayList<String> noTargetAnswers= new ArrayList<String>() ;;

	Map<String, String> hitIdtoType = new HashMap<String, String>();

	/**
	 * Constructor: creates a service to make calls to the Amazon database
	 */
	public LexicalSubSurvey()
	{
		service = new RequesterService(new PropertiesClientConfig());
	}

	/** A method to create a HIT with context and the target word provided
	 * Takes in the target word and the surrounding context in two pieces
	 * 
	 * @param Context before the target word
 	 * @param Target word
 	 * @param Context after the target word
	 * @throws FileNotFoundException
	 */
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
					(long)432000, (long)345600, numAssignments,
					word, requirements, null
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

	/** A method to create a HIT with the context given but the target word omitted
	 * 
	 * @param Context before the target word
 	 * @param Target word
 	 * @param Context after the target word
	 * @throws FileNotFoundException
	 */
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
					(long)432000, (long)259200, numAssignments,
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

	/** A method to create a HIT with target word given but the context omitted
	 * 
 	 * @param Target word
 	 * @param Word sense
 	 * @param Part of speech
	 * @throws FileNotFoundException
	 */
	public void createNoContextGivenSurvey(String word, String sense, String POS) throws FileNotFoundException
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
							word, sense, POS),
							reward,
							(long)300,
							(long)432000, (long)259200, numAssignments,
							word, requirements, null);


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
	/** A Method to create a HIT with the target word and the 2 words on either side along with the sense and part of speech
	 * 
	 * @param Context before the target word
 	 * @param Target word
 	 * @param Context after the target word
 	 * @param Word sense
 	 * @param Part of speech
	 */
	private void createPartialContextGivenSurvey(String partialFirst, String target, String partialSecond, String sense, String POS) {
		try 
		{
			HIT hit = service.createHIT
			(
					null,
					partialContextTitle,
					partialContextDescription,
					null,
					partialContextSub(
							partialFirst, target, partialSecond, sense, POS),
							reward,
							(long)300,
							(long)432000, (long)259200, numAssignments,
							target, requirements, null);


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


	/** Deletes the hit whose ID is given
	 *  
	 * @param HIT Id
	 * @throws IOException
	 */
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


	/** Approves all the submissions for the hit whose ID is given
	 * 
	 * @param HIT Id
	 * @throws IOException
	 */
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

	/** Generates a string for the HTML of a HIT with the target word provided in its context
	 * 
	 * @param Context before the target word
 	 * @param Target word
 	 * @param Context after the target word
	 * @return String representing the HTML for a Context HIT
	 */
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
		q += " 				alert(\"Please provide a word.\");";
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
 	
 	/** Generates a string for the HTML of a hit that gives the target word with 2 words before and after and the sense and part of speech
 	 * 
 	 * @param Context before the target word
 	 * @param Target word
 	 * @param Context after the target word
 	 * @param Word sense
 	 * @param Part of speech
 	 * @return String representing the HTML for a Partial Context HIT
 	 */
 	public static String partialContextSub(String firstPartQuestion, String word, String secondPartQuestion, String sense, String POS){
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
		q += "		Below is a word in context with its definition. Enter a <i>simpler</i> word in the box below that could be substituted for the red, bold word in the context.";
		q += "		A <i>simpler</i> word is one that would be understood by more people or people with a lower reading level (e.g. children). <br/> <br/>";
		q += "		Make sure that the simple word you enter fits in the context of the sentence.";
		q += "		For example, given the context and definition:<br/> <br/>" ;
		q += "		<span style=\"margin-bottom:10px\">horse was <span style=\"color:red;\">galloping</span> through the</span> <br/><br/>";
		q += "		<span style=\"margin-top:10px\">galloping (VERB): go at galloping speed</span><br/><br/>";
		q += "		Entering <b>run</b> would NOT be appropriate, however <b>running</b> would be.<br/>";
		q += "		<hr />";
		q += "		<br /><u><b><span style=\"font-size:25px;\">Task:</span></b></u><br />";
		q += "		<br /><span style = \"font-size:20px; margin-bottom:10px;\">" + firstPartQuestion + "<b style=\"color:red;\">" + word + "</b>" + secondPartQuestion + "</span><br/>";
		q += "		<br /><span style = \"font-size:20px; margin-top:10px\"><b>" + word + "</b>: (" + POS + ") " + sense + "</span>";
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
		q += "			if (document.getElementById(\"answer\").value == \"\" || document.getElementById(\"answer\").value.trim() == \"\"){";
		q += " 				alert(\"Please provide a word.\");";
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

	/** Generates a string for the HTML of a HIT with the context given but the target word omitted
	 * 
	 * @param Context before the target word
 	 * @param Target word
 	 * @param Context after the target word
	 * @return String representing the HTML for a No Target HIT
	 */
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

	/** Generates a string for the HTML of a HIT with the target word given but the context omitted
	 * 
 	 * @param Target word
 	 * @param Word sense
 	 * @param Part of speech
	 * @return String representing the HTML for a No Context HIT
	 */
	public static String noContextGivenSub(String word, String sense, String POS) {
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
		q += " 				alert(\"Please provide a word.\");";
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
	 * Main method drives all methods
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{

		String usageError = "Please provide a valid option. Such as: " +
		"\n -add FILENAME 				*creates new HITs from the data provided in the given file(s)* " +
		"\n -delete FILENAME        	*deletes all of the HITs with IDs matching those given in the file(s)*" +
		"\n -approveAll FILENAME 		*approves all the assignments for all HITs with IDs in the given file(s)*";

		if (args.length >=1){
			// Create an instance of this class.
			LexicalSubSurvey app = new LexicalSubSurvey();
			File inputFile = null;

			try {
				if (args.length>1)
					inputFile = new File(args[1]);


				if (args[0].equals("-add")){
					//When -add tag is given in adds HITs to Mechanical turk depending on the URL in the mturk.properties file

					String[] parts = {"NN", "NNS", "JJ", "JJR", "JJS", "RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
					ArrayList<String> pos = new ArrayList<String>();
					for (int i=0; i<parts.length; i++){
						pos.add(parts[i]);
					}



					ExamplePairReader reader = new ExamplePairReader(PARSED, ALIGN);
					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))); // typical file name: "sub.simple.first100"
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
					Date date = new Date();
					
					
					
					// The three different experiments leave one uncommented at a time to do single groupings
					app.contextpr = new PrintWriter(new FileOutputStream(new File(inputFile.getName() +"ContextGivenIDs" + dateFormat.format(date) )));
					app.partialContextpr = new PrintWriter(new FileOutputStream(new File(inputFile.getName() + "partialContextIDs" + dateFormat.format(date) )));
					app.noContextpr = new PrintWriter(new FileOutputStream(new File(inputFile.getName() + "NoContextGivenIDs" + dateFormat.format(date) )));
					
					

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

					for( int k = 0; k <1000000 && reader.hasNext(); k++ ){ // for counted input goes through until reaches end or max number	
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

								
								//comment out 2 out of the 3 for single grouping
								app.createContextGivenSurvey(firstPart, target, secondPart);
								app.createPartialContextGivenSurvey(partialFirst, target, partialSecond, sense, POS);
								app.createNoContextGivenSurvey(target, sense, POS);
								
								
							}
						}
					}
					
					
					//comment out 2 for single grouping
					app.contextpr.close();
					app.partialContextpr.close();
					app.noContextpr.close();
					
					
					

				}else if (args[0].equals("-delete")){ //deletes the hits whose IDs are in the given file
					System.out.println("deleting");
					// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
					BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
					String hitId = "";

					for (hitId = fileReader.readLine(); hitId !=null; hitId = fileReader.readLine()){
						System.out.println(hitId);
						app.deleteHIT(hitId);
					}
					
				}else if (args[0].equals("-approveAll")){ // approves all submissions for all hits whose IDs in the given file
					System.out.println("approving");
					// IDs are usually stored in these files: NoContextGivenIDs, NoTargetGivenIDs, ContextGivenIDs 
					BufferedReader fileReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
					String hitId = "";

					for (hitId = fileReader.readLine(); hitId !=null; hitId = fileReader.readLine()){
						System.out.println(hitId);
						app.approveHIT(hitId);
					}
					
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
}
