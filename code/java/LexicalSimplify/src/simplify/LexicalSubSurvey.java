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
import java.util.Iterator;
import java.util.StringTokenizer;

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
	
	// define the writers for storage of HIT IDs
	private PrintWriter noContextpr;
	private PrintWriter contextpr;
	private PrintWriter noTargetpr;
	private PrintWriter answerOutput;

	private QualificationRequirement acceptanceRate = new QualificationRequirement("000000000000000000L0", Comparator.GreaterThanOrEqualTo, 90, null, false);
	private QualificationRequirement location = new QualificationRequirement("00000000000000000071", Comparator.EqualTo, null, new Locale("US"), false);
	private QualificationRequirement[] requirements = {acceptanceRate, location};
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
					(long)3600,
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
					(long)3600,
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
	public void createNoContextGivenSurvey(String firstSentence, String word, String secondSentence) throws FileNotFoundException
	{
		try 
		{
			HIT hit = service.createHIT
			(
					null,
					noContextGivenTitle,
					noContextGivenDescription,
					null,
					noContextGivenSub(firstSentence, word, secondSentence),
					reward,
					(long)3600,
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

	// HTML for a HIT with target word given but the context omitted
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

						String[] parts = {"NN", "NNS", "JJ", "JJR", "JJS", "RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN", "VBP", "VBZ"};
						ArrayList<String> pos = new ArrayList<String>();
						for (int i=0; i<parts.length; i++){
							pos.add(parts[i]);
						}
						
						String[] simpleList = {"America", "American", "April", "August", "Christmas", "December", "English", "February", 
								"French", "Friday", "God", "I", "I'd", "I'll", "I'm", "I've", "Indian", "January", "July", "June", "March", 
								"May", "Miss", "Monday", "Mr.", "Mrs.", "Negro", "November", "October", "Saturday", "September", "States", 
								"Sunday", "Thanksgiving", "Thursday", "Tuesday", "United", "Wednesday", "a", "able", "aboard", "about", "above", 
								"absent", "accept", "accident", "account", "ache", "aching", "acid", "acorn", "acre", "across", "act", "acts", "add", 
								"addition", "address", "adjustment", "admire", "adventure", "advertisement", "afar", "afraid", "after", "afternoon", 
								"afterward", "afterwards", "again", "against", "age", "aged", "ago", "agree", "agreement", "ah", "ahead", "aid", 
								"aim", "air", "airfield", "airplane", "airport", "airship", "airy", "alarm", "alike", "alive", "all", "alley", 
								"alligator", "allow", "almost", "alone", "along", "aloud", "already", "also", "always", "am", "among", "amount", 
								"amusement", "an", "and", "angel", "anger", "angle", "angry", "animal", "another", "answer", "ant", "any", "anybody", 
								"anyhow", "anyone", "anything", "anyway", "anywhere", "apart", "apartment", "ape", "apiece", "apparatus", "appear", 
								"apple", "approval", "apron", "arch", "are", "aren't", "argument", "arise", "arithmetic", "arm", "armful", "army", "arose", 
								"around", "arrange", "arrive", "arrived", "arrow", "art", "artist", "as", "ash", "ashes", "aside", "ask", "asleep", "at", 
								"ate", "attack", "attempt", "attend", "attention", "attraction", "aunt", "author", "authority", "auto", "automatic", 
								"automobile", "autumn", "avenue", "awake", "awaken", "away", "awful", "awfully", "awhile", "ax", "axe", "baa", "babe", 
								"babies", "baby", "back", "background", "backward", "backwards", "bacon", "bad", "badge", "badly", "bag", "bake", "baker", 
								"bakery", "baking", "balance", "ball", "balloon", "banana", "band", "bandage", "bang", "banjo", "bank", "banker", "bar", 
								"barber", "bare", "barefoot", "barely", "bark", "barn", "barrel", "base", "baseball", "basement", "basin", "basket", "bat", 
								"batch", "bath", "bathe", "bathing", "bathroom", "bathtub", "battle", "battleship", "bay", "be", "beach", "bead", "beam", 
								"bean", "bear", "beard", "beast", "beat", "beating", "beautiful", "beautify", "beauty", "became", "because", "become", 
								"becoming", "bed", "bedbug", "bedroom", "bedspread", "bedtime", "bee", "beech", "beef", "beefsteak", "beehive", "been", 
								"beer", "beet", "before", "beg", "began", "beggar", "begged", "begin", "beginning", "begun", "behave", "behavior", "behind", 
								"being", "belief", "believe", "bell", "belong", "below", "belt", "bench", "bend", "beneath", "bent", "berries", "berry", 
								"beside", "besides", "best", "bet", "better", "between", "bib", "bible", "bicycle", "bid", "big", "bigger", "bill", "billboard", 
								"bin", "bind", "bird", "birth", "birthday", "biscuit", "bit", "bite", "biting", "bitter", "black", "blackberry", "blackbird", 
								"blackboard", "blackness", "blacksmith", "blade", "blame", "blank", "blanket", "blast", "blaze", "bleed", "bless", "blessing", 
								"blew", "blind", "blindfold", "blinds", "block", "blood", "bloom", "blossom", "blot", "blow", "blue", "blueberry", "bluebird", 
								"blush", "board", "boast", "boat", "bob", "bobwhite", "bodies", "body", "boil", "boiler", "boiling", "bold", "bone", "bonnet", 
								"boo", "book", "bookcase", "bookkeeper", "boom", "boot", "born", "borrow", "boss", "both", "bother", "bottle", "bottom", 
								"bought", "bounce", "bow", "bow-wow", "bowl", "box", "boxcar", "boxer", "boxes", "boy", "boyhood", "bracelet", "brain", "brake", 
								"bran", "branch", "brass", "brave", "bread", "break", "breakfast", "breast", "breath", "breathe", "breeze", "brick", "bride", 
								"bridge", "bright", "brightness", "bring", "broad", "broadcast", "broke", "broken", "brook", "broom", "brother", "brought", 
								"brown", "brush", "bubble", "bucket", "buckle", "bud", "buffalo", "bug", "buggy", "build", "building", "built", "bulb", "bull", 
								"bullet", "bum", "bumblebee", "bump", "bun", "bunch", "bundle", "bunny", "burn", "burst", "bury", "bus", "bush", "bushel", 
								"business", "busy", "but", "butcher", "butt", "butter", "buttercup", "butterfly", "buttermilk", "butterscotch", "button", 
								"buttonhole", "buy", "buzz", "by", "bye", "cab", "cabbage", "cabin", "cabinet", "cackle", "cage", "cake", "calendar", "calf", 
								"call", "caller", "calling", "came", "camel", "camera", "camp", "campfire", "can", "can't", "canal", "canary", "candle", 
								"candlestick", "candy", "cane", "cannon", "cannot", "canoe", "canvas", "canyon", "cap", "cape", "capital", "captain", "car", 
								"card", "cardboard", "care", "careful", "careless", "carelessness", "carload", "carpenter", "carpet", "carriage", "carrot", 
								"carry", "cart", "carve", "case", "cash", "cashier", "castle", "cat", "catbird", "catch", "catcher", "caterpillar", "catfish", 
								"catsup", "cattle", "caught", "cause", "cave", "ceiling", "cell", "cellar", "cent", "center", "cereal", "certain", "certainly", 
								"chain", "chair", "chalk", "champion", "chance", "change", "chap", "charge", "charm", "chart", "chase", "chatter", "cheap", 
								"cheat", "check", "checkers", "cheek", "cheer", "cheese", "chemical", "cherry", "chest", "chew", "chick", "chicken", "chief", 
								"child", "childhood", "children", "chill", "chilly", "chimney", "chin", "china", "chip", "chipmunk", "chocolate", "choice", 
								"choose", "chop", "chorus", "chose", "chosen", "christen", "church", "churn", "cigarette", "circle", "circus", "citizen", 
								"city", "clang", "clap", "class", "classmate", "classroom", "claw", "clay", "clean", "cleaner", "clear", "clerk", "clever", 
								"click", "cliff", "climb", "clip", "cloak", "clock", "close", "closet", "cloth", "clothes", "clothing", "cloud", "cloudy", 
								"clover", "clown", "club", "cluck", "clump", "coach", "coal", "coast", "coat", "cob", "cobbler", "cocoa", "coconut", "cocoon", 
								"cod", "codfish", "coffee", "coffeepot", "coin", "cold", "collar", "college", "color", "colored", "colt", "column", "comb", 
								"come", "comfort", "comic", "coming", "committee", "common", "company", "compare", "comparison", "competition", "complete", 
								"complex", "condition", "conductor", "cone", "connect", "connection", "conscious", "control", "coo", "cook", "cooked", "cookie", 
								"cookies", "cooking", "cool", "cooler", "coop", "copper", "copy", "cord", "cork", "corn", "corner", "correct", "cost", "cot", 
								"cottage", "cotton", "couch", "cough", "could", "couldn't", "count", "counter", "country", "county", "course", "court", 
								"cousin", "cover", "cow", "coward", "cowardly", "cowboy", "cozy", "crab", "crack", "cracker", "cradle", "cramps", "cranberry", 
								"crank", "cranky", "crash", "crawl", "crazy", "cream", "creamy", "credit", "creek", "creep", "crept", "cried", "cries", "crime", 
								"croak", "crook", "crooked", "crop", "cross", "cross-eyed", "crossing", "crow", "crowd", "crowded", "crown", "cruel", "crumb", 
								"crumble", "crush", "crust", "cry", "cub", "cuff", "cup", "cupboard", "cupful", "cure", "curl", "curly", "current", "curtain", 
								"curve", "cushion", "custard", "customer", "cut", "cute", "cutting", "dab", "dad", "daddy", "daily", "dairy", "daisy", "dam", 
								"damage", "dame", "damp", "dance", "dancer", "dancing", "dandy", "danger", "dangerous", "dare", "dark", "darkness", "darling", 
								"darn", "dart", "dash", "date", "daughter", "dawn", "day", "daybreak", "daytime", "dead", "deaf", "deal", "dear", "death", 
								"debt", "decide", "decision", "deck", "deed", "deep", "deer", "defeat", "defend", "defense", "degree", "delicate", "delight", 
								"den", "dentist", "depend", "dependent", "deposit", "describe", "desert", "deserve", "design", "desire", "desk", "destroy", 
								"destruction", "detail", "development", "devil", "dew", "diamond", "did", "didn't", "die", "died", "dies", "difference", 
								"different", "dig", "digestion", "dim", "dime", "dine", "ding-dong", "dinner", "dip", "direct", "direction", "dirt", "dirty", 
								"discover", "discovery", "discussion", "disease", "disgust", "dish", "dislike", "dismiss", "distance", "distribution", "ditch", 
								"dive", "diver", "divide", "division", "do", "dock", "doctor", "does", "doesn't", "dog", "doll", "dollar", "dolly", "don't", 
								"done", "donkey", "door", "doorbell", "doorknob", "doorstep", "dope", "dot", "double", "doubt", "dough", "dove", "down", 
								"downstairs", "downtown", "dozen", "drag", "drain", "drank", "draw", "drawer", "drawing", "dream", "dress", "dresser", 
								"dressmaker", "drew", "dried", "drift", "drill", "drink", "drip", "drive", "driven", "driver", "driving", "drop", "drove", 
								"drown", "drowsy", "drub", "drum", "drunk", "dry", "duck", "due", "dug", "dull", "dumb", "dump", "during", "dust", "dusty", 
								"duty", "dwarf", "dwell", "dwelt", "dying", "each", "eager", "eagle", "ear", "early", "earn", "earth", "east", "eastern", 
								"easy", "eat", "eaten", "edge", "education", "effect", "egg", "eh", "eight", "eighteen", "eighth", "eighty", "either", 
								"elastic", "elbow", "elder", "eldest", "electric", "electricity", "elephant", "eleven", "elf", "elm", "else", "elsewhere", 
								"empty", "end", "ending", "enemy", "engine", "engineer", "enjoy", "enough", "enter", "envelope", "equal", "erase", "eraser", 
								"errand", "error", "escape", "eve", "even", "evening", "event", "ever", "every", "everybody", "everyday", "everyone", 
								"everything", "everywhere", "evil", "exact", "example", "except", "exchange", "excited", "exciting", "excuse", "existence", 
								"exit", "expansion", "expect", "experience", "expert", "explain", "extra", "eye", "eyebrow", "fable", "face", "facing", "fact", 
								"factory", "fail", "faint", "fair", "fairy", "faith", "fake", "fall", "false", "family", "fan", "fancy", "far", "far-off", 
								"faraway", "fare", "farm", "farmer", "farming", "farther", "fashion", "fast", "fasten", "fat", "father", "fault", "favor", 
								"favorite", "fear", "feast", "feather", "fed", "feeble", "feed", "feel", "feeling", "feet", "fell", "fellow", "felt", "female", 
								"fence", "fertile", "fever", "few", "fib", "fiction", "fiddle", "field", "fife", "fifteen", "fifth", "fifty", "fig", "fight", 
								"figure", "file", "fill", "film", "finally", "find", "fine", "finger", "finish", "fire", "firearm", "firecracker", "fireplace", 
								"fireworks", "firing", "first", "fish", "fisherman", "fist", "fit", "fits", "five", "fix", "fixed", "flag", "flake", "flame", 
								"flap", "flash", "flashlight", "flat", "flea", "flesh", "flew", "flies", "flight", "flip", "flip-flop", "float", "flock", 
								"flood", "floor", "flop", "flour", "flow", "flower", "flowery", "flutter", "fly", "foam", "fog", "foggy", "fold", "folks", 
								"follow", "following", "fond", "food", "fool", "foolish", "foot", "football", "footprint", "for", "force", "forehead", 
								"forest", "forget", "forgive", "forgot", "forgotten", "fork", "form", "fort", "forth", "fortune", "forty", "forward", "fought", 
								"found", "fountain", "four", "fourteen", "fourth", "fowl", "fox", "frame", "free", "freedom", "freeze", "freight", "frequent", 
								"fresh", "fret", "fried", "friend", "friendly", "friendship", "frighten", "frog", "from", "front", "frost", "frown", "froze", 
								"fruit", "fry", "fudge", "fuel", "full", "fully", "fun", "funny", "fur", "furniture", "further", "future", "fuzzy", "gain", 
								"gallon", "gallop", "game", "gang", "garage", "garbage", "garden", "gas", "gasoline", "gate", "gather", "gave", "gay", "gear", 
								"geese", "general", "gentle", "gentleman", "gentlemen", "geography", "get", "getting", "giant", "gift", "gingerbread", "girl", 
								"give", "given", "giving", "glad", "gladly", "glance", "glass", "glasses", "gleam", "glide", "glory", "glove", "glow", "glue", 
								"go", "goal", "goat", "gobble", "god", "godmother", "goes", "going", "gold", "golden", "goldfish", "golf", "gone", "good", 
								"good-by", "good-bye", "good-looking", "goodbye", "goodness", "goods", "goody", "goose", "gooseberry", "got", "govern", 
								"government", "gown", "grab", "gracious", "grade", "grain", "grand", "grandchild", "grandchildren", "granddaughter", 
								"grandfather", "grandma", "grandmother", "grandpa", "grandson", "grandstand", "grape", "grapefruit", "grapes", "grass", 
								"grasshopper", "grateful", "grave", "gravel", "graveyard", "gravy", "gray", "graze", "grease", "great", "green", "greet", 
								"grew", "grey/gray", "grind", "grip", "groan", "grocery", "ground", "group", "grove", "grow", "growth", "guard", "guess", 
								"guest", "guide", "gulf", "gum", "gun", "gunpowder", "guy", "ha", "habit", "had", "hadn't", "hail", "hair", "haircut", 
								"hairpin", "half", "hall", "halt", "ham", "hammer", "hand", "handful", "handkerchief", "handle", "handwriting", "hang", 
								"hanging", "happen", "happily", "happiness", "happy", "harbor", "hard", "hardly", "hardship", "hardware", "hare", "hark", 
								"harm", "harmony", "harness", "harp", "harvest", "has", "hasn't", "haste", "hasten", "hasty", "hat", "hatch", "hatchet", 
								"hate", "haul", "have", "haven't", "having", "hawk", "hay", "hayfield", "haystack", "he", "he'd", "he'll", "he's", "head", 
								"headache", "heal", "health", "healthy", "heap", "hear", "heard", "hearing", "heart", "heat", "heater", "heaven", "heavy", 
								"heel", "height", "held", "hell", "hello", "helmet", "help", "helper", "helpful", "hem", "hen", "henhouse", "her", "herd", 
								"here", "here's", "hero", "hers", "herself", "hey", "hickory", "hid", "hidden", "hide", "high", "highway", "hill", "hillside", 
								"hilltop", "hilly", "him", "himself", "hind", "hint", "hip", "hire", "his", "hiss", "history", "hit", "hitch", "hive", "ho", 
								"hoe", "hog", "hold", "holder", "hole", "holiday", "hollow", "holy", "home", "homely", "homesick", "honest", "honey", 
								"honeybee", "honeymoon", "honk", "honor", "hood", "hoof", "hook", "hoop", "hop", "hope", "hopeful", "hopeless", "horn", "horse", 
								"horseback", "horseshoe", "hose", "hospital", "host", "hot", "hotel", "hound", "hour", "house", "housetop", "housewife", 
								"housework", "how", "however", "howl", "hug", "huge", "hum", "humble", "humor", "hump", "hundred", "hung", "hunger", "hungry", 
								"hunk", "hunt", "hunter", "hurrah", "hurried", "hurry", "hurt", "husband", "hush", "hut", "hymn", "ice", "icy", "idea", "ideal", 
								"if", "ill", "important", "impossible", "improve", "impulse", "in", "inch", "inches", "income", "increase", "indeed", "indoors", 
								"industry", "ink", "inn", "insect", "inside", "instant", "instead", "instrument", "insult", "insurance", "intend", "interest", 
								"interested", "interesting", "into", "invention", "invite", "iron", "is", "island", "isn't", "it", "it's", "its", "itself", 
								"ivory", "ivy", "jacket", "jacks", "jail", "jam", "jar", "jaw", "jay", "jelly", "jellyfish", "jerk", "jewel", "jig", "job", 
								"jockey", "join", "joke", "joking", "jolly", "journey", "joy", "joyful", "joyous", "judge", "jug", "juice", "juicy", "jump", 
								"junior", "junk", "just", "keen", "keep", "kept", "kettle", "key", "kick", "kid", "kill", "killed", "kind", "kindly", 
								"kindness", "king", "kingdom", "kiss", "kitchen", "kite", "kitten", "kitty", "knee", "kneel", "knew", "knife", "knit", "knives", 
								"knob", "knock", "knot", "know", "knowledge", "known", "lace", "lad", "ladder", "ladies", "lady", "laid", "lake", "lamb", 
								"lame", "lamp", "land", "lane", "language", "lantern", "lap", "lard", "large", "lash", "lass", "last", "late", "laugh", 
								"laundry", "law", "lawn", "lawyer", "lay", "lazy", "lead", "leader", "leaf", "leak", "lean", "leap", "learn", "learned", 
								"learning", "least", "leather", "leave", "leaving", "led", "left", "leg", "lemon", "lemonade", "lend", "length", "less", 
								"lesson", "let", "let's", "letter", "letting", "lettuce", "level", "liberty", "library", "lice", "lick", "lid", "lie", "life", 
								"lift", "light", "lightness", "lightning", "like", "likely", "liking", "lily", "limb", "lime", "limit", "limp", "line", "linen", 
								"lion", "lip", "liquid", "list", "listen", "lit", "little", "live", "lively", "liver", "lives", "living", "lizard", "load", 
								"loaf", "loan", "loaves", "lock", "locomotive", "log", "lone", "lonely", "lonesome", "long", "look", "lookout", "loop", "loose", 
								"lord", "lose", "loser", "loss", "lost", "lot", "loud", "love", "lovely", "lover", "low", "luck", "lucky", "lumber", "lump", 
								"lunch", "lying", "ma", "machine", "machinery", "mad", "made", "magazine", "magic", "maid", "mail", "mailbox", "mailman", 
								"major", "make", "making", "male", "mama", "mamma", "man", "manager", "mane", "manger", "many", "map", "maple", "marble", 
								"march", "mare", "mark", "market", "marriage", "married", "marry", "mask", "mass", "mast", "master", "mat", "match", "material", 
								"matter", "mattress", "may", "maybe", "mayor", "maypole", "me", "meadow", "meal", "mean", "means", "meant", "measure", "meat", 
								"medical", "medicine", "meet", "meeting", "melt", "member", "memory", "men", "mend", "meow", "merry", "mess", "message", "met", 
								"metal", "mew", "mice", "middle", "midnight", "might", "mighty", "mile", "miler", "military", "milk", "milkman", "mill", 
								"million", "mind", "mine", "miner", "mint", "minute", "mirror", "mischief", "miss", "misspell", "mist", "mistake", "misty", 
								"mitt", "mitten", "mix", "mixed", "moment", "money", "monkey", "month", "moo", "moon", "moonlight", "moose", "mop", "more", 
								"morning", "morrow", "moss", "most", "mostly", "mother", "motion", "motor", "mount", "mountain", "mouse", "mouth", "move", 
								"movie", "movies", "moving", "mow", "much", "mud", "muddy", "mug", "mule", "multiply", "murder", "muscle", "music", "must", 
								"my", "myself", "nail", "name", "nap", "napkin", "narrow", "nasty", "nation", "natural", "naughty", "navy", "near", "nearby", 
								"nearly", "neat", "necessary", "neck", "necktie", "need", "needle", "needn't", "neighbor", "neighborhood", "neither", "nerve", 
								"nest", "net", "never", "nevermore", "new", "news", "newspaper", "next", "nibble", "nice", "nickel", "night", "nightgown", 
								"nine", "nineteen", "ninety", "no", "nobody", "nod", "noise", "noisy", "none", "noon", "nor", "normal", "north", "northern", 
								"nose", "not", "note", "nothing", "notice", "now", "nowhere", "number", "nurse", "nut", "o'clock", "oak", "oar", "oatmeal", 
								"oats", "obey", "observation", "ocean", "odd", "of", "off", "offer", "office", "officer", "often", "oh", "oil", "old", 
								"old-fashioned", "on", "once", "one", "onion", "only", "onward", "open", "operation", "opinion", "opposite", "or", "orange", 
								"orchard", "order", "ore", "organ", "organization", "ornament", "other", "otherwise", "ouch", "ought", "our", "ours", 
								"ourselves", "out", "outdoors", "outfit", "outlaw", "outline", "outside", "outward", "oven", "over", "overalls", "overcoat", 
								"overeat", "overhead", "overhear", "overnight", "overturn", "owe", "owing", "owl", "own", "owner", "ox", "pa", "pace", "pack", 
								"package", "pad", "page", "paid", "pail", "pain", "painful", "paint", "painter", "painting", "pair", "pal", "palace", "pale", 
								"pan", "pancake", "pane", "pansy", "pants", "papa", "paper", "parade", "parallel", "parcel", "pardon", "parent", "park", "part", 
								"partly", "partner", "party", "pass", "passenger", "past", "paste", "pasture", "pat", "patch", "path", "patter", "pave", 
								"pavement", "paw", "pay", "payment", "pea", "peace", "peaceful", "peach", "peaches", "peak", "peanut", "pear", "pearl", "peas", 
								"peck", "peek", "peel", "peep", "peg", "pen", "pencil", "penny", "people", "pepper", "peppermint", "perfume", "perhaps", 
								"person", "pet", "phone", "physical", "piano", "pick", "pickle", "picnic", "picture", "pie", "piece", "pig", "pigeon", "piggy", 
								"pile", "pill", "pillow", "pin", "pine", "pineapple", "pink", "pint", "pipe", "pistol", "pit", "pitch", "pitcher", "pity", 
								"place", "plain", "plan", "plane", "plant", "plate", "platform", "platter", "play", "player", "playground", "playhouse", 
								"playmate", "plaything", "pleasant", "please", "pleasure", "plenty", "plough/plow", "plow", "plug", "plum", "pocket", 
								"pocketbook", "poem", "point", "poison", "poke", "pole", "police", "policeman", "polish", "polite", "political", "pond", 
								"ponies", "pony", "pool", "poor", "pop", "popcorn", "popped", "porch", "pork", "porter", "position", "possible", "post", 
								"postage", "postman", "pot", "potato", "potatoes", "pound", "pour", "powder", "power", "powerful", "praise", "pray", "prayer", 
								"prepare", "present", "pretty", "price", "prick", "prince", "princess", "print", "prison", "private", "prize", "probable", 
								"process", "produce", "profit", "promise", "proper", "property", "prose", "protect", "protest", "proud", "prove", "prune", 
								"public", "puddle", "puff", "pull", "pump", "pumpkin", "punch", "punish", "punishment", "pup", "pupil", "puppy", "pure", 
								"purple", "purpose", "purse", "push", "puss", "pussy", "pussycat", "put", "putting", "puzzle", "quack", "quality", "quart", 
								"quarter", "queen", "queer", "question", "quick", "quickly", "quiet", "quilt", "quit", "quite", "rabbit", "race", "rack", 
								"radio", "radish", "rag", "rail", "railroad", "railway", "rain", "rainbow", "rainy", "raise", "raisin", "rake", "ram", "ran", 
								"ranch", "rang", "range", "rap", "rapidly", "rat", "rate", "rather", "rattle", "raw", "ray", "reach", "reaction", "read", 
								"reader", "reading", "ready", "real", "really", "reap", "rear", "reason", "rebuild", "receipt", "receive", "recess", "record", 
								"red", "redbird", "redbreast", "refuse", "regret", "regular", "reindeer", "rejoice", "relation", "religion", "remain", 
								"remember", "remind", "remove", "rent", "repair", "repay", "repeat", "report", "representative", "request", "respect", 
								"responsible", "rest", "return", "review", "reward", "rhythm", "rib", "ribbon", "rice", "rich", "rid", "riddle", "ride", 
								"rider", "riding", "right", "rim", "ring", "rip", "ripe", "rise", "rising", "river", "road", "roadside", "roar", "roast", 
								"rob", "robber", "robe", "robin", "rock", "rocket", "rocky", "rod", "rode", "roll", "roller", "roof", "room", "rooster", 
								"root", "rope", "rose", "rosebud", "rot", "rotten", "rough", "round", "route", "row", "rowboat", "royal", "rub", "rubbed", 
								"rubber", "rubbish", "rug", "rule", "ruler", "rumble", "run", "rung", "runner", "running", "rush", "rust", "rusty", "rye", 
								"sack", "sad", "saddle", "sadness", "safe", "safety", "said", "sail", "sailboat", "sailor", "saint", "salad", "sale", "salt", 
								"same", "sand", "sandwich", "sandy", "sang", "sank", "sap", "sash", "sat", "satin", "satisfactory", "sausage", "savage", "save", 
								"savings", "saw", "say", "scab", "scale", "scales", "scare", "scarf", "school", "schoolboy", "schoolhouse", "schoolmaster", 
								"schoolroom", "science", "scissors", "scorch", "score", "scrap", "scrape", "scratch", "scream", "screen", "screw", "scrub", 
								"sea", "seal", "seam", "search", "season", "seat", "second", "secret", "secretary", "see", "seed", "seeing", "seek", "seem", 
								"seen", "seesaw", "select", "selection", "self", "selfish", "sell", "send", "sense", "sent", "sentence", "separate", "serious", 
								"servant", "serve", "service", "set", "setting", "settle", "settlement", "seven", "seventeen", "seventh", "seventy", "several", 
								"sew", "sex", "shade", "shadow", "shady", "shake", "shaker", "shaking", "shall", "shame", "shan't", "shape", "share", "sharp", 
								"shave", "she", "she'd", "she'll", "she's", "shear", "shears", "shed", "sheep", "sheet", "shelf", "shell", "shepherd", "shine", 
								"shining", "shiny", "ship", "shirt", "shock", "shoe", "shoemaker", "shone", "shook", "shoot", "shop", "shopping", "shore", 
								"short", "shot", "should", "shoulder", "shouldn't", "shout", "shovel", "show", "shower", "shut", "shy", "sick", "sickness", 
								"side", "sidewalk", "sideways", "sigh", "sight", "sign", "silence", "silent", "silk", "sill", "silly", "silver", "simple", 
								"sin", "since", "sing", "singer", "single", "sink", "sip", "sir", "sis", "sissy", "sister", "sit", "sitting", "six", "sixteen", 
								"sixth", "sixty", "size", "skate", "skater", "ski", "skin", "skip", "skirt", "sky", "slam", "slap", "slate", "slave", "sled", 
								"sleep", "sleepy", "sleeve", "sleigh", "slept", "slice", "slid", "slide", "sling", "slip", "slipped", "slipper", "slippery", 
								"slit", "slope", "slow", "slowly", "sly", "smack", "small", "smart", "smash", "smell", "smile", "smoke", "smooth", "snail", 
								"snake", "snap", "snapping", "sneeze", "snow", "snowball", "snowflake", "snowy", "snuff", "snug", "so", "soak", "soap", "sob", 
								"society", "sock", "socks", "sod", "soda", "sofa", "soft", "soil", "sold", "soldier", "sole", "solid", "some", "somebody", 
								"somehow", "someone", "something", "sometime", "sometimes", "somewhere", "son", "song", "soon", "sore", "sorrow", "sorry", 
								"sort", "soul", "sound", "soup", "sour", "south", "southern", "space", "spade", "spank", "sparrow", "speak", "speaker", 
								"spear", "special", "speech", "speed", "spell", "spelling", "spend", "spent", "spider", "spike", "spill", "spin", "spinach", 
								"spirit", "spit", "splash", "spoil", "spoke", "sponge", "spook", "spoon", "sport", "spot", "spread", "spring", "springtime", 
								"sprinkle", "square", "squash", "squeak", "squeeze", "squirrel", "stable", "stack", "stage", "stair", "stall", "stamp", "stand", 
								"star", "stare", "start", "starve", "state", "statement", "station", "stay", "steak", "steal", "steam", "steamboat", "steamer", 
								"steel", "steep", "steeple", "steer", "stem", "step", "stepping", "stick", "sticky", "stiff", "still", "stillness", "sting", 
								"stir", "stitch", "stock", "stocking", "stole", "stomach", "stone", "stood", "stool", "stoop", "stop", "stopped", "stopping", 
								"store", "stories", "stork", "storm", "stormy", "story", "stove", "straight", "strange", "stranger", "strap", "straw", 
								"strawberry", "stream", "street", "stretch", "string", "strip", "stripes", "strong", "structure", "stuck", "study", "stuff", 
								"stump", "stung", "subject", "substance", "such", "suck", "sudden", "suffer", "sugar", "suggestion", "suit", "sum", "summer", 
								"sun", "sunflower", "sung", "sunk", "sunlight", "sunny", "sunrise", "sunset", "sunshine", "supper", "support", "suppose", 
								"sure", "surely", "surface", "surprise", "swallow", "swam", "swamp", "swan", "swat", "swear", "sweat", "sweater", "sweep", 
								"sweet", "sweetheart", "sweetness", "swell", "swept", "swift", "swim", "swimming", "swing", "switch", "sword", "swore", "system", 
								"table", "tablecloth", "tablespoon", "tablet", "tack", "tag", "tail", "tailor", "take", "taken", "taking", "tale", "talk", 
								"talker", "tall", "tame", "tan", "tank", "tap", "tape", "tar", "tardy", "task", "taste", "taught", "tax", "tea", "teach", 
								"teacher", "teaching", "team", "tear", "tease", "teaspoon", "teeth", "telephone", "tell", "temper", "ten", "tendency", "tennis", 
								"tent", "term", "terrible", "test", "than", "thank", "thankful", "thanks", "that", "that's", "the", "theater", "thee", "their", 
								"them", "then", "theory", "there", "these", "they", "they'd", "they'll", "they're", "they've", "thick", "thief", "thimble", 
								"thin", "thing", "think", "third", "thirsty", "thirteen", "thirty", "this", "thorn", "those", "though", "thought", "thousand", 
								"thread", "three", "threw", "throat", "throne", "through", "throw", "thrown", "thumb", "thunder", "thy", "tick", "ticket", 
								"tickle", "tie", "tiger", "tight", "till", "time", "tin", "tinkle", "tiny", "tip", "tiptoe", "tire", "tired", "title", "to", 
								"toad", "toadstool", "toast", "tobacco", "today", "toe", "together", "toilet", "told", "tomato", "tomorrow", "ton", "tone", 
								"tongue", "tonight", "too", "took", "tool", "toot", "tooth", "toothbrush", "toothpick", "top", "tore", "torn", "toss", "touch", 
								"tow", "toward", "towards", "towel", "tower", "town", "toy", "trace", "track", "trade", "train", "tramp", "transport", "trap", 
								"tray", "treasure", "treat", "tree", "trick", "tricycle", "tried", "trim", "trip", "trolley", "trouble", "trousers", "truck", 
								"true", "truly", "trunk", "trust", "truth", "try", "tub", "tug", "tulip", "tumble", "tune", "tunnel", "turkey", "turn", "turtle", 
								"twelve", "twenty", "twice", "twig", "twin", "twist", "two", "ugly", "umbrella", "uncle", "under", "understand", "underwear", 
								"undress", "unfair", "unfinished", "unfold", "unfriendly", "unhappy", "unhurt", "uniform", "unit", "unkind", "unknown", "unless", 
								"unpleasant", "until", "unwilling", "up", "upon", "upper", "upset", "upside", "upstairs", "uptown", "upward", "us", "use", "used", 
								"useful", "valentine", "valley", "valuable", "value", "vase", "vegetable", "velvet", "verse", "very", "vessel", "victory", "view", 
								"village", "vine", "violent", "violet", "visit", "visitor", "voice", "vote", "wag", "wagon", "waist", "wait", "waiting", "wake", 
								"waken", "walk", "wall", "walnut", "want", "war", "warm", "warn", "was", "wash", "washer", "washtub", "wasn't", "waste", "watch", 
								"watchman", "water", "watermelon", "waterproof", "wave", "wax", "way", "wayside", "we", "we'd", "we'll", "we're", "we've", "weak", 
								"weaken", "weakness", "wealth", "weapon", "wear", "weary", "weather", "weave", "web", "wedding", "wee", "weed", "week", "weep", 
								"weigh", "weight", "welcome", "well", "went", "were", "west", "western", "wet", "whale", "what", "what's", "wheat", "wheel", 
								"when", "whenever", "where", "which", "while", "whip", "whipped", "whirl", "whiskey", "whisky", "whisper", "whistle", "white", 
								"who", "who'd", "who'll", "who's", "whole", "whom", "whose", "why", "wicked", "wide", "wife", "wiggle", "wild", "wildcat", 
								"will", "willing", "willow", "win", "wind", "windmill", "window", "windy", "wine", "wing", "wink", "winner", "winter", "wipe", 
								"wire", "wise", "wish", "wit", "witch", "with", "without", "woke", "wolf", "woman", "women", "won", "won't", "wonder", 
								"wonderful", "wood", "wooden", "woodpecker", "woods", "wool", "woolen", "word", "wore", "work", "worker", "workman", "world", 
								"worm", "worn", "worry", "worse", "worst", "worth", "would", "wouldn't", "wound", "wove", "wrap", "wrapped", "wreck", "wren", 
								"wring", "write", "writing", "written", "wrong", "wrote", "wrung", "yard", "yarn", "year", "yell", "yellow", "yes", "yesterday", 
								"yet", "yolk", "yonder", "you", "you'd", "you'll", "you're", "you've", "young", "youngster", "your", "yours", "yourself", 
								"yourselves", "youth",};
						ArrayList<String> simpleWordsList = new ArrayList<String>();
						for (int i=0; i<parts.length; i++){
							simpleWordsList.add(simpleList[i]);
						}
						
						ExamplePairReader reader = new ExamplePairReader(PARSED, ALIGN);
//						BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))); // typical file name: "sub.simple.first100"
						app.noContextpr = new PrintWriter(new FileOutputStream(new File("NoContextGivenIDs")));
						app.noTargetpr = new PrintWriter(new FileOutputStream(new File("NoTargetGivenIDs")));
						app.contextpr = new PrintWriter(new FileOutputStream(new File("ContextGivenIDs")));

//						String input = in.readLine();
						for( int k = 0; k <25; k++ ){ // for counted input
//						while ( input != null ){ // for text input
//						while ( reader.hasNext() ){ // for ExamplePairReader input	
//							String sampleSentence = "";
//							String firstPart = "";
//							String focus = "";
//							String secondPart = "";
//
//							StringTokenizer splitter = new StringTokenizer(input, "\t");
//							String inputSentence = splitter.nextToken();
//							int index = Integer.parseInt(splitter.nextToken());
//							StringTokenizer sentenceSplitter = new StringTokenizer(inputSentence);
//							String word = sentenceSplitter.nextToken();
//
//							for (int i = 0; sentenceSplitter.hasMoreTokens(); i++){
//								if (i < index)
//									firstPart += word + " ";
//								if (i > index)
//									secondPart += word + " ";
//
//								else
//									focus = word +" ";
//
//
//								sampleSentence += word + " ";
//								word = sentenceSplitter.nextToken();
//							}
//
//							sampleSentence = sampleSentence +".";

//							app.createContextGivenSurvey(firstPart, focus, secondPart);
//							app.createNoTargetGivenSurvey(firstPart, focus, secondPart);
//							app.createNoContextGivenSurvey(firstPart, focus, secondPart);

							ExamplePair p = reader.next();
							Alignment align = p.getAlignment();
							ArrayList<Word> normalWords = p.getNormal().getWords();
							ArrayList<Word> simpleWords = p.getSimple().getWords();
							
							for( AlignPair pair: align){
								int n = pair.getNormalIndex();
								int s = pair.getSimpleIndex();
								Word normal = normalWords.get(n);
								Word simple = simpleWords.get(s);
								boolean diffWords = !normal.getWord().toLowerCase().equals( simple.getWord().toLowerCase() );
								boolean normWordSimplePOS = pos.contains( normal.getPos() );
								boolean posEqual = normal.getPos().equals( simple.getPos() );
								boolean normalIsAlreadySimple = simpleWordsList.contains( normal.getWord() );
								
								
								if( diffWords && normWordSimplePOS && posEqual && !normalIsAlreadySimple){
									String firstPart = "";
									String target = normal.getWord() + " ";
									String secondPart = "";
									
									for ( int i = 0; i < normalWords.size(); i++ ){
										if ( i < n ){
											firstPart += normalWords.get(i).getWord();
											firstPart += " ";
										}
										if ( i > n ){
											secondPart += normalWords.get(i).getWord();
											secondPart += " ";
										}
									}

									System.out.println(firstPart + target + secondPart);
//									app.createContextGivenSurvey(firstPart, target, secondPart);
//									app.createNoTargetGivenSurvey(firstPart, target, secondPart);
//									app.createNoContextGivenSurvey(firstPart, target, secondPart);
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