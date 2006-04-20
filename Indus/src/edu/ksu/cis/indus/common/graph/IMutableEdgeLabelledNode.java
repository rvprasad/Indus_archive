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

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This is the interface to the nodes in a mutable edge-labelled directed graphs. The methods of this interface should and
 * will only update the information at this node and <b>not</b> the nodes connected by the connected edges.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the subtype of this type.
 */
public interface IMutableEdgeLabelledNode<N extends IMutableEdgeLabelledNode<N>>
		extends IEdgeLabelledNode<N>, IMutableNode<N> {

	/**
	 * Adds an incoming edge from <code>node</code> to this node that is labelled with <code>label</code>.
	 * 
	 * @param label of the edge to be added.
	 * @param node is the source of the edge to be added.
	 * @return <code>true</code> if the edge did not exist and it was added; <code>false</code>, otherwise.
	 */
	boolean addIncomingEdgeLabelledFrom(@NonNull @Immutable IEdgeLabel label, @NonNull N node);

	/**
	 * Adds an outgoing edge to <code>node</code> from this node that is labelled with <code>label</code>.
	 * 
	 * @param label of the edge to be added.
	 * @param node is the destination of the edge to be added.
	 * @return <code>true</code> if the edge did not exist and it was added; <code>false</code>, otherwise.
	 */
	boolean addOutgoingEdgeLabelledTo(@NonNull @Immutable IEdgeLabel label, @NonNull N node);

	/**
	 * Removes the incoming edge from <code>node</code> to this node that is labelled with <code>label</code>.
	 * 
	 * @param label of the edge to be removed.
	 * @param node is the source of the edge to be removed.
	 * @return <code>true</code> if the edge did exist and it was removed; <code>false</code>, otherwise.
	 */
	boolean removeIncomingEdgeLabelledFrom(@NonNull @Immutable IEdgeLabel label, @NonNull N node);

	/**
	 * Removes all incoming edges of this node labelled with <code>label</code>.
	 * 
	 * @param label of the edges to be removed.
	 * @return <code>true</code> if all edges were removed; <code>false</code>, otherwise.
	 */
	boolean removeIncomingEdgesLabelled(@NonNull @Immutable IEdgeLabel label);

	/**
	 * Removes the outgoing edge to <code>node</code> from this node that is labelled with <code>label</code>.
	 * 
	 * @param label of the edge to be removed.
	 * @param node is the destination of the edge to be removed.
	 * @return <code>true</code> if the edge did exist and it was removed; <code>false</code>, otherwise.
	 */
	boolean removeOutgoingEdgeLabelledTo(@NonNull @Immutable IEdgeLabel label, @NonNull N node);

	/**
	 * Removes all outgoing edges from this node labelled with <code>label</code>.
	 * 
	 * @param label of the edges to be removed.
	 * @return <code>true</code> if all edges were removed; <code>false</code>, otherwise.
	 */
	boolean removeOutgoingEdgesLabelled(@NonNull @Immutable IEdgeLabel label);
}
