
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
import soot.Value;

import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

import soot.toolkits.graph.CompleteUnitGraph;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.Pair.PairManager;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

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
 * This class provides divergence dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.  This implementation by default does not consider call-sites for dependency
 * calculation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod, Map(Stmt, Collection(Stmt))))
 * @invariant dependentMap.values()->forall(o | o.getValue().size = o.getKey().getBody(Jimple.v()).getStmtList().size())
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Sequence(Collection(Stmt))))
 * @invariant dependeeMap.values()->forall(o | o.getValue().size = o.getKey().getBody(Jimple.v()).getStmtList().size())
 */
public class DivergenceDA
  extends DependencyAnalysis {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(DivergenceDA.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	final Map method2callBasedPreDivPoints = new HashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	PairManager pairMgr;

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
	private boolean considerCallSites = false;

	/**
	 * DOCUMENT ME! This analysis does not require basic block graph manager for preprocessing.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	protected class Processor
	  extends AbstractProcessor {
		/*
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(soot.Value, edu.ksu.cis.indus.staticanalyses.Context)
		 */
		public void callback(final Value value, final Context context) {
			if (value instanceof InvokeExpr) {
				SootMethod method = context.getCurrentMethod();
				Collection c = (Collection) method2callBasedPreDivPoints.get(method);

				if (c == null) {
					c = new ArrayList();
					method2callBasedPreDivPoints.put(method, c);
				}

				Stmt stmt = context.getStmt();
				c.add(stmt);
			}
		}

		/*
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(StaticInvokeExpr.class, this);
			ppc.register(SpecialInvokeExpr.class, this);
			ppc.register(VirtualInvokeExpr.class, this);
			ppc.register(InterfaceInvokeExpr.class, this);
		}

		/*
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(StaticInvokeExpr.class, this);
			ppc.unregister(SpecialInvokeExpr.class, this);
			ppc.unregister(VirtualInvokeExpr.class, this);
			ppc.unregister(InterfaceInvokeExpr.class, this);
		}
	}

	/**
	 * Sets if the analyses should consider the effects of method calls.  This method may change the preprocessing
	 * requirements of this analysis.  Hence, it should be called
	 *
	 * @param consider <code>true</code> indicates call-sites that invoke methods containing pre-divergence points should be
	 * 		  considered as pre-divergence points; <code>false</code>, otherwise.
	 */
	public void setConsiderCallSites(final boolean consider) {
		considerCallSites = consider;
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
		Collection result = Collections.EMPTY_LIST;
		List list = (List) dependeeMap.get(method);

		if (list != null) {
			List sl = getStmtList((SootMethod) method);
			int pos = sl.indexOf(dependentStmt);

			if (pos != -1) {
				Collection c = (Collection) list.get(pos);

				if (c != null) {
					result = c;
				}
			}
		}

		return result;
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
		Collection result = Collections.EMPTY_LIST;
		Map stmt2List = (Map) dependentMap.get(method);

		if (stmt2List != null) {
			Collection t = (Collection) stmt2List.get(dependeeStmt);

			if (t != null) {
				result = Collections.unmodifiableCollection(t);
			}
		}

		return result;
	}

	/**
	 * Calculates the divergence dependency in the methods.
	 *
	 * @return <code>true</code> as analysis happens in a single run.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		Map method2preDivPoints = new HashMap();
		findPreDivPoints(method2preDivPoints);

		List dependents = new ArrayList();
		WorkBag wb = new WorkBag(WorkBag.LIFO);
		Collection preDivPointBBs = new HashSet();
		Collection processed = new HashSet();

		// Pass 2:Record dependence information from pre-divergence points to pre-divergence points or exits.
		for (Iterator i = method2preDivPoints.entrySet().iterator(); i.hasNext();) {
			// Pass 2.1: For each pre-divergence point, record dependence for statments following it in it's basic block.
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod method = (SootMethod) entry.getKey();
			Collection preDivPoints = (Collection) entry.getValue();
			BasicBlockGraph bbg = getBasicBlockGraph(method);

			if (bbg == null) {
				bbg = getBasicBlockGraph(new CompleteUnitGraph(method.getActiveBody()));
			}

			List sl = getStmtList(method);
			preDivPointBBs.clear();
			wb.clear();

			Map spanningSuccs = bbg.getSpanningSuccs();

			for (Iterator j = preDivPoints.iterator(); j.hasNext();) {
				Stmt dependee = (Stmt) j.next();

                BasicBlock bb = bbg.getEnclosingBlock(dependee);
                preDivPointBBs.add(bb);

				if (processed.contains(dependee)) {
					continue;
				}

				List bbsl = bb.getStmtFrom(sl.indexOf(dependee));
				bbsl.remove(0);
				dependents.clear();

				// Inter-basic block pre-divergence points have to be call-sites leading to pre-divergent methods.  
				// Hence, only in these cases can bbsl be non-empty and so it be processed. 
				if (!bbsl.isEmpty()) {
					for (Iterator k = bbsl.iterator(); k.hasNext();) {
						Stmt dependent = (Stmt) k.next();
						dependents.add(dependent);

						// handle pre-divergent call-sites.
						if (considerCallSites && preDivPoints.contains(dependent)) {
							recordDependenceInfo(dependee, method, dependents);
							dependee = dependent;
							dependents.clear();
							processed.add(dependee);
						}
					}
				}

				recordDependenceInfo(dependee, method, dependents);
				dependents.clear();
                Collection succs = bb.getSuccsOf();
				for (Iterator k = succs.iterator(); k.hasNext();) {
					recordDependenceInfo(dependee, method, Collections.singleton(((BasicBlock) k.next()).getLeaderStmt()));
				}
				wb.addAllWork(succs);
			}

			// Pass 2.2: Propogate the pre-divergence information to the rest of the basic blocks.
            
			while (wb.hasWork()) {
				BasicBlock bb = (BasicBlock) wb.getWork();
System.out.println(method.getSignature() + " " + preDivPointBBs + " " + bb + "  " + bb.getStmtsOf());                
				Collection dependees = getDependees(bb.getLeaderStmt(), method);
				dependents.clear();
				dependents.addAll(bb.getStmtsOf());
				dependents.remove(0);

				if (preDivPointBBs.contains(bb)) {
					if (!dependents.isEmpty()) {
						Object o = null;

						for (Iterator j = dependents.iterator(); j.hasNext();) {
							o = j.next();

							if (preDivPoints.contains(o)) {
								break;
							}
						}

						List l = dependents.subList(0, dependents.indexOf(o));
						l.add(o);
						recordDependenceInfo(dependees, method, l);
					}
				} else {
					recordDependenceInfo(dependees, method, dependents);
					wb.addAllWork(bb.getSuccsOf());
				}
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
			new StringBuffer("Statistics for divergence dependence as calculated by " + getClass().getName() + "\n");
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependeeMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod method = (SootMethod) entry.getKey();
			localEdgeCount = 0;

			List stmts = getStmtList(method);
			List dependees = (List) entry.getValue();

			for (int j = 0; j < stmts.size(); j++) {
				Collection c = (Collection) dependees.get(j);

				if (c != null) {
					temp.append("\t\t" + stmts.get(j) + " --> " + c + "\n");
					localEdgeCount += c.size();
				}
			}

			result.append("\tFor " + method + " there are " + localEdgeCount + " divergence dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}

		result.append("A total of " + edgeCount + " divergence dependence edges exist.");

		return result.toString();
	}

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 *
	 * @pre self.considerCallSites implies (info.get(ICallGraphInfo.ID) != null and
	 * 		info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		pairMgr = (PairManager) info.get(PairManager.ID);

		if (considerCallSites) {
			callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

			if (callgraph == null) {
				throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method2preDivPoints DOCUMENT ME!
	 */
	private void findPreDivPoints(final Map method2preDivPoints) {
		WorkBag preDivMethods = null;
		Collection temp = new ArrayList();

		if (considerCallSites) {
			preDivMethods = new WorkBag(WorkBag.LIFO);
		}

		// Pass 1: Calculate pre-divergence points
		// Pass 1.1: Calculate intraprocedural pre-divergence points
		for (Iterator i = method2stmtGraph.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod method = (SootMethod) entry.getKey();
			BasicBlockGraph bbGraph = getBasicBlockGraph(method);

			if (bbGraph == null) {
				bbGraph = getBasicBlockGraph(new CompleteUnitGraph(method.getActiveBody()));
			}

			Collection sccs = bbGraph.getSCCs(false);
			Collection preDivPoints = new ArrayList();
			boolean flag;

			do {
				flag = false;

				// calculate the pre-divergence points 
				for (Iterator j = sccs.iterator(); j.hasNext();) {
					Collection scc = (Collection) j.next();

					for (Iterator k = scc.iterator(); k.hasNext();) {
						BasicBlock bb = (BasicBlock) k.next();
						Collection succs = bb.getSuccsOf();

						if ((succs.size() > 1) && !scc.containsAll(succs)) {
							preDivPoints.add(bb.getTrailerStmt());
							temp.add(bb);
							flag = true;
						}
					}

					if (flag) {
						scc.removeAll(temp);
						temp.clear();
					}
				}
			} while (flag);

			if (!preDivPoints.isEmpty()) {
				method2preDivPoints.put(method, new ArrayList(preDivPoints));

				if (considerCallSites) {
					preDivMethods.addWork(method);
				}

				preDivPoints.clear();
			}
		}

		// Pass 1.2: In case of interprocedural analysis, filter out call-sites which do not lead to pre-divergent methods.
		if (considerCallSites) {
			Collection processed = new HashSet();

			while (preDivMethods.hasWork()) {
				SootMethod callee = (SootMethod) preDivMethods.getWork();
				processed.add(callee);

				for (Iterator j = callgraph.getCallers(callee).iterator(); j.hasNext();) {
					CallTriple ctrp = (CallTriple) j.next();
					SootMethod caller = ctrp.getMethod();
					Collection c = (Collection) method2preDivPoints.get(caller);

					if (c == null) {
						c = new ArrayList();
						method2preDivPoints.put(caller, c);
					}

					c.add(ctrp.getStmt());

					if (!processed.contains(caller)) {
						preDivMethods.addWork(caller);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param dependees DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param dependents DOCUMENT ME!
	 */
	private void recordDependenceInfo(final Collection dependees, final SootMethod method, final Collection dependents) {
		for (Iterator i = dependees.iterator(); i.hasNext();) {
			recordDependenceInfo((Stmt) i.next(), method, dependents);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param dependee DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param dependents DOCUMENT ME!
	 */
	private void recordDependenceInfo(final Stmt dependee, final SootMethod method, final Collection dependents) {
		// record dependent information.
		Map stmt2List = (Map) dependentMap.get(method);
		Collection deList;

		if (stmt2List == null) {
			stmt2List = new HashMap();
			dependentMap.put(method, stmt2List);
			deList = new ArrayList();
			stmt2List.put(dependee, deList);
		} else {
			deList = (Collection) stmt2List.get(dependee);

			if (deList == null) {
				deList = new ArrayList();
				stmt2List.put(dependee, deList);
			}
		}

		deList.addAll(dependents);

		// record dependee information.
		List stmt2dt = (List) dependeeMap.get(method);
		List sl = getStmtList(method);

		if (stmt2dt == null) {
			stmt2dt = new ArrayList();
			dependeeMap.put(method, stmt2dt);

			for (int i = sl.size(); i > 0; i--) {
				stmt2dt.add(null);
			}
		}

		for (Iterator i = dependents.iterator(); i.hasNext();) {
			Object o = i.next();
			int pos = sl.indexOf(o);
			Collection c = (Collection) stmt2dt.get(pos);

			if (c == null) {
				c = new HashSet();
				stmt2dt.set(pos, c);
			}
			c.add(dependee);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/09/09 00:17:35  venku
   - This is a BUGGY version.  I am checking in as I want
     to make some major changes.
   Revision 1.8  2003/08/25 10:04:04  venku
   Renamed setInterProcedural() to setConsiderCallSites().
   Revision 1.7  2003/08/25 09:58:57  venku
   Initialization of interProcedural behavior happened during construction.
   This was too rigid and has now been relaxed via setInterProcedural() method.
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
