package edu.isi.ldviews.query;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
		// this would be so much cleaner in scala!!!!
		List<WorkerQueryTypeResultSummary> searchstats = new LinkedList<WorkerQueryTypeResultSummary>();
		List<WorkerQueryTypeResultSummary>  facetstats = new LinkedList<WorkerQueryTypeResultSummary>();
		List<WorkerQueryTypeResultSummary>  aggstats = new LinkedList<WorkerQueryTypeResultSummary>();
		List<WorkerQueryTypeResultSummary>  combinedstats = new LinkedList<WorkerQueryTypeResultSummary>();
		for(WorkerResultSummary workerResultSummary : workerResultSummaries)
		{
			aggstats.add(workerResultSummary.getSummaryByType(QueryType.AGGREGATE));
			facetstats.add(workerResultSummary.getSummaryByType(QueryType.FACET));
			searchstats.add(workerResultSummary.getSummaryByType(QueryType.SEARCH));
			combinedstats.add(workerResultSummary.getSummaryByType(QueryType.COMBINED));
		}
		RunQueryTypeResultSummary aggsummary = new RunQueryTypeResultSummary(QueryType.AGGREGATE, aggstats);
		RunQueryTypeResultSummary facetsummary = new RunQueryTypeResultSummary(QueryType.FACET, facetstats);
		RunQueryTypeResultSummary searchsummary = new RunQueryTypeResultSummary(QueryType.SEARCH, searchstats);
		RunQueryTypeResultSummary combinedsummary = new RunQueryTypeResultSummary(QueryType.COMBINED, combinedstats);
		summaries.put(QueryType.AGGREGATE, aggsummary);
		summaries.put(QueryType.FACET, facetsummary);
		summaries.put(QueryType.SEARCH, searchsummary);
		summaries.put(QueryType.COMBINED, combinedsummary);
		
		
		
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
