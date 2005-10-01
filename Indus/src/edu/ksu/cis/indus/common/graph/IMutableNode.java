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

/**
 * This is the interface to a node in the mutable directed graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T> the sub type of this type.
 */
public interface IMutableNode<T extends IMutableNode<T>>
		extends INode<T> {

	/**
	 * Adds a predecessor to this node.
	 * 
	 * @param node is the node to be added as the predecessor.
	 * @return <code>true</code> if the predecessor was added while not existing previously; <code>false</code>,
	 *         otherwise.
	 * @post self.getPredsOf()->includes(node)
	 * @post not self$pre.getPredsOf()->includes(node)
	 */
	boolean addPredecessor(final T node);

	/**
	 * Adds a successor to this node.
	 * 
	 * @param node is the node to be added as the successor.
	 * @return <code>true</code> if the successor was added while not existing previously; <code>false</code>, otherwise.
	 * @post self.getSuccsOf()->includes(node)
	 * @post not self$pre.getSuccsOf()->includes(node)
	 */
	boolean addSuccessor(final T node);

	/**
	 * Removes a predecessor to this node.
	 * 
	 * @param node is the node to be added as the predecessor.
	 * @return <code>true</code> if the predecessor existed and it was removed; <code>false</code>, otherwise.
	 * @post not self.getPredsOf()->includes(node)
	 * @post self$pre.getPredsOf()->includes(node)
	 */
	boolean removePredecessor(final T node);

	/**
	 * Removes a successor to this node.
	 * 
	 * @param node is the node to be added as the successor.
	 * @return <code>true</code> if the successor existed and it was removed; <code>false</code>, otherwise.
	 * @post not self.getSuccsOf()->includes(node)
	 * @post self$pre.getSuccsOf()->includes(node)
	 */
	boolean removeSuccessor(final T node);
}
