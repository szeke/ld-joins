package edu.isi.ldviews.query;

import java.util.List;

import org.json.JSONObject;

public class WorkerQueryTypeResultSummary {

	QueryType queryType;
	double min = Double.MAX_VALUE;
	double max = 0.0;
	double avg = 0.0;
	int count = 0;
	
	WorkerQueryTypeResultSummary(QueryType queryType, List<QueryResultStatistics> results)
	{
		this.queryType = queryType;
		count = results.size();
		for(QueryResultStatistics result : results )
		{
			max = Math.max(max, result.getQueryTime());
			min = Math.min(min, result.getQueryTime());
			avg += result.getQueryTime();
			
		}
		if(count != 0)
		{
			avg = avg / (double)count;
		}
		else
		{
			min = 0.0;
		}
	}

	public QueryType getQueryType() {
		return queryType;
	}

	public void setQueryType(QueryType queryType) {
		this.queryType = queryType;
	}

	public double getMin() {
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
		return avg;
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
