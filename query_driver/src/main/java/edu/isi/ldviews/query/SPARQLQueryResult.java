package edu.isi.ldviews.query;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class SPARQLQueryResult implements QueryResult {

	private String[] responses;
	public SPARQLQueryResult(String responseBody) {
		this.responses = responseBody.split("\n");
	}

	public JSONObject getFacetValue(JSONObject queryType, Random rand) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray getAnchorsFromResults(String field) {
		JSONArray anchorsByResults = new JSONArray();
		Set<String> uniqueURIs = new HashSet<String>();
		for(int i = 1; i < responses.length; i++)
		{
			uniqueURIs.add((responses[i].substring(1,responses[i].indexOf(',')-1)));;
		}
		for(String uri : uniqueURIs)
		{
			JSONArray anchors= new JSONArray();
			JSONObject resultWithValues = new JSONObject();
			resultWithValues.put("uri", uri);
			anchors.put(uri);
			resultWithValues.put("anchors", anchors);
			anchorsByResults.put(resultWithValues);
		}
		return anchorsByResults;
	}

}
