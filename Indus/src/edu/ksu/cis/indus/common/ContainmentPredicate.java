
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

package edu.ksu.cis.indus.common;

import java.util.Collection;

import org.apache.commons.collections.Predicate;


/**
 * This predicate checks if the value occurs in the given container.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ContainmentPredicate
  implements Predicate {
	/** 
	 * The container which is the basis of the containment check.
	 */
	private Collection collection;

	/**
	 * Set the container.
	 *
	 * @param container is the container.
	 */
	public void setContainer(final Collection container) {
		collection = container;
	}

	/**
	 * @see Predicate#evaluate(Object)
	 */
	public boolean evaluate(final Object o) {
		return collection.contains(o);
	}
}

/*
   ChangeLog:
   $Log$
 */
