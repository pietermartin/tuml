package org.tuml.javageneration.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.internal.operations.OperationOperations;
import org.tuml.javageneration.ocl.TumlOcl2Java;

public class TumlOperationOperations extends OperationOperations {

	public static String asOclSignature(Operation oper) {
		StringBuilder sb = new StringBuilder();
		sb.append(oper.getClass_().getName());
		sb.append("::");
		sb.append(oper.getName());
		sb.append("(");
		List<Parameter> parametersExceptReturn = getParametersExceptReturn(oper);
		int count = 1;
		for (Parameter param : parametersExceptReturn) {
			sb.append(param.getName());
			sb.append(": ");
			sb.append(param.getType().getName());
			if (count++ != parametersExceptReturn.size()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		Parameter returnResult = oper.getReturnResult();
		if (returnResult!=null) {
			sb.append(" : ");
			if (TumlMultiplicityOperations.isMany(returnResult)) {
				sb.append(TumlOcl2Java.getCollectionInterface(returnResult));
				sb.append("(");
				sb.append(returnResult.getType().getName());
				sb.append(")");
			} else {
				sb.append(returnResult.getType().getName());
			}
		}
		return sb.toString();
	}

	public static List<Parameter> getParametersExceptReturn(Operation oper) {
		List<Parameter> result = new ArrayList<Parameter>();
		for (Parameter param : oper.getOwnedParameters()) {
			if (param.getDirection() != ParameterDirectionKind.RETURN_LITERAL) {
				result.add(param);
			}
		}
		return result;
	}
}
