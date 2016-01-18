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
	private Exception e = null;
	
	public WorkerResultSummary(long seed) {
		this.seed = seed;
		initializeSummaries();
		
	}

	public void initializeSummaries()
	{
		for(QueryType queryType : Arrays.asList(QueryType.values()))
		{
			summaries.put(queryType, new WorkerQueryTypeResultSummary(queryType));
		}
	}
	
	public void addStatistic(QueryResultStatistics statistics)
	{
		summaries.get(statistics.queryType).addStatistics(statistics);
	}
	
	
	
	public WorkerQueryTypeResultSummary getSummaryByType(QueryType queryType)
	{
		return summaries.get(queryType);
	}
	
	public void setException(Exception e)
	{
		this.e = e;
	}
	public Exception getException()
	{
		return e;
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
