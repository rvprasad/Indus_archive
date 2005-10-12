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

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <K> DOCUMENT ME!
 * @param <V> DOCUMENT ME!
 */
public class TransformerBasedLazyMap<K, V>
		implements Map<K, V> {

	/**
	 * DOCUMENT ME!
	 */
	private final Map<K, V> backedMap;

	/**
	 * DOCUMENT ME!
	 */
	private final ITransformer<K, V> transformer;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param map DOCUMENT ME!
	 * @param valueTransformer DOCUMENT ME!
	 */
	public TransformerBasedLazyMap(final Map<K, V> map, final ITransformer<K, V> valueTransformer) {
		backedMap = map;
		transformer = valueTransformer;
	}

	/**
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		backedMap.clear();
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(final Object key) {
		return true;
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(final Object value) {
		return backedMap.containsValue(value);
	}

	/**
	 * @see java.util.AbstractMap#entrySet()
	 */
	public Set<Map.Entry<K, V>> entrySet() {
		return backedMap.entrySet();
	}

	/**
	 * @see java.util.Map#equals(java.lang.Object)
	 */
	@Override public boolean equals(final Object o) {
		return backedMap.equals(o);
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public V get(final Object key) {
		final K _k = (K) key;
		if (!backedMap.containsKey(key)) {
			final V _t = transformer.transform(_k);
			backedMap.put(_k, _t);
			return _t;
		}
		return backedMap.get(_k);
	}

	/**
	 * @see java.util.Map#hashCode()
	 */
	@Override public int hashCode() {
		return backedMap.hashCode();
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		return backedMap.isEmpty();
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	public Set<K> keySet() {
		return backedMap.keySet();
	}

	/**
	 * @see java.util.Map#put(K, V)
	 */
	public V put(final K key, final V value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends K, ? extends V> t) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public V remove(final Object key) {
		return backedMap.remove(key);
	}

	/**
	 * @see java.util.Map#size()
	 */
	public int size() {
		return backedMap.size();
	}

	/**
	 * @see java.util.Map#values()
	 */
	public Collection<V> values() {
		return backedMap.values();
	}
}

// End of File
