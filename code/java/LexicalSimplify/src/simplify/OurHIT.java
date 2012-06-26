package simplify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;

public class OurHIT
{
	private RequesterService service = new RequesterService(new PropertiesClientConfig());
	public HIT amazonHIT = null;
	public String ID = null;
	public String typeID = null;
	public ArrayList<String> answers = new ArrayList<String>();
	public Assignment[] assignments = null;
	public String targetWord = "";
	public Map<String, Integer> frequencyCounter = new HashMap<String, Integer>();
	public int highestFreq = 0;
	
	public OurHIT(String hitID, Map<String, String[]> wordToSense)
	{
		hitID.trim();
		amazonHIT = service.getHIT(hitID);
		typeID = amazonHIT.getHITTypeId();
		ID = hitID;
	
		getAnswers();
		getTargetWord(wordToSense);

	}
	
	public OurHIT(String hitID, String typeID, String word, ArrayList<String> answerArray)
	{
		ID = hitID;
		this.typeID = typeID;
		targetWord = word;
		answers = answerArray;
		
		for (String answer: answers)
		{
			if (frequencyCounter.containsKey(answer))
				frequencyCounter.put(answer, frequencyCounter.get(answer) + 1);
			else
				frequencyCounter.put(answer, 1);
		}
		
		for (String text: frequencyCounter.keySet()){
			int freq = frequencyCounter.get(text);
			if (freq > highestFreq){
				highestFreq = freq;
			}
		}
	}
	
	public void getAnswers()
	{
		assignments = service.getAllAssignmentsForHIT
		(
				amazonHIT.getHITId()
		);
	
		for (Assignment ass: assignments)
		{
			int textStart = ass.getAnswer().indexOf("<FreeText>");
			int textEnd = ass.getAnswer().indexOf("</FreeText>");
			String answerText = ass.getAnswer().substring(textStart + 10, textEnd).toLowerCase();
			answerText.trim();
			if ( !answerText.equals("") )
			{
				answers.add(answerText);
			}
			
			if (frequencyCounter.containsKey(answerText))
				frequencyCounter.put(answerText, frequencyCounter.get(answerText) + 1);
			else
				frequencyCounter.put(answerText, 1);
		}
		
		for (String text: frequencyCounter.keySet()){
			int freq = frequencyCounter.get(text);
			if (freq > highestFreq){
				highestFreq = freq;
			}
		}
	}

	public void getTargetWord(Map<String, String[]> wordToSense)
	{
		String question = amazonHIT.getQuestion();
		int wordStart = question.substring(question.indexOf("<b", question.indexOf("Task:") + 35)).indexOf(">") + question.indexOf("<b", question.indexOf("Task:") + 35) + 1;
		int wordEnd = question.indexOf("</b>", question.indexOf("Task:") + 17 );
		 
		if (wordStart < 0 || wordEnd <0)
		{
			for (String key: wordToSense.keySet())
			{
				String firstHalf = wordToSense.get(key)[0];
				if (question.indexOf(firstHalf) > 0)
				{
					targetWord = key;
				}
			}
		} else
			targetWord = question.substring(wordStart, wordEnd);

		targetWord = targetWord.trim();
	}

	public ArrayList<String> getHITAnswers()
	{
		return answers;
	}

	public Assignment[] getHITassignments()
	{
		return assignments;
	}
	
	public String getHITtype()
	{
		return amazonHIT.getHITTypeId();
	}

	public Map<String, Integer> getFrequencyCounter()
	{
		return frequencyCounter;
	}
	
	public String toString(){
		String out = ID + '\t' + typeID + '\t' + targetWord + '\t';
		for(String answer: answers)
			out += answer + " ";
		return out;
	}
}
