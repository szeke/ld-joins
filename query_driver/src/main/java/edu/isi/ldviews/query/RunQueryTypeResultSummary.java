package edu.isi.ldviews.query;

import java.util.List;

import org.json.JSONObject;

public class RunQueryTypeResultSummary {

	QueryType queryType;
	double min = Double.MAX_VALUE;
	double max = 0.0;
	double avg = 0.0;
	int count = 0;
	double sum = 0.0;
	RunQueryTypeResultSummary(JSONObject object)
	{
		this.queryType = QueryType.valueOf(object.getString("type"));
		this.min = object.getDouble("min");
		this.max = object.getDouble("max");
		this.avg = object.getDouble("avg");
		this.count = object.getInt("count");
		this.sum = object.getDouble("sum");
	}
	RunQueryTypeResultSummary(QueryType queryType, List<WorkerQueryTypeResultSummary> results)
	{
		this.queryType = queryType;
		for(WorkerQueryTypeResultSummary result : results )
		{
			max = Math.max(max, result.getMax());
			min = Math.min(min, result.getMin());
			sum += result.getAvg()* result.getCount();
			count += result.getCount();
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

		if(count != 0)
		{
			return sum / (double)count;
		}
		else
		{
			return 0.0;
		}
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
		obj.put("avg", getAvg());
		obj.put("count", count);
		obj.put("sum", sum);
		return obj;
	}

	public void toCSV(StringBuilder sb) {
		sb.append(",");
		sb.append(queryType.toString());
		sb.append(",");
		sb.append(getAvg());
		sb.append(",");
		sb.append(min);
		sb.append(",");
		sb.append(max);
		sb.append(",");
		sb.append(count);
		sb.append(",");
		sb.append(sum);
		
	}
}
