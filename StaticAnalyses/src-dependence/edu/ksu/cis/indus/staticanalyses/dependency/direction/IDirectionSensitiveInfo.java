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

package edu.ksu.cis.indus.staticanalyses.dependency.direction;

import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import java.util.Collection;

import soot.jimple.Stmt;

/**
 * This is the interface that can be used to access direction-sensitive information.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IDirectionSensitiveInfo {

	/**
	 * Retrieve the first statement in the given basic block depending on the direction of the analysis.
	 * 
	 * @param bb is the basic block of interest.
	 * @return the first statement.
	 * @pre bb != null
	 * @post result != null and bb.getStmtsOf().contains(result)
	 */
	Stmt getFirstStmtInBB(final BasicBlock bb);

	/**
	 * Retrieves the basic blocks that follow the given basic block in a direction.
	 * 
	 * @param bb is the basic block of interest.
	 * @return a collection of basic blocks.
	 * @pre bb != null
	 * @post result != null
	 */
	Collection<BasicBlock> getFollowersOfBB(BasicBlock bb);

	/**
	 * Retrieves the dependents of the given statement in the given basic block.
	 * 
	 * @param bb is the basic block containing <code>stmt</code>.
	 * @param stmt is the divergence point.
	 * @return the collection of statements.
	 * @pre bb != null and stmt != null
	 * @pre bb.getStmtsOf().contains(stmt)
	 */
	Collection<Stmt> getIntraBBDependents(final BasicBlock bb, final Stmt stmt);
}

// End of File
