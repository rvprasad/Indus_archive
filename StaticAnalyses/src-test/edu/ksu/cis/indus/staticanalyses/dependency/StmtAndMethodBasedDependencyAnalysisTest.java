
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(StmtAndMethodBasedDependencyAnalysisTest.class);

	/**
	 * @see AbstractDependencyAnalysisTest#verifyDAFor(soot.jimple.Stmt, soot.SootMethod)
	 */
	protected void verifyDAFor(final Stmt unit, final SootMethod sm) {
		final IDependencyAnalysis _da = getDA();
		verifyDependentPosition(unit, sm, _da);
		verifyDependeePosition(unit, sm, _da);
	}

	/**
	 * Verifies dependency analysis from dependee position, i.e., checks if the given unit occurs as a dependee in the
	 * dependees set of all it's dependents.
	 *
	 * @param dependee is the dependee
	 * @param method is the method containing <code>stmt</code>.
	 * @param dependencyAnalysis is the analysis to be tested.
	 *
	 * @throws IllegalStateException when the given dependency analysis does not report info as pairs or as statements.
	 *
	 * @pre dependee != null and method != null and dependencyAnalysis != null
	 */
	private void verifyDependeePosition(final Stmt dependee, final SootMethod method,
		final IDependencyAnalysis dependencyAnalysis) {
		final Collection _dependents = dependencyAnalysis.getDependents(dependee, method);

		if (!_dependents.isEmpty()) {
			final Object _temp = _dependents.iterator().next();
			final Object _dependee;

			if (_temp instanceof Pair) {
				_dependee = new Pair(dependee, method);
			} else {
				_dependee = dependee;
			}

			for (final Iterator _i = _dependents.iterator(); _i.hasNext();) {
				final Object _o = _i.next();
				Stmt _stmt;
				SootMethod _method = method;

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

				assertTrue("Dependee:" + getDA().getClass().getName() + ": (" + dependee + ", " + method + ") -> (" + _stmt
					+ ", " + _method + ")", dependencyAnalysis.getDependees(_stmt, _method).contains(_dependee));
			}
		}
	}

	/**
	 * Verifies dependency analysis from dependent position, i.e., checks if the given unit occurs as a dependent in the
	 * dependents set of all it's dependees.
	 *
	 * @param dependent is the dependent
	 * @param method is the method containing <code>stmt</code>.
	 * @param dependencyAnalysis is the analysis to be tested.
	 *
	 * @throws IllegalStateException when the given dependency analysis does not report info as pairs or as statements.
	 *
	 * @pre dependent != null and method != null and dependencyAnalysis != null
	 */
	private void verifyDependentPosition(final Stmt dependent, final SootMethod method,
		final IDependencyAnalysis dependencyAnalysis) {
		final Collection _dependees = dependencyAnalysis.getDependees(dependent, method);

		if (!_dependees.isEmpty()) {
			final Object _temp = _dependees.iterator().next();
			final Object _dependent;

			if (_temp instanceof Pair) {
				_dependent = new Pair(dependent, method);
			} else {
				_dependent = dependent;
			}

			for (final Iterator _i = _dependees.iterator(); _i.hasNext();) {
				final Object _o = _i.next();
				Stmt _stmt;
				SootMethod _method = method;

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

				assertTrue("Dependent:" + getDA().getClass().getName() + ": (" + dependent + ", " + method + ") -> (" + _stmt
					+ ", " + _method + ")", dependencyAnalysis.getDependents(_stmt, _method).contains(_dependent));
			}
		}
	}
}

// End of File
