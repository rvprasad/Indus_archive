
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

package edu.ksu.cis.indus.common.soot;

import java.lang.ref.WeakReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import soot.Body;
import soot.SootMethod;
import soot.VoidType;

import soot.jimple.Jimple;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides the an abstract implementation of <code>IUnitGraphFactory</code> via which unit graphs can be
 * retrieved.  The subclasses should provide suitable  unit graph implementation.  The control flow edges in the  provided
 * unit graphs are pruned by matching the thrown exceptions to the enclosing catch blocks.  Refer to
 * <code>Util.pruneExceptionBasedControlFlow()</code> for more information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractUnitGraphFactory
  implements IUnitGraphFactory {
	/**
	 * This maps methods to unit graphs.
	 *
	 * @invariant method2UnitGraph != null and method2UnitGraph.oclIsKindOf(Map(SootMethod, UnitGraph))
	 */
	private final Map method2UnitGraph = new HashMap();

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
	public final UnitGraph getUnitGraph(final SootMethod method) {
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
			_result = getUnitGraphForMethod(method);

			if (_result == null) {
				// stub in an empty graph.
				final Jimple _jimple = Jimple.v();
				final Body _body = _jimple.newBody();
				_body.setMethod(method);

				final Collection _units = _body.getUnits();

				if (method.getReturnType() instanceof VoidType) {
					_units.add(_jimple.newReturnVoidStmt());
				} else {
					_units.add(_jimple.newReturnStmt(Util.getDefaultValueFor(method.getReturnType())));
				}
				_result = getUnitGraphForBody(_body);
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
	protected abstract UnitGraph getUnitGraphForBody(final Body body);

	/**
	 * Get the unit graph associated with the method.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the unit graph.
	 *
	 * @pre not method.isConcrete() implies result == null
	 */
	protected abstract UnitGraph getUnitGraphForMethod(final SootMethod method);
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2004/03/21 01:47:48  venku
   - documentation.
   Revision 1.7  2004/03/08 02:10:14  venku
   - enabled preliminary support to prune exception based intraprocedural
     control flow edges.
   Revision 1.6  2004/03/04 11:56:48  venku
   - renamed a method.
   - added a valid empty body into native methods.
   Revision 1.5  2004/01/28 22:41:08  venku
   - added a new method to extract default bodies.
   Revision 1.4  2003/12/31 09:30:18  venku
   - removed unused code.
   Revision 1.3  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.2  2003/12/09 04:42:42  venku
   - unit graph factories are responsible to construct empty
     bodies for methods not BasicBlockGraphMgr.  FIXED.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.5  2003/12/08 10:16:26  venku
   - refactored classes such that the subclasses only provide the
     unit graphs whereas the parent class does the bookkeeping.
   Revision 1.4  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.3  2003/09/29 04:20:30  venku
   - coding convention.
   Revision 1.2  2003/09/28 06:54:17  venku
   - one more small change to the interface.
   Revision 1.1  2003/09/28 06:46:49  venku
   - Some more changes to extract unit graphs from the enviroment.
 */
