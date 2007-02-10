/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
