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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IWorkBagProvider;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collections;

import soot.Value;

/**
 * This class represents the flow graph node that accumulates objects as their entities would refer to objects at run-time.
 * This is an Object-flow analysis specific implementation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T>  is the type of the token set object.
 */
class OFAFGNode<T extends ITokens<T, Value>>
		extends AbstractFGNode<Value, T, OFAFGNode<T>> {

	/**
	 * The token manager that manages the tokens whose flow is being instrumented.
	 *
	 * @invariant tokenMgr != null
	 */
	private final ITokenManager<T, Value, ?> tokenMgr;

	/**
	 * Creates a new <code>OFAFGNode</code> instance.
	 *
	 * @param provider provides the work bag instance associated with the framework within which this node exists.
	 * @param tokenManager that manages the tokens whose flow is being instrumented.
	 * @pre provider != null and tokenManager != null
	 */
	public OFAFGNode(final IWorkBagProvider provider, final ITokenManager<T, Value, ?> tokenManager) {
		super(provider, tokenManager.getNewTokenSet());
		tokenMgr = tokenManager;
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o is the work bag provider to be passed to the constructor of this class.
	 * @return a new instance of this class parameterized by <code>o</code>.
	 * @pre o != null and o[0].oclIsKindOf(IWorkBagProvider)
	 * @post result != null
	 */
	public OFAFGNode<T> getClone(final Object... o) {
		return new OFAFGNode<T>((IWorkBagProvider) o[0], tokenMgr);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFGNode#injectValue(java.lang.Object)
	 */
	public void injectValue(final Value value) {
		final T _tokens = tokenMgr.getTokens(Collections.singleton(value));
		injectTokens(_tokens);
	}
}

// End of File
