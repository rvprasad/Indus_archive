
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
import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


// StmtSwitch.java

/**
 * <p>This is the statement visitor class.  It is used by the method variant to process statements in object flow analysis.
 * This class in turn uses a expression visitor process expressions that occur in a statement.</p>
 *
 * Created: Sun Jan 27 13:28:32 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class StmtSwitch
  extends AbstractStmtSwitch {
	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(StmtSwitch.class);

	/**
	 * <p>Creates a new <code>StmtSwitch</code> instance.</p>
	 *
	 * @param m the <code>MethodVariant</code> which uses this object.
	 */
	public StmtSwitch(MethodVariant m) {
		super(m);
	}

	/**
	 * <p>Processes the assignment statement.  It processes the rhs expression and the lhs expression and connects the flow
	 * graph nodes corresponding to these expressions.</p>
	 *
	 * @param stmt the assignment statement to be processed.
	 */
	public void caseAssignStmt(AssignStmt stmt) {
		rexpr.process(stmt.getRightOpBox());

		FGNode right = (FGNode)rexpr.getResult();
		lexpr.process(stmt.getLeftOpBox());

		FGNode left = (FGNode)lexpr.getResult();
		right.addSucc(left);
	}

	/**
	 * <p>Processes the enter monitor statement.  Current implementation visits the monitor expression.</p>
	 *
	 * @param stmt the enter monitor statement to be processed.
	 */
	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	/**
	 * <p>Processes the exit monitor statement.  Current implementation visits the monitor expression.</p>
	 *
	 * @param stmt the exit monitor statement to be processed.
	 */
	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	/**
	 * <p>Processes the identity statement.  It processes the rhs expression and the lhs expression and connects the flow
	 * graph nodes corresponding to these expressions.</p>
	 *
	 * @param stmt the identity statement to be processed.
	 */
	public void caseIdentityStmt(IdentityStmt stmt) {
		rexpr.process(stmt.getRightOpBox());

		FGNode right = (FGNode)rexpr.getResult();
		lexpr.process(stmt.getLeftOpBox());

		FGNode left = (FGNode)lexpr.getResult();
		right.addSucc(left);
	}

	/**
	 * <p>Processes the if statement.  Current implementation visits the condition expression.</p>
	 *
	 * @param stmt the if statement to be processed.
	 */
	public void caseIfStmt(IfStmt stmt) {
		rexpr.process(stmt.getConditionBox());
	}

	/**
	 * <p>Processes the invoke statement.  Current implementation visits the invoke expression.</p>
	 *
	 * @param stmt the invoke statement to be processed.
	 */
	public void caseInvokeStmt(InvokeStmt stmt) {
		rexpr.process(stmt.getInvokeExprBox());
	}

	/**
	 * <p>Processes the lookup switch statement.  Current implementation visits the switch expression.</p>
	 *
	 * @param stmt the lookup switch  statement to be processed.
	 */
	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		rexpr.process(stmt.getKeyBox());
	}

	/**
	 * <p>Processes the return statement.  Current implementation visits the address expression.</p>
	 *
	 * @param stmt the return statement to be processed.
	 */
	public void caseRetStmt(RetStmt stmt) {
		rexpr.process(stmt.getStmtAddressBox());
	}

	/**
	 * <p>Processes the return statement.  Current implementation visits the return value expression and connects it to node
	 * corresponding to the return node of the enclosing method variant.</p>
	 *
	 * @param stmt the return statement to be processed.
	 */
	public void caseReturnStmt(ReturnStmt stmt) {
		rexpr.process(stmt.getReturnValueBox());
		((FGNode)rexpr.getResult()).addSucc(method.queryReturnNode());
	}

	/**
	 * <p>Processes the table switch statement.  Current implementation visits the switch expression.</p>
	 *
	 * @param stmt the table switch  statement to be processed.
	 */
	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		rexpr.process(stmt.getKeyBox());
	}

	/**
	 * <p>Processes the throw statement.  Current implementation visits the throw expression.</p>
	 *
	 * @param stmt the throw statement to be processed.
	 */
	public void caseThrowStmt(ThrowStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	/**
	 * <p>Returns a new instance of this class.</p>
	 *
	 * @param o the method variant which uses this object.  This is of type <code>MethodVariant</code>.
	 * @return the new instance of this class.
	 */
	public Object prototype(Object o) {
		return new StmtSwitch((MethodVariant)o);
	}
} // StmtSwitch
