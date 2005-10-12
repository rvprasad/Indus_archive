
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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
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
 * @param <T> 
 */
public final class ListOrderedSet<T>
		extends AbstractSet<T> {

	/**
	 * DOCUMENT ME!
	 */
	private final Set<T> set = new HashSet<T>();
	
	/**
	 * DOCUMENT ME!
	 */
	private final List<T> list = new ArrayList<T>();
	
	/**
	 * Creates an instance of this class.
	 * 
	 */
	public ListOrderedSet() {
		super();
	}

	/** 
	 * @see java.util.AbstractCollection#iterator()
	 */
	@Override public Iterator<T> iterator() {
		return list.iterator();
	}

	/** 
	 * @see java.util.AbstractCollection#size()
	 */
	@Override public int size() {
		return set.size();
	}

	/** 
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	@Override public boolean equals(final Object o) {
		if (o instanceof ListOrderedSet) {
			final ListOrderedSet _t = (ListOrderedSet) o;
			return set.equals(_t.list) && list.equals(_t.list);			
		}
		return false;
	}

	/** 
	 * @see java.util.AbstractSet#hashCode()
	 */
	@Override public int hashCode() {
		return set.hashCode() + list.hashCode();
	}

	/** 
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	@Override public boolean removeAll(final Collection<?> c) {
		return super.removeAll(c);
	}

	/** 
	 * @see java.util.AbstractCollection#add(E)
	 */
	@Override public boolean add(final T o) {
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
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	@Override public boolean isEmpty() {
		return set.isEmpty();
	}

	/** 
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	@Override public boolean remove(final Object o) {
		list.remove(o);
		return set.remove(o);
	}

	/** 
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Override public boolean retainAll(final Collection<?> c) {
		list.retainAll(c);
		return set.retainAll(c);
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

	/**
	 * DOCUMENT ME!
	 * 
	 * @param object DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public int indexOf(final T object) {
		return list.indexOf(object);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param i DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public T get(final int i) {
		return list.get(i);
	}

}


// End of File