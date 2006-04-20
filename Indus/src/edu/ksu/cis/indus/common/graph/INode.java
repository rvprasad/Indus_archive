/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.annotations.Functional.AccessSpecifier;

import java.util.Collection;

/**
 * The interface to be implemented by node objects occuring in <code>DirectedGraph</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the sub type of this type.
 */
public interface INode<T extends INode<T>>
		extends IDirectedGraphView.INode {

	/**
	 * Retrieves the predecessors of this node.
	 * 
	 * @return the collection of predecessors of this node.
	 * @post result->forall(o | o.getSuccsOf()->includes(this))
	 */
	@NonNull @NonNullContainer @Functional(level = AccessSpecifier.PUBLIC) Collection<T> getPredsOf();

	/**
	 * Retrieves the successors of this node.
	 * 
	 * @param forward <code>true</code> implies forward direction(successors); <code>false</code> implies backward
	 *            direction (predecessors).
	 * @return the collection of successors of this node.
	 * @post forward == true implies result->forall(o | o.getPredsOf()->includes(this))
	 * @post forward == false implies result->forall(o | o.getSuccsOf()->includes(this))
	 */
	@NonNull @NonNullContainer @Functional(level = AccessSpecifier.PUBLIC) Collection<T> getSuccsNodesInDirection(
			boolean forward);

	/**
	 * Retrieves the set of successor nodes of this node.
	 * 
	 * @return the collection of successor nodes(<code>INode</code>) of this node.
	 * @post result->forall(o | o.getPredsOf()->includes(this))
	 */
	@NonNull @NonNullContainer @Functional(level = AccessSpecifier.PUBLIC) Collection<T> getSuccsOf();
}
