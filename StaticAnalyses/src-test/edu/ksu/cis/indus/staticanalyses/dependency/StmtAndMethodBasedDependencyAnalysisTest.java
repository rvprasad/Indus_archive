
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class tests results from dependency analysis which can be queried for dependence on a statement in a method. 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class StmtAndMethodBasedDependencyAnalysisTest
  extends AbstractDependencyAnalysisTest {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(StmtAndMethodBasedDependencyAnalysisTest.class);

	/**
	 * @see AbstractDependencyAnalysisTest#verifyDAFor(soot.jimple.Stmt, soot.SootMethod)
	 */
	protected void verifyDAFor(final Stmt unit, final SootMethod sm) {
		final Collection _dependees = getDA().getDependees(unit, sm);

		if (!_dependees.isEmpty()) {
			final Object _temp = _dependees.iterator().next();
			final Object _dependee;

			if (_temp instanceof Pair) {
				_dependee = new Pair(unit, sm);
			} else {
				_dependee = unit;
			}

			for (final Iterator _i = _dependees.iterator(); _i.hasNext();) {
				final Object _o = _i.next();
				Stmt _stmt;
				SootMethod _method = sm;

				if (_o instanceof Pair) {
					final Pair _pair = (Pair) _o;
					_stmt = (Stmt) _pair.getFirst();
					_method = (SootMethod) _pair.getSecond();
				} else if (_o instanceof Stmt) {
					_stmt = (Stmt) _o;
				} else {
					final String _msg = "This class can only handle Pair and Stmt as targets of dependence information.";
					LOGGER.error(_msg);
					throw new IllegalStateException(_msg);
				}

				final Collection _dependents = getDA().getDependents(_stmt, _method);
				assertTrue(getDA().getClass().getName() + ": (" + unit + ", " + sm + ") -> (" + _stmt + ", " + _method + ")",
					_dependents.contains(_dependee));
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/05/14 09:02:56  venku
   - refactored:
     - The ids are available in IDependencyAnalysis, but their collection is
       available via a utility class, DependencyAnalysisUtil.
     - DependencyAnalysis will have a sanity check via Unit Tests.
   - ripple effect.

 */
