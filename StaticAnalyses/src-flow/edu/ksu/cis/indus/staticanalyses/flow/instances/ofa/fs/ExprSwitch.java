
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

import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.InstanceFieldRef;


/**
 * The expression visitor used in flow sensitive mode of object flow analysis.  Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ExprSwitch
  extends edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi.ExprSwitch {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ExprSwitch.class);

	/**
	 * Creates a new <code>ExprSwitch</code> instance.
	 *
	 * @param stmtSwitchParam the statement visitor which uses this instance of expression visitor.
	 * @param nodeConnector the connector to be used to connect the ast and non-ast nodes.
	 *
	 * @pre stmtSwitchParam != null and nodeConnector != null
	 */
	public ExprSwitch(final AbstractStmtSwitch stmtSwitchParam, final IFGNodeConnector nodeConnector) {
		super(stmtSwitchParam, nodeConnector);
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the statement visitor which shall use the created visitor instance.
	 *
	 * @return the new visitor instance.
	 *
	 * @pre o != null and o.oclIsKindOf(AbstractStmtSwitch)
	 * @post result != null and result.oclIsKindOf(AbstractExprSwitch)
	 */
	public Object getClone(final Object o) {
		return new ExprSwitch((AbstractStmtSwitch) o, connector);
	}

	/**
	 * Handles the array reference expressions.
	 *
	 * @param e the array ref expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseArrayRef(final ArrayRef e) {
		super.caseArrayRef(e);
		postProcessBase(e.getBaseBox());
	}

	/**
	 * Handles the instance field reference expressions.
	 *
	 * @param e the instance field ref expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseInstanceFieldRef(final InstanceFieldRef e) {
		super.caseInstanceFieldRef(e);
		postProcessBase(e.getBaseBox());
	}

	/**
	 * Process the expression at the given program point.
	 *
	 * @param vb the program point encapsulating the expression to be processed.
	 *
	 * @pre vb != null
	 */
	public void process(final ValueBox vb) {
		ValueBox temp = context.setProgramPoint(vb);
		super.process(vb);
		context.setProgramPoint(temp);
	}

	/**
	 * Connects the flow graph nodes corresponding to definition of the primary to the use of the primary at the reference
	 * site.  This method assumes that the primary in a access expression is a local variable.  The idea is that once the
	 * nodes have been set up for the primary and the identifier, the nodes corresponding to the primary is connected
	 * according to the mode of operation to instigate flow of values into fields and array components according to the
	 * mode.
	 *
	 * @param e the reference program point to be processed.
	 *
	 * @pre e != null
	 */
	protected void postProcessBase(final ValueBox e) {
		Local l = (Local) e.getValue();
		ValueBox backup = context.setProgramPoint(e);
		IFGNode localNode = method.getASTNode(l);

		for (Iterator i = method.getDefsOfAt(l, stmtSwitch.getCurrentStmt()).iterator(); i.hasNext();) {
			DefinitionStmt defStmt = (DefinitionStmt) i.next();
			context.setProgramPoint(defStmt.getLeftOpBox());

			IFGNode defNode = method.getASTNode(defStmt.getLeftOp());

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Local Def:" + defStmt.getLeftOp() + "\n" + defNode + context);
			}
			defNode.addSucc(localNode);
		}

		// end of for (Iterator i = defs.getDefsOfAt(e, stmt.stmt).iterator(); i.hasNext();)
		context.setProgramPoint(backup);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.4  2003/08/15 02:54:06  venku
   Spruced up specification and documentation for flow-insensitive classes.
   Changed names in AbstractExprSwitch.
   Ripple effect of above change.
   Formatting changes to IPrototype.
   Revision 1.3  2003/08/13 08:58:25  venku
   Formatted with Jalopy.
   Revision 1.2  2003/08/13 08:58:04  venku
   Spruced up documentation and specification.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.6  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
