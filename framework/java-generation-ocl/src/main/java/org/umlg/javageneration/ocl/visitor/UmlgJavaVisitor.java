package org.umlg.javageneration.ocl.visitor;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.expressions.*;
import org.eclipse.ocl.expressions.Variable;
import org.eclipse.ocl.types.CollectionType;
import org.eclipse.ocl.types.VoidType;
import org.eclipse.ocl.uml.impl.CollectionItemImpl;
import org.eclipse.ocl.uml.impl.CollectionTypeImpl;
import org.eclipse.ocl.util.TypeUtil;
import org.eclipse.ocl.utilities.*;
import org.eclipse.ocl.utilities.TypedElement;
import org.eclipse.uml2.uml.*;
import org.eclipse.uml2.uml.internal.operations.MultiplicityElementOperations;
import org.umlg.java.metamodel.OJPathName;
import org.umlg.java.metamodel.annotation.OJAnnotatedClass;
import org.umlg.javageneration.ocl.visitor.tojava.*;
import org.umlg.javageneration.util.PropertyWrapper;
import org.umlg.javageneration.util.UmlgClassOperations;
import org.umlg.javageneration.util.UmlgCollectionKindEnum;
import org.umlg.javageneration.util.UmlgGenerationUtil;

import java.util.Iterator;
import java.util.List;

