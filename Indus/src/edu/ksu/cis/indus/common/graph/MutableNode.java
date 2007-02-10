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
import edu.ksu.cis.indus.annotations.NonNullContainer;

import java.util.Collection;

/**
 * This class extends <code>INode</code> such that the resulting node can mutated.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> the subtype of this type.
 */
public class MutableNode<T extends MutableNode<T>>
		extends Node<T>
		implements IMutableNode<T> {

	/**
	 * Creates a new MutableNode object.
	 * 
	 * @param preds is the reference to the collection of predecessors.
	 * @param succs is the reference to the collection of successors.
	 */
	protected MutableNode(@NonNull @NonNullContainer final Collection<T> preds,
			@NonNull @NonNullContainer final Collection<T> succs) {
		super(preds, succs);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean addPredecessor(@Immutable @NonNull final T node) {
		return predecessors.add(node);
	}

	/**
	 * {@inheritDoc}
	 */
	public final boolean addSuccessor(@Immutable @NonNull final T node) {
		return successors.add(node);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removePredecessor(@Immutable @NonNull final T node) {
		return predecessors.remove(node);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeSuccessor(@Immutable @NonNull final T node) {
		return successors.remove(node);
	}
}
