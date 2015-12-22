package edu.isi.ldviews.query;

import java.util.LinkedList;
import java.util.List;

public class SuperQuery {

	private List<Query> queries = new LinkedList<Query>();
	public void addQuery(Query query)
	{
		queries.add(query);
	}
	public List<Query> getQueries()
	{
		return queries;
	}
}
