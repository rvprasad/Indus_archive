package edu.ksu.cis.bandera.bfa.analysis.ofa.fs;


import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;

import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.DefinitionStmt;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.ValueBox;
import ca.mcgill.sable.util.Iterator;

import org.apache.log4j.Category;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// ExprSwitch.java
/**
 * <p>The expression visitor used in flow sensitive mode of object flow analysis. </p>
 *
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class ExprSwitch extends edu.ksu.cis.bandera.bfa.analysis.ofa.fi.ExprSwitch {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(ExprSwitch.class);

	/**
	 * <p>Creates a new <code>ExprSwitch</code> instance.</p>
	 *
	 * @param stmt the statement visitor which uses this instance of expression visitor.
	 * @param connector the connector to be used to connect the ast and non-ast nodes.
	 */
	public ExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		super(stmt, connector);
	}

	/**
	 * <p>Handles the array reference expressions.  This calls <code>postProcessBase</code> to finish up processing.</p>
	 *
	 * @param e the array ref expression to be processed.
	 */
	public void caseArrayRef(ArrayRef e) {
		super.caseArrayRef(e);
		postProcessBase(e.getBaseBox());
	}

	/**
	 * <p>Handles the instance field reference expressions.  This calls <code>postProcessBase</code> to finish up
	 * processing.</p>
	 *
	 * @param e the instance field ref expression to be processed.
	 */
	public void caseInstanceFieldRef(InstanceFieldRef e) {
		super.caseInstanceFieldRef(e);
		postProcessBase(e.getBaseBox());
	}

	/**
	 * <p>Connects the flow graph nodes corresponding to definition of the primary to the use of the primary at the reference
	 * site.  This method assumes that the primary in a access expression is a local variable.  The idea is that once the
	 * nodes have been set up for the primary and the identifier, the nodes corresponding to the primary is connected
	 * according to the mode of operation to instigate flow of values into fields and array components according to the
	 * mode.</p>
	 *
	 * @param e the reference program point to be processed.
	 */
	public void postProcessBase(ValueBox e) {
		Local l = (Local)e.getValue();
		ValueBox backup = context.setProgramPoint(e);
		FGNode localNode = method.getASTNode(l);
		for (Iterator i = method.getDefsOfAt(l, stmt.getStmt()).iterator(); i.hasNext();) {
			DefinitionStmt defStmt = (DefinitionStmt)i.next();
			context.setProgramPoint(defStmt.getLeftOpBox());
			FGNode defNode = method.getASTNode(defStmt.getLeftOp());
			logger.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
			defNode.addSucc(localNode);
		} // end of for (Iterator i = defs.getDefsOfAt(e, stmt.stmt).iterator(); i.hasNext();)
		context.setProgramPoint(backup);
	}

	/**
	 * <p>Process the expression at the given program point.</p>
	 *
	 * @param vb the program point encapsulating the expression to be processed.
	 */
	public void process(ValueBox vb) {
		ValueBox temp = context.setProgramPoint(vb);
		super.process(vb);
		context.setProgramPoint(temp);
	}

	/**
	 * <p>Returns a new instance of this class.</p>
	 *
	 * @param o the statement visitor which shall use the created visitor instance.  This is of type
	 * <code>AbstractStmtSwitch</code>.
	 * @return the new visitor instance.
	 */
	public Object prototype(Object o) {
		return new ExprSwitch((AbstractStmtSwitch)o, connector);
	}

}// ExprSwitch
