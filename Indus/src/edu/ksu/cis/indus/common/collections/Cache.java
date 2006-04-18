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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a least-recently-accessed algorithm based cache implementation.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <K> the type of the key in this cache.
 * @param <V> the type of the value in this cache.
 */
public final class Cache<K, V>
		extends LinkedHashMap<K, V> {

	/**
	 * The serial version UID.
	 */
	private static final long serialVersionUID = 4347239164041878764L;

	/**
	 * The default load factor to be used when none is specified. This is taken from the super class
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The number of elements this cache will hold.
	 */
	private final int maximumCapacity;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param loadFactor <i>see the documentation in the super class.</i>
	 * @param maxCapacity <i>see the documentation in the super class.</i>
	 */
	public Cache(final int maxCapacity, final float loadFactor) {
		super(maxCapacity, loadFactor, true);
		maximumCapacity = maxCapacity;
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param maxCapacity <i>see the documentation in the super class.</i>
	 */
	public Cache(final int maxCapacity) {
		super(maxCapacity, DEFAULT_LOAD_FACTOR, true);
		maximumCapacity = maxCapacity;
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param m is the map whose mappings should be injected into this map.
	 * @param maxCapacity is the threshold on the size of this cache.
	 */
	public Cache(@NonNull @Immutable final Map<K, V> m, final int maxCapacity) {
		this(maxCapacity);
		putAll(m);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override protected boolean removeEldestEntry(@SuppressWarnings("unused") final Map.Entry<K, V> eldest) {
		return size() > maximumCapacity;
	}

}

// End of File
