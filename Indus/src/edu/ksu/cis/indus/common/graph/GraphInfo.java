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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.collections.ListOrderedSet;

import gnu.trove.TObjectIntHashMap;

import java.util.AbstractList;
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
	 */
	@NonNullContainer private final ListOrderedSet<N> nodes = new ListOrderedSet<N>();

	/**
	 * Creates an instance of this class.
	 */
	@Empty public GraphInfo() {
		super();
	}

	/**
	 * Retrieves the index of the given node in the sequence of nodes in the associated graph.
	 * 
	 * @param node of interest.
	 * @return the index of the given node.
	 */
	public final int getIndexOfNode(@NonNull @Immutable final N node) {
		final int _result;

		if (node2index.containsKey(node)) {
			_result = node2index.get(node);
		} else {
			_result = getNodes().indexOf(node);
			node2index.put(node, _result);
			assert _result != -1;
		}
		return _result;
	}

	/**
	 * Retrieves the nodes maintained by this object.
	 * 
	 * @return a collection of nodes.
	 */
	@NonNull @NonNullContainer @Functional public final List<N> getNodes() {
		return Collections.unmodifiableList(new AbstractList<N>() {

			@Override public N get(final int index) {
				return nodes.get(index);
			}

			@Override public int size() {
				return nodes.size();
			}

			@Override public boolean contains(Object o) {
				return nodes.contains(o);
			}

		});
	}

	/**
	 * Removes the given node from the collection of nodes maintained by this object.
	 * 
	 * @param node of interest.
	 * @return <code>true</code> if <code>node</code> was maintained and was removed; <code>false</code>, otherwise.
	 */
	public boolean removeNode(@NonNull @Immutable final N node) {
		node2index.remove(node);
		return nodes.remove(node);
	}

	/**
	 * Resets internal caches.
	 */
	public final void shapeChanged() {
		node2index.clear();
	}

	/**
	 * Add the given node to the collection of nodes maintained by this object.
	 * 
	 * @param node to be maintained.
	 * @return <code>true</code> if the node was new; <code>false</code>, otherwise.
	 */
	protected final boolean addNode(@NonNull @Immutable final N node) {
		final boolean _result = nodes.add(node);

		if (_result) {
			node2index.put(node, nodes.size() - 1);
		}
		return _result;
	}
}

// End of File
