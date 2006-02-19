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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.annotations.AInternalUse;
import edu.ksu.cis.indus.common.graph.SCCRelatedData;
import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;

/**
 * The super interface to be implemented by all the flow graph node objects used in FA framework. It provides the basic
 * methods to add a node into the flow graph and to add values the node. Although the methods provide to add values, it upto
 * the implementation to process the values as it sees fit. So, <code>addValue</code> means that a value arrived at this
 * node, and an implementation can create a store the incoming value or derive another value and store the derived value.
 * <p>
 * The main purpose of this class in FA framework is to represent the summary set, and hence, it provides mostly basic set
 * operations. However, it is possible to derive complex operations from these basic operations. There is no support for
 * removing of nodes or values as it is designed to be used in an additive environment. Moreover, removing of either nodes or
 * values will require other specific processing which is unknown at this level of abstraction.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <N> DOCUMENT ME!
 * @param <SYM> DOCUMENT ME!
 * @param <T> DOCUMENT ME!
 */
@AInternalUse public interface IFGNode<SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>>
		extends IPrototype<N> {

	/**
	 * Absorbs the tokens lazily. The laziness is defined by the implementation.
	 * 
	 * @param tokens to be absorbed.
	 * @pre tokens != null
	 */
	void absorbTokensLazily(T tokens);

	/**
	 * Adds a successor node to this node.
	 * 
	 * @param node the node to be added as a successor node.
	 * @pre node != null
	 */
	void addSucc(N node);

	/**
	 * Retrieves SCC related data of this node.
	 * 
	 * @return SCC related data.
	 * @post result != null
	 */
	SCCRelatedData getSCCRelatedData();

	/**
	 * Retrieves the successors of this node.
	 * 
	 * @return the successor nodes.
	 * @post result != null
	 */
	Collection<N> getSuccs();

	/**
	 * Retrieves the tokens accumulated at this node.
	 * 
	 * @return the accumulated tokens.
	 * @post result != null
	 */
	T getTokens();

	/**
	 * Returns the values in this node.
	 * 
	 * @return the values in this node.
	 * @post result != null
	 */
	Collection<SYM> getValues();

	/**
	 * Injects the given tokens into this node.
	 * 
	 * @param tokens to be injected into this node.
	 * @pre tokens != null
	 */
	void injectTokens(T tokens);

	/**
	 * Injects a value into this node.
	 * 
	 * @param value to be injected into this node.
	 * @pre value != null
	 */
	void injectValue(SYM value);

	/**
	 * Sets a filter object which will filter the values flowing into this node.
	 * 
	 * @param filter object to be used.
	 * @pre filter != null
	 */
	void setFilter(ITokenFilter<T, SYM> filter);

	/**
	 * Sets the given data as the SCC related data of this node.
	 * 
	 * @param data to be used.
	 * @pre data != null
	 */
	void setSCCRelatedData(SCCRelatedData data);

	/**
	 * Sets the successor collection to be used to store successors.
	 * 
	 * @param successors the new collection.
	 * @pre successors != null
	 */
	void setSuccessorSet(final Collection<N> successors);

	/**
	 * DOCUMENT ME!
	 */
	void setInSCCWithMultipleNodes();

	/**
	 * Sets the token set to be used.
	 * 
	 * @param newTokenSet to be used.
	 * @pre newTokenSet != null
	 */
	void setTokenSet(final T newTokenSet);
}

// End of File
