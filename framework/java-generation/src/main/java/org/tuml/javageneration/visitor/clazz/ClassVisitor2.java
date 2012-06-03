package org.tuml.javageneration.visitor.clazz;

import java.util.List;

import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Property;
import org.opaeum.java.metamodel.OJConstructor;
import org.opaeum.java.metamodel.OJField;
import org.opaeum.java.metamodel.OJIfStatement;
import org.opaeum.java.metamodel.OJPathName;
import org.opaeum.java.metamodel.OJSimpleStatement;
import org.opaeum.java.metamodel.OJSwitchCase;
import org.opaeum.java.metamodel.OJSwitchStatement;
import org.opaeum.java.metamodel.annotation.OJAnnotatedClass;
import org.opaeum.java.metamodel.annotation.OJAnnotatedOperation;
import org.opaeum.java.metamodel.annotation.OJAnnotationValue;
import org.opaeum.java.metamodel.annotation.OJEnum;
import org.opaeum.java.metamodel.annotation.OJEnumLiteral;
import org.tuml.framework.Visitor;
import org.tuml.javageneration.util.TinkerGenerationUtil;
import org.tuml.javageneration.util.TumlClassOperations;
import org.tuml.javageneration.visitor.BaseVisitor;
import org.tuml.javageneration.visitor.property.PropertyWrapper;

public class ClassVisitor2 extends BaseVisitor implements Visitor<Class> {

	@Override
	public void visitBefore(Class clazz) {
		OJAnnotatedClass annotatedClass = findOJClass(clazz);
		setSuperClass(annotatedClass, clazz);
		
		implementCompositionNode(annotatedClass);
		implementIsRoot(annotatedClass, TumlClassOperations.getOtherEndToComposite(clazz) == null);
		addPersistentConstructor(annotatedClass);
		addInitialiseProperties(annotatedClass, clazz);
		addInitialiseProperty(annotatedClass, clazz);
		addContructorWithVertex(annotatedClass, clazz);
		
		if (clazz.getGeneralizations().isEmpty()) {
			persistUid(annotatedClass);
			addGetObjectVersion(annotatedClass);
			addGetSetId(annotatedClass);
			initialiseVertexInPersistentConstructor(annotatedClass, clazz);
		} else {
			addSuperWithPersistenceToDefaultConstructor(annotatedClass);
		}
		addInitialisePropertiesInPersistentConstructor(annotatedClass);
		addTumlRuntimePropertyEnum(annotatedClass, clazz);
	}

	@Override
	public void visitAfter(Class clazz) {
		// TODO Auto-generated method stub

	}

