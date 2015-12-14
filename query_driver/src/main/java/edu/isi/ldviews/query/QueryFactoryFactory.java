package edu.isi.ldviews.query;

public class QueryFactoryFactory {
	
	public static QueryFactory getQueryFactory(String databaseType)
	{
		if(databaseType.compareTo("ES") == 0)
		{
			return new ESQueryFactory();
		}
		if(databaseType.compareTo("SPARQL") == 0)
		{
			return new SPARQLQueryFactory();
		}
		return null;
	}
}
