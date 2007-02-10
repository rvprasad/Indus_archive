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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This is a generic container of objects. The order in which the objects are added and removed can be configured. At present,
 * it supports LIFO and FIFO ordering. This affects the order in which the <code>getWork()</code> will return the work added
 * to this bag.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad</a>
 * @version $Revision$ $Date$
 * @param <T> the type of the work handled by this work bag.
 * @since Created: Thu Jul 25 18:37:24 2002.
 */
public interface IWorkBag<T> {

	/**
	 * Returns a work pieces.
	 * 
	 * @return a work piece.
	 * @post hasWork() implies self$pre->exists(o | result == o)
	 */
	T getWork();

	/**
	 * Adds all the work pieces in <code>c</code> to the bag. This will not check if the work piece exists in the bag.
	 * 
	 * @param c the work pieces to be added.
	 * @invariant self->includesAll(self$pre)
	 * @post self->includesAll(c)
	 */
	void addAllWork(@Immutable final Collection<? extends T> c);

	/**
	 * Adds all the work pieces in <code>c</code> to the bag, if they do not exist in the bag.
	 * 
	 * @param c the work pieces to be added.
	 * @return the work pieces that were rejected as duplicates.
	 * @invariant self->includesAll(self$pre)
	 * @pre c.oclIsKindOf(Collection(Object))
	 * @post self->includesAll(c)
	 * @post self->forall( o | self->count() = 1)
	 * @post result->forall(j | c.contains(j))
	 */
	@NonNull Collection<T> addAllWorkNoDuplicates(@Immutable final Collection<? extends T> c);

	/**
	 * Adds a new work to the bag. This will not check if the work exists in the bag.
	 * 
	 * @param o the work to be added.
	 * @invariant self->includesAll(self$pre)
	 * @post self->includes(o)
	 */
	void addWork(@Immutable final T o);

	/**
	 * Adds a new work to the bag, if it does not exist in the bag.
	 * 
	 * @param o the work to be added.
	 * @return <code>true</code> if <code>o</code> was added to the bag; <code>false</code>, if <code>o</code> was a
	 *         duplicate.
	 * @invariant self->includesAll(self$pre)
	 * @post self->includes(o)
	 * @post self->forall( o | self->count() = 1)
	 */
	boolean addWorkNoDuplicates(@Immutable final T o);

	/**
	 * Removes all work pieces in this bag.
	 * 
	 * @post hasWork() == true
	 */
	void clear();

	/**
	 * Checks if there is any work in this bag.
	 * 
	 * @return <code>true</code> if the bag is non-empty; <code>false</code>, otherwise.
	 * @post result == (self->size() != 0)
	 */
	boolean hasWork();
}

// End of File
