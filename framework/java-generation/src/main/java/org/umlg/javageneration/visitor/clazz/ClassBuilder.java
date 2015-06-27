package org.umlg.javageneration.visitor.clazz;

import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.uml2.uml.*;
import org.eclipse.uml2.uml.Class;
import org.umlg.framework.ModelLoader;
import org.umlg.framework.VisitSubclasses;
import org.umlg.framework.Visitor;
import org.umlg.generation.Workspace;
import org.umlg.java.metamodel.*;
import org.umlg.java.metamodel.annotation.OJAnnotatedClass;
import org.umlg.java.metamodel.annotation.OJAnnotatedOperation;
import org.umlg.java.metamodel.annotation.OJAnnotationValue;
import org.umlg.javageneration.ocl.UmlgOcl2Java;
import org.umlg.javageneration.util.*;
import org.umlg.javageneration.visitor.BaseVisitor;
import org.umlg.ocl.UmlgOcl2Parser;

import java.util.List;
import java.util.Set;

public class ClassBuilder extends BaseVisitor implements Visitor<Class> {

    public static final String INIT_VARIABLES = "initVariables";
    public static final String INITIALISE_PROPERTIES = "initialiseProperties";

    public ClassBuilder(Workspace workspace) {
        super(workspace);
    }

    public ClassBuilder(Workspace workspace, String sourceDir) {
        super(workspace, sourceDir);
    }

    @Override
    @VisitSubclasses({Class.class, AssociationClass.class})
    public void visitBefore(Class clazz) {
        OJAnnotatedClass annotatedClass = findOJClass(clazz);
        setSuperClass(annotatedClass, clazz);
        implementCompositionNode(annotatedClass);
        addDefaultSerialization(annotatedClass);
        implementIsRoot(annotatedClass, UmlgClassOperations.getOtherEndToComposite(clazz).isEmpty());
        addPersistentConstructor(annotatedClass);
        callPersistentConstructorFromDefault(annotatedClass);
        addInitialiseProperties(annotatedClass, clazz);
        addContructorWithVertexAndConstructorWithId(annotatedClass, clazz);
        if (clazz.getGeneralizations().isEmpty()) {
            persistUid(annotatedClass);
        }
        addInitVariables(annotatedClass, clazz);
        addDelete(annotatedClass, clazz);
        addGetQualifiedName(annotatedClass, clazz);
//        if (!clazz.isAbstract()) {
//            addEdgeToMetaNode(annotatedClass, clazz);
//        }
        addAllInstances(annotatedClass, clazz);
        addAllInstancesWithFilter(annotatedClass, clazz);
        addConstraints(annotatedClass, clazz);
        if (UmlgClassOperations.isAssociationClass(clazz)) {
            annotatedClass.addToImplementedInterfaces(UmlgGenerationUtil.AssociationClassNode);
            createCopyOnePrimitivePropertiesToEdge(annotatedClass, clazz);
        }
    }


    @Override
    public void visitAfter(Class clazz) {
    }

    private void createCopyOnePrimitivePropertiesToEdge(OJAnnotatedClass annotatedClass, Class clazz) {
        OJAnnotatedOperation copyOnePrimitivePropertiesToEdge = new OJAnnotatedOperation("z_internalCopyOnePrimitivePropertiesToEdge");
        UmlgGenerationUtil.addOverrideAnnotation(copyOnePrimitivePropertiesToEdge);
        copyOnePrimitivePropertiesToEdge.addToParameters(new OJParameter("edge", UmlgGenerationUtil.edgePathName));
        annotatedClass.addToOperations(copyOnePrimitivePropertiesToEdge);
    }

    // TODO turn into proper value
    private void addDefaultSerialization(OJAnnotatedClass annotatedClass) {
        OJField defaultSerialization = new OJField(annotatedClass, "serialVersionUID", new OJPathName("long"));
        defaultSerialization.setFinal(true);
        defaultSerialization.setStatic(true);
        defaultSerialization.setInitExp("1L");
    }

    private void setSuperClass(OJAnnotatedClass annotatedClass, Class clazz) {
        List<Classifier> generals = clazz.getGenerals();
        if (generals.size() > 1) {
            throw new IllegalStateException(
                    String.format("Multiple inheritance is not supported! Class %s has more than on genereralization.", clazz.getName()));
        }
        if (!generals.isEmpty()) {
            Classifier superClassifier = generals.get(0);
            OJAnnotatedClass superClass = findOJClass(superClassifier);
            annotatedClass.setSuperclass(superClass.getPathName());
        }
    }

