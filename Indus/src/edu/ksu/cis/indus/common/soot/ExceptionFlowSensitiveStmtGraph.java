
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
import java.util.List;

import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.Trap;
import soot.TrapManager;
import soot.Unit;

import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;

import soot.util.Chain;


/**
 * This is a specialized version of <code>UnitGraph</code> in which the control flow edges based on exceptions can be
 * controlled.  The user can specify the names of exceptions and the control flow via the throw of these exceptions will not
 * be included in the graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class ExceptionFlowSensitiveStmtGraph
  extends UnitGraph {
	/**
	 * Creates an instance of this unit graph corresponding to the given body and options.
	 *
	 * @param unitBody is the body for which the graph should be constructed.
	 * @param namesOfExceptionsToIgnore is the fully qualified names of the exceptions.  Control flow based on these
	 * 		  exceptions are not captured in the graph.
	 * @param flag <code>true</code> indicates that the edges from the predecessors of excepting statements should be
	 * 		  included; <code>false</code>, otherwise.
	 *
	 * @pre unitBody != null and namesOfExceptionsToIgnore != null
	 * @pre namesOfExceptionsToIgnore.oclIsKindOf(Collection(String))
	 */
	ExceptionFlowSensitiveStmtGraph(final JimpleBody unitBody, final Collection namesOfExceptionsToIgnore, boolean flag) {
		super(unitBody, true, flag);
		pruneExceptionalEdges(namesOfExceptionsToIgnore);
		pruneExceptionBasedControlFlow();
	}

	/**
	 * Removes exception based control flow based on <code>throws</code> clause and exception inference based on the
	 * expressions in the statements.
	 *
	 * @pre body != null
	 */
	private void pruneExceptionBasedControlFlow() {
		// process each trapped unit
		for (final Iterator _i = TrapManager.getTrappedUnitsOf(body).iterator(); _i.hasNext();) {
			final Stmt _unit = (Stmt) _i.next();
			final List _traps = TrapManager.getTrapsAt(_unit, body);

			// gather all the exception types that is assumed to be thrown by the current unit.
			for (final Iterator _j = _traps.iterator(); _j.hasNext();) {
				final Trap _trap = (Trap) _j.next();
				final Unit _handler = _trap.getHandlerUnit();
				final List _temp = TrapManager.getExceptionTypesOf(_handler, body);

				boolean _retainflag = false;
				final boolean _hasArrayRef = _unit.containsArrayRef();
				final boolean _hasFieldRef = _unit.containsFieldRef();
				final boolean _hasInstanceFieldRef = _hasFieldRef && _unit.getFieldRef() instanceof InstanceFieldRef;
				final boolean _hasInvokeExpr = _unit.containsInvokeExpr();
				final InvokeExpr _invokeExpr = _hasInvokeExpr ? _unit.getInvokeExpr()
															  : null;
				final boolean _hasInstanceInvokeExpr = _hasInvokeExpr && _invokeExpr instanceof InstanceInvokeExpr;

				// for each declared caught exception type validate the declaration and tailor the graph as needed.
				for (final Iterator _k = _temp.iterator(); _k.hasNext() && !_retainflag;) {
					final SootClass _exception = ((RefType) _k.next()).getSootClass();
					_retainflag =
						(_hasArrayRef || _hasInstanceFieldRef || _hasInstanceInvokeExpr)
						  && Util.isDescendentOf(_exception, "java.lang.NullPointerException");
					_retainflag |= _hasArrayRef
					  && Util.isDescendentOf(_exception, "java.lang.ArrayIndexOutOfBoundsException");

					if (_hasInvokeExpr) {
						for (final Iterator _l = _invokeExpr.getMethod().getExceptions().iterator(); _l.hasNext();) {
							final SootClass _thrown = (SootClass) _l.next();
							_retainflag |= Util.isDescendentOf(_thrown, _exception);
						}
					}
				}

				if (!_retainflag) {
					final List _preds = new ArrayList((List) unitToPreds.get(_handler));
					_preds.remove(_unit);
					unitToPreds.put(_handler, _preds);

					final List _succs = new ArrayList((List) unitToSuccs.get(_unit));
					_succs.remove(_handler);
					unitToPreds.put(_unit, _succs);
				}
			}
		}
	}

	/**
	 * Removes all edges pertaining to exceptions named in <code>namesOfExceptionsToIgnore</code>
	 *
	 * @param namesOfExceptionsToIgnore is the fully qualified names of exceptions.  Control flow based on these exceptions
	 * 		  is deleted from the graph.
	 */
	private void pruneExceptionalEdges(final Collection namesOfExceptionsToIgnore) {
		final Body unitBody = getBody();
		final Chain _traps = unitBody.getTraps();
		final Collection _preds = new ArrayList();

		for (final Iterator _j = _traps.iterator(); _j.hasNext();) {
			final Trap _trap = (Trap) _j.next();

			if (namesOfExceptionsToIgnore.contains(_trap.getException().getName())) {
				final Chain _units = unitBody.getUnits();
				final Unit _handler = _trap.getHandlerUnit();
				final Unit _endUnit = (Unit) _units.getPredOf(_trap.getEndUnit());
				_preds.clear();

				for (final Iterator _i = _units.iterator(_trap.getBeginUnit(), _endUnit); _i.hasNext();) {
					final Unit _unit = (Unit) _i.next();
					final List _list = new ArrayList((List) unitToSuccs.get(_unit));
					_list.remove(_handler);
					unitToSuccs.put(_unit, _list);
					_preds.add(_unit);
				}

				final List _list = new ArrayList((List) unitToPreds.get(_handler));
				_list.removeAll(_preds);
				unitToPreds.put(_handler, _list);
				_j.remove();
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/03/17 11:47:27  venku
   - changes to confirm to the new graph class hierarchy in Soot.
   Revision 1.4  2004/02/23 09:07:43  venku
   - the redundant handler was not deleted from the body. FIXED.
   Revision 1.3  2004/02/23 08:27:21  venku
   - the graphs were created as complete unit graphs. FIXED.
   Revision 1.2  2004/02/23 06:37:16  venku
   - succs/preds in the parent  class were unmodifiable.  FIXED.
   Revision 1.1  2004/02/17 05:59:15  venku
   - renamed ExceptionFlowSensitiveStmtGraphXXXX to
     ExceptionFlowSensitiveUnitGraph.
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
