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

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <E>
 */
public class ListOrderedSet<E>
		extends AbstractSet<E> {

	/**
	 * DOCUMENT ME!
	 */
	final List<E> list;

	/**
	 * DOCUMENT ME!
	 */
	private final Set<E> set;

	/**
	 * Creates an instance of this class.
	 */
	public ListOrderedSet() {
		super();
		set = new HashSet<E>();
		list = new ArrayList<E>();
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param c DOCUMENT ME!
	 */
	public ListOrderedSet(final Collection<E> c) {
		set = new HashSet<E>(c);
		list = new ArrayList<E>(c);
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param initialCapacity DOCUMENT ME!
	 */
	public ListOrderedSet(final int initialCapacity) {
		set = new HashSet<E>(initialCapacity);
		list = new ArrayList<E>(initialCapacity);
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param initialCapacity DOCUMENT ME!
	 * @param loadFactor DOCUMENT ME!
	 */
	public ListOrderedSet(final int initialCapacity, final float loadFactor) {
		set = new HashSet<E>(initialCapacity, loadFactor);
		list = new ArrayList<E>(initialCapacity);
	}

	/**
	 * @see java.util.AbstractCollection#add(Object)
	 */
	@Override public boolean add(final E o) {
		final boolean _t = set.add(o);
		if (_t) {
			list.add(o);
		}
		return _t;
	}

	/**
	 * @see java.util.AbstractCollection#clear()
	 */
	@Override public void clear() {
		set.clear();
		list.clear();
	}

	/**
	 * @see java.util.AbstractCollection#contains(java.lang.Object)
	 */
	@Override public boolean contains(Object o) {
		return set.contains(o);
	}

	/**
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	@Override public boolean containsAll(final Collection<?> c) {
		return set.containsAll(c);
	}

	/**
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	@Override public boolean equals(final Object o) {
		if (o instanceof ListOrderedSet) {
			final ListOrderedSet<?> _t = (ListOrderedSet) o;
			return set.equals(_t.list) && list.equals(_t.list);
		}
		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param i DOCUMENT ME!
	 * @return DOCUMENT ME!
	 * @throws IndexOutOfBoundsException when <code>i</code> is less than 0 or when <code>i</code> is greater than or
	 *             equal to size().
	 */
	public E get(final int i) throws IndexOutOfBoundsException {
		return list.get(i);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public List<E> getUnmodifiableList() {
		return Collections.unmodifiableList(new AbstractList<E>() {

			@Override public E get(int index) {
				return list.get(index);
			}

			@Override public int size() {
				return list.size();
			}

		});
	}

	/**
	 * @see java.util.AbstractSet#hashCode()
	 */
	@Override public int hashCode() {
		return set.hashCode() + list.hashCode();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param object DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public int indexOf(final E object) {
		return list.indexOf(object);
	}

	/**
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	@Override public boolean isEmpty() {
		return set.isEmpty();
	}

	/**
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override public Iterator<E> iterator() {
		return list.iterator();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param i DOCUMENT ME!
	 * @return DOCUMENT ME!
	 * @throws IndexOutOfBoundsException when <code>i</code> is less than 0 or when <code>i</code> is greater than or
	 *             equal to size().
	 */
	public E remove(final int i) {
		final E _t = get(i);
		set.remove(_t);
		return _t;
	}

	/**
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	@Override public boolean remove(final Object o) {
		list.remove(o);
		return set.remove(o);
	}

	/**
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	@Override public boolean removeAll(final Collection<?> c) {
		return super.removeAll(c);
	}

	/**
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Override public boolean retainAll(final Collection<?> c) {
		list.retainAll(c);
		return set.retainAll(c);
	}

	/**
	 * @see java.util.AbstractCollection#size()
	 */
	@Override public int size() {
		return set.size();
	}

	/**
	 * @see java.util.AbstractCollection#toArray()
	 */
	@Override public Object[] toArray() {
		return list.toArray();
	}

	/**
	 * @see java.util.AbstractCollection#toArray(T[])
	 */
	@Override public <T> T[] toArray(final T[] a) {
		return list.toArray(a);
	}

}

// End of File
