
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Quadraple;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.IMonitorInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
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
 * This class provides synchronization dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.
 * 
 * <p>
 * <i>Synchronization dependence</i>: All non-monitor statement in a method are synchronization dependent on the immediately
 * enclosing monitor statements in the same method.
 * </p>
 * 
 * <p>
 * In case of synchronized methods, the statements in the method not enclosed by monitor statements are dependent on the
 * entry and exit into the method which is tied to the call-sites.  Hence, <code>getDependents()</code> and
 * <code>getDependees()</code> do not include this dependence as it is application specific and can be derived from the
 * control-flow.  If the return points and  entry point are assumed to comprise the monitor then there may be more than one
 * monitor pair as there are many return points, hence, not all statements in the method may be dependent on the same
 * monitor pair.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependee2dependent.oclIsKindOf(Map(SootMethod, Map(ExitMonitorStmt, Collection(EnterMonitorStmt))))
 * @invariant dependent2dependee.oclIsKindOf(Map(SootMethod, Map(EnterMonitortmt, Collection(ExitMonitorStmt))))
 */
public final class SynchronizationDA
  extends AbstractDependencyAnalysis
  implements IMonitorInfo {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(SynchronizationDA.class);

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
	 * This collects the enter-monitor statements in each method during preprocessing.
	 *
	 * @invariant method2enterMonitors.oclIsKindOf(Map(SootMethod, Collection(EnterMonitorStmt)))
	 */
	final Map method2enterMonitors = new HashMap();

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
	private final Map syncedMethod2dependents = new HashMap();

	/**
	 * Creates a new SynchronizationDA object.
	 */
	public SynchronizationDA() {
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
	 * Returns the enter and exit monitor statements on which the given statement is dependent on in the given method.
	 *
	 * @param dependentStmt is a statement in the method.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of enter and exit monitor statements.
	 *
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependentStmt.oclIsKindOf(Stmt)
	 * @post result->forall( o | o.oclIsKindOf(ExitMonitorStmt) or o.oclIsKindOf(EnterMonitorStmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object method) {
		return getDepemdsHelper(dependent2dependee, dependentStmt, method);
	}

	/**
	 * Returns the statements which depend on the given enter or exit monitor statement in the given method.
	 *
	 * @param dependeeStmt is the enter or exit monitor statement.
	 * @param method in which<code>dependeeStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependeeStmt.oclIsKindOf(ExitMonitorStmt) or dependeeStmt.oclIsKindOf(EnterMonitorStmt)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object method) {
		return getDepemdsHelper(dependee2dependent, dependeeStmt, method);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosedStmts(soot.jimple.Stmt, SootMethod, boolean)
	 */
	public Collection getEnclosedStmts(final Stmt monitorStmt, final SootMethod method, final boolean transitive) {
		final Collection _result = new HashSet();

		if (transitive) {
			_result.addAll(DependencyAnalysisUtil.getDependentTransitiveClosureOf(monitorStmt, method, this,
					DependencyAnalysisUtil.STMT_RESULT_DEPENDENCE_RETRIEVER));
		} else {
			_result.addAll(getDependents(monitorStmt, method));
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
		_result.addAll((Collection) MapUtils.getObject(syncedMethod2dependents, method, Collections.EMPTY_LIST));

		if (transitive) {
			final Collection _temp = new HashSet();
			_temp.addAll(_result);
			CollectionUtils.filter(_temp, ENTER_MONITOR_STMT_PREDICATE);

			final Iterator _i = _temp.iterator();
			final int _iEnd = _temp.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Stmt _monitorStmt = (Stmt) _i.next();
				_result.addAll(DependencyAnalysisUtil.getDependentTransitiveClosureOf(_monitorStmt, method, this,
						DependencyAnalysisUtil.STMT_RESULT_DEPENDENCE_RETRIEVER));
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IMonitorInfo#getEnclosingMonitors(soot.jimple.Stmt, SootMethod, boolean)
	 */
	public Collection getEnclosingMonitors(final Stmt stmt, final SootMethod method, final boolean transitive) {
		final Collection _result = new HashSet();

		if (transitive) {
			_result.addAll(DependencyAnalysisUtil.getDependeeTransitiveClosureOf(stmt, method, this,
					DependencyAnalysisUtil.STMT_RESULT_DEPENDENCE_RETRIEVER));
		} else {
			_result.addAll(getDependents(stmt, method));
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
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getId()
	 */
	public Object getId() {
		return IDependencyAnalysis.SYNCHRONIZATION_DA;
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
			LOGGER.info("BEGIN: Synchronization Dependence processing");
		}

		final Map _enter2exits = new HashMap();
		final Collection _processedMonitors = new HashSet();

		/*
		 * Calculating Sync DA is not as simple as it looks in the presence of exceptions. The exit monitors are the tricky
		 * ones.  The exit monitors are guarded by catch block generated by the compiler.  The user cannot generate     such
		 * complex catch blocks, but nevertheless we cannot assume about the compiler. So, we proceed by calculating the
		 * dependence edges over a complete unit graph and use object flow information to arbitrate suspicious enter-exit
		 * matches.
		 */
		for (final Iterator _i = method2enterMonitors.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final SootMethod _method = (SootMethod) _entry.getKey();
			_enter2exits.clear();

			for (final Iterator _j = ((Collection) _entry.getValue()).iterator(); _j.hasNext();) {
				final Stmt _enterMonitor = (Stmt) _j.next();

				if (!_processedMonitors.contains(_enterMonitor)
					  && CollectionsUtilities.getMapFromMap(dependee2dependent, _method).get(_enterMonitor) == null) {
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
			LOGGER.info("END: Synchronization Dependence processing");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		monitorTriples.clear();
		syncedMethods.clear();
		syncedMethod2dependents.clear();
	}

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Synchronization dependence as calculated by " + getClass().getName() + "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependent2dependee.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_localEdgeCount = 0;

			final SootMethod _method = (SootMethod) _entry.getKey();

			for (final Iterator _j = ((Map) _entry.getValue()).entrySet().iterator(); _j.hasNext();) {
				final Map.Entry _entry2 = (Map.Entry) _j.next();
				final Stmt _dependent = (Stmt) _entry2.getKey();

				for (final Iterator _k = ((Collection) _entry2.getValue()).iterator(); _k.hasNext();) {
					final Stmt _obj = (Stmt) _k.next();
					_temp.append("\t\t" + _dependent + "[" + _dependent.hashCode() + "] -SDA-> " + _obj + "["
						+ _obj.hashCode() + "]\n");
				}
				_localEdgeCount += ((Collection) _entry2.getValue()).size();
			}
			_result.append("\tFor " + _method + " there are " + _localEdgeCount + " sync dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}

		int _edgeCount2 = 0;

		for (final Iterator _i = dependent2dependee.values().iterator(); _i.hasNext();) {
			for (final Iterator _j = ((Map) _i.next()).values().iterator(); _j.hasNext();) {
				_edgeCount2 += ((Collection) _j.next()).size();
			}
		}
		_result.append("A total of " + _edgeCount + "/" + _edgeCount2 + " synchronization dependence edges exist.\n");
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
	 * Helper method to getDependeXX() methods.
	 *
	 * @param map from which the information is extracted.
	 * @param stmt of interest.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result.size() != 0 implies (result->includesAll(map.get(method).get(stmt)) and
	 * 		 map.get(method).get(stmt)->includesAll(result))
	 * @post result != null
	 */
	protected static Collection getDepemdsHelper(final Map map, final Object stmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final Map _stmt2ddeXXs = (Map) map.get(method);

		if (_stmt2ddeXXs != null) {
			final Collection _temp = (Collection) _stmt2ddeXXs.get(stmt);

			if (_temp != null) {
				_result = Collections.unmodifiableCollection(_temp);
			}
		}
		return _result;
	}

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
	 * Process the exit monitor to collect synchronization dependency information.
	 *
	 * @param method in which the exit monitor occurs.
	 * @param enter2exits maps enter monitors in the method to a collection of exit monitors.
	 * @param enterStack is the stack of enter monitors.
	 * @param currStmts is collection of current statement matched to the topmost monitor.
	 * @param exitMonitor is the exit monitor to be processed.
	 *
	 * @return a collection of dependent statements corresponding to the enclosing monitor.
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
			currStmts.add(_enter);
			currStmts.add(_exit);

			// add dependee information.
			final Map _dent2ddees = CollectionsUtilities.getMapFromMap(dependent2dependee, method);
			final Map _ddee2ddents = CollectionsUtilities.getMapFromMap(dependee2dependent, method);
			final Collection _dependees = new HashSet();
			_dependees.add(_enter);
			_dependees.add(_exit);

			for (final Iterator _k = currStmts.iterator(); _k.hasNext();) {
				final Stmt _curr = (Stmt) _k.next();
				CollectionsUtilities.putAllIntoSetInMap(_dent2ddees, _curr, _dependees);
			}

			// add dependent information for enter and exit monitor
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
	 * Process the given monitor statement for dependency information.
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
	 * Process the synchronized methods to collect the statements that are synchronization dependent on the entry/exit point
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
				_dependees.addAll(getDependees(_stmt, _sm));
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
				syncedMethod2dependents.put(_sm, new ArrayList(_temp));
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
   Revision 1.51  2004/07/21 02:07:35  venku
   - spruced up IMonitorInfo interface with method to extract more information.
   - updated SynchronizationDA to provide the methods introduced in IMonitorInfo.
   Revision 1.50  2004/07/11 09:42:13  venku
   - Changed the way status information was handled the library.
     - Added class AbstractStatus to handle status related issues while
       the implementations just announce their status.
   Revision 1.49  2004/07/09 09:43:23  venku
   - added clover tags to control coverage of toSting()
   Revision 1.48  2004/07/07 06:29:20  venku
   - coding convention and documentation.
   Revision 1.47  2004/07/07 06:25:07  venku
   - the way statement sub list was constructed in the basic block was incorrect.  FIXED.
   - ripple effect.
   Revision 1.46  2004/07/04 11:52:41  venku
   - renamed getStmtFrom() to getStmtsFrom().
   Revision 1.45  2004/06/27 04:57:58  venku
   - a subtlety about valid enter/exit monitor patterns in the code was addressed. FIXED.
   Revision 1.44  2004/06/27 03:58:20  venku
   - bug #395. FIXED.
   Revision 1.43  2004/06/16 14:30:12  venku
   - logging.
   Revision 1.42  2004/06/03 20:24:12  venku
   - documentation.
   Revision 1.41  2004/06/03 20:23:23  venku
   - MAJOR CHANGE - Reworked the impl as it was missing some dependencies.
   Revision 1.40  2004/06/01 06:29:57  venku
   - added new methods to CollectionUtilities.
   - ripple effect.
   Revision 1.39  2004/05/31 21:38:08  venku
   - moved BasicBlockGraph and BasicBlockGraphMgr from common.graph to common.soot.
   - ripple effect.
   Revision 1.38  2004/05/21 22:11:47  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.
   Revision 1.37  2004/05/14 06:27:24  venku
   - renamed DependencyAnalysis as AbstractDependencyAnalysis.
   Revision 1.36  2004/03/29 01:55:03  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.35  2004/03/04 11:52:21  venku
   - modified ReadyDA to use CollectionsModifiers.
   - fixed some subtle bugs in SyncDA.
   Revision 1.34  2004/03/03 10:11:40  venku
   - formatting.
   Revision 1.33  2004/03/03 10:07:24  venku
   - renamed dependeeMap as dependent2dependee
   - renamed dependentmap as dependee2dependent
   Revision 1.32  2004/02/25 00:04:02  venku
   - documenation.
   Revision 1.31  2004/01/21 13:56:26  venku
   - tracking sync DA in synchronized methods is unnecessary.
   Revision 1.30  2004/01/21 13:52:12  venku
   - documentation.
   Revision 1.29  2004/01/19 08:57:29  venku
   - documentation and formatting.
   Revision 1.28  2004/01/19 08:26:59  venku
   - enabled logging of criteria when they are created in SlicerTool.
   Revision 1.27  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.26  2003/12/30 09:17:47  venku
   - method level synchronization Triple is explicitly optimized.
   Revision 1.25  2003/12/15 06:54:03  venku
   - formatting.
   Revision 1.24  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.23  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.22  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.21  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.20  2003/11/17 01:40:40  venku
   - documentation.
   Revision 1.19  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.18  2003/11/10 03:17:18  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   - ripple effect.
   Revision 1.17  2003/11/06 05:31:08  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.16  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.15  2003/11/05 09:29:05  venku
   - ripple effect of splitting IWorkBag.
   Revision 1.14  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.
   Revision 1.13  2003/11/03 07:54:56  venku
   - added logging.
   Revision 1.12  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.11  2003/09/12 22:33:09  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
   Revision 1.10  2003/09/10 11:50:23  venku
   - formatting.
   Revision 1.9  2003/09/10 11:49:31  venku
   - documentation change.
   Revision 1.8  2003/09/08 02:25:04  venku
   - Ripple effect of changes to ValueAnalyzerBasedProcessingController.
   Revision 1.7  2003/09/07 09:02:13  venku
   - Synchronization dependence now handles exception based
     sync dep edges.  This requires a Value Flow analysis which can
     provides value binding information for a local at a program point.
   - Ripple effect of the above change.
   Revision 1.6  2003/08/21 03:56:18  venku
   Ripple effect of adding IStatus.
   Revision 1.5  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.4  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/09 23:29:52  venku
   Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.
   Revision 1.1  2003/08/07 06:38:05  venku
   Major:
    - Moved the packages under indus umbrella.
    - Renamed MethodLocalDataDA to NonAliasedDataDA.
    - Added class for AliasedDataDA.
    - Documented and specified the classes.
 */