public class UmlgJavaVisitor extends
        AbstractVisitor<String, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint> {

    private OJAnnotatedClass ojClass;
    private NamedElement element;
    private final Environment<?, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint, ?, ?> env;
    private final UMLReflection<?, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint> uml;

    /**
     * Indicates where a required element in the AST was <code>null</code>, so
     * that it is evident in the debugger that something was missing. We don't
     * want just <code>"null"</code> because that would look like the OclVoid
     * literal.
     */
    protected static String NULL_PLACEHOLDER = "\"<null>\""; //$NON-NLS-1$

    /**
     * Initializes me with my environment.
     *
     * @param env my environment
     */
    protected UmlgJavaVisitor(OJAnnotatedClass ojClass, NamedElement element,
                              Environment<?, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint, ?, ?> env) {
        this.ojClass = ojClass;
        this.element = element;
        this.env = env;
        this.uml = (env == null) ? null : env.getUMLReflection();
    }

    /**
     * Obtains an instance of the <tt>toString()</tt> visitor for the specified
     * environment.
     *
     * @param env an OCL environment
     * @return the corresponding instance
     */
    public static UmlgJavaVisitor getInstance(OJAnnotatedClass ojClass, NamedElement element,
                                              Environment<?, Classifier, Operation, Property, EnumerationLiteral, Parameter, State, CallOperationAction, SendSignalAction, Constraint, ?, ?> env) {

        return new UmlgJavaVisitor(ojClass, element, env);
    }

    /**
     * Obtains an instance of the <tt>toString()</tt> visitor for the specified
     * expression or other typed element.
     *
     * @param element an OCL expression or other typed element such as a variable
     * @return the corresponding instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static UmlgJavaVisitor getInstance(OJAnnotatedClass ojClass, NamedElement namedElement, TypedElement<Classifier> element) {
        return new UmlgJavaVisitor(ojClass, namedElement, (Environment) Environment.Registry.INSTANCE.getEnvironmentFor(element));
    }

    /**
     * Null-safe access to the name of a named element.
     *
     * @param named a named element or <code>null</code>
     * @return a name, or the null placeholder if the named element or its name
     * be <code>null</code>. i.e., <code>null</code> is never returned
     */
    protected String getName(Object named) {
        String tmp = (uml == null) ? NULL_PLACEHOLDER : uml.getName(named);
        if (tmp.equals("=")) {
            tmp = "equals";
        } else if (tmp.equals("<>")) {
            tmp = "equals";
        }
        return tmp;
    }

    /**
     * Null-safe access to the qualified name of a named element.
     *
     * @param named a named element or <code>null</code>
     * @return a qualified name, or the null placeholder if the named element or
     * its name be <code>null</code>. i.e., <code>null</code> is never
     * returned
     */
    protected String getQualifiedName(Object named) {
        return (uml == null) ? NULL_PLACEHOLDER : uml.getQualifiedName(named);
    }

    /**
     * Callback for an OperationCallExp visit.
     * <p/>
     * Look at the source to determine operator ( -> or . )
     *
     * @param oc the operation call expression
     * @return string
     */
    @Override
    protected String handleOperationCallExp(OperationCallExp<Classifier, Operation> oc, String sourceResult, List<String> argumentResults) {
        String name = oc.getReferredOperation().getName();
        return OclOperationExpEnum.from(name).setOJClass(this.ojClass).handleOperationExp(oc, sourceResult, argumentResults);
    }

    /**
     * Callback for an EnumLiteralExp visit.
     *
     * @param el the enumeration literal expresion
     * @return the enumeration literal toString()
     */
    @Override
    public String visitEnumLiteralExp(EnumLiteralExp<Classifier, EnumerationLiteral> el) {
        EnumerationLiteral l = el.getReferredEnumLiteral();
        this.ojClass.addToImports(UmlgClassOperations.getPathName(l.getClassifier()));
        return UmlgClassOperations.getPathName(l.getClassifier()).getLast() + "." + l.getName();
    }

    /**
     * Callback for a VariableExp visit.
     *
     * @param v the variable expression
     * @return the variable name
     */
    @Override
    public String visitVariableExp(VariableExp<Classifier, Parameter> v) {
        Variable<Classifier, Parameter> vd = v.getReferredVariable();
        String result = (vd == null) ? null : vd.getName();

        if (result == null) {
            result = NULL_PLACEHOLDER;
        }

        return result;
    }

    /**
     * Callback for an AssociationEndCallExp visit.
     *
     * @param pc the property call expression
     * @return string source.ref
     */
    @Override
    protected String handlePropertyCallExp(PropertyCallExp<Classifier, Property> pc, String sourceResult, List<String> qualifierResults) {

        Property property = pc.getReferredProperty();
        PropertyWrapper pWrap = new PropertyWrapper(property);

        boolean addCollect = false;
        //check if the source property is a qualified property
        //if so and if the its multiplicity is 1 then the raw multiplicity is actually *
        //therefore treat the current property expression as being called on a many property, i.e. as a collect statement
        OCLExpression oclExpression = pc.getSource();
        if (qualifierResults.isEmpty() && oclExpression instanceof PropertyCallExp) {
            PropertyCallExp<?, Property> propertyPropertyCallExp = (PropertyCallExp<?, Property>) oclExpression;
            //if the source property is qualified but the oocl expression itself has qualifier then the properties multiplicity will be correct
            //i.e. no need for a collect statement
            if (propertyPropertyCallExp.getQualifier().isEmpty()) {
                Property sourceProperty = propertyPropertyCallExp.getReferredProperty();
                PropertyWrapper sourcePropertyWrapper = PropertyWrapper.from(sourceProperty);
                addCollect = sourcePropertyWrapper.isQualified() && sourcePropertyWrapper.isMany();
            }
        }


        //Validate property is navigable
        if (!pWrap.isNavigable()) {
            throw new IllegalStateException(String.format("Property %s is not navigable! It can not be navigated in OCL from element %s", new String[]{pWrap.getQualifiedName(), this.element.getQualifiedName()}));
        }

        //Bug in ocl parser. It uses the inc
        String getter;
        if (!qualifierResults.isEmpty()) {
            getter = pWrap.getQualifiedNameFor(pWrap.getQualifiersAsPropertyWrappers());
            getter += "(";
            int count = 1;
            for (String qualifierValue : qualifierResults) {
                getter += "Pair.of(Compare.eq, " + qualifierValue + ")";
                if (count++ < qualifierResults.size()) {
                    getter += ", ";
                }
            }
            getter += ")";
            this.ojClass.addToImports(UmlgGenerationUtil.token);
            this.ojClass.addToImports(UmlgGenerationUtil.Pair);
        } else {
            getter = pWrap.getter();
            getter += "()";
        }
        StringBuilder result = new StringBuilder();
        if (sourceResult != null && !sourceResult.equals("self")) {
            result.append(sourceResult);
            if (addCollect) {
                result.append(".<");
                result.append(pWrap.javaBaseTypePath().getLast());
                result.append(", ");
                result.append(pWrap.javaTypePath().getLast());
                result.append(">collect(a -> a");
            }
            result.append(".");
        }
        result.append(getter);
        if (addCollect) {
            result.append(")");
        }
        return result.toString();
    }

    /**
     * Callback for an AssociationClassCallExp visit.
     *
     * @param ac the association class expression
     * @return string source.ref
     */
    @Override
    protected String handleAssociationClassCallExp(AssociationClassCallExp<Classifier, Property> ac, String sourceResult, List<String> qualifierResults) {

        Classifier ref = ac.getReferredAssociationClass();
//        String name = initialLower(getName(ref));

        //This is a bit silly but its the same way the PropertyVisitors generates the name
        PropertyWrapper navigationSource = new PropertyWrapper(ac.getNavigationSource());
        String associationClassName = navigationSource.getAssociationClassPathName().getLast();

        if (!associationClassName.equals(UmlgClassOperations.className(ref))) {
             throw new IllegalStateException("Association class member end \"getAssociationClassPathName()\" must be the same as the reffered association class!");
        }

        //TODO @pre not implemented
//        StringBuilder result = new StringBuilder(maybeAtPre(ac, sourceResult + ".get" + name));//$NON-NLS-1$
        StringBuilder result = new StringBuilder();
        if (!sourceResult.equals("self")) {
            result.append(sourceResult);
        } else {
            result.append("this");
        }
        result.append(".");

        if (!qualifierResults.isEmpty()) {
            //qualifiers on association class navigation is necessary on recursive associations to indicate the direction
            //only one qualifier is supported.
            if (qualifierResults.size() > 1) {
                throw new IllegalStateException(String.format("There can only be one qualifier (role) on a association class navigation! Association Class %s", new String[]{ref.getQualifiedName()}));
            }
        }
        result.append(navigationSource.associationClassGetter());
        result.append("()");

        return result.toString();
    }

    protected String initialLower(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }

        StringBuilder result = new StringBuilder(name);
        result.setCharAt(0, Character.toLowerCase(result.charAt(0)));
        return result.toString();
    }

    /**
     * Callback for the Variable visit.
     *
     * @param vd the variable declaration
     * @return string
     */
    @Override
    protected String handleVariable(Variable<Classifier, Parameter> vd, String initResult) {
        return new OclVariableExpToJava().setOJClass(this.ojClass).handleVariable(vd, initResult);
    }

    /**
     * Callback for an IfExp visit.
     *
     * @param ifExp an IfExp
     * @return the string representation
     */
    @Override
    protected String handleIfExp(IfExp<Classifier> ifExp, String conditionResult, String thenResult, String elseResult) {
        return new OclIfExpToJava().setOJClass(this.ojClass).setElement(this.element).handleIfExp(ifExp, conditionResult, thenResult, elseResult);
    }

    @Override
    public String visitTypeExp(TypeExp<Classifier> t) {
        // return getQualifiedName(t.getReferredType());
        return getName(t.getReferredType());
    }

    @Override
    public String visitStateExp(StateExp<Classifier, State> s) {
        return getName(s);
    }

    /**
     * Callback for an UnspecifiedValueExp visit.
     *
     * @param uv - UnspecifiedValueExp
     * @return the string representation
     */
    @Override
    public String visitUnspecifiedValueExp(UnspecifiedValueExp<Classifier> uv) {
        StringBuilder result = new StringBuilder();
        result.append("?"); //$NON-NLS-1$
        if (uv.getType() != null && !(uv.getType() instanceof VoidType<?>)) {
            result.append(" : "); //$NON-NLS-1$
            result.append(getName(uv.getType()));
        }

        return result.toString();
    }

    /**
     * Callback for an IntegerLiteralExp visit.
     *
     * @param il -- integer literal expression
     * @return String
     */
    @Override
    public String visitIntegerLiteralExp(IntegerLiteralExp<Classifier> il) {
        return (il.getIntegerSymbol() == null) ? NULL_PLACEHOLDER : il.getIntegerSymbol().toString();
    }

    /**
     * Callback for an UnlimitedNaturalLiteralExp visit.
     *
     * @param unl -- unlimited natural literal expression
     * @return String
     */
    @Override
    public String visitUnlimitedNaturalLiteralExp(UnlimitedNaturalLiteralExp<Classifier> unl) {
        if (unl.isUnlimited()) {
            return "*"; //$NON-NLS-1$
        }

        return (unl.getIntegerSymbol() == null) ? NULL_PLACEHOLDER : unl.getIntegerSymbol().toString();
    }

    /**
     * Callback for a RealLiteralExp visit.
     *
     * @param rl -- real literal expression
     * @return the value of the real literal as a java.lang.Double.
     */
    @Override
    public String visitRealLiteralExp(RealLiteralExp<Classifier> rl) {
        return (rl.getRealSymbol() == null) ? NULL_PLACEHOLDER : rl.getRealSymbol().toString();
    }

    /**
     * Callback for a StringLiteralExp visit.
     *
     * @param sl -- string literal expression
     * @return the value of the string literal as a java.lang.String.
     */
    @Override
    public String visitStringLiteralExp(StringLiteralExp<Classifier> sl) {
        return "\"" + ((sl.getStringSymbol() == null) ? NULL_PLACEHOLDER //$NON-NLS-1$
                : sl.getStringSymbol()) + "\"";//$NON-NLS-1$
    }

    /**
     * Callback for a BooleanLiteralExp visit.
     *
     * @param bl -- boolean literal expression
     * @return the value of the boolean literal as a java.lang.Boolean.
     */
    @Override
    public String visitBooleanLiteralExp(BooleanLiteralExp<Classifier> bl) {
        return (bl.getBooleanSymbol() == null) ? NULL_PLACEHOLDER : bl.getBooleanSymbol().toString();
    }

    /**
     * Callback for LetExp visit.
     *
     * @param letExp a let expression
     * @return the string representation
     */
    @Override
    protected String handleLetExp(LetExp<Classifier, Parameter> letExp, String variableResult, String inResult) {

        StringBuilder result = new StringBuilder();
        result.append("let ").append(variableResult); //$NON-NLS-1$
        result.append(" in ").append(inResult); //$NON-NLS-1$

        return result.toString();

    }

    /**
     * Callback for an IterateExp visit.
     *
     * @param callExp an iterate expression
     * @return the string representation
     */
    @Override
    protected String handleIterateExp(IterateExp<Classifier, Parameter> callExp, String sourceResult, List<String> variableResults, String resultResult,
                                      String bodyResult) {
        this.ojClass.addToImports(UmlgGenerationUtil.umlgMemoryCollectionLib);
        this.ojClass.addToImports(UmlgGenerationUtil.umlgOclStdCollectionLib);
        this.ojClass.addToImports("java.util.*");
        return new OclIterateExpToJava().handleIterateExp(callExp, sourceResult, variableResults, resultResult, bodyResult);
    }

    /**
     * Callback for an IteratorExp visit.
     *
     * @param callExp an iterator expression
     * @return the string representation
     */
    @Override
    protected String handleIteratorExp(IteratorExp<Classifier, Parameter> callExp, String sourceResult, List<String> variableResults, String bodyResult) {
        this.ojClass.addToImports(UmlgGenerationUtil.umlgOclStdCollectionLib);
        this.ojClass.addToImports("java.util.*");
        String name = callExp.getName();
        return OclIteratorExpEnum.from(name).handleIteratorExp(this.ojClass, callExp, sourceResult, variableResults, bodyResult);
    }

    /**
     * Callback for a CollectionLiteralExp visit.
     * This seems to be called when an object needs to converted to a collection
     *
     * @param cl collection literal expression
     * @return String
     */
    @Override
    protected String handleCollectionLiteralExp(CollectionLiteralExp<Classifier> cl, List<String> partResults) {
        //This is a hack to get around ocl incorrect parsing of qualified association.
        //Ocl treats the qualified association as a multiplicity of 1, thus this handleCollectionLiteralExp fires.
        //This check skips the to collection logic
        boolean qualified = false;
        List<CollectionLiteralPart<Classifier>> parts = cl.getPart();
        for (CollectionLiteralPart<Classifier> part : parts) {
            if (part instanceof CollectionItemImpl) {
                CollectionItemImpl partCollectionItem = (CollectionItemImpl) part;
                OCLExpression<Classifier> item = partCollectionItem.getItem();
                if (item instanceof org.eclipse.ocl.uml.PropertyCallExp) {
                    org.eclipse.ocl.uml.PropertyCallExp propertyCallExp = (org.eclipse.ocl.uml.PropertyCallExp) item;
                    Property p = propertyCallExp.getReferredProperty();
                    qualified = !p.getQualifiers().isEmpty();
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        if (!qualified) {
            CollectionTypeImpl c = (CollectionTypeImpl) cl.getType();
            Classifier elementType = c.getElementType();
            OJPathName o = UmlgCollectionKindEnum.from(cl.getKind()).getMemoryCollection();
            if (!(elementType instanceof org.eclipse.ocl.uml.VoidType)) {
                OJPathName collectionGenericPath = UmlgClassOperations.getPathName(elementType);
                o.addToGenerics(collectionGenericPath);
            }
            sb.append("new " + o.getLast() + "(Arrays.asList(");
            int count = 1;
            for (String value : partResults) {
                sb.append(value);
                if (partResults.size() != count++) {
                    sb.append(", ");
                }
            }
            sb.append("))");
            this.ojClass.addToImports(o);
            this.ojClass.addToImports(new OJPathName("java.util.Arrays"));
            // Can not add a generic parameter here as the information is not
            // available in the collection literal
        } else {
            int count = 1;
            for (String value : partResults) {
                sb.append(value);
                if (partResults.size() != count++) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }

    @Override
    protected String handleCollectionItem(CollectionItem<Classifier> item, String itemResult) {
        return itemResult;
    }

    @Override
    protected String handleCollectionRange(CollectionRange<Classifier> range, String firstResult, String lastResult) {
        return firstResult + ".." + lastResult; //$NON-NLS-1$
    }

    /**
     * Callback for a TupleLiteralExp visit.
     *
     * @param literalExp tuple literal expression
     * @return the string representation
     */
    @Override
    protected String handleTupleLiteralExp(TupleLiteralExp<Classifier, Property> literalExp, List<String> partResults) {
        return new OclTupleLiteralExpToJava().setOJClass(this.ojClass).handleTupleLiteralExp(literalExp, partResults);

    }

    @Override
    protected String handleTupleLiteralPart(TupleLiteralPart<Classifier, Property> part, String valueResult) {
        return new OclTupleLiteralPartToJava().setOJClass(this.ojClass).handleTupleLiteralPart(part, valueResult);
    }

    @Override
    protected String handleMessageExp(MessageExp<Classifier, CallOperationAction, SendSignalAction> messageExp, String targetResult,
                                      List<String> argumentResults) {
        StringBuilder result = new StringBuilder();

        result.append(targetResult);

        result.append((messageExp.getType() instanceof CollectionType<?, ?>) ? "^^" : "^"); //$NON-NLS-1$//$NON-NLS-2$

        if (messageExp.getCalledOperation() != null) {
            result.append(getName(getOperation(messageExp.getCalledOperation())));
        } else if (messageExp.getSentSignal() != null) {
            result.append(getName(getSignal(messageExp.getSentSignal())));
        }

        result.append('(');

        for (Iterator<String> iter = argumentResults.iterator(); iter.hasNext(); ) {
            result.append(iter.next());

            if (iter.hasNext()) {
                result.append(", "); //$NON-NLS-1$
            }
        }

        result.append(')');

        return result.toString();
    }

    protected Operation getOperation(CallOperationAction callOperationAction) {
        return (uml == null) ? null : uml.getOperation(callOperationAction);
    }

    protected Classifier getSignal(SendSignalAction sendSignalAction) {
        return (uml == null) ? null : uml.getSignal(sendSignalAction);
    }

    /**
     * Renders an ExpressionInOcl with its context variables and body.
     */
    @Override
    public String visitExpressionInOCL(ExpressionInOCL<Classifier, Parameter> expression) {
        return expression.getBodyExpression().accept(this);
    }

    /**
     * Renders a constraint with its context and expression.
     */
    @Override
    public String visitConstraint(Constraint constraint) {
        StringBuilder result = new StringBuilder();

        List<? extends EObject> constrained = getConstrainedElements(constraint);

        if (!constrained.isEmpty()) {
            EObject elem = constrained.get(0);

            result.append("context "); //$NON-NLS-1$
            if (isClassifier(elem)) {
                result.append(getName(elem));
            } else if (isOperation(elem)) {
                Operation oper = (Operation) elem;
                appendOperationSignature(result, oper);
            } else if (isProperty(elem)) {
                Property prop = (Property) elem;
                appendPropertySignature(result, prop);
            }

            result.append(' ');
        }

        String stereo = getStereotype(constraint);
        if (UMLReflection.PRECONDITION.equals(stereo)) {
            result.append("pre: "); //$NON-NLS-1$
        } else if (UMLReflection.POSTCONDITION.equals(stereo)) {
            result.append("post: "); //$NON-NLS-1$
        } else if (UMLReflection.BODY.equals(stereo)) {
            result.append("body: "); //$NON-NLS-1$
        } else if (UMLReflection.INITIAL.equals(stereo)) {
            result.append("init: "); //$NON-NLS-1$
        } else if (UMLReflection.DERIVATION.equals(stereo)) {
            result.append("derive: "); //$NON-NLS-1$
        } else if (UMLReflection.POSTCONDITION.equals(stereo)) {
            result.append("def: "); //$NON-NLS-1$

            EObject elem = constrained.get(1);

            if (isOperation(elem)) {
                Operation oper = (Operation) elem;
                appendOperationSignature(result, oper);
            } else if (isProperty(elem)) {
                Property prop = (Property) elem;
                appendPropertySignature(result, prop);
            }

            result.append(" = "); //$NON-NLS-1$
        } else {
            result.append("inv "); //$NON-NLS-1$
            result.append(getName(constraint));
            result.append(": "); //$NON-NLS-1$
        }

        result.append(visit(getSpecification(constraint)));

        return result.toString();
    }

    protected boolean isClassifier(Object element) {
        return (uml == null) ? null : uml.isClassifier(element);
    }

    protected boolean isOperation(Object element) {
        return (uml == null) ? null : uml.isOperation(element);
    }

    protected boolean isProperty(Object element) {
        return (uml == null) ? null : uml.isProperty(element);
    }

    protected List<? extends EObject> getConstrainedElements(Constraint constraint) {
        return (uml == null) ? null : uml.getConstrainedElements(constraint);
    }

    protected String getStereotype(Constraint constraint) {
        return (uml == null) ? null : uml.getStereotype(constraint);
    }

    @Override
    protected ExpressionInOCL<Classifier, Parameter> getSpecification(Constraint constraint) {
        return (uml == null) ? null : uml.getSpecification(constraint);
    }

    private void appendOperationSignature(StringBuilder buf, Operation operation) {
        buf.append(getName(operation)).append('(');

        boolean comma = false;
        for (Iterator<Parameter> iter = getParameters(operation).iterator(); iter.hasNext(); ) {
            Parameter parm = iter.next();

            if (comma) {
                buf.append(", "); //$NON-NLS-1$
            } else {
                comma = true;
            }

            buf.append(getName(parm)).append(" : "); //$NON-NLS-1$

            if (getType(parm) != null) {
                buf.append(getName(getType(parm)));
            } else {
                buf.append("OclVoid"); //$NON-NLS-1$
            }
        }

        buf.append(") :"); //$NON-NLS-1$
        if (getType(operation) != null) {
            buf.append(' ').append(getName(getType(operation)));
        }
    }

    protected Classifier getType(Object typedElement) {
        return (uml == null) ? null : TypeUtil.resolveType(env, uml.getOCLType(typedElement));
    }

    protected List<Parameter> getParameters(Operation operation) {
        return (uml == null) ? null : uml.getParameters(operation);
    }

    private void appendPropertySignature(StringBuilder buf, Property property) {
        buf.append(getName(property));
        if (getType(property) != null) {
            buf.append(" : ").append(getName(getType(property))); //$NON-NLS-1$
        }
    }

    /**
     * @since 3.1
     */
    protected String maybeAtPre(FeatureCallExp<Classifier> mpc, String base) {
        return mpc.isMarkedPre() ? base + "@pre" : base; //$NON-NLS-1$
    }

    @Override
    public String visitInvalidLiteralExp(InvalidLiteralExp<Classifier> il) {
        return "invalid"; //$NON-NLS-1$
    }

    @Override
    public String visitNullLiteralExp(NullLiteralExp<Classifier> il) {
        return "null"; //$NON-NLS-1$
    }

    private String visit(Visitable visitable) {
        return (visitable == null) ? NULL_PLACEHOLDER : (String) visitable.accept(this);
    }

}
