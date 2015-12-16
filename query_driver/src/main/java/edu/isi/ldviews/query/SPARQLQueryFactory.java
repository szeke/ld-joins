package edu.isi.ldviews.query;

import org.json.JSONObject;

public class SPARQLQueryFactory implements QueryFactory {

	public Query generateQuery(JSONObject queryTypeSpec) {
		Query query = new SPARQLQuery();
		query.setName(queryTypeSpec.getString("name"));
		query.addType(queryTypeSpec);
		query.addKeywords(queryTypeSpec.getJSONObject("query"));
		query.addFields(queryTypeSpec.getJSONObject("results").getJSONArray("fields"));
		query.addFacets(queryTypeSpec.getJSONArray("facets"));
		return query;
	}

	@Override
	public Query generateAggregateQuery(JSONObject queryTypeSpec, JSONObject anchor) {

		Query query = new SPARQLQuery();
		query.setName(queryTypeSpec.getString("name"));
		return query;
		
	}

}
