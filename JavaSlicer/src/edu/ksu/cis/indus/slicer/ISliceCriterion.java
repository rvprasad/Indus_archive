
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

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import soot.SootMethod;


/**
 * This is an interface via which slice criterion is exposed to the external world.
 * 
 * <p>
 * The purpose of this interface is to identify an object as a poolable slicing criterion.  To be more precise,
 * </p>
 * 
 * <ul>
 * <li>
 * as it is poolable, it means  that  the application obtaining/creating a criterion with this interface is responsible for
 * returning the criterion to the pool and
 * </li>
 * <li>
 * to identify an object as a criterion
 * </li>
 * </ul>
 * 
 * <p>
 * Does this mean that any object that implements this interface is a valid slice criterion?  NO!  In other words, all slice
 * criteria accepted by the slicing engine will implement this interface but not all objects implementing this interface can
 * be slicing criteria.  Hence, the user may use this interface to identify if an object is  a slicing criterion.  However,
 * he/she should not use this interface to provide a new implementation of the slicing criterion as the slicing engine places
 * certain internal requirements on the implementation of the criterion.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISliceCriterion {
	/**
	 * Retrieves the call stack.
	 *
	 * @return the call stack.
	 *
	 * @post result != null
	 */
	Stack<CallTriple> getCallStack();

	/**
	 * Provides the method in which criterion occurs.
	 *
	 * @return the method in which the slice statement occurs.
	 *
	 * @post result != null
	 */
	SootMethod getOccurringMethod();

	/**
	 * Sets the call stack of call triples leading upto the method containing the criteria.
	 *
	 * @param theCallStack obviously.
	 *
	 * @pre theCallStack != null
	 */
	void setCallStack(final Stack<CallTriple> theCallStack);
}

// End of File
