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
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.annotations.Functional.AccessSpecifier;

import java.util.Collection;

/**
 * This interface is intended to be used to view some data as an edge-labelled directed graph without actually constructing
 * the graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of the node in this view.
 */
public interface IEdgeLabelledDirectedGraphView<N extends IDirectedGraphView.INode>
		extends IDirectedGraphView<N> {

	/**
	 * Retrieves the labels of the incoming edges of the given node.
	 * 
	 * @param node of interest.
	 * @return the labels on the incoming edges of the given node.
	 */
	@Functional(level = AccessSpecifier.PUBLIC) @NonNull @NonNullContainer Collection<IEdgeLabel> getIncomingEdgeLabels(
			@NonNull N node);

	/**
	 * Retrieves the labels of the outgoing edges of the given node.
	 * 
	 * @param node of interest.
	 * @return the labels on the outgoing edges of the given node.
	 * @post result.oclIsKindOf(Collection(ILabel))
	 */
	@Functional(level = AccessSpecifier.PUBLIC) @NonNull @NonNullContainer Collection<IEdgeLabel> getOutgoingEdgeLabels(
			@NonNull N node);

	/**
	 * Retrieves the source nodes of the edges that are labelled with <code>label</code> and are incident on the given node.
	 * 
	 * @param node of interest.
	 * @param label of the incoming edges onto <code>node</code>.
	 * @return a collection of nodes.
	 */
	@Functional(level = AccessSpecifier.PUBLIC) @NonNull @NonNullContainer Collection<N> getPredsViaEdgesLabelled(
			@NonNull N node, @NonNull IEdgeLabel label);

	/**
	 * Retrieves the destination nodes of the outgoing edges from the given node that are labelled with <code>label</code>.
	 * 
	 * @param node of interest.
	 * @param label of the outgoing edges from <code>node</code>.
	 * @return a collection of nodes.
	 */
	@Functional(level = AccessSpecifier.PUBLIC) @NonNull @NonNullContainer Collection<N> getSuccsViaEdgesLabelled(
			@NonNull N node, @NonNull IEdgeLabel label);
}

// End of File
