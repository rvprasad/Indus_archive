
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

package edu.ksu.cis.indus.common.datastructures;

import java.util.Collection;


/**
 * This is a generic container of objects.  The order in which the objects are added and removed can be configured.  At
 * present, it supports LIFO and FIFO ordering.  This affects the order in which the <code>getWork()</code> will return the
 * work added to this bag.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad</a>
 * @version $Revision$
 *
 * @since Created: Thu Jul 25 18:37:24 2002.
 */
public interface IWorkBag {
	/**
	 * Returns a work pieces.
	 *
	 * @return a work piece.
	 *
	 * @post hasWork() implies self$pre->exists(o | result == o)
	 */
	Object getWork();

	/**
	 * Adds all the work pieces in <code>c</code> to the bag.  This will not check if the work piece exists in the bag.
	 *
	 * @param c the work pieces to be added.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @post self->includesAll(c)
	 */
	void addAllWork(final Collection c);

	/**
	 * Adds all the work pieces in <code>c</code> to the bag, if they do not exist in the bag.
	 *
	 * @param c the work pieces to be added.
	 *
	 * @return the work pieces that were rejected as duplicates.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @pre c.oclIsKindOf(Collection(Object))
	 * @post self->includesAll(c)
	 * @post self->forall( o | self->count() = 1)
	 * @post result != null
	 * @post result->forall(j | c.contains(j))
	 */
	Collection addAllWorkNoDuplicates(final Collection c);

	/**
	 * Adds a new work to the bag.  This will not check if the work exists in the bag.
	 *
	 * @param o the work to be added.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @post self->includes(o)
	 */
	void addWork(final Object o);

	/**
	 * Adds a new work to the bag, if it does not exist in the bag.
	 *
	 * @param o the work to be added.
	 *
	 * @return <code>true</code> if <code>o</code> was added to the bag; <code>false</code>, if <code>o</code> was a
	 * 		   duplicate.
	 *
	 * @invariant self->includesAll(self$pre)
	 * @post self->includes(o)
	 * @post self->forall( o | self->count() = 1)
	 */
	boolean addWorkNoDuplicates(final Object o);

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
	 *
	 * @post result == (self->size() != 0)
	 */
	boolean hasWork();
}

// End of File
