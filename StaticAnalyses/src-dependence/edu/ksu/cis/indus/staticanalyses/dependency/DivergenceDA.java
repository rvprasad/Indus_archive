
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

import soot.SootMethod;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.LIFOWorkBag;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
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
 * @invariant dependentMap.values()->forall(o | o.getValue().size = o.getKey().getActiveBody().getUnits().size())
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Sequence(Collection(Stmt))))
 * @invariant dependeeMap.values()->forall(o | o.getValue().size = o.getKey().getActiveBody().getUnits().size())
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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DivergenceDA.class);

	/**
	 * This provides the call graph information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * This indicates if call-sites that invoke methods containing pre-divergence points should be considered as
	 * pre-divergence points.
	 */
	private boolean considerCallSites = false;

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
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Divergence Dependence processing");
		}

		Map method2preDivPoints = new HashMap();
		findPreDivPoints(method2preDivPoints);

		final List DEPENDENTS = new ArrayList();
		final Collection SUCCS = new HashSet();
		WorkBag wb = new LIFOWorkBag();
		final Collection PREDIVPOINTBBS = new HashSet();

		// Pass 2:Record dependence information from pre-divergence points to pre-divergence points or exits.
		for (Iterator i = method2preDivPoints.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			final SootMethod METHOD = (SootMethod) entry.getKey();
			final Collection PREDIVPOINTS = (Collection) entry.getValue();
			PREDIVPOINTBBS.clear();
			wb.clear();

			List sl = getStmtList(METHOD);

			List de = (List) dependeeMap.get(METHOD);

			if (de == null) {
				de = new ArrayList();
				dependeeMap.put(METHOD, de);

				for (int j = sl.size(); j > 0; j--) {
					de.add(null);
				}
			}

			/*
			 * Pass 2.1: For each pre-divergence point, record dependence for statments following it in it's basic block.
			 * Also, propogate the information to the leader statement of the successive basic blocks and add them to a
			 * worklist
			 */
			final BasicBlockGraph BBG = getBasicBlockGraph(METHOD);

			for (Iterator j = PREDIVPOINTS.iterator(); j.hasNext();) {
				Stmt dependee = (Stmt) j.next();
				BasicBlock bb = BBG.getEnclosingBlock(dependee);

				if (PREDIVPOINTBBS.contains(bb)) {
					continue;
				}
				PREDIVPOINTBBS.add(bb);

				/*
				 * Only in case of inter-procedural divergence dependence will there be more than one pre-divergent point in
				 * a basic block.  In such cases, it makes sense to handle all pre-divergent points in the block in one sweep.
				 * THINK: How does this perform when exception-based control-flow graph in Soot is considered?
				 */
				if (considerCallSites) {
					Iterator k = bb.getStmtsOf().iterator();

					for (; k.hasNext();) {
						dependee = (Stmt) k.next();

						if (PREDIVPOINTS.contains(dependee)) {
							break;
						}
					}
					DEPENDENTS.clear();

					for (; k.hasNext();) {
						Stmt dependent = (Stmt) k.next();
						DEPENDENTS.add(dependent);

						// handle pre-divergent call-sites.
						if (PREDIVPOINTS.contains(dependent)) {
							recordDependenceInfo(dependee, METHOD, DEPENDENTS);
							dependee = dependent;
							DEPENDENTS.clear();
						}
					}
					recordDependenceInfo(dependee, METHOD, DEPENDENTS);
				}
				SUCCS.clear();
				SUCCS.addAll(bb.getSuccsOf());

				for (Iterator k = SUCCS.iterator(); k.hasNext();) {
					DEPENDENTS.clear();
					DEPENDENTS.add(((BasicBlock) k.next()).getLeaderStmt());
					recordDependenceInfo(dependee, METHOD, DEPENDENTS);
				}
				wb.addAllWorkNoDuplicates(SUCCS);
			}

			// Pass 2.2: Propogate the pre-divergence information to the rest of the basic blocks.
			while (wb.hasWork()) {
				final BasicBlock BB = (BasicBlock) wb.getWork();
				final Stmt LEADER = BB.getLeaderStmt();
				final Collection DEPENDEES = getDependees(LEADER, METHOD);
				DEPENDENTS.clear();
				DEPENDENTS.addAll(BB.getStmtsOf());

				// in case of injecting info into a pre-divergent basic block, do not propogate the information.
				if (PREDIVPOINTBBS.contains(BB) && !DEPENDENTS.isEmpty()) {
					Object o = null;

					for (Iterator j = DEPENDENTS.iterator(); j.hasNext();) {
						o = j.next();

						if (PREDIVPOINTS.contains(o)) {
							break;
						}
					}

					List l = DEPENDENTS.subList(0, DEPENDENTS.indexOf(o));
					l.add(o);
					recordDependenceInfoInBB(DEPENDEES, METHOD, l, sl);
				} else if (!DEPENDEES.isEmpty()) {
					// in case of non-pre-divergent basic block, remember to propogate the information to the successors.
					recordDependenceInfoInBB(DEPENDEES, METHOD, DEPENDENTS, sl);
					SUCCS.clear();

					for (Iterator j = BB.getSuccsOf().iterator(); j.hasNext();) {
						BasicBlock succ = (BasicBlock) j.next();

						if (succ != BB) {
							SUCCS.add(succ.getLeaderStmt());
							wb.addWorkNoDuplicates(succ);
						}
					}

					for (Iterator j = DEPENDEES.iterator(); j.hasNext();) {
						recordDependenceInfo((Stmt) j.next(), METHOD, SUCCS);
					}
				}
			}

			/*
			 * Pass 2.3: Prune the information in cycles from the pre-divergent point of the cycle to the next pre-divergent
			 * point in the cycle.
			 * TODO:
			 */
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Divergence Dependence processing");
		}
		stable = true;
	}

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		StringBuffer result =
			new StringBuffer("Statistics for divergence dependence as calculated by " + getClass().getName() + "["
				+ hashCode() + "]\n");
		result.append("The analyses setup was : \n \tinterprocedural: " + considerCallSites + "\n");

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
	 * @pre info.get(ICallGraphInfo.ID) != null and    info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();
		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
		}
	}

	/**
	 * Finds the pre-divergent points in terms of pre-divergent statements and populates the given map.
	 *
	 * @param method2preDivPoints maps a method to the set of pre-divergent statements in it.  This is an out parameter.
	 *
	 * @pre method2preDivPoints != null
	 * @post method2preDivPoints.oclIsKindOf(Map(SootMethod, Set))
	 */
	private void findPreDivPoints(final Map method2preDivPoints) {
		WorkBag preDivMethods = null;
		Collection preDivPoints = new HashSet();
		Collection succs = new ArrayList();

		if (considerCallSites) {
			preDivMethods = new LIFOWorkBag();
		}

		// Pass 1: Calculate pre-divergence points
		// Pass 1.1: Calculate intraprocedural pre-divergence points
		for (Iterator i = callgraph.getReachableMethods().iterator(); i.hasNext();) {
			final SootMethod METHOD = (SootMethod) i.next();
			final BasicBlockGraph BBGRAPH = getBasicBlockGraph(METHOD);

			if (BBGRAPH == null) {
				LOGGER.error("Method " + METHOD.getSignature() + " did not have a basic block graph.");
				continue;
			}

			final Collection BACKEDGES = BBGRAPH.getBackEdges();
			final Collection HANDLERBLOCKS = BBGRAPH.getHandlerBlocks();

			for (Iterator j = BACKEDGES.iterator(); j.hasNext();) {
				Pair edge = (Pair) j.next();
				BasicBlock src = (BasicBlock) edge.getFirst();
				BasicBlock dest = (BasicBlock) edge.getSecond();
				succs.clear();
				succs.addAll(src.getSuccsOf());
				succs.removeAll(HANDLERBLOCKS);

				for (Iterator k = succs.iterator(); k.hasNext();) {
					BasicBlock succ = (BasicBlock) k.next();

					if (succ != dest && !BBGRAPH.isAncestorOf(succ, dest)) {
						preDivPoints.add(src.getTrailerStmt());
					}
				}
				succs.clear();
				succs.addAll(dest.getSuccsOf());
				succs.removeAll(HANDLERBLOCKS);

				for (Iterator k = succs.iterator(); k.hasNext();) {
					BasicBlock succ = (BasicBlock) k.next();

					if (succ != src && !BBGRAPH.isAncestorOf(succ, src)) {
						preDivPoints.add(dest.getTrailerStmt());
					}
				}
			}

			if (!preDivPoints.isEmpty()) {
				method2preDivPoints.put(METHOD, new ArrayList(preDivPoints));

				if (considerCallSites) {
					preDivMethods.addWork(METHOD);
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
	 * Records the dependence information among the given data.  Dependence of dependee upon itself will not be recorded.
	 *
	 * @param dependee is the dependee.
	 * @param method in which the dependence occurs.
	 * @param dependents is the set of dependents.
	 *
	 * @pre dependee != null and method != null and dependents != null
	 * @pre dependents.oclIsKindOf(Collection(Stmt))
	 */
	private void recordDependenceInfo(final Stmt dependee, final SootMethod method, final Collection dependents) {
		// record dependent information.
		dependents.remove(dependee);

		if (!dependents.isEmpty()) {
			Map stmt2List = (Map) dependentMap.get(method);
			Collection dtList;

			if (stmt2List == null) {
				stmt2List = new HashMap();
				dependentMap.put(method, stmt2List);
				dtList = new HashSet();
				stmt2List.put(dependee, dtList);
			} else {
				dtList = (Collection) stmt2List.get(dependee);

				if (dtList == null) {
					dtList = new HashSet();
					stmt2List.put(dependee, dtList);
				}
			}

			dtList.addAll(dependents);

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

	/**
	 * Records dependence information collectively.  It is required that all dependents occur in the same basic block as a
	 * continous sequence.
	 *
	 * @param dependees of course.
	 * @param method in which the dependence occurs.
	 * @param dependents of course.
	 * @param stmtList is the list of statements in <code>method</code>.
	 *
	 * @pre dependees != null and dependees.oclIsKindOf(Collection(Stmt))
	 * @pre dependents != null and dependents.oclIsKindOf(Collection(Stmt))
	 * @pre method != null and stmtList != null and stmtList.oclIsKindOf(Stmt)
	 */
	private void recordDependenceInfoInBB(final Collection dependees, final SootMethod method, final Collection dependents,
		final List stmtList) {
		Collection c = new HashSet(dependees);
		List de = (List) dependeeMap.get(method);

		for (Iterator i = dependents.iterator(); i.hasNext();) {
			Stmt dependent = (Stmt) i.next();
			de.set(stmtList.indexOf(dependent), c);
		}

		Map stmt2List = (Map) dependentMap.get(method);

		for (Iterator i = dependees.iterator(); i.hasNext();) {
			Stmt dependee = (Stmt) i.next();
			Collection dt = (Collection) stmt2List.get(dependee);
			dt.addAll(dependents);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.18  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.

   Revision 1.17  2003/09/28 06:20:38  venku
   - made the core independent of hard code used to create unit graphs.
     The core depends on the environment to provide a factory that creates
     these unit graphs.
   Revision 1.16  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.15  2003/09/15 01:40:48  venku
   - well, getMethods() was not changed in findPreDivPoints(). FIXED.
   Revision 1.14  2003/09/13 05:56:08  venku
   - bumped up log levels to error.
   Revision 1.13  2003/09/13 05:42:07  venku
   - What if the unit graphs for all methods are unavailable?  Hence,
     added a method to AbstractAnalysis to retrieve the methods to
     process.  The subclasses work only on this methods.
   Revision 1.12  2003/09/12 22:33:08  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
   Revision 1.11  2003/09/12 08:09:14  venku
   - Well, well, well.  Things work in the presence of exceptions too.
   - However, statements in a cycle are indicated as being dependent
     on the pre-divergent point that links the cycle with the control flow.
     This needs to be fixed.
   Revision 1.10  2003/09/10 23:18:25  venku
   - another one of those not so good solutions.  So, checking in to
     start all over again.
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
