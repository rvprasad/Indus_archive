
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.dependency;

import ca.mcgill.sable.soot.Modifier;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.EnterMonitorStmt;
import ca.mcgill.sable.soot.jimple.ExitMonitorStmt;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtList;

import edu.ksu.cis.bandera.staticanalyses.InitializationException;
import edu.ksu.cis.bandera.staticanalyses.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.AbstractProcessor;
import edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo.CallTriple;
import edu.ksu.cis.bandera.staticanalyses.interfaces.Environment;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.Pair.PairManager;
import edu.ksu.cis.bandera.staticanalyses.support.Util;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class provides ready dependency information.  This implementation refers to the technical report <i>"A Formal  Study
 * of Slicing for Multi-threaded Program with JVM Concurrency Primitives"</i> by John Hatcliff, James Corbett, Matthew
 * Dwyer, Stefan Sokolowski, and Hongjun Zheng which is available from <a href="http://www.cis.ksu.edu/santos/">Santos
 * Laboratory</a>, Kansas State University.  We refer to this report when we say "report" in this documentation.
 * 
 * <p>
 * The dependence information is stored as follows:  For each
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ReadyDAv2
  extends DependencyAnalysis {
	/**
	 * The  collection of <code>notifyXX</code> methods as available in the <code>Object</code> class.
	 *
	 * @invariant notifyMethods->forall(o | o.oclType = SootMethod)
	 */
	static Collection notifyMethods;

	/**
	 * The collection of <code>wait</code> methods as available in the <code>Object</code> class.
	 *
	 * @invariant waitMethods->forall(o | o.oclType = SootMethod)
	 */
	static Collection waitMethods;

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
	 * This maps methods to collection of enter monitor statements.
	 *
	 * @invariant enterMonitors.oclIsKindOf(Map(SootMethod, Set(EnterMonitorStmt)))
	 */
	final Map enterMonitors = new HashMap();

	/**
	 * This maps methods to collection of exit monitor statements.
	 *
	 * @invariant exitMonitors.oclIsKindOf(Map(SootMethod, Set(ExitMonitorStmt)))
	 */
	final Map exitMonitors = new HashMap();

	/**
	 * This maps methods to <code>Object.notifyXX</code> method calls in them.
	 *
	 * @invariant notifies.oclIsKindOf(Map(SootMethod, Set(NonStaticInvokeExpr)))
	 */
	final Map notifies = new HashMap();

	/**
	 * This maps methods to <code>Object.wait</code> method calls in them.
	 *
	 * @invariant wait.oclIsKindOf(Map(SootMethod, Set(NonStaticInvokeExpr)))
	 */
	final Map waits = new HashMap();

	/**
	 * This stores the methods that are synchronized or have synchronized blocks in them.
	 */
	Collection monitorMethods = new HashSet();

	/**
	 * This provide call graph information about the analyzed system.  This is required by the analysis.
	 */
	private CallGraphInfo callgraph;

	/**
	 * This provides information
	 */
	private Environment environment;

	/**
	 * This manages pairs.  This is used to implement <i>flyweight</i> pattern to conserve memory.
	 */
	private PairManager pairMgr;

	/**
	 * This is the logical OR of the <code>RULE_XX</code> as provided by the user.  This indicates the rules which need to be
	 * considered while calculating ready dependency.
	 */
	private int rules = RULE_1 | RULE_2 | RULE_3 | RULE_4;

	/**
	 * Creates a new ReadyDAv1 object.
	 */
	public ReadyDAv2() {
		preprocessor = new PreProcessor();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class PreProcessor
	  extends AbstractProcessor {
		/**
		 * Preprocesses the given method.  It collects synchronized methods.the monitor statements and statements with
		 * <code>Object.wait()</code> and <code>Object.notifyXX()</code> method invocations.
		 *
		 * @param method to be preprocessed.
		 */
		public void callback(SootMethod method) {
			if(Modifier.isSynchronized(method.getModifiers())) {
				monitorMethods.add(method);
			}
		}

		/**
		 * Preprocesses the given stmt.  It collects monitor statements and statements with <code>Object.wait()</code> and
		 * <code>Object.notifyXX()</code> method invocations.
		 *
		 * @param stmt to be preprocessed.
		 * @param context in which <code>stmt</code> occurs.
		 */
		public void callback(Stmt stmt, Context context) {
			SootMethod method = context.getCurrentMethod();
			Map map = null;

			if(stmt instanceof EnterMonitorStmt) {
				map = enterMonitors;
				monitorMethods.add(method);
			} else if(stmt instanceof ExitMonitorStmt) {
				map = exitMonitors;
				monitorMethods.add(method);
			}

			if(map != null) {
				Collection temp;

				if(map.containsKey(method)) {
					temp = (Collection) map.get(method);
				} else {
					temp = new HashSet();
					map.put(method, temp);
				}
				temp.add(stmt);
			} else {
				InvokeExpr expr = null;

				if(stmt instanceof InvokeStmt) {
					expr = (InvokeExpr) ((InvokeStmt) stmt).getInvokeExpr();
				}

				if(expr != null && expr instanceof NonStaticInvokeExpr) {
					NonStaticInvokeExpr invokeExpr = (NonStaticInvokeExpr) expr;
					SootMethod callee = invokeExpr.getMethod();

					if(waitMethods.contains(callee)) {
						map = waits;
					} else if(notifyMethods.contains(callee)) {
						map = notifies;
					}

					if(map != null) {
						Collection temp;

						if(map.containsKey(method)) {
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
		 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#hookup(
		 * 		edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
		 */
		public void hookup(ProcessingController ppc) {
			ppc.register(InvokeStmt.class, this);
		}

		/**
		 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#unhook(
		 * 		edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
		 */
		public void unhook(ProcessingController ppc) {
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
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees( java.lang.Object, java.lang.
	 * 		Object)
	 */
	public Collection getDependees(Object dependentStmt, Object context) {
		return Collections.singletonList((Collection) dependeeMap.get(dependentStmt));
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
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependents( java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependents(Object dependeeStmt, Object context) {
		return Collections.unmodifiableCollection((Collection) dependentMap.get(dependeeStmt));
	}

	/**
	 * Sets the rules to be processed.  By default, all rules are considered for the analysis.
	 *
	 * @param rules is the logical OR of <i>RULE_XX</i> constants defined in this class.
	 *
	 * @throws IllegalArgumentException when rules is not a valid combination of <i>RULE_XX</i> constants.
	 */
	public void setRules(int rules) {
		if((rules & ~(RULE_1 | RULE_2 | RULE_3 | RULE_4)) != 0) {
			throw new IllegalArgumentException("rules has to be a combination of RULE_XX constants defined in this class.");
		}
		this.rules = rules;
	}

	/**
	 * Calculates ready dependency for the methods provided at initialization.  It considers the rules as specified by
	 * <code>rules</code> field which is set via <code>setRules</code> method.
	 *
	 * @return <code>true</code> as this completes in a single run.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		if((rules & (RULE_1 | RULE_3)) != 0) {
			processRule1And3();
		}

		if((rules & RULE_2) != 0) {
			processRule2();
		}

		if((rules & RULE_4) != 0) {
			processRule4();
		}

		fixupInterMethodReadyDA();
		return true;
	}

	/**
	 * Resets the internal data structures.  The rules are not reset.
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
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public String toString() {
		StringBuffer result =
			new StringBuffer("Statistics for Interference Dependence as calculated by " + getClass().getName() + "\n");
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for(Iterator i = dependeeMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			localEdgeCount = 0;

			for(Iterator j = ((Map) entry.getValue()).entrySet().iterator(); j.hasNext();) {
				Map.Entry entry2 = (Map.Entry) j.next();

				for(Iterator k = ((Collection) entry2.getValue()).iterator(); k.hasNext();) {
					temp.append("\t\t" + entry2.getKey() + " --> " + k.next() + "\n");
				}
				localEdgeCount += ((Collection) entry2.getValue()).size();
			}
			result.append("\tFor " + entry.getKey() + " there are " + localEdgeCount + " interference dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " interference Dependence edges exist.");
		return result.toString();
	}

	/**
	 * Extracts information as provided by environment at initialization time.  It collects <code>wait</code> and
	 * <code>notifyXX</code> methods as represented in the AST system. It also extract call graph info, pair manaing
	 * service, and environment from the <code>info</code> member.
	 *
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 * 		   <code>info</code> member.
	 */
	protected void setup() {
		if(waitMethods == null) {
			for(Iterator i = environment.getClasses().iterator(); i.hasNext();) {
				SootClass sc = (SootClass) i.next();

				if(sc.getName().equals("java.lang.Object")) {
					ca.mcgill.sable.util.List temp = sc.getMethods();
					waitMethods = new ArrayList();

					for(ca.mcgill.sable.util.Iterator j = temp.iterator(); j.hasNext();) {
						SootMethod sm = (SootMethod) j.next();

						if(sm.getName().equals("wait")) {
							waitMethods.add(sm);
						}
					}
					waitMethods = Collections.unmodifiableCollection(waitMethods);

					notifyMethods = new ArrayList();
					notifyMethods.add(sc.getMethod("notify"));
					notifyMethods.add(sc.getMethod("notifyAll"));
					notifyMethods = Collections.unmodifiableCollection(notifyMethods);
				}
			}
		}

		environment = (Environment) info.get(PairManager.ID);

		if(environment == null) {
			throw new InitializationException(Environment.ID + " was not provided in info.");
		}
		callgraph = (CallGraphInfo) info.get(CallGraphInfo.ID);

		if(callgraph == null) {
			throw new InitializationException(CallGraphInfo.ID + " was not provided in info.");
		}
		pairMgr = (PairManager) info.get(PairManager.ID);

		if(pairMgr == null) {
			throw new InitializationException(PairManager.ID + " was not provided in info.");
		}
	}

	/**
	 * Fixes inter-method ready dependencies.  All statements in a caller which are dominated by a call site which leads to a
	 * <code>Object.wait</code> call or a synchronized block are recorded as ready dependent on the call site.
	 */
	private void fixupInterMethodReadyDA() {
		Collection methodsToProcess = new HashSet();

		if((rules & (RULE_3 | RULE_4)) != 0) {
			methodsToProcess.addAll(waits.keySet());
		}

		if((rules & (RULE_1 | RULE_2)) != 0) {
			for(Iterator i = monitorMethods.iterator(); i.hasNext();) {
				methodsToProcess.add((SootMethod) i.next());
			}
		}

		WorkBag progPoints = new WorkBag(WorkBag.FIFO);

		for(Iterator i = methodsToProcess.iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			progPoints.addAllWork(callgraph.getCallers(method));
		}

		WorkBag workbag = new WorkBag(WorkBag.LIFO);
		Collection temp = new HashSet();
		Collection processed = new HashSet();

		while(!(progPoints.isEmpty())) {
			CallTriple progPoint = (CallTriple) progPoints.getWork();
			processed.add(progPoint);

			SootMethod caller = progPoint.getMethod();
			Stmt dependee = progPoint.getStmt();
			BasicBlockGraph bbGraph = getBasicBlockGraph(caller);
			BasicBlock bb = bbGraph.getEnclosingBlock(dependee);
			Collection col = new ArrayList(bb.getStmtFrom(getStmtList(caller).indexOf(dependee)));
			col.remove(dependee);

			for(Iterator i = col.iterator(); i.hasNext();) {
				dependeeMap.put(i.next(), dependee);
			}
			dependentMap.put(dependee, col);
			workbag.addAllWork(bb.getSuccsOf());
			temp.clear();

			while(!(workbag.isEmpty())) {
				bb = (BasicBlock) workbag.getWork();

				Collection stmts = bb.getStmtsOf();

				for(Iterator i = stmts.iterator(); i.hasNext();) {
					dependeeMap.put(i.next(), dependee);
				}
				col.addAll(stmts);
				temp.add(bb);

				for(Iterator i = bb.getSuccsOf().iterator(); i.hasNext();) {
					BasicBlock block = (BasicBlock) i.next();

					if(!temp.contains(block)) {
						workbag.addWork(block);
					}
				}
			}

			for(Iterator i = callgraph.getCallers(caller).iterator(); i.hasNext();) {
				Object pp = i.next();

				if(!processed.contains(pp)) {
					progPoints.addWork(pp);
				}
			}
		}
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

		if((rules & RULE_1) != 0) {
			for(Iterator i = enterMonitors.keySet().iterator(); i.hasNext();) {
				SootMethod method = (SootMethod) i.next();
				map.put(method, new ArrayList((Collection) enterMonitors.get(method)));
			}
		}

		if((rules & RULE_3) != 0) {
			for(Iterator i = waits.keySet().iterator(); i.hasNext();) {
				SootMethod method = (SootMethod) i.next();

				if(map.get(method) != null) {
					((Collection) map.get(method)).addAll((Collection) waits.get(method));
				} else {
					Collection tmp = new ArrayList((Collection) waits.get(method));
					map.put(method, tmp);
				}
			}
		}

		for(Iterator i = map.keySet().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			StmtList stmts = getStmtList(method);
			BasicBlockGraph bbGraph = getBasicBlockGraph(method);
			Collection dependees = (Collection) map.get(method);

			for(Iterator j = dependees.iterator(); j.hasNext();) {
				Stmt dependee = (Stmt) j.next();
				BasicBlock bb = bbGraph.getEnclosingBlock(dependee);
				List sl = bb.getStmtFrom(stmts.indexOf(dependee));
				sl.remove(0);  // remove the dependee from the list
				temp = new HashSet();

				Pair pair = pairMgr.getPair(dependee, method);

				for(Iterator k = sl.iterator(); k.hasNext();) {
					Object o = k.next();
					dependeeMap.put(o, pair);

					if(!dependees.contains(o)) {
						temp.add(pairMgr.getPair(o, method));
					}
				}
				workbag.clear();
				processed.clear();
				workbag.addAllWork(bb.getSuccsOf());

				while(!workbag.isEmpty()) {
					BasicBlock work = (BasicBlock) workbag.getWork();

					for(Iterator k = work.getStmtsOf().iterator(); k.hasNext();) {
						Object o = k.next();
						dependeeMap.put(o, pair);

						if(!dependees.contains(o)) {
							temp.add(pairMgr.getPair(o, method));
						}
					}

					if(!processed.contains(bb)) {
						workbag.addAllWork(bb.getSuccsOf());
					}
					processed.add(bb);
				}
				dependentMap.put(dependee, temp);
			}
		}
	}

	/**
	 * Processes the system as per to rule 2 in the report.  For each possible enter- and exit-monitor statement, a simple
	 * class hierarchy based dependency is calculated.  <i>BFA can be used to improve the precision here.</i>
	 */
	private void processRule2() {
		Collection temp = new HashSet();

		for(Iterator i = exitMonitors.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			Object method = entry.getKey();

			for(Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				Object o = j.next();
				dependeeMap.put(o, Collections.EMPTY_LIST);
				temp.add(pairMgr.getPair(method, o));
			}
		}

		Collection nSet = new ArrayList();
		Collection xSet = new ArrayList();

		for(Iterator i = enterMonitors.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod method = (SootMethod) entry.getKey();

			for(Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				EnterMonitorStmt enter = (EnterMonitorStmt) j.next();
				Pair enterPair = pairMgr.getPair(enter, method);
				SootClass enterClass = environment.getClass(((RefType) enter.getOp().getType()).className);

				/*
				 * This iteration adds dependency between enter-exit pair of a monitor block which may be false.
				 * TODO: Use threaded call graph to remove such redundant dependencies.
				 */
				for(Iterator k = temp.iterator(); k.hasNext();) {
					Pair exitPair = (Pair) k.next();
					ExitMonitorStmt exit = (ExitMonitorStmt) exitPair.getFirst();
					xSet.clear();

					//TODO: This is a mere class based dependency.  OFA based dependency will be more precise.
					SootClass exitClass = environment.getClass(((RefType) exit.getOp().getType()).className);

					if(Util.isHierarchicallyRelated(enterClass, exitClass)) {
						xSet.add(enterPair);
						nSet.add(exitPair);
					}

					if(!xSet.isEmpty()) {
						Collection exitSet = (Collection) dependeeMap.get(exit);

						if(exitSet == null) {
							exitSet = new ArrayList();
							dependeeMap.put(exit, exitSet);
						}
						exitSet.addAll(xSet);
					}
				}

				if(nSet.isEmpty()) {
					dependentMap.put(enter, Collections.EMPTY_LIST);
				} else {
					dependentMap.put(enter, nSet);
					nSet = new ArrayList();
				}
			}
		}
	}

	/**
	 * Processes the system as per to rule 4 in the report.  For each possible wait and notifyXX call-sites, a simple class
	 * hierarchy based dependency is calculated.  <i>BFA can be used to improve the precision here.</i>
	 */
	private void processRule4() {
		Collection dependents = new HashSet();

		for(Iterator i = waits.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod wMethod = (SootMethod) entry.getKey();

			for(Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				InvokeStmt wait = (InvokeStmt) j.next();
				SootClass waitClass =
					environment.getClass(((RefType) ((NonStaticInvokeExpr) wait.getInvokeExpr()).getBase().getType()).className);

				for(Iterator k = notifies.entrySet().iterator(); k.hasNext();) {
					entry = (Map.Entry) k.next();

					SootMethod nMethod = (SootMethod) entry.getKey();

					for(Iterator l = ((Collection) entry.getValue()).iterator(); l.hasNext();) {
						InvokeStmt notify = (InvokeStmt) l.next();
						SootClass notifyClass =
							environment.getClass(((RefType) ((NonStaticInvokeExpr) notify.getInvokeExpr()).getBase().getType()).className);

						// TODO: This is a mere class based dependency.  OFA based dependency will be more precise.
						if(Util.isHierarchicallyRelated(waitClass, notifyClass)) {
							Collection temp = (Collection) dependeeMap.get(notify);

							if(temp == null) {
								temp = new HashSet();
								dependeeMap.put(notify, temp);
							}
							temp.add(pairMgr.getPair(wait, wMethod));
							dependents.add(pairMgr.getPair(notify, nMethod));
						}
					}
				}

				if(dependents.size() == 0) {
					dependentMap.put(wait, Collections.EMPTY_LIST);
				} else {
					dependentMap.put(wait, dependents);
					dependents = new HashSet();
				}
			}
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
