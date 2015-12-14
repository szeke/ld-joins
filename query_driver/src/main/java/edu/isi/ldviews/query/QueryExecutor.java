package edu.isi.ldviews.query;

import java.util.concurrent.Future;

public interface QueryExecutor {

	Future<QueryResult> execute(Query query);

	void shutdown();
}
