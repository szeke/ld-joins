package edu.isi.ldviews.query;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class WorkerResultSummary {

	Map<QueryType, WorkerQueryTypeResultSummary> summaries= new HashMap<QueryType, WorkerQueryTypeResultSummary>();
	private long seed;
	public WorkerQueryTypeResultSummary getSummaryByType(QueryType queryType)
	{
		return summaries.get(queryType);
	}
	
	public WorkerResultSummary(long seed, List<QueryResultStatistics> queryResultStatistics) {
		this.seed = seed;
		// this would be so much cleaner in scala!!!!
		List<QueryResultStatistics> searchstats = new LinkedList<QueryResultStatistics>();
		List<QueryResultStatistics>  facetstats = new LinkedList<QueryResultStatistics>();
		List<QueryResultStatistics>  aggstats = new LinkedList<QueryResultStatistics>();
		List<QueryResultStatistics>  combinedstats = new LinkedList<QueryResultStatistics>();
		for(QueryResultStatistics statistics : queryResultStatistics)
		{
			if(statistics.queryType == QueryType.SEARCH)
			{
				searchstats.add(statistics);
			}
			else if(statistics.queryType == QueryType.AGGREGATE)
			{
				aggstats.add(statistics);
			}
			else if(statistics.queryType == QueryType.FACET)
			{
				facetstats.add(statistics);
			}
			else if(statistics.queryType == QueryType.COMBINED)
			{
				combinedstats.add(statistics);
			}
		}
		WorkerQueryTypeResultSummary aggsummary = new WorkerQueryTypeResultSummary(QueryType.AGGREGATE, aggstats);
		WorkerQueryTypeResultSummary facetsummary = new WorkerQueryTypeResultSummary(QueryType.FACET, facetstats);
		WorkerQueryTypeResultSummary searchsummary = new WorkerQueryTypeResultSummary(QueryType.SEARCH, searchstats);
		WorkerQueryTypeResultSummary combinedsummary = new WorkerQueryTypeResultSummary(QueryType.COMBINED, combinedstats);
		summaries.put(QueryType.AGGREGATE, aggsummary);
		summaries.put(QueryType.FACET, facetsummary);
		summaries.put(QueryType.SEARCH, searchsummary);
		summaries.put(QueryType.COMBINED, combinedsummary);
		
	}


	public void addSummary(QueryType queryType, WorkerQueryTypeResultSummary summary)
	{
		this.summaries.put(queryType, summary);
	}
	
	public JSONObject toJSONObject()
	{
		JSONObject obj = new JSONObject();
		
		obj.put("seed", seed);
		for(WorkerQueryTypeResultSummary summary : summaries.values())
		{
		obj.accumulate("query_summaries", summary.toJSONObject());
		}
		return obj;
	}
}
