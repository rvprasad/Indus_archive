
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

package edu.ksu.cis.indus.common;

import java.util.Collection;

import org.apache.commons.collections.Predicate;


/**
 * This class can be used to filter out objects based on their membership in a collection.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class MembershipPredicate
  implements Predicate {
	/** 
	 * The collection being tracked.
	 */
	private final Collection collection;

	/** 
	 * This indicates if the node should be evaluated for membership or otherwise.
	 */
	private final boolean membership;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theMembership controls the membership test direction.
	 * @param theCollection tracks the path used in membership test.
	 */
	public MembershipPredicate(final boolean theMembership, final Collection theCollection) {
		super();
		membership = theMembership;
		collection = theCollection;
	}

	/**
	 * The membership test.
	 *
	 * @param object to be tested.
	 *
	 * @return <code>true</code> if the <code>object</code> belongs to <code>collection</code> and membership is
	 * 		   <code>true</code> or if <code>object</code> does not belong to <code>collection</code> and membership is
	 * 		   <code>false</code>; <code>false</code>, otherwise.
	 */
	public boolean evaluate(final Object object) {
		return (membership && collection.contains(object)) || (!membership && !collection.contains(object));
	}
}

// End of File
