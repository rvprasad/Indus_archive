
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

import java.util.Comparator;


/**
 * This class compares given objects based on the return value of <code>toString()</code> method.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ToStringBasedComparator
  implements Comparator {
	/** 
	 * A single of this class.
	 */
	public static final Comparator SINGLETON = new ToStringBasedComparator();

	/// CLOVER:OFF

	/**
	 * Creates an instance of this class.
	 */
	private ToStringBasedComparator() {
	}

	/// CLOVER:ON

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(final Object o1, final Object o2) {
		return String.valueOf(o1).compareTo(String.valueOf(o2));
	}
}

// End of File
