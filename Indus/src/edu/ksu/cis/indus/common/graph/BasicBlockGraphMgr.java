
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

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.common.soot.IUnitGraphFactory;

import java.lang.ref.WeakReference;

import java.util.HashMap;
import java.util.Map;

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;


/**
 * This class manages a set of basic block graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class BasicBlockGraphMgr {
	/**
	 * This maps methods to basic block graphs.
	 *
	 * @invariant method2graph.oclIsKindOf(Map(SootMethod, BasicBlockGraph))
	 */
	private final Map method2graph = new HashMap();

	/**
	 * This provides <code>UnitGraph</code>s required to construct the basic block graphs.
	 */
	private IUnitGraphFactory unitGraphProvider;

	/**
	 * Retrieves the basic block graph corresponding to the given method.  Returns an empty basic block graph if the method
	 * is abstract or has no available implementation.
	 *
	 * @param sm is the method for which the graph is requested.
	 *
	 * @return the basic block graph corresponding to <code>sm</code>.
	 *
	 * @throws IllegalStateException when this method is called without calling <code>setStmtGraphProvider()</code>.
	 */
	public BasicBlockGraph getBasicBlockGraph(final SootMethod sm) {
		if (unitGraphProvider == null) {
			throw new IllegalStateException("You need to set the unit graph provider via setStmtGraphProvider() before "
				+ "calling this method.");
		}

		final WeakReference _ref = (WeakReference) method2graph.get(sm);
		BasicBlockGraph result = null;
		boolean flag = false;

		if (_ref == null) {
			flag = true;
		} else {
			result = (BasicBlockGraph) _ref.get();

			if (result == null) {
				flag = true;
			}
		}

		if (flag) {
			final UnitGraph _graph = unitGraphProvider.getUnitGraph(sm);
			result = new BasicBlockGraph(_graph);
			method2graph.put(sm, new WeakReference(result));
		}
		return result;
	}

	/**
	 * Provides the unit graph for the given method.  This is retrieved from the unit graph provider set via
	 * <code>setUnitGraphProvider</code>.
	 *
	 * @param method for which the unit graph is requested.
	 *
	 * @return the unit graph for the method.
	 *
	 * @pre method != null
	 * @post result != null
	 */
	public UnitGraph getUnitGraph(final SootMethod method) {
		return unitGraphProvider.getUnitGraph(method);
	}

	/**
	 * Sets the unit graph provider.
	 *
	 * @param cfgProvider provides <code>UnitGraph</code>s required to construct the basic block graphs.
	 */
	public void setUnitGraphProvider(final IUnitGraphFactory cfgProvider) {
		unitGraphProvider = cfgProvider;
	}

	/**
	 * Resets the internal data structures.
	 */
	public void reset() {
		method2graph.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/12/09 04:32:09  venku
   - empty method body needs a method to be associated with it.  FIXED.

   Revision 1.2  2003/12/09 04:23:49  venku
   - used IUnitGraphFactory instead of AbstractGraphUnitFactory.

   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.

   Revision 1.2  2003/12/09 04:02:43  venku
   - empty body is used for methods with no body.

   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.12  2003/12/08 10:26:30  venku
   - ensured bbg's are returned when the method has concrete implementation.
   - removed a method whose logic is now dependent on the factory.
   - ripple effect.
   Revision 1.11  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.10  2003/09/28 07:31:28  venku
   - ensured that null graph is returned if the method does not
     have a body.
   Revision 1.9  2003/09/28 06:54:17  venku
   - one more small change to the interface.
   Revision 1.8  2003/09/28 06:46:49  venku
   - Some more changes to extract unit graphs from the enviroment.
   Revision 1.7  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.6  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/09/15 02:21:24  venku
   - added reset method.
   Revision 1.4  2003/09/10 10:51:07  venku
   - documentation.
   - removed unnecessary typecast.
   Revision 1.3  2003/08/11 06:40:54  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
   Revision 1.5  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
