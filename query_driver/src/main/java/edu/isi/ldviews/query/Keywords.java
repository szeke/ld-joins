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
		return keywords.getString(rand.nextInt(keywords.length()));
	}
	
	public int count(){
		return keywords.length();
	}
}
