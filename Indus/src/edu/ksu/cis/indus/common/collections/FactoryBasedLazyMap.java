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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * A factory based lazy map that uses the factory to creates values when there is no mapping for a given key.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <K> is the type of the key.
 * @param <V> is the type of the value.
 */
public class FactoryBasedLazyMap<K, V>
		extends AbstractMap<K, V> {

	/**
	 * The map that actually stores the mapping.
	 */
	@NonNull private final Map<K, V> backedMap;

	/**
	 * The factory used to create values.
	 */
	@NonNull private final IFactory<V> factory;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param map to be used to store the actual mappings.
	 * @param valueFactory to be used to create values.
	 */
	public FactoryBasedLazyMap(@NonNull @Immutable final Map<K, V> map, @NonNull @Immutable final IFactory<V> valueFactory) {
		backedMap = map;
		factory = valueFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public boolean containsKey(final Object key) {
		return get(key) != null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override @NonNull public Set<Map.Entry<K, V>> entrySet() {
		return backedMap.entrySet();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public boolean equals(final Object o) {
		return backedMap.equals(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public V get(final Object key) {
		if (!backedMap.containsKey(key)) {
			final V _c = factory.create();
			@SuppressWarnings("unchecked") final K _k = (K) key;
			backedMap.put(_k, _c);
			return _c;
		}
		return backedMap.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public int hashCode() {
		return backedMap.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public V put(@Immutable final K key, @Immutable final V value) {
		return backedMap.put(key, value);
	}
}

// End of File
