package org.umlg.javageneration.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.uml2.uml.*;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.internal.operations.PropertyOperations;
import org.umlg.framework.ModelLoader;
import org.umlg.java.metamodel.OJPathName;
import org.umlg.java.metamodel.OJSimpleStatement;

public final class TumlPropertyOperations extends PropertyOperations {

    public static List<Constraint> getConstraints(Property p) {
        return ModelLoader.INSTANCE.getConstraints(p);
    }

	public static Type findCompositeParent(PropertyWrapper propertyWrapper, PropertyWrapper otherEnd) {
		List<Type> orderedListOfCompositeTypes = new ArrayList<Type>();
		createListOfOrderedTypes(orderedListOfCompositeTypes, propertyWrapper);
		List<Type> otherEndOrderedListOfCompositeTypes = new ArrayList<Type>();
		createListOfOrderedTypes(otherEndOrderedListOfCompositeTypes, otherEnd);

		for (Type type : orderedListOfCompositeTypes) {
			if (otherEndOrderedListOfCompositeTypes.contains(type)) {
				return type;
			}
		}
		return null;
	}

	private static void createListOfOrderedTypes(List<Type> orderedListOfCompositeTypes, PropertyWrapper propertyWrapper) {
		orderedListOfCompositeTypes.add(propertyWrapper.getType());
		Property otherEndToComposite;
		if (propertyWrapper.getType() instanceof Interface) {
			Interface type = (Interface) propertyWrapper.getType();
			otherEndToComposite = TumlInterfaceOperations.getOtherEndToComposite(type);
		} else if (propertyWrapper.getType() instanceof Class) {
			Class type = (Class) propertyWrapper.getType();
			otherEndToComposite = TumlClassOperations.getOtherEndToComposite(type);
		} else {
			throw new RuntimeException("TODO " + propertyWrapper.getType());
		}
		if (otherEndToComposite != null) {
			createListOfOrderedTypes(orderedListOfCompositeTypes, new PropertyWrapper(otherEndToComposite));
		}
	}

	public static Type getOwningType(Property p) {
		Element owner = p.getOwner();
		// Association must come first in this if statement as Association is
		// also a Classifier
		if (owner instanceof Association) {
			Association a = (Association) owner;
			List<Property> members = a.getMemberEnds();
			Property otherEnd = null;
			for (Property member : members) {
				if (member != p) {
					otherEnd = member;
					break;
				}
			}
			if (otherEnd == null) {
				throw new IllegalStateException("Oy, where is the other end gone to!!!");
			}
			return otherEnd.getType();
		} else if (owner instanceof Classifier) {
			return (Classifier) owner;
		} else if (owner instanceof Property && isQualifier(p)) {
			throw new IllegalStateException("Property is a qualifier, this method can not be called for qualifiers");
		} else {
			throw new IllegalStateException("Not catered for, think about ne. " + owner.getClass().getSimpleName());
		}
	}

	public static boolean isUnqualifiedOne(Property property) {
		return !isUnqualifiedMany(property);
	}

	public static boolean isUnqualifiedMany(Property property) {
		return TumlMultiplicityOperations.isMany(property);
	}

	public static boolean isOne(Property property) {
		return !isMany(property);
	}

	public static boolean isQualifier(Property p) {
		return p.getOwner() != null && p.getOwner() instanceof Property;
	}

	public static String fieldName(Property p) {
		return StringUtils.uncapitalize(p.getName());
	}

	public static boolean isPrimitive(Property property) {
		return property.getType() instanceof PrimitiveType;
	}

	public static boolean isEnumeration(Property property) {
		return property.getType() instanceof Enumeration;
	}

	public static boolean isControllingSide(Property p) {
		boolean result = p.isComposite();
		if (p.getOtherEnd() == null) {
			return result = true;
		} else if (isOneToOne(p) && !p.isComposite() && !p.getOtherEnd().isComposite()) {
			// If association is OneToOne and both sides are non composite then
			// take the 1-1 side as inverse=true else compare alphabetically
			if (p.getLower() == 0 && p.getOtherEnd().getLower() == 0) {
				result = p.getName().compareTo(p.getOtherEnd().getName()) > -1;
			} else {
				result = p.getLower() == 1 && p.getUpper() == 1;
			}
		} else if (isOneToMany(p) && !p.isComposite() && !p.getOtherEnd().isComposite()) {
			// If association is OneToMany and both sides are non composite then
			// take the many side as inverse=true
			result = p.getUpper() == -1 || p.getUpper() > 1;
		} else if (isManyToMany(p) && !p.isComposite() && !p.getOtherEnd().isComposite()) {
			// If association is ManyToMany and both sides are non composite
			// then take any side consistently
			result = 0 > p.getName().compareTo(p.getOtherEnd().getName());
		}
		return result;
	}

