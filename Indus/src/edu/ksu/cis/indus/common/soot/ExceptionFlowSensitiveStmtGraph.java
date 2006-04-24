/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.PatchingChain;
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
 * controlled. The user can specify the names of exceptions and the control flow via the throw of these exceptions will not be
 * included in the graph.
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
	private static final List<String> NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING;

	static {
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING = new ArrayList<String>();
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.add("java.lang.RuntimeException");
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.add("java.lang.Exception");
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.add("java.lang.Throwable");
		NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.add("java.lang.Error");
	}

	/**
	 * The sequence of units represented in this graph.
	 */
	@NonNull @NonNullContainer private List<Stmt> nodes;

	/**
	 * A cache of the nodes for which predecessors need to be fixed after processing.
	 */
	@NonNull @NonNullContainer private Collection<Stmt> predsToBeProcessedCache;

	/**
	 * A cache of the nodes for which successors need to be fixed after processing.
	 */
	@NonNull @NonNullContainer private Collection<Stmt> succsToBeProcessedCache;

	/**
	 * Creates an instance of this unit graph corresponding to the given body and options.
	 * 
	 * @param unitBody is the body for which the graph should be constructed.
	 * @param namesOfExceptionsToIgnore is the fully qualified names of the exceptions. Control flow based on these exceptions
	 *            are not captured in the graph.
	 * @param exceptionEdges <code>true</code> indicates that the edges from the predecessors of excepting statements should
	 *            be included; <code>false</code>, otherwise.
	 */
	@SuppressWarnings("unchecked") ExceptionFlowSensitiveStmtGraph(@NonNull final JimpleBody unitBody,
			@NonNull final Collection<String> namesOfExceptionsToIgnore, final boolean exceptionEdges) {
		super(unitBody, true, exceptionEdges);
		predsToBeProcessedCache = new HashSet<Stmt>();
		succsToBeProcessedCache = new HashSet<Stmt>();
		deleteEdgesResultingFromTheseExceptions(namesOfExceptionsToIgnore);
		pruneExceptionBasedControlFlow();

		for (final Iterator<Stmt> _i = succsToBeProcessedCache.iterator(); _i.hasNext();) {
			final Stmt _unit = _i.next();
			unitToSuccs.put(_unit, Collections.unmodifiableCollection((Collection) unitToSuccs.get(_unit)));
		}

		for (final Iterator<Stmt> _i = predsToBeProcessedCache.iterator(); _i.hasNext();) {
			final Stmt _unit = _i.next();
			unitToPreds.put(_unit, Collections.unmodifiableCollection((Collection) unitToPreds.get(_unit)));
		}
		predsToBeProcessedCache = null;
		succsToBeProcessedCache = null;
		fixupMapsAndIterator();
	}

	/**
	 * Returns an iterator over the statements represented in this graph. The order of the statements will reflect the order
	 * of the statements in the sequence of statements obtained from the method. {@inheritDoc}
	 */
	@Functional @NonNull @Override public Iterator<Stmt> iterator() {
		return nodes.iterator();
	}

	/**
	 * Deletes all edges resulting from exceptions named in <code>namesOfExceptionsToIgnore</code>.
	 * 
	 * @param namesOfExceptionsToIgnore is the fully qualified names of exceptions. Control flow based on these exceptions is
	 *            deleted from the graph.
	 */
	@SuppressWarnings("unchecked") private void deleteEdgesResultingFromTheseExceptions(
			@NonNullContainer @NonNull final Collection<String> namesOfExceptionsToIgnore) {
		final Chain _traps = body.getTraps();
		final Chain _units = body.getUnits();

		for (@SuppressWarnings("unchecked") final Iterator<Trap> _j = _traps.iterator(); _j.hasNext();) {
			final Trap _trap = _j.next();

			if (namesOfExceptionsToIgnore.contains(_trap.getException().getName())) {
				final Stmt _handler = (Stmt) _trap.getHandlerUnit();
				final Stmt _endUnit = (Stmt) _units.getPredOf(_trap.getEndUnit());

				final List<Stmt> _preds = new ArrayList<Stmt>((Collection) unitToPreds.get(_handler));

				for (final Iterator<Stmt> _i = _units.iterator(_trap.getBeginUnit(), _endUnit); _i.hasNext();) {
					final Stmt _unit = _i.next();
					final List<Stmt> _succs = new ArrayList<Stmt>((Collection) unitToSuccs.get(_unit));
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
	@SuppressWarnings("unchecked") private void fixupMapsAndIterator() {
		final List<Object> _temp = new ArrayList<Object>();
		final IWorkBag<Object> _wb = new HistoryAwareFIFOWorkBag<Object>(_temp);
		final PatchingChain _units = getBody().getUnits();

		// find reachable units.
		_wb.addWork(_units.getFirst());

		while (_wb.hasWork()) {
			final Stmt _unit = (Stmt) _wb.getWork();
			_wb.addAllWork(getSuccsOf(_unit));
		}

		nodes = new ArrayList<Stmt>(_units);
		nodes.retainAll(_temp);
		nodes = Collections.unmodifiableList(nodes);

		// fix the maps such that there is no info pertaining to unreachable statements
		final Collection<Stmt> _stmts = new ArrayList<Stmt>(_units);
		_stmts.removeAll(_temp);

		for (final Iterator<?> _i = _stmts.iterator(); _i.hasNext();) {
			final Object _stmt = _i.next();
			unitToPreds.remove(_stmt);
			unitToSuccs.remove(_stmt);
		}
	}

	/**
	 * Removes control flow edges based on the matching of the exceptions resulting from source expression and the exception
	 * being handled.
	 */
	@SuppressWarnings("unchecked") private void pruneExceptionBasedControlFlow() {
		// process each trapped unit
		for (final Iterator<Stmt> _i = TrapManager.getTrappedUnitsOf(body).iterator(); _i.hasNext();) {
			final Stmt _unit = _i.next();
			final List<Trap> _traps = TrapManager.getTrapsAt(_unit, body);

			// gather all the exception types that is assumed to be thrown by the current unit.
			for (final Iterator<Trap> _j = _traps.iterator(); _j.hasNext();) {
				final Trap _trap = _j.next();
				final SootClass _exception = _trap.getException();

				if (NAMES_OF_EXCEPTIONS_TO_IGNORE_WHILE_CFG_PRUNING.contains(_exception.getName())) {
					continue;
				}

				final Stmt _handler = (Stmt) _trap.getHandlerUnit();
				final boolean _hasArrayRef = _unit.containsArrayRef();
				final boolean _hasFieldRef = _unit.containsFieldRef();
				final boolean _hasInstanceFieldRef = _hasFieldRef && _unit.getFieldRef() instanceof InstanceFieldRef;
				final boolean _hasInvokeExpr = _unit.containsInvokeExpr();
				final InvokeExpr _invokeExpr = _hasInvokeExpr ? _unit.getInvokeExpr() : null;
				final boolean _hasInstanceInvokeExpr = _hasInvokeExpr && _invokeExpr instanceof InstanceInvokeExpr;

				// for the declared caught exception type, validate the declaration and tailor the graph as needed.
				boolean _retainflag = (_hasArrayRef || _hasInstanceFieldRef || _hasInstanceInvokeExpr)
						&& Util.isDescendentOf(_exception, "java.lang.NullPointerException");
				_retainflag |= _hasArrayRef && Util.isDescendentOf(_exception, "java.lang.ArrayIndexOutOfBoundsException");

				if (_hasInvokeExpr) {
					for (final Iterator<SootClass> _l = _invokeExpr.getMethod().getExceptions().iterator(); _l.hasNext();) {
						final SootClass _thrown = _l.next();
						_retainflag |= Util.isDescendentOf(_thrown, _exception);
					}
				}

				if (!_retainflag) {
					final List<?> _preds = new ArrayList<Stmt>((Collection) unitToPreds.get(_handler));
					_preds.remove(_unit);
					unitToPreds.put(_handler, _preds);
					predsToBeProcessedCache.add(_handler);

					final List<?> _succs = new ArrayList<Stmt>((Collection) unitToSuccs.get(_unit));
					_succs.remove(_handler);
					unitToSuccs.put(_unit, _succs);
					succsToBeProcessedCache.add(_unit);
				}
			}
		}
	}
}

// End of File
