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
 * This is the interface to mutable edge-labelled directed graphs.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the node type of this graph.
 */
public interface IMutableEdgeLabelledDirectedGraph<N extends IMutableEdgeLabelledNode<N>>
		extends IMutableDirectedGraph<N> {

	/**
	 * Adds an edge between the given nodes with the given label.
	 * 
	 * @param src is the source node of the edge to be added.
	 * @param label is the edge label.
	 * @param dest is the destination node of the edge to be added.
	 * @return <code>true</code> if the edge did not exist and it was added; <code>false</code>, otherwise.
	 */
	boolean addEdgeFromTo(@NonNull N src, @NonNull @Immutable IEdgeLabel label, @NonNull N dest);

	/**
	 * Removes all edges in this graph that are labelled with <code>label</code>. This updates the source and destination
	 * of the edges.
	 * 
	 * @param label of the edges to be removed.
	 * @return <code>true</code> if all edges were removed; <code>false</code>, otherwise.
	 */
	boolean removeAllEdgesLabelled(@NonNull @Immutable IEdgeLabel label);

	/**
	 * Removes all edges from <code>node</code> that are labelled with <code>label</code>. This updates the destinations
	 * of the edges.
	 * 
	 * @param label of the edge to be removed.
	 * @param node is the source of the edge to be removed.
	 * @return <code>true</code> if there were such edges and they were all removed; <code>false</code>, otherwise.
	 */
	boolean removeAllEdgesLabelledFrom(@NonNull @Immutable IEdgeLabel label, @NonNull N node);

	/**
	 * Removes all edges to <code>node</code> that are labelled with <code>label</code>. This updates the sources of the
	 * edges.
	 * 
	 * @param label of the edge to be removed.
	 * @param node is the destination of the edge to be removed.
	 * @return <code>true</code> if there were such edges and they were all removed; <code>false</code>, otherwise.
	 */
	boolean removeAllEdgesLabelledTo(@NonNull @Immutable IEdgeLabel label, @NonNull N node);

	/**
	 * Removes the edge labelled <code>label</code> between <code>source</code> and <code>destination</code>.
	 * 
	 * @param source of the edge to be removed.
	 * @param label of the edge to be removed
	 * @param destination of the edge to be removed.
	 * @return <code>true</code> if there was such an edge and it was removed; <code>false</code>, otherwise.
	 */
	boolean removeEdgeFromTo(@NonNull N source, @NonNull @Immutable IEdgeLabel label,
			@NonNull  N destination);
}

// End of File
