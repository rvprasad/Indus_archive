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

import edu.ksu.cis.indus.interfaces.IPrototype;

import java.util.Collection;

/**
 * This represents a collection of tokens that can be used in flow analysis. The idea is to represent values as tokens and
 * provide an abstraction to make the flow analysis generic in nature.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the type of token set objects.
 * @param <V> is the type of the representation types.
 */
public interface ITokens<T extends ITokens<T, V>, V>
		extends IPrototype<T>, Cloneable {

	/**
	 * Checks if there are no tokens in the collection.
	 * 
	 * @return <code>true</code> if there are not tokens in the collection; <code>false</code>, otherwise.
	 */
	boolean isEmpty();

	/**
	 * Retrieves the values represented by the tokens in the collection.
	 * 
	 * @return a collection of values.
	 * @post result != null
	 */
	Collection<V> getValues();

	/**
	 * Adds the given tokens into this collection of tokens. Implementation dictates properties such as duplication.
	 * 
	 * @param newTokens to be added to this colleciton.
	 * @pre newTokens != null
	 */
	void addTokens(T newTokens);

	/**
	 * Empties this collection.
	 */
	void clear();

	/**
	 * Returns a collection of tokens that only occur in this collection and not in <code>tokens</code>. In short, it
	 * subtracts <code>tokens</code> from this.
	 * 
	 * @param tokens is the subtrahend in the difference operation.
	 * @return a collection of tokens that represent the difference.
	 * @pre tokens != null
	 * @post result != null
	 */
	T diffTokens(T tokens);
}

// End of File
