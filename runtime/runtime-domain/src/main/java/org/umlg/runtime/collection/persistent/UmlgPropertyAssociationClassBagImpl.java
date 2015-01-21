package org.umlg.runtime.collection.persistent;

import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Vertex;
import org.umlg.runtime.adaptor.UMLG;
import org.umlg.runtime.collection.UmlgCollection;
import org.umlg.runtime.collection.UmlgPropertyAssociationClassBag;
import org.umlg.runtime.collection.UmlgRuntimeProperty;
import org.umlg.runtime.domain.AssociationClassNode;
import org.umlg.runtime.domain.UmlgNode;

import java.util.Set;

/**
 * Date: 2013/06/18
 * Time: 5:59 PM
 */
public class UmlgPropertyAssociationClassBagImpl<E, AC extends AssociationClassNode> extends UmlgBagImpl<E> implements UmlgPropertyAssociationClassBag<E, AC> {

    public UmlgPropertyAssociationClassBagImpl(UmlgNode owner, UmlgRuntimeProperty runtimeProperty, UmlgRuntimeProperty associationClassRuntimeProperty) {
        super(owner, runtimeProperty);
    }

    @Override
    public boolean add(E e, AC associationClass) {
        if (super.add(e)) {
            associationClass.internalAdder(umlgRuntimeProperty, true, this.owner);
            associationClass.internalAdder(umlgRuntimeProperty, false, (UmlgNode) e);
            this.edge.property(UmlgCollection.ASSOCIATION_CLASS_VERTEX_ID, associationClass.getId());
            this.edge.property("className", associationClass.getClass().getName());
            associationClass.z_internalCopyOnePrimitivePropertiesToEdge(this.edge);
            associationClass.getVertex().property(UmlgCollection.ASSOCIATION_CLASS_EDGE_ID, this.edge.id().toString());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean add(E e) {
        throw new RuntimeException("The standard add method can not be used on an edge that represents and association class. Please use the add(element, associationClass) method.");
    }

    @Override
    public boolean remove(Object o) {
        maybeLoad();
        Vertex v;
        if (o instanceof UmlgNode) {
            UmlgNode node = (UmlgNode) o;
            v = node.getVertex();
            removeEdge(v);
        } else if (o.getClass().isEnum()) {
            v = removeFromInternalMap(o);
            removeEdge(v);
            v.remove();
        } else if (isOnePrimitive() || getDataTypeEnum() != null) {
            throw new IllegalStateException("one primitive or data type can not have an association class.");
        } else {
            v = removeFromInternalMap(o);
            removeEdge(v);
            v.remove();
        }

        return super.remove(o);
    }

    private void removeEdge(Vertex v) {
        Set<Edge> edges = UMLG.get().getEdgesBetween(this.vertex, v, this.getLabel());
        for (Edge edge : edges) {
            Vertex associationClassVertex = UMLG.get().V(edge.value(UmlgCollection.ASSOCIATION_CLASS_VERTEX_ID)).next();
            //The remove code will delete all in and out edges
            associationClassVertex.remove();
        }
    }

}