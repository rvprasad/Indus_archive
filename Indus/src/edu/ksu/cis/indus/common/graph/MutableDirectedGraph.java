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
 * This class provides common implementations and methods to mutate a directed graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of the nodes in this graph.
 */
public class MutableDirectedGraph<N extends IMutableNode<N>>
		extends AbstractDirectedGraph<N>
		implements IMutableDirectedGraph<N> {

	/**
	 * This maintains the graph information.
	 */
	protected final GraphInfo<N> graphInfo;

	/**
	 * Creates a new AbstractDirectedGraph object.
	 */
	protected MutableDirectedGraph() {
		graphInfo = new GraphInfo<N>();
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param info maintains the information for this graph.
	 * @pre info != null
	 */
	protected MutableDirectedGraph(final GraphInfo<N> info) {
		graphInfo = info;
	}

	/**
	 * @see IMutableDirectedGraph#addEdgeFromTo(INode, INode)
	 */
	public boolean addEdgeFromTo(final N src, final N dest) {
		boolean _result = false;

		if (containsNode(src) && containsNode(dest)) {
			src.addSuccessor(dest);
			dest.addPredecessor(src);
			shapeChanged();
			_result = true;
		} else {
			throw new IllegalArgumentException("Either or both of the provided nodes do not exist in this graph.");
		}

		return _result;
	}

	/**
	 * @see IMutableDirectedGraph#addNode(INode)
	 */
	public boolean addNode(final N node) {
		return graphInfo.addNode(node);
	}

	/**
	 * @see IDirectedGraph#getNodes()
	 */
	public List<N> getNodes() {
		return graphInfo.getNodes();
	}

	/**
	 * @see IMutableDirectedGraph#removeEdgeFromTo(INode,INode)
	 */
	public boolean removeEdgeFromTo(final N src, final N dest) {
		boolean _result = false;

		if (containsNode(src) && containsNode(dest)) {
			src.removeSuccessor(dest);
			dest.removePredecessor(src);
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
	public final boolean removeNode(final N node) {
		final Collection<N> _succsOf = new ArrayList<N>(node.getSuccsOf());
		final Iterator<N> _i = _succsOf.iterator();
		final int _iEnd = _succsOf.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			_i.next().removePredecessor(node);
		}

		final Collection<N> _predsOf = new ArrayList<N>(node.getPredsOf());
		final Iterator<N> _j = _predsOf.iterator();
		final int _jEnd = _predsOf.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			_j.next().removeSuccessor(node);
		}
		shapeChanged();
		return graphInfo.removeNode(node);
	}

	/**
	 * Checks if the given node exists in the graph. This implementation throws an exception.
	 * 
	 * @param node to be checked for containment.
	 * @return <code>true</code> if <code>node</code> is contained in this graph; <code>false</code>, otherwise.
	 * @pre node != null
	 */
	protected final boolean containsNode(final N node) {
		return getNodes().contains(node);
	}

	/**
	 * @see AbstractDirectedGraph#getIndexOfNode(INode)
	 */
	@Override protected final int getIndexOfNode(final N node) {
		return graphInfo.getIndexOfNode(node);
	}

	/**
	 * @see AbstractDirectedGraph#shapeChanged()
	 */
	@Override protected final void shapeChanged() {
		super.shapeChanged();
		graphInfo.shapeChanged();
	}
}

// End of File
