
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi;

import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.EnterMonitorStmt;
import ca.mcgill.sable.soot.jimple.ExitMonitorStmt;
import ca.mcgill.sable.soot.jimple.IdentityStmt;
import ca.mcgill.sable.soot.jimple.IfStmt;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
import ca.mcgill.sable.soot.jimple.LookupSwitchStmt;
import ca.mcgill.sable.soot.jimple.RetStmt;
import ca.mcgill.sable.soot.jimple.ReturnStmt;
import ca.mcgill.sable.soot.jimple.TableSwitchStmt;
import ca.mcgill.sable.soot.jimple.ThrowStmt;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.IFGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <p>
 * This is the statement visitor class.  It is used by the method variant to process statements in object flow analysis. This
 * class in turn uses a expression visitor process expressions that occur in a statement.
 * </p>
 * Created: Sun Jan 27 13:28:32 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class StmtSwitch
  extends AbstractStmtSwitch {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(StmtSwitch.class);

	/**
	 * <p>
	 * Creates a new <code>StmtSwitch</code> instance.
	 * </p>
	 *
	 * @param m the <code>MethodVariant</code> which uses this object.
	 */
	public StmtSwitch(MethodVariant m) {
		super(m);
	}

	/**
	 * <p>
	 * Processes the assignment statement.  It processes the rhs expression and the lhs expression and connects the flow
	 * graph nodes corresponding to these expressions.
	 * </p>
	 *
	 * @param stmt the assignment statement to be processed.
	 */
	public void caseAssignStmt(AssignStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmt);
		}
		rexpr.process(stmt.getRightOpBox());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processed RHS");
		}

		IFGNode right = (IFGNode) rexpr.getResult();
		lexpr.process(stmt.getLeftOpBox());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processed LHS");
		}

		IFGNode left = (IFGNode) lexpr.getResult();
		right.addSucc(left);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmt);
		}
	}

	/**
	 * <p>
	 * Processes the enter monitor statement.  Current implementation visits the monitor expression.
	 * </p>
	 *
	 * @param stmt the enter monitor statement to be processed.
	 */
	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getOpBox());
	}

	/**
	 * <p>
	 * Processes the exit monitor statement.  Current implementation visits the monitor expression.
	 * </p>
	 *
	 * @param stmt the exit monitor statement to be processed.
	 */
	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getOpBox());
	}

	/**
	 * <p>
	 * Processes the identity statement.  It processes the rhs expression and the lhs expression and connects the flow graph
	 * nodes corresponding to these expressions.
	 * </p>
	 *
	 * @param stmt the identity statement to be processed.
	 */
	public void caseIdentityStmt(IdentityStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getRightOpBox());

		IFGNode right = (IFGNode) rexpr.getResult();
		lexpr.process(stmt.getLeftOpBox());

		IFGNode left = (IFGNode) lexpr.getResult();
		right.addSucc(left);
	}

	/**
	 * <p>
	 * Processes the if statement.  Current implementation visits the condition expression.
	 * </p>
	 *
	 * @param stmt the if statement to be processed.
	 */
	public void caseIfStmt(IfStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getConditionBox());
	}

	/**
	 * <p>
	 * Processes the invoke statement.  Current implementation visits the invoke expression.
	 * </p>
	 *
	 * @param stmt the invoke statement to be processed.
	 */
	public void caseInvokeStmt(InvokeStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmt);
		}

		rexpr.process(stmt.getInvokeExprBox());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmt);
		}
	}

	/**
	 * <p>
	 * Processes the lookup switch statement.  Current implementation visits the switch expression.
	 * </p>
	 *
	 * @param stmt the lookup switch  statement to be processed.
	 */
	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getKeyBox());
	}

	/**
	 * <p>
	 * Processes the return statement.  Current implementation visits the address expression.
	 * </p>
	 *
	 * @param stmt the return statement to be processed.
	 */
	public void caseRetStmt(RetStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getStmtAddressBox());
	}

	/**
	 * <p>
	 * Processes the return statement.  Current implementation visits the return value expression and connects it to node
	 * corresponding to the return node of the enclosing method variant.
	 * </p>
	 *
	 * @param stmt the return statement to be processed.
	 */
	public void caseReturnStmt(ReturnStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmt);
		}

		rexpr.process(stmt.getReturnValueBox());

		IFGNode retNode = (IFGNode) rexpr.getResult();
		retNode.addSucc(method.queryReturnNode());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmt);
		}
	}

	/**
	 * <p>
	 * Processes the table switch statement.  Current implementation visits the switch expression.
	 * </p>
	 *
	 * @param stmt the table switch  statement to be processed.
	 */
	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getKeyBox());
	}

	/**
	 * <p>
	 * Processes the throw statement.  Current implementation visits the throw expression.
	 * </p>
	 *
	 * @param stmt the throw statement to be processed.
	 */
	public void caseThrowStmt(ThrowStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getOpBox());
	}

	/**
	 * <p>
	 * Returns a new instance of this class.
	 * </p>
	 *
	 * @param o the method variant which uses this object.  This is of type <code>MethodVariant</code>.
	 *
	 * @return the new instance of this class.
	 */
	public Object prototype(Object o) {
		return new StmtSwitch((MethodVariant) o);
	}
}

/*****
 ChangeLog:

$Log$

*****/