    protected void persistUid(OJAnnotatedClass ojClass) {
        OJAnnotatedOperation getUid = new OJAnnotatedOperation("getUid");
        getUid.setReturnType(new OJPathName("String"));
        getUid.addAnnotationIfNew(new OJAnnotationValue(new OJPathName("java.lang.Override")));
        getUid.getBody().removeAllFromStatements();

        new OJField(getUid.getBody(), "uid", "String");
        OJIfStatement ifUidNotPresent = new OJIfStatement("!this.vertex.property(\"uid\").isPresent()");
        ifUidNotPresent.addToThenPart("uid=UUID.randomUUID().toString()");
        ifUidNotPresent.addToThenPart("this.vertex.property(\"uid\", uid)");
        ifUidNotPresent.addToElsePart("uid=this.vertex.value(\"uid\")");
        getUid.getBody().addToStatements(ifUidNotPresent);
        getUid.getBody().addToStatements("return uid");

        ojClass.addToImports("java.util.UUID");
        ojClass.addToOperations(getUid);
    }

    protected void addPersistentConstructor(OJAnnotatedClass ojClass) {
        OJConstructor persistentConstructor = new OJConstructor();
        persistentConstructor.setName(UmlgGenerationUtil.PERSISTENT_CONSTRUCTOR_NAME);
        persistentConstructor.addParam(UmlgGenerationUtil.PERSISTENT_CONSTRUCTOR_PARAM_NAME, new OJPathName("java.lang.Boolean"));
        persistentConstructor.getBody().addToStatements("super(" + UmlgGenerationUtil.PERSISTENT_CONSTRUCTOR_PARAM_NAME + ")");
        ojClass.addToConstructors(persistentConstructor);
    }

    private void callPersistentConstructorFromDefault(OJAnnotatedClass annotatedClass) {
        annotatedClass.getDefaultConstructor().getBody().addToStatements("this(true)");
    }

    public static void addInitialiseProperties(OJAnnotatedClass annotatedClass, Classifier classifier) {
        OJAnnotatedOperation initialiseProperties = new OJAnnotatedOperation(INITIALISE_PROPERTIES);
        UmlgGenerationUtil.addOverrideAnnotation(initialiseProperties);
        if (!classifier.getGeneralizations().isEmpty()) {
            initialiseProperties.getBody().addToStatements("super." + INITIALISE_PROPERTIES + "()");
        }
        annotatedClass.addToOperations(initialiseProperties);
        for (Property p : UmlgClassOperations.getAllOwnedProperties(classifier)) {
            PropertyWrapper pWrap = new PropertyWrapper(p);
            if (!(pWrap.isDerived() || pWrap.isDerivedUnion()) && !(pWrap.isRefined()) &&
                    (!(classifier instanceof Enumeration && pWrap.getType() instanceof DataType))) {
                OJSimpleStatement statement = new OJSimpleStatement("this." + pWrap.fieldname() + " = " + pWrap.javaDefaultInitialisation(classifier));
                statement.setName(pWrap.fieldname());
                initialiseProperties.getBody().addToStatements(statement);
                if (pWrap.isOne() && pWrap.isBoolean()) {
                    OJIfStatement ifEmpty = new OJIfStatement("this." + pWrap.fieldname() + ".isEmpty()");
                    ifEmpty.setComment("Booleans are defaulted to false if the entity already exist then it will already have a value");
                    ifEmpty.addToThenPart("this." + pWrap.fieldname() + ".add(false)");
                    initialiseProperties.getBody().addToStatements(ifEmpty);
                }
                annotatedClass.addToImports(pWrap.javaImplTypePath());

                if (pWrap.isMemberOfAssociationClass()) {
                    //Initialize the collection to the association class
                    statement = new OJSimpleStatement("this." + pWrap.getAssociationClassFakePropertyName() + " = " + pWrap.javaDefaultInitialisationForAssociationClass(classifier));
                    statement.setName(pWrap.getAssociationClassFakePropertyName());
                    initialiseProperties.getBody().addToStatements(statement);
                    annotatedClass.addToImports(UmlgPropertyOperations.getDefaultTinkerCollectionForAssociationClass(pWrap.getProperty()));
                }
            }
        }

        if (classifier instanceof AssociationClass) {
            //Do the property for the association class
            AssociationClass associationClass = (AssociationClass) classifier;
            List<Property> memberEnds = associationClass.getMemberEnds();
            for (Property memberEnd : memberEnds) {
                PropertyWrapper pWrap = new PropertyWrapper(memberEnd);
                OJSimpleStatement statement = new OJSimpleStatement("this." + pWrap.fieldname() + " = " + pWrap.javaDefaultInitialisation(classifier, true));
                statement.setName(pWrap.fieldname());
                initialiseProperties.getBody().addToStatements(statement);
                annotatedClass.addToImports(UmlgPropertyOperations.getDefaultTinkerCollection(memberEnd, true));
            }
        }
    }

