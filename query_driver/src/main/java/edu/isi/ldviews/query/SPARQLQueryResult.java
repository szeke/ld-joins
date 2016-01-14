package edu.isi.ldviews.query;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.json.JSONObject;

public class SPARQLQueryResult implements QueryResult {

	private CSVParser parser = null;
	private long start;
	private long stop;
	
	public SPARQLQueryResult(String responseBody, long start, long stop) throws IOException{
		this.start = start;
		this.stop = stop;
			parser = CSVParser.parse(responseBody, CSVFormat.DEFAULT.withHeader());
		
	}

	public long getQueryTime()
	{
		return stop - start;
	}
	public JSONObject getFacetValue(JSONObject facetSpec, Random rand) {
		
		try
		{
			List<CSVRecord> records = parser.getRecords();
			if(records.size() == 0)
			{
				return null;
			}
			if(records.size() == 1 && records.get(0).get(0).isEmpty())
			{
				System.out.println("skipping !");
				return null;
			}
			int randomIndex = rand.nextInt(records.size());
		CSVRecord record =records.get(randomIndex);
		
		String result = record.get("category");
		JSONObject facetToFilterOn = new JSONObject();
		facetToFilterOn.put("path", facetSpec.getString("path"));
		facetToFilterOn.put("term", result);
		facetToFilterOn.put("name", facetSpec.getString("name"));
		return facetToFilterOn;
		}
		catch(IOException e)
		{
			return null;
		}
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
