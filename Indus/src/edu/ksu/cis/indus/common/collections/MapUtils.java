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

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains static utility methods that are useful in the context of <code>java.util.Map</code> instances.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class MapUtils {

	// / CLOVER:OFF
	/**
	 * Creates an instance of this class.
	 */
	private MapUtils() {
		super();
	}

	// / CLOVER:ON

	/**
	 * Retrieves a collection mapped to the given key in the given map. If no mapping exists, a new mapping is created with a
	 * new collection serving as the value.
	 * 
	 * @param map from which the mapping is to be retrieved.
	 * @param key for which the mapping is to be retrieved.
	 * @param <K> is the type of the key to the map.
	 * @param <V> is the type of the objects stored in the value collection in the map.
	 * @param <K1> is the type of the key object.
	 * @return a collection.
	 */
	@NonNull @Immutable public static <K, V, K1 extends K> Collection<V> getCollectionFromMap(
			@NonNull final Map<K, Collection<V>> map, @Immutable final K1 key) {
		return getFromMapUsingFactory(map, key, SetUtils.<V> getFactory());
	}

	/**
	 * Retrieves a collection mapped to the given key in the given map. If no mapping exists, a new mapping is created with an
	 * unmodifiable empty collection serving as the value.
	 * 
	 * @param map from which the mapping is to be retrieved.
	 * @param key for which the mapping is to be retrieved.
	 * @param <K> is the type of the key to the map.
	 * @param <V> is the type of the objects stored in the value collection in the map.
	 * @param <K1> is the type of the key object.
	 * @return a collection.
	 */
	@NonNull @Immutable public static <K, V, K1 extends K> Collection<V> getEmptyCollectionFromMap(
			@NonNull final Map<K, Collection<V>> map, @Immutable final K1 key) {
		return getFromMap(map, key, Collections.<V> emptyList());
	}

	/**
	 * Retrieves a list mapped to the given key in the given map. If no mapping exists, a new mapping is created with an
	 * unmodifiable empty list serving as the value.
	 * 
	 * @param map from which the mapping is to be retrieved.
	 * @param key for which the mapping is to be retrieved.
	 * @param <K> is the type of the key to the map.
	 * @param <V> is the type of the objects stored in the value list in the map.
	 * @param <K1> is the type of the key object.
	 * @return a list.
	 */
	@NonNull @Immutable public static <K, V, K1 extends K> List<V> getEmptyListFromMap(@NonNull final Map<K, List<V>> map,
			@Immutable final K1 key) {
		return getFromMap(map, key, Collections.<V> emptyList());
	}

	/**
	 * Retrieves a map mapped to the given key in the given map. If no mapping exists, a new mapping is created with an
	 * unmodifiable empty map serving as the value.
	 * 
	 * @param map from which the mapping is to be retrieved.
	 * @param key for which the mapping is to be retrieved.
	 * @param <K> is the type of the key to the map.
	 * @param <K2> is the type of the key to the returned map.
	 * @param <V2> is the type of the objects stored in the returned map.
	 * @param <K1> is the type of the key object.
	 * @return a collection.
	 */
	@NonNull @Immutable public static <K, K2, V2, K1 extends K> Map<K2, V2> getEmptyMapFromMap(
			@NonNull final Map<K, Map<K2, V2>> map, @Immutable final K1 key) {
		return getFromMap(map, key, Collections.<K2, V2> emptyMap());
	}

	/**
	 * Retrieves a set mapped to the given key in the given map. If no mapping exists, a new mapping is created with an
	 * unmodifiable empty set serving as the value.
	 * 
	 * @param map from which the mapping is to be retrieved.
	 * @param key for which the mapping is to be retrieved.
	 * @param <K> is the type of the key to the map.
	 * @param <V> is the type of the objects stored in the value set in the map.
	 * @param <K1> is the type of the key object.
	 * @return a set.
	 */
	@NonNull @Immutable public static <K, V, K1 extends K> Set<V> getEmptySetFromMap(@NonNull final Map<K, Set<V>> map,
			@Immutable final K1 key) {
		return getFromMap(map, key, Collections.<V> emptySet());
	}

	/**
	 * Retrieves a map creating factory.
	 * 
	 * @param <K> is the type of the key in the map.
	 * @param <V> is the type of the value in the map.
	 * @return a map creating factory.
	 */
	@NonNull @Immutable public static <K, V> IFactory<Map<K, V>> getFactory() {
		return new IFactory<Map<K, V>>() {

			public Map<K, V> create() {
				return new HashMap<K, V>();
			}

		};
	}

	/**
	 * Retrieves the value mapped to the given key in the given map. If no mapping exists, a new mapping from the given key to
	 * the default value is injected into the map and the deffault value is returned.
	 * 
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 * @param defaultValue is the default value to be used if no mapping exists for <code>key</code> in <code>map</code>.
	 * @param <K> is the type of the key in the map.
	 * @param <V> is the type of the value in the map.
	 * @param <K1> is the type of the key object.
	 * @param <V1> is the type of the defautl value.
	 * @return an object.
	 * @post map$pre.get(key) = null implies map.get(key) = defaultValue
	 * @post map.get(key) = result
	 */
	@NonNull @Immutable public static <K, V, K1 extends K, V1 extends V> V getFromMap(@NonNull final Map<K, V> map,
			@Immutable final K1 key, @Immutable final V1 defaultValue) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		map.put(key, defaultValue);
		return defaultValue;
	}

	/**
	 * Retrieves the value for a key from the map. If the key has no mapping, a new mapping from the key to the given default
	 * value is inserted into the map.
	 * 
	 * @param <T1> The type of keys in the input map.
	 * @param <T2> The type of values in the input map.
	 * @param <T3> The type of the key.
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 * @param factory used to create the default value.
	 * @return the value mapped to <code>key</code>.
	 */
	@NonNull @Immutable public static <T1, T2, T3 extends T1> T2 getFromMapUsingFactory(@NonNull final Map<T1, T2> map,
			@NonNull @Immutable final T3 key, @NonNull @Immutable final IFactory<? extends T2> factory) {
		if (!map.containsKey(key)) {
			map.put(key, factory.create());
		}
		return map.get(key);
	}

	/**
	 * Retrieves a list mapped to the given key in the given map. If no mapping exists, a new mapping is created with a new
	 * collection serving as the value.
	 * 
	 * @param map from which the mapping is to be retrieved.
	 * @param key for which the mapping is to be retrieved.
	 * @param <K> is the type of the key to the map.
	 * @param <V> is the type of the objects stored in the value list in the map.
	 * @return a list.
	 */
	@NonNull @Immutable public static <K, V> List<V> getListFromMap(@NonNull final Map<K, List<V>> map,
			@NonNull @Immutable final K key) {
		return getFromMapUsingFactory(map, key, ListUtils.<V> getFactory());
	}

	/**
	 * Retrieves a map mapped to the given key in the given map. If no mapping exists, a new mapping is created with a new map
	 * serving as the value.
	 * 
	 * @param map from which the mapping is to be retrieved.
	 * @param key for which the mapping is to be retrieved.
	 * @param <K> is the type of the key of the input map.
	 * @param <K1> is the type of the key object.
	 * @param <K2> is the type of the key of the returned map.
	 * @param <V2> is the type of the objects stored in the value collection in the map.
	 * @return a map.
	 */
	@NonNull @Immutable public static <K, K1 extends K, K2, V2> Map<K2, V2> getMapFromMap(
			@NonNull final Map<K, Map<K2, V2>> map, @NonNull @Immutable final K1 key) {
		return getFromMapUsingFactory(map, key, MapUtils.<K2, V2> getFactory());
	}

	/**
	 * Retrieves a set mapped to the given key in the given map. If no mapping exists, a new mapping is created with a new set
	 * serving as the value.
	 * 
	 * @param map from which the mapping is to be retrieved.
	 * @param key for which the mapping is to be retrieved.
	 * @param <K> is the type of the key to the map.
	 * @param <V> is the type of the objects stored in the value set in the map.
	 * @return a set.
	 */
	@NonNull @Immutable public static <K, V> Set<V> getSetFromMap(@NonNull final Map<K, Set<V>> map,
			@NonNull @Immutable final K key) {
		return getFromMapUsingFactory(map, key, SetUtils.<V> getFactory());
	}

	/**
	 * Inverts the given map. In the returned map, each key is mapped to a collection. This is to address situation where
	 * multiple keys may map to the same value in the given map.
	 * 
	 * @param <K> the type of the keys in the given map.
	 * @param <V> the type of the elements in the given map.
	 * @param map to be inverted.
	 * @return the inverted map.
	 * @post map.values().containsAll(result.keySet())
	 * @post result.keySet().containsAll(map.values())
	 * @post result.values()->forall(o | map.keySet().containsAll(o))
	 * @post map.keySet()->forall(o | result.values()->exists(p | p.contains(o)))
	 */
	@NonNull @Immutable public static <K, V> Map<V, Set<K>> invertMap(@NonNull final Map<K, V> map) {
		final Map<V, Set<K>> _result = new HashMap<V, Set<K>>();
		final Collection<K> _values = map.keySet();
		final Iterator<K> _i = _values.iterator();
		final int _iEnd = _values.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final K _valueInResult = _i.next();
			final V _keyInResult = map.get(_valueInResult);
			if (!_result.containsKey(_keyInResult)) {
				final Set<K> _set = new HashSet<K>();
				_result.put(_keyInResult, _set);
			}
			final Collection<K> _t = _result.get(_keyInResult);
			_t.add(_valueInResult);
		}
		return _result;
	}

	/**
	 * Puts the given values into the collection mapped to the given key in the given map. If no mapping exists, then a new
	 * mapping is created from the key to a new collection.
	 * 
	 * @param <T1> is the type of the key in the map.
	 * @param <T3> is the type of the key object.
	 * @param <T2> is the type of the objects in the value collections in the map.
	 * @param <T4> is the type of objects to be injected.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the set mapped to <code>key</code>.
	 * @return <code>true</code> if the values were added; <code>false</code>, otherwise.
	 */
	@NonNull @Immutable public static <T1, T3 extends T1, T2, T4 extends T2> boolean putAllIntoCollectionInMap(
			@NonNull final Map<T1, Collection<T2>> map, @Immutable final T3 key,
			@NonNull @Immutable final Collection<T4> values) {
		return putAllIntoCollectionInMapUsingFactory(map, key, values, SetUtils.<T2> getFactory());
	}

	/**
	 * Puts all values in <code>values</code> into the value of the given key in the given map . If no collection exists
	 * against the given key, the given collection is installed as the value for the given key and the values are loaded into
	 * it.
	 * 
	 * @param <T1> is the type of the key in the map.
	 * @param <T3> is the type of the key object.
	 * @param <T2> is the type of the objects in the value collections in the map.
	 * @param <T4> is the type of objects to be injected.
	 * @param <T5> is the type of the value in the map.
	 * @param <T6> is the type of the object created by the factory.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the collection mapped to <code>key</code>.
	 * @param factory to be used to create the collection if there is no collection mapped to <code>key</code>.
	 * @return <code>true</code> if all values were added; <code>false</code>, otherwise.
	 * @pre map != null and key != null and values != null and factory != null
	 */
	@NonNull @Immutable public static <T1, T3 extends T1, T2, T4 extends T2, T5 extends Collection<T2>, T6 extends T5> boolean putAllIntoCollectionInMapUsingFactory(
			@NonNull final Map<T1, T5> map, @Immutable final T3 key, @NonNull @Immutable final Collection<T4> values,
			@NonNull @Immutable final IFactory<T6> factory) {
		T5 _temp = map.get(key);

		if (_temp == null) {
			_temp = factory.create();
			map.put(key, _temp);
		}
		return _temp.addAll(values);
	}

	/**
	 * Puts the given values into the list mapped to the given key in the given map. If no mapping exists, then a new mapping
	 * is created from the key to a new list.
	 * 
	 * @param <T1> is the type of the key in the map.
	 * @param <T3> is the type of the key object.
	 * @param <T2> is the type of the objects in the value lists in the map.
	 * @param <T4> is the type of objects to be injected.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the set mapped to <code>key</code>.
	 * @return <code>true</code> if the values were added; <code>false</code>, otherwise.
	 */
	@NonNull @Immutable public static <T1, T3 extends T1, T2, T4 extends T2> boolean putAllIntoListInMap(
			@NonNull final Map<T1, List<T2>> map, @Immutable final T3 key, @NonNull @Immutable final Collection<T4> values) {
		return putAllIntoCollectionInMapUsingFactory(map, key, values, ListUtils.<T2> getFactory());
	}

	/**
	 * Puts the given values into the set mapped to the given key in the given map. If no mapping exists, then a new mapping
	 * is created from the key to a new set.
	 * 
	 * @param <T1> is the type of the key in the map.
	 * @param <T3> is the type of the key object.
	 * @param <T2> is the type of the objects in the value sets in the map.
	 * @param <T4> is the type of objects to be injected.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the set mapped to <code>key</code>.
	 * @return <code>true</code> if the values were added; <code>false</code>, otherwise.
	 */
	@NonNull @Immutable public static <T1, T3 extends T1, T2, T4 extends T2> boolean putAllIntoSetInMap(
			@NonNull final Map<T1, Set<T2>> map, @Immutable final T3 key, @NonNull @Immutable final Collection<T4> values) {
		return putAllIntoCollectionInMapUsingFactory(map, key, values, SetUtils.<T2> getFactory());
	}

	/**
	 * Puts <code>value</code> into the collection value of the given key in the given map . If no list exists against the
	 * given key, then a new collection is created as the value for the given key and <code>value</code> is inserted into
	 * this collection.
	 * 
	 * @param <T1> is the type of the keys in the given map.
	 * @param <T2> is the type of the objects in value collection in the given map.
	 * @param <T3> is the type of the given key.
	 * @param <T4> is the type of the given value.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the list mapped to <code>key</code>.
	 * @return <code>true</code> if the value was added; <code>false</code>, otherwise.
	 */
	@NonNull @Immutable public static <T1, T2, T3 extends T1, T4 extends T2> boolean putIntoCollectionInMap(
			@NonNull final Map<T1, Collection<T2>> map, @Immutable final T3 key, @Immutable final T4 value) {
		return putIntoCollectionInMapUsingFactory(map, key, value, SetUtils.<T2> getFactory());
	}

	/**
	 * Puts <code>value</code> into the collection value of the given key in the given map . If no collection exists against
	 * the given key, then the collection created by the given factory is installed as the value for the given key and
	 * <code>value</code> is inserted into this collection.
	 * 
	 * @param <T1> is the type of the keys in the given map.
	 * @param <T2> is the type of the objects in value collection in the given map.
	 * @param <T3> is the type of the given key.
	 * @param <T4> is the type of the given value.
	 * @param <T5> is the type of the collection stored as values in the given map.
	 * @param <T6> is the type of objects created by the factory.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the collection mapped to <code>key</code>.
	 * @param factory to be used to create a collection if there is no collection mapped to <code>key</code>.
	 * @return <code>true</code> if the value was added; <code>false</code>, otherwise.
	 */
	@NonNull @Immutable public static <T1, T2, T3 extends T1, T4 extends T2, T5 extends Collection<T2>, T6 extends T5> boolean putIntoCollectionInMapUsingFactory(
			@NonNull final Map<T1, T5> map, @Immutable final T3 key, @Immutable final T4 value,
			@NonNull @Immutable final IFactory<T6> factory) {
		T5 _temp = map.get(key);

		if (_temp == null) {
			_temp = factory.create();
			map.put(key, _temp);
		}
		return _temp.add(value);
	}

	/**
	 * Puts <code>value</code> into the list value of the given key in the given map . If no list exists against the given
	 * key, then a new list is created as the value for the given key and <code>value</code> is inserted into this list.
	 * 
	 * @param <T1> is the type of the keys in the given map.
	 * @param <T2> is the type of the objects in value list in the given map.
	 * @param <T3> is the type of the given key.
	 * @param <T4> is the type of the given value.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the list mapped to <code>key</code>.
	 * @return <code>true</code> if the value was added; <code>false</code>, otherwise.
	 */
	@NonNull @Immutable public static <T1, T3 extends T1, T2, T4 extends T2> boolean putIntoListInMap(
			@NonNull final Map<T1, Collection<T2>> map, @Immutable final T3 key, @Immutable final T4 value) {
		return putIntoCollectionInMapUsingFactory(map, key, value, ListUtils.<T2> getFactory());
	}

	/**
	 * Puts <code>value</code> into the set value of the given key in the given map . If no set exists against the given
	 * key, then a new set is created as the value for the given key and <code>value</code> is inserted into this set.
	 * 
	 * @param <T1> is the type of the keys in the given map.
	 * @param <T2> is the type of the objects in value set in the given map.
	 * @param <T3> is the type of the given key.
	 * @param <T4> is the type of the given value.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the sett mapped to <code>key</code>.
	 * @return <code>true</code> if the value was added; <code>false</code>, otherwise.
	 */
	@NonNull @Immutable public static <T1, T3 extends T1, T2, T4 extends T2> boolean putIntoSetInMap(
			@NonNull final Map<T1, Collection<T2>> map, @Immutable final T3 key, @Immutable final T4 value) {
		return putIntoCollectionInMapUsingFactory(map, key, value, SetUtils.<T2> getFactory());
	}

	/**
	 * Retrieves the collection mapped to the given key in the given map. If no mapping exists, then an empty collection is
	 * returned but no mapping is inserted into the map.
	 * 
	 * @param <T3> is the type of the key object.
	 * @param <T2> is the type of objects stored in the returned collection.
	 * @param <T1> is the type of the key of the map.
	 * @param map to be read.
	 * @param key of interest.
	 * @return the value collection.
	 */
	@NonNull @Immutable public static <T1, T2, T3 extends T1> Collection<T2> queryCollection(
			@NonNull @Immutable final Map<T1, ? extends Collection<T2>> map, @Immutable final T3 key) {
		final Collection<T2> _value = map.get(key);
		if (_value != null) {
			return _value;
		}
		return Collections.<T2> emptySet();
	}

	/**
	 * Retrieves the list mapped to the given key in the given map. If no mapping exists, then an empty list is returned but
	 * no mapping is inserted into the map.
	 * 
	 * @param <T3> is the type of the key object.
	 * @param <T2> is the type of objects stored in the returned list.
	 * @param <T1> is the type of the key of the map.
	 * @param map to be read.
	 * @param key of interest.
	 * @return the list object.
	 */
	@NonNull @Immutable public static <T1, T2, T3 extends T1> List<T2> queryList(
			@NonNull @Immutable final Map<T1, ? extends List<T2>> map, @Immutable final T3 key) {
		final List<T2> _value = map.get(key);
		if (_value != null) {
			return _value;
		}
		return Collections.<T2> emptyList();
	}

	/**
	 * Retrieves the map mapped to the given key in the given map. If no mapping exists, then an empty is returned but no
	 * mapping is inserted into the map.
	 * 
	 * @param <T4> is the type of the key object.
	 * @param <T3> is the type of the value in the returned map.
	 * @param <T2> is the type of the key in the returned map.
	 * @param <T1> is the type of the key of the map.
	 * @param map to be read.
	 * @param key of interest.
	 * @return the map object.
	 */
	@NonNull @Immutable public static <T1, T2, T3, T4 extends T1> Map<T2, T3> queryMap(
			@NonNull @Immutable final Map<T1, ? extends Map<T2, T3>> map, @Immutable final T4 key) {
		final Map<T2, T3> _value = map.get(key);
		if (_value != null) {
			return _value;
		}
		return Collections.<T2, T3> emptyMap();
	}

	/**
	 * Retrieves the object mapped to the given key in the given map. If no mapping exists, then the default value is returned
	 * but no mapping is inserted into the map.
	 * 
	 * @param <T3> is the type of the key object.
	 * @param <T4> is the type of the default value object.
	 * @param <T2> is the type of the value of the map.
	 * @param <T1> is the type of the key of the map.
	 * @param map to be read.
	 * @param key of interest.
	 * @param defaultValue to be returned if no mapping exists.
	 * @return the object.
	 */
	@NonNull @Immutable public static <T1, T2, T3 extends T1, T4 extends T2> T2 queryObject(
			@NonNull @Immutable final Map<T1, T2> map, final T3 key, @Immutable final T4 defaultValue) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return defaultValue;
	}

	/**
	 * Retrieves the set mapped to the given key in the given map. If no mapping exists, then an empty set is returned but no
	 * mapping is inserted into the map.
	 * 
	 * @param <T3> is the type of the key object.
	 * @param <T2> is the type of objects stored in the returned set.
	 * @param <T1> is the type of the key of the map.
	 * @param map to be read.
	 * @param key of interest.
	 * @return the set object.
	 */
	@NonNull @Immutable public static <T1, T2, T3 extends T1> Set<T2> querySet(
			@NonNull @Immutable final Map<T1, ? extends Set<T2>> map, @Immutable final T3 key) {
		final Set<T2> _value = map.get(key);
		if (_value != null) {
			return _value;
		}
		return Collections.<T2> emptySet();
	}

	/**
	 * An utility method to print a map.
	 * 
	 * @param <T1> is the type of the key.
	 * @param <T2> is the type of the value.
	 * @param map to be printed.
	 * @return the printed representation of the map.
	 */
	@NonNull @Immutable public static <T1, T2> String verbosePrint(@NonNull @Immutable final Map<T1, T2> map) {
		final StringBuilder _sb = new StringBuilder();
		final char _newline = '\n';
		final char _indent = '\t';
		for (final T1 _k : map.keySet()) {
			final T2 _v = map.get(_k);
			_sb.append(verbosePrintHelper(_indent, _k, _v));
			_sb.append(_newline);
		}
		return _sb.toString();
	}

	/**
	 * A helper method for purposes of verbose printing for a map entry.
	 * 
	 * @param <T1> is the type of the key.
	 * @param <T2> is the type of the value.
	 * @param indent is the indentation character.
	 * @param key obviously.
	 * @param value obviously.
	 * @return a printed representation of the given key and value pair.
	 */
	@NonNull @Immutable private static <T1, T2> String verbosePrintHelper(final char indent, @Immutable final T1 key,
			@Immutable final T2 value) {
		final StringBuilder _sb = new StringBuilder();
		_sb.append(indent);
		_sb.append(key);
		_sb.append(" = ");

		if (value instanceof Map) {
			final Map<?, ?> _map = (Map) value;
			_sb.append(verbosePrint(_map));
		} else {
			_sb.append(String.valueOf(value));
		}
		_sb.append('\n');
		return _sb.toString();
	}
}

// End of File
