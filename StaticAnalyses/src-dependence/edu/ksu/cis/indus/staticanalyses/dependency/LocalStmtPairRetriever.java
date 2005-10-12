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

import soot.Local;
import soot.SootMethod;
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
			IDependencyAnalysis<Pair<Stmt, Local>, SootMethod, DefinitionStmt, DefinitionStmt, SootMethod, Stmt> da,
			DefinitionStmt base, SootMethod context) {
		// TODO: Auto-generated method stub
		return null;
	}

	/**
	 * @see IDependenceRetriever#getDependents(edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis, Object,
	 *      Object)
	 */
	public Collection getDependents(
			IDependencyAnalysis<Pair<Stmt, Local>, SootMethod, DefinitionStmt, DefinitionStmt, SootMethod, Stmt> da,
			Stmt base, SootMethod context) {
		// TODO: Auto-generated method stub
		return null;
	}
}

// End of File
