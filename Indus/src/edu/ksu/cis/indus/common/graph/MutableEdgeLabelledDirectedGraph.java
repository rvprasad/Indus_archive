
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

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraph.IEdgeLabelledNode;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import org.apache.commons.collections.collection.CompositeCollection;


/**
 * This is abstract implementation of mutable edge labelled directed graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class MutableEdgeLabelledDirectedGraph
  extends MutableDirectedGraph
  implements IMutableEdgeLabelledDirectedGraph {
	/**
	 * Creates an instance of this class.
	 *
	 * @param info maintains the information of this graph.
	 *
	 * @pre info != null
	 */
	protected MutableEdgeLabelledDirectedGraph(final GraphInfo info) {
		super(info);
	}

	/**
	 * This is an implementation of the node in mutable edge-labelled directed graphs.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	protected static class MutableEdgeLabelledNode
	  extends EdgeLabelledNode
	  implements IMutableEdgeLabelledNode {
		/**
		 * Creates an instance of this class.
		 *
		 * @param preds is the reference to the collection of predecessors.
		 * @param succs is the reference to the collection of successors.
		 *
		 * @pre preds != null and succs != null
		 */
		protected MutableEdgeLabelledNode(final Collection preds, final Collection succs) {
			super(preds, succs);
		}

		/**
		 * @see IMutableDirectedGraph.IMutableNode#addPredecessor(IDirectedGraph.INode)
		 */
		public final boolean addPredecessor(final INode node) {
			return addIncomingEdgeLabelledFrom(IEdgeLabel.DUMMY_LABEL, (IEdgeLabelledNode) node);
		}

		/**
		 * @see IMutableDirectedGraph.IMutableNode#addSuccessor(IDirectedGraph.INode)
		 */
		public final boolean addSuccessor(final INode node) {
			return addOutgoingEdgeLabelledTo(IEdgeLabel.DUMMY_LABEL, (IEdgeLabelledNode) node);
		}

		/**
		 * @see IMutableEdgeLabelledDirectedGraph.IMutableEdgeLabelledNode#addIncomingEdgeLabelledFrom(IEdgeLabel,
		 * 		IEdgeLabelledDirectedGraph.IEdgeLabelledNode)
		 */
		public boolean addIncomingEdgeLabelledFrom(final IEdgeLabel label, final IEdgeLabelledNode node) {
			predecessors.add(node);
			return CollectionsUtilities.putIntoSetInMap(label2inNodes, label, node);
		}

		/**
		 * @see IMutableEdgeLabelledDirectedGraph.IMutableEdgeLabelledNode#addOutgoingEdgeLabelledTo(IEdgeLabel,
		 * 		IEdgeLabelledDirectedGraph.IEdgeLabelledNode)
		 */
		public boolean addOutgoingEdgeLabelledTo(final IEdgeLabel label, final IEdgeLabelledNode node) {
			successors.add(node);
			return CollectionsUtilities.putIntoSetInMap(label2outNodes, label, node);
		}

		/**
		 * @see IMutableEdgeLabelledDirectedGraph.IMutableEdgeLabelledNode#removeIncomingEdgeLabelledFrom(IEdgeLabel,
		 * 		IEdgeLabelledDirectedGraph.IEdgeLabelledNode)
		 */
		public boolean removeIncomingEdgeLabelledFrom(final IEdgeLabel label, final IEdgeLabelledNode node) {
			return removeEdgesLabelledForViaUpdate(label, node, label2inNodes, predecessors);
		}

		/**
		 * @see IMutableEdgeLabelledDirectedGraph.IMutableEdgeLabelledNode#removeIncomingEdgesLabelled(IEdgeLabel)
		 */
		public boolean removeIncomingEdgesLabelled(final IEdgeLabel label) {
			return removedEdgesLabelled(label, label2inNodes, predecessors);
		}

		/**
		 * @see IMutableEdgeLabelledDirectedGraph.IMutableEdgeLabelledNode#removeOutgoingEdgeLabelledTo(IEdgeLabel,
		 * 		IEdgeLabelledDirectedGraph.IEdgeLabelledNode)
		 */
		public boolean removeOutgoingEdgeLabelledTo(final IEdgeLabel label, final IEdgeLabelledNode node) {
			return removeEdgesLabelledForViaUpdate(label, node, label2outNodes, successors);
		}

		/**
		 * @see IMutableEdgeLabelledDirectedGraph.IMutableEdgeLabelledNode#removeOutgoingEdgesLabelled(IEdgeLabel)
		 */
		public boolean removeOutgoingEdgesLabelled(final IEdgeLabel label) {
			return removedEdgesLabelled(label, label2outNodes, successors);
		}

		/**
		 * @see IMutableDirectedGraph.IMutableNode#removePredecessor(IDirectedGraph.INode)
		 */
		public boolean removePredecessor(final INode node) {
			return removeNode(node, label2inNodes, predecessors);
		}

		/**
		 * @see IMutableDirectedGraph.IMutableNode#removeSuccessor(IDirectedGraph.INode)
		 */
		public boolean removeSuccessor(final INode node) {
			return removeNode(node, label2outNodes, successors);
		}

		/**
		 * Removes the edges with the given label that to/from the given node by updating the given map and collection. The
		 * map and the collection dictate the to/from direction.
		 *
		 * @param label of interest.
		 * @param node of interest.
		 * @param map that maps labels to predecessor or successor nodes.  This is updated.
		 * @param col is a collection of predecessor or successor nodes of this node. This is updated.
		 *
		 * @return <code>true</code> if the node existed and was removed; <code>false</code>, otherwise.
		 *
		 * @pre map != null and col != null
		 */
		private boolean removeEdgesLabelledForViaUpdate(final IEdgeLabel label, final IEdgeLabelledNode node, final Map map,
			final Collection col) {
			final Collection _t = (Collection) MapUtils.getObject(map, label, Collections.EMPTY_SET);
			final boolean _result;

			if (_t.isEmpty()) {
				_result = false;
			} else {
				_result = _t.remove(node);

				if (_result) {
					if (_t.isEmpty()) {
						map.remove(label);
					}
					retainAllIn(col, map.values());
				}
			}
			return _result;
		}

		/**
		 * Removes the relation between this and the given node by updating the given map and collection.
		 *
		 * @param node of interest.
		 * @param map that maps labels to predecessor or successor nodes.  This is updated.
		 * @param col is a collection of predecessor or successor nodes of this node. This is updated.
		 *
		 * @return <code>true</code> if the node was related to this node and it as removed; <code>false</code>, otherwise.
		 *
		 * @pre map != null and col != null
		 */
		private static boolean removeNode(final INode node, final Map map, final Collection col) {
			final Iterator _i = map.keySet().iterator();
			final int _iEnd = map.keySet().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Object _k = _i.next();
				((Collection) map.get(_k)).remove(node);
			}
			return col.remove(node);
		}

		/**
		 * Removes all nodes that were related to this node via the given label by updating the given map and collection.
		 *
		 * @param label of interest.
		 * @param map that maps labels to predecessor or successor nodes.  This is updated.
		 * @param col is a collection of predecessor or successor nodes of this node. This is updated.
		 *
		 * @return <code>true</code> if the node was related to this node and it as removed; <code>false</code>, otherwise.
		 *
		 * @pre map != null and col != null
		 */
		private static boolean removedEdgesLabelled(final IEdgeLabel label, final Map map, final Collection col) {
			final boolean _result = map.remove(label) != null;
			retainAllIn(col, map.values());
			return _result;
		}

		/**
		 * Retains all elements in <code>col</code> that exist in any collection in <code>collections</code>.
		 *
		 * @param col to be updated.
		 * @param collections is a collection of collections.
		 *
		 * @pre col != null and collections != null
		 * @pre collections.oclIsKindOf(Collection(Collection))
		 */
		private static void retainAllIn(final Collection col, final Collection collections) {
			final CompositeCollection _c = new CompositeCollection();
			_c.addComposited((Collection[]) collections.toArray());
			col.retainAll(_c);
		}
	}

	/**
	 * @see IMutableEdgeLabelledDirectedGraph#addEdgeFromTo(IEdgeLabelledDirectedGraph.IEdgeLabelledNode, IEdgeLabel,
	 * 		IEdgeLabelledDirectedGraph.IEdgeLabelledNode)
	 */
	public final boolean addEdgeFromTo(final IEdgeLabelledNode src, final IEdgeLabel label, final IEdgeLabelledNode dest) {
		final boolean _result = super.addEdgeFromTo(src, dest);

		if (_result) {
			((IMutableEdgeLabelledNode) src).addOutgoingEdgeLabelledTo(label, dest);
			((IMutableEdgeLabelledNode) dest).addIncomingEdgeLabelledFrom(label, src);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @pre src != null and dest != null
	 * @pre src.oclIsKindOf(IEdgeLabelledNode) and dest.oclIsKindOf(IEdgeLabelledNode)
	 */
	public boolean addEdgeFromTo(final INode src, final INode dest) {
		return addEdgeFromTo((IEdgeLabelledNode) src, IEdgeLabel.DUMMY_LABEL, (IEdgeLabelledNode) dest);
	}

	/**
	 * @see IMutableEdgeLabelledDirectedGraph#removeAllEdgesLabelled(IEdgeLabel)
	 */
	public boolean removeAllEdgesLabelled(final IEdgeLabel label) {
		boolean _result = true;
		final Iterator _i = getNodes().iterator();
		final int _iEnd = getNodes().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IMutableEdgeLabelledNode _node = (IMutableEdgeLabelledNode) _i.next();
			_result &= _node.removeOutgoingEdgesLabelled(label);
			_result &= _node.removeIncomingEdgesLabelled(label);
		}
		return _result;
	}

	/**
	 * @see IMutableEdgeLabelledDirectedGraph#removeAllEdgesLabelledFrom(IEdgeLabel,
	 * 		IEdgeLabelledDirectedGraph.IEdgeLabelledNode)
	 */
	public boolean removeAllEdgesLabelledFrom(final IEdgeLabel label, final IEdgeLabelledNode node) {
		final Collection _dests = ((IMutableEdgeLabelledNode) node).getSuccsViaEdgesLabelled(label);
		final Iterator _i = _dests.iterator();
		final int _iEnd = _dests.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IMutableEdgeLabelledNode _dest = (IMutableEdgeLabelledNode) _i.next();
			_dest.removeIncomingEdgeLabelledFrom(label, node);
		}
		return ((IMutableEdgeLabelledNode) node).removeOutgoingEdgesLabelled(label);
	}

	/**
	 * @see IMutableEdgeLabelledDirectedGraph#removeAllEdgesLabelledTo(IEdgeLabel,
	 * 		IEdgeLabelledDirectedGraph.IEdgeLabelledNode)
	 */
	public boolean removeAllEdgesLabelledTo(final IEdgeLabel label, final IEdgeLabelledNode node) {
		final Collection _srcs = ((IMutableEdgeLabelledNode) node).getPredsViaEdgesLabelled(label);
		final Iterator _i = _srcs.iterator();
		final int _iEnd = _srcs.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IMutableEdgeLabelledNode _src = (IMutableEdgeLabelledNode) _i.next();
			_src.removeOutgoingEdgeLabelledTo(label, node);
		}
		return ((IMutableEdgeLabelledNode) node).removeIncomingEdgesLabelled(label);
	}

	/**
	 * @see IMutableDirectedGraph#removeEdgeFromTo(IDirectedGraph.INode,     IDirectedGraph.INode)
	 */
	public boolean removeEdgeFromTo(final INode src, final INode dest) {
		return removeEdgeFromTo((IEdgeLabelledNode) src, IEdgeLabel.DUMMY_LABEL, (IEdgeLabelledNode) dest);
	}

	/**
	 * @see IMutableEdgeLabelledDirectedGraph#removeEdgeFromTo(IEdgeLabelledDirectedGraph.IEdgeLabelledNode, IEdgeLabel,
	 * 		IEdgeLabelledDirectedGraph.IEdgeLabelledNode)
	 */
	public boolean removeEdgeFromTo(final IEdgeLabelledNode src, final IEdgeLabel label, final IEdgeLabelledNode dest) {
		return ((IMutableEdgeLabelledNode) src).removeOutgoingEdgeLabelledTo(label, dest)
		  && ((IMutableEdgeLabelledNode) dest).removeIncomingEdgeLabelledFrom(label, src);
	}
}

// End of File
