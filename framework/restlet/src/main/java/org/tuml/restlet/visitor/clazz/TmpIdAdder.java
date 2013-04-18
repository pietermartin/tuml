package org.tuml.restlet.visitor.clazz;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.tuml.framework.Visitor;
import org.tuml.generation.Workspace;
import org.tuml.java.metamodel.OJField;
import org.tuml.java.metamodel.OJIfStatement;
import org.tuml.java.metamodel.OJPathName;
import org.tuml.java.metamodel.OJSimpleStatement;
import org.tuml.java.metamodel.annotation.OJAnnotatedClass;
import org.tuml.java.metamodel.annotation.OJAnnotatedOperation;
import org.tuml.java.metamodel.generated.OJVisibilityKindGEN;
import org.tuml.javageneration.visitor.BaseVisitor;

import java.util.List;

/**
 * Date: 2013/04/14
 * Time: 5:11 PM
 */
public class TmpIdAdder extends BaseVisitor implements Visitor<Class> {

    public TmpIdAdder(Workspace workspace, String sourceDir) {
        super(workspace, sourceDir);
    }

    @Override
    public void visitBefore(Class clazz) {
        OJAnnotatedClass annotatedClass = findOJClass(clazz);
        List<Classifier> generals = clazz.getGenerals();
        if (generals.isEmpty()) {
            addTmpIdField(annotatedClass);
            addTmpIdToFromJson(annotatedClass);
            addTmpIdToToJson(annotatedClass);
        }

    }

    private void addTmpIdToToJson(OJAnnotatedClass annotatedClass) {
        OJAnnotatedOperation toJson = annotatedClass.findOperation("toJson", new OJPathName("Boolean"));
        //Insert the line at second line
        toJson.getBody().getStatements().add(1, new OJSimpleStatement("sb.append(\"\\\"tmpId\\\": \\\"\" + this.tmpId + \"\\\", \")"));

        OJAnnotatedOperation toJsonWithoutCompositeParent = annotatedClass.findOperation("toJsonWithoutCompositeParent", new OJPathName("Boolean"));
        //Insert the line at second line
        toJsonWithoutCompositeParent.getBody().getStatements().add(1, new OJSimpleStatement("sb.append(\"\\\"tmpId\\\": \\\"\" + this.tmpId + \"\\\", \")"));

    }

    private void addTmpIdToFromJson(OJAnnotatedClass annotatedClass) {
        OJAnnotatedOperation fromJson = annotatedClass.findOperation("fromJson", new OJPathName("java.util.Map"));
        OJIfStatement  ifStatement = new OJIfStatement("propertyMap.containsKey(\"tmpId\")");
        OJIfStatement ifStatement1 = new OJIfStatement("propertyMap.get(\"tmpId\") != null");
        ifStatement.addToThenPart(ifStatement1);
        ifStatement1.addToThenPart("this.tmpId = (String)propertyMap.get(\"tmpId\")");
        ifStatement1.addToElsePart("this.tmpId = null");
        fromJson.getBody().addToStatements(ifStatement);
    }

    //TODO make transient
    private void addTmpIdField(OJAnnotatedClass annotatedClass) {
        OJField tmpId = new OJField("tmpId", "String");
        tmpId.setComment("tmpId is only used the tuml restlet gui. It is never persisted. Its value is generated by the gui.");
        tmpId.setVisibility(OJVisibilityKindGEN.PRIVATE);
        annotatedClass.addToFields(tmpId);
    }

    @Override
    public void visitAfter(Class element) {
    }
}
