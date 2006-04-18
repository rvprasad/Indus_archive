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

import edu.ksu.cis.indus.annotations.InternalUse;
import edu.ksu.cis.indus.common.datastructures.IWork;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.SCCRelatedData;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Flow graph node associated with value associated variants. This class provides the basic behavior required by the nodes in
 * the flow graph. It is required that the nodes be able to keep track of the successor nodes and the set of values. However,
 * an implementation may transform the existing values as new values arrive, or change successors as new successors are added.
 * Hence, all imlementing classes are required to implement <code>IFGNode.onNewSucc</code> and
 * <code>IFGNode.onNewTokens</code> methods.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <N> DOCUMENT ME!
 * @param <SYM> DOCUMENT ME!
 * @param <T> DOCUMENT ME!
 */
@InternalUse public abstract class AbstractFGNode<SYM, T extends ITokens<T, SYM>, N extends AbstractFGNode<SYM, T, N>>
		implements IFGNode<SYM, T, N> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFGNode.class);

	/**
	 * The work bag provided associated with the enclosing instance of the framework. This is required if subclasses want to
	 * generate new work depending on the new values or new successors that may occur.
	 * 
	 * @invariant workbagProvider != null
	 */
	protected final IWorkBagProvider workbagProvider;

	/**
	 * A filter that controls the flow values into and out of this node.
	 */
	private ITokenFilter<T, SYM> filter;

	/**
	 * DOCUMENT ME!
	 */
	private boolean inSCCWithMultipleNodes;

	/**
	 * The piece of data required to perform strongly connected component-based optimization.
	 */
	private SCCRelatedData sccData;

	/**
	 * This refers to the work piece which will inject tokens in to this node. The protocol is that if this field is
	 * <code>null</code> then there are no tokens to be injected into this node. If this field is not-<code>null</code>
	 * then there is are some tokens waiting to be injected into this node. So, any new tokens to be injected at that point
	 * can be added to the work referred to by this work piece rather than creating and adding a new work piece to the work
	 * list.
	 */
	private SendTokensWork<SYM, T, N> sendTokensWork;

	/**
	 * The set of immediate successor nodes, i.e., there is direct edge from this node to the successor nodes, of this node.
	 * The elements in the set are of type <code>IFGNode</code>.
	 * 
	 * @invariant succs != null
	 */
	private Collection<N> succs = new HashSet<N>();

	/**
	 * The set of tokens that will be used to store tokens at this node.
	 * 
	 * @invariant tokens != null
	 */
	private T tokens;

	/**
	 * Creates a new <code>AbstractFGNode</code> instance.
	 * 
	 * @param provider provides the work bag instance associated with the enclosing instance of the framework.
	 * @param tokenSet to be used to store the tokens at this node.
	 * @pre worklistToUse != null and tokenSet != null
	 */
	protected AbstractFGNode(final IWorkBagProvider provider, final T tokenSet) {
		workbagProvider = provider;
		tokens = tokenSet;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#absorbTokensLazily(ITokens)
	 */
	public void absorbTokensLazily(final T tokensToBeInjected) {
		final T _diff = tokensToBeInjected.diffTokens(tokens);
		final boolean _tokensWillBeAbsorbed = !_diff.isEmpty();

		if (_tokensWillBeAbsorbed) {
			final IWorkBag<IWork> _workBag = workbagProvider.getWorkBag();

			if (sendTokensWork == null) {
				sendTokensWork = new SendTokensWork<SYM, T, N>((N) this, _diff);
				_workBag.addWork(sendTokensWork);
			} else {
				sendTokensWork.addTokens(_diff);
				_workBag.addWorkNoDuplicates(sendTokensWork);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Values: " + _diff.getValues() + "\n into " + this);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#addSucc(IFGNode)
	 */
	public void addSucc(final N node) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + node + " as the successor to " + this);
		}
		succs.add(node);
		onNewSucc(node);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFGNode#getSCCRelatedData()
	 */
	public final SCCRelatedData getSCCRelatedData() {
		if (sccData == null) {
			sccData = new SCCRelatedData();
		}
		return sccData;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#getSuccs()
	 */
	public final Collection<N> getSuccs() {
		return succs;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#getTokens()
	 */
	public final T getTokens() {
		return filterTokens(tokens);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#getValues()
	 */
	public final Collection<SYM> getValues() {
		return getTokens().getValues();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#injectTokens(ITokens)
	 */
	public final void injectTokens(final T newTokens) {
		final T _diffTokens = filterTokens(newTokens.diffTokens(tokens));
		final boolean _injectedTokens = !_diffTokens.isEmpty();

		if (_injectedTokens) {
			tokens.addTokens(_diffTokens);
			onNewTokens(_diffTokens);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#setFilter(ITokenFilter)
	 */
	public final void setFilter(final ITokenFilter<T, SYM> filterToUse) {
		filter = filterToUse;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFGNode#setInSCCWithMultipleNodes()
	 */
	public final void setInSCCWithMultipleNodes() {
		inSCCWithMultipleNodes = true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#setSCCRelatedData(SCCRelatedData)
	 */
	public final void setSCCRelatedData(final SCCRelatedData data) {
		sccData = data;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#setSuccessorSet(Collection)
	 */
	public final void setSuccessorSet(final Collection<N> successors) {
		succs = successors;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param work DOCUMENT ME!
	 */
	public final void setTokenSendingWork(final SendTokensWork<SYM, T, N> work) {
		assert inSCCWithMultipleNodes : "setInSCCWithMultipleNodes() before calling this method.";
		assert work != null : "The argument to this method cannot be null.";
		sendTokensWork = work;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IFGNode#setTokenSet(ITokens)
	 */
	public final void setTokenSet(final T newTokenSet) {
		tokens = newTokenSet;
	}

	/**
	 * Returns a stringized representation of this object.
	 * 
	 * @return the stringized representation of this object.
	 * @post result != null
	 */
	@Override public String toString() {
		return "IFGNode:" + hashCode();
	}

	/**
	 * Provides the tokens that go through the in filter.
	 * 
	 * @param tokenSet to be filtered.
	 * @return the filterate tokens.
	 * @pre tokenSet != null
	 * @post result != null
	 * @post filter == null implies result.equals(tokenSet)
	 */
	protected final T filterTokens(final T tokenSet) {
		final T _result;

		if (filter != null) {
			_result = filter.filter(tokenSet);
		} else {
			_result = tokenSet;
		}

		return _result;
	}

	/**
	 * Adds a new work to the worklist to propogate the values in this node to <code>succ</code>. Only the difference
	 * values are propogated.
	 * 
	 * @param succ the successor node that was added to this node.
	 * @pre succ != null
	 */
	protected void onNewSucc(final N succ) {
		succ.absorbTokensLazily(tokens);
	}

	/**
	 * Processing to be done on receiving new acceptable tokens. This implementation adds a new work to the worklist to
	 * propogate the new values to it's successor nodes.
	 * 
	 * @param newTokens the values to be propogated to the successor node. The collection contains object of type
	 *            <code>Object</code>.
	 * @pre newTokens != null
	 */
	protected void onNewTokens(final T newTokens) {
		if (!succs.isEmpty()) {
			final T _outTokens = filterTokens(newTokens);

			for (final Iterator<N> _i = succs.iterator(); _i.hasNext();) {
				final N _succ = _i.next();
				_succ.absorbTokensLazily(_outTokens);
			}
		}
	}

	/**
	 * Forgets about the associated work that pushes values to the successor nodes.
	 */
	void forgetSendTokensWork() {
		if (!inSCCWithMultipleNodes) {
			sendTokensWork = null;
		}
	}
}

// End of File
