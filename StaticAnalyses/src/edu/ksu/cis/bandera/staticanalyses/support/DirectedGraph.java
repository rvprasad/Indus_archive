
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

package edu.ksu.cis.bandera.staticanalyses.support;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * This class represents a directed graph in which nodes are represented by <code>Node</code> objects.  It is abstract for
 * the reason of extensibility.  The subclasses are responsible for maintaining the collection of nodes that make up this
 * graph. The nodes in the graph are to be ordered.  the subclasses can determine the ordering, but it needs to be
 * unmodifiable over the lifetime of the graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class DirectedGraph {
	/**
	 * The set of nodes that constitute the head nodes of this graph.  <i>This needs to be populated by the subclass.</i>
	 *
	 * @invariant heads.oclIsKindOf(Set(edu.ksu.cis.bandera.staticanalyses.support.Node))
	 */
	protected final Set heads = new HashSet();

	/**
	 * The set of nodes that constitute the tail nodes of this graph.  <i>This needs to be populated by the subclass.</i>
	 *
	 * @invariant heads.oclIsKindOf(Set(edu.ksu.cis.bandera.staticanalyses.support.Node))
	 */
	protected final Set tails = new HashSet();

	/**
	 * This indicates if this graph has a spanning Forest.
	 */
	protected boolean hasSpanningTree;

	/**
	 * This maps a node to it's spanning successor nodes.
	 *
	 * @invariant spanningSuccs.keySet()->forall( o | o.oclIsKindOf(edu.ksu.cis.bandera.staticanalyses.support.Node))
	 * @invariant spanningSuccs.values()->forall( o | o.oclIsKindOf(Set) and o->forall( p | p.oclIsKindOf(Node)))
	 */
	private Map spanningSuccs;

	/**
	 * Retrieves the predecessor relation in the graph.  The <code>BitSet</code> object occurring at the location (in the
	 * returned array) captures the predecessor information of the node associated with the location.  The size of all
	 * BitSet objects in the array is equal to the number of nodes in the graph.  The position of each set bit in the BitSet
	 * indicates index of the predecessor node in the list of nodes.
	 *
	 * @return the predecessor relation in the graph.
	 *
	 * @post result.oclIsKindOf(Sequence)
	 * @post result->size = size
	 * @post result->forall( o | o.oclIsKindOf(Sequence) and o->size = size)
	 */
	public BitSet [] getAllPredsAsBitSet() {
		List nodes = getNodes();
		BitSet result[] = new BitSet[nodes.size()];

		for(int i = 0, len = nodes.size(); i < len; i++) {
			Node temp = (Node) nodes.get(i);
			BitSet preds = new BitSet(nodes.size());

			for(Iterator j = temp.getPredsOf().iterator(); j.hasNext();) {
				preds.set(nodes.indexOf(j));
			}
			result[i] = preds;
		}

		return result;
	}

	/**
	 * Retrieves the successor nodes of the given node only.  The successors as they occur in the spanning tree of the graph
	 * are returned.
	 *
	 * @param node of interest.
	 *
	 * @return the successor nodes(<code>Node</code>) of <code>node</code>.
	 *
	 * @post result.oclIsKindOf(Collection)
	 * @post result->forall( o | o.oclIsKindOf(edu.ksu.cis.bandera.staticanalyses.support.Node))
	 */
	public Collection getForwardSuccsOf(Node node) {
		if(!hasSpanningTree) {
			createSpanningForest();
		}
		return (Collection) spanningSuccs.get(node);
	}

	/**
	 * Retrieves the head nodes of this graph.
	 *
	 * @return the head nodes(<code>Node</code>) of this graph.
	 *
	 * @post result.oclIsKindOf(Collection)
	 * @post result->forall( o | o.oclIsKindOf(edu.ksu.cis.bandera.staticanalyses.support.Node))
	 * @post result->includesAll(heads)
	 */
	public Collection getHeads() {
		return Collections.unmodifiableCollection(heads);
	}

	/**
	 * Retrieves the nodes in the graph.  The order of the nodes should be the same across calls to this method, if no nodes
	 * are added or removed.  In case of addition and removal, the relative ordering between the old nodes should not change
	 * across calls.
	 *
	 * @return the nodes(<code>Node</code>) in the graph.
	 *
	 * @post result.oclIsKindOf(Sequence)
	 * @post result->forall( o | o.oclIsKindOf(edu.ksu.cis.bandera.staticanalyses.support.Node))
	 */
	public abstract List getNodes();

	/**
	 * Retrieves the successor relation in the graph.  Please refer to {@link #getAllPredsAsBitSet() getAllPredAsBitSet} for
	 * details.
	 *
	 * @return the successor relation in the graph.
	 *
	 * @post result.oclIsKindOf(Sequence)
	 */
	public BitSet [] getAllSuccsAsBitSet() {
		List nodes = getNodes();
		BitSet result[] = new BitSet[nodes.size()];

		for(int i = 0, len = nodes.size(); i < len; i++) {
			Node temp = (Node) nodes.get(i);
			BitSet succs = new BitSet(nodes.size());

			for(Iterator j = temp.getSuccsOf().iterator(); j.hasNext();) {
				succs.set(nodes.indexOf(j));
			}
			result[i] = succs;
		}

		return result;
	}

	/**
	 * Checks if the given destination nodes is reachable from the givne source node in the given direction.
	 *
	 * @param src is the source node in the graph.
	 * @param dest is the destination node in the graph.
	 * @param forward <code>true</code> indicates by forward traversal; <code>false</code> indicates backward traversal.
	 *
	 * @return <code>true</code> if <code>dest</code> is reachable from <code>src</code> in the given direction;
	 * 		   <code>false</code>, otherwise.
	 */
	public boolean isReachable(Node src, Node dest, boolean forward) {
		boolean result = false;
		Collection processed = new HashSet();
		WorkBag worklist = new WorkBag(WorkBag.LIFO);
		worklist.addAllWorkNoDuplicates(src.getSuccsNodesInDirection(forward));

		while(!worklist.isEmpty()) {
			Node node = (Node) worklist.getWork();

			if(node == dest) {
				result = true;
				break;
			}

			if(!processed.contains(node)) {
				processed.add(node);
				worklist.addAllWorkNoDuplicates(node.getSuccsNodesInDirection(forward));
			}
		}
		return result;
	}

	/**
	 * Retrieves the succession information as it occues in this graph's spanning tree.  The returned map maps a
	 * <code>Node</code> to a <code>Colleciton</code> of <code>Node</code> objects which succeed the value node upon
	 * creating a spanning tree of this graph.
	 *
	 * @return an read-only copy of succession information as it occurs in this graph's spanning tree.
	 *
	 * @post result.equals(spanningSuccs)
	 */
	public final Map getSpanningSuccs() {
		if(!hasSpanningTree) {
			createSpanningForest();
			hasSpanningTree = true;
		}
		return Collections.unmodifiableMap(spanningSuccs);
	}

	/**
	 * Retrieves the tail nodes of this graph.
	 *
	 * @return the tail nodes(<code>Node</code>) of this graph.
	 *
	 * @post result.oclIsKindOf(java.util.Collection)
	 * @post result->forall( o | o.oclIsKindOf(edu.ksu.cis.bandera.staticanalyses.support.Node))
	 * @post result->includesAll(tails)
	 */
	public Collection getTails() {
		return Collections.unmodifiableCollection(tails);
	}

	/**
	 * Returns the size of this graph.
	 *
	 * @return the number of nodes in this graph.
	 */
	public abstract int size();

	/**
	 * Returns the cycles that occur in the graph.
	 *
	 * @return a collection of list of nodes which form cycles in this graph.  The head of the list is the initiator/head of
	 * 		   the cycle.
	 *
	 * @post result->forall(o | o.oclIsKindOf(List(Node)))
	 */
	public final Collection getCycles() {
		Collection result = new ArrayList();
		WorkBag wb = new WorkBag(WorkBag.LIFO);
		Stack dfsPath = new Stack();

		for(Iterator i = getHeads().iterator(); i.hasNext();) {
			Node head = (Node) i.next();
			wb.clear();
			wb.addWork(head);
			dfsPath.clear();

			while(!(wb.isEmpty())) {
				Object o = wb.getWork();

				if(o instanceof Marker) {
					Object temp = ((Marker) o).content;

					for(Object obj = dfsPath.pop(); !temp.equals(obj); obj = dfsPath.pop()) {
						;
					}
				} else {
					Node node = (Node) o;

					if(dfsPath.contains(node)) {
						result.add(new ArrayList(dfsPath.subList(dfsPath.indexOf(node), dfsPath.size())));
					} else {
						Collection succs = node.getSuccsOf();

						if(!succs.isEmpty()) {
							dfsPath.push(node);
							wb.addWork(new Marker(node));
							wb.addAllWork(succs);
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Returns a collection of strongly-connected components in this graph.
	 *
	 * @param topDown <code>true</code> indicates returned sccs should be in the top-down order; <code>false</code>,
	 * 		  indicates bottom-up.
	 *
	 * @return a collection of <code>List</code> of <code>Node</code>s that form SCCs in this graph. NOTE: It is possible to
	 * 		   reach nodes not in the SCC but in this graph by following edges in reverse direction.
	 *
	 * @post result.isOclKindOf(Collection(List(Node)))
	 */
	public final Collection getSCCs(boolean topDown) {
		Collection result = new ArrayList();
		List nodes = getNodes();
		Map finishTime2node = new HashMap();
		Collection processed = new HashSet();
		int time = 0;

		for(Iterator i = getHeads().iterator(); i.hasNext();) {
			Node node = (Node) i.next();

			if(!processed.contains(node)) {
				time = getFinishTimes(nodes, node, processed, finishTime2node, time, true);
			}
		}

		List c = new ArrayList();
		c.addAll(finishTime2node.keySet());
		Collections.sort(c);
		Collections.reverse(c);

		Map node2finishTime = new HashMap();

		for(Iterator i = c.iterator(); i.hasNext();) {
			Object element = (Object) i.next();
			node2finishTime.put(finishTime2node.get(element), element);
		}
		processed.clear();

		Stack stack = new Stack();
		Map fn2scc = new HashMap();

		for(Iterator i = c.iterator(); i.hasNext() && !processed.containsAll(nodes);) {
			Integer fn = (Integer) i.next();
			Node node = (Node) finishTime2node.get(fn);

			if(processed.contains(node)) {
				continue;
			}
			stack.push(node);

			List scc = new ArrayList();

			while(!stack.isEmpty()) {
				node = (Node) stack.pop();

				if(processed.contains(node)) {
					continue;
				}

				Integer temp = (Integer) node2finishTime.get(node);

				if(temp.intValue() > fn.intValue()) {
					fn = temp;
				}
				scc.add(node);
				processed.add(node);
				stack.addAll(node.getPredsOf());
			}
			fn2scc.put(fn, scc);
		}
		c.clear();
		c.addAll(fn2scc.keySet());
		Collections.sort(c);

		if(topDown) {
			Collections.reverse(c);
		}

		for(Iterator i = c.iterator(); i.hasNext();) {
			result.add(fn2scc.get(i.next()));
		}
		return result;
	}

	/**
	 * Checks if the given node occurs in a cycle in the graph.  This may be sufficient in some cases rather than capturing
	 * the cycle itself.
	 *
	 * @param node which may occur in a cycle.
	 *
	 * @return <code>true</code> if <code>node</code> occurs in cycle; <code>false</code>, otherwise.
	 */
	public final boolean occursInCycle(Node node) {
		boolean result = false;
		WorkBag wb = new WorkBag(WorkBag.LIFO);
		wb.addWork(node);

		Collection processed = new HashSet();

		while(!wb.isEmpty()) {
			Node temp = (Node) wb.getWork();

			if(processed.contains(temp)) {
				result = true;
				break;
			}
			processed.add(temp);
			wb.addAllWorkNoDuplicates(temp.getSuccsOf());
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param nodes DOCUMENT ME!
	 * @param topdown DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public static final List performTopologicalSort(List nodes, boolean topdown) {
		List result = new ArrayList();
		Map finishTime2node = new HashMap();
		Collection processed = new ArrayList();
		int time = 0;

		for(Iterator i = nodes.iterator(); i.hasNext();) {
			Node node = (Node) i.next();

			if(processed.contains(node)) {
				continue;
			}
			time = getFinishTimes(nodes, node, processed, finishTime2node, time, topdown);
		}

		List temp = new ArrayList(finishTime2node.keySet());
		Collections.sort(temp);

		for(Iterator i = temp.iterator(); i.hasNext();) {
			result.add(0, finishTime2node.get(i.next()));
		}
		return result;
	}

	/**
	 * Calculates the finish times for the nodes of this graph.  version should be coded.
	 *
	 * @param nodes in this graph.
	 * @param node to start dfs from.
	 * @param processed are the nodes processed during dfs.
	 * @param finishTime2node maps finishTime(<code>Integer</code>) to a node.
	 * @param time is the counter used to calculate finish times.
	 * @param forward is the direction in which finish time should be calculated.
	 *
	 * @return the finish time after the given dfs traversal.
	 */
	private static int getFinishTimes(List nodes, Node node, Collection processed, Map finishTime2node, int time,
		boolean forward) {
		processed.add(node);
		time++;

		Iterator i = node.getSuccsNodesInDirection(forward).iterator();

		for(; i.hasNext();) {
			Node succ = (Node) i.next();

			if(processed.contains(succ) || !nodes.contains(succ)) {
				continue;
			}
			time = getFinishTimes(nodes, succ, processed, finishTime2node, time, forward);
		}
		finishTime2node.put(new Integer(++time), node);
		return time;
	}

	/**
	 * Creates the spanning forest of the graph.
	 *
	 * @post hasSpanningTree = true
	 */
	private final void createSpanningForest() {
		Collection processed = new HashSet();

		if(spanningSuccs == null) {
			spanningSuccs = new HashMap();
		}
		spanningSuccs.clear();

		for(Iterator i = getHeads().iterator(); i.hasNext();) {
			Node node = (Node) i.next();

			if(!processed.contains(node)) {
				Collection temp = new HashSet();
				spanningSuccs.put(node, temp);

				for(Iterator j = node.getSuccsOf().iterator(); j.hasNext();) {
					Node succ = (Node) j.next();

					if(!processed.contains(succ)) {
						temp.add(succ);
					}
				}
			}
			processed.add(node);
		}
		hasSpanningTree = true;
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.3  2003/02/21 07:22:22  venku
Changed \@pre to $pre in the ocl constraints specified in Javadoc.

Revision 1.2  2003/02/19 17:31:19  venku
Things are in flux.  Stabilizing them with CVS.


*****/
