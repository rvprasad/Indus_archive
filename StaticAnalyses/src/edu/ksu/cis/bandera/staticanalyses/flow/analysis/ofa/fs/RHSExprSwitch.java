package edu.ksu.cis.bandera.bfa.analysis.ofa.fs;

import ca.mcgill.sable.soot.jimple.DefinitionStmt;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.util.Iterator;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import ca.mcgill.sable.soot.jimple.ValueBox;

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

	private static final Logger logger = LogManager.getLogger(RHSExprSwitch.class);

	public RHSExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		super(stmt, connector);
	}

	public void caseLocal(Local e) {
		FGNode ast = method.getASTNode(e);
		logger.debug("Local:" + e + "\n" + ast);
		ValueBox temp = context.getProgramPoint();
		for (Iterator i = method.defs.getDefsOfAt(e, stmt.getStmt()).iterator(); i.hasNext();) {
			DefinitionStmt defStmt = (DefinitionStmt)i.next();
			context.setProgramPoint(defStmt.getLeftOpBox());
			FGNode defNode = method.getASTNode(defStmt.getLeftOp());
			logger.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
			defNode.addSucc(ast);
		} // end of for (Iterator i = defs.getDefsOfAt(e, stmt.stmt).iterator(); i.hasNext();)
		context.setProgramPoint(temp);
		setResult(ast);
	}

	public Object prototype(Object o) {
		return new RHSExprSwitch((AbstractStmtSwitch)o, connector);
	}

}// RHSExprSwitch
