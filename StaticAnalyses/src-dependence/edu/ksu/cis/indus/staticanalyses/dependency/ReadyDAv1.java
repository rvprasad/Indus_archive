
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

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.Pair.PairManager;
import edu.ksu.cis.indus.staticanalyses.support.Util;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
	  extends AbstractProcessor {
		/**
		 * Collects synchronized methods.
		 *
		 * @param method to be preprocessed.
		 */
		public void callback(final SootMethod method) {
			if (method.isSynchronized()) {
				monitorMethods.add(method);
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
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(EnterMonitorStmt.class, this);
			ppc.register(ExitMonitorStmt.class, this);
			ppc.register(InvokeStmt.class, this);
			ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(ProcessingController)
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
	 * @post result->forall(o | o.isOclKindOf(Collection(Stmt)))
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
	 * @post result->forall(o | o.isOclKindOf(Collection(Stmt)))
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
		super.reset();
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
			result.append("\tFor " + entry.getKey() + "[" + entry.getKey().hashCode() + "] there are " + localEdgeCount
				+ " Ready dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " Ready dependence edges exist.");
		return result.toString();
	}

	/**
	 * Checks if the given enter monitor statement is dependent on the exit monitor statement according to rule 2.  The
	 * dependence  is determined based on the relation between the classes  immediately enclosing the given statements
	 * occur.
	 *
	 * @param enterPair is the enter monitor statement.
	 * @param exitPair is the exit monitor statement.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre enterPair.getFirst() != null and exitPair.getFirst() != null
	 */
	protected boolean ifDependentOnByRule2(final Pair enterPair, final Pair exitPair) {
		EnterMonitorStmt enter = (EnterMonitorStmt) enterPair.getFirst();
		ExitMonitorStmt exit = (ExitMonitorStmt) exitPair.getFirst();
		SootClass enterClass = env.getClass(((RefType) enter.getOp().getType()).getClassName());
		SootClass exitClass = env.getClass(((RefType) exit.getOp().getType()).getClassName());
		return Util.isHierarchicallyRelated(enterClass, exitClass);
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
		WorkBag workbag = new WorkBag(WorkBag.LIFO);
		Collection processed = new HashSet();
		Map map = new HashMap();

		if ((rules & RULE_1) != 0) {
			for (Iterator i = enterMonitors.keySet().iterator(); i.hasNext();) {
				SootMethod method = (SootMethod) i.next();
				map.put(method, new HashSet((Collection) enterMonitors.get(method)));
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

		if (waits.size() == 0 ^ notifies.size() == 0) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("There are wait()s and/or notify()s in this program without corresponding notify()s and/or "
					+ "wait()s that occur in different threads.");
			}
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
				dependeeMap.put(o, Collections.EMPTY_LIST);
				temp.add(pairMgr.getOptimizedPair(o, method));
			}
		}

		Collection nSet = new ArrayList();
		Collection xSet = new ArrayList();

		/*
		 * Iterate thru enter-monitor sites and record dependencies, in both direction, between each exit-monitor sites.
		 */
		for (Iterator i = enterMonitors.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod method = (SootMethod) entry.getKey();

			for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				EnterMonitorStmt enter = (EnterMonitorStmt) j.next();
				Pair enterPair = pairMgr.getOptimizedPair(enter, method);
				nSet.clear();

				// add dependee to dependent information 
				for (Iterator k = temp.iterator(); k.hasNext();) {
					Pair exitPair = (Pair) k.next();
					xSet.clear();

					ExitMonitorStmt exit = (ExitMonitorStmt) exitPair.getFirst();

					if (ifDependentOnByRule2(enterPair, exitPair)) {
						xSet.add(enterPair);
						nSet.add(exitPair);
					}

					if (!xSet.isEmpty()) {
						Collection exitSet = (Collection) dependeeMap.get(exit);

						if (exitSet.equals(Collections.EMPTY_LIST)) {
							exitSet = new ArrayList();
							dependeeMap.put(exit, exitSet);
						}
						exitSet.addAll(xSet);
					}
				}

				// add dependent to dependee information
				if (nSet.isEmpty()) {
					dependentMap.put(enter, Collections.EMPTY_LIST);
				} else {
					dependentMap.put(enter, new ArrayList(nSet));
				}
			}
		}
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
		for (Iterator iter = waits.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			SootMethod wMethod = (SootMethod) entry.getKey();

			for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				InvokeStmt wait = (InvokeStmt) j.next();
				Pair wPair = pairMgr.getOptimizedPair(wait, wMethod);
				dependents.clear();

				// add dependee to dependent information
				for (Iterator k = notifies.entrySet().iterator(); k.hasNext();) {
					entry = (Map.Entry) k.next();

					SootMethod nMethod = (SootMethod) entry.getKey();

					for (Iterator l = ((Collection) entry.getValue()).iterator(); l.hasNext();) {
						InvokeStmt notify = (InvokeStmt) l.next();

						Pair nPair = pairMgr.getOptimizedPair(notify, nMethod);

						if (ifDependentOnByRule4(wPair, nPair)) {
							Collection temp = (Collection) dependeeMap.get(notify);

							if (temp == null) {
								temp = new ArrayList();
								dependeeMap.put(notify, temp);
							}
							temp.add(wPair);
							dependents.add(nPair);
						}
					}
				}

				// add dependent to dependee information
				if (dependents.size() == 0) {
					dependentMap.put(wait, Collections.EMPTY_LIST);
				} else {
					dependentMap.put(wait, new ArrayList(dependents));
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
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
   - Ripple effect of changes to ProcessingController.
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
