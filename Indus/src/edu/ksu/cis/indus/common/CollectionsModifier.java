
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

import java.util.Collection;
import java.util.Map;


/**
 * This a utility class with a collection of methods that encapsulate common logic used to update collections in Indus.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CollectionsModifier {
	/**
	 * Creates a new CollectionsModifier object.
	 */
	private CollectionsModifier() {
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
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/01/22 00:53:32  venku
   - formatting and coding convention.
   Revision 1.1  2004/01/21 13:41:49  venku
    - a new class to provide methods with common collection operations.
 */
