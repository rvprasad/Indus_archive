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
	Comparable<String> ID = "exception throw Information";

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
