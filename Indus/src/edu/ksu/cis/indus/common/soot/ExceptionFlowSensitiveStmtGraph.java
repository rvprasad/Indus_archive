
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

import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;

import soot.util.Chain;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExceptionFlowSensitiveStmtGraph
  extends UnitGraph {
	/**
	 * DOCUMENT ME!
	 *
	 * @param unitBody DOCUMENT ME!
	 */
	public ExceptionFlowSensitiveStmtGraph(final Body unitBody, final Collection exceptionNamesToIgnore) {
		super(unitBody, true);

		final Chain _traps = unitBody.getTraps();
		final Collection _preds = new ArrayList();

		for (final Iterator _j = _traps.iterator(); _j.hasNext();) {
			final Trap _trap = (Trap) _j.next();

			if (exceptionNamesToIgnore.contains(_trap.getException().getName())) {
				final Chain _units = unitBody.getUnits();
				final Stmt _handler = (Stmt) _trap.getHandlerUnit();
				_preds.clear();

				for (final Iterator _i = _units.iterator(_trap.getBeginUnit(), _trap.getEndUnit()); _i.hasNext();) {
					final Stmt _stmt = (Stmt) _i.next();
					((Collection) unitToSuccs.get(_stmt)).remove(_handler);
					_preds.add(_stmt);
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
   Revision 1.1  2004/02/16 19:30:55  venku
   - added a new class, ExceptionFlowSensitiveStmtGraph, to
     provide a stmt graph that is specific to jimple but considers
     the effect of exceptions raised at each statement while
     building the graph.  The idea is for the user to control the
     effect of exception flow on the graph structure.
 */
