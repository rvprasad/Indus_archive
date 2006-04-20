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
