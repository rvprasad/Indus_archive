
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

import edu.ksu.cis.indus.common.CollectionsModifier;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

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

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
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
 * By default, all rules are considered for the analysis.  This can be changed via <code>setRules()</code>.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependeeMap.oclIsKindOf(Map(Stmt, Collection(Pair(Stmt, SootMethod))))
 * @invariant dependentMap.oclIsKindOf(Map(Stmt, Collection(Pair(Stmt, SootMethod))))
 */
public class ReadyDAv1
  extends DependencyAnalysis {
	/**
	 * The  collection of <code>notifyXX</code> methods as available in the <code>Object</code> class.
	 *
	 * @invariant notifyMethods->forall(o | o.oclIsTypeOf(SootMethod))
	 */
	static Collection notifyMethods;

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
	 * This is the <code>java.lang.Object.wait()</code> method.
	 *
	 * @invariant waitMethods.oclIsKindOf(Collection(SootMethod))
	 */
	static Collection waitMethods;

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ReadyDAv1.class);

	/**
	 * A token used to represent the nonexistent monitor entry/exit statements in synchronized methods.
	 */
	protected static final Object SYNC_METHOD_PROXY_STMT = "SYNC_METHOD_PROXY_STMT";

	/**
	 * This is the logical OR of the <code>RULE_XX</code> as provided by the user.  This indicates the rules which need to be
	 * considered while calculating ready dependency.
	 */
	protected int rules = RULE_1 | RULE_2 | RULE_3 | RULE_4;

	/**
	 * This stores the methods that are synchronized or have synchronized blocks in them.
	 *
	 * @invariant monitorMethods.oclIsKindOf(Collection(SootMethod))
	 */
	final Collection monitorMethods = new HashSet();

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
	 * This provides call graph of the system being analyzed.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * This provides information such as the classes occurring in the system being analyzed.
	 */
	private IEnvironment env;

	/**
	 * This provides call graph of the system being analyzed.
	 */
	private IThreadGraphInfo threadgraph;

	/**
	 * This manages pairs.
	 */
	private PairManager pairMgr;

	/**
	 * This indicates if dependence should be considered across call-sites.  Depending on the application, one may choose to
	 * ignore ready dependence across call-sites and rely on other dependence analysis to include the call-site.  This only
	 * affects how rule 1 and 3 are interpreted.
	 */
	private boolean considerCallSites;

	/**
	 * Creates a new ReadyDAv1 object.
	 */
	public ReadyDAv1() {
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
	  extends AbstractValueAnalyzerBasedProcessor {
		/**
		 * Collects synchronized methods.
		 *
		 * @param method to be preprocessed.
		 */
		public void callback(final SootMethod method) {
			if (method.isSynchronized()) {
				monitorMethods.add(method);
				CollectionsModifier.putIntoCollectionInMap(enterMonitors, method, SYNC_METHOD_PROXY_STMT, new HashSet());
				CollectionsModifier.putIntoCollectionInMap(exitMonitors, method, SYNC_METHOD_PROXY_STMT, new HashSet());
			}
		}

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
			SootMethod method = context.getCurrentMethod();
			Map map = null;

			if (stmt instanceof EnterMonitorStmt) {
				map = enterMonitors;
				monitorMethods.add(method);
			} else if (stmt instanceof ExitMonitorStmt) {
				map = exitMonitors;
				monitorMethods.add(method);
			}

			if (map != null) {
				Collection temp;

				if (map.containsKey(method)) {
					temp = (Collection) map.get(method);
				} else {
					temp = new HashSet();
					map.put(method, temp);
				}
				temp.add(stmt);
			} else {
				// InvokeStmt branch
				InvokeExpr expr = null;

				if (stmt instanceof InvokeStmt) {
					expr = stmt.getInvokeExpr();
				}

				if (expr != null && expr instanceof VirtualInvokeExpr) {
					VirtualInvokeExpr invokeExpr = (VirtualInvokeExpr) expr;
					SootMethod callee = invokeExpr.getMethod();

					if (waitMethods.contains(callee)) {
						map = waits;
					} else if (notifyMethods.contains(callee)) {
						map = notifies;
					}

					if (map != null) {
						Collection temp;

						if (map.containsKey(method)) {
							temp = (Collection) map.get(method);
						} else {
							temp = new HashSet();
							map.put(method, temp);
						}
						temp.add(stmt);
					}
				}
			}
		}

		/**
		 * Collects all the methods that encloses an enter-monitor statement or a call to <code>wait()</code> method.
		 */
		public void consolidate() {
			readyMethods.addAll(monitorMethods);
			readyMethods.addAll(waits.keySet());
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(EnterMonitorStmt.class, this);
			ppc.register(ExitMonitorStmt.class, this);
			ppc.register(InvokeStmt.class, this);
			ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(EnterMonitorStmt.class, this);
			ppc.unregister(ExitMonitorStmt.class, this);
			ppc.unregister(InvokeStmt.class, this);
			ppc.unregister(this);
		}
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
	 * Returns the statements on which the <code>dependentStmt</code> depends on.
	 *
	 * @param dependentStmt is the statement for which the dependee info is requested.
	 * @param context is ignored.
	 *
	 * @return a collection of statement.
	 *
	 * @pre dependentStmt.isOclKindOf(Stmt)
	 * @post result.isOclKindOf(Collection(Pair(Stmt, Method)))
	 *
	 * @see DependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object context) {
		Collection result = (Collection) dependeeMap.get(dependentStmt);

		if (result != null) {
			result = Collections.unmodifiableCollection(result);
		} else {
			result = Collections.EMPTY_SET;
		}
		return result;
	}

	/**
	 * Returns the statements which depend on <code>dependeeStmt</code>.
	 *
	 * @param dependeeStmt is the statement for which the dependent info is requested.
	 * @param context is ignored.
	 *
	 * @return a collection of statement.
	 *
	 * @pre dependeeStmt.isOclKindOf(Stmt)
	 * @post result.isOclKindOf(Collection(Pair(Stmt, SootMethod)))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependents( java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object context) {
		Collection result = (Collection) dependentMap.get(dependeeStmt);

		if (result != null) {
			result = Collections.unmodifiableCollection(result);
		} else {
			result = Collections.EMPTY_SET;
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getId()
	 */
	public Object getId() {
		return DependencyAnalysis.READY_DA;
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
	 * Calculates ready dependency for the methods provided at initialization.  It considers only the rules specified by via
	 * <code>setRules</code> method. By default, all rules are considered for the analysis.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Ready Dependence [" + this.getClass() + " processing");
		}

		if (!threadgraph.getStartSites().isEmpty()) {
			if (!monitorMethods.isEmpty() && (rules & (RULE_1 | RULE_3)) != 0) {
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

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Ready Dependence processing");
		}

		stable = true;
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
		waits.clear();
		notifies.clear();
		monitorMethods.clear();
	}

	/**
	 * Returns a stringified representation of the analysis information.
	 *
	 * @return a string containing the analysis info.
	 *
	 * @post result != null
	 */
	public String toString() {
		StringBuffer result =
			new StringBuffer("Statistics for Ready dependence as calculated by " + getClass().getName() + "\n");
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependeeMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Object dependent = entry.getKey();

			for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				Object dependee = j.next();
				temp.append("\t\t" + dependent + " --> " + dependee + "\n");
			}

			int localEdgeCount = ((Collection) entry.getValue()).size();
            final Object _key = entry.getKey();
			result.append("\tFor " + _key + "[" + (_key != null ? _key.hashCode() : 0) + "] there are " + localEdgeCount
				+ " Ready dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " Ready dependence edges exist.");
		return result.toString();
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
		SootClass _enterClass;
		SootClass _exitClass;
		final SootMethod _sm1 = (SootMethod) enterPair.getSecond();
		final SootMethod _sm2 = (SootMethod) exitPair.getSecond();

		boolean _syncedStaticMethod1 = false;
		boolean _syncedStaticMethod2 = false;
		final Object _o1 = enterPair.getFirst();

		if (_o1.equals(SYNC_METHOD_PROXY_STMT)) {
			_enterClass = _sm1.getDeclaringClass();
			_syncedStaticMethod1 = _sm1.isStatic();
		} else {
			final EnterMonitorStmt _enter = (EnterMonitorStmt) _o1;
			_enterClass = env.getClass(((RefType) _enter.getOp().getType()).getClassName());
		}

		final Object _o2 = exitPair.getFirst();

		if (_o2.equals(SYNC_METHOD_PROXY_STMT)) {
			_exitClass = _sm2.getDeclaringClass();
			_syncedStaticMethod2 = _sm2.isStatic();
		} else {
			final ExitMonitorStmt _exit = (ExitMonitorStmt) _o2;
			_exitClass = env.getClass(((RefType) _exit.getOp().getType()).getClassName());
		}

		boolean _result;

		if (_syncedStaticMethod1 && _syncedStaticMethod2) {
			// if we are dealing with synchronized static methods, then they will lock the class object, hence, inheritance
			// relation should not be considered.            
			_result = _enterClass.equals(_exitClass);
		} else if (_syncedStaticMethod1 ^ _syncedStaticMethod2) {
			/*
			 * if only one of the methods is static and synchronized then we cannot determine RDA as it is possible that
			 * the monitor in the non-static method may actually be on the class object of the class in  which the static
			 * method is defined.  There are many combinations which can be pruned.  No time now. THINK
			 */
			_result = true;
		} else {
			_result = Util.isHierarchicallyRelated(_enterClass, _exitClass);
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
		InvokeStmt notify = (InvokeStmt) nPair.getFirst();
		SootClass notifyClass =
			env.getClass(((RefType) ((InstanceInvokeExpr) notify.getInvokeExpr()).getBase().getType()).getClassName());
		InvokeStmt wait = (InvokeStmt) wPair.getFirst();
		SootClass waitClass =
			env.getClass(((RefType) ((InstanceInvokeExpr) wait.getInvokeExpr()).getBase().getType()).getClassName());
		return Util.isHierarchicallyRelated(notifyClass, waitClass);
	}

	/**
	 * Extracts information provided by environment at initialization time.
	 *
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 * 		   <code>info</code> member.
	 *
	 * @pre info.get(IEnvironment.ID) != null and info.get(ICallGraphInfo.ID) != null and info.get(IThreadGraphInfo.ID) !=
	 * 		null and info.get(PairManager.ID) != null
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		env = (IEnvironment) info.get(IEnvironment.ID);

		if (env == null) {
			throw new InitializationException(IEnvironment.ID + " was not provided in info.");
		}

		if (waitMethods == null) {
			for (Iterator i = env.getClasses().iterator(); i.hasNext();) {
				SootClass sc = (SootClass) i.next();

				if (sc.getName().equals("java.lang.Object")) {
					waitMethods = new ArrayList();
					waitMethods.add(sc.getMethod("void wait()"));
					waitMethods.add(sc.getMethod("void wait(long)"));
					waitMethods.add(sc.getMethod("void wait(long,int)"));
					waitMethods = Collections.unmodifiableCollection(waitMethods);

					notifyMethods = new ArrayList();
					notifyMethods.add(sc.getMethodByName("notify"));
					notifyMethods.add(sc.getMethodByName("notifyAll"));
					notifyMethods = Collections.unmodifiableCollection(notifyMethods);
				}
			}
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
		boolean result = false;

		if (considerCallSites && stmt.containsInvokeExpr()) {
			if (!CollectionUtils.intersection(readyMethods, callgraph.getMethodsReachableFrom(stmt, caller)).isEmpty()) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Processes the system as per to rule 1 and rule 3 as discussed in the report.  For each <code>Object.wait</code> call
	 * site or synchronized block in a method, the dependency is calculated for all dominated statements in the same method.
	 */
	private void processRule1And3() {
		Collection temp = new HashSet();
		IWorkBag workbag = new LIFOWorkBag();
		Collection processed = new HashSet();
		Map map = new HashMap();

		if ((rules & RULE_1) != 0) {
			for (Iterator i = enterMonitors.keySet().iterator(); i.hasNext();) {
				SootMethod method = (SootMethod) i.next();
				final Collection _col = new HashSet((Collection) enterMonitors.get(method));

				if (method.isSynchronized()) {
					_col.remove(SYNC_METHOD_PROXY_STMT);
				}
				map.put(method, _col);
			}
		}

		if ((rules & RULE_3) != 0) {
			for (Iterator i = waits.keySet().iterator(); i.hasNext();) {
				SootMethod method = (SootMethod) i.next();

				if (map.get(method) != null) {
					((Collection) map.get(method)).addAll((Collection) waits.get(method));
				} else {
					Collection tmp = new HashSet((Collection) waits.get(method));
					map.put(method, tmp);
				}
			}
		}

		if ((waits.size() == 0 ^ notifies.size() == 0) && LOGGER.isWarnEnabled()) {
			LOGGER.warn("There are wait()s and/or notify()s in this program without corresponding notify()s and/or "
				+ "wait()s that occur in different threads.");
		}

		for (Iterator i = map.keySet().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			List stmts = getStmtList(method);
			BasicBlockGraph bbGraph = getBasicBlockGraph(method);
			Collection dependees = (Collection) map.get(method);

			for (Iterator j = dependees.iterator(); j.hasNext();) {
				Stmt dependee = (Stmt) j.next();
				BasicBlock bb = bbGraph.getEnclosingBlock(dependee);
				List sl = bb.getStmtFrom(stmts.indexOf(dependee));
				sl.remove(0);  // remove the dependee from the list
				temp = new HashSet();

				boolean shouldContinue = true;
				Pair pair = pairMgr.getOptimizedPair(dependee, method);

				// add dependee to dependent direction information.
				for (Iterator k = sl.iterator(); k.hasNext();) {
					Stmt stmt = (Stmt) k.next();
					Collection t = (Collection) dependeeMap.get(stmt);

					if (t == null) {
						t = new HashSet();
						dependeeMap.put(stmt, t);
					}
					t.add(pair);

					// add dependent to dependee direction information
					temp.add(pairMgr.getOptimizedPair(stmt, method));

					/*
					 * In case there is a statement that is a wait() call-site, enter-monitor, or a ready-method call-site,
					 * flag that the following successors should not be considered for this dependence and break.
					 */
					if (dependees.contains(stmt) || callsReadyMethod(stmt, method)) {
						shouldContinue = false;
						break;
					}
				}

				// Process the successive basic blocks if there was no ready dependence head statement. 
				if (shouldContinue) {
					workbag.clear();
					processed.clear();
					workbag.addAllWork(bb.getSuccsOf());

					while (workbag.hasWork()) {
						bb = (BasicBlock) workbag.getWork();
						shouldContinue = true;

						// add dependee to dependent direction information.
						for (Iterator k = bb.getStmtsOf().iterator(); k.hasNext();) {
							Stmt stmt = (Stmt) k.next();
							Collection t = (Collection) dependeeMap.get(stmt);

							if (t == null) {
								t = new HashSet();
								dependeeMap.put(stmt, t);
							}
							t.add(pair);

							// add dependent to dependee direction information
							temp.add(pairMgr.getOptimizedPair(stmt, method));

							/*
							 * In case there is a statement that is a wait() call-site, enter-monitor, or a ready-method
							 * call-site , flag that the following successors should not be considered for this dependence
							 * and break.
							 */
							if (dependees.contains(stmt) || callsReadyMethod(stmt, method)) {
								shouldContinue = false;
								break;
							}
						}

						if (!processed.contains(bb) && shouldContinue) {
							workbag.addAllWork(bb.getSuccsOf());
						}
						processed.add(bb);
					}
				}

				//add dependee to dependent direction information.
				dependentMap.put(dependee, temp);
			}
		}
	}

	/**
	 * Processes the system as per to rule 2 in the report.  For each possible enter- and exit-monitor statements occurring
	 * in different threads, the combination  of these to be  considered is determined by <code>ifRelatedByRule2()</code>.
	 */
	private void processRule2() {
		Collection temp = new HashSet();

		for (Iterator i = exitMonitors.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Object method = entry.getKey();

			for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				Object o = j.next();
				temp.add(pairMgr.getOptimizedPair(o, method));
			}
		}

		final Collection _nSet = new HashSet();
		final Collection _xSet = new HashSet();
		final Collection _tails = new HashSet();
		/*
		 * Iterate thru enter-monitor sites and record dependencies, in both direction, between each exit-monitor sites.
		 */
		for (Iterator i = enterMonitors.entrySet().iterator(); i.hasNext();) {
			Map.Entry _entry = (Map.Entry) i.next();
			SootMethod _enterMethod = (SootMethod) _entry.getKey();

			for (Iterator j = ((Collection) _entry.getValue()).iterator(); j.hasNext();) {
				final Object _enter = j.next();

				final Pair _enterPair = pairMgr.getOptimizedPair(_enter, _enterMethod);
				_nSet.clear();

				// add dependee to dependent information 
				for (Iterator k = temp.iterator(); k.hasNext();) {
					final Pair _exitPair = (Pair) k.next();
					_xSet.clear();

					final Object _exit = _exitPair.getFirst();

					if (ifDependentOnByRule2(_enterPair, _exitPair)) {
						_xSet.add(_enterPair);
						_nSet.add(_exitPair);
					}

					if (!_xSet.isEmpty()) {
                        normalizeEntryInformation(_xSet);
						if (_exit.equals(SYNC_METHOD_PROXY_STMT)) {
                            _tails.clear();
                            final BasicBlockGraph _graph = getBasicBlockGraph((SootMethod)_exitPair.getSecond());
                            _tails.addAll(_graph.getPseudoTails());
                            _tails.addAll(_graph.getTails());
                            for (final Iterator _l = _tails.iterator(); _l.hasNext(); ) {
                                final BasicBlock _bb = (BasicBlock) _l.next();
                                CollectionsModifier.putAllIntoCollectionInMap(dependeeMap, _bb.getTrailerStmt(), _xSet, new HashSet());
                            }
                            //_key = exitPair.getSecond();
						} else {
                            CollectionsModifier.putAllIntoCollectionInMap(dependeeMap, _exit, _xSet, new HashSet());
                            //_key = exit;
						}
                        //CollectionsModifier.putAllIntoCollectionInMap(dependeeMap, _key, xSet, new HashSet());
					}
				}

				// add dependent to dependee information
				if (!_nSet.isEmpty()) {
					normalizeExitInformation(_nSet);
					if (_enter.equals(SYNC_METHOD_PROXY_STMT)) {
                        final BasicBlock _headBB = getBasicBlockGraph(_enterMethod).getHead();
                        Stmt _head = null;
                        if (_headBB != null)
                            _head = _headBB.getLeaderStmt();
                        CollectionsModifier.putAllIntoCollectionInMap(dependeeMap, _head, _nSet, new HashSet());
                        //_key = enterPair.getSecond();                        
					} else {
                        CollectionsModifier.putAllIntoCollectionInMap(dependentMap, _enter, _nSet, new HashSet());
						//_key = enter;
					}
                    //CollectionsModifier.putAllIntoCollectionInMap(dependentMap, _key, nSet, new HashSet());
				}
			}
		}
	}

    /**
     * DOCUMENT ME!
     * 
     * @param set
     */
    private void normalizeExitInformation(final Collection set) {
        final Collection _result = new HashSet();
        final Collection _removed = new HashSet();
        final Collection _tails = new HashSet();
        for (final Iterator _i = set.iterator(); _i.hasNext(); ) {
            final Pair _pair = (Pair) _i.next();
            final Object _first = _pair.getFirst();
            if (!(_first instanceof Stmt)) {
                _removed.add(_pair);
                final SootMethod _sm = (SootMethod)_pair.getSecond();
                _tails.clear();
                final BasicBlockGraph _graph = getBasicBlockGraph(_sm);
                _tails.addAll(_graph.getPseudoTails());
                _tails.addAll(_graph.getTails());
                for (final Iterator _j = _tails.iterator(); _j.hasNext(); ) {
                    final BasicBlock _tail = (BasicBlock) _j.next();
                    final Object _tailStmt = _tail.getTrailerStmt();
                    _result.add(pairMgr.getOptimizedPair(_tailStmt, _sm));                    
                }

            }
        }
        set.removeAll(_removed);
        set.addAll(_result);
    }
    
	/**
     * DOCUMENT ME!
     * 
     * @param set
     */
    private void normalizeEntryInformation(final Collection set) {
        final Collection _result = new HashSet();
        final Collection _removed = new HashSet();
        for (final Iterator _i = set.iterator(); _i.hasNext(); ) {
            final Pair _pair = (Pair) _i.next();
            final Object _first = _pair.getFirst();
            if (!(_first instanceof Stmt)) {
                _removed.add(_pair);
                final SootMethod _sm = (SootMethod)_pair.getSecond();
                final BasicBlock _head = getBasicBlockGraph(_sm).getHead();
                Object _headStmt = null;
                if (_head != null) {
                    _headStmt = _head.getLeaderStmt();
                _result.add(pairMgr.getOptimizedPair(_headStmt, _sm));
                }
            }
        }
        set.removeAll(_removed);
        set.addAll(_result);
    }

    /**
	 * Processes the system as per to rule 4 in the report.  For each possible wait and notifyXX call-sites in different
	 * threads, the combination  of these to be  considered is determined by <code>ifRelatedByRule4()</code>.
	 */
	private void processRule4() {
		Collection dependents = new HashSet();

		/*
		 * Iterate thru wait() call-sites and record dependencies, in both direction, between each notify() call-sites.
		 */
		for (Iterator iter = notifies.entrySet().iterator(); iter.hasNext();) {
			final Map.Entry _nEntry = (Map.Entry) iter.next();
			SootMethod nMethod = (SootMethod) _nEntry.getKey();

			for (Iterator j = ((Collection) _nEntry.getValue()).iterator(); j.hasNext();) {
				InvokeStmt notify = (InvokeStmt) j.next();
				Pair nPair = pairMgr.getOptimizedPair(notify, nMethod);
				dependents.clear();

				// add dependee to dependent information
				for (Iterator k = waits.entrySet().iterator(); k.hasNext();) {
					final Map.Entry _wEntry = (Map.Entry) k.next();

					SootMethod wMethod = (SootMethod) _wEntry.getKey();

					for (Iterator l = ((Collection) _wEntry.getValue()).iterator(); l.hasNext();) {
						InvokeStmt wait = (InvokeStmt) l.next();

						Pair wPair = pairMgr.getOptimizedPair(wait, wMethod);

						if (ifDependentOnByRule4(wPair, nPair)) {
							Collection temp = (Collection) dependeeMap.get(wait);

							if (temp == null) {
								temp = new ArrayList();
								dependeeMap.put(wait, temp);
							}
							temp.add(nPair);
							dependents.add(wPair);
						}
					}
				}

				// add dependent to dependee information
				if (!dependents.isEmpty()) {
					dependentMap.put(notify, new ArrayList(dependents));
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.39  2004/01/21 13:52:12  venku
   - documentation.

   Revision 1.38  2004/01/21 13:44:09  venku
   - made ready dependence to consider synchronized methods as well.
   - ReadyDAv2 uses escape information for both sorts of inter-thread
     ready DA.
   - ReadyDAv3 uses escape and object flow information for
     monitor based inter-thread ready DA while using symbol-based
     escape information for wait/notify based inter-thread ready DA.

   Revision 1.37  2004/01/20 16:50:16  venku
   - inadvertently, the dependency between waits and notify was
     recorded in the reverse direction. FIXED.
   Revision 1.36  2004/01/18 00:02:01  venku
   - more logging info.
   Revision 1.35  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.34  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.33  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.32  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.31  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.30  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.29  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.28  2003/11/10 03:17:18  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   - ripple effect.
   Revision 1.27  2003/11/10 02:25:03  venku
   - coding convention.
   Revision 1.26  2003/11/10 01:22:25  venku
   - documentation.
   Revision 1.25  2003/11/06 05:31:07  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.24  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.23  2003/11/05 09:29:51  venku
   - ripple effect of splitting IWorkBag.
   Revision 1.22  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.
   Revision 1.21  2003/09/29 06:40:51  venku
   - redundant call to super.reset was deleted.
   Revision 1.20  2003/09/29 06:35:48  venku
   - using null as values in maps and then to resolve during information
     request is cheaper.  FIXED.
   Revision 1.19  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.18  2003/09/24 15:52:53  venku
   - documentation.
   Revision 1.17  2003/09/24 15:41:33  venku
   - provided a getter method for rules.
   Revision 1.16  2003/09/12 22:33:08  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
   Revision 1.15  2003/09/10 11:50:23  venku
   - formatting.
   Revision 1.14  2003/09/10 11:49:30  venku
   - documentation change.
   Revision 1.13  2003/09/08 02:25:25  venku
   - Ripple effect of changes to ValueAnalyzerBasedProcessingController.
   Revision 1.12  2003/08/27 12:41:30  venku
   It is possible that in ill balanced wait/notify lead to a situation
   where there are no entities to match them, in particular, when
   there are single wait or notify, the error was not be flagged.  FIXED.
   It now flags a log error indicating the source has anamolies.
   Revision 1.11  2003/08/26 16:54:33  venku
   exit sets are initialized to EMPTY_LIST rather than null.  This
   was overlooked when they were set for the first time.  FIXED.
   Revision 1.10  2003/08/25 11:47:37  venku
   Fixed minor glitches.
   Revision 1.9  2003/08/25 10:04:04  venku
   Renamed setInterProcedural() to setConsiderCallSites().
   Revision 1.8  2003/08/25 09:15:51  venku
   Initialization of interProcedural was missing in ReadyDAv1.
   Ripple effect of this and previous change in ReadyDAv1/2 in RDADriver.
   Revision 1.7  2003/08/25 09:04:31  venku
   It was not a good decision to decide interproceduralness of the
   analyses at construction.  Hence, it now can be controlled via public
   method setInterprocedural().
   Ripple effect.
   Revision 1.6  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.5  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.4  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.2  2003/08/09 23:33:30  venku
    - Enabled ready dependency to be interprocedural.
    - Utilized containsXXX() method in Stmt.
 */
