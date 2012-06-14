package mturk;

import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;
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
   private String contextGivenTitle = "Word Simplification in Sentence_";
   private String noTargetGivenTitle = "Word Suggestion in Sentence_";
   private String noContextGivenTitle = "Word Simplification_";
   private String contextGivenDescription = 
    "Replace a word with a simple substitute in the given sentence.";
   private String noTargetGivenDescription = 
	"Suggest a simple word in the given sentence.";
   private String noContextGivenDescription = 
	"Replace a word with a simple substitute.";
   private int numAssignments = 10;
   private double reward = 00.02;

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
       PrintWriter pr = new PrintWriter(new FileOutputStream("ContextGivenIDs"));
       pr.print(hit.getHITId());
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
       PrintWriter pr = new PrintWriter(new FileOutputStream("NoTargetGivenIDs"));
       pr.print(hit.getHITId());
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
       PrintWriter pr = new PrintWriter(new FileOutputStream("NoContextGivenIDs"));
       pr.print(hit.getHITId());
       System.out.println("HIT location: ");
       System.out.println(service.getWebsiteURL() + "/mturk/preview?groupId=" + hit.getHITTypeId());
      
    }
    catch (ServiceException e) 
    {
       System.err.println(e.getLocalizedMessage());
    }
  }
   
   
   public static String contextGivenSub(String firstPartQuestion, String word, String secondPartQuestion) {
	    String q = "";
	    q += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	    q += "<QuestionForm xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd\">";
	    q += "  <Question>"; 
	    q += "    <QuestionIdentifier>1</QuestionIdentifier>";
	    q += "    <QuestionContent>";
	    q += "      <FormattedContent><![CDATA[";
	    q += "			<p>Please provide a simpler substitution for the the bolded red word:";
	    q += "			<br/>__________________________________________________________________";			
	    q += "			<br/><br/><font size=\"4\"> "+ firstPartQuestion + "<b><font face=\"bold\" color=\"red\">" + word + "</font></b>" + secondPartQuestion + "</font></p>";
	    q += "		]]></FormattedContent>";
	    q += "    </QuestionContent>"; 
	    q += "    <AnswerSpecification>";
	    q += "      <FreeTextAnswer>";
	    q += "      	<NumberOfLinesSuggestion>1</NumberOfLinesSuggestion>";
	    q += "      </FreeTextAnswer>";
	    q += "    </AnswerSpecification>"; 
	    q += "  </Question>";
	    q += "</QuestionForm>";
	    return q;
	  }

   public static String noTargetGivenSub(String firstPartQuestion, String word, String secondPartQuestion) {
	    String q = "";
	    q += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	    q += "<QuestionForm xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd\">";
	    q += "  <Question>"; 
	    q += "    <QuestionIdentifier>1</QuestionIdentifier>";
	    q += "    <QuestionContent>";
	    q += "      <FormattedContent><![CDATA[";
	    q += "			<p>Please provide a simple word for the blank:";
	    q += "			<br/>___________________________________________";
	    q += "			<br/><br/><font size=\"4\"> "+ firstPartQuestion + "_______ " +  secondPartQuestion + "</font></p>";
	    q += "		]]></FormattedContent>";
	    q += "    </QuestionContent>"; 
	    q += "    <AnswerSpecification>";
	    q += "      <FreeTextAnswer>";
	    q += "      	<NumberOfLinesSuggestion>1</NumberOfLinesSuggestion>";
	    q += "      </FreeTextAnswer>";
	    q += "    </AnswerSpecification>"; 
	    q += "  </Question>";
	    q += "</QuestionForm>";
	    return q;
	  }
  
   public static String noContextGivenSub(String firstPartQuestion, String word, String secondPartQuestion) {
	    String q = "";
	    q += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	    q += "<QuestionForm xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd\">";
	    q += "  <Question>"; 
	    q += "    <QuestionIdentifier>1</QuestionIdentifier>";
	    q += "    <QuestionContent>";
	    q += "      <FormattedContent><![CDATA[";
	    q += "			<p>Please provide a simple substitute for the word below:";
	    q += "			<br/>______________________________________________________";
	    q += "			<br/><br/><b><font size=\"4\" face=\"bold\"> "+ word + "</font></b></p>";
	    q += "		]]></FormattedContent>";
	    q += "    </QuestionContent>"; 
	    q += "    <AnswerSpecification>";
	    q += "      <FreeTextAnswer>";
	    q += "      	<NumberOfLinesSuggestion>1</NumberOfLinesSuggestion>";
	    q += "      </FreeTextAnswer>";
	    q += "    </AnswerSpecification>"; 
	    q += "  </Question>";
	    q += "</QuestionForm>";
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
     // Create an instance of this class.  
     LexicalSubSurvey app = new LexicalSubSurvey();
     
     
     BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(new File("sub.simple.first100"))));
     
     String input = in.readLine();
     
     while (input != null){
    	 String sampleSentence = "";
    	 String firstPart = "";
    	 String focus = "";
    	 String secondPart = "";
    	 
    	 StringTokenizer splitter = new StringTokenizer(input, "{");
    	 String inputSentence = splitter.nextToken();
    	 int index = Integer.parseInt(splitter.nextToken(",").substring(1));
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
   }
}
