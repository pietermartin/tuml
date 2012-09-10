package org.tuml.runtime.collection.ocl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.set.ListOrderedSet;
import org.tuml.runtime.collection.TinkerBag;
import org.tuml.runtime.collection.TinkerCollection;
import org.tuml.runtime.collection.TinkerOrderedSet;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class OclStdLibOrderedSetImpl<E> extends OclStdLibCollectionImpl<E> implements TinkerOrderedSet<E> {

	private ListOrderedSet orderedSet;

	/**
	 * A regular constructor compiles in eclipse but not in maven
	 * @param collection
	 * @return
	 */
	public static <E> OclStdLibOrderedSetImpl<E> get(Collection<E> collection) {
		return new OclStdLibOrderedSetImpl<E>(ListOrderedSet.decorate(new ArrayList<E>(collection)));
	}
	
	@SuppressWarnings("unchecked")
	public OclStdLibOrderedSetImpl(ListOrderedSet orderedSet) {
		super(orderedSet);
		this.orderedSet = orderedSet;
	}
	
	@Override
	public TinkerOrderedSet<E> append(E e) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public TinkerOrderedSet<E> prepend(E e) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public TinkerOrderedSet<E> insertAt(Integer index, E e) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public TinkerOrderedSet<E> subOrderedSet(Integer lower, Integer upper) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public E at(Integer i) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public E first() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public E last() {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public TinkerOrderedSet<E> reverse() {
		throw new RuntimeException("Not implemented");
	}

	
	/***************************************************
	 * Iterate goodies
	 ***************************************************/
	
	@Override
	public TinkerOrderedSet<E> select(BooleanExpressionEvaluator<E> v) {
		ListOrderedSet result = new ListOrderedSet();
		for (E e : this.collection) {
			if (v.evaluate(e)) {
				result.add(e);
			}
		}
		return new OclStdLibOrderedSetImpl<E>(result);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R> TinkerBag<R> collectNested(BodyExpressionEvaluator<R, E> v) {
		Multiset<R> result = HashMultiset.create();
		for (Object e : this.orderedSet) {
			result.add(v.evaluate((E)e));
		}
		return new OclStdLibBagImpl<R>(result);
	}
	
	@Override
	public <T, R> TinkerBag<T> collect(BodyExpressionEvaluator<R, E> v) {
		return collectNested(v).flatten();
	}

	@Override
	public <R> TinkerOrderedSet<R> flatten() {
		ListOrderedSet result = new ListOrderedSet();
		for (Object e : this.orderedSet) {
			if (e instanceof TinkerCollection) {
				TinkerCollection<?> collection = (TinkerCollection<?>) e;
				result.addAll(collection.<R> flatten());
			} else {
				result.add(e);
			}
		}
		return new OclStdLibOrderedSetImpl<R>(result);
	}

	@Override
	public boolean isEmpty() {
		return this.orderedSet.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.orderedSet.contains(o);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Iterator<E> iterator() {
		return this.orderedSet.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.orderedSet.toArray();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] a) {
		return (T[]) this.orderedSet.toArray(a);
	}

	@Override
	public boolean add(E e) {
		return this.orderedSet.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return this.orderedSet.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.orderedSet.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return this.orderedSet.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.orderedSet.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.orderedSet.removeAll(c);
	}

	@Override
	public void clear() {
		this.orderedSet.clear();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return this.orderedSet.addAll(index, c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		return (E) this.orderedSet.get(index);
	}

	@Override
	public E set(int index, E element) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public void add(int index, E element) {
		this.orderedSet.add(index, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E remove(int index) {
		return (E) this.orderedSet.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return this.orderedSet.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public ListIterator<E> listIterator() {
		throw new RuntimeException("Not supported");
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public TinkerOrderedSet<E> including(E e) {
		if (e != null) {
			this.orderedSet.add(e);
		}
		return this;
	}

	@Override
	public String toJson() {
		//TODO
		throw new RuntimeException("Not yet implemented");
	}

}
