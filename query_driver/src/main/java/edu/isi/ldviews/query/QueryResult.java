package edu.isi.ldviews.query;

import java.util.Random;

import org.json.JSONObject;

public interface QueryResult {

	JSONObject getFacetValue(JSONObject queryType, Random rand);

}