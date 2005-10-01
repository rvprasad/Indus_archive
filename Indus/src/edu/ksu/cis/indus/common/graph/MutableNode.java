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
	 * @pre preds != null and succs != null
	 */
	protected MutableNode(final Collection<T> preds, final Collection<T> succs) {
		super(preds, succs);
	}

	/**
	 * @see IMutableNode#addPredecessor(INode)
	 */
	public final boolean addPredecessor(final T node) {
		return predecessors.add(node);
	}

	/**
	 * @see IMutableNode#addSuccessor(INode)
	 */
	public final boolean addSuccessor(final T node) {
		return successors.add(node);
	}

	/**
	 * @see IMutableNode#removePredecessor(INode)
	 */
	public boolean removePredecessor(final T node) {
		return predecessors.remove(node);
	}

	/**
	 * @see IMutableNode#removeSuccessor(INode)
	 */
	public boolean removeSuccessor(final T node) {
		return successors.remove(node);
	}
}
