package org.umlg.javageneration.visitor.clazz;

import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Property;
import org.umlg.java.metamodel.OJField;
import org.umlg.java.metamodel.OJForStatement;
import org.umlg.java.metamodel.OJIfStatement;
import org.umlg.java.metamodel.OJPathName;
import org.umlg.java.metamodel.annotation.OJAnnotatedClass;
import org.umlg.java.metamodel.annotation.OJAnnotatedOperation;
import org.umlg.java.metamodel.annotation.OJEnum;
import org.umlg.java.metamodel.annotation.OJEnumLiteral;
import org.umlg.javageneration.util.DataTypeEnum;
import org.umlg.javageneration.util.PropertyWrapper;
import org.umlg.javageneration.util.UmlgAssociationClassOperations;
import org.umlg.javageneration.util.UmlgGenerationUtil;
import org.umlg.javageneration.validation.Validation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class RuntimePropertyImplementor {

    public static OJEnum addTumlRuntimePropertyEnum(OJAnnotatedClass annotatedClass, String enumName, NamedElement namedElement,
                                                    Set<Property> allOwnedProperties,
                                                    boolean hasCompositeOwner, String modelName) {

        annotatedClass.addToImports(UmlgGenerationUtil.UmlgLabelConverterFactoryPathName);

        OJEnum ojEnum = new OJEnum(enumName);
        ojEnum.setStatic(true);
        ojEnum.addToImplementedInterfaces(UmlgGenerationUtil.umlgRuntimePropertyPathName.getCopy());
        annotatedClass.addInnerEnum(ojEnum);

        OJField qualifiedName = new OJField();
        qualifiedName.setType(new OJPathName("String"));
        qualifiedName.setName("_qualifiedName");
        ojEnum.addToFields(qualifiedName);

        OJField persistentName = new OJField();
        persistentName.setType(new OJPathName("String"));
        persistentName.setName("_persistentName");
        ojEnum.addToFields(persistentName);

        OJField inverseName = new OJField();
        inverseName.setType(new OJPathName("String"));
        inverseName.setName("_inverseName");
        ojEnum.addToFields(inverseName);

        OJField inverseQualifiedName = new OJField();
        inverseQualifiedName.setType(new OJPathName("String"));
        inverseQualifiedName.setName("_inverseQualifiedName");
        ojEnum.addToFields(inverseQualifiedName);

        OJField isAssociationClassOneField = new OJField();
        isAssociationClassOneField.setType(new OJPathName("boolean"));
        isAssociationClassOneField.setName("_associationClassOne");
        ojEnum.addToFields(isAssociationClassOneField);

        OJField isMemberEndOfAssociationClassField = new OJField();
        isMemberEndOfAssociationClassField.setType(new OJPathName("boolean"));
        isMemberEndOfAssociationClassField.setName("_memberEndOfAssociationClass");
        ojEnum.addToFields(isMemberEndOfAssociationClassField);

        OJField associationClassPropertyNameField = new OJField();
        associationClassPropertyNameField.setType(new OJPathName("String"));
        associationClassPropertyNameField.setName("_associationClassPropertyName");
        ojEnum.addToFields(associationClassPropertyNameField);

        OJField inverseAssociationClassPropertyNameField = new OJField();
        inverseAssociationClassPropertyNameField.setType(new OJPathName("String"));
        inverseAssociationClassPropertyNameField.setName("_inverseAssociationClassPropertyName");
        ojEnum.addToFields(inverseAssociationClassPropertyNameField);

        OJField isAssociationClassPropertyField = new OJField();
        isAssociationClassPropertyField.setType(new OJPathName("boolean"));
        isAssociationClassPropertyField.setName("_associationClassProperty");
        ojEnum.addToFields(isAssociationClassPropertyField);

        OJField isOnePrimitivePropertyOfAssociationClassField = new OJField();
        isOnePrimitivePropertyOfAssociationClassField.setType(new OJPathName("boolean"));
        isOnePrimitivePropertyOfAssociationClassField.setName("_onePrimitivePropertyOfAssociationClass");
        ojEnum.addToFields(isOnePrimitivePropertyOfAssociationClassField);

        OJField isOnePrimitiveField = new OJField();
        isOnePrimitiveField.setType(new OJPathName("boolean"));
        isOnePrimitiveField.setName("_onePrimitive");
        ojEnum.addToFields(isOnePrimitiveField);

        OJField readOnly = new OJField();
        readOnly.setType(new OJPathName("Boolean"));
        readOnly.setName("_readOnly");
        ojEnum.addToFields(readOnly);

        OJField dataTypeEnum = new OJField();
        dataTypeEnum.setType(UmlgGenerationUtil.DataTypeEnum);
        dataTypeEnum.setName("dataTypeEnum");
        ojEnum.addToFields(dataTypeEnum);
        annotatedClass.addToImports(UmlgGenerationUtil.DataTypeEnum);

        OJField validations = new OJField();
        validations.setType(new OJPathName("java.util.List").addToGenerics(UmlgGenerationUtil.UmlgValidation));
        validations.setName("validations");
        ojEnum.addToFields(validations);
        annotatedClass.addToImports(UmlgGenerationUtil.UmlgValidation);

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

        OJField inverseUpperField = new OJField();
        inverseUpperField.setType(new OJPathName("int"));
        inverseUpperField.setName("_inverseUpper");
        ojEnum.addToFields(inverseUpperField);

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

        OJField inverseUniqueField = new OJField();
        inverseUniqueField.setType(new OJPathName("boolean"));
        inverseUniqueField.setName("_inverseUnique");
        ojEnum.addToFields(inverseUniqueField);

        OJField derivedField = new OJField();
        derivedField.setType(new OJPathName("boolean"));
        derivedField.setName("_derived");
        ojEnum.addToFields(derivedField);

        OJField navigabilityField = new OJField();
        navigabilityField.setType(new OJPathName("boolean"));
        navigabilityField.setName("_navigability");
        ojEnum.addToFields(navigabilityField);

        OJField propertyType = new OJField();
        propertyType.setType(new OJPathName("java.lang.Class"));
        propertyType.setName("_propertyType");
        ojEnum.addToFields(propertyType);

        OJField jsonField = new OJField();
        jsonField.setType(new OJPathName("String"));
        jsonField.setName("_json");
        ojEnum.addToFields(jsonField);

        OJField changeListenerField = new OJField();
        changeListenerField.setType(new OJPathName("boolean"));
        changeListenerField.setName("_changeListener");
        ojEnum.addToFields(changeListenerField);

        ojEnum.implementGetter();
        ojEnum.createConstructorFromFields();

        OJAnnotatedOperation fromLabel = new OJAnnotatedOperation("fromLabel", new OJPathName(enumName));
        fromLabel.addParam("_label", new OJPathName("String"));
        fromLabel.setStatic(true);
        ojEnum.addToOperations(fromLabel);

        OJAnnotatedOperation fromQualifiedName = new OJAnnotatedOperation("fromQualifiedName", new OJPathName(enumName));
        fromQualifiedName.addParam("qualifiedName", new OJPathName("String"));
        fromQualifiedName.setStatic(true);
        ojEnum.addToOperations(fromQualifiedName);

        OJAnnotatedOperation fromInverseQualifiedName = new OJAnnotatedOperation("fromInverseQualifiedName", new OJPathName(enumName));
        fromInverseQualifiedName.addParam("inverseQualifiedName", new OJPathName("String"));
        fromInverseQualifiedName.setStatic(true);
        ojEnum.addToOperations(fromInverseQualifiedName);

        OJAnnotatedOperation isValid = new OJAnnotatedOperation("isValid", new OJPathName("boolean"));
        UmlgGenerationUtil.addOverrideAnnotation(isValid);
        isValid.addParam("elementCount", new OJPathName("int"));
        OJIfStatement ifQualified = new OJIfStatement("isQualified()");
        ifQualified.addToThenPart("return elementCount >= getLower()");
        ifQualified.addToElsePart("return (getUpper() == -1 || elementCount <= getUpper()) && elementCount >= getLower()");
        isValid.getBody().addToStatements(ifQualified);
        ojEnum.addToOperations(isValid);

        OJAnnotatedOperation toJson = new OJAnnotatedOperation("toJson", new OJPathName("String"));
        UmlgGenerationUtil.addOverrideAnnotation(toJson);
        toJson.getBody().addToStatements("return getJson()");
        ojEnum.addToOperations(toJson);

        OJAnnotatedOperation asJson = new OJAnnotatedOperation("asJson", new OJPathName("String"));
        asJson.setStatic(true);
        asJson.getBody().addToStatements("StringBuilder sb = new StringBuilder();");
        asJson.getBody().addToStatements("name", "sb.append(\"{\\\"name\\\": \\\"" + namedElement.getName() + "\\\", \")");
        asJson.getBody().addToStatements("qualifiedName", "sb.append(\"\\\"qualifiedName\\\": \\\"" + namedElement.getQualifiedName() + "\\\", \")");
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

            annotatedClass.addToImports(pWrap.javaBaseTypePath());

            int inverseUpper = 1;
            if (pWrap.getOtherEnd() != null) {
                inverseUpper = pWrap.getOtherEnd().getUpper();
            }
            //!(namedElement instanceof AssociationClass)
            if (!(namedElement instanceof Classifier && UmlgAssociationClassOperations.extendsAssociationClass((Classifier)namedElement))) {
                addEnumLiteral(
                        false,
                        pWrap.isMemberOfAssociationClass(),
                        pWrap.isMemberOfAssociationClass() ? pWrap.getAssociationClassFakePropertyName() : null,
                        pWrap.isMemberOfAssociationClass() ? new PropertyWrapper(pWrap.getOtherEnd()).getAssociationClassFakePropertyName() : null,
//                        pWrap.isMemberOfAssociationClass() ? pWrap.getAssociationClassName() : null,
//                        pWrap.isMemberOfAssociationClass() ? new PropertyWrapper(pWrap.getOtherEnd()).getAssociationClassName() : null,
                        false,
                        false,
                        ojEnum,
                        fromLabel,
                        fromQualifiedName,
                        fromInverseQualifiedName,
                        pWrap.fieldname(),
                        pWrap.getQualifiedName(),
                        pWrap.getPersistentName(),
                        pWrap.getInverseName(),
                        pWrap.getInverseQualifiedName(),
                        pWrap.isReadOnly(),
                        pWrap.isPrimitive(),
                        pWrap.getDataTypeEnum(),
                        pWrap.getValidations(),
                        pWrap.isEnumeration(),
                        pWrap.isManyToOne(),
                        pWrap.isMany(),
                        pWrap.isControllingSide(),
                        pWrap.isComposite(),
                        pWrap.isInverseComposite(),
                        pWrap.isOneToOne(),
                        pWrap.isOneToMany(),
                        pWrap.isManyToMany(),
                        pWrap.getUpper(),
                        pWrap.getLower(),
                        inverseUpper,
                        pWrap.isQualified(),
                        pWrap.isInverseQualified(),
                        pWrap.isOrdered(),
                        pWrap.isInverseOrdered(),
                        pWrap.isUnique(),
                        pWrap.isInverseUnique(),
                        pWrap.isDerived(),
                        pWrap.isNavigable(),
                        UmlgGenerationUtil.getEdgeName(pWrap.getProperty()),
                        pWrap.javaBaseTypePath().getTypeName(),
                        pWrap.isChangedListener()
                );
            } else {
                //This is for properties of the association class itself
                if (pWrap.isMemberOfAssociationClass()) {
                    //These are fake properties, simulating navigating from the association class to its member ends
                    addEnumLiteral(
                            true,
                            false,
                            pWrap.getAssociationClassFakePropertyName(),
                            new PropertyWrapper(pWrap.getOtherEnd()).getAssociationClassFakePropertyName(),
//                            pWrap.getAssociationClassName(),
//                            new PropertyWrapper(pWrap.getOtherEnd()).getAssociationClassName(),
                            true,
                            false,
                            ojEnum,
                            fromLabel,
                            fromQualifiedName,
                            fromInverseQualifiedName,
                            pWrap.fieldname(),
                            pWrap.getQualifiedName(),
                            pWrap.getPersistentName(),
                            pWrap.getInverseName(), pWrap.getInverseQualifiedName() + "AC", pWrap.isReadOnly(), pWrap.isPrimitive(), pWrap.getDataTypeEnum(),
                            pWrap.getValidations(), pWrap.isEnumeration(), /*manyToOne*/pWrap.isACManyToOne(), /*many*/pWrap.isACMany(), pWrap.isControllingSide(),
                                /*composite*/true, /*inverseComposite*/true, /*oneToOne*/pWrap.isACOneToOne(), /*oneToMany*/pWrap.isACOneToMany(),
                                /*manyToMany*/pWrap.isACManyToMany(), /*upper*/1, /*lower*/1, inverseUpper, pWrap.isQualified(),
                            pWrap.isInverseQualified(), pWrap.isOrdered(), pWrap.isInverseOrdered(), pWrap.isACUnique(), pWrap.isInverseUnique(),
                            pWrap.isDerived(),
                            pWrap.isNavigable(),
                            UmlgGenerationUtil.getEdgeName(pWrap.getProperty()) + "_" + pWrap.getName() + "_AC",
                            pWrap.javaBaseTypePath().getTypeName(),
                            pWrap.isChangedListener()
                    );
                } else {
                    addEnumLiteral(
                            false /*isAssociationClassOne*/,
                            false/*isMemberEndOfAssociationClass*/,
                            null/*associationClassPropertyName*/,
                            null/*inverseAssociationClassPropertyName*/,
                            false/*isAssociationClassProperty*/,
                            (pWrap.isOne() && pWrap.isDataType()) ? true : false,
                            ojEnum,
                            fromLabel,
                            fromQualifiedName,
                            fromInverseQualifiedName,
                            pWrap.fieldname(),
                            pWrap.getQualifiedName(),
                            pWrap.getPersistentName(),
                            pWrap.getInverseName(), pWrap.getInverseQualifiedName(), pWrap.isReadOnly(), pWrap.isPrimitive(), pWrap.getDataTypeEnum(), pWrap.getValidations(),
                            pWrap.isEnumeration(), pWrap.isManyToOne(), pWrap.isMany(), pWrap.isControllingSide(), pWrap.isComposite(), pWrap.isInverseComposite(),
                            pWrap.isOneToOne(), pWrap.isOneToMany(), pWrap.isManyToMany(), pWrap.getUpper(), pWrap.getLower(), inverseUpper, pWrap.isQualified(),
                            pWrap.isInverseQualified(), pWrap.isOrdered(), pWrap.isInverseOrdered(), pWrap.isUnique(), pWrap.isInverseUnique(),
                            false/*derived*/,
                            true,/*navigable*/
                            UmlgGenerationUtil.getEdgeName(pWrap.getProperty()),
                            pWrap.javaBaseTypePath().getTypeName(),
                            pWrap.isChangedListener()
                    );
                }
            }

            if (pWrap.isMemberOfAssociationClass() && !((namedElement instanceof Classifier && (UmlgAssociationClassOperations.extendsAssociationClass((Classifier)namedElement))))) {
                //These are fake properties, simulating navigating from the member end's type to the association class itself
                addEnumLiteral(
                        true,
                        false,
                        pWrap.getAssociationClassFakePropertyName(),
                        new PropertyWrapper(pWrap.getOtherEnd()).getAssociationClassFakePropertyName(),
//                        pWrap.getAssociationClassName(),
//                        new PropertyWrapper(pWrap.getOtherEnd()).getAssociationClassName(),
                        false,
                        false,
                        ojEnum,
                        fromLabel,
                        fromQualifiedName,
                        fromInverseQualifiedName,
                        pWrap.getAssociationClassFakePropertyName(),
                        pWrap.getQualifiedName() + "AC",
                        pWrap.getPersistentName() + "AC",
                        pWrap.getInverseName(), pWrap.getInverseQualifiedName() + "AC", pWrap.isReadOnly(), pWrap.isPrimitive(), pWrap.getDataTypeEnum(), pWrap.getValidations(),
                        pWrap.isEnumeration(), pWrap.isManyToOne(), pWrap.isMany(), pWrap.isControllingSide(), pWrap.isComposite(), pWrap.isInverseComposite(),
                        pWrap.isOneToOne(), pWrap.isOneToMany(), pWrap.isManyToMany(), pWrap.getUpper(), pWrap.getLower(), inverseUpper, pWrap.isQualified(),
                        pWrap.isInverseQualified(), pWrap.isOrdered(), pWrap.isInverseOrdered(), pWrap.isUnique(), pWrap.isInverseUnique(),
                        false/*derived*/,
                        true,/*navigable*/
                        UmlgGenerationUtil.getEdgeName(pWrap.getProperty()) + "_" + pWrap.getName() + "_AC",
//                        UmlgGenerationUtil.getEdgeName(pWrap.getProperty()) + "_AC",
                        pWrap.javaBaseTypePath().getTypeName(),
                        pWrap.isChangedListener());
            }
        }
//        }

        if (!hasCompositeOwner) {
            // Add in fake property to root
            addEnumLiteral(
                    false,
                    false,
                    null,
                    null,
                    false,
                    false,
                    ojEnum,
                    fromLabel,
                    fromQualifiedName,
                    fromInverseQualifiedName,
                    modelName,
                    modelName.replace("::", "_"),
                    modelName,
                    "inverseOf" + modelName, "inverseOf" + modelName, false, false, null,
                    Collections.<Validation>emptyList(), false, false, false, true, false, true, true, false, false, -1, 0, 1, false, false, false, false, false, false, false, false,
                    "root" + namedElement.getName(),
                    "Object",
                    false);
        }
        asJson.getBody().addToStatements("sb.append(\"]}\")");
        asJson.getBody().addToStatements("return sb.toString()");
        fromLabel.getBody().addToStatements("return null");
        fromQualifiedName.getBody().addToStatements("return null");
        fromInverseQualifiedName.getBody().addToStatements("return null");

        return ojEnum;
    }

    /**
     * Very important, the order of adding the attribute values to the literal must be the same as the order the fields were created as that is the order of the constructor
     */
    public static OJEnumLiteral addEnumLiteral(
            boolean isAssociationClassOne /*This is true for the fake properties to and from the association class*/,
            boolean isMemberEndOfAssociationClass,
            String associationClassPropertyName,
            String inverseAssociationClassPropertyName,
            boolean isAssociationClassProperty /*Ths is true for the fake property from the association class to the member end*/,
            boolean isOnePrimitivePropertyOfAssociationClass /*Ths is true for the any primitive property on the association class*/,
            OJEnum ojEnum,
            OJAnnotatedOperation fromLabel,
            OJAnnotatedOperation fromQualifiedName,
            OJAnnotatedOperation fromInverseQualifiedName,
            String fieldName,
            String qualifiedName,
            String persistentName,
            String inverseName,
            String inverseQualifiedName,
            boolean isReadOnly,
            boolean isPrimitive,
            DataTypeEnum dataTypeEnum,
            List<Validation> validations,
            boolean isEnumeration,
            boolean isManyToOne,
            boolean isMany,
            boolean isControllingSide,
            boolean isComposite,
            boolean isInverseComposite,
            boolean isOneToOne,
            boolean isOneToMany,
            boolean isManyToMany,
            int getUpper,
            int getLower,
            int getInverseUpper,
            boolean isQualified,
            boolean isInverseQualified,
            boolean isOrdered,
            boolean isInverseOrdered,
            boolean isUnique,
            boolean isInverseUnique,
            boolean isDerived,
            boolean isNavigable,
            String edgeName,
            String javaQualifiedClass,
            boolean isChangeListener) {

        OJIfStatement ifLabelEquals = new OJIfStatement(fieldName + ".getLabel().equals(_label)");
        // Do not make upper case, leave with java case sensitive
        // semantics
        ifLabelEquals.addToThenPart("return " + fieldName);
        fromLabel.getBody().addToStatements(0, ifLabelEquals);

        OJIfStatement ifLabelEqualsForQualifiedName = new OJIfStatement(fieldName + ".getQualifiedName().equals(qualifiedName)");
        ifLabelEqualsForQualifiedName.addToThenPart("return " + fieldName);
        fromQualifiedName.getBody().addToStatements(0, ifLabelEqualsForQualifiedName);

        OJIfStatement ifLabelEqualsForInverseQualifiedName = new OJIfStatement(fieldName + ".getInverseQualifiedName().equals(inverseQualifiedName)");
        ifLabelEqualsForInverseQualifiedName.addToThenPart("return " + fieldName);
        fromInverseQualifiedName.getBody().addToStatements(0, ifLabelEqualsForInverseQualifiedName);

        OJEnumLiteral ojEnumLiteral = new OJEnumLiteral(fieldName);

        OJField propertyQualifiedNameField = new OJField();
        propertyQualifiedNameField.setName("qualifiedName");
        propertyQualifiedNameField.setType(new OJPathName("String"));
        propertyQualifiedNameField.setInitExp("\"" + qualifiedName + "\"");
        ojEnumLiteral.addToAttributeValues(propertyQualifiedNameField);

        OJField propertyPersistentNameField = new OJField();
        propertyPersistentNameField.setName("persistentName");
        propertyPersistentNameField.setType(new OJPathName("String"));
        propertyPersistentNameField.setInitExp("\"" + persistentName + "\"");
        ojEnumLiteral.addToAttributeValues(propertyPersistentNameField);

        OJField propertyInverseNameField = new OJField();
        propertyInverseNameField.setName("inverseName");
        propertyInverseNameField.setType(new OJPathName("String"));
        propertyInverseNameField.setInitExp("\"" + inverseName + "\"");
        ojEnumLiteral.addToAttributeValues(propertyInverseNameField);

        OJField propertyInverseQualifiedNameField = new OJField();
        propertyInverseQualifiedNameField.setName("inverseQualifiedName");
        propertyInverseQualifiedNameField.setType(new OJPathName("String"));
        propertyInverseQualifiedNameField.setInitExp("\"" + inverseQualifiedName + "\"");
        ojEnumLiteral.addToAttributeValues(propertyInverseQualifiedNameField);

        OJField isAssociationClassOneField = new OJField();
        isAssociationClassOneField.setName("isAssociationClassOne");
        isAssociationClassOneField.setType(new OJPathName("boolean"));
        isAssociationClassOneField.setInitExp(Boolean.toString(isAssociationClassOne));
        ojEnumLiteral.addToAttributeValues(isAssociationClassOneField);

        OJField isMemberEndOfAssociationClassField = new OJField();
        isMemberEndOfAssociationClassField.setName("isMemberEndOfAssociationClass");
        isMemberEndOfAssociationClassField.setType(new OJPathName("boolean"));
        isMemberEndOfAssociationClassField.setInitExp(Boolean.toString(isMemberEndOfAssociationClass));
        ojEnumLiteral.addToAttributeValues(isMemberEndOfAssociationClassField);

        OJField propertyAssociationClassPropertyNameField = new OJField();
        propertyAssociationClassPropertyNameField.setName("associationClassPropertyNameField");
        propertyAssociationClassPropertyNameField.setType(new OJPathName("String"));
        propertyAssociationClassPropertyNameField.setInitExp("\"" + associationClassPropertyName + "\"");
        ojEnumLiteral.addToAttributeValues(propertyAssociationClassPropertyNameField);

        OJField propertyInverseAssociationClassPropertyNameField = new OJField();
        propertyInverseAssociationClassPropertyNameField.setName("inverseAssociationClassPropertyNameField");
        propertyInverseAssociationClassPropertyNameField.setType(new OJPathName("String"));
        propertyInverseAssociationClassPropertyNameField.setInitExp("\"" + inverseAssociationClassPropertyName + "\"");
        ojEnumLiteral.addToAttributeValues(propertyInverseAssociationClassPropertyNameField);

        OJField isAssociationClassPropertyField = new OJField();
        isAssociationClassPropertyField.setName("isAssociationClassProperty");
        isAssociationClassPropertyField.setType(new OJPathName("boolean"));
        isAssociationClassPropertyField.setInitExp(Boolean.toString(isAssociationClassProperty));
        ojEnumLiteral.addToAttributeValues(isAssociationClassPropertyField);

        OJField isOnePrimitivePropertyOfAssociationClassField = new OJField();
        isOnePrimitivePropertyOfAssociationClassField.setName("isOnePrimitivePropertyOfAssociationClass");
        isOnePrimitivePropertyOfAssociationClassField.setType(new OJPathName("boolean"));
        isOnePrimitivePropertyOfAssociationClassField.setInitExp(Boolean.toString(isOnePrimitivePropertyOfAssociationClass));
        ojEnumLiteral.addToAttributeValues(isOnePrimitivePropertyOfAssociationClassField);

        OJField propertyOnePrimitiveField = new OJField();
        propertyOnePrimitiveField.setName("isOnePrimitive");
        propertyOnePrimitiveField.setType(new OJPathName("boolean"));
        // A one primitive property is a isManyToOne. Seeing as the
        // opposite end is null it defaults to many
        propertyOnePrimitiveField.setInitExp(Boolean.toString(isPrimitive && isManyToOne));
        ojEnumLiteral.addToAttributeValues(propertyOnePrimitiveField);

        OJField readOnlyField = new OJField();
        readOnlyField.setName("isReadOnly");
        readOnlyField.setType(new OJPathName("boolean"));
        readOnlyField.setInitExp(Boolean.toString(isReadOnly));
        ojEnumLiteral.addToAttributeValues(readOnlyField);

        OJField propertyDataTypeEnumField = new OJField();
        propertyDataTypeEnumField.setName("dataTypeEnum");
        propertyDataTypeEnumField.setType(UmlgGenerationUtil.DataTypeEnum);
        if (dataTypeEnum != null) {
            propertyDataTypeEnumField.setInitExp(dataTypeEnum.getInitExpression());
        } else {
            propertyDataTypeEnumField.setInitExp("null");
        }
        ojEnumLiteral.addToAttributeValues(propertyDataTypeEnumField);

        OJField propertyValidationsField = new OJField();
        propertyValidationsField.setName("validations");
        propertyValidationsField.setType(new OJPathName("java.util.ArrayList"));
        StringBuilder sb1 = new StringBuilder();
        for (Validation validation : validations) {
            sb1.append(validation.toNewRuntimeTumlValidation());
            ojEnum.addToImports(validation.getPathName());
        }
        if (validations.isEmpty()) {
            propertyValidationsField.setInitExp("Collections.<UmlgValidation>emptyList()");
            ojEnum.addToImports("java.util.Collections");
        } else {
            ojEnum.addToImports(new OJPathName("java.util.Arrays"));
            propertyValidationsField.setInitExp("Arrays.<UmlgValidation>asList(" + sb1.toString() + ")");
        }
        ojEnumLiteral.addToAttributeValues(propertyValidationsField);

        OJField propertyManyPrimitiveField = new OJField();
        propertyManyPrimitiveField.setName("isManyPrimitive");
        propertyManyPrimitiveField.setType(new OJPathName("boolean"));
        propertyManyPrimitiveField.setInitExp(Boolean.toString(isPrimitive && isManyToMany));
        ojEnumLiteral.addToAttributeValues(propertyManyPrimitiveField);

        OJField propertyOneEnumerationField = new OJField();
        propertyOneEnumerationField.setName("oneEnumeration");
        propertyOneEnumerationField.setType(new OJPathName("boolean"));
        // A one primitive property is a isManyToOne. Seeing as the
        // opposite end is null it defaults to many
        propertyOneEnumerationField.setInitExp(Boolean.toString(isEnumeration && (isManyToOne || isOneToOne)));
        ojEnumLiteral.addToAttributeValues(propertyOneEnumerationField);

        OJField propertyManyEnumerationField = new OJField();
        propertyManyEnumerationField.setName("manyEnumeration");
        propertyManyEnumerationField.setType(new OJPathName("boolean"));
        propertyManyEnumerationField.setInitExp(Boolean.toString(isEnumeration && isMany));
        ojEnumLiteral.addToAttributeValues(propertyManyEnumerationField);

        OJField propertyControllingSideField = new OJField();
        propertyControllingSideField.setName("isControllingSide");
        propertyControllingSideField.setType(new OJPathName("boolean"));
        propertyControllingSideField.setInitExp(Boolean.toString(isControllingSide));
        ojEnumLiteral.addToAttributeValues(propertyControllingSideField);

        OJField compositeLabelField = new OJField();
        compositeLabelField.setName("isComposite");
        compositeLabelField.setType(new OJPathName("boolean"));
        compositeLabelField.setInitExp(Boolean.toString(isComposite));
        ojEnumLiteral.addToAttributeValues(compositeLabelField);

        OJField inverseCompositeLabelField = new OJField();
        inverseCompositeLabelField.setName("isInverseComposite");
        inverseCompositeLabelField.setType(new OJPathName("boolean"));
        inverseCompositeLabelField.setInitExp(Boolean.toString(isInverseComposite));
        ojEnumLiteral.addToAttributeValues(inverseCompositeLabelField);

        OJField propertyLabelField = new OJField();
        propertyLabelField.setName("label");
        propertyLabelField.setType(new OJPathName("String"));
        propertyLabelField.setInitExp(UmlgGenerationUtil.UmlgLabelConverterFactoryPathName.getLast() + ".getUmlgLabelConverter().convert(\"" + edgeName + "\")");
        ojEnumLiteral.addToAttributeValues(propertyLabelField);

        OJField isOneToOneAttribute = new OJField();
        isOneToOneAttribute.setName("isOneToOne");
        isOneToOneAttribute.setType(new OJPathName("boolean"));
        isOneToOneAttribute.setInitExp(Boolean.toString(isOneToOne));
        ojEnumLiteral.addToAttributeValues(isOneToOneAttribute);

        OJField isOneToManyAttribute = new OJField();
        isOneToManyAttribute.setName("isOneToMany");
        isOneToManyAttribute.setType(new OJPathName("boolean"));
        isOneToManyAttribute.setInitExp(Boolean.toString(isOneToMany));
        ojEnumLiteral.addToAttributeValues(isOneToManyAttribute);

        OJField isManyToOneAttribute = new OJField();
        isManyToOneAttribute.setName("isManyToOne");
        isManyToOneAttribute.setType(new OJPathName("boolean"));
        isManyToOneAttribute.setInitExp(Boolean.toString(isManyToOne));
        ojEnumLiteral.addToAttributeValues(isManyToOneAttribute);

        OJField isManyToManyAttribute = new OJField();
        isManyToManyAttribute.setName("isManyToMany");
        isManyToManyAttribute.setType(new OJPathName("boolean"));
        isManyToManyAttribute.setInitExp(Boolean.toString(isManyToMany));
        ojEnumLiteral.addToAttributeValues(isManyToManyAttribute);

        OJField upperAttribute = new OJField();
        upperAttribute.setName("upper");
        upperAttribute.setType(new OJPathName("int"));
        upperAttribute.setInitExp(Integer.toString(getUpper));
        ojEnumLiteral.addToAttributeValues(upperAttribute);

        OJField lowerAttribute = new OJField();
        lowerAttribute.setName("lower");
        lowerAttribute.setType(new OJPathName("int"));
        lowerAttribute.setInitExp(Integer.toString(getLower));
        ojEnumLiteral.addToAttributeValues(lowerAttribute);

        OJField inverseUpperAttribute = new OJField();
        inverseUpperAttribute.setName("inverseUpper");
        inverseUpperAttribute.setType(new OJPathName("int"));
        inverseUpperAttribute.setInitExp(Integer.toString(getInverseUpper));
        ojEnumLiteral.addToAttributeValues(inverseUpperAttribute);

        OJField qualifiedAttribute = new OJField();
        qualifiedAttribute.setName("isQualified");
        qualifiedAttribute.setType(new OJPathName("boolean"));
        qualifiedAttribute.setInitExp(Boolean.toString(isQualified));
        ojEnumLiteral.addToAttributeValues(qualifiedAttribute);

        OJField inverseQualifiedAttribute = new OJField();
        inverseQualifiedAttribute.setName("isInverseQualified");
        inverseQualifiedAttribute.setType(new OJPathName("boolean"));
        inverseQualifiedAttribute.setInitExp(Boolean.toString(isInverseQualified));
        ojEnumLiteral.addToAttributeValues(inverseQualifiedAttribute);

        OJField orderedAttribute = new OJField();
        orderedAttribute.setName("isOrdered");
        orderedAttribute.setType(new OJPathName("boolean"));
        orderedAttribute.setInitExp(Boolean.toString(isOrdered));
        ojEnumLiteral.addToAttributeValues(orderedAttribute);

        OJField inverseOrderedAttribute = new OJField();
        inverseOrderedAttribute.setName("isInverseOrdered");
        inverseOrderedAttribute.setType(new OJPathName("boolean"));
        inverseOrderedAttribute.setInitExp(Boolean.toString(isInverseOrdered));
        ojEnumLiteral.addToAttributeValues(inverseOrderedAttribute);

        OJField uniqueAttribute = new OJField();
        uniqueAttribute.setName("isUnique");
        uniqueAttribute.setType(new OJPathName("boolean"));
        uniqueAttribute.setInitExp(Boolean.toString(isUnique));
        ojEnumLiteral.addToAttributeValues(uniqueAttribute);

        OJField inverseUniqueAttribute = new OJField();
        inverseUniqueAttribute.setName("isInverseUnique");
        inverseUniqueAttribute.setType(new OJPathName("boolean"));
        inverseUniqueAttribute.setInitExp(Boolean.toString(isInverseUnique));
        ojEnumLiteral.addToAttributeValues(inverseUniqueAttribute);

        OJField derivedAttribute = new OJField();
        derivedAttribute.setName("isDerived");
        derivedAttribute.setType(new OJPathName("boolean"));
        derivedAttribute.setInitExp(Boolean.toString(isDerived));
        ojEnumLiteral.addToAttributeValues(derivedAttribute);

        OJField navigableAttribute = new OJField();
        navigableAttribute.setName("isNavigable");
        navigableAttribute.setType(new OJPathName("boolean"));
        navigableAttribute.setInitExp(Boolean.toString(isNavigable));
        ojEnumLiteral.addToAttributeValues(navigableAttribute);

        OJField javaClassAttribute = new OJField();
        javaClassAttribute.setName("propertyType");
        javaClassAttribute.setType(new OJPathName("java.lang.Class"));
        javaClassAttribute.setInitExp(javaQualifiedClass + ".class");
        ojEnumLiteral.addToAttributeValues(javaClassAttribute);

        OJField jsonAttribute = new OJField();
        jsonAttribute.setName("json");
        jsonAttribute.setType(new OJPathName("String"));

        StringBuilder sb = new StringBuilder();
        sb.append("{\\\"name\\\": \\\"");
        sb.append(fieldName);

        sb.append("\\\", ");

        sb.append("\\\"associationClassOne\\\": ");
        sb.append(isAssociationClassOneField.getInitExp());
        sb.append(", ");

        sb.append("\\\"memberEndOfAssociationClass\\\": ");
        sb.append(isMemberEndOfAssociationClassField.getInitExp());
        sb.append(", ");

        if (associationClassPropertyName != null) {
            sb.append("\\\"associationClassPropertyName\\\": \\");
            sb.append(propertyAssociationClassPropertyNameField.getInitExp().subSequence(0, propertyAssociationClassPropertyNameField.getInitExp().length() - 1));
            sb.append("\\\", ");
        } else {
            sb.append("\\\"associationClassPropertyName\\\": null, ");
        }

        if (inverseAssociationClassPropertyName != null) {
            sb.append("\\\"inverseAssociationClassPropertyName\\\": \\");
            sb.append(propertyInverseAssociationClassPropertyNameField.getInitExp().subSequence(0, propertyInverseAssociationClassPropertyNameField.getInitExp().length() - 1));
            sb.append("\\\", ");
        } else {
            sb.append("\\\"inverseAssociationClassPropertyName\\\": null, ");
        }

        sb.append("\\\"associationClassProperty\\\": ");
        sb.append(isAssociationClassPropertyField.getInitExp());
        sb.append(", ");

        sb.append("\\\"onePrimitivePropertyOfAssociationClass\\\": ");
        sb.append(isOnePrimitivePropertyOfAssociationClassField.getInitExp());
        sb.append(", ");

        sb.append("\\\"onePrimitive\\\": ");
        sb.append(propertyOnePrimitiveField.getInitExp());
        sb.append(", ");

        sb.append("\\\"readOnly\\\": ");
        sb.append(readOnlyField.getInitExp());
        sb.append(", ");

        if (dataTypeEnum != null) {
            sb.append("\\\"dataTypeEnum\\\": \\\"\" + ");
            sb.append(propertyDataTypeEnumField.getInitExp());
            sb.append(".toString() + \"\\\", ");
        } else {
            sb.append("\\\"dataTypeEnum\\\": null, ");
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

        sb.append("\\\"persistentName\\\": \\");
        sb.append(propertyPersistentNameField.getInitExp().subSequence(0, propertyPersistentNameField.getInitExp().length() - 1));
        sb.append("\\\", ");

        sb.append("\\\"inverseName\\\": \\");
        sb.append(propertyInverseNameField.getInitExp().subSequence(0, propertyInverseNameField.getInitExp().length() - 1));
        sb.append("\\\", ");

        sb.append("\\\"inverseQualifiedName\\\": \\");
        sb.append(propertyInverseQualifiedNameField.getInitExp().subSequence(0, propertyInverseQualifiedNameField.getInitExp().length() - 1));
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
        sb.append("\\\"inverseUpper\\\": ");
        sb.append(inverseUpperAttribute.getInitExp());
        sb.append(", ");
        sb.append("\\\"label\\\": \\");
        sb.append(("\"" + edgeName + "\"").subSequence(0, ("\"" + edgeName + "\"").length() - 1));
        sb.append("\\\", ");
        sb.append("\\\"qualified\\\": ");
        sb.append(qualifiedAttribute.getInitExp());
        sb.append(", ");
        sb.append("\\\"inverseQualified\\\": ");
        sb.append(inverseQualifiedAttribute.getInitExp());
        sb.append(", ");
        sb.append("\\\"ordered\\\": ");
        sb.append(orderedAttribute.getInitExp());
        sb.append(", ");
        sb.append("\\\"inverseOrdered\\\": ");
        sb.append(inverseOrderedAttribute.getInitExp());
        sb.append(", ");
        sb.append("\\\"unique\\\": ");
        sb.append(uniqueAttribute.getInitExp());
        sb.append(", ");
        sb.append("\\\"inverseUnique\\\": ");
        sb.append(inverseUniqueAttribute.getInitExp());
        sb.append(", ");
        sb.append("\\\"derived\\\": ");
        sb.append(derivedAttribute.getInitExp());
        sb.append(", ");
        sb.append("\\\"navigable\\\": ");
        sb.append(navigableAttribute.getInitExp());
        sb.append("}");
        jsonAttribute.setInitExp("\"" + sb.toString() + "\"");
        ojEnumLiteral.addToAttributeValues(jsonAttribute);

        OJField isChangeListenerAttribute = new OJField();
        isChangeListenerAttribute.setName("isChangeListenerAttribute");
        isChangeListenerAttribute.setType(new OJPathName("boolean"));
        isChangeListenerAttribute.setInitExp(String.valueOf(isChangeListener));
        ojEnumLiteral.addToAttributeValues(isChangeListenerAttribute);

        ojEnum.addToLiterals(ojEnumLiteral);
        return ojEnumLiteral;
    }

}
