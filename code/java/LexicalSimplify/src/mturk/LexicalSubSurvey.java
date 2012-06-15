package mturk;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;
import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
import com.amazonaws.mturk.requester.HIT;
import java.io.*;
import java.util.StringTokenizer;

/**
 * A class to create lexical simplification HITs
 * Patrick Adelstein, Teddy Pendergast, David Kauchack, Middlebury College Summer 2012
 */
public class LexicalSubSurvey{


	private RequesterService service;

	// Define the properties of the HIT to be created.
	private String contextGivenTitle = "Word Simplification in Sentence";
	private String noTargetGivenTitle = "Word Suggestion in Sentence";
	private String noContextGivenTitle = "Word Simplification";
	private String contextGivenDescription = 
		"Replace a word with a simple substitute in the given sentence.";
	private String noTargetGivenDescription = 
		"Suggest a simple word in the given sentence.";
	private String noContextGivenDescription = 
		"Replace a word with a simple substitute.";
	private int numAssignments = 1;
	private double reward = 00.02;
	private PrintWriter noContextpr;
	private PrintWriter contextpr;
	private PrintWriter noTargetpr;
	private PrintWriter answerOutput;

	/**
	 * Constructor
	 */
	public LexicalSubSurvey()
	{
		service = new RequesterService(new PropertiesClientConfig());
	}

	public void createContextGivenSurvey(String firstSentence, String word, String secondSentence) throws FileNotFoundException
	{
		try 
		{
			HIT hit = service.createHIT
			(
					contextGivenTitle,
					contextGivenDescription,
					reward,
					contextGivenSub(
							firstSentence, word, secondSentence),
							numAssignments);

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

	public void createNoTargetGivenSurvey(String firstSentence, String word, String secondSentence) throws FileNotFoundException
	{
		try 
		{
			HIT hit = service.createHIT
			(
					noTargetGivenTitle,
					noTargetGivenDescription,
					reward,
					noTargetGivenSub(
							firstSentence, word, secondSentence),
							numAssignments);

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

	public void createNoContextGivenSurvey(String firstSentence, String word, String secondSentence) throws FileNotFoundException
	{
		try 
		{
			HIT hit = service.createHIT
			(
					noContextGivenTitle,
					noContextGivenDescription,
					reward,
					noContextGivenSub(
							firstSentence, word, secondSentence),
							numAssignments);

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

	public void getAssignmentsHIT(String hitId) throws IOException
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
				int textStart = answer.getAnswer().indexOf("<FreeText>");
				int textEnd = answer.getAnswer().indexOf("</FreeText>");
				answerOutput.print(answer.getAnswer().substring(textStart + 10, textEnd) + " ");
			}
			answerOutput.println();

		}
		catch (ServiceException e) 
		{
			System.err.println(e.getLocalizedMessage());
		}
	}
	
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
		q += "		<p>This is the real ONE</p>Please provide a simpler substitution for the the bolded red word:<br/>";
		q += "      <div id=\"test\"></div>";
		q += "		<hr />";
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
		q += "  <FrameHeight>200</FrameHeight>";
		q += "</HTMLQuestion>";
		return q;
	}

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
		q += "		<p>This is the real ONE</p>Please provide a simple word for the blank:<br/>";
		q += "      <div id=\"test\"></div>";
		q += "		<hr />";
		q += "      <form name='mturk_form' method='post' id='mturk_form' action='https://www.mturk.com/mturk/externalSubmit'>";
		q += "		<br /><span style = \"font-size:20px;\">" + firstPartQuestion + "<input type=\"text\" name=\"HITAnswer\" id =\"answer\"/> " + secondPartQuestion + "</span>";
		q += "      <br/><input type=\'hidden\' value=\'\' name =\'assignmentId\' id=\'assignmentId\'/>";
		q += "      <input type=\"submit\" value=\"Submit\" id=\"submit_button\" style=\"margin-top:10px\"/>";
		q += "      </form>";
		q += "    <script language='Javascript'>turkSetAssignmentID();</script>";
		q += "    <script type=\'text/javascript\'>";
		q += "      if (document.getElementById(\"assignmentId\").value == \"ASSIGNMENT_ID_NOT_AVAILABLE\") {";
		q += "        document.getElementById(\"submit_button\").disable = true;";
		q += "        document.getElementById(\"answer\").disabled = true; } ";
		q += "      else {document.getElementById(\"submit_button\").disabled = false;";
		q += "		  document.getElementById(\"answer\").disabled = false; }";
		q += "    </script>";
		q += "  </body>";
		q += "</html>]]>";
		q += "  </HTMLContent>";
		q += "  <FrameHeight>200</FrameHeight>";
		q += "</HTMLQuestion>";
		return q;
	}

	public static String noContextGivenSub(String firstPartQuestion, String word, String secondPartQuestion) {
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
		q += "		<p>This is the real ONE</p>Please provide a simple substitute for the word below:<br/>";
		q += "      <div id=\"test\"></div>";
		q += "		<hr />";
		q += "		<br /><span style = \"font-size:20px;\"><b>" + word + "</b></span>";
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
		q += "  <FrameHeight>200</FrameHeight>";
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

						BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))); // typical file name: "sub.simple.first100"
						app.noContextpr = new PrintWriter(new FileOutputStream(new File("NoContextGivenIDs")));
						app.noTargetpr = new PrintWriter(new FileOutputStream(new File("NoTargetGivenIDs")));
						app.contextpr = new PrintWriter(new FileOutputStream(new File("ContextGivenIDs")));

						String input = in.readLine();
						for(int k = 0; k <10; k++){
//						while (input != null){
							String sampleSentence = "";
							String firstPart = "";
							String focus = "";
							String secondPart = "";

							StringTokenizer splitter = new StringTokenizer(input, "\t");
							String inputSentence = splitter.nextToken();
							int index = Integer.parseInt(splitter.nextToken());
							StringTokenizer sentenceSplitter = new StringTokenizer(inputSentence);
							String word = sentenceSplitter.nextToken();

							for (int i = 0; sentenceSplitter.hasMoreTokens(); i++){
								if (i < index)
									firstPart += word + " ";
								if (i > index)
									secondPart += word + " ";

								else
									focus = word +" ";


								sampleSentence += word + " ";
								word = sentenceSplitter.nextToken();
							}

							sampleSentence = sampleSentence +".";
							app.createContextGivenSurvey(firstPart, focus, secondPart);
							app.createNoTargetGivenSurvey(firstPart, focus, secondPart);
							app.createNoContextGivenSurvey(firstPart, focus, secondPart);

							input = in.readLine();
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
						String hitId = "";
						String destination = inputFile.getName() + "Results";
						app.answerOutput = new PrintWriter(new FileOutputStream(new File(destination)));
						for (hitId = fileReader.readLine(); hitId !=null; hitId = fileReader.readLine()){
							System.out.println(hitId);
							app.getAssignmentsHIT(hitId);
						}
						app.answerOutput.close();
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
}