package edu.isi.ldviews.query;

import java.util.concurrent.Future;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;

public class SPARQLQueryExecutor implements QueryExecutor {
	AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	private String host;
	private int port;
	
	public SPARQLQueryExecutor(String host, int port)
	{
		this.host = host;
		this.port = port;
	}
	public Future<QueryResult> execute(Query query) {
		
		String queryURL = "http://" + host + ":" + port + "/";
		BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(queryURL);
		requestBuilder.setBody(query.toString());
		return null;
	}
	@Override
	public void shutdown() {
		asyncHttpClient.close();		
	}
	
}
