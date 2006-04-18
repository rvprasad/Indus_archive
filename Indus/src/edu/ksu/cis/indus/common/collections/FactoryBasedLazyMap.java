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

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * A factory based lazy map that uses the factory to creates values when there is no mapping for a given key.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
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
}

// End of File
