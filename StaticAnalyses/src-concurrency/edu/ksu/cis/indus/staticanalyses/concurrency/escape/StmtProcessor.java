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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * This class encapsulates the logic to process the statements during escape analysis. Each overridden methods in this class
 * will process the expressions in the statement and unify them as per to the rules associated with the statements.
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
	 * The logger used by instances of <code>StmtProcessor</code> class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(StmtProcessor.class);

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
	 * {@inheritDoc}
	 * 
	 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
	 */
	@Override public void caseAssignStmt(final AssignStmt stmt) {
		final boolean _temp = ecba.valueProcessor.setRHS(true);
		ecba.valueProcessor.process(stmt.getRightOp());
		final AliasSet _r = (AliasSet) ecba.valueProcessor.getResult();
		ecba.valueProcessor.setRHS(false);
		ecba.valueProcessor.process(stmt.getLeftOp());
		final AliasSet _l = (AliasSet) ecba.valueProcessor.getResult();
		ecba.valueProcessor.setRHS(_temp);

		if ((_r != null) && (_l != null)) {
			_l.unifyAliasSet(_r);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
	 */
	@Override public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
		ecba.valueProcessor.process(stmt.getOp());
		((AliasSet) ecba.valueProcessor.getResult()).setLocked();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
	 */
	@Override public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
		ecba.valueProcessor.process(stmt.getOp());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
	 */
	@Override public void caseIdentityStmt(final IdentityStmt stmt) {
		final boolean _t = ecba.valueProcessor.setMarkLocals(false);
		ecba.valueProcessor.process(stmt.getRightOp());

		final AliasSet _r = (AliasSet) ecba.valueProcessor.getResult();
		ecba.valueProcessor.process(stmt.getLeftOp());

		final AliasSet _l = (AliasSet) ecba.valueProcessor.getResult();
		ecba.valueProcessor.setMarkLocals(_t);

		if ((_r != null) && (_l != null)) {
			_l.unifyAliasSet(_r);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
	 */
	@Override public void caseInvokeStmt(final InvokeStmt stmt) {
		ecba.valueProcessor.process(stmt.getInvokeExpr());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
	 */
	@Override public void caseReturnStmt(final ReturnStmt stmt) {
		ecba.valueProcessor.process(stmt.getOp());

		final AliasSet _l = (AliasSet) ecba.valueProcessor.getResult();

		if (_l != null) {
			ecba.methodCtxtCache.getReturnAS().unifyAliasSet(_l);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
	 */
	@Override public void caseThrowStmt(final ThrowStmt stmt) {
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
	 * @pre stmt != null
	 */
	void process(final Stmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}
		stmt.apply(this);
	}
}

// End of File
