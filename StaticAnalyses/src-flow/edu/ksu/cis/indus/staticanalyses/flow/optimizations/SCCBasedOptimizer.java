
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

import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IMutableFGNode;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class contains the logic to optimize flow graphs based on SCCs in the graph.
 * 
 * <p>
 * This class is not for external use.
 * </p>
 * 
 * <p>
 * This class relies on data(sccRelatedData) associated with flow graph nodes.
 * </p>
 * 
 * <p>
 * We have implemented the SCC algorithm as described in "Introduction to Algorithms - Udi Manber".  We have optimized the
 * algorithm in terms of initialization of dfs number and high value associated with the nodes.  This optimization can
 * handle graphs of with 2^31 - 1 nodes.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SCCBasedOptimizer {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SCCBasedOptimizer.class);

	/** 
	 * The filter used to select mutable graph nodes.
	 */
	private static final Predicate MUTABLE_FG_NODE_FILTER = PredicateUtils.instanceofPredicate(IMutableFGNode.class);

	/** 
	 * This indicates we will be using positive integer domain for dfs numbers.
	 */
	private static final int POSITIVE_DOMAIN = 1;

	/** 
	 * This indicates we will be using negative integer domain for dfs numbers.
	 */
	private static final int NEGATIVE_DOMAIN = 0;

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
	 *
	 * @pre rootNodes != null and tokenMgr != null
	 * @pre rootNodes.oclIsKindOf(Collection(IFGNode))
	 */
	public void optimize(final Collection rootNodes, final ITokenManager tokenMgr) {
		final Collection _sccs = getSCCs(rootNodes);
		final Iterator _i = _sccs.iterator();
		final int _iEnd = _sccs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Collection _scc = (Collection) _i.next();

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
	 * Retrieves the SCCs in the graph.
	 *
	 * @param rootNodes to start SCC detection algorithm from.
	 *
	 * @return a collection of SCCs.
	 *
	 * @pre rootNodes != null and rootNodes.oclIsKindOf(Collection(IFGNode))
	 * @post result != null and result.oclIsKindOf(Collection(Collection(IMutableFGNode)))
	 */
	private Collection getSCCs(final Collection rootNodes) {
		final ArrayStack _stack = new ArrayStack();
		setMaxNumberForSelectedNumberDomain();
		componentNumber = 0;

		final Collection _sccs = new ArrayList();
		final Iterator _i = IteratorUtils.filteredIterator(rootNodes.iterator(), MUTABLE_FG_NODE_FILTER);

		for (; _i.hasNext();) {
			final IMutableFGNode _node = (IMutableFGNode) _i.next();

			if (unexplored(_node.getSCCRelatedData())) {
				calculateSCC(_node, _sccs, _stack);
			}
		}
		toggleNumberDomain();
		return _sccs;
	}

	/**
	 * Calculates the SCC starting from the given root node.
	 *
	 * @param root to start SCC detection from.
	 * @param sccs is an out argument that contains SCCs.
	 * @param stack used during DFS.
	 *
	 * @pre root != null and sccs != null and stack != null
	 * @pre sccs.oclIsKindOf(Collection(Collection(IMutableFGNode)))
	 * @post sccs.containsAll(sccs$pre)
	 */
	private void calculateSCC(final IMutableFGNode root, final Collection sccs, final ArrayStack stack) {
		final SCCRelatedData _srd = root.getSCCRelatedData();
		_srd.setComponentNum(0);
		_srd.setDfsNum(noOfNodes);
		_srd.setHigh(noOfNodes);
		stack.push(root);
		noOfNodes--;

		final Collection _succs = CollectionUtils.select(root.getSuccs(), MUTABLE_FG_NODE_FILTER);
		final Iterator _i = _succs.iterator();
		final int _iEnd = _succs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IMutableFGNode _succ = (IMutableFGNode) _i.next();
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

			IMutableFGNode _o;
			final Collection _set = new HashSet();
			final SCCRelatedData _commonSRD = root.getSCCRelatedData();
			_commonSRD.setComponentNum(componentNumber);

			do {
				_o = (IMutableFGNode) stack.pop();
				_o.setSCCRelatedData(_commonSRD);
				_set.add(_o);
			} while (_o != root);
			sccs.add(_set);
		}
	}

	/**
	 * Optimize the SCC.
	 *
	 * @param scc to be optimized.
	 * @param tokenManager to be used.
	 *
	 * @pre scc != null and scc.oclIsKindOf(Collection(IMutableFGNode))
	 * @pre tokenManager != null
	 */
	private void optimizeSCC(final Collection scc, final ITokenManager tokenManager) {
		final ITokens _unifiedTokens = tokenManager.getNewTokenSet();
		final ITokens _newTokenSet = tokenManager.getNewTokenSet();
		final Collection _succs = new HashSet();
		final Collection _newSuccs = new HashSet();
		final Iterator _i = scc.iterator();
		final int _iEnd = scc.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IMutableFGNode _node = (IMutableFGNode) _i.next();
			_unifiedTokens.addTokens(_node.getTokens());
			_succs.addAll(_node.getSuccs());
			_node.setTokenSet(_newTokenSet);
			_node.setSuccessorSet(_newSuccs);
		}

		// We don't add the scc nodes to the successor set of the SCC. 
		_newSuccs.addAll(CollectionUtils.subtract(_succs, scc));
		((IFGNode) scc.iterator().next()).injectTokens(_unifiedTokens);
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
	 *
	 * @return <code>true</code> if unexplored; <code>false</code>, otherwise.
	 *
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
