
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

import edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
  implements IFGNode {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractFGNode.class);

	/** 
	 * The set of immediate successor nodes, i.e., there is direct edge from this node to the successor nodes, of this node.
	 * The elements in the set are of type <code>IFGNode</code>.
	 *
	 * @invariant succs != null
	 */
	protected final Set succs = new HashSet();

	/** 
	 * A filter that controls the outflow of values from this node.
	 */
	protected ITokenFilter filter;

	/** 
	 * The set of tokens that will be used to store tokens at this node.
	 *
	 * @invariant tokens != null
	 */
	protected final ITokens tokens;

	/** 
	 * The work bag provided associated with the enclosing instance of the framework.  This is required if subclasses will
	 * want to generate new work depending on the new values or new successors that may occur.
	 *
	 * @invariant workbagProvider != null
	 */
	protected final IWorkBagProvider workbagProvider;

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
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported by this class but should be implemented by
	 * 		   subclasses.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("Parameterless prototype() method is not supported.");
	}

	/**
	 * This method will throw <code>UnsupprotedOperationException</code>.
	 *
	 * @param param <i>ignored</i>.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported by this class but should be implemented by
	 * 		   subclasses.
	 */
	public Object getClone(final Object param) {
		throw new UnsupportedOperationException("prototype(param1) method is not supported.");
	}

	/**
	 * Sets the filter on this node.
	 *
	 * @param filterToUse to be used by this node.
	 */
	public void setFilter(final ITokenFilter filterToUse) {
		this.filter = filterToUse;
	}

	/**
	 * Retrieves the set of tokens accumulated in this node.
	 *
	 * @return the set of tokens.
	 *
	 * @post result != null
	 */
	public final ITokens getTokens() {
		return this.tokens;
	}

	/**
	 * Retrieves the values that have accumulated at this node.
	 *
	 * @return the values accumulated at this node.
	 *
	 * @post result != null
	 */
	public final Collection getValues() {
		return tokens.getValues();
	}

	/**
	 * Adds a successor node to this node.
	 *
	 * @param node the node to be added as successor to this node.
	 *
	 * @pre node != null
	 */
	public void addSucc(final IFGNode node) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + node + " as the successor to " + this);
		}
		succs.add(node);
		onNewSucc(node);
	}

	/**
	 * Injects a set of values into the set of values associated with this node.
	 *
	 * @param newTokens the collection of tokens to be added as successors to this node.
	 *
	 * @pre newTokens != null
	 */
	public final void injectTokens(final ITokens newTokens) {
		final ITokens _diffTokens = newTokens.diffTokens(tokens);

		if (!_diffTokens.isEmpty()) {
			tokens.addTokens(_diffTokens);
			onNewTokens(_diffTokens);
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
		final ITokens _filterate;

		if (filter != null) {
			_filterate = filter.filter(tokens);
		} else {
			_filterate = tokens;
		}

		final ITokens _temp = _filterate.diffTokens(succ.getTokens());

		if (!_temp.isEmpty()) {
			generateWorkToInjectWorkInto(_temp, succ);
		}
	}

	/**
	 * Adds a new work to the worklist to propogate <code>values</code> in this node to it's successor nodes.
	 *
	 * @param newTokens the values to be propogated to the successor node.  The collection contains object of
	 * 		  type<code>Object</code>.
	 *
	 * @pre newTokens != null
	 */
	protected void onNewTokens(final ITokens newTokens) {
		if (!succs.isEmpty()) {
			final ITokens _temp;

			if (filter != null) {
				_temp = filter.filter(newTokens);
			} else {
				_temp = newTokens;
			}

			for (final Iterator _i = succs.iterator(); _i.hasNext();) {
				final IFGNode _succ = (IFGNode) _i.next();
				final ITokens _diff = _temp.diffTokens(_succ.getTokens());

				if (!_diff.isEmpty()) {
					generateWorkToInjectWorkInto(_diff, _succ);
				}
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
	 * Enqueues a work piece to inject the given tokens into the given target.  If there is a work piece associated with the
	 * target that is enqueued, then we piggy-back on it.
	 *
	 * @param tokensToBeSent are the tokens to be injected.
	 * @param target into which to inject the new tokens.
	 *
	 * @pre tokensToBeSent != null and target != null
	 */
	private void generateWorkToInjectWorkInto(final ITokens tokensToBeSent, final IFGNode target) {
		final IWorkBag _workBag = workbagProvider.getWorkBag();

		if (target instanceof AbstractFGNode) {
			SendTokensWork _work = ((AbstractFGNode) target).sendTokensWork;

			if (_work == null) {
				_work = SendTokensWork.getWork(target, tokensToBeSent);
				((AbstractFGNode) target).sendTokensWork = _work;
				_workBag.addWork(_work);
			} else {
				_work.addTokens(tokensToBeSent);
			}
		} else {
			_workBag.addWork(SendTokensWork.getWork(target, tokensToBeSent));
		}
	}
}

// End of File
