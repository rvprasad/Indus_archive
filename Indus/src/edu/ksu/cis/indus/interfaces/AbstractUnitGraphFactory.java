
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

package edu.ksu.cis.indus.interfaces;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;


/**
 * This class provides the interface via which unit graphs can be retrieved.  The subclasses should provide suitable  unit
 * graph implementation.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractUnitGraphFactory {
	/**
	 * This maps methods to unit graphs.
	 *
	 * @invariant method2UnitGraph != null and method2UnitGraph.oclIsKindOf(Map(SootMethod, UnitGraph))
	 */
	protected final Map method2UnitGraph = new HashMap();

	/**
	 * Provides the set of methods for which this object has provided graphs.
	 *
	 * @return a collection of methods
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
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
	 */
	public abstract UnitGraph getUnitGraph(final SootMethod method);

	/**
	 * Resets all internal datastructures.
	 */
	public final void reset() {
		method2UnitGraph.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/29 04:20:30  venku
   - coding convention.
   Revision 1.2  2003/09/28 06:54:17  venku
   - one more small change to the interface.
   Revision 1.1  2003/09/28 06:46:49  venku
   - Some more changes to extract unit graphs from the enviroment.
 */
