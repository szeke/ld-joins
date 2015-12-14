package edu.isi.ldviews.query;

import org.json.JSONArray;
import org.json.JSONObject;

public class ESQuery implements Query {

	JSONObject source = new JSONObject("{\"include\":[]}");
	JSONObject keywordQuery = new JSONObject("{\"query_string\":{\"query\":\"\",\"fields\":[]}}");
//	JSONObject facets = new JSONObject();
	JSONObject aggregations = new JSONObject();
	String type;
	String name;
	public void setName(String name) {
		this.name =name;
	}
	
	public String getName()
	{
		return name;
	}
	public void addType(String type) {
		this.type =type;
	}
	
	public String getType()
	{
		return type;
	}

	public void addFields(JSONArray queryFieldsSpec) {
		JSONArray includeFields = source.getJSONArray("include");
		for(int i = 0; i < queryFieldsSpec.length(); i++)
		{
			JSONObject queryField = queryFieldsSpec.getJSONObject(i);
			includeFields.put(queryField.getString("path"));
		}
	}

	public void addFacets(JSONArray queryFacetsSpec) {
		for(int i = 0; i < queryFacetsSpec.length(); i++)
		{
			JSONObject queryFacetSpec = queryFacetsSpec.getJSONObject(i);
			String queryFacetName = queryFacetSpec.getString("name");
			String queryFacetPath = queryFacetSpec.getString("path");
			if(queryFacetSpec.has("userfilter"))
			{
				JSONObject facetWithFilter = new JSONObject();
				JSONObject filter = new JSONObject();
				JSONArray shouldStatements = new JSONArray();
				JSONArray userFilters = queryFacetSpec.getJSONArray("userfilter");
				for(int j = 0; j < userFilters.length(); j++)
				{
					JSONObject userFilter = userFilters.getJSONObject(j);
					JSONObject termFilter = new JSONObject();
					termFilter.put(userFilter.getString("path"), userFilter.getString("term"));
					JSONObject termFilterWrapper = new JSONObject();
					termFilterWrapper.put("term", termFilter);
					shouldStatements.put(termFilterWrapper);
				}
				JSONObject shouldStatementWrapper = new JSONObject();
				shouldStatementWrapper.put("should", shouldStatements);
				filter.put("bool", shouldStatementWrapper);
				facetWithFilter.put("filter", filter);
				JSONObject nestedAgg = new JSONObject();
				JSONObject termsFacet = new JSONObject();
				termsFacet.put("field", queryFacetPath);
				termsFacet.put("size", 20);
				JSONObject termsFacetWrapper = new JSONObject();
				termsFacetWrapper.put("terms", termsFacet);
				nestedAgg.put(queryFacetName+"_facet", termsFacetWrapper);
				facetWithFilter.put("aggs",nestedAgg);
				aggregations.put(queryFacetName+"_facet", facetWithFilter);
				
			}
			else
			{
				JSONObject facet = new JSONObject();
				JSONObject termsFacet = new JSONObject();
				termsFacet.put("field", queryFacetPath);
				termsFacet.put("size", 20);
				facet.put("terms", termsFacet);
				aggregations.put(queryFacetName + "_facet", facet);
			}
		}
		
	}

	public void addAggregations(JSONArray queryAggregationsSpec) {
		for(int i = 0; i < queryAggregationsSpec.length(); i++)
		{
			JSONObject queryAggregationSpec = queryAggregationsSpec.getJSONObject(i);
			String queryAggregationQuery = queryAggregationSpec.getString("query");
			JSONObject queryAggregation = queryAggregationSpec.getJSONObject("elastic");
			if(queryAggregationSpec.has("userfilter"))
			{
				
			}
			else
			{
				//aggregations.put(queryAggregationQuery, queryAggregation);
			}
		}
		
	}



	public void addKeywords(JSONObject queryKeywordSpec) {

		
		JSONObject namedQuery = keywordQuery.getJSONObject("query_string");
		String namedQueryKeywords = namedQuery.getString("query");
		JSONArray namedQueryFields = namedQuery.getJSONArray("fields");
		JSONArray specKeywords = queryKeywordSpec.getJSONArray("keywords");
		JSONArray specFields = queryKeywordSpec.getJSONArray("fields");
		for(int i = 0; i < specKeywords.length(); i ++)
		{
			namedQueryKeywords += " " + (specKeywords.getString(i));
		}
		namedQuery.put("query", namedQueryKeywords);
		for(int i = 0; i < specFields.length(); i ++)
		{
			namedQueryFields.put(specFields.getString(i));
		}
	}

	@Override 
	public String toString()
	{
		JSONObject query = new JSONObject();
		query.put("_source", source);
		query.put("query", keywordQuery);
		query.put("aggs", aggregations);
		return query.toString(4);
	}

	@Override
	public void applyFilter(JSONObject facetValueFilter) {
		// TODO Auto-generated method stub
		
	}
}
