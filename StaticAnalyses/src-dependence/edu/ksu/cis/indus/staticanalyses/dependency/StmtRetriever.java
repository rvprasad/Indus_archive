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
	@Empty public StmtRetriever() {
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
