package org.umlg.runtime.domain.activity;

import com.tinkerpop.blueprints.Vertex;

import java.util.List;

/*
 * Join node can have one in control flow and one in object flow. In this case an object flow flows out
 */
public abstract class ManyJoinNodeObjectTokenKnownWithInControlToken<O> extends JoinNodeObjectTokenKnownWithInControlToken<O, CollectionObjectToken<O>> {

	public ManyJoinNodeObjectTokenKnownWithInControlToken() {
		super();
	}

	public ManyJoinNodeObjectTokenKnownWithInControlToken(Vertex vertex) {
		super(vertex);
	}

	public ManyJoinNodeObjectTokenKnownWithInControlToken(boolean persist, String name) {
		super(persist, name);
	}

	@Override
	protected abstract ManyObjectFlowKnown<O> getOutFlow();

	@Override
	public abstract List<ActivityEdge<Token>> getIncoming();

}