
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

package edu.ksu.cis.indus.staticanalyses.concurrency;

import edu.ksu.cis.indus.common.datastructures.Triple;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;


/**
 * A class that provides helper methods related to concurrency. 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ConcurrencyHelper {
	/**
	 * Checks if the given entity is a monitor triple.
	 *
	 * @param entity to be checked.
	 *
	 * @return <code>true</code> if <code>entity</code> is a monitor triple (it contains enter/exit monitor statement and
	 * 		   the method or just a synchronized statement); <code>false</code>, otherwise.
	 *
	 * @pre entity != null
	 */
	public static boolean isMonitorTriple(final Object entity) {
		boolean _result = false;

		if (entity instanceof Triple) {
			final Triple _triple = (Triple) entity;
			final Object _third = _triple.getThird();

			if (_third instanceof SootMethod) {
				final Object _second = _triple.getSecond();
				final Object _first = _triple.getFirst();
				_result =
					(_second == null && _first == null && ((SootMethod) _third).isSynchronized())
					  || (_first instanceof EnterMonitorStmt && _second instanceof ExitMonitorStmt);
			}
		}
		return _result;
	}
}

// End of File
