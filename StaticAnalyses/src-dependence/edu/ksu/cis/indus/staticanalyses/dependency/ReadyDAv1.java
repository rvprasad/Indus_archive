
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.concurrency.SafeLockAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.BackwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.ForwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.IDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class provides ready dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>. This implementation by default does not consider call-sites for dependency
 * calculation.
 * 
 * <p>
 * <i>Ready Dependence</i>: In a thread, all statements reachable from an enter-monitor statement or a <code>wait()</code>
 * call-site via intra-procedural control-flow path with no intervening enter-monitor statement or <code>wait()</code>
 * call-site are ready dependent on the enter-monitor statement. Across different threads, enter-monitor statements and
 * <code>wait()</code> call-sites in a thread are ready dependent on corresponding exit-monitor statements and
 * <code>notify()/notifyAll()</code> call-sites, respectively, occurring in a different thread.
 * </p>
 * 
 * <p>
 * By default, all rules are considered for the analysis.  This can be changed via <code>setRules()</code>. This class will
 * also use OFA information if it is configured to do so.
 * </p>
 * 
 * <p>
 * Ready dependence information pertaining to entry and exit points of synchronized methods cause a divergence in the way
 * information is provided via <code>getDependees</code> and <code>getDependents</code>.
 * </p>
 * 
 * <p>
 * In case the body of the synchronized method is available, then any dependence involving the entry point of the method will
 * use the first statement of the method as the dependee/dependent statement.  Similarly, the exit points of the method will
 * be provided as the  dependee statements.
 * </p>
 * 
 * <p>
 * In case the body of the synchronized method is unavailable, then any dependence involving entry and exit points of the
 * method will use null as the dependee/dependent statement.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependent2dependee.oclIsKindOf(Map(Stmt, Collection(Pair(Stmt, SootMethod))))
 * @invariant dependee2dependent.oclIsKindOf(Map(Stmt, Collection(Pair(Stmt, SootMethod))))
 */
