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

package edu.ksu.cis.indus.interfaces;

import java.util.Collection;

import soot.SootClass;
import soot.SootMethod;

import soot.jimple.Stmt;

/**
 * This is the interface to access exception raising information.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IExceptionRaisingInfo
		extends IIdentification, IStatus {

	/**
	 * This is the unique identifier that can be used to identify an instance of this class.
	 */
	Comparable ID = "exception throw Information";

	/**
	 * Checks if the given statement throws an uncaught exception in the given method.
	 * 
	 * @param stmt of interest.
	 * @param method in which <code>stmt</code> occurs.
	 * @return <code>true</code> if an uncaught exception will be thrown; <code>false</code>, otherwise.
	 * @pre stmt != null and method != null
	 */
	boolean doesStmtThrowUncaughtException(final Stmt stmt, final SootMethod method);

	/**
	 * Retrieves the exceptions thrown by the given statement in the given method.
	 * 
	 * @param stmt of interest.
	 * @param method in which <code>stmt</code> occurs.
	 * @return a collection of exception types.
	 * @pre stmt != null and method != null
	 * @post result != null
	 * @post result.containsAll(getUncaughtExceptionsThrownBy(stmt, method))
	 */
	Collection<SootClass> getExceptionsThrownBy(final Stmt stmt, final SootMethod method);

	/**
	 * Retrieves the uncaught exceptions thrown by the given statement in the given method.
	 * 
	 * @param stmt of interest.
	 * @param method in which <code>stmt</code> occurs.
	 * @return a collection of exception types.
	 * @pre stmt != null and method != null
	 * @post result != null
	 * @post getExceptionsThrownBy(stmt, method).containsAll(result)
	 */
	Collection<SootClass> getUncaughtExceptionsThrownBy(final Stmt stmt, final SootMethod method);
}

// End of File
