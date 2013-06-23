package org.umlg.runtime.collection;

import org.umlg.runtime.domain.AssociationClassNode;

/**
 * Date: 2013/06/18
 * Time: 6:00 PM
 */
public interface UmlgPropertyAssociationClassOrderedSet<E, AC extends AssociationClassNode> extends TinkerOrderedSet<E> {

    boolean add(E e, AC associationClass);
}
