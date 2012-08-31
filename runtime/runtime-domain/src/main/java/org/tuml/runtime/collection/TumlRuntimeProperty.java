package org.tuml.runtime.collection;

public interface TumlRuntimeProperty {
	boolean isOnePrimitive();
	boolean isManyPrimitive();
	boolean isControllingSide();
	boolean isComposite();
	boolean isInverseComposite();
	boolean isOneToOne(); 
	boolean isOneToMany(); 
	boolean isManyToOne(); 
	boolean isManyToMany();
	int getUpper();
	int getLower();
	String getLabel();
	boolean isValid(int elementCount);
	boolean isQualified();
	boolean isInverseQualified();
	boolean isOrdered();
	boolean isInverseOrdered();
	boolean isUnique();
	String toJson();
}
