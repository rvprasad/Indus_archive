
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
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Quadraple;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.IDirectedGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

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
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(MonitorAnalysis.class);

	/** 
	 * A predicate used to filter <code>EnterMonitorStmt</code>.
	 */
	private static final Predicate ENTER_MONITOR_STMT_PREDICATE =
		new Predicate() {
			public boolean evaluate(final Object obj) {
				return obj instanceof EnterMonitorStmt;
			}
		};

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
	final Map method2enclosedStmts2monitors = new HashMap();

	/** 
	 * This collects the enter-monitor statements in each method during preprocessing.
	 *
	 * @invariant method2enterMonitors.oclIsKindOf(Map(SootMethod, Collection(EnterMonitorStmt)))
	 */
	final Map method2enterMonitors = new HashMap();

	/** 
	 * This maps methods to a map from monitor statements to the immediately enclosed statements.
	 *
	 * @invariant method2enclosedStmts2monitors.oclIsKindOf(Map(SootMethod(Map(Stmt, Collection(Stmt)))))
	 */
	final Map method2monitor2encloseStmts = new HashMap();

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
	  extends AbstractValueAnalyzerBasedProcessor {
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
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosedStmts(soot.jimple.Stmt, SootMethod, boolean)
	 */
	public Collection getEnclosedStmts(final Stmt monitorStmt, final SootMethod method, final boolean transitive) {
		final Collection _result = new HashSet();

		final Map _map = (Map) MapUtils.getObject(method2monitor2encloseStmts, method, Collections.EMPTY_MAP);

		if (_map.size() > 0) {
			if (transitive) {
				_result.addAll(calculateTransitiveClosure(_map, monitorStmt));
			} else {
				_result.addAll((Collection) MapUtils.getObject(_map, monitorStmt, Collections.EMPTY_LIST));
			}

			// remove the monitor from the set.
			final Collection _monitorTriples = getMonitorTriplesFor(monitorStmt, method);
			final Iterator _i = _monitorTriples.iterator();
			final int _iEnd = _monitorTriples.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Triple _triple = (Triple) _i.next();
				_result.remove(_triple.getFirst());
				_result.remove(_triple.getSecond());
			}
		}
		return _result;
	}

	/**
	 * @see IMonitorInfo#getEnclosedStmts(SootMethod,boolean)
	 */
	public Collection getEnclosedStmts(final SootMethod method, final boolean transitive) {
		if (!syncedMethods.isEmpty()) {
			processSyncedMethods();
		}

		final Collection _result = new HashSet();
		_result.addAll((Collection) MapUtils.getObject(syncedMethod2enclosedStmts, method, Collections.EMPTY_LIST));

		if (transitive && !_result.isEmpty()) {
			final Collection _temp = new HashSet();
			_temp.addAll(_result);
			CollectionUtils.filter(_temp, ENTER_MONITOR_STMT_PREDICATE);

			final Map _monitor2enclosedStmts = (Map) method2monitor2encloseStmts.get(method);
			final Iterator _i = _temp.iterator();
			final int _iEnd = _temp.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Stmt _monitorStmt = (Stmt) _i.next();

				_result.addAll(calculateTransitiveClosure(_monitor2enclosedStmts, _monitorStmt));
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosingMonitors(soot.jimple.Stmt, SootMethod, boolean)
	 */
	public Collection getEnclosingMonitors(final Stmt stmt, final SootMethod method, final boolean transitive) {
		final Collection _result = new HashSet();

		final Map _map = (Map) MapUtils.getObject(method2enclosedStmts2monitors, method, Collections.EMPTY_MAP);

		if (_map.size() > 0) {
			if (transitive) {
				_result.addAll(calculateTransitiveClosure(_map, stmt));
			} else {
				_result.addAll((Collection) MapUtils.getObject(_map, stmt, Collections.EMPTY_LIST));
			}

			if (stmt instanceof EnterMonitorStmt || stmt instanceof ExitMonitorStmt) {
				// remove the monitor from the set.
				final Collection _monitorTriples = getMonitorTriplesFor(stmt, method);
				final Iterator _i = _monitorTriples.iterator();
				final int _iEnd = _monitorTriples.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final Triple _triple = (Triple) _i.next();
					_result.remove(_triple.getFirst());
					_result.remove(_triple.getSecond());
				}
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getId()
	 */
	public Object getId() {
		return IMonitorInfo.ID;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getMonitorGraph(edu.ksu.cis.indus.interfaces.ICallGraphInfo)
	 */
	public IDirectedGraph getMonitorGraph(final ICallGraphInfo callgraphInfo) {
		// TODO: Auto-generated method stub
		return null;
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

			if (_triple.getThird().equals(method)
				  && (_triple.getFirst().equals(monitorStmt) || _triple.getSecond().equals(monitorStmt))) {
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
				final Map _monitor2enclosedStmts = CollectionsUtilities.getMapFromMap(method2monitor2encloseStmts, _method);

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
				final Stmt _dependent = (Stmt) _entry2.getKey();

				for (final Iterator _k = ((Collection) _entry2.getValue()).iterator(); _k.hasNext();) {
					final Stmt _obj = (Stmt) _k.next();
					_temp.append("\t\t" + _dependent + "[" + _dependent.hashCode() + "] -encloses- " + _obj + "["
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

		for (final Iterator _i = method2monitor2encloseStmts.values().iterator(); _i.hasNext();) {
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
	}

	/**
	 * Calculates the monitor statement transitive closure for the given statement based in the given map.
	 *
	 * @param map on which to calculate the closure.
	 * @param stmt from which to initiate the closure.
	 *
	 * @return the contents of the closure.
	 *
	 * @pre map != null and stmt != null
	 * @pre map.oclIsKindOf(Map(Stmt, Collection(Stmt))
	 * @post result != null and result.oclIsKindOf(Collection(Stmt))
	 */
	private Collection calculateTransitiveClosure(final Map map, final Stmt stmt) {
		final Collection _result = new HashSet();
		final IWorkBag _wb = new LIFOWorkBag();
		final Collection _stmts = new HashSet();
		_wb.addWork(stmt);
		_result.add(stmt);

		while (_wb.hasWork()) {
			final Stmt _s = (Stmt) _wb.getWork();
			_stmts.clear();
			_stmts.addAll((Collection) map.get(_s));
			CollectionUtils.filter(_stmts, ENTER_MONITOR_STMT_PREDICATE);
			_result.addAll(_stmts);
		}
		return _result;
	}

	/**
	 * Populates the work bag with work to proecess the successors of the given basic block.
	 *
	 * @param workbag to populate.
	 * @param enterBasicBlock is the basic blocks whose successors need to be processed.
	 * @param enterStack is the stack of enter monitors.
	 * @param currStmts is collection of current statement matched to the topmost monitor.
	 *
	 * @pre workbag != null and enterBasicBlock != null nad enterStack != null and currStmts != null
	 * @pre enterStack.oclIsKindOf(Sequence(Pair(EnterMonitorStmt, Collection(Stmt))))
	 * @pre currStmts.oclIsKindOf(Collection(Stmt))
	 */
	private void populateWorkBagToProcessSuccessors(final IWorkBag workbag, final BasicBlock enterBasicBlock,
		final Stack enterStack, final Collection currStmts) {
		final Collection _succs = enterBasicBlock.getSuccsOf();

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

			if (_exit == _exitBlock.getTrailerStmt()) {
				populateWorkBagToProcessSuccessors(workbag, _exitBlock, enterStack, currStmts);
			} else {
				final List _stmts = _exitBlock.getStmtsOf();
				workbag.addWork(new Quadraple(_exitBlock, _stmts.get(_stmts.indexOf(_exit) + 1), enterStack.clone(),
						((HashSet) currStmts).clone()));
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
			/*
			 * This needs to be accounted in synchronization DA.
			 * currStmts.add(_enter);
			 * currStmts.add(_exit);
			 */

			// add dependee information.
			final Map _dent2ddees = CollectionsUtilities.getMapFromMap(method2enclosedStmts2monitors, method);
			final Map _ddee2ddents = CollectionsUtilities.getMapFromMap(method2monitor2encloseStmts, method);
			final Collection _dependees = new HashSet();
			_dependees.add(_enter);
			_dependees.add(_exit);

			for (final Iterator _k = currStmts.iterator(); _k.hasNext();) {
				final Stmt _curr = (Stmt) _k.next();
				CollectionsUtilities.putAllIntoSetInMap(_dent2ddees, _curr, _dependees);
			}

			// add enclosure information for enter and exit monitor
			CollectionsUtilities.putAllIntoSetInMap(_ddee2ddents, _enter, currStmts);
			CollectionsUtilities.putAllIntoSetInMap(_ddee2ddents, _exit, currStmts);

			// collect monitor triples
			final Triple _triple = new Triple(_enter, _exit, method);
			monitorTriples.add(_triple);
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
		final Collection _dependees = new ArrayList();
		final Collection _temp = new HashSet();

		for (final Iterator _i = syncedMethods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			for (final Iterator _j = getUnitGraph(_sm).iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();
				_dependees.clear();
				_dependees.addAll(getEnclosingMonitors(_stmt, _sm, false));
				CollectionUtils.filter(_dependees, ENTER_MONITOR_STMT_PREDICATE);

				final int _size;

				if (_stmt instanceof EnterMonitorStmt || _stmt instanceof ExitMonitorStmt) {
					_size = 1;
				} else {
					_size = 0;
				}

				if (_dependees.size() == _size) {
					_temp.add(_stmt);
				}
			}

			if (!_temp.isEmpty()) {
				syncedMethod2enclosedStmts.put(_sm, new ArrayList(_temp));
				_temp.clear();
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
 */
