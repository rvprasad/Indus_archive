
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
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
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi;

import soot.RefLikeType;

import soot.jimple.AssignStmt;
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

import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is used to process statements in object flow analysis. This class in turn uses a expression visitor to process
 * expressions that occur in a statement.
 * 
 * <p>
 * Created: Sun Jan 27 13:28:32 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class StmtSwitch
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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		rexpr.process(stmt.getRightOpBox());

		if (stmt.getRightOp().getType() instanceof RefLikeType) {
			IFGNode right = (IFGNode) rexpr.getResult();
			lexpr.process(stmt.getLeftOpBox());

			IFGNode left = (IFGNode) lexpr.getResult();
			right.addSucc(left);
		}
	}

	/**
	 * Processes the enter monitor statement.  Current implementation visits the monitor expression.
	 *
	 * @param stmt the enter monitor statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

		if (stmt.getRightOp().getType() instanceof RefLikeType) {
			rexpr.process(stmt.getRightOpBox());

			IFGNode right = (IFGNode) rexpr.getResult();
			lexpr.process(stmt.getLeftOpBox());

			IFGNode left = (IFGNode) lexpr.getResult();
			right.addSucc(left);
		}
	}

	/**
	 * Processes the if statement.  Current implementation visits the condition expression.
	 *
	 * @param stmt the if statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseIfStmt(final IfStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + stmt);
		}

		rexpr.process(stmt.getInvokeExprBox());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + stmt);
		}
	}

	/**
	 * Processes the lookup switch statement.  Current implementation visits the switch expression.
	 *
	 * @param stmt the lookup switch  statement to be processed.
	 *
	 * @pre stmt != null
	 */
	public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing statement: " + stmt);
		}

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

		rexpr.process(stmt.getOpBox());

		IFGNode retNode = (IFGNode) rexpr.getResult();
		retNode.addSucc(method.queryReturnNode());

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
}

/*
   ChangeLog:

   $Log$

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
 */
