
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
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
		boolean flag = OFAnalyzer.isReferenceType(stmt.getRightOp().getType());
		IFGNode left = null;
		Value leftOp = stmt.getLeftOp();

		if (flag || leftOp instanceof ArrayRef || leftOp instanceof FieldRef) {
			lexpr.process(stmt.getLeftOpBox());
			left = (IFGNode) lexpr.getResult();
		}

		IFGNode right = null;
		Value rightOp = stmt.getRightOp();

		if (flag || rightOp instanceof ArrayRef || rightOp instanceof FieldRef || rightOp instanceof InvokeExpr) {
			rexpr.process(stmt.getRightOpBox());
			right = (IFGNode) rexpr.getResult();
		}

		if (flag) {
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
		boolean flag = OFAnalyzer.isReferenceType(stmt.getRightOp().getType());
		IFGNode left = null;
		Value leftOp = stmt.getLeftOp();

		if (flag || leftOp instanceof ArrayRef || leftOp instanceof FieldRef) {
			lexpr.process(stmt.getLeftOpBox());
			left = (IFGNode) lexpr.getResult();
		}

		IFGNode right = null;
		Value rightOp = stmt.getRightOp();

		if (flag || rightOp instanceof ArrayRef || rightOp instanceof FieldRef || rightOp instanceof InvokeExpr) {
			rexpr.process(stmt.getRightOpBox());
			right = (IFGNode) rexpr.getResult();
		}

		if (flag) {
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

		if (OFAnalyzer.isReferenceType(stmt.getOp().getType())) {
			rexpr.process(stmt.getOpBox());

			IFGNode retNode = (IFGNode) rexpr.getResult();
			retNode.addSucc(method.queryReturnNode());
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
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/12/05 15:31:05  venku
   - identity and assignment statements skipped certain values.  FIXED.

   Revision 1.5  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/26 17:53:55  venku
   Actually we can use the types to cut down the number of edges
   between the flow nodes. The current fix uses a method in OFAnalyzer
   to check for reference types, only if the type matches the given expression
   is processed.  However, this does not apply for staticfield, instancefield, and
   array access expressions.
   Revision 1.2  2003/08/15 02:54:06  venku
   Spruced up specification and documentation for flow-insensitive classes.
   Changed names in AbstractExprSwitch.
   Ripple effect of above change.
   Formatting changes to IPrototype.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
 */
