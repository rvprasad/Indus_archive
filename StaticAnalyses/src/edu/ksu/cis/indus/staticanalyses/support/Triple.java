
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.support;

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
	 * Creates a new Triple object.
	 *
	 * @param firstParam the first object of this triple.
	 * @param secondParam the second object of this triple.
	 * @param thirdParam the third object of this triple
	 * @param optimized <code>true</code> indicates that the stringized representation and the hashcode of this object should
	 * 		  be calculated and cached for the rest of it's lifetime. <code>false</code> indicates that these values shoudl
	 * 		  be calculated on the fly upon request.
	 *
	 * @post optimized == false implies str == null
	 * @post optimized == true implies str != null
	 */
	public Triple(final Object firstParam, final Object secondParam, final Object thirdParam, final boolean optimized) {
		this.first = firstParam;
		this.second = secondParam;
		this.third = thirdParam;

		if (optimized) {
			optimize();
		}
	}

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
		optimize();
	}

	/**
	 * This class manages a collection of triples.  This realizes the <i>flyweight</i> pattern for triples.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public static final class PairManager {
		/**
		 * This is the id of this service.
		 */
		public static final String ID = "Triple management service";

		/**
		 * The collection of managed triples.
		 */
		private final List triples = new ArrayList();

		/**
		 * The scratch pad triple object to be used for does-it-manage check.
		 */
		private final Triple triple = new Triple(null, null, null, false);

		/**
		 * Provides an optimized triple containing 3 given objects in the given order.
		 *
		 * @param firstParam first element of the requested triple.
		 * @param secondParam second element of the requested triple.
		 * @param thirdParam third element of the requested triple.
		 *
		 * @return the optimized triple containing the given objects.
		 *
		 * @post result != null
		 */
		public final Triple getOptimizedTriple(final Object firstParam, final Object secondParam, final Object thirdParam) {
			return getTriple(firstParam, secondParam, thirdParam, true);
		}

		/**
		 * Provides an unoptimized triple containing 3 given objects in the given order.
		 *
		 * @param firstParam first element of the requested triple.
		 * @param secondParam second element of the requested triple.
		 * @param thirdParam third element of the requested triple.
		 *
		 * @return the unoptimized triple containing the given objects.
		 *
		 * @post result != null
		 */
		public final Triple getUnOptimizedTriple(final Object firstParam, final Object secondParam, final Object thirdParam) {
			return getTriple(firstParam, secondParam, thirdParam, true);
		}

		/**
		 * Forgets about all managed triples.
		 */
		public final void reset() {
			triples.clear();
		}

		/**
		 * Provides a pair containing 3 given objects in the given order.
		 *
		 * @param firstParam first element of the requested triple.
		 * @param secondParam second element of the requested triple.
		 * @param thirdParam thrid element of the  requested triple
		 * @param optimized <code>true</code> indicates that the stringized representation and the hashcode of this object
		 * 		  should be calculated and cached for the rest of it's lifetime. <code>false</code> indicates that these
		 * 		  values should be calculated on the fly upon request.
		 *
		 * @return the triple containing the given objects.
		 *
		 * @post result != null
		 */
		private final Triple getTriple(final Object firstParam, final Object secondParam, final Object thirdParam,
			final boolean optimized) {
			Triple result;
			triple.first = firstParam;
			triple.second = secondParam;

			if (triples.contains(triple)) {
				result = (Triple) triples.get(triples.indexOf(triple));
			} else {
				result = new Triple(firstParam, secondParam, thirdParam, optimized);
				triples.add(0, result);
			}
			return result;
		}
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
	public final boolean equals(final Object o) {
		boolean result = false;

		if (o != null && o instanceof Triple) {
			Triple temp = (Triple) o;

			if (first != null) {
				result = first.equals(temp.first);
			} else {
				result = first == temp.first;
			}

			if (result) {
				if (second != null) {
					result = result && second.equals(temp.second);
				} else {
					result = result && second == temp.second;
				}

				if (result) {
					if (third != null) {
						result = result && third.equals(temp.third);
					} else {
						result = result && third == temp.third;
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns the hash code for this triple. Depending on how the object was created the cached value or the value
	 * calculated on the fly is returned.
	 *
	 * @return the hash code of this triple.
	 */
	public final int hashCode() {
		int result;

		if (str == null) {
			result = hash();
		} else {
			result = hashCode;
		}
		return result;
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
		String result;

		if (str == null) {
			result = stringize();
		} else {
			result = str;
		}
		return result;
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
	protected int hash() {
		int result = 17;

		if (first != null) {
			result = 37 * result + first.hashCode();
		}

		if (second != null) {
			result = 37 * result + second.hashCode();
		}

		if (third != null) {
			result = 37 * result + third.hashCode();
		}
		return result;
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
   Revision 1.3  2003/08/11 07:13:58  venku
 *** empty log message ***
     Revision 1.2  2003/08/11 04:20:19  venku
     - Pair and Triple were changed to work in optimized and unoptimized mode.
     - Ripple effect of the previous change.
     - Documentation and specification of other classes.
     Revision 1.1  2003/08/07 06:42:16  venku
     Major:
      - Moved the package under indus umbrella.
      - Renamed isEmpty() to hasWork() in WorkBag.
 */
