
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
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
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports">A Formal  Study of Slicing for Multi-threaded Program with
 * JVM Concurrency Primitives"</a>.
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
	 * This is the <code>java.lang.Object.wait()</code> method.
	 */
	SootMethod waitMethod;

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
	private final boolean interProcedural;

	/**
	 * Creates a new ReadyDAv1 object.
	 *
	 * @param acrossMethodCalls <code>true</code> indicates that any call-site leading to wait() call-site or enter-monitor
	 * 		  statement should be considered as a ready dependeee; <code>false</code>, otherwise. This only affects how rule
	 * 		  1 and 3 are interpreted.
	 */
	public ReadyDAv1(final boolean acrossMethodCalls) {
		preprocessor = new PreProcessor();
		interProcedural = acrossMethodCalls;
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

					if (waitMethod.equals(callee)) {
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
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(EnterMonitorStmt.class, this);
			ppc.unregister(ExitMonitorStmt.class, this);
			ppc.unregister(InvokeStmt.class, this);
		}
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
		this.rules = rulesParam;
	}

	/**
	 * Calculates ready dependency for the methods provided at initialization.  It considers only the rules specified by via
	 * <code>setRules</code> method. By default, all rules are considered for the analysis.
	 *
	 * @return <code>true</code> as this analysis completes in a single run.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		if (!threadgraph.getStartSites().isEmpty()) {
			if (!monitorMethods.isEmpty() && (rules & (RULE_1 | RULE_3)) != 0) {
				processRule1And3();
			}

			if (!waits.isEmpty() && !notifies.isEmpty() && (rules & RULE_2) != 0) {
				processRule2();
			}

			if (!waits.isEmpty() && !notifies.isEmpty() && (rules & RULE_4) != 0) {
				processRule4();
			}

			//fixupInterMethodReadyDA();
		}
		return true;
	}

	/**
	 * Resets the internal data structures.  <i>The rules are not reset.</i>
	 */
	public void reset() {
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
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependeeMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			localEdgeCount = 0;

			for (Iterator j = ((Map) entry.getValue()).entrySet().iterator(); j.hasNext();) {
				Map.Entry entry2 = (Map.Entry) j.next();

				for (Iterator k = ((Collection) entry2.getValue()).iterator(); k.hasNext();) {
					temp.append("\t\t" + entry2.getKey() + " --> " + k.next() + "\n");
				}
				localEdgeCount += ((Collection) entry2.getValue()).size();
			}
			result.append("\tFor " + entry.getKey() + " there are " + localEdgeCount + " Ready dependence edges.\n");
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

		if (waitMethod == null) {
			for (Iterator i = env.getClasses().iterator(); i.hasNext();) {
				SootClass sc = (SootClass) i.next();

				if (sc.getName().equals("java.lang.Object")) {
					List temp = sc.getMethods();

					for (Iterator j = temp.iterator(); j.hasNext();) {
						SootMethod sm = (SootMethod) j.next();

						if (sm.getName().equals("wait")) {
							waitMethod = sm;
						}
					}

					notifyMethods = new ArrayList();
					notifyMethods.add(sc.getMethod("notify"));
					notifyMethods.add(sc.getMethod("notifyAll"));
					notifyMethods = Collections.unmodifiableCollection(notifyMethods);
				}
			}
		}

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

		if (interProcedural && stmt.containsInvokeExpr()) {
			if (!CollectionUtils.intersection(readyMethods, callgraph.getMethodsReachableFrom(stmt, caller)).isEmpty()) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Fixes inter-method ready dependencies.  All statements in a caller which are pre-dominated by a call site which leads
	 * to a <code>Object.wait</code> call or a synchronized block are recorded as ready dependent on the call site.
	 */

	/*
	   private void fixupInterMethodReadyDA() {
	       Collection methodsToProcess = new HashSet();
	       if ((rules & (RULE_3 | RULE_4)) != 0) {
	           methodsToProcess.addAll(waits.keySet());
	       }
	       if ((rules & (RULE_1 | RULE_2)) != 0) {
	           for (Iterator i = monitorMethods.iterator(); i.hasNext();) {
	               methodsToProcess.add(i.next());
	           }
	       }
	       WorkBag progPoints = new WorkBag(WorkBag.FIFO);
	       for (Iterator i = methodsToProcess.iterator(); i.hasNext();) {
	           SootMethod method = (SootMethod) i.next();
	           progPoints.addAllWork(callgraph.getCallers(method));
	       }
	       WorkBag workbag = new WorkBag(WorkBag.LIFO);
	       Collection temp = new HashSet();
	       Collection processed = new HashSet();
	       Collection col = new ArrayList();
	       while (progPoints.hasWork()) {
	           CallTriple progPoint = (CallTriple) progPoints.getWork();
	           processed.add(progPoint);
	           SootMethod caller = progPoint.getMethod();
	           Stmt dependee = progPoint.getStmt();
	           BasicBlockGraph bbGraph = getBasicBlockGraph(caller);
	           BasicBlock bb = bbGraph.getEnclosingBlock(dependee);
	           col.clear();
	           col.addAll(bb.getStmtFrom(getStmtList(caller).indexOf(dependee)));
	           col.remove(dependee);
	           for (Iterator i = col.iterator(); i.hasNext();) {
	               dependeeMap.put(i.next(), dependee);
	           }
	           Collection dependents = (Collection) dependentMap.get(dependee);
	           if (dependents == null) {
	               dependents = new ArrayList();
	           }
	           dependents.addAll(col);
	           workbag.addAllWork(bb.getSuccsOf());
	           temp.clear();
	           while (workbag.hasWork()) {
	               bb = (BasicBlock) workbag.getWork();
	               Collection stmts = bb.getStmtsOf();
	               for (Iterator i = stmts.iterator(); i.hasNext();) {
	                   dependeeMap.put(i.next(), dependee);
	               }
	               col.addAll(stmts);
	               temp.add(bb);
	               for (Iterator i = bb.getSuccsOf().iterator(); i.hasNext();) {
	                   BasicBlock block = (BasicBlock) i.next();
	                   if (!temp.contains(block)) {
	                       workbag.addWork(block);
	                   }
	               }
	           }
	           for (Iterator i = callgraph.getCallers(caller).iterator(); i.hasNext();) {
	               Object pp = i.next();
	               if (!processed.contains(pp)) {
	                   progPoints.addWork(pp);
	               }
	           }
	       }
	   }
	 */

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
				map.put(method, new ArrayList((Collection) enterMonitors.get(method)));
			}
		}

		if ((rules & RULE_3) != 0) {
			for (Iterator i = waits.keySet().iterator(); i.hasNext();) {
				SootMethod method = (SootMethod) i.next();

				if (map.get(method) != null) {
					((Collection) map.get(method)).addAll((Collection) waits.get(method));
				} else {
					Collection tmp = new ArrayList((Collection) waits.get(method));
					map.put(method, tmp);
				}
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
						t = new ArrayList();
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
								t = new ArrayList();
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
				temp.add(pairMgr.getOptimizedPair(method, o));
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

						if (exitSet == null) {
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
