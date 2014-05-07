package org.umlg.javageneration.visitor.clazz;

import org.eclipse.uml2.uml.AssociationClass;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.umlg.framework.ModelLoader;
import org.umlg.framework.VisitSubclasses;
import org.umlg.framework.Visitor;
import org.umlg.generation.Workspace;
import org.umlg.java.metamodel.*;
import org.umlg.java.metamodel.annotation.OJAnnotatedClass;
import org.umlg.java.metamodel.annotation.OJAnnotatedOperation;
import org.umlg.java.metamodel.generated.OJVisibilityKindGEN;
import org.umlg.javageneration.util.Namer;
import org.umlg.javageneration.util.UmlgGenerationUtil;
import org.umlg.javageneration.util.UmlgClassOperations;

import java.util.Set;

/**
 * Date: 2012/12/25
 * Time: 2:47 PM
 */
public class MetaClassBuilder extends ClassBuilder implements Visitor<Class> {

    public MetaClassBuilder(Workspace workspace, String sourceDir) {
        super(workspace, sourceDir);
    }

    @Override
    @VisitSubclasses({Class.class, AssociationClass.class})
    public void visitBefore(Class clazz) {
        OJAnnotatedClass metaClass = new OJAnnotatedClass(UmlgClassOperations.getMetaClassName(clazz));
        OJPackage ojPackage = new OJPackage(Namer.name(clazz.getNearestPackage()) + ".meta");
        metaClass.setMyPackage(ojPackage);
        metaClass.setVisibility(UmlgClassOperations.getVisibility(clazz.getVisibility()));

        if (ModelLoader.INSTANCE.isUmlGLibIncluded()) {
            metaClass.setSuperclass(UmlgGenerationUtil.BASE_CLASS_UMLG);
            addDefaultConstructor(metaClass, clazz);
            addContructorWithVertex(metaClass, clazz);
            //Ensure the meta class instance does not also try to create a edge to a meta class as it is also a normal entity
            addEmptyAddEdgeToMetaNode(metaClass);
            addAddToThreadEntityVar(metaClass);
        } else {
            metaClass.setSuperclass(UmlgGenerationUtil.BASE_META_NODE);
            addDefaultConstructorStandAlone(metaClass, clazz);
            addConstructorWithVertexStandAlone(metaClass, clazz);
        }
        addToSource(metaClass);
        addGetEdgeToRootLabel(metaClass, clazz);
        addImplementsTumlMetaNode(metaClass);
        OJAnnotatedClass annotatedClass = findOJClass(clazz);
        addMetaClassGetterToRoot(clazz, metaClass);
        addDefaultCreate(metaClass);
        if (!clazz.isAbstract()) {
            addAndImplementUmlgLibNodeOnOriginalClass(annotatedClass, clazz, metaClass.getPathName());
            addGetAllInstances(clazz, metaClass);
            addGetAllInstancesWithFilter(clazz, metaClass);
        } else {
            addGetAllInstancesForAbstractClass(clazz, metaClass);
            addGetAllInstancesWithFilterForAbstractClass(clazz, metaClass);
        }
    }

    private void addAddToThreadEntityVar(OJAnnotatedClass metaClass) {
        OJAnnotatedOperation addToThreadMetaEntityVar = new OJAnnotatedOperation("addToThreadEntityVar");
        UmlgGenerationUtil.addOverrideAnnotation(addToThreadMetaEntityVar);
        addToThreadMetaEntityVar.getBody().addToStatements(UmlgGenerationUtil.transactionThreadMetaNodeVar.getLast() + ".setNewEntity(this)");
        metaClass.addToOperations(addToThreadMetaEntityVar);

    }

    private void addDefaultCreate(OJAnnotatedClass metaClass) {
        OJAnnotatedOperation defaultCreate = new OJAnnotatedOperation("defaultCreate");
        UmlgGenerationUtil.addOverrideAnnotation(defaultCreate);
        defaultCreate.getBody().addToStatements("getUid()");
        metaClass.addToOperations(defaultCreate);
    }

    private void addConstructorWithVertexStandAlone(OJAnnotatedClass metaClass, Class clazz) {
        OJConstructor constructor = new OJConstructor();
        constructor.addParam("vertex", UmlgGenerationUtil.vertexPathName);
        constructor.getBody().addToStatements("this.vertex= vertex");
        metaClass.addToConstructors(constructor);
    }

