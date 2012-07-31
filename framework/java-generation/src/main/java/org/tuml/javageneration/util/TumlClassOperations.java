package org.tuml.javageneration.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.ocl.uml.CollectionType;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.VisibilityKind;
import org.eclipse.uml2.uml.internal.operations.ClassOperations;
import org.opaeum.java.metamodel.OJPathName;
import org.opaeum.java.metamodel.OJVisibilityKind;
import org.tuml.javageneration.naming.Namer;
import org.tuml.javageneration.ocl.util.TumlCollectionKindEnum;

public class TumlClassOperations extends ClassOperations {

	public static Set<Property> getChildPropertiesToDelete(org.eclipse.uml2.uml.Class clazz) {
		Set<Property> result = new HashSet<Property>();
		Set<Property> ownedProperties = getAllOwnedProperties(clazz);
		for (Property p : ownedProperties) {
			PropertyWrapper pWrap = new PropertyWrapper(p);
			if (!pWrap.isDerived() && (pWrap.isComposite() || (!pWrap.isPrimitive() && !pWrap.isEnumeration() && pWrap.getOtherEnd()==null))) {
				result.add(p);
			}
		}
		return result;
	}

	/*
	 * Returns all properties that are to be persisted on the clazz's vertex.
	 * i.e. only simple types and enums
	 */
	public static Set<Property> getOnePrimitiveOrEnumProperties(org.eclipse.uml2.uml.Class clazz) {
		Set<Property> result = new HashSet<Property>();
		for (Property p : clazz.getAttributes()) {
			PropertyWrapper pWrap = new PropertyWrapper(p);
			if (!pWrap.isDerived() && pWrap.isOne() && (pWrap.isPrimitive() || pWrap.isEnumeration())) {
				result.add(p);
			}
		}
		return result;
	}

	/*
	 * These include all properties that are on the other end of an
	 * association
	 */
	public static Set<Property> getAllOwnedProperties(org.eclipse.uml2.uml.Class clazz) {
		Set<Property> result = new HashSet<Property>(clazz.getAttributes());
		List<Association> associations = clazz.getAssociations();
		for (Association association : associations) {
			List<Property> memberEnds = association.getMemberEnds();
			for (Property property : memberEnds) {
				if (property.getType() != clazz) {
					result.add(property);
				}
			}
		}
		result.addAll(getPropertiesOnRealizedInterfaces(clazz));
		return result;
	}
	
	public static Set<Property> getPropertiesOnRealizedInterfaces(org.eclipse.uml2.uml.Class clazz) {
		Set<Property> result = new HashSet<Property>();
		List<Interface> interfaces = clazz.getImplementedInterfaces();
		for (Interface inf : interfaces) {
			Set<Property> properties = TumlInterfaceOperations.getAllProperties(inf);
			for (Property p : properties) {
				result.add(p);
			}
		}
		return result;
	}

	public static Property getOtherEndToComposite(Class clazz) {
		Set<Association> associations = getAllAssociations(clazz);
		for (Association association : associations) {
			List<Property> memberEnds = association.getMemberEnds();
			for (Property property : memberEnds) {
				if (!property.isComposite() && property.getType() != clazz && property.getOtherEnd().isComposite() && isSpecializationOf(clazz, property.getOtherEnd().getType())) {
					return property;
				}
			}
		}
		return null;
	}

	/*
	 * Only BehavioredClassifier can realize interfaces
	 */
	public static boolean isSpecializationOf(Classifier classifier, Type type) {
		if (classifier == type) {
			return true;
		}
		if ((classifier instanceof BehavioredClassifier) && ((BehavioredClassifier) classifier).getAllImplementedInterfaces().contains(type)) {
			return true;
		}
		for (Classifier general : classifier.getGenerals()) {
			if (isSpecializationOf(general, type)) {
				return true;
			}
		}
		return false;
	}

	public static Set<Association> getAllAssociations(Class clazz) {
		Set<Association> result = new HashSet<Association>();
		for (Interface implementedInterface : clazz.getAllImplementedInterfaces()) {
			result.addAll(implementedInterface.getAssociations());
		}
		getAllAssociationsFromGenerals(clazz, result);
		return result;
	}

	public static void getAllAssociationsFromGenerals(Classifier classifier, Set<Association> result) {
		result.addAll(classifier.getAssociations());
		for (Classifier general : classifier.getGenerals()) {
			getAllAssociationsFromGenerals(general, result);
		}
	}

	public static OJPathName getOtherEndToCompositePathName(Class clazz) {
		Property endToComposite = getOtherEndToComposite(clazz);
		if (endToComposite != null) {
			return getPathName(endToComposite.getType());
		} else {
			return null;
		}
	}

	public static OJPathName getPathName(Type type) {
		String className = Namer.name(type);
		String fullPackageName = Namer.name(type.getNearestPackage());
		OJPathName ojPathName = new OJPathName(fullPackageName + "." + className);
		return ojPathName;
	}

	public static boolean hasSupertype(Class clazz) {
		return !getConcreteGenerals(clazz).isEmpty();
	}

	public static List<Class> getConcreteGenerals(Class clazz) {
		List<Class> result = new ArrayList<Class>();
		List<Classifier> generals = clazz.getGenerals();
		for (Classifier classifier : generals) {
			if (classifier instanceof Class) {
				result.add((Class) classifier);
			}
		}
		return result;
	}

	public static boolean hasCompositeOwner(Class clazz) {
		return getOtherEndToComposite(clazz) != null;
	}

	public static String className(Classifier clazz) {
		if (clazz instanceof CollectionType) {
			CollectionType collectionType = (CollectionType)clazz;
			StringBuilder sb = new StringBuilder();
			sb.append(TumlCollectionKindEnum.from(collectionType.getKind()).getOjPathName().getLast());
			sb.append("<");
			sb.append(className(collectionType.getElementType()));
			sb.append(">");
			return sb.toString();
		} else {
			return Namer.name(clazz);
		}
	}
	
	public static String propertyEnumName(Type type) {
		return Namer.name(type) + "RuntimePropertyEnum";
	}

	public static OJVisibilityKind getVisibility(VisibilityKind visibility) {
		switch (visibility) {
		case PRIVATE_LITERAL:
			return OJVisibilityKind.PRIVATE;
		case PROTECTED_LITERAL:
			return OJVisibilityKind.PROTECTED;
		case PUBLIC_LITERAL:
			return OJVisibilityKind.PUBLIC;
		case PACKAGE_LITERAL:
			return OJVisibilityKind.DEFAULT;
		default:
			throw new RuntimeException("Not supported");
		}
	}

	public static boolean isRoot(Class clazz) {
		return false;
	}

	public static OJPathName getAuditPathName(Class c) {
		OJPathName pathName = getPathName(c);
		return pathName.renameLast(pathName.getLast() + "Audit");
	}

	public static Property getAttribute(Class c , String name) {
		for (Property p : c.getAllAttributes()) {
			if (p.getName().equals(name)) {
				return p;
			}
		}
		return null;
	}

	public static boolean isOnInterface(PropertyWrapper pWrap) {
		return pWrap.getOwningType() instanceof Interface;
	}

}
