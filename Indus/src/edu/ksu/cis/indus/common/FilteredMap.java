
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

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Predicate;


/**
 * This class provides a filtered access to the keys and values in the backed map.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class FilteredMap
  extends AbstractMap {
	/**
	 * The backed map.
	 */
	private final Map origMap;

	/**
	 * The predicate to filter keys.
	 */
	private final Predicate keyPred;

	/**
	 * The predicate to filter values.
	 */
	private final Predicate valuePred;

	/**
	 * Creates a new FilteredMap object.
	 *
	 * @param map to be filtered.
	 * @param keyPredicate to filter the keys in <code>map</code>.
	 * @param valuePredicate to filter the values in <code>map</code>.
	 */
	FilteredMap(final Map map, final Predicate keyPredicate, final Predicate valuePredicate) {
		origMap = map;
		keyPred = keyPredicate;
		valuePred = valuePredicate;
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet() {
		Set _result = new HashSet();

		for (final Iterator _i = origMap.keySet().iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if ((keyPred != null && keyPred.evaluate(_o)) || keyPred == null) {
				final Object _val = origMap.get(_o);

				if ((valuePred != null && valuePred.evaluate(_val)) || valuePred == null) {
					_result.add(new MapEntry(_o, _val));
				}
			}
		}

		if (_result.isEmpty()) {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
 */
