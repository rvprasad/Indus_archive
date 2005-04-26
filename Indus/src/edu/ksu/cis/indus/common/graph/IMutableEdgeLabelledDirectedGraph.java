
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

import edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraph.IEdgeLabelledNode;


/**
 * This is the interface to mutable edge-labelled directed graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IMutableEdgeLabelledDirectedGraph
  extends IMutableDirectedGraph {
	/**
	 * This is the interface to the nodes in a mutable edge-labelled directed graphs.  The methods of this  interface should
	 * and will only update the information at this node and <b>not</b> the nodes connected by the  connected edges.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public interface IMutableEdgeLabelledNode
	  extends IEdgeLabelledNode,
		  IMutableNode {
		/**
		 * Adds an incoming edge from <code>node</code> to this node that is labelled with <code>label</code>.
		 *
		 * @param label of the edge to be added.
		 * @param node is the source of the edge to be added.
		 *
		 * @return <code>true</code> if the edge did not exist and it was added; <code>false</code>, otherwise.
		 *
		 * @pre label != null and node != null
		 */
		boolean addIncomingEdgeLabelledFrom(IEdgeLabel label, IEdgeLabelledNode node);

		/**
		 * Adds an outgoing edge to <code>node</code> from this node that is labelled with <code>label</code>.
		 *
		 * @param label of the edge to be added.
		 * @param node is the destination of the edge to be added.
		 *
		 * @return <code>true</code> if the edge did not exist and it was added; <code>false</code>, otherwise.
		 *
		 * @pre label != null and node != null
		 */
		boolean addOutgoingEdgeLabelledTo(IEdgeLabel label, IEdgeLabelledNode node);

		/**
		 * Removes the incoming edge from <code>node</code> to this node that is labelled with <code>label</code>.
		 *
		 * @param label of the edge to be removed.
		 * @param node is the source of the edge to be removed.
		 *
		 * @return <code>true</code> if the edge did exist and it was removed; <code>false</code>, otherwise.
		 *
		 * @pre label != null and node != null
		 */
		boolean removeIncomingEdgeLabelledFrom(IEdgeLabel label, IEdgeLabelledNode node);

		/**
		 * Removes all incoming edges of this node labelled with <code>label</code>.
		 *
		 * @param label of the edges to be removed.
		 *
		 * @return <code>true</code> if all edges were removed; <code>false</code>, otherwise.
		 *
		 * @pre label != null
		 */
		boolean removeIncomingEdgesLabelled(IEdgeLabel label);

		/**
		 * Removes the outgoing edge to <code>node</code> from this node that is labelled with <code>label</code>.
		 *
		 * @param label of the edge to be removed.
		 * @param node is the destination of the edge to be removed.
		 *
		 * @return <code>true</code> if the edge did exist and it was removed; <code>false</code>, otherwise.
		 *
		 * @pre label != null and node != null
		 */
		boolean removeOutgoingEdgeLabelledTo(IEdgeLabel label, IEdgeLabelledNode node);

		/**
		 * Removes all outgoing edges from this node labelled with <code>label</code>.
		 *
		 * @param label of the edges to be removed.
		 *
		 * @return <code>true</code> if all edges were removed; <code>false</code>, otherwise.
		 *
		 * @pre label != null
		 */
		boolean removeOutgoingEdgesLabelled(IEdgeLabel label);
	}

	/**
	 * Adds an edge between the given nodes with the given label.
	 *
	 * @param src is the source node of the edge to be added.
	 * @param label is the edge label.
	 * @param dest is the destination node of the edge to be added.
	 *
	 * @return <code>true</code> if the edge did not exist and it was added; <code>false</code>, otherwise.
	 *
	 * @pre src != null and label != null and dest != null
	 */
	boolean addEdgeFromTo(IEdgeLabelledNode src, IEdgeLabel label, IEdgeLabelledNode dest);

	/**
	 * Removes all edges in this graph that are  labelled with <code>label</code>. This updates the source and destination of
	 * the edges.
	 *
	 * @param label of the edges to be removed.
	 *
	 * @return <code>true</code> if all edges were removed; <code>false</code>, otherwise.
	 *
	 * @pre label != null
	 */
	boolean removeAllEdgesLabelled(IEdgeLabel label);

	/**
	 * Removes all edges from <code>node</code> that are labelled with <code>label</code>.  This updates the destinations of
	 * the edges.
	 *
	 * @param label of the edge to be removed.
	 * @param node is the source of the edge to be removed.
	 *
	 * @return <code>true</code> if there were such edges and they were all removed; <code>false</code>, otherwise.
	 *
	 * @pre label != null and node != null
	 */
	boolean removeAllEdgesLabelledFrom(IEdgeLabel label, IEdgeLabelledNode node);

	/**
	 * Removes all edges to <code>node</code> that are labelled with <code>label</code>.  This updates the sources of the
	 * edges.
	 *
	 * @param label of the edge to be removed.
	 * @param node is the destination of the edge to be removed.
	 *
	 * @return <code>true</code> if there were such edges and they were all removed; <code>false</code>, otherwise.
	 *
	 * @pre label != null and node != null
	 */
	boolean removeAllEdgesLabelledTo(IEdgeLabel label, IEdgeLabelledNode node);

	/**
	 * Removes the edge labelled <code>label</code> between <code>source</code> and <code>destination</code>.
	 *
	 * @param source of the edge to be removed.
	 * @param label of the edge to be removed
	 * @param destination of the edge to be removed.
	 *
	 * @return <code>true</code> if there was such an edge and it was removed; <code>false</code>, otherwise.
	 *
	 * @pre source != null and destination != null and label != null
	 */
	boolean removeEdgeFromTo(IEdgeLabelledNode source, IEdgeLabel label, IEdgeLabelledNode destination);
}

// End of File