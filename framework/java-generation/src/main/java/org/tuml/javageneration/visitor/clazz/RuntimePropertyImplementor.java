package org.tuml.javageneration.visitor.clazz;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.opaeum.java.metamodel.OJField;
import org.opaeum.java.metamodel.OJForStatement;
import org.opaeum.java.metamodel.OJIfStatement;
import org.opaeum.java.metamodel.OJPathName;
import org.opaeum.java.metamodel.annotation.OJAnnotatedClass;
import org.opaeum.java.metamodel.annotation.OJAnnotatedOperation;
import org.opaeum.java.metamodel.annotation.OJEnum;
import org.opaeum.java.metamodel.annotation.OJEnumLiteral;
import org.tuml.javageneration.util.DataTypeEnum;
import org.tuml.javageneration.util.PropertyWrapper;
import org.tuml.javageneration.util.TinkerGenerationUtil;
import org.tuml.javageneration.validation.Validation;

public class RuntimePropertyImplementor {

	public static OJEnum addTumlRuntimePropertyEnum(OJAnnotatedClass annotatedClass, String enumName, NamedElement className, Set<Property> allOwnedProperties,
			boolean hasCompositeOwner, String modelName) {
		OJEnum ojEnum = new OJEnum(enumName);
		ojEnum.setStatic(true);
		ojEnum.addToImplementedInterfaces(TinkerGenerationUtil.tumlRuntimePropertyPathName.getCopy());
		annotatedClass.addInnerEnum(ojEnum);

		OJField qualifiedName = new OJField();
		qualifiedName.setType(new OJPathName("String"));
		qualifiedName.setName("_qualifiedName");
		ojEnum.addToFields(qualifiedName);

		OJField isOnePrimitiveField = new OJField();
		isOnePrimitiveField.setType(new OJPathName("boolean"));
		isOnePrimitiveField.setName("_onePrimitive");
		ojEnum.addToFields(isOnePrimitiveField);

		OJField dataTypeEnum = new OJField();
		dataTypeEnum.setType(TinkerGenerationUtil.DataTypeEnum);
		dataTypeEnum.setName("dataTypeEnum");
		ojEnum.addToFields(dataTypeEnum);
		annotatedClass.addToImports(TinkerGenerationUtil.DataTypeEnum);

		OJField validations = new OJField();
		validations.setType(new OJPathName("java.util.List").addToGenerics(TinkerGenerationUtil.TumlValidation));
		validations.setName("validations");
		ojEnum.addToFields(validations);
		annotatedClass.addToImports(TinkerGenerationUtil.TumlValidation);

		OJField isManyPrimitiveField = new OJField();
		isManyPrimitiveField.setType(new OJPathName("boolean"));
		isManyPrimitiveField.setName("_manyPrimitive");
		ojEnum.addToFields(isManyPrimitiveField);

		OJField isOneEnumerationField = new OJField();
		isOneEnumerationField.setType(new OJPathName("boolean"));
		isOneEnumerationField.setName("_oneEnumeration");
		ojEnum.addToFields(isOneEnumerationField);

		OJField isManyEnumerationField = new OJField();
		isManyEnumerationField.setType(new OJPathName("boolean"));
		isManyEnumerationField.setName("_manyEnumeration");
		ojEnum.addToFields(isManyEnumerationField);

		OJField inverseField = new OJField();
		inverseField.setType(new OJPathName("boolean"));
		inverseField.setName("_controllingSide");
		ojEnum.addToFields(inverseField);

		OJField compositeField = new OJField();
		compositeField.setType(new OJPathName("boolean"));
		compositeField.setName("_composite");
		ojEnum.addToFields(compositeField);

		OJField inverseCompositeField = new OJField();
		inverseCompositeField.setType(new OJPathName("boolean"));
		inverseCompositeField.setName("_inverseComposite");
		ojEnum.addToFields(inverseCompositeField);

		OJField labelField = new OJField();
		labelField.setType(new OJPathName("String"));
		labelField.setName("_label");
		ojEnum.addToFields(labelField);

		OJField isOneToOneField = new OJField();
		isOneToOneField.setType(new OJPathName("boolean"));
		isOneToOneField.setName("_oneToOne");
		ojEnum.addToFields(isOneToOneField);

		OJField isOneToManyField = new OJField();
		isOneToManyField.setType(new OJPathName("boolean"));
		isOneToManyField.setName("_oneToMany");
		ojEnum.addToFields(isOneToManyField);

		OJField isManyToOneField = new OJField();
		isManyToOneField.setType(new OJPathName("boolean"));
		isManyToOneField.setName("_manyToOne");
		ojEnum.addToFields(isManyToOneField);

		OJField isManyToManyField = new OJField();
		isManyToManyField.setType(new OJPathName("boolean"));
		isManyToManyField.setName("_manyToMany");
		ojEnum.addToFields(isManyToManyField);

		OJField upperField = new OJField();
		upperField.setType(new OJPathName("int"));
		upperField.setName("_upper");
		ojEnum.addToFields(upperField);

		OJField lowerField = new OJField();
		lowerField.setType(new OJPathName("int"));
		lowerField.setName("_lower");
		ojEnum.addToFields(lowerField);

		OJField qualifiedField = new OJField();
		qualifiedField.setType(new OJPathName("boolean"));
		qualifiedField.setName("_qualified");
		ojEnum.addToFields(qualifiedField);

		OJField inverseQualifiedField = new OJField();
		inverseQualifiedField.setType(new OJPathName("boolean"));
		inverseQualifiedField.setName("_inverseQualified");
		ojEnum.addToFields(inverseQualifiedField);

		OJField orderedField = new OJField();
		orderedField.setType(new OJPathName("boolean"));
		orderedField.setName("_ordered");
		ojEnum.addToFields(orderedField);

		OJField inverseOrderedField = new OJField();
		inverseOrderedField.setType(new OJPathName("boolean"));
		inverseOrderedField.setName("_inverseOrdered");
		ojEnum.addToFields(inverseOrderedField);

		OJField uniqueField = new OJField();
		uniqueField.setType(new OJPathName("boolean"));
		uniqueField.setName("_unique");
		ojEnum.addToFields(uniqueField);

		OJField jsonField = new OJField();
		jsonField.setType(new OJPathName("String"));
		jsonField.setName("_json");
		ojEnum.addToFields(jsonField);

		ojEnum.implementGetter();
		ojEnum.createConstructorFromFields();

		OJAnnotatedOperation fromLabel = new OJAnnotatedOperation("fromLabel", new OJPathName(enumName));
		fromLabel.addParam("label", new OJPathName("String"));
		fromLabel.setStatic(true);
		ojEnum.addToOperations(fromLabel);

		OJAnnotatedOperation isValid = new OJAnnotatedOperation("isValid", new OJPathName("boolean"));
		TinkerGenerationUtil.addOverrideAnnotation(isValid);
		isValid.addParam("elementCount", new OJPathName("int"));
		OJIfStatement ifQualified = new OJIfStatement("isQualified()");
		ifQualified.addToThenPart("return elementCount >= getLower()");
		ifQualified.addToElsePart("return (getUpper() == -1 || elementCount <= getUpper()) && elementCount >= getLower()");
		isValid.getBody().addToStatements(ifQualified);
		ojEnum.addToOperations(isValid);

		OJAnnotatedOperation toJson = new OJAnnotatedOperation("toJson", new OJPathName("String"));
		TinkerGenerationUtil.addOverrideAnnotation(toJson);
		toJson.getBody().addToStatements("return getJson()");
		ojEnum.addToOperations(toJson);

		OJAnnotatedOperation asJson = new OJAnnotatedOperation("asJson", new OJPathName("String"));
		asJson.setStatic(true);
		asJson.getBody().addToStatements("StringBuilder sb = new StringBuilder();");
		asJson.getBody().addToStatements("name", "sb.append(\"{\\\"name\\\": \\\"" + className.getName() + "\\\", \")");
		asJson.getBody().addToStatements("uri", "sb.append(\"\\\"uri\\\": \\\"TODO\\\", \")");
		asJson.getBody().addToStatements("properties", "sb.append(\"\\\"properties\\\": [\")");

		OJField count = new OJField("count", new OJPathName("int"));
		count.setInitExp("1");
		asJson.getBody().addToLocals(count);

		OJForStatement forLiterals = new OJForStatement("l", new OJPathName(enumName), ojEnum.getName() + ".values()");
		forLiterals.getBody().addToStatements("sb.append(l.toJson())");
		OJIfStatement ifCountSize = new OJIfStatement("count < " + ojEnum.getName() + ".values().length");
		ifCountSize.addToThenPart("count++");
		ifCountSize.addToThenPart("sb.append(\",\")");
		forLiterals.getBody().addToStatements(ifCountSize);
		asJson.getBody().addToStatements(forLiterals);
		ojEnum.addToOperations(asJson);

		for (Property p : allOwnedProperties) {
			PropertyWrapper pWrap = new PropertyWrapper(p);
			if (!(pWrap.isDerived() || pWrap.isDerivedUnion())) {
				addEnumLiteral(ojEnum, fromLabel, pWrap.fieldname(), pWrap.javaBaseTypePath().toJavaString(), pWrap.isPrimitive(), pWrap.getDataTypeEnum(), pWrap.getValidations(), pWrap.isEnumeration(),
						pWrap.isManyToOne(), pWrap.isMany(), pWrap.isControllingSide(), pWrap.isComposite(), pWrap.isInverseComposite(), pWrap.isOneToOne(), pWrap.isOneToMany(),
						pWrap.isManyToMany(), pWrap.getUpper(), pWrap.getLower(), pWrap.isQualified(), pWrap.isInverseQualified(), pWrap.isOrdered(), pWrap.isInverseOrdered(),
						pWrap.isUnique(), TinkerGenerationUtil.getEdgeName(pWrap.getProperty()));
			}
		}

		if (!hasCompositeOwner/* && !(className instanceof Model)*/) {
			// Add in fake property to root
			addEnumLiteral(ojEnum, fromLabel, modelName, modelName, false, null, Collections.<Validation> emptyList(), false, false, false, true, false, true, true, false, false, -1, 0,
					false, false, false, false, false, "root" + className.getName());
		}
		asJson.getBody().addToStatements("sb.append(\"]}\")");
		asJson.getBody().addToStatements("return sb.toString()");
		fromLabel.getBody().addToStatements("return null");
		return ojEnum;
	}

