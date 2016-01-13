package edu.isi.ldviews.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
	private String typeAndpathFromTypeToWebpageCloseWithLimit = null;
	private String name;
	private String aggSparql = null;
	private String type =null;
	private String facet;
	private String limit;
	private SPARQLPathElement root = new SPARQLPathElement("x", true);
	public void addType(JSONObject querySpec) {
		type = querySpec.getString("type");
		StringBuilder typeBuilder = new StringBuilder();
		typeBuilder.append("\t{\n\t\tselect distinct ?x where \n\t\t{\n");
		typeBuilder.append("\t\t\t?x a ");
		typeBuilder.append(querySpec.getString("type"));
		
		if(querySpec.getString("type").compareTo("s:WebPage") != 0)
		{
			//typeBuilder.append(" ;\n\t\t\t");
			//typeBuilder.append(querySpec.getString("path_to_webpage"));
			//typeBuilder.append(" ?wp .\n");
			List<String> pathToWPElements = translateToSPARQLPathListFromSlashes(querySpec.getString("path_to_webpage"));
			pathToWPElements.add("?wp");
			this.root.addValues(pathToWPElements);
		}
		//else
		{
			typeBuilder.append(" .\n");
		}
		//typeBuilder.append("\t\t}\t\t\n\t\tlimit 20\n\t}\n");
		typeAndpathFromTypeToWebpageOpen = typeBuilder.toString();
		typeAndpathFromTypeToWebpageClose ="\t\t}\t\t\n\t\t\n\t}\n";
		typeAndpathFromTypeToWebpageCloseWithLimit ="\t\t}\t\t\n\t\tlimit 20\n\t}\n";
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

	public void addFacets(JSONArray queryFacetsSpec, int facetIndex) {
		
		selectStatement = "select ?facet str(?category) as ?category count(distinct(?x)) as ?count where \n";
		StringBuilder facetBuilder = new StringBuilder();
		{
			JSONObject queryFacetSpec = queryFacetsSpec.getJSONObject(facetIndex);
			String queryFacetName = queryFacetSpec.getString("name");
			String queryFacetPath = queryFacetSpec.getString("path");
			String sparqlPath = translateToSPARQLPath(queryFacetPath);
			
			if(queryFacetSpec.has("userfilter"))
			{
				JSONArray userFilters = queryFacetSpec.getJSONArray("userfilter");
				for(int j = 0; j < userFilters.length(); j++)
				{
					JSONObject userFilter = userFilters.getJSONObject(j);
					String queryFacetFilterPath = userFilter.getString("path");
					
					List<String> filterElements = translateToSPARQLPathList(queryFacetFilterPath);
					filterElements.add("\"" + userFilter.getString("term")+"\"");
					root.addValues(filterElements);
				}
				
			
			}
				
				facetBuilder.append("\t\t\t {\n");
				facetBuilder.append("\t\t\t BIND(str(");
				facetBuilder.append("\"");
				facetBuilder.append(queryFacetName);
				facetBuilder.append("\") as ?facet)\n");
				facetBuilder.append("\t\t\t?x ");
				facetBuilder.append(sparqlPath);
				facetBuilder.append(" ?category .\n");
				facetBuilder.append("\t\t\t}\n");
			
			
		}
		this.facet = facetBuilder.toString();
		//this.groupOrder = "group by ?facet ?category\norder by desc(?count)\n";
		this.groupOrder = "group by ?facet ?category\norder by desc(?count) ?category\n";
		this.limit = "\nlimit 20\n";
	}


	public static List<String> translateToSPARQLPathList(String path)
	{
		String[] fields = JSONCollector.splitPath(path);
		List<String> fieldsList = new ArrayList<String>(fields.length);
		for(String field : fields)
		{
			fieldsList.add(prependPrefix(field));
		}
		return fieldsList;
			
	}
	

	public static List<String> translateToSPARQLPathListFromSlashes(String string) {
		String[] fields = string.split("\\/");
		List<String> fieldsList = new ArrayList<String>(fields.length);
		for(String field : fields)
		{
			fieldsList.add(prependPrefix(field));
		}
		return fieldsList;
	}


	
	
	public static String translateToSPARQLPath(String path) {
		String[] fields = JSONCollector.splitPath(path);
		StringBuilder sparqlPathBuilder = new StringBuilder();
		sparqlPathBuilder.append(prependPrefix(fields[0]));
		for(int j = 1; j < fields.length; j++)
		{
			sparqlPathBuilder.append("/");
			sparqlPathBuilder.append(prependPrefix(fields[j]));
		}
		String sparqlPath = sparqlPathBuilder.toString();
		return sparqlPath;
	}


	private static String prependPrefix(String field) {
		if(memexPrefixed.contains(field))
		{
			return "m:"+field;
		}
		else if(!field.startsWith("s:") && !field.startsWith("m:"))
		{
			return"s:"+field;
		}
		else
		{
			return field;
		}
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
			keywordFilterBuilder.append(specKeywords.getString(i));
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
		
		if(root.hasAChild() || root.hasChildren())
		{
			root.serialize(sb);
		}
		if(keywordFilters != null)
		{
			sb.append(keywordFilters);
		}
		if(facet != null && typeAndpathFromTypeToWebpageClose != null)
		{
			sb.append(typeAndpathFromTypeToWebpageClose);
		}
		else if(typeAndpathFromTypeToWebpageCloseWithLimit != null)
		{
			sb.append(typeAndpathFromTypeToWebpageCloseWithLimit);
		}
		if(optionalFields != null)
		{
			sb.append(optionalFields);
		}
		if(facet != null)
		{
			sb.append(facet);
		}
		sb.append("}\n");
		if(groupOrder != null)
			sb.append(groupOrder);
		if(limit != null)
		{
			sb.append(limit);
		}
		return sb.toString();
		
	}

}
