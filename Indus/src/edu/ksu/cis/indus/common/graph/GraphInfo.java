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

import gnu.trove.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a common implementation that maintains graph information.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the type of the nodes for which this object maintain information.
 */
public class GraphInfo<N extends INode<N>> {

	/**
	 * This maps nodes to their indices in the node list of this graph.
	 * 
	 * @invariant node2indices.oclIsTypeOf(Map(INode, int))
	 */
	private final TObjectIntHashMap node2index = new TObjectIntHashMap();

	/**
	 * The sequence of nodes in this graph. They are stored in the order that the nodes are created.
	 * 
	 * @invariant nodes.oclIsTypeOf(Sequence(INode))
	 */
	private final List<N> nodes = new ArrayList<N>();

	/**
	 * Retrieves the index of the given node in the sequence of nodes in the associated graph.
	 * 
	 * @param node of interest.
	 * @return the index of the given node.
	 * @pre node != null
	 */
	public final int getIndexOfNode(final N node) {
		final int _result;
		assert getNodes().contains(node);

		if (node2index.containsKey(node)) {
			_result = node2index.get(node);
		} else {
			_result = getNodes().indexOf(node);
			node2index.put(node, _result);
		}
		return _result;
	}

	/**
	 * Retrieves the nodes maintained by this object.
	 * 
	 * @return a collection of nodes.
	 * @post result != null
	 */
	public final List<N> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * Removes the given node from the collection of nodes maintained by this object.
	 * 
	 * @param node of interest.
	 * @return <code>true</code> if <code>node</code> was maintained and was removed; <code>false</code>, otherwise.
	 */
	public boolean removeNode(final N node) {
		node2index.remove(node);
		return nodes.remove(node);
	}

	/**
	 * Resets internal caches.
	 */
	public void shapeChanged() {
		node2index.clear();
	}

	/**
	 * Add the given node to the collection of nodes maintained by this object.
	 * 
	 * @param node to be maintained.
	 * @return <code>true</code> if the node was new; <code>false</code>, otherwise.
	 */
	protected final boolean addNode(final N node) {
		final boolean _result = nodes.add(node);

		if (_result) {
			node2index.put(node, nodes.size() - 1);
		}
		return _result;
	}
}

// End of File
