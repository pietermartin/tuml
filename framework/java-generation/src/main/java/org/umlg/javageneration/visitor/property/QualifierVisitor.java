package org.umlg.javageneration.visitor.property;

import java.util.Iterator;
import java.util.List;

import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.umlg.java.metamodel.OJBlock;
import org.umlg.java.metamodel.OJField;
import org.umlg.java.metamodel.OJIfStatement;
import org.umlg.java.metamodel.OJPathName;
import org.umlg.java.metamodel.OJSimpleStatement;
import org.umlg.java.metamodel.annotation.OJAnnotatedClass;
import org.umlg.java.metamodel.annotation.OJAnnotatedOperation;
import org.umlg.framework.Visitor;
import org.umlg.generation.Workspace;
import org.umlg.javageneration.util.PropertyWrapper;
import org.umlg.javageneration.util.TinkerGenerationUtil;
import org.umlg.javageneration.visitor.BaseVisitor;

public class QualifierVisitor extends BaseVisitor implements Visitor<Property> {

    public QualifierVisitor(Workspace workspace) {
        super(workspace);
    }

    @Override
    public void visitBefore(Property p) {
        PropertyWrapper pWrap = new PropertyWrapper(p);
        if (pWrap.hasQualifiers()) {
            //This generates the method that returns the umlg qualifier, i.e. org.umlg.runtime.collection.Qualifier
            generateQualifierGetter(findOJClass(pWrap), pWrap);
            //This generates the getter that takes qualifier value as input
            generateQualifiedGetter(pWrap);
        }
    }

    @Override
    public void visitAfter(Property p) {
    }

    private void validateHasCorrespondingDerivedProperty(PropertyWrapper qualifier) {
        if (!qualifier.haveQualifierCorrespondingDerivedProperty()) {
            throw new IllegalStateException(String.format("Qualifier %s on %s does not have a corresponding derived property on %s",
                    new Object[]{qualifier.getName(), qualifier.getOwner(), qualifier.getQualifierContext().getName()}));
        }
    }

