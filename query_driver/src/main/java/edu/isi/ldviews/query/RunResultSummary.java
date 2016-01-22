package edu.isi.ldviews.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

public class RunResultSummary {

Map<QueryType, RunQueryTypeResultSummary> summaries= new HashMap<QueryType, RunQueryTypeResultSummary>();
private long seed;	
private int numWorkers;
private int maxConcurrency;
private int numTraces;
private double arrivalRate;
private String databaseType;

public RunQueryTypeResultSummary getSummaryByType(QueryType queryType)
{
	return summaries.get(queryType);
}

	public RunResultSummary(JSONObject object)
	{
		
		this.seed = object.getLong("seed");
		this.numWorkers = object.getInt("number_of_workers");
		this.maxConcurrency = object.getInt("max_concurrency");
		this.numTraces = object.getInt("number_of_traces");
		this.arrivalRate = object.getDouble("arrival_rate");
		this.databaseType = object.getString("database_type");
		
		for(int i = 0; i < object.getJSONArray("query_summaries").length(); i++)
		{
			RunQueryTypeResultSummary summary = new RunQueryTypeResultSummary(object.getJSONArray("query_summaries").getJSONObject(i));
			summaries.put(summary.getQueryType(), summary);
		}
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
		obj.put("number_of_workers", numWorkers);
		obj.put("max_concurrency", maxConcurrency);
		obj.put("number_of_traces", numTraces);
		obj.put("arrival_rate", arrivalRate);
		obj.put("database_type", databaseType);
		for(RunQueryTypeResultSummary summary : summaries.values())
		{
			obj.accumulate("query_summaries", summary.toJSONObject());
		}
		return obj;
	}
	
	public String toCSV()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(databaseType);
		sb.append(",");
		sb.append(numWorkers);
		sb.append(",");
		sb.append(maxConcurrency);
		sb.append(",");
		sb.append(arrivalRate);
		sb.append(",");
		sb.append(numTraces);
		sb.append(",");
		sb.append(seed);
		List<RunQueryTypeResultSummary> sortedSummaries = new LinkedList<RunQueryTypeResultSummary>();
		sortedSummaries.addAll(summaries.values()); 
		Collections.sort(sortedSummaries, new Comparator<RunQueryTypeResultSummary>(){

			@Override
			public int compare(RunQueryTypeResultSummary o1,
					RunQueryTypeResultSummary o2) {
				
				return o1.queryType.compareTo(o2.queryType);
			}});
		for(RunQueryTypeResultSummary summary : sortedSummaries)
		{
			summary.toCSV(sb);
		}
		return sb.toString();
	}
	
	public void setNumWorkers(int numWorkers) {
		this.numWorkers = numWorkers;
	}
	public void setMaxConcurrency(int maxConcurrency) {
		this.maxConcurrency = maxConcurrency;
	}
	public void setNumTraces(int numTraces) {
		this.numTraces = numTraces;
	}
	public void setArrivalRate(double arrivalRate) {
		this.arrivalRate = arrivalRate;
	}

	public void setDatabaseType(String databaseType)
	{
		this.databaseType = databaseType;
	}
}
