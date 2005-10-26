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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class LocalStmtPairRetriever
		implements IDependenceRetriever<Pair<Stmt, Local>, SootMethod, DefinitionStmt, DefinitionStmt, SootMethod, Stmt> {

	/**
	 * Creates an instance of this class.
	 */
	@AEmpty public LocalStmtPairRetriever() {
		// does nothing
	}

	/**
	 * @see IDependenceRetriever#getDependees(edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis, Object, Object)
	 */
	public Collection getDependees(
			final IDependencyAnalysis<Pair<Stmt, Local>, SootMethod, DefinitionStmt, DefinitionStmt, SootMethod, Stmt> da,
			final DefinitionStmt base, final SootMethod context) {
		final Collection<DefinitionStmt> _result = new HashSet<DefinitionStmt>();
		for (final Iterator<ValueBox> _i = base.getUseBoxes().iterator(); _i.hasNext();) {
			final ValueBox _vb = _i.next();
			final Value _v = _vb.getValue();
			if (_v instanceof Local) {
				_result.addAll(da.getDependees(new Pair<Stmt, Local>(base, (Local) _v), context));
			}
		}
		return _result;
	}

	/**
	 * @see IDependenceRetriever#getDependents(edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis, Object,
	 *      Object)
	 */
	public Collection getDependents(
			IDependencyAnalysis<Pair<Stmt, Local>, SootMethod, DefinitionStmt, DefinitionStmt, SootMethod, Stmt> da,
			Stmt base, SootMethod context) {
		final Collection<Stmt> _result;
		if (base instanceof DefinitionStmt) {
			_result = da.getDependents((DefinitionStmt) base, context);
		} else {
			_result = Collections.emptySet();
		}
		return _result;
	}
}

// End of File
