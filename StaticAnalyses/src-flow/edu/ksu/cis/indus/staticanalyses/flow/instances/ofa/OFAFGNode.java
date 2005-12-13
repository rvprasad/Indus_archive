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
 * @param <T> DOCUMENT ME!
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