    private void generateQualifierGetter(OJAnnotatedClass ojClass, PropertyWrapper qualified) {
        OJAnnotatedOperation qualifierGetter = new OJAnnotatedOperation(qualified.getQualifiedGetterName());
        qualifierGetter.addParam("context", qualified.getQualifierContextPathName());
        ojClass.addToOperations(qualifierGetter);
        OJField result = new OJField();
        result.setName("result");
        result.setType(new OJPathName("java.util.List"));
        result.getType().addToElementTypes(TinkerGenerationUtil.TINKER_QUALIFIER_PATHNAME);
        result.setInitExp("new ArrayList<" + TinkerGenerationUtil.TINKER_QUALIFIER_PATHNAME.getLast() + ">()");
        ojClass.addToImports("java.util.ArrayList");
        qualifierGetter.setReturnType(result.getType());
        qualifierGetter.getBody().addToLocals(result);

        StringBuilder sb = new StringBuilder();
        sb.append("result.add(");
        sb.append("new ");
        sb.append(TinkerGenerationUtil.TINKER_QUALIFIER_PATHNAME.getLast());
        sb.append("(new String[]{");

        for (Iterator<Property> iterator = qualified.getQualifiers().iterator(); iterator.hasNext(); ) {
            PropertyWrapper qWrap = new PropertyWrapper(iterator.next());
            sb.append("\"");
            sb.append(qWrap.getQualifiedName());
            sb.append("\"");
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}, new String[]{");
        for (Iterator<Property> iterator = qualified.getQualifiers().iterator(); iterator.hasNext(); ) {
            PropertyWrapper qWrap = new PropertyWrapper(iterator.next());
            sb.append("context.");
            sb.append(qWrap.getter());
            sb.append("() == null ? ");
            sb.append("getId() + \"___NULL___\" : getId() + ");
            sb.append("context.");
            sb.append(qWrap.getter());
            sb.append("().toString() ");
            if (iterator.hasNext()) {
                sb.append(", ");
            }
        }

        sb.append("}, ");
        sb.append(TinkerGenerationUtil.calculateMultiplcity(qualified));
        sb.append("))");
        qualifierGetter.getBody().addToStatements(sb.toString());

        qualifierGetter.getBody().addToStatements("return result");
        ojClass.addToImports(TinkerGenerationUtil.TINKER_QUALIFIER_PATHNAME);
        ojClass.addToImports(TinkerGenerationUtil.tinkerMultiplicityPathName);
    }

    private void generateQualifiedGetter(PropertyWrapper qualified) {
        List<PropertyWrapper> qualifiers = qualified.getQualifiersAsPropertyWrappers();
        for (PropertyWrapper qualifier : qualifiers) {
            validateHasCorrespondingDerivedProperty(qualifier);
        }

        //TODO might be a intermittent bug in getting owning type logic for many to manies
        Type qualifiedClassifier = qualified.getOwningType();
        OJAnnotatedClass ojClass = findOJClass(qualifiedClassifier);

        OJAnnotatedOperation qualifierValue = new OJAnnotatedOperation(qualified.getQualifiedNameFor(qualifiers));
        if (qualified.isUnqualifiedOne()) {
            qualifierValue.setReturnType(qualified.javaBaseTypePath());
        } else {
            // This needs to only return a Set or Bag for now, not sorting the
            // result
            // by index as yet
            qualifierValue.setReturnType(qualified.javaTypePath());
        }
        for (PropertyWrapper qualifier : qualifiers) {
            qualifierValue.addParam(qualifier.fieldname(), qualifier.javaBaseTypePath());
        }
        ojClass.addToImports(TinkerGenerationUtil.tinkerCloseableIterablePathName);
        ojClass.addToImports(TinkerGenerationUtil.tinkerDirection);
        ojClass.addToImports(TinkerGenerationUtil.edgePathName);
//        qualifierValue.getBody().addToStatements(
//                TinkerGenerationUtil.tinkerIndexPathName.getLast() + "<" + TinkerGenerationUtil.edgePathName.getLast() + "> index = " + TinkerGenerationUtil.graphDbAccess + ".getIndex(" + qualified.getTumlRuntimePropertyEnum() + ".getQualifiedName(), Edge.class)");
//        OJIfStatement ifIndexNull = new OJIfStatement("index==null", "return null");

        OJBlock elseBlock = new OJBlock();
        OJField indexKey = new OJField(elseBlock, "indexKey", new OJPathName("String"));
        StringBuilder init = new StringBuilder();
        int count = 0;

        for (PropertyWrapper qualifier : qualifiers) {
            count++;
            init.append("\"" + qualifier.getQualifiedName() + "\"");
            if (count != qualifiers.size()) {
                init.append(" + ");
            }
        }
        indexKey.setInitExp(init.toString());
        elseBlock.addToLocals(indexKey);

        OJField indexValue = new OJField(elseBlock, "indexValue", new OJPathName("String"));
        boolean first = true;
        for (PropertyWrapper qualifier : qualifiers) {
            if (first) {
                first = false;
                indexValue.setInitExp(qualifier.fieldname() + " == null ? getId() + \"___NULL___\" : getId() + " + qualifier.fieldname());
            } else {
                elseBlock.addToStatements("indexValue += " + qualifier.fieldname() + " == null ? getId() + \"___NULL___\" : getId() + " + qualifier.fieldname());
            }
        }

//        Iterator<Edge> iterator = GraphDb.getDb().query().has(indexKey, indexValue).edges().iterator();
        elseBlock.addToStatements("Iterator<Edge> iterator = " + TinkerGenerationUtil.graphDbAccess + ".query().has(indexKey, indexValue).edges().iterator()");

//        elseBlock.addToStatements(TinkerGenerationUtil.tinkerCloseableIterablePathName.getCopy().addToGenerics(TinkerGenerationUtil.edgePathName).getLast()
//                + " closeableIterable = index.get(" + "indexKey" + ", indexValue)");
        qualifierValue.getBody().addToStatements(elseBlock);
//        qualifierValue.getBody().addToStatements("Iterator<Edge> iterator = closeableIterable.iterator()");
        ojClass.addToImports("java.util.Iterator");
        OJIfStatement ifHasNext = new OJIfStatement("iterator.hasNext()");
        if (qualified.isUnqualifiedOne()) {
            ifHasNext.addToThenPart("return new " + qualified.javaBaseTypePath().getLast() + "(iterator.next().getVertex("
                    + TinkerGenerationUtil.tinkerDirection.getLast() + ".IN))");
            ifHasNext.addToElsePart("return null");
        } else {
            OJSimpleStatement ojSimpleStatement;
            ojSimpleStatement = new OJSimpleStatement("return new "
                    + qualified.javaClosableIteratorTypePath().getCopy().getLast());
            ojSimpleStatement.setExpression(ojSimpleStatement.getExpression() + "(iterator, " + qualified.getTumlRuntimePropertyEnum() + ")");
            ojClass.addToImports(qualified.javaClosableIteratorTypePath());
            ifHasNext.addToThenPart(ojSimpleStatement);
            ifHasNext.addToElsePart("return " + qualified.emptyCollection());
            ojClass.addToImports(TinkerGenerationUtil.tumlTumlCollections);
        }

        qualifierValue.getBody().addToStatements(ifHasNext);
        ojClass.addToOperations(qualifierValue);
    }
}
