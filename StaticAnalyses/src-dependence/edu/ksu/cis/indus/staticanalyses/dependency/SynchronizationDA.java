
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

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.Quadraple;
import edu.ksu.cis.indus.staticanalyses.support.Triple;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;


/**
 * This class provides synchronization dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program with
 * JVM Concurrency Primitives"</a>.
 * 
 * <p>
 * <i>Synchronization dependence</i>: All non-monitor statement in a method are synchronization dependent on the immediately
 * enclosing monitor statements in the same method.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod, Map(ExitMonitorStmt, Collection(EnterMonitorStmt))))
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Map(EnterMonitortmt, Collection(ExitMonitorStmt))))
 */
public class SynchronizationDA
  extends DependencyAnalysis
  implements IMonitorInfo {
	/**
	 * This collects the enter-monitor statements during preprocessing.
	 *
	 * @invariant enterMonitors.oclIsKindOf(Collection(EnterMonitorStmt))
	 */
	final Collection enterMonitors = new HashSet();

	/**
	 * This collects the exit-monitor statements during preprocessing.
	 *
	 * @invariant exitMonitors.oclIsKindOf(Collection(ExitMonitorStmt))
	 */
	final Collection exitMonitors = new HashSet();

	/**
	 * This is collection of monitor triples that occur in the analyzed system.  The elements of the triple are of type
	 * <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and <code>SootMethod</code>, respectively.
	 *
	 * @invariant monitorTriples.oclIsKindOf(Collection(Triple(EnterMonitorStmt, EnterMonitorStmt, SootMethod)))
	 * @invariant monitorTriples->forall(o | o.getFirst() == null and o.getSecond() == null implies
	 * 			  o.getThird().isSynchronized())
	 */
	final Collection monitorTriples = new HashSet();

	/**
	 * This provides object flow information.
	 */
	private IValueAnalyzer ofa;

	/**
	 * This indicates if the analysis has stabilized.  If so, it is safe to query this object for information.
	 */
	private boolean stable;

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
	private class PreProcessor
	  extends AbstractProcessor {
		/**
		 * Preprocesses the given method.  It records if the method is synchronized.
		 *
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(SootMethod)
		 */
		public void callback(final SootMethod method) {
			if (method.isSynchronized()) {
				monitorTriples.add(new Triple(null, null, method));
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
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(Stmt,Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
			if (stmt instanceof EnterMonitorStmt) {
				enterMonitors.add(new Pair(stmt, context.getCurrentMethod()));
			} else {
				exitMonitors.add(new Pair(stmt, context.getCurrentMethod()));
			}
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(EnterMonitorStmt.class, this);
			ppc.register(ExitMonitorStmt.class, this);
            ppc.register(this);
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(EnterMonitorStmt.class, this);
			ppc.unregister(ExitMonitorStmt.class, this);
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
	 * @return <code>true</code> as analysis happens in a single method call.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		stable = false;

		WorkBag workbag = new WorkBag(WorkBag.LIFO);
		Collection temp = new HashSet();
		Collection col;
		Stack stack = new Stack();
		Collection processedStmts = new HashSet();
		Collection processedMonitors = new HashSet();
		Collection coupled = new HashSet();

		/*
		 * Calculating Sync DA is not as simple as it looks in the presence of exceptions.The exit monitors are the tricky
		 * ones.  The exit monitors are guarded by catch block generated by the compiler.  The user cannot generated such
		 * complex catch blocks, but nevertheless we cannot assume about the compiler. So, we proceed by calculating the
		 * dependence edges over a complete unit graph and use object flow information to arbitrate suspicious enter-exit
		 * matches.
		 */
		for (Iterator i = enterMonitors.iterator(); i.hasNext();) {
			Pair pair = (Pair) i.next();
			Stmt enterMonitor = (Stmt) pair.getFirst();
			SootMethod method = (SootMethod) pair.getSecond();
			Map stmt2ddents = (Map) dependentMap.get(method);
			Map stmt2ddees;
			Context context = new Context();

			if (stmt2ddents == null) {
				stmt2ddents = new HashMap();
				stmt2ddees = new HashMap();
				dependentMap.put(method, stmt2ddents);
				dependeeMap.put(method, stmt2ddees);
			} else if (stmt2ddents.get(enterMonitor) != null) {
				continue;
			} else {
				stmt2ddees = (Map) dependeeMap.get(method);
			}

			BasicBlockGraph bbGraph = getBasicBlockGraph(method);
			workbag.clear();
			stack.clear();
			temp.clear();
			workbag.addWork(new Quadraple(bbGraph.getEnclosingBlock(enterMonitor), enterMonitor, stack, temp, false));

			List stmtList = getStmtList(method);
			processedStmts.clear();
nextBasicBlock: 
			do {
				Quadraple work = (Quadraple) workbag.getWork();
				BasicBlock bb = (BasicBlock) work.getFirst();
				Stmt leadStmt = (Stmt) work.getSecond();
				Stack enterStack = (Stack) work.getThird();
				HashSet currStmts = (HashSet) work.getFourth();

				for (Iterator j = bb.getStmtFrom(stmtList.indexOf(leadStmt)).iterator(); j.hasNext();) {
					Stmt stmt = (Stmt) j.next();

					// This is to avoid processing induced by back-edgesr.
					if (processedStmts.contains(stmt)) {
						continue nextBasicBlock;
					}
					processedStmts.add(stmt);

					if (stmt instanceof EnterMonitorStmt) {
						currStmts.add(stmt);
						enterStack.push(new Pair(stmt, currStmts));
						currStmts = new HashSet();
					} else if (stmt instanceof ExitMonitorStmt) {
						pair = (Pair) enterStack.pop();

						EnterMonitorStmt enter = (EnterMonitorStmt) pair.getFirst();
						ExitMonitorStmt exit = (ExitMonitorStmt) stmt;

						// If the current monitor was processed, we cannot add any more information to it. So, chug along.
						if (processedMonitors.contains(enter)) {
							currStmts = (HashSet) pair.getSecond();
							currStmts.add(stmt);
							continue;
						} else {
							/*
							 * if the monitor object at the enter and exit statements contain the same objects then
							 * consider this pair, if not continue.  The assumption here is that the compiler will copy the
							 * monitor object before the enter monitor to be used in exit monitor.  Hence, a flow
							 * sensitive analysis should be able to provide identical value sets.
							 */
							context.setRootMethod(method);
							context.setProgramPoint(enter.getOpBox());
							context.setStmt(enter);

							Collection nValues = ofa.getValues(enter.getOp(), context);
							context.setProgramPoint(exit.getOpBox());
							context.setStmt(exit);

							Collection xValues = ofa.getValues(exit.getOp(), context);

							if (nValues.size() != xValues.size() || !nValues.containsAll(xValues)) {
								continue;
							}
						}

						col = new HashSet();
						col.add(enter);
						col.add(stmt);

						for (Iterator k = currStmts.iterator(); k.hasNext();) {
							Stmt curr = (Stmt) k.next();
							stmt2ddees.put(curr, col);
						}
						stmt2ddents.put(stmt, new HashSet(currStmts));
						col = (Collection) stmt2ddents.get(enter);

						if (col == null) {
							col = new HashSet();
							stmt2ddents.put(enter, col);
						}
						col.addAll(currStmts);
						currStmts = (HashSet) pair.getSecond();
						currStmts.add(stmt);
						monitorTriples.add(new Triple(enter, stmt, method));
						coupled.add(stmt);

						if (enterStack.isEmpty()) {
							break;
						}
					} else {
						currStmts.add(stmt);
					}
				}

				if (!enterStack.isEmpty()) {
					Collection succs = bb.getSuccsOf();

					if (succs.size() == 1) {
						BasicBlock succ = (BasicBlock) succs.iterator().next();
						workbag.addWork(new Quadraple(succ, succ.getLeaderStmt(), enterStack, currStmts, false));
					} else {
						for (Iterator j = succs.iterator(); j.hasNext();) {
							BasicBlock succ = (BasicBlock) j.next();
							Stack clone = new Stack();

							for (Iterator iter = enterStack.iterator(); iter.hasNext();) {
								Pair p = (Pair) iter.next();
								clone.add(new Pair(p.getFirst(), ((HashSet) p.getSecond()).clone()));
							}
							workbag.addWork(new Quadraple(succ, succ.getLeaderStmt(), clone, currStmts.clone(), false));
						}
					}
				}
			} while (workbag.hasWork());
			processedMonitors.add(enterMonitor);
		}

		stable = true;
		return true;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		monitorTriples.clear();
		enterMonitors.clear();
		exitMonitors.clear();
	}

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		StringBuffer result =
			new StringBuffer("Statistics for Synchronization dependence as calculated by " + getClass().getName() + "\n");
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependentMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			localEdgeCount = 0;

			SootMethod method = (SootMethod) entry.getKey();

			for (Iterator j = ((Map) entry.getValue()).entrySet().iterator(); j.hasNext();) {
				Map.Entry entry2 = (Map.Entry) j.next();
				Stmt dependee = (Stmt) entry2.getKey();

				for (Iterator k = ((Collection) entry2.getValue()).iterator(); k.hasNext();) {
					Stmt obj = (Stmt) k.next();
					temp.append("\t\t" + dependee + "[" + dependee.hashCode() + "] <- " + obj + "[" + obj.hashCode() + "]\n");
				}
				localEdgeCount += ((Collection) entry2.getValue()).size();
			}
			result.append("\tFor " + method + " there are " + localEdgeCount + " sync dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " synchronization dependence edges exist.\n");
		result.append("MonitorInfo follows:\n");

		for (Iterator i = monitorTriples.iterator(); i.hasNext();) {
			Triple trip = (Triple) i.next();

			if (trip.getFirst() != null) {
				result.append("[" + trip.getFirst() + " " + trip.getFirst().hashCode() + ", " + trip.getSecond() + " "
					+ trip.getSecond().hashCode() + "] occurs in " + trip.getThird() + "\n");
			} else {
				result.append(trip.getThird() + " is synchronized.\n");
			}
		}
		return result.toString();
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
		Collection result = Collections.EMPTY_LIST;
		Map stmt2ddeXXs = (Map) map.get(method);

		if (stmt2ddeXXs != null) {
			Collection temp = (Collection) stmt2ddeXXs.get(stmt);

			if (temp != null) {
				result = Collections.unmodifiableCollection(temp);
			}
		}
		return result;
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
   Revision 1.8  2003/09/08 02:25:04  venku
   - Ripple effect of changes to ProcessingController.

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
