
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

import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.DirectedGraph;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class provides intraprocedural control dependency information. This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Sequence(Stmt)))
 * @invariant dependeeMap.entrySet()->forall(o | o.getValue().size() = o.getKey().getActiveBody().getUnits().size())
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod, Sequence(Set(Stmt))))
 * @invariant dependentMap.entrySet()->forall(o | o.getValue().size() = o.getKey().getActiveBody().getUnits().size())
 */
public class ControlDA
  extends DependencyAnalysis {
	/*
	 * The dependence information is stored as follows: For each method, a list of collection is maintained.  Each location in
	 * the list corresponds to the statement at the same location in the statement list of the method.  The collection is the
	 * statements to which the statement at the location of the collection is related via control dependence.
	 */

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ControlDA.class);

	/**
	 * Returns the statements on which <code>dependentStmt</code> depends on in the given <code>method</code>.
	 *
	 * @param dependentStmt is the dependent of interest.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre dependentStmt.oclIsKindOf(Stmt)
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt)) and result.size() == 1
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object method) {
		Collection result = Collections.EMPTY_LIST;
		List list = (List) dependeeMap.get(method);

		if (list != null) {
			int index = getStmtList((SootMethod) method).indexOf(dependentStmt);

			if (list.get(index) != null) {
				result = Collections.singletonList(list.get(index));
			}
		}
		return result;
	}

	/**
	 * Returns the statements which depend on <code>dependeeStmt</code> in the given <code>method</code>.
	 *
	 * @param dependeeStmt is the dependee of interest.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre dependeeStmt.isOclKindOf(Stmt)
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependents(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object method) {
		Collection result = Collections.EMPTY_LIST;
		List list = (List) dependentMap.get(method);

		if (list != null) {
			int index = getStmtList((SootMethod) method).indexOf(dependeeStmt);

			if (list.get(index) != null) {
				result = Collections.unmodifiableCollection((Collection) list.get(index));
			}
		}
		return result;
	}

	/**
	 * Calculates the control dependency information for the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		for (Iterator i = getMethods().iterator(); i.hasNext();) {
			SootMethod currMethod = (SootMethod) i.next();
			BasicBlockGraph bbGraph = getBasicBlockGraph(currMethod);

			if (bbGraph == null) {
				LOGGER.error("Method " + currMethod.getSignature() + " did not have a basic block graph.");
				continue;
			}

			BitSet[] bbCDBitSets = computeControlDependency(bbGraph);
			fixupMaps(bbGraph, bbCDBitSets, currMethod);
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
			new StringBuffer("Statistics for control dependence as calculated by " + getClass().getName() + "\n");
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependeeMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod method = (SootMethod) entry.getKey();
			localEdgeCount = 0;

			List stmts = getStmtList(method);
			List cd = (List) entry.getValue();

			for (int j = 0; j < stmts.size(); j++) {
				if (cd == null) {
					continue;
				}

				Collection dees = (Collection) cd.get(j);

				if (dees != null) {
					temp.append("\t\t" + stmts.get(j) + " --> " + dees + "\n");
					localEdgeCount += dees.size();
				} else {
					temp.append("\t\t" + stmts.get(j) + " --> METHOD_ENTRY\n");
				}
			}

			result.append("\tFor " + entry.getKey() + " there are " + localEdgeCount + " control dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " control dependence edges exist.");
		return result.toString();
	}

	/**
	 * Calculates the control dependency from a directed graph.  This calculates the dependence information in terms of nodes
	 * in the graph.  This is later translated to statement level information by {@link #fixupMaps fixupMaps}.
	 *
	 * @param graph for which dependence info needs to be calculated.  Each node in the graph should have an unique index and
	 * 		  the indices should start from 0.
	 *
	 * @return an array of bitsets.  The length of the array and each of the bitset in it is equal to the number of nodes in
	 * 		   the graph.  The nth bitset captures the dependence information via set bits.  The BitSets capture
	 * 		   dependent->dependee information.
	 *
	 * @post result.oclIsTypeOf(Sequence(BitSet)) and result->size() == graph.getNodes().size()
	 * @post result->forall(o | o.size() == graph.getNodes().size())
	 */
	protected BitSet[] computeControlDependency(final DirectedGraph graph) {
		Map dag = graph.getDAG();
		final List NODES = graph.getNodes();
		final int NUM_OF_NODES = NODES.size();
		BitSet[][] cd = new BitSet[NUM_OF_NODES][NUM_OF_NODES];
		BitSet[] result = new BitSet[NUM_OF_NODES];
		Collection processed = new ArrayList();
		WorkBag wb = new WorkBag(WorkBag.FIFO);
		wb.addAllWorkNoDuplicates(graph.getHeads());

		while (wb.hasWork()) {
			BasicBlock bb = (BasicBlock) wb.getWork();
			Pair dagBlock = (Pair) dag.get(bb);

			if (!processed.containsAll((Collection) dagBlock.getFirst())) {
				wb.addWorkNoDuplicates(bb);
				continue;
			}

			// propogate data to the successors   
			int currIndex = NODES.indexOf(bb);
			Collection succs = (Collection) dagBlock.getSecond();
			BitSet[] currCD = cd[currIndex];
			BitSet currResult = new BitSet();

			for (Iterator j = processed.iterator(); j.hasNext();) {
				Object o = j.next();
				int pIndex = NODES.indexOf(o);
				int pSuccs = ((Collection) ((Pair) dag.get(o)).getSecond()).size();
				BitSet pCD = currCD[pIndex];

				if (pCD != null) {
					boolean assignFlag = pCD.cardinality() == pSuccs;

					if (assignFlag) {
						currCD[pIndex] = null;
					} else {
						currResult.set(pIndex);
					}

					for (Iterator i = succs.iterator(); i.hasNext();) {
						int succIndex = NODES.indexOf(i.next());
						BitSet[] succCDs = cd[succIndex];

						if (assignFlag) {
							succCDs[pIndex] = pCD;
						} else {
							BitSet succCD = succCDs[pIndex];

							if (succCD == null) {
								succCD = new BitSet();
								succCDs[pIndex] = succCD;
							}
							succCD.or(pCD);
						}
					}
				}
			}

			if (succs.size() > 1) {
				int count = 0;

				for (Iterator i = succs.iterator(); i.hasNext();) {
					int succIndex = NODES.indexOf(i.next());
					BitSet succCD = cd[succIndex][currIndex];

					if (succCD == null) {
						succCD = new BitSet();
						cd[succIndex][currIndex] = succCD;
					}

					succCD.set(count++);
				}
			}

			result[currIndex] = currResult;
			// Add the successors of the node 
			wb.addAllWorkNoDuplicates(succs);
			processed.add(bb);
		}

		for (int i = 0; i < NUM_OF_NODES; i++) {
			System.out.println(i + " " + result[i] + " " + ((BasicBlock) NODES.get(i)).getStmtsOf());
		}

		return result;
	}

	/**
	 * Translates the dependence information as captured in <code>bbCDBitSets</code> to statement level info and populates
	 * the dependeXXMap fields.
	 *
	 * @param graph is the basic block graph corresponding to <code>method</code>.
	 * @param bbCDBitSets is the array that contains the basic block level dependence information as calculated by {@link
	 * 		  #computeControlDependency(DirectedGraph) computeControlDependency}.
	 * @param method for which the maps are being populated.
	 *
	 * @pre graph != null and bbCDBitSets != null and method != null
	 * @post dependentMap.get(method) != null
	 * @post dependentMap.values()->forall(o | o->forall(p | p != null()))
	 * @post dependeeMap.get(method) != null
	 * @post dependeeMap.values()->forall(o | o->forall(p | p != null()))
	 */
	protected void fixupMaps(final BasicBlockGraph graph, final BitSet[] bbCDBitSets, final SootMethod method) {
		List nodes = graph.getNodes();
		List sl = getStmtList(method);
		List mDependee = new ArrayList();
		List mDependent = new ArrayList();

		for (int i = sl.size(); i > 0; i--) {
			mDependee.add(null);
			mDependent.add(null);
		}

		boolean flag = false;

		for (int i = bbCDBitSets.length - 1; i >= 0; i--) {
			BitSet cd = bbCDBitSets[i];
			flag |= cd != null;

			if (cd != null && !cd.isEmpty()) {
				Collection cdp = new ArrayList();
				BasicBlock bb = (BasicBlock) nodes.get(i);

				for (Iterator j = bb.getStmtsOf().iterator(); j.hasNext();) {
					mDependee.set(sl.indexOf(j.next()), cdp);
				}

				for (int j = cd.nextSetBit(0); j != -1; j = cd.nextSetBit(j + 1)) {
					BasicBlock cdbb = (BasicBlock) nodes.get(j);
					cdp.add(cdbb.getTrailerStmt());

					int deIndex = sl.indexOf(cdbb.getTrailerStmt());
					Collection dees = (Collection) mDependent.get(deIndex);

					if (dees == null) {
						dees = new ArrayList();
						mDependent.set(deIndex, dees);
					}
					dees.add(bb.getStmtsOf());
				}
			}
		}

		if (flag) {
			dependentMap.put(method, new ArrayList(mDependent));
			dependeeMap.put(method, new ArrayList(mDependee));
		} else {
			dependentMap.put(method, null);
			dependeeMap.put(method, null);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/09/15 00:58:25  venku
   - well, things were fine I guess. Nevertheless, they are more
     streamlined now.
   Revision 1.8  2003/09/14 23:24:26  venku
   - alas a working control DA. However, I have not been able
     to compile a program such that the basic block has two CD points.
     This is possible when the else branch of the enclosed and enclosing
     if's are identical.
   Revision 1.7  2003/09/13 05:56:34  venku
   - an early commit to a (hopefully) working solution.
   - need to document it still.
   Revision 1.6  2003/09/12 23:49:46  venku
   - another one of those unsuccessful solutions.  Checking in to start over.
   Revision 1.5  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.4  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.2  2003/08/09 23:29:52  venku
   Ripple Effect of renaming Inter/Intra procedural data DAs to Aliased/NonAliased data DA.
   Revision 1.1  2003/08/07 06:38:05  venku
   Major:
    - Moved the packages under indus umbrella.
    - Renamed MethodLocalDataDA to NonAliasedDataDA.
    - Added class for AliasedDataDA.
    - Documented and specified the classes.
 */
