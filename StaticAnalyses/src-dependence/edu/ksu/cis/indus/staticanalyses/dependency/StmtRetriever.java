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

import java.util.Collection;

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
 * @param <C1> DOCUMENT ME!
 * @param <C2> DOCUMENT ME!
 */
final class StmtRetriever
		implements IDependenceRetriever<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt> {

	/**
	 * Creates an instance of this class.
	 */
	@AEmpty public StmtRetriever() {
		// does nothing
	}

	/**
	 * @see IDependenceRetriever#getDependees(IDependencyAnalysis, Object, Object)
	 */
	public Collection<Stmt> getDependees(final IDependencyAnalysis<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt> da,
			final Stmt dependent, final SootMethod origContext) {
		return da.getDependees(dependent, origContext);
	}

	/**
	 * @see IDependenceRetriever#getDependents(IDependencyAnalysis, Object, Object)
	 */
	public Collection<Stmt> getDependents(final IDependencyAnalysis<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt> da,
			final Stmt dependee, final SootMethod origContext) {
		return da.getDependents(dependee, origContext);
	}
}

// End of File
