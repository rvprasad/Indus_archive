
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.Constants;

import java.lang.ref.WeakReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.VoidType;

import soot.jimple.Jimple;
import soot.jimple.JimpleBody;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides the an abstract implementation of <code>IStmtGraphFactory</code> via which unit graphs can be
 * retrieved.  The subclasses should provide suitable  unit graph implementation.  The control flow edges in the  provided
 * unit graphs are pruned by matching the thrown exceptions to the enclosing catch blocks.  Refer to
 * <code>Util.pruneExceptionBasedControlFlow()</code> for more information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractStmtGraphFactory
  implements IStmtGraphFactory {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractStmtGraphFactory.class);

	/** 
	 * This maps methods to unit graphs.
	 *
	 * @invariant method2UnitGraph != null and method2UnitGraph.oclIsKindOf(Map(SootMethod, UnitGraph))
	 */
	private final Map method2UnitGraph = new HashMap(Constants.getNumOfMethodsInApplication());

	/**
	 * Retrieves the unit graph of the given method.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the requested unit graph.
	 *
	 * @post result != null and result.oclIsKindOf(UnitGraph)
	 * @post method.isConcrete() implies result.getBody() = method.getBody()
	 * @post 1method.isConcrete() implies result.getBody() != method.getBody()
	 */
	public final UnitGraph getStmtGraph(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getStmtGraph(method = " + method + ")");
		}

		final WeakReference _ref = (WeakReference) method2UnitGraph.get(method);
		UnitGraph _result = null;
		boolean _flag = false;

		if (_ref == null) {
			_flag = true;
		} else {
			_result = (UnitGraph) _ref.get();

			if (_result == null) {
				_flag = true;
			}
		}

		if (_flag) {
			_result = getStmtGraphForMethod(method);

			if (_result == null) {
				// stub in an empty graph.
				final Jimple _jimple = Jimple.v();
				final JimpleBody _body = _jimple.newBody();
				_body.setMethod(method);

				final Collection _units = _body.getUnits();

				if (method.getReturnType() instanceof VoidType) {
					_units.add(_jimple.newReturnVoidStmt());
				} else {
					_units.add(_jimple.newReturnStmt(Util.getDefaultValueFor(method.getReturnType())));
				}
				_result = getStmtGraphForBody(_body);
			}
			method2UnitGraph.put(method, new WeakReference(_result));
		}
		return _result;
	}

	/**
	 * Resets all internal datastructures.
	 */
	public final void reset() {
		method2UnitGraph.clear();
	}

	/**
	 * Retreives the unit graph (of a particular implementation) for the given body.
	 *
	 * @param body to be represented as a graph.
	 *
	 * @return a unit graph.
	 *
	 * @pre body != null
	 * @post result != null
	 */
	protected abstract UnitGraph getStmtGraphForBody(final JimpleBody body);

	/**
	 * Get the unit graph associated with the method.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the unit graph.
	 *
	 * @pre not method.isConcrete() implies result == null
	 */
	protected abstract UnitGraph getStmtGraphForMethod(final SootMethod method);
}

// End of File
