
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2002, 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;


/**
 * This is used to process statements in object flow analysis. This class in turn uses a expression visitor to process
 * expressions that occur in a statement.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
class StmtSwitch
  extends AbstractStmtSwitch {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(StmtSwitch.class);

	/**
	 * Creates a new <code>StmtSwitch</code> instance.
	 *
	 * @param m the <code>MethodVariant</code> which uses this object.
	 *
	 * @pre m != null
	 */
	public StmtSwitch(final MethodVariant m) {
		super(m);
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the method variant which uses this object.  This is of type <code>MethodVariant</code>.
	 *
	 * @return the new instance of this class.
	 *
	 * @pre o != null and o.oclIsKindOf(MethodVariant)
	 * @post result != null and result.oclIsKindOf(StmtSwitch)
	 */
	public Object getClone(final Object o) {
		return new StmtSwitch((MethodVariant) o);
	}

	/**
	 * Processes the assignment statement.  It processes the rhs expression and the lhs expression and connects the flow
	 * graph nodes corresponding to these expressions.
	 *
	 * @param stmt the assignment statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseAssignStmt(final AssignStmt stmt) {
		processDefinitionStmt(stmt);
	}

	/**
	 * Processes the enter monitor statement.  Current implementation visits the monitor expression.
	 *
	 * @param stmt the enter monitor statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	/**
	 * Processes the exit monitor statement.  Current implementation visits the monitor expression.
	 *
	 * @param stmt the exit monitor statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	/**
	 * Processes the identity statement.  It processes the rhs expression and the lhs expression and connects the flow graph
	 * nodes corresponding to these expressions.
	 *
	 * @param stmt the identity statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseIdentityStmt(final IdentityStmt stmt) {
		processDefinitionStmt(stmt);
	}

	/**
	 * Processes the if statement.  Current implementation visits the condition expression.
	 *
	 * @param stmt the if statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseIfStmt(final IfStmt stmt) {
		rexpr.process(stmt.getConditionBox());
	}

	/**
	 * Processes the invoke statement.  Current implementation visits the invoke expression.
	 *
	 * @param stmt the invoke statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseInvokeStmt(final InvokeStmt stmt) {
		rexpr.process(stmt.getInvokeExprBox());
	}

	/**
	 * Processes the lookup switch statement.  Current implementation visits the switch expression.
	 *
	 * @param stmt the lookup switch  statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
		rexpr.process(stmt.getKeyBox());
	}

	/**
	 * Processes the return statement.  Current implementation visits the address expression.
	 *
	 * @param stmt the return statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseRetStmt(final RetStmt stmt) {
		rexpr.process(stmt.getStmtAddressBox());
	}

	/**
	 * Processes the return statement.  Current implementation visits the return value expression and connects it to node
	 * corresponding to the return node of the enclosing method variant.
	 *
	 * @param stmt the return statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseReturnStmt(final ReturnStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmt);
		}

		if (Util.isReferenceType(stmt.getOp().getType())) {
			rexpr.process(stmt.getOpBox());

			final IFGNode _retNode = (IFGNode) rexpr.getResult();
			_retNode.addSucc(method.queryReturnNode());
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmt);
		}
	}

	/**
	 * Processes the table switch statement.  Current implementation visits the switch expression.
	 *
	 * @param stmt the table switch  statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getKeyBox());
	}

	/**
	 * Processes the throw statement.  Current implementation visits the throw expression.
	 *
	 * @param stmt the throw statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseThrowStmt(final ThrowStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getOpBox());
	}

	/**
	 * Processes the definition statements.  It processes the rhs expression and the lhs expression and connects the flow
	 * graph nodes corresponding to these expressions.
	 *
	 * @param stmt the defintion statement to be processed.
	 *
	 * @pre stmt != null
	 */
	private void processDefinitionStmt(final DefinitionStmt stmt) {
		lexpr.process(stmt.getLeftOpBox());

		final IFGNode _left = (IFGNode) lexpr.getResult();

		rexpr.process(stmt.getRightOpBox());

		final IFGNode _right = (IFGNode) rexpr.getResult();

		if (Util.isReferenceType(stmt.getRightOp().getType())) {
			_right.addSucc(_left);
		}
	}
}

// End of File
