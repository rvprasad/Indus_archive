
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

import edu.ksu.cis.indus.common.graph.IDirectedGraph.INode;


/**
 * This is the interface to mutable directed graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IMutableDirectedGraph {
	/**
	 * This is the interface to a node in the mutable directed graph.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public interface IMutableNode
	  extends INode {
		/**
		 * Adds a predecessor to this node.
		 *
		 * @param node is the node to be added as the predecessor.
		 *
		 * @return <code>true</code> if the predecessor was added while not existing previously; <code>false</code>,
		 * 		   otherwise.
		 *
		 * @post self.getPredsOf()->includes(node)
		 * @post not self$pre.getPredsOf()->includes(node)
		 */
		boolean addPredecessor(final INode node);

		/**
		 * Adds a successor to this node.
		 *
		 * @param node is the node to be added as the successor.
		 *
		 * @return <code>true</code> if the successor was added while not existing previously; <code>false</code>, otherwise.
		 *
		 * @post self.getSuccsOf()->includes(node)
		 * @post not self$pre.getSuccsOf()->includes(node)
		 */
		boolean addSuccessor(final INode node);

		/**
		 * Removes a predecessor to this node.
		 *
		 * @param node is the node to be added as the predecessor.
		 *
		 * @return <code>true</code> if the predecessor existed and it was removed; <code>false</code>, otherwise.
		 *
		 * @post not self.getPredsOf()->includes(node)
		 * @post self$pre.getPredsOf()->includes(node)
		 */
		boolean removePredecessor(final INode node);

		/**
		 * Removes a successor to this node.
		 *
		 * @param node is the node to be added as the successor.
		 *
		 * @return <code>true</code> if the successor existed and it was removed; <code>false</code>, otherwise.
		 *
		 * @post not self.getSuccsOf()->includes(node)
		 * @post self$pre.getSuccsOf()->includes(node)
		 */
		boolean removeSuccessor(final INode node);
	}

	/**
	 * Adds a directed edge between the given nodes.  Both the nodes should have been obtained by calling
	 * <code>getNode()</code> on this object.
	 *
	 * @param src is the source of the edge.
	 * @param dest is the destination of the edge.
	 *
	 * @return <code>true</code> if an edge was added; <code>false</code>, otherwise.
	 *
	 * @pre src != null and dest != null
	 * @post src.getSuccsOf()->includes(dest) and dest.getPredsOf()->includes(src)
	 */
	boolean addEdgeFromTo(final INode src, final INode dest);

	/**
	 * Adds the given node to the graph.
	 *
	 * @param node to be added.
	 *
	 * @return <code>true</code> if it did not previously occur and it was added; <code>false</code>, otherwise.
	 *
	 * @post not getNodes()->includes(node)
	 */
	boolean addNode(final INode node);

	/**
	 * Removes a directed edge between the given nodes.  Both the nodes should have been obtained by calling
	 * <code>getNode()</code> on this object.
	 *
	 * @param src is the source of the edge.
	 * @param dest is the destination of the edge.
	 *
	 * @return <code>true</code> if an edge was removed; <code>false</code>, otherwise.
	 *
	 * @pre src != null and dest != null
	 * @post not src.getSuccsOf()->includes(dest) and not dest.getPredsOf()->includes(src)
	 */
	boolean removeEdgeFromTo(final INode src, final INode dest);

	/**
	 * Removes the given node from the graph.
	 *
	 * @param node to be removed.
	 *
	 * @return <code>true</code> if the node was removed; <code>false</code>, otherwise.
	 *
	 * @pre node != null
	 */
	boolean removeNode(final INode node);
}

// End of File
