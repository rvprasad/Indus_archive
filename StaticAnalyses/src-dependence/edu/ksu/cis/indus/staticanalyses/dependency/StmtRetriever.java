
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


import java.util.Collection;

import soot.jimple.Stmt;


/**
 * This implementation of <code>IDependenceRetriever</code> is used in instance when the dependence information is provided
 * in terms of statements and the context does not change from the orignal context.  Use this in cases such as control
 * dependence.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class StmtRetriever
  implements IDependenceRetriever {
	/**
	 * @see IDependenceRetriever#getDependees(IDependencyAnalysis, Object, Object)
	 */
	public Collection getDependees(final IDependencyAnalysis da, final Object dependent, final Object origContext) {
		final Stmt _stmt = (Stmt) dependent;
		return da.getDependees(_stmt, origContext);
	}

	/**
	 * @see IDependenceRetriever#getDependents(IDependencyAnalysis, Object, Object)
	 */
	public Collection getDependents(final IDependencyAnalysis da, final Object dependee, final Object origContext) {
		final Stmt _stmt = (Stmt) dependee;
		return da.getDependents(_stmt, origContext);
	}
}

/*
   ChangeLog:
   $Log$
 */
