
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

/**
 * This class is used to indicate the status of an object before dispatching any methods on it.  The intent of this interface
 * is that the users of information-providing interfaces should be able to check if the object providing that interface is
 * in a stable state to do so.  Hence, it is adviced that information-providing interfaces inherit this interface.  On a
 * general note,  this interface can be used in situations where a support to query status is required.
 * 
 * <p>
 * A note on stability.  In most cases, active entities like analysis may provide different answers for identical queries at
 * different times when they are active, i.e, when analysis is happening. However, while not active, it will usually provide
 * the same answer for identical queries at different times.  Nevertheless the answer may be incorrect, but the analysis is
 * consistent in it's answer. It is this state in which the analysis provide consistent answers that we refer to as stable
 * state,  a state in which the external behavior of an object will be consistent.  Hence, implementation can use
 * <code>isStable</code> to indicate/detect activeness.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IStatus {
	/**
	 * Checks if the object is in a stable state so that it can be queried for information.
	 *
	 * @return <code>true</code> if the implementation is in a stable state; <code>false</code>, otherwise.
	 */
	boolean isStable();
}

// End of File