    private void addDefaultConstructorStandAlone(OJAnnotatedClass metaClass, Class clazz) {
        metaClass.getDefaultConstructor().getBody().addToStatements("this.vertex = " + UmlgGenerationUtil.UMLGAccess + ".addVertex(this.getClass().getName())");
        metaClass.getDefaultConstructor().getBody().addToStatements("this.vertex.setProperty(\"className\", getClass().getName())");
        metaClass.getDefaultConstructor().getBody().addToStatements("defaultCreate()");
        metaClass.getDefaultConstructor().getBody().addToStatements(UmlgGenerationUtil.UMLGAccess + ".addEdge(null, " + UmlgGenerationUtil.UMLGAccess + ".getRoot(), this.vertex, getEdgeToRootLabel())");
    }

    private void addEmptyAddEdgeToMetaNode(OJAnnotatedClass metaClass) {
        OJAnnotatedOperation addEdgeToMetaNode = new OJAnnotatedOperation("addEdgeToMetaNode");
        UmlgGenerationUtil.addOverrideAnnotation(addEdgeToMetaNode);
        metaClass.addToImports(UmlgGenerationUtil.UMLGPathName);
        metaClass.addToOperations(addEdgeToMetaNode);
    }

    private void addGetAllInstances(Class clazz, OJAnnotatedClass metaClass) {
        OJAnnotatedOperation allInstances = new OJAnnotatedOperation("getAllInstances");
        UmlgGenerationUtil.addOverrideAnnotation(allInstances);
        OJPathName classPathName = UmlgClassOperations.getPathName(clazz);
        allInstances.setReturnType(UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(classPathName));

        OJField resultField = new OJField("result", UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(classPathName));
        resultField.setInitExp("new " + UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(classPathName).getLast() + "()");
        allInstances.getBody().addToLocals(resultField);
        OJField iter = new OJField("iter", new OJPathName("java.lang.Iterable").addToGenerics(UmlgGenerationUtil.edgePathName));
        iter.setInitExp("this.vertex.getEdges(Direction.OUT, " + UmlgGenerationUtil.UMLG_NODE.getLast() + ".ALLINSTANCES_EDGE_LABEL)");
        allInstances.getBody().addToLocals(iter);

        OJForStatement forIter = new OJForStatement("edge", UmlgGenerationUtil.edgePathName, "iter");
        forIter.getBody().addToStatements("result.add(" + UmlgGenerationUtil.UMLGAccess + ".<" + classPathName.getLast() + ">instantiateClassifier(edge.getVertex(Direction.IN).getId()))");
        allInstances.getBody().addToStatements(forIter);
        allInstances.getBody().addToStatements("return result");

        metaClass.addToImports(UmlgGenerationUtil.UMLG_NODE);
        metaClass.addToOperations(allInstances);
    }

    private void addGetAllInstancesWithFilter(Class clazz, OJAnnotatedClass metaClass) {
        OJAnnotatedOperation allInstances = new OJAnnotatedOperation("getAllInstances");
        allInstances.addToParameters(new OJParameter("filter", UmlgGenerationUtil.Filter));

        UmlgGenerationUtil.addOverrideAnnotation(allInstances);
        OJPathName classPathName = UmlgClassOperations.getPathName(clazz);
        allInstances.setReturnType(UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(classPathName));

        OJField resultField = new OJField("result", UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(classPathName));
        resultField.setInitExp("new " + UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(classPathName).getLast() + "()");
        allInstances.getBody().addToLocals(resultField);
        OJField iter = new OJField("iter", new OJPathName("java.lang.Iterable").addToGenerics(UmlgGenerationUtil.edgePathName));
        iter.setInitExp("this.vertex.getEdges(Direction.OUT, " + UmlgGenerationUtil.UMLG_NODE.getLast() + ".ALLINSTANCES_EDGE_LABEL)");
        allInstances.getBody().addToLocals(iter);

        OJForStatement forIter = new OJForStatement("edge", UmlgGenerationUtil.edgePathName, "iter");
        forIter.getBody().addToStatements(classPathName.getLast() + " instance = " + UmlgGenerationUtil.UMLGAccess + ".instantiateClassifier(edge.getVertex(Direction.IN).getId())");
        OJIfStatement ifFilter = new OJIfStatement("filter.filter(instance)");
        ifFilter.addToThenPart("result.add(instance)");
        forIter.getBody().addToStatements(ifFilter);

        allInstances.getBody().addToStatements(forIter);
        allInstances.getBody().addToStatements("return result");

        metaClass.addToImports(UmlgGenerationUtil.UMLG_NODE);
        metaClass.addToOperations(allInstances);
    }

