
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import soot.Body;
import soot.SootMethod;

import soot.jimple.Jimple;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides the an abstract implementation of <code>IUnitGraphFactory</code> via which unit graphs can be
 * retrieved.  The subclasses should provide suitable  unit graph implementation.
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
	 * {@inheritDoc}
	 */
	public final Collection getManagedMethods() {
		return Collections.unmodifiableCollection(method2UnitGraph.keySet());
	}

	/**
	 * Retrieves the unit graph of the given method.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the requested unit graph.
	 *
	 * @post result != null
	 * @post method.isConcrete() implies result != null and result.oclIsKindOf(CompleteUnitGraph)
	 */
	public final UnitGraph getUnitGraph(final SootMethod method) {
		final WeakReference _ref = (WeakReference) method2UnitGraph.get(method);
		UnitGraph result = null;
		boolean flag = false;

		if (_ref == null) {
			flag = true;
		} else {
			result = (UnitGraph) _ref.get();

			if (result == null) {
				flag = true;
			}
		}

		if (flag) {
			result = getUnitGraphForMethod(method);

			if (result == null) {
				final Body _body = Jimple.v().newBody();
				_body.setMethod(method);
				result = new UnitGraph(_body, false);
			}

			method2UnitGraph.put(method, new WeakReference(result));
		}
		return result;
	}

	/**
	 * Resets all internal datastructures.
	 */
	public final void reset() {
		method2UnitGraph.clear();
	}

	/**
	 * Get the unit graph associated with the method.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the unit graph.
	 *
	 * @pre not method.isConcrete() implies result == null
	 */
	protected abstract UnitGraph getUnitGraphForMethod(SootMethod method);
}

/*
   ChangeLog:
   $Log$
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
