
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.common.graph;

import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * This is an interface to a directed graph in which nodes are represented by <code>INode</code> objects.   The nodes in the
 * graph are to be ordered.  The concrete implementation can determine the ordering, but it needs to be fixed over the
 * lifetime of the graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IDirectedGraph {
	/**
	 * Checks if the given nodes have ancestral relationship.  A node is considered as the ancestor of itself.
	 *
	 * @param ancestor in the relationship.
	 * @param descendent in the relationship.
	 *
	 * @return <code>true</code> if <code>ancestor</code> is the ancestor of <code>descendent</code>; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre ancestor != null and descendent != null and getNodes().contains(ancestor) and getNodes().contains(descendent)
	 */
	boolean isAncestorOf(final INode ancestor, final INode descendent);

	/**
	 * Retrieves the back edges in this graph.
	 *
	 * @return a collection of pairs containing the backedge. The source and the destination nodes of the edge are the first
	 * 		   and the secondelement of the pair, respectively.
	 *
	 * @post result != null and result.oclIsKindOf(Pair(INode, INode))
	 * @post getNodes().contains(result.getFirst()) and getNodes().contains(result.getFirst())
	 */
	Collection getBackEdges();

	/**
	 * Returns the cycles that occur in the graph.
	 *
	 * @return a collection of list of nodes which form cycles in this graph.  The head of the list is the initiator/head of
	 * 		   the cycle.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Sequence(INode)))
	 * @post result->forall(o | getNodes().containsAll(o))
	 */
	Collection getCycles();

	/**
	 * Returns the directed-acyclic graph of this graph.  The objects in the nodes in the returned graph are nodes in this
	 * graph.  For each edge in the returned graph, there will be an edges between the nodes corresponding to the source and
	 * destination nodes in this graph.
	 *
	 * @return a DAG.
	 *
	 * @post result != null
	 */
	IObjectDirectedGraph getDAG();

	/**
	 * Retrieves the head nodes of this graph.
	 *
	 * @return the head nodes(<code>INode</code>) of this graph.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(INode))
	 * @post result->forall(o | o.getPredsOf().size == 0) and getNodes().containsAll(result)
	 */
	Collection getHeads();

	/**
	 * Retrieves the nodes in the graph.  The order of the nodes should be the same across calls to this method, if no nodes
	 * are added or removed.  In case of addition and removal, the relative ordering between the old nodes should not change
	 * across calls.
	 *
	 * @return the nodes(<code>INode</code>) in the graph.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(INode))
	 */
	List getNodes();

	/**
	 * Retrieves the nodes that occur on the path between the given nodes.
	 *
	 * @param nodes of interest.
	 *
	 * @return a collection of nodes that occur on path between the given nodes.
	 *
	 * @pre nodes != null and nodes.oclIsKindOf(Collection(INode))
	 */
	Collection getNodesInPathBetween(Collection nodes);

	/**
	 * Retrieves the psuedo-tails of the given graph.  Psuedo tails are tail end of loops in which there is no path from the
	 * head to a proper tail.
	 *
	 * @return a collection of nodes which comprise the psuedo tails in the graph.
	 *
	 * @post result != null
	 * @post result->forall(o | getNodes().contains(o))
	 */
	Collection getPseudoTails();

	/**
	 * Checks if the given destination node is reachable from the given source node in the given direction via outgoing
	 * edges.
	 *
	 * @param src is the source node in the graph.
	 * @param dest is the destination node in the graph.
	 * @param forward <code>true</code> indicates by forward traversal; <code>false</code> indicates backward traversal.
	 *
	 * @return <code>true</code> if <code>dest</code> is reachable from <code>src</code> in the given direction;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre src != null and dest != null and getNodes().contains(src) and getNodes().contains(dest)
	 */
	boolean isReachable(final INode src, final INode dest, final boolean forward);

	/**
	 * Retrieves the nodes reachable from <code>root</code> in the requested direction.
	 *
	 * @param root is the node from which the reachability needs to be calculated.
	 * @param forward <code>true</code> indicates that nodes reachable by following the edges from <code>root</code> are
	 * 		  requirested.  <code>false</code> indicates that nodes reachables by following the edges in the reverse
	 * 		  direction from <code>root</code> are required.
	 *
	 * @return the collection of reachable nodes.
	 *
	 * @pre root != null
	 * @post result != null and result.oclIsKindOf(Collection(INode))
	 */
	Collection getReachablesFrom(final INode root, final boolean forward);

	/**
	 * Returns a sequence of strongly-connected components in this graph.
	 *
	 * @param topDown <code>true</code> indicates returned sccs should be in the top-down order; <code>false</code>,
	 * 		  indicates bottom-up.
	 *
	 * @return a sequence of <code>List</code> of <code>INode</code>s that form SCCs in this graph.
	 *
	 * @post result != null and result.isOclKindOf(Sequence(Sequence(INode)))
	 * @post result->forall(o | getNodes().containsAll(o))
	 */
	List getSCCs(final boolean topDown);

	/**
	 * Retrieves the succession information as it occurs in this graph's spanning tree.  The returned map maps a node to a
	 * collection of nodes which immediately succeed the key node in the  spanning tree of this graph.
	 *
	 * @return an read-only copy of immediate succession information as it occurs in this graph's spanning tree.
	 *
	 * @post result.oclIsKindOf(Map(INode, Collection(INode)))
	 * @post result != null
	 */
	Map getSpanningSuccs();

	/**
	 * Retrieves the tail nodes of this graph.
	 *
	 * @return the tail nodes(<code>INode</code>) of this graph.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(INode))
	 * @post result->forall(o | o.getSuccsOf()->size() == 0) and getNodes().containsAll(result)
	 */
	Collection getTails();

	/**
	 * Performs (pseudo-)topological sort of the given nodes in the given direction.
	 *
	 * @param topdown <code>true</code> indicates follow the forward edges while sorting; <code>false</code> indicates follow
	 * 		  the backward edges.
	 *
	 * @return a list containing the nodes but in the sorted order.
	 *
	 * @post result != null and result.oclIsKindOf(Sequence(INode)) and getNodes().containsAll(result)
	 */
	List performTopologicalSort(final boolean topdown);
}

// End of File
