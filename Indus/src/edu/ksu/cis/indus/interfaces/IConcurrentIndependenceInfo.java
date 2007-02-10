
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

import soot.jimple.Stmt;


/**
 * This interface exposes independence information in the context of concurrency.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IConcurrentIndependenceInfo
  extends IIdentification {
	/**
	 * This is the unique identifier that can be used to identify an instance of this class.
	 */
	Comparable<String> ID = "Concurrent Independence Analysis";

	/**
	 * Checks if the statement is independent.
	 *
	 * @param stmt to be checked.
	 *
	 * @return <code>true</code> if <code>stmt</code> is independent; <code>false</code>, otherwise.
	 *
	 * @pre stmt != null
	 */
	boolean isIndependent(final Stmt stmt);
}

// End of File
