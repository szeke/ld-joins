package edu.isi.ldviews.query;

import java.util.Random;

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
		JSONArray facetsSpec = queryTypeSpec.getJSONArray("facets");
		JSONObject facetSpec = facetsSpec.getJSONObject(rand.nextInt(facetsSpec.length()));
		String facetNameSpec = facetSpec.getString("name");
		String facetName = facetNameSpec+ "_facet";
		JSONObject facetResults = json.getJSONObject("aggregations").getJSONObject(facetName);
		JSONArray buckets = facetResults.getJSONArray("buckets");
		String facetBucketKey = buckets.getJSONObject(rand.nextInt(buckets.length())).getString("key");
		JSONObject facetToFilterOn = new JSONObject();
		facetToFilterOn.put("path", facetSpec.getString("path"));
		facetToFilterOn.put("term", facetBucketKey);
		facetToFilterOn.put("name", facetNameSpec);
		return facetToFilterOn;
	}
}
