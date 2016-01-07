package edu.isi.ldviews.query;

import java.util.Random;

import org.json.JSONArray;

public class Keywords {

	private JSONArray keywords;
	public Keywords (JSONArray keywords)
	{
		this.keywords = keywords;
	}
	
	public String getKeyword(Random rand)
	{
		int randomKeywordIndex = rand.nextInt(keywords.length());
		String chosenKeyword = keywords.getString(randomKeywordIndex);
		System.out.println("chose " + chosenKeyword + " at " + randomKeywordIndex);
		return chosenKeyword;
	}
	
	public int count(){
		return keywords.length();
	}
}
