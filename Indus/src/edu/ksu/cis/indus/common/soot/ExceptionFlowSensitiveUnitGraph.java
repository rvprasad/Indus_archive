
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import soot.Body;
import soot.Trap;
import soot.Unit;

import soot.toolkits.graph.UnitGraph;

import soot.util.Chain;


/**
 * This is a specialized version of <code>UnitGraph</code> in which the control flow edges can be based on exceptions can be
 * controlled.  The user can specify the names of exceptions and the control flow via the throw of these exceptions will not
 * be included  in the graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class ExceptionFlowSensitiveUnitGraph
  extends UnitGraph {
	/**
	 * Creates an instance of this class.
	 *
	 * @param unitBody is the body for which the graph should be constructed.
	 * @param exceptionNamesToIgnore is the fully qualified names of the exceptions.  Control flow based on these exceptions
	 * 		  are not captured in the graph.
	 *
	 * @pre unitBody != null and exceptionNamesToIgnore != null
	 * @pre exceptionNamesToIgnore.oclIsKindOf(Collection(String))
	 */
	ExceptionFlowSensitiveUnitGraph(final Body unitBody, final Collection exceptionNamesToIgnore) {
		super(unitBody, true);

		final Chain _traps = unitBody.getTraps();
		final Collection _preds = new ArrayList();

		for (final Iterator _j = _traps.iterator(); _j.hasNext();) {
			final Trap _trap = (Trap) _j.next();

			if (exceptionNamesToIgnore.contains(_trap.getException().getName())) {
				final Chain _units = unitBody.getUnits();
				final Unit _handler = _trap.getHandlerUnit();
				_preds.clear();

				for (final Iterator _i = _units.iterator(_trap.getBeginUnit(), _trap.getEndUnit()); _i.hasNext();) {
					final Unit _unit = (Unit) _i.next();
					((Collection) unitToSuccs.get(_unit)).remove(_handler);
					_preds.add(_unit);
				}
				((Collection) unitToPreds.get(_handler)).removeAll(_preds);
				break;
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/02/17 05:45:34  venku
   - added the logic to create stmt graphs whose structure can be
     tuned to consider the flow of control due to certain exceptions.
   Revision 1.1  2004/02/16 19:30:55  venku
   - added a new class, ExceptionFlowSensitiveUnitGraph, to
     provide a stmt graph that is specific to jimple but considers
     the effect of exceptions raised at each statement while
     building the graph.  The idea is for the user to control the
     effect of exception flow on the graph structure.
 */
