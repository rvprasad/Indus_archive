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

import edu.ksu.cis.indus.annotations.AEmpty;

import java.util.Comparator;

/**
 * This class compares given objects based on the return value of <code>toString()</code> method.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the type of the objects that this comparator can compare.
 */
public final class ToStringBasedComparator<T>
		implements Comparator<T> {

	/**
	 * A single of this class.
	 * 
	 * @param <T1> is the type of object that the return comparator will handle.
	 * @return the comparator
	 * @post result != null
	 */
	public static final <T1> Comparator<T1> getComparator() {
		return new ToStringBasedComparator<T1>();
	}

	// / CLOVER:OFF

	/**
	 * Creates an instance of this class.
	 */
	@AEmpty private ToStringBasedComparator() {
		// does nothing.
	}

	// / CLOVER:ON

	/**
	 * @see java.util.Comparator#compare(Object, Object)
	 */
	public int compare(final T o1, final T o2) {
		return String.valueOf(o1).compareTo(String.valueOf(o2));
	}
}

// End of File
