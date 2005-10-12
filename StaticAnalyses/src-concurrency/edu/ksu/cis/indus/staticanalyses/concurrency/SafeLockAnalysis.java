
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

package edu.ksu.cis.indus.staticanalyses.concurrency;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.AbstractDirectedGraph;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo.IMonitorGraph;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class is an implementation of Safe lock analysis as described in the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SafeLockAnalysis
  extends AbstractAnalysis {
	/** 
	 * This is the id of safe lock related analysis.
	 */
	public static final Comparable ID = "Safe Lock Analysis";

	/** 
	 * The logger used to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(SafeLockAnalysis.class);

	/** 
	 * The map from wait invoking statements to the immediately enclosing method.
	 *
	 * @invariant waitStmt2method.oclIsKindOf(Map(InvokeStmt, SootMethod))
	 */
	final Map waitStmt2method = new HashMap();

	/** 
	 * The call graph to be used during analysis.
	 */
	ICallGraphInfo callgraphInfo;

	/** 
	 * The monitor analysis to be used during analysis.
	 */
	IMonitorInfo monitorInfo;

	/** 
	 * The pair manager to be used during analysis.
	 */
	PairManager pairMgr;

	/** 
	 * The collection of triples of unsafe monitors.
	 */
	private final Collection unsafeMonitors = new HashSet(MonitorAnalysis.NUM_OF_MONITORS_IN_APPLICATION);

	/** 
	 * The value analyzer to be used.
	 */
	private IValueAnalyzer iva;

	/** 
	 * This maps monitor to related monitors.  A monitor is related to other monitors if they may acquire the same lock.
	 *
	 * @invariant monitor2relatedMonitors.oclIsKindOf(Map(Triple, Collection(Triple)))
	 * @invariant monitor2relatedMonitors.keySet()->forall(o | o.oclIsKindOf(EnterMonitorStmt, ExitMonitorStmt, SootMethod))
	 * 			  or o.oclIsKindOf(null, null, SootMethod))
	 * @invariant monitor2relatedMonitors.values()->forall(p | p->forall(o | o.oclIsKindOf(EnterMonitorStmt, ExitMonitorStmt,
	 * 			  SootMethod) or o.oclIsKindOf(null, null, SootMethod))
	 */
	private Map monitor2relatedMonitors = new HashMap(MonitorAnalysis.NUM_OF_MONITORS_IN_APPLICATION);

	/** 
	 * This maps a monitor to a collection of wait that occur in the monitor.
	 *
	 * @invariant monitor2waits.oclIsKindOf(Map(Triple, Collection(InvokeStmt)))
	 */
	private final Map monitor2waits = new HashMap(MonitorAnalysis.NUM_OF_MONITORS_IN_APPLICATION);

	/**
	 * Creates an instance of the analysis.
	 */
	public SafeLockAnalysis() {
		preprocessor = new WaitInvocationCollectingProcessor();
	}

	/**
	 * This processor collects <code>wait</code> invocations.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class WaitInvocationCollectingProcessor
	  extends AbstractProcessor {
		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
			final SootMethod _currentMethod = context.getCurrentMethod();

			if (Util.isWaitInvocation((InvokeStmt) stmt, _currentMethod, callgraphInfo)) {
				waitStmt2method.put(stmt, _currentMethod);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(InvokeStmt.class, this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(InvokeStmt.class, this);
		}
	}

	/**
	 * Checks if the lock associated with the monitor of the given synchronized method is safe.
	 *
	 * @param method of interest.
	 *
	 * @return <code>true</code> if the lock associated with the monitor of the given synchronized method is safe;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre method != null and method.isSynchronized()
	 */
	public boolean isLockSafe(final SootMethod method) {
		return !unsafeMonitors.contains(new Triple(null, null, method));
	}

	/**
	 * Checks if the lock associated with the monitor occurring in the given statement in the given method is safe.
	 *
	 * @param stmt is a monitor (enter/exit) statement.
	 * @param method containing <code>stmt</code>.
	 *
	 * @return <code>true</code> if the lock associated with the monitor occurring in the given statement in the given method
	 * 		   is safe; <code>false</code>, otherwise.
	 *
	 * @pre method != null and stmt != null
	 * @pre stmt.oclIsKindOf(EnterMonitorStmt) || stmt.oclIsKindOf(ExitMonitorStmt)
	 */
	public boolean isLockSafe(final Stmt stmt, final SootMethod method) {
		return !CollectionUtils.containsAny(unsafeMonitors, monitorInfo.getMonitorTriplesFor(stmt, method));
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (monitorInfo.isStable() && callgraphInfo.isStable()) {
			final IMonitorGraph _monitorGraph = monitorInfo.getMonitorGraph(callgraphInfo);
			processMonitorAndWaitsForLockBasedRelation();
			processMonitorsAndWaitsForEnclosureBaseRelation(_monitorGraph);

			/*
			 * Assume there is an API that can be used to check if waits and notifies are valid.
			 * For each method do the following.  [processMethod]
			 * - Find all the statements immediately enclosed in monitors.
			 * - If any of these statements correspond to a invalid wait, then the enclosing monitor is unsafe. Hence,
			 *   the lock of objects that are used as monitor variable are unsafe.  {monitorIsSafeBasedOnWaitSafety}
			 * - For each such statement set in safe monitors do the following [waitFreeCyclesExistInMonitor]
			 *   - find the basic blocks containing these statements
			 *   - Check if there is a cycle in the basic blocks (excluding those containing the enclosing monitor)
			 *     with no wait().
			 *     - If so, the monitor is unsafe.  Hence, the lock of objects that are used as monitor variable are
			 *       unsafe.
			 * Retrieve monitor containment graph for the system.
			 * Do a fixed point
			 * - propagate the unsafe=ness information in the graph.
			 * - for each monitor newly marked as unsafe, mark related waits with their enclosing monitors and
			 *   related monitors as unsafe.
			 */
			final Collection _monitors = monitorInfo.getMonitorTriples();
			final Collection _seedUnsafeMonitors = new HashSet();
			final Iterator _i = _monitors.iterator();
			final int _iEnd = _monitors.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Triple _monitor = (Triple) _i.next();

				if (!_seedUnsafeMonitors.contains(_monitor)) {
					if (!(safeByCondition2(_monitor) && safeByCondition1(_monitor, _monitorGraph))) {
						_seedUnsafeMonitors.addAll(monitorInfo.getMonitorTriplesOf(_monitor));
					}
				}
			}

			propagateSafetyInformation(_seedUnsafeMonitors, _monitorGraph);
			stable();

			if (LOGGER.isDebugEnabled()) {
				final Collection _safeMonitors = CollectionUtils.subtract(monitorInfo.getMonitorTriples(), unsafeMonitors);
				LOGGER.debug("Unsafe Monitors: \n" + CollectionsUtilities.prettyPrint(unsafeMonitors));
				LOGGER.debug("Safe Monitors: \n" + CollectionsUtilities.prettyPrint(_safeMonitors));

				final Collection _temp = CollectionUtils.subtract(monitorInfo.getMonitorTriples(), _safeMonitors);
				_temp.removeAll(unsafeMonitors);
				LOGGER.debug("Unaccounted Monitors: \n" + CollectionsUtilities.prettyPrint(_temp));
				LOGGER.debug("Overlapping Monitors: \n"
					+ CollectionsUtilities.prettyPrint(CollectionUtils.intersection(_safeMonitors, unsafeMonitors)));
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		unsafeMonitors.clear();
		monitor2relatedMonitors.clear();
		monitor2waits.clear();
		waitStmt2method.clear();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws InitializationException when any of the implementations required in the preconditions are not provided.
	 *
	 * @pre info.get(IMonitorInfo.ID) != null and info.get(ICallGraphInfo.ID) != null
	 * @pre info.get(IValueAnalyzer.ID) != null and info.get(PairManager.ID) != null
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();
		monitorInfo = (IMonitorInfo) info.get(IMonitorInfo.ID);

		if (monitorInfo == null) {
			final String _msg = "An interface with id, " + IMonitorInfo.ID + ", was not provided.";
			LOGGER.error(_msg);
			throw new InitializationException(_msg);
		}

		callgraphInfo = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (monitorInfo == null) {
			final String _msg = "An interface with id, " + ICallGraphInfo.ID + ", was not provided.";
			LOGGER.error(_msg);
			throw new InitializationException(_msg);
		}

		iva = (IValueAnalyzer) info.get(IValueAnalyzer.ID);

		if (iva == null) {
			final String _msg = "An interface with id, " + IValueAnalyzer.ID + ", was not provided.";
			LOGGER.error(_msg);
			throw new InitializationException(_msg);
		}

		pairMgr = (PairManager) info.get(PairManager.ID);

		if (pairMgr == null) {
			final String _msg = "An interface with id, " + PairManager.ID + ", was not provided.";
			LOGGER.error(_msg);
			throw new InitializationException(_msg);
		}
	}

	/**
	 * Checks if the given basic blocks are safe by condition 1 of safe lock.
	 *
	 * @param bbg containing the given basic blocks.
	 * @param basicBlocks enclosed by the monitor being checked.
	 * @param method in which the basic blocks occur.
	 * @param waitMethods are the collection of wait methods in the system.
	 *
	 * @return <code>true</code> if the given blocks are safe by condition 1; <code>false</code>, otherwise.
	 *
	 * @pre bbg != null and basicBlocks != null and waitMethods != null and method != null
	 * @pre bbg.getNodes().containsAll(basicBlocks)
	 */
	private boolean isMonitorSafeByCond1InTheseBasicBlocks(final BasicBlockGraph bbg, final Collection basicBlocks,
		final SootMethod method, final Collection waitMethods) {
		boolean _result = true;
		final Collection _t = AbstractDirectedGraph.findCycles(basicBlocks, bbg.getBackEdges());
		final Iterator _j = _t.iterator();
		final int _jEnd = _t.size();

		for (int _jIndex = 0; _jIndex < _jEnd && _result; _jIndex++) {
			final Collection _cycle = (Collection) _j.next();
			boolean _cycleIsUnsafe = true;
			final Collection _invocationStmts = bbg.getEnclosedStmts(_cycle);
			final Iterator _filteredIterator =
				IteratorUtils.filteredIterator(_invocationStmts.iterator(),
					SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE);

			for (final Iterator _k = _filteredIterator; _k.hasNext() && _cycleIsUnsafe;) {
				_cycleIsUnsafe = !callgraphInfo.areAnyMethodsReachableFrom(waitMethods, (Stmt) _k.next(), method);
			}
			_result &= !_cycleIsUnsafe;
		}
		return _result;
	}

	/**
	 * Processes the monitors and the wait invocations in the system to establish lock based relation.
	 */
	private void processMonitorAndWaitsForLockBasedRelation() {
		final Collection _monitorTriples = monitorInfo.getMonitorTriples();
		final Collection _temp = new HashSet(_monitorTriples);
		final Context _context = new Context();
		final Iterator _i = _monitorTriples.iterator();
		final int _iEnd = _monitorTriples.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple _monitor1 = (Triple) _i.next();
			final EnterMonitorStmt _enter1 = (EnterMonitorStmt) _monitor1.getFirst();
			final SootMethod _sm1 = (SootMethod) _monitor1.getThird();
			_context.setRootMethod(_sm1);
			_context.setStmt(_enter1);

			final Collection _c1;
			final Collection _mons;

			if (_enter1 != null) {
				_context.setProgramPoint(_enter1.getOpBox());
				_c1 = iva.getValues(_enter1.getOp(), _context);
				_mons = monitorInfo.getMonitorTriplesFor(_enter1, _sm1);
			} else {
				if (_sm1.isStatic()) {
					_c1 = Collections.singleton(_sm1.getDeclaringClass());
				} else {
					_c1 = iva.getValuesForThis(_context);
				}
				_mons = Collections.singleton(_monitor1);
			}

			_temp.removeAll(_mons);

			final Iterator _j = _temp.iterator();
			final int _jEnd = _temp.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Triple _monitor2 = (Triple) _j.next();
				final EnterMonitorStmt _enter2 = (EnterMonitorStmt) _monitor2.getFirst();
				final SootMethod _sm2 = (SootMethod) _monitor2.getThird();
				_context.setRootMethod(_sm2);
				_context.setStmt(_enter2);

				final Collection _c2;

				if (_enter2 != null) {
					_context.setProgramPoint(_enter2.getOpBox());
					_c2 = iva.getValues(_enter2.getOp(), _context);
				} else {
					if (_sm2.isStatic()) {
						_c2 = Collections.singleton(_sm2.getDeclaringClass());
					} else {
						_c2 = iva.getValuesForThis(_context);
					}
				}

				if (CollectionUtils.containsAny(_c1, _c2)) {
					CollectionsUtilities.putIntoSetInMap(monitor2relatedMonitors, _monitor1, _monitor2);
				}
			}
			_temp.addAll(_mons);
		}
	}

	/**
	 * Processes the monitors and the wait invocations in the system to establish "semantic" enclosure relation.
	 *
	 * @param monitorGraph to be used.
	 *
	 * @pre monitorGraph != null
	 */
	private void processMonitorsAndWaitsForEnclosureBaseRelation(final IMonitorGraph monitorGraph) {
		final Set _keySet = waitStmt2method.keySet();
		final Iterator _i = _keySet.iterator();
		final int _iEnd = _keySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final InvokeStmt _waitStmt = (InvokeStmt) _i.next();
			final SootMethod _method = (SootMethod) waitStmt2method.get(_waitStmt);
			final Collection _enclosingMonitors =
				monitorGraph.getInterProcedurallyEnclosingMonitorTriples(_waitStmt, _method, false).values();
			final Iterator _j = _enclosingMonitors.iterator();
			final int _jEnd = _enclosingMonitors.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Collection _monitors = (Collection) _j.next();
				final Iterator _k = _monitors.iterator();
				final int _kEnd = _monitors.size();

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final Triple _monitor = (Triple) _k.next();
					CollectionsUtilities.putIntoSetInMap(monitor2waits, _monitor, pairMgr.getPair(_waitStmt, _method));
				}
			}
		}
	}

	/**
	 * Propagates the "unsafe" information to other monitors based on monitor enclosures and lock based relations.
	 *
	 * @param collectedUnsafeMonitors is the collection of unsafe monitors.
	 * @param monitorGraph to be used.
	 *
	 * @pre monitorGraph != null
	 * @pre collectedUnsafeMonitors != null
	 * @pre collectedUnsafeMonitors.oclIsKindOf(Collection(Triple))
	 */
	private void propagateSafetyInformation(final Collection collectedUnsafeMonitors, final IMonitorGraph monitorGraph) {
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(unsafeMonitors);
		_wb.addAllWork(collectedUnsafeMonitors);

		while (_wb.hasWork()) {
			final Triple _monitor = (Triple) _wb.getWork();
			_wb.addAllWork((Collection) MapUtils.getObject(monitor2relatedMonitors, _monitor, Collections.EMPTY_SET));

			final INode _node = monitorGraph.queryNode(_monitor);
			_wb.addAllWork(CollectionUtils.collect(monitorGraph.getReachablesFrom(_node, false),
					IObjectDirectedGraph.OBJECT_EXTRACTOR));
		}
	}

	/**
	 * Checks if the given monitor is safe by condition 1 - there are no wait free loops in the monitor.
	 *
	 * @param monitor to be analyzed.
	 * @param monitorGraph to be used.
	 *
	 * @return <code>true</code> if the monitor is safe; <code>false</code>, otherwise.
	 *
	 * @pre monitor != null
	 */
	private boolean safeByCondition1(final Triple monitor, final IMonitorGraph monitorGraph) {
		final Collection _waitMethods = new HashSet();
		final Collection _waits = (Collection) MapUtils.getObject(monitor2waits, monitor, Collections.EMPTY_SET);

		for (final Iterator _i = _waits.iterator(); _i.hasNext();) {
			final Pair _pair = (Pair) _i.next();
			_waitMethods.add(_pair.getSecond());
		}

		boolean _monitorIsSafe = true;
		SootMethod _method = (SootMethod) monitor.getThird();
		final Map _method2enclosedStmts = monitorGraph.getInterProcedurallyEnclosedStmts(monitor, false);
		BasicBlockGraph _bbg = getBasicBlockGraph(_method);
		Collection _stmts = (Collection) _method2enclosedStmts.remove(_method);
		final Collection _basicBlocks = _bbg.getEnclosingBasicBlocks(_stmts);

		if (monitor.getFirst() != null) {
			_basicBlocks.remove(_bbg.getEnclosingBlock((Stmt) monitor.getFirst()));
			_basicBlocks.remove(_bbg.getEnclosingBlock((Stmt) monitor.getSecond()));
		}
		_monitorIsSafe = isMonitorSafeByCond1InTheseBasicBlocks(_bbg, _basicBlocks, _method, _waitMethods);

		final Set _entrySet = _method2enclosedStmts.entrySet();
		final Iterator _i = _entrySet.iterator();
		final int _iEnd = _entrySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd && _monitorIsSafe; _iIndex++) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_method = (SootMethod) _entry.getKey();
			_bbg = getBasicBlockGraph(_method);
			_stmts = (Collection) _entry.getValue();
			_monitorIsSafe &= isMonitorSafeByCond1InTheseBasicBlocks(_bbg, _bbg.getEnclosingBasicBlocks(_stmts), _method,
				  _waitMethods);
		}

		return _monitorIsSafe;
	}

	/**
	 * Checks if the given monitor is safe by condition 2 - no waits for other locks.
	 *
	 * @param monitor to be analyzed.
	 *
	 * @return <code>true</code> if the monitor is safe; <code>false</code>, otherwise.
	 *
	 * @pre monitor != null
	 */
	private boolean safeByCondition2(final Triple monitor) {
		boolean _safe = true;
		final EnterMonitorStmt _monitorStmt = (EnterMonitorStmt) monitor.getFirst();
		final SootMethod _monitorMethod = (SootMethod) monitor.getThird();
		final Context _context = new Context();
		_context.setRootMethod(_monitorMethod);
		_context.setStmt(_monitorStmt);

		final Collection _c1;

		if (_monitorStmt != null) {
			_context.setProgramPoint(_monitorStmt.getOpBox());
			_c1 = iva.getValues(_monitorStmt.getOp(), _context);
		} else {
			if (_monitorMethod.isStatic()) {
				_c1 = Collections.singleton(_monitorMethod.getDeclaringClass());
			} else {
				_c1 = iva.getValuesForThis(_context);
			}
		}

		final Collection _waits = (Collection) MapUtils.getObject(monitor2waits, monitor, Collections.EMPTY_SET);
		final Iterator _j = _waits.iterator();
		final int _jEnd = _waits.size();

		for (int _jIndex = 0; _jIndex < _jEnd && _safe; _jIndex++) {
			final Pair _pair = (Pair) _j.next();
			final InvokeStmt _waitStmt = (InvokeStmt) _pair.getFirst();
			final SootMethod _waitMethod = (SootMethod) _pair.getSecond();
			final VirtualInvokeExpr _invokeExpr = (VirtualInvokeExpr) _waitStmt.getInvokeExpr();
			_context.setRootMethod(_waitMethod);
			_context.setStmt(_waitStmt);
			_context.setProgramPoint(_invokeExpr.getBaseBox());

			final Collection _c2 = iva.getValues(_invokeExpr.getBase(), _context);

			_safe = _c1.size() == 1 && _c1.equals(_c2);
		}

		return _safe;
	}
}

// End of File