	private void setSuperClass(OJAnnotatedClass annotatedClass, Class clazz) {
		List<Classifier> generals = clazz.getGenerals();
		if (generals.size() > 1) {
			throw new IllegalStateException(String.format("Multiple inheritence is not supported! Class %s has more than on genereralization.", clazz.getName()));
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
		getUid.getBody().addToStatements("String uid = (String) this.vertex.getProperty(\"uid\")");
		OJIfStatement ifStatement = new OJIfStatement("uid==null || uid.trim().length()==0");
		ifStatement.setCondition("uid==null || uid.trim().length()==0");
		ifStatement.addToThenPart("uid=UUID.randomUUID().toString()");
		ifStatement.addToThenPart("this.vertex.setProperty(\"uid\", uid)");
		getUid.getBody().addToStatements(ifStatement);
		getUid.getBody().addToStatements("return uid");
		ojClass.addToImports("java.util.UUID");
		ojClass.addToOperations(getUid);
	}

	protected void addGetObjectVersion(OJAnnotatedClass ojClass) {
		OJAnnotatedOperation getObjectVersion = new OJAnnotatedOperation("getObjectVersion");
		TinkerGenerationUtil.addOverrideAnnotation(getObjectVersion);
		getObjectVersion.setReturnType(new OJPathName("int"));
		getObjectVersion.getBody().addToStatements("return TinkerIdUtilFactory.getIdUtil().getVersion(this.vertex)");
		ojClass.addToOperations(getObjectVersion);
	}

	protected void addGetSetId(OJAnnotatedClass ojClass) {
		OJAnnotatedOperation getId = new OJAnnotatedOperation("getId");
		getId.addAnnotationIfNew(new OJAnnotationValue(new OJPathName("java.lang.Override")));
		getId.setReturnType(new OJPathName("java.lang.Long"));
		getId.getBody().addToStatements("return TinkerIdUtilFactory.getIdUtil().getId(this.vertex)");
		ojClass.addToOperations(getId);

		OJAnnotatedOperation setId = new OJAnnotatedOperation("setId");
		setId.addAnnotationIfNew(new OJAnnotationValue(new OJPathName("java.lang.Override")));
		setId.addParam("id", new OJPathName("java.lang.Long"));
		setId.getBody().addToStatements("TinkerIdUtilFactory.getIdUtil().setId(this.vertex, id)");
		ojClass.addToImports(TinkerGenerationUtil.tinkerIdUtilFactoryPathName);
		ojClass.addToOperations(setId);
	}

	protected void initialiseVertexInPersistentConstructor(OJAnnotatedClass ojClass, Classifier c) {
		OJConstructor constructor = ojClass.findConstructor(new OJPathName("java.lang.Boolean"));
		constructor.getBody().addToStatements("this.vertex = " + TinkerGenerationUtil.graphDbAccess + ".addVertex(\"dribble\")");
		constructor.getBody().addToStatements("TransactionThreadEntityVar.setNewEntity(this)");
		constructor.getBody().addToStatements("defaultCreate()");
		ojClass.addToImports(TinkerGenerationUtil.transactionThreadEntityVar);
		ojClass.addToImports(TinkerGenerationUtil.graphDbPathName);
	}

	private void addSuperWithPersistenceToDefaultConstructor(OJAnnotatedClass ojClass) {
		OJConstructor constructor = ojClass.findConstructor(new OJPathName("java.lang.Boolean"));
		constructor.getBody().getStatements().add(0, new OJSimpleStatement("super( " + TinkerGenerationUtil.PERSISTENT_CONSTRUCTOR_PARAM_NAME + " )"));
	}

	private void implementCompositionNode(OJAnnotatedClass ojClass) {
		ojClass.addToImplementedInterfaces(TinkerGenerationUtil.tinkerCompositionNodePathName);
	}

	protected void addPersistentConstructor(OJAnnotatedClass ojClass) {
		OJConstructor persistentConstructor = new OJConstructor();
		persistentConstructor.setName(TinkerGenerationUtil.PERSISTENT_CONSTRUCTOR_NAME);
		persistentConstructor.addParam(TinkerGenerationUtil.PERSISTENT_CONSTRUCTOR_PARAM_NAME, new OJPathName("java.lang.Boolean"));
		ojClass.addToConstructors(persistentConstructor);
	}

	private void addInitialiseProperties(OJAnnotatedClass annotatedClass, Class clazz) {
		OJAnnotatedOperation initialiseProperties = new OJAnnotatedOperation("initialiseProperties");
		TinkerGenerationUtil.addOverrideAnnotation(initialiseProperties);
		if (!clazz.getGeneralizations().isEmpty()) {
			initialiseProperties.getBody().addToStatements("super.initialiseProperties()");
		}
		annotatedClass.addToOperations(initialiseProperties);
		for (Property p :  TumlClassOperations.getAllOwnedProperties(clazz)) {
			PropertyWrapper pWrap = new PropertyWrapper(p);
			if (!(pWrap.isDerived() || pWrap.isDerivedUnion())) {
				OJSimpleStatement statement = new OJSimpleStatement("this." + pWrap.fieldname() + " = " + pWrap.javaDefaultInitialisation());
				statement.setName(pWrap.fieldname());
				initialiseProperties.getBody().addToStatements(statement);
				annotatedClass.addToImports(pWrap.javaImplTypePath());
			}
		}
	}

	private void addInitialiseProperty(OJAnnotatedClass annotatedClass, Class clazz) {
		OJAnnotatedOperation initialiseProperty = new OJAnnotatedOperation("initialiseProperty");
		TinkerGenerationUtil.addOverrideAnnotation(initialiseProperty);
		initialiseProperty.addParam("tumlRuntimeProperty", TinkerGenerationUtil.tumlRuntimePropertyPathName.getCopy());
		if (!clazz.getGeneralizations().isEmpty()) {
			initialiseProperty.getBody().addToStatements("super.initialiseProperties()");
		}
		annotatedClass.addToOperations(initialiseProperty);

		OJSwitchStatement ojSwitchStatement = new OJSwitchStatement();
		ojSwitchStatement.setCondition("(" + TumlClassOperations.propertyEnumName(clazz) + ".fromLabel(tumlRuntimeProperty.getLabel()))");
		initialiseProperty.getBody().addToStatements(ojSwitchStatement);
		
		for (Property p :  TumlClassOperations.getAllOwnedProperties(clazz)) {
			PropertyWrapper pWrap = new PropertyWrapper(p);
			if (!(pWrap.isDerived() || pWrap.isDerivedUnion())) {
				OJSwitchCase ojSwitchCase = new OJSwitchCase();
				ojSwitchCase.setLabel(pWrap.fieldname().toUpperCase());
				OJSimpleStatement statement = new OJSimpleStatement("this." + pWrap.fieldname() + " = " + pWrap.javaDefaultInitialisation());
				statement.setName(pWrap.fieldname());
				ojSwitchCase.getBody().addToStatements(statement);
				annotatedClass.addToImports(pWrap.javaImplTypePath());
				ojSwitchStatement.addToCases(ojSwitchCase);
			}
		}
	}

	
	protected void addContructorWithVertex(OJAnnotatedClass ojClass, Class clazz) {
		OJConstructor constructor = new OJConstructor();
		constructor.addParam("vertex", new OJPathName("com.tinkerpop.blueprints.pgm.Vertex"));
		if (clazz.getGeneralizations().isEmpty()) {
			constructor.getBody().addToStatements("this.vertex=vertex");
		} else {
			constructor.getBody().addToStatements("super(vertex)");
		}
		constructor.getBody().addToStatements("initialiseProperties()");
		ojClass.addToConstructors(constructor);
	}

	protected void implementIsRoot(OJAnnotatedClass ojClass, boolean b) {
		OJAnnotatedOperation isRoot = new OJAnnotatedOperation("isTinkerRoot");
		isRoot.addAnnotationIfNew(new OJAnnotationValue(new OJPathName("java.lang.Override")));
		isRoot.setReturnType(new OJPathName("boolean"));
		isRoot.getBody().addToStatements("return " + b);
		ojClass.addToOperations(isRoot);
	}

	private void addInitialisePropertiesInPersistentConstructor(OJAnnotatedClass annotatedClass) {
		OJConstructor constructor = annotatedClass.findConstructor(new OJPathName("java.lang.Boolean"));
		constructor.getBody().addToStatements("initialiseProperties()");
	}

	private void addTumlRuntimePropertyEnum(OJAnnotatedClass annotatedClass, Class clazz) {
		OJEnum ojEnum = new OJEnum(TumlClassOperations.propertyEnumName(clazz));
		ojEnum.addToImplementedInterfaces(TinkerGenerationUtil.tumlRuntimePropertyPathName.getCopy());
		annotatedClass.addInnerEnum(ojEnum);

		OJField inverseField = new OJField();
		inverseField.setType(new OJPathName("boolean"));
		inverseField.setName("controllingSide");
		ojEnum.addToFields(inverseField);

		OJField compositeField = new OJField();
		compositeField.setType(new OJPathName("boolean"));
		compositeField.setName("composite");
		ojEnum.addToFields(compositeField);

		OJField labelField = new OJField();
		labelField.setType(new OJPathName("String"));
		labelField.setName("label");
		ojEnum.addToFields(labelField);
		
		OJField isOneToOneField = new OJField();
		isOneToOneField.setType(new OJPathName("boolean"));
		isOneToOneField.setName("oneToOne");
		ojEnum.addToFields(isOneToOneField);

		OJField isOneToManyField = new OJField();
		isOneToManyField.setType(new OJPathName("boolean"));
		isOneToManyField.setName("oneToMany");
		ojEnum.addToFields(isOneToManyField);

		OJField isManyToOneField = new OJField();
		isManyToOneField.setType(new OJPathName("boolean"));
		isManyToOneField.setName("manyToOne");
		ojEnum.addToFields(isManyToOneField);

		OJField isManyToManyField = new OJField();
		isManyToManyField.setType(new OJPathName("boolean"));
		isManyToManyField.setName("manyToMany");
		ojEnum.addToFields(isManyToManyField);

		OJField upperField = new OJField();
		upperField.setType(new OJPathName("int"));
		upperField.setName("upper");
		ojEnum.addToFields(upperField);

		OJField lowerField = new OJField();
		lowerField.setType(new OJPathName("int"));
		lowerField.setName("lower");
		ojEnum.addToFields(lowerField);
		
		ojEnum.implementGetter();
		ojEnum.createConstructorFromFields();
		
		OJAnnotatedOperation fromLabel = new OJAnnotatedOperation("fromLabel", new OJPathName(TumlClassOperations.propertyEnumName(clazz)));
		fromLabel.addParam("label", new OJPathName("String"));
		fromLabel.setStatic(true);
		ojEnum.addToOperations(fromLabel);
		
		for (Property p :  TumlClassOperations.getAllOwnedProperties(clazz)) {
			PropertyWrapper pWrap = new PropertyWrapper(p);
			if (!(pWrap.isDerived() || pWrap.isDerivedUnion())) {

				OJIfStatement ifLabelEquals = new OJIfStatement(pWrap.fieldname().toUpperCase() + ".getLabel().equals(label)");
				ifLabelEquals.addToThenPart("return " + pWrap.fieldname().toUpperCase());
				fromLabel.getBody().addToStatements(ifLabelEquals);
				
				OJEnumLiteral ojLiteral = new OJEnumLiteral(pWrap.fieldname().toUpperCase());

				OJField propertyControllingSideField = new OJField();
				propertyControllingSideField.setType(new OJPathName("boolean"));
				propertyControllingSideField.setInitExp(Boolean.toString(pWrap.isControllingSide()));
				ojLiteral.addToAttributeValues(propertyControllingSideField);

				OJField compositeLabelField = new OJField();
				compositeLabelField.setType(new OJPathName("boolean"));
				compositeLabelField.setInitExp(Boolean.toString(pWrap.isComposite()));
				ojLiteral.addToAttributeValues(compositeLabelField);
				
				OJField propertyLabelField = new OJField();
				propertyLabelField.setType(new OJPathName("String"));
				propertyLabelField.setInitExp("\""+ TinkerGenerationUtil.getEdgeName(p) +"\"");
				ojLiteral.addToAttributeValues(propertyLabelField);
				
				OJField isOneToManyAttribute = new OJField();
				isOneToManyAttribute.setType(new OJPathName("boolean"));
				isOneToManyAttribute.setInitExp(Boolean.toString(pWrap.isOneToMany()));
				ojLiteral.addToAttributeValues(isOneToManyAttribute);

				OJField isManyToOneAttribute = new OJField();
				isManyToOneAttribute.setType(new OJPathName("boolean"));
				isManyToOneAttribute.setInitExp(Boolean.toString(pWrap.isManyToOne()));
				ojLiteral.addToAttributeValues(isManyToOneAttribute);

				OJField isOneToOneAttribute = new OJField();
				isOneToOneAttribute.setType(new OJPathName("boolean"));
				isOneToOneAttribute.setInitExp(Boolean.toString(pWrap.isOneToOne()));
				ojLiteral.addToAttributeValues(isOneToOneAttribute);

				OJField isManyToManyAttribute = new OJField();
				isManyToManyAttribute.setType(new OJPathName("boolean"));
				isManyToManyAttribute.setInitExp(Boolean.toString(pWrap.isManyToMany()));
				ojLiteral.addToAttributeValues(isManyToManyAttribute);

				OJField upperAttribute = new OJField();
				upperAttribute.setType(new OJPathName("int"));
				upperAttribute.setInitExp(Integer.toString(pWrap.getUpper()));
				ojLiteral.addToAttributeValues(upperAttribute);

				OJField lowerAttribute = new OJField();
				lowerAttribute.setType(new OJPathName("int"));
				lowerAttribute.setInitExp(Integer.toString(pWrap.getLower()));
				ojLiteral.addToAttributeValues(lowerAttribute);

				ojEnum.addToLiterals(ojLiteral);
			}
		}
		fromLabel.getBody().addToStatements("throw new IllegalStateException()");
	}

}