    private void addGetAllInstancesForAbstractClass(Class clazz, OJAnnotatedClass metaClass) {
        OJAnnotatedOperation allInstances = new OJAnnotatedOperation("getAllInstances");
        UmlgGenerationUtil.addOverrideAnnotation(allInstances);
        OJPathName classPathName = UmlgClassOperations.getPathName(clazz);
        allInstances.setReturnType(UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(classPathName));

        OJField resultField = new OJField("result", UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(classPathName));
        resultField.setInitExp("new " + UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(classPathName).getLast() + "()");
        allInstances.getBody().addToLocals(resultField);

        Set<Classifier> specializations =  UmlgClassOperations.getSpecializations(clazz);
        for (Classifier specialization : specializations) {
            allInstances.getBody().addToStatements("result.addAll(" + UmlgClassOperations.getMetaClassName(specialization) + ".getInstance().getAllInstances())");
            metaClass.addToImports(UmlgClassOperations.getMetaClassPathName(specialization));
        }
        allInstances.getBody().addToStatements("return result");

        metaClass.addToImports(UmlgGenerationUtil.UMLG_NODE);
        metaClass.addToOperations(allInstances);
    }

    private void addGetAllInstancesWithFilterForAbstractClass(Class clazz, OJAnnotatedClass metaClass) {
        OJAnnotatedOperation allInstances = new OJAnnotatedOperation("getAllInstances");
        allInstances.addToParameters(new OJParameter("filter", UmlgGenerationUtil.Filter));

        UmlgGenerationUtil.addOverrideAnnotation(allInstances);
        OJPathName classPathName = UmlgClassOperations.getPathName(clazz);
        allInstances.setReturnType(UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(classPathName));

        OJField resultField = new OJField("result", UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(classPathName));
        resultField.setInitExp("new " + UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(classPathName).getLast() + "()");
        allInstances.getBody().addToLocals(resultField);

        Set<Classifier> specializations =  UmlgClassOperations.getSpecializations(clazz);
        for (Classifier specialization : specializations) {
            allInstances.getBody().addToStatements("result.addAll(" + UmlgClassOperations.getMetaClassName(specialization) + ".getInstance().getAllInstances())");
            metaClass.addToImports(UmlgClassOperations.getMetaClassPathName(specialization));
        }
        allInstances.getBody().addToStatements("return result");

        metaClass.addToImports(UmlgGenerationUtil.UMLG_NODE);
        metaClass.addToOperations(allInstances);
    }

    private void addGetEdgeToRootLabel(OJAnnotatedClass metaClass, Class clazz) {
        OJAnnotatedOperation getEdgeToRootLabel = new OJAnnotatedOperation("getEdgeToRootLabel", new OJPathName("String"));
        getEdgeToRootLabel.getBody().addToStatements("return " + UmlgGenerationUtil.UmlgLabelConverterFactoryPathName.getLast() + ".getUmlgLabelConverter().convert(\"" + UmlgGenerationUtil.getEdgeToRootLabelStrategyMeta(clazz) + "\")");
        metaClass.addToImports(UmlgGenerationUtil.UmlgLabelConverterFactoryPathName);
        metaClass.addToOperations(getEdgeToRootLabel);
    }

