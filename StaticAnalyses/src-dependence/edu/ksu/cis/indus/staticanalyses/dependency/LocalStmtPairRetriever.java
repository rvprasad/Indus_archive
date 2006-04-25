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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Experimental;
import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.Collection;

import soot.Local;
import soot.SootMethod;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

/**
 * * This implementation of <code>IDependenceRetriever</code> is used in instance when the dependence information is
 * provided in terms of local variables and statements pair. Use this in cases such as ready dependence.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
@Experimental public class LocalStmtPairRetriever
		extends
		AbstractDependenceRetriever<Pair<Local, Stmt>, SootMethod, DefinitionStmt, DefinitionStmt, SootMethod, Pair<Local, Stmt>> {

	/**
	 * Creates an instance of this class.
	 */
	@Empty public LocalStmtPairRetriever() {
		// does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<Pair<DefinitionStmt, SootMethod>> convertToConformantDependees(
			@SuppressWarnings("unused") final Collection<Pair<Local, Stmt>> dependees,
			@SuppressWarnings("unused") final DefinitionStmt base, @SuppressWarnings("unused") final SootMethod context) {
		// TODO: Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection<Pair<Pair<Local, Stmt>, SootMethod>> convertToConformantDependents(
			@SuppressWarnings("unused") final Collection<DefinitionStmt> dependents,
			@SuppressWarnings("unused") final Pair<Local, Stmt> base, @SuppressWarnings("unused") final SootMethod context) {
		// TODO: Auto-generated method stub
		return null;
	}
}

// End of File
