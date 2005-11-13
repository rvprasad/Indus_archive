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

import edu.ksu.cis.indus.annotations.AEmpty;
import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.Collection;
import java.util.HashSet;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * This implementation of <code>IDependenceRetriever</code> is used in instance when the dependence information is provided
 * in terms of statements and the context does not change from the orignal context. Use this in cases such as control
 * dependence.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class StmtRetriever
		extends AbstractDependenceRetriever<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt> {

	/**
	 * Creates an instance of this class.
	 */
	@AEmpty public StmtRetriever() {
		// does nothing
	}

	/**
	 * @see IDependenceRetriever#convertToConformantDependees(java.util.Collection, java.lang.Object, java.lang.Object)
	 */
	public Collection<Pair<Stmt, SootMethod>> convertToConformantDependees(final Collection<Stmt> dependents, final Stmt base,
			final SootMethod context) {
		final Collection<Pair<Stmt, SootMethod>> _result = new HashSet<Pair<Stmt, SootMethod>>();
		for (final Stmt _t2 : dependents) {
			_result.add(new Pair<Stmt, SootMethod>(_t2, context));
		}
		return _result;
	}

	/**
	 * @see IDependenceRetriever#convertToConformantDependents(java.util.Collection, java.lang.Object, java.lang.Object)
	 */
	public Collection<Pair<Stmt, SootMethod>> convertToConformantDependents(final Collection<Stmt> dependees, final Stmt base,
			final SootMethod context) {
		final Collection<Pair<Stmt, SootMethod>> _result = new HashSet<Pair<Stmt, SootMethod>>();
		for (final Stmt _e1 : dependees) {
			_result.add(new Pair<Stmt, SootMethod>(_e1, context));
		}
		return _result;
	}

}

// End of File
