package org.umlg.runtime.collection.persistent;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.collections.set.ListOrderedSet;
import org.umlg.runtime.adaptor.GraphDb;
import org.umlg.runtime.collection.TinkerOrderedSet;
import org.umlg.runtime.collection.TinkerSequence;
import org.umlg.runtime.collection.TumlRuntimeProperty;
import org.umlg.runtime.collection.ocl.BodyExpressionEvaluator;
import org.umlg.runtime.collection.ocl.BooleanExpressionEvaluator;
import org.umlg.runtime.collection.ocl.OclStdLibOrderedSet;
import org.umlg.runtime.collection.ocl.OclStdLibOrderedSetImpl;
import org.umlg.runtime.domain.TumlNode;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

/**
 * Date: 2013/03/02
 * Time: 9:18 PM
 */
public abstract class TumlBaseOrderedSet<E> extends BaseCollection<E> implements TinkerOrderedSet<E> {

    protected OclStdLibOrderedSet<E> oclStdLibOrderedSet;

    @SuppressWarnings("unchecked")
    public TumlBaseOrderedSet(TumlNode owner, TumlRuntimeProperty runtimeProperty) {
        super(owner, runtimeProperty);
        this.internalCollection = new ListOrderedSet();
        this.oclStdLibOrderedSet = new OclStdLibOrderedSetImpl<E>((ListOrderedSet) this.internalCollection);
        this.oclStdLibCollection = this.oclStdLibOrderedSet;
    }

    protected ListOrderedSet getInternalListOrderedSet() {
        return (ListOrderedSet) this.internalCollection;
    }

    @SuppressWarnings("unchecked")
    protected Edge addToListAtIndex(int indexOf, E e) {
        //If it is the last element then the edge (LABEL_TO_LAST_ELEMENT_IN_SEQUENCE) needs to be moved
        this.getInternalListOrderedSet().add(indexOf, e);
        //Add the regular edge for the label
        Edge edge = addInternal(e);
        //If it is the first element then the edge (LABEL_TO_FIRST_ELEMENT_IN_SEQUENCE) needs to be moved
        if (indexOf == 0) {
            if (this.vertex.getEdges(Direction.OUT, LABEL_TO_FIRST_ELEMENT_IN_SEQUENCE + getLabel()).iterator().hasNext()) {
                Edge edgeToFirstElement = this.vertex.getEdges(Direction.OUT, LABEL_TO_FIRST_ELEMENT_IN_SEQUENCE + getLabel()).iterator().next();
                GraphDb.getDb().removeEdge(edgeToFirstElement);
            }
            GraphDb.getDb().addEdge(null, this.vertex, edge.getVertex(Direction.IN), LABEL_TO_FIRST_ELEMENT_IN_SEQUENCE + getLabel());
        }
        if (indexOf == size() - 1) {
            if (this.vertex.getEdges(Direction.OUT, LABEL_TO_LAST_ELEMENT_IN_SEQUENCE + getLabel()).iterator().hasNext()) {
                Edge edgeToLastElement = this.vertex.getEdges(Direction.OUT, LABEL_TO_LAST_ELEMENT_IN_SEQUENCE + getLabel()).iterator().next();
                GraphDb.getDb().removeEdge(edgeToLastElement);
            }
            GraphDb.getDb().addEdge(null, this.vertex, edge.getVertex(Direction.IN), LABEL_TO_LAST_ELEMENT_IN_SEQUENCE + getLabel());
        }
        //Shift the linked list
        //Find the element at the index
        if (size() > 1) {
            if (indexOf == 0) {
                //add a edge to the previous first element
                E previous = (E) this.getInternalListOrderedSet().get(1);
                Vertex previousVertex = getVertexFromElement(previous, e);
                GraphDb.getDb().addEdge(null, edge.getVertex(Direction.IN), previousVertex, LABEL_TO_NEXT_IN_SEQUENCE);
            } else {
                E previous = (E) this.getInternalListOrderedSet().get(indexOf - 1);
                Vertex previousVertex = getVertexFromElement(previous, e);
                //size already includes the current added element, so if the size is 2 it means that there was only one in the list, i.e. no edge (LABEL_TO_NEXT_IN_SEQUENCE)
                if (size() > 2 && indexOf + 1 < size()) {
                    Edge edgeToNextElement = previousVertex.getEdges(Direction.OUT, LABEL_TO_NEXT_IN_SEQUENCE).iterator().next();
                    Vertex shiftedVertex = edgeToNextElement.getVertex(Direction.IN);
                    GraphDb.getDb().removeEdge(edgeToNextElement);
                    GraphDb.getDb().addEdge(null, edge.getVertex(Direction.IN), shiftedVertex, LABEL_TO_NEXT_IN_SEQUENCE);
                }
                GraphDb.getDb().addEdge(null, previousVertex, edge.getVertex(Direction.IN), LABEL_TO_NEXT_IN_SEQUENCE);
            }
        }
        return edge;
    }

