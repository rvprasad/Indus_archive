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
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This predicate checks if a collection is empty. Hence, this predicate should be used in conjunction with
 * <code>collection</code> objects.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CollectionEmptinessPredicate
		implements IPredicate<Collection<?>> {

	/**
	 * The singleton instance of this predicate that checks for emptiness.
	 */
	public static final CollectionEmptinessPredicate EMPTY_SINGLETON = new CollectionEmptinessPredicate(true);

	/**
	 * The singleton instance of this predicate that checks for non-emptiness.
	 */
	public static final CollectionEmptinessPredicate NON_EMPTY_SINGLETON = new CollectionEmptinessPredicate(false);

	/**
	 * This indicates if the check is for emptiness or otherwise.
	 */
	private final boolean emptiness;

	/**
	 * Creates a new CollectionEmptinessPredicate object.
	 * 
	 * @param emptinessTest <code>true</code> if the test is for emptiness; <code>false</code>, otherwise.
	 */
	private CollectionEmptinessPredicate(final boolean emptinessTest) {
		emptiness = emptinessTest;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public boolean evaluate(@NonNull final Collection<?> object) {
		return object.isEmpty() == emptiness;
	}
}

// End of File
