package org.tuml.restlet.visitor.clazz;

import org.apache.commons.lang.StringUtils;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.tuml.java.metamodel.OJBlock;
import org.tuml.java.metamodel.OJField;
import org.tuml.java.metamodel.OJPackage;
import org.tuml.java.metamodel.OJPathName;
import org.tuml.java.metamodel.OJVisibilityKind;
import org.tuml.java.metamodel.annotation.OJAnnotatedClass;
import org.tuml.java.metamodel.annotation.OJAnnotatedInterface;
import org.tuml.java.metamodel.annotation.OJAnnotatedOperation;
import org.tuml.java.metamodel.annotation.OJAnnotationValue;
import org.tuml.java.metamodel.annotation.OJEnum;
import org.tuml.java.metamodel.annotation.OJEnumLiteral;
import org.tuml.framework.Visitor;
import org.tuml.generation.Workspace;
import org.tuml.javageneration.util.PropertyWrapper;
import org.tuml.javageneration.util.TinkerGenerationUtil;
import org.tuml.javageneration.util.TumlClassOperations;
import org.tuml.restlet.util.TumlRestletGenerationUtil;

public class LookupOnCompositeParentCompositeParentResourceBuilder extends BaseServerResourceBuilder implements Visitor<Property> {

	public LookupOnCompositeParentCompositeParentResourceBuilder(Workspace workspace, String sourceDir) {
		super(workspace, sourceDir);
        if (true) {
            throw new RuntimeException("deprecated");
        }
	}

	@Override
	public void visitBefore(Property p) {
        PropertyWrapper pWrap = new PropertyWrapper(p);
		if (pWrap.hasLookup()) {

			OJAnnotatedClass owner = findOJClass(p);
			OJAnnotatedInterface annotatedInf;
			OJAnnotatedClass annotatedClass;
			Classifier owningType = (Classifier) pWrap.getOwningType();
			if (TumlClassOperations.hasCompositeOwner(owningType)) {
				Type compositeParentType = TumlClassOperations.getOtherEndToComposite(owningType).getType();
				annotatedInf = new OJAnnotatedInterface(TumlClassOperations.getPathName(compositeParentType).getLast() + "_" + pWrap.getOtherEnd().getName()
						+ "_lookupCompositeParent" + StringUtils.capitalize(pWrap.getName()) + "_ServerResource");
				annotatedClass = new OJAnnotatedClass(TumlClassOperations.getPathName(compositeParentType).getLast() + "_" + pWrap.getOtherEnd().getName()
						+ "_lookupCompositeParent" + StringUtils.capitalize(pWrap.getName()) + "_ServerResourceImpl");
			} else {
				annotatedInf = new OJAnnotatedInterface("Root_" + pWrap.getOtherEnd().getName() + "_lookupCompositeParent"
						+ StringUtils.capitalize(pWrap.getName()) + "_ServerResource");
				annotatedClass = new OJAnnotatedClass("Root_" + pWrap.getOtherEnd().getName() + "_lookupCompositeParent"
						+ StringUtils.capitalize(pWrap.getName()) + "_ServerResourceImpl");
			}
			OJPackage ojPackage = new OJPackage(owner.getMyPackage().toString() + ".restlet");
			annotatedInf.setMyPackage(ojPackage);
			addToSource(annotatedInf);

			annotatedClass.setSuperclass(TumlRestletGenerationUtil.ServerResource);
			annotatedClass.addToImplementedInterfaces(annotatedInf.getPathName());
			annotatedClass.setMyPackage(ojPackage);
			addToSource(annotatedClass);
			addDefaultConstructor(annotatedClass);

			if (TumlClassOperations.hasCompositeOwner(owningType)) {
				addCompositeParentIdField(pWrap, annotatedClass);
				addGetObjectRepresentation(pWrap, annotatedInf, annotatedClass);
				addServerResourceToRouterEnum(pWrap, annotatedClass);
			} else {
				addGetObjectRepresentationFromRoot(pWrap, annotatedInf, annotatedClass);
				addServerResourceToRouterEnumFromRoot(pWrap, annotatedClass);
			}
		}
	}

	@Override
	public void visitAfter(Property p) {
	}

	private void addCompositeParentIdField(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass) {
		Type compositeParentType = TumlClassOperations.getOtherEndToComposite((Class) pWrap.getOwningType()).getType();
		OJField compositeParentFieldId = new OJField(TumlClassOperations.getPathName(compositeParentType).getLast().toLowerCase() + "Id", new OJPathName("int"));
		compositeParentFieldId.setVisibility(OJVisibilityKind.PRIVATE);
		annotatedClass.addToFields(compositeParentFieldId);
	}

