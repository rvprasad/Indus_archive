
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

import org.apache.commons.collections.Predicate;

import org.apache.commons.collections.collection.AbstractCollectionDecorator;

import org.apache.commons.collections.iterators.FilterIterator;


/**
 * This class provides a filtered updatable view of a collection.
 * 
 * <p>
 * Only elements that satisfy the predicate are accessible  to the user via this class.  Likewise, only elements that satisfy
 * the predicate are added/removed from the collection. Another way of looking at this class is all elements of the
 * wrapped/decorated class are filtered through the predicate and the requested operation is executed on the result of the
 * filtering operation.  Similarly, all updates are filtered through the predicate and then applied to the decorated class.
 * </p>
 * 
 * <p>
 * All operations via iterators obtained by this class are also filtered.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class FilteredCollection
  extends AbstractCollectionDecorator {
	/**
	 * The predicate that defines the filtering criterion.
	 *
	 * @invariant predicate != null
	 */
	protected final Predicate predicate;

	/**
	 * Creates an instance of this class.
	 *
	 * @param col is the collection to be wrapped.
	 * @param thePredicate that defines the filtering criterion.
	 *
	 * @pre col != null and predicate != null
	 */
	public FilteredCollection(final Collection col, final Predicate thePredicate) {
		super(col);
		predicate = thePredicate;
	}

	/**
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return super.isEmpty() || size() == 0;
	}

	/**
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(final Object object) {
		boolean _result = predicate.evaluate(object);

		if (_result) {
			_result = super.add(object);
		}
		return _result;
	}

	/**
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	public boolean addAll(final Collection col) {
		boolean _result = true;

		for (final Iterator _i = col.iterator(); _i.hasNext();) {
			_result &= add(_i.next());
		}
		return _result;
	}

	/**
	 * @see java.util.Collection#clear()
	 */
	public void clear() {
		for (final Iterator _i = super.iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (predicate.evaluate(_o)) {
				_i.remove();
			}
		}
	}

	/**
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	public boolean contains(final Object object) {
		return predicate.evaluate(object) && super.contains(object);
	}

	/**
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(final Collection col) {
		boolean _result = super.containsAll(col);

		for (final Iterator _i = col.iterator(); _i.hasNext() && _result;) {
			_result &= predicate.evaluate(_i.next());
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object object) {
		boolean _result = false;

		if (object instanceof Collection) {
			final Collection _candidate = (Collection) object;
			_result = _candidate.size() == size() && _candidate.containsAll(this);
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int _hashCode = 17;

		for (final Iterator _i = iterator(); _i.hasNext();) {
			_hashCode += 37 * _hashCode + _i.next().hashCode();
		}
		return _hashCode;
	}

	/**
	 * @see java.util.Collection#iterator()
	 */
	public Iterator iterator() {
		return new FilterIterator(super.iterator(), predicate);
	}

	/**
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(final Object object) {
		boolean _result = false;

		if (predicate.evaluate(object)) {
			_result = super.remove(object);
		}
		return _result;
	}

	/**
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	public boolean removeAll(final Collection col) {
		boolean _result = false;

		for (final Iterator _i = col.iterator(); _i.hasNext();) {
			_result |= remove(_i.next());
		}
		return _result;
	}

	/**
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(final Collection coll) {
		boolean _result = false;

		for (final Iterator _i = iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (!coll.contains(_o)) {
				_i.remove();
				_result = true;
			}
		}
		return _result;
	}

	/**
	 * @see java.util.Collection#size()
	 */
	public int size() {
		int _size = 0;

		for (final Iterator _i = iterator(); _i.hasNext(); _i.next()) {
			_size++;
		}

		return _size;
	}

	/**
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		final Object[] _result = new Object[size()];
		int _j = 0;

		for (final Iterator _i = iterator(); _i.hasNext();) {
			final Object _o = _i.next();
			_result[_j++] = _o;
		}
		return _result;
	}

	/**
	 * @see java.util.Collection#toArray(java.lang.Object[])
	 */
	public Object[] toArray(final Object[] array) {
		final int _size = size();
		Object[] _result = array;

		if (_result.length < _size) {
			_result = (Object[]) java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), _size);
		}

		final Iterator _it = iterator();

		for (int _i = 0; _i < _size; _i++) {
			_result[_i] = _it.next();
		}

		if (_result.length > _size) {
			_result[_size] = null;
		}

		return _result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer _result = new StringBuffer();
		_result.append("[");

		for (final Iterator _i = iterator(); _i.hasNext();) {
			_result.append(_i.next());
			_result.append(", ");
		}
		_result.append("]");
		return _result.toString();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/06/28 15:38:32  venku
   - documentation.
   Revision 1.2  2004/06/28 08:08:27  venku
   - new collections classes for filtered access and update.
   Revision 1.1  2004/06/27 23:23:11  venku
   - initial commit.
   - This version has update methods as well.
 */
