
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
public class Pair
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
		public final Pair getOptimizedPair(final Object firstParam, final Object secondParam) {
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
		public final Pair getUnOptimizedPair(final Object firstParam, final Object secondParam) {
			return getPair(firstParam, secondParam, true);
		}

		/**
		 * Forgets about all managed pairs.
		 */
		public final void reset() {
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
		private final Pair getPair(final Object firstParam, final Object secondParam, final boolean optimized) {
			Pair result;
			pair.first = firstParam;
			pair.second = secondParam;

			if (pairs.contains(pair)) {
				result = (Pair) pairs.get(pairs.indexOf(pair));
			} else {
				result = new Pair(firstParam, secondParam, optimized);
				pairs.add(0, result);
			}
			return result;
		}
	}

	/**
	 * Returns the first element in the pair.
	 *
	 * @return the first element in the pair.
	 */
	public final Object getFirst() {
		return first;
	}

	/**
	 * Returns the second element in the pair.
	 *
	 * @return the second element in the pair.
	 */
	public final Object getSecond() {
		return second;
	}

	/**
	 * Clones this pair using shallow-copy semantics.
	 *
	 * @return a cloned copy of this pair.
	 *
	 * @throws CloneNotSupportedException will not be thrown.
	 */
	public final Object clone()
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
	public final boolean equals(final Object o) {
		boolean result = false;

		if (o != null && o instanceof Pair) {
			Pair temp = (Pair) o;

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
			}
		}
		return result;
	}

	/**
	 * Returns the hash code for this pair. Depending on how the object was created the cached value or the value calculated
	 * on the fly is returned.
	 *
	 * @return the hash code of this pair.
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
	 * Returns a stringified version of this object. Depending on how the object was created the cached value or the value
	 * calculated on the fly is returned.
	 *
	 * @return a stringified version of this object.
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
	 * Provides the hashcode of this object.  Subclasses may override this method to suit their needs.
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
		return result;
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
     Revision 1.4  2003/05/22 22:18:31  venku
     All the interfaces were renamed to start with an "I".
     Optimizing changes related Strings were made.
 */
