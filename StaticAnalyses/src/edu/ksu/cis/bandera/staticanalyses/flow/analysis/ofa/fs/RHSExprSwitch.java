package edu.ksu.cis.bandera.bfa.analysis.ofa.fs;

import edu.ksu.cis.bandera.bfa.AbstractFGNode;
import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;

import ca.mcgill.sable.soot.jimple.CompleteStmtGraph;
import ca.mcgill.sable.soot.jimple.DefinitionStmt;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.SimpleLocalDefs;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.util.Iterator;

import org.apache.log4j.Category;

/**
 * RHSExprSwitch.java
 *
 *
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class RHSExprSwitch extends ExprSwitch {

	private static final Category cat = Category.getInstance(RHSExprSwitch.class.getName());

	public RHSExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		super(stmt, connector);
	}

	public void caseLocal(Local e) {
		StmtList list = ((StmtBody)method.sm.getBody(method.bodyrep)).getStmtList();
		SimpleLocalDefs defs = new SimpleLocalDefs(new CompleteStmtGraph(list));
		AbstractFGNode ast = method.getASTNode(e);
		for (Iterator i = defs.getDefsOfAt(e, stmt.getStmt()).iterator(); i.hasNext();) {
			DefinitionStmt defStmt = (DefinitionStmt)i.next();
			context.setProgramPoint(defStmt.getLeftOpBox());
			AbstractFGNode defNode = method.getASTNode(defStmt.getLeftOp());
			defNode.addSucc(ast);
		} // end of for (Iterator i = defs.getDefsOfAt(e, stmt.stmt).iterator(); i.hasNext();)

		setResult(ast);
	}

	public Object prototype(Object o) {
		return new RHSExprSwitch((AbstractStmtSwitch)o, connector);
	}

}// RHSExprSwitch
