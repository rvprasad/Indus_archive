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

package edu.ksu.cis.indus.common.collections;

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
		implements IPredicate<Collection> {

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
	 * @see edu.ksu.cis.indus.common.collections.IPredicate#evaluate(Object)
	 */
	public boolean evaluate(final Collection object) {
		return object.isEmpty() == emptiness;
	}
}

// End of File
