
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

import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class provides divergence dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports">A Formal  Study of Slicing for Multi-threaded Program with
 * JVM Concurrency Primitives"</a>.
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
	/*
	 * The dependence information is stored as follows: For each method, a sequence of collection of statements is maintained.
	 * The length of the sequence is equal to the number of statements in the method.  The statement collection at a location
	 * in this sequence corresponds to statements related via dependency to the statement at the same location in the
	 * statement list of the method. The collection is a singleton in case of dependee information.
	 */

	/**
	 * This provides the call graph information in case this object was setup to provide interprocedural divergence
	 * dependence information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * This indicates if call-sites that invoke methods containing pre-divergence points should be considered as
	 * pre-divergence points.
	 */
	private boolean interProcedural;

	/**
	 * Sets if the analyses should consider the effects of method calls.
	 *
	 * @param acrossMethodCalls <code>true</code> indicates call-sites that invoke methods containing pre-divergence points
	 * 		  should be considered as pre-divergence points; <code>false</code>, otherwise.
	 */
	public void setInterProcedural(final boolean acrossMethodCalls) {
		interProcedural = acrossMethodCalls;
	}

	/**
	 * Returns the statements on which the given statement depends on in the given method.
	 *
	 * @param dependentStmt is the statement for which dependees are requested.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.  However, in this case the collection contains only one statement as the .
	 *
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependentStmt.isOclKindOf(Stmt)
	 * @post result->forall( o | o.isOclKindOf(Stmt))
	 * @post result.size == 1
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object method) {
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
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependentStmt.isOclKindOf(Stmt)
	 * @post result->forall( o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependents(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object method) {
		return getDependeXXHelper(dependentMap, dependeeStmt, method);
	}

	/**
	 * Calculates the divergence dependency in the methods.
	 *
	 * @return <code>true</code> as analysis happens in a single run.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		Map preDivPointsMap = new HashMap();
		List stmt2ddents = new ArrayList();
		List stmt2ddees = new ArrayList();

		// for each method
		for (Iterator i = method2stmtGraph.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod method = (SootMethod) entry.getKey();
			BasicBlockGraph bbGraph = getBasicBlockGraph(method);
			Collection sccs = bbGraph.getSCCs(false);

			// calculate the pre-divergence points 
			for (Iterator j = sccs.iterator(); j.hasNext();) {
				Collection scc = (Collection) j.next();

				if (scc.size() > 1) {
					Collection preDivPoints = (Collection) preDivPointsMap.get(method);

					if (preDivPoints == null) {
						preDivPoints = new ArrayList();
						preDivPointsMap.put(method, preDivPoints);
					}

					for (Iterator k = scc.iterator(); k.hasNext();) {
						BasicBlock bb = (BasicBlock) k.next();

						if (!scc.containsAll(bb.getSuccsOf())) {
							preDivPoints.add(bb);
						}
					}
				}
			}
		}

		WorkBag wb = new WorkBag(WorkBag.FIFO);
		Collection collected = new ArrayList();

		for (Iterator i = preDivPointsMap.keySet().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			Collection preDivPoints = (Collection) preDivPointsMap.get(method);
			List sl = getStmtList(method);

			// if there are no pre-divergence points, there can be no divergence dependence.
			if (preDivPoints == null) {
				dependeeMap.put(method, Collections.EMPTY_LIST);
				dependentMap.put(method, Collections.EMPTY_LIST);
			} else {
				Collections.fill(stmt2ddees, Collections.EMPTY_LIST);
				Collections.fill(stmt2ddents, Collections.EMPTY_LIST);

				for (Iterator j = preDivPoints.iterator(); j.hasNext();) {
					stmt2ddents.set(((BasicBlock) j.next())._trailer, new HashSet());
				}

				/*
				 * For each pre-divergent basic block, traverse the path till the next pre-divergenct basic block.  At each
				 * basic block including the last basic block that is pre-divergent, update the divergence dependence
				 * information as obtained from it's predecessor.
				 */
				for (Iterator j = preDivPoints.iterator(); j.hasNext();) {
					BasicBlock bb = (BasicBlock) j.next();
					wb.addAllWork(bb.getSuccsOf());

					while (wb.hasWork()) {
						BasicBlock succBB = (BasicBlock) wb.getWork();
						Collection set = (Collection) stmt2ddees.get(succBB._leader);
						Collection stmts = succBB.getStmtsOf();

						if (set.equals(Collections.EMPTY_LIST)) {
							set = new HashSet();
							stmt2ddees.set(succBB._leader, set);
						}

						for (Iterator k = succBB.getPredsOf().iterator(); k.hasNext();) {
							BasicBlock pred = (BasicBlock) k.next();

							if (preDivPoints.contains(pred)) {
								set.add(sl.get(pred._trailer));
							} else {
								set.addAll((Collection) stmt2ddees.get(pred._trailer));
							}
						}

						for (Iterator l = stmts.iterator(); l.hasNext();) {
							Stmt stmt = (Stmt) l.next();
							collected.add(stmt);
							stmt2ddees.set(sl.indexOf(stmt), set);

							if (callsDivergentMethod(stmt, method, preDivPointsMap)) {
								for (Iterator k = set.iterator(); k.hasNext();) {
									((Collection) stmt2ddents.get(sl.indexOf(k.next()))).addAll(collected);
								}
								set = new HashSet();
								set.add(stmt);
								collected.clear();
							}
						}

						for (Iterator k = set.iterator(); k.hasNext();) {
							((Collection) stmt2ddents.get(sl.indexOf(k.next()))).addAll(collected);
						}

						for (Iterator k = succBB.getSuccsOf().iterator(); k.hasNext();) {
							BasicBlock temp = (BasicBlock) k.next();

							if (!preDivPoints.contains(temp)) {
								wb.addWorkNoDuplicates(temp);
							}
						}
					}
				}
				dependentMap.put(method, new ArrayList(stmt2ddents));
				dependeeMap.put(method, new ArrayList(stmt2ddees));
				preDivPoints.clear();
				stmt2ddents.clear();
				stmt2ddees.clear();
			}
		}
		return true;
	}

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		StringBuffer result =
			new StringBuffer("Statistics for Divergence dependence as calculated by " + getClass().getName() + "\n");
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependentMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			localEdgeCount = 0;

			List stmts = getStmtList((SootMethod) entry.getKey());

			for (Iterator j = ((List) entry.getValue()).iterator(); j.hasNext();) {
				Collection c = (Collection) j.next();
				int count = 0;

				for (Iterator k = c.iterator(); k.hasNext();) {
					temp.append("\t\t" + stmts.get(count++) + " --> " + k.next() + "\n");
				}
				localEdgeCount += c.size();
			}
			result.append("\tFor " + entry.getKey() + " there are " + localEdgeCount + " Divergence dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " Divergence dependence edges exist.");
		return result.toString();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		if (interProcedural) {
			callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

			if (callgraph == null) {
				throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
			}
		}
	}

	/**
	 * Helper method for <code>getDependeXX()</code> methods.
	 *
	 * @param map from which the information should be retrieved.
	 * @param stmt is the statement for which the information is requested.
	 * @param context is the method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statement.
	 *
	 * @pre context.oclIsTypeOf(SootMethod)
	 * @pre stmt.oclIsTypeOf(Stmt)
	 * @pre map.oclIsTypeOf(Map(SootMethod, Sequence(Collection)))
	 */
	private Collection getDependeXXHelper(final Map map, final Object stmt, final Object context) {
		Collection result = Collections.EMPTY_LIST;
		SootMethod method = (SootMethod) context;
		List list = (List) map.get(method);

		if (list != null) {
			List sl = getStmtList(method);
			int pos = sl.indexOf(stmt);

			if (pos != -1) {
				result = Collections.unmodifiableCollection((Collection) list.get(pos));
			}
		}
		return result;
	}

	/**
	 * Checks if the given stmt contains a call-site.  If so, it checks if it results in the invocation of a
	 * divergent-method.
	 *
	 * @param stmt that could result in the invocation of divergent-method via a call-chain.
	 * @param caller in which <code>stmt</code> occurs.
	 * @param preDivPointsMap is a map from methods to pre-divergence points that occur within them.
	 *
	 * @return <code>true</code> if <code>stmt</code> results in the invocation of a divergent-method via a call-chain;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre stmt != null and caller != null and stmt.containsInvokeExpr() == true and preDivPointsMap != null
	 * @pre preDivPointsMap.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private boolean callsDivergentMethod(final Stmt stmt, final SootMethod caller, final Map preDivPointsMap) {
		boolean result = false;

		if (interProcedural && stmt.containsInvokeExpr()) {
			Collection callees = callgraph.getMethodsReachableFrom(stmt, caller);

			for (Iterator i = callees.iterator(); i.hasNext();) {
				if (((Collection) preDivPointsMap.get(i.next())).size() > 0) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.

   Revision 1.5  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file

   Revision 1.4  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.2  2003/08/09 23:30:28  venku
   Enabled divergence dependency to be interprocedural.
 */
