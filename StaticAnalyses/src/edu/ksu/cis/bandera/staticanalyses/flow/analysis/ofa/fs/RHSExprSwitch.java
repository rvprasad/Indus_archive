package edu.ksu.cis.bandera.bfa.analysis.ofa.fs;




import ca.mcgill.sable.soot.jimple.DefinitionStmt;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.ValueBox;
import ca.mcgill.sable.util.Iterator;
import ca.mcgill.sable.util.List;
import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// RHSExprSwitch.java
/**
 * <p>This is the RHS expression visitor.  It provides implementation to handle locals on RHS in flow sensitive mode.</p>
 *
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class RHSExprSwitch extends ExprSwitch {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(RHSExprSwitch.class);

	/**
	 * <p>Creates a new <code>RHSExprSwitch</code> instance.</p>
	 *
	 * @param stmt the statement visitor which uses this instance of expression visitor.
	 * @param connector the connector to be used to connect the ast and non-ast nodes.
	 */
	public RHSExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		super(stmt, connector);
	}

	/**
	 * <p>Handles the expression containing local variables on rhs.  It connects the flow graph nodes corresponding to def
	 * sites of the locals to the nodes corresponding to the  use sites of the locals.</p>
	 *
	 * @param e the local to be visited.
	 */
	public void caseLocal(Local e) {
		FGNode ast = method.getASTNode(e);
		logger.debug("Local:" + e + "\n" + ast);
		List l = method.defs.getDefsOfAt(e, stmt.getStmt());
		if (l != null) {
			ValueBox temp = context.getProgramPoint();
			for (Iterator i = l.iterator(); i.hasNext();) {
				DefinitionStmt defStmt = (DefinitionStmt)i.next();
				context.setProgramPoint(defStmt.getLeftOpBox());
				FGNode defNode = method.getASTNode(defStmt.getLeftOp());
				logger.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
				defNode.addSucc(ast);
			} // end of for (Iterator i = defs.getDefsOfAt(e, stmt.stmt).iterator(); i.hasNext();)
			context.setProgramPoint(temp);
		} // end of if (l != null)
		setResult(ast);
	}

	/**
	 * <p>Returns a new instance of this class.</p>
	 *
	 * @param o the statement visitor which shall use the created visitor instance.  This is of type
	 * <code>AbstractStmtSwitch</code>.
	 * @return the new visitor instance.
	 */
	public Object prototype(Object o) {
		return new RHSExprSwitch((AbstractStmtSwitch)o, connector);
	}

}// RHSExprSwitch
