
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

import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Marker;
import edu.ksu.cis.indus.common.datastructures.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.CollectionUtils;


/**
 * This class provides abstract implementation of <code>IDirectedGraph</code> in which nodes are represented by
 * <code>INode</code> objects.   The subclasses are responsible for maintaining the collection of nodes that make up this
 * graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractDirectedGraph
  implements IDirectedGraph {
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
	 * This is the collection of back edges in this graph.
	 */
	private Collection backedges = new ArrayList();

	/**
	 * The collection of pseudo tails in the given graph.  Refer to <code>getPseudoTails()</code> for details.
	 */
	private final Collection pseudoTails = new HashSet();

	/**
	 * This maps a node to it's spanning successor nodes.
	 *
	 * @invariant spanningSuccs.keySet()->forall( o | o.oclIsKindOf(INode))
	 * @invariant spanningSuccs.values()->forall( o | o.oclIsKindOf(Set) and o->forall( p | p.oclIsKindOf(INode)))
	 * @invariant getNodes().containsAll(spanningSuccs.keySet())
	 * @invariant spanningSuccs.values()->forall( o | getNodes().containsAll(o))
	 */
	private Map spanningSuccs;

	/**
	 * This captures backward reachability information.
	 */
	private boolean[][] backwardReachabilityMatrix;

	/**
	 * This captures backward reachability information.
	 */
	private boolean[][] forwardReachabilityMatrix;

	/**
	 * This is the node indexed prenum of the nodes in this graph.
	 *
	 * @invariant postnums.size = getNodes().size()
	 * @invariant postnums->forall(o | o > 0)
	 */
	private int[] postnums;

	/**
	 * This is the node indexed prenum of the nodes in this graph.
	 *
	 * @invariant prenums.size = getNodes().size()
	 * @invariant prenums->forall(o | o > 0)
	 */
	private int[] prenums;

	/**
	 * This indicates if pseudo tails have been calculated for this graph.
	 */
	private boolean pseudoTailsCalculated;

	/**
	 * This indicates if reachability information has been calculated for this graph.
	 */
	private boolean reachability;

	/**
	 * @see IDirectedGraph#getBackEdges()
	 */
	public final Collection getBackEdges() {
		if (!hasSpanningForest) {
			createSpanningForest();
		}

		final List _nodes = getNodes();
		final Collection _temp = new ArrayList();

		for (final Iterator _i = backedges.iterator(); _i.hasNext();) {
			final Pair _edge = (Pair) _i.next();
			final int _descendent = _nodes.indexOf(_edge.getFirst());
			final int _ancestor = _nodes.indexOf(_edge.getSecond());

			if (prenums[_ancestor] > prenums[_descendent] || postnums[_ancestor] < postnums[_descendent]) {
				_temp.add(_edge);
			}
		}
		backedges.removeAll(_temp);

		final Collection _result;

		if (backedges.isEmpty()) {
			_result = Collections.EMPTY_LIST;
		} else {
			_result = Collections.unmodifiableCollection(backedges);
		}
		return _result;
	}

	/**
	 * @see IDirectedGraph#getCycles()
	 */
	public final Collection getCycles() {
		Collection _result = new ArrayList();

		for (final Iterator _i = getSCCs(true).iterator(); _i.hasNext();) {
			final Collection _scc = (Collection) _i.next();

			final Collection _temp = findCycles(_scc);

			if (!_temp.isEmpty()) {
				_result.addAll(_temp);
			}
		}

		if (_result.isEmpty()) {
			_result = Collections.EMPTY_LIST;
		}

		return _result;
	}

	/**
	 * @see IDirectedGraph#getDAG()
	 */
	public final Map getDAG() {
		final Map _result = new HashMap();
		final Map _srcdestBackEdges = Pair.mapify(getBackEdges(), true);
		final Map _destsrcBackEdges = Pair.mapify(getBackEdges(), false);

		final Collection _succs = new HashSet();
		final Collection _preds = new HashSet();

		for (final Iterator _i = getNodes().iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			final Collection _backSuccessors = (Collection) _srcdestBackEdges.get(_node);
			_succs.clear();
			_succs.addAll(_node.getSuccsOf());

			if (_backSuccessors != null) {
				_succs.removeAll(_backSuccessors);
			}

			final Collection _backPredecessors = (Collection) _destsrcBackEdges.get(_node);
			_preds.clear();
			_preds.addAll(_node.getPredsOf());

			if (_backPredecessors != null) {
				_preds.removeAll(_backPredecessors);
			}

			Collection _s;
			Collection _p;

			if (_succs.isEmpty()) {
				_s = Collections.EMPTY_LIST;
			} else {
				_s = new ArrayList(_succs);
			}

			if (_preds.isEmpty()) {
				_p = Collections.EMPTY_LIST;
			} else {
				_p = new ArrayList(_preds);
			}

			_result.put(_node, new Pair(_p, _s));
		}
		return _result;
	}

	/**
	 * @see IDirectedGraph#getHeads()
	 */
	public final Collection getHeads() {
		return Collections.unmodifiableCollection(heads);
	}

	/**
	 * @see IDirectedGraph#getPseudoTails()
	 */
	public final Collection getPseudoTails() {
		if (!pseudoTailsCalculated) {
			// get the tails of the DAG into dtails
			final Map _dag = getDAG();
			final Collection _dtails = new HashSet();

			for (final Iterator _i = _dag.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final Pair _pair = (Pair) _entry.getValue();

				if (((Collection) _pair.getSecond()).isEmpty()) {
					_dtails.add(_entry.getKey());
				}
			}

			// get the tails of the graph into _tails
			final Collection _tails = getTails();

			// for each dtail that is not a tail, check if tail is reachable from it.  
			// If so, dtail is not a pseudo tail.  If not, it is a pseudo tail.
			_dtails.removeAll(_tails);

			final Collection _temp = getDestUnreachableSources(_dtails, _tails, true, false);
			final Collection _result = getDestUnreachableSources(_temp, _temp, true, false);

			/*
			 * It is possible that a graph have 2 pseudo tails which are mutually reachable in the graph. In that case,
			 * _result is empty.  This is the case when cycles in graph have common nodes. In such case, both should qualify
			 * as pseudo tails.  In other cases, only those in _result should qualify as pseudo tails.
			 */
			if (_result.isEmpty()) {
				pseudoTails.addAll(_temp);
			} else {
				pseudoTails.addAll(_result);
			}
			pseudoTailsCalculated = true;
		}
		return Collections.unmodifiableCollection(pseudoTails);
	}

	/**
	 * @see IDirectedGraph#getNodes()
	 */
	public abstract List getNodes();

	/**
	 * @see IDirectedGraph#isAncestorOf(INode,INode)
	 */
	public final boolean isAncestorOf(final INode ancestor, final INode descendent) {
		if (!hasSpanningForest) {
			createSpanningForest();
		}

		final int _anc = getNodes().indexOf(ancestor);
		final int _desc = getNodes().indexOf(descendent);
		return prenums[_anc] <= prenums[_desc] && postnums[_anc] >= postnums[_desc];
	}

	/**
	 * @see IDirectedGraph#isReachable(INode,INode,boolean)
	 */
	public final boolean isReachable(final INode src, final INode dest, final boolean forward) {
		if (!reachability) {
			calculateReachabilityInfo();
		}

		final boolean[][] _matrix;
		final List _nodes = getNodes();

		if (forward) {
			_matrix = forwardReachabilityMatrix;
		} else {
			_matrix = backwardReachabilityMatrix;
		}
		return _matrix[_nodes.indexOf(src)][_nodes.indexOf(dest)];
	}

	/**
	 * @see IDirectedGraph#getReachablesFrom(INode, boolean)
	 */
	public final Collection getReachablesFrom(final INode root, final boolean forward) {
		if (!reachability) {
			calculateReachabilityInfo();
		}

		final boolean[] _matrix;
		final List _nodes = getNodes();

		if (forward) {
			_matrix = forwardReachabilityMatrix[_nodes.indexOf(root)];
		} else {
			_matrix = backwardReachabilityMatrix[_nodes.indexOf(root)];
		}

		final Collection _result = new ArrayList();

		for (int _i = _nodes.size() - 1; _i >= 0; _i--) {
			if (_matrix[_i]) {
				_result.add(_nodes.get(_i));
			}
		}
		return _result;
	}

	/**
	 * @see IDirectedGraph#getSCCs(boolean)
	 */
	public final List getSCCs(final boolean topDown) {
		final List _result;
		final List _nodes = getNodes();
		final Map _finishTime2node = new HashMap();
		final Collection _processed = new HashSet();
		int _time = 0;

		for (final Iterator _i = _nodes.iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();

			if (!_processed.contains(_node)) {
				_time = getFinishTimes(_nodes, _node, _processed, _finishTime2node, _time, true);
			}
		}

		final Map _fn2scc = constructSCCs(_finishTime2node);
		final List _finishTimes = new ArrayList();
		_finishTimes.addAll(_fn2scc.keySet());
		Collections.sort(_finishTimes);

		if (topDown) {
			Collections.reverse(_finishTimes);
		}

		_result = new ArrayList();

		for (final Iterator _i = _finishTimes.iterator(); _i.hasNext();) {
			_result.add(_fn2scc.get(_i.next()));
		}
		return _result;
	}

	/**
	 * @see IDirectedGraph#getSpanningSuccs()
	 */
	public final Map getSpanningSuccs() {
		if (!hasSpanningForest) {
			createSpanningForest();
		}
		return Collections.unmodifiableMap(spanningSuccs);
	}

	/**
	 * @see IDirectedGraph#getTails()
	 */
	public final Collection getTails() {
		return Collections.unmodifiableCollection(tails);
	}

	/**
	 * @see IDirectedGraph#performTopologicalSort(boolean)
	 */
	public final List performTopologicalSort(final boolean topdown) {
		List _result;
		final List _nodes = getNodes();
		final Map _finishTime2node = new HashMap();
		final Collection _processed = new HashSet();
		int _time = 0;

		for (final Iterator _i = _nodes.iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();

			if (!_processed.contains(_node)) {
				_time = getFinishTimes(_nodes, _node, _processed, _finishTime2node, _time, topdown);
			}
		}

		final List _temp = new ArrayList(_finishTime2node.keySet());
		Collections.sort(_temp);
		_result = new ArrayList();

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			_result.add(0, _finishTime2node.get(_i.next()));
		}
		return _result;
	}

	/**
	 * Finds cycles in the given SCC.
	 *
	 * @param scc in which to search for cycles.
	 *
	 * @return a collection of cycles. Each cycle is represented as a sequence in which the first element starts the cycle.
	 *
	 * @pre scc != null and scc.oclIsKindOf(Collection(INode))
	 * @post result != null and result.oclIsKindOf(Collection(Sequence(INode)))
	 */
	public static Collection findCycles(final Collection scc) {
		final Collection _result = new ArrayList();

		if (scc.size() == 1) {
			final INode _node = (INode) scc.iterator().next();

			if (_node.getSuccsOf().contains(_node)) {
				_result.add(Collections.singleton(_node));
			}
		} else {
			final IWorkBag _wb = new LIFOWorkBag();
			final Stack _dfsPath = new Stack();
			_wb.addWork(scc.iterator().next());

			while (_wb.hasWork()) {
				final Object _o = _wb.getWork();

				if (_o instanceof Marker) {
					final Object _temp = ((Marker) _o).getContent();

					while (!_temp.equals(_dfsPath.peek())) {
						_dfsPath.pop();
					}
				} else if (_dfsPath.contains(_o)) {
					final List _cycle = _dfsPath.subList(_dfsPath.indexOf(_o), _dfsPath.size());

					if (!_result.contains(_cycle)) {
						_result.add(new ArrayList(_cycle));
					}
				} else {
					final INode _node = (INode) _o;
					final Collection _succs = CollectionUtils.intersection(_node.getSuccsOf(), scc);

					if (!_succs.isEmpty()) {
						_dfsPath.push(_node);

						if (_succs.size() > 1) {
							final Marker _marker = new Marker(_node);

							for (final Iterator _j = _succs.iterator(); _j.hasNext();) {
								final Object _ele = _j.next();
								_wb.addWork(_marker);
								_wb.addWork(_ele);
							}
						} else {
							_wb.addWork(_succs.iterator().next());
						}
					}
				}
			}
		}
		return _result;
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		final StringBuffer _sb = new StringBuffer();
		final List _nodes = getNodes();

		for (final Iterator _i = _nodes.iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();
			final int _nodePos = _nodes.indexOf(_node);

			for (final Iterator _j = _node.getSuccsOf().iterator(); _j.hasNext();) {
				final Object _succ = _j.next();
				_sb.append(_nodePos).append(" -> ").append(_nodes.indexOf(_succ)).append("\n");
			}
		}
		return _sb.toString();
	}

	/**
	 * Changes the state of the graph as it's shape changed.
	 */
	protected void shapeChanged() {
		hasSpanningForest = false;
		pseudoTailsCalculated = false;
		reachability = false;
	}

	/**
	 * Retrieves the source nodes which cannot reach any of the given destinations.  If there are no destination nodes then
	 * all source nodes are returned.
	 *
	 * @param sources is the collection of source nodes.
	 * @param destinations is the collection of destination nodes.
	 * @param forward <code>true</code> indicates following outgoing edges; <code>false</code> indicates following incoming
	 * 		  edges.
	 * @param considerSelfReachability <code>true</code> indicates that the reachability of a source from the source should
	 * 		  be considered; <code>false</code>, otherwise.  In other words, if a source node is also a destination node
	 * 		  then  the source node will not occur in the result if this parameter is <code>true</code>.  It will occur in
	 * 		  the result when the parameter is <code>false</code>.
	 *
	 * @return a collection of source nodes.
	 *
	 * @pre sources != null and destinations != null
	 * @post result != null and result.oclIsKindOf(Collection(INode))
	 * @post sources->includesAll(result)
	 * @post considerSelfReachability implies not result->exists(o | sources.contains(o))
	 * @post (not considerSelfReachability) implies sources->forall(o | destinations.contains(o) implies result.contains(o))
	 */
	private Collection getDestUnreachableSources(final Collection sources, final Collection destinations,
		final boolean forward, final boolean considerSelfReachability) {
		final Collection _result = new HashSet(sources);
		final Collection _temp = new ArrayList(destinations);

		if (!destinations.isEmpty()) {
			final Iterator _i = sources.iterator();
			final int _iEnd = sources.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final INode _src = (INode) _i.next();
				final Collection _reachables = getReachablesFrom(_src, forward);
				final boolean _flag = destinations.contains(_src);

				if (_flag && !considerSelfReachability) {
					_temp.remove(_src);
				}

				if (CollectionUtils.containsAny(_reachables, _temp)) {
					_result.remove(_src);
				}

				if (_flag && !considerSelfReachability) {
					_temp.add(_src);
				}
			}
		}
		return _result;
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
	 *
	 * @pre getNodes().contains(node)
	 */
	private int getFinishTimes(final List nodes, final INode node, final Collection processed, final Map finishTime2node,
		final int time, final boolean forward) {
		processed.add(node);

		int _temp = time;
		_temp++;

		for (final Iterator _i = node.getSuccsNodesInDirection(forward).iterator(); _i.hasNext();) {
			final INode _succ = (INode) _i.next();

			if (processed.contains(_succ) || !nodes.contains(_succ)) {
				continue;
			}
			_temp = getFinishTimes(nodes, _succ, processed, finishTime2node, _temp, forward);
		}
		finishTime2node.put(new Integer(++_temp), node);
		return _temp;
	}

	/**
	 * Calculates reachability information for this graph.
	 */
	private void calculateReachabilityInfo() {
		if (!reachability) {
			final List _nodes = getNodes();
			final int _noOfNodes = _nodes.size();
			forwardReachabilityMatrix = new boolean[_noOfNodes][_noOfNodes];
			backwardReachabilityMatrix = new boolean[_noOfNodes][_noOfNodes];

			for (int _iIndex = 0; _iIndex < _noOfNodes; _iIndex++) {
				final INode _node = (INode) _nodes.get(_iIndex);
				final Iterator _j = _node.getSuccsOf().iterator();
				final int _jEnd = _node.getSuccsOf().size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final INode _succ = (INode) _j.next();
					forwardReachabilityMatrix[_iIndex][_nodes.indexOf(_succ)] = true;
					backwardReachabilityMatrix[_nodes.indexOf(_succ)][_iIndex] = true;
				}
			}

			for (int _j = 0; _j < _noOfNodes; _j++) {
				for (int _k = 0; _k < _noOfNodes; _k++) {
					for (int _l = 0; _l < _noOfNodes; _l++) {
						if (forwardReachabilityMatrix[_k][_j] && forwardReachabilityMatrix[_j][_l]) {
							forwardReachabilityMatrix[_k][_l] = true;
							backwardReachabilityMatrix[_l][_k] = true;
						}
					}
				}
			}
			reachability = true;
		}
	}

	/**
	 * Constructs the SCCS from the given information.
	 *
	 * @param finishTime2Node maps the finish time to nodes in the graph.
	 *
	 * @return a map from finish time to SCC.
	 *
	 * @pre finishTime2Node != null
	 * @pre finishTime2Node.oclIsKindOf(Map(Integer, INode))
	 * @post result != null and result.oclIsKindOf(Map(Integer, Sequence(INode)))
	 */
	private Map constructSCCs(final Map finishTime2Node) {
		final Stack _stack = new Stack();
		final Map _fn2scc = new HashMap();
		final Collection _processed = new HashSet();
		final List _finishTimes = new ArrayList();
		final Collection _nodesInGraph = getNodes();
		_finishTimes.addAll(finishTime2Node.keySet());
		Collections.sort(_finishTimes);
		Collections.reverse(_finishTimes);

		final Map _node2finishTime = new HashMap();

		for (final Iterator _i = _finishTimes.iterator(); _i.hasNext();) {
			final Object _element = _i.next();
			_node2finishTime.put(finishTime2Node.get(_element), _element);
		}

		for (final Iterator _i = _finishTimes.iterator(); _i.hasNext() && !_processed.containsAll(_nodesInGraph);) {
			final Integer _fn = (Integer) _i.next();
			INode _node = (INode) finishTime2Node.get(_fn);

			if (_processed.contains(_node)) {
				continue;
			}
			_stack.push(_node);

			final List _scc = new ArrayList();

			while (!_stack.isEmpty()) {
				_node = (INode) _stack.pop();

				if (_processed.contains(_node)) {
					continue;
				}
				_scc.add(_node);
				_processed.add(_node);
				_stack.addAll(_node.getPredsOf());
			}
			_fn2scc.put(_fn, _scc);
		}
		return _fn2scc;
	}

	/**
	 * Creates the spanning forest of the graph.
	 *
	 * @post hasSpanningForest = true
	 */
	private void createSpanningForest() {
		final Collection _reached = new HashSet();

		if (spanningSuccs == null) {
			spanningSuccs = new HashMap();
		} else {
			spanningSuccs.clear();
		}

		final IWorkBag _order = new LIFOWorkBag();
		final List _nodes = getNodes();

		// It is possible that the graph has no heads, i.e., nodes with no predecessors, and these are handled here. 
		if (getHeads().isEmpty()) {
			_order.addAllWork(_nodes);
		} else {
			_order.addAllWork(getHeads());
		}

		final Collection _processed = new ArrayList();
		int _prenum = 0;
		int _postnum = 0;
		prenums = new int[_nodes.size()];
		postnums = new int[_nodes.size()];
		backedges.clear();

		while (_order.hasWork()) {
			final Object _work = _order.getWork();

			if (_work instanceof Marker) {
				final INode _node = (INode) ((Marker) _work).getContent();
				postnums[_nodes.indexOf(_node)] = ++_postnum;
			} else if (!_processed.contains(_work)) {
				// we do not want to process nodes that are already processed.
				final INode _node = (INode) _work;
				prenums[_nodes.indexOf(_node)] = ++_prenum;
				_processed.add(_node);

				_postnum = processNodeForSpanningTree(_reached, _order, _processed, _postnum, _node);
			}
		}

		hasSpanningForest = true;
	}

	/**
	 * Processes the given node while creating a spanning tree.
	 *
	 * @param reachedNodes is the collection of nodes already visited or reached.
	 * @param workBag is the work bag that needs to be updates during processing.
	 * @param processedNodes is the collection of nodes already processed while creating the spanning tree.
	 * @param currPostNumber is the post number is the seeding post number for processing.
	 * @param nodeToProcess is the node to be processed.
	 *
	 * @return the post number of the last node visited during processing.
	 *
	 * @pre reachedNodes != null and workBag != null and processedNodes != null and currPostNumber != null and nodeToProcess
	 * 		!= null
	 * @post processedNodes.containsAll(processedNodes$pre)
	 * @post reachedNodes.containsAll(reachedNodes$pre)
	 * @post processedNodes.containsAll(processedNodes$pre)
	 */
	private int processNodeForSpanningTree(final Collection reachedNodes, final IWorkBag workBag,
		final Collection processedNodes, final int currPostNumber, final INode nodeToProcess) {
		final Collection _temp = new HashSet();
		int _postnum = currPostNumber;
		boolean _flag = true;

		spanningSuccs.put(nodeToProcess, _temp);
		workBag.addWork(new Marker(nodeToProcess));

		for (final Iterator _j = nodeToProcess.getSuccsOf().iterator(); _j.hasNext();) {
			final INode _succ = (INode) _j.next();

			// record only those successors which have not been visited via other nodes. 
			if (!reachedNodes.contains(_succ) && !processedNodes.contains(_succ)) {
				_temp.add(_succ);
				reachedNodes.add(_succ);
				workBag.addWork(_succ);
				_flag = false;
			} else {
				backedges.add(new Pair(nodeToProcess, _succ, false));
			}
		}

		if (_flag) {
			postnums[getNodes().indexOf(nodeToProcess)] = ++_postnum;
			workBag.getWork();
		}
		return _postnum;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.19  2004/07/07 10:07:36  venku
   - added new method to calculate reachables. With test of course.
   Revision 1.18  2004/07/04 04:56:27  venku
   - made findCycles() public and static.
   Revision 1.17  2004/07/03 07:56:56  venku
   - improved the algorithm to calculate cycles.
   Revision 1.16  2004/06/04 04:49:50  venku
   - added toString() method.
   Revision 1.15  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.14  2004/02/24 22:25:56  venku
   - documentation
   Revision 1.13  2004/02/05 18:17:29  venku
   - getPseudoTails() is incorrect when the pseudo tails are mutually
     reachable.  FIXED.
   Revision 1.12  2004/01/28 00:23:21  venku
   - dtails are only those that aren't tails.  FIXED.
   Revision 1.11  2004/01/25 08:58:43  venku
   - coding convention.
   Revision 1.10  2004/01/22 12:23:30  venku
   - subtle boundary condition bug in getPseudoTails. FIXED.
   Revision 1.9  2004/01/22 08:15:55  venku
   - added a new method to calculated pseudo tails.
   - added a new method that can be used to indicate the
     graph has changed shape, hence, marking any cached
     data as stale.
   Revision 1.8  2004/01/22 00:53:32  venku
   - formatting and coding convention.
   Revision 1.7  2004/01/20 21:23:18  venku
   - the return value of getSCCs needs to be ordered if
     it accepts a direction parameter.  FIXED.
   Revision 1.6  2004/01/06 00:53:35  venku
   - coding conventions.
   Revision 1.5  2004/01/06 00:17:10  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.4  2003/12/31 10:43:08  venku
   - size() was unused in IDirectedGraph, hence, removed it.
     Ripple effect.
   Revision 1.3  2003/12/31 08:47:01  venku
   - getCycles() was broken. FIXED.
   Revision 1.2  2003/12/30 09:11:28  venku
   - formatting
   - concretized size() based on getNodes().
   Revision 1.1  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.14  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.13  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.12  2003/11/05 09:27:48  venku
   - ripple effect of splitting IWorkBag.
   Revision 1.11  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.10  2003/09/14 23:20:48  venku
   - added support to retrieve a DAG from a graph.
   - removed support to extract preds/succs as a bitst from the graph.
   - added/removed tests for the above changes.
   Revision 1.9  2003/09/11 12:31:00  venku
   - made ancestral relationship antisymmetric
   - added testcases to test the relationship.
   Revision 1.8  2003/09/11 12:18:35  venku
   - added support to retrieve basic blocks in which
     exception handlers begin.
   - added support to detect ancestral relationship between nodes.
   Revision 1.7  2003/09/11 01:52:07  venku
   - prenum, postnum, and back edges support has been added.
   - added test case to test the above addition.
   - corrected subtle bugs in test1
   - refactored test1 so that setup local testing can be added by subclasses.
   Revision 1.6  2003/09/10 07:40:34  venku
   - documentation change.
   Revision 1.5  2003/09/01 20:57:11  venku
   - Deleted getForwardSuccsOf().
   Revision 1.4  2003/08/24 12:04:32  venku
   Removed occursInCycle() method from DirectedGraph.
   Installed occursInCycle() method in CFGAnalysis.
   Converted performTopologicalsort() and getFinishTimes() into instance methods.
   Ripple effect of the above changes.
   Revision 1.3  2003/08/11 07:13:58  venku
   empty log message
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
   Revision 1.6  2003/05/22 22:18:31  venku
   - All the interfaces were renamed to start with an "I".
   - Optimizing changes related Strings were made.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
 */
