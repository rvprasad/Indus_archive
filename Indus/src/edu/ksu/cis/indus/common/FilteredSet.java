
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.common;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.Predicate;

import org.apache.commons.collections.set.AbstractSetDecorator;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FilteredSet
  extends AbstractSetDecorator {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final FilteredCollection filteredCollection;

	/**
	 * DOCUMENT ME!
	 *
	 * @param set DOCUMENT ME!
	 * @param thePredicate DOCUMENT ME!
	 */
	public FilteredSet(final Set set, final Predicate thePredicate) {
		super(set);
		filteredCollection = new FilteredCollection(set, thePredicate);
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#isEmpty()
	 */
	public boolean isEmpty() {
		return filteredCollection.isEmpty();
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#contains(java.lang.Object)
	 */
	public boolean contains(final Object o) {
		return filteredCollection.contains(o);
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#containsAll(java.util.Collection)
	 */
	public boolean containsAll(final Collection c) {
		return filteredCollection.containsAll(c);
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#equals(java.lang.Object)
	 */
	public boolean equals(final Object obj) {
		return filteredCollection.equals(obj);
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#hashCode()
	 */
	public int hashCode() {
		return filteredCollection.hashCode();
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#iterator()
	 */
	public Iterator iterator() {
		return filteredCollection.iterator();
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#size()
	 */
	public int size() {
		return filteredCollection.size();
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#toArray()
	 */
	public Object[] toArray() {
		return filteredCollection.toArray();
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#toArray(java.lang.Object[])
	 */
	public Object[] toArray(final Object[] a) {
		return filteredCollection.toArray(a);
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#toString()
	 */
	public String toString() {
		return filteredCollection.toString();
	}
}

/*
   ChangeLog:
   $Log$
 */
