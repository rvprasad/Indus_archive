
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
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.EnterMonitorStmt;
import ca.mcgill.sable.soot.jimple.ExitMonitorStmt;
import ca.mcgill.sable.soot.jimple.Stmt;

import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors.AbstractProcessor;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.MonitorInfo;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.Triple;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;


/**
 * This class provides synchronization dependency information.
 * 
 * <p>
 * The dependence information is stored as follows: For each method, a map from a statement to a collection of statement
 * which are related to the key via dependence is maintained.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod, Map(stmt, Collection(Stmt))))
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Map(stmt, Collection(Stmt))))
 */
public class SynchronizationDA
  extends DependencyAnalysis
  implements MonitorInfo {
	/**
	 * This is collection of monitor triples that occur in the analyzed system.  The elements of the triple are of type
	 * <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and <code>SootMethod</code>, respectively.
	 *
	 * @invariant monitorTriples->forall(o | o.oclIsKindOf(Triple(EnterMonitorStmt, EnterMonitorStmt, SootMethod))
	 */
	Collection monitorTriples = new HashSet();

	/**
	 * This collects the enter-monitor statements while preprocessing the system.  This is used during analysis.
	 *
	 * @invariant pointsOfInterest->forall(o | o.oclType = EnterMonitorStmt))
	 */
	Collection pointsOfInterest = new HashSet();

	/**
	 * Creates a new SynchronizationDA object.
	 */
	public SynchronizationDA() {
		preprocessor = new PreProcessor();
	}

	/**
	 * DOCUMENT ME!
	 * <p></p>
	 *
	 * @version $Revision$
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 */
	class PreProcessor
	  extends AbstractProcessor {
		/**
		 * Preprocesses the given method.  This implementation records if the method is synchronized.
		 *
		 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#callback(ca.mcgill.sable.soot.SootMethod)
		 */
		public void callback(SootMethod method) {
			if((method.getModifiers() & Modifier.SYNCHRONIZED) == Modifier.SYNCHRONIZED) {
				monitorTriples.add(new Triple(null, null, method));
			}
		}

		/**
		 * Preprocesses the given stmt.  This implementation records if the <code>stmt</code> is an enter-monitor statement.
		 *
		 * @param stmt DOCUMENT ME!
		 * @param context DOCUMENT ME!
		 *
		 * @pre stmt.oclType = EnterMonitorStmt
		 *
		 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#callback(ca.mcgill.sable.soot.jimple.Stmt,
		 * 		edu.ksu.cis.bandera.staticanalyses.flow.Context)
		 */
		public void callback(Stmt stmt, Context context) {
			pointsOfInterest.add(new Pair(stmt, context.getCurrentMethod()));
		}

		/**
		 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Processor#hookup(edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
		 */
		public void hookup(ProcessingController ppc) {
			ppc.register(EnterMonitorStmt.class, this);
		}
	}

	/**
	 * Returns the statements which depend on the given statement in the given method.
	 *
	 * @param dependentStmt is the statement for which dependees are requested.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of dependee statements.
	 *
	 * @pre method.oclType = SootMethod
	 * @pre dependentStmt.oclType = Stmt
	 * @post result->forall( o | o.oclIsKindOf(EnterMonitorStmt) || o.oclIsKindOf(ExitMonitorStmt))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(Object dependentStmt, Object method) {
		return getHelper(dependeeMap, dependentStmt, method);
	}

	/**
	 * Returns the statements on which the given statement depends on in the given method depends.
	 *
	 * @param dependee is the statement for which dependents are requested.
	 * @param context in which<code>dependentStmt</code> occurs.
	 *
	 * @return a collection of dependent statements.
	 *
	 * @pre method.oclType = SootMethod
	 * @pre dependeeStmt.oclType = Stmt
	 * @post result->forall( o | o.oclType = Stmt)
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(Object dependee, Object context) {
		return getHelper(dependentMap, dependee, context);
	}

	/**
	 * Returns the monitors that occur in the analyzed system.
	 *
	 * @return a collection of <code>Triples</code>.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.MonitorInfo#getMonitorTriples()
	 */
	public Collection getMonitorTriples() {
		return Collections.unmodifiableCollection(monitorTriples);
	}

	/**
	 * Calculates the synchronization dependency information for the methods provided during initialization.
	 *
	 * @return <code>true</code> as analysis happens in a single run.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		Stack currEnterStmt = new Stack();
		Collection currStmts = new ArrayList();
		WorkBag workbag = new WorkBag(WorkBag.FIFO);
		Collection temp = new HashSet();

		for(Iterator i = pointsOfInterest.iterator(); i.hasNext();) {
			Pair pair = (Pair) i.next();
			Stmt enter = (Stmt) pair.getFirst();
			SootMethod method = (SootMethod) pair.getSecond();
			Map stmt2ddents = (Map) dependentMap.get(method);
			Map stmt2ddees = (Map) dependeeMap.get(method);

			if(stmt2ddents == null) {
				stmt2ddents = new HashMap();
				stmt2ddees = new HashMap();
				dependentMap.put(method, stmt2ddents);
				dependeeMap.put(method, stmt2ddees);
			} else if(stmt2ddents.get(enter) != null) {
				continue;
			}

			BasicBlockGraph bbGraph = getBasicBlockGraph(method);
			workbag.clear();
			workbag.addWork(bbGraph.getEnclosingBlock(enter));

			do {
				BasicBlock bb = (BasicBlock) workbag.getWork();

				for(Iterator j = bb.getStmtsOf().iterator(); j.hasNext();) {
					Stmt stmt = (Stmt) j.next();

					if(stmt instanceof EnterMonitorStmt) {
						currStmts.add(stmt);
						currEnterStmt.push(new Pair(stmt, currStmts));
						currStmts = new ArrayList();
					} else if(stmt instanceof ExitMonitorStmt) {
						pair = (Pair) currEnterStmt.pop();
						enter = (Stmt) pair.getFirst();
						temp.clear();
						temp.add(enter);
						temp.add(stmt);

						for(Iterator k = currStmts.iterator(); k.hasNext();) {
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

				for(Iterator j = bbGraph.getForwardSuccsOf(bb).iterator(); j.hasNext();) {
					BasicBlock succ = (BasicBlock) j.next();
					currEnterStmt.push(pair);
					workbag.addWork(succ);
				}
			} while(!workbag.isEmpty());
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
	 * @pre method.oclType = ca.mcgill.sable.soot.SootMethod
	 * @pre stmt = ca.mcgill.sable.soot.jimple.Stmt
	 * @post result->forall( o | o.oclType ca.mcgill.sable.soot.jimple.Stmt)
	 */
	protected static Collection getHelper(Map map, Object stmt, Object method) {
		Collection result = Collections.EMPTY_LIST;
		Map stmt2ddeXXs = (Map) map.get(method);

		if(stmt2ddeXXs != null) {
			Collection temp = (Collection) stmt2ddeXXs.get(stmt);

			if(temp != null) {
				result = Collections.unmodifiableCollection(temp);
			}
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$

*****/
