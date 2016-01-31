package edu.isi.ldviews.query;

import org.json.JSONArray;
import org.json.JSONObject;

public interface Query {
	String getName();
	String getType();
	void setName(String name);
	public void addType(JSONObject querySpec);
	void addKeywords(JSONObject queryKeywordSpec);
	void addFields(JSONArray queryFieldsSpec);
	void addFacets(JSONArray queryFacetsSpec, int facetIndex);
	public void addMissingFacet(JSONArray queryFacetsSpec, int facetIndex);
	void addAggregations(JSONObject queryAggregationsSpec, JSONObject anchor);
	public QueryType getQueryType();
}
