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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

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
	final Map<IEdgeLabel, Collection<T>> label2inNodes = new HashMap<IEdgeLabel, Collection<T>>();

	/**
	 * This maps nodes to maps that map labels to nodes that can reachable the key node via an edge with the key label.
	 */
	final Map<IEdgeLabel, Collection<T>> label2outNodes = new HashMap<IEdgeLabel, Collection<T>>();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param preds is the reference to the collection of predecessors.
	 * @param succs is the reference to the collection of successors.
	 * @pre preds != null and succs != null
	 */
	protected EdgeLabelledNode(final Collection<T> preds, final Collection<T> succs) {
		super(preds, succs);
	}

	/**
	 * @see IEdgeLabelledNode#getIncomingEdgeLabels()
	 */
	public Collection<IEdgeLabel> getIncomingEdgeLabels() {
		return Collections.unmodifiableCollection(label2inNodes.keySet());
	}

	/**
	 * @see IEdgeLabelledNode#getOutGoingEdgeLabels()
	 */
	public Collection<IEdgeLabel> getOutGoingEdgeLabels() {
		return Collections.unmodifiableCollection(label2outNodes.keySet());
	}

	/**
	 * @see IEdgeLabelledNode#getPredsViaEdgesLabelled(IEdgeLabel)
	 */
	public Collection<T> getPredsViaEdgesLabelled(final IEdgeLabel label) {
		return (Collection) MapUtils.getObject(label2inNodes, label, Collections.emptySet());
	}

	/**
	 * @see IEdgeLabelledNode#getSuccsViaEdgesLabelled(IEdgeLabel)
	 */
	public Collection<T> getSuccsViaEdgesLabelled(final IEdgeLabel label) {
		return (Collection) MapUtils.getObject(label2outNodes, label, Collections.emptySet());
	}

	/**
	 * @see IEdgeLabelledNode#hasIncomingEdgeLabelled(IEdgeLabel)
	 */
	public boolean hasIncomingEdgeLabelled(final IEdgeLabel label) {
		return label2inNodes.containsKey(label);
	}

	/**
	 * @see IEdgeLabelledNode#hasOutgoingEdgeLabelled(IEdgeLabel)
	 */
	public boolean hasOutgoingEdgeLabelled(final IEdgeLabel label) {
		return label2outNodes.containsKey(label);
	}
}

// End of File
