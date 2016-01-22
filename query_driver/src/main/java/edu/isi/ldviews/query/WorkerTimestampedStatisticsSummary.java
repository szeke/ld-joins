package edu.isi.ldviews.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class WorkerTimestampedStatisticsSummary {
	Map<TimestampedStatisticType, List<TimestampedStatistic>> summaries= new HashMap<TimestampedStatisticType, List<TimestampedStatistic>>();
	private long seed;
	private Exception e = null;
	
	public WorkerTimestampedStatisticsSummary(long seed) {
		this.seed = seed;
		initializeSummaries();
		
	}

	public void initializeSummaries()
	{
		for(TimestampedStatisticType statisticType : Arrays.asList(TimestampedStatisticType.values()))
		{
			summaries.put(statisticType, new LinkedList<TimestampedStatistic>());
		}
	}
	
	public void addStatistic(TimestampedStatistic statistics)
	{
		summaries.get(statistics.getType()).add(statistics);
	}
	
	
	
	public List<TimestampedStatistic> getSummaryByType(TimestampedStatisticType type)
	{
		return summaries.get(type);
	}
	
	public void setException(Exception e)
	{
		this.e = e;
	}
	public Exception getException()
	{
		return e;
	}
	public JSONObject toJSONObject() throws Exception
	{
		throw new Exception("Nope!");
	}
}
