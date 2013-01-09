package org.tuml.runtime.adaptor;

import com.tinkerpop.blueprints.Vertex;

public class TumlNeo4jIdUtilImpl implements TinkerIdUtil {

	private static TinkerIdUtil INSTANCE = new TumlNeo4jIdUtilImpl();
	
	private TumlNeo4jIdUtilImpl() {
		super();
	}

	public static TinkerIdUtil getInstance() {
		return INSTANCE;
	}
	
	@Override
	public Long getId(Vertex v) {
		return (Long)v.getId();
	}
	
	@Override
	public void setId(Vertex v, Long id) {
		throw new IllegalStateException("Id can not be set using Neo4j");
	}
	
	@Override
	public int getVersion(Vertex v) {
		return -1;
	}	
	
}