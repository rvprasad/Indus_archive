
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

import edu.ksu.cis.indus.interfaces.IPrototype;

import java.util.Collection;


/**
 * This represents a collection of tokens that can be used in flow analysis.  The idea is to represent values as tokens and
 * provide an abstraction to make the flow analysis generic in nature.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> DOCUMENT ME!
 * @param <V> DOCUMENT ME!
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
	 *
	 * @post result != null
	 */
	Collection<V> getValues();

	/**
	 * Adds the given tokens into this collection of tokens.  Implementation dictates properties such as duplication.
	 *
	 * @param newTokens to be added to this colleciton.
	 *
	 * @pre newTokens != null
	 */
	void addTokens(T newTokens);

	/**
	 * Empties this collection.
	 */
	void clear();

	/**
	 * Returns a collection of tokens that only occur in this collection and not in <code>tokens</code>.  In short, it
	 * subtracts <code>tokens</code> from this.
	 *
	 * @param tokens is the subtrahend in the difference operation.
	 *
	 * @return a collection of tokens that represent the difference.
	 *
	 * @pre tokens != null
	 * @post result != null
	 */
	T diffTokens(T tokens);
}

// End of File
