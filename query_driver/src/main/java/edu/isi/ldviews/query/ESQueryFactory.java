package edu.isi.ldviews.query;

import org.json.JSONObject;

public class ESQueryFactory implements QueryFactory{

	public Query generateQuery(JSONObject queryTypeSpec) {
		Query query = new ESQuery();
		query.setName(queryTypeSpec.getString("name"));
		query.addType(queryTypeSpec.getString("type"));
		query.addKeywords(queryTypeSpec.getJSONObject("query"));
		query.addFields(queryTypeSpec.getJSONObject("results").getJSONArray("fields"));
		query.addFacets(queryTypeSpec.getJSONArray("facets"));
		query.addAggregations(queryTypeSpec.getJSONObject("results").getJSONArray("aggregations"));
		return query;
	}

	
}