    protected void addContructorWithVertexAndConstructorWithId(OJAnnotatedClass ojClass, Classifier classifier) {
        OJConstructor constructor = new OJConstructor();
        constructor.addParam("id", "Object");
        constructor.getBody().addToStatements("super(id)");
        ojClass.addToConstructors(constructor);

        OJConstructor vertexConstructor = new OJConstructor();
        vertexConstructor.addParam("vertex", UmlgGenerationUtil.vertexPathName);
        vertexConstructor.getBody().addToStatements("super(vertex)");
        ojClass.addToConstructors(vertexConstructor);

    }

    protected void implementIsRoot(OJAnnotatedClass ojClass, boolean b) {
        OJAnnotatedOperation isRoot = new OJAnnotatedOperation("isTinkerRoot");
        isRoot.addAnnotationIfNew(new OJAnnotationValue(new OJPathName("java.lang.Override")));
        isRoot.setReturnType(new OJPathName("boolean"));
        isRoot.getBody().addToStatements("return " + b);
        ojClass.addToOperations(isRoot);
    }

    private void addInitVariables(OJAnnotatedClass annotatedClass, Class clazz) {
        OJOperation initVariables = new OJAnnotatedOperation(INIT_VARIABLES);
        if (UmlgClassOperations.hasSupertype(clazz)) {
            OJSimpleStatement simpleStatement = new OJSimpleStatement("super.initVariables()");
            if (initVariables.getBody().getStatements().isEmpty()) {
                initVariables.getBody().addToStatements(simpleStatement);
            } else {
                initVariables.getBody().getStatements().set(0, simpleStatement);
            }
        }
        annotatedClass.addToOperations(initVariables);
    }

    private void addDelete(OJAnnotatedClass annotatedClass, Class clazz) {
        OJAnnotatedOperation delete = new OJAnnotatedOperation("delete");
        UmlgGenerationUtil.addOverrideAnnotation(delete);
        annotatedClass.addToOperations(delete);
    }

    private void implementCompositionNode(OJAnnotatedClass annotatedClass) {
        annotatedClass.addToImplementedInterfaces(UmlgGenerationUtil.umlgCompositionNodePathName);
    }

    public static void addGetQualifiedName(OJAnnotatedClass annotatedClass, Classifier c) {
        OJAnnotatedOperation getQualifiedName = new OJAnnotatedOperation("getQualifiedName");
        UmlgGenerationUtil.addOverrideAnnotation(getQualifiedName);
        getQualifiedName.setReturnType("String");
        getQualifiedName.getBody().addToStatements("return \"" + c.getQualifiedName() + "\"");
        annotatedClass.addToOperations(getQualifiedName);
    }

    private void addEdgeToMetaNode(OJAnnotatedClass annotatedClass, Class clazz) {
        OJAnnotatedOperation addEdgeToMetaNode = new OJAnnotatedOperation("addEdgeToMetaNode");
        UmlgGenerationUtil.addOverrideAnnotation(addEdgeToMetaNode);
        addEdgeToMetaNode.getBody().addToStatements("getMetaNode().getVertex().addEdge(" + UmlgGenerationUtil.UMLG_NODE.getLast() + ".ALLINSTANCES_EDGE_LABEL, this.vertex)");
        annotatedClass.addToImports(UmlgGenerationUtil.UMLGPathName);
        annotatedClass.addToImports(UmlgGenerationUtil.UMLG_NODE);
        annotatedClass.addToOperations(addEdgeToMetaNode);
    }

    private void addAllInstances(OJAnnotatedClass annotatedClass, Class clazz) {
        OJAnnotatedOperation allInstances = new OJAnnotatedOperation("allInstances");
        allInstances.setStatic(true);
        if (UmlgClassOperations.getConcreteImplementations(clazz).isEmpty()) {
            allInstances.setReturnType(UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(UmlgClassOperations.getPathName(clazz)));
        } else {
            String pathName = "? extends " + UmlgClassOperations.getPathName(clazz).getLast();
            allInstances.setReturnType(UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(pathName));
        }

        OJField result = new OJField("result", UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(UmlgClassOperations.getPathName(clazz)));
        result.setInitExp("new " + UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(UmlgClassOperations.getPathName(clazz)).getLast() + "()");
        allInstances.getBody().addToLocals(result);
        Set<Classifier> specializations = UmlgClassOperations.getConcreteImplementations(clazz);
        if (!clazz.isAbstract()) {
            specializations.add(clazz);
        }
        for (Classifier c : specializations) {
            annotatedClass.addToImports(UmlgClassOperations.getPathName(c));
            allInstances.getBody().addToStatements("result.addAll(" + UmlgGenerationUtil.UMLGPathName.getLast() + ".get().allInstances(" + UmlgClassOperations.getPathName(c).getLast() + ".class.getName()))");
            annotatedClass.addToImports(UmlgClassOperations.getPathName(c));
        }
        annotatedClass.addToImports(UmlgGenerationUtil.UMLGPathName);

        allInstances.getBody().addToStatements("return result");
        annotatedClass.addToImports(UmlgGenerationUtil.umlgMemorySet);
        annotatedClass.addToOperations(allInstances);
    }

