package edu.isi.ldviews.query;

import org.json.JSONObject;

public class ESQueryFactory implements QueryFactory{

	public Query generateQuery(JSONObject queryTypeSpec) {
		Query query = new ESQuery(QueryType.SEARCH);
		query.setName(queryTypeSpec.getString("name"));
		query.addType(queryTypeSpec);
		query.addKeywords(queryTypeSpec.getJSONObject("query"));
		query.addFields(queryTypeSpec.getJSONObject("results").getJSONArray("fields"));
		return query;
	}

	@Override
	public Query generateAggregateQuery(JSONObject queryTypeSpec, JSONObject anchor) {
		Query query = new ESQuery(QueryType.AGGREGATE);
		query.setName(queryTypeSpec.getString("name"));
		query.addAggregations(queryTypeSpec, anchor);
		return query;
	}

	@Override
	public Query generateFacetQuery(JSONObject queryTypeSpec, int facetIndex) {
		Query query = new ESQuery(QueryType.FACET);
		query.setName(queryTypeSpec.getString("name"));
		query.addType(queryTypeSpec);
		query.addKeywords(queryTypeSpec.getJSONObject("query"));
		query.addFacets(queryTypeSpec.getJSONArray("facets"), facetIndex);
		return query;
	}

	@Override
	public Query generateMissingFacetQuery(JSONObject queryTypeSpec, int j) {
		Query query = new ESQuery(QueryType.FACET_MISSING);
		query.setName(queryTypeSpec.getString("name"));
		query.addType(queryTypeSpec);
		query.addKeywords(queryTypeSpec.getJSONObject("query"));
		query.addMissingFacet(queryTypeSpec.getJSONArray("facets"), j);
		return query;
	}

	
}
