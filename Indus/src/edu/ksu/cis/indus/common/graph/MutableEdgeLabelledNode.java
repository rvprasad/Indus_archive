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

import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * This is an implementation of the node in mutable edge-labelled directed graphs.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the subtype of this type.
 */
public class MutableEdgeLabelledNode<T extends MutableEdgeLabelledNode<T>>
		extends EdgeLabelledNode<T>
		implements IMutableEdgeLabelledNode<T> {

	/**
	 * Creates an instance of this class.
	 * 
	 * @param preds is the reference to the collection of predecessors.
	 * @param succs is the reference to the collection of successors.
	 * @pre preds != null and succs != null
	 */
	protected MutableEdgeLabelledNode(final Collection<T> preds, final Collection<T> succs) {
		super(preds, succs);
	}

	/**
	 * Removes all nodes that were related to this node via the given label by updating the given map and collection.
	 * 
	 * @param <T1> the type of objects in the collection.
	 * @param label of interest.
	 * @param map that maps labels to predecessor or successor nodes. This is updated.
	 * @param col is a collection of predecessor or successor nodes of this node. This is updated.
	 * @return <code>true</code> if the node was related to this node and it as removed; <code>false</code>, otherwise.
	 * @pre map != null and col != null
	 */
	private static <T1> boolean removedEdgesLabelled(final IEdgeLabel label, final Map<IEdgeLabel, Collection<T1>> map,
			final Collection<T1> col) {
		final boolean _result = map.remove(label) != null;
		retainAllIn(col, map.values());
		return _result;
	}

	/**
	 * Removes the relation between this and the given node by updating the given map and collection.
	 * 
	 * @param <T1> the node type.
	 * @param node of interest.
	 * @param map that maps labels to predecessor or successor nodes. This is updated.
	 * @param col is a collection of predecessor or successor nodes of this node. This is updated.
	 * @return <code>true</code> if the node was related to this node and it as removed; <code>false</code>, otherwise.
	 * @pre map != null and col != null
	 */
	private static <T1> boolean removeNode(final T1 node, final Map<IEdgeLabel, Collection<T1>> map, final Collection<T1> col) {
		final Iterator<IEdgeLabel> _i = map.keySet().iterator();
		final int _iEnd = map.keySet().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IEdgeLabel _k = _i.next();
			map.get(_k).remove(node);
		}
		return col.remove(node);
	}

	/**
	 * Retains all elements in <code>col</code> that exist in any collection in <code>collections</code>.
	 * 
	 * @param <T1> the type of objects in the collection.
	 * @param col to be updated.
	 * @param collections is a collection of collections.
	 * @pre col != null and collections != null
	 */
	private static <T1> void retainAllIn(final Collection<T1> col, final Collection<Collection<T1>> collections) {
		for (final Collection<T1> _c : collections) {
			col.retainAll(_c);			
		}
	}
	
	/**
	 * @see IMutableEdgeLabelledNode#addIncomingEdgeLabelledFrom(IEdgeLabel, IMutableEdgeLabelledNode)
	 */
	public boolean addIncomingEdgeLabelledFrom(final IEdgeLabel label, final T node) {
		predecessors.add(node);
		return MapUtils.putIntoCollectionInMapUsingFactory(label2inNodes, label, node, SetUtils.<T>getFactory());
	}

	/**
	 * @see IMutableEdgeLabelledNode#addOutgoingEdgeLabelledTo(IEdgeLabel, IMutableEdgeLabelledNode)
	 */
	public boolean addOutgoingEdgeLabelledTo(final IEdgeLabel label, final T node) {
		successors.add(node);
		return MapUtils.putIntoCollectionInMapUsingFactory(label2outNodes, label, node, SetUtils.<T>getFactory());
	}

	/**
	 * @see IMutableNode#addPredecessor(IMutableNode)
	 */
	public final boolean addPredecessor(final T node) {
		return addIncomingEdgeLabelledFrom(IEdgeLabel.DUMMY_LABEL, node);
	}

	/**
	 * @see IMutableNode#addSuccessor(IMutableNode)
	 */
	public final boolean addSuccessor(final T node) {
		return addOutgoingEdgeLabelledTo(IEdgeLabel.DUMMY_LABEL, node);
	}

	/**
	 * @see IMutableEdgeLabelledNode#removeIncomingEdgeLabelledFrom(IEdgeLabel, IMutableEdgeLabelledNode)
	 */
	public boolean removeIncomingEdgeLabelledFrom(final IEdgeLabel label, final T node) {
		return removeEdgesLabelledForViaUpdate(label, node, label2inNodes, predecessors);
	}

	/**
	 * @see IMutableEdgeLabelledNode#removeIncomingEdgesLabelled(IEdgeLabel)
	 */
	public boolean removeIncomingEdgesLabelled(final IEdgeLabel label) {
		return removedEdgesLabelled(label, label2inNodes, predecessors);
	}

	/**
	 * @see IMutableEdgeLabelledNode#removeOutgoingEdgeLabelledTo(IEdgeLabel, IMutableEdgeLabelledNode)
	 */
	public boolean removeOutgoingEdgeLabelledTo(final IEdgeLabel label, final T node) {
		return removeEdgesLabelledForViaUpdate(label, node, label2outNodes, successors);
	}

	/**
	 * @see IMutableEdgeLabelledNode#removeOutgoingEdgesLabelled(IEdgeLabel)
	 */
	public boolean removeOutgoingEdgesLabelled(final IEdgeLabel label) {
		return removedEdgesLabelled(label, label2outNodes, successors);
	}

	/**
	 * @see IMutableNode#removePredecessor(IMutableNode)
	 */
	public boolean removePredecessor(final T node) {
		return removeNode(node, label2inNodes, predecessors);
	}

	/**
	 * @see IMutableNode#removeSuccessor(IMutableNode)
	 */
	public boolean removeSuccessor(final T node) {
		return removeNode(node, label2outNodes, successors);
	}

	/**
	 * Removes the edges with the given label that to/from the given node by updating the given map and collection. The map
	 * and the collection dictate the to/from direction.
	 * 
	 * @param label of interest.
	 * @param node of interest.
	 * @param map that maps labels to predecessor or successor nodes. This is updated.
	 * @param col is a collection of predecessor or successor nodes of this node. This is updated.
	 * @return <code>true</code> if the node existed and was removed; <code>false</code>, otherwise.
	 * @pre map != null and col != null
	 */
	private boolean removeEdgesLabelledForViaUpdate(final IEdgeLabel label, final T node,
			final Map<IEdgeLabel, Collection<T>> map, final Collection<T> col) {
		final Collection<T> _t = MapUtils.queryObject(map, label, Collections.EMPTY_SET);
		final boolean _result;

		if (_t.isEmpty()) {
			_result = false;
		} else {
			_result = _t.remove(node);

			if (_result) {
				if (_t.isEmpty()) {
					map.remove(label);
				}
				retainAllIn(col, map.values());
			}
		}
		return _result;
	}
}
