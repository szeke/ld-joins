package edu.isi.ldviews.query;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;

public class SPARQLQueryExecutor implements QueryExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(SPARQLQueryExecutor.class); 
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
		final String queryString = query.toString();
		LOG.info(queryString);
		requestBuilder.addFormParam("query", queryString);
		requestBuilder.addHeader("Accept", "text/csv");
		requestBuilder.setRequestTimeout(100000);
		return requestBuilder
				.execute(new AsyncCompletionHandler<QueryResult>() {

					@Override
					public QueryResult onCompleted(Response response)
							throws Exception {
						LOG.info(response.getResponseBody());
						LOG.info(""+response.getStatusCode());
						LOG.info(response.getStatusText());
						return new SPARQLQueryResult(response.getResponseBody());
					}

					@Override
					public void onThrowable(Throwable t) {
						LOG.error("Unable to complete query: \n" + queryString,  t);
					}
				});
	}
	@Override
	public void shutdown() {
		asyncHttpClient.close();		
	}
	
}
