
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

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtGraph;
import ca.mcgill.sable.soot.jimple.StmtList;

import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class provides divergence dependency information.
 *
 * <p>
 * This dependence information is stored as follows: For each method, a sequence of collection of statements is maintained.
 * The length of the sequence is equal to the number of statements in the method.  The statement collection at a location in
 * this sequence corresponds to statements related via dependency to the statement at the same location in the statement
 * list of the method. The collection is a singleton in case of dependee information.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod, Sequence(Bag(Stmt))))
 * @invariant dependentMap.values()->forall(o | o.getValue().size = o.getKey().getBody(Jimple.v()).getStmtList().size())
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Sequence(Bag(Stmt))))
 * @invariant dependeeMap.values()->forall(o | o.getValue().size = o.getKey().getBody(Jimple.v()).getStmtList().size())
 */
public class DivergenceDA
  extends DependencyAnalysis {
	/**
	 * Returns the statements on which the given statement depends on in the given method.
	 *
	 * @param dependentStmt is the statement for which dependees are requested.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.  However, in this case the collection contains only one statement.
	 *
	 * @pre method.oclType = SootMethod
	 * @pre dependentStmt.isOclKindOf(Stmt)
	 * @post result->forall( o | o.isOclKindOf(Stmt))
	 * @post result.size = 1
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(Object dependentStmt, Object method) {
		return getDependeXXHelper(dependeeMap, dependentStmt, method);
	}

	/**
	 * Returns the statements which depend on the given statement in the given method.
	 *
	 * @param dependeeStmt is the statement for which dependents are requested.
	 * @param method in which <code>dependeeStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre method.oclType = SootMethod
	 * @pre dependentStmt.isOclKindOf(Stmt)
	 * @post result->forall( o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#getDependents(java.lang.Object,
	 *         java.lang.Object)
	 */
	public Collection getDependents(Object dependeeStmt, Object method) {
		return getDependeXXHelper(dependentMap, dependeeStmt, method);
	}

	/**
	 * Calculates the divergence dependency in the methods provided during initialization.
	 *
	 * @return <code>true</code> as analysis happens in a single run.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		Collection preDivPoints = new HashSet();
		List stmt2ddents = new ArrayList();
		List stmt2ddees = new ArrayList();

		for (Iterator i = method2stmtGraph.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod method = (SootMethod) entry.getKey();
			StmtList sl = ((StmtGraph) entry.getValue()).getBody().getStmtList();
			preDivPoints.clear();

			BasicBlockGraph bbGraph = getBasicBlockGraph(method);

			for (Iterator j = bbGraph.getNodes().iterator(); j.hasNext();) {
				BasicBlock bb = (BasicBlock) j.next();

				// This will not work.  Backedge is not detected.  Implement SCC at bb level and use it here.
				if (bbGraph.isReachable(bb, bb, true) && bb.getSuccsOf().size() > 1) {
					preDivPoints.add(bb);
					stmt2ddents.add(bb._TRAILER, new HashSet());
				}
			}

			WorkBag wb = new WorkBag(WorkBag.FIFO);
			wb.addAllWork(bbGraph.getHeads());

			while (!wb.isEmpty()) {
				BasicBlock bb = (BasicBlock) wb.getWork();

				for (Iterator k = bb.getPredsOf().iterator(); k.hasNext();) {
					BasicBlock pred = (BasicBlock) k.next();
					Collection stmts = bb.getStmtsOf();

					if (preDivPoints.contains(pred)) {
						Collection set = (Collection) stmt2ddees.get(bb._LEADER);

						if (set == null) {
							set = new HashSet();
						}

						for (Iterator l = stmts.iterator(); l.hasNext();) {
							Stmt stmt = (Stmt) l.next();
							stmt2ddees.add(sl.indexOf(stmt), set);
						}
						((Collection) stmt2ddents.get(pred._TRAILER)).addAll(stmts);
					} else {
						Collection set = (Collection) stmt2ddees.get(pred._LEADER);

						for (Iterator l = stmts.iterator(); l.hasNext();) {
							Stmt stmt = (Stmt) l.next();
							stmt2ddees.add(sl.indexOf(stmt), set);
						}

						for (Iterator l = set.iterator(); l.hasNext();) {
							((Collection) stmt2ddents.get(sl.indexOf((Stmt) l.next()))).addAll(stmts);
						}
					}
				}
				wb.addAllWork(bbGraph.getForwardSuccsOf(bb));
			}

			if (stmt2ddents.size() == 0) {
				dependentMap.put(method, Collections.EMPTY_LIST);
			} else {
				dependentMap.put(method, stmt2ddents);
				stmt2ddents = new ArrayList();
			}

			if (stmt2ddees.size() == 0) {
				dependeeMap.put(method, Collections.EMPTY_LIST);
			} else {
				dependeeMap.put(method, stmt2ddees);
				stmt2ddees = new ArrayList();
			}
		}
		return true;
	}

	/**
	 * Helper method for <code>getDependeXX()</code> methods.
	 *
	 * @param map from which the information should be retrieved.
	 * @param stmt is the statement for which the information is requested.
	 * @param context is the method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statement.
	 */
	private Collection getDependeXXHelper(Map map, Object stmt, Object context) {
		Collection result = Collections.EMPTY_LIST;
		SootMethod method = (SootMethod) context;
		List list = (List) map.get(method);

		if (list != null) {
			StmtList sl = ((StmtGraph) method2stmtGraph.get(method)).getBody().getStmtList();
			int pos = sl.indexOf((Stmt) stmt);

			if (pos != -1) {
				result = Collections.unmodifiableCollection((Collection) list.get(pos));
			}
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$

*****/
