
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

import edu.ksu.cis.indus.common.collections.MembershipPredicate;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Marker;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNodeGraphBuilder;

import gnu.trove.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import org.apache.commons.collections.map.LazyMap;


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
	 * This is used in the calculation of SCCs.
	 */
	private static int currCompNum;

	/** 
	 * This is used in the calculation of SCCs.
	 */
	private static int dfsNum;

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
	 * The graph builder to use to build graphs that represent views of this graph.
	 */
	protected IObjectDirectedGraphBuilder builder;

	/** 
	 * This indicates if this graph has a spanning forest.
	 */
	protected boolean hasSpanningForest;

	/** 
	 * This is the node indexed discover time of the nodes in this graph.
	 *
	 * @invariant discoverTimes.size = getNodes().size()
	 * @invariant discoverTimes->forall(o | o > 0)
	 */
	int[] discoverTimes;

	/** 
	 * This is the collection of back edges in this graph corresponding to the minimum spanning calculated for this instance
	 * of the graph.
	 */
	private Collection backedges = new ArrayList();

	/** 
	 * This is the collection of cross edges in this graph corresponding to the minimum spanning calculated for this instance
	 * of the graph.
	 */
	private Collection crossedges = new ArrayList();

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
	 * This is the node indexed finish time of the nodes in this graph.
	 *
	 * @invariant finishTimes.size = getNodes().size()
	 * @invariant finishTimes->forall(o | o > 0)
	 */
	private int[] finishTimes;

	/** 
	 * This captures backward reachability information.
	 */
	private boolean[][] forwardReachabilityMatrix;

	/** 
	 * This indicates if dag has been calculated for this graph.
	 */
	private boolean dagExists;

	/** 
	 * This indicates if pseudo tails have been calculated for this graph.
	 */
	private boolean pseudoTailsCalculated;

	/** 
	 * This indicates if reachability information has been calculated for this graph.
	 */
	private boolean reachability;

	/**
	 * @see IDirectedGraph#isAncestorOf(INode,INode)
	 */
	public final boolean isAncestorOf(final INode ancestor, final INode descendent) {
		if (!hasSpanningForest) {
			createSpanningForest();
		}

		final int _anc = getIndexOfNode(ancestor);
		final int _desc = getIndexOfNode(descendent);
		return discoverTimes[_anc] <= discoverTimes[_desc] && finishTimes[_anc] >= finishTimes[_desc];
	}

	/**
	 * @see IDirectedGraph#getBackEdges()
	 */
	public final Collection getBackEdges() {
		if (!hasSpanningForest) {
			createSpanningForest();
		}

		final Collection _result;

		if (backedges.isEmpty()) {
			_result = Collections.EMPTY_LIST;
		} else {
			_result = Collections.unmodifiableCollection(backedges);
		}
		return _result;
	}

	/**
	 * @see IDirectedGraph#getCommonReachablesFrom(INode, boolean, INode, boolean)
	 */
	public Collection getCommonReachablesFrom(final INode node1, final boolean forward1, final INode node2,
		final boolean forward2) {
		if (!reachability) {
			calculateReachabilityInfo();
		}

		final Collection _result = new ArrayList();
		final boolean[] _n1;
		final boolean[] _n2;

		if (forward1) {
			_n1 = forwardReachabilityMatrix[getIndexOfNode(node1)];
		} else {
			_n1 = backwardReachabilityMatrix[getIndexOfNode(node1)];
		}

		if (forward2) {
			_n2 = forwardReachabilityMatrix[getIndexOfNode(node2)];
		} else {
			_n2 = backwardReachabilityMatrix[getIndexOfNode(node2)];
		}

		final List _nodes = getNodes();

		for (int _i = _nodes.size() - 1; _i >= 0; _i--) {
			if (_n1[_i] && _n2[_i]) {
				_result.add(_nodes.get(_i));
			}
		}

		return _result;
	}

	/**
	 * @see IDirectedGraph#getCycles()
	 */
	public final Collection getCycles() {
		return findCycles(getNodes(), getBackEdges());
	}

	/**
	 * @see IDirectedGraph#getDAG()
	 */
	public final IObjectDirectedGraph getDAG() {
		if (!dagExists) {
			setupGraphBuilder();

			final List _topoSorted = performTopologicalSort(true);
			final List _succs = new ArrayList(_topoSorted);
			final List _preds = new ArrayList();
			builder.createGraph();

			final Iterator _i = _topoSorted.iterator();
			final int _iEnd = _topoSorted.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final INode _node = (INode) _i.next();
				_succs.remove(_node);
				builder.createNode(_node);
				builder.addEdgeFromTo(_node, CollectionUtils.intersection(_node.getSuccsOf(), _succs));
				builder.addEdgeFromTo(CollectionUtils.intersection(_node.getPredsOf(), _preds), _node);
				_preds.add(_node);
			}
			builder.finishBuilding();
			dagExists = true;
		}
		return builder.getBuiltGraph();
	}

	/**
	 * @see IDirectedGraph#getHeads()
	 */
	public final Collection getHeads() {
		return Collections.unmodifiableCollection(heads);
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IDirectedGraph#getNodesOnPathBetween(java.util.Collection)
	 */
	public Collection getNodesOnPathBetween(final Collection nodes) {
		final Collection _result = new HashSet();
		final Map _node2ancestorsMap =
			LazyMap.decorate(new HashMap(),
				new Transformer() {
					public Object transform(final Object key) {
						return getReachablesFrom((INode) key, false);
					}
				});

		final Iterator _i = nodes.iterator();
		final int _iEnd = nodes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final INode _node1 = (INode) _i.next();
			final Collection _descendants = getReachablesFrom(_node1, true);
			final Collection _intersection = CollectionUtils.intersection(nodes, _descendants);
			final Iterator _j = _intersection.iterator();
			final int _jEnd = _intersection.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final INode _node2 = (INode) _j.next();
				final Collection _ancestors = (Collection) _node2ancestorsMap.get(_node2);
				_result.addAll(CollectionUtils.intersection(_ancestors, _descendants));
				_result.add(_node2);
			}

			if (_jEnd > 0) {
				_result.add(_node1);
			}
		}
		return _result;
	}

	/**
	 * @see IDirectedGraph#getPseudoTails()
	 */
	public final Collection getPseudoTails() {
		if (!pseudoTailsCalculated) {
			// get the tails of the DAG into dtails
			final IDirectedGraph _graph = getDAG();
			final Collection _dtails = new HashSet(_graph.getTails());
			CollectionUtils.transform(_dtails, IObjectDirectedGraph.OBJECT_EXTRACTOR);

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
	 * @see IDirectedGraph#isReachable(INode,INode,boolean)
	 */
	public final boolean isReachable(final INode src, final INode dest, final boolean forward) {
		if (!reachability) {
			calculateReachabilityInfo();
		}

		final boolean[][] _matrix;

		if (forward) {
			_matrix = forwardReachabilityMatrix;
		} else {
			_matrix = backwardReachabilityMatrix;
		}
		return _matrix[getIndexOfNode(src)][getIndexOfNode(dest)];
	}

	/**
	 * @see IDirectedGraph#getReachablesFrom(INode, boolean)
	 */
	public final Collection getReachablesFrom(final INode root, final boolean forward) {
		if (!reachability) {
			calculateReachabilityInfo();
		}

		final boolean[] _matrix;

		if (forward) {
			_matrix = forwardReachabilityMatrix[getIndexOfNode(root)];
		} else {
			_matrix = backwardReachabilityMatrix[getIndexOfNode(root)];
		}

		final Collection _result = new ArrayList();
		final List _nodes = getNodes();

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
		final List _nodes = getNodes();
		final Collection _sccs = findSCCs(_nodes);
		final Map _node2scc = new HashMap();

		for (final Iterator _i = _sccs.iterator(); _i.hasNext();) {
			final Collection _scc = (Collection) _i.next();

			for (final Iterator _j = _scc.iterator(); _j.hasNext();) {
				final Object _n = _j.next();
				_node2scc.put(_n, _scc);
			}
		}

		final List _topologicallyOrdered = performTopologicalSort(false);
		final List _result = new ArrayList();
		final Collection _keySet = _node2scc.keySet();
		final Iterator _i =
			IteratorUtils.filteredIterator(_topologicallyOrdered.iterator(),
				new Predicate() {
					public boolean evaluate(final Object o) {
						return _node2scc.containsKey(o);
					}
				});

		for (; _i.hasNext();) {
			final Object _o = _i.next();
			final Collection _scc = (Collection) _node2scc.get(_o);
			_result.add(_scc);
			_keySet.removeAll(_scc);
		}

		if (topDown) {
			Collections.reverse(_result);
		}

		return _result;
	}

	/**
	 * Finds SCCs in the given node.
	 *
	 * @param nodes of interest.
	 *
	 * @return a collection of sccs
	 *
	 * @pre nodes != null and nodes.oclIsKindOf(Collection(INode))
	 * @post result != null and result.oclIsKindOf(Collection(List(INode)))
	 * @post result->forall(o | o->forall(p | nodes.contains(p)))
	 * @post nodes->forall(o | result->exists(p | p.contains(o)))
	 */
	public static Collection findSCCs(final Collection nodes) {
		final Collection _result = new ArrayList();
		final Map _node2srd =
			LazyMap.decorate(new HashMap(),
				new Factory() {
					public Object create() {
						return new SCCRelatedData();
					}
				});
		currCompNum = 0;
		dfsNum = 0;

		final ArrayStack _stack = new ArrayStack();
		final Iterator _i = nodes.iterator();

		while (_i.hasNext()) {
			final INode _n = (INode) _i.next();
			final SCCRelatedData _nSRD = (SCCRelatedData) _node2srd.get(_n);

			if (_nSRD.getDfsNum() == 0) {
				calculateSCCs(nodes, _node2srd, _n, _stack, _result);
			}
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
		final List _nodes = getNodes();
		final TIntObjectHashMap _finishTime2node = new TIntObjectHashMap();
		final Collection _processed = new HashSet();
		int _time = 0;

		for (final Iterator _i = _nodes.iterator(); _i.hasNext();) {
			final INode _node = (INode) _i.next();

			if (!_processed.contains(_node)) {
				_time = getFinishTimes(_node, _processed, _finishTime2node, _time);
			}
		}

		final int[] _keys = _finishTime2node.keys();
		final List _result = new ArrayList(_keys.length);
		Arrays.sort(_keys);

		for (int _i = 0; _i < _keys.length; _i++) {
			_result.add(_finishTime2node.get(_keys[_i]));
		}

		if (topdown) {
			Collections.reverse(_result);
		}

		return _result;
	}

	/**
	 * Finds cycles in the given set of nodes.  This implementation is <i>exponential</i> in the number of cycles in the the
	 * nodes.
	 *
	 * @param nodes in which to search for cycles.
	 * @param backedges is the back edges between the given set of nodes (and may be other nodes).
	 *
	 * @return a collection of cycles. Each cycle is represented as a sequence in which the first element starts the cycle.
	 *
	 * @pre scc != null and nodes.oclIsKindOf(Collection(INode))
	 * @pre backEdges != null and backEdges.oclIsKindOf(Collection(Pair(INode, INode)))
	 * @post result != null and result.oclIsKindOf(Collection(Sequence(INode)))
	 * @post result->forall(o | nodes.containsAll(o))
	 */
	public static Collection findCycles(final Collection nodes, final Collection backedges) {
		final Collection _result = new ArrayList();
		final Collection _sccs = findSCCs(nodes);
		final Iterator _j = _sccs.iterator();
		final int _jEnd = _sccs.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final List _scc = (List) _j.next();

			if (_scc.size() == 1) {
				final INode _node = (INode) _scc.iterator().next();

				if (_node.getSuccsOf().contains(_node)) {
					_result.add(Collections.singletonList(_node));
				}
			} else {
				final Collection _edges = new ArrayList(backedges);

				for (final Iterator _i = _edges.iterator(); _i.hasNext();) {
					final Pair _edge = (Pair) _i.next();

					if (!_scc.contains(_edge.getFirst()) || !_scc.contains(_edge.getSecond())) {
						_i.remove();
					}
				}
				_result.addAll(findCyclesOccurringIn(_scc, _edges));
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
			final int _nodePos = getIndexOfNode(_node);
			final String _str = "[" + _node.toString() + "]";

			for (final Iterator _j = _node.getSuccsOf().iterator(); _j.hasNext();) {
				final Object _succ = _j.next();
				_sb.append(_nodePos).append(_str).append(" -> ").append(getIndexOfNode((INode) _succ)).append("[")
					 .append(_succ).append("]").append("\n");
			}

			for (final Iterator _j = _node.getPredsOf().iterator(); _j.hasNext();) {
				final Object _pred = _j.next();
				_sb.append(_nodePos).append(_str).append(" <- ").append(getIndexOfNode((INode) _pred)).append("[")
					 .append(_pred).append("]").append("\n");
			}
		}
		return _sb.toString();
	}

	/**
	 * Retrieves the index of the given node in the list of nodes of this graph.  This implementation will return the value
	 * of <code>getNodes().indexOf(node)</code>.  However, subclasses are free to implement this in an optimal manner.
	 *
	 * @param node for which the index is requested.
	 *
	 * @return the index of the node in the list of nodes.
	 *
	 * @post result = getNodes().indexOf(node)
	 */
	protected int getIndexOfNode(final INode node) {
		return getNodes().indexOf(node);
	}

	/**
	 * Sets up the graph builder required by this graph.  This method will be called before the builder will be used.  This
	 * enables us to delay the creation of the builder until it is required.  This implementation will use
	 * <code>SimpleNodeGraphBuilder</code>.
	 *
	 * @post builder != null
	 */
	protected void setupGraphBuilder() {
		builder = new SimpleNodeGraphBuilder();
	}

	/**
	 * Changes the state of the graph as it's shape changed.
	 */
	protected void shapeChanged() {
		hasSpanningForest = false;
		pseudoTailsCalculated = false;
		reachability = false;
		dagExists = false;
	}

	/**
	 * Gets immediate successors that occur in nodes and are not reachable via the given edges.
	 *
	 * @param node whose successors are required.
	 * @param edgesNotToUse are the edges whose destination nodes should not be included in the result when the source node
	 * 		  is same as <code>node</code>.
	 * @param nodes is a collection of nodes of which the result nodes should be a members of.
	 *
	 * @return a collection of nodes.
	 *
	 * @pre node != null and edgesNotToUse != null and nodes != null
	 * @pre nodes.oclIsKindOf(Collection(INode))
	 * @pre edgesNotToUse.oclIsKindOf(Collection(Pair(INode, INode)))
	 * @post result != null and result.oclIsKindOf(Collection(INode))
	 * @post nodes.containsAll(result)
	 * @post edgesNotToUse->forall(o | o.getFirst().equals(node) implies !result.contains(o.getSecond()))
	 */
	private static Collection getLimitedSuccsOf(final INode node, final Collection edgesNotToUse, final Collection nodes) {
		final Collection _result = new HashSet(node.getSuccsOf());

		for (final Iterator _i = edgesNotToUse.iterator(); _i.hasNext();) {
			final Pair _edge = (Pair) _i.next();

			if (_edge.getFirst().equals(node)) {
				_result.remove(_edge.getSecond());
			}
		}
		_result.retainAll(nodes);

		return _result;
	}

	/**
	 * Calculates SCC according to the algorithm in Udi Manber's book.
	 *
	 * @param nodes of interest.
	 * @param node2srd maps nodes to an instance of <code>SCCRelatedData</code>.
	 * @param node to be explored.
	 * @param stack to be used.
	 * @param sccs is the outgoing argument to contains the SCCs.
	 *
	 * @pre nodes != null and nodes.oclIsKindOf(Collection(INode))
	 * @pre node != null and stack != null and stack.oclIsKindOf(ArrayStack(INode))
	 * @pre sccs != null and sccs.oclIsKindOf(Collection(List(INode)))
	 * @post sccs.containsAll(sccs$pre)
	 * @post sccs->forall(o | o->forall(p | nodes.contains(p)))
	 */
	private static void calculateSCCs(final Collection nodes, final Map node2srd, final INode node, final ArrayStack stack,
		final Collection sccs) {
		final SCCRelatedData _nodeSRD = (SCCRelatedData) node2srd.get(node);
		_nodeSRD.setDfsNum(dfsNum);
		_nodeSRD.setHigh(dfsNum);
		stack.push(node);
		dfsNum--;

		final Iterator _j =
			IteratorUtils.filteredIterator(node.getSuccsOf().iterator(),
				new Predicate() {
					public boolean evaluate(final Object o) {
						return nodes.contains(o);
					}
				});

		for (; _j.hasNext();) {
			final INode _succ = (INode) _j.next();
			final SCCRelatedData _succSRD = (SCCRelatedData) node2srd.get(_succ);

			if (_succSRD.getDfsNum() == 0) {
				calculateSCCs(nodes, node2srd, _succ, stack, sccs);
				_nodeSRD.setHigh(Math.max(_nodeSRD.getHigh(), _succSRD.getHigh()));
			} else if (_succSRD.getDfsNum() > _nodeSRD.getDfsNum() && _succSRD.getComponentNum() == 0) {
				_nodeSRD.setHigh(Math.max(_nodeSRD.getHigh(), _succSRD.getDfsNum()));
			}
		}

		if (_nodeSRD.getHigh() == _nodeSRD.getDfsNum()) {
			currCompNum++;
			_nodeSRD.setComponentNum(currCompNum);

			INode _o;
			final Collection _scc = new ArrayList();

			do {
				_o = (INode) stack.pop();
				node2srd.put(_o, _nodeSRD);
				_scc.add(_o);
			} while (_o != node);
			sccs.add(_scc);
		}
	}

	/**
	 * Checks if the given cycle has been recorded.
	 *
	 * @param newCycle is the cycle being checked for if it is recorded.
	 * @param cycles is the collection of recorded cycles.
	 *
	 * @return <code>true</code> if <code>newCycle</code> occurs in <code>cycles</code>; <code>false</code>, otherwise.
	 *
	 * @pre newCycle != null and cycles != null
	 * @pre cyclces.oclIsKindOf(Collection(Sequence(Object)))
	 */
	private static boolean cycleNotRecorded(final List newCycle, final Collection cycles) {
		boolean _result = true;
		final List _temp = new ArrayList();
		final Iterator _iter = cycles.iterator();
		final int _iterEnd = cycles.size();

		for (int _iterIndex = 0; _iterIndex < _iterEnd && _result; _iterIndex++) {
			final List _cycle = (List) _iter.next();

			if (_cycle.size() == newCycle.size()) {
				_temp.clear();
				_temp.addAll(_cycle);
				_temp.addAll(_cycle);
				_result = Collections.indexOfSubList(_temp, newCycle) == -1;
			}
		}
		return _result;
	}

	/**
	 * Finds cycles containing only the given SCC.
	 *
	 * @param scc in which the cycles should be detected.
	 * @param backedges is the back edges only between the given set of nodes.
	 *
	 * @return a collection of cycles.
	 *
	 * @pre nodes != null and nodes.oclIsKindOf(Collection(INode)) and nodes.size() > 1
	 * @post result != null and result.oclIsKindOf(Collection(Sequence(INode)))
	 * @post result->forall(o | nodes->containsAll(o))
	 * @pre backEdges != null and backEdges.oclIsKindOf(Collection(Pair(INode, INode)))
	 * @pre backEdges->forall(o | scc.contains(o.getFirst()) and scc.contains(o.getSecond()))
	 */
	private static Collection findCyclesOccurringIn(final Collection scc, final Collection backedges) {
		final IWorkBag _wb = new LIFOWorkBag();
		final Stack _dfsPath = new Stack();
		final Collection _result = new HashSet();
		final Predicate _cyclePredicate = new MembershipPredicate(true, _dfsPath);
		final Predicate _noncyclePredicate = new MembershipPredicate(false, _dfsPath);
		final Collection _backEdgesNotToUse = new HashSet(backedges);
		final Iterator _k = backedges.iterator();
		final int _kEnd = backedges.size();

		for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
			final Pair _edge = (Pair) _k.next();
			_backEdgesNotToUse.remove(_edge);
			_dfsPath.clear();

			final Object _dest = _edge.getSecond();
			_wb.addWork(_dest);

			while (_wb.hasWork()) {
				final Object _o = _wb.getWork();

				if (_o instanceof Marker) {
					final Object _temp = ((Marker) _o).getContent();

					while (!_temp.equals(_dfsPath.peek())) {
						_dfsPath.pop();
					}
				} else {
					final INode _node = (INode) _o;
					final Collection _succsOf = getLimitedSuccsOf(_node, _backEdgesNotToUse, scc);

					_dfsPath.push(_node);

					for (final Iterator _i = IteratorUtils.filteredIterator(_succsOf.iterator(), _cyclePredicate);
						  _i.hasNext();) {
						final Object _ele = _i.next();
						final List _cycle = _dfsPath.subList(_dfsPath.indexOf(_ele), _dfsPath.size());

						if (cycleNotRecorded(_cycle, _result)) {
							final Collection _temp = new ArrayList(_cycle);
							_result.add(_temp);
						}
					}

					CollectionUtils.filter(_succsOf, _noncyclePredicate);

					if (!_succsOf.isEmpty()) {
						final Marker _marker = new Marker(_node);
						final Iterator _j = _succsOf.iterator();
						final int _jEnd = _succsOf.size();

						for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
							final Object _ele = _j.next();

							if (_jEnd > 1) {
								_wb.addWork(_marker);
							}
							_wb.addWork(_ele);
						}
					}
				}
			}
			_backEdgesNotToUse.add(_edge);
		}
		return _result;
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
	 * Calculates the finish times for the nodes of this graph.
	 *
	 * @param node to start dfs from.
	 * @param processed are the nodes processed during dfs.
	 * @param finishTime2node maps finishTime(<code>Integer</code>) to a node.
	 * @param time is the counter used to calculate finish times.
	 *
	 * @return the finish time after the given dfs traversal.
	 *
	 * @pre getNodes().contains(node)
	 */
	private static int getFinishTimes(final INode node, final Collection processed, final TIntObjectHashMap finishTime2node,
		final int time) {
		processed.add(node);

		int _temp = time;
		_temp++;

		for (final Iterator _i = node.getSuccsOf().iterator(); _i.hasNext();) {
			final INode _succ = (INode) _i.next();

			if (processed.contains(_succ)) {
				continue;
			}
			_temp = getFinishTimes(_succ, processed, finishTime2node, _temp);
		}
		finishTime2node.put(++_temp, node);
		return _temp;
	}

	/**
	 * Calculates reachability information for this graph.
	 */
	private void calculateReachabilityInfo() {
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
				final int _indexOfSucc = getIndexOfNode(_succ);
				forwardReachabilityMatrix[_iIndex][_indexOfSucc] = true;
				backwardReachabilityMatrix[_indexOfSucc][_iIndex] = true;
			}
		}

		for (int _j = 0; _j < _noOfNodes; _j++) {
			for (int _k = 0; _k < _noOfNodes; _k++) {
				if (forwardReachabilityMatrix[_k][_j]) {
					for (int _l = 0; _l < _noOfNodes; _l++) {
						if (forwardReachabilityMatrix[_j][_l]) {
							forwardReachabilityMatrix[_k][_l] = true;
							backwardReachabilityMatrix[_l][_k] = true;
						}
					}
				}
			}
		}
		reachability = true;
	}

	/**
	 * Creates the spanning forest of the graph.
	 *
	 * @post hasSpanningForest = true
	 */
	private void createSpanningForest() {
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

		final Collection _blackNodes = new ArrayList();
		final Collection _grayNodes = new ArrayList();
		int _discoverTime = 0;
		discoverTimes = new int[_nodes.size()];
		finishTimes = new int[_nodes.size()];
		//highnums = new int[_nodes.size()];
		backedges.clear();
		crossedges.clear();

		while (_order.hasWork()) {
			final Object _work = _order.getWork();

			if (_work instanceof Marker) {
				final INode _node = (INode) ((Marker) _work).getContent();
				final int _indexOfNode = getIndexOfNode(_node);
				finishTimes[_indexOfNode] = ++_discoverTime;
				//processNodeForHighValues(_node, _indexOfNode);
			} else if (!_blackNodes.contains(_work)) {
				// we do not want to process nodes that are already processed.
				final INode _node = (INode) _work;
				discoverTimes[getIndexOfNode(_node)] = ++_discoverTime;
				_grayNodes.add(_node);
				_order.addWork(new Marker(_node));
				processNodeForSpanningTree(_grayNodes, _order, _node);
				_blackNodes.add(_node);
			}
		}

		hasSpanningForest = true;
	}

	/**
	 * Processes the given node while creating a spanning tree.
	 *
	 * @param grayNodes is the collection of nodes already visited or reached but not processed.
	 * @param workBag is the work bag that needs to be updates during processing.
	 * @param nodeToProcess is the node to be processed.
	 *
	 * @pre grayNodes != null and workBag != null and nodeToProcess != null
	 * @post processedNodes.containsAll(processedNodes$pre)
	 * @post reachedNodes.containsAll(reachedNodes$pre)
	 * @post processedNodes.containsAll(processedNodes$pre)
	 */
	private void processNodeForSpanningTree(final Collection grayNodes, final IWorkBag workBag, final INode nodeToProcess) {
		final Collection _temp = new HashSet();
		spanningSuccs.put(nodeToProcess, _temp);

		for (final Iterator _j = nodeToProcess.getSuccsOf().iterator(); _j.hasNext();) {
			final INode _succ = (INode) _j.next();

			// edges to visited nodes can only be backedges or crossedges.
			if (grayNodes.contains(_succ)) {
				final int _destIndex = getIndexOfNode(_succ);
				final Pair _edge = new Pair(nodeToProcess, _succ);

				if (finishTimes[_destIndex] > 0) {
					crossedges.add(_edge);
				} else {
					backedges.add(_edge);
				}
			} else {
				_temp.add(_succ);
				workBag.addWork(_succ);
			}
		}
	}
}

// End of File
