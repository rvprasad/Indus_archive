
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

package edu.ksu.cis.indus.common.datastructures;

import java.util.ArrayList;
import java.util.List;


/**
 * This class represents a quadraple of objects.  The hashcode/stringized rep. of this object is derived from it's
 * constituents.
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
public final class Quadraple
  implements Cloneable {
	/**
	 * The first object in this quadraple.
	 */
	protected Object first;

	/**
	 * The fourth object in this quadraple.
	 */
	protected Object fourth;

	/**
	 * The second object in this quadraple.
	 */
	protected Object second;

	/**
	 * The third object in this quadraple.
	 */
	protected Object third;

	/**
	 * A cached copy of the stringized representation of this object.
	 */
	private String str;

	/**
	 * A cached copy of the hash code of this object.
	 */
	private int hashCode;

	/**
	 * Creates a new Quadraple object.
	 *
	 * @param firstParam the first object of this quadraple.
	 * @param secondParam the second object of this quadraple.
	 * @param thirdParam the third object of this quadraple
	 * @param fourthParam fourth element of the requested quadraple.
	 * @param optimized <code>true</code> indicates that the stringized representation and the hashcode of this object should
	 * 		  be calculated and cached for the rest of it's lifetime. <code>false</code> indicates that these values shoudl
	 * 		  be calculated on the fly upon request.
	 *
	 * @post optimized == false implies str == null
	 * @post optimized == true implies str != null
	 */
	public Quadraple(final Object firstParam, final Object secondParam, final Object thirdParam, final Object fourthParam,
		final boolean optimized) {
		this.first = firstParam;
		this.second = secondParam;
		this.third = thirdParam;
		this.fourth = fourthParam;

		if (optimized) {
			optimize();
		}
	}

	/**
	 * Creates a new optimized Quadraple object.
	 *
	 * @param firstParam the first object of this quadraple.
	 * @param secondParam the second object of this quadraple.
	 * @param thirdParam the third object of this quadraple
	 * @param fourthParam fourth element of the requested quadraple.
	 *
	 * @post str != null
	 */
	public Quadraple(final Object firstParam, final Object secondParam, final Object thirdParam, final Object fourthParam) {
		this.first = firstParam;
		this.second = secondParam;
		this.third = thirdParam;
		this.fourth = fourthParam;
		optimize();
	}

	/**
	 * This class manages a collection of quadraples.  This realizes the <i>flyweight</i> pattern for quadraples.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public static final class QuadrapleManager {
		/**
		 * This is the id of this service.
		 */
		public static final String ID = "Quadraple management service";

		/**
		 * The collection of managed quadraples.
		 */
		private final List quadraples = new ArrayList();

		/**
		 * The scratch pad quadraple object to be used for does-it-manage check.
		 */
		private final Quadraple quadrapleCache = new Quadraple(null, null, null, null, false);

		/**
		 * Provides an optimized quadraple containing 3 given objects in the given order.
		 *
		 * @param firstParam first element of the requested quadraple.
		 * @param secondParam second element of the requested quadraple.
		 * @param thirdParam third element of the requested quadraple.
		 * @param fourthParam fourth element of the requested quadraple.
		 *
		 * @return the optimized quadraple containing the given objects.
		 *
		 * @post result != null
		 */
		public Quadraple getOptimizedQuadraple(final Object firstParam, final Object secondParam, final Object thirdParam,
			final Object fourthParam) {
			return getQuadraple(firstParam, secondParam, thirdParam, fourthParam, true);
		}

		/**
		 * Provides an unoptimized quadraple containing 3 given objects in the given order.
		 *
		 * @param firstParam first element of the requested quadraple.
		 * @param secondParam second element of the requested quadraple.
		 * @param thirdParam third element of the requested quadraple.
		 * @param fourthParam fourth element of the requested quadraple.
		 *
		 * @return the unoptimized quadraple containing the given objects.
		 *
		 * @post result != null
		 */
		public Quadraple getUnOptimizedQuadraple(final Object firstParam, final Object secondParam, final Object thirdParam,
			final Object fourthParam) {
			return getQuadraple(firstParam, secondParam, thirdParam, fourthParam, true);
		}

		/**
		 * Forgets about all managed quadraples.
		 */
		public void reset() {
			quadraples.clear();
		}

		/**
		 * Provides a pair containing 3 given objects in the given order.
		 *
		 * @param firstParam first element of the requested quadraple.
		 * @param secondParam second element of the requested quadraple.
		 * @param thirdParam thrid element of the  requested quadraple
		 * @param fourthParam fourth element of the requested quadraple.
		 * @param optimized <code>true</code> indicates that the stringized representation and the hashcode of this object
		 * 		  should be calculated and cached for the rest of it's lifetime. <code>false</code> indicates that these
		 * 		  values should be calculated on the fly upon request.
		 *
		 * @return the quadraple containing the given objects.
		 *
		 * @post result != null
		 */
		private Quadraple getQuadraple(final Object firstParam, final Object secondParam, final Object thirdParam,
			final Object fourthParam, final boolean optimized) {
			Quadraple _result;
			quadrapleCache.first = firstParam;
			quadrapleCache.second = secondParam;

			if (quadraples.contains(quadrapleCache)) {
				_result = (Quadraple) quadraples.get(quadraples.indexOf(quadrapleCache));
			} else {
				_result = new Quadraple(firstParam, secondParam, thirdParam, fourthParam, optimized);
				quadraples.add(0, _result);
			}
			return _result;
		}
	}

	/**
	 * Returns the first object in the quadraple.
	 *
	 * @return the first object in the quadraple.
	 */
	public Object getFirst() {
		return first;
	}

	/**
	 * Returns the fourth object in the quadraple.
	 *
	 * @return the fourth object in the quadraple.
	 */
	public Object getFourth() {
		return fourth;
	}

	/**
	 * Returns the second object in the quadraple.
	 *
	 * @return the second object in the quadraple.
	 */
	public Object getSecond() {
		return second;
	}

	/**
	 * Returns the third object in the quadraple.
	 *
	 * @return the third object in the quadraple.
	 */
	public Object getThird() {
		return third;
	}

	/**
	 * Clones this object.  The contents are cloned based on shallow-copying semantics.
	 *
	 * @return a cloned copy of this quadraple.
	 *
	 * @throws CloneNotSupportedException if <code>super.clone()</code> fails.
	 */
	public Object clone()
	  throws CloneNotSupportedException {
		return (Quadraple) super.clone();
	}

	/**
	 * Checks if the given object is equal to this quadraple.
	 *
	 * @param o is the object to be tested for equality with this object.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this quadraple; <code>false</code>, otherwise.
	 *
	 * @post result == true implies o.oclTypeOf(Quadraple) and (o.first.equals(first) or o.first == first) and
	 * 		 (o.second.equals(second) or o.second == second) and (o.third.equals(third) or o.third == third)
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o instanceof Quadraple) {
			final Quadraple _temp = (Quadraple) o;

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

			if (_result) {
				if (third == null) {
					_result = third == _temp.third;
				} else {
					_result = third.equals(_temp.third);
				}
			}

			if (_result) {
				if (fourth == null) {
					_result = fourth == _temp.fourth;
				} else {
					_result = fourth.equals(_temp.fourth);
				}
			}
		} else {
			_result = super.equals(o);
		}
		return _result;
	}

	/**
	 * Returns the hash code for this quadraple. Depending on how the object was created the cached value or the value
	 * calculated on the fly is returned.
	 *
	 * @return the hash code of this quadraple.
	 */
	public int hashCode() {
		int _result;

		if (str == null) {
			_result = hash();
		} else {
			_result = hashCode;
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
	 * Returns a stringified representation of this object.  Depending on how the object was created the cached value or the
	 * value calculated on the fly is returned.
	 *
	 * @return stringified representation of this object.
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
	 * Provides the hashcode of this object.
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

		if (third != null) {
			_result = 37 * _result + third.hashCode();
		}

		if (fourth != null) {
			_result = 37 * _result + fourth.hashCode();
		}
		return _result;
	}

	/**
	 * Provides the stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 */
	protected String stringize() {
		return "(" + first + ", " + second + ", " + third + ", " + fourth + ")";
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/01/06 00:17:10  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.3  2003/12/28 03:07:04  venku
   - renamed field triple/quadraple to tripleCache/quadrapleCache.
   Revision 1.2  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.2  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.1  2003/09/02 12:29:59  venku
   - Installed a new component to represent tuples of size 4.
 */
