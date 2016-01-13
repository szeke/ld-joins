package edu.isi.ldviews.query;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestSPARQLPathElement {

	@Test
	public void test() {
		SPARQLPathElement root = new SPARQLPathElement("x", true );
		List<String> pathToWP = SPARQLQuery.translateToSPARQLPathListFromSlashes("m:owner/s:makesOffer/s:mainEntityOfPage");
		pathToWP.add("?wp");
		root.addValues(pathToWP);
		List<String> filter = SPARQLQuery.translateToSPARQLPathListFromSlashes("m:owner/s:makesOffer/s:mainEntityOfPage/s:publisher/s:name");
		filter.add("backpage.com");
		root.addValues(filter);
		StringBuilder consolidatedPathsBuilder = new StringBuilder();
		root.serialize(consolidatedPathsBuilder);
		String consolidatedPaths = consolidatedPathsBuilder.toString();
		Assert.assertTrue(!consolidatedPaths.isEmpty());
		System.out.println(consolidatedPaths);
	}

}
