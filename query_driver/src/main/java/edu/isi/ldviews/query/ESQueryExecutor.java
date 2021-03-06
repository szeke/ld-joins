package edu.isi.ldviews.query;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHandler.STATE;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;

public class ESQueryExecutor implements QueryExecutor {
	private static final Logger LOG = LoggerFactory.getLogger(ESQueryExecutor.class); 
	AsyncHttpClient asyncHttpClient = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setReadTimeout(120000).setRequestTimeout(120000).setConnectTimeout(120000).build());
	private String host;
	private int port;
	private String index;

	public ESQueryExecutor(String host, int port, String index) {
		this.host = host;
		this.port = port;
		this.index = index;
	}

	public Future<QueryResult> execute(Query query) {
		final QueryType queryType = query.getQueryType();
		String routing = query instanceof ESQuery ? ((ESQuery)query).getRouting() : null;
		String queryURL = "http://" + host + ":" + port + "/" + index + "/"
				+ query.getName().toLowerCase() + "/_search";
		if(routing!= null)
		{
			queryURL = queryURL + "?routing=" + routing.replaceAll("\\/", "%2F");
		}
		BoundRequestBuilder requestBuilder = asyncHttpClient
				.preparePost(queryURL);
		final String  queryString = query.toString();
		LOG.trace(queryString);
		requestBuilder.setBody(queryString);
		requestBuilder.setRequestTimeout(120000);
		return requestBuilder
				.execute(new AsyncCompletionHandler<QueryResult>() {
					

					@Override
					public STATE onHeaderWriteCompleted() {
						
						this.timestamp = System.currentTimeMillis();
						return STATE.CONTINUE;
						
					}
					
					long createdTimestamp =  System.currentTimeMillis();
					long timestamp;
					@Override
					public QueryResult onCompleted(Response response)
							throws Exception {
						// Do something with the Response
						return new ESQueryResult(response.getResponseBody(), timestamp, System.currentTimeMillis(), queryType) ;
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
