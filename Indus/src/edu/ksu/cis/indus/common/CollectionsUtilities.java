
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

package edu.ksu.cis.indus.common;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.Predicate;


/**
 * This a utility class with a collection of methods that encapsulates common logic pertaining to use of collections in
 * Indus.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CollectionsUtilities {
	/** 
	 * A factory to create <code>java.util.ArrayList</code>.
	 */
	public static final Factory ARRAY_LIST_FACTORY =
		new Factory() {
			public Object create() {
				return new ArrayList();
			}
		};

	/** 
	 * A factory to create <code>Collections.EMPTY_LIST</code>.
	 */
	public static final Factory EMPTY_LIST_FACTORY =
		new Factory() {
			public Object create() {
				return Collections.EMPTY_LIST;
			}
		};

	/** 
	 * A factory to create <code>Collections.EMPTY_SET</code>.
	 */
	public static final Factory EMPTY_SET_FACTORY =
		new Factory() {
			public Object create() {
				return Collections.EMPTY_SET;
			}
		};

	/** 
	 * A factory to create <code>Collections.EMPTY_MAP</code>.
	 */
	public static final Factory EMPTY_MAP_FACTORY =
		new Factory() {
			public Object create() {
				return Collections.EMPTY_MAP;
			}
		};

	/** 
	 * A factory to create <code>java.util.HashMap</code>.
	 */
	public static final Factory HASH_MAP_FACTORY =
		new Factory() {
			public Object create() {
				return new HashMap();
			}
		};

	/** 
	 * A factory to create <code>java.util.HashSet</code>.
	 */
	public static final Factory HASH_SET_FACTORY =
		new Factory() {
			public Object create() {
				return new HashSet();
			}
		};

	/** 
	 * A factory to create <code>java.util.BitSet</code>.
	 */
	public static final Factory BIT_SET_FACTORY =
		new Factory() {
			public Object create() {
				return new BitSet();
			}
		};

	///CLOVER:OFF

	/**
	 * Creates a new CollectionsUtilities object.
	 */
	private CollectionsUtilities() {
	}

	///CLOVER:ON

	/**
	 * Retrieves an element at the given index in the list.  If the value at the given index is <code>null</code> then the
	 * null value is replaced by the <code>defaultValue</code> and the same is returned. If the index does not occur in the
	 * list (list is short) then the list is extended with null values, <code>defaultValue</code> is added at the
	 * appropriate index, and the same is  returned.
	 *
	 * @param list from which to retrieve the value.
	 * @param index in <code>list</code> from which to retrive the value.
	 * @param factory provides the default value to be injected and returned if none exists or if <code>null</code> exists.
	 *
	 * @return the value at <code>index</code> in <code>list</code>.
	 *
	 * @throws IndexOutOfBoundsException when <code>index</code> is less than 0.
	 *
	 * @pre list != null and index != null and defaultValue != null
	 * @post list$pre.get(index) = null or list$pre.size() &lt; index implies result = defaultValue
	 * @post list.get(index) = result
	 */
	public static Object getAtIndexFromList(final List list, final int index, final Factory factory) {
		if (index < 0) {
			throw new IndexOutOfBoundsException("invalid index: " + index + " < 0");
		}

		if (index > list.size()) {
			ensureSize(list, index, null);
			list.set(index, factory.create());
		} else if (list.get(index) == null) {
			list.set(index, factory.create());
		}
		return list.get(index);
	}

	/**
	 * Retrieves a filtered map backed by the given map.
	 *
	 * @param map to be filtered.
	 * @param keyPredicate to filter the keys.
	 * @param valuePredicate to filter the values.
	 *
	 * @return a filtered map.
	 *
	 * @pre map != null and keyPredicate != null and valuePredicate != null
	 * @post result != null
	 * @post result.entrySet()->forall(o | (keyPredicate == null or keyPredicate.evaluate(o.getKey())) and (valuePredicate ==
	 * 		 null or valuePredicate.evaluate(o.getValue())))
	 */
	public static Map getFilteredMap(final Map map, final Predicate keyPredicate, final Predicate valuePredicate) {
		return new FilteredMap(map, keyPredicate, valuePredicate);
	}

	/**
	 * Retrieves the value for a key from the map.  If the key has no mapping, a new mapping from the key to the given
	 * default value is inserted into the map.
	 *
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 * @param factory used to create the default value.
	 *
	 * @return the value mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and factory != null
	 * @post map.get(key) != null
	 * @post result != null
	 */
	public static Object getFromMap(final Map map, final Object key, final Factory factory) {
		if (!map.containsKey(key)) {
			map.put(key, factory.create());
		}
		return map.get(key);
	}

	/**
	 * <code>getAtIndexFromListMap</code> version specialized for cases when the stored value is of type
	 * <code>java.util.List</code>.
	 *
	 * @param list from which to retrieve the value.
	 * @param index in <code>list</code> from which to retrive the value.
	 *
	 * @return the value at <code>index</code> in <code>list</code>.
	 *
	 * @pre list != null and index != null and defaultValue != null
	 * @post result != null
	 * @post list$pre.get(index) = null or list$pre.size() &lt; index implies result.oclIsKindOf(java.util.List)
	 * @post list.get(index) = result
	 *
	 * @see #getAtIndexFromList(List, int, Factory)
	 */
	public static Object getListAtIndexFromList(final List list, final int index) {
		return getAtIndexFromList(list, index, ARRAY_LIST_FACTORY);
	}

	/**
	 * <code>getFromMap</code> version specialized for cases when the value is of type <code>java.util.List</code>.
	 *
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 *
	 * @return the value mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null
	 * @post map.get(key) != null
	 * @post result != null
	 */
	public static List getListFromMap(final Map map, final Object key) {
		return (List) getFromMap(map, key, ARRAY_LIST_FACTORY);
	}

	/**
	 * <code>getFromMap</code> version specialized for cases when the value is of type <code>java.util.Map</code>.
	 *
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 *
	 * @return the value mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null
	 * @post map.get(key) != null
	 * @post result != null
	 */
	public static Map getMapFromMap(final Map map, final Object key) {
		return (Map) getFromMap(map, key, HASH_MAP_FACTORY);
	}

	/**
	 * <code>getAtIndexFromListMap</code> version specialized for cases when the stored value is of type
	 * <code>java.util.Set</code>.
	 *
	 * @param list from which to retrieve the value.
	 * @param index in <code>list</code> from which to retrive the value.
	 *
	 * @return the value at <code>index</code> in <code>list</code>.
	 *
	 * @pre list != null and index != null and defaultValue != null
	 * @post result != null
	 * @post list$pre.get(index) = null or list$pre.size() &lt; index implies result.oclIsKindOf(java.util.Set)
	 * @post list.get(index) = result
	 *
	 * @see #getAtIndexFromList(List, int, Factory)
	 */
	public static Object getSetAtIndexFromList(final List list, final int index) {
		return getAtIndexFromList(list, index, HASH_SET_FACTORY);
	}

	/**
	 * <code>getFromMap</code> version specialized for cases when the value is of type <code>java.util.Set</code>.
	 *
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 *
	 * @return the value mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null
	 * @post map.get(key) != null
	 * @post result != null
	 */
	public static Set getSetFromMap(final Map map, final Object key) {
		return (Set) getFromMap(map, key, HASH_SET_FACTORY);
	}

	/**
	 * Ensures that <code>list.size()</code> returns <code>finalSize</code>. This is achieved by appending
	 * <code>defaultValue</code> appropriate number of times to <code>list</code>.
	 *
	 * @param list to be modified.
	 * @param finalSize of <code>list</code>
	 * @param defaultValue to be injected into the list, if required, to ensure it's size.
	 *
	 * @pre list != null
	 * @post list.size() = finalSize
	 * @post list$pre.size() &lt; finalSize implies list.contains(defaultValue)
	 * @post list$pre.size() &gt;= finalSize implies list$pre.equals(list)
	 */
	public static void ensureSize(final List list, final int finalSize, final Object defaultValue) {
		final int _size = list.size();

		if (finalSize > _size) {
			for (int _i = finalSize - _size; _i > 0; _i--) {
				list.add(defaultValue);
			}
		}
	}

	/**
	 * Inverts the given map.  In the returned map, each key is mapped to a collection.  This is to address situation where
	 * multiple keys may map to the same value in the given map.
	 *
	 * @param map to be inverted.
	 *
	 * @return the inverted map.
	 *
	 * @pre map != null
	 * @post result != null
	 * @post map.values().containsAll(result.keySet())
	 * @post result.keySet().containsAll(map.values())
	 * @post result.values()->forall(o | map.keySet().containsAll(o))
	 * @post map.keySet()->forall(o | result.values()->exists(p | p.contains(o)))
	 */
	public static Map invertMap(final Map map) {
		final Map _result = new HashMap();
		final Collection _values = map.keySet();
		final Iterator _i = _values.iterator();
		final int _iEnd = _values.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Object _key = _i.next();
			CollectionsUtilities.getListFromMap(_result, map.get(_key)).add(_key);
		}
		return _result;
	}

	/**
	 * Returns a pretty print representation of the given collection.
	 *
	 * @param collection to be pretty printed.
	 *
	 * @return pretty print representation.
	 *
	 * @pre collection != null
	 * @post result != null
	 */
	public static String prettyPrint(final Collection collection) {
		final StringBuffer _sb = new StringBuffer();
		_sb.append("-----------------------Collection: " + collection.getClass().getName() + " / " + collection.hashCode()
			+ " [" + collection.size() + "]");

		final List _t = new ArrayList(collection);
		Collections.sort(_t);
		final Iterator _i = _t.iterator();
		final int _iEnd = _t.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Object _element = _i.next();
			_sb.append("\n");
			_sb.append(_element.toString());
		}
		_sb.append("\n=====================================================");
		return _sb.toString();
	}

	/**
	 * Returns a pretty print representation of the given map.
	 *
	 * @param map to be pretty printed.
	 *
	 * @return pretty print representation.
	 *
	 * @pre map != null
	 * @post result != null
	 */
	public static String prettyPrint(final Map map) {
		final StringBuffer _sb = new StringBuffer();
		_sb.append("-----------------------Collection: " + map.getClass().getName() + " / " + map.hashCode() + " ["
			+ map.size() + "]");

		final Iterator _i = map.entrySet().iterator();
		final int _iEnd = map.entrySet().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_sb.append("\n");
			_sb.append(_entry.getKey() + " --> " + _entry.getValue());
		}
		_sb.append("\n=====================================================");
		return _sb.toString();
	}

	/**
	 * Puts all values in <code>values</code> into the value of the given key in the given map .  If no collection exists
	 * against the  given key, the given collection is installed as the value for the given key and the values are loaded
	 * into it.
	 *
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the collection mapped to <code>key</code>.
	 * @param factory to be used to create the collection if there is no collection mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and values != null and factory != null
	 * @pre map.oclIsKindOf(Map(Object, Collection))
	 */
	public static void putAllIntoCollectionInMap(final Map map, final Object key, final Collection values,
		final Factory factory) {
		Collection _temp = (Collection) map.get(key);

		if (_temp == null) {
			_temp = (Collection) factory.create();
			map.put(key, _temp);
		}
		_temp.addAll(values);
	}

	/**
	 * <code>putAllIntoCollectionInMap</code> version specialized for cases when the value is of type
	 * <code>java.util.List</code>.
	 *
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the collection mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and values != null
	 * @pre map.oclIsKindOf(Map(Object, Collection))
	 *
	 * @see #putAllIntoCollectionInMap(Map,Object,Collection,Factory)
	 */
	public static void putAllIntoListInMap(final Map map, final Object key, final Collection values) {
		putAllIntoCollectionInMap(map, key, values, ARRAY_LIST_FACTORY);
	}

	/**
	 * <code>putAllIntoCollectionInMap</code> version specialized for cases when the value is of type
	 * <code>java.util.Set</code>.
	 *
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the collection mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and values != null
	 * @pre map.oclIsKindOf(Map(Object, Collection))
	 *
	 * @see #putAllIntoCollectionInMap(Map,Object,Collection,Factory)
	 */
	public static void putAllIntoSetInMap(final Map map, final Object key, final Collection values) {
		putAllIntoCollectionInMap(map, key, values, HASH_SET_FACTORY);
	}

	/**
	 * Puts <code>value</code> into the value of the given key in the given map .  If no collection exists against the  given
	 * key, the given collection is installed as the value for the given key and <code>value</code> is inserted into it.
	 *
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the collection mapped to <code>key</code>.
	 * @param factory to be used to create a collection if there is no collection mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and values != null and factory != null
	 * @pre map.oclIsKindOf(Map(Object, Collection))
	 */
	public static void putIntoCollectionInMap(final Map map, final Object key, final Object value, final Factory factory) {
		Collection _temp = (Collection) map.get(key);

		if (_temp == null) {
			_temp = (Collection) factory.create();
			map.put(key, _temp);
		}
		_temp.add(value);
	}

	/**
	 * <code>putAllIntoCollectionInMap</code> version specialized for cases when the value is of type
	 * <code>java.util.List</code>.
	 *
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the collection mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and values != null
	 * @pre map.oclIsKindOf(Map(Object, Collection))
	 */
	public static void putIntoListInMap(final Map map, final Object key, final Object value) {
		putIntoCollectionInMap(map, key, value, ARRAY_LIST_FACTORY);
	}

	/**
	 * <code>putAllIntoCollectionInMap</code> version specialized for cases when the value is of type
	 * <code>java.util.Set</code>.
	 *
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the collection mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and values != null
	 * @pre map.oclIsKindOf(Map(Object, Collection))
	 */
	public static void putIntoSetInMap(final Map map, final Object key, final Object value) {
		putIntoCollectionInMap(map, key, value, HASH_SET_FACTORY);
	}
}

// End of File
