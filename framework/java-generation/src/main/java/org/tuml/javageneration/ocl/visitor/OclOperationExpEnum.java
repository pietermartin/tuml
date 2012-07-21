package org.tuml.javageneration.ocl.visitor;

import java.util.List;
import java.util.logging.Logger;

import org.eclipse.ocl.expressions.OperationCallExp;
import org.eclipse.ocl.utilities.PredefinedType;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Operation;
import org.tuml.javageneration.ocl.visitor.tojava.OclAsBagExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclAsOrderedSetExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclAsSequenceExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclAsSetExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclConcatExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclDefaultToStringExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclEqualExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclFirstExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclFlattenExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclIncludingExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclMinusExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclNotEqualExprToJava;
import org.tuml.javageneration.ocl.visitor.tojava.OclToStringExprToJava;

public enum OclOperationExpEnum implements HandleOperationExp {

	INCLUDING(new OclIncludingExprToJava()), TO_STRING(new OclToStringExprToJava()), FIRST(new OclFirstExprToJava()), MINUS(new OclMinusExprToJava()), EQUAL(new OclEqualExprToJava()), NOT_EQUAL(new OclNotEqualExprToJava()), AS_SET(new OclAsSetExprToJava()), AS_SEQUENCE(new OclAsSequenceExprToJava()), AS_ORDERED_SET(
			new OclAsOrderedSetExprToJava()), AS_BAG(new OclAsBagExprToJava()), FLATTEN(new OclFlattenExprToJava()), CONCAT(new OclConcatExprToJava()), DEFAULT(new OclDefaultToStringExprToJava());
	private static Logger logger = Logger.getLogger(OclOperationExpEnum.class.getPackage().getName());
	private HandleOperationExp implementor;

	private OclOperationExpEnum(HandleOperationExp implementor) {
		this.implementor = implementor;
	}

	public static OclOperationExpEnum from(String name) {
		if (name.equals(PredefinedType.EQUAL_NAME)) {
			return EQUAL;
		} else if (name.equals(PredefinedType.NOT_EQUAL_NAME)) {
			return NOT_EQUAL;
		} else if (name.equals(PredefinedType.AS_SET_NAME)) {
			return AS_SET;
		} else if (name.equals(PredefinedType.AS_SEQUENCE_NAME)) {
			return AS_SEQUENCE;
		} else if (name.equals(PredefinedType.AS_ORDERED_SET_NAME)) {
			return AS_ORDERED_SET;
		} else if (name.equals(PredefinedType.AS_BAG_NAME)) {
			return AS_BAG;
		} else if (name.equals(PredefinedType.FLATTEN_NAME)) {
			return FLATTEN;
		} else if (name.equals(PredefinedType.CONCAT_NAME)) {
			return CONCAT;
		} else if (name.equals(PredefinedType.MINUS_NAME)) {
			return MINUS;
		} else if (name.equals(PredefinedType.FIRST_NAME)) {
			return FIRST;
		} else if (name.equals(PredefinedType.TO_STRING_NAME)) {
			return TO_STRING;
		} else if (name.equals(PredefinedType.INCLUDING_NAME)) {
			return INCLUDING;
		} else {
			logger.warning(String.format("Not yet implemented, '%s'", name));
			return DEFAULT;
		}
	}

	public String handleOperationExp(OperationCallExp<Classifier, Operation> oc, String sourceResult, List<String> argumentResults) {
		return implementor.handleOperationExp(oc, sourceResult, argumentResults);
	}
}
