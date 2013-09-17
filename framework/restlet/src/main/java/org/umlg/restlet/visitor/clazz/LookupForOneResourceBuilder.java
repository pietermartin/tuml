package org.umlg.restlet.visitor.clazz;

import org.eclipse.uml2.uml.Property;
import org.umlg.java.metamodel.*;
import org.umlg.java.metamodel.annotation.OJAnnotatedClass;
import org.umlg.java.metamodel.annotation.OJAnnotatedInterface;
import org.umlg.java.metamodel.annotation.OJAnnotatedOperation;
import org.umlg.java.metamodel.annotation.OJAnnotationValue;
import org.umlg.java.metamodel.annotation.OJEnum;
import org.umlg.java.metamodel.annotation.OJEnumLiteral;
import org.umlg.framework.Visitor;
import org.umlg.generation.Workspace;
import org.umlg.javageneration.util.PropertyWrapper;
import org.umlg.javageneration.util.TinkerGenerationUtil;
import org.umlg.javageneration.util.TumlClassOperations;
import org.umlg.restlet.util.TumlRestletGenerationUtil;

public class LookupForOneResourceBuilder extends BaseServerResourceBuilder implements Visitor<Property> {

    public LookupForOneResourceBuilder(Workspace workspace, String sourceDir) {
        super(workspace, sourceDir);
    }

    @Override
    public void visitBefore(Property p) {
        PropertyWrapper pWrap = new PropertyWrapper(p);
        if (pWrap.hasLookup() && pWrap.isOne()) {

            OJAnnotatedClass owner = findOJClass(p);

//            OJAnnotatedInterface annotatedInf = new OJAnnotatedInterface(TumlClassOperations.getPathName(pWrap.getOwningType()).getLast() + "_"
//                    + pWrap.getOtherEnd().getName() + "_" + pWrap.getName() + "_lookUpForOne" + "_ServerResource");
            OJPackage ojPackage = new OJPackage(owner.getMyPackage().toString() + ".restlet");
//            annotatedInf.setMyPackage(ojPackage);
//            addToSource(annotatedInf);

            OJAnnotatedClass annotatedClass = new OJAnnotatedClass(TumlClassOperations.getPathName(pWrap.getOwningType()).getLast() + "_"
                    + pWrap.getOtherEnd().getName() + "_" + pWrap.getName() + "_lookUpForOne" + "_ServerResourceImpl");
            annotatedClass.setSuperclass(TumlRestletGenerationUtil.ServerResource);
//            annotatedClass.addToImplementedInterfaces(annotatedInf.getPathName());
            annotatedClass.setMyPackage(ojPackage);
            addToSource(annotatedClass);
            addDefaultConstructor(annotatedClass);

            addCompositeParentIdField(pWrap, annotatedClass);
            addGetObjectRepresentation(pWrap/*, annotatedInf*/, annotatedClass);
            addServerResourceToRouterEnum(pWrap, annotatedClass);
        }
    }

    @Override
    public void visitAfter(Property p) {
    }

    private void addCompositeParentIdField(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass) {
        OJField compositeParentFieldId = new OJField(TumlClassOperations.getPathName(pWrap.getOtherEnd().getType()).getLast().toLowerCase() + "Id",
                new OJPathName("Object"));
        compositeParentFieldId.setVisibility(OJVisibilityKind.PRIVATE);
        annotatedClass.addToFields(compositeParentFieldId);
    }

