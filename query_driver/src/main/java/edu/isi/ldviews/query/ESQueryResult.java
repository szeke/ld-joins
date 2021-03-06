package edu.isi.ldviews.query;

import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESQueryResult implements QueryResult {

	private static final Logger LOG = LoggerFactory.getLogger(ESQueryResult.class);
	private JSONObject json;
	private long start;
	private long stop;
	private QueryType queryType;
	
	public ESQueryResult(String jsonResponse, long start, long stop, QueryType queryType)
	{
		this.start = start;
		this.stop = stop;
		LOG.trace(jsonResponse);
		this.json = new JSONObject(jsonResponse);
		this.queryType = queryType;
	}

	public JSONObject getFacetValue(JSONObject facetSpec, Random rand) {
	
		JSONObject facetResults = null;
		String facetName = null;
		String facetNameSpec = null;
	
		if(!this.json.has("aggregations"))
		{
			throw new NullPointerException("No aggregations in query response: " + json.toString());
		}
			facetNameSpec = facetSpec.getString("name");
			facetName = facetNameSpec+ "_facet";
			facetResults  = json.getJSONObject("aggregations").getJSONObject(facetName);
			
		if(facetSpec.has("userfilter") && facetSpec.getJSONArray("userfilter").length() > 0)
		{
			facetResults = facetResults.getJSONObject(facetName);
		}
		JSONArray buckets = facetResults.getJSONArray("buckets");
		if(buckets.length() <= 0)
		{
			return null;
		}
		int randomIndex = rand.nextInt(buckets.length()); 
		String facetBucketKey = buckets.getJSONObject(randomIndex).getString("key");
		JSONObject facetToFilterOn = new JSONObject();
		facetToFilterOn.put("path", facetSpec.getString("path"));
		facetToFilterOn.put("term", facetBucketKey);
		facetToFilterOn.put("name", facetNameSpec);

		return facetToFilterOn;
		
		
	}

	@Override
	public JSONArray getAnchorsFromResults(String path, JSONArray fieldsSpec) {
		if(!this.json.has("hits"))
		{
			return null;
		}
		JSONObject outerHits = this.json.getJSONObject("hits");
		JSONArray hits = outerHits.getJSONArray("hits");
		JSONArray anchorsByResult = new JSONArray();
		String[] fields = JSONCollector.splitPath(path);
		for(int i = 0; i < hits.length(); i++)
		{
			JSONArray anchors = new JSONArray();
			JSONObject source = hits.getJSONObject(i).getJSONObject("_source");
			String uri = hits.getJSONObject(i).getString("_id");
			JSONCollector.collectJSONObject(source,fields, 0, anchors);
			JSONObject resultWithValues = new JSONObject();
			resultWithValues.put("uri", uri);
			resultWithValues.put("anchors", anchors);
			resultWithValues.put("path", path);
			anchorsByResult.put(resultWithValues);
			
		}
		
		return anchorsByResult;
	}

	@Override
	public QueryResultStatistics getQueryResultStatistics() {
		return new QueryResultStatistics(queryType, getQueryTime());
	}
	public long getQueryTime()
	{
		return stop - start;
	}

}
