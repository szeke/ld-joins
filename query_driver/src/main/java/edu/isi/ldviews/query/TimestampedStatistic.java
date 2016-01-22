package edu.isi.ldviews.query;

import org.json.JSONObject;

public class TimestampedStatistic {

	private TimestampedStatisticType type;
	private long timestamp;
	private double value;
	
	public TimestampedStatistic(TimestampedStatisticType type, long timestamp, double value)
	{
		this.type = type;
		this.timestamp = timestamp;
		this.value = value;
	}
	
	public TimestampedStatisticType getType() {
		return type;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public double getValue() {
		return value;
	}
	
	public JSONObject toJSONObject()
	{
		JSONObject object = new JSONObject();
		object.put("type", type.toString());
		object.put("timestamp", timestamp);
		object.put("value", value);
		return object; 
	}
	
	public void toCSV(StringBuilder sb)
	{
	//	sb.append(",");
		sb.append(type.toString());
		sb.append(",");
		sb.append(timestamp);
		sb.append(",");
		sb.append(value);
	}
}
