package edu.ksu.cis.bandera.bfa.analysis.ofa.fs;

import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.DefinitionStmt;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.ValueBox;
import ca.mcgill.sable.util.Iterator;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * ExprSwitch.java
 *
 *
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ExprSwitch extends edu.ksu.cis.bandera.bfa.analysis.ofa.fi.ExprSwitch {

	private static final Logger logger = LogManager.getLogger(ExprSwitch.class);

	public ExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		super(stmt, connector);
	}

	public void caseArrayRef(ArrayRef e) {
		super.caseArrayRef(e);
		postProcessBase(e.getBaseBox());
	}

	public void caseArrayRef(InstanceFieldRef e) {
		super.caseInstanceFieldRef(e);
		postProcessBase(e.getBaseBox());
	}

	public void postProcessBase(ValueBox e) {
		Local l = (Local)e.getValue();
		ValueBox backup = context.setProgramPoint(e);
		FGNode localNode = method.getASTNode(l);
		for (Iterator i = method.defs.getDefsOfAt(l, stmt.getStmt()).iterator(); i.hasNext();) {
			DefinitionStmt defStmt = (DefinitionStmt)i.next();
			context.setProgramPoint(defStmt.getLeftOpBox());
			FGNode defNode = method.getASTNode(defStmt.getLeftOp());
			logger.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
			defNode.addSucc(localNode);
		} // end of for (Iterator i = defs.getDefsOfAt(e, stmt.stmt).iterator(); i.hasNext();)
		context.setProgramPoint(backup);
	}

	public void process(ValueBox vb) {
		ValueBox temp = context.setProgramPoint(vb);
		super.process(vb);
		context.setProgramPoint(temp);
	}

	public Object prototype(Object o) {
		return new ExprSwitch((AbstractStmtSwitch)o, connector);
	}

}// ExprSwitch
