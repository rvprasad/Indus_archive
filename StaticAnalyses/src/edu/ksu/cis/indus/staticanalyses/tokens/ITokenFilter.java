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

package edu.ksu.cis.indus.staticanalyses.tokens;

/**
 * This type represents a filter for tokens.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the type of token set objects.
 * @param <V> is the type of the value object (in the representation).
 */
public interface ITokenFilter<T extends ITokens<T, V>, V> {

	/**
	 * Filters the given values.
	 * 
	 * @param tokens to be filtered.
	 * @return a collection of values without the values that were filtered.
	 */
	T filter(final T tokens);
}

// End of File
