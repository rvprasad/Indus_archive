package edu.ksu.cis.bandera.bfa.analysis.ofa.fi;


import edu.ksu.cis.bandera.bfa.AbstractFGNode;
import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.MethodVariant;

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

import org.apache.log4j.Logger;

/**
 * StmtSwitch.java
 *
 *
 * Created: Sun Jan 27 13:28:32 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class StmtSwitch extends AbstractStmtSwitch {

	private static final Logger logger = Logger.getLogger(StmtSwitch.class.getName());

	public StmtSwitch (MethodVariant m){
		super(m);
	}

	public void caseAssignStmt(AssignStmt stmt) {
		rexpr.process(stmt.getRightOpBox());
		AbstractFGNode right = (AbstractFGNode)rexpr.getResult();
		lexpr.process(stmt.getLeftOpBox());
		AbstractFGNode left = (AbstractFGNode)lexpr.getResult();
		right.addSucc(left);
	}

	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	public void caseIdentityStmt(IdentityStmt stmt) {
		rexpr.process(stmt.getRightOpBox());
		AbstractFGNode right = (AbstractFGNode)rexpr.getResult();
		lexpr.process(stmt.getLeftOpBox());
		AbstractFGNode left = (AbstractFGNode)lexpr.getResult();
		right.addSucc(left);
	}

	public void caseIfStmt(IfStmt stmt) {
		rexpr.process(stmt.getConditionBox());
	}

	public void caseInvokeStmt(InvokeStmt stmt) {
		rexpr.process(stmt.getInvokeExprBox());
	}

	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		rexpr.process(stmt.getKeyBox());
	}

	public void caseRetStmt(RetStmt stmt) {
		rexpr.process(stmt.getStmtAddressBox());
	}

	public void caseReturnStmt(ReturnStmt stmt) {
		rexpr.process(stmt.getReturnValueBox());
	}

	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		rexpr.process(stmt.getKeyBox());
	}

	public void caseThrowStmt(ThrowStmt stmt) {
		rexpr.process(stmt.getOpBox());
	}

	public Object prototype(Object o) {
		return new StmtSwitch((MethodVariant)o);
	}

}// StmtSwitch
