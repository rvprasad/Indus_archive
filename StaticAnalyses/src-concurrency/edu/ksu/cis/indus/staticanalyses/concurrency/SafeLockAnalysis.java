
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

package edu.ksu.cis.indus.staticanalyses.concurrency;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.AbstractDirectedGraph;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph.IObjectNode;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;

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
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SafeLockAnalysis
  extends AbstractAnalysis {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	public static final Object ID = "Lock Analysis";

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static final Log LOGGER = LogFactory.getLog(SafeLockAnalysis.class);

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Collection waits = new HashSet();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final PairManager pairMgr = new PairManager();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	ICallGraphInfo callgraphInfo;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	IMonitorInfo monitorInfo;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Collection unsafeMonitors = new HashSet();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Collection unsafeWaits = new HashSet();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IValueAnalyzer iva;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Map monitor2relatedMonitors = new HashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Map monitor2relatedWaits = new HashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Map monitor2waits = new HashMap();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Map wait2monitorTriples = new HashMap();

	/**
	 * DOCUMENT ME!
	 */
	public SafeLockAnalysis() {
		preprocessor = new WaitInvocationCollectingProcessor();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
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
				waits.add(pairMgr.getOptimizedPair(stmt, _currentMethod));
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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (monitorInfo.isStable() && callgraphInfo.isStable()) {
			processMonitorAndWaitsForLockBasedRelation();
			processMonitorsAndWaitsForEnclosureBaseRelation();

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
			final Collection _reachableMethods = callgraphInfo.getReachableMethods();
			final Iterator _i = _reachableMethods.iterator();
			final int _iEnd = _reachableMethods.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootMethod _method = (SootMethod) _i.next();
				processMethod(_method);
			}

			final IObjectDirectedGraph _monitorGraph = monitorInfo.getMonitorGraph(callgraphInfo);

			final IWorkBag _wb = new HistoryAwareFIFOWorkBag(new HashSet());
			_wb.addAllWork(unsafeMonitors);

			while (_wb.hasWork()) {
				final Triple _monitor = (Triple) _wb.getWork();
				markRelatedMonitorsAndWaitsAsUnsafe(_monitor);

				final INode _node = _monitorGraph.queryNode(_monitor);
				final Iterator _j = _node.getPredsOf().iterator();
				final int _jEnd = _node.getPredsOf().size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final IObjectNode _pred = (IObjectNode) _j.next();
					final Object _containerMonitor = _pred.getObject();
					unsafeMonitors.add(_containerMonitor);
					_wb.addWork(_containerMonitor);
				}
			}
			stable();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Unsafe Monitors: \n" + CollectionsUtilities.prettyPrint(unsafeMonitors));
				LOGGER.debug("Safe Monitors: \n"
					+ CollectionsUtilities.prettyPrint(CollectionUtils.subtract(monitorInfo.getMonitorTriples(),
							unsafeMonitors)));
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		pairMgr.reset();
		unsafeMonitors.clear();
		monitor2relatedMonitors.clear();
		monitor2relatedWaits.clear();
		monitor2waits.clear();
		waits.clear();
		wait2monitorTriples.clear();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @throws InitializationException DOCUMENT ME!
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
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param waitMethodPair DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Collection getMonitorsEnclosingWait(final Pair waitMethodPair) {
		return CollectionsUtilities.getSetFromMap(wait2monitorTriples, waitMethodPair);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param monitor DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Collection getWaitStmtsInMonitor(final Triple monitor) {
		return (Collection) MapUtils.getObject(monitor2waits, monitor, Collections.EMPTY_LIST);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	private void processMonitorsAndWaitsForEnclosureBaseRelation() {
		final Collection _temp = new HashSet();
		final IWorkBag _wb = new HistoryAwareFIFOWorkBag(_temp);
		final Iterator _i = waits.iterator();
		final int _iEnd = waits.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair _waitMethodPair = (Pair) _i.next();
			final Stmt _waitStmt = (Stmt) _waitMethodPair.getFirst();
			final SootMethod _method = (SootMethod) _waitMethodPair.getSecond();
			_temp.clear();
			_wb.clear();
			_wb.addWork(new CallTriple(_method, _waitStmt, _waitStmt.getInvokeExpr()));

			while (_wb.hasWork()) {
				final CallTriple _ctrp = (CallTriple) _wb.getWork();
				final Stmt _stmt = _ctrp.getStmt();
				final SootMethod _sm = _ctrp.getMethod();
				final Collection _enclosingMonitors = monitorInfo.getEnclosingMonitorTriples(_stmt, _sm, false);

				if (_enclosingMonitors.isEmpty()) {
					_wb.addAllWorkNoDuplicates(callgraphInfo.getCallers(_sm));
				} else {
					final Iterator _j = _enclosingMonitors.iterator();
					final int _jEnd = _enclosingMonitors.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Triple _monitor = (Triple) _j.next();
						CollectionsUtilities.putIntoListInMap(monitor2waits, _monitor, _waitMethodPair);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param monitor DOCUMENT ME!
	 */
	private void markRelatedMonitorsAndWaitsAsUnsafe(final Triple monitor) {
		final Collection _monitors = (Collection) monitor2relatedMonitors.get(monitor);

		if (_monitors != null) {
			unsafeMonitors.addAll(_monitors);
		}

		final Collection _waits = (Collection) monitor2relatedWaits.get(monitor);

		if (_waits != null) {
			for (final Iterator _i = _waits.iterator(); _i.hasNext();) {
				final Pair _waitMethodpair = (Pair) _i.next();
				unsafeMonitors.addAll(getMonitorsEnclosingWait(_waitMethodpair));
				unsafeWaits.add(_waitMethodpair);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 */
	private void processMethod(final SootMethod method) {
		final Collection _temp = new HashSet();
		final Collection _monitorTriplesIn = monitorInfo.getMonitorTriplesIn(method);
		final Iterator _i = _monitorTriplesIn.iterator();
		final int _iEnd = _monitorTriplesIn.size();
		final BasicBlockGraph _bbg = getBasicBlockGraph(method);

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple _monitorTriple = (Triple) _i.next();

			if (!_temp.contains(_monitorTriple)) {
				final List _involvedBBs = _bbg.getEnclosedBasicBlocks(monitorInfo.getEnclosedStmts(_monitorTriple, false));

				if (!(safeByCondition2(_monitorTriple)
					  && safeByCondition3(_monitorTriple)
					  && safeByCondition1(_monitorTriple, method, _involvedBBs))) {
					unsafeMonitors.add(_monitorTriple);
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
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

			final Iterator _iter = waits.iterator();
			final int _iterEnd = waits.size();

			for (int _iterIndex = 0; _iterIndex < _iterEnd; _iterIndex++) {
				final Pair _waitMethodPair = (Pair) _iter.next();
				final Stmt _stmt = (Stmt) _waitMethodPair.getFirst();
				final SootMethod _sm = (SootMethod) _waitMethodPair.getSecond();
				final VirtualInvokeExpr _invokeExpr = (VirtualInvokeExpr) _stmt.getInvokeExpr();
				_context.setRootMethod(_sm);
				_context.setStmt(_stmt);
				_context.setProgramPoint(_invokeExpr.getBaseBox());				

                final Collection _c2 = iva.getValues((_invokeExpr).getBase(), _context);

				if (CollectionUtils.containsAny(_c1, _c2)) {
					CollectionsUtilities.putIntoSetInMap(monitor2relatedWaits, _monitor1, _waitMethodPair);
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param monitor DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param involvedBasicBlock DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private boolean safeByCondition1(final Triple monitor, final SootMethod method, final Collection involvedBasicBlock) {
		final BasicBlockGraph _bbg = getBasicBlockGraph(method);
		final Collection _monitorStmts = monitorInfo.getStmtsOfMonitor(monitor);

		for (final Iterator _i = _monitorStmts.iterator(); _i.hasNext();) {
			final Stmt _monitorStmt = (Stmt) _i.next();
			involvedBasicBlock.remove(_bbg.getEnclosingBlock(_monitorStmt));
		}

		final Collection _waitStmts = new HashSet(getWaitStmtsInMonitor(monitor));
		final Collection _waitMethods = new HashSet();

		for (final Iterator _i = _waitStmts.iterator(); _i.hasNext();) {
			final Pair _pair = (Pair) _i.next();
			_waitMethods.add(_pair.getSecond());
		}

		boolean _safe = true;

		final Collection _temp1 = new HashSet();
		final Collection _t = AbstractDirectedGraph.findCycles(involvedBasicBlock);
		final Iterator _j = _t.iterator();
		final int _jEnd = _t.size();

		for (int _jIndex = 0; _jIndex < _jEnd && _safe; _jIndex++) {
			final Collection _cycle = (Collection) _j.next();
			_safe = false;

			final Iterator _k = _cycle.iterator();
			final int _kEnd = _cycle.size();

			for (int _kIndex = 0; _kIndex < _kEnd && !_safe; _kIndex++) {
				final BasicBlock _bb = (BasicBlock) _k.next();
				final List _stmtsOf = _bb.getStmtsOf();
				_temp1.clear();
				_temp1.addAll(_stmtsOf);

				CollectionUtils.filter(_temp1, MonitorAnalysis.INVOKE_EXPR_PREDICATE);

				final Iterator _l = _temp1.iterator();
				final int _lEnd = _temp1.size();

				for (int _lIndex = 0; _lIndex < _lEnd && !_safe; _lIndex++) {
					final Stmt _stmt = (Stmt) _l.next();
					_safe = CollectionUtils.containsAny(callgraphInfo.getMethodsReachableFrom(_stmt, method), _waitMethods);
				}
			}
		}

		return _safe;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param monitor DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
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

		final Collection _waits = getWaitStmtsInMonitor(monitor);
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

            final Collection _c2 = iva.getValues((_invokeExpr).getBase(), _context);

			_safe = _c1.size() == 1 && _c1.equals(_c2);
		}

		return _safe;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param monitor DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private boolean safeByCondition3(final Triple monitor) {
		boolean _safe = true;
		final EnterMonitorStmt _enter1 = (EnterMonitorStmt) monitor.getFirst();
		final SootMethod _sm1 = (SootMethod) monitor.getThird();
		final Context _context = new Context();
		_context.setRootMethod(_sm1);
		_context.setStmt(_enter1);

		final Collection _c1;

		if (_enter1 != null) {
			_context.setProgramPoint(_enter1.getOpBox());
			_c1 = iva.getValues(_enter1.getOp(), _context);
		} else {
			if (_sm1.isStatic()) {
				_c1 = Collections.singleton(_sm1.getDeclaringClass());
			} else {
				_c1 = iva.getValuesForThis(_context);
			}
		}

		final IObjectDirectedGraph _graph = monitorInfo.getMonitorGraph(callgraphInfo);
		final Collection _reachables = _graph.getReachablesFrom(_graph.queryNode(monitor), true);
		final Iterator _j = _reachables.iterator();
		final int _jEnd = _reachables.size();

		for (int _jIndex = 0; _jIndex < _jEnd && _safe; _jIndex++) {
			final Triple _mon = (Triple) ((IObjectNode) _j.next()).getObject();
			final EnterMonitorStmt _enter2 = (EnterMonitorStmt) _mon.getFirst();
			final SootMethod _sm2 = (SootMethod) _mon.getThird();
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

			_safe = _c1.size() == 1 && _c1.equals(_c2);
		}

		return _safe;
	}

    /**
     * DOCUMENT ME!
     * 
     * @param method
     * @return
     */
    public boolean isLockSafe(final SootMethod method) {
        return !unsafeMonitors.contains(new Triple(null, null, method));
    }

    /**
     * DOCUMENT ME!
     * 
     * @param stmt
     * @param method
     * @return
     */
    public boolean isLockSafe(final Stmt stmt, final SootMethod method) {
        return !CollectionUtils.containsAny(unsafeMonitors, monitorInfo.getMonitorTriplesFor(stmt, method));
    }
}

/*
   ChangeLog:
   $Log$
   Revision 1.13  2004/07/27 11:07:20  venku
   - updated project to use safe lock analysis.

   Revision 1.12  2004/07/27 07:08:25  venku
   - revamped IMonitorInfo interface.
   - ripple effect in MonitorAnalysis, SafeLockAnalysis, and SychronizationDA.
   - deleted WaitNotifyAnalysis
   - ripple effect in EquivalenceClassBasedEscapeAnalysis.
   Revision 1.11  2004/07/25 10:52:22  venku
   - minor changes.
   Revision 1.10  2004/07/25 10:26:07  venku
   - added a new interface to query values attached to nodes.
   Revision 1.9  2004/07/23 13:09:44  venku
   - Refactoring in progress.
     - Extended IMonitorInfo interface.
     - Teased apart the logic to calculate monitor info from SynchronizationDA
       into MonitorAnalysis.
     - Casted EquivalenceClassBasedEscapeAnalysis as an AbstractAnalysis.
     - ripple effect.
     - Implemented safelock analysis to handle intraprocedural processing.
   Revision 1.8  2004/07/11 09:42:14  venku
   - Changed the way status information was handled the library.
     - Added class AbstractStatus to handle status related issues while
       the implementations just announce their status.
   Revision 1.7  2004/06/16 08:45:46  venku
   - need to put in the framework for the analysis.
   Revision 1.6  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.5  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.4  2003/09/28 03:17:13  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/09/15 01:42:21  venku
   - removed unnecessary TODO markers.
   Revision 1.2  2003/09/12 23:21:15  venku
   - committing to avoid annoyance.
   Revision 1.1  2003/09/12 23:15:40  venku
   - committing to avoid annoyance.
 */
