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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * DOCUMENT ME!
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <K> DOCUMENT ME!
 * @param <V> DOCUMENT ME!
 */
public final class Cache<K, V>
		extends LinkedHashMap<K, V> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4347239164041878764L;

	/**
	 * DOCUMENT ME!
	 */
	private final int maximumCapacity;

	/**
	 * @see java.util.LinkedHashMap#removeEldestEntry(java.util.Map.Entry)
	 */
	@Override protected boolean removeEldestEntry(@SuppressWarnings("unused") final Entry<K, V> eldest) {
		return size() > maximumCapacity;
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param loadFactor DOCUMENT ME!
	 * @param maxCapacity DOCUMENT ME!
	 */
	public Cache(final float loadFactor, final int maxCapacity) {
		super(maxCapacity, loadFactor);
		maximumCapacity = maxCapacity;
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param maxCapacity DOCUMENT ME!
	 */
	public Cache(final int maxCapacity) {
		super(maxCapacity);
		maximumCapacity = maxCapacity;
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param m DOCUMENT ME!
	 * @param maxCapacity DOCUMENT ME!
	 */
	public Cache(final Map<K, V> m, final int maxCapacity) {
		this(maxCapacity);
		putAll(m);
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param initialCapacity DOCUMENT ME!
	 * @param loadFactor DOCUMENT ME!
	 * @param accessOrder DOCUMENT ME!
	 * @param maxCapacity DOCUMENT ME!
	 */
	public Cache(final float loadFactor, final boolean accessOrder, final int maxCapacity) {
		super(maxCapacity, loadFactor, accessOrder);
		maximumCapacity = maxCapacity;
	}

}

// End of File
