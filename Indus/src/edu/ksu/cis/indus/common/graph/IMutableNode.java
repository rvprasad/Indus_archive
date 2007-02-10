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

/**
 * This is the interface to a node in the mutable directed graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
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
	boolean addPredecessor(@NonNull @Immutable final T node);

	/**
	 * Adds a successor to this node.
	 * 
	 * @param node is the node to be added as the successor.
	 * @return <code>true</code> if the successor was added while not existing previously; <code>false</code>, otherwise.
	 * @post self.getSuccsOf()->includes(node)
	 * @post not self$pre.getSuccsOf()->includes(node)
	 */
	boolean addSuccessor(@NonNull @Immutable final T node);

	/**
	 * Removes a predecessor to this node.
	 * 
	 * @param node is the node to be added as the predecessor.
	 * @return <code>true</code> if the predecessor existed and it was removed; <code>false</code>, otherwise.
	 * @post not self.getPredsOf()->includes(node)
	 * @post self$pre.getPredsOf()->includes(node)
	 */
	boolean removePredecessor(@NonNull @Immutable final T node);

	/**
	 * Removes a successor to this node.
	 * 
	 * @param node is the node to be added as the successor.
	 * @return <code>true</code> if the successor existed and it was removed; <code>false</code>, otherwise.
	 * @post not self.getSuccsOf()->includes(node)
	 * @post self$pre.getSuccsOf()->includes(node)
	 */
	boolean removeSuccessor(@NonNull @Immutable final T node);
}
