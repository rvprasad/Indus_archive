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

package edu.ksu.cis.indus.staticanalyses.flow.optimizations;

import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.graph.SCCRelatedData;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.SendTokensWork;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the logic to optimize flow graphs based on SCCs in the graph.
 * <p>
 * This class is not for external use.
 * </p>
 * <p>
 * This class relies on data(sccRelatedData) associated with flow graph nodes.
 * </p>
 * <p>
 * We have implemented the SCC algorithm as described in "Introduction to Algorithms - Udi Manber". We have optimized the
 * algorithm in terms of initialization of dfs number and high value associated with the nodes. This optimization can handle
 * graphs of with 2^31 - 1 nodes.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <SYM> is the type of value being tracked.
 * @param <T> is the type of the token set object.
 * @param <N> is the type of the summary node in the flow analysis.
 */
public class SCCBasedOptimizer<SYM, T extends ITokens<T, SYM>, N extends IFGNode<SYM, T, N>> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SCCBasedOptimizer.class);

	/**
	 * This indicates we will be using negative integer domain for dfs numbers.
	 */
	private static final int NEGATIVE_DOMAIN = 0;

	/**
	 * This indicates we will be using positive integer domain for dfs numbers.
	 */
	private static final int POSITIVE_DOMAIN = 1;

	/**
	 * This indicates the current component number.
	 */
	int componentNumber;

	/**
	 * This provides the ceiling value from which dfs number and high values are calculated.
	 */
	int noOfNodes;

	/**
	 * This indicates the domain used for numbering: positive integer domain or negative integer domain.
	 */
	private int numberingDomain = NEGATIVE_DOMAIN;

	/**
	 * Starting from the given nodes, optimize the graph based on SCC.
	 * 
	 * @param rootNodes are the nodes that serve as root nodes for SCC detection.
	 * @param tokenMgr to be used.
	 * @pre rootNodes != null and tokenMgr != null
	 */
	public void optimize(final Collection<N> rootNodes, final ITokenManager<T, SYM, ?> tokenMgr) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Collapsing SCCs...");
		}

		final Collection<Collection<N>> _sccs = getSCCs(rootNodes);
		final Iterator<Collection<N>> _i = _sccs.iterator();
		final int _iEnd = _sccs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Collection<N> _scc = _i.next();

			if (_scc.size() > 1) {
				optimizeSCC(_scc, tokenMgr);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Collapsed an SCC of size " + _scc.size());
				}
			}
		}
	}

	/**
	 * Reset internal data structures.
	 */
	public void reset() {
		numberingDomain = NEGATIVE_DOMAIN;
	}

	/**
	 * Calculates the SCC starting from the given root node.
	 * 
	 * @param root to start SCC detection from.
	 * @param sccs is an out argument that contains SCCs.
	 * @param stack used during DFS.
	 * @pre root != null and sccs != null and stack != null
	 * @post sccs.containsAll(sccs$pre)
	 */
	private void calculateSCC(final N root, final Collection<Collection<N>> sccs, final Stack<N> stack) {
		final SCCRelatedData _srd = root.getSCCRelatedData();
		_srd.setComponentNum(0);
		_srd.setDfsNum(noOfNodes);
		_srd.setHigh(noOfNodes);
		stack.push(root);
		noOfNodes--;

		final Collection<N> _succs = root.getSuccs();
		final Iterator<N> _i = _succs.iterator();
		final int _iEnd = _succs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final N _succ = _i.next();
			final SCCRelatedData _succSRD = _succ.getSCCRelatedData();

			if (unexplored(_succSRD)) {
				calculateSCC(_succ, sccs, stack);
				_srd.setHigh(Math.max(_srd.getHigh(), _succSRD.getHigh()));
			} else if (_succSRD.getDfsNum() > _srd.getDfsNum() && _succSRD.getComponentNum() == 0) {
				_srd.setHigh(Math.max(_srd.getHigh(), _succSRD.getDfsNum()));
			}
		}

		if (_srd.getHigh() == _srd.getDfsNum()) {
			componentNumber++;

			N _o;
			final Collection<N> _set = new HashSet<N>();
			final SCCRelatedData _commonSRD = root.getSCCRelatedData();
			_commonSRD.setComponentNum(componentNumber);

			do {
				_o = stack.pop();
				_o.setSCCRelatedData(_commonSRD);
				_set.add(_o);
			} while (_o != root);
			sccs.add(_set);
		}
	}

	/**
	 * Retrieves the SCCs in the graph.
	 * 
	 * @param rootNodes to start SCC detection algorithm from.
	 * @return a collection of SCCs.
	 * @pre rootNodes != null
	 * @post result != null
	 */
	private Collection<Collection<N>> getSCCs(final Collection<N> rootNodes) {
		final Stack<N> _stack = new Stack<N>();
		setMaxNumberForSelectedNumberDomain();
		componentNumber = 0;

		final Collection<Collection<N>> _sccs = new ArrayList<Collection<N>>();
		final Iterator<N> _i = rootNodes.iterator();

		for (; _i.hasNext();) {
			final N _node = _i.next();

			if (unexplored(_node.getSCCRelatedData())) {
				calculateSCC(_node, _sccs, _stack);
			}
		}
		toggleNumberDomain();
		return _sccs;
	}

	/**
	 * Optimize the SCC.
	 * 
	 * @param scc to be optimized.
	 * @param tokenManager to be used.
	 * @pre scc != nul
	 * @pre tokenManager != null
	 */
	private void optimizeSCC(final Collection<N> scc, final ITokenManager<T, SYM, ?> tokenManager) {
		final SendTokensWork<SYM, T, N> _work = new SendTokensWork<SYM, T, N>(scc.iterator().next(), tokenManager
				.getNewTokenSet());
		final T _unifiedTokens = tokenManager.getNewTokenSet();
		final T _newTokenSet = tokenManager.getNewTokenSet();
		final Collection<N> _succs = new HashSet<N>();
		final Collection<N> _newSuccs = new HashSet<N>();
		final Iterator<N> _i = scc.iterator();
		final int _iEnd = scc.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final N _node = _i.next();
			_unifiedTokens.addTokens(_node.getTokens());
			_succs.addAll(_node.getSuccs());
			_node.setTokenSet(_newTokenSet);
			_node.setSuccessorSet(_newSuccs);
			_node.setInSCCWithMultipleNodes();
			if (_node instanceof AbstractFGNode) {
				final AbstractFGNode _abstractFGNode = (AbstractFGNode) _node;
				(_abstractFGNode).setTokenSendingWork(_work);
			}
		}

		// We don't add the scc nodes to the successor set of the SCC.
		_newSuccs.addAll(SetUtils.difference(_succs, scc));
		scc.iterator().next().injectTokens(_unifiedTokens);
	}

	/**
	 * Sets the maximum number in the selected number domain.
	 */
	private void setMaxNumberForSelectedNumberDomain() {
		if (numberingDomain == NEGATIVE_DOMAIN) {
			noOfNodes = -1;
		} else {
			noOfNodes = Integer.MAX_VALUE;
		}
	}

	/**
	 * Toggles number domain.
	 */
	private void toggleNumberDomain() {
		if (numberingDomain == NEGATIVE_DOMAIN) {
			numberingDomain = POSITIVE_DOMAIN;
		} else {
			numberingDomain = NEGATIVE_DOMAIN;
		}
	}

	/**
	 * Checks if the given data (hence the associated node) is unexplored.
	 * 
	 * @param data to be checked.
	 * @return <code>true</code> if unexplored; <code>false</code>, otherwise.
	 * @pre data != null
	 */
	private boolean unexplored(final SCCRelatedData data) {
		boolean _result;

		if (numberingDomain == NEGATIVE_DOMAIN) {
			_result = data.getDfsNum() >= 0;
		} else {
			_result = data.getDfsNum() <= 0;
		}
		return _result;
	}
}

// End of File
