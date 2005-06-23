
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

import edu.ksu.cis.indus.common.graph.IDirectedGraph.INode;

import java.util.Collection;
import java.util.Collections;


/**
 * This is an abstract non-edge-labelled implementation of INode.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class Node
  implements INode {
	/** 
	 * The collection of nodes which precede this node in the graph.
	 *
	 * @invariant predecessors != null
	 */
	protected final Collection predecessors;

	/** 
	 * The collection of nodes which succeed this node in the graph.
	 *
	 * @invariant successors != null
	 */
	protected final Collection successors;

	/**
	 * Creates an instance of this class.
	 *
	 * @param preds is the reference to the collection of predecessors.
	 * @param succs is the reference to the collection of successors.
	 *
	 * @pre preds != null and succs != null
	 */
	public Node(final Collection preds, final Collection succs) {
		super();
		this.predecessors = preds;
		this.successors = succs;
	}

	/**
	 * @see IDirectedGraph.INode#getPredsOf()
	 */
	public Collection getPredsOf() {
		return Collections.unmodifiableCollection(predecessors);
	}

	/**
	 * @see IDirectedGraph.INode#getSuccsNodesInDirection(boolean)
	 */
	public final Collection getSuccsNodesInDirection(final boolean forward) {
		Collection _result;

		if (forward) {
			_result = getSuccsOf();
		} else {
			_result = getPredsOf();
		}
		return _result;
	}

	/**
	 * @see IDirectedGraph.INode#getSuccsOf()
	 */
	public Collection getSuccsOf() {
		return Collections.unmodifiableCollection(successors);
	}
}

// End of File
