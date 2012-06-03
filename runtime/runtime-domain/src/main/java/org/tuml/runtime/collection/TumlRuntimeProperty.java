package org.tuml.runtime.collection;

public interface TumlRuntimeProperty {
	boolean isControllingSide();
	boolean isComposite();
	boolean isOneToOne(); 
	boolean isOneToMany(); 
	boolean isManyToOne(); 
	boolean isManyToMany();
	int getUpper();
	int getLower();
	String getLabel();
}