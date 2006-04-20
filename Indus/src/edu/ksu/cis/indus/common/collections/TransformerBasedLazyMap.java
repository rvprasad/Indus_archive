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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This is a lazy map that stores mappings from a key to its transformed value as determined by the transformer provided
 * during creation.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <K> is the type of keys.
 * @param <V> is the type of values.
 */
public class TransformerBasedLazyMap<K, V>
		implements Map<K, V> {

	/**
	 * This stores the mappings.
	 */
	@NonNull private final Map<K, V> backedMap;

	/**
	 * This is used to transform the keys into values.
	 */
	@NonNull private final ITransformer<K, V> transformer;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param map to be used internally to store the mapping.
	 * @param valueTransformer to be used to transform the keys into values.
	 */
	public TransformerBasedLazyMap(@NonNull final Map<K, V> map,
			@NonNull @Immutable final ITransformer<K, V> valueTransformer) {
		backedMap = map;
		transformer = valueTransformer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		backedMap.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public boolean containsKey(@SuppressWarnings("unused") final Object key) {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public boolean containsValue(final Object value) {
		return backedMap.containsValue(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public Set<Map.Entry<K, V>> entrySet() {
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
	public V get(@Immutable final Object key) {
		@SuppressWarnings("unchecked") final K _k = (K) key;
		if (!backedMap.containsKey(key)) {
			final V _t = transformer.transform(_k);
			backedMap.put(_k, _t);
			return _t;
		}
		return backedMap.get(_k);
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
	@Functional public boolean isEmpty() {
		return backedMap.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public Set<K> keySet() {
		return backedMap.keySet();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Functional public V put(@SuppressWarnings("unused") final K key, @SuppressWarnings("unused") final V value)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @throws UnsupportedOperationException always.
	 */
	@Functional public void putAll(@SuppressWarnings("unused") final Map<? extends K, ? extends V> t)
			throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	public V remove(@Immutable final Object key) {
		return backedMap.remove(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public int size() {
		return backedMap.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public Collection<V> values() {
		return backedMap.values();
	}
}

// End of File
