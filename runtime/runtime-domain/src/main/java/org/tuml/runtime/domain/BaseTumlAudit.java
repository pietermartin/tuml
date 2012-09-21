package org.tuml.runtime.domain;

import java.io.Serializable;

import org.joda.time.DateTime;
import org.tuml.runtime.adaptor.TransactionThreadVar;
import org.tuml.runtime.util.TinkerFormatter;

import com.tinkerpop.blueprints.Vertex;

public abstract class BaseTumlAudit extends BaseTuml implements TinkerAuditableNode, Serializable {

	private static final long serialVersionUID = 3751023772087546585L;
	protected Vertex auditVertex;

	public BaseTumlAudit() {
		super();
	}

	public Vertex getAuditVertex() {
		return auditVertex;
	}

	public DateTime getDeletedOn() {
		return TinkerFormatter.parseDateTime((String) this.vertex.getProperty("deletedOn"));
	}

	public void setDeletedOn(DateTime deletedOn) {
		this.vertex.setProperty("deletedOn", TinkerFormatter.format(deletedOn));
		if ( TransactionThreadVar.hasNoAuditEntry(getClass().getName() + getUid()) ) {
			createAuditVertex(false);
		}
		getAuditVertex().setProperty("deletedOn", TinkerFormatter.format(deletedOn));
	}

	public void defaultCreate() {
		super.defaultCreate();
		this.vertex.setProperty("deletedOn", TinkerFormatter.format(new DateTime(1000L * 60 * 60 * 24 * 365 * 1000)));
	}

}
