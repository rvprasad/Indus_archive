
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


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class FilteredCollection
  extends AbstractCollectionDecorator {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected final Predicate predicate;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private int size;

	/**
	 * DOCUMENT ME!
	 *
	 * @param col DOCUMENT ME!
	 * @param thePredicate DOCUMENT ME!
	 */
	public FilteredCollection(final Collection col, final Predicate thePredicate) {
		super(col);
		predicate = thePredicate;

		for (final Iterator _i = col.iterator(); _i.hasNext();) {
			final Object _obj = _i.next();

			if (predicate.evaluate(_obj)) {
				size++;
			}
		}
	}

	/**
	 * @see java.util.Collection#isEmpty()
	 */
	public boolean isEmpty() {
		return collection.isEmpty() || size() == 0;
	}

	/**
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	public boolean add(final Object object) {
		boolean _result = predicate.evaluate(object);

		if (_result) {
			_result = super.add(object);

			if (_result) {
				size++;
			}
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
		for (final Iterator _i = getCollection().iterator(); _i.hasNext();) {
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
		// TODO: Auto-generated method stub
		return super.equals(object);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		// TODO: Auto-generated method stub
		return super.hashCode();
	}

	/**
	 * @see java.util.Collection#iterator()
	 */
	public Iterator iterator() {
		return new FilteredCollectionIterator(super.iterator(), predicate);
	}

	/**
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	public boolean remove(final Object object) {
		boolean _result = false;

		if (predicate.evaluate(object)) {
			_result = super.remove(object);

			if (_result) {
				size++;
			}
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
		return size;
	}

	/**
	 * @see java.util.Collection#toArray()
	 */
	public Object[] toArray() {
		final Object[] _result = new Object[size];
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
		// TODO: Auto-generated method stub
		return super.toString();
	}
}

/*
   ChangeLog:
   $Log$
 */
