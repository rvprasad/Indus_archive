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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.processing.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

/**
 * The statement visitor class. This class provides the default implementation for all the statements that need to be dealt at
 * Jimple level. The class is tagged as <code>abstract</code> to force the users to extend the class as required. It extends
 * <code>AbstractJimpleStmtSwitch</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <S> DOCUMENT ME!
 * @param <N> DOCUMENT ME!
 */
public abstract class AbstractStmtSwitch<S extends AbstractStmtSwitch<S, N>, N extends IFGNode<?, ?, N>>
		extends soot.jimple.AbstractStmtSwitch
		implements IStmtSwitch {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStmtSwitch.class);

	/**
	 * The context in which this object should process statements. It is possible for this object to alter the context, but it
	 * should restore it back to it's initial state before returning from it's methods.
	 */
	protected final Context context;

	/**
	 * The LHS expression visitor used to this object to process LHS expressions.
	 */
	protected final IExprSwitch<N> lexpr;

	/**
	 * The method variant in which this visitor is used.
	 */
	protected final IMethodVariant<N> method;

	/**
	 * The RHS expression visitor used to this object to process RHS expressions.
	 */
	protected final IExprSwitch<N> rexpr;

	/**
	 * Creates a new <code>AbstractStmtSwitch</code> instance. In non-prototype mode, all of the fields (declared in this
	 * class) will be non-null after returning from the constructor.
	 *
	 * @param m the method variant in which this visitor is used.
	 */
	protected AbstractStmtSwitch(final IMethodVariant<N> m) {
		method = m;

		if (m != null) {
			context = m.getContext();
			lexpr = m.getFA().getLHSExpr(this);
			rexpr = m.getFA().getRHSExpr(this);
		} else {
			context = null;
			lexpr = null;
			rexpr = null;
		}
	}

	/**
	 * Processes the assignment statement. It processes the rhs expression and the lhs expression and connects the flow graph
	 * nodes corresponding to these expressions.
	 *
	 * @param stmt the assignment statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseAssignStmt(final AssignStmt stmt) {
		processDefinitionStmt(stmt);
	}

	/**
	 * Processes the enter monitor statement. Current implementation visits the monitor expression.
	 *
	 * @param stmt the enter monitor statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	/**
	 * Processes the exit monitor statement. Current implementation visits the monitor expression.
	 *
	 * @param stmt the exit monitor statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	/**
	 * Processes the identity statement. It processes the rhs expression and the lhs expression and connects the flow graph
	 * nodes corresponding to these expressions.
	 *
	 * @param stmt the identity statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseIdentityStmt(final IdentityStmt stmt) {
		processDefinitionStmt(stmt);
	}

	/**
	 * Processes the if statement. Current implementation visits the condition expression.
	 *
	 * @param stmt the if statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseIfStmt(final IfStmt stmt) {
		rexpr.process(stmt.getConditionBox());
	}

	/**
	 * Processes the invoke statement. Current implementation visits the invoke expression.
	 *
	 * @param stmt the invoke statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseInvokeStmt(final InvokeStmt stmt) {
		rexpr.process(stmt.getInvokeExprBox());
	}

	/**
	 * Processes the lookup switch statement. Current implementation visits the switch expression.
	 *
	 * @param stmt the lookup switch statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
		rexpr.process(stmt.getKeyBox());
	}

	/**
	 * Processes the return statement. Current implementation visits the address expression.
	 *
	 * @param stmt the return statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseRetStmt(final RetStmt stmt) {
		rexpr.process(stmt.getStmtAddressBox());
	}

	/**
	 * Processes the return statement. Current implementation visits the return value expression and connects it to node
	 * corresponding to the return node of the enclosing method variant.
	 *
	 * @param stmt the return statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseReturnStmt(final ReturnStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmt);
		}

		if (Util.isReferenceType(stmt.getOp().getType())) {
			rexpr.process(stmt.getOpBox());

			final N _retNode = rexpr.getFlowNode();
			_retNode.addSucc(method.queryReturnNode());
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmt);
		}
	}

	/**
	 * Processes the table switch statement. Current implementation visits the switch expression.
	 *
	 * @param stmt the table switch statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getKeyBox());
	}

	/**
	 * Processes the throw statement. Current implementation visits the throw expression.
	 *
	 * @param stmt the throw statement to be processed.
	 * @pre stmt != null
	 */
	@Override public void caseThrowStmt(final ThrowStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getOpBox());
	}

	/**
	 * Handles situations when alien statement types are visited, i.e., there are no instructions available on how to handle a
	 * particular statement type.
	 *
	 * @param o the statement to be visited.
	 * @pre o != null
	 */
	@Override public void defaultCase(final Object o) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(o + " is not handled.");
		}
	}

	/**
	 * This method is not supproted. To be implemented by subclasses.
	 *
	 * @param o is ignored.
	 * @return (This method will raise an exception.)
	 * @throws UnsupportedOperationException as the operation is not supported.
	 */
	public S getClone(@SuppressWarnings("unused") final Object... o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

	/**
	 * Process the given statement. The usual implementation would be visit the expressions in the statement.
	 *
	 * @param stmtToProcess the statement being visited or to be processed.
	 * @pre stmtToProcess != null
	 */
	public void process(final Stmt stmtToProcess) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmtToProcess);
		}

		final Stmt _temp = context.setStmt(stmtToProcess);
		stmtToProcess.apply(this);
		context.setStmt(_temp);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmtToProcess);
		}
	}

	/**
	 * Processes the definition statements. It processes the rhs expression and the lhs expression and connects the flow graph
	 * nodes corresponding to these expressions.
	 *
	 * @param stmt the defintion statement to be processed.
	 * @pre stmt != null
	 */
	protected abstract void processDefinitionStmt(final DefinitionStmt stmt);

}

// End of File