	private void addGetObjectRepresentation(PropertyWrapper pWrap, OJAnnotatedInterface annotatedInf, OJAnnotatedClass annotatedClass) {

		OJAnnotatedOperation getInf = new OJAnnotatedOperation("get", TumlRestletGenerationUtil.Representation);
		annotatedInf.addToOperations(getInf);
		getInf.addAnnotationIfNew(new OJAnnotationValue(TumlRestletGenerationUtil.Get, "json"));

		OJAnnotatedOperation get = new OJAnnotatedOperation("get", TumlRestletGenerationUtil.Representation);
		get.addToThrows(TumlRestletGenerationUtil.ResourceException);
		annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
		TinkerGenerationUtil.addOverrideAnnotation(get);

		Type compositeParentType = TumlClassOperations.getOtherEndToComposite((Class) pWrap.getOwningType()).getType();
		OJPathName parentPathName = TumlClassOperations.getPathName(compositeParentType);
		get.getBody().addToStatements(
				"this." + parentPathName.getLast().toLowerCase() + "Id = Integer.parseInt((String)getRequestAttributes().get(\""
						+ parentPathName.getLast().toLowerCase() + "Id\"))");
		get.getBody().addToStatements(
				parentPathName.getLast() + " resource = GraphDb.getDb().instantiateClassifier(Long.valueOf(this." + parentPathName.getLast().toLowerCase() + "Id" + "))");
		annotatedClass.addToImports(parentPathName);
		buildToJson(pWrap, annotatedClass, get.getBody());
		annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
		annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
		annotatedClass.addToOperations(get);
	}

	private void buildToJson(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass, OJBlock block) {
		block.addToStatements("StringBuilder json = new StringBuilder()");
		block.addToStatements("json.append(\"{\\\"data\\\": \")");
		block.addToStatements("json.append(ToJsonUtil.toJson(resource." + pWrap.lookupCompositeParent() + "()))");
		annotatedClass.addToImports(TinkerGenerationUtil.ToJsonUtil);
		block.addToStatements("json.append(\",\")");
		block.addToStatements("json.append(\" \\\"meta\\\" : [\")");
		block.addToStatements("json.append(" + TumlClassOperations.propertyEnumName(pWrap.getOwningType()) + ".asJson())");
		annotatedClass.addToImports(TumlClassOperations.getPathName(pWrap.getOwningType()).append(TumlClassOperations.propertyEnumName(pWrap.getOwningType())));
		block.addToStatements("json.append(\", \")");
		block.addToStatements("json.append(" + TumlClassOperations.propertyEnumName(new PropertyWrapper(pWrap.getOtherEnd()).getOwningType()) + ".asJson())");
		annotatedClass.addToImports(TumlClassOperations.getPathName(new PropertyWrapper(pWrap.getOtherEnd()).getOwningType()).append(
				TumlClassOperations.propertyEnumName(new PropertyWrapper(pWrap.getOtherEnd()).getOwningType())));
		block.addToStatements("json.append(\"]}\")");
		block.addToStatements("return new " + TumlRestletGenerationUtil.JsonRepresentation.getLast() + "(json.toString())");
	}

	private void addServerResourceToRouterEnum(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass) {
		Type compositeParentType = TumlClassOperations.getOtherEndToComposite((Class) pWrap.getOwningType()).getType();
		OJEnum routerEnum = (OJEnum) this.workspace.findOJClass("restlet.RestletRouterEnum");
		OJEnumLiteral ojLiteral = new OJEnumLiteral(TumlClassOperations.getPathName(compositeParentType).getLast().toUpperCase() + "_"
				+ pWrap.lookupCompositeParent());

		OJField uri = new OJField();
		uri.setType(new OJPathName("String"));
		uri.setInitExp("\"/" + TumlClassOperations.getPathName(compositeParentType).getLast().toLowerCase() + "s/{"
				+ TumlClassOperations.getPathName(compositeParentType).getLast().toLowerCase() + "Id}/" + pWrap.lookupCompositeParent() + "\"");
		ojLiteral.addToAttributeValues(uri);

		OJField serverResourceClassField = new OJField();
		serverResourceClassField.setType(new OJPathName("java.lang.Class"));
		serverResourceClassField.setInitExp(annotatedClass.getName() + ".class");
		ojLiteral.addToAttributeValues(serverResourceClassField);
		routerEnum.addToImports(annotatedClass.getPathName());
		routerEnum.addToImports(TumlRestletGenerationUtil.ServerResource);

		routerEnum.addToLiterals(ojLiteral);

		OJAnnotatedOperation attachAll = routerEnum.findOperation("attachAll", TumlRestletGenerationUtil.Router);
		attachAll.getBody().addToStatements(routerEnum.getName() + "." + ojLiteral.getName() + ".attach(router)");
	}

	private void addServerResourceToRouterEnumFromRoot(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass) {
		// TODO Auto-generated method stub
		
	}

	private void addGetObjectRepresentationFromRoot(PropertyWrapper pWrap, OJAnnotatedInterface annotatedInf, OJAnnotatedClass annotatedClass) {
		// TODO Auto-generated method stub
		
	}

}
