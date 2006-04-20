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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This is the interface to a node that resides in the edge-labelled directed graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> a subtype of this type.
 */
public interface IEdgeLabelledNode<T extends IEdgeLabelledNode<T>>
		extends INode<T> {

	/**
	 * Retrieves the labels of the incoming edges.
	 * 
	 * @return the labels on the incoming edges.
	 */
	@Functional @NonNull Collection<IEdgeLabel> getIncomingEdgeLabels();

	/**
	 * Retrieves the labels of the outgoing edges.
	 * 
	 * @return the labels on the outgoing edges.
	 */
	@Functional @NonNull Collection<IEdgeLabel> getOutGoingEdgeLabels();

	/**
	 * Retrieves the nodes from which the edge labelled with <code>label</code> are incident on this node.
	 * 
	 * @param label of the incoming edges onto this node.
	 * @return a collection of nodes.
	 */
	@Functional @NonNull Collection<T> getPredsViaEdgesLabelled(@NonNull final IEdgeLabel label);

	/**
	 * Retrieves the destination nodes of the outgoing edges from this node that are labelled with <code>label</code>.
	 * 
	 * @param label of the outgoing edges from this node.
	 * @return a collection of nodes.
	 */
	@Functional @NonNull Collection<T> getSuccsViaEdgesLabelled(@NonNull final IEdgeLabel label);

	/**
	 * Checks if there is an incoming edge with the given label on this node.
	 * 
	 * @param label of the outgoing edge.
	 * @return <code>true</code> if there is such an outgoing edge; <code>false</code>, otherwise.
	 */
	@Functional @NonNull boolean hasIncomingEdgeLabelled(@NonNull final IEdgeLabel label);

	/**
	 * Checks if there is an outgoing edge with the given label from this node.
	 * 
	 * @param label of the outgoing edge.
	 * @return <code>true</code> if there is such an outgoing edge; <code>false</code>, otherwise.
	 */
	@Functional @NonNull boolean hasOutgoingEdgeLabelled(@NonNull final IEdgeLabel label);
}
