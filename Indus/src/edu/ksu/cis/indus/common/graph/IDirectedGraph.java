/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is an interface to a directed graph in which nodes are represented by <code>INode</code> objects. The nodes in the
 * graph are to be ordered. The concrete implementation can determine the ordering, but it needs to be fixed over the lifetime
 * of the graph.
 * <p>
 * A node <i>a</i> is reachable from node <i>b</i> if there is an explicit edge from <i>a</i> to <i>b</i>. This also holds
 * when <i>a=b</i>.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> is the type of the node in this graph.
 */
public interface IDirectedGraph<N extends INode<N>> {

	/**
	 * Retrieves the back edges in this graph.
	 * 
	 * @return a collection of pairs containing the backedge. The source and the destination nodes of the edge are the first
	 *         and the secondelement of the pair, respectively.
	 * @post getNodes().contains(result.getFirst()) and getNodes().contains(result.getFirst())
	 */
	@NonNull @NonNullContainer Collection<Pair<N, N>> getBackEdges();

	/**
	 * Returns the intersection of the nodes reachable from the given nodes in the given direction. This is equivalent to
	 * <code>CollectionUtils.intersection(getReachablesFrom(node1, forward1), getReachablesFrom(node2, forward2))</code>.
	 * 
	 * @param node1 of interest.
	 * @param forward1 direction of reachability from <code>node1</code>.
	 * @param node2 of interest.
	 * @param forward2 direction of reachability from <code>node2</code>.
	 * @return a collection of nodes.
	 * @post getReachablesFrom(node2, forward2)->forall(o | getReachablesFrom(node1, forward2).contains(o) implies
	 *       result.contains(o))
	 * @post result->forall(o | getReachablesFrom(node2, forward2).contains(o) and getReachableFrom(node1,
	 *       forward1).contains(o))
	 */
	@NonNull @NonNullContainer Collection<N> getCommonReachablesFrom(@NonNull @Immutable N node1, boolean forward1,
			@NonNull @Immutable N node2, boolean forward2);

	/**
	 * Returns the minimum set of nodes that are needed to ensure that the given nodes remain reachable from or can reach
	 * common nodes. The removal of this set of nodes will ensure that there are no common ancestors (descendants) of the
	 * given nodes when forward is true (false).
	 * 
	 * @param node1 is one node of interest.
	 * @param node2 is another node of interest.
	 * @param forward <code>true</code> indicates that common nodes that can reach the given nodes are required;
	 *            <code>false</code> indicates that the common nodes that are reachable from the given nodes are required.
	 * @return a collection of nodes.
	 * @post getNodes().containsAll(result)
	 * @post result->forall(o | isReachable(o, node1, forward) and isReachable(o, node2, forward))
	 */
	@NonNull @NonNullContainer Collection<N> getConnectivityNodesFor(@NonNull @Immutable N node1,
			@NonNull @Immutable N node2, boolean forward);

	/**
	 * Returns the cycles that occur in the graph.
	 * 
	 * @return a collection of list of nodes which form cycles in this graph. The head of the list is the initiator/head of
	 *         the cycle.
	 * @post result->forall(o | getNodes().containsAll(o))
	 */
	@NonNull @NonNullContainer Collection<List<N>> getCycles();

	/**
	 * Returns the directed-acyclic graph of this graph. The objects in the nodes in the returned graph are nodes in this
	 * graph. For each edge in the returned graph, there will be an edges between the nodes corresponding to the source and
	 * destination nodes in this graph.
	 * 
	 * @return a DAG.
	 */
	@NonNull IObjectDirectedGraph<? extends IObjectNode<?, N>, N> getDAG();

	/**
	 * Retrieves the nodes in the graph. The order of the nodes should be the same across calls to this method, if no nodes
	 * are added or removed. In case of addition and removal, the relative ordering between the old nodes should not change
	 * across calls.
	 * 
	 * @return the nodes(<code>INode</code>) in the graph.
	 */
	@NonNull @NonNullContainer List<N> getNodes();

	/**
	 * Retrieves the nodes that occur on the paths between the given nodes.
	 * 
	 * @param nodes of interest.
	 * @return a collection of nodes that occur on path between the given nodes.
	 */
	@NonNull @NonNullContainer Collection<N> getNodesOnPathBetween(@NonNull @Immutable @NonNullContainer Collection<N> nodes);

	/**
	 * Retrieves the nodes reachable from <code>root</code> in the requested direction.
	 * 
	 * @param root is the node from which the reachability needs to be calculated.
	 * @param forward <code>true</code> indicates that nodes reachable by following the edges from <code>root</code> are
	 *            requirested. <code>false</code> indicates that nodes reachables by following the edges in the reverse
	 *            direction from <code>root</code> are required.
	 * @return the collection of reachable nodes.
	 */
	@NonNull @NonNullContainer Collection<N> getReachablesFrom(@NonNull @Immutable N root, boolean forward);

