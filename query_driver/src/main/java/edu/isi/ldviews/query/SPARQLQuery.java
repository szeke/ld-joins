package edu.isi.ldviews.query;

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class SPARQLQuery implements Query {

	private static final String PREFIXES = "prefix s: <http://schema.org/>\nprefix m: <http://memexproxy.com/ontology/>\nprefix xsd: <http://www.w3.org/2001/XMLSchema#>\n";
	private static final Set<String> memexPrefixed = new HashSet<String>();
	static {
		memexPrefixed.add("owner");
		memexPrefixed.add("hairColor");
		memexPrefixed.add("eyeColor");
		memexPrefixed.add("personAge");
	}
	
	private String groupOrder = null;
	private String keywordFilters = null;
	private String optionalFields = null;
	private String selectStatement = null;
	private String typeAndpathFromTypeToWebpageOpen = null;
	private String typeAndpathFromTypeToWebpageClose = null;
	private String name;
	private String aggSparql = null;
	private String type =null; 
	public void addType(JSONObject querySpec) {
		type = querySpec.getString("type");
		StringBuilder typeBuilder = new StringBuilder();
		typeBuilder.append("\t{\n\t\tselect distinct ?x where \n\t\t{\n");
		typeBuilder.append("\t\t\t?x a ");
		typeBuilder.append(querySpec.getString("type"));
		
		if(querySpec.getString("type").compareTo("s:WebPage") != 0)
		{
			typeBuilder.append(" ;\n\t\t\t");
			typeBuilder.append(querySpec.getString("path_to_webpage"));
			typeBuilder.append(" ?wp .\n");
		}
		else
		{
			typeBuilder.append(" .\n");
		}
		//typeBuilder.append("\t\t}\t\t\n\t\tlimit 20\n\t}\n");
		typeAndpathFromTypeToWebpageOpen = typeBuilder.toString();
		typeAndpathFromTypeToWebpageClose ="\t\t}\t\t\n\t\tlimit 20\n\t}\n"; 
	}
	

	public void addFields(JSONArray queryFieldsSpec) {
		StringBuilder selectStatementBuilder = new StringBuilder();
		StringBuilder fields = new StringBuilder();
		selectStatementBuilder.append("select ?x ");
		for(int i = 0; i < queryFieldsSpec.length(); i++)
		{
			
			JSONObject queryField = queryFieldsSpec.getJSONObject(i);
			if(queryField.getString("path").compareTo("uri") == 0) continue;
			fields.append("\toptional { ?x ");
			String path = queryField.getString("path");
			String[] pathElements = path.split("\\.");
			for(int j = 0; j < pathElements.length; j++)
			{
				if(!memexPrefixed.contains(pathElements[j]))
				fields.append("s:");
				else
					fields.append("m:");
				fields.append(pathElements[j]);
				if(j != pathElements.length-1)
				{
					fields.append("/");
				}
			}
			fields.append(" ?");
			fields.append(queryField.getString("name"));
			selectStatementBuilder.append(" ?");
			selectStatementBuilder.append(queryField.getString("name"));
			fields.append(" . }\n");
			
		}
		selectStatementBuilder.append(" where\n ");
		selectStatement = selectStatementBuilder.toString();
		optionalFields = fields.toString();
	}

	public void addFacets(JSONArray queryFacetsSpec) {
		// TODO Auto-generated method stub
		
	}

	public void addKeywords(JSONObject queryKeywordSpec) {
		
		StringBuilder keywordFilterBuilder = new StringBuilder("");
		if(type.compareTo("s:WebPage") != 0)
		{
			keywordFilterBuilder.append("\t\t\t?wp a s:WebPage ;\n");
		}
		else
		{
			keywordFilterBuilder.append("?x ");
		}
		keywordFilterBuilder.append("\n\t\t\ts:description ?text .\n");
		keywordFilterBuilder.append("\t\t\tFILTER ");
		JSONArray specKeywords = queryKeywordSpec.getJSONArray("keywords");
		if(specKeywords.length()> 0)
		{
			keywordFilterBuilder.append("bif:contains(?text, '\"");
			keywordFilterBuilder.append(specKeywords.getString(0));
			keywordFilterBuilder.append("\" ");
		}
		for(int i = 1; i < specKeywords.length(); i ++)
		{
			keywordFilterBuilder.append(" or ");
			keywordFilterBuilder.append("\"");
			keywordFilterBuilder.append(specKeywords.getString(0));
			keywordFilterBuilder.append("\" ");
		}
		
		keywordFilterBuilder.append("') .\n");
		this.keywordFilters = keywordFilterBuilder.toString();
	}

	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
			this.name =name;
		
	}

	@Override
	public void addAggregations(JSONObject queryAggregationsSpec,
			JSONObject anchor) {
		// TODO Auto-generated method stub
		selectStatement = "select sample(?uri) as ?uri ?category count(?item) as ?count where \n";
		JSONArray anchors = anchor.getJSONArray("anchors");
		
		String bind = "BIND (iri(<" + anchors.getString(0) +">) as ?uri) .\n";
		// TODO replace URI with ?uri in query spec
		 aggSparql = bind + queryAggregationsSpec.getString("sparql").replaceFirst("URI", "?uri");
		
		this.groupOrder = "group by ?category\norder by desc(?count)\n";
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(PREFIXES);
		sb.append(selectStatement);
		sb.append("{\n");
		if(aggSparql != null)
		{
			sb.append(aggSparql);
		}
		if(typeAndpathFromTypeToWebpageOpen != null)
		{
			sb.append(typeAndpathFromTypeToWebpageOpen);
		}
		if(keywordFilters != null)
		{
			sb.append(keywordFilters);
		}
		if(typeAndpathFromTypeToWebpageClose != null)
		{
			sb.append(typeAndpathFromTypeToWebpageClose);
		}
		if(optionalFields != null)
		{
			sb.append(optionalFields);
		}
		sb.append("}\n");
		if(groupOrder != null)
			sb.append(groupOrder);
		//sb.append("limit 20");
		return sb.toString();
		
	}

}
