package edu.isi.ldviews.query;

import java.util.concurrent.Future;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;

public class ESQueryExecutor implements QueryExecutor {
	AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
	private String host;
	private int port;
	private String index;

	public ESQueryExecutor(String host, int port, String index) {
		this.host = host;
		this.port = port;
		this.index = index;
	}

	public Future<QueryResult> execute(Query query) {
		String routing = query instanceof ESQuery ? ((ESQuery)query).getRouting() : null;
		String queryURL = "http://" + host + ":" + port + "/" + index + "/"
				+ query.getName().toLowerCase() + "/_search";
		if(routing!= null)
		{
			queryURL = queryURL + "?routing=" + routing.replaceAll("\\/", "%2F");
		}
		BoundRequestBuilder requestBuilder = asyncHttpClient
				.preparePost(queryURL);
		String queryString = query.toString();
		System.out.println(queryString);
		requestBuilder.setBody(queryString);

		return requestBuilder
				.execute(new AsyncCompletionHandler<QueryResult>() {

					@Override
					public QueryResult onCompleted(Response response)
							throws Exception {
						// Do something with the Response
						return new ESQueryResult(response.getResponseBody());
					}

					@Override
					public void onThrowable(Throwable t) {
						// Something wrong happened.
					}
				});

	}

	@Override
	public void shutdown() {
		asyncHttpClient.close();		
	}

}