    @Override
    protected void addToLinkedList(Edge edge) {
        //Get the new vertex for the element
        Vertex newElementVertex = getVertexForDirection(edge);
        if (this.vertex.getEdges(Direction.OUT, LABEL_TO_LAST_ELEMENT_IN_SEQUENCE + getLabel()).iterator().hasNext()) {
            Edge edgeToLastVertex = this.vertex.getEdges(Direction.OUT, LABEL_TO_LAST_ELEMENT_IN_SEQUENCE + getLabel()).iterator().next();
            Vertex lastVertex = edgeToLastVertex.getVertex(Direction.IN);

            //move the edge to the last vertex
            GraphDb.getDb().removeEdge(edgeToLastVertex);
            GraphDb.getDb().addEdge(null, this.vertex, newElementVertex, LABEL_TO_LAST_ELEMENT_IN_SEQUENCE + getLabel());

            //add the element to the linked list
            GraphDb.getDb().addEdge(null, lastVertex, newElementVertex, LABEL_TO_NEXT_IN_SEQUENCE);

        } else {
            //its the first element in the list
            GraphDb.getDb().addEdge(null, this.vertex, newElementVertex, LABEL_TO_FIRST_ELEMENT_IN_SEQUENCE + getLabel());
            GraphDb.getDb().addEdge(null, this.vertex, newElementVertex, LABEL_TO_LAST_ELEMENT_IN_SEQUENCE + getLabel());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void loadFromVertex() {
        Direction direction;
        Direction inverseDirection;
        if (isControllingSide()) {
            direction = Direction.OUT;
            inverseDirection = Direction.IN;
        } else {
            direction = Direction.IN;
            inverseDirection = Direction.OUT;
        }
        if (getUpper() == 1) {
            Iterable<Edge> edgeIterable = this.vertex.getEdges(direction, getLabel());
            if (!edgeIterable.iterator().hasNext()) {
                this.loaded = true;
            } else {
                Edge edgeToElement = edgeIterable.iterator().next();
                if (!GraphDb.getDb().hasEdgeBeenDeleted(edgeToElement)) {
                    Vertex element = edgeToElement.getVertex(inverseDirection);
                    loadNode(edgeToElement, element);
                    this.loaded = true;
                } else {
                    this.loaded = true;
                }
            }
        } else {
            Iterable<Edge> edgeIterable = this.vertex.getEdges(direction, LABEL_TO_FIRST_ELEMENT_IN_SEQUENCE + getLabel());
            if (!edgeIterable.iterator().hasNext()) {
                this.loaded = true;
            } else {
                Edge edgeToFirstElement = edgeIterable.iterator().next();
                if (!GraphDb.getDb().hasEdgeBeenDeleted(edgeToFirstElement)) {
                    Vertex firstVertexInSequence = edgeToFirstElement.getVertex(inverseDirection);
                    loadNode(edgeToFirstElement, firstVertexInSequence);
                    Vertex elementVertex = firstVertexInSequence;
                    while (elementVertex.getEdges(Direction.OUT, LABEL_TO_NEXT_IN_SEQUENCE).iterator().hasNext()) {
                        Edge edgeToNext = elementVertex.getEdges(Direction.OUT, LABEL_TO_NEXT_IN_SEQUENCE).iterator().next();
                        if (!GraphDb.getDb().hasEdgeBeenDeleted(edgeToNext)) {
                            elementVertex = edgeToNext.getVertex(Direction.IN);
                            loadNode(edgeToNext, elementVertex);
                        }
                    }
                    this.loaded = true;
                } else {
                    this.loaded = true;
                }
            }
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
    public E remove(int index) {
        E e = this.get(index);
        this.remove(e);
        return e;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void clear() {
        maybeLoad();
        for (E e : new HashSet<E>(this.getInternalListOrderedSet())) {
            this.remove(e);
        }
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

    @Override
    public <R> TinkerSequence<R> collectNested(BodyExpressionEvaluator<R, E> v) {
        maybeLoad();
        return this.oclStdLibOrderedSet.collectNested(v);
    }

    @Override
    public <T, R> TinkerSequence<T> collect(BodyExpressionEvaluator<R, E> v) {
        maybeLoad();
        return this.oclStdLibOrderedSet.collect(v);
    }

    @Override
    public <T2> TinkerSequence<T2> flatten() {
        maybeLoad();
        return this.oclStdLibOrderedSet.flatten();
    }

    @Override
    public TinkerOrderedSet<E> select(BooleanExpressionEvaluator<E> v) {
        maybeLoad();
        return this.oclStdLibOrderedSet.select(v);
    }

    @Override
    public TinkerOrderedSet<E> append(E e) {
        maybeLoad();
        return this.oclStdLibOrderedSet.append(e);
    }

    @Override
    public TinkerOrderedSet<E> prepend(E e) {
        maybeLoad();
        return this.oclStdLibOrderedSet.prepend(e);
    }

    @Override
    public TinkerOrderedSet<E> insertAt(Integer index, E e) {
        maybeLoad();
        return this.oclStdLibOrderedSet.insertAt(index, e);
    }

    @Override
    public TinkerOrderedSet<E> subOrderedSet(Integer lower, Integer upper) {
        maybeLoad();
        return this.oclStdLibOrderedSet.subOrderedSet(lower, upper);
    }

    @Override
    public E at(Integer i) {
        maybeLoad();
        return this.oclStdLibOrderedSet.at(i);
    }

    @Override
    public E first() {
        maybeLoad();
        return this.oclStdLibOrderedSet.first();
    }

    @Override
    public E last() {
        maybeLoad();
        return this.oclStdLibOrderedSet.last();
    }

    @Override
    public TinkerOrderedSet<E> reverse() {
        maybeLoad();
        return this.oclStdLibOrderedSet.reverse();
    }

    @Override
    public TinkerOrderedSet<E> including(E e) {
        maybeLoad();
        return this.oclStdLibOrderedSet.including(e);
    }

    private Vertex getVertexFromElement(E previous, E e) {
        Vertex previousVertex;
        if (previous instanceof TumlNode) {
            TumlNode node = (TumlNode) previous;
            previousVertex = node.getVertex();
        } else if (e.getClass().isEnum()) {
            previousVertex = this.internalVertexMap.get(((Enum<?>) previous).name());
        } else {
            previousVertex = this.internalVertexMap.get(previous);
        }
        return previousVertex;
    }

    protected void loadNode(Edge edgeToFirstElement, Vertex vertex) {
        E node;
        try {
            Class<?> c = this.getClassToInstantiate(edgeToFirstElement);
            if (TumlNode.class.isAssignableFrom(c)) {
                node = (E) c.getConstructor(Vertex.class).newInstance(vertex);
            } else if (c.isEnum()) {
                Object value = vertex.getProperty("value");
                node = (E) Enum.valueOf((Class<? extends Enum>) c, (String) value);
                this.internalVertexMap.put(value, vertex);
            } else {
                Object value = vertex.getProperty("value");
                node = (E) value;
                this.internalVertexMap.put(value, vertex);
            }
            this.getInternalListOrderedSet().add(node);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
