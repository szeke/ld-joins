package edu.isi.ldviews.query;

import org.json.JSONArray;
import org.json.JSONObject;

public interface Query {
	String getName();
	String getType();
	void setName(String name);
	void addType(String type);
	void addKeywords(JSONObject queryKeywordSpec);
	void addFields(JSONArray queryFieldsSpec);
	void addFacets(JSONArray queryFacetsSpec);
	void addAggregations(JSONArray queryAggregationsSpec);
	void applyFilter(JSONObject facetValueFilter);
}
