
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
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.Predicate;

import org.apache.commons.collections.iterators.FilterIterator;

import org.apache.commons.collections.list.AbstractListDecorator;


/**
 * This class provides a filtered updatable view of a list.  Like <code>FilteredCollection</code>, all operations are
 * filtered.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 *
 * @see FilteredCollection
 */
public final class FilteredList
  extends AbstractListDecorator {
	/**
	 * The delegate collection instance that does the filtering.
	 *
	 * @invariant filteredCollection != null
	 */
	private final FilteredCollection filteredCollection;

	/**
	 * The predicate that defines the filtering criterion.
	 *
	 * @invariant predicate != null
	 */
	private final Predicate predicate;

	/**
	 * Creates an instance of this class.
	 *
	 * @param list to be filtered/decorated.
	 * @param thePredicate that defines the filtering criterion.
	 *
	 * @pre list != null and thePredicate != null
	 */
	public FilteredList(final List list, final Predicate thePredicate) {
		filteredCollection = new FilteredCollection(list, thePredicate);
		predicate = thePredicate;
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#isEmpty()
	 */
	public boolean isEmpty() {
		return filteredCollection.isEmpty();
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#add(java.lang.Object)
	 */
	public boolean add(final Object o) {
		return filteredCollection.add(o);
	}

	/**
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(final int index, final Object object) {
		if (predicate.evaluate(object)) {
			super.add(index, object);
		}
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#addAll(java.util.Collection)
	 */
	public boolean addAll(final Collection c) {
		return filteredCollection.addAll(c);
	}

	/**
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(final int index, final Collection col) {
		int _temp = index;

		for (final Iterator _i = new FilterIterator(col.iterator(), predicate); _i.hasNext();) {
			super.add(_temp++, _i.next());
		}
		return _temp != index;
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#clear()
	 */
	public void clear() {
		filteredCollection.clear();
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
	 * @see java.util.List#get(int)
	 */
	public Object get(final int index) {
		Object _result = null;

		if (predicate.evaluate(_result)) {
			_result = super.get(index);
		}
		return _result;
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#hashCode()
	 */
	public int hashCode() {
		return filteredCollection.hashCode();
	}

	/**
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(final Object object) {
		int _result = -1;

		if (predicate.evaluate(object)) {
			_result = super.indexOf(object);
		}
		return _result;
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#iterator()
	 */
	public Iterator iterator() {
		return filteredCollection.iterator();
	}

	/**
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(final Object object) {
		int _result = -1;

		if (predicate.evaluate(object)) {
			_result = super.lastIndexOf(object);
		}
		return _result;
	}

	/**
	 * @see java.util.List#listIterator()
	 */
	public ListIterator listIterator() {
		return listIterator(0);
	}

	/**
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator listIterator(final int index) {
		return new FilteredListIterator(super.listIterator(index), predicate);
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#remove(java.lang.Object)
	 */
	public boolean remove(final Object o) {
		return filteredCollection.remove(o);
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#removeAll(java.util.Collection)
	 */
	public boolean removeAll(final Collection c) {
		return filteredCollection.removeAll(c);
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#retainAll(java.util.Collection)
	 */
	public boolean retainAll(final Collection c) {
		return filteredCollection.retainAll(c);
	}

	/**
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public Object set(final int index, final Object object) {
		Object _result = null;

		if (predicate.evaluate(object)) {
			_result = super.set(index, object);
		}
		return _result;
	}

	/**
	 * @see org.apache.commons.collections.collection.AbstractCollectionDecorator#size()
	 */
	public int size() {
		return filteredCollection.size();
	}

	/**
	 * @see java.util.List#subList(int, int)
	 */
	public List subList(final int fromIndex, final int toIndex) {
		return new FilteredList(super.subList(fromIndex, toIndex), predicate);
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
   Revision 1.3  2004/06/29 01:59:46  venku
   - added more operations via delegation.

   Revision 1.2  2004/06/28 15:53:30  venku
   - documentation.
   Revision 1.1  2004/06/28 08:08:27  venku
   - new collections classes for filtered access and update.
 */
