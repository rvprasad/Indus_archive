
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
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.AbstractDirectedGraph;
import edu.ksu.cis.indus.common.graph.IDirectedGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 * TODO: Implement this.
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
	private static final Log LOGGER = LogFactory.getLog(SafeLockAnalysis.class);

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	final PairManager pairMgr = new PairManager();

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private ICallGraphInfo callgraphInfo;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IMonitorInfo monitorInfo;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private Map enterMonitor2waits = new HashMap();

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private WaitNotifyAnalysis waitNotifyAnalysis;

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (monitorInfo.isStable()) {
			final Collection _reachableMethods = callgraphInfo.getReachableMethods();
			final Iterator _i = _reachableMethods.iterator();
			final int _iEnd = _reachableMethods.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootMethod _method = (SootMethod) _i.next();
				processMethod(_method);

				final IDirectedGraph _monitorGraph = monitorInfo.getMonitorGraph(callgraphInfo);

				/*
				 * Assume there is an API that can be used to check if waits and notifies are valid.
				 * For each method do the following.
				 * - Find all the statements immediately enclosed in monitors. [processMethod]
				 * - If any of these statements correspond to a invalid wait, then the enclosing monitor is unsafe. Hence,
				 *   the lock of objects that are used as monitor variable are unsafe.  [monitorIsSafeBasedOnWaitSafety]
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
			}
			stable();
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
	}

	/**
	 * DOCUMENT ME! <p></p>
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
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param monitorStmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Collection getWaitStmtsInMonitor(final EnterMonitorStmt monitorStmt, final SootMethod method) {
		return (Collection) MapUtils.getObject(enterMonitor2waits, monitorStmt, Collections.EMPTY_LIST);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param monitorStmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	private void addToUnsafeMonitors(EnterMonitorStmt monitorStmt, SootMethod method) {
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	private void addToUnsafeWaits(InvokeStmt stmt, SootMethod method) {
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 * @param monitorStmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	private void addWaitToMonitorWaitSet(final InvokeStmt stmt, final EnterMonitorStmt monitorStmt, final SootMethod method) {
		CollectionsUtilities.putIntoListInMap(enterMonitor2waits, pairMgr.getOptimizedPair(monitorStmt, method), stmt);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param monitorStmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param involvedBasicBlocks DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private boolean monitorIsSafeBasedOnWaitSafety(final EnterMonitorStmt monitorStmt, final SootMethod method,
		final Collection involvedBasicBlocks) {
		boolean _result = true;
		final Collection _stmts = monitorInfo.getEnclosedStmts(monitorStmt, method, false);
		final BasicBlockGraph _bbg = getBasicBlockGraph(method);
		final Iterator _i = _stmts.iterator();
		final int _iEnd = _stmts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Stmt _stmt = (Stmt) _i.next();

			if (_stmt instanceof InvokeStmt) {
				final InvokeStmt _invokeStmt = (InvokeStmt) _stmt;

				if (WaitNotifyAnalysis.isWaitInvocation(_invokeStmt, method, callgraphInfo)) {
					addWaitToMonitorWaitSet(_invokeStmt, monitorStmt, method);

					if (!waitNotifyAnalysis.isWaitCoupled(_invokeStmt, method)) {
						addToUnsafeWaits(_invokeStmt, method);
						_result = false;
					}
				}
			}
			involvedBasicBlocks.add(_bbg.getEnclosingBlock(_stmt));
		}
		return _result;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param method DOCUMENT ME!
	 */
	private void processMethod(final SootMethod method) {
		final Collection _enterMonitorStmts = new HashSet();
		final Collection _involvedBBs = new HashSet();
		final Collection _monitorTriplesIn = monitorInfo.getMonitorTriplesIn(method);
		final Iterator _i = _monitorTriplesIn.iterator();
		final int _iEnd = _monitorTriplesIn.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple _triple = (Triple) _i.next();
			final EnterMonitorStmt _enterMonitor = (EnterMonitorStmt) _triple.getFirst();

			if (!_enterMonitorStmts.contains(_enterMonitor)) {
				_involvedBBs.clear();

				boolean _safe = monitorIsSafeBasedOnWaitSafety(_enterMonitor, method, _involvedBBs);
				_safe = _safe && waitFreeCyclesExistInMonitor(_enterMonitor, method, _involvedBBs);

				if (!_safe) {
					addToUnsafeMonitors(_enterMonitor, method);
				}
			}
			_enterMonitorStmts.add(_enterMonitor);
		}
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param monitorStmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param involvedBasicBlock DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private boolean waitFreeCyclesExistInMonitor(final EnterMonitorStmt monitorStmt, final SootMethod method,
		final Collection involvedBasicBlock) {
		final BasicBlockGraph _bbg = getBasicBlockGraph(method);
		final Collection _triples = monitorInfo.getMonitorTriplesFor(monitorStmt, method);
		involvedBasicBlock.remove(_bbg.getEnclosingBlock(monitorStmt));

		for (final Iterator _i = _triples.iterator(); _i.hasNext();) {
			final Triple _monitor = (Triple) _i.next();
			final Stmt _exitMonitorStmt = (Stmt) _monitor.getSecond();

			if (_exitMonitorStmt != null) {
				involvedBasicBlock.remove(_bbg.getEnclosingBlock(_exitMonitorStmt));
			}
		}

		boolean _safe = true;

		for (final Iterator _i = _bbg.getSCCs(true).iterator(); _i.hasNext() && _safe;) {
			final Collection _scc = (Collection) _i.next();

			if (CollectionUtils.containsAny(_scc, involvedBasicBlock)) {
				for (final Iterator _j = AbstractDirectedGraph.findCycles(_scc).iterator(); _j.hasNext() && _safe;) {
					final Collection _cycle = (Collection) _j.next();

					if (involvedBasicBlock.containsAll(_cycle)) {
						for (final Iterator _k = _cycle.iterator(); _k.hasNext() && _safe;) {
							final BasicBlock _bb = (BasicBlock) _k.next();

							if (!CollectionUtils.containsAny(_bb.getStmtsOf(), getWaitStmtsInMonitor(monitorStmt, method))) {
								_safe = false;
							}
						}
					}
				}
			}
		}
		return _safe;
	}
}

/*
   ChangeLog:
   $Log$
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
