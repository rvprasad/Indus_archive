
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.common.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.Predicate;

import org.apache.commons.collections.set.AbstractSetDecorator;


/**
 * This class provides a filtered updatable view of a set.  Like <code>FilteredCollection</code>, all operations are
 * filtered.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 *
 * @see FilteredCollection
 */
public class FilteredSet
  extends AbstractSetDecorator {
	/** 
	 * The delegate collection instance that does the filtering.
	 *
	 * @invariant filteredCollection != null
	 */
	private final FilteredCollection filteredCollection;

	/**
	 * Creates an instance of this class.
	 *
	 * @param set to be filtered/decorated.
	 * @param thePredicate that defines the filtering criterion.
	 *
	 * @pre set != null and thePredicate != null
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

// End of File