	public static boolean isOneToMany(Property p) {
		return otherEndIsOne(p) && isMany(p);
	}

	public static boolean isManyToMany(Property p) {
		return !otherEndIsOne(p) && isMany(p);
	}

	public static boolean isManyToOne(Property p) {
		return !otherEndIsOne(p) && isOne(p);
	}

	public static boolean isMany(Property property) {
		int qualifierCount = property.getQualifiers().size();
		return TumlMultiplicityOperations.isMany(property) || qualifierCount > 0;
	}

	public static boolean isOneToOne(Property p) {
		return otherEndIsOne(p) && isOne(p);
	}

	protected static boolean otherEndIsOne(Property p) {
		if (p.getOtherEnd() != null) {
			Property otherEnd = p.getOtherEnd();
			return isOne(otherEnd) && otherEnd.getQualifiers().size() == 0;
		} else {
			// TODO think about, this was false
			//if the other end does not exist treat as a many
			return false;
		}
	}

	public static OJPathName getDefaultTinkerCollection(Property p) {
		OJPathName collectionPathName;
		if (p.isOrdered() && p.isUnique()) {
			if (!p.getQualifiers().isEmpty()) {
				collectionPathName = TumlCollectionKindEnum.QUALIFIED_ORDERED_SET.getImplementationPathName();
			} else {
				collectionPathName = TumlCollectionKindEnum.ORDERED_SET.getImplementationPathName();
			}
		} else if (p.isOrdered() && !p.isUnique()) {
			if (!p.getQualifiers().isEmpty()) {
				collectionPathName = TumlCollectionKindEnum.QUALIFIED_SEQUENCE.getImplementationPathName();
			} else {
				collectionPathName = TumlCollectionKindEnum.SEQUENCE.getImplementationPathName();
			}
		} else if (!p.isOrdered() && !p.isUnique()) {
			if (!p.getQualifiers().isEmpty()) {
				collectionPathName = TumlCollectionKindEnum.QUALIFIED_BAG.getImplementationPathName();
			} else {
				collectionPathName = TumlCollectionKindEnum.BAG.getImplementationPathName();
			}
		} else if (!p.isOrdered() && p.isUnique()) {
			if (!p.getQualifiers().isEmpty()) {
				collectionPathName = TumlCollectionKindEnum.QUALIFIED_SET.getImplementationPathName();
			} else {
				collectionPathName = TumlCollectionKindEnum.SET.getImplementationPathName();
			}
		} else {
			throw new RuntimeException("wtf");
		}
		collectionPathName.addToGenerics(getTypePath(p));
		return collectionPathName;
	}

    public static OJPathName getDefaultTinkerCollectionForAssociationClass(Property p) {
        OJPathName collectionPathName;
        if (p.isOrdered() && p.isUnique()) {
            if (!p.getQualifiers().isEmpty()) {
                collectionPathName = TumlCollectionKindEnum.QUALIFIED_ORDERED_SET.getImplementationPathName();
            } else {
                collectionPathName = TumlCollectionKindEnum.ORDERED_SET.getImplementationPathName();
            }
        } else if (p.isOrdered() && !p.isUnique()) {
            if (!p.getQualifiers().isEmpty()) {
                collectionPathName = TumlCollectionKindEnum.QUALIFIED_SEQUENCE.getImplementationPathName();
            } else {
                collectionPathName = TumlCollectionKindEnum.SEQUENCE.getImplementationPathName();
            }
        } else if (!p.isOrdered() && !p.isUnique()) {
            if (!p.getQualifiers().isEmpty()) {
                collectionPathName = TumlCollectionKindEnum.QUALIFIED_BAG.getImplementationPathName();
            } else {
                collectionPathName = TumlCollectionKindEnum.BAG.getImplementationPathName();
            }
        } else if (!p.isOrdered() && p.isUnique()) {
            if (!p.getQualifiers().isEmpty()) {
                collectionPathName = TumlCollectionKindEnum.QUALIFIED_SET.getImplementationPathName();
            } else {
                collectionPathName = TumlCollectionKindEnum.SET.getImplementationPathName();
            }
        } else {
            throw new RuntimeException("wtf");
        }
        collectionPathName.addToGenerics(new PropertyWrapper(p).getAssociationClassPathName());
        return collectionPathName;
    }


