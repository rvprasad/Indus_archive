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

package edu.ksu.cis.indus.staticanalyses.tokens;

import java.util.Collection;

/**
 * This is the interface used to manage tokens.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the type of token set objects.
 * @param <V> is the type of the value objects (in the representation).
 * @param <R> is the type of the representation types.
 */
public interface ITokenManager<T extends ITokens<T, V>, V, R> {

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
	 * @return a type manager.
	 * @post result != null
	 */
	ITypeManager<R, V> getTypeManager();

	/**
	 * Reset the token manager.
	 */
	void reset();
}

// End of File
