
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

import edu.ksu.cis.indus.common.datastructures.IWorkBag;

import edu.ksu.cis.indus.interfaces.AbstractPrototype;

import edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Flow graph node associated with value associated variants.  This class provides the basic behavior required by the nodes
 * in the flow graph.  It is required that the nodes be able to keep track of the successor nodes and the set of values.
 * However, an implementation may transform the existing values as new values arrive, or change successors as new successors
 * are added.  Hence, all imlementing classes are required to implement <code>IFGNode.onNewSucc</code> and
 * <code>IFGNode.onNewTokens</code> methods.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractFGNode
  extends AbstractPrototype
  implements IMutableFGNode {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractFGNode.class);

	/** 
	 * The work bag provided associated with the enclosing instance of the framework.  This is required if subclasses  want
	 * to generate new work depending on the new values or new successors that may occur.
	 *
	 * @invariant workbagProvider != null
	 */
	protected final IWorkBagProvider workbagProvider;

	/** 
	 * The set of immediate successor nodes, i.e., there is direct edge from this node to the successor nodes, of this node.
	 * The elements in the set are of type <code>IFGNode</code>.
	 *
	 * @invariant succs != null and succs.oclIsKindOf(IFGNode)
	 */
	private Collection succs = new HashSet();

	/** 
	 * A filter that controls the inflow of values to this node.
	 */
	private ITokenFilter inFilter;

	/** 
	 * A filter that controls the outflow of values from this node.
	 */
	private ITokenFilter outFilter;

	/** 
	 * The set of tokens that will be used to store tokens at this node.
	 *
	 * @invariant tokens != null
	 */
	private ITokens tokens;

	/** 
	 * The piece of data required to perform strongly connected component-based optimization.
	 */
	private SCCRelatedData sccData;

	/** 
	 * This refers to the work piece which will inject tokens in to this node.  The protocol is that if this field is
	 * <code>null</code> then there are no tokens to be injected into this node.  If this field is not-<code>null</code>
	 * then there is are some  tokens waiting to be injected into this node.  So, any new tokens to be injected at that
	 * point can be added to the work referred to by this work piece rather than creating and adding a new work piece to the
	 * work list.
	 */
	private SendTokensWork sendTokensWork;

	/**
	 * Creates a new <code>AbstractFGNode</code> instance.
	 *
	 * @param provider provides the work bag instance associated with the enclosing instance of the framework.
	 * @param tokenSet to be used to store the tokens at this node.
	 *
	 * @pre worklistToUse != null and tokenSet != null
	 */
	protected AbstractFGNode(final IWorkBagProvider provider, final ITokens tokenSet) {
		workbagProvider = provider;
		tokens = tokenSet;
	}

	/**
	 * @see IFGNode#setInFilter(ITokenFilter)
	 */
	public final void setInFilter(final ITokenFilter filterToUse) {
		inFilter = filterToUse;
	}

	/**
	 * @see IFGNode#setOutFilter(ITokenFilter)
	 */
	public final void setOutFilter(final ITokenFilter filterToUse) {
		outFilter = filterToUse;
	}

	/**
	 * @see IMutableFGNode#setSCCRelatedData(SCCRelatedData)
	 */
	public final void setSCCRelatedData(final SCCRelatedData data) {
		sccData = data;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IMutableFGNode#getSCCRelatedData()
	 */
	public final SCCRelatedData getSCCRelatedData() {
		if (sccData == null) {
			sccData = new SCCRelatedData();
		}
		return sccData;
	}

	/**
	 * @see IMutableFGNode#setSuccessorSet(Collection)
	 */
	public final void setSuccessorSet(final Collection successors) {
		succs = successors;
	}

	/**
	 * @see IFGNode#getSuccs()
	 */
	public final Collection getSuccs() {
		return succs;
	}

	/**
	 * @see IMutableFGNode#setTokenSet(ITokens)
	 */
	public final void setTokenSet(final ITokens newTokenSet) {
		tokens = newTokenSet;
	}

	/**
	 * @see IFGNode#getTokens()
	 */
	public final ITokens getTokens() {
		return filterTokens(outFilter, tokens);
	}

	/**
	 * @see IFGNode#getValues()
	 */
	public final Collection getValues() {
		return getTokens().getValues();
	}

	/**
	 * @see IFGNode#addSucc(IFGNode)
	 */
	public void addSucc(final IFGNode node) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + node + " as the successor to " + this);
		}
		succs.add(node);
		onNewSucc(node);
	}

	/**
	 * @see IFGNode#injectTokens(ITokens)
	 */
	public final void injectTokens(final ITokens newTokens) {
		final ITokens _diffTokens = filterTokens(inFilter, newTokens.diffTokens(tokens));

		if (!_diffTokens.isEmpty()) {
			tokens.addTokens(_diffTokens);
			onNewTokens(_diffTokens);
		}
	}

	/**
	 * @see IFGNode#injectTokensLazily(ITokens)
	 */
	public void injectTokensLazily(final ITokens tokensToBeInjected) {
		final IWorkBag _workBag = workbagProvider.getWorkBag();
		final ITokens _diff = tokensToBeInjected.diffTokens(tokens);

		if (!_diff.isEmpty()) {
			if (sendTokensWork == null) {
				sendTokensWork = SendTokensWork.getWork(this, _diff);
				_workBag.addWork(sendTokensWork);
			} else {
				sendTokensWork.addTokens(_diff);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Values: " + _diff.getValues() + "\n into " + this);
			}
		}
	}

	/**
	 * Returns a stringized representation of this object.
	 *
	 * @return the stringized representation of this object.
	 *
	 * @post result != null
	 */
	public String toString() {
		return "IFGNode:" + hashCode();
	}

	/**
	 * Adds a new work to the worklist to propogate the values in this node to <code>succ</code>.  Only the difference values
	 * are propogated.
	 *
	 * @param succ the successor node that was added to this node.
	 *
	 * @pre succ != null
	 */
	protected void onNewSucc(final IFGNode succ) {
		succ.injectTokensLazily(getTokens());
	}

	/**
	 * Processing to be done on receiving new acceptable tokens. This implementation adds a new work to the worklist to
	 * propogate the new values (that satisfy the associated out filter) to it's successor nodes. 
	 *
	 * @param newTokens the values to be propogated to the successor node.  The collection contains object of
	 * 		  type<code>Object</code>.
	 *
	 * @pre newTokens != null
	 */
	protected void onNewTokens(final ITokens newTokens) {
		if (!succs.isEmpty()) {
            final ITokens _temp = filterTokens(outFilter, newTokens);
			for (final Iterator _i = succs.iterator(); _i.hasNext();) {
				final IFGNode _succ = (IFGNode) _i.next();
				_succ.injectTokensLazily(_temp);
			}
		}
	}

	/**
	 * Forgets about the associated work that pushes values to the successor nodes.
	 */
	void forgetSendTokensWork() {
		sendTokensWork = null;
	}

	/**
	 * Provides the tokens that go through the given filter.
	 *
	 * @param filter to be used.
	 * @param tokenSet to be filtered.
	 *
	 * @return the filterate tokens.
	 *
	 * @pre tokenSet != null
	 * @post result != null
	 * @post filter == null implies result.equals(tokenSet)
	 */
	private static ITokens filterTokens(final ITokenFilter filter, final ITokens tokenSet) {
		final ITokens _result;

		if (filter != null) {
			_result = filter.filter(tokenSet);
		} else {
			_result = tokenSet;
		}

		return _result;
	}
}

// End of File
