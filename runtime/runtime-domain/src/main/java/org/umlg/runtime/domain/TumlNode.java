package org.umlg.runtime.domain;

import com.tinkerpop.blueprints.Vertex;
import org.umlg.runtime.collection.Qualifier;
import org.umlg.runtime.collection.TinkerSet;
import org.umlg.runtime.collection.TumlRuntimeProperty;
import org.umlg.runtime.domain.ocl.OclAny;
import org.umlg.runtime.validation.TumlConstraintViolation;

import java.util.List;

public interface TumlNode extends TumlEnum, OclAny, PersistentObject {
    public static final String ALLINSTANCES_EDGE_LABEL = "allinstances";
	Vertex getVertex();
	boolean isTinkerRoot();
	void initialiseProperties();
	void initialiseProperty(TumlRuntimeProperty tumlRuntimeProperty, boolean inverse);
    TumlRuntimeProperty internalAdder(TumlRuntimeProperty tumlRuntimeProperty, boolean inverse, TumlNode umlgNode);
    void initVariables();
	List<Qualifier> getQualifiers(TumlRuntimeProperty tumlRuntimeProperty, TumlNode node, boolean inverse);
	void delete();
	int getSize(TumlRuntimeProperty tumlRuntimeProperty);
	<E> TinkerSet<E> asSet();
	List<TumlConstraintViolation> validateMultiplicities();
    List<TumlConstraintViolation> checkClassConstraints();
	TumlNode getOwningObject();
	<T extends TumlNode> List<T> getPathToCompositionalRoot();
    void addEdgeToMetaNode();
    TumlMetaNode getMetaNode();
}
