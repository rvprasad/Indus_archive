
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
import edu.ksu.cis.indus.staticanalyses.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.Triple;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;


/**
 * This class provides synchronization dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports">A Formal  Study of Slicing for Multi-threaded Program with
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
	 * This is collection of monitor triples that occur in the analyzed system.  The elements of the triple are of type
	 * <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and <code>SootMethod</code>, respectively.
	 *
	 * @invariant monitorTriples.oclIsKindOf(Collection(Triple(EnterMonitorStmt, EnterMonitorStmt, SootMethod)))
	 * @invariant monitorTriples->forall(o | o.getFirst() == null and o.getSecond() == null implies
	 * 			  o.getThird().isSynchronized())
	 */
	final Collection monitorTriples = new HashSet();

	/**
	 * This collects the enter-monitor statements during preprocessing.
	 *
	 * @invariant pointsOfInterest.oclIsKindOf(Collection(EnterMonitorStmt))
	 */
	final Collection pointsOfInterest = new HashSet();

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
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(soot.SootMethod)
		 */
		public void callback(final SootMethod method) {
			if (method.isSynchronized()) {
				monitorTriples.add(new Triple(null, null, method));
			}
		}

		/**
		 * Preprocesses the given stmt.  It records if the <code>stmt</code> is an enter-monitor statement.
		 *
		 * @param stmt is the enter monitor statement.
		 * @param context in which <code>stmt</code> occurs.  This contains the method that encloses <code>stmt</code>.
		 *
		 * @pre stmt.isOclTypeOf(EnterMonitorStmt)
		 * @pre context.getCurrentMethod() != null
		 *
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(soot.jimple.Stmt,
		 * 		edu.ksu.cis.indus.staticanalyses.flow.Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
			pointsOfInterest.add(new Pair(stmt, context.getCurrentMethod()));
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(
		 * 		edu.ksu.cis.indus.staticanalyses.flow.ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(EnterMonitorStmt.class, this);
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(
		 * 		edu.ksu.cis.indus.staticanalyses.flow.ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(EnterMonitorStmt.class, this);
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
	 * Calculates the synchronization dependency information for the methods provided during initialization.
	 *
	 * @return <code>true</code> as analysis happens in a single method call.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		Stack currEnterStmt = new Stack();
		Collection currStmts = new ArrayList();
		WorkBag workbag = new WorkBag(WorkBag.FIFO);
		Collection temp = new HashSet();

		for (Iterator i = pointsOfInterest.iterator(); i.hasNext();) {
			Pair pair = (Pair) i.next();
			Stmt enter = (Stmt) pair.getFirst();
			SootMethod method = (SootMethod) pair.getSecond();
			Map stmt2ddents = (Map) dependentMap.get(method);
			Map stmt2ddees = (Map) dependeeMap.get(method);

			if (stmt2ddents == null) {
				stmt2ddents = new HashMap();
				stmt2ddees = new HashMap();
				dependentMap.put(method, stmt2ddents);
				dependeeMap.put(method, stmt2ddees);
			} else if (stmt2ddents.get(enter) != null) {
				continue;
			}

			BasicBlockGraph bbGraph = getBasicBlockGraph(method);
			workbag.clear();
			workbag.addWork(bbGraph.getEnclosingBlock(enter));

			do {
				BasicBlock bb = (BasicBlock) workbag.getWork();

				for (Iterator j = bb.getStmtsOf().iterator(); j.hasNext();) {
					Stmt stmt = (Stmt) j.next();

					if (stmt instanceof EnterMonitorStmt) {
						currStmts.add(stmt);
						currEnterStmt.push(new Pair(stmt, currStmts));
						currStmts = new ArrayList();
					} else if (stmt instanceof ExitMonitorStmt) {
						pair = (Pair) currEnterStmt.pop();
						enter = (Stmt) pair.getFirst();
						temp.clear();
						temp.add(enter);
						temp.add(stmt);

						for (Iterator k = currStmts.iterator(); k.hasNext();) {
							Stmt curr = (Stmt) k.next();
							stmt2ddees.put(curr, temp);
						}
						stmt2ddents.put(stmt, currStmts);
						stmt2ddents.put(enter, currStmts);
						currStmts = (Collection) pair.getSecond();
						currStmts.add(stmt);
						monitorTriples.add(new Triple(enter, stmt, method));
					} else {
						currStmts.add(stmt);
					}
				}
				pair = (Pair) currEnterStmt.pop();

				for (Iterator j = bbGraph.getForwardSuccsOf(bb).iterator(); j.hasNext();) {
					BasicBlock succ = (BasicBlock) j.next();
					currEnterStmt.push(pair);
					workbag.addWork(succ);
				}
			} while (workbag.hasWork());
		}
		return true;
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
}

/*
 ChangeLog:

$Log$
Revision 1.2  2003/08/09 23:29:52  venku
Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.

Revision 1.1  2003/08/07 06:38:05  venku
Major:
 - Moved the packages under indus umbrella.
 - Renamed MethodLocalDataDA to NonAliasedDataDA.
 - Added class for AliasedDataDA.
 - Documented and specified the classes.


*/