	public static OJEnumLiteral addEnumLiteral(OJEnum ojEnum, OJAnnotatedOperation fromLabel, String fieldName, String qualifiedName, boolean isPrimitive, DataTypeEnum dataTypeEnum,
			List<Validation> validations, boolean isEnumeration, boolean isManyToOne, boolean isMany, boolean isControllingSide, boolean isComposite, boolean isInverseComposite,
			boolean isOneToOne, boolean isOneToMany, boolean isManyToMany, int getUpper, int getLower, boolean isQualified, boolean isInverseQualified, boolean isOrdered,
			boolean isInverseOrdered, boolean isUnique, String edgeName) {

		OJIfStatement ifLabelEquals = new OJIfStatement(fieldName + ".getLabel().equals(label)");
		// Do not make upper case, leave with java case sensitive
		// semantics
		ifLabelEquals.addToThenPart("return " + fieldName);
		fromLabel.getBody().addToStatements(0, ifLabelEquals);

		OJEnumLiteral ojLiteral = new OJEnumLiteral(fieldName);

		OJField propertyQualifiedNameField = new OJField();
		propertyQualifiedNameField.setType(new OJPathName("String"));
		propertyQualifiedNameField.setInitExp("\"" + qualifiedName + "\"");
		ojLiteral.addToAttributeValues(propertyQualifiedNameField);

		OJField propertyOnePrimitiveField = new OJField();
		propertyOnePrimitiveField.setType(new OJPathName("boolean"));
		// A one primitive property is a isManyToOne. Seeing as the
		// opposite end is null it defaults to many
		propertyOnePrimitiveField.setInitExp(Boolean.toString(isPrimitive && isManyToOne));
		ojLiteral.addToAttributeValues(propertyOnePrimitiveField);

		OJField propertyDataTypeEnumField = new OJField();
		propertyDataTypeEnumField.setType(TinkerGenerationUtil.DataTypeEnum);
		if (dataTypeEnum != null) {
			propertyDataTypeEnumField.setInitExp(dataTypeEnum.getInitExpression());
		} else {
			propertyDataTypeEnumField.setInitExp("null");
		}
		ojLiteral.addToAttributeValues(propertyDataTypeEnumField);

		OJField propertyValidationsField = new OJField();
		propertyValidationsField.setType(new OJPathName("java.util.ArrayList"));
		StringBuilder sb1 = new StringBuilder();
		for (Validation validation : validations) {
			sb1.append(validation.toNewRuntimeTumlValidation());
			ojEnum.addToImports(validation.getPathName());
		}
		if (validations.isEmpty()) {
			propertyValidationsField.setInitExp("Collections.<TumlValidation>emptyList()");
			ojEnum.addToImports("java.util.Collections");
		} else {
			ojEnum.addToImports(new OJPathName("java.util.Arrays"));
			propertyValidationsField.setInitExp("Arrays.<TumlValidation>asList(" + sb1.toString() + ")");
		}
		ojLiteral.addToAttributeValues(propertyValidationsField);

		OJField propertyManyPrimitiveField = new OJField();
		propertyManyPrimitiveField.setType(new OJPathName("boolean"));
		propertyManyPrimitiveField.setInitExp(Boolean.toString(isPrimitive && isMany));
		ojLiteral.addToAttributeValues(propertyManyPrimitiveField);

		OJField propertyOneEnumerationField = new OJField();
		propertyOneEnumerationField.setType(new OJPathName("boolean"));
		// A one primitive property is a isManyToOne. Seeing as the
		// opposite end is null it defaults to many
		propertyOneEnumerationField.setInitExp(Boolean.toString(isEnumeration && isManyToOne));
		ojLiteral.addToAttributeValues(propertyOneEnumerationField);

		OJField propertyManyEnumerationField = new OJField();
		propertyManyEnumerationField.setType(new OJPathName("boolean"));
		propertyManyEnumerationField.setInitExp(Boolean.toString(isEnumeration && isMany));
		ojLiteral.addToAttributeValues(propertyManyEnumerationField);

		OJField propertyControllingSideField = new OJField();
		propertyControllingSideField.setType(new OJPathName("boolean"));
		propertyControllingSideField.setInitExp(Boolean.toString(isControllingSide));
		ojLiteral.addToAttributeValues(propertyControllingSideField);

		OJField compositeLabelField = new OJField();
		compositeLabelField.setType(new OJPathName("boolean"));
		compositeLabelField.setInitExp(Boolean.toString(isComposite));
		ojLiteral.addToAttributeValues(compositeLabelField);

		OJField inverseCompositeLabelField = new OJField();
		inverseCompositeLabelField.setType(new OJPathName("boolean"));
		inverseCompositeLabelField.setInitExp(Boolean.toString(isInverseComposite));
		ojLiteral.addToAttributeValues(inverseCompositeLabelField);

		OJField propertyLabelField = new OJField();
		propertyLabelField.setType(new OJPathName("String"));
		propertyLabelField.setInitExp("\"" + edgeName + "\"");
		ojLiteral.addToAttributeValues(propertyLabelField);

		OJField isOneToOneAttribute = new OJField();
		isOneToOneAttribute.setType(new OJPathName("boolean"));
		isOneToOneAttribute.setInitExp(Boolean.toString(isOneToOne));
		ojLiteral.addToAttributeValues(isOneToOneAttribute);

		OJField isOneToManyAttribute = new OJField();
		isOneToManyAttribute.setType(new OJPathName("boolean"));
		isOneToManyAttribute.setInitExp(Boolean.toString(isOneToMany));
		ojLiteral.addToAttributeValues(isOneToManyAttribute);

		OJField isManyToOneAttribute = new OJField();
		isManyToOneAttribute.setType(new OJPathName("boolean"));
		isManyToOneAttribute.setInitExp(Boolean.toString(isManyToOne));
		ojLiteral.addToAttributeValues(isManyToOneAttribute);

		OJField isManyToManyAttribute = new OJField();
		isManyToManyAttribute.setType(new OJPathName("boolean"));
		isManyToManyAttribute.setInitExp(Boolean.toString(isManyToMany));
		ojLiteral.addToAttributeValues(isManyToManyAttribute);

		OJField upperAttribute = new OJField();
		upperAttribute.setType(new OJPathName("int"));
		upperAttribute.setInitExp(Integer.toString(getUpper));
		ojLiteral.addToAttributeValues(upperAttribute);

		OJField lowerAttribute = new OJField();
		lowerAttribute.setType(new OJPathName("int"));
		lowerAttribute.setInitExp(Integer.toString(getLower));
		ojLiteral.addToAttributeValues(lowerAttribute);

		OJField qualifiedAttribute = new OJField();
		qualifiedAttribute.setType(new OJPathName("boolean"));
		qualifiedAttribute.setInitExp(Boolean.toString(isQualified));
		ojLiteral.addToAttributeValues(qualifiedAttribute);

		OJField inverseQualifiedAttribute = new OJField();
		inverseQualifiedAttribute.setType(new OJPathName("boolean"));
		inverseQualifiedAttribute.setInitExp(Boolean.toString(isInverseQualified));
		ojLiteral.addToAttributeValues(inverseQualifiedAttribute);

		OJField orderedAttribute = new OJField();
		orderedAttribute.setType(new OJPathName("boolean"));
		orderedAttribute.setInitExp(Boolean.toString(isOrdered));
		ojLiteral.addToAttributeValues(orderedAttribute);

		OJField inverseOrderedAttribute = new OJField();
		inverseOrderedAttribute.setType(new OJPathName("boolean"));
		inverseOrderedAttribute.setInitExp(Boolean.toString(isInverseOrdered));
		ojLiteral.addToAttributeValues(inverseOrderedAttribute);

		OJField uniqueAttribute = new OJField();
		uniqueAttribute.setType(new OJPathName("boolean"));
		uniqueAttribute.setInitExp(Boolean.toString(isUnique));
		ojLiteral.addToAttributeValues(uniqueAttribute);

		OJField jsonAttribute = new OJField();
		jsonAttribute.setName("json");
		jsonAttribute.setType(new OJPathName("String"));

		StringBuilder sb = new StringBuilder();
		sb.append("{\\\"name\\\": \\\"");
		sb.append(fieldName);
		

		sb.append("\\\", ");
		sb.append("\\\"onePrimitive\\\": ");
		sb.append(propertyOnePrimitiveField.getInitExp());
		sb.append(", ");
		if (dataTypeEnum != null) {
			sb.append("\\\"dataTypeEnum\\\": \\\"\" + ");
			sb.append(propertyDataTypeEnumField.getInitExp());
			sb.append(".toString() + \"\\\", ");
		} else {
			sb.append("\\\"dateTypeEnum\\\": null, ");
		}

		if (!validations.isEmpty()) {
			sb.append("\\\"validations\\\": {");
			for (Validation validation : validations) {
				sb.append(validation.toJson());
			}
			sb.append("}, ");
		} else {
			sb.append("\\\"validations\\\": null, ");
		}

		sb.append("\\\"qualifiedName\\\": \\");
		sb.append(propertyQualifiedNameField.getInitExp().subSequence(0, propertyQualifiedNameField.getInitExp().length() - 1));
		sb.append("\\\", ");
		
		sb.append("\\\"manyPrimitive\\\": ");
		sb.append(propertyManyPrimitiveField.getInitExp());
		sb.append(", ");
		sb.append("\\\"oneEnumeration\\\": ");
		sb.append(propertyOneEnumerationField.getInitExp());
		sb.append(", ");
		sb.append("\\\"manyEnumeration\\\": ");
		sb.append(propertyManyEnumerationField.getInitExp());
		sb.append(", ");
		sb.append("\\\"controllingSide\\\": ");
		sb.append(propertyControllingSideField.getInitExp());
		sb.append(", ");
		sb.append("\\\"composite\\\": ");
		sb.append(compositeLabelField.getInitExp());
		sb.append(", ");
		sb.append("\\\"inverseComposite\\\": ");
		sb.append(inverseCompositeLabelField.getInitExp());
		sb.append(", ");
		sb.append("\\\"oneToOne\\\": ");
		sb.append(isOneToOneAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"oneToMany\\\": ");
		sb.append(isOneToManyAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"manyToOne\\\": ");
		sb.append(isManyToOneAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"manyToMany\\\": ");
		sb.append(isManyToManyAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"upper\\\": ");
		sb.append(upperAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"lower\\\": ");
		sb.append(lowerAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"label\\\": \\");
		sb.append(propertyLabelField.getInitExp().subSequence(0, propertyLabelField.getInitExp().length() - 1));
		sb.append("\\\", ");
		sb.append("\\\"qualified\\\": ");
		sb.append(qualifiedAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"inverseQualified\\\": ");
		sb.append(inverseQualifiedAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"inverseOrdered\\\": ");
		sb.append(orderedAttribute.getInitExp());
		sb.append(", ");
		sb.append("\\\"unique\\\": ");
		sb.append(uniqueAttribute.getInitExp());
		sb.append("}");
		jsonAttribute.setInitExp("\"" + sb.toString() + "\"");
		ojLiteral.addToAttributeValues(jsonAttribute);

		ojEnum.addToLiterals(ojLiteral);
		return ojLiteral;
	}

}
