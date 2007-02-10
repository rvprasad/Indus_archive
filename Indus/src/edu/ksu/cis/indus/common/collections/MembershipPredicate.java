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

package edu.ksu.cis.indus.common.collections;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This class can be used to filter out objects based on their membership in a collection.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the type of input object to the predicate.
 */
public final class MembershipPredicate<T>
		implements IPredicate<T> {

	/**
	 * The collection being tracked.
	 */
	@NonNull @Immutable private final Collection<T> collection;

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
	public MembershipPredicate(final boolean theMembership, @NonNull @Immutable final Collection<T> theCollection) {
		super();
		membership = theMembership;
		collection = theCollection;
	}

	/**
	 * The membership test.
	 * 
	 * @param <T1> is the type of the input object.
	 * @param object to be tested.
	 * @return <code>true</code> if the <code>object</code> belongs to <code>collection</code> and membership is
	 *         <code>true</code> or if <code>object</code> does not belong to <code>collection</code> and membership is
	 *         <code>false</code>; <code>false</code>, otherwise.
	 */
	@Functional public <T1 extends T> boolean evaluate(@NonNull final T1 object) {
		return membership == collection.contains(object);
	}
}

// End of File
