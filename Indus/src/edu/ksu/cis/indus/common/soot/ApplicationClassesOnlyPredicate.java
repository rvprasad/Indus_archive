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

import edu.ksu.cis.indus.common.collections.IPredicate;

import soot.SootClass;
import soot.SootMethod;

/**
 * This predicate can be used to check if a given class is or method belongs to an application class.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ApplicationClassesOnlyPredicate
		implements IPredicate<Object> {

	/**
	 * Creates an instance of this class.
	 */
	public ApplicationClassesOnlyPredicate() {
		super();
	}

	/**
	 * @see IPredicate#evaluate(java.lang.Object)
	 */
	public boolean evaluate(final Object object) {
		final boolean _result;

		if (object instanceof SootMethod) {
			final SootMethod _sm = (SootMethod) object;
			_result = _sm.getDeclaringClass().isApplicationClass();
		} else if (object instanceof SootClass) {
			final SootClass _sc = (SootClass) object;
			_result = _sc.isApplicationClass();
		} else {
			_result = true;
		}
		return _result;
	}
}

// End of File
