/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
