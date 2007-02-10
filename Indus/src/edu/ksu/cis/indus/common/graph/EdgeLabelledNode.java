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
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.collections.MapUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is an implementation of edge-labelled nodes.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the sub type of this type.
 */
public class EdgeLabelledNode<T extends EdgeLabelledNode<T>>
		extends Node<T>
		implements IEdgeLabelledNode<T> {

	/**
	 * This maps nodes to maps that map labels to nodes reachable from the key node via an edge with the key label.
	 */
	@NonNullContainer final Map<IEdgeLabel, Collection<T>> label2inNodes = new HashMap<IEdgeLabel, Collection<T>>();

	/**
	 * This maps nodes to maps that map labels to nodes that can reachable the key node via an edge with the key label.
	 */
	@NonNullContainer final Map<IEdgeLabel, Collection<T>> label2outNodes = new HashMap<IEdgeLabel, Collection<T>>();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param preds is the reference to the collection of predecessors.
	 * @param succs is the reference to the collection of successors.
	 */
	protected EdgeLabelledNode(@Immutable @NonNull @NonNullContainer final Collection<T> preds,
			@Immutable @NonNull @NonNullContainer final Collection<T> succs) {
		super(preds, succs);
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional public Collection<IEdgeLabel> getIncomingEdgeLabels() {
		return Collections.unmodifiableCollection(label2inNodes.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional public Collection<IEdgeLabel> getOutGoingEdgeLabels() {
		return Collections.unmodifiableCollection(label2outNodes.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional public Collection<T> getPredsViaEdgesLabelled(@NonNull final IEdgeLabel label) {
		return MapUtils.queryCollection(label2inNodes, label);
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional public Collection<T> getSuccsViaEdgesLabelled(@NonNull final IEdgeLabel label) {
		return MapUtils.queryCollection(label2outNodes, label);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public final boolean hasIncomingEdgeLabelled(@NonNull final IEdgeLabel label) {
		return label2inNodes.containsKey(label);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public final boolean hasOutgoingEdgeLabelled(@NonNull final IEdgeLabel label) {
		return label2outNodes.containsKey(label);
	}
}

// End of File
