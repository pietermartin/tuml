package org.tuml.runtime.domain.neo4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.tuml.runtime.adaptor.BaseTumlGraph;
import org.tuml.runtime.adaptor.TumlGraph;
import org.tuml.runtime.adaptor.TumlTinkerIndex;
import org.tuml.runtime.adaptor.TransactionThreadEntityVar;
import org.tuml.runtime.domain.PersistentObject;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jEdge;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jVertex;

public class TumlNeo4jGraph extends BaseTumlGraph implements TumlGraph {

    private static final long serialVersionUID = 7025198246796291511L;
    private Neo4jGraph neo4jGraph;
    private TransactionEventHandler<PersistentObject> transactionEventHandler;

    public TumlNeo4jGraph(Neo4jGraph neo4jGraph) {
        super(neo4jGraph);
        this.neo4jGraph = neo4jGraph;
    }

    public Neo4jGraph getNeo4jGraph() {
        return this.neo4jGraph;
    }

    @Override
    public <T extends Element> Index<T> createIndex(String indexName, Class<T> indexClass, Parameter... indexParameters) {
        return new TumlNeo4jIndex(this.neo4jGraph.createIndex(indexName, indexClass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Element> TumlTinkerIndex<T> createIndex(String indexName, Class<T> indexClass) {
        return new TumlNeo4jIndex(this.neo4jGraph.createIndex(indexName, indexClass));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Element> TumlTinkerIndex<T> getIndex(String indexName, Class<T> indexClass) {
        Index<T> index = this.neo4jGraph.getIndex(indexName, indexClass);
        if (index != null) {
            return new TumlNeo4jIndex(index);
        } else {
            return null;
        }
    }

    @Override
    public Set<Edge> getEdgesBetween(Vertex v1, Vertex v2, String... labels) {
        Node n1 = ((Neo4jVertex) v1).getRawVertex();
        Node n2 = ((Neo4jVertex) v2).getRawVertex();
        List<DynamicRelationshipType> dynaRel = new ArrayList<DynamicRelationshipType>(labels.length);
        for (String label : labels) {
            dynaRel.add(DynamicRelationshipType.withName(label));
        }
        Set<Edge> result = new HashSet<Edge>(dynaRel.size());
        Iterable<Relationship> relationships = n1.getRelationships(dynaRel.toArray(new DynamicRelationshipType[]{}));
        for (Relationship relationship : relationships) {
            if ((relationship.getStartNode().equals(n1) && relationship.getEndNode().equals(n2))
                    || (relationship.getStartNode().equals(n2) && relationship.getEndNode().equals(n1))) {

                result.add(this.neo4jGraph.getEdge(relationship.getId()));
            }
        }
        return result;
    }

    @Override
    public void addRoot() {
        try {
            this.neo4jGraph.getRawGraph().getNodeById(1);
        } catch (NotFoundException e) {
            try {
                ((EmbeddedGraphDatabase) this.neo4jGraph.getRawGraph()).getTxManager().begin();
                ((EmbeddedGraphDatabase) this.neo4jGraph.getRawGraph()).getNodeManager().setReferenceNodeId(this.neo4jGraph.getRawGraph().createNode().getId());
                ((EmbeddedGraphDatabase) this.neo4jGraph.getRawGraph()).getTxManager().commit();
            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
            Vertex root = getRoot();
            root.setProperty("transactionCount", 1);
        }
    }

    @Override
    public Vertex getRoot() {
        return this.neo4jGraph.getVertex(1L);
    }

    @Override
    public long countVertices() {
        return ((EmbeddedGraphDatabase) this.neo4jGraph.getRawGraph()).getNodeManager().getNumberOfIdsInUse(Node.class) - 1;
    }

    @Override
    public long countEdges() {
        return ((EmbeddedGraphDatabase) this.neo4jGraph.getRawGraph()).getNodeManager().getNumberOfIdsInUse(Relationship.class);
    }

    @Override
    public void registerListeners() {
        if (this.transactionEventHandler == null) {
            this.transactionEventHandler = new TumlTransactionEventHandler<PersistentObject>();
            this.neo4jGraph.getRawGraph().registerTransactionEventHandler(this.transactionEventHandler);
        }
    }

    @Override
    public TransactionManager getTransactionManager() {
        return ((AbstractGraphDatabase) this.neo4jGraph.getRawGraph()).getTxManager();
    }

    @Override
    public void resume(Transaction t) {
        try {
            getTransactionManager().resume(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction suspend() {
        try {
            return getTransactionManager().suspend();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction getTransaction() {
        try {
            return getTransactionManager().getTransaction();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setCheckElementsInTransaction(boolean b) {
        this.neo4jGraph.setCheckElementsInTransaction(b);
    }

    @Override
    public boolean hasEdgeBeenDeleted(Edge edge) {
        Neo4jEdge neo4jEdge = (Neo4jEdge) edge;
        try {
            neo4jEdge.getRawEdge().hasProperty("asd");
            return false;
        } catch (Exception e) {
            return true;
        }
        // The way below requires a transaction to have been started.

        // Neo4jEdge neo4jEdge = (Neo4jEdge) edge;
        // EmbeddedGraphDatabase g =
        // (EmbeddedGraphDatabase)this.neo4jGraph.getRawGraph();
        // for (Relationship r :
        // g.getNodeManager().getTransactionData().deletedRelationships()) {
        // if (neo4jEdge.getRawEdge().equals(r)) {
        // return true;
        // }
        // }
        // return false;
    }

}
