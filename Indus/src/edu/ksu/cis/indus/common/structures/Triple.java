
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
import java.util.List;


/**
 * This class represents a triplet of objects.  The hashcode/stringized rep. of this object is derived from it's
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
public class Triple
  implements Cloneable {
	/**
	 * The first object in this triple.
	 */
	protected Object first;

	/**
	 * The second object in this triple.
	 */
	protected Object second;

	/**
	 * The third object in this triple.
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
	 * Creates a new optimized Triple object.
	 *
	 * @param firstParam the first object of this triple.
	 * @param secondParam the second object of this triple.
	 * @param thirdParam the third object of this triple
	 *
	 * @post str != null
	 */
	public Triple(final Object firstParam, final Object secondParam, final Object thirdParam) {
		this.first = firstParam;
		this.second = secondParam;
		this.third = thirdParam;
	}

	/**
	 * Returns the first object in the triple.
	 *
	 * @return the first object in the triple.
	 */
	public final Object getFirst() {
		return first;
	}

	/**
	 * Returns the second object in the triple.
	 *
	 * @return the second object in the triple.
	 */
	public final Object getSecond() {
		return second;
	}

	/**
	 * Returns the third object in the triple.
	 *
	 * @return the third object in the triple.
	 */
	public final Object getThird() {
		return third;
	}

	/**
	 * Clones this object.  The contents are cloned based on shallow-copying semantics.
	 *
	 * @return a cloned copy of this triple.
	 *
	 * @throws CloneNotSupportedException if <code>super.clone()</code> fails.
	 */
	public Object clone()
	  throws CloneNotSupportedException {
		return (Triple) super.clone();
	}

	/**
	 * Checks if the given object is equal to this triple.
	 *
	 * @param o is the object to be tested for equality with this object.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this triple; <code>false</code>, otherwise.
	 *
	 * @post result == true implies o.oclTypeOf(Triple) and (o.first.equals(first) or o.first == first) and
	 * 		 (o.second.equals(second) or o.second == second) and (o.third.equals(third) or o.third == third)
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o != null && o instanceof Triple) {
			final Triple _temp = (Triple) o;

			_result = first == _temp.first || first.equals(_temp.first);

			if (_result) {
				_result = second == _temp.second || second.equals(_temp.second);
			}

			if (_result) {
				_result = third == _temp.third || third.equals(_temp.third);
			}
		}
		return _result;
	}

	/**
	 * Returns the hash code for this triple. Depending on how the object was created the cached value or the value
	 * calculated on the fly is returned.
	 *
	 * @return the hash code of this triple.
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
	public final void optimize() {
		hashCode = hash();
		str = stringize();
	}

	/**
	 * Returns a stringified representation of this object.  Depending on how the object was created the cached value or the
	 * value calculated on the fly is returned.
	 *
	 * @return stringified representation of this object.
	 */
	public final String toString() {
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
	public final void unoptimize() {
		str = null;
	}

	/**
	 * Provides the hashcode of this object.
	 *
	 * @return the hashcode of this object.
	 */
	protected final int hash() {
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
		return _result;
	}

	/**
	 * Provides the stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 */
	protected String stringize() {
		return "(" + first + ", " + second + ", " + third + ")";
	}
}

/*
   ChangeLog:
   $Log$
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
   Revision 1.8  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.7  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.6  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/09/02 02:48:44  venku
   - TripleManager was misnamed as PairManager. FIXED.
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
 */
