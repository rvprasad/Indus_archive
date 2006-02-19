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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This is a simple implementation of <code>java.util.Set</code> that supports equality-based element retrieval. The user
 * can provide an element to <code>get(Object)</code> and retrieve the element in the set that is equal to the given
 * element. <code>add(), clear(), contains(), </code> and <code>remove()</code> are optimized based on the internal data
 * structures.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <E> is type of objects stored in this set.
 */
public final class RetrievableSet<E>
		extends AbstractSet<E> {

	/**
	 * This contains the elements in the set.
	 * 
	 * @invariant map->entrySet()->forall(o | o.getKey() == o.getValue())
	 */
	private final Map<E, E> map = new HashMap<E, E>();

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean add(final E o) {
		final boolean _result = !map.containsKey(o);

		if (_result) {
			map.put(o, o);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void clear() {
		map.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean contains(final Object o) {
		return map.containsKey(o);
	}

	/**
	 * Retrieves the element in this set that is equal to <code>o</code>.
	 * 
	 * @param o is the element used to select the element from this set.
	 * @return the element equal to <code>o</code>.
	 * @throws NoSuchElementException when no element in this set is equal to <code>o</code>.
	 * @pre o != null
	 * @post result != null and result.equals(o)
	 */
	public E get(final E o) {
		final E _result;

		if (map.containsKey(o)) {
			_result = map.get(o);
		} else {
			throw new NoSuchElementException(o + " does not occur in this set.");
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean remove(final Object o) {
		final boolean _result = map.containsKey(o);

		if (_result) {
			map.remove(o);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public int size() {
		return map.size();
	}
}

// End of File
