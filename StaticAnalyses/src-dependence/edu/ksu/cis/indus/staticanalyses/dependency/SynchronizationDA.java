
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

import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Quadraple;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;

import edu.ksu.cis.indus.interfaces.IMonitorInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

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
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod, Map(ExitMonitorStmt, Collection(EnterMonitorStmt))))
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Map(EnterMonitortmt, Collection(ExitMonitorStmt))))
 */
public final class SynchronizationDA
  extends DependencyAnalysis
  implements IMonitorInfo {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SynchronizationDA.class);

	/**
	 * This collects the enter-monitor statements during preprocessing.
	 *
	 * @invariant enterMonitors.oclIsKindOf(Collection(EnterMonitorStmt))
	 */
	final Collection enterMonitors = new HashSet();

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
	 * This provides object flow information.
	 */
	private IValueAnalyzer ofa;

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
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#callback(SootMethod)
		 */
		public void callback(final SootMethod method) {
			if (method.isSynchronized()) {
				Triple _triple = new Triple(null, null, method);
				_triple.optimize();
				monitorTriples.add(_triple);
			}
		}

		/**
		 * Preprocesses the given stmt.  It records if the <code>stmt</code> is an enter/exit monitor statement.
		 *
		 * @param stmt is the enter/exit monitor statement.
		 * @param context in which <code>stmt</code> occurs.  This contains the method that encloses <code>stmt</code>.
		 *
		 * @pre stmt.isOclTypeOf(EnterMonitorStmt) or stmt.isOclTypeOf(ExitMonitorStmt)
		 * @pre context.getCurrentMethod() != null
		 *
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#callback(Stmt,Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
			if (stmt instanceof EnterMonitorStmt) {
				enterMonitors.add(new Pair(stmt, context.getCurrentMethod()));
			}
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(EnterMonitorStmt.class, this);
			ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#unhook(ProcessingController)
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
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object method) {
		return getHelper(dependeeMap, dependentStmt, method);
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
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object method) {
		return getHelper(dependentMap, dependeeStmt, method);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getId()
	 */
	public Object getId() {
		return DependencyAnalysis.SYNCHRONIZATION_DA;
	}

	/**
	 * Returns the monitors that occur in the analyzed system.
	 *
	 * @return a collection of <code>Triples</code>.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IMonitorInfo#getMonitorTriples()
	 */
	public Collection getMonitorTriples() {
		return Collections.unmodifiableCollection(monitorTriples);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return stable;
	}

	/**
	 * Calculates the synchronization dependency information for the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Synchronization Dependence processing");
		}

		final IWorkBag _workbag = new LIFOWorkBag();
		final Collection _temp = new HashSet();
		final Stack _stack = new Stack();
		final Collection _processedStmts = new HashSet();
		final Collection _processedMonitors = new HashSet();
		final Collection _coupled = new HashSet();

		/*
		 * Calculating Sync DA is not as simple as it looks in the presence of exceptions. The exit monitors are the tricky
		 * ones.  The exit monitors are guarded by catch block generated by the compiler.  The user cannot generated such
		 * complex catch blocks, but nevertheless we cannot assume about the compiler. So, we proceed by calculating the
		 * dependence edges over a complete unit graph and use object flow information to arbitrate suspicious enter-exit
		 * matches.
		 */
		for (final Iterator _i = enterMonitors.iterator(); _i.hasNext();) {
			Pair _pair = (Pair) _i.next();
			final Stmt _enterMonitor = (Stmt) _pair.getFirst();
			final SootMethod _method = (SootMethod) _pair.getSecond();
			Map _stmt2ddents = (Map) dependentMap.get(_method);
			Map _stmt2ddees;
			final Context _context = new Context();

			if (_stmt2ddents == null) {
				_stmt2ddents = new HashMap();
				_stmt2ddees = new HashMap();
				dependentMap.put(_method, _stmt2ddents);
				dependeeMap.put(_method, _stmt2ddees);
			} else if (_stmt2ddents.get(_enterMonitor) != null) {
				continue;
			} else {
				_stmt2ddees = (Map) dependeeMap.get(_method);
			}

			final BasicBlockGraph _bbGraph = getBasicBlockGraph(_method);
			final List _stmtList = getStmtList(_method);
			_processedStmts.clear();
			_workbag.clear();
			_stack.clear();
			_temp.clear();
			_workbag.addWork(new Quadraple(_bbGraph.getEnclosingBlock(_enterMonitor), _enterMonitor, _stack, _temp, false));

nextBasicBlock: 
			do {
				final Quadraple _work = (Quadraple) _workbag.getWork();
				final BasicBlock _bb = (BasicBlock) _work.getFirst();
				final Stmt _leadStmt = (Stmt) _work.getSecond();
				final Stack _enterStack = (Stack) _work.getThird();
				Collection _currStmts = (HashSet) _work.getFourth();

				for (final Iterator _j = _bb.getStmtFrom(_stmtList.indexOf(_leadStmt)).iterator(); _j.hasNext();) {
					final Stmt _stmt = (Stmt) _j.next();

					// This is to avoid processing induced by back-edgesr.
					if (_processedStmts.contains(_stmt)) {
						continue nextBasicBlock;
					}
					_processedStmts.add(_stmt);

					if (_stmt instanceof EnterMonitorStmt) {
						_currStmts.add(_stmt);
						_enterStack.push(new Pair(_stmt, _currStmts));
						_currStmts = new HashSet();
					} else if (_stmt instanceof ExitMonitorStmt) {
						_pair = (Pair) _enterStack.pop();

						final EnterMonitorStmt _enter = (EnterMonitorStmt) _pair.getFirst();
						final ExitMonitorStmt _exit = (ExitMonitorStmt) _stmt;

						// If the current monitor was processed, we cannot add any more information to it. So, chug along.
						if (_processedMonitors.contains(_enter)) {
							_currStmts = (HashSet) _pair.getSecond();
							_currStmts.add(_stmt);
							continue;
						} else {
							/*
							 * if the monitor object at the enter and exit statements contain the same objects then
							 * consider this pair, if not continue.  The assumption here is that the compiler will copy the
							 * monitor object before the enter monitor to be used in exit monitor.  Hence, a flow
							 * sensitive analysis should be able to provide identical value sets.
							 */
							_context.setRootMethod(_method);
							_context.setProgramPoint(_enter.getOpBox());
							_context.setStmt(_enter);

							final Collection _nValues = ofa.getValues(_enter.getOp(), _context);
							_context.setProgramPoint(_exit.getOpBox());
							_context.setStmt(_exit);

							final Collection _xValues = ofa.getValues(_exit.getOp(), _context);

							if (_nValues.size() != _xValues.size() || !_nValues.containsAll(_xValues)) {
								continue;
							}
						}

						Collection _col = new HashSet();
						_col.add(_enter);
						_col.add(_stmt);

						for (final Iterator _k = _currStmts.iterator(); _k.hasNext();) {
							final Stmt _curr = (Stmt) _k.next();
							_stmt2ddees.put(_curr, _col);
						}
						_stmt2ddents.put(_stmt, new HashSet(_currStmts));
						_col = (Collection) _stmt2ddents.get(_enter);

						if (_col == null) {
							_col = new HashSet();
							_stmt2ddents.put(_enter, _col);
						}
						_col.addAll(_currStmts);

						final Triple _triple = new Triple(_enter, ((ExitMonitorStmt) _stmt), _method);
						monitorTriples.add(_triple);
						_currStmts = (HashSet) _pair.getSecond();
						_currStmts.add(_stmt);
						_coupled.add(_stmt);

						if (_enterStack.isEmpty()) {
							break;
						}
					} else {
						_currStmts.add(_stmt);
					}
				}

				if (!_enterStack.isEmpty()) {
					final Collection _succs = _bb.getSuccsOf();

					if (_succs.size() == 1) {
						final BasicBlock _succ = (BasicBlock) _succs.iterator().next();
						_workbag.addWork(new Quadraple(_succ, _succ.getLeaderStmt(), _enterStack, _currStmts, false));
					} else {
						for (final Iterator _j = _succs.iterator(); _j.hasNext();) {
							final BasicBlock _succ = (BasicBlock) _j.next();
							final Stack _clone = new Stack();

							for (final Iterator _iter = _enterStack.iterator(); _iter.hasNext();) {
								final Pair _p = (Pair) _iter.next();
								_clone.add(new Pair(_p.getFirst(), ((HashSet) _p.getSecond()).clone()));
							}
							_workbag.addWork(new Quadraple(_succ, _succ.getLeaderStmt(), _clone,
									((HashSet) _currStmts).clone(), false));
						}
					}
				}
			} while (_workbag.hasWork());
			_processedMonitors.add(_enterMonitor);
		}

		stable = true;

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
		enterMonitors.clear();
	}

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

		for (final Iterator _i = dependentMap.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_localEdgeCount = 0;

			final SootMethod _method = (SootMethod) _entry.getKey();

			for (final Iterator _j = ((Map) _entry.getValue()).entrySet().iterator(); _j.hasNext();) {
				final Map.Entry _entry2 = (Map.Entry) _j.next();
				final Stmt _dependee = (Stmt) _entry2.getKey();

				for (final Iterator _k = ((Collection) _entry2.getValue()).iterator(); _k.hasNext();) {
					final Stmt _obj = (Stmt) _k.next();
					_temp.append("\t\t" + _dependee + "[" + _dependee.hashCode() + "] <- " + _obj + "[" + _obj.hashCode()
						+ "]\n");
				}
				_localEdgeCount += ((Collection) _entry2.getValue()).size();
			}
			_result.append("\tFor " + _method + " there are " + _localEdgeCount + " sync dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " synchronization dependence edges exist.\n");
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
	protected static Collection getHelper(final Map map, final Object stmt, final Object method) {
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
}

/*
   ChangeLog:
   $Log$
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
