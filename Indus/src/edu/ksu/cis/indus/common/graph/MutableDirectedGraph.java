
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This class provides abstract implementation and methods to mutate a directed graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class MutableDirectedGraph
  extends AbstractDirectedGraph
  implements IMutableDirectedGraph {
	/** 
	 * This maintains the graph information.
	 */
	protected final GraphInfo graphInfo;

	/**
	 * Creates a new AbstractDirectedGraph object.
	 */
	protected MutableDirectedGraph() {
		graphInfo = new GraphInfo();
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param info maintains the information for this graph.
	 *
	 * @pre info != null
	 */
	protected MutableDirectedGraph(final GraphInfo info) {
		graphInfo = info;
	}

	/**
	 * This class extends <code>INode</code> such that the resulting node can mutated.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	protected static class MutableNode
	  extends Node
	  implements INode,
		  IMutableNode {
		/**
		 * Creates a new MutableNode object.
		 *
		 * @param preds is the reference to the collection of predecessors.
		 * @param succs is the reference to the collection of successors.
		 *
		 * @pre preds != null and succs != null
		 */
		protected MutableNode(final Collection preds, final Collection succs) {
			super(preds, succs);
		}

		/**
		 * @see IMutableNode#addPredecessor(INode)
		 */
		public final boolean addPredecessor(final INode node) {
			return predecessors.add(node);
		}

		/**
		 * @see IMutableNode#addSuccessor(INode)
		 */
		public final boolean addSuccessor(final INode node) {
			return successors.add(node);
		}

		/**
		 * @see IMutableNode#removePredecessor(INode)
		 */
		public boolean removePredecessor(final INode node) {
			return predecessors.remove(node);
		}

		/**
		 * @see IMutableNode#removeSuccessor(INode)
		 */
		public boolean removeSuccessor(final INode node) {
			return successors.remove(node);
		}
	}

	/**
	 * @see IDirectedGraph#getNodes()
	 */
	public List getNodes() {
		return graphInfo.getNodes();
	}

	/**
	 * @see IMutableDirectedGraph#addEdgeFromTo(INode, INode)
	 */
	public boolean addEdgeFromTo(final INode src, final INode dest) {
		boolean _result = false;

		if (containsNode(src) && containsNode(dest)) {
			((IMutableNode) src).addSuccessor(dest);
			((IMutableNode) dest).addPredecessor(src);
			heads.remove(dest);
			shapeChanged();
			_result = true;
		} else {
			throw new IllegalArgumentException("Either or both of the provided nodes do not exist in this graph.");
		}

		return _result;
	}

	/**
	 * @see IMutableDirectedGraph#removeEdgeFromTo(INode,INode)
	 */
	public boolean removeEdgeFromTo(final INode src, final INode dest) {
		boolean _result = false;

		if (containsNode(src) && containsNode(dest)) {
			((IMutableNode) src).removeSuccessor(dest);
			((IMutableNode) dest).removePredecessor(src);
			shapeChanged();
			_result = true;
		} else {
			throw new IllegalArgumentException("Either or both of the provided nodes do not exist in this graph.");
		}

		return _result;
	}

	/**
	 * @see IMutableDirectedGraph#removeNode(INode)
	 */
	public final boolean removeNode(final INode node) {
		final Collection _succsOf = new ArrayList(node.getSuccsOf());
		final Iterator _i = _succsOf.iterator();
		final int _iEnd = _succsOf.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			((IMutableNode) _i.next()).removePredecessor(node);
		}

		final Collection _predsOf = new ArrayList(node.getPredsOf());
		final Iterator _j = _predsOf.iterator();
		final int _jEnd = _predsOf.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			((IMutableNode) _j.next()).removeSuccessor(node);
		}
		heads.remove(node);
		shapeChanged();
		return graphInfo.removeNode(node);
	}

	/**
	 * @see IMutableDirectedGraph#addNode(IDirectedGraph.INode)
	 */
	public boolean addNode(final INode node) {
		return graphInfo.addNode(node);
	}

	/**
	 * @see AbstractDirectedGraph#getIndexOfNode(INode)
	 */
	protected final int getIndexOfNode(final INode node) {
		return graphInfo.getIndexOfNode(node);
	}

	/**
	 * Checks if the given node exists in the graph. This implementation throws an exception.
	 *
	 * @param node to be checked for containment.
	 *
	 * @return <code>true</code> if <code>node</code> is contained in this graph; <code>false</code>, otherwise.
	 *
	 * @pre node != null
	 */
	protected final boolean containsNode(final INode node) {
		return getNodes().contains(node);
	}

	/**
	 * @see AbstractDirectedGraph#shapeChanged()
	 */
	protected final void shapeChanged() {
		super.shapeChanged();
		graphInfo.shapeChanged();
	}
}

// End of File
