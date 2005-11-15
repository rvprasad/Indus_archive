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

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.common.collections.IteratorUtils;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Quadraple;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.SimpleNode;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.Constants;
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.Stmt;

/**
 * This class provides monitor information about the application. It provides information such the monitors in the
 * application, monitor enclosure information, and even a monitor graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class MonitorAnalysis
		extends AbstractAnalysis
		implements IMonitorInfo<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> {

	/**
	 * This represents monitor enclosure as a graph with each monitor represented as a node and an edge representing that the
	 * monitor of the source node encloses the monitor of the destination node.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class MonitorGraph
			extends SimpleNodeGraph<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>
			implements IMonitorInfo.IMonitorGraph<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> {

		/**
		 * The call graph on which this monitor graph is based on.
		 * 
		 * @invariant cgi != null
		 */
		private final ICallGraphInfo cgi;

		/**
		 * Creates an instance of this class.
		 * 
		 * @param callgraphInfo provides call graph information.
		 * @pre callgraphInfo != null
		 */
		public MonitorGraph(final ICallGraphInfo callgraphInfo) {
			cgi = callgraphInfo;
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo.IMonitorGraph#getInterProcedurallyEnclosedStmts(Triple, boolean)
		 */
		public Map<SootMethod, Collection<Stmt>> getInterProcedurallyEnclosedStmts(
				final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> monitorTriple, final boolean transitive) {
			final Map<SootMethod, Collection<Stmt>> _result;
			final EnterMonitorStmt _stmt = monitorTriple.getFirst();
			final SootMethod _sootMethod = monitorTriple.getThird();

			if (_stmt == null) {
				_result = getInterProcedurallyEnclosedStmts(_sootMethod, transitive);
			} else {
				_result = getInterProcedurallyEnclosedStmts(_stmt, _sootMethod, transitive);
			}
			return _result;
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo.IMonitorGraph#getInterProcedurallyEnclosingMonitorTriples(Stmt,
		 *      SootMethod, boolean)
		 */
		public Map<SootMethod, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> getInterProcedurallyEnclosingMonitorTriples(
				final Stmt stmt, final SootMethod method, final boolean transitive) {
			final Map<SootMethod, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> _result = new HashMap<SootMethod, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>>();
			final Collection<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> _monitorNodes = new HashSet<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>>();
			final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _immediateMonitors = getEnclosingMonitorTriples(
					stmt, method, false);
			_result.put(method, _immediateMonitors);

			if (transitive && !_immediateMonitors.isEmpty()) {
				final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = _immediateMonitors.iterator();
				final int _iEnd = _immediateMonitors.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _i.next();
					_monitorNodes.addAll(getReachablesFrom(queryNode(_monitor), false));
				}
			}
			final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _monitors = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
			CollectionUtils.transform(_monitorNodes, getObjectExtractor(), _monitors);

			@SuppressWarnings("unchecked") final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = _monitors.iterator();
			final int _iEnd = _monitors.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _i.next();
				MapUtils.putIntoSetInMap(_result, _monitor.getThird(), _monitor);
			}

			return _result;
		}

		/**
		 * This helps <code>getInterProcedurallyEnclosedStmts</code> methods to calculate enclosures.
		 * 
		 * @param method of interest.
		 * @param transitive <code>true</code> if transitive closure is required; <code>false</code>, otherwise.
		 * @param method2stmts an out parameter in which each method will be mapped to a set of statements.
		 * @param stmtsWithInvokeExpr is an iterator over statements with invoke expresssions.
		 * @pre method != null
		 * @pre methdo2stmts != null
		 * @pre stmtsWithInvokeExpr != null
		 * @post method2stmts.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
		 */
		private void calculateInterprocedurallyEnclosedStmts(final SootMethod method, final boolean transitive,
				final Map<SootMethod, Collection<Stmt>> method2stmts, final Iterator<Stmt> stmtsWithInvokeExpr) {
			final Context _context = new Context();
			final IWorkBag<Pair<Stmt, SootMethod>> _wb = new HistoryAwareLIFOWorkBag<Pair<Stmt, SootMethod>>(
					new HashSet<Pair<Stmt, SootMethod>>());

			for (final Iterator<Stmt> _i = stmtsWithInvokeExpr; _i.hasNext();) {
				final Stmt _stmt = _i.next();
				_wb.addWork(pairMgr.getPair(_stmt, method));
			}

			while (_wb.hasWork()) {
				final Pair<Stmt, SootMethod> _pair = _wb.getWork();
				final Stmt _stmt = _pair.getFirst();
				final SootMethod _caller = _pair.getSecond();
				_context.setStmt(_stmt);
				_context.setRootMethod(_caller);

				final Collection<SootMethod> _callees = cgi.getCallees(_stmt.getInvokeExpr(), _context);
				final Iterator<SootMethod> _i = _callees.iterator();
				final int _iEnd = _callees.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final SootMethod _callee = _i.next();

					if (!_callee.isSynchronized() || (_callee.isSynchronized() && transitive)) {
						final Collection<Stmt> _stmts = getUnenclosedStmtsOf(_callee);
						MapUtils.putAllIntoCollectionInMap(method2stmts, _callee, _stmts);

						for (final Stmt _s : IteratorUtils.filteredIterable(_stmts,	SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE)) {
							_wb.addWork(pairMgr.getPair(_s, _callee));
						}
					}
				}
			}
		}

		/**
		 * Retrieves the statements enclosed in the monitor of the synchronized method. This retrieves inter procedural
		 * enclosures.
		 * 
		 * @param method of interest.
		 * @param transitive <code>true</code> if transitive closure is required; <code>false</code>, otherwise.
		 * @return a map from methods to statements in them that occur in the enclosure.
		 * @pre method != null
		 * @post result != null and result.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
		 */
		private Map<SootMethod, Collection<Stmt>> getInterProcedurallyEnclosedStmts(final SootMethod method,
				final boolean transitive) {
			final Collection<Stmt> _intraStmts = getEnclosedStmts(new Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>(
					null, null, method), transitive);
			final Map<SootMethod, Collection<Stmt>> _method2stmts = new HashMap<SootMethod, Collection<Stmt>>();
			_method2stmts.put(method, _intraStmts);

			final Iterator<Stmt> _stmtsWithInvokeExpr = IteratorUtils.filteredIterator(_intraStmts.iterator(),
					SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE);
			calculateInterprocedurallyEnclosedStmts(method, transitive, _method2stmts, _stmtsWithInvokeExpr);
			return _method2stmts;
		}

		/**
		 * Retrieves the statements enclosed in the monitor of the synchronized method. This retrieves inter procedural
		 * enclosures.
		 * 
		 * @param monitorStmt of interest.
		 * @param method of interest.
		 * @param transitive <code>true</code> if transitive closure is required; <code>false</code>, otherwise.
		 * @return a map from methods to statements in them that occur in the enclosure.
		 * @pre method != null and monitorStmt != null
		 * @pre monitorStmt.oclIsKindOf(EnterMonitorStmt) or monitorStmt.oclIsKindOf(ExitMonitorStmt)
		 * @post result != null and result.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
		 */
		private Map<SootMethod, Collection<Stmt>> getInterProcedurallyEnclosedStmts(final Stmt monitorStmt,
				final SootMethod method, final boolean transitive) {
			final Collection<Stmt> _intraStmts = new HashSet<Stmt>();
			final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _monitorTriplesFor = getMonitorTriplesFor(
					monitorStmt, method);
			final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = _monitorTriplesFor.iterator();
			final int _iEnd = _intraStmts.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _i.next();
				_intraStmts.addAll(getEnclosedStmts(_monitor, transitive));
			}

			final Map<SootMethod, Collection<Stmt>> _method2stmts = new HashMap<SootMethod, Collection<Stmt>>();
			_method2stmts.put(method, _intraStmts);

			final Iterator<Stmt> _stmtsWithInvokeExpr = IteratorUtils.filteredIterator(_intraStmts.iterator(),
					SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE);
			calculateInterprocedurallyEnclosedStmts(method, transitive, _method2stmts, _stmtsWithInvokeExpr);
			return _method2stmts;
		}
	}

	/**
	 * This the preprocessor which captures the synchronization points in the system.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private final class PreProcessor
			extends AbstractProcessor {

		/**
		 * Preprocesses the given method. It records if the method is synchronized.
		 * 
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(SootMethod)
		 */
		@Override public void callback(final SootMethod method) {
			if (method.isSynchronized()) {
				final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _triple = new Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>(
						null, null, method);
				_triple.optimize();
				monitorTriples.add(_triple);
				syncedMethods.add(method);
			}
		}

		/**
		 * Preprocesses the given stmt. It records if the <code>stmt</code> is an enter/exit monitor statement.
		 * 
		 * @param stmt is the enter/exit monitor statement.
		 * @param context in which <code>stmt</code> occurs. This contains the method that encloses <code>stmt</code>.
		 * @pre stmt.isOclTypeOf(EnterMonitorStmt)
		 * @pre context.getCurrentMethod() != null
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(Stmt,Context)
		 */
		@Override public void callback(final Stmt stmt, final Context context) {
			if (stmt instanceof EnterMonitorStmt) {
				MapUtils.putIntoSetInMap(method2enterMonitors, context.getCurrentMethod(),
						(EnterMonitorStmt) stmt);
			}
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(EnterMonitorStmt.class, this);
			ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(EnterMonitorStmt.class, this);
			ppc.unregister(this);
		}
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(MonitorAnalysis.class);

	/**
	 * This is the approximate number of monitors in the application.
	 */
	static final int NUM_OF_MONITORS_IN_APPLICATION = 500;

	/**
	 * This is the approximate number of synchronized methods in the application.
	 */
	static final int NUM_OF_SYNCED_METHODS_IN_APPLICATION = 50;

	/**
	 * This maps methods to a map from statements to the immediately enclosing monitor statements.
	 */
	final Map<SootMethod, Map<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>>> method2enclosedStmts2monitors = new HashMap<SootMethod, Map<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>>>(
			Constants.getNumOfMethodsInApplication());

	/**
	 * This collects the enter-monitor statements in each method during preprocessing. This is used during analysis only.
	 */
	final Map<SootMethod, Collection<EnterMonitorStmt>> method2enterMonitors = new HashMap<SootMethod, Collection<EnterMonitorStmt>>(
			Constants.getNumOfMethodsInApplication());

	/**
	 * This maps methods to a map from monitor statements to the immediately enclosed statements.
	 */
	final Map<SootMethod, Map<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>, Collection<Stmt>>> method2monitor2enclosedStmts = new HashMap<SootMethod, Map<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>, Collection<Stmt>>>(
			Constants.getNumOfMethodsInApplication());

	/**
	 * This is collection of monitor triples that occur in the analyzed system. The elements of the triple are of type
	 * <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and <code>SootMethod</code>, respectively.
	 * 
	 * @invariant monitorTriples->forall(o | o.getFirst() == null and o.getSecond() == null implies
	 *            o.getThird().isSynchronized())
	 */
	final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> monitorTriples = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>(
			NUM_OF_MONITORS_IN_APPLICATION);

	/**
	 * The pair manager.
	 */
	PairManager pairMgr;

	/**
	 * This is a temporary collection of sychronized methods.
	 */
	final Collection<SootMethod> syncedMethods = new HashSet<SootMethod>(NUM_OF_SYNCED_METHODS_IN_APPLICATION);

	/**
	 * This provides object flow information.
	 */
	private IValueAnalyzer<Value> ofa;

	/**
	 * This maps synchronized methods to the collection of statements in them that are synchronization dependent on the entry
	 * and exit points of the method.
	 */
	private final Map<SootMethod, Collection<Stmt>> syncedMethod2enclosedStmts = new HashMap<SootMethod, Collection<Stmt>>(
			NUM_OF_SYNCED_METHODS_IN_APPLICATION);

	/**
	 * Creates a new MonitorAnalysis object.
	 */
	public MonitorAnalysis() {
		preprocessor = new PreProcessor();
	}

	/**
	 * Calculates the synchronization dependency information for the methods provided during initialization.
	 * 
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	@Override public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Monitor Analysis processing");
		}

		final Map<EnterMonitorStmt, Collection<ExitMonitorStmt>> _enter2exits = new HashMap<EnterMonitorStmt, Collection<ExitMonitorStmt>>();
		final Collection<EnterMonitorStmt> _processedMonitors = new HashSet<EnterMonitorStmt>();

		/*
		 * Calculating monitor info is not as simple as it looks in the presence of exceptions. The exit monitors are the
		 * tricky ones. The exit monitors are guarded by catch block generated by the compiler. The user cannot generate such
		 * complex catch blocks, but nevertheless we cannot assume about the compiler. So, we proceed by calculating the
		 * monitor info over a complete unit graph and use object flow information to arbitrate suspicious enter-exit matches.
		 */
		for (final Iterator<Map.Entry<SootMethod, Collection<EnterMonitorStmt>>> _i = method2enterMonitors.entrySet()
				.iterator(); _i.hasNext();) {
			final Map.Entry<SootMethod, Collection<EnterMonitorStmt>> _entry = _i.next();
			final SootMethod _method = _entry.getKey();
			_enter2exits.clear();

			for (final Iterator<EnterMonitorStmt> _j = _entry.getValue().iterator(); _j.hasNext();) {
				final EnterMonitorStmt _enterMonitor = _j.next();
				final Map<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>, Collection<Stmt>> _monitor2enclosedStmts = MapUtils
						.getMapFromMap(method2monitor2enclosedStmts, _method);

				if (!_processedMonitors.contains(_enterMonitor) && _monitor2enclosedStmts.get(_enterMonitor) == null) {
					_processedMonitors.addAll(processMonitor(_processedMonitors, _enterMonitor, _method, _enter2exits));
				}
			}
		}

		method2enterMonitors.clear();
		stable();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("analyze() - " + toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Monitor Analysis processing");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosedStmts(edu.ksu.cis.indus.common.datastructures.Triple,
	 *      boolean)
	 */
	public Collection<Stmt> getEnclosedStmts(final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> monitorTriple,
			final boolean transitive) {
		final Collection<Stmt> _result = new HashSet<Stmt>();
		final EnterMonitorStmt _enterMonitorStmt = monitorTriple.getFirst();
		final SootMethod _sm = monitorTriple.getThird();

		if (_enterMonitorStmt == null) {
			if (!syncedMethods.isEmpty()) {
				processSyncedMethods();
			}

			_result.addAll(MapUtils.getEmptyCollectionFromMap(syncedMethod2enclosedStmts, _sm));

			if (transitive && !_result.isEmpty()) {
				final Map<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>, Collection<Stmt>> _monitor2enclosedStmts = method2monitor2enclosedStmts
						.get(_sm);
				final Collection<Stmt> _temp = new HashSet<Stmt>();
				_temp.addAll(_result);
				CollectionUtils.filter(_temp, SootPredicatesAndTransformers.ENTER_MONITOR_STMT_PREDICATE);

				final Iterator<Stmt> _i = _temp.iterator();
				final int _iEnd = _temp.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final Stmt _monitorStmt = _i.next();
					final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _monitorTriplesFor = getMonitorTriplesFor(
							_monitorStmt, _sm);
					final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _j = _monitorTriplesFor.iterator();
					final int _jEnd = _monitorTriplesFor.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _j.next();
						_result.addAll(calculateTransitiveClosureOfEnclosedStmts(_monitor2enclosedStmts, _monitor, _sm));
					}
				}
			}
		} else {
			final Map<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>, Collection<Stmt>> _monitor2enclosedStmts = MapUtils
					.getEmptyMapFromMap(method2monitor2enclosedStmts, _sm);

			if (!_monitor2enclosedStmts.isEmpty()) {
				if (transitive) {
					_result.addAll(calculateTransitiveClosureOfEnclosedStmts(_monitor2enclosedStmts, monitorTriple, _sm));
				} else {
					_result.addAll(MapUtils.getFromMap(_monitor2enclosedStmts, monitorTriple, Collections
							.<Stmt>emptyList()));
				}
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosingMonitorStmts(soot.jimple.Stmt, soot.SootMethod, boolean)
	 */
	public Collection<MonitorStmt> getEnclosingMonitorStmts(final Stmt stmt, final SootMethod method, final boolean transitive) {
		final Collection<MonitorStmt> _result = new HashSet<MonitorStmt>();
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _temp = getEnclosingMonitorTriples(stmt,
				method, transitive);
		final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = _temp.iterator();
		final int _iEnd = _temp.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _i.next();
			final EnterMonitorStmt _enter = _monitor.getFirst();

			if (_enter != null) {
				_result.add(_enter);
				_result.add(_monitor.getSecond());
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosingMonitorTriples(soot.jimple.Stmt, SootMethod, boolean)
	 */
	public Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> getEnclosingMonitorTriples(final Stmt stmt,
			final SootMethod method, final boolean transitive) {
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _result = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		final Map<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> _enclosedStmt2monitors = MapUtils.getEmptyMapFromMap(method2enclosedStmts2monitors, method);

		if (_enclosedStmt2monitors.size() > 0) {
			if (transitive) {
				_result.addAll(calculateTransitiveClosureOfEnclosingMonitor(_enclosedStmt2monitors, stmt));
			} else {
				_result.addAll(MapUtils.getEmptyCollectionFromMap(_enclosedStmt2monitors, stmt));
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection<? extends Comparable<?>> getIds() {
		return Collections.singleton(IMonitorInfo.ID);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorGraph(edu.ksu.cis.indus.interfaces.ICallGraphInfo)
	 */
	public IMonitorGraph<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> getMonitorGraph(final ICallGraphInfo callgraphInfo) {
		final MonitorGraph _result = new MonitorGraph(callgraphInfo);
		final Collection<CallTriple> _temp = new HashSet<CallTriple>();
		final IWorkBag<CallTriple> _wb = new HistoryAwareFIFOWorkBag<CallTriple>(_temp);
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _considered = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		final Collection<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> _dests = new HashSet<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>>();
		final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = monitorTriples.iterator();
		final int _iEnd = monitorTriples.size();

		// we use a backward search on the call graph to construct the monitor graph.
		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _i.next();

			if (_considered.contains(_monitor)) {
				continue;
			}

			final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _monitorTriplesOf = getMonitorTriplesOf(_monitor);
			_considered.addAll(_monitorTriplesOf);
			_dests.clear();

			final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _j = _monitorTriplesOf.iterator();
			final int _jEnd = _monitorTriplesOf.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				_dests.add(_result.getNode(_j.next()));
			}

			final EnterMonitorStmt _stmt = _monitor.getFirst();
			final SootMethod _method = _monitor.getThird();
			final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _enclosingMonitors = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();

			if (_stmt != null) {
				_enclosingMonitors.addAll(getEnclosingMonitorTriples(_stmt, _method, false));
			}

			if (!_enclosingMonitors.isEmpty()) {
				addEdgesFromToIn(_enclosingMonitors, _dests, _result);
			} else {
				_temp.clear();
				_wb.clear();
				_wb.addAllWork(callgraphInfo.getCallers(_method));

				while (_wb.hasWork()) {
					final CallTriple _ctrp = _wb.getWork();
					final SootMethod _sm = _ctrp.getMethod();
					final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _mons = getEnclosingMonitorTriples(
							_ctrp.getStmt(), _sm, false);

					if (_mons.isEmpty()) {
						_wb.addAllWorkNoDuplicates(callgraphInfo.getCallers(_sm));
					} else {
						_enclosingMonitors.addAll(_mons);
					}
				}

				if (!_enclosingMonitors.isEmpty()) {
					addEdgesFromToIn(_enclosingMonitors, _dests, _result);
				}
			}

			_considered.addAll(_monitorTriplesOf);
		}
		return _result;
	}

	/**
	 * Returns the monitors that occur in the analyzed system.
	 * 
	 * @return a collection of <code>Triples</code>.
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorTriples()
	 */
	public Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> getMonitorTriples() {
		return Collections.unmodifiableCollection(monitorTriples);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorTriplesFor(soot.jimple.Stmt, SootMethod)
	 */
	public Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> getMonitorTriplesFor(final Stmt monitorStmt,
			final SootMethod method) {
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _result = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _monitorTriplesInMethod = getMonitorTriplesIn(method);
		final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = _monitorTriplesInMethod.iterator();
		final int _iEnd = _monitorTriplesInMethod.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _triple = _i.next();

			final Object _enter = _triple.getFirst();
			final Object _exit = _triple.getSecond();

			if ((_enter != null && _enter.equals(monitorStmt)) || (_exit != null && _exit.equals(monitorStmt))) {
				_result.add(_triple);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorTriplesIn(SootMethod)
	 */
	public Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> getMonitorTriplesIn(final SootMethod method) {
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _result = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = monitorTriples.iterator();
		final int _iEnd = monitorTriples.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _triple = _i.next();

			if (_triple.getThird().equals(method)) {
				_result.add(_triple);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorTriplesOf(edu.ksu.cis.indus.common.datastructures.Triple)
	 */
	public Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> getMonitorTriplesOf(
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> monitor) {
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _triples;

		if (monitor.getFirst() == null) {
			_triples = Collections.singleton(monitor);
		} else {
			_triples = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();

			final Collection<? extends MonitorStmt> _stmts = getStmtsOfMonitor(monitor);
			final SootMethod _method = monitor.getThird();

			final Iterator<? extends MonitorStmt> _i = _stmts.iterator();
			final int _iEnd = _stmts.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Stmt _stmt = _i.next();
				_triples.addAll(getMonitorTriplesFor(_stmt, _method));
			}
		}
		return _triples;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getStmtsOfMonitor(edu.ksu.cis.indus.common.datastructures.Triple)
	 */
	public Collection<MonitorStmt> getStmtsOfMonitor(final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> monitor) {
		final Collection<MonitorStmt> _result;
		final EnterMonitorStmt _monitorStmt = monitor.getFirst();

		if (_monitorStmt != null) {
			_result = new HashSet<MonitorStmt>();
			_result.add(_monitorStmt);

			for (final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = getMonitorTriplesFor(
					_monitorStmt, monitor.getThird()).iterator(); _i.hasNext();) {
				final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _i.next();
				_result.add(_monitor.getSecond());
			}
		} else {
			_result = Collections.emptySet();
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getUnenclosedStmtsOf(soot.SootMethod)
	 */
	public Collection<Stmt> getUnenclosedStmtsOf(final SootMethod method) {
		final Collection<Stmt> _enclosedStmts = MapUtils.getEmptyMapFromMap(method2enclosedStmts2monitors, method).keySet();
		final IPredicate<Stmt> _predicate = new IPredicate<Stmt>() {

			public boolean evaluate(final Stmt o) {
				return !_enclosedStmts.contains(o);
			}
		};

		final Collection<Stmt> _result = IteratorUtils.toList(IteratorUtils.filteredIterator(getUnitGraph(method).iterator(),
				_predicate));
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	@Override public void reset() {
		super.reset();
		monitorTriples.clear();
		syncedMethods.clear();
		syncedMethod2enclosedStmts.clear();
		method2enclosedStmts2monitors.clear();
		method2monitor2enclosedStmts.clear();
		method2enterMonitors.clear();
	}

	// /CLOVER:OFF
	/**
	 * Returns a stringized representation of this analysis. The representation includes the results of the analysis.
	 * 
	 * @return a stringized representation of this object.
	 */
	@Override public String toString() {
		final StringBuffer _result = new StringBuffer("Statistics for Monitor Analysis as calculated by "
				+ getClass().getName() + "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator<Map.Entry<SootMethod, Map<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>>>> _i = method2enclosedStmts2monitors
				.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry<SootMethod, Map<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>>> _entry = _i
					.next();
			_localEdgeCount = 0;

			final SootMethod _method = _entry.getKey();

			for (final Iterator<Map.Entry<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>>> _j = _entry
					.getValue().entrySet().iterator(); _j.hasNext();) {
				final Map.Entry<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> _entry2 = _j.next();
				final Stmt _dependent = _entry2.getKey();

				for (final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _k = _entry2.getValue().iterator(); _k
						.hasNext();) {
					final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _obj = _k.next();
					_temp.append("\t\t" + _dependent + "[" + _dependent.hashCode() + "] -enclosed by- " + _obj + "["
							+ _obj.hashCode() + "]\n");
				}
				_localEdgeCount += ((Collection) _entry2.getValue()).size();
			}
			_result.append("\tFor " + _method + " there are " + _localEdgeCount + " enclosures.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}

		int _edgeCount2 = 0;

		for (final Iterator<Map<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>, Collection<Stmt>>> _i = method2monitor2enclosedStmts
				.values().iterator(); _i.hasNext();) {
			for (final Iterator<Collection<Stmt>> _j = _i.next().values().iterator(); _j.hasNext();) {
				_edgeCount2 += _j.next().size();
			}
		}
		_result.append("A total of " + _edgeCount + "/" + _edgeCount2 + " enclosures exist.\n");
		_result.append("MonitorInfo follows:\n");

		for (final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = monitorTriples.iterator(); _i
				.hasNext();) {
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _trip = _i.next();

			if (_trip.getFirst() != null) {
				_result.append("[" + _trip.getFirst() + " " + _trip.getFirst().hashCode() + ", " + _trip.getSecond() + " "
						+ _trip.getSecond().hashCode() + "] occurs in " + _trip.getThird() + "\n");
			} else {
				_result.append(_trip.getThird() + " is synchronized.\n");
			}
		}
		return _result.toString();
	}

	// /CLOVER:ON
	/**
	 * {@inheritDoc}
	 * 
	 * @throws InitializationException when object flow analysis is not provided.
	 * @pre info.get(OFAnalyzer.ID) != null and info.get(OFAnalyzer.ID).oclIsTypeOf(OFAnalyzer)
	 * @pre info.get(PairManager.ID) != null and info.get(PairManager.ID).oclIsTypeOf(PairManager)
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	@Override protected void setup() throws InitializationException {
		super.setup();

		ofa = (IValueAnalyzer) info.get(IValueAnalyzer.ID);

		if (ofa == null) {
			throw new InitializationException(IValueAnalyzer.ID + " was not provided in the info.");
		}

		pairMgr = (PairManager) info.get(PairManager.ID);

		if (pairMgr == null) {
			throw new InitializationException(PairManager.ID + " was not provided in the info.");
		}
	}

	/**
	 * Adds edges from the given sources to the given destination (all-to-all) in the given graph.
	 * 
	 * @param srcs is the collection of enclosing monitors.
	 * @param dests is the collection of nodes corresponding to enclosed monitors.
	 * @param graph is the monitor graph being constructed.
	 * @pre srcs != null and dests != null and graph != null
	 * @post srcs->forall(o | graph.queryNode(o ) != null) and dests->forall(o | graph.queryNode(o) != null)
	 * @post srcs->forall(o | dests->forall(p | graph.queryNode(o).getSuccsOf()->contains(graph.queryNode(p))))
	 */
	private void addEdgesFromToIn(final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> srcs,
			final Collection<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> dests, final MonitorGraph graph) {
		final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _k = srcs.iterator();
		final int _kEnd = srcs.size();

		for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _t = _k.next();
			final SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _src = graph.getNode(_t);
			final Iterator<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> _l = dests.iterator();
			final int _lEnd = dests.size();

			for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
				graph.addEdgeFromTo(_src, _l.next());
			}
		}
	}

	/**
	 * Calculates the transitive closure of enclosing monitor for the given statement based in the given map.
	 * 
	 * @param monitor2stmts maps a statement to it's immediately enclosing intraprocedural monitor.
	 * @param monitor from which to initiate the closure.
	 * @param method in which <code>stmt</code> occurs.
	 * @return the contents of the closure.
	 * @pre map != null and stmt != null and method != null
	 * @post result != null
	 */
	private Collection<Stmt> calculateTransitiveClosureOfEnclosedStmts(
			final Map<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>, Collection<Stmt>> monitor2stmts,
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> monitor, final SootMethod method) {
		final Collection<Stmt> _result = new HashSet<Stmt>();
		final Collection<Stmt> _temp = new HashSet<Stmt>();
		final IWorkBag<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _wb = new LIFOWorkBag<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		_wb.addWork(monitor);

		while (_wb.hasWork()) {
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _wb.getWork();
			_temp.clear();
			_temp.addAll(MapUtils.getEmptyCollectionFromMap(monitor2stmts, _monitor));
			_result.addAll(_temp);
			CollectionUtils.filter(_temp, SootPredicatesAndTransformers.ENTER_MONITOR_STMT_PREDICATE);

			final Iterator<Stmt> _i = _temp.iterator();
			final int _iEnd = _temp.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Stmt _monitorStmt = _i.next();
				_wb.addAllWorkNoDuplicates(getMonitorTriplesFor(_monitorStmt, method));
			}
		}
		return _result;
	}

	/**
	 * Calculates the transitive closure of enclosing monitor for the given statement based in the given map.
	 * 
	 * @param stmt2monitors maps a statement to it's immediately enclosing intraprocedural monitor.
	 * @param stmt from which to initiate the closure.
	 * @return the contents of the closure.
	 * @pre map != null and stmt != null
	 * @post result != null
	 */
	private Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> calculateTransitiveClosureOfEnclosingMonitor(
			final Map<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> stmt2monitors, final Stmt stmt) {
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _result = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		final IWorkBag<Stmt> _wb = new LIFOWorkBag<Stmt>();
		_wb.addWork(stmt);

		while (_wb.hasWork()) {
			final Stmt _s = _wb.getWork();
			final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _monitors = MapUtils
					.getEmptyCollectionFromMap(stmt2monitors, _s);
			_result.addAll(_monitors);

			final Iterator<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _i = _monitors.iterator();
			final int _iEnd = _monitors.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _monitor = _i.next();
				_wb.addAllWorkNoDuplicates(getStmtsOfMonitor(_monitor));
			}
		}
		return _result;
	}

	/**
	 * Populates the work bag with work to proecess the successors of the given basic block.
	 * 
	 * @param workbag to populate.
	 * @param exitBasicBlock is the basic blocks whose successors need to be processed.
	 * @param enterStack is the stack of enter monitors.
	 * @param currStmts is collection of current statement matched to the topmost monitor.
	 * @pre workbag != null and exitBasicBlock != null nad enterStack != null and currStmts != null
	 */
	private void populateWorkBagToProcessSuccessors(
			final IWorkBag<Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>> workbag,
			final BasicBlock exitBasicBlock, final Stack<Pair<EnterMonitorStmt, Collection<Stmt>>> enterStack,
			final Collection<Stmt> currStmts) {
		final Collection<BasicBlock> _succs = exitBasicBlock.getSuccsOf();

		if (_succs.size() == 1) {
			final BasicBlock _succ = _succs.iterator().next();
			workbag
					.addWork(new Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>(
							_succ, _succ.getLeaderStmt(), enterStack, currStmts));
		} else {
			for (final Iterator<BasicBlock> _j = _succs.iterator(); _j.hasNext();) {
				final BasicBlock _succ = _j.next();
				final Stack<Pair<EnterMonitorStmt, Collection<Stmt>>> _clone = new Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>();

				for (final Iterator<Pair<EnterMonitorStmt, Collection<Stmt>>> _iter = enterStack.iterator(); _iter.hasNext();) {
					final Pair<EnterMonitorStmt, Collection<Stmt>> _p = _iter.next();
					_clone
							.add(new Pair<EnterMonitorStmt, Collection<Stmt>>(_p.getFirst(),
									new HashSet<Stmt>(_p.getSecond())));
				}
				workbag
						.addWork(new Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>(
								_succ, _succ.getLeaderStmt(), _clone, new HashSet<Stmt>(currStmts)));
			}
		}
	}

	/**
	 * Populates the work bag with work to precess the basic blocks containing the given exit montiors or their successors.
	 * 
	 * @param workbag to populate.
	 * @param exitMonitors is the collection of exit monitors whose basic block should be processed.
	 * @param enterStack is the stack of enter monitors.
	 * @param currStmts is collection of current statement matched to the topmost monitor.
	 * @param graph is the basic block graph.
	 * @pre workbag != null and exitMonitors != null and enterStack != null and currStmts != null and graph != null
	 */
	private void populateWorkBagWithSuccessorsOfNestedMonitor(
			final IWorkBag<Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>> workbag,
			final Collection<ExitMonitorStmt> exitMonitors, final Stack<Pair<EnterMonitorStmt, Collection<Stmt>>> enterStack,
			final Collection<Stmt> currStmts, final BasicBlockGraph graph) {
		for (final Iterator<ExitMonitorStmt> _k = exitMonitors.iterator(); _k.hasNext();) {
			final ExitMonitorStmt _exit = _k.next();
			final BasicBlock _exitBlock = graph.getEnclosingBlock(_exit);

			// as we are short circuiting, remember to add the exit monitor stmt of the monitor we just skipped to the
			// current statement list.
			final Collection<Stmt> _currStmtsClone = new HashSet<Stmt>(currStmts);
			_currStmtsClone.add(_exit);

			if (_exit == _exitBlock.getTrailerStmt()) {
				populateWorkBagToProcessSuccessors(workbag, _exitBlock, enterStack, _currStmtsClone);
			} else {
				final List<Stmt> _stmts = _exitBlock.getStmtsOf();
				final Stack<Pair<EnterMonitorStmt, Collection<Stmt>>> _clone = new Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>();
				_clone.addAll(enterStack);
				workbag
						.addWork(new Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>(
								_exitBlock, _stmts.get(_stmts.indexOf(_exit) + 1), _clone, _currStmtsClone));
			}
		}
	}

	/**
	 * Process the exit monitor to collect synchronization information.
	 * 
	 * @param method in which the exit monitor occurs.
	 * @param enter2exits maps enter monitors in the method to a collection of exit monitors.
	 * @param enterStack is the stack of enter monitors.
	 * @param currStmts is collection of current statement matched to the topmost monitor.
	 * @param exitMonitor is the exit monitor to be processed.
	 * @return a collection of statements enclosed in the monitor corresponding to <code>exitMonitor</code>.
	 * @pre method != null and enter2exits != null and enterStack != null and currStmts!= null and exitMonitor != null
	 */
	private Collection<Stmt> processExitMonitor(final SootMethod method,
			final Map<EnterMonitorStmt, Collection<ExitMonitorStmt>> enter2exits,
			final Stack<Pair<EnterMonitorStmt, Collection<Stmt>>> enterStack, final Collection<Stmt> currStmts,
			final ExitMonitorStmt exitMonitor) {
		final Pair<EnterMonitorStmt, Collection<Stmt>> _pair = enterStack.pop();
		final EnterMonitorStmt _enter = _pair.getFirst();
		final ExitMonitorStmt _exit = exitMonitor;
		Collection<Stmt> _result = currStmts;

		if (shouldCollectInfo(method, _enter, _exit)) {
			// add dependee information.
			final Map<Stmt, Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> _dent2ddees = MapUtils
					.getMapFromMap(method2enclosedStmts2monitors, method);
			final Map<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>, Collection<Stmt>> _ddee2ddents = MapUtils
					.getMapFromMap(method2monitor2enclosedStmts, method);

			// collect monitor triples
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _dependee = new Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>(
					_enter, _exit, method);
			monitorTriples.add(_dependee);

			for (final Iterator<Stmt> _k = currStmts.iterator(); _k.hasNext();) {
				final Stmt _curr = _k.next();
				MapUtils.putIntoCollectionInMap(_dent2ddees, _curr, _dependee);
			}

			// add enclosure information for enter and exit monitor
			MapUtils.putAllIntoCollectionInMap(_ddee2ddents, _dependee, currStmts);
			MapUtils.putIntoCollectionInMap(enter2exits, _enter, _exit);
			
			// load up the statements in the enclosing critical region for processing
			_result = _pair.getSecond();
			_result.add(_exit);
		}
		return _result;
	}

	/**
	 * Process the given monitor statement for enclosure information.
	 * 
	 * @param processedMonitors is the collection of monitors which have been processed.
	 * @param enterMonitor is the monitor to be processed.
	 * @param method in which <code>enterMonitor</code> occurs.
	 * @param enter2exits maps enter monitors in the method to a collection of exit monitors.
	 * @return the collection of monitors that were processed.
	 * @pre method != null and enter2exits != null and processedMonitors != null and enterMonitor != null
	 * @post result->forall(o | !enter2exits.get(o).isEmpty())
	 * @post result.contains(enterMonitor)
	 */
	private Collection<EnterMonitorStmt> processMonitor(final Collection<EnterMonitorStmt> processedMonitors,
			final EnterMonitorStmt enterMonitor, final SootMethod method,
			final Map<EnterMonitorStmt, Collection<ExitMonitorStmt>> enter2exits) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing stmt " + enterMonitor + " in method " + method);
		}

		final IWorkBag<Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>> _workbag = new HistoryAwareLIFOWorkBag<Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>>(
				new HashSet<Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>>());
		final BasicBlockGraph _bbGraph = getBasicBlockGraph(method);
		final Collection<EnterMonitorStmt> _enterMonitors = new HashSet<EnterMonitorStmt>();
		_enterMonitors.add(enterMonitor);
		_workbag.addWork(new Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>>(
				_bbGraph.getEnclosingBlock(enterMonitor), enterMonitor,
				new Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>(), new HashSet<Stmt>()));

		outerloop: do {
			final Quadraple<BasicBlock, Stmt, Stack<Pair<EnterMonitorStmt, Collection<Stmt>>>, Collection<Stmt>> _work = _workbag
					.getWork();
			final BasicBlock _bb = _work.getFirst();
			final Stmt _leadStmt = _work.getSecond();
			final Stack<Pair<EnterMonitorStmt, Collection<Stmt>>> _enterStack = _work.getThird();
			Collection<Stmt> _currStmts = _work.getFourth();
			boolean _skippingSuccs = false;

			for (final Iterator<Stmt> _j = _bb.getStmtsFrom(_leadStmt).iterator(); _j.hasNext();) {
				final Stmt _stmt = _j.next();

				/*
				 * We do not employ the usual "do-not-process-processed-statements" here as in the following case comm1 and
				 * exitmonitor will be processed before the second entermonitor is processed. bb1: if <cnd> goto bb2 else bb3
				 * bb2: entermonitor r1; goto bb4 bb3: entermontior r1; goto bb4 bb4: comm1; exitmonitor r1 Hence, we just
				 * process the statements and rely on the enterStack for validity.
				 */
				if (_stmt instanceof EnterMonitorStmt) {
					final EnterMonitorStmt _enter = (EnterMonitorStmt) _stmt;

					if (!processedMonitors.contains(_enter)) {
						// if the monitor was not processed, then hoist the monitor up for processing.
						_currStmts.add(_enter);
						_enterStack.push(new Pair<EnterMonitorStmt, Collection<Stmt>>(_enter, _currStmts));
						_currStmts = new HashSet<Stmt>();
						_enterMonitors.add(_enter);
					} else {
						final Collection<ExitMonitorStmt> _exits = MapUtils.getCollectionFromMap(enter2exits, _enter);

						// add the current monitor to the list of current statements
						_currStmts.add(_stmt);
						populateWorkBagWithSuccessorsOfNestedMonitor(_workbag, _exits, _enterStack, _currStmts, _bbGraph);
						_skippingSuccs = true;
						break;
					}
				} else if (_stmt instanceof ExitMonitorStmt) {
					_currStmts = processExitMonitor(method, enter2exits, _enterStack, _currStmts, (ExitMonitorStmt) _stmt);

					/*
					 * Although it seems that continuing processing the rest of the statements in the basic block seems like a
					 * efficient idea, but it fails in the following case if we start processing the monitor in bb2 bb1:
					 * entermonitor r1 bb2: entermonitor r2 bb3: exitmonitor r2 exitmonitor r1 Hence, it is safer to end local
					 * processing when the monitor stack is empty.
					 */
					if (_enterStack.isEmpty()) {
						continue outerloop;
					}
				} else {
					if (!_currStmts.add(_stmt) || (!_enterStack.isEmpty() && _enterStack.peek().getSecond().contains(_stmt))) {
						/*
						 * If the current statement was already seen then the path from hereon was traversed earlier. Hence,
						 * we can skip processing the successors.
						 */
						_skippingSuccs = true;
						break;
					}
				}
			}

			if (!(_skippingSuccs || _enterStack.isEmpty())) {
				// populate the workbag with work depending on the fanout number of the current basic block.
				populateWorkBagToProcessSuccessors(_workbag, _bb, _enterStack, _currStmts);
			}
		} while (_workbag.hasWork());
		return _enterMonitors;
	}

	/**
	 * Process the synchronized methods to collect the statements that are enclosed by the monitors at the entry/exit point of
	 * the method.
	 */
	private void processSyncedMethods() {
		for (final Iterator<SootMethod> _i = syncedMethods.iterator(); _i.hasNext();) {
			final SootMethod _sm = _i.next();
			final Collection<Stmt> _temp = getUnenclosedStmtsOf(_sm);

			if (!_temp.isEmpty()) {
				syncedMethod2enclosedStmts.put(_sm, new ArrayList<Stmt>(_temp));
			}
		}
		syncedMethods.clear();
	}

	/**
	 * Checks if the given monitor pair do indeed belong to a monitor.
	 * 
	 * @param method in which the monitor occurs.
	 * @param enter is the entry part of the monitor.
	 * @param exit is the exit part of the monitor.
	 * @return <code>true</code> if the given parts do indeed constitute a monitor; <code>false</code>, otherwise.
	 * @pre method != null and enter != null and exit != null
	 */
	private boolean shouldCollectInfo(final SootMethod method, final EnterMonitorStmt enter, final ExitMonitorStmt exit) {
		/*
		 * if the monitor object at the enter and exit statements contain atleast one common object then consider this pair,
		 * if not continue.
		 */
		final Type _enterType = enter.getOp().getType();
		final Type _exitType = exit.getOp().getType();
		boolean _result = Util.isSameOrSubType(_enterType, _exitType, ofa.getEnvironment())
				|| Util.isSameOrSubType(_exitType, _enterType, ofa.getEnvironment());

		if (!_result) {
			final Context _context = new Context();
			_context.setRootMethod(method);
			_context.setProgramPoint(enter.getOpBox());
			_context.setStmt(enter);

			final Collection<Value> _nValues = ofa.getValues(enter.getOp(), _context);
			_context.setProgramPoint(exit.getOpBox());
			_context.setStmt(exit);

			final Collection<Value> _xValues = ofa.getValues(exit.getOp(), _context);

			if (!(_xValues.isEmpty() || _nValues.isEmpty())) {
				_result = !SetUtils.intersection(_xValues, _nValues).isEmpty();
			}
		}
		return _result;
	}
}

// End of File
