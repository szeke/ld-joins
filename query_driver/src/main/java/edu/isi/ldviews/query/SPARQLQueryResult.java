package edu.isi.ldviews.query;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
	public JSONArray getAnchorsFromResults(String path, JSONArray resultsFieldsSpec) {
		JSONArray anchorsByResults = new JSONArray();
		Set<String> uniqueURIs = new HashSet<String>();
		Map<String, JSONObject> uniqueResultsWithValues = new HashMap<String, JSONObject>();
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
		String field =null;
		int fieldIndex = -1;
		if(path.compareTo("uri") == 0)
		{
			fieldIndex = 0;
		}
		else
		{
		for (int i = 0; i < resultsFieldsSpec.length(); i++)
		{
			JSONObject fieldSpec = resultsFieldsSpec.getJSONObject(i);
			
			if(fieldSpec.getString("path").compareTo(path) ==0)
			{
				field = fieldSpec.getString("name");
				Map<String,Integer> headerMap = parser.getHeaderMap();
				fieldIndex =headerMap.get(field);
				break;
			}
		}
		}
		for(CSVRecord record : records)
		{
			String uri = record.get(0);
			JSONObject resultWithValues = null;
			JSONArray anchors= null;
			if(!uniqueResultsWithValues.containsKey(uri))
			{
				resultWithValues = new JSONObject();
				resultWithValues.put("uri", uri);
				anchors =  new JSONArray();
				resultWithValues.put("anchors", anchors);
				uniqueResultsWithValues.put(uri, resultWithValues);
				
			}
			else
			{
				resultWithValues = uniqueResultsWithValues.get(uri);
				anchors = resultWithValues.getJSONArray("anchors");
			}
			String anchorValue = record.get(fieldIndex);
			boolean duplicated = false;
			for(int i = 0; i < anchors.length(); i++)
			{
				if(anchorValue.compareTo(anchors.getString(i))== 0)
				{
					duplicated = true;
					break;
				}
			}
			if(!duplicated)
			{
				anchors.put(anchorValue);
			}
			
		}
		return new JSONArray(uniqueResultsWithValues.values());
	}

	@Override
	public QueryResultStatistics getQueryResultStatistics() {
		return new QueryResultStatistics(queryType, getQueryTime());
	}

}
