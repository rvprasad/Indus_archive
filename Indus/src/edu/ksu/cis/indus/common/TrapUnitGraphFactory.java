
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

package edu.ksu.cis.indus.common;

import soot.SootMethod;

import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;

import edu.ksu.cis.indus.interfaces.AbstractUnitGraphFactory;

import java.lang.ref.WeakReference;


/**
 * This class provides <code>soot.toolkits.graph.TrapUnitGraph</code>s.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class TrapUnitGraphFactory
  extends AbstractUnitGraphFactory {
	/**
	 * {@inheritDoc}
	 *
	 * @post method.isConcret() implies result != null and result.oclIsKindOf(TrapUnitGraph)
	 *
	 * @see edu.ksu.cis.indus.interfaces.AbstractUnitGraphFactory#getStmtGraph(soot.SootMethod)
	 */
	public UnitGraph getUnitGraph(SootMethod method) {
		WeakReference ref = (WeakReference) method2UnitGraph.get(method);
		UnitGraph result = null;

		if (ref == null || ref.get() == null) {
            if (method.isConcrete()) {
                result = new TrapUnitGraph(method.retrieveActiveBody());
                method2UnitGraph.put(method, new WeakReference(result));
            }
		} else if (ref != null) {
			result = (TrapUnitGraph) ref.get();
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$ */
