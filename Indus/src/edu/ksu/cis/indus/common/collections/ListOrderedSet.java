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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NumericalConstraint;
import edu.ksu.cis.indus.annotations.NumericalConstraint.NumericalValue;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This is a ordered set that maintains insertion ordering.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <E> is the type of object in the set.
 */
public class ListOrderedSet<E>
		extends AbstractSet<E> {

	/**
	 * This stores the elements for order related operation.
	 */
	@NonNull final List<E> list;

	/**
	 * This stores the elements in a set for fast access.
	 */
	@NonNull private final Set<E> set;

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
	 * @param c is the collection whose contents will be added to this set.
	 */
	public ListOrderedSet(@NonNull @Immutable final Collection<E> c) {
		set = new HashSet<E>(c);
		list = new ArrayList<E>(c);
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param initialCapacity <i>see super class constructor.</i>
	 */
	public ListOrderedSet(@NumericalConstraint(value = NumericalValue.NON_NEGATIVE) final int initialCapacity) {
		set = new HashSet<E>(initialCapacity);
		list = new ArrayList<E>(initialCapacity);
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param initialCapacity <i>see super class constructor.</i>
	 * @param loadFactor <i>see super class constructor.</i>
	 */
	public ListOrderedSet(@NumericalConstraint(value = NumericalValue.NON_NEGATIVE) final int initialCapacity,
			@NumericalConstraint(value = NumericalValue.NON_NEGATIVE) final float loadFactor) {
		set = new HashSet<E>(initialCapacity, loadFactor);
		list = new ArrayList<E>(initialCapacity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean add(@Immutable final E o) {
		final boolean _t = set.add(o);
		if (_t) {
			list.add(o);
		}
		return _t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void clear() {
		set.clear();
		list.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public boolean contains(final Object o) {
		return set.contains(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public boolean containsAll(@NonNull final Collection<?> c) {
		return set.containsAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public boolean equals(@NonNull @Immutable final Object o) {
		if (o instanceof ListOrderedSet) {
			final ListOrderedSet<?> _t = (ListOrderedSet) o;
			return set.equals(_t.list) && list.equals(_t.list);
		}
		return false;
	}

	/**
	 * Retrieves the i-th element according to the insertion order.
	 * 
	 * @param i is the index.
	 * @return the element, if it exists.
	 * @throws IndexOutOfBoundsException when <code>i</code> is less than 0 or when <code>i</code> is greater than or
	 *             equal to size().
	 */
	@Functional public E get(final int i) throws IndexOutOfBoundsException {
		return list.get(i);
	}

	/**
	 * Retrieves a view of the set as an unmodifiable list.
	 * 
	 * @return the unmodifiable sequential view of the set.
	 */
	@Functional @NonNull public List<E> getUnmodifiableList() {
		return Collections.unmodifiableList(new AbstractList<E>() {

			@Override public E get(final int index) {
				return list.get(index);
			}

			@Override public int size() {
				return list.size();
			}

		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public int hashCode() {
		return set.hashCode() + list.hashCode();
	}

	/**
	 * Retrieves the index of the given object corresponding to its first insertion into the set.
	 * 
	 * @param object of interest.
	 * @return a non-negative index if <code>object</code> exists in the set; -1, otherwise.
	 */
	@Functional public int indexOf(final E object) {
		return list.indexOf(object);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public boolean isEmpty() {
		return set.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @NonNull @Override public Iterator<E> iterator() {
		return list.iterator();
	}

	/**
	 * Removes the i-th inserted element.
	 * 
	 * @param i is the index of the element that should be removed.
	 * @return the removed element, if it existed.
	 * @throws IndexOutOfBoundsException when <code>i</code> is less than 0 or when <code>i</code> is greater than or
	 *             equal to size().
	 */
	@Functional @NonNull public E remove(final int i) throws IndexOutOfBoundsException {
		final E _t = get(i);
		set.remove(_t);
		return _t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean remove(@Immutable final Object o) {
		list.remove(o);
		return set.remove(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean removeAll(@NonNull @Immutable final Collection<?> c) {
		return super.removeAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean retainAll(@NonNull @Immutable final Collection<?> c) {
		list.retainAll(c);
		return set.retainAll(c);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public int size() {
		return set.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @NonNull @Override public Object[] toArray() {
		return list.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public <T> T[] toArray(@NonNull final T[] a) {
		return list.toArray(a);
	}

}

// End of File
