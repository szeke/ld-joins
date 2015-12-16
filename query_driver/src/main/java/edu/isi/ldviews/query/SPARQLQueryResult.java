package edu.isi.ldviews.query;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;

public class SPARQLQueryResult implements QueryResult {

	CSVParser parser = null;
	public SPARQLQueryResult(String responseBody) {
		try {
			parser = CSVParser.parse(responseBody, CSVFormat.DEFAULT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public JSONObject getFacetValue(JSONObject queryType, Random rand) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray getAnchorsFromResults(String field) {
		JSONArray anchorsByResults = new JSONArray();
		Set<String> uniqueURIs = new HashSet<String>();
		try {
			for(CSVRecord record : parser.getRecords())
			{
				record.get(0);
				uniqueURIs.add(record.get(0));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
