package edu.ksu.cis.indus.staticanalyses.dependency.direction;

import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import java.util.Collection;
import java.util.List;

import soot.jimple.Stmt;

/**
 * This class provides information to drive the analysis to generate information that is backward in direction.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class BackwardDirectionSensitiveInfo
  implements IDirectionSensitiveInfo {
	/**
	 * @see IDirectionSensitiveInfo#getFirstStmtInBB(BasicBlock)
	 */
	public Stmt getFirstStmtInBB(final BasicBlock bb) {
		return bb.getLeaderStmt();
	}

	/**
	 * @see IDirectionSensitiveInfo#getFollowersOfBB(edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock)
	 */
	public Collection getFollowersOfBB(final BasicBlock bb) {
		return bb.getSuccsOf();
	}

	/**
	 * {@inheritDoc}  This provides information for backward direciton.
	 */
	public List getIntraBBDependents(final BasicBlock bb, final Stmt divPoint) {
		final List _stmtsFrom = bb.getStmtsFrom(divPoint);
		_stmtsFrom.remove(divPoint);
		return _stmtsFrom;
	}
}

/*
ChangeLog:

$Log$
*/