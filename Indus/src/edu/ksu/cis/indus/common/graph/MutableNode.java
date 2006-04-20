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
