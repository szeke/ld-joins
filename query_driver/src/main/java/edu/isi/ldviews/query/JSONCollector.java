package edu.isi.ldviews.query;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONCollector  {
	

	protected static String[] splitPath(String aPath) {
		List<String> levelsCandidates = new LinkedList<String>();
		int lastSplit = 0;
		for(int i = 0; i < aPath.length() - 1; i++)
		{
			if(aPath.charAt(i) != '\\' && aPath.charAt(i+1) == '.')
			{
				levelsCandidates.add(aPath.substring(lastSplit, aPath.charAt(i) != '\\'? i+1 : i).replace("\\", ""));
				lastSplit = i+2;
			}
		}
		if(lastSplit < aPath.length())
		{
			levelsCandidates.add(aPath.substring(lastSplit, aPath.length()).replace("\\", ""));
		}
		String[] levels = new String[levelsCandidates.size()];
		levels = levelsCandidates.toArray(levels);
		return levels;
	}

	public static void collectJSONObject(JSONObject obj, String[] levels, int i, JSONArray array) {
		if (i < levels.length && obj.has(levels[i])) {
			if (i == levels.length - 1) {
				Object value = obj.get(levels[i]);
				if (value instanceof JSONArray) {
					JSONArray t = (JSONArray)value;
					for (int j = 0; j < t.length(); j++) {
						array.put(t.get(j));
					}
				}
				else {
					array.put(value);
				}
			}
			else {
				Object value = obj.get(levels[i]);
				if (value instanceof JSONArray) {
					JSONArray t = (JSONArray)value;
					for (int j = 0; j < t.length(); j++) {
						try {
							JSONObject o = t.getJSONObject(j);
							collectJSONObject(o, levels, i + 1, array);
						}catch (Exception e) {

						}
					}
				}
				else if (value instanceof JSONObject){
					collectJSONObject((JSONObject)value, levels, i + 1, array);
				}

			}
		}
	}
}
