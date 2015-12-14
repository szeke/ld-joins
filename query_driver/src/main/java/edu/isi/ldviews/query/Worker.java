package edu.isi.ldviews.query;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

public class Worker implements Callable<String>{

	private QueryFactory queryFactory;
	private JSONObject querySpec;
	private Keywords keywords;
	private Random rand;
	private int keywordCount = 2;
	private double probabilitySearchSatisfied = 0.9;
	private QueryExecutor queryExecutor;
	private int maxQueryDepth = 4;
	
	public Worker(QueryExecutor queryExecutor, QueryFactory queryFactory, JSONObject querySpec, Keywords keywords, long seed)
	{
		this.queryExecutor = queryExecutor;
		this.queryFactory = queryFactory;
		//each worker is going to modify the spec to insert filters and stuff
		this.querySpec = new JSONObject(querySpec.toString());
		this.keywords = keywords;
		this.rand = new Random(seed);
	}

	public String call() throws Exception {

		Set<String> selectedKeywords = new HashSet<String>();
		while(selectedKeywords.size() < keywordCount && selectedKeywords.size() <= keywords.count())
		{
			selectedKeywords.add(keywords.getKeyword(rand));
		}
		
		JSONArray queryTypes = querySpec.getJSONArray("types");
		JSONObject queryType = queryTypes.getJSONObject(rand.nextInt(queryTypes.length()));
		JSONObject queryKeywordSpec = queryType.getJSONObject("query");
		JSONArray queryKeywords = queryKeywordSpec.getJSONArray("keywords");
		for(String selectedKeyword : selectedKeywords)
		{
			queryKeywords.put(selectedKeyword);
		}
			
		Query query = queryFactory.generateQuery(queryType);
		do{
			Future<QueryResult> queryResultFuture = queryExecutor.execute(query);
			QueryResult queryResult = queryResultFuture.get(10, TimeUnit.SECONDS);
			JSONObject facetValue = queryResult.getFacetValue(queryType, rand);
			query = queryFactory.generateQuery(applyFilter(queryType, facetValue));
		}while(rand.nextDouble() < probabilitySearchSatisfied);
		queryExecutor.shutdown();
		return null;
	}

	private JSONObject applyFilter(JSONObject queryType, JSONObject facetValue) {
		//top level filter
		if(!queryType.has("userfilter"))
		{
			queryType.put("userfilter", new JSONArray());
		}
		JSONArray queryUserFilters = queryType.getJSONArray("userfilter");
		queryUserFilters.put(facetValue);
		
		// agg filters
		
		
		// facet filters
		JSONArray facets = queryType.getJSONArray("facets");
		for(int i = 0; i < facets.length(); i++)
		{
			JSONObject facet = facets.getJSONObject(i);
			if(facet.getString("name").compareTo(facetValue.getString("name"))==0)
			{
				continue;
			}
			if(!facet.has("userfilter")){
				facet.put("userfilter", new JSONArray());
			}
			JSONArray facetUserFilters = facet.getJSONArray("userfilter");
			facetUserFilters.put(facetValue);
		}
		return queryType;
	}
}
