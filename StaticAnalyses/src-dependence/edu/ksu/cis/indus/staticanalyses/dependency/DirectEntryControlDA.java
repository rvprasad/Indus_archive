
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.graph.INode;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This class provides direct entry-based intraprocedural control dependence information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 *
 * @see EntryControlDA
 */
public class DirectEntryControlDA
  extends EntryControlDA {
	// TODO: Link to documentation describing direct entry-based intraprocedural control dependence needs to be added.

	/*
	 * In this class, the tokens corresponding to ancestors are blocked at control points. Only when a node accumulates all
	 * tokens of a control point node, the tokens at the control point corresponding to the ancestor of the control point are
	 * injected into the token set of the node.
	 */

	/**
	 * @see EntryControlDA#processNode(INode,BitSet[][])
	 */
	protected Collection processNode(final INode node, final BitSet[][] tokenSets) {
		final Collection _result;
		accumulateTokensAtNode(node, tokenSets);

		if (!nodesWithChildrenCache.contains(node)) {
			_result = super.processNode(node, tokenSets);
		} else {
			/*
			 * Say B and C are control points, A is the control merge point of B, and A and B are dependent on C.
			 * It is possible that node A accumulated all tokens from control point B when tokens from control point C had
			 * not reached B.  In such cases, we need to find and add nodes such as A to the workbag for processing.
			 */
			_result = new HashSet();

			final int _iEnd = nodesCache.size();
			final int _nodeIndex = nodesCache.indexOf(node);
			final int _succsSize = node.getSuccsOf().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final BitSet _bitSet = tokenSets[_iIndex][_nodeIndex];

				if (_bitSet != null && _bitSet.cardinality() == _succsSize && _nodeIndex != _iIndex) {
					_result.add(nodesCache.get(_iIndex));
				}
			}
		}
		return _result;
	}

	/**
	 * Accumulates the tokens of ancestor nodes for the purpose of direct CD calculation.  In this method,  the tokens at
	 * ancestors of the control points which were dependees for the given node are injected into the token set of the given
	 * node if the dependees are no longer dependees.
	 *
	 * @param node at which to accumulate tokens.
	 * @param tokenSets is the collection of token sets of the nodes in the graph.
	 *
	 * @pre node != null and tokenSets != null
	 */
	private void accumulateTokensAtNode(final INode node, final BitSet[][] tokenSets) {
		final int _nodeIndex = nodesCache.indexOf(node);

		for (int _ctrlPointNodeIndex = nodesCache.size() - 1; _ctrlPointNodeIndex >= 0; _ctrlPointNodeIndex--) {
			final BitSet _nodesCtrlPointBitSet = tokenSets[_nodeIndex][_ctrlPointNodeIndex];
			final INode _ctrlPointNode = (INode) nodesCache.get(_ctrlPointNodeIndex);

			if (_nodesCtrlPointBitSet != null
				  && _nodesCtrlPointBitSet.cardinality() == _ctrlPointNode.getSuccsOf().size()
				  && _nodeIndex != _ctrlPointNodeIndex) {
				copyAncestorBitSetsFromTo(_ctrlPointNodeIndex, _nodeIndex, tokenSets);
			}
		}
	}

	/**
	 * Injects the tokens corresponding to the ancestors of the node at <code>src</code> into the token sets corresponding to
	 * the same  ancestors at the node at <code>dest</code>.
	 *
	 * @param src is the index of the node whose ancestor's tokens need to be propagated.
	 * @param dest is the index of the node into which the tokens will be propagated to.
	 * @param tokenSets is the collection of token sets of the nodes in the graph.
	 *
	 * @pre tokenSets != null
	 * @pre 0 &lt;= src &lt; tokensSets.length
	 * @pre 0 &lt;= dest &lt; tokensSets.length
	 */
	private void copyAncestorBitSetsFromTo(final int src, final int dest, final BitSet[][] tokenSets) {
		final BitSet[] _srcBitSets = tokenSets[src];
		final BitSet[] _destBitSets = tokenSets[dest];
		final BitSet _temp = new BitSet();
		final Iterator _i = nodesWithChildrenCache.iterator();
		final int _iEnd = nodesWithChildrenCache.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final INode _ancestor = (INode) _i.next();
			final int _ancestorIndex = nodesCache.indexOf(_ancestor);
			final BitSet _srcsAncestorBitSet = _srcBitSets[_ancestorIndex];

			if (_srcsAncestorBitSet != null) {
				final BitSet _destAndAncestorBitSet = getBitSetAtLocation(_destBitSets, _ancestorIndex);
				_temp.clear();
				_temp.or(_srcsAncestorBitSet);
				_temp.andNot(_destAndAncestorBitSet);

				if (_temp.cardinality() > 0) {
					_destAndAncestorBitSet.or(_srcsAncestorBitSet);
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/07/08 10:28:48  venku
   - identity cases were not handled in DirectEntryControlDA and EntryControlDA. FIXED.
   Revision 1.4  2004/06/22 01:01:37  venku
   - BitSet(1) creates an empty bitset.  Instead we use BitSet() to create a
     bit set that contains a long array of length 1.
   Revision 1.3  2004/06/06 08:33:37  venku
   - completed implementation and documentation.
   Revision 1.2  2004/06/06 02:28:50  venku
   - INTERIM : still implementating direct control dependence calculation.
   Revision 1.1  2004/06/05 09:52:24  venku
   - INTERIM COMMIT
     - Reimplemented EntryControlDA.  It provides indirect control dependence info.
     - DirectEntryControlDA provides direct control dependence info.
     - ExitControlDA will follow same suite as EntryControlDA with new implementation
       and new class for direct dependence.
 */
