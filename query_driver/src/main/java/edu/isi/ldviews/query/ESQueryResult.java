package edu.isi.ldviews.query;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class ESQueryResult implements QueryResult {

	private JSONObject json;

	public ESQueryResult(String jsonResponse)
	{
		System.out.println(jsonResponse);
		this.json = new JSONObject(jsonResponse);
	}

	public JSONObject getFacetValue(JSONObject queryTypeSpec, Random rand) {
		Set<Integer> facetsEvaluated = new HashSet<Integer>();
		
		JSONArray facetsSpec = queryTypeSpec.getJSONArray("facets");
		boolean found = false;
		JSONObject facetSpec = null;
		JSONObject facetResults = null;
		String facetName = null;
		String facetNameSpec = null;
		while(facetsEvaluated.size() < facetsSpec.length() && !found)
		{
			Integer facetIndexToEvaluate = rand.nextInt(facetsSpec.length());
			if(!facetsEvaluated.add(facetIndexToEvaluate))
			{
				facetResults = null;
				continue;
			}
		
			facetSpec = facetsSpec.getJSONObject(facetIndexToEvaluate);
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
			continue;
		}
		String facetBucketKey = buckets.getJSONObject(rand.nextInt(buckets.length())).getString("key");
		JSONObject facetToFilterOn = new JSONObject();
		facetToFilterOn.put("path", facetSpec.getString("path"));
		facetToFilterOn.put("term", facetBucketKey);
		facetToFilterOn.put("name", facetNameSpec);

		found = true;
		System.out.println("User selected: " + facetToFilterOn);
		return facetToFilterOn;
		}
			return new JSONObject();
		
		
	}

	@Override
	public JSONArray getAnchorsFromResults(String path) {
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
}
