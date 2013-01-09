package org.tuml.runtime.adaptor;

import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;

import java.io.File;

public class TumlNeo4jGraphFactory implements TumlGraphFactory {
	
	public static TumlNeo4jGraphFactory INSTANCE = new TumlNeo4jGraphFactory();
	
	private TumlNeo4jGraphFactory() {
	}
	
	public static TumlGraphFactory getInstance() {
		return INSTANCE;
	}
	
	@Override
	public TumlGraph getTumlGraph(String url) {
		File f = new File(url);
		Neo4jGraph db = new Neo4jGraph(f.getAbsolutePath());
		TransactionThreadEntityVar.remove();
		TumlGraph nakedGraph = new TumlNeo4jGraph(db);
		nakedGraph.addRoot();
		nakedGraph.registerListeners();		
		nakedGraph.commit();
		return nakedGraph;
	}

}