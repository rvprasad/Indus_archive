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

import java.util.Collection;

/**
 * This is the interface used to manage tokens.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> DOCUMENT ME!
 * @param <V> DOCUMENT ME!
 */
public interface ITokenManager<T extends ITokens<T, V>, V> {

	/**
	 * Retrieves a new empty token set.
	 * 
	 * @return the new token set.
	 * @post result != null and result.getValues().isEmpty()
	 */
	T getNewTokenSet();

	/**
	 * Retrieves a collection of tokens that represents the given values.
	 * 
	 * @param values is the collection of values to be represented as tokens.
	 * @return a collection of tokens.
	 * @pre values != null
	 * @post result != null
	 */
	T getTokens(Collection<V> values);

	/**
	 * Retrieves a token filter that can be used to filter out values that are not of the given type.
	 * 
	 * @param type is the type of the values which the filter will let through.
	 * @return a token filter.
	 * @pre type != null
	 * @post result != null
	 */
	ITokenFilter<T, V> getTypeBasedFilter(final IType type);

	/**
	 * Retrieves the type manager used in conjunction with this token manager.
	 * 
	 * @param <R> the type in the representation type system.
	 * @return a type manager.
	 * @post result != null
	 */
	<R> ITypeManager<R, V> getTypeManager();

	/**
	 * Reset the token manager.
	 */
	void reset();
}

// End of File
