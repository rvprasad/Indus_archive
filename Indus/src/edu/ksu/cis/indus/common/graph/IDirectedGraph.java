
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
	 * @post hasSpanningForest = true
	 */
	boolean isAncestorOf(final INode ancestor, final INode descendent);

	/**
	 * Retrieves the back edges in this graph.
	 *
	 * @return a collection of pairs containing the backedge. The source and the destination nodes of the edge are the first
	 * 		   and the secondelement of the pair, respectively.
	 *
	 * @post result != null and hasSpanningForest = true and result.oclIsKindOf(Pair(INode, INode))
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
	 * Retrieves the directed acyclic graph from the given graph.  It removes all the backedges from the given graph.
	 *
	 * @return a map from nodes in <code>graph</code> to a collection of pairs in <code>graph</code>.
	 *
	 * @pre graph != null
	 * @post result.oclIsKindOf(Map(INode, Pair(Collection(INode), Collection(INode)))
	 * @post result->entrySet()->forall(o | graph.getNodes()->includes(o.getKey()) and
	 * 		 graph.getNodes()->includes(o.getValue().getFirst()) and graph.getNodes()->includes(o.getValue().getSecond()))
	 * @post graph.getNodes()->forall(o | result.keySet()->includes(o))
	 */
	Map getDAG();

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
	 * Returns a collection of strongly-connected components in this graph.
	 *
	 * @param topDown <code>true</code> indicates returned sccs should be in the top-down order; <code>false</code>,
	 * 		  indicates bottom-up.
	 *
	 * @return a collection of <code>List</code> of <code>INode</code>s that form SCCs in this graph.
	 *
	 * @post result != null and result.isOclKindOf(Collection(Sequence(INode)))
	 * @post result->forall(o | getNodes().containsAll(o))
	 */
	Collection getSCCs(final boolean topDown);

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

	/**
	 * Returns the size of this graph.
	 *
	 * @return the number of nodes in this graph.
	 *
	 * @post result == getNodes().size
	 */
	int size();
}

/*
   ChangeLog:
   $Log$
 */
