
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

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;

import gnu.trove.TObjectIntHashMap;
import gnu.trove.TObjectIntIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import org.apache.commons.collections.list.LazyList;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class SCCDetector {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final Predicate mutableFGNodefilter = PredicateUtils.instanceofPredicate(IMutableFGNode.class);

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final Predicate nonMutableFGNodefilter = PredicateUtils.notPredicate(mutableFGNodefilter);

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	int componentNumber;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	int noOfNodes;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private final ArrayStack stack = new ArrayStack();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final TObjectIntHashMap node2componentNum = new TObjectIntHashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final TObjectIntHashMap node2dfsNum = new TObjectIntHashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final TObjectIntHashMap node2high = new TObjectIntHashMap();

	/**
	 * DOCUMENT ME!
	 *
	 * @param rootNodes
	 *
	 * @return DOCUMENT ME!
	 */
	Collection getSCCs(final Collection rootNodes) {
		componentNumber = 0;
		setNumberOfNodes(rootNodes);

		for (final Iterator _i = IteratorUtils.filteredIterator(rootNodes.iterator(), mutableFGNodefilter); _i.hasNext();) {
			final IMutableFGNode _node = (IMutableFGNode) _i.next();

			if (node2dfsNum.get(_node) == 0) {
				calculateSCC(_node);
			}
		}

		for (final Iterator _i = IteratorUtils.filteredIterator(rootNodes.iterator(), nonMutableFGNodefilter); _i.hasNext();) {
			final IFGNode _node = (IFGNode) _i.next();
			componentNumber++;
			node2componentNum.put(_node, componentNumber);
		}

		final List _result = new ArrayList(componentNumber);
		final List _temp = LazyList.decorate(_result, CollectionsUtilities.ARRAY_LIST_FACTORY);
		final TObjectIntIterator _i = node2componentNum.iterator();
		final int _iEnd = node2componentNum.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			_i.advance();
			((Collection) _temp.get(_i.value())).add(_i.key());
		}
		_result.remove(0);

		node2high.clear();
		node2dfsNum.clear();
		node2componentNum.clear();
		stack.clear();

		return _result;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param nodes
	 */
	private void setNumberOfNodes(final Collection nodes) {
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());
		_wb.addAllWorkNoDuplicates(nodes);
		noOfNodes = 0;
        
		while (_wb.hasWork()) {
			final IFGNode _node = (IFGNode) _wb.getWork();
			final Collection _succs = _node.getSuccs();
			_wb.addAllWorkNoDuplicates(_succs);
			noOfNodes++;

            if (_node instanceof IMutableFGNode) {
                final IMutableFGNode _t = (IMutableFGNode) _node;
                _t.resetSCCRelatedInfo();
            }
		}

		System.out.println("Number of nodes: " + noOfNodes);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param node DOCUMENT ME!
	 */
	private void calculateSCC(final IMutableFGNode node) {
        node2dfsNum.put(node, noOfNodes);
		node2high.put(node, noOfNodes);
		stack.push(node);
		noOfNodes--;

		final Collection _succs = CollectionUtils.select(node.getSuccs(), mutableFGNodefilter);
		final Iterator _i = _succs.iterator();
		final int _iEnd = _succs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IMutableFGNode _succ = (IMutableFGNode) _i.next();

			if (node2dfsNum.get(_succ) == 0) {
				calculateSCC(_succ);
				node2high.put(node, Math.max(node2high.get(node), node2high.get(_succ)));
			} else if (node2dfsNum.get(_succ) > node2dfsNum.get(node) && node2componentNum.get(_succ) == 0) {
				node2high.put(node, Math.max(node2high.get(node), node2dfsNum.get(_succ)));
			}
		}

		if (node2high.get(node) == node2dfsNum.get(node)) {
			componentNumber++;

			Object _o;

			do {
				_o = stack.pop();
				node2componentNum.put(_o, componentNumber);
			} while (_o != node);
		}
	}
}

// End of File
