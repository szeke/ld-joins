package edu.isi.ldviews.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPARQLPathElement {

	String value;
	Map<String, SPARQLPathElement> children = new HashMap<String, SPARQLPathElement>();
	boolean isRoot;
	public SPARQLPathElement(String value) {
		this.value = value;
		this.isRoot = false;
	}

	public SPARQLPathElement(String value, boolean isRoot)
	{
		this.value = value;
		this.isRoot = true;
	}
	
	public boolean hasAChild()
	{
		return children.size() ==1;
	}
	public boolean hasChildren()
	{
		return children.size() > 1;
	}
	public void addValues(List<String> values) {
		if (values == null || values.isEmpty()) {
			return;
		}
		if(children.containsKey(values.get(0)))
		{
			children.get(values.get(0)).addValues(values.subList(1, values.size()));
		}
		else
		{
			String nextValue = values.get(0);
			SPARQLPathElement child = new SPARQLPathElement(values.get(0));
			children.put(nextValue, child);
			child.addValues(values.subList(1, values.size()));
		}
	}

	public void serialize(StringBuilder sb) {
		
		if(children.isEmpty())
		{
			sb.append(value);
			sb.append(" .\n");
		}
		else if(children.size() == 1)
		{
			if(isRoot)
				sb.append("?");
			sb.append(value);
			SPARQLPathElement child = children.values().iterator().next();
			if(!isRoot && (child.hasAChild() || child.hasChildren()))
			{
				sb.append("/");
			}
			else
			{
				sb.append(" ");
			}
			child.serialize(sb);
		}
		else
		{
			String subjectVar = "";
			if(isRoot)
			{
				subjectVar = value;
			}
			else
			{
				//random!
				sb.append(" ");
				sb.append(value);
				sb.append(" ");
				if(!children.containsKey("?wp"))
				{
				subjectVar = "_" + sb.length();
				}
				else
				{
					subjectVar = "wp";
				}
				sb.append(" ?");
				sb.append(subjectVar);
				
				
				sb.append(" .\n");
			}
			
			for(SPARQLPathElement child: children.values())
			{
			
				if(subjectVar.compareTo("wp") != 0 )
				{
				sb.append("?");
				sb.append(subjectVar);
				sb.append(" ");
				child.serialize(sb);
				}
			}
		}
	}

}
