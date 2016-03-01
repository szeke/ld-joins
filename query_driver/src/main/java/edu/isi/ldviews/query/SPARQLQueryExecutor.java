package edu.isi.ldviews.query;

import java.io.IOException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Response;

public class SPARQLQueryExecutor implements QueryExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(SPARQLQueryExecutor.class); 
	AsyncHttpClient asyncHttpClient;
	private String host;
	private int port;
	private int timeout;
	public SPARQLQueryExecutor(String host, int port, int timeout)
	{
		this.host = host;
		this.port = port;
		this.timeout = timeout;
		asyncHttpClient =  new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setReadTimeout(timeout).setRequestTimeout(timeout).setConnectTimeout(timeout).build());
	}
	public Future<QueryResult> execute(Query query) {
		
		String queryURL = "http://" + host + ":" + port + "/sparql/";
		BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(queryURL);
		requestBuilder.setHeader("Content-Type", "application/x-www-form-urlencoded");
		final String queryString = query.toString();
		LOG.trace(queryString);
		requestBuilder.addFormParam("query", queryString);
		requestBuilder.addHeader("Accept", "text/csv");
		requestBuilder.setRequestTimeout(timeout);
		final QueryType queryType = query.getQueryType();
		return requestBuilder
				.execute(new AsyncCompletionHandler<QueryResult>() {

					long timestamp =  System.currentTimeMillis();
					@Override
					public QueryResult onCompleted(Response response)
							throws Exception {
						
						LOG.trace(response.getResponseBody());
						LOG.trace(""+response.getStatusCode());
						if(response.getStatusCode() == 400)
						{
							LOG.error("This request failed : "+ queryString);
						}
						LOG.trace(response.getStatusText());
						try
						{
						QueryResult queryResult = new SPARQLQueryResult(response.getResponseBody(), timestamp, System.currentTimeMillis(), queryType);
						LOG.trace(""+queryResult.getQueryTime());
						return queryResult;
						}
						catch(IOException e)
						{
							LOG.error("Request returned bad response: " + queryString);
							LOG.error("Unable to parse response body: " + response.getResponseBody(), e);
							throw e;
						}
						
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
