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

import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This is the interface to mutable directed graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the sub type of this type.
 */
public interface IMutableDirectedGraph<N extends IMutableNode<N>> {

	/**
	 * Adds a directed edge between the given nodes. Both the nodes should have been obtained by calling
	 * <code>getNode()</code> on this object.
	 * 
	 * @param src is the source of the edge.
	 * @param dest is the destination of the edge.
	 * @return <code>true</code> if an edge was added; <code>false</code>, otherwise.
	 * @post src.getSuccsOf()->includes(dest) and dest.getPredsOf()->includes(src)
	 */
	boolean addEdgeFromTo(@NonNull final N src, @NonNull final N dest);

	/**
	 * Adds the given node to the graph.
	 * 
	 * @param node to be added.
	 * @return <code>true</code> if it did not previously occur and it was added; <code>false</code>, otherwise.
	 * @post not getNodes()->includes(node)
	 */
	boolean addNode(@NonNull final N node);

	/**
	 * Removes a directed edge between the given nodes. Both the nodes should have been obtained by calling
	 * <code>getNode()</code> on this object.
	 * 
	 * @param src is the source of the edge.
	 * @param dest is the destination of the edge.
	 * @return <code>true</code> if an edge was removed; <code>false</code>, otherwise.
	 * @post not src.getSuccsOf()->includes(dest) and not dest.getPredsOf()->includes(src)
	 */
	boolean removeEdgeFromTo(@NonNull final N src, @NonNull final N dest);

	/**
	 * Removes the given node from the graph.
	 * 
	 * @param node to be removed.
	 * @return <code>true</code> if the node was removed; <code>false</code>, otherwise.
	 */
	boolean removeNode(@NonNull final N node);
}

// End of File
