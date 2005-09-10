
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2002, 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import java.lang.ref.WeakReference;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

import soot.toolkits.graph.CompleteUnitGraph;

import soot.toolkits.scalar.SimpleLocalDefs;


/**
 * The expression visitor used in flow sensitive mode of object flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Date$
 */
class FlowSensitiveExprSwitch
  extends edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.FlowInsensitiveExprSwitch {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FlowSensitiveExprSwitch.class);

	/** 
	 * This is a weak reference to the local def information and it provides the def sites for local variables in the
	 * associated method.  This is used in conjunction with flow-sensitive information calculation.
	 */
	protected WeakReference defs = new WeakReference(null);

	/** 
	 * This indicates if the method variant is unretrievale due to various reasons such as non-concrete body.
	 */
	protected boolean unRetrievable;

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
	 * Returns the definitions of local variable <code>l</code> that arrive at statement <code>s</code>.
	 *
	 * @param l the local for which the definitions are requested.
	 * @param s the statement at which the definitions are requested.
	 *
	 * @return the list of definitions of <code>l</code> that arrive at statement <code>s</code>.
	 *
	 * @pre l != null and s != null
	 */
	public List getDefsOfAt(final Local l, final Stmt s) {
		List _result = Collections.EMPTY_LIST;

		if (!unRetrievable) {
			final SimpleLocalDefs _temp = (SimpleLocalDefs) defs.get();

			if (_temp != null) {
				_result = _temp.getDefsOfAt(l, s);
			} else {
				final SootMethod _method = method.getMethod();

				if (_method.hasActiveBody()) {
					final SimpleLocalDefs _temp2 = new SimpleLocalDefs(new CompleteUnitGraph(_method.retrieveActiveBody()));
					defs = new WeakReference(_temp2);
					_result = _temp2.getDefsOfAt(l, s);
				} else {
					unRetrievable = true;
				}
			}
		}
		return _result;
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
			final List _defs = getDefsOfAt(e, _stmt);

			for (final Iterator _i = _defs.iterator(); _i.hasNext();) {
				final DefinitionStmt _defStmt = (DefinitionStmt) _i.next();
				context.setProgramPoint(_defStmt.getLeftOpBox());

				final IFGNode _defNode = method.getASTNode(_defStmt.getLeftOp());

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Local Def:" + _defStmt.getLeftOp() + "\n" + _defNode + context);
				}
				_defNode.addSucc(_localNode);
                MethodVariant.setOutFilterOfBasedOn(_defNode, e.getType(), tokenMgr);
			}
		}

		context.setProgramPoint(_backup);
		setResult(_localNode);
	}
}

// End of File