    private void addAllInstancesWithFilter(OJAnnotatedClass annotatedClass, Class clazz) {
        OJAnnotatedOperation allInstances = new OJAnnotatedOperation("allInstances");
        allInstances.addToParameters(new OJParameter("filter", UmlgGenerationUtil.Filter));
        allInstances.setStatic(true);
        if (UmlgClassOperations.getConcreteImplementations(clazz).isEmpty()) {
            allInstances.setReturnType(UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(UmlgClassOperations.getPathName(clazz)));
        } else {
            String pathName = "? extends " + UmlgClassOperations.getPathName(clazz).getLast();
            allInstances.setReturnType(UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(pathName));
        }

        OJField result = new OJField("result", UmlgGenerationUtil.umlgSet.getCopy().addToGenerics(UmlgClassOperations.getPathName(clazz)));
        result.setInitExp("new " + UmlgGenerationUtil.umlgMemorySet.getCopy().addToGenerics(UmlgClassOperations.getPathName(clazz)).getLast() + "()");
        allInstances.getBody().addToLocals(result);
        Set<Classifier> specializations = UmlgClassOperations.getConcreteImplementations(clazz);
        if (!clazz.isAbstract()) {
            specializations.add(clazz);
        }
        for (Classifier c : specializations) {
            annotatedClass.addToImports(UmlgClassOperations.getPathName(c));
            allInstances.getBody().addToStatements("result.addAll(UMLG.get().allInstances(" + UmlgClassOperations.getPathName(c).getLast() + ".class.getName(), filter))");
            annotatedClass.addToImports(UmlgClassOperations.getPathName(c));
        }

        allInstances.getBody().addToStatements("return result");
        annotatedClass.addToImports(UmlgGenerationUtil.umlgMemorySet);
//        annotatedClass.addToImports(UmlgGenerationUtil.Root);
        annotatedClass.addToOperations(allInstances);
    }

    private void addConstraints(OJAnnotatedClass annotatedClass, Class clazz) {
        List<Constraint> constraints = ModelLoader.INSTANCE.getConstraints(clazz);
        for (Constraint constraint : constraints) {
            ConstraintWrapper constraintWrapper = new ConstraintWrapper(constraint);
            OJAnnotatedOperation checkClassConstraint = new OJAnnotatedOperation(UmlgClassOperations.checkClassConstraintName(constraint));

            checkClassConstraint.setReturnType(new OJPathName("java.util.List").addToGenerics(UmlgGenerationUtil.UmlgConstraintViolation));
            OJField result = new OJField("result", new OJPathName("java.util.List").addToGenerics(UmlgGenerationUtil.UmlgConstraintViolation));
            result.setInitExp("new ArrayList<" + UmlgGenerationUtil.UmlgConstraintViolation.getLast() + ">()");

            OJIfStatement ifConstraintFails = new OJIfStatement();
            String ocl = constraintWrapper.getConstraintOclAsString();
            checkClassConstraint.setComment(String.format("Implements the ocl statement for constraint '%s'\n<pre>\n%s\n</pre>", constraintWrapper.getName(), ocl));
            OCLExpression<Classifier> oclExp = UmlgOcl2Parser.INSTANCE.parseOcl(ocl);

            ifConstraintFails.setCondition("(" + UmlgOcl2Java.oclToJava(clazz, annotatedClass, oclExp) + ") == false");
            ifConstraintFails.addToThenPart("result.add(new " + UmlgGenerationUtil.UmlgConstraintViolation.getLast() + "(\"" + constraintWrapper.getName() + "\", \""
                    + clazz.getQualifiedName() + "\", \"ocl\\n" + ocl.replace("\n", "\\n") + "\\nfails!\"))");

            checkClassConstraint.getBody().addToStatements(ifConstraintFails);
            checkClassConstraint.getBody().addToLocals(result);
            checkClassConstraint.getBody().addToStatements("return result");

            annotatedClass.addToOperations(checkClassConstraint);
        }
    }

}
