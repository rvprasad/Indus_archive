
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	///CLOVER:OFF

	/**
	 * Creates a new CollectionsUtilities object.
	 */
	private CollectionsUtilities() {
	}

	///CLOVER:ON

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
	 * @param defaultValue is the default value.
	 *
	 * @return the value mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and value != null
	 * @post map.get(key) != null
	 * @post result != null
	 */
	public static Object getFromMap(final Map map, final Object key, final Object defaultValue) {
		Object _return = map.get(key);

		if (_return == null) {
			_return = defaultValue;
			map.put(key, defaultValue);
		}
		return _return;
	}

	/**
	 * <code>getFromMap</code> version specialized for cases when the value is of type <code>java.util.List</code>.
	 *
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 *
	 * @return the value mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and value != null
	 * @post map.get(key) != null
	 * @post result != null
	 *
	 * @see #getFromMap(Map,Object,Object);
	 */
	public static List getListFromMap(final Map map, final Object key) {
		return (List) getFromMap(map, key, new ArrayList());
	}

	/**
	 * <code>getFromMap</code> version specialized for cases when the value is of type <code>java.util.Set</code>.
	 *
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 *
	 * @return the value mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and value != null
	 * @post map.get(key) != null
	 * @post result != null
	 *
	 * @see #getFromMap(Map,Object,Object);
	 */
	public static Set getSetFromMap(final Map map, final Object key) {
		return (Set) getFromMap(map, key, new HashSet());
	}

	/**
	 * Puts all values in <code>values</code> into the value of the given key in the given map .  If no collection exists
	 * against the  given key, the given collection is installed as the value for the given key and the values are loaded
	 * into it.
	 *
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the collection mapped to <code>key</code>.
	 * @param collection to be used if there is no collection mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and values != null and collection != null
	 * @pre map.oclIsKindOf(Map(Object, Collection))
	 */
	public static void putAllIntoCollectionInMap(final Map map, final Object key, final Collection values,
		final Collection collection) {
		Collection _temp = (Collection) map.get(key);

		if (_temp == null) {
			_temp = collection;
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
	 * @see #putAllIntoCollectionInMap(Map,Object,Collection,Collection);
	 */
	public static void putAllIntoListInMap(final Map map, final Object key, final Collection values) {
		putAllIntoCollectionInMap(map, key, values, new ArrayList());
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
	 * @see #putAllIntoCollectionInMap(Map,Object,Collection,Collection);
	 */
	public static void putAllIntoSetInMap(final Map map, final Object key, final Collection values) {
		putAllIntoCollectionInMap(map, key, values, new HashSet());
	}

	/**
	 * Puts <code>value</code> into the value of the given key in the given map .  If no collection exists against the  given
	 * key, the given collection is installed as the value for the given key and <code>value</code> is inserted into it.
	 *
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the collection mapped to <code>key</code>.
	 * @param col to be used if there is no collection mapped to <code>key</code>.
	 *
	 * @pre map != null and key != null and values != null and col != null
	 * @pre map.oclIsKindOf(Map(Object, Collection))
	 */
	public static void putIntoCollectionInMap(final Map map, final Object key, final Object value, final Collection col) {
		Collection _temp = (Collection) map.get(key);

		if (_temp == null) {
			_temp = col;
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
		putIntoCollectionInMap(map, key, value, new ArrayList());
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
		putIntoCollectionInMap(map, key, value, new HashSet());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/03/04 11:56:07  venku
   - added a new method to do safe and destructive queries on map.
   Revision 1.4  2004/01/28 22:45:42  venku
   - added clover source code directives.
   Revision 1.3  2004/01/25 08:57:24  venku
   - coding convention.
   Revision 1.2  2004/01/22 00:53:32  venku
   - formatting and coding convention.
   Revision 1.1  2004/01/21 13:41:49  venku
    - a new class to provide methods with common collection operations.
 */
