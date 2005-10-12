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
 * @param <K>
 * @param <V>
 */
public class FactoryBasedLazyMap<K, V>
		implements Map<K, V> {

	/**
	 * DOCUMENT ME!
	 */
	private final Map<K, V> backedMap;

	/**
	 * DOCUMENT ME!
	 */
	private final IFactory<V> factory;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param map DOCUMENT ME!
	 * @param valueFactory DOCUMENT ME!
	 */
	public FactoryBasedLazyMap(final Map<K, V> map, final IFactory<V> valueFactory) {
		backedMap = map;
		factory = valueFactory;
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
		return get(key) != null;
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
	public boolean equals(final Object o) {
		return backedMap.equals(o);
	}

	/**
	 * @see java.util.AbstractMap#get(java.lang.Object)
	 */
	public V get(final Object key) {
		if (!backedMap.containsKey(key)) {
			final V _c = factory.create();
			backedMap.put((K) key, _c);
			return _c;
		}
		return backedMap.get(key);
	}

	/**
	 * @see java.util.Map#hashCode()
	 */
	public int hashCode() {
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
		return backedMap.put(key, value);
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(final Map<? extends K, ? extends V> t) {
		backedMap.putAll(t);
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
