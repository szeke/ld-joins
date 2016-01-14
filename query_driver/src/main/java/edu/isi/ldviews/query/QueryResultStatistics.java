package edu.isi.ldviews.query;

public class QueryResultStatistics {

	QueryType queryType;
	long queryTime;
	
	public QueryResultStatistics(QueryType queryType, long queryTime)
	{
		this.queryType = queryType;
		this.queryTime = queryTime;
	}

	public QueryType getQueryType()
	{
		return queryType;
	}
	public long getQueryTime()
	{
		return queryTime;
	}
}
