package org.umlg.javageneration.visitor.enumeration;

import org.eclipse.uml2.uml.Enumeration;
import org.umlg.java.metamodel.OJIfStatement;
import org.umlg.java.metamodel.OJPathName;
import org.umlg.java.metamodel.annotation.OJAnnotatedClass;
import org.umlg.java.metamodel.annotation.OJAnnotatedOperation;
import org.umlg.framework.Visitor;
import org.umlg.generation.Workspace;
import org.umlg.javageneration.visitor.BaseVisitor;

public class TofromJsonForEnumCreator extends BaseVisitor implements Visitor<Enumeration> {

	public TofromJsonForEnumCreator(Workspace workspace) {
		super(workspace);
	}

	@Override
	public void visitBefore(Enumeration element) {
		OJAnnotatedClass annotatedClass = findOJClass(element);
		OJAnnotatedOperation toJson = new OJAnnotatedOperation("toJson", new OJPathName("String"));
		toJson.getBody().addToStatements("return name()");
		annotatedClass.addToOperations(toJson);
		
		OJAnnotatedOperation fromJson = new OJAnnotatedOperation("fromJson", annotatedClass.getPathName());
		fromJson.setStatic(true);
		fromJson.addParam("json", new OJPathName("String"));
		OJIfStatement ifS = new OJIfStatement("json == null || json.equals(\"null\")", "return null");
		ifS.addToElsePart("return " + annotatedClass.getPathName().getLast() + ".valueOf(json)");
		fromJson.getBody().addToStatements(ifS);
		annotatedClass.addToOperations(fromJson);
		
	}

	@Override
	public void visitAfter(Enumeration element) {
	}

}
