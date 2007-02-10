/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;

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

	// / CLOVER:ON

	/**
	 * Creates an instance of this class.
	 */
	@Empty private ToStringBasedComparator() {
		// does nothing.
	}

	// / CLOVER:ON

	/**
	 * A single of this class.
	 * 
	 * @param <T1> is the type of object that the return comparator will handle.
	 * @return the comparator
	 * @post result != null
	 */
	public static <T1> Comparator<T1> getComparator() {
		return new ToStringBasedComparator<T1>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public int compare(final T o1, final T o2) {
		return String.valueOf(o1).compareTo(String.valueOf(o2));
	}
}

// End of File
