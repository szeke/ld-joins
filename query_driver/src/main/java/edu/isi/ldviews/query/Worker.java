package edu.isi.ldviews.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker implements Callable<WorkerResultSummary>{

	private static Logger LOG = LoggerFactory.getLogger(Worker.class);
	private QueryFactory queryFactory;
	private JSONObject querySpec;
	private Keywords keywords;
	private Random rand;
	private long seed; 
	private int keywordCount = 2;
	private double probabilitySearchSatisfied = 0.9;
	private QueryExecutor queryExecutor;
	private int maxQueryDepth = 3;
	private RandomDataGenerator rdg;
	private double queryRate;
	private int numberoftraces;
	
	public Worker(QueryExecutor queryExecutor, QueryFactory queryFactory, JSONObject querySpec, Keywords keywords, long seed, double queryRate, int numberoftraces)
	{
		this.queryExecutor = queryExecutor;
		this.queryFactory = queryFactory;
		//each worker is going to modify the spec to insert filters and stuff
		this.querySpec = new JSONObject(querySpec.toString());
		this.keywords = keywords;
		this.rand = new Random(seed);
		// Share the same seed.  Doesn't really matter
		rdg = new RandomDataGenerator();
		rdg.reSeed(seed);
		this.queryRate = queryRate;
		this.seed = seed;
		this.numberoftraces = numberoftraces;
		
	}

	public WorkerResultSummary call() throws Exception {

		LOG.info("Worker " + seed + " is starting");
		List<QueryResultStatistics> queryResultStatistics = new LinkedList<QueryResultStatistics>();
		for(int tracenumber = 0; tracenumber < numberoftraces; tracenumber++)
		{
			long traceStart = System.currentTimeMillis();
		JSONObject querySpec =  new JSONObject(this.querySpec.toString());
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
		int queryDepth = 0;
		try
		{
		do{
			double waitTime = rdg.nextExponential(1.0/ queryRate);
			Thread.sleep((long) (waitTime *1000));
			Future<QueryResult> queryResultFuture = queryExecutor.execute(query);
			queryResultStatistics.add(new QueryResultStatistics(QueryType.USERDELAY, (long)(waitTime *1000)));
			
			
			JSONArray facetsSpec = queryType.getJSONArray("facets");
			List<Future<QueryResult>> facetResultFutures = new LinkedList<Future<QueryResult>>();
			for(int j = 0; j < facetsSpec.length(); j++)
			{
				Query facetQuery = queryFactory.generateFacetQuery(queryType, j);
				facetResultFutures.add(queryExecutor.execute(facetQuery));
			}
				
			QueryResult queryResult = queryResultFuture.get(100, TimeUnit.SECONDS);
			queryResultStatistics.add(queryResult.getQueryResultStatistics());
			List<Future<QueryResult>> aggregationResultFutures = new LinkedList<Future<QueryResult>>();
		
			JSONArray aggregationsSpec = queryType.getJSONObject("results").getJSONArray("aggregations");
			for(int j = 0; j < aggregationsSpec.length(); j++)
			{
				JSONObject aggregationSpec = aggregationsSpec.getJSONObject(j);
				String anchorPath = aggregationSpec.getString("anchor_path");
				JSONArray anchors = queryResult.getAnchorsFromResults(anchorPath);
						
				for(int i = 0; i < anchors.length(); i++ )
				{
					JSONObject anchor = anchors.getJSONObject(i);
					Query aggregationQuery = queryFactory.generateAggregateQuery(aggregationSpec, anchor);
					aggregationResultFutures.add(queryExecutor.execute(aggregationQuery));
				}
			}
				
		
			// the results should be populated in the same order as the facet spec
			ArrayList<QueryResult> facetResults = new ArrayList<QueryResult>(facetResultFutures.size());
			for(Future<QueryResult> facetResultFuture : facetResultFutures)
			{
				QueryResult facetQueryResult = facetResultFuture.get(100, TimeUnit.SECONDS);
				facetResults.add(facetQueryResult);
				queryResultStatistics.add(facetQueryResult.getQueryResultStatistics());
			}
			for(Future<QueryResult> aggregationResultFuture : aggregationResultFutures)
			{
				queryResultStatistics.add(aggregationResultFuture.get(100, TimeUnit.SECONDS).getQueryResultStatistics());
			}
			JSONObject facetValue = getFacetValue(queryType, rand, facetResults);
			if(facetValue == null)
			{
				break;
			}
			query = queryFactory.generateQuery(applyFilter(queryType, facetValue));
			queryDepth++;
		}while(queryDepth < maxQueryDepth && rand.nextDouble() < probabilitySearchSatisfied);
		  QueryResultStatistics qrs = new QueryResultStatistics(QueryType.COMBINED, System.currentTimeMillis() - traceStart);
		  queryResultStatistics.add(qrs);
		}
		catch(Exception e)
		{
			LOG.error("Worker " + seed + " unable to complete queries.", e);
			break;
		}
		}
		LOG.info("Worker "+seed+"is finishing");
		queryExecutor.shutdown();
		WorkerResultSummary workerResultSummary = new WorkerResultSummary(seed, queryResultStatistics);
		
		return workerResultSummary;
		
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
	
	public JSONObject getFacetValue(JSONObject queryTypeSpec, Random rand, ArrayList<QueryResult> facetQueryResults) {
		Set<Integer> facetsEvaluated = new HashSet<Integer>();
		int emptyFacetResults = 0;
		JSONArray facetsSpec = queryTypeSpec.getJSONArray("facets");
		boolean found = false;
		JSONObject facetSpec = null;
		JSONArray userfilters = null;
		if(queryTypeSpec.has("userfilter"))
		{
			userfilters = queryTypeSpec.getJSONArray("userfilter");
		}
		while(facetsEvaluated.size() < facetsSpec.length() && !found)
		{
			Integer facetIndexToEvaluate = rand.nextInt(facetsSpec.length());
			//System.out.println("random " + facetIndexToEvaluate);
			facetSpec = facetsSpec.getJSONObject(facetIndexToEvaluate);
			
			if(!facetsEvaluated.add(facetIndexToEvaluate) || userfilteralreadychosen(userfilters, facetSpec))
			{
				continue;
			}
		
			QueryResult facetQueryResult = facetQueryResults.get(facetIndexToEvaluate);
			JSONObject facetValue = facetQueryResult.getFacetValue(facetSpec, rand);
			if(facetValue == null)
			{
				emptyFacetResults++;
				continue;
			}
			

			found = true;
			
			return facetValue;
		}
		LOG.trace("Worker " + seed + " was unable to select a facet from " + facetsSpec);
		LOG.trace("Worker " + seed + " had " + emptyFacetResults + " empty facets");
		
			return null;
		
		
	}

	private boolean userfilteralreadychosen(JSONArray userfilters,
			JSONObject facetSpec) {
		if(userfilters == null)
		{
			return false;
		}
		String facetspecname = facetSpec.getString("name");
		for(int i = 0; i < userfilters.length(); i++)
		{
			JSONObject userfilter = userfilters.getJSONObject(i);
			if(facetspecname.compareTo(userfilter.getString("name"))== 0)
			{
				return true;
			}
		}
		
		return false;
	}
}
