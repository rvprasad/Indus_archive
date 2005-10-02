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

package edu.ksu.cis.indus.processing;

import java.util.Collection;
import java.util.List;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * This interface is used to retrieve a collection of statement sequences. This is used in conjuction with controlling the
 * order of visiting parts of the system.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IStmtSequencesRetriever {

	/**
	 * Retrievs a collection of statement sequences.
	 * 
	 * @param method for which the statement sequences are requested.
	 * @return a collection of statement sequence.
	 * @pre method != null
	 * @post result != null
	 * @post not result->exists(o | o == null)
	 */
	Collection<List<Stmt>> retreiveStmtSequences(SootMethod method);
}

// End of File
