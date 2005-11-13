
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
	Comparable<? extends Object> ID = "Concurrent Independence Analysis";

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