	/**
	 * Returns a sequence of strongly-connected components in this graph.
	 * 
	 * @param topDown <code>true</code> indicates returned sccs should be in the top-down order; <code>false</code>,
	 *            indicates bottom-up.
	 * @return a sequence of <code>List</code> of <code>INode</code>s that form SCCs in this graph.
	 * @post result->forall(o | getNodes().containsAll(o))
	 */
	@NonNull @NonNullContainer List<List<N>> getSCCs(boolean topDown);

	/**
	 * Retrieves the sink nodes of this graph.
	 * 
	 * @return the sink nodes(<code>INode</code>) of this graph.
	 * @post result->forall(o | o.getSuccsOf()->size() == 0) and getNodes().containsAll(result)
	 */
	@NonNull @NonNullContainer Collection<N> getSinks();

	/**
	 * Retrieves the source nodes of this graph.
	 * 
	 * @return the source nodes(<code>INode</code>) of this graph.
	 * @post result->forall(o | o.getPredsOf()->size() == 0) and getNodes().containsAll(result)
	 */
	@NonNull @NonNullContainer Collection<N> getSources();

	/**
	 * Retrieves the succession information as it occurs in this graph's spanning tree. The returned map maps a node to a
	 * collection of nodes which immediately succeed the key node in the spanning tree of this graph.
	 * 
	 * @return an read-only copy of immediate succession information as it occurs in this graph's spanning tree.
	 */
	@NonNull @NonNullContainer Map<N, Set<N>> getSpanningSuccs();

	/**
	 * Retrieves the tails of the given graph. A Tail is a sink node or an arbitrary node in a strongly-connected component
	 * that acts as a sink (only the nodes of the SCC are reachable from the nodes in the SCC).
	 * 
	 * @return a collection of nodes.
	 * @post result->forall(o | getNodes().contains(o))
	 * @post result.containsAll(getSinks()) and getNodes().containsAll(result)
	 */
	@NonNull @NonNullContainer Collection<N> getTails();

	/**
	 * Checks if there is any node that can be reached from the give nodes in the given direction.
	 * 
	 * @param node1 is a node of interest.
	 * @param forward1 <code>true</code> indicates forward direction; <code>false</code> indicates backward direction.
	 * @param node2 is another node of interest.
	 * @param forward2 <code>true</code> indicates forward direction; <code>false</code> indicates backward direction.
	 * @return <code>true</code> if there is common node that is reachable from <code>node1</code> in
	 *         <code>forward1</code> direction and is reachable from <code>node2</code> in <code>forward2</code>
	 *         direction.
	 */
	boolean hasCommonReachablesFrom(@NonNull @Immutable N node1, boolean forward1, @NonNull @Immutable N node2,
			boolean forward2);

	/**
	 * Checks if the given nodes have ancestral relationship. A node is considered as an ancestor of itself.
	 * 
	 * @param ancestor in the relationship.
	 * @param descendent in the relationship.
	 * @return <code>true</code> if <code>ancestor</code> is the ancestor of <code>descendent</code>;
	 *         <code>false</code>, otherwise.
	 * @pre getNodes().contains(ancestor) and getNodes().contains(descendent)
	 */
	boolean isAncestorOf(@NonNull @Immutable N ancestor, @NonNull @Immutable N descendent);

	/**
	 * Checks if the given destination node is reachable from the given source node in the given direction via outgoing edges.
	 * 
	 * @param src is the source node in the graph.
	 * @param dest is the destination node in the graph.
	 * @param forward <code>true</code> indicates by forward traversal; <code>false</code> indicates backward traversal.
	 * @return <code>true</code> if <code>dest</code> is reachable from <code>src</code> in the given direction;
	 *         <code>false</code>, otherwise.
	 * @pre getNodes().contains(src) and getNodes().contains(dest)
	 */
	boolean isReachable(@NonNull @Immutable N src, @NonNull @Immutable N dest, boolean forward);

	/**
	 * Performs (pseudo-)topological sort of the given nodes in the given direction.
	 * 
	 * @param topdown <code>true</code> indicates follow the forward edges while sorting; <code>false</code> indicates
	 *            follow the backward edges.
	 * @return a list containing the nodes but in the sorted order.
	 * @post getNodes().containsAll(result)
	 */
	@NonNull @NonNullContainer List<N> performTopologicalSort(boolean topdown);
}

// End of File
