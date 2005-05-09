
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;


/**
 * This class encapsulates the logic to process the statements during escape analysis.  Each overridden methods in  this
 * class will process the expressions in the statement and unify them as per to the rules associated with the statements.
 * 
 * <p>
 * The arguments to any of the overridden methods cannot be <code>null</code>.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
final class StmtProcessor
  extends AbstractStmtSwitch {
	/** 
	 * The associated escape analysis. 
	 */
	private final EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Creates an instance of this class.
	 *
	 * @param analysis associated with this instance.
     * @pre analysis != null
	 */
	StmtProcessor(final EquivalenceClassBasedEscapeAnalysis analysis) {
		ecba = analysis;
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
	 */
	public void caseAssignStmt(final AssignStmt stmt) {
		boolean _temp = ecba.valueProcessor.rhs;
		ecba.valueProcessor.rhs = true;
		ecba.valueProcessor.process(stmt.getRightOp());
		ecba.valueProcessor.rhs = _temp;

		final AliasSet _r = (AliasSet) ecba.valueProcessor.getResult();
		_temp = ecba.valueProcessor.rhs;
		ecba.valueProcessor.rhs = false;
		ecba.valueProcessor.process(stmt.getLeftOp());
		ecba.valueProcessor.rhs = _temp;

		final AliasSet _l = (AliasSet) ecba.valueProcessor.getResult();

		if ((_r != null) && (_l != null)) {
			_l.unifyAliasSet(_r);
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
	 */
	public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
		ecba.valueProcessor.process(stmt.getOp());
		((AliasSet) ecba.valueProcessor.getResult()).addNewLockEntity();
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
	 */
	public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
		ecba.valueProcessor.process(stmt.getOp());
		((AliasSet) ecba.valueProcessor.getResult()).addNewLockEntity();
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
	 */
	public void caseIdentityStmt(final IdentityStmt stmt) {
		boolean _temp = ecba.valueProcessor.rhs;
		ecba.valueProcessor.rhs = true;
		ecba.valueProcessor.process(stmt.getRightOp());
		ecba.valueProcessor.rhs = _temp;

		final AliasSet _r = (AliasSet) ecba.valueProcessor.getResult();
		_temp = ecba.valueProcessor.rhs;
		ecba.valueProcessor.rhs = false;
		ecba.valueProcessor.process(stmt.getLeftOp());
		ecba.valueProcessor.rhs = _temp;

		final AliasSet _l = (AliasSet) ecba.valueProcessor.getResult();

		if ((_r != null) && (_l != null)) {
			_l.unifyAliasSet(_r);
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
	 */
	public void caseInvokeStmt(final InvokeStmt stmt) {
		ecba.valueProcessor.process(stmt.getInvokeExpr());
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
	 */
	public void caseReturnStmt(final ReturnStmt stmt) {
		ecba.valueProcessor.process(stmt.getOp());

		final AliasSet _l = (AliasSet) ecba.valueProcessor.getResult();

		if (_l != null) {
			ecba.methodCtxtCache.getReturnAS().unifyAliasSet(_l);
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
	 */
	public void caseThrowStmt(final ThrowStmt stmt) {
		ecba.valueProcessor.process(stmt.getOp());

		final AliasSet _l = (AliasSet) ecba.valueProcessor.getResult();

		if (_l != null) {
			ecba.methodCtxtCache.getThrownAS().unifyAliasSet(_l);
		}
	}

	/**
	 * Processes the given statement.
	 *
	 * @param stmt to be processed.
	 *
	 * @pre stmt != null
	 */
	void process(final Stmt stmt) {
		if (EquivalenceClassBasedEscapeAnalysis.STMT_PROCESSOR_LOGGER.isTraceEnabled()) {
			EquivalenceClassBasedEscapeAnalysis.STMT_PROCESSOR_LOGGER.trace("Processing statement: " + stmt);
		}
		stmt.apply(this);
	}
}

// End of File
