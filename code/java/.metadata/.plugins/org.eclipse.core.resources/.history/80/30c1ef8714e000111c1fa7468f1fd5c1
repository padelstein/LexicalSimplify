package simplify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.AssignmentStatus;
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
	public double entropy = 0;
	// public double entropyy25 = 0;
	// public double entropy50 = 0;

	public OurHIT(String hitID, Map<String, String[]> wordToSense)
	{
		hitID.trim();
		amazonHIT = service.getHIT(hitID);
		typeID = amazonHIT.getHITTypeId();
		ID = hitID;

		calcAnswers();
		calcTargetWord(wordToSense);
		calcEntropy();

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
		calcEntropy();
	}

	public void calcEntropy()
	{
		for ( Integer value : frequencyCounter.values() )
		{
			double p = value/(double)answers.size();
			double log = Math.log(p)/Math.log(answers.size());
			entropy += -1.0 * p * log;
		}
	}

	public void calcAnswers()
	{
		assignments = service.getAllAssignmentsForHIT
		(
				amazonHIT.getHITId()
		);

		for (Assignment ass: assignments)
		{
			if ( ass.getAssignmentStatus().equals(AssignmentStatus.Approved) )
			{
				int textStart = ass.getAnswer().indexOf("<FreeText>");
				int textEnd = ass.getAnswer().indexOf("</FreeText>");
				String answerText = ass.getAnswer().substring(textStart + 10, textEnd).toLowerCase();
				answerText = answerText.trim();
				answers.add(answerText);

				if (frequencyCounter.containsKey(answerText))
					frequencyCounter.put(answerText, frequencyCounter.get(answerText) + 1);
				else
					frequencyCounter.put(answerText, 1);
			}
		}
		for (String text: frequencyCounter.keySet())
		{
			int freq = frequencyCounter.get(text);
			if (freq > highestFreq)
			{
				highestFreq = freq;
			}
		}
	}

	public void calcTargetWord(Map<String, String[]> wordToSense)
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

	public String toString(){
		String out = ID + '\t' + typeID + '\t' + targetWord;
		for(String answer: answers)
			out += '\t' + answer;
		return out;
	}
}