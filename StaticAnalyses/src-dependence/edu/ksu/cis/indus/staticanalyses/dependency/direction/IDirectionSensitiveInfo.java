package edu.ksu.cis.indus.staticanalyses.dependency.direction;


import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import java.util.Collection;
import java.util.List;

import soot.jimple.Stmt;

/**
 * This is the interface that can be used to access direction-sensitive information.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IDirectionSensitiveInfo {
	/**
	 * Retrieve the first statement in the given basic block depending on the direction of the analysis.
	 *
	 * @param bb is the basic block of interest.
	 *
	 * @return the first statement.
	 *
	 * @pre bb != null
	 * @post result != null and bb.getStmtsOf().contains(result)
	 */
	Stmt getFirstStmtInBB(final BasicBlock bb);

	/**
	 * Retrieves the basic blocks that follow the given basic block in a direction.
	 *
	 * @param bb is the basic block of interest.
	 *
	 * @return a collection of basic blocks.
	 *
	 * @pre bb != null
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
	 */
	Collection getFollowersOfBB(BasicBlock bb);

	/**
	 * Retrieves the dependents of the given divergence point in the given basic block.
	 *
	 * @param bb is the basic block containing <code>divPoint</code>.
	 * @param divPoint is the divergence point.
	 *
	 * @return the collection of statements.
	 *
	 * @pre bb != null and divPoint != null
	 * @pre bb.getStmtsOf().contains(divPoint)
	 */
	List getIntraBBDependents(final BasicBlock bb, final Stmt divPoint);
}

/*
ChangeLog:

$Log$
*/