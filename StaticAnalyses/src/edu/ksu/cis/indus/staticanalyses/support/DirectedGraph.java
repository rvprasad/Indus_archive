
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

package edu.ksu.cis.indus.staticanalyses.support;

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
 * This class represents a directed graph in which nodes are represented by <code>INode</code> objects.  It is abstract for
 * the reason of extensibility.  The subclasses are responsible for maintaining the collection of nodes that make up this
 * graph. The nodes in the graph are to be ordered.  The subclasses can determine the ordering, but it needs to be fixed
 * over the lifetime of the graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class DirectedGraph {
	/**
	 * The set of nodes that constitute the head nodes of this graph.  <i>This needs to be populated by the subclass.</i>
	 *
	 * @invariant heads.oclIsKindOf(Set(INode))
	 */
	protected final Set heads = new HashSet();

	/**
	 * The set of nodes that constitute the tail nodes of this graph.  <i>This needs to be populated by the subclass.</i>
	 *
	 * @invariant heads.oclIsKindOf(Set(INode))
	 */
	protected final Set tails = new HashSet();

	/**
	 * This indicates if this graph has a spanning forest.
	 */
	protected boolean hasSpanningForest;

	/**
	 * This maps a node to it's spanning successor nodes.
	 *
	 * @invariant spanningSuccs.keySet()->forall( o | o.oclIsKindOf(INode))
	 * @invariant spanningSuccs.values()->forall( o | o.oclIsKindOf(Set) and o->forall( p | p.oclIsKindOf(INode)))
	 */
	private Map spanningSuccs;

	/**
	 * Retrieves the predecessor relation in the graph.  The <code>BitSet</code> object occurring at the location (in the
	 * returned array) captures the predecessor information of the node at that location in the graph.  The size of all
	 * BitSet objects in the array is equal to the number of nodes in the graph.  The position of each set bit in the BitSet
	 * indicates index of the predecessor node in the list of nodes.
	 *
	 * @return the predecessor relation in the graph.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(Sequence))
	 * @post result->size == self.getNodes().size()
	 * @post result->forall( o | o->size == self.getNodes().size())
	 */
	public BitSet[] getAllPredsAsBitSet() {
		List nodes = getNodes();
		BitSet[] result = new BitSet[nodes.size()];

		for (int i = 0, len = nodes.size(); i < len; i++) {
			INode temp = (INode) nodes.get(i);
			BitSet preds = new BitSet(nodes.size());

			for (Iterator j = temp.getPredsOf().iterator(); j.hasNext();) {
				preds.set(nodes.indexOf(j));
			}
			result[i] = preds;
		}

		return result;
	}

	/**
	 * Retrieves the successor nodes of the given node only as they occur in the spanning tree of the graph.
	 *
	 * @param node of interest.
	 *
	 * @return the successors the given node.
	 *
	 * @pre node != null
	 * @post result != null and result.oclIsKindOf(Collection(INode))
	 * @post result->forall(o | o.getPredsOf()->includes(self))
	 */
	public Collection getForwardSuccsOf(final INode node) {
		if (!hasSpanningForest) {
			createSpanningForest();
		}
		return (Collection) spanningSuccs.get(node);
	}

	/**
	 * Retrieves the head nodes of this graph.
	 *
	 * @return the head nodes(<code>INode</code>) of this graph.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(INode))
	 * @post result->forall(o | o.getPredsOf().size == 0)
	 */
	public Collection getHeads() {
		return Collections.unmodifiableCollection(heads);
	}

	/**
	 * Retrieves the nodes in the graph.  The order of the nodes should be the same across calls to this method, if no nodes
	 * are added or removed.  In case of addition and removal, the relative ordering between the old nodes should not change
	 * across calls.
	 *
	 * @return the nodes(<code>INode</code>) in the graph.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(INode))
	 */
	public abstract List getNodes();

	/**
	 * Retrieves the successor relation in the graph.  Please refer to {@link #getAllPredsAsBitSet() getAllPredAsBitSet} for
	 * details.
	 *
	 * @return the successor relation in the graph.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(Sequence))
	 * @post result->size == self.getNodes().size()
	 * @post result->forall( o | o->size == self.getNodes().size())
	 */
	public BitSet[] getAllSuccsAsBitSet() {
		List nodes = getNodes();
		BitSet[] result = new BitSet[nodes.size()];

		for (int i = 0, len = nodes.size(); i < len; i++) {
			INode temp = (INode) nodes.get(i);
			BitSet succs = new BitSet(nodes.size());

			for (Iterator j = temp.getSuccsOf().iterator(); j.hasNext();) {
				succs.set(nodes.indexOf(j));
			}
			result[i] = succs;
		}

		return result;
	}

	/**
	 * Checks if the given destination node is reachable from the given source node in the given direction.
	 *
	 * @param src is the source node in the graph.
	 * @param dest is the destination node in the graph.
	 * @param forward <code>true</code> indicates by forward traversal; <code>false</code> indicates backward traversal.
	 *
	 * @return <code>true</code> if <code>dest</code> is reachable from <code>src</code> in the given direction;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre src != null and dest != null
	 */
	public boolean isReachable(final INode src, final INode dest, final boolean forward) {
		boolean result = false;
		Collection processed = new HashSet();
		WorkBag worklist = new WorkBag(WorkBag.LIFO);
		worklist.addAllWorkNoDuplicates(src.getSuccsNodesInDirection(forward));

		while (worklist.hasWork()) {
			INode node = (INode) worklist.getWork();

			if (node == dest) {
				result = true;
				break;
			}

			if (!processed.contains(node)) {
				processed.add(node);
				worklist.addAllWorkNoDuplicates(node.getSuccsNodesInDirection(forward));
			}
		}
		return result;
	}

	/**
	 * Retrieves the succession information as it occurs in this graph's spanning tree.  The returned map maps a node to a
	 * collection of nodes which immediately succeed the key node in the  spanning tree of this graph.
	 *
	 * @return an read-only copy of immediate succession information as it occurs in this graph's spanning tree.
	 *
	 * @post result.equals(spanningSuccs)
	 * @post result != null
	 */
	public final Map getSpanningSuccs() {
		if (!hasSpanningForest) {
			createSpanningForest();
		}
		return Collections.unmodifiableMap(spanningSuccs);
	}

	/**
	 * Retrieves the tail nodes of this graph.
	 *
	 * @return the tail nodes(<code>INode</code>) of this graph.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(INode))
	 * @post result->forall(o | o.getSuccsOf()->size() == 0)
	 */
	public Collection getTails() {
		return Collections.unmodifiableCollection(tails);
	}

	/**
	 * Returns the size of this graph.
	 *
	 * @return the number of nodes in this graph.
	 *
	 * @post result == getNodes().size
	 */
	public abstract int size();

	/**
	 * Returns the cycles that occur in the graph.
	 *
	 * @return a collection of list of nodes which form cycles in this graph.  The head of the list is the initiator/head of
	 * 		   the cycle.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Sequence(INode)))
	 */
	public final Collection getCycles() {
		Collection result = new ArrayList();
		WorkBag wb = new WorkBag(WorkBag.LIFO);
		Stack dfsPath = new Stack();

		for (Iterator i = getHeads().iterator(); i.hasNext();) {
			INode head = (INode) i.next();
			wb.clear();
			wb.addWork(head);
			dfsPath.clear();

			while (wb.hasWork()) {
				Object o = wb.getWork();

				if (o instanceof Marker) {
					Object temp = ((Marker) o)._content;
					Object obj = dfsPath.pop();

					while (!temp.equals(obj)) {
						obj = dfsPath.pop();
					}
				} else {
					INode node = (INode) o;

					if (dfsPath.contains(node)) {
						result.add(new ArrayList(dfsPath.subList(dfsPath.indexOf(node), dfsPath.size())));
					} else {
						Collection succs = node.getSuccsOf();

						if (!succs.isEmpty()) {
							dfsPath.push(node);
							wb.addWork(new Marker(node));
							wb.addAllWork(succs);
						}
					}
				}
			}
		}
		return result.size() == 0 ? Collections.EMPTY_LIST
								  : result;
	}

	/**
	 * Returns a collection of strongly-connected components in this graph.
	 *
	 * @param topDown <code>true</code> indicates returned sccs should be in the top-down order; <code>false</code>,
	 * 		  indicates bottom-up.
	 *
	 * @return a collection of <code>List</code> of <code>INode</code>s that form SCCs in this graph. NOTE: It is possible to
	 * 		   reach nodes not in the SCC but in this graph by following edges in reverse direction.
	 *
	 * @post result != null and result.isOclKindOf(Collection(Sequence(INode)))
	 */
	public final Collection getSCCs(final boolean topDown) {
		Collection result;
		List nodes = getNodes();
		Map finishTime2node = new HashMap();
		Collection processed = new HashSet();
		int time = 0;

		for (Iterator i = getHeads().iterator(); i.hasNext();) {
			INode node = (INode) i.next();

			if (!processed.contains(node)) {
				time = getFinishTimes(nodes, node, processed, finishTime2node, time, true);
			}
		}

		List c = new ArrayList();
		c.addAll(finishTime2node.keySet());
		Collections.sort(c);
		Collections.reverse(c);

		Map node2finishTime = new HashMap();

		for (Iterator i = c.iterator(); i.hasNext();) {
			Object element = i.next();
			node2finishTime.put(finishTime2node.get(element), element);
		}
		processed.clear();

		Stack stack = new Stack();
		Map fn2scc = new HashMap();

		for (Iterator i = c.iterator(); i.hasNext() && !processed.containsAll(nodes);) {
			Integer fn = (Integer) i.next();
			INode node = (INode) finishTime2node.get(fn);

			if (processed.contains(node)) {
				continue;
			}
			stack.push(node);

			List scc = new ArrayList();

			while (!stack.isEmpty()) {
				node = (INode) stack.pop();

				if (processed.contains(node)) {
					continue;
				}

				Integer temp = (Integer) node2finishTime.get(node);

				if (temp.intValue() > fn.intValue()) {
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

		if (topDown) {
			Collections.reverse(c);
		}

		result = new ArrayList();

		for (Iterator i = c.iterator(); i.hasNext();) {
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
	public final boolean occursInCycle(final INode node) {
		boolean result = false;
		WorkBag wb = new WorkBag(WorkBag.LIFO);
		wb.addWork(node);

		Collection processed = new HashSet();

		while (wb.hasWork()) {
			INode temp = (INode) wb.getWork();

			if (processed.contains(temp)) {
				result = true;
				break;
			}
			processed.add(temp);
			wb.addAllWorkNoDuplicates(temp.getSuccsOf());
		}
		return result;
	}

	/**
	 * Performs (pseudo-)topological sort of the given nodes in the given direction.
	 *
	 * @param nodes to be sorted.
	 * @param topdown <code>true</code> indicates follow the forward edges while sorting; <code>false</code> indicates follow
	 * 		  the backward edges.
	 *
	 * @return a list containing the nodes but in the sorted order.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(INode))
	 */
	public static final List performTopologicalSort(final List nodes, final boolean topdown) {
		List result;
		Map finishTime2node = new HashMap();
		Collection processed = new ArrayList();
		int time = 0;

		for (Iterator i = nodes.iterator(); i.hasNext();) {
			INode node = (INode) i.next();

			if (processed.contains(node)) {
				continue;
			}
			time = getFinishTimes(nodes, node, processed, finishTime2node, time, topdown);
		}

		List temp = new ArrayList(finishTime2node.keySet());
		Collections.sort(temp);

		result = new ArrayList();

		for (Iterator i = temp.iterator(); i.hasNext();) {
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
	private static int getFinishTimes(final List nodes, final INode node, final Collection processed,
		final Map finishTime2node, final int time, final boolean forward) {
		processed.add(node);

		int temp = time;
		temp++;

		Iterator i = node.getSuccsNodesInDirection(forward).iterator();

		for (; i.hasNext();) {
			INode succ = (INode) i.next();

			if (processed.contains(succ) || !nodes.contains(succ)) {
				continue;
			}
			temp = getFinishTimes(nodes, succ, processed, finishTime2node, time, forward);
		}
		finishTime2node.put(new Integer(++temp), node);
		return time;
	}

	/**
	 * Creates the spanning forest of the graph.
	 *
	 * @post hasSpanningForest = true
	 */
	private final void createSpanningForest() {
		Collection processed = new HashSet();

		if (spanningSuccs == null) {
			spanningSuccs = new HashMap();
		}
		spanningSuccs.clear();

		for (Iterator i = getHeads().iterator(); i.hasNext();) {
			INode node = (INode) i.next();

			if (!processed.contains(node)) {
				Collection temp = new HashSet();
				spanningSuccs.put(node, temp);

				for (Iterator j = node.getSuccsOf().iterator(); j.hasNext();) {
					INode succ = (INode) j.next();

					if (!processed.contains(succ)) {
						temp.add(succ);
					}
				}
			}
			processed.add(node);
		}
		hasSpanningForest = true;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.6  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
