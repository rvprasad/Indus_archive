
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

import java.util.Collection;


/**
 * This is the interface to an edge labelled directed graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IEdgeLabelledDirectedGraph
  extends IDirectedGraph {
	/**
	 * This is a marker interface to the edge labels of this graph.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public interface ILabel {
	}

	/**
	 * Retrieves the nodes into which the edge labelled with <code>label</code> from <code>node</code> lead into.
	 *
	 * @param node is the source node.
	 * @param label of the outgoing edge from <code>node</code>.
	 *
	 * @return a collection of nodes.
	 *
	 * @pre node != null and label != null
	 */
	Collection getDestOfOutgoingEdgesLabelled(final INode node, final ILabel label);

	/**
	 * Checks if there is an outgoing edge with the given label from the given node.
	 *
	 * @param node of interest.
	 * @param label of the outgoing edge.
	 *
	 * @return <code>true</code> if there is such an outgoing edge; <code>false</code>, otherwise.
	 *
	 * @pre node != null and label != null
	 */
	boolean hasOutgoingEdgeLabelled(final INode node, final ILabel label);
}

// End of File
