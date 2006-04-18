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

package edu.ksu.cis.indus.common.collections;

/**
 * A predicate to check on an object.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> is the type of object on which the predicate is evaluated.
 */
public interface IPredicate<T> {

	/**
	 * Evaluates the predicate on the given object.
	 * 
	 * @param <T1> is the type of the input.
	 * @param t is the input.
	 * @return <code>true</code> if the predicate evaluates to <code>true</code>; <code>false</code>, otherwise.
	 */
	<T1 extends T> boolean evaluate(T1 t);

}

// End of File
