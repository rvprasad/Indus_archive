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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Marker;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This interface is intended to be used to view some data as a directed graph without actually constructing the graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of the nodes in this view.
 */
public interface IDirectedGraphView<N extends IDirectedGraphView.INode> {

	/**
	 * This is a marker interface for the node of the graph being navigated.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	@Marker public interface INode {
		// empty
	}

	/**
	 * Retrieves the predecessors of the given node.
	 * 
	 * @param node of interest.
	 * @return the collection of predecessors of the given node.
	 * @post result->forall(o | getSuccsOf(o)->includes(node))
	 */
	@Functional @NonNull Collection<N> getPredsOf(@NonNull final N node);

	/**
	 * Retrieves the successors of the given node.
	 * 
	 * @param node of interest.
	 * @return the collection of successors of the given node.
	 * @post result->forall(o | getPredsOf(o)->includes(node))
	 */
	@Functional @NonNull Collection<N> getSuccsOf(@NonNull final N node);
}

// End of File
