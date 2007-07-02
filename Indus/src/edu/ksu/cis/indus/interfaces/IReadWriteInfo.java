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

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import soot.SootMethod;

/**
 * The interface to access object read-write information. An object is read if it's field is read and an object is written if
 * it's field is written.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IReadWriteInfo
		extends IIdentification, IStatus {

	/**
	 * This constant identifies the cells of an array in the field map of it's alias set.
	 */
	String ARRAY_FIELD = "$ELT";

	/**
	 * This indentifies the interface.
	 */
	Comparable<String> ID = "Object read-write Information";

	/**
	 * Checks if the method invocation induces sharing of objects between the object graphs rooted at the given argument and
	 * the receiver.
	 * 
	 * @param callerTriple is the call-site.
	 * @param argPos is the position of the argument.
	 * @return <code>true</code> if the object graphs rooted at the argument and the receiver may share objects after the
	 *         invocation; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided argument position is invalid.
	 * @pre callerTriple != null and argPos1 >= 0 and argPos2 >= 0 and not callerTriple.getMethod().isStatic()
	 */
	boolean canMethodInduceSharingBetweenArgAndReceiver(final CallTriple callerTriple, final int argPos)
			throws IllegalArgumentException;

	/**
	 * Checks if the method invocation induces sharing of objects between the object graphs rooted at the given arguments.
	 * 
	 * @param callerTriple is the call-site.
	 * @param argPos1 is the position of an argument.
	 * @param argPos2 is the position of another argument.
	 * @return <code>true</code> if the object graphs rooted at the given arguments may share objects after the invocation;
	 *         <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if either of the provided argument position is invalid.
	 * @pre callerTriple != null and argPos1 >= 0 and argPos2 >= 0
	 */
	boolean canMethodInduceSharingBetweenArgs(final CallTriple callerTriple, final int argPos1, final int argPos2)
			throws IllegalArgumentException;

	/**
	 * Checks if the method induces sharing of objects between the object graphs rooted at the given parameter and the this
	 * variable.
	 * 
	 * @param method of interest.
	 * @param paramPos is the position of the parameter.
	 * @return <code>true</code> if the object graphs rooted at the given parameter and the this variable may share objects
	 *         after the invocation; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided parameter position is invalid.
	 * @pre callerTriple != null and paraPos >= 0 and not method.isStatic()
	 */
	boolean canMethodInduceSharingBetweenParamAndThis(final SootMethod method, final int paramPos)
			throws IllegalArgumentException;

	/**
	 * Checks if the method induces sharing of objects between the object graphs rooted at the given parameters.
	 * 
	 * @param method of interest.
	 * @param paramPos1 is the position of a parameter.
	 * @param paramPos2 is the position of another parameter.
	 * @return <code>true</code> if the object graphs rooted at the given parameters may share objects after the invocation;
	 *         <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if either of the provided parameter position is invalid.
	 * @pre callerTriple != null and paraPos1 >= 0 and paramPos2 >= 0
	 */
	boolean canMethodInduceSharingBetweenParams(final SootMethod method, final int paramPos1, final int paramPos2)
			throws IllegalArgumentException;

	/**
	 * Checks if the given invocation reads any global data.
	 * 
	 * @param callerTriple is the invocation with the invocation expression, statement containing the invocation, and the
	 *            method in which the invocation occurs.
	 * @return <code>true</code> if the invocation reads global data; <code>false</code>, otherwise.
	 * @pre callerTriple != null
	 */
	boolean doesInvocationReadGlobalData(final CallTriple callerTriple);

	/**
	 * Checks if the given invocation writes any global data.
	 * 
	 * @param callerTriple is the invocation with the invocation expression, statement containing the invocation, and the
	 *            method in which the invocation occurs.
	 * @return <code>true</code> if the invocation writes global data; <code>false</code>, otherwise.
	 * @pre callerTriple != null
	 */
	boolean doesInvocationWriteGlobalData(final CallTriple callerTriple);

	/**
	 * Checks if the method invocation induces sharing of objects accessible at the given access paths rooted at the given
	 * arguments.
	 * 
	 * @param callerTriple is the call-site.
	 * @param argPos1 is the position of an argument.
	 * @param accesspath1 is the accesspath rooted at <code>argPos1</code>.
	 * @param argPos2 is the position of another argument.
	 * @param accesspath2 is the accesspath rooted at <code>argPos2</code>.
	 * @return <code>true</code> if the object graphs rooted at the given arguments may share objects after the invocation;
	 *         <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if either of the provided argument position is invalid.
	 * @pre callerTriple != null and argPos1 >= 0 and argPos2 >= 0 and accesspath1 != null and accesspath1->forall(o | o !=
	 *      null) and and accesspath2 != null and accesspath2->forall(o | o != null)
	 */
	boolean doesMethodInduceSharingBetweenAccesspaths(final CallTriple callerTriple, final int argPos1,
			final String[] accesspath1, final int argPos2, final String[] accesspath2) throws IllegalArgumentException;

	/**
	 * Checks if the method invocation induces sharing of objects accessible at the given access paths rooted at the given
	 * argument and the receiver.
	 * 
	 * @param callerTriple is the call-site.
	 * @param argPos is the position of an argument.
	 * @param accesspath is the accesspath rooted at <code>argPos</code>.
	 * @param receiverBasedAccesspath is the accesspath rooted at the receiver.
	 * @return <code>true</code> if the object graphs rooted at the given arguments may share objects after the invocation;
	 *         <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if either of the provided argument position is invalid.
	 * @pre callerTriple != null and argPos1 >= 0 and argPos2 >= 0 and not callerTriple.getMethod().isStatic() and accesspath !=
	 *      null and accesspath->forall(o | o != null) and and receiverBasedAccesspath != null and
	 *      receiverBasedAccesspath->forall(o | o != null)
	 */
	boolean doesMethodInduceSharingBetweenAccesspaths(final CallTriple callerTriple, final int argPos,
			final String[] accesspath, final String[] receiverBasedAccesspath) throws IllegalArgumentException;

	/**
	 * Checks if the method induces sharing of objects accessible at the given access paths rooted at the given arguments.
	 * 
	 * @param method of interest.
	 * @param paramPos1 is the position of an argument.
	 * @param accesspath1 is the accesspath rooted at <code>paramPos1</code>.
	 * @param paramPos2 is the position of another argument.
	 * @param accesspath2 is the accesspath rooted at <code>paramPos2</code>.
	 * @return <code>true</code> if the object graphs rooted at the given arguments may share objects after the invocation;
	 *         <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if either of the provided argument position is invalid.
	 * @pre callerTriple != null and paramPos1 >= 0 and paramPos2 >= 0 and accesspath1 != null and accesspath1->forall(o | o !=
	 *      null) and and accesspath2 != null and accesspath2->forall(o | o != null)
	 */
	boolean doesMethodInduceSharingBetweenAccesspaths(final SootMethod method, final int paramPos1,
			final String[] accesspath1, final int paramPos2, final String[] accesspath2) throws IllegalArgumentException;

	/**
	 * Checks if the method induces sharing of objects accessible at the given access paths rooted at the given argument and
	 * the this variable.
	 * 
	 * @param method of interest.
	 * @param paramPos is the position of an argument.
	 * @param accesspath is the accesspath rooted at <code>paramPos</code>.
	 * @param thisBasedAccesspath is the accesspath rooted at the receiver.
	 * @return <code>true</code> if the object graphs rooted at the given arguments may share objects after the invocation;
	 *         <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if either of the provided argument position is invalid.
	 * @pre callerTriple != null and paramPos >= 0 and not callerTriple.getMethod().isStatic() and accesspath != null and
	 *      accesspath->forall(o | o != null) and and thisBasedAccesspath != null and thisBasedAccesspath->forall(o | o !=
	 *      null)
	 */
	boolean doesMethodInduceSharingBetweenAccesspaths(final SootMethod method, final int paramPos, final String[] accesspath,
			final String[] thisBasedAccesspath) throws IllegalArgumentException;

	/**
	 * Checks if the given method reads any global data.
	 * 
	 * @param method of interest.
	 * @return <code>true</code> if the method reads global data; <code>false</code>, otherwise.
	 * @pre method != null
	 */
	boolean doesMethodReadGlobalData(final SootMethod method);

	/**
	 * Checks if the given method writes any global data.
	 * 
	 * @param method of interest.
	 * @return <code>true</code> if the method writes global data; <code>false</code>, otherwise.
	 * @pre method != null
	 */
	boolean doesMethodWriteGlobalData(final SootMethod method);

	/**
	 * Checks if the end point of the access path starting at an argument at a call-site is read.
	 * 
	 * @param callerTriple is the call-site.
	 * @param argPos is the position of the argument.
	 * @param accesspath is the access path from the given argument to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 *            length greater than 1. <code>false</code> indicates the check should consider access paths from the end
	 *            point that are of length 1 .
	 * @return <code>true</code> if the end point is read; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided argument position is invalid.
	 * @pre callerTriple != null and argPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isArgumentBasedAccessPathRead(final CallTriple callerTriple, final int argPos, final String[] accesspath,
			final boolean recurse) throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at an argument at a call-site is written to.
	 * 
	 * @param callerTriple is the call-site.
	 * @param argPos is the position of the argument.
	 * @param accesspath is the access path from the given argument to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 *            length greater than 1. <code>false</code> indicates the check should consider access paths from the end
	 *            point that are of length 1 .
	 * @return <code>true</code> if the end point is written; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided argument position is invalid.
	 * @pre callerTriple != null and argPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isArgumentBasedAccessPathWritten(final CallTriple callerTriple, final int argPos, final String[] accesspath,
			final boolean recurse) throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at a parameter of a method is read.
	 * 
	 * @param method of reference.
	 * @param paramPos is the position of the parameter.
	 * @param accesspath is the access path from the given parameter to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 *            length greater than 1. <code>false</code> indicates the check should consider access paths from the end
	 *            point that are of length 1 .
	 * @return <code>true</code> if the end point is read; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided parameter position is invalid.
	 * @pre method != null and paramPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isParameterBasedAccessPathRead(final SootMethod method, final int paramPos, final String[] accesspath,
			final boolean recurse) throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at a parameter of a method is written.
	 * 
	 * @param method of reference.
	 * @param paramPos is the position of the parameter.
	 * @param accesspath is the access path from the given parameter to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 *            length greater than 1. <code>false</code> indicates the check should consider access paths from the end
	 *            point that are of length 1 .
	 * @return <code>true</code> if the end point is written; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided parameter position is invalid.
	 * @pre method != null and paramPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isParameterBasedAccessPathWritten(final SootMethod method, final int paramPos, final String[] accesspath,
			final boolean recurse) throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at the receiver at the call-site is read.
	 * 
	 * @param callerTriple is the call-site.
	 * @param accesspath is the access path from the given receiver to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths from the end point that are of
	 *            length greater than 1. <code>false</code> indicates the check should consider access paths from the end
	 *            point that are of length 1 .
	 * @return <code>true</code> if the end point is read; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided method is static.
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
	 *            length greater than 1. <code>false</code> indicates the check should consider access paths from the end
	 *            point that are of length 1 .
	 * @return <code>true</code> if the end point is written; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided method is static.
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
	 *            length greater than 1. <code>false</code> indicates the check should consider access paths from the end
	 *            point that are of length 1 .
	 * @return <code>true</code> if the end point is read; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided method is static.
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
	 *            length greater than 1. <code>false</code> indicates the check should consider access paths from the end
	 *            point that are of length 1 .
	 * @return <code>true</code> if the end point is written; <code>false</code>, otherwise.
	 * @throws IllegalArgumentException if the provided method is static.
	 * @pre method != null and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isThisBasedAccessPathWritten(final SootMethod method, final String[] accesspath, final boolean recurse)
			throws IllegalArgumentException;

}

// End of File
