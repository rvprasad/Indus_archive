
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fs;

import soot.Local;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.List;


/**
 * This is the RHS expression visitor.  It provides implementation to handle locals on RHS in flow sensitive mode.
 * 
 * <p>
 * Created: Sun Jan 27 14:29:14 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class RHSExprSwitch
  extends ExprSwitch {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(RHSExprSwitch.class);

	/**
	 * Creates a new <code>RHSExprSwitch</code> instance.
	 *
	 * @param stmtSwitchParam the statement visitor which uses this instance of expression visitor.
	 * @param nodeConnector the connector to be used to connect the ast and non-ast nodes.
	 *
	 * @pre stmtSwitch != null and nodeConnector != null
	 */
	public RHSExprSwitch(final AbstractStmtSwitch stmtSwitchParam, final IFGNodeConnector nodeConnector) {
		super(stmtSwitchParam, nodeConnector);
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the statement visitor which shall use the created visitor instance.  This is of type
	 * 		  <code>AbstractStmtSwitch</code>.
	 *
	 * @return the new visitor instance.
	 *
	 * @pre o != null and o.oclIsKindOf(AbstractStmtSwitch)
	 * @post result != null and result.oclIsKindOf(AbstractExprSwitch)
	 */
	public Object getClone(final Object o) {
		return new RHSExprSwitch((AbstractStmtSwitch) o, connector);
	}

	/**
	 * Handles the expression containing local variables on rhs.  It connects the flow graph nodes corresponding to def sites
	 * of the locals to the nodes corresponding to the  use sites of the locals.
	 *
	 * @param e the local to be visited.
	 *
	 * @pre e != null
	 */
	public void caseLocal(final Local e) {
		IFGNode ast = method.getASTNode(e);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Local:" + e + "\n" + ast);
		}

		List l = method.getDefsOfAt(e, stmtSwitch.getCurrentStmt());

		if (l != null) {
			ValueBox temp = context.getProgramPoint();

			for (Iterator i = l.iterator(); i.hasNext();) {
				DefinitionStmt defStmt = (DefinitionStmt) i.next();
				context.setProgramPoint(defStmt.getLeftOpBox());

				IFGNode defNode = method.getASTNode(defStmt.getLeftOp());

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
				}
				defNode.addSucc(ast);
			}
			context.setProgramPoint(temp);
		}
		setResult(ast);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.3  2003/08/15 02:54:06  venku
   Spruced up specification and documentation for flow-insensitive classes.
   Changed names in AbstractExprSwitch.
   Ripple effect of above change.
   Formatting changes to IPrototype.
   Revision 1.2  2003/08/13 08:58:04  venku
   Spruced up documentation and specification.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.5  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
