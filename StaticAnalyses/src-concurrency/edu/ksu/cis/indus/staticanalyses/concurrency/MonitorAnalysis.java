
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
import edu.ksu.cis.indus.common.Constants;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Quadraple;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Type;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.Stmt;


/**
 * This class provides monitor information.  It provides
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class MonitorAnalysis
  extends AbstractAnalysis
  implements IMonitorInfo {
	/** 
	 * A predicate used to filter <code>EnterMonitorStmt</code>.
	 */
	public static final Predicate ENTER_MONITOR_STMT_PREDICATE =
		new Predicate() {
			public boolean evaluate(final Object obj) {
				return obj instanceof EnterMonitorStmt;
			}
		};

	/** 
	 * A predicate used to filter statements with invoke expressions. Filter expression is
	 * <code>((Stmt)o).containsInvokeExpr()</code>.
	 */
	public static final Predicate INVOKE_EXPR_PREDICATE =
		new Predicate() {
			public boolean evaluate(final Object object) {
				return ((Stmt) object).containsInvokeExpr();
			}
		};

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(MonitorAnalysis.class);

	/** 
	 * This is collection of monitor triples that occur in the analyzed system.  The elements of the triple are of type
	 * <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and <code>SootMethod</code>, respectively.
	 *
	 * @invariant monitorTriples.oclIsKindOf(Collection(Triple(EnterMonitorStmt, ExitMonitorStmt, SootMethod)))
	 * @invariant monitorTriples->forall(o | o.getFirst() == null and o.getSecond() == null implies
	 * 			  o.getThird().isSynchronized())
	 */
	final Collection monitorTriples = new HashSet();

	/** 
	 * This is a temporary collection of sychronized methods.
	 */
	final Collection syncedMethods = new HashSet();

	/** 
	 * This maps methods to a map from statements to the immediately enclosing monitor statements.
	 *
	 * @invariant method2enclosedStmts2monitors.oclIsKindOf(Map(SootMethod(Map(Stmt, Collection(Stmt)))))
	 */
	final Map method2enclosedStmts2monitors = new HashMap(Constants.getNumOfMethodsInApplication());

	/** 
	 * This collects the enter-monitor statements in each method during preprocessing.  This is used during analysis only.
	 *
	 * @invariant method2enterMonitors.oclIsKindOf(Map(SootMethod, Collection(EnterMonitorStmt)))
	 */
	final Map method2enterMonitors = new HashMap(Constants.getNumOfMethodsInApplication());

	/** 
	 * This maps methods to a map from monitor statements to the immediately enclosed statements.
	 *
	 * @invariant method2enclosedStmts2monitors.oclIsKindOf(Map(SootMethod(Map(Stmt, Collection(Stmt)))))
	 */
	final Map method2monitor2enclosedStmts = new HashMap(Constants.getNumOfMethodsInApplication());

	/** 
	 * This provides object flow information.
	 */
	private IValueAnalyzer ofa;

	/** 
	 * This maps synchronized methods to the collection of statements in them that are synchronization dependent on the entry
	 * and exit points of the method.
	 *
	 * @invariant syncedMethod2dependents.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private final Map syncedMethod2enclosedStmts = new HashMap();

	/** 
	 * The monitor graph.
	 */
	private IObjectDirectedGraph monitorGraph;

	/** 
	 * The pair manager.
	 */
	private PairManager pairMgr;

	/**
	 * Creates a new MonitorAnalysis object.
	 */
	public MonitorAnalysis() {
		preprocessor = new PreProcessor();
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
		 * Preprocesses the given method.  It records if the method is synchronized.
		 *
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(SootMethod)
		 */
		public void callback(final SootMethod method) {
			if (method.isSynchronized()) {
				final Triple _triple = new Triple(null, null, method);
				_triple.optimize();
				monitorTriples.add(_triple);
				syncedMethods.add(method);
			}
		}

		/**
		 * Preprocesses the given stmt.  It records if the <code>stmt</code> is an enter/exit monitor statement.
		 *
		 * @param stmt is the enter/exit monitor statement.
		 * @param context in which <code>stmt</code> occurs.  This contains the method that encloses <code>stmt</code>.
		 *
		 * @pre stmt.isOclTypeOf(EnterMonitorStmt)
		 * @pre context.getCurrentMethod() != null
		 *
		 * @see edu.ksu.cis.indus.processing.IProcessor#callback(Stmt,Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
			if (stmt instanceof EnterMonitorStmt) {
				CollectionsUtilities.putIntoSetInMap(method2enterMonitors, context.getCurrentMethod(), stmt);
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
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosedStmts(edu.ksu.cis.indus.common.datastructures.Triple,
	 * 		boolean)
	 */
	public Collection getEnclosedStmts(final Triple monitorTriple, final boolean transitive) {
		final Collection _result = new HashSet();
		final EnterMonitorStmt _enterMonitorStmt = (EnterMonitorStmt) monitorTriple.getFirst();
		final SootMethod _sm = (SootMethod) monitorTriple.getThird();

		if (_enterMonitorStmt == null) {
			if (!syncedMethods.isEmpty()) {
				processSyncedMethods();
			}

			_result.addAll((Collection) MapUtils.getObject(syncedMethod2enclosedStmts, _sm, Collections.EMPTY_LIST));

			if (transitive && !_result.isEmpty()) {
				final Map _monitor2enclosedStmts = (Map) method2monitor2enclosedStmts.get(_sm);
				final Collection _temp = new HashSet();
				_temp.addAll(_result);
				CollectionUtils.filter(_temp, ENTER_MONITOR_STMT_PREDICATE);

				final Iterator _i = _temp.iterator();
				final int _iEnd = _temp.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final Stmt _monitorStmt = (Stmt) _i.next();
					final Collection _monitorTriplesFor = getMonitorTriplesFor(_monitorStmt, _sm);
					final Iterator _j = _monitorTriplesFor.iterator();
					final int _jEnd = _monitorTriplesFor.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Triple _monitor = (Triple) _j.next();
						_result.addAll(calculateTransitiveClosureOfEnclosedStmts(_monitor2enclosedStmts, _monitor, _sm));
					}
				}
			}
		} else {
			final Map _monitor2enclosedStmts =
				(Map) MapUtils.getObject(method2monitor2enclosedStmts, _sm, Collections.EMPTY_MAP);

			if (!_monitor2enclosedStmts.isEmpty()) {
				if (transitive) {
					_result.addAll(calculateTransitiveClosureOfEnclosedStmts(_monitor2enclosedStmts, monitorTriple, _sm));
				} else {
					_result.addAll((Collection) MapUtils.getObject(_monitor2enclosedStmts, monitorTriple,
							Collections.EMPTY_LIST));
				}
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosingMonitorStmts(soot.jimple.Stmt, soot.SootMethod, boolean)
	 */
	public Collection getEnclosingMonitorStmts(final Stmt stmt, final SootMethod method, final boolean transitive) {
		final Collection _result = new HashSet();
		final Collection _temp = getEnclosingMonitorTriples(stmt, method, transitive);
		final Iterator _i = _temp.iterator();
		final int _iEnd = _temp.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple _monitor = (Triple) _i.next();
			final Object _enter = _monitor.getFirst();

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
	public Collection getEnclosingMonitorTriples(final Stmt stmt, final SootMethod method, final boolean transitive) {
		final Collection _result = new HashSet();
		final Map _enclosedStmt2monitors =
			(Map) MapUtils.getObject(method2enclosedStmts2monitors, method, Collections.EMPTY_MAP);

		if (_enclosedStmt2monitors.size() > 0) {
			if (transitive) {
				_result.addAll(calculateTransitiveClosureOfEnclosingMonitor(_enclosedStmt2monitors, stmt));
			} else {
				_result.addAll((Collection) MapUtils.getObject(_enclosedStmt2monitors, stmt, Collections.EMPTY_LIST));
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getId()
	 */
	public Object getId() {
		return IMonitorInfo.ID;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getInterProcedurallyEnclosedStmts(Triple,     boolean,
	 * 		edu.ksu.cis.indus.interfaces.ICallGraphInfo)
	 */
	public Map getInterProcedurallyEnclosedStmts(final Triple monitorTriple, final boolean transitive,
		final ICallGraphInfo callgraph) {
		final Map _result;
		final Stmt _stmt = (Stmt) monitorTriple.getFirst();
		final SootMethod _sootMethod = (SootMethod) monitorTriple.getThird();

		if (_stmt == null) {
			_result = getInterProcedurallyEnclosedStmts(_sootMethod, transitive, callgraph);
		} else {
			_result = getInterProcedurallyEnclosedStmts(_stmt, _sootMethod, transitive, callgraph);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getInterProcedurallyEnclosingMonitorStmts(soot.jimple.Stmt,
	 * 		soot.SootMethod,     boolean, edu.ksu.cis.indus.interfaces.ICallGraphInfo)
	 */
	public Map getInterProcedurallyEnclosingMonitorStmts(final Stmt stmt, final SootMethod method, final boolean transitive,
		final ICallGraphInfo callgraph) {
		final Map _result = new HashMap();
		final Map _method2monitors = getInterProcedurallyEnclosingMonitorTriples(stmt, method, transitive, callgraph);
		final Iterator _i = _method2monitors.entrySet().iterator();
		final int _iEnd = _method2monitors.entrySet().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Collection _monitors = (Collection) _entry.getValue();
			final Collection _temp = new HashSet();
			final Iterator _j = _monitors.iterator();
			final int _jEnd = _monitors.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Triple _monitor = (Triple) _j.next();
				_temp.addAll(getStmtsOfMonitor(_monitor));
			}
			_result.put(_entry.getKey(), _temp);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getInterProcedurallyEnclosingMonitorTriples(soot.jimple.Stmt,
	 * 		soot.SootMethod, boolean, edu.ksu.cis.indus.interfaces.ICallGraphInfo)
	 */
	public Map getInterProcedurallyEnclosingMonitorTriples(final Stmt stmt, final SootMethod method,
		final boolean transitive, final ICallGraphInfo callgraph) {
		final Map _result = new HashMap();
		_result.put(method, getEnclosingMonitorTriples(stmt, method, transitive));

		if (transitive) {
			final IWorkBag _wb = new HistoryAwareFIFOWorkBag(new HashSet());
			_wb.addAllWork(callgraph.getCallers(method));

			while (_wb.hasWork()) {
				final CallTriple _ctrp = (CallTriple) _wb.getWork();
				final SootMethod _caller = _ctrp.getMethod();
				_result.put(_caller, getEnclosingMonitorTriples(_ctrp.getStmt(), _caller, transitive));
				_wb.addAllWorkNoDuplicates(callgraph.getCallers(_caller));
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorGraph(edu.ksu.cis.indus.interfaces.ICallGraphInfo)
	 */
	public IObjectDirectedGraph getMonitorGraph(final ICallGraphInfo callgraphInfo) {
		if (monitorGraph == null) {
			final SimpleNodeGraph _result = new SimpleNodeGraph();
			final Collection _temp = new HashSet();
			final IWorkBag _wb = new HistoryAwareFIFOWorkBag(_temp);
			final Iterator _i = monitorTriples.iterator();
			final int _iEnd = monitorTriples.size();

			// we use a backward search on the call graph to construct the monitor graph.
			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Triple _monitor = (Triple) _i.next();
				final INode _dest = _result.getNode(_monitor);
				final Stmt _stmt = (Stmt) _monitor.getFirst();
				final SootMethod _method = (SootMethod) _monitor.getThird();
				final Collection _enclosingMonitors = new HashSet();

				if (_stmt != null) {
					_enclosingMonitors.addAll(getEnclosingMonitorTriples(_stmt, _method, false));
				}

				if (!_enclosingMonitors.isEmpty()) {
					final Iterator _j = _enclosingMonitors.iterator();
					final int _jEnd = _enclosingMonitors.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Triple _t = (Triple) _j.next();
						final INode _src = _result.getNode(_t);
						_result.addEdgeFromTo(_src, _dest);
					}
				} else {
					_temp.clear();
					_wb.clear();
					_wb.addAllWork(callgraphInfo.getCallers(_method));

					while (_wb.hasWork()) {
						final CallTriple _ctrp = (CallTriple) _wb.getWork();
						final SootMethod _sm = _ctrp.getMethod();
						final Collection _mons = getEnclosingMonitorTriples(_ctrp.getStmt(), _sm, false);

						if (_mons.isEmpty()) {
							_wb.addAllWorkNoDuplicates(callgraphInfo.getCallers(_sm));
						} else {
							_enclosingMonitors.addAll(_mons);
						}
					}

					if (!_enclosingMonitors.isEmpty()) {
						final Iterator _j = _enclosingMonitors.iterator();
						final int _jEnd = _enclosingMonitors.size();

						for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
							final Triple _t = (Triple) _j.next();
							final INode _src = _result.getNode(_t);
							_result.addEdgeFromTo(_src, _dest);
						}
					}
				}
			}
			monitorGraph = _result;
		}
		return monitorGraph;
	}

	/**
	 * Returns the monitors that occur in the analyzed system.
	 *
	 * @return a collection of <code>Triples</code>.
	 *
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorTriples()
	 */
	public Collection getMonitorTriples() {
		return Collections.unmodifiableCollection(monitorTriples);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorTriplesFor(soot.jimple.Stmt, SootMethod)
	 */
	public Collection getMonitorTriplesFor(final Stmt monitorStmt, final SootMethod method) {
		final Collection _result = new HashSet();
		final Collection _monitorTriplesInMethod = getMonitorTriplesIn(method);
		final Iterator _i = _monitorTriplesInMethod.iterator();
		final int _iEnd = _monitorTriplesInMethod.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple _triple = (Triple) _i.next();

			final Object _enter = _triple.getFirst();
			final Object _exit = _triple.getSecond();

			if (_triple.getThird().equals(method)
				  && ((_enter != null && _enter.equals(monitorStmt)) || (_exit != null && _exit.equals(monitorStmt)))) {
				_result.add(_triple);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorTriplesIn(SootMethod)
	 */
	public Collection getMonitorTriplesIn(final SootMethod method) {
		final Collection _result = new HashSet();
		final Iterator _i = monitorTriples.iterator();
		final int _iEnd = monitorTriples.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple _triple = (Triple) _i.next();

			if (_triple.getThird().equals(method)) {
				_result.add(_triple);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getStmtsOfMonitor(edu.ksu.cis.indus.common.datastructures.Triple)
	 */
	public Collection getStmtsOfMonitor(final Triple monitor) {
		final Collection _result;
		final Stmt _monitorStmt = (Stmt) monitor.getFirst();

		if (_monitorStmt != null) {
			_result = new HashSet();
			_result.add(_monitorStmt);

			for (final Iterator _i = getMonitorTriplesFor(_monitorStmt, (SootMethod) monitor.getThird()).iterator();
				  _i.hasNext();) {
				final Triple _monitor = (Triple) _i.next();
				_result.add(_monitor.getSecond());
			}
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getUnenclosedStmtsOf(soot.SootMethod)
	 */
	public Collection getUnenclosedStmtsOf(final SootMethod method) {
		final Collection _enclosedStmts =
			((Map) MapUtils.getObject(method2enclosedStmts2monitors, method, Collections.EMPTY_MAP)).keySet();
		final Predicate _predicate =
			new Predicate() {
				public boolean evaluate(final Object o) {
					return !_enclosedStmts.contains(o);
				}
			};

		final Collection _result =
			IteratorUtils.toList(IteratorUtils.filteredIterator(getUnitGraph(method).iterator(), _predicate));
		return _result;
	}

	/**
	 * Calculates the synchronization dependency information for the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Monitor Analysis processing");
		}

		final Map _enter2exits = new HashMap();
		final Collection _processedMonitors = new HashSet();

		/*
		 * Calculating monitor info  is not as simple as it looks in the presence of exceptions. The exit monitors are the
		 * tricky ones.  The exit monitors are guarded by catch block generated by the compiler.  The user cannot generate
		 * such complex catch blocks, but nevertheless we cannot assume about the compiler. So, we proceed by calculating the
		 * monitor info over a complete unit graph and use object flow information to arbitrate suspicious enter-exit
		 * matches.
		 */
		for (final Iterator _i = method2enterMonitors.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final SootMethod _method = (SootMethod) _entry.getKey();
			_enter2exits.clear();

			for (final Iterator _j = ((Collection) _entry.getValue()).iterator(); _j.hasNext();) {
				final Stmt _enterMonitor = (Stmt) _j.next();
				final Map _monitor2enclosedStmts = CollectionsUtilities.getMapFromMap(method2monitor2enclosedStmts, _method);

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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		monitorTriples.clear();
		syncedMethods.clear();
		syncedMethod2enclosedStmts.clear();
		method2enclosedStmts2monitors.clear();
		method2monitor2enclosedStmts.clear();
		method2enterMonitors.clear();
		monitorGraph = null;
	}

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Monitor Analysis as calculated by " + getClass().getName() + "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = method2enclosedStmts2monitors.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_localEdgeCount = 0;

			final SootMethod _method = (SootMethod) _entry.getKey();

			for (final Iterator _j = ((Map) _entry.getValue()).entrySet().iterator(); _j.hasNext();) {
				final Map.Entry _entry2 = (Map.Entry) _j.next();
				final Object _dependent = _entry2.getKey();

				for (final Iterator _k = ((Collection) _entry2.getValue()).iterator(); _k.hasNext();) {
					final Object _obj = _k.next();
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

		for (final Iterator _i = method2monitor2enclosedStmts.values().iterator(); _i.hasNext();) {
			for (final Iterator _j = ((Map) _i.next()).values().iterator(); _j.hasNext();) {
				_edgeCount2 += ((Collection) _j.next()).size();
			}
		}
		_result.append("A total of " + _edgeCount + "/" + _edgeCount2 + " enclosures exist.\n");
		_result.append("MonitorInfo follows:\n");

		for (final Iterator _i = monitorTriples.iterator(); _i.hasNext();) {
			final Triple _trip = (Triple) _i.next();

			if (_trip.getFirst() != null) {
				_result.append("[" + _trip.getFirst() + " " + _trip.getFirst().hashCode() + ", " + _trip.getSecond() + " "
					+ _trip.getSecond().hashCode() + "] occurs in " + _trip.getThird() + "\n");
			} else {
				_result.append(_trip.getThird() + " is synchronized.\n");
			}
		}
		return _result.toString();
	}

	///CLOVER:ON

	/**
	 * {@inheritDoc}
	 *
	 * @throws InitializationException when object flow analysis is not provided.
	 *
	 * @pre info.get(OFAnalyzer.ID) != null and info.get(OFAnalyzer.ID).oclIsTypeOf(OFAnalyzer)
	 * @pre info.get(PairManager.ID) != null and info.get(PairManager.ID).oclIsTypeOf(PairManager)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
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
	 * Retrieves the statements enclosed in the monitor of the synchronized method.  This retrieves inter procedural
	 * enclosures.
	 *
	 * @param method of interest.
	 * @param transitive <code>true</code> if transitive closure is required; <code>false</code>, otherwise.
	 * @param callgraph to be used.
	 *
	 * @return a map from methods to statements in them that occur in the enclosure.
	 *
	 * @pre method != null and callgraph != null
	 * @post result != null and result.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private Map getInterProcedurallyEnclosedStmts(final SootMethod method, final boolean transitive,
		final ICallGraphInfo callgraph) {
		final Collection _intraStmts = getEnclosedStmts(new Triple(null, null, method), transitive);
		final Map _method2stmts = new HashMap();
		_method2stmts.put(method, _intraStmts);

		final Iterator _stmtsWithInvokeExpr = IteratorUtils.filteredIterator(_intraStmts.iterator(), INVOKE_EXPR_PREDICATE);
		calculateInterprocedurallyEnclosedStmts(method, transitive, callgraph, _method2stmts, _stmtsWithInvokeExpr);
		return _method2stmts;
	}

	/**
	 * Retrieves the statements enclosed in the monitor of the synchronized method.  This retrieves inter procedural
	 * enclosures.
	 *
	 * @param monitorStmt of interest.
	 * @param method of interest.
	 * @param transitive <code>true</code> if transitive closure is required; <code>false</code>, otherwise.
	 * @param callgraph to be used.
	 *
	 * @return a map from methods to statements in them that occur in the enclosure.
	 *
	 * @pre method != null and callgraph != null and monitorStmt != null
	 * @pre monitorStmt.oclIsKindOf(EnterMonitorStmt) or monitorStmt.oclIsKindOf(ExitMonitorStmt)
	 * @post result != null and result.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private Map getInterProcedurallyEnclosedStmts(final Stmt monitorStmt, final SootMethod method, final boolean transitive,
		final ICallGraphInfo callgraph) {
		final Collection _intraStmts = new HashSet();
		final Collection _monitorTriplesFor = getMonitorTriplesFor(monitorStmt, method);
		final Iterator _i = _monitorTriplesFor.iterator();
		final int _iEnd = _intraStmts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Triple _monitor = (Triple) _i.next();
			_intraStmts.addAll(getStmtsOfMonitor(_monitor));
		}

		final Map _method2stmts = new HashMap();
		_method2stmts.put(method, _intraStmts);

		final Iterator _stmtsWithInvokeExpr = IteratorUtils.filteredIterator(_intraStmts.iterator(), INVOKE_EXPR_PREDICATE);
		calculateInterprocedurallyEnclosedStmts(method, transitive, callgraph, _method2stmts, _stmtsWithInvokeExpr);
		return _method2stmts;
	}

	/**
	 * This helps <code>getInterProcedurallyEnclosedStmts</code> methods to calculate enclosures.
	 *
	 * @param method of interest.
	 * @param transitive <code>true</code> if transitive closure is required; <code>false</code>, otherwise.
	 * @param callgraph to be used.
	 * @param method2stmts an out parameter in which each method will be mapped to a set of statements.
	 * @param stmtsWithInvokeExpr is an iterator over statements with invoke expresssions.
	 *
	 * @pre method != null and callgraph != null
	 * @pre methdo2stmts != null
	 * @pre stmtsWithInvokeExpr != null
	 * @post method2stmts.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private void calculateInterprocedurallyEnclosedStmts(final SootMethod method, final boolean transitive,
		final ICallGraphInfo callgraph, final Map method2stmts, final Iterator stmtsWithInvokeExpr) {
		final Context _context = new Context();
		final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());

		for (final Iterator _i = stmtsWithInvokeExpr; _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();
			_wb.addWork(pairMgr.getPair(_stmt, method));
		}

		while (_wb.hasWork()) {
			final Pair _pair = (Pair) _wb.getWork();
			final Stmt _stmt = (Stmt) _pair.getFirst();
			final SootMethod _caller = (SootMethod) _pair.getSecond();
			_context.setStmt(_stmt);
			_context.setRootMethod(_caller);

			final Collection _callees = callgraph.getCallees(_stmt.getInvokeExpr(), _context);
			final Iterator _i = _callees.iterator();
			final int _iEnd = _callees.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootMethod _callee = (SootMethod) _i.next();

				if (!_callee.isSynchronized() || (_callee.isSynchronized() && transitive)) {
					final Collection _stmts = getUnenclosedStmtsOf(_callee);
					CollectionsUtilities.putAllIntoSetInMap(method2stmts, _callee, _stmts);

					for (final Iterator _j = IteratorUtils.filteredIterator(_stmts.iterator(), INVOKE_EXPR_PREDICATE);
						  _j.hasNext();) {
						final Stmt _s = (Stmt) _j.next();
						_wb.addWork(pairMgr.getPair(_s, _callee));
					}
				}
			}
		}
	}

	/**
	 * Calculates the transitive closure of enclosing monitor for the given statement based in the given map.
	 *
	 * @param monitor2stmts maps a statement to it's immediately enclosing intraprocedural monitor.
	 * @param monitor from which to initiate the closure.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return the contents of the closure.
	 *
	 * @pre map != null and stmt != null and method != null
	 * @pre map.oclIsKindOf(Map(Triple(EnterMonitorStmt, ExitMonitorStmt, SootMethod), Collection(Stmt)))
	 * @post result != null and result.oclIsKindOf(Collection(Triple(EnterMonitorStmt, ExitMonitorStmt, SootMethod)))
	 */
	private Collection calculateTransitiveClosureOfEnclosedStmts(final Map monitor2stmts, final Triple monitor,
		final SootMethod method) {
		final Collection _result = new HashSet();
		final Collection _temp = new HashSet();
		final IWorkBag _wb = new LIFOWorkBag();
		_wb.addWork(monitor);

		while (_wb.hasWork()) {
			final Triple _monitor = (Triple) _wb.getWork();
			_temp.clear();
			_temp.addAll((Collection) CollectionsUtilities.getFromMap(monitor2stmts, _monitor,
					CollectionsUtilities.EMPTY_LIST_FACTORY));
			_result.addAll(_temp);
			CollectionUtils.filter(_temp, ENTER_MONITOR_STMT_PREDICATE);

			final Iterator _i = _temp.iterator();
			final int _iEnd = _temp.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Stmt _monitorStmt = (Stmt) _i.next();
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
	 *
	 * @return the contents of the closure.
	 *
	 * @pre map != null and stmt != null
	 * @pre map.oclIsKindOf(Map(Stmt, Collection(Triple(EnterMonitorStmt, ExitMonitorStmt, SootMethod))))
	 * @post result != null and result.oclIsKindOf(Collection(Triple(EnterMonitorStmt, ExitMonitorStmt, SootMethod)))
	 */
	private Collection calculateTransitiveClosureOfEnclosingMonitor(final Map stmt2monitors, final Stmt stmt) {
		final Collection _result = new HashSet();
		final IWorkBag _wb = new LIFOWorkBag();
		_wb.addWork(stmt);

		while (_wb.hasWork()) {
			final Stmt _s = (Stmt) _wb.getWork();
			final Collection _monitors =
				(Collection) CollectionsUtilities.getFromMap(stmt2monitors, _s, CollectionsUtilities.EMPTY_LIST_FACTORY);
			_result.add(_monitors);

			final Iterator _i = _monitors.iterator();
			final int _iEnd = _monitors.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Triple _monitor = (Triple) _i.next();
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
	 *
	 * @pre workbag != null and exitBasicBlock != null nad enterStack != null and currStmts != null
	 * @pre enterStack.oclIsKindOf(Sequence(Pair(EnterMonitorStmt, Collection(Stmt))))
	 * @pre currStmts.oclIsKindOf(Collection(Stmt))
	 */
	private void populateWorkBagToProcessSuccessors(final IWorkBag workbag, final BasicBlock exitBasicBlock,
		final Stack enterStack, final Collection currStmts) {
		final Collection _succs = exitBasicBlock.getSuccsOf();

		if (_succs.size() == 1) {
			final BasicBlock _succ = (BasicBlock) _succs.iterator().next();
			workbag.addWork(new Quadraple(_succ, _succ.getLeaderStmt(), enterStack, currStmts));
		} else {
			for (final Iterator _j = _succs.iterator(); _j.hasNext();) {
				final BasicBlock _succ = (BasicBlock) _j.next();
				final Stack _clone = new Stack();

				for (final Iterator _iter = enterStack.iterator(); _iter.hasNext();) {
					final Pair _p = (Pair) _iter.next();
					_clone.add(new Pair(_p.getFirst(), ((HashSet) _p.getSecond()).clone()));
				}
				workbag.addWork(new Quadraple(_succ, _succ.getLeaderStmt(), _clone, ((HashSet) currStmts).clone()));
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
	 *
	 * @pre workbag != null and exitMonitors != null and enterStack != null and currStmts != null and graph != null
	 * @pre enterStack.oclIsKindOf(Sequence(Pair(EnterMonitorStmt, Collection(Stmt))))
	 * @pre currStmts.oclIsKindOf(Collection(Stmt))
	 */
	private void populateWorkBagWithSuccessorsOfNestedMonitor(final IWorkBag workbag, final Collection exitMonitors,
		final Stack enterStack, final Collection currStmts, final BasicBlockGraph graph) {
		for (final Iterator _k = exitMonitors.iterator(); _k.hasNext();) {
			final Stmt _exit = (Stmt) _k.next();
			final BasicBlock _exitBlock = graph.getEnclosingBlock(_exit);

			// as we are short circuiting, remember to add the exit monitor stmt of the monitor we just skipped to the 
			// current statement list.
			final Collection _currStmtsClone = (Collection) ((HashSet) currStmts).clone();
			_currStmtsClone.add(_exit);

			if (_exit == _exitBlock.getTrailerStmt()) {
				populateWorkBagToProcessSuccessors(workbag, _exitBlock, enterStack, _currStmtsClone);
			} else {
				final List _stmts = _exitBlock.getStmtsOf();
				workbag.addWork(new Quadraple(_exitBlock, _stmts.get(_stmts.indexOf(_exit) + 1), enterStack.clone(),
						_currStmtsClone));
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
	 *
	 * @return a collection of statements enclosed in the monitor corresponding to <code>exitMonitor</code>.
	 *
	 * @pre method != null and enter2exits != null and enterStack != null and currStmts!= null and exitMonitor != null
	 * @pre enter2exits.oclIsKindOf(Map(EnterMonitorStmt, Collection(ExitMonitorStmt)))
	 * @pre enterStack.oclIsKindOf(Sequence(Pair(EnterMonitorStmt, Collection(Stmt))))
	 * @pre currStmts.oclIsKindOf(Collection(Stmt))
	 */
	private Collection processExitMonitor(final SootMethod method, final Map enter2exits, final Stack enterStack,
		final Collection currStmts, final Stmt exitMonitor) {
		final Pair _pair = (Pair) enterStack.pop();
		final EnterMonitorStmt _enter = (EnterMonitorStmt) _pair.getFirst();
		final ExitMonitorStmt _exit = (ExitMonitorStmt) exitMonitor;
		Collection _result = currStmts;

		if (shouldCollectInfo(method, _enter, _exit)) {
			// add dependee information.
			final Map _dent2ddees = CollectionsUtilities.getMapFromMap(method2enclosedStmts2monitors, method);
			final Map _ddee2ddents = CollectionsUtilities.getMapFromMap(method2monitor2enclosedStmts, method);

			// collect monitor triples
			final Triple _dependee = new Triple(_enter, _exit, method);
			monitorTriples.add(_dependee);

			for (final Iterator _k = currStmts.iterator(); _k.hasNext();) {
				final Stmt _curr = (Stmt) _k.next();
				CollectionsUtilities.putIntoSetInMap(_dent2ddees, _curr, _dependee);
			}

			// add enclosure information for enter and exit monitor
			CollectionsUtilities.putAllIntoSetInMap(_ddee2ddents, _dependee, currStmts);

			CollectionsUtilities.putIntoSetInMap(enter2exits, _enter, _exit);
			// load up the statements in the enclosing critical region for processing
			_result = (HashSet) _pair.getSecond();
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
	 *
	 * @return the collection of monitors that were processed.
	 *
	 * @pre method != null and enter2exits != null and processedMonitors != null and enterMonitor != null
	 * @pre enter2exits.oclIsKindOf(Map(EnterMonitorStmt, Collection(ExitMonitorStmt)))
	 * @pre processedMonitors.oclIsKindOf(Collection(EnterMonitorStmt))
	 * @post result.oclIsKindOf(Collection(EnterMonitorStmt))
	 * @post result->forall(o | !enter2exits.get(o).isEmpty())
	 * @post result.contains(enterMonitor)
	 */
	private Collection processMonitor(final Collection processedMonitors, final Stmt enterMonitor, final SootMethod method,
		final Map enter2exits) {
		final IWorkBag _workbag = new HistoryAwareLIFOWorkBag(new HashSet());
		final BasicBlockGraph _bbGraph = getBasicBlockGraph(method);
		final Collection _enterMonitors = new HashSet();
		_enterMonitors.add(enterMonitor);
		_workbag.addWork(new Quadraple(_bbGraph.getEnclosingBlock(enterMonitor), enterMonitor, new Stack(), new HashSet()));

outerloop: 
		do {
			final Quadraple _work = (Quadraple) _workbag.getWork();
			final BasicBlock _bb = (BasicBlock) _work.getFirst();
			final Stmt _leadStmt = (Stmt) _work.getSecond();
			final Stack _enterStack = (Stack) _work.getThird();
			Collection _currStmts = (HashSet) _work.getFourth();
			boolean _skippingSuccs = false;

			for (final Iterator _j = _bb.getStmtsFrom(_leadStmt).iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();

				/*
				 * We do not employ the usual "do-not-process-processed-statements" here as in the following case comm1 and
				 * exitmonitor will be processed before the second entermonitor is processed.
				 * bb1: if <cnd> goto bb2 else bb3
				 * bb2: entermonitor r1; goto bb4
				 * bb3: entermontior r1; goto bb4
				 * bb4: comm1; exitmonitor r1
				 * Hence, we just process the statements and rely on the enterStack for validity.
				 */
				if (_stmt instanceof EnterMonitorStmt) {
					final Stmt _enter = (EnterMonitorStmt) _stmt;

					if (!processedMonitors.contains(_enter)) {
						// if the monitor was not processed, then hoist the monitor up for processing.
						_currStmts.add(_enter);
						_enterStack.push(new Pair(_enter, _currStmts));
						_currStmts = new HashSet();
						_enterMonitors.add(_enter);
					} else {
						final Collection _exits = CollectionsUtilities.getSetFromMap(enter2exits, _enter);

						// add the current monitor to the list of current statements
						_currStmts.add(_stmt);
						populateWorkBagWithSuccessorsOfNestedMonitor(_workbag, _exits, _enterStack, _currStmts, _bbGraph);
						_skippingSuccs = true;
						break;
					}
				} else if (_stmt instanceof ExitMonitorStmt) {
					_currStmts = processExitMonitor(method, enter2exits, _enterStack, _currStmts, _stmt);

					/*
					 * Although it seems that continuing processing the rest of the statements in the basic block seems like
					 * a efficient idea, but it fails in the following case if we start processing the monitor in bb2
					 * bb1: entermonitor r1
					 * bb2: entermonitor r2
					 * bb3: exitmonitor r2
					 *      exitmonitor r1
					 * Hence, it is safer to end local processing when the monitor stack is empty.
					 */
					if (_enterStack.isEmpty()) {
						continue outerloop;
					}
				} else {
					_currStmts.add(_stmt);
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
	 * Process the synchronized methods to collect the statements that are enclosed by the monitors at the entry/exit point
	 * of the method.
	 */
	private void processSyncedMethods() {
		for (final Iterator _i = syncedMethods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final Collection _temp = getUnenclosedStmtsOf(_sm);

			if (!_temp.isEmpty()) {
				syncedMethod2enclosedStmts.put(_sm, new ArrayList(_temp));
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
	 *
	 * @return <code>true</code> if the given parts do indeed constitute a monitor; <code>false</code>, otherwise.
	 *
	 * @pre method != null and enter != null and exit != null
	 */
	private boolean shouldCollectInfo(final SootMethod method, final EnterMonitorStmt enter, final ExitMonitorStmt exit) {
		/*
		 * if the monitor object at the enter and exit statements contain atleast one common object then
		 * consider this pair, if not continue.
		 */
		final Type _enterType = enter.getOp().getType();
		final Type _exitType = exit.getOp().getType();
		boolean _result =
			Util.isSameOrSubType(_enterType, _exitType, ofa.getEnvironment())
			  || Util.isSameOrSubType(_exitType, _enterType, ofa.getEnvironment());

		if (!_result) {
			final Context _context = new Context();
			_context.setRootMethod(method);
			_context.setProgramPoint(enter.getOpBox());
			_context.setStmt(enter);

			final Collection _nValues = ofa.getValues(enter.getOp(), _context);
			_context.setProgramPoint(exit.getOpBox());
			_context.setStmt(exit);

			final Collection _xValues = ofa.getValues(exit.getOp(), _context);

			if (!(_xValues.isEmpty() || _nValues.isEmpty())) {
				_result = !CollectionUtils.intersection(_xValues, _nValues).isEmpty();
			}
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2004/08/06 14:53:12  venku
   - a subtle error while adding elements to collection. FIXED.

   Revision 1.5  2004/08/02 07:33:45  venku
   - small but significant change to the pair manager.
   - ripple effect.

   Revision 1.4  2004/07/27 11:07:20  venku
   - updated project to use safe lock analysis.
   Revision 1.3  2004/07/27 07:08:25  venku
   - revamped IMonitorInfo interface.
   - ripple effect in MonitorAnalysis, SafeLockAnalysis, and SychronizationDA.
   - deleted WaitNotifyAnalysis
   - ripple effect in EquivalenceClassBasedEscapeAnalysis.
   Revision 1.2  2004/07/25 10:27:27  venku
   - extended MonitorInfo interface with convenience methods.
   - implemented the above methods in MonitorAnalysis.
   Revision 1.1  2004/07/23 13:09:44  venku
   - Refactoring in progress.
     - Extended IMonitorInfo interface.
     - Teased apart the logic to calculate monitor info from SynchronizationDA
       into MonitorAnalysis.
     - Casted EquivalenceClassBasedEscapeAnalysis as an AbstractAnalysis.
     - ripple effect.
     - Implemented safelock analysis to handle intraprocedural processing.
 */
