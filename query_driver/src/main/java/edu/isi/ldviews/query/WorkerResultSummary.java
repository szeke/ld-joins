package edu.isi.ldviews.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		Map<QueryType, List<QueryResultStatistics>> queryTypeToStatistics = new HashMap<QueryType, List<QueryResultStatistics>>();
		
		for(QueryType queryType : Arrays.asList(QueryType.values()))
		{
			queryTypeToStatistics.put(queryType,  new LinkedList<QueryResultStatistics>());
		}
		for(QueryResultStatistics statistics : queryResultStatistics)
		{
			queryTypeToStatistics.get(statistics.queryType).add(statistics);	
			
		}
		for(Entry<QueryType, List<QueryResultStatistics>> entry : queryTypeToStatistics.entrySet())
		{
			summaries.put(entry.getKey(), new WorkerQueryTypeResultSummary(entry.getKey(), entry.getValue()));
		}
		
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
