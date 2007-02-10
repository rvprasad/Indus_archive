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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.annotations.InternalUse;
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
 * @param <N> is the type of the summary node in the flow analysis.
 * @param <SYM> is the type of symbol whose flow is being analyzed.
 * @param <T>  is the type of the token set object.
 */
@InternalUse public interface IFGNode<SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>>
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
	 * Marks the node as being part of a SCC of multiple nodes in the flow graph.
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
