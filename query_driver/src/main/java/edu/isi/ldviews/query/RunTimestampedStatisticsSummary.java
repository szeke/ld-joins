package edu.isi.ldviews.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RunTimestampedStatisticsSummary {

	Map<TimestampedStatisticType, List<TimestampedStatistic>> summaries= new HashMap<TimestampedStatisticType, List<TimestampedStatistic>>();
	private long seed;	
	private int numWorkers;
	private int maxConcurrency;
	private int numTraces;
	private double arrivalRate;
	private String databaseType;
	
	public RunTimestampedStatisticsSummary(
			long randomseed,
			List<WorkerTimestampedStatisticsSummary> workerTimestampedStatisticsSummaries) {
		this.seed = randomseed;
		initializeSummaries(workerTimestampedStatisticsSummaries);
	}

	private void initializeSummaries(
			List<WorkerTimestampedStatisticsSummary> workerTimestampedStatisticsSummaries) {
		
		
		List<TimestampedStatisticType> statisticTypes =  Arrays.asList(TimestampedStatisticType.values());
		for(TimestampedStatisticType statisticType : statisticTypes)
		{
			summaries.put(statisticType,  new LinkedList<TimestampedStatistic>());
		}
		for(WorkerTimestampedStatisticsSummary workerSummary : workerTimestampedStatisticsSummaries)
		{
			for(TimestampedStatisticType statisticType : statisticTypes)
			{
				summaries.get(statisticType).addAll(workerSummary.getSummaryByType(statisticType));
			}
			
		}
		
		for(List<TimestampedStatistic> statisticsList : summaries.values())
		{
			Collections.sort(statisticsList, new Comparator<TimestampedStatistic>(){

				@Override
				public int compare(TimestampedStatistic o1,
						TimestampedStatistic o2) {
					return (int)(o1.getTimestamp() - o2.getTimestamp());
				}
				
			});
		}
		
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
	
	public String toCSV(){
		StringBuilder sb = new StringBuilder();
		

		for(List<TimestampedStatistic> statisticsList : summaries.values())
		{
			for(TimestampedStatistic statistic : statisticsList)
			{
				statistic.toCSV(sb);
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
}
