
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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
public final class Pair {
	/** 
	 * The first element of this pair.
	 */
	protected final Object first;

	/** 
	 * The second element of this pair.
	 */
	protected final Object second;

	/** 
	 * A cached copy of the stringized representation of this object.
	 */
	private String str;

	/** 
	 * This indicates if this pair is optimized for hash code calls.
	 */
	private boolean hashOptimized;

	/** 
	 * A cached copy of the hash code of this object.
	 */
	private int hashCode;

	/**
	 * Creates a new Pair object.
	 *
	 * @param firstParam the first element of this pair.
	 * @param secondParam the second element of this pair.
	 */
	public Pair(final Object firstParam, final Object secondParam) {
		this.first = firstParam;
		this.second = secondParam;
	}

	/**
	 * Creates a new Pair object.
	 *
	 * @param firstParam the first element of this pair.
	 * @param secondParam the second element of this pair.
	 * @param hashcodeOptimized <code>true</code> indicates that the the hashcode of this object should be calculated and
	 * 		  cached for the rest of it's lifetime. <code>false</code> indicates that this value should be calculated on the
	 * 		  fly upon request.
	 * @param toStringOptimized <code>true</code> indicates that the the hashcode of this object should be calculated and
	 * 		  cached for the rest of it's lifetime. <code>false</code> indicates that this value should be calculated on the
	 * 		  fly upon request.
	 *
	 * @post optimized == false implies str == null
	 * @post optimized == true implies str != null
	 */
	public Pair(final Object firstParam, final Object secondParam, final boolean hashcodeOptimized,
		final boolean toStringOptimized) {
		this.first = firstParam;
		this.second = secondParam;

		if (hashcodeOptimized) {
			optimizeHashCode();
		}

		if (toStringOptimized) {
			optimizeToString();
		}
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
		 *
		 * @invariant pairs.oclIsKindOf(Object, Map(Object, Pair))
		 * @invariant pairs.entrySet()->forall(o | o.getValue()->forall(p | p.entrySet()->forall(q | q.getFirst() == 0 and
		 * 			  q.getSecond() == p)))
		 */
		private final Map pairs = new HashMap(Constants.getNumOfMethodsInApplication());

		/** 
		 * This indicates if the generated pairs should be optimized for hash code.
		 */
		private final boolean hashcodeOptimized;

		/** 
		 * This indicates if the generated pairs should be optimized for string generation.
		 */
		private final boolean stringOptimized;

		/**
		 * Creates a new PairManager object.
		 *
		 * @param optimizeToString <code>true</code> indicates generated pairs are optimized for string generation;
		 * 		  <code>false</code>, otherwise.
		 * @param optimizeHashCode <code>true</code> indicates generated pairs are optimized for hash code generation;
		 * 		  <code>false</code>, otherwise.
		 */
		public PairManager(final boolean optimizeToString, final boolean optimizeHashCode) {
			stringOptimized = optimizeToString;
			hashcodeOptimized = optimizeHashCode;
		}

		/**
		 * Provides a pair containing 2 given objects in the given order.
		 *
		 * @param firstParam first element of the requested pair.
		 * @param secondParam second element of the requested pair.
		 *
		 * @return the pair containing the given objects.
		 *
		 * @post result != null
		 */
		public Pair getPair(final Object firstParam, final Object secondParam) {
			final Map _values = CollectionsUtilities.getMapFromMap(pairs, secondParam);

			Pair _result = (Pair) _values.get(firstParam);

			if (_result == null) {
				_result = new Pair(firstParam, secondParam, hashcodeOptimized, stringOptimized);
				_values.put(firstParam, _result);
			}
			return _result;
		}

		/**
		 * Forgets about all managed pairs.
		 */
		public void reset() {
			pairs.clear();
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

		if (o instanceof Pair) {
			final Pair _temp = (Pair) o;

			if (first == null) {
				_result = first == _temp.first;
			} else {
				_result = first.equals(_temp.first);
			}

			if (_result) {
				if (second == null) {
					_result = second == _temp.second;
				} else {
					_result = second.equals(_temp.second);
				}
			}
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

		if (hashOptimized) {
			_result = hashCode;
		} else {
			_result = hash();
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
	 * Optimizes this object with regard to hashCode representation retrival.  It (re)calculates the hashcode representation
	 * of this object and caches the new values.
	 */
	public void optimizeHashCode() {
		hashOptimized = true;
		hashCode = hash();
	}

	/**
	 * Optimizes this object with regard to it's stringized representation retrival.  It (re)calculates the stringized
	 * representation of this object and caches the new values.
	 *
	 * @post str != null
	 */
	public void optimizeToString() {
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
	 * Unoptimizes this object with regard to hashCode representation retrival.  It forgets any cached values so that they
	 * calculates on the fly when requested next.
	 */
	public void unoptimizeHashCode() {
		hashOptimized = false;
	}

	/**
	 * Unoptimizes this object with regard to stringized representation retrival.  It forgets any cached values so that they
	 * calculates on the fly when requested next.
	 *
	 * @post str == null
	 */
	public void unoptimizeToString() {
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

// End of File
