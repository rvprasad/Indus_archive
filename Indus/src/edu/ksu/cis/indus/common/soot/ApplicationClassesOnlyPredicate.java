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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.common.collections.IPredicate;

import soot.SootMethod;

/**
 * This predicate can be used to check if a given class is or method belongs to an application class.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ApplicationClassesOnlyPredicate
		implements IPredicate<SootMethod> {

	/**
	 * Creates an instance of this class.
	 */
	@Empty public ApplicationClassesOnlyPredicate() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean evaluate(final SootMethod object) {
		return object.getDeclaringClass().isApplicationClass();
	}
}

// End of File
