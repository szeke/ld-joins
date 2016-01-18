package edu.isi.ldviews.query;

import java.util.List;

import org.json.JSONObject;

public class WorkerQueryTypeResultSummary {

	QueryType queryType;
	double min = Double.MAX_VALUE;
	double max = 0.0;
	double avg = 0.0;
	double sum = 0.0;
	int count = 0;
	
	WorkerQueryTypeResultSummary(QueryType queryType)
	{
		this.queryType = queryType;
		
	}
	WorkerQueryTypeResultSummary(QueryType queryType, List<QueryResultStatistics> results)
	{
		this.queryType = queryType;
		for(QueryResultStatistics result : results )
		{
			addStatistics(result);
			
		}
	}

	
	public void addStatistics(QueryResultStatistics statistics)
	{
		count++;
		max = Math.max(max, statistics.getQueryTime());
		min = Math.min(min, statistics.getQueryTime());
		sum += statistics.getQueryTime();
	}
	public QueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	public double getMin() {
		if(count == 0)
		{
			return 0;
		}
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getAvg() {
		if(count != 0)
		{
			return sum / (double)count;
		}
		return 0.0;
	}

	public void setAvg(double avg) {
		this.avg = avg;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public JSONObject toJSONObject()
	{
		JSONObject obj = new JSONObject();
		obj.put("type", queryType.toString());
		obj.put("min", min);
		obj.put("max", max);
		obj.put("avg", avg);
		obj.put("count", count);
		return obj;
	}
	
	
}
