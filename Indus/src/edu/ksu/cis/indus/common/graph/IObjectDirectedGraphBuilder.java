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

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;

import java.util.Collection;

/**
 * This interface is used to build graphs.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of nodes in the built graph.
 * @param <O> the type of objects stored in the nodes in the build graph.
 */
public interface IObjectDirectedGraphBuilder<N extends IObjectNode<N, O>, O> {

	/**
	 * Adds an edge from the nodes representing the nodes in <code>nodes</code> to the node representing <code>dest</code>.
	 * 
	 * @param nodes are the nodes in the originating graph.
	 * @param dest is the node in the originating graph.
	 */
	void addEdgeFromTo(@NonNull @NonNullContainer @Immutable Collection<O> nodes, @Immutable O dest);

	/**
	 * Adds an edge from the node representing <code>src</code> to the nodes representing the object in <code>dests</code>.
	 * 
	 * @param src is the node in the originating graph.
	 * @param dests are the nodes in the originating graph.
	 */
	void addEdgeFromTo(@Immutable O src, @NonNull @NonNullContainer @Immutable Collection<O> dests);

	/**
	 * Adds an edge from the node representing <code>src</code> the node representing <code>dest</code>.
	 * 
	 * @param src node in the originating graph.
	 * @param dest node in the originating graph.
	 */
	void addEdgeFromTo(final O src, final O dest);

	/**
	 * Create the graph to be built. This method should be called before starting to build the graph.
	 */
	void createGraph();

	/**
	 * Creates a node to represent the given object. This method is needed to create a graph with one node.
	 * 
	 * @param obj to be represented.
	 */
	void createNode(@Immutable O obj);

	/**
	 * Finish up the built graph.
	 */
	void finishBuilding();

	/**
	 * Retrieves the built graph.
	 * 
	 * @return the build graph.
	 */
	@NonNull IObjectDirectedGraph<N, O> getBuiltGraph();
}

// End of File
