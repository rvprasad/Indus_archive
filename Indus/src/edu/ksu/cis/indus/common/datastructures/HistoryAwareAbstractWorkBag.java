
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
 * This is an abstract implementation of <code>IWorkBag</code> with the ability to remember about processing of work pieces.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class HistoryAwareAbstractWorkBag
  extends AbstractWorkBag {
	/**
	 * The container that remembers the items that were processed by this work bag.
	 */
	private final Collection processedWorkPieces;

	/**
	 * Creates a new AbstractWorkBag object.  If a non-null collection is provided via <code>processed</code>, then a work
	 * piece will be processed only once even if it is added multiple times.
	 *
	 * @param processed If it is non-null, this collection is used to remember the work pieces added to this work bag.
	 */
	protected HistoryAwareAbstractWorkBag(final Collection processed) {
		processedWorkPieces = processed;
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#addWork(Object)
	 */
	public final void addWork(final Object o) {
		if (processedWorkPieces != null) {
			if (!processedWorkPieces.contains(o)) {
				processedWorkPieces.add(o);
				subAddWork(o);
			}
		} else {
			subAddWork(o);
		}
	}

	/**
	 * Subclasses should add the work to the container by overriding this method.
	 *
	 * @param o is the object to be added.
	 */
	protected abstract void subAddWork(final Object o);
}

/*
   ChangeLog:
   $Log$
 */
