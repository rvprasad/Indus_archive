
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
	 * @throws IllegalStateException when the workbag is empty.
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

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/01/06 00:17:10  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.2  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.3  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/12/01 13:42:02  venku
   - added support to provide information about which work peices
     were added to the bag and which weren't.
   Revision 1.1  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.8  2003/11/05 09:27:10  venku
   - Split IWorkBag into an interface and an implementation
     for the sake of performance.
   Revision 1.7  2003/11/05 08:46:13  venku
   - coding convention.
   Revision 1.6  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/17 10:33:03  venku
   WorkList does not inherit from IWorkBag rather contains an instance of IWorkBag.
   Ripple effect of the above change.
   Revision 1.4  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.3  2003/08/11 06:40:54  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
   Revision 1.7  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