public class ReadyDAv1
  extends AbstractDependencyAnalysis {
	/** 
	 * This indicates rule 1 of ready dependency as described in the report.
	 */
	public static final int RULE_1 = 1;

	/** 
	 * This indicates rule 2 of ready dependency as described in the report.
	 */
	public static final int RULE_2 = 2;

	/** 
	 * This indicates rule 3 of ready dependency as described in the report.
	 */
	public static final int RULE_3 = 4;

	/** 
	 * This indicates rule 4 of ready dependency as described in the report.
	 */
	public static final int RULE_4 = 8;

	/** 
	 * A token used to represent the nonexistent monitor entry/exit statements in synchronized methods.
	 */
	protected static final Object SYNC_METHOD_PROXY_STMT = "SYNC_METHOD_PROXY_STMT";

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ReadyDAv1.class);

	/** 
	 * This is the logical OR of the <code>RULE_XX</code> as provided by the user.  This indicates the rules which need to be
	 * considered while calculating ready dependency.
	 */
	protected int rules = RULE_1 | RULE_2 | RULE_3 | RULE_4;

	/** 
	 * The collection of methods (readyMethods) which contain at least an enter-monitor statement or a <code>wait()</code>
	 * call-site.
	 */
	final Collection readyMethods = new HashSet();

	/** 
	 * This maps a method to a collection of enter monitor statements in that method.
	 *
	 * @invariant enterMonitors.oclIsKindOf(Map(SootMethod, Set(EnterMonitorStmt)))
	 */
	final Map enterMonitors = new HashMap();

	/** 
	 * This maps a method to a collection of exit monitor statements in that method.
	 *
	 * @invariant exitMonitors.oclIsKindOf(Map(SootMethod, Set(ExitMonitorStmt)))
	 */
	final Map exitMonitors = new HashMap();

	/** 
	 * This maps methods to <code>Object.notifyXX</code> method calls in them.
	 *
	 * @invariant notifies.oclIsKindOf(Map(SootMethod, Set(VirtualInvokeExpr)))
	 */
	final Map notifies = new HashMap();

	/** 
	 * This maps methods to <code>Object.wait</code> method calls in them.
	 *
	 * @invariant wait.oclIsKindOf(Map(SootMethod, Set(VirtualInvokeExpr)))
	 */
	final Map waits = new HashMap();

	/** 
	 * This is a collection of dependeXX in Pair form which result from application of rule 2 to synchronized methods.
	 */
	private final Collection specials = new HashSet();

	/** 
	 * The direction of the analysis.
	 */
	private final Object theDirection;

	/** 
	 * This provides call graph of the system being analyzed.
	 */
	private ICallGraphInfo callgraph;

	/** 
	 * This provides direction-sensitive information to make the analysis direction sensitive.
	 */
	private final IDirectionSensitiveInfo directionSensInfo;

	/** 
	 * This provides information such as the classes occurring in the system being analyzed.
	 */
	private IEnvironment env;

	/** 
	 * This provides monitor information in the system.
	 */
	private IMonitorInfo monitorInfo;

	/** 
	 * This provides call graph of the system being analyzed.
	 */
	private IThreadGraphInfo threadgraph;

	/** 
	 * The object flow analysis to be used.
	 */
	private IValueAnalyzer ofa;

	/** 
	 * This manages pairs.
	 */
	private PairManager pairMgr;

	/** 
	 * This provides safe lock information.
	 */
	private SafeLockAnalysis safelockAnalysis;

	/** 
	 * This indicates if dependence should be considered across call-sites.  Depending on the application, one may choose to
	 * ignore ready dependence across call-sites and rely on other dependence analysis to include the call-site.  This only
	 * affects how rule 1 and 3 are interpreted.
	 */
	private boolean considerCallSites;

	/** 
	 * This indicates if object flow analysis should be used.
	 */
	private boolean useOFA;

	/** 
	 * This indicates if safe lock analysis should be used.
	 */
	private boolean useSafeLockAnalysis;

	/**
	 * Creates an instance of this class.
	 *
	 * @param directionSensitiveInfo that controls the direction.
	 * @param direction of the analysis
	 *
	 * @pre info != null and direction != null
	 */
	protected ReadyDAv1(final IDirectionSensitiveInfo directionSensitiveInfo, final Object direction) {
		directionSensInfo = directionSensitiveInfo;
		theDirection = direction;
		preprocessor = new PreProcessor();
		considerCallSites = false;
	}

	/**
	 * This preprocesses information before ready dependence is calculated.  Information required during the analysis is
	 * collected by this class.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class PreProcessor
	  extends AbstractProcessor {
		/**
		 * Collects monitor statements and statements with <code>Object.wait()</code> and <code>Object.notifyXX()</code>
		 * call-sites.
		 *
		 * @param stmt to be preprocessed.
		 * @param context in which <code>stmt</code> occurs.
		 *
		 * @pre stmt.oclIsTypeOf(ExitMonitorStmt) or stmt.oclIsTypeOf(EnterMonitorStmt) or stmt.oclIsTypeOf(InvokeStmt)
		 * @pre stmt != null and context.getCurrentMethod() != null
		 */
		public void callback(final Stmt stmt, final Context context) {
			final SootMethod _method = context.getCurrentMethod();

			InvokeExpr _expr = null;

			if (stmt instanceof InvokeStmt) {
				_expr = stmt.getInvokeExpr();
			}

			if (_expr != null && _expr instanceof VirtualInvokeExpr) {
				final VirtualInvokeExpr _invokeExpr = (VirtualInvokeExpr) _expr;
				final SootMethod _callee = _invokeExpr.getMethod();

				Map _method2stmts = null;

				if (Util.isWaitMethod(_callee)) {
					_method2stmts = waits;
				} else if (Util.isNotifyMethod(_callee)) {
					_method2stmts = notifies;
				}

				if (_method2stmts != null) {
					CollectionsUtilities.putIntoCollectionInMap(_method2stmts, _method, stmt,
						CollectionsUtilities.HASH_SET_FACTORY);
				}
			}
		}

		/**
		 * Collects all the methods that encloses an enter-monitor statement or a call to <code>wait()</code> method.
		 */
		public void consolidate() {
			readyMethods.addAll(waits.keySet());
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(InvokeStmt.class, this);
		}

		/**
		 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(InvokeStmt.class, this);
		}
	}

	/**
	 * Retrieves an instance of ready dependence analysis that calculates information in backward direction.
	 *
	 * @return an instance of ready dependence.
	 *
	 * @post result != null
	 */
	public static ReadyDAv1 getBackwardReadyDA() {
		return new ReadyDAv1(new BackwardDirectionSensitiveInfo(), BACKWARD_DIRECTION);
	}

	/**
	 * Retrieves an instance of ready dependence analysis that calculates information in forward direction.
	 *
	 * @return an instance of ready dependence.
	 *
	 * @post result != null
	 */
	public static ReadyDAv1 getForwardReadyDA() {
		return new ReadyDAv1(new ForwardDirectionSensitiveInfo(), FORWARD_DIRECTION);
	}

	/**
	 * Records if ready dependency should be interprocedural or otherwise.
	 *
	 * @param consider <code>true</code> indicates that any call-site leading to wait() call-site or enter-monitor statement
	 * 		  should be considered as a ready dependeee; <code>false</code>, otherwise. This only affects how rule 1 and 3
	 * 		  are interpreted
	 */
	public void setConsiderCallSites(final boolean consider) {
		considerCallSites = consider;
	}

	/**
	 * Returns the statements on which the <code>dependentStmt</code> depends on.  Refer to class level documentation for
	 * important details.
	 *
	 * @param dependentStmt is the statement for which the dependee info is requested.
	 * @param context is the method in which the statement occurs.
	 *
	 * @return a collection of statement.
	 *
	 * @pre dependentStmt.isOclKindOf(Stmt) and context.isOclIsKindOf(SootMethod)
	 * @post result.isOclKindOf(Collection(Pair(Stmt, Method)))
	 *
	 * @see AbstractDependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object context) {
		final Map _temp = (Map) dependent2dependee.get(context);
		Collection _result = Collections.EMPTY_LIST;

		if (_temp != null && _temp.containsKey(dependentStmt)) {
			_result = Collections.unmodifiableCollection((Collection) _temp.get(dependentStmt));
		}
		return _result;
	}

	/**
	 * Returns the statements which depend on <code>dependeeStmt</code>. Refer to class level documentation for  important
	 * details.
	 *
	 * @param dependeeStmt is the statement for which the dependent info is requested.
	 * @param context is ignored.
	 *
	 * @return a collection of statement.
	 *
	 * @pre dependeeStmt.isOclKindOf(Stmt)
	 * @post result.isOclKindOf(Collection(Pair(Stmt, SootMethod)))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependents( java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object context) {
		final Map _temp = (Map) dependee2dependent.get(context);
		Collection _result = Collections.EMPTY_LIST;

		if (_temp != null && _temp.containsKey(dependeeStmt)) {
			_result = Collections.unmodifiableCollection((Collection) _temp.get(dependeeStmt));
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getDirection()
	 */
	public Object getDirection() {
		return theDirection;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IDependencyAnalysis.READY_DA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis getIndirectVersionOfDependence() {
		return new IndirectDependenceAnalysis(this, IDependenceRetriever.PAIR_DEP_RETRIEVER);
	}

	/**
	 * Sets the rules to be processed.
	 *
	 * @param rulesParam is the logical OR of <i>RULE_XX</i> constants defined in this class.
	 *
	 * @throws IllegalArgumentException when rules is not a valid combination of <i>RULE_XX</i> constants.
	 */
	public void setRules(final int rulesParam) {
		if ((rulesParam & ~(RULE_1 | RULE_2 | RULE_3 | RULE_4)) != 0) {
			throw new IllegalArgumentException("rules has to be a combination of RULE_XX constants defined in this class.");
		}
		rules = rulesParam;
	}

	/**
	 * Provides the rules that are active at present.
	 *
	 * @return the active rules as a logical OR of <i>RULE_XX</i> constants.
	 *
	 * @post result and not (RULE_1 or RULE_2 or RULE_3 or RULE_4) == 0
	 */
	public int getRules() {
		return rules;
	}

	/**
	 * Retrieves only the entry and exit points of synchronized methods from the given collection of dependence points.
	 *
	 * @param dependences which need to be filtered.
	 *
	 * @return a collection of entry and exit points that occur in synchronized methods.
	 *
	 * @pre dependences != null and dependences.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
	 * @post result != null and result.oclIsKindOf(Collection(Pair(Stmt, SootMethod)
	 * @post dependences.conatinsAll(result)
	 */
	public Collection getSynchronizedMethodEntryExitPoints(final Collection dependences) {
		final Collection _result = new HashSet(dependences);
		_result.retainAll(specials);
		return _result;
	}

	/**
	 * Sets if object flow analysis should be used or not.
	 *
	 * @param flag <code>true</code> indicates that object flow analysis should be used; <code>false</code>, otherwise.
	 */
	public final void setUseOFA(final boolean flag) {
		useOFA = flag;
	}

	/**
	 * Controls if safe lock analysis should be used.
	 *
	 * @param b <code>true</code> indicates the analysis should be used; <code>false</code>, otherwise.
	 */
	public void setUseSafeLockAnalysis(final boolean b) {
		useSafeLockAnalysis = b;
	}

	/**
	 * Calculates ready dependency for the methods provided at initialization.  It considers only the rules specified by via
	 * <code>setRules</code> method. By default, all rules are considered for the analysis.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (monitorInfo.isStable()
			  && callgraph.isStable()
			  && threadgraph.isStable()
			  && (!useSafeLockAnalysis || safelockAnalysis.isStable())) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("BEGIN: Ready Dependence [" + this.getClass() + " processing");
			}

			if (!threadgraph.getCreationSites().isEmpty()) {
				final boolean _syncedMethodsExist = processMonitorInfo();

				if (_syncedMethodsExist && (rules & (RULE_1 | RULE_3)) != 0) {
					processRule1And3();
				}

				if (!waits.isEmpty() && !notifies.isEmpty()) {
					if ((rules & RULE_2) != 0) {
						processRule2();
					}

					if ((rules & RULE_4) != 0) {
						processRule4();
					}
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("analyze() - " + toString());
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("END: Ready Dependence processing");
			}

			stable();
		} else {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Delaying execution as prerequisites are unsatisfied.");
			}
		}
	}

	/**
	 * Resets internal data structures. <i>The rules are not reset.</i>  Also, the data acquired at setup time is not erased.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		enterMonitors.clear();
		exitMonitors.clear();
		specials.clear();
		waits.clear();
		notifies.clear();
	}

	///CLOVER:OFF

	/**
	 * Returns a stringified representation of the analysis information.
	 *
	 * @return a string containing the analysis info.
	 *
	 * @post result != null
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Ready dependence as calculated by " + getClass().getName() + "\n");
		int _edgeCount1 = 0;
		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependent2dependee.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Object _method = _entry.getKey();
			_result.append("In method " + _method + "\n ");

			for (final Iterator _k = ((Map) _entry.getValue()).entrySet().iterator(); _k.hasNext();) {
				final Map.Entry _entry1 = (Map.Entry) _k.next();
				final Object _dependent = _entry1.getKey();

				int _localEdgeCount = 0;

				for (final Iterator _j = ((Collection) _entry1.getValue()).iterator(); _j.hasNext();) {
					final Object _dependee = _j.next();
					_temp.append("\t\t" + _dependee + " <-- " + _dependent + "\n");
				}
				_localEdgeCount += ((Collection) _entry1.getValue()).size();

				final Object _key = _entry1.getKey();
				_result.append("\tFor " + _key + "[");

				if (_key != null) {
					_result.append(_key.hashCode());
				} else {
					_result.append(0);
				}
				_result.append("] there are " + _localEdgeCount + " Ready dependence edges.\n");
				_result.append(_temp);
				_temp.delete(0, _temp.length());
				_edgeCount1 += _localEdgeCount;
			}
		}
		_result.append("A total of " + _edgeCount1 + " Ready dependence edges exist.");
		return _result.toString();
	}

	///CLOVER:ON

	/**
	 * Checks if the given wait invocation is dependent on the notify invocation according to rule 4 based of OFA
	 * information.
	 *
	 * @param waitPair is the wait invocation statement and containg method pair.
	 * @param notifyPair is the notify invocation statement and containg method pair.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre waitPair.getFirst() != null and waitPair.getSecond() != null
	 * @pre notifyPair.getFirst() != null and notifyPair.getSecond() != null
	 */
	protected final boolean ifDependentOnBasedOnOFAByRule4(final Pair waitPair, final Pair notifyPair) {
		boolean _result;

		final SootMethod _waitMethod = (SootMethod) waitPair.getSecond();
		final SootMethod _notifyMethod = (SootMethod) notifyPair.getSecond();
		final Object _waitStmt = waitPair.getFirst();
		final Object _notifyStmt = notifyPair.getFirst();
		final Context _context = new AllocationContext();

		final Stmt _wStmt = (Stmt) _waitStmt;
		final InstanceInvokeExpr _wExpr = ((InstanceInvokeExpr) _wStmt.getInvokeExpr());
		_context.setProgramPoint(_wExpr.getBaseBox());
		_context.setStmt(_wStmt);
		_context.setRootMethod(_waitMethod);

		final Collection _col1 = ofa.getValues(_wExpr.getBase(), _context);

		final Stmt _nStmt = (Stmt) _notifyStmt;
		final InstanceInvokeExpr _nExpr = ((InstanceInvokeExpr) _nStmt.getInvokeExpr());
		_context.setProgramPoint(_nExpr.getBaseBox());
		_context.setStmt(_nStmt);
		_context.setRootMethod(_notifyMethod);

		final Collection _col2 = ofa.getValues(_nExpr.getBase(), _context);

		final NullConstant _n = NullConstant.v();
		final Collection _temp = CollectionUtils.intersection(_col1, _col2);

		while (_temp.remove(_n)) {
			;
		}
		_result = !_temp.isEmpty();

		return _result;
	}

	/**
	 * Checks if the given enter monitor statement/synchronized method  is dependent on the exit monitor
	 * statement/synchronized method according to rule 2.  The dependence is determined based on the relation between the
	 * classes of the objects on which synchronization is being performed.
	 *
	 * @param enterPair is the enter monitor statement and containg statement pair.
	 * @param exitPair is the exit monitor statement and containg statement pair.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre enterPair != null and exitPair != null
	 * @pre enterPair.getSecond() != null and exitPair.getSecond() != null
	 * @pre enterPair.getSecond().oclIsKindOf(SootMethod) and exitPair.getSecond().oclIsKindOf(SootMethod)
	 * @pre enterPair.getFirst() != null
	 * @pre enterPair.getFirst().oclIsKindOf(EnterMonitorStmt) or enterPair.getFirst().equals(SYNC_METHOD_PROXY_STMT)
	 * @pre exitPair.getFirst() != null
	 * @pre exitPair.getFirst().oclIsKindOf(ExitMonitorStmt) or exitPair.getFirst().equals(SYNC_METHOD_PROXY_STMT)
	 */
	protected boolean ifDependentOnByRule2(final Pair enterPair, final Pair exitPair) {
		Type _enterMonitorOptype;
		Type _exitMonitorOpType;
		final SootMethod _sm1 = (SootMethod) enterPair.getSecond();

		boolean _result = isLockUnsafe(enterPair.getFirst(), _sm1);

		if (_result) {
			final SootMethod _sm2 = (SootMethod) exitPair.getSecond();

			boolean _syncedStaticMethod1 = false;
			boolean _syncedStaticMethod2 = false;
			final Object _o1 = enterPair.getFirst();

			if (_o1.equals(SYNC_METHOD_PROXY_STMT)) {
				_enterMonitorOptype = _sm1.getDeclaringClass().getType();
				_syncedStaticMethod1 = _sm1.isStatic();
			} else {
				final EnterMonitorStmt _enter = (EnterMonitorStmt) _o1;
				_enterMonitorOptype = _enter.getOp().getType();
			}

			final Object _o2 = exitPair.getFirst();

			if (_o2.equals(SYNC_METHOD_PROXY_STMT)) {
				_exitMonitorOpType = _sm2.getDeclaringClass().getType();
				_syncedStaticMethod2 = _sm2.isStatic();
			} else {
				final ExitMonitorStmt _exit = (ExitMonitorStmt) _o2;
				_exitMonitorOpType = _exit.getOp().getType();
			}

			if (_syncedStaticMethod1 && _syncedStaticMethod2) {
				// if we are dealing with synchronized static methods, then they will lock the class object, hence,
				// inheritance relation should not be considered.            
				_result = _enterMonitorOptype.equals(_exitMonitorOpType);
			} else if (_syncedStaticMethod1 ^ _syncedStaticMethod2) {
				/*
				 * if only one of the methods is static and synchronized then we cannot determine RDA as it is possible that
				 * the monitor in the non-static method may actually be on the class object of the class in  which the static
				 * method is defined.  There are many combinations which can be pruned.  No time now. THINK
				 */
				_result = true;
			} else {
				_result = Util.isSameOrSubType(_enterMonitorOptype, _exitMonitorOpType, env);

				if (_result && useOFA) {
					_result = ifDependentOnBasedOnOFAByRule2(enterPair, exitPair);
				}
			}
		}
		return _result;
	}

	/**
	 * Checks if the given <code>wait()</code> call-site is dependent on the <code>notifyXX()</code> call-site according to
	 * rule 2.  The dependence  is determined based on the relation between the classes  immediately enclosing the given
	 * statements occur.
	 *
	 * @param wPair is the statement in which <code>java.lang.Object.wait()</code> is invoked.
	 * @param nPair is the statement in which <code>java.lang.Object.notifyXX()</code> is invoked.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre wPair.getFirst() != null and nPair.getFirst() != null
	 */
	protected boolean ifDependentOnByRule4(final Pair wPair, final Pair nPair) {
		final InvokeStmt _notify = (InvokeStmt) nPair.getFirst();
		final SootClass _notifyClass =
			env.getClass(((RefType) ((InstanceInvokeExpr) _notify.getInvokeExpr()).getBase().getType()).getClassName());
		final InvokeStmt _wait = (InvokeStmt) wPair.getFirst();
		final SootClass _waitClass =
			env.getClass(((RefType) ((InstanceInvokeExpr) _wait.getInvokeExpr()).getBase().getType()).getClassName());
		boolean _result = Util.isHierarchicallyRelated(_notifyClass, _waitClass);

		if (_result && useOFA) {
			_result = ifDependentOnBasedOnOFAByRule4(wPair, nPair);
		}
		return _result;
	}

	/**
	 * Extracts information provided by environment at initialization time.
	 *
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 * 		   <code>info</code> member.
	 *
	 * @pre info.get(IEnvironment.ID) != null and info.get(ICallGraphInfo.ID) != null
	 * @pre info.get(IThreadGraphInfo.ID) != null and info.get(PairManager.ID) != null
	 * @pre useOFA implies info.get(IValueAnalyzer.ID) != null and info.get(IValueAnalyzer.ID).oclIsKindOf(OFAnalyzer)
	 * @pre info.get(IMonitorInfo.ID) != null
	 * @pre useSafeLockAnalysis implies info.get(SafeLockAnalysis.ID) != null
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		env = (IEnvironment) info.get(IEnvironment.ID);

		if (env == null) {
			throw new InitializationException(IEnvironment.ID + " was not provided in info.");
		}

		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided in info.");
		}
		threadgraph = (IThreadGraphInfo) info.get(IThreadGraphInfo.ID);

		if (threadgraph == null) {
			throw new InitializationException(IThreadGraphInfo.ID + " was not provided in info.");
		}
		pairMgr = (PairManager) info.get(PairManager.ID);

		if (pairMgr == null) {
			throw new InitializationException(PairManager.ID + " was not provided in info.");
		}

		ofa = (OFAnalyzer) info.get(IValueAnalyzer.ID);

		if (useOFA && ofa == null) {
			throw new InitializationException(IValueAnalyzer.ID + " was not provided in the info.");
		}

		monitorInfo = (IMonitorInfo) info.get(IMonitorInfo.ID);

		if (monitorInfo == null) {
			final String _msg = "An interface with id, " + IMonitorInfo.ID + ", was not provided.";
			LOGGER.error(_msg);
			throw new InitializationException(_msg);
		}

		safelockAnalysis = (SafeLockAnalysis) info.get(SafeLockAnalysis.ID);

		if (useSafeLockAnalysis && safelockAnalysis == null) {
			final String _msg = "An interface with id, " + SafeLockAnalysis.ID + ", was not provided.";
			LOGGER.error(_msg);
			throw new InitializationException(_msg);
		}
	}

	/**
	 * Retrieves pairs of exitmonitor statements and the methods that containing the statement.
	 *
	 * @return a collection of pairs.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Pair(ExitMonitorStmt, SootMethod)))
	 */
	private Collection getExitMonitorStmtMethodPairs() {
		final Collection _temp = new HashSet();

		for (final Iterator _i = exitMonitors.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final Object _method = _entry.getKey();

			for (final Iterator _j = ((Collection) _entry.getValue()).iterator(); _j.hasNext();) {
				final Object _o = _j.next();
				_temp.add(pairMgr.getPair(_o, _method));
			}
		}
		return _temp;
	}

	/**
	 * This checks if the lock associated the given monitor is unsafe.
	 *
	 * @param monitorStmt obviously.
	 * @param method in which <code>monitorStmt</code> occurs.
	 *
	 * @return <code>true</code> if the lock is unsafe; <code>false</code>, otherwise.
	 *
	 * @pre monitorStmt != null and method != null
	 */
	private boolean isLockUnsafe(final Object monitorStmt, final SootMethod method) {
		boolean _result = true;

		if (useSafeLockAnalysis) {
			if (monitorStmt.equals(SYNC_METHOD_PROXY_STMT)) {
				_result = !safelockAnalysis.isLockSafe(method);
			} else {
				_result = !safelockAnalysis.isLockSafe((Stmt) monitorStmt, method);
			}
		}
		return _result;
	}

	/**
	 * Checks if the given stmt contains a call-site.  If so, it checks if it results in the invocation of a method
	 * (ready-method) that has atleast an enter-monitor statement or a <code>wait()</code> call-site.
	 *
	 * @param stmt that could result in the invocation of ready-method via a call-chain.
	 * @param caller in which <code>stmt</code> occurs.
	 *
	 * @return <code>true</code> if <code>stmt</code> results in the invocation of a ready-method via a call-chain;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre stmt != null and caller != null and stmt.containsInvokeExpr() == true
	 */
	private boolean callsReadyMethod(final Stmt stmt, final SootMethod caller) {
		boolean _result = false;

		if (considerCallSites && stmt.containsInvokeExpr()) {
			if (!CollectionUtils.intersection(readyMethods, callgraph.getMethodsReachableFrom(stmt, caller)).isEmpty()) {
				_result = true;
			}
		}
		return _result;
	}

	/**
	 * Collects the dependees in each method.
	 *
	 * @return a collection of dependees in each method.
	 *
	 * @post result != null and result.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private Map collectDependeesInMethods() {
		final Map _method2dependeeMap = new HashMap();

		if ((rules & RULE_1) != 0) {
			final Collection _temp = new ArrayList();

			for (final Iterator _i = enterMonitors.keySet().iterator(); _i.hasNext();) {
				final SootMethod _method = (SootMethod) _i.next();

				// if the method is not concrete we there can be no intra-procedural ready dependence.  So, don't bother.
				if (_method.isConcrete()) {
					final Collection _enterMonitorStmts = (Collection) enterMonitors.get(_method);
					final Collection _col = new HashSet();
					final Iterator _j = _enterMonitorStmts.iterator();
					final int _jEnd = _enterMonitorStmts.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Object _enter = _j.next();
						final boolean _unsafe = isLockUnsafe(_enter, _method);

						if (_unsafe) {
							if (_enter.equals(SYNC_METHOD_PROXY_STMT)) {
								_col.remove(SYNC_METHOD_PROXY_STMT);
								_temp.clear();
								_temp.add(pairMgr.getPair(SYNC_METHOD_PROXY_STMT, _method));
								normalizeEntryInformation(_temp);

								for (final Iterator _k = _temp.iterator(); _k.hasNext();) {
									_col.add(((Pair) _k.next()).getFirst());
								}
							} else {
								_col.add(_enter);
							}
						}
					}
					_method2dependeeMap.put(_method, _col);
				}
			}
		}

		if ((rules & RULE_3) != 0) {
			for (final Iterator _i = waits.keySet().iterator(); _i.hasNext();) {
				final SootMethod _method = (SootMethod) _i.next();
				CollectionsUtilities.putAllIntoSetInMap(_method2dependeeMap, _method, (Collection) waits.get(_method));
			}
		}
		return _method2dependeeMap;
	}

	/**
	 * Checks if the given enter monitor statement/synchronized method  is dependent on the exit monitor
	 * statement/synchronized method according to rule 2 based on OFA.
	 *
	 * @param enterPair is the enter monitor statement and containg method pair.
	 * @param exitPair is the exit monitor statement and containg method pair.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre enterPair.getSecond() != null and exitPair.getSecond() != null
	 *
	 * @see ReadyDAv2#ifDependentOnByRule2(Pair, Pair)
	 */
	private boolean ifDependentOnBasedOnOFAByRule2(final Pair enterPair, final Pair exitPair) {
		boolean _result;

		final SootMethod _enterMethod = (SootMethod) enterPair.getSecond();
		final SootMethod _exitMethod = (SootMethod) exitPair.getSecond();
		final Object _enterStmt = enterPair.getFirst();
		final Object _exitStmt = exitPair.getFirst();
		Collection _col1;
		Collection _col2;
		boolean _syncedStaticMethod1 = false;
		boolean _syncedStaticMethod2 = false;
		final Context _context = new Context();

		if (_enterStmt.equals(SYNC_METHOD_PROXY_STMT)) {
			_syncedStaticMethod1 = _enterMethod.isStatic();

			if (!_syncedStaticMethod1) {
				_context.setRootMethod(_enterMethod);
				_col1 = ofa.getValuesForThis(_context);
			} else {
				_col1 = Collections.EMPTY_LIST;
			}
		} else {
			final EnterMonitorStmt _o1 = (EnterMonitorStmt) _enterStmt;
			_context.setProgramPoint(_o1.getOpBox());
			_context.setStmt(_o1);
			_context.setRootMethod(_enterMethod);
			_col1 = ofa.getValues(_o1.getOp(), _context);
		}

		if (_exitStmt.equals(SYNC_METHOD_PROXY_STMT)) {
			_syncedStaticMethod2 = _exitMethod.isStatic();

			if (!_syncedStaticMethod2) {
				_context.setProgramPoint(null);
				_context.setStmt(null);
				_context.setRootMethod(_exitMethod);
				_col2 = ofa.getValuesForThis(_context);
			} else {
				_col2 = Collections.EMPTY_LIST;
			}
		} else {
			final ExitMonitorStmt _o2 = (ExitMonitorStmt) _exitStmt;
			_context.setProgramPoint(_o2.getOpBox());
			_context.setStmt(_o2);
			_context.setRootMethod(_exitMethod);
			_col2 = ofa.getValues(_o2.getOp(), _context);
		}

		if (_syncedStaticMethod1 ^ _syncedStaticMethod2) {
			/*
			 * if only one of the methods is static and synchronized then we cannot determine RDA as it is possible that
			 * the monitor in the non-static method may actually be on the class object of the class in  which the static
			 * method is defined.  There are many combinations which can be pruned.  No time now. THINK
			 */
			_result = true;
		} else {
			final NullConstant _n = NullConstant.v();
			final Collection _temp = CollectionUtils.intersection(_col1, _col2);

			while (_temp.remove(_n)) {
				;
			}
			_result = !_temp.isEmpty();
		}

		return _result;
	}

	/**
	 * Normalizes enter monitor information provided in the given set.  This basically converts information pertaining entry
	 * points into sychronized methods into a form amenable to that catered by the analysis interface.
	 *
	 * @param set is the collection of enter monitor information to be normalized.
	 *
	 * @pre set != null and set.oclIsKindOf(Collection(Pair(Object, SootMethod)))
	 */
	private void normalizeEntryInformation(final Collection set) {
		final Collection _result = new HashSet();
		final Collection _removed = new HashSet();

		for (final Iterator _i = set.iterator(); _i.hasNext();) {
			final Pair _pair = (Pair) _i.next();
			final Object _first = _pair.getFirst();

			if (!(_first instanceof Stmt)) {
				_removed.add(_pair);

				final SootMethod _sm = (SootMethod) _pair.getSecond();
				final BasicBlock _head = getBasicBlockGraph(_sm).getHead();
				Object _headStmt = null;

				if (_head != null) {
					_headStmt = _head.getLeaderStmt();
				}

				final Pair _special = pairMgr.getPair(_headStmt, _sm);
				specials.add(_special);
				_result.add(_special);
			}
		}
		set.removeAll(_removed);
		set.addAll(_result);
	}

	/**
	 * Normalizes exit monitor information provided in the given set.  This basically converts information pertaining exit
	 * points into sychronized methods into a form amenable to that catered by the analysis interface.
	 *
	 * @param set is the collection of exit monitor information to be normalized.
	 *
	 * @pre set != null and set.oclIsKindOf(Collection(Pair(Object, SootMethod)))
	 */
	private void normalizeExitInformation(final Collection set) {
		final Collection _result = new HashSet();
		final Collection _removed = new HashSet();
		final Collection _tails = new HashSet();

		for (final Iterator _i = set.iterator(); _i.hasNext();) {
			final Pair _pair = (Pair) _i.next();
			final Object _first = _pair.getFirst();

			if (!(_first instanceof Stmt)) {
				_removed.add(_pair);

				final SootMethod _sm = (SootMethod) _pair.getSecond();
				_tails.clear();

				final BasicBlockGraph _graph = getBasicBlockGraph(_sm);
				_tails.addAll(_graph.getPseudoTails());
				_tails.addAll(_graph.getTails());

				for (final Iterator _j = _tails.iterator(); _j.hasNext();) {
					final BasicBlock _tail = (BasicBlock) _j.next();
					Object _tailStmt = null;

					if (_tail != null) {
						_tailStmt = _tail.getTrailerStmt();
					}

					final Pair _special = pairMgr.getPair(_tailStmt, _sm);
					specials.add(_special);
					_result.add(_special);
				}
			}
		}
		set.removeAll(_removed);
		set.addAll(_result);
	}

	/**
	 * Process monitor info.
	 *
	 * @return <code>true</code> if the system has any methods with synchronization; <code>false</code>, otherwise.
	 */
	private boolean processMonitorInfo() {
		boolean _result = false;
		final Collection _reachableMethods = callgraph.getReachableMethods();
		final Iterator _i = _reachableMethods.iterator();
		final int _iEnd = _reachableMethods.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _method = (SootMethod) _i.next();
			final Collection _monitorTriplesIn = monitorInfo.getMonitorTriplesIn(_method);
			final Iterator _j = _monitorTriplesIn.iterator();
			final int _jEnd = _monitorTriplesIn.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Triple _monitor = (Triple) _j.next();
				final Object _enter = _monitor.getFirst();

				if (_enter == null) {
					CollectionsUtilities.putIntoSetInMap(enterMonitors, _method, SYNC_METHOD_PROXY_STMT);
					CollectionsUtilities.putIntoSetInMap(exitMonitors, _method, SYNC_METHOD_PROXY_STMT);
				} else {
					CollectionsUtilities.putIntoSetInMap(enterMonitors, _method, _enter);
					CollectionsUtilities.putIntoSetInMap(exitMonitors, _method, _monitor.getSecond());
				}
			}

			if (_jEnd > 0) {
				_result = true;
				readyMethods.add(_method);
			}
		}
		return _result;
	}

	/**
	 * Processes the system as per to rule 1 and rule 3 as discussed in the report.  For each <code>Object.wait</code> call
	 * site or synchronized block in a method, the dependency is calculated for all dominated statements in the same method.
	 */
	private void processRule1And3() {
		final Collection _processed = new HashSet();
		final IWorkBag _workbag = new HistoryAwareLIFOWorkBag(_processed);
		final Map _method2dependeeMap = collectDependeesInMethods();

		if ((waits.size() == 0 ^ notifies.size() == 0) && LOGGER.isWarnEnabled()) {
			LOGGER.warn("There are wait()s and/or notify()s in this program without corresponding notify()s and/or "
				+ "wait()s that occur in different threads.");
		}

		final Collection _dependents = new HashSet();

		for (final Iterator _i = _method2dependeeMap.keySet().iterator(); _i.hasNext();) {
			final SootMethod _method = (SootMethod) _i.next();
			final BasicBlockGraph _bbGraph = getBasicBlockGraph(_method);
			final Collection _dependees = (Collection) _method2dependeeMap.get(_method);
			final Map _dents2dees = CollectionsUtilities.getMapFromMap(dependent2dependee, _method);

			for (final Iterator _j = _dependees.iterator(); _j.hasNext();) {
				final Stmt _dependee = (Stmt) _j.next();
				BasicBlock _bb = _bbGraph.getEnclosingBlock(_dependee);
				final List _sl = directionSensInfo.getIntraBBDependents(_bb, _dependee);
				_dependents.clear();

				final Pair _pair = pairMgr.getPair(_dependee, _method);
				boolean _shouldContinue =
					recordDependent2DependeeInfo(_dependents, _method, _dependees, _dents2dees, _sl, _pair);

				// Process the successive basic blocks if there was no ready dependence head statement. 
				if (_shouldContinue) {
					_workbag.clear();
					_processed.clear();
					_workbag.addAllWork(directionSensInfo.getFollowersOfBB(_bb));

					while (_workbag.hasWork()) {
						_bb = (BasicBlock) _workbag.getWork();
						_shouldContinue =
							recordDependent2DependeeInfo(_dependents, _method, _dependees, _dents2dees, _bb.getStmtsOf(),
								_pair);

						if (_shouldContinue) {
							_workbag.addAllWork(directionSensInfo.getFollowersOfBB(_bb));
						}
					}
				}

				//add dependee to dependent direction information.
				final Map _dees2dents = CollectionsUtilities.getMapFromMap(dependee2dependent, _method);
				CollectionsUtilities.putAllIntoSetInMap(_dees2dents, _dependee, _dependents);
			}
		}
	}

	/**
	 * Processes the system as per to rule 2 in the report.  For each possible enter- and exit-monitor statements occurring
	 * in different threads, the combination  of these to be  considered is determined by <code>ifRelatedByRule2()</code>.
	 */
	private void processRule2() {
		final Collection _deSet = new HashSet();
		final Collection _dtSet = new HashSet();
		final Collection _tails = new HashSet();
		final Collection _temp = getExitMonitorStmtMethodPairs();

		/*
		 * Iterate thru enter-monitor sites and record dependencies, in both direction, between each exit-monitor sites.
		 */
		for (final Iterator _i = enterMonitors.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final SootMethod _enterMethod = (SootMethod) _entry.getKey();

			for (final Iterator _j = ((Collection) _entry.getValue()).iterator(); _j.hasNext();) {
				final Object _enter = _j.next();

				final Pair _enterPair = pairMgr.getPair(_enter, _enterMethod);
				_deSet.clear();

				// add dependee to dependent information 
				for (final Iterator _k = _temp.iterator(); _k.hasNext();) {
					final Pair _exitPair = (Pair) _k.next();
					_dtSet.clear();

					final Object _exit = _exitPair.getFirst();

					if (ifDependentOnByRule2(_enterPair, _exitPair)) {
						_dtSet.add(_enterPair);
						_deSet.add(_exitPair);

						normalizeEntryInformation(_dtSet);

						final SootMethod _exitMethod = (SootMethod) _exitPair.getSecond();

						if (_exit.equals(SYNC_METHOD_PROXY_STMT)) {
							_tails.clear();

							final BasicBlockGraph _graph = getBasicBlockGraph(_exitMethod);
							_tails.addAll(_graph.getPseudoTails());
							_tails.addAll(_graph.getTails());

							for (final Iterator _l = _tails.iterator(); _l.hasNext();) {
								final BasicBlock _bb = (BasicBlock) _l.next();
								final Map _dees2dents = CollectionsUtilities.getMapFromMap(dependee2dependent, _exitMethod);
								CollectionsUtilities.putAllIntoSetInMap(_dees2dents, _bb.getTrailerStmt(), _dtSet);
							}
						} else {
							final Map _dees2dents = CollectionsUtilities.getMapFromMap(dependee2dependent, _exitMethod);
							CollectionsUtilities.putAllIntoSetInMap(_dees2dents, _exit, _dtSet);
						}
					}
				}

				// add dependent to dependee information
				if (!_deSet.isEmpty()) {
					normalizeExitInformation(_deSet);

					Object _key = _enter;

					if (_enter.equals(SYNC_METHOD_PROXY_STMT)) {
						final BasicBlock _headBB = getBasicBlockGraph(_enterMethod).getHead();
						_key = null;

						if (_headBB != null) {
							_key = _headBB.getLeaderStmt();
						}
					}

					if (_key != null) {
						final Map _dents2dees = CollectionsUtilities.getMapFromMap(dependent2dependee, _enterMethod);
						CollectionsUtilities.putAllIntoSetInMap(_dents2dees, _key, _deSet);
					} else {
						LOGGER.error("How can we record ready dependence for a synchronized method with no head?");
					}
				}
			}
		}
	}

	/**
	 * Processes the system as per to rule 4 in the report.  For each possible wait and notifyXX call-sites in different
	 * threads, the combination  of these to be  considered is determined by <code>ifRelatedByRule4()</code>.
	 */
	private void processRule4() {
		final Collection _dependents = new HashSet();

		/*
		 * Iterate thru wait() call-sites and record dependencies, in both direction, between each notify() call-sites.
		 */
		for (final Iterator _iter = notifies.entrySet().iterator(); _iter.hasNext();) {
			final Map.Entry _nEntry = (Map.Entry) _iter.next();
			final SootMethod _nMethod = (SootMethod) _nEntry.getKey();

			for (final Iterator _j = ((Collection) _nEntry.getValue()).iterator(); _j.hasNext();) {
				final InvokeStmt _notify = (InvokeStmt) _j.next();
				final Pair _nPair = pairMgr.getPair(_notify, _nMethod);
				_dependents.clear();

				// add dependee to dependent information
				for (final Iterator _k = waits.entrySet().iterator(); _k.hasNext();) {
					final Map.Entry _wEntry = (Map.Entry) _k.next();
					final SootMethod _wMethod = (SootMethod) _wEntry.getKey();

					for (final Iterator _l = ((Collection) _wEntry.getValue()).iterator(); _l.hasNext();) {
						final InvokeStmt _wait = (InvokeStmt) _l.next();

						final Pair _wPair = pairMgr.getPair(_wait, _wMethod);

						if (ifDependentOnByRule4(_wPair, _nPair)) {
							final Map _dents2dees = CollectionsUtilities.getMapFromMap(dependent2dependee, _wMethod);
							CollectionsUtilities.putIntoCollectionInMap(_dents2dees, _wait, _nPair,
								CollectionsUtilities.HASH_SET_FACTORY);
							_dependents.add(_wPair);
						}
					}
				}

				// add dependee to dependent information
				if (!_dependents.isEmpty()) {
					final Map _dees2dents = CollectionsUtilities.getMapFromMap(dependee2dependent, _nMethod);
					CollectionsUtilities.putAllIntoCollectionInMap(_dees2dents, _notify, _dependents,
						CollectionsUtilities.HASH_SET_FACTORY);
				}
			}
		}
	}

	/**
	 * Records dependent to dependee information while capturing the dependents for recording information in the other
	 * direction.
	 *
	 * @param dependents is the collection of dependents.  This is an <code>out</code> parameter.
	 * @param method in which the dependees occur.
	 * @param dependees is the collection of dependees.
	 * @param dents2dees is the map which needs to be updated.
	 * @param stmts is the collection of statements to be processed for dependent status.
	 * @param pair is the dependee for which current execution is recording dependence.
	 *
	 * @return <code>true</code> if the following basic blocks of the basic block in which <code>stmts</code> occur should be
	 * 		   considered for dependence; <code>false</code>, otherwise.
	 *
	 * @pre dependents != null and dependents.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
	 * @pre method != null and pair != null and pair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @pre dependees != null and dependees.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
	 * @pre stmts != null and stmts.oclIsKindOf(Sequence(Stmt))
	 * @pre dents2dees != null and dents2dees.oclIsKindOf(Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt, SootMethod))))
	 */
	private boolean recordDependent2DependeeInfo(final Collection dependents, final SootMethod method,
		final Collection dependees, final Map dents2dees, final List stmts, final Pair pair) {
		boolean _shouldContinue;
		_shouldContinue = true;

		// add dependent to dependee direction information.
		for (final Iterator _k = stmts.iterator(); _k.hasNext() && _shouldContinue;) {
			final Stmt _stmt = (Stmt) _k.next();

			CollectionsUtilities.putIntoSetInMap(dents2dees, _stmt, pair);

			// record dependee to dependent direction information
			dependents.add(pairMgr.getPair(_stmt, method));

			/*
			 * In case there is a statement that is a wait() call-site, enter-monitor, or a ready-method
			 * call-site, flag that the following successors should not be considered for this dependence
			 * and break.
			 */
			_shouldContinue = !(dependees.contains(_stmt) || callsReadyMethod(_stmt, method));
		}
		return _shouldContinue;
	}
}

// End of File
