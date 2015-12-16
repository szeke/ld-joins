package edu.isi.ldviews.query;

import java.net.URLEncoder;
import java.util.concurrent.Future;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
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
		
		String queryURL = "http://" + host + ":" + port + "/sparql/";
		BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(queryURL);
		requestBuilder.setHeader("Content-Type", "application/x-www-form-urlencoded");
		String queryString = query.toString();
		System.out.println(queryString);
		requestBuilder.addFormParam("query", queryString);
		requestBuilder.addHeader("Accept", "text/csv");
		
		return requestBuilder
				.execute(new AsyncCompletionHandler<QueryResult>() {

					@Override
					public QueryResult onCompleted(Response response)
							throws Exception {
						System.out.println(response.getResponseBody());
						System.out.println(response.getStatusCode());
						System.out.println(response.getStatusText());
						return new SPARQLQueryResult(response.getResponseBody());
					}

					@Override
					public void onThrowable(Throwable t) {
System.err.println(t.toString());
					}
				});
	}
	@Override
	public void shutdown() {
		asyncHttpClient.close();		
	}
	
}
