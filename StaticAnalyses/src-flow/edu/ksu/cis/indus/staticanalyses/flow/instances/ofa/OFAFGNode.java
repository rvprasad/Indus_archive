
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


/**
 * This class represents the flow graph node that accumulates objects as their entities would refer to objects at run-time.
 * This is an Object-flow analysis specific implementation.
 * 
 * <p>
 * Created: Thu Jan 31 00:42:34 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class OFAFGNode
  extends AbstractFGNode {
	/** 
	 * The token manager that manages the tokens whose flow is being instrumented.
	 *
	 * @invariant tokenMgr != null
	 */
	private final ITokenManager tokenMgr;

	/**
	 * Creates a new <code>OFAFGNode</code> instance.
	 *
	 * @param provider provides the work bag instance associated with the framework within which this node exists.
	 * @param tokenManager that manages the tokens whose flow is being instrumented.
	 *
	 * @pre provider != null and tokenManager != null
	 */
	public OFAFGNode(final IWorkBagProvider provider, final ITokenManager tokenManager) {
		super(provider, tokenManager.getNewTokenSet());
		tokenMgr = tokenManager;
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o is the work bag provider to be passed to the constructor of this class.
	 *
	 * @return a new instance of this class parameterized by <code>o</code>.
	 *
	 * @pre o != null and o.oclIsKindOf(IWorkBagProvider)
	 * @post result != null and result.oclIsKindOf(OFAFGNode)
	 */
	public Object getClone(final Object o) {
		return new OFAFGNode((IWorkBagProvider) o, tokenMgr);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFGNode#injectValue(java.lang.Object)
	 */
	public void injectValue(final Object value) {
		final ITokens _tokens = tokenMgr.getTokens(Collections.singleton(value));
		injectTokens(_tokens);
	}
}

// End of File
