package edu.isi.ldviews.query;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Worker implements Callable<WorkerResultSummary> {

	private static Logger LOG = LoggerFactory.getLogger(Worker.class);
	private QueryFactory queryFactory;
	private JSONObject querySpec;
	private Keywords keywords;
	private Random rand;
	private long seed;
	private int keywordCount = 2;
	private double probabilitySearchSatisfied = 0.9;
	private long timeout;
	private QueryExecutor queryExecutor;
	private int maxQueryDepth = 3;
	private RandomDataGenerator rdg;
	private double queryRate;
	private int numberoftraces;
	//private List<QueryResultStatistics> queryResultStatistics = new LinkedList<QueryResultStatistics>();
	private Exception queryException = null;
	private boolean timeoutOccurred = false;
	private WorkerResultSummary workerResultSummary;
	private static AtomicInteger concurrentWorkers = new AtomicInteger(0);
	private static AtomicInteger queuedWorkers = new AtomicInteger(0);
	private WorkerTimestampedStatisticsSummary workerTimestampedStatisticsSummary = new WorkerTimestampedStatisticsSummary(seed);
	private TimestampedStatistic queuedStatistic;
	public Worker(QueryExecutor queryExecutor, QueryFactory queryFactory,
			JSONObject querySpec, Keywords keywords, long seed,
			double queryRate, int numberoftraces, int timeout) {
		this.queryExecutor = queryExecutor;
		this.queryFactory = queryFactory;
		// each worker is going to modify the spec to insert filters and stuff
		this.querySpec = new JSONObject(querySpec.toString());
		this.keywords = keywords;
		this.rand = new Random(seed);
		// Share the same seed. Doesn't really matter
		rdg = new RandomDataGenerator();
		rdg.reSeed(seed);
		this.queryRate = queryRate;
		this.seed = seed;
		workerResultSummary = new WorkerResultSummary(seed);
		this.numberoftraces = numberoftraces;
		int numberOfQueuedWorkers = queuedWorkers.incrementAndGet();
		queuedStatistic = new TimestampedStatistic(TimestampedStatisticType.NUMBER_OF_QUEUED_USERS, System.currentTimeMillis(), numberOfQueuedWorkers);
		this.timeout = timeout;
		
	}

	public WorkerResultSummary call() throws Exception {

		LOG.info("Worker " + seed + " is starting");
		int completedTraces = 0;
		try {
			int numberOfQueuedWorkers = queuedWorkers.decrementAndGet();
			if(System.currentTimeMillis() -queuedStatistic.getTimestamp() > 1000L)
			{
				workerTimestampedStatisticsSummary.addStatistic(queuedStatistic);
			workerTimestampedStatisticsSummary.addStatistic(new TimestampedStatistic(TimestampedStatisticType.NUMBER_OF_QUEUED_USERS, System.currentTimeMillis(), numberOfQueuedWorkers));
			}
			int numberOfConcurrentWorkers = concurrentWorkers.incrementAndGet();
			workerTimestampedStatisticsSummary.addStatistic(new TimestampedStatistic(TimestampedStatisticType.NUMBER_OF_CONCURRENT_USERS, System.currentTimeMillis(), numberOfConcurrentWorkers));
			for (int tracenumber = 0; tracenumber < numberoftraces; tracenumber++) {
				long traceStart = System.currentTimeMillis();
				JSONObject querySpec = new JSONObject(this.querySpec.toString());
				Set<String> selectedKeywords = new HashSet<String>();
				while (selectedKeywords.size() < keywordCount
						&& selectedKeywords.size() <= keywords.count()) {
					selectedKeywords.add(keywords.getKeyword(rand));
				}

				JSONArray queryTypes = querySpec.getJSONArray("types");
				JSONObject queryType = queryTypes.getJSONObject(rand
						.nextInt(queryTypes.length()));
				//LOG.info(seed + " " + queryType);
				JSONObject queryKeywordSpec = queryType.getJSONObject("query");
				JSONArray queryKeywords = queryKeywordSpec
						.getJSONArray("keywords");
				for (String selectedKeyword : selectedKeywords) {
					queryKeywords.put(selectedKeyword);
				}

				Query query = queryFactory.generateQuery(queryType);
				int queryDepth = 0;
				double totalWaitTime = 0.0;
				long totalCombinedNoAggregationsTime = 0;
				
				try {
					do {
						long clickStartTime = System.currentTimeMillis();
						double waitTime = waitForUserDecision();
						totalWaitTime += waitTime ;
						
						Future<QueryResult> queryResultFuture = queryExecutor
								.execute(query);
						
						List<Future<QueryResult>> facetResultFutures = issueFacetQueries(
								queryType);
						List<Future<QueryResult>> missingFacetResultFutures = issueFacetMissingQueries(
								queryType);
						QueryResult queryResult =
								collectAndRecordResultAndStatistics(QueryType.SEARCH, queryResultFuture);
						List<Future<QueryResult>> aggregationResultFutures = new LinkedList<Future<QueryResult>>();
						if(queryResult != null)
						{
							aggregationResultFutures = issueAggregationQueries(
								queryType, query, queryResult);
						}
						List<QueryResult> facetResults =
								collectAndRecordResultsAndStatistics(facetResultFutures, QueryType.FACET);
						collectAndRecordResultsAndStatistics(missingFacetResultFutures, QueryType.FACET_MISSING);
						
						totalCombinedNoAggregationsTime = recordCombinedNoAggregationStatistics(
								totalCombinedNoAggregationsTime,
								clickStartTime, waitTime);
						
						collectAndRecordResultsAndStatistics(aggregationResultFutures, QueryType.AGGREGATE);

						recordCombinedStatistics(clickStartTime, waitTime);
						
						JSONObject facetValue = getFacetValue(queryType, rand,
								facetResults);
						if ((facetValue == null || queryResult == null) && !this.timeoutOccurred) {
							break;
						}
						if(!timeoutOccurred)
						{
							query = queryFactory.generateQuery(applyFilter(
								queryType, facetValue));
						}
						queryDepth++;
					} while (queryDepth < maxQueryDepth
 						//	&& rand.nextDouble() < probabilitySearchSatisfied && !timeoutOccurred);
							&& rand.nextDouble() < probabilitySearchSatisfied);
					recordTraceStatistics(traceStart, totalWaitTime,
							totalCombinedNoAggregationsTime);
					
					completedTraces++;
					LOG.info("Worker " + seed + " completed trace " + completedTraces);
				} catch (Exception e) {
					LOG.error(
							"Worker " + seed + " unable to complete queries.",
							e);
					workerResultSummary.setException(e);
					workerTimestampedStatisticsSummary.addStatistic(new TimestampedStatistic(TimestampedStatisticType.NUMBER_OF_COMPLETED_TRACES_AT_FAILURE, System.currentTimeMillis(), completedTraces));
					break;
				}
			}
		} catch (Exception e) {
			workerResultSummary.setException(e);
		} finally {
			queryExecutor.shutdown();
			
		}
		LOG.info("Worker " + seed + "is finishing");
		int numberOfConcurrentWorkers = concurrentWorkers.decrementAndGet();
		workerTimestampedStatisticsSummary.addStatistic(new TimestampedStatistic(TimestampedStatisticType.NUMBER_OF_CONCURRENT_USERS, System.currentTimeMillis(), numberOfConcurrentWorkers));
		return workerResultSummary;

	}

	private void recordTraceStatistics(long traceStart, double totalWaitTime,
			long totalCombinedNoAggregationsTime) {
		QueryResultStatistics traceCombinedQRS = new QueryResultStatistics(
				QueryType.TRACE_COMBINED, System.currentTimeMillis()
						- traceStart);
		workerResultSummary.addStatistic(traceCombinedQRS);
		QueryResultStatistics traceCombinedNoUserDelayQRS = new QueryResultStatistics(
				QueryType.TRACE_COMBINED_NO_USERDELAY, (long)((System.currentTimeMillis()
						- traceStart) - totalWaitTime));
		workerResultSummary.addStatistic(traceCombinedNoUserDelayQRS);
		QueryResultStatistics traceCombinedNoAggsQRS = new QueryResultStatistics(
				QueryType.TRACE_COMBINED_NO_AGGREGATIONS, totalCombinedNoAggregationsTime);
		workerResultSummary.addStatistic(traceCombinedNoAggsQRS);
	}

	private long recordCombinedNoAggregationStatistics(
			long totalCombinedNoAggregationsTime, long clickStartTime,
			double waitTime) {
		long combinedNoAggregationsTime = (long)((System.currentTimeMillis()
				- clickStartTime) - waitTime);
		QueryResultStatistics combinedNoAggsQRS = new QueryResultStatistics(
				QueryType.COMBINED_NO_AGGREGATIONS, combinedNoAggregationsTime);
		totalCombinedNoAggregationsTime += combinedNoAggregationsTime;
		workerResultSummary.addStatistic(combinedNoAggsQRS);
		return totalCombinedNoAggregationsTime;
	}

	private List<Future<QueryResult>> issueAggregationQueries(
			JSONObject queryType, Query query, QueryResult queryResult)
			throws Exception {
		List<Future<QueryResult>> aggregationResultFutures = new LinkedList<Future<QueryResult>>();

		JSONArray aggregationsSpec = queryType.getJSONObject(
				"results").getJSONArray("aggregations");
		JSONArray fieldsSpec = queryType.getJSONObject(
				"results").getJSONArray("fields");
		for (int j = 0; j < aggregationsSpec.length(); j++) {
			JSONObject aggregationSpec = aggregationsSpec
					.getJSONObject(j);
			String anchorPath = aggregationSpec
					.getString("anchor_path");
			JSONArray anchors = queryResult
					.getAnchorsFromResults(anchorPath, fieldsSpec);
			if(anchors == null)
			{
				throw new Exception("No anchors from query: " + query.toString());
			}
			for (int i = 0; i < anchors.length(); i++) {
				JSONObject anchor = anchors.getJSONObject(i);
				Query aggregationQuery = queryFactory
						.generateAggregateQuery(
								aggregationSpec, anchor);
				aggregationResultFutures.add(queryExecutor
						.execute(aggregationQuery));
			}
		}
		return aggregationResultFutures;
	}


	private double waitForUserDecision() throws InterruptedException {
		double waitTime = 0.0;
			
		 waitTime = rdg.nextExponential(1.0/queryRate) * 1000;
		 
		Thread.sleep((long) (waitTime));
		workerResultSummary.addStatistic(new QueryResultStatistics(
				QueryType.USERDELAY, (long) (waitTime)));
		return waitTime;
	}

	private List<Future<QueryResult>> issueFacetMissingQueries(
			JSONObject queryType) {

		JSONArray facetsSpec = queryType.getJSONArray("facets");
		List<Future<QueryResult>> missingFacetResultFutures = new LinkedList<Future<QueryResult>>();
		for (int j = 0; j < facetsSpec.length(); j++) {

			Query missingFacetQuery = queryFactory.generateMissingFacetQuery(queryType, j);
			missingFacetResultFutures.add(queryExecutor
					.execute(missingFacetQuery));
		}
		return missingFacetResultFutures;
	}

	private List<Future<QueryResult>> issueFacetQueries(JSONObject queryType) {

		JSONArray facetsSpec = queryType.getJSONArray("facets");
		List<Future<QueryResult>> facetResultFutures = new LinkedList<Future<QueryResult>>();
		for (int j = 0; j < facetsSpec.length(); j++) {
			Query facetQuery = queryFactory.generateFacetQuery(
					queryType, j);
			facetResultFutures.add(queryExecutor
					.execute(facetQuery));
			
		}
		return facetResultFutures;
	}

	private void recordCombinedStatistics(long clickStartTime,
			double waitTime) {
		QueryResultStatistics combinedQRS = new QueryResultStatistics(
				QueryType.COMBINED, System.currentTimeMillis()
						- clickStartTime);
		workerResultSummary.addStatistic(combinedQRS);
		QueryResultStatistics combinedNoUserDelayQRS = new QueryResultStatistics(
				QueryType.COMBINED_NO_USERDELAY, (long)((System.currentTimeMillis()
						- clickStartTime) - waitTime));
		workerResultSummary.addStatistic(combinedNoUserDelayQRS);
	}

	private List<QueryResult> collectAndRecordResultsAndStatistics(
			List<Future<QueryResult>> resultFutures, QueryType type)
			throws Exception {
		List<QueryResult> results = new LinkedList<QueryResult>();
		for (Future<QueryResult> resultFuture : resultFutures) {
			QueryResult qr = collectAndRecordResultAndStatistics(type,
					resultFuture);
			if(qr != null)
			{
				results.add(qr);
			}
		}
		return results;
	}

	private QueryResult collectAndRecordResultAndStatistics(
			QueryType type, Future<QueryResult> resultFuture)
			throws Exception {
		try{
			QueryResult queryResult = resultFuture
				.get(120, TimeUnit.SECONDS);
			workerResultSummary.addStatistic(queryResult
					.getQueryResultStatistics());
			return (queryResult);
		}
		catch (java.util.concurrent.TimeoutException timeoutException)
		{
			workerResultSummary.addStatistic(new QueryResultStatistics(type,  timeout));
			timeoutOccurred= true;
			return null;
		}
		catch (java.util.concurrent.ExecutionException executionException)
		{
			if(executionException.getCause() instanceof java.util.concurrent.TimeoutException ||
					executionException.getCause() instanceof java.net.ConnectException)
			{
				workerResultSummary.addStatistic(new QueryResultStatistics(type,  timeout));
				timeoutOccurred= true;
				return null;	
			}
			LOG.error(this.seed + " got a non timeout exception ", executionException);
			throw executionException;
		}
		catch (Exception otherException)
		{
			LOG.error(this.seed + " got a non timeout exception ", otherException);
			throw otherException;
		}
	}
	
	public WorkerResultSummary getSummary()
	{
		return workerResultSummary;
	}

	private JSONObject applyFilter(JSONObject queryType, JSONObject facetValue) {
		// top level filter
		if (!queryType.has("userfilter")) {
			queryType.put("userfilter", new JSONArray());
		}
		JSONArray queryUserFilters = queryType.getJSONArray("userfilter");
		queryUserFilters.put(facetValue);

		// agg filters

		// facet filters
		JSONArray facets = queryType.getJSONArray("facets");
		for (int i = 0; i < facets.length(); i++) {
			JSONObject facet = facets.getJSONObject(i);
			if (facet.getString("name").compareTo(facetValue.getString("name")) == 0) {
				continue;
			}
			if (!facet.has("userfilter")) {
				facet.put("userfilter", new JSONArray());
			}
			JSONArray facetUserFilters = facet.getJSONArray("userfilter");
			facetUserFilters.put(facetValue);
		}
		return queryType;
	}

	public JSONObject getFacetValue(JSONObject queryTypeSpec, Random rand,
			List<QueryResult> facetQueryResults) {
		Set<Integer> facetsEvaluated = new HashSet<Integer>();
		int emptyFacetResults = 0;
		JSONArray facetsSpec = queryTypeSpec.getJSONArray("facets");
		if(facetQueryResults.size() != facetsSpec.length())
		{
			LOG.trace("Worker " + seed + " was missing facet results "
					+ facetsSpec);
			return null;
		}
		boolean found = false;
		JSONObject facetSpec = null;
		JSONArray userfilters = null;
		if (queryTypeSpec.has("userfilter")) {
			userfilters = queryTypeSpec.getJSONArray("userfilter");
		}
		while (facetsEvaluated.size() < facetsSpec.length() && !found) {
			Integer facetIndexToEvaluate = rand.nextInt(facetsSpec.length());
			// System.out.println("random " + facetIndexToEvaluate);
			facetSpec = facetsSpec.getJSONObject(facetIndexToEvaluate);

			if (!facetsEvaluated.add(facetIndexToEvaluate)
					|| userfilteralreadychosen(userfilters, facetSpec)) {
				continue;
			}

			QueryResult facetQueryResult = facetQueryResults
					.get(facetIndexToEvaluate);
			JSONObject facetValue = facetQueryResult.getFacetValue(facetSpec,
					rand);
			if (facetValue == null) {
				emptyFacetResults++;
				continue;
			}

			found = true;

			return facetValue;
		}
		LOG.trace("Worker " + seed + " was unable to select a facet from "
				+ facetsSpec);
		LOG.trace("Worker " + seed + " had " + emptyFacetResults
				+ " empty facets");

		return null;

	}

	private boolean userfilteralreadychosen(JSONArray userfilters,
			JSONObject facetSpec) {
		if (userfilters == null) {
			return false;
		}
		String facetspecname = facetSpec.getString("name");
		for (int i = 0; i < userfilters.length(); i++) {
			JSONObject userfilter = userfilters.getJSONObject(i);
			if (facetspecname.compareTo(userfilter.getString("name")) == 0) {
				return true;
			}
		}

		return false;
	}
	
	public WorkerTimestampedStatisticsSummary getTimestampedStatisticsSummary()
	{
		return workerTimestampedStatisticsSummary;
	}

	public long getSeed() {
		return seed;
	}
}
