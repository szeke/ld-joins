package edu.isi.ldviews.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

public class RunResultSummary {

Map<QueryType, RunQueryTypeResultSummary> summaries= new HashMap<QueryType, RunQueryTypeResultSummary>();
private long seed;	
public RunQueryTypeResultSummary getSummaryByType(QueryType queryType)
{
	return summaries.get(queryType);
}
	public RunResultSummary(long seed, List<WorkerResultSummary> workerResultSummaries) {
		
		this.seed = seed;
		
		Map<QueryType, List<WorkerQueryTypeResultSummary>> queryTypeToSummary = new HashMap<QueryType, List<WorkerQueryTypeResultSummary>>();
		
		List<QueryType> queryTypes =  Arrays.asList(QueryType.values());
		for(QueryType queryType : queryTypes)
		{
			queryTypeToSummary.put(queryType,  new LinkedList<WorkerQueryTypeResultSummary>());
		}
		for(WorkerResultSummary summary : workerResultSummaries)
		{
			for(QueryType queryType : queryTypes)
			{
				queryTypeToSummary.get(queryType).add(summary.getSummaryByType(queryType));
			}
			
		}
		
		for(Entry<QueryType, List<WorkerQueryTypeResultSummary>> entry : queryTypeToSummary.entrySet())
		{
			summaries.put(entry.getKey(), new RunQueryTypeResultSummary(entry.getKey(), entry.getValue()));
		}
		
	}


	public void addSummary(QueryType queryType, RunQueryTypeResultSummary summary)
	{
		this.summaries.put(queryType, summary);
	}
	
	
	public JSONObject toJSONObject()
	{
		JSONObject obj = new JSONObject();
		
		obj.put("seed", seed);
		for(RunQueryTypeResultSummary summary : summaries.values())
		{
		obj.accumulate("query_summaries", summary.toJSONObject());
		}
		return obj;
	}
}
