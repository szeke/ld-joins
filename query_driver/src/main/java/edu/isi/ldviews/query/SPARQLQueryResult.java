package edu.isi.ldviews.query;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
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
	private QueryType queryType;
	private long start;
	private long stop;
	private List<CSVRecord> records = new LinkedList<CSVRecord>();
	public SPARQLQueryResult(String responseBody, long start, long stop,QueryType queryType) throws IOException{
		this.start = start;
		this.stop = stop;
			parser = CSVParser.parse(responseBody, CSVFormat.DEFAULT.withHeader());
			this.queryType = queryType;
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
				return null;
			}
			int randomIndex = rand.nextInt(records.size());
		CSVRecord record =records.get(randomIndex);
		
		String result = record.get("category");
		Integer count = Integer.parseInt(record.get("count"));
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
		catch(java.lang.IllegalArgumentException e)
		{
			
			return null;
		}
	}

	@Override
	public JSONArray getAnchorsFromResults(String field) {
		JSONArray anchorsByResults = new JSONArray();
		Set<String> uniqueURIs = new HashSet<String>();
		if(records.isEmpty())
		{
			try {
				for(CSVRecord record : parser.getRecords())
				{
					records.add(record);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(CSVRecord record : records)
		{
			uniqueURIs.add(record.get(0));
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

	@Override
	public QueryResultStatistics getQueryResultStatistics() {
		return new QueryResultStatistics(queryType, getQueryTime());
	}

}
