
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
 * The interface to access side-effect information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISideEffectInfo
  extends IIdentification,
	  IStatus {
	/** 
	 * This indentifies the interface.
	 */
	Object ID = "Side Effect Information";

	/** 
	 * This constant identifies the cells of an array in the field map of it's alias set.
	 */
	String ARRAY_FIELD = "$ELT";

	/**
	 * Checks if the end point of the access path starting at an argument at a call-site is side-affected.
	 *
	 * @param callerTriple is the call-site.
	 * @param argPos is the position of the argument.
	 * @param accesspath is the access path from the given parameter to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths of length  greater than 1 from
	 * 		  the end point. <code>false</code> indicates the check should consider access paths of length 1 from the end
	 * 		  point .
	 *
	 * @return <code>true</code> if the end point is side-affected; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided argument position is invalid.
	 *
	 * @pre callerTriple != null and argPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isArgumentBasedAccessPathSideAffected(final CallTriple callerTriple, final int argPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the argument of a call-site is side-affected. This considers side-effect via access paths of length greater
	 * than 1.
	 *
	 * @param callerTriple is the call-site.
	 * @param argPos is the position of the argument.
	 *
	 * @return <code>true</code> if the argument is side-affected; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided argument position is invalid.
	 *
	 * @pre method != null and argPos >= 0
	 */
	boolean isArgumentSideAffected(final CallTriple callerTriple, final int argPos)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at a parameter of a method is side-affected.
	 *
	 * @param method of reference.
	 * @param paramPos is the position of the parameter.
	 * @param accesspath is the access path from the given parameter to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths of length  greater than 1 from
	 * 		  the end point. <code>false</code> indicates the check should consider access paths of length 1 from the end
	 * 		  point .
	 *
	 * @return <code>true</code> if the end point is side-affected; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided parameter position is invalid.
	 *
	 * @pre method != null and paramPos >= 0 and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isParameterBasedAccessPathSideAffected(final SootMethod method, final int paramPos, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if the parameter of a method is side-affected. This considers side-effect via access paths of length greater
	 * than 1.
	 *
	 * @param method of reference.
	 * @param paramPos is the position of the parameter.
	 *
	 * @return <code>true</code> if the parameter is side-affected; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided parameter position is invalid.
	 *
	 * @pre method != null and paramPos >= 0
	 */
	boolean isParameterSideAffected(final SootMethod method, final int paramPos)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at the receiver at the call-site is side-affected.
	 *
	 * @param callerTriple is the call-site.
	 * @param accesspath is the access path from the given parameter to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths of length  greater than 1 from
	 * 		  the end point. <code>false</code> indicates the check should consider access paths of length 1 from the end
	 * 		  point .
	 *
	 * @return <code>true</code> if the end point is side-affected; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided method is static.
	 *
	 * @pre method != null and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isReceiverBasedAccessPathSideAffected(final CallTriple callerTriple, final String[] accesspath,
		final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if receiver variable at the call-site is side-affected. This considers side-effect via access paths of length
	 * greater  than 1.
	 *
	 * @param callerTriple is the call-site.
	 *
	 * @return <code>true</code> if the receiver is side-affected; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the invoked method is static.
	 *
	 * @pre method != null
	 */
	boolean isReceiverSideAffected(final CallTriple callerTriple)
	  throws IllegalArgumentException;

	/**
	 * Checks if the end point of the access path starting at "this" variable of a method is side-affected.
	 *
	 * @param method of reference.
	 * @param accesspath is the access path from the given parameter to an end point.
	 * @param recurse <code>true</code> indicates that the check should consider access paths of length  greater than 1 from
	 * 		  the end point. <code>false</code> indicates the check should consider access paths of length 1 from the end
	 * 		  point .
	 *
	 * @return <code>true</code> if the end point is side-affected; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided method is static.
	 *
	 * @pre method != null and accesspath != null and accesspath->forall(o | o != null)
	 */
	boolean isThisBasedAccessPathSideAffected(final SootMethod method, final String[] accesspath, final boolean recurse)
	  throws IllegalArgumentException;

	/**
	 * Checks if "this" variable of the method is side-affected. This considers side-effect via access paths of length
	 * greater  than 1.
	 *
	 * @param method of reference.
	 *
	 * @return <code>true</code> if "this" is side-affected; <code>false</code>, otherwise.
	 *
	 * @throws IllegalArgumentException if the provided method is static.
	 *
	 * @pre method != null
	 */
	boolean isThisSideAffected(final SootMethod method)
	  throws IllegalArgumentException;

	/**
	 * Checks if the given invocation side affects any global data.
	 *
	 * @param callTriple is the invocation with the invocation expression, statement containing the invocation, and the
	 * 		  method in which the invocation occurs.
	 *
	 * @return <code>true</code> if the invocation side-affects; <code>false</code>, otherwise.
	 *
	 * @pre callTriple != null
	 */
	boolean doesInvocationAffectGlobalData(final CallTriple callTriple);

	/**
	 * Checks if the given method side affects any global data.
	 *
	 * @param method of interest.
	 *
	 * @return <code>true</code> if the method side-affects; <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	boolean doesMethodAffectGlobalData(final SootMethod method);
}

// End of File
