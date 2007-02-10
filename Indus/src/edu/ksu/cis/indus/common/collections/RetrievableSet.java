/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

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
	@Override public boolean add(@Immutable final E o) {
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
	@Functional @Override public boolean contains(final Object o) {
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
	@Functional public E get(final E o) {
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
	@Functional @NonNull @Override public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean remove(@Immutable final Object o) {
		final boolean _result = map.containsKey(o);

		if (_result) {
			map.remove(o);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public int size() {
		return map.size();
	}
}

// End of File
