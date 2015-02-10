package org.umlg.runtime.adaptor;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.process.computer.GraphComputer;
import com.tinkerpop.gremlin.process.graph.traversal.GraphTraversal;
import com.tinkerpop.gremlin.structure.*;
import com.tinkerpop.gremlin.structure.strategy.ReadOnlyStrategy;
import org.apache.commons.configuration.Configuration;
import org.umlg.runtime.collection.Filter;
import org.umlg.runtime.collection.UmlgSet;
import org.umlg.runtime.collection.memory.UmlgMemorySet;
import org.umlg.runtime.domain.PersistentObject;
import org.umlg.runtime.domain.UmlgApplicationNode;
import org.umlg.runtime.util.UmlgProperties;
import org.umlg.sqlg.structure.SqlgGraph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Date: 2013/01/09
 * Time: 8:09 PM
 */
public class UmlgSqlgGraph implements UmlgGraph, UmlgAdminGraph {

    private UmlgTransactionEventHandlerImpl transactionEventHandler;
    private Class<UmlgApplicationNode> umlgApplicationNodeClass;
    protected SqlgGraph sqlG;
    //cache the root vertex
    private Vertex rootVertex;

    public UmlgSqlgGraph(Configuration config) {
        this.sqlG = SqlgGraph.open(config);
        this.transactionEventHandler = new UmlgTransactionEventHandlerImpl();
    }

    public UmlgSqlgGraph(SqlgGraph sqlgGraph) {
        this.sqlG = sqlgGraph;
        this.transactionEventHandler = new UmlgTransactionEventHandlerImpl();
    }

    void setBypass(boolean bypasss) {
        this.transactionEventHandler.setBypass(bypasss);
    }

    @Override
    public Graph getReadOnlyGraph() {
        return this.sqlG.strategy(ReadOnlyStrategy.instance());
    }

    @Override
    public Configuration configuration() {
        return this.sqlG.configuration();
    }

    @Override
    public Iterators iterators() {
        return this.sqlG.iterators();
    }

    @Override
    public void batchModeOn() {
        this.sqlG.tx().batchModeOn();
    }

