
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;


/**
 * The expression visitor used in flow sensitive mode of object flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Date$
 */
public class FlowSensitiveExprSwitch
  extends edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.FlowInsensitiveExprSwitch {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FlowSensitiveExprSwitch.class);

	/**
	 * Creates a new <code>FlowSensitiveExprSwitch</code> instance.
	 *
	 * @param stmtSwitchParam the statement visitor which uses this instance of expression visitor.
	 * @param nodeConnector the connector to be used to connect the ast and non-ast nodes.
	 *
	 * @pre stmtSwitchParam != null and nodeConnector != null
	 */
	public FlowSensitiveExprSwitch(final AbstractStmtSwitch stmtSwitchParam, final IFGNodeConnector nodeConnector) {
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
	 * @post result != null and result.oclIsKindOf(FlowSensitiveExprSwitch)
	 */
	public Object getClone(final Object o) {
		return new FlowSensitiveExprSwitch((AbstractStmtSwitch) o, connector);
	}

	/**
	 * Processes the local expression.  This implementation connects the nodes at the def sites to the nodes at the use site
	 * of the local.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseLocal(final Local e) {
		final IFGNode _localNode = method.getASTNode(e);
		final Stmt _stmt = context.getStmt();
		final ValueBox _backup = context.setProgramPoint(null);

		if (_stmt.getUseBoxes().contains(_backup)) {
			final List _defs = method.getDefsOfAt(e, _stmt);

			for (final Iterator _i = _defs.iterator(); _i.hasNext();) {
				final DefinitionStmt _defStmt = (DefinitionStmt) _i.next();
				context.setProgramPoint(_defStmt.getLeftOpBox());

				final IFGNode _defNode = method.getASTNode(_defStmt.getLeftOp());

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Local Def:" + _defStmt.getLeftOp() + "\n" + _defNode + context);
				}
				_defNode.addSucc(_localNode);
			}
		}

		context.setProgramPoint(_backup);
		setResult(_localNode);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/12/05 15:31:41  venku
   - deleted method as it contained redundant logic.
   Revision 1.8  2003/12/05 02:27:20  venku
   - unnecessary methods and fields were removed. Like
       getCurrentProgramPoint()
       getCurrentStmt()
   - context holds current information and only it must be used
     to retrieve this information.  No auxiliary arguments. FIXED.
   Revision 1.7  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
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
