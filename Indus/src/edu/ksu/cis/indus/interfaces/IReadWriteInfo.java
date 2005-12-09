
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

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import soot.SootMethod;


/**
 * The interface to access object read-write information.  An object is read if it's field is read and an object is written
 * if it's field is written.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IReadWriteInfo
  extends IIdentification,
	  IStatus {
	/**
	 * This indentifies the interface.
	 */
	Comparable<String> ID = "Object read-write Information";

	/**
	 * This constant identifies the cells of an array in the field map of it's alias set.
	 */
	String ARRAY_FIELD = "$ELT";

	/**
	 * Checks if the end point of the access path starting at an argument at a call-site is read.
	 *
	 * @param callerTriple is the call-site.
	 * @param argPos is the position of the argument.
	 * @param accesspath is the access path from the given argument to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 * 		  length  greater than 1. <code>false</code> indicates the check should consider access paths from the end point
	 * 		  that are of length 1 .
	 *
	 * @return <code>true</code> if the end point is read; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided argument position is invalid.
	 *
	 * @pre callerTriple != null and argPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isArgumentBasedAccessPathRead(final CallTriple callerTriple, final int argPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at an argument at a call-site is written to.
	 *
	 * @param callerTriple is the call-site.
	 * @param argPos is the position of the argument.
	 * @param accesspath is the access path from the given argument to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 * 		  length  greater than 1. <code>false</code> indicates the check should consider access paths from the end point
	 * 		  that are of length 1 .
	 *
	 * @return <code>true</code> if the end point is written; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided argument position is invalid.
	 *
	 * @pre callerTriple != null and argPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isArgumentBasedAccessPathWritten(final CallTriple callerTriple, final int argPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at a parameter of a method is read.
	 *
	 * @param method of reference.
	 * @param paramPos is the position of the parameter.
	 * @param accesspath is the access path from the given parameter to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 * 		  length  greater than 1. <code>false</code> indicates the check should consider access paths from the end point
	 * 		  that are of length 1 .
	 *
	 * @return <code>true</code> if the end point is read; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided parameter position is invalid.
	 *
	 * @pre method != null and paramPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isParameterBasedAccessPathRead(final SootMethod method, final int paramPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at a parameter of a method is written.
	 *
	 * @param method of reference.
	 * @param paramPos is the position of the parameter.
	 * @param accesspath is the access path from the given parameter to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 * 		  length  greater than 1. <code>false</code> indicates the check should consider access paths from the end point
	 * 		  that are of length 1 .
	 *
	 * @return <code>true</code> if the end point is written; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided parameter position is invalid.
	 *
	 * @pre method != null and paramPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isParameterBasedAccessPathWritten(final SootMethod method, final int paramPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at the receiver at the call-site is read.
	 *
	 * @param callerTriple is the call-site.
	 * @param accesspath is the access path from the given receiver to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 * 		  length  greater than 1. <code>false</code> indicates the check should consider access paths from the end point
	 * 		  that are of length 1 .
	 *
	 * @return <code>true</code> if the end point is read; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided method is static.
	 *
	 * @pre method != null and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isReceiverBasedAccessPathRead(final CallTriple callerTriple, final String[] accesspath, final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at the receiver at the call-site is written.
	 *
	 * @param callerTriple is the call-site.
	 * @param accesspath is the access path from the given receiver to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 * 		  length  greater than 1. <code>false</code> indicates the check should consider access paths from the end point
	 * 		  that are of length 1 .
	 *
	 * @return <code>true</code> if the end point is written; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided method is static.
	 *
	 * @pre method != null and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isReceiverBasedAccessPathWritten(final CallTriple callerTriple, final String[] accesspath, final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at "this" variable of a method is read.
	 *
	 * @param method of reference.
	 * @param accesspath is the access path from the given "this" variable to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 * 		  length  greater than 1. <code>false</code> indicates the check should consider access paths from the end point
	 * 		  that are of length 1 .
	 *
	 * @return <code>true</code> if the end point is read; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided method is static.
	 *
	 * @pre method != null and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isThisBasedAccessPathRead(final SootMethod method, final String[] accesspath, final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at "this" variable of a method is written.
	 *
	 * @param method of reference.
	 * @param accesspath is the access path from the given "this" variable to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 * 		  length  greater than 1. <code>false</code> indicates the check should consider access paths from the end point
	 * 		  that are of length 1 .
	 *
	 * @return <code>true</code> if the end point is written; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided method is static.
	 *
	 * @pre method != null and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isThisBasedAccessPathWritten(final SootMethod method, final String[] accesspath, final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the given invocation reads any global data.
	 *
	 * @param callerTriple is the invocation with the invocation expression, statement containing the invocation, and the
	 * 		  method in which the invocation occurs.
	 *
	 * @return <code>true</code> if the invocation reads global data; <code>false</code>, otherwise.
	 *
	 * @pre callerTriple != null
	 */
	boolean doesInvocationReadGlobalData(final CallTriple callerTriple);

	/**
	 * Checks if the given invocation writes any global data.
	 *
	 * @param callerTriple is the invocation with the invocation expression, statement containing the invocation, and the
	 * 		  method in which the invocation occurs.
	 *
	 * @return <code>true</code> if the invocation writes global data; <code>false</code>, otherwise.
	 *
	 * @pre callerTriple != null
	 */
	boolean doesInvocationWriteGlobalData(final CallTriple callerTriple);

	/**
	 * Checks if the given method reads any global data.
	 *
	 * @param method of interest.
	 *
	 * @return <code>true</code> if the method reads global data; <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	boolean doesMethodReadGlobalData(final SootMethod method);

	/**
	 * Checks if the given method writes any global data.
	 *
	 * @param method of interest.
	 *
	 * @return <code>true</code> if the method writes global data; <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	boolean doesMethodWriteGlobalData(final SootMethod method);
}

// End of File
