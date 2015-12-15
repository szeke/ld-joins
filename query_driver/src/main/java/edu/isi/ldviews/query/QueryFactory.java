package edu.isi.ldviews.query;

import org.json.JSONObject;

public interface QueryFactory {

	Query generateQuery(JSONObject queryTypeSpec);
	Query generateAggregateQuery(JSONObject queryTypeSpec, JSONObject anchor);
}
