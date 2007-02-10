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
 * This interface is used to build edge-labelled directed object graphs.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <O> the type of object stored in the nodes of this graph.
 */
public interface IEdgeLabelledObjectDirectedGraphBuilder<O> {

	/**
	 * Adds a labelled edge between each of the source nodes and the destination node.
	 * 
	 * @param sources are the source nodes.
	 * @param label is the edge label.
	 * @param dest is the destination node.
	 */
	void addEdgeFromTo(@NonNull @Immutable @NonNullContainer final Collection<O> sources,
			@NonNull @Immutable final Object label, @NonNull final O dest);

	/**
	 * Adds a labelled edge between the source node and each of the destination nodes.
	 * 
	 * @param src is the source node.
	 * @param label is the edge label.
	 * @param destinations are the destination nodes.
	 */
	void addEdgeFromTo(@NonNull final O src, @NonNull @Immutable final Object label,
			@NonNull @Immutable @NonNullContainer final Collection<O> destinations);

	/**
	 * Adds a labelled edge between 2 nodes.
	 * 
	 * @param src is the source node.
	 * @param label is the edge label.
	 * @param dest is the destination node.
	 */
	void addEdgeFromTo(@NonNull final O src, @NonNull @Immutable final Object label, @NonNull final O dest);
}

// End of File
