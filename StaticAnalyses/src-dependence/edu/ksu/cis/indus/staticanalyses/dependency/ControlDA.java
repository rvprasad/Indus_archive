
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

import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.DirectedGraph;
import edu.ksu.cis.indus.staticanalyses.support.INode;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class provides intraprocedural control dependency information. This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports">A Formal  Study of Slicing for Multi-threaded Program with
 * JVM Concurrency Primitives"</a>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependeeMap.oclIsKindOf(Map(SootMethod, Sequence(Stmt)))
 * @invariant dependeeMap.entrySet()->forall(o | o.getValue().size() = o.getKey().getBody(Jimple.v()).getStmtList(). size())
 * @invariant dependentMap.oclIsKindOf(Map(SootMethod, Sequence(Bag(Stmt))))
 * @invariant dependentMap.entrySet()->forall(o | o.getValue().size() = o.getKey().getBody(Jimple.v()).getStmtList().size())
 */
public class ControlDA
  extends DependencyAnalysis {
	/*
	 * The dependence information is stored as follows: For each method, a list of collection is maintained.  Each location in
	 * the list corresponds to the statement at the same location in the statement list of the method.  The collection is the
	 * statements to which the statement at the location of the collection is related via control dependence.
	 */

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
		List list = (List) dependeeMap.get(method);

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
	 * @return <code>true</code> as analysis happens in a single run.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		for (Iterator i = method2stmtGraph.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod currMethod = (SootMethod) entry.getKey();
			BasicBlockGraph bbGraph = getBasicBlockGraph(currMethod);
			BitSet[] bbCDBitSets = computeControlDependency(bbGraph);
			fixupMaps(bbGraph, bbCDBitSets, currMethod);
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
			new StringBuffer("Statistics for Control dependence as calculated by " + getClass().getName() + "\n");
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
			result.append("\tFor " + entry.getKey() + " there are " + localEdgeCount + " Control dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " Control dependence edges exist.");
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
		BitSet[] preds = graph.getAllPredsAsBitSet();
		BitSet[] succs = graph.getAllSuccsAsBitSet();
		int size = graph.size();
		BitSet[] cds = new BitSet[size];

		for (int i = cds.length - 1; i >= 0; i--) {
			cds[i] = new BitSet();
		}

		List nodes = graph.getNodes();
		WorkBag workbag = new WorkBag(WorkBag.FIFO);
		workbag.addAllWork(graph.getHeads());

		BitSet testSet = new BitSet(size);

		while (true) {
			while (workbag.hasWork()) {
				INode node = (INode) workbag.getWork();
				int currIndex = nodes.indexOf(node);
				BitSet currSuccs = succs[currIndex];
				int noOfSuccs = currSuccs.cardinality();

				if (noOfSuccs > 1) {
					/*
					 * If there is more than on successor then it is possible that the successors are control dependent on the
					 * current statement.  Hence, capture this in cds and host the successor for processing.
					 */
					for (int i = currSuccs.nextSetBit(0); i >= 0; i = currSuccs.nextSetBit(i + 1)) {
						cds[i].set(currIndex);
						workbag.addWork(nodes.get(i));
					}
				} else if (noOfSuccs == 1) {
					/*
					 * If there is just one successor then the successor is control dependent on the same node as the current
					 * node.  Record this info and host the successor for processing.
					 */
					int succIndex = currSuccs.nextSetBit(0);
					workbag.addWork(nodes.get(succIndex));

					BitSet currPreds = preds[currIndex];

					/*
					 * For the sake of termination, severe the successor relation of the current node with it's predecessor
					 * nodes.
					 */
					for (int i = currPreds.nextSetBit(0); i >= 0; i = currPreds.nextSetBit(i + 1)) {
						succs[i].clear(currIndex);
					}

					// copy the current control dependency info for the succssor. 
					cds[succIndex].or(cds[currIndex]);

					/*
					 * For the sake of termination, severe the predecessor relation of the successor node with the current
					 * node.
					 */
					preds[succIndex].clear(currIndex);
				}
				currSuccs.clear();
			}
			testSet.clear();

			for (int i = 0; i < size; i++) {
				testSet.or(succs[i]);
			}

			if (testSet.nextSetBit(0) == -1) {
				break;
			} else {
				for (int i = 0; i < size; i++) {
					if (preds[i].nextSetBit(0) >= 0) {
						workbag.addWork(nodes.get(i));
					}
				}
			}
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
		Collections.fill(mDependee, Collections.EMPTY_LIST);
		Collections.fill(mDependent, Collections.EMPTY_LIST);

		for (Iterator i = sl.iterator(); i.hasNext();) {
			Stmt j = (Stmt) i.next();
			BasicBlock bb = graph.getEnclosingBlock(j);
			int l = bbCDBitSets[nodes.indexOf(bb)].nextSetBit(0);

			if (l != -1) {
				BasicBlock bb2 = graph.getEnclosingBlock((Stmt) sl.get(l));
				mDependee.set(sl.indexOf(j), Collections.singletonList(sl.get(bb2._TRAILER)));

				ArrayList dependents = (ArrayList) mDependent.get(bb2._TRAILER);

				if (dependents.equals(Collections.EMPTY_LIST)) {
					dependents = new ArrayList();
					mDependent.set(bb2._TRAILER, dependents);
				}
				dependents.add(j);
			}
		}
		dependentMap.put(method, mDependent);
		dependeeMap.put(method, mDependee);
	}
}

/*****
 ChangeLog:

$Log$

*****/
