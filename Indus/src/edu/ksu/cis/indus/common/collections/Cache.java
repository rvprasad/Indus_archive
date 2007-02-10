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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is a least-recently-accessed algorithm based cache implementation.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
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
