
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
 * This interface is intended to be used to view some data as an edge-labelled directed graph without actually constructing
 * the graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IEdgeLabelledDirectedGraphView
  extends IDirectedGraphView {
	/**
	 * Retrieves the labels of the incoming edges of the given node.
	 *
	 * @param node of interest.
	 *
	 * @return the labels on the incoming edges of the given node.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(ILabel))
	 */
	Collection getIncomingEdgeLabels(INode node);

	/**
	 * Retrieves the labels of the outgoing edges of the given node.
	 *
	 * @param node of interest.
	 *
	 * @return the labels on the outgoing edges of the given node.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(ILabel))
	 */
	Collection getOutgoingEdgeLabels(INode node);

	/**
	 * Retrieves the source nodes of the edges that are labelled with <code>label</code> and are incident on the given node.
	 *
	 * @param node of interest.
	 * @param label of the incoming edges onto <code>node</code>.
	 *
	 * @return a collection of nodes.
	 *
	 * @pre label != null and node != null
	 */
	Collection getPredsViaEdgesLabelled(INode node, IEdgeLabel label);

	/**
	 * Retrieves the destination nodes of the outgoing edges from the given node that are labelled with <code>label</code>.
	 *
	 * @param node of interest.
	 * @param label of the outgoing edges from <code>node</code>.
	 *
	 * @return a collection of nodes.
	 *
	 * @pre label != null and node != null
	 */
	Collection getSuccsViaEdgesLabelled(INode node, IEdgeLabel label);
}

// End of File
