
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

import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.DirectedGraph;
import edu.ksu.cis.indus.staticanalyses.support.INode;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

		if (!list.equals(Collections.EMPTY_LIST)) {
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
		List list = (List) dependeeMap.get(method);

		if (!list.equals(Collections.EMPTY_LIST)) {
			int index = getStmtList((SootMethod) method).indexOf(dependeeStmt);

			if (!list.get(index).equals(Collections.EMPTY_LIST)) {
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
				Stmt de = (Stmt) cd.get(j);

				if (de != null) {
					temp.append("\t\t" + stmts.get(j) + " --> " + de + "\n");
					localEdgeCount++;
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
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param graph DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	protected BitSet[] computeControlDependency(final DirectedGraph graph) {
		final List NODES = graph.getNodes();
		final int NUM_OF_NODES = NODES.size();
		BitSet[][] cd = new BitSet[NUM_OF_NODES][NUM_OF_NODES];

		// calculate the successors and predecessors in the graph disregarding backedges.
		// dag : Map(Pair(Sequence(BasicBlock), (BasicBlock)))
		Map dag = getDAG(graph);
		WorkBag wb = new WorkBag(WorkBag.FIFO);
		wb.addAllWorkNoDuplicates(graph.getHeads());

		Collection processed = new HashSet();

		while (wb.hasWork()) {
			BasicBlock bb = (BasicBlock) wb.getWork();
			Pair dagBlock = (Pair) dag.get(bb);

			if (!processed.containsAll((Collection) dagBlock.getFirst())) {
				wb.addWorkNoDuplicates(bb);
				System.out.println("continuing \n" + bb + "\n" + processed);
				continue;
			}

			// propogate data to the successors   
			int currIndex = NODES.indexOf(bb);
			Collection succs = (Collection) dagBlock.getSecond();
			BitSet[] currCD = cd[currIndex];

			for (int index = NUM_OF_NODES - 1; index >= 0; index--) {
				if (currCD[index] != null) {
					for (Iterator i = succs.iterator(); i.hasNext();) {
						int succIndex = NODES.indexOf(i.next());
						BitSet succCD = cd[succIndex][index];

						if (succCD == null) {
							succCD = new BitSet();
							cd[succIndex][index] = succCD;
						}
						succCD.or(currCD[index]);
					}
				}
			}

			// inject new information into the successors if there are more than one successors.
			if (succs.size() > 1) {
				int count = 0;

				for (Iterator i = succs.iterator(); i.hasNext();) {
					Object succ = i.next();
					BitSet succCDS = cd[NODES.indexOf(succ)][currIndex];

					if (succCDS == null) {
						succCDS = new BitSet();
						cd[NODES.indexOf(succ)][currIndex] = succCDS;
					}
					succCDS.set(count++);
				}
			}

			// Add the successors of the node 
			wb.addAllWorkNoDuplicates(succs);
			processed.add(bb);
		}

		// compute the idom        
		BitSet[] result = new BitSet[NUM_OF_NODES];

		for (int i = NUM_OF_NODES - 1; i >= 0; i--) {
			result[i] = new BitSet();

			for (int j = NUM_OF_NODES - 1; j >= 0; j--) {
				if (j != i) {
					int noOfSuccs = ((Collection) ((Pair) dag.get(NODES.get(j))).getSecond()).size();
					BitSet temp = cd[i][j];
					System.out.println(i + " " + j + " " + cd[i][j] + "  " + noOfSuccs);

					if (temp != null && temp.cardinality() != noOfSuccs) {
						result[i].set(j);
					}
				}
			}
		}

		for (int i = 0; i < result.length; i++) {
			System.out.println("CDS: " + result[i] + " " + ((BasicBlock) NODES.get(i)).getStmtsOf());
		}

		return result;
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
	protected BitSet[] computeControlDependencyOld(final DirectedGraph graph) {
		BitSet[] preds = graph.getAllPredsAsBitSet();
		BitSet[] succs = graph.getAllSuccsAsBitSet();
		int size = graph.size();
		BitSet[] cds = new BitSet[size];

		for (int i = cds.length - 1; i >= 0; i--) {
			cds[i] = new BitSet(size);
		}

		List nodes = graph.getNodes();
		WorkBag workbag = new WorkBag(WorkBag.LIFO);
		workbag.addAllWork(graph.getHeads());

		BitSet testSet = new BitSet(size);

		System.out.println("HEADS:" + graph.getHeads());
		System.out.println("NODES:" + graph.getNodes());

		while (true) {
			while (workbag.hasWork()) {
				INode node = (INode) workbag.getWork();
				int currIndex = nodes.indexOf(node);
				BitSet currSuccs = succs[currIndex];
				int noOfSuccs = currSuccs.cardinality();
				BitSet currPreds = preds[currIndex];
				int noOfPreds = currPreds.cardinality();
				System.out.println("CURRSUCCS: " + currIndex + " " + ((BasicBlock) node).getStmtsOf() + "  " + currSuccs);

				if (noOfPreds > 1) {
					BitSet posCD = new BitSet(size);

					for (int i = currPreds.nextSetBit(0); i >= 0; i = currPreds.nextSetBit(i + 1)) {
						System.out.println(i + " " + succs[i]);

						if (succs[i].cardinality() > 1) {
							posCD.set(i);
						}
					}

					BitSet j = new BitSet();
					System.out.println("noOFPREDS>1: " + posCD);

					for (int i = currPreds.nextSetBit(0); i >= 0; i = currPreds.nextSetBit(i + 1)) {
						if (succs[i].cardinality() == 1) {
							j.clear();
							j.or(cds[i]);
							j.and(posCD);

							if (j.cardinality() > 0) {
								cds[currIndex].clear(i);
							}
							System.out.println("J: " + j + " " + cds[currIndex]);
						}
					}
				}

				if (noOfSuccs > 1) {
					/*
					 * If there is more than one successor then it is possible that the successors are control dependent on
					 * the current statement.  Hence, capture this in cds and hoist the successor for processing.
					 */
					for (int i = currSuccs.nextSetBit(0); i >= 0; i = currSuccs.nextSetBit(i + 1)) {
						cds[i].set(currIndex);
						workbag.addWork(nodes.get(i));
					}
				} else if (noOfSuccs == 1) {
					/*
					 * If there is just one successor then the successor may be control dependent on the same node as the
					 * current node.  Record this info and hoist the successor for processing.
					 */
					int succIndex = currSuccs.nextSetBit(0);
					workbag.addWork(nodes.get(succIndex));

					/*
					 * For the sake of termination, sever the successor relation of the current node with it's predecessor
					 * nodes.
					 *
					                       for (int i = currPreds.nextSetBit(0); i >= 0; i = currPreds.nextSetBit(i + 1)) {
					                           succs[i].clear(currIndex);
					                       }*/
					// copy the control dependency info from the current node to the successor. 
					cds[succIndex].or(cds[currIndex]);
				}

				for (int i = 0; i < size; i++) {
					System.out.println(i + " " + cds[i] + " " + succs[i] + " " + preds[i]);
				}
			}

			boolean flag = false;

			for (int i = 0; i < size; i++) {
				if (cds[i].cardinality() > 1) {
					flag = true;
				}
			}
			System.out.println("TESTSET: " + testSet);

			if (flag) {
				for (int i = 0; i < size; i++) {
					if (preds[i].nextSetBit(0) >= 0) {
						workbag.addWork(nodes.get(i));
					}
				}
			} else {
				break;
			}
		}

		for (int i = size - 1; i >= 0; i--) {
			System.out.println(i + ") " + cds[i] + " " + ((BasicBlock) nodes.get(i)).getStmtsOf());
		}
		return cds;
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
			mDependent.add(Collections.EMPTY_LIST);
		}

		boolean flag = false;

		for (Iterator i = sl.iterator(); i.hasNext();) {
			Stmt j = (Stmt) i.next();
			BasicBlock bb = graph.getEnclosingBlock(j);
			int l = bbCDBitSets[nodes.indexOf(bb)].nextSetBit(0);

			if (l != -1) {
				flag = true;

				BasicBlock bb2 = graph.getEnclosingBlock((Stmt) sl.get(l));
				mDependee.set(sl.indexOf(j), sl.get(bb2._trailer));

				List dependents = (List) mDependent.get(bb2._trailer);

				if (dependents.equals(Collections.EMPTY_LIST)) {
					dependents = new ArrayList();
					mDependent.set(bb2._trailer, dependents);
				}
				dependents.add(j);
			}
		}

		if (flag) {
			dependentMap.put(method, new ArrayList(mDependent));
			dependeeMap.put(method, new ArrayList(mDependee));
		} else {
			dependentMap.put(method, Collections.EMPTY_LIST);
			dependeeMap.put(method, Collections.EMPTY_LIST);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param c DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Map getDAG(final DirectedGraph c) {
		Map result = new HashMap();
		Map srcdestBackEdges = mapify(c.getBackEdges(), true);
		Map destsrcBackEdges = mapify(c.getBackEdges(), false);

		Collection succs = new HashSet();
		Collection preds = new HashSet();

		for (Iterator i = c.getNodes().iterator(); i.hasNext();) {
			INode node = (INode) i.next();
			Collection backSuccessors = (Collection) srcdestBackEdges.get(node);
			succs.clear();
			succs.addAll(node.getSuccsOf());

			if (backSuccessors != null) {
				succs.removeAll(backSuccessors);
			}

			backSuccessors = (Collection) destsrcBackEdges.get(node);
			preds.clear();
			preds.addAll(node.getPredsOf());

			if (backSuccessors != null) {
				preds.removeAll(backSuccessors);
			}
			result.put(node, new Pair(new ArrayList(preds), new ArrayList(succs)));
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param pairs DOCUMENT ME!
	 * @param forward DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private Map mapify(final Collection pairs, final boolean forward) {
		Map result = new HashMap();

		for (Iterator i = pairs.iterator(); i.hasNext();) {
			Pair pair = (Pair) i.next();
			Object key;
			Object value;

			if (forward) {
				key = pair.getFirst();
				value = pair.getSecond();
			} else {
				key = pair.getSecond();
				value = pair.getFirst();
			}

			Collection c = (Collection) result.get(key);

			if (c == null) {
				c = new ArrayList();
				result.put(key, c);
			}
			c.add(value);
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
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
