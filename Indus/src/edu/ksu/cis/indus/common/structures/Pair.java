
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

package edu.ksu.cis.indus.common.structures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class represents a pair of objects.  The hashcode/stringized rep. of this object is derived from it's constituents.
 * 
 * <p>
 * Instances of this class can occur in <i>optimized</i> or <i>unoptimized</i> modes.  In optimized mode, the
 * hashcode/stringized rep. are precalculated at creation time or on call to <code>optimize()</code>. Hence, any future
 * calls to <code>hashCode()</code> and <code>toString()</code> will return this cached copy.  In the unoptimized mode, the
 * hashcode/stringized rep. are calculated on the fly upon request.   It is possible to toggle an instance between optimized
 * and unoptimized mode.
 * </p>
 * 
 * <p>
 * The above feature of this class can lead to a situation where the hashcode of an instance obtained via
 * <code>hashCode()</code> in optimized mode is not equal to the hashcode of the instance if calculated on the fly.  This is
 * not a serious ramification as this will not affect the equality test of instances rather only the preformance of
 * container classes using these instances as keys.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class Pair
  implements Cloneable {
	/**
	 * The first element of this pair.
	 */
	protected Object first;

	/**
	 * The second element of this pair.
	 */
	protected Object second;

	/**
	 * A cached copy of the stringized representation of this object.
	 */
	private String str;

	/**
	 * A cached copy of the hash code of this object.
	 */
	private int hashCode;

	/**
	 * Creates a new Pair object.
	 *
	 * @param firstParam the first element of this pair.
	 * @param secondParam the second element of this pair.
	 * @param optimized <code>true</code> indicates that the stringized representation and the hashcode of this object should
	 * 		  be calculated and cached for the rest of it's lifetime. <code>false</code> indicates that these values shoudl
	 * 		  be calculated on the fly upon request.
	 *
	 * @post optimized == false implies str == null
	 * @post optimized == true implies str != null
	 */
	public Pair(final Object firstParam, final Object secondParam, final boolean optimized) {
		this.first = firstParam;
		this.second = secondParam;

		if (optimized) {
			optimize();
		}
	}

	/**
	 * Creates a new optimized Pair object.
	 *
	 * @param firstParam the first element of this pair.
	 * @param secondParam the second element of this pair.
	 *
	 * @post str != null
	 */
	public Pair(final Object firstParam, final Object secondParam) {
		this(firstParam, secondParam, true);
	}

	/**
	 * This class manages a collection of pairs.  This realizes the <i>flyweight</i> pattern for pairs.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public static final class PairManager {
		/**
		 * This is the id of this service.
		 */
		public static final String ID = "Pair management service";

		/**
		 * The collection of managed pairs.
		 */
		private final List pairs = new ArrayList();

		/**
		 * The scratch pad pair object to be used for does-it-manage check.
		 */
		private final Pair pair = new Pair(null, null, false);

		/**
		 * Provides an optimized pair containing 2 given objects in the given order.
		 *
		 * @param firstParam first element of the requested pair.
		 * @param secondParam second element of the requested pair.
		 *
		 * @return the optimized pair containing the given objects.
		 *
		 * @post result != null
		 */
		public Pair getOptimizedPair(final Object firstParam, final Object secondParam) {
			return getPair(firstParam, secondParam, true);
		}

		/**
		 * Provides an unoptimized pair containing 2 given objects in the given order.
		 *
		 * @param firstParam first element of the requested pair.
		 * @param secondParam second element of the requested pair.
		 *
		 * @return the unoptimized pair containing the given objects.
		 *
		 * @post result != null
		 */
		public Pair getUnOptimizedPair(final Object firstParam, final Object secondParam) {
			return getPair(firstParam, secondParam, false);
		}

		/**
		 * Forgets about all managed pairs.
		 */
		public void reset() {
			pairs.clear();
		}

		/**
		 * Provides a pair containing 2 given objects in the given order.
		 *
		 * @param firstParam first element of the requested pair.
		 * @param secondParam second element of the requested pair.
		 * @param optimized <code>true</code> indicates that the stringized representation and the hashcode of this object
		 * 		  should be calculated and cached for the rest of it's lifetime. <code>false</code> indicates that these
		 * 		  values should be calculated on the fly upon request.
		 *
		 * @return the pair containing the given objects.
		 *
		 * @post result != null
		 */
		private Pair getPair(final Object firstParam, final Object secondParam, final boolean optimized) {
			Pair _result;
			pair.first = firstParam;
			pair.second = secondParam;

			if (pairs.contains(pair)) {
				_result = (Pair) pairs.get(pairs.indexOf(pair));
			} else {
				_result = new Pair(firstParam, secondParam, optimized);
				pairs.add(0, _result);
			}
			return _result;
		}
	}

	/**
	 * Returns the first element in the pair.
	 *
	 * @return the first element in the pair.
	 */
	public Object getFirst() {
		return first;
	}

	/**
	 * Returns the second element in the pair.
	 *
	 * @return the second element in the pair.
	 */
	public Object getSecond() {
		return second;
	}

	/**
	 * Clones this pair using shallow-copy semantics.
	 *
	 * @return a cloned copy of this pair.
	 *
	 * @throws CloneNotSupportedException will not be thrown.
	 */
	public Object clone()
	  throws CloneNotSupportedException {
		return (Pair) super.clone();
	}

	/**
	 * Checks if the given object is equal to this pair.
	 *
	 * @param o is the object to be tested for equality with this pair.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this pair; <code>false</code>, otherwise.
	 *
	 * @post result == true implies o.oclTypeOf(Pair) and (o.first.equals(first) or o.first == first) and
	 * 		 (o.second.equals(second) or o.second == second)
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o != null && o instanceof Pair) {
			final Pair _temp = (Pair) o;

			_result = first == _temp.first || first.equals(_temp.first);

			if (_result) {
				_result = second == _temp.second || _result && second.equals(_temp.second);
			}
		} else {
			_result = super.equals(o);
		}
		return _result;
	}

	/**
	 * Returns the hash code for this pair. Depending on how the object was created the cached value or the value calculated
	 * on the fly is returned.
	 *
	 * @return the hash code of this pair.
	 */
	public int hashCode() {
		final int _result;

		if (str == null) {
			_result = hash();
		} else {
			_result = hashCode;
		}
		return _result;
	}

	/**
	 * Mapifies a given collection of pairs.
	 *
	 * @param pairs is a collection of <code>Pair</code> objects.
	 * @param forward <code>true</code> indicates that the map should map the first element to the second elements in the
	 * 		  pair; <code>false</code>, indicates the reverse direction.
	 *
	 * @return an object to collection map.
	 *
	 * @pre pairs != null and not pairs->includes(null)
	 * @pre pairs.oclIsKindOf(Collection(edu.ksu.cis.indus.common.graph.Pair))
	 * @post result.oclIsKindOf(Map(Object, Collection(Object)))
	 * @post result->entrySet()->forall(o | o.getValue()->forall(p | pairs->includes(Pair(o.getKey(), p))))
	 * @post pairs->forall(o | result.get(o.getFirst())->includes(o.getSecond()))
	 */
	public static Map mapify(final Collection pairs, final boolean forward) {
		Map _result = new HashMap();

		for (final Iterator _i = pairs.iterator(); _i.hasNext();) {
			final Pair _pair = (Pair) _i.next();
			Object _key;
			Object _value;

			if (forward) {
				_key = _pair.getFirst();
				_value = _pair.getSecond();
			} else {
				_key = _pair.getSecond();
				_value = _pair.getFirst();
			}

			Collection _c = (Collection) _result.get(_key);

			if (_c == null) {
				_c = new ArrayList();
				_result.put(_key, _c);
			}
			_c.add(_value);
		}

		if (_result.isEmpty()) {
			_result = Collections.EMPTY_MAP;
		}
		return _result;
	}

	/**
	 * Optimizes this object with regard to hashCode and stringized representation retrival.  It (re)calculates the hashcode
	 * and the stringized representation of this object and caches the new values.
	 *
	 * @post str != null
	 */
	public void optimize() {
		hashCode = hash();
		str = stringize();
	}

	/**
	 * Returns a stringified version of this object. Depending on how the object was created the cached value or the value
	 * calculated on the fly is returned.
	 *
	 * @return a stringified version of this object.
	 */
	public String toString() {
		String _result;

		if (str == null) {
			_result = stringize();
		} else {
			_result = str;
		}
		return _result;
	}

	/**
	 * Unoptimizes this object with regard to hashCode and stringized representation retrival.  It forgets any cached values
	 * so that they calculates on the fly when requested next.
	 *
	 * @post str == null
	 */
	public void unoptimize() {
		str = null;
	}

	/**
	 * Provides the hashcode of this object.  Subclasses may override this method to suit their needs.
	 *
	 * @return the hashcode of this object.
	 */
	protected int hash() {
		int _result = 17;

		if (first != null) {
			_result = 37 * _result + first.hashCode();
		}

		if (second != null) {
			_result = 37 * _result + second.hashCode();
		}
		return _result;
	}

	/**
	 * Provides the stringized representation of this object.  Subclasses may override this method to suit their needs.
	 *
	 * @return the stringized representation of this object.
	 */
	protected String stringize() {
		return "(" + first + ", " + second + ")";
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.9  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.8  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.7  2003/11/05 00:35:33  venku
   - even unoptimized version was returning optimized
     pair instance.  FIXED.
   Revision 1.6  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/09/14 22:53:54  venku
   - added method to mapify a collection of pairs.
   Revision 1.4  2003/08/11 08:12:26  venku
   Major changes in equals() method of Context, Pair, Marker, and Triple.
   Similar changes in hashCode()
   Spruced up Documentation and Specification.
   Formatted code.
   Revision 1.3  2003/08/11 07:13:58  venku
 *** empty log message ***
                   Revision 1.2  2003/08/11 04:20:19  venku
                   - Pair and Triple were changed to work in optimized and unoptimized mode.
                   - Ripple effect of the previous change.
                   - Documentation and specification of other classes.
                   Revision 1.1  2003/08/07 06:42:16  venku
                   Major:
                    - Moved the package under indus umbrella.
                    - Renamed isEmpty() to hasWork() in IWorkBag.
                   Revision 1.4  2003/05/22 22:18:31  venku
                   All the interfaces were renamed to start with an "I".
                   Optimizing changes related Strings were made.
 */