    private void addGetObjectRepresentation(PropertyWrapper pWrap/*, OJAnnotatedInterface annotatedInf*/, OJAnnotatedClass annotatedClass) {

//        OJAnnotatedOperation getInf = new OJAnnotatedOperation("get", TumlRestletGenerationUtil.Representation);
//        annotatedInf.addToOperations(getInf);
//        getInf.addAnnotationIfNew(new OJAnnotationValue(TumlRestletGenerationUtil.Get, "json"));

        OJAnnotatedOperation get = new OJAnnotatedOperation("get", TumlRestletGenerationUtil.Representation);
        get.addToThrows(TumlRestletGenerationUtil.ResourceException);
        annotatedClass.addToImports(TumlRestletGenerationUtil.ResourceException);
        TinkerGenerationUtil.addOverrideAnnotation(get);

        OJTryStatement ojTryStatement = new OJTryStatement();

        OJPathName parentPathName = TumlClassOperations.getPathName(pWrap.getOtherEnd().getType());
        ojTryStatement.getTryPart().addToStatements(
                "this." + parentPathName.getLast().toLowerCase() + "Id = getRequestAttributes().get(\""
                        + parentPathName.getLast().toLowerCase() + "Id\")");
        ojTryStatement.getTryPart().addToStatements(
                parentPathName.getLast() + " resource = GraphDb.getDb().instantiateClassifier(this." + parentPathName.getLast().toLowerCase() + "Id"
                        + ")");
        annotatedClass.addToImports(parentPathName);
        buildToJson(pWrap, annotatedClass, ojTryStatement.getTryPart());
        ojTryStatement.setCatchPart(null);

        ojTryStatement.getFinallyPart().addToStatements(TinkerGenerationUtil.graphDbAccess + ".rollback()");
        get.getBody().addToStatements(ojTryStatement);

        annotatedClass.addToImports(TinkerGenerationUtil.graphDbPathName);
        annotatedClass.addToImports(TumlRestletGenerationUtil.JsonRepresentation);
        annotatedClass.addToOperations(get);
    }

    private void buildToJson(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass, OJBlock block) {
        block.addToStatements("StringBuilder json = new StringBuilder()");
        block.addToStatements("json.append(\"{\\\"data\\\": [\")");
        block.addToStatements("json.append(" + TumlRestletGenerationUtil.TumlRestletToJsonUtil.getLast() + ".toJson(resource." + pWrap.lookup() + "()))");
        annotatedClass.addToImports(TumlRestletGenerationUtil.TumlRestletToJsonUtil);
        block.addToStatements("json.append(\"],\")");
        block.addToStatements("json.append(\" \\\"meta\\\" : {\")");
        block.addToStatements("json.append(\"\\\"qualifiedName\\\": \\\"" + pWrap.getQualifiedName() + "\\\"\")");
        block.addToStatements("json.append(\", \\\"to\\\": \")");
        block.addToStatements("json.append(" + TumlClassOperations.propertyEnumName(new PropertyWrapper(pWrap.getOtherEnd()).getOwningType()) + ".asJson())");
        annotatedClass.addToImports(TumlClassOperations.getPathName(new PropertyWrapper(pWrap.getOtherEnd()).getOwningType()).append(
                TumlClassOperations.propertyEnumName(new PropertyWrapper(pWrap.getOtherEnd()).getOwningType())));
        block.addToStatements("json.append(\", \\\"from\\\": \")");
        block.addToStatements("json.append(" + TumlClassOperations.propertyEnumName(pWrap.getOwningType()) + ".asJson())");
        annotatedClass.addToImports(TumlClassOperations.getPathName(pWrap.getOwningType()).append(TumlClassOperations.propertyEnumName(pWrap.getOwningType())));

        block.addToStatements("json.append(\"}}\")");
        block.addToStatements("return new " + TumlRestletGenerationUtil.JsonRepresentation.getLast() + "(json.toString())");
    }

    private void addServerResourceToRouterEnum(PropertyWrapper pWrap, OJAnnotatedClass annotatedClass) {
        OJEnum routerEnum = (OJEnum) this.workspace.findOJClass("restlet.RestletRouterEnum");
        OJEnumLiteral ojLiteral = new OJEnumLiteral(TumlClassOperations.getPathName(pWrap.getOwningType()).getLast().toUpperCase() + "_" + pWrap.lookup());

        OJField uri = new OJField();
        uri.setType(new OJPathName("String"));
        uri.setInitExp("\"/" + TumlClassOperations.getPathName(pWrap.getOwningType()).getLast().toLowerCase() + "s/{"
                + TumlClassOperations.getPathName(pWrap.getOwningType()).getLast().toLowerCase() + "Id}/" + pWrap.lookup() + "\"");
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

}