    public static OJSimpleStatement getDefaultTinkerCollectionInitalisation(Property p, BehavioredClassifier propertyConcreteOwner) {
		OJSimpleStatement s = getDefaultTinkerCollectionInitalisation(p, propertyConcreteOwner, getDefaultTinkerCollection(p));
		return s;
	}

    public static OJSimpleStatement getDefaultTinkerCollectionInitalisationForAssociationClass(Property p, BehavioredClassifier propertyConcreteOwner) {
        OJSimpleStatement s = getDefaultTinkerCollectionInitalisationForAssociationClass(p, propertyConcreteOwner, getDefaultTinkerCollectionForAssociationClass(p));
        return s;
    }

    private static OJSimpleStatement getDefaultTinkerCollectionInitalisation(Property p, BehavioredClassifier propertyConcreteOwner, OJPathName collectionPathName) {
		OJSimpleStatement ojSimpleStatement = new OJSimpleStatement(" new " + collectionPathName.getCollectionTypeName() + "(this");
		ojSimpleStatement.setExpression(ojSimpleStatement.getExpression() + ", " + TumlClassOperations.propertyEnumName(propertyConcreteOwner) + "." + new PropertyWrapper(p).fieldname());
		ojSimpleStatement.setExpression(ojSimpleStatement.getExpression() + ")");
		return ojSimpleStatement;
	}

    private static OJSimpleStatement getDefaultTinkerCollectionInitalisationForAssociationClass(Property p, BehavioredClassifier propertyConcreteOwner, OJPathName collectionPathName) {
        OJSimpleStatement ojSimpleStatement = new OJSimpleStatement(" new " + collectionPathName.getCollectionTypeName() + "(this");
        ojSimpleStatement.setExpression(ojSimpleStatement.getExpression() + ", " + TumlClassOperations.propertyEnumName(propertyConcreteOwner) + "." + new PropertyWrapper(p).getAssociationClassFakePropertyName());
        ojSimpleStatement.setExpression(ojSimpleStatement.getExpression() + ")");
        return ojSimpleStatement;
    }

    public static OJPathName getTypePath(Property p) {
		if (!(p.getType() instanceof PrimitiveType) && !(p.getType() instanceof Enumeration) && p.getType() instanceof DataType) {
			return DataTypeEnum.getPathNameFromDataType((DataType) p.getType());
		} else {
			return new OJPathName(Namer.name(p.getType().getNearestPackage()) + "." + TinkerGenerationUtil.umlPrimitiveTypeToJava(p.getType()));
		}
	}

	public static String getter(Property property) {
		return "get" + StringUtils.capitalize(property.getName());
	}

	public static String setter(Property property) {
		return "set" + StringUtils.capitalize(property.getName());
	}

	public static String validator(Property property) {
		return "validate" + StringUtils.capitalize(property.getName());
	}

    public static String checkConstraint(Property property, Constraint constraint) {
        return "checkConstraintForProperty" + StringUtils.capitalize(property.getName() + "AndConstraint" + StringUtils.capitalize(constraint.getName()));
    }

    public static String adder(Property property) {
		return "addTo" + StringUtils.capitalize(property.getName());
	}

	public static String remover(Property property) {
		return "removeFrom" + StringUtils.capitalize(property.getName());
	}

	public static String clearer(Property property) {
		return "clear" + StringUtils.capitalize(property.getName());
	}

	public static String internalAdder(Property property) {
		return "z_internalAddTo" + StringUtils.capitalize(property.getName());
	}

	public static String internalRemover(Property property) {
		return "z_internalRemoveFrom" + StringUtils.capitalize(property.getName());
	}

}
