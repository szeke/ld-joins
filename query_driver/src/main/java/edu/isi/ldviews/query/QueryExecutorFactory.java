package edu.isi.ldviews.query;

public class QueryExecutorFactory {
	
	public static QueryExecutor getQueryExecutor(String databaseType, String host, int port, String index)
	{
		if(databaseType.compareTo("ES") == 0)
		{
			return new ESQueryExecutor(host, port, index);
		}
		if(databaseType.compareTo("SPARQL") == 0)
		{
			return new SPARQLQueryExecutor(host, port);
		}
		return null;
	}
}
