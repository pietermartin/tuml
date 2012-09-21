package org.tuml.runtime.domain;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;
import org.tuml.runtime.collection.TinkerSet;
import org.tuml.runtime.collection.memory.TumlMemorySet;
import org.tuml.runtime.domain.ocl.OclState;
import org.tuml.runtime.util.TinkerFormatter;

import com.tinkerpop.blueprints.Vertex;

public abstract class BaseTuml implements TumlNode, Serializable {

	private static final long serialVersionUID = 3751023772087546585L;
	protected Vertex vertex;
	protected boolean hasInitBeenCalled = false;

	public BaseTuml() {
		super();
	}

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	public DateTime getCreatedOn() {
		return TinkerFormatter.parseDateTime((String) this.vertex.getProperty("createdOn"));
	}

	public void setCreatedOn(DateTime createdOn) {
		this.vertex.setProperty("createdOn", TinkerFormatter.format(createdOn));
	}

	public DateTime getUpdatedOn() {
		return TinkerFormatter.parseDateTime((String) this.vertex.getProperty("updatedOn"));
	}

	public void setUpdatedOn(DateTime updatedOn) {
		this.vertex.setProperty("updatedOn", TinkerFormatter.format(updatedOn));
	}

	public void defaultCreate() {
		setCreatedOn(new DateTime());
		setUpdatedOn(new DateTime());
		getUid();
	}

	public void defaultUpdate() {
		setUpdatedOn(new DateTime());
	}

	public String getName() {
		return getClass().getName() + "[" + getId() + "]";
	}

	public boolean hasInitBeenCalled() {
		return hasInitBeenCalled;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(getId()).toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		BaseTuml rhs = (BaseTuml) obj;
		return new EqualsBuilder().append(getId(), rhs.getId()).isEquals();
	}

	@Override
	public boolean notEquals(Object object) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Boolean oclIsNew() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Boolean oclIsUndefined() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Boolean oclIsInvalid() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public <T> T oclAsType(T classifier) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Boolean oclIsTypeOf(Object object) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Boolean oclIsKindOf(Object object) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Boolean oclIsInState(OclState state) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public <T  extends Object> Class<T> oclType() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public String oclLocale() {
		throw new RuntimeException("Not implemented");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <E> TinkerSet<E> asSet() {
		TinkerSet<E> result = new TumlMemorySet<E>();
		result.add((E)this);
		return result;
	}

}
