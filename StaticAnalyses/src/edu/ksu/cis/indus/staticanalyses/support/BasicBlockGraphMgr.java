
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

package edu.ksu.cis.indus.staticanalyses.support;

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;

import edu.ksu.cis.indus.interfaces.AbstractUnitGraphFactory;

import java.lang.ref.WeakReference;

import java.util.HashMap;
import java.util.Map;


/**
 * This class manages a set of basic block graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class BasicBlockGraphMgr {
	/**
	 * This maps methods to basic block graphs.
	 *
	 * @invariant method2graph.oclIsKindOf(Map(SootMethod, BasicBlockGraph))
	 */
	private final Map method2graph = new HashMap();

	/**
	 * This provides <code>UnitGraph</code>s required to construct the basic block graphs.
	 */
	private AbstractUnitGraphFactory unitGraphProvider;

	/**
	 * Provides the basic block graph corresponding to the given control flow graph.  It creates one if none exists.
	 *
	 * @param stmtGraph is the control flow graph of interest.
	 *
	 * @return the basic block graph corresonding to <code>stmtGraph</code>.
	 *
	 * @post result != null
	 */
	public BasicBlockGraph getBasicBlockGraph(final UnitGraph stmtGraph) {
		SootMethod method = stmtGraph.getBody().getMethod();
		WeakReference ref = (WeakReference) method2graph.get(method);

		if (ref == null || ref.get() == null) {
			ref = new WeakReference(new BasicBlockGraph(stmtGraph));
			method2graph.put(method, ref);
		}
		return (BasicBlockGraph) ref.get();
	}

	/**
	 * Retrieves the basic block graph corresponding to the given method.
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
		return getBasicBlockGraph(unitGraphProvider.getUnitGraph(sm));
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
	public void setUnitGraphProvider(final AbstractUnitGraphFactory cfgProvider) {
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
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.5  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