    private void addMetaClassGetterToRoot(Class clazz, OJAnnotatedClass metaClass) {

        OJAnnotatedOperation INSTANCE = new OJAnnotatedOperation("getInstance");
        INSTANCE.setStatic(true);
        INSTANCE.setReturnType(metaClass.getPathName());
        metaClass.addToOperations(INSTANCE);
        OJField result = new OJField("result", metaClass.getPathName());
        INSTANCE.getBody().addToLocals(result);

        INSTANCE.getBody().addToStatements("Iterator<Edge> iter = " + UmlgGenerationUtil.UMLGAccess + ".getRoot().getEdges(Direction.OUT, " + UmlgGenerationUtil.UmlgLabelConverterFactoryPathName.getLast() + ".getUmlgLabelConverter().convert(\"" + UmlgGenerationUtil.getEdgeToRootLabelStrategyMeta(clazz) + "\")).iterator()");
        OJIfStatement ifHasNext = new OJIfStatement("iter.hasNext()");
        ifHasNext.addToThenPart("result =  new " + UmlgClassOperations.getMetaClassName(clazz) + "(iter.next().getVertex(Direction.IN))");
        INSTANCE.getBody().addToStatements(ifHasNext);

        ifHasNext.addToElsePart("iter = " + UmlgGenerationUtil.UMLGAccess + ".getRoot().getEdges(Direction.OUT, " + UmlgGenerationUtil.UmlgLabelConverterFactoryPathName.getLast() + ".getUmlgLabelConverter().convert(\"" + UmlgGenerationUtil.getEdgeToRootLabelStrategyMeta(clazz) + "\")).iterator()");

        OJIfStatement ifIter2 = new OJIfStatement("!iter.hasNext()");
        ifIter2.addToThenPart("result = new " + metaClass.getName() + "()");

        ifIter2.addToElsePart("result = new " + metaClass.getName() + "(iter.next().getVertex(Direction.IN))");
        ifHasNext.addToElsePart(ifIter2);

        INSTANCE.getBody().addToStatements("return result");
        metaClass.addToImports("java.util.Iterator");

        metaClass.addToImports("com.tinkerpop.blueprints.Direction");
        metaClass.addToImports("com.tinkerpop.blueprints.Direction");
        metaClass.addToImports("com.tinkerpop.blueprints.Edge");
        metaClass.addToImports(UmlgGenerationUtil.UMLGPathName);

    }

    @Override
    protected void addContructorWithVertex(OJAnnotatedClass ojClass, Classifier classifier) {
        OJConstructor constructor = new OJConstructor();
        constructor.setVisibility(OJVisibilityKindGEN.PUBLIC);
        constructor.addParam("vertex", UmlgGenerationUtil.vertexPathName);
        constructor.getBody().addToStatements("super(vertex)");
        constructor.getBody().addToStatements(UmlgGenerationUtil.transactionThreadMetaNodeVar.getLast() + ".setNewEntity(this)");
        constructor.getBody().addToStatements(UmlgGenerationUtil.transactionThreadEntityVar.getLast() + ".remove(this)");
        ojClass.addToImports(UmlgGenerationUtil.transactionThreadMetaNodeVar);
        ojClass.addToConstructors(constructor);
    }

    private void addAndImplementUmlgLibNodeOnOriginalClass(OJAnnotatedClass annotatedClass, Class clazz, OJPathName metaClassPathName) {
        OJAnnotatedOperation getMetaNode = new OJAnnotatedOperation("getMetaNode");
        getMetaNode.setReturnType(UmlgGenerationUtil.UmlgMetaNode);
        annotatedClass.addToOperations(getMetaNode);
        getMetaNode.setAbstract(clazz.isAbstract());
        if (!clazz.isAbstract()) {
            annotatedClass.addToImports(metaClassPathName);
            getMetaNode.getBody().addToStatements("return " + UmlgClassOperations.getMetaClassName(clazz) + ".getInstance()");
        }
    }

    @Override
    public void visitAfter(Class element) {
    }

    private void addImplementsTumlMetaNode(OJAnnotatedClass annotatedClass) {
        annotatedClass.addToImplementedInterfaces(UmlgGenerationUtil.UmlgMetaNode);
    }

    private void addDefaultConstructor(OJAnnotatedClass annotatedClass, Class clazz) {
        annotatedClass.getDefaultConstructor().setVisibility(OJVisibilityKind.PRIVATE);
        annotatedClass.getDefaultConstructor().getBody().addToStatements("super(true)");
        annotatedClass.getDefaultConstructor().setVisibility(OJVisibilityKind.PRIVATE);
        annotatedClass.getDefaultConstructor().getBody().addToStatements(UmlgGenerationUtil.transactionThreadEntityVar.getLast() + ".remove(this)");
        annotatedClass.addToImports(UmlgGenerationUtil.transactionThreadEntityVar);
    }

}
