package org.tuml.runtime.collection;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.collections.set.ListOrderedSet;
import org.tuml.runtime.adaptor.GraphDb;
import org.tuml.runtime.adaptor.NakedTinkerIndex;
import org.tuml.runtime.domain.CompositionNode;
import org.tuml.runtime.domain.TinkerNode;

import com.tinkerpop.blueprints.pgm.CloseableSequence;
import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Vertex;

public class TinkerOrderedSetImpl<E> extends BaseCollection<E> implements TinkerOrderedSet<E> {

	protected NakedTinkerIndex<Edge> index;
	
	protected ListOrderedSet getInternalListOrderedSet() {
		return (ListOrderedSet) this.internalCollection;
	}
	
	@SuppressWarnings("unchecked")
	public TinkerOrderedSetImpl(CompositionNode owner, String uid, TumlRuntimeProperty multiplicity) {
		super();
		this.internalCollection =  new ListOrderedSet();
		this.owner = owner;
		this.vertex = owner.getVertex();
		this.parentClass = owner.getClass();
		this.index = GraphDb.getDb().getIndex(uid + ":::" + getLabel(), Edge.class);
		if (this.index == null) {
			this.index = GraphDb.getDb().createManualIndex(uid + ":::" + getLabel(), Edge.class);
		}
		this.tumlRuntimeProperty = multiplicity;
	}
	
	@Override
	public boolean add(E e) {
		maybeCallInit(e);
		maybeLoad();
		boolean result = this.getInternalListOrderedSet().add(e);
		if (result) {
			Edge edge = addInternal(e);
			this.index.put("index", new Float(this.getInternalListOrderedSet().size() - 1), edge);
			getVertexForDirection(edge).setProperty("tinkerIndex", new Float(this.getInternalListOrderedSet().size() - 1));
		}
		return result;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		maybeLoad();
		int indexOf = index;
		for (E e : c) {
			this.add(indexOf++, e);
		}
		return true;
	}
	
	@Override
	public void add(int indexOf, E e) {
		maybeCallInit(e);
		maybeLoad();
		addToListAndListIndex(indexOf, e);
	}

	@SuppressWarnings("unchecked")
	protected Edge addToListAndListIndex(int indexOf, E e) {
		E previous = (E)this.getInternalListOrderedSet().get(indexOf - 1);
		E current = (E)this.getInternalListOrderedSet().get(indexOf);
		this.getInternalListOrderedSet().add(indexOf, e);
		Edge edge = addInternal(e);

		float min;
		float max;
		if (e instanceof CompositionNode) {
			min = (Float) ((CompositionNode)previous).getVertex().getProperty("tinkerIndex");
			max = (Float) ((CompositionNode)current).getVertex().getProperty("tinkerIndex");
		} else if (e.getClass().isEnum()) {
			min = (Float) this.internalVertexMap.get(((Enum<?>) previous).name()).getProperty("tinkerIndex");
			max = (Float) this.internalVertexMap.get(((Enum<?>) current).name()).getProperty("tinkerIndex");
		} else {
			min = (Float) this.internalVertexMap.get(previous).getProperty("tinkerIndex");
			max = (Float) this.internalVertexMap.get(current).getProperty("tinkerIndex");
		}
		float tinkerIndex = (min + max) / 2; 
		this.index.put("index", tinkerIndex, edge);
		getVertexForDirection(edge).setProperty("tinkerIndex", tinkerIndex);
		return edge;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void loadFromVertex() {
		CloseableSequence<Edge> edges = this.index.queryList(0F, true, false);
		for (Edge edge : edges) {
			E node = null;
			try {
				Class<?> c = this.getClassToInstantiate(edge);
				Object value = this.getVertexForDirection(edge).getProperty("value");
				if (c.isEnum()) {
					node = (E) Enum.valueOf((Class<? extends Enum>) c, (String) value);
					this.internalVertexMap.put(value, this.getVertexForDirection(edge));
				} else if (TinkerNode.class.isAssignableFrom(c)) {
					node = (E) c.getConstructor(Vertex.class).newInstance(this.getVertexForDirection(edge));
				} else {
					node = (E) value;
					this.internalVertexMap.put(value, this.getVertexForDirection(edge));
				}
				this.getInternalListOrderedSet().add(node);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		this.loaded = true;
	}

	@Override
	public boolean remove(Object o) {
		maybeLoad();
		int indexOf = this.getInternalListOrderedSet().indexOf(o);
		boolean result = this.getInternalListOrderedSet().remove(o);
		if (result) {
			Vertex v;
			if (o instanceof CompositionNode) {
				CompositionNode node = (CompositionNode) o;
				v = node.getVertex();
				Set<Edge> edges = GraphDb.getDb().getEdgesBetween(this.vertex, v, this.getLabel());
				for (Edge edge : edges) {
					removeEdgefromIndex(v, edge, indexOf);
					GraphDb.getDb().removeEdge(edge);
				}
			} else if (o.getClass().isEnum()) {
				v = this.internalVertexMap.get(((Enum<?>) o).name());
				Edge edge = v.getInEdges(this.getLabel()).iterator().next();
				removeEdgefromIndex(v, edge, indexOf);
				GraphDb.getDb().removeVertex(v);
			} else {
				v = this.internalVertexMap.get(o);
				Edge edge = v.getInEdges(this.getLabel()).iterator().next();
				removeEdgefromIndex(v, edge, indexOf);
				GraphDb.getDb().removeVertex(v);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		maybeLoad();
		for (E e : new HashSet<E>(this.getInternalListOrderedSet())) {
			this.remove(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public E get(int index) {
		if (!this.loaded) {
			loadFromVertex();
		}
		return (E) this.getInternalListOrderedSet().get(index);
	}

	@Override
	public E set(int index, E element) {
		E removedElement = this.remove(index);
		this.add(index, element);
		return removedElement;
	}

	@Override
	public E remove(int index) {
		E e = this.get(index);
		this.remove(e);
		return e;
	}

	@Override
	public int indexOf(Object o) {
		maybeLoad();
		return this.getInternalListOrderedSet().indexOf(o);
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

	protected void removeEdgefromIndex(Vertex v, Edge edge, int indexOf) {
		this.index.remove("index", v.getProperty("tinkerIndex"), edge);
	}

}