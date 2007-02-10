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

import java.util.Collection;
import java.util.Collections;

/**
 * This is an abstract non-edge-labelled implementation of INode.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the sub type of this type.
 */
public class Node<T extends Node<T>>
		implements INode<T> {

	/**
	 * The collection of nodes which precede this node in the graph.
	 */
	@NonNull @NonNullContainer protected final Collection<T> predecessors;

	/**
	 * The collection of nodes which succeed this node in the graph.
	 */
	@NonNull @NonNullContainer protected final Collection<T> successors;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param preds is the reference to the collection of predecessors.
	 * @param succs is the reference to the collection of successors.
	 */
	public Node(@Immutable @NonNull @NonNullContainer final Collection<T> preds,
			@Immutable @NonNull @NonNullContainer final Collection<T> succs) {
		super();
		this.predecessors = preds;
		this.successors = succs;
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional public Collection<T> getPredsOf() {
		return Collections.unmodifiableCollection(predecessors);
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional public final Collection<T> getSuccsNodesInDirection(final boolean forward) {
		Collection<T> _result;

		if (forward) {
			_result = getSuccsOf();
		} else {
			_result = getPredsOf();
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional public Collection<T> getSuccsOf() {
		return Collections.unmodifiableCollection(successors);
	}
}

// End of File
