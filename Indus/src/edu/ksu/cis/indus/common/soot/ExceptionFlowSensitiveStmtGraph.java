
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

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.SootClass;
import soot.Trap;
import soot.TrapManager;

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
	 * The CFG edges based on the exceptions named here should not be considered during CFG pruning.
	 */
	private static final List NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING;

	static {
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING = new ArrayList();
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.add("java.lang.RuntimeException");
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.add("java.lang.Exception");
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.add("java.lang.Throwable");
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.add("java.lang.Error");
	}

	/**
	 * A cache of the nodes for which predecessors need to be fixed after processing.
	 */
	private Collection predsToBeProcessedCache;

	/**
	 * A cache of the nodes for which successors need to be fixed after processing.
	 *
	 * @invariant succsToBeProcessedCache.oclIsKindOf(Collection(Stmt))
	 */
	private Collection succsToBeProcessedCache;

	/**
	 * The sequence of units represented in this graph.
	 */
	private List nodes;

	/**
	 * Creates an instance of this unit graph corresponding to the given body and options.
	 *
	 * @param unitBody is the body for which the graph should be constructed.
	 * @param namesOfExceptionsToIgnore is the fully qualified names of the exceptions.  Control flow based on these
	 * 		  exceptions are not captured in the graph.
	 * @param exceptionEdges <code>true</code> indicates that the edges from the predecessors of excepting statements should
	 * 		  be included; <code>false</code>, otherwise.
	 *
	 * @pre unitBody != null and namesOfExceptionsToIgnore != null
	 * @pre namesOfExceptionsToIgnore.oclIsKindOf(Collection(String))
	 */
	ExceptionFlowSensitiveStmtGraph(final JimpleBody unitBody, final Collection namesOfExceptionsToIgnore,
		final boolean exceptionEdges) {
		super(unitBody, true, exceptionEdges);
		predsToBeProcessedCache = new HashSet();
		succsToBeProcessedCache = new HashSet();
		deleteEdgesResultingFromTheseExceptions(namesOfExceptionsToIgnore);
		pruneExceptionBasedControlFlow();

		for (final Iterator _i = succsToBeProcessedCache.iterator(); _i.hasNext();) {
			final Stmt _unit = (Stmt) _i.next();
			unitToSuccs.put(_unit, Collections.unmodifiableList((List) unitToSuccs.get(_unit)));
		}

		for (final Iterator _i = predsToBeProcessedCache.iterator(); _i.hasNext();) {
			final Stmt _unit = (Stmt) _i.next();
			unitToPreds.put(_unit, Collections.unmodifiableList((List) unitToPreds.get(_unit)));
		}
		predsToBeProcessedCache = null;
		succsToBeProcessedCache = null;
		fixupMapsAndIterator();
	}

	/**
	 * Returns an iterator over the statements represented in this graph.  The order of the statements will reflect the order
	 * of the statements in the sequence of statements obtained from the method.
	 *
	 * @see soot.toolkits.graph.DirectedGraph#iterator()
	 */
	public Iterator iterator() {
		return nodes.iterator();
	}

	/**
	 * Deletes all edges resulting from exceptions named in <code>namesOfExceptionsToIgnore</code>.
	 *
	 * @param namesOfExceptionsToIgnore is the fully qualified names of exceptions.  Control flow based on these exceptions
	 * 		  is deleted from the graph.
	 */
	private void deleteEdgesResultingFromTheseExceptions(final Collection namesOfExceptionsToIgnore) {
		final Chain _traps = body.getTraps();
		final Chain _units = body.getUnits();

		for (final Iterator _j = _traps.iterator(); _j.hasNext();) {
			final Trap _trap = (Trap) _j.next();

			if (namesOfExceptionsToIgnore.contains(_trap.getException().getName())) {
				final Stmt _handler = (Stmt) _trap.getHandlerUnit();
				final Stmt _endUnit = (Stmt) _units.getPredOf(_trap.getEndUnit());

				final List _preds = new ArrayList((List) unitToPreds.get(_handler));

				for (final Iterator _i = _units.iterator(_trap.getBeginUnit(), _endUnit); _i.hasNext();) {
					final Stmt _unit = (Stmt) _i.next();
					final List _succs = new ArrayList((List) unitToSuccs.get(_unit));
					_succs.remove(_handler);
					unitToSuccs.put(_unit, _succs);
					succsToBeProcessedCache.add(_unit);
					_preds.remove(_unit);
				}
				unitToPreds.put(_handler, _preds);
				predsToBeProcessedCache.add(_handler);
				_j.remove();
			}
		}
	}

	/**
	 * Fixes up the maps for unreachable statements and collects the reachable nodes to be made available for iterators.
	 */
	private void fixupMapsAndIterator() {
		final List _temp = new ArrayList();
		final IWorkBag _wb = new HistoryAwareFIFOWorkBag(_temp);

		for (final Iterator _i = getHeads().iterator(); _i.hasNext();) {
			final Stmt _head = (Stmt) _i.next();

			if (_head == body.getUnits().getFirst()) {
				_wb.addWork(_head);
				break;
			}
		}

		while (_wb.hasWork()) {
			final Stmt _unit = (Stmt) _wb.getWork();
			_wb.addAllWork(getSuccsOf(_unit));
		}

		nodes = new ArrayList(getBody().getUnits());
		nodes.retainAll(_temp);
		nodes = Collections.unmodifiableList(nodes);

		// fix the maps such that there is no info pertaining to unreachable statements 
		final Collection _stmts = new ArrayList(getBody().getUnits());
		_stmts.removeAll(_temp);

		for (final Iterator _i = _stmts.iterator(); _i.hasNext();) {
			final Object _stmt = _i.next();
			unitToPreds.remove(_stmt);
			unitToSuccs.remove(_stmt);
		}
	}

	/**
	 * Removes control flow edges based on the matching of the exceptions resulting from source expression and the exception
	 * being  handled.
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
				final SootClass _exception = _trap.getException();

				if (NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.contains(_exception.getName())) {
					continue;
				}

				final Stmt _handler = (Stmt) _trap.getHandlerUnit();
				final boolean _hasArrayRef = _unit.containsArrayRef();
				final boolean _hasFieldRef = _unit.containsFieldRef();
				final boolean _hasInstanceFieldRef = _hasFieldRef && _unit.getFieldRef() instanceof InstanceFieldRef;
				final boolean _hasInvokeExpr = _unit.containsInvokeExpr();
				final InvokeExpr _invokeExpr = _hasInvokeExpr ? _unit.getInvokeExpr()
															  : null;
				final boolean _hasInstanceInvokeExpr = _hasInvokeExpr && _invokeExpr instanceof InstanceInvokeExpr;

				// for the declared caught exception type validate the declaration and tailor the graph as needed.
				boolean _retainflag =
					(_hasArrayRef || _hasInstanceFieldRef || _hasInstanceInvokeExpr)
					  && Util.isDescendentOf(_exception, "java.lang.NullPointerException");
				_retainflag |= _hasArrayRef && Util.isDescendentOf(_exception, "java.lang.ArrayIndexOutOfBoundsException");

				if (_hasInvokeExpr) {
					for (final Iterator _l = _invokeExpr.getMethod().getExceptions().iterator(); _l.hasNext();) {
						final SootClass _thrown = (SootClass) _l.next();
						_retainflag |= Util.isDescendentOf(_thrown, _exception);
					}
				}

				if (!_retainflag) {
					final List _preds = new ArrayList((List) unitToPreds.get(_handler));
					_preds.remove(_unit);
					unitToPreds.put(_handler, _preds);
					predsToBeProcessedCache.add(_handler);

					final List _succs = new ArrayList((List) unitToSuccs.get(_unit));
					_succs.remove(_handler);
					unitToSuccs.put(_unit, _succs);
					succsToBeProcessedCache.add(_unit);
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.13  2004/06/15 10:18:17  venku
   - pruning control flow edges based on exception was incorrect. FIXED.
   - info pertaining to units represented was incorrect. FIXED.

   Revision 1.12  2004/06/14 04:55:04  venku
   - documentation.
   - coding conventions.
   Revision 1.11  2004/06/13 22:24:43  venku
   -  RuntimeExceptions were not being considered at invoke expr sites while pruning edges.  FIXED.
   Revision 1.10  2004/06/13 07:27:36  venku
   - ensured all collections stored in unitToXXXX mapping are unmodifiable.
   Revision 1.9  2004/06/12 20:42:21  venku
   - renaming of methods.
   Revision 1.8  2004/06/01 06:31:04  venku
   - made ExceptionFlowSensitiveStmtGraph.iterator() return ordered statement list.
   - ProcessingController uses UnitGraph.iterator() to visit the statements of a method.
   Revision 1.7  2004/06/01 01:12:16  venku
   - added a new testcase to test BasicBlockGraph.
   - documentation.
   - added iterator() method to ExceptionFlowSensitiveStmtGraph to
     return only statement captured in the graph.
   Revision 1.6  2004/03/27 08:39:40  venku
   - predecessor mapping was updated instread of successor mapping. FIXED.
   Revision 1.5  2004/03/26 00:22:31  venku
   - renamed getUnitGraph() to getStmtGraph() in IStmtGraphFactory.
   - ripple effect.
   - changed logic in ExceptionFlowSensitiveStmtGraph.
   Revision 1.4  2004/03/26 00:07:26  venku
   - renamed XXXXUnitGraphFactory to XXXXStmtGraphFactory.
   - ripple effect in classes and method names.
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