    @Override
    public boolean isInBatchMode() {
        return this.sqlG.tx().isInBatchMode();
    }

    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass, final UmlgParameter... indexUmlgParameters) {
        if (elementClass == null) {
            throw Exceptions.classForElementCannotBeNull();
        }

        UmlgParameter<String, Class<?>> indexUmlgParameter = indexUmlgParameters[0];
        UmlgParameter<String, Boolean> uniqueUmlgParameter = indexUmlgParameters[1];
        UmlgParameter<String, String> labelUmlgParameter = indexUmlgParameters[2];

        if (Vertex.class.isAssignableFrom(elementClass)) {
            this.tx().readWrite();
            if (uniqueUmlgParameter.getValue()) {
                //TODO
//                this.sqlG.createUniqueConstraint(labelParameter.getValue(), key);
                this.sqlG.createVertexLabeledIndex(labelUmlgParameter.getValue(), key, SqlgDefaultValueUtil.valueFor(indexUmlgParameter.getValue()));
            } else {
                this.sqlG.createVertexLabeledIndex(labelUmlgParameter.getValue(), key, SqlgDefaultValueUtil.valueFor(indexUmlgParameter.getValue()));

            }
        } else if (Edge.class.isAssignableFrom(elementClass)) {
            this.tx().readWrite();
            this.sqlG.createEdgeLabeledIndex(labelUmlgParameter.getValue(), key);
        } else {
            throw Exceptions.classIsNotIndexable(elementClass);
        }
    }

    /**
     * Generic for all graphs start
     */
    @Override
    public void incrementTransactionCount() {
//        this.getRoot().property("transactionCount", (Integer) this.getRoot().value("transactionCount") + 1);
    }

    @Override
    public long getTransactionCount() {
        return this.getRoot().value("transactionCount");
    }

    @Override
    public void addRoot() {
        Vertex root = addVertex(ROOT_VERTEX);
        root.property("transactionCount", 1);
        root.property("className", ROOT_CLASS_NAME);
    }

    public void commit() {
        try {
            //This null check is here for when a graph is created. It calls commit before the listener has been set.
            if (this.transactionEventHandler != null) {
                this.transactionEventHandler.beforeCommit();
            }
            this.sqlG.tx().commit();
        } finally {
            TransactionThreadEntityVar.remove();
            TransactionThreadMetaNodeVar.remove();
            //This may start a new transaction
            this.transactionEventHandler.afterCommit();
            TransactionThreadNotificationVar.remove();
        }
    }

    public void rollback() {
        try {
            this.sqlG.tx().rollback();
        } finally {
            TransactionThreadEntityVar.remove();
            TransactionThreadMetaNodeVar.remove();
            TransactionThreadNotificationVar.remove();
        }
    }

    @Override
    public <TT extends PersistentObject> UmlgSet<TT> allInstances(String className) {
        UmlgMemorySet<TT> result = new UmlgMemorySet();
        String label;
        int lastIndexOfDot = className.lastIndexOf(".");
        if (lastIndexOfDot != -1) {
            label = className.substring(lastIndexOfDot + 1);
        } else {
            label = className;
        }
        this.sqlG.V().<Vertex>has(T.label, label).forEachRemaining (
                vertex -> result.add(UMLG.get().<TT>getEntity(vertex))
        );
        return result;
    }

    @Override
    public <TT extends PersistentObject> UmlgSet<TT> allInstances(String className, Filter filter) {
        UmlgMemorySet<TT> result = new UmlgMemorySet();
        String label;
        int lastIndexOfDot = className.lastIndexOf(".");
        if (lastIndexOfDot != -1) {
            label = className.substring(lastIndexOfDot + 1);
        } else {
            label = className;
        }
        this.sqlG.V().<Vertex>has(T.label, label).forEachRemaining (
                vertex -> {
                    TT entity = UMLG.get().<TT>getEntity(vertex);
                    if (filter.filter(entity)) {
                        result.add(entity);
                    }
                }
        );
        return result;
    }

    @Override
    public Vertex addVertex(String className) {
        String label;
        if (className != null) {
            label = shortenClassName(className);
            return this.sqlG.addVertex(T.label, label);
        } else {
            return this.sqlG.addVertex();
        }
    }

    private String shortenClassName(String className) {
        int lastIndexOfDot = className.lastIndexOf(".");
        if (lastIndexOfDot != -1) {
            return className.substring(lastIndexOfDot + 1);
        } else {
            lastIndexOfDot = className.lastIndexOf(":");
            if (lastIndexOfDot != -1) {
                return className.substring(lastIndexOfDot + 1);
            } else {
                return className;
            }
        }
    }

    @Override
    public void addDeletionNode() {
        Vertex v = addVertex(DELETION_VERTEX);
        getRoot().addEdge(DELETED_VERTEX_EDGE, v);
    }

    private Vertex getDeletionVertex() {
        if (getRoot() != null && getRoot().outE(DELETED_VERTEX_EDGE).hasNext()) {
            return getRoot().outE(DELETED_VERTEX_EDGE).next().inV().next();
        } else {
            return null;
        }
    }

    @Override
    public <T extends PersistentObject> T getEntity(Object id) {
        try {
            Long tmpId;
            if (id instanceof Long) {
                tmpId = (Long)id;
            } else if (id instanceof String) {
                tmpId = Long.valueOf((String)id);
            } else {
                throw new IllegalStateException(String.format("Sqlg only supports Long ids!, %s", id.toString()));
            }

            GraphTraversal<Vertex, Vertex> traversal = this.V(tmpId);
            if (!traversal.hasNext()) {
                throw new RuntimeException(String.format("No vertex found for id %d", new Object[]{tmpId}));
            }
            return instantiateClassifier(traversal.next());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends PersistentObject> T getEntity(Vertex vertex) {
        return instantiateClassifier(vertex);
    }

    private <T> T instantiateClassifier(Vertex v) {
        try {
            // TODO reimplement schemaHelper
            String className = v.value("className");
            Class<?> c = Class.forName(className);
            return (T) c.getConstructor(Vertex.class).newInstance(v);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PersistentObject getFromUniqueIndex(String label, String indexKey, Object indexValue) {
        Iterator<Vertex> iterator = this.V().has(T.label, shortenClassName(label)).has(indexKey, indexValue);
        if (iterator.hasNext()) {
            return instantiateClassifier(iterator.next());
        } else {
            return null;
        }
    }

    @Override
    public List<PersistentObject> getFromIndex(String label, String indexKey, Object indexValue) {
        final Iterator<Vertex> iterator = this.V().has(T.label, shortenClassName(label)).has(indexKey, indexValue);
        Iterable<Vertex> iterable = () -> iterator;
        Stream<PersistentObject> targetStream = StreamSupport.stream(iterable.spliterator(), false)
                .map(v -> UMLG.get().getEntity(v));
        List<PersistentObject> result = targetStream.collect(Collectors.toList());
        return result;
    }

    /* Generic for all graphs end */

    @Override
    public String executeQueryToJson(UmlgQueryEnum umlgQueryEnum, Object contextId, String query) {
        try {
            switch (umlgQueryEnum) {
                case OCL:
                    try {
                        Class<?> umlgOclExecutor = Class.forName("org.umlg.ocl.UmlgOclExecutor");
                        Method method = umlgOclExecutor.getMethod("executeOclQueryAsJson", Object.class, String.class);
                        String result = (String) method.invoke(null, contextId, query);
                        return result;
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("UmlgOclExecutor is not on the class path.");
                    } catch (Exception e) {
                        if (e instanceof RuntimeException) {
                            throw (RuntimeException) e;
                        } else if (e instanceof InvocationTargetException) {
                            Throwable target = ((InvocationTargetException) e).getTargetException();
                            if (target instanceof RuntimeException) {
                                throw (RuntimeException) target;
                            } else {
                                throw new RuntimeException(target);
                            }
                        } else {
                            throw new RuntimeException(e);
                        }
                    }
                case GROOVY:
                    String result;
                    if (contextId != null) {
                        result = GroovyExecutor.INSTANCE.executeGroovyAsString(contextId, query);
                    } else {
                        result = GroovyExecutor.INSTANCE.executeGroovyAsString(null, query);
                    }
                    return result;
                case NATIVE:
//                    StopWatch stopWatch = new StopWatch();
//                    stopWatch.start();
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.sqlG.query(query));
//                    stopWatch.stop();
//                    sb.append("Time to execute query = ");
//                    sb.append(stopWatch.toString());
                    return sb.toString();
                default:
                    throw new RuntimeException("Unknown query enum");
            }
        } finally {
            this.rollback();
        }
    }

    @Override
    public <T> T executeQuery(UmlgQueryEnum umlgQueryEnum, Object contextId, String query) {
        switch (umlgQueryEnum) {
            case OCL:
                try {
                    Class<?> umlgOclExecutor = Class.forName("org.umlg.ocl.UmlgOclExecutor");
                    Method method = umlgOclExecutor.getMethod("executeOclQuery", Object.class, String.class);
                    Object result = method.invoke(null, contextId, query);
                    return (T) result;
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("UmlgOclExecutor is not on the class path.");
                } catch (Exception e) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else if (e instanceof InvocationTargetException) {
                        Throwable target = ((InvocationTargetException) e).getTargetException();
                        if (target instanceof RuntimeException) {
                            throw (RuntimeException) target;
                        } else {
                            throw new RuntimeException(target);
                        }
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            case GROOVY:
                Object result;
                if (contextId != null) {
                    result = GroovyExecutor.INSTANCE.executeGroovy(contextId, query);
                } else {
                    result = GroovyExecutor.INSTANCE.executeGroovy(null, query);
                }
                return (T) result;
            case NATIVE:
                return (T) "//TODO";
            default:
                throw new RuntimeException("Unknown query enum");
        }
    }

    @Override
    public void drop() {
        UmlgGraphManager.INSTANCE.deleteGraph();
    }

    @Override
    public void clear() {
        this.sqlG.getSchemaManager().clear();
    }

    @Override
    public Vertex getRoot() {
        if (this.rootVertex == null) {
            this.rootVertex = this.V(this.sqlG.getSqlDialect().getSequenceStart()).next();
        }
        return this.rootVertex;
    }

    @Override
    public UmlgApplicationNode getUmlgApplicationNode() {
        try {
            if (this.umlgApplicationNodeClass == null) {
                this.umlgApplicationNodeClass = (Class<UmlgApplicationNode>) Thread.currentThread().getContextClassLoader().loadClass(UmlgProperties.INSTANCE.getModelJavaName());
            }
            return (UmlgApplicationNode) this.umlgApplicationNodeClass.getField("INSTANCE").get(null);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Edge> getEdgesBetween(Vertex v1, Vertex v2, String... labels) {

        Set<Edge> result = new HashSet<>();
        GraphTraversal<Vertex, Edge> edges = v1.bothE(labels);

        if (!v1.equals(v2)) {

            edges.forEachRemaining(
                    edge -> {
                        if (edge.inV().next().equals(v2) || edge.outV().next().equals(v2)) {
                            result.add(edge);
                        }
                    }
            );

        } else {

            edges.forEachRemaining(
                    edge -> {
                        if (edge.inV().next().equals(v2) && edge.outV().next().equals(v2)) {
                            result.add(edge);
                        }
                    }
            );

        }
        return result;

    }

    @Override
    public long countVertices() {
        return this.sqlG.countVertices() - 1;
    }

    @Override
    public long countEdges() {
        return this.sqlG.countEdges();
    }

    @Override
    public boolean hasEdgeBeenDeleted(Edge edge) {
        return false;
    }

    @Override
    public boolean isTransactionActive() {
        return tx().isOpen();
    }

    @Override
    public void afterThreadContext() {
//        if (TransactionThreadEntityVar.get()!=null && TransactionThreadEntityVar.get().size()>0) {
//            throw new RuntimeException("wtf");
//        }
//        if (TransactionThreadMetaNodeVar.get()!=null && TransactionThreadMetaNodeVar.get().size()>0) {
//            throw new RuntimeException("wtf");
//        }
        TransactionThreadEntityVar.remove();
        TransactionThreadMetaNodeVar.remove();
        TransactionThreadNotificationVar.remove();
        UmlgAssociationClassManager.remove();
        UMLG.remove();
    }

    @Override
    public Graph getUnderlyingGraph() {
        return this.sqlG;
    }

    @Override
    public Vertex addVertex(Object... keyValues) {
        return this.sqlG.addVertex(keyValues);
    }

    @Override
    public GraphTraversal<Vertex, Vertex> V(final Object... vertexIds) {
        return this.sqlG.V(validateIds(vertexIds));
    }

    @Override
    public GraphTraversal<Edge, Edge> E(final Object... edgeIds) {
        return this.sqlG.E(validateIds(edgeIds));
    }

    private Long[] validateIds(final Object... ids) {
        List<Long> longIds = new ArrayList<>();
        for (Object id : ids) {
           longIds.add(Long.valueOf(id.toString()));
        }
        return longIds.toArray(new Long[]{});
    }

    @Override
    public GraphComputer compute(final Class... graphComputerClass) {
        return this.sqlG.compute(graphComputerClass);
    }

    @Override
    public Transaction tx() {
        return this.sqlG.tx();
    }

    @Override
    public Variables variables() {
        return this.sqlG.variables();
    }

    @Override
    public void close() throws Exception {
        this.sqlG.close();
    }

    @Override
    public Features features() {
        return this.sqlG.features();
    }
}
