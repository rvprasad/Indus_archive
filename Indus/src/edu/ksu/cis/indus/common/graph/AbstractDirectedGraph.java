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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.annotations.NumericalConstraint;
import edu.ksu.cis.indus.annotations.Functional.AccessSpecifier;
import edu.ksu.cis.indus.annotations.NumericalConstraint.NumericalValue;
import edu.ksu.cis.indus.common.collections.Cache;
import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.FactoryBasedLazyMap;
import edu.ksu.cis.indus.common.collections.IFactory;
import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.common.collections.ITransformer;
import edu.ksu.cis.indus.common.collections.IteratorUtils;
import edu.ksu.cis.indus.common.collections.MembershipPredicate;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.collections.TransformerBasedLazyMap;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Marker;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;

import gnu.trove.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides abstract implementation of <code>IDirectedGraph</code>. The subclasses are responsible for
 * maintaining the collection of nodes that make up this graph.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <N> the node type of this graph.
 */
public abstract class AbstractDirectedGraph<N extends INode<N>>
		implements IDirectedGraph<N>, Iterable<N> {

	/**
	 * This is used in the calculation of SCCs.
	 */
	private static int currCompNum;

	/**
	 * This is used in the calculation of SCCs.
	 */
	private static int dfsNum;

	/**
	 * This is the node indexed discover time of the nodes in this graph.
	 * 
	 * @invariant discoverTimes.size = getNodes().size()
	 */
	int[] discoverTimes;

	/**
	 * This is the collection of back edges in this graph corresponding to the minimum spanning calculated for this instance
	 * of the graph.
	 */
	@NonNullContainer private final Collection<Pair<N, N>> backedges = new ArrayList<Pair<N, N>>();

	/**
	 * This captures backward reachability information.
	 */
	private BitSet[] backwardReachabilityMatrix;

	/**
	 * The graph builder to use to build graphs that represent views of this graph.
	 */
	private SimpleNodeGraphBuilder<N> builder;

	/**
	 * This is a node-node connectivity cache.
	 */
	@NonNull private Map<Triple<N, N, Boolean>, Collection<N>> connectivityCache = new Cache<Triple<N, N, Boolean>, Collection<N>>(
			50);

	/**
	 * This is the collection of cross edges in this graph corresponding to the minimum spanning calculated for this instance
	 * of the graph.
	 */
	@NonNullContainer private final Collection<Pair<N, N>> crossedges = new ArrayList<Pair<N, N>>();

	/**
	 * This indicates if dag has been calculated for this graph.
	 */
	private boolean dagExists;

	/**
	 * This is the node indexed finish time of the nodes in this graph.
	 * 
	 * @invariant finishTimes.size = getNodes().size()
	 */
	private int[] finishTimes;

	/**
	 * This captures backward reachability information.
	 */
	private BitSet[] forwardReachabilityMatrix;

	/**
	 * This indicates if the SCCs have been identified for this graph.
	 */
	private boolean hasSCC;

	/**
	 * This indicates if this graph has a spanning forest.
	 */
	private boolean hasSpanningForest;

	/**
	 * The collection of pseudo tails in the given graph. Refer to <code>getPseudoTails()</code> for details.
	 */
	@NonNullContainer private final Collection<N> pseudoTails = new HashSet<N>();

	/**
	 * This indicates if pseudo tails have been calculated for this graph.
	 */
	private boolean pseudoTailsCalculated;

	/**
	 * This indicates if reachability information has been calculated for this graph.
	 */
	private boolean reachability;

	/**
	 * This is the collection of SCCs in this graph.
	 */
	@NonNullContainer private List<List<N>> scc;

	/**
	 * This is the collection of sink nodes in the graph.
	 * 
	 * @invariant sinks->forall(o | o.getSuccsOf().size() = 0)
	 */
	@NonNullContainer private final Collection<N> sinks = new HashSet<N>();

	/**
	 * This flag indicates if sinks are available (have been calculated).
	 */
	private boolean sinksAreAvailable;

	/**
	 * This is the collection of source nodes in the graph.
	 * 
	 * @invariant sinks->forall(o | o.getPredsOf().size() = 0)
	 */
	@NonNullContainer private final Collection<N> sources = new HashSet<N>();

	/**
	 * This flag indicates if sources are available (have been calculated).
	 */
	private boolean sourcesAreAvailable;

	/**
	 * This maps a node to it's spanning successor nodes.
	 * 
	 * @invariant getNodes().containsAll(spanningSuccs.keySet())
	 * @invariant spanningSuccs.values()->forall( o | getNodes().containsAll(o))
	 */
	private Map<N, Set<N>> spanningSuccs;

	/**
	 * Finds cycles in the given set of nodes. This implementation is <i>exponential</i> in the number of cycles in the the
	 * nodes.
	 * 
	 * @param <T> the type of the given nodes.
	 * @param nodes in which to search for cycles.
	 * @param backedges is the back edges between the given set of nodes (and may be other nodes).
	 * @return a collection of cycles. Each cycle is represented as a sequence in which the first element starts the cycle.
	 * @post result->forall(o | nodes.containsAll(o))
	 */
	@NonNull @NonNullContainer @Functional public static <T extends INode<T>> Collection<List<T>> findCycles(
			@NonNull @NonNullContainer @Immutable final Collection<T> nodes,
			@NonNull @NonNullContainer @Immutable final Collection<Pair<T, T>> backedges) {
		final Collection<List<T>> _result = new ArrayList<List<T>>();
		final Collection<List<T>> _sccs = findSCCs(nodes);
		final Iterator<List<T>> _j = _sccs.iterator();
		final int _jEnd = _sccs.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final List<T> _scc = _j.next();

			if (_scc.size() == 1) {
				final T _node = _scc.iterator().next();

				if (_node.getSuccsOf().contains(_node)) {
					_result.add(Collections.singletonList(_node));
				}
			} else {
				final Collection<Pair<T, T>> _edges = new ArrayList<Pair<T, T>>(backedges);

				for (final Iterator<Pair<T, T>> _i = _edges.iterator(); _i.hasNext();) {
					final Pair<T, T> _edge = _i.next();

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
	 * Finds SCCs in the given nodes.
	 * 
	 * @param <T> the type of the given nodes.
	 * @param nodes of interest.
	 * @return a collection of sccs
	 * @post result->forall(o | o->forall(p | nodes.contains(p)))
	 * @post nodes->forall(o | result->exists(p | p.contains(o)))
	 */
	@NonNull @NonNullContainer @Functional public static <T extends INode<T>> Collection<List<T>> findSCCs(
			@NonNull @NonNullContainer @Immutable final Collection<T> nodes) {
		final Collection<List<T>> _result = new ArrayList<List<T>>();
		final Map<T, SCCRelatedData> _node2srd = new FactoryBasedLazyMap<T, SCCRelatedData>(new HashMap<T, SCCRelatedData>(),
				new IFactory<SCCRelatedData>() {

					public SCCRelatedData create() {
						return new SCCRelatedData();
					}
				});
		currCompNum = 0;
		dfsNum = 0;

		final Stack<T> _stack = new Stack<T>();
		final Iterator<T> _i = nodes.iterator();

		while (_i.hasNext()) {
			final T _n = _i.next();
			final SCCRelatedData _nSRD = _node2srd.get(_n);

			if (_nSRD.getDfsNum() == 0) {
				calculateSCCs(nodes, _node2srd, _n, _stack, _result);
			}
		}
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
	 * @post sccs.containsAll(sccs$pre)
	 * @post sccs->forall(o | o->forall(p | nodes.contains(p)))
	 * @param <T> the type of nodes being processed.
	 */
	@Immutable private static <T extends INode<T>> void calculateSCCs(
			@NonNull @NonNullContainer @Immutable final Collection<T> nodes,
			@NonNull @NonNullContainer final Map<T, SCCRelatedData> node2srd,
			@NonNull @NonNullContainer @Immutable final T node, @NonNull @NonNullContainer final Stack<T> stack,
			@NonNull @NonNullContainer final Collection<List<T>> sccs) {
		final SCCRelatedData _nodeSRD = node2srd.get(node);
		_nodeSRD.setDfsNum(dfsNum);
		_nodeSRD.setHigh(dfsNum);
		stack.push(node);
		dfsNum--;

		final Iterator<T> _j = IteratorUtils.filteredIterator(node.getSuccsOf().iterator(), new MembershipPredicate<T>(true,
				nodes));

		for (; _j.hasNext();) {
			final T _succ = _j.next();
			final SCCRelatedData _succSRD = node2srd.get(_succ);

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

			T _o;
			final List<T> _scc = new ArrayList<T>();

			do {
				_o = stack.pop();
				node2srd.put(_o, _nodeSRD);
				_scc.add(_o);
			} while (_o != node);
			sccs.add(_scc);
		}
	}

	/**
	 * Checks if the given cycle has been recorded.
	 * 
	 * @param <T> the type of nodes being processed.
	 * @param newCycle is the cycle being checked for if it is recorded.
	 * @param cycles is the collection of recorded cycles.
	 * @return <code>true</code> if <code>newCycle</code> occurs in <code>cycles</code>; <code>false</code>,
	 *         otherwise.
	 */
	@Functional private static <T extends INode<T>> boolean cycleNotRecorded(
			@NonNull @NonNullContainer @Immutable final List<T> newCycle,
			@Immutable @NonNull @NonNullContainer final Collection<List<T>> cycles) {
		boolean _result = true;
		final List<T> _temp = new ArrayList<T>();
		final Iterator<List<T>> _iter = cycles.iterator();
		final int _iterEnd = cycles.size();

		for (int _iterIndex = 0; _iterIndex < _iterEnd && _result; _iterIndex++) {
			final List<T> _cycle = _iter.next();

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
	 * @param <T> is the type of the nodes occuring in the SCC.
	 * @param scc in which the cycles should be detected.
	 * @param backedges is the back edges only between the given set of nodes.
	 * @return a collection of cycles.
	 * @post result->forall(o | scc->containsAll(o))
	 * @pre backEdges->forall(o | scc.contains(o.getFirst()) and scc.contains(o.getSecond()))
	 */
	@NonNull @NonNullContainer @Functional private static <T extends INode<T>> Collection<List<T>> findCyclesOccurringIn(
			@NonNull @NonNullContainer final List<T> scc, @NonNull @NonNullContainer final Collection<Pair<T, T>> backedges) {
		final IWorkBag<Object> _wb = new LIFOWorkBag<Object>();
		final Stack<T> _dfsPath = new Stack<T>();
		final Collection<List<T>> _result = new HashSet<List<T>>();
		final IPredicate<T> _cyclePredicate = new MembershipPredicate<T>(true, _dfsPath);
		final IPredicate<T> _noncyclePredicate = new MembershipPredicate<T>(false, _dfsPath);
		final Collection<Pair<T, T>> _backEdgesNotToUse = new HashSet<Pair<T, T>>(backedges);
		final Iterator<Pair<T, T>> _k = backedges.iterator();
		final int _kEnd = backedges.size();

		for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
			final Pair<T, T> _edge = _k.next();
			_backEdgesNotToUse.remove(_edge);
			_dfsPath.clear();

			final T _dest = _edge.getSecond();
			_wb.addWork(_dest);

			while (_wb.hasWork()) {
				final Object _o = _wb.getWork();

				if (_o instanceof Marker) {
					final Object _temp = ((Marker) _o).getContent();

					while (!_temp.equals(_dfsPath.peek())) {
						_dfsPath.pop();
					}
				} else {
					@SuppressWarnings("unchecked") final T _node = (T) _o;
					final Collection<T> _succsOf = getLimitedImmediateSuccsOf(_node, _backEdgesNotToUse, scc);

					_dfsPath.push(_node);

					for (@SuppressWarnings("unchecked") final Iterator<T> _i = IteratorUtils.filteredIterator(_succsOf
							.iterator(), _cyclePredicate); _i.hasNext();) {
						final T _ele = _i.next();
						final List<T> _cycle = _dfsPath.subList(_dfsPath.indexOf(_ele), _dfsPath.size());

						if (cycleNotRecorded(_cycle, _result)) {
							final List<T> _temp = new ArrayList<T>(_cycle);
							_result.add(_temp);
						}
					}

					CollectionUtils.filter(_succsOf, _noncyclePredicate);

					if (!_succsOf.isEmpty()) {
						final Marker _marker = new Marker(_node);
						final Iterator<T> _j = _succsOf.iterator();
						final int _jEnd = _succsOf.size();

						for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
							final T _ele = _j.next();

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
	 * Calculates the finish times for the nodes of this graph.
	 * 
	 * @param <T> the type of the node.
	 * @param node to start dfs from.
	 * @param processed are the nodes processed during dfs.
	 * @param finishTime2node maps finishTime(<code>Integer</code>) to a node.
	 * @param time is the counter used to calculate finish times.
	 * @return the finish time after the given dfs traversal.
	 * @pre getNodes().contains(node)
	 */
	@Immutable private static <T extends INode<T>> int getFinishTimes(@NonNull @Immutable final T node,
			@NonNull @NonNullContainer final Collection<T> processed,
			@NonNull @NonNullContainer final TIntObjectHashMap finishTime2node, final int time) {
		processed.add(node);

		int _temp = time;
		_temp++;

		for (final Iterator<T> _i = node.getSuccsOf().iterator(); _i.hasNext();) {
			final T _succ = _i.next();

			if (processed.contains(_succ)) {
				continue;
			}
			_temp = getFinishTimes(_succ, processed, finishTime2node, _temp);
		}
		finishTime2node.put(++_temp, node);
		return _temp;
	}

	/**
	 * Gets immediate successors that occur in nodes and are not reachable via the given edges.
	 * 
	 * @param node whose successors are required.
	 * @param edgesNotToUse are the edges whose destination nodes should not be included in the result when the source node is
	 *            same as <code>node</code>.
	 * @param nodes is a collection of nodes of which the result nodes should be a members of.
	 * @return a collection of nodes.
	 * @param <T> the type of nodes being processed.
	 * @post nodes.containsAll(result)
	 * @post edgesNotToUse->forall(o | o.getFirst().equals(node) implies !result.contains(o.getSecond()))
	 */
	@Functional @NonNull @NonNullContainer private static <T extends INode<T>> Collection<T> getLimitedImmediateSuccsOf(
			@NonNull @Immutable final T node,
			@NonNull @NonNullContainer @Immutable final Collection<Pair<T, T>> edgesNotToUse,
			@NonNull @Immutable final Collection<T> nodes) {
		final Collection<T> _result = new HashSet<T>(node.getSuccsOf());

		for (final Iterator<Pair<T, T>> _i = edgesNotToUse.iterator(); _i.hasNext();) {
			final Pair<T, T> _edge = _i.next();

			if (_edge.getFirst().equals(node)) {
				_result.remove(_edge.getSecond());
			}
		}
		_result.retainAll(nodes);

		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final Collection<Pair<N, N>> getBackEdges() {
		createSpanningForest();

		final Collection<Pair<N, N>> _result;

		if (backedges.isEmpty()) {
			_result = Collections.emptyList();
		} else {
			_result = Collections.unmodifiableCollection(backedges);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional(level = AccessSpecifier.PACKAGE) public Collection<N> getCommonReachablesFrom(
			@NonNull @Immutable final N node1, final boolean forward1, @NonNull @Immutable final N node2,
			final boolean forward2) {
		calculateReachabilityInfo();

		final Collection<N> _result;
		final BitSet _n1;
		final BitSet _n2;

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

		if (_n1.intersects(_n2)) {
			final List<N> _nodes = getNodes();
			_result = new ArrayList<N>();
			final BitSet _r = new BitSet();
			_r.or(_n1);
			_r.and(_n2);

			for (int _i = _r.nextSetBit(0); _i >= 0; _i = _r.nextSetBit(_i + 1)) {
				_result.add(_nodes.get(_i));
			}
		} else {
			_result = Collections.emptyList();
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @NonNull @NonNullContainer public final Collection<N> getConnectivityNodesFor(
			@NonNull @Immutable final N node1, @NonNull @Immutable final N node2, final boolean forward) {
		Collection<N> _result = Collections.emptySet();
		// the order of the following check is important as connectivity depends on reachability.
		if (hasCommonReachablesFrom(node1, forward, node2, forward)) {
			final Triple<N, N, Boolean> _trp1 = new Triple<N, N, Boolean>(node1, node2, forward);

			if (connectivityCache.containsKey(_trp1)) {
				_result = connectivityCache.get(_trp1);
			} else {
				final Triple<N, N, Boolean> _trp2 = new Triple<N, N, Boolean>(node2, node1, !forward);

				if (connectivityCache.containsKey(_trp2)) {
					_result = connectivityCache.get(_trp2);
				} else {
					_result = calculateConnectivityNodes(node1, node2, forward);
					connectivityCache.put(_trp1, _result);
				}
			}
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @NonNullContainer @Functional(level = AccessSpecifier.PACKAGE) public final Collection<List<N>> getCycles() {
		return findCycles(getNodes(), getBackEdges());
	}

	/**
	 * {@inheritDoc}
	 */
	@NonNull @Functional(level = AccessSpecifier.PACKAGE) public final SimpleNodeGraph<N> getDAG() {
		if (!dagExists) {
			builder = new SimpleNodeGraphBuilder<N>();
			builder.createGraph();

			createSpanningForest();

			final List<N> _nodes = getNodes();
			for (final N _node : _nodes) {
				final int _nodeIndex = _nodes.indexOf(_node);
				final int _disTime = discoverTimes[_nodeIndex];
				final int _finTime = finishTimes[_nodeIndex];
				builder.createNode(_node);
				for (final N _succ : _node.getSuccsOf()) {
					builder.createNode(_succ);
					final int _succIndex = _nodes.indexOf(_succ);
					if (!(discoverTimes[_succIndex] <= _disTime && finishTimes[_succIndex] >= _finTime)) {
						builder.addEdgeFromTo(_node, _succ);
					}
				}
			}

			builder.finishBuilding();
			dagExists = true;
		}
		return builder.getBuiltGraph();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public Collection<N> getNodesOnPathBetween(
			@NonNull @NonNullContainer final Collection<N> nodes) {
		final Collection<N> _result = new HashSet<N>();
		final Map<N, Collection<N>> _node2ancestorsMap = new TransformerBasedLazyMap<N, Collection<N>>(
				new HashMap<N, Collection<N>>(), new ITransformer<N, Collection<N>>() {

					public Collection<N> transform(final N key) {
						return getReachablesFrom(key, false);
					}
				});

		final Iterator<N> _i = nodes.iterator();
		final int _iEnd = nodes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final N _node1 = _i.next();
			final Collection<N> _descendants = getReachablesFrom(_node1, true);
			final Collection<N> _intersection = SetUtils.intersection(nodes, _descendants);
			final Iterator<N> _j = _intersection.iterator();
			final int _jEnd = _intersection.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final N _node2 = _j.next();
				final Collection<N> _ancestors = _node2ancestorsMap.get(_node2);
				final Collection<N> _ancestorsInGivenNodes = SetUtils.intersection(_ancestors, _descendants);
				_result.addAll(_ancestorsInGivenNodes);
				_result.add(_node2);
			}

			if (_jEnd > 0) {
				_result.add(_node1);
			}
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final Collection<N> getReachablesFrom(
			@NonNull @Immutable final N root, final boolean forward) {
		calculateReachabilityInfo();

		final BitSet _matrix;

		if (forward) {
			_matrix = forwardReachabilityMatrix[getIndexOfNode(root)];
		} else {
			_matrix = backwardReachabilityMatrix[getIndexOfNode(root)];
		}

		final Collection<N> _result = new ArrayList<N>();
		final List<N> _nodes = getNodes();

		for (int _i = _matrix.nextSetBit(0); _i >= 0; _i = _matrix.nextSetBit(_i + 1)) {
			_result.add(_nodes.get(_i));
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final List<List<N>> getSCCs(
			final boolean topDown) {
		if (!hasSCC) {
			final List<N> _nodes = getNodes();
			final Collection<List<N>> _sccs = findSCCs(_nodes);
			final Map<N, List<N>> _node2scc = new HashMap<N, List<N>>();
			final SimpleNodeGraphBuilder<List<N>> _sngb = new SimpleNodeGraphBuilder<List<N>>();
			_sngb.createGraph();

			for (final Iterator<List<N>> _i = _sccs.iterator(); _i.hasNext();) {
				final List<N> _scc = _i.next();
				_sngb.createNode(_scc);

				for (final Iterator<N> _j = _scc.iterator(); _j.hasNext();) {
					final N _n = _j.next();
					_node2scc.put(_n, _scc);
				}
			}

			for (final Iterator<List<N>> _i = _sccs.iterator(); _i.hasNext();) {
				final List<N> _scc = _i.next();

				for (final Iterator<N> _j = _scc.iterator(); _j.hasNext();) {
					final N _n = _j.next();
					for (final Iterator<N> _k = _n.getSuccsOf().iterator(); _k.hasNext();) {
						final N _succ = _k.next();
						if (!_scc.contains(_succ)) {
							_sngb.addEdgeFromTo(_scc, _node2scc.get(_succ));
						}
					}
				}
			}

			_sngb.finishBuilding();
			final IObjectDirectedGraph<SimpleNode<List<N>>, List<N>> _sng = _sngb.getBuiltGraph();
			final List<SimpleNode<List<N>>> _r = _sng.performTopologicalSort(true);
			scc = CollectionUtils.collect(_r, _sng.getObjectExtractor());
			hasSCC = true;
		}

		final List<List<N>> _result = new ArrayList<List<N>>(scc);

		if (!topDown) {
			Collections.reverse(_result);
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final Collection<N> getSinks() {
		if (!sinksAreAvailable) {
			sinks.clear();

			final Iterator<N> _i = getNodes().iterator();
			final int _iEnd = getNodes().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final N _node = _i.next();

				if (_node.getSuccsOf().isEmpty()) {
					sinks.add(_node);
				}
			}
			sinksAreAvailable = true;
		}
		return Collections.unmodifiableCollection(sinks);
	}

	/**
	 * {@inheritDoc}
	 */
	public SimpleNodeGraph<N> getSpanningSubgraph(final IPredicate<Pair<N, N>> pred) {
		final SimpleNodeGraph<N> _projection = new SimpleNodeGraph<N>();
		for (final N _node : getNodes()) {
			for (final N _succ : _node.getSuccsOf()) {
				if (pred.evaluate(new Pair<N, N>(_node, _succ))) {
					final SimpleNode<N> _n1 = _projection.getNode(_node);
					final SimpleNode<N> _n2 = _projection.getNode(_succ);
					_projection.addEdgeFromTo(_n1, _n2);
				}
			}
		}
		return _projection;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final Collection<N> getSources() {
		if (!sourcesAreAvailable) {
			sources.clear();

			final Iterator<N> _i = getNodes().iterator();
			final int _iEnd = getNodes().size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final N _node = _i.next();

				if (_node.getPredsOf().isEmpty()) {
					sources.add(_node);
				}
			}
			sourcesAreAvailable = true;
		}
		return Collections.unmodifiableCollection(sources);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final Map<N, Set<N>> getSpanningSuccs() {
		createSpanningForest();
		return Collections.unmodifiableMap(spanningSuccs);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final Collection<N> getTails() {
		if (!pseudoTailsCalculated) {
			// get the tails of the DAG into dtails
			final SimpleNodeGraph<N> _graph = getDAG();
			final Collection<SimpleNode<N>> _dtails1 = new HashSet<SimpleNode<N>>(_graph.getSinks());
			final Collection<N> _dtails2 = new HashSet<N>();
			CollectionUtils.transform(_dtails1, _graph.getObjectExtractor(), _dtails2);

			// get the tails of the graph into _tails
			final Collection<N> _tails = getSinks();

			// for each dtail that is not a tail, check if tail is reachable from it.
			// If so, dtail is not a pseudo tail. If not, it is a pseudo tail.
			_dtails2.removeAll(_tails);

			final Collection<N> _temp = getDestUnreachableSources(_dtails2, _tails, true, false);
			final Collection<N> _result = getDestUnreachableSources(_temp, _temp, true, false);

			/*
			 * It is possible that a graph have 2 pseudo tails which are mutually reachable in the graph. In that case,
			 * _result is empty. This is the case when cycles in graph have common nodes. In such case, both should qualify as
			 * pseudo tails. In other cases, only those in _result should qualify as pseudo tails.
			 */
			if (_result.isEmpty()) {
				pseudoTails.addAll(_temp);
			} else {
				pseudoTails.addAll(_result);
			}
			pseudoTailsCalculated = true;
		}
		@SuppressWarnings("unchecked") final Collection<N> _tails = SetUtils.union(pseudoTails, getSinks());
		return _tails;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional public final boolean hasCommonReachablesFrom(@NonNull @Immutable final N node1, final boolean forward1,
			@NonNull @Immutable final N node2, final boolean forward2) {
		calculateReachabilityInfo();

		final BitSet _n1;
		final BitSet _n2;

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

		return _n1.intersects(_n2);
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final boolean isAncestorOf(
			@NonNull @Immutable final N ancestor, @NonNull @Immutable final N descendent) {
		createSpanningForest();

		final int _anc = getIndexOfNode(ancestor);
		final int _desc = getIndexOfNode(descendent);
		return discoverTimes[_anc] <= discoverTimes[_desc] && finishTimes[_anc] >= finishTimes[_desc];
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) public final boolean isReachable(@NonNull @Immutable final N src,
			@NonNull @Immutable final N dest, final boolean forward) {
		calculateReachabilityInfo();

		final BitSet[] _matrix;

		if (forward) {
			_matrix = forwardReachabilityMatrix;
		} else {
			_matrix = backwardReachabilityMatrix;
		}
		return _matrix[getIndexOfNode(src)].get(getIndexOfNode(dest));
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @NonNull @NonNullContainer public Iterator<N> iterator() {
		return getNodes().iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional(level = AccessSpecifier.PACKAGE) @NonNull @NonNullContainer public final List<N> performTopologicalSort(
			final boolean topdown) {
		final SimpleNodeGraph<N> _dag = getDAG();
		final List<SimpleNode<N>> _nodes = _dag.getNodes();
		final TIntObjectHashMap _finishTime2node = new TIntObjectHashMap();
		final Collection<SimpleNode<N>> _processed = new HashSet<SimpleNode<N>>();
		int _time = 0;

		for (final Iterator<SimpleNode<N>> _i = _nodes.iterator(); _i.hasNext();) {
			final SimpleNode<N> _node = _i.next();

			if (!_processed.contains(_node)) {
				_time = getFinishTimes(_node, _processed, _finishTime2node, _time);
			}
		}

		final int[] _keys = _finishTime2node.keys();
		final List<SimpleNode<N>> _result1 = new ArrayList<SimpleNode<N>>(_keys.length);
		Arrays.sort(_keys);

		for (int _i = 0; _i < _keys.length; _i++) {
			@SuppressWarnings("unchecked") final SimpleNode<N> _object = (SimpleNode<N>) _finishTime2node.get(_keys[_i]);
			_result1.add(_object);
		}

		if (topdown) {
			Collections.reverse(_result1);
		}

		final List<N> _result = new ArrayList<N>();
		CollectionUtils.transform(_result1, _dag.getObjectExtractor(), _result);
		return _result;
	}

	/**
	 * Sets the size of the connectivity cache.
	 * 
	 * @param size to be used.
	 */
	@Functional public final void setConnectivityCacheSize(
			@NumericalConstraint(value = NumericalValue.NON_NEGATIVE) final int size) {
		final Map<Triple<N, N, Boolean>, Collection<N>> _t = new Cache<Triple<N, N, Boolean>, Collection<N>>(size);
		_t.putAll(connectivityCache);
		connectivityCache = _t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @NonNull @Override public String toString() {
		final StringBuffer _sb = new StringBuffer();
		final List<N> _nodes = getNodes();

		for (final Iterator<N> _i = _nodes.iterator(); _i.hasNext();) {
			final N _node = _i.next();
			final int _nodePos = getIndexOfNode(_node);
			final String _str = "[" + _node.toString() + "]";

			for (final Iterator<N> _j = _node.getSuccsOf().iterator(); _j.hasNext();) {
				final N _succ = _j.next();
				_sb.append(_nodePos).append(_str).append(" -> ").append(getIndexOfNode(_succ)).append("[").append(_succ)
						.append("]").append("\n");
			}

			for (final Iterator<N> _j = _node.getPredsOf().iterator(); _j.hasNext();) {
				final N _pred = _j.next();
				_sb.append(_nodePos).append(_str).append(" <- ").append(getIndexOfNode(_pred)).append("[").append(_pred)
						.append("]").append("\n");
			}
		}
		return _sb.toString();
	}

	/**
	 * Retrieves the index of the given node in the list of nodes of this graph. This implementation will return the value of
	 * <code>getNodes().indexOf(node)</code>. However, subclasses are free to implement this in an optimal manner.
	 * 
	 * @param node for which the index is requested.
	 * @return the index of the node in the list of nodes.
	 * @post result = getNodes().indexOf(node)
	 */
	@Functional protected int getIndexOfNode(@Immutable @NonNull final N node) {
		return getNodes().indexOf(node);
	}

	/**
	 * Changes the state of the graph as it's shape changed.
	 */
	protected void shapeChanged() {
		hasSpanningForest = false;
		pseudoTailsCalculated = false;
		reachability = false;
		dagExists = false;
		sinksAreAvailable = false;
		sourcesAreAvailable = false;
		hasSCC = false;
	}

	/**
	 * Calculates the nodes that are needed to connect the given nodes.
	 * 
	 * @param src is the source node.
	 * @param dest is the destination node.
	 * @param forward <code>true</code> indicates forward direction; <code>false</code> indicates backward direction.
	 * @return a collection of connectivity nodes.
	 */
	@NonNull @NonNullContainer @Functional private Collection<N> calculateConnectivityNodes(@NonNull final N src,
			@NonNull final N dest, final boolean forward) {
		final Collection<N> _col = new HashSet<N>();
		final IWorkBag<N> _wb = new HistoryAwareFIFOWorkBag<N>(new HashSet<N>());
		_wb.addAllWork(src.getSuccsNodesInDirection(forward));

		while (_wb.hasWork()) {
			final N _succ = _wb.getWork();

			if (isReachable(_succ, dest, !forward)) {
				_col.add(_succ);
			} else {
				_wb.addAllWorkNoDuplicates(_succ.getSuccsNodesInDirection(forward));
			}
		}

		return _col;
	}

	/**
	 * Calculates reachability information for this graph.
	 */
	private void calculateReachabilityInfo() {
		if (!reachability) {
			final List<N> _nodes = getNodes();
			final int _noOfNodes = _nodes.size();
			final Collection<Integer> _sccIndices = new HashSet<Integer>();
			forwardReachabilityMatrix = new BitSet[_noOfNodes];
			backwardReachabilityMatrix = new BitSet[_noOfNodes];

			for (final List<N> _scc : getSCCs(true)) {
				final BitSet _f = new BitSet(_noOfNodes);
				final BitSet _b = new BitSet(_noOfNodes);

				_sccIndices.add(getIndexOfNode(_scc.get(0)));

				for (final N _node : _scc) {
					final int _iIndex = getIndexOfNode(_node);
					forwardReachabilityMatrix[_iIndex] = _f;
					backwardReachabilityMatrix[_iIndex] = _b;
				}
			}

			for (int _iIndex = 0; _iIndex < _noOfNodes; _iIndex++) {
				final N _node = _nodes.get(_iIndex);
				final Iterator<N> _j = _node.getSuccsOf().iterator();
				final int _jEnd = _node.getSuccsOf().size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final N _succ = _j.next();
					final int _indexOfSucc = getIndexOfNode(_succ);
					forwardReachabilityMatrix[_iIndex].set(_indexOfSucc);
					backwardReachabilityMatrix[_indexOfSucc].set(_iIndex);
				}
			}

			for (final int _j : _sccIndices) {
				final BitSet _jf = forwardReachabilityMatrix[_j];
				final BitSet _jb = backwardReachabilityMatrix[_j];
				for (final int _k : _sccIndices) {
					if (_k != _j) {
						final BitSet _kf = forwardReachabilityMatrix[_k];
						if (_kf.get(_j)) {
							_kf.or(_jf);
						}
						final BitSet _kb = backwardReachabilityMatrix[_k];
						if (_kb.get(_j)) {
							_kb.or(_jb);
						}
					}
				}
			}

			reachability = true;
			connectivityCache = new Cache<Triple<N, N, Boolean>, Collection<N>>(_noOfNodes);
		}
	}

	/**
	 * Creates the spanning forest of the graph.
	 * 
	 * @post hasSpanningForest = true
	 */
	private void createSpanningForest() {
		if (hasSpanningForest) {
			return;
		}

		if (spanningSuccs == null) {
			spanningSuccs = new HashMap<N, Set<N>>();
		} else {
			spanningSuccs.clear();
		}

		final IWorkBag<Object> _order = new LIFOWorkBag<Object>();
		final List<N> _nodes = getNodes();

		// It is possible that the graph has no heads, i.e., nodes with no predecessors, and these are handled here.
		if (getSources().isEmpty()) {
			_order.addAllWork(_nodes);
		} else {
			_order.addAllWork(getSources());
		}

		final Collection<N> _blackNodes = new ArrayList<N>();
		final Collection<N> _grayNodes = new ArrayList<N>();
		int _discoverTime = 0;
		discoverTimes = new int[_nodes.size()];
		finishTimes = new int[_nodes.size()];
		backedges.clear();
		crossedges.clear();

		while (_order.hasWork()) {
			final Object _work = _order.getWork();

			if (_work instanceof Marker) {
				@SuppressWarnings("unchecked") final N _node = (N) ((Marker) _work).getContent();
				final int _indexOfNode = getIndexOfNode(_node);
				finishTimes[_indexOfNode] = ++_discoverTime;
				// processNodeForHighValues(_node, _indexOfNode);
			} else if (!_blackNodes.contains(_work)) {
				// we do not want to process nodes that are already processed.
				@SuppressWarnings("unchecked") final N _node = (N) _work;
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
	 * Retrieves the source nodes which cannot reach any of the given destinations. If there are no destination nodes then all
	 * source nodes are returned.
	 * 
	 * @param sourceNodes is the collection of source nodes.
	 * @param destinations is the collection of destination nodes.
	 * @param forward <code>true</code> indicates following outgoing edges; <code>false</code> indicates following
	 *            incoming edges.
	 * @param considerSelfReachability <code>true</code> indicates that the reachability of a source from the source should
	 *            be considered; <code>false</code>, otherwise. In other words, if a source node is also a destination node
	 *            then the source node will not occur in the result if this parameter is <code>true</code>. It will occur
	 *            in the result when the parameter is <code>false</code>.
	 * @return a collection of source nodes.
	 * @post sources->includesAll(result)
	 * @post considerSelfReachability implies not result->exists(o | sources.contains(o))
	 * @post (not considerSelfReachability) implies sources->forall(o | destinations.contains(o) implies result.contains(o))
	 */
	@NonNull @Functional private Collection<N> getDestUnreachableSources(
			@NonNull @NonNullContainer @Immutable final Collection<N> sourceNodes,
			@NonNull @NonNullContainer @Immutable final Collection<N> destinations, final boolean forward,
			final boolean considerSelfReachability) {
		final Collection<N> _result = new HashSet<N>(sourceNodes);
		final Collection<N> _temp = new ArrayList<N>(destinations);

		if (!destinations.isEmpty()) {
			final Iterator<N> _i = sourceNodes.iterator();
			final int _iEnd = sourceNodes.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final N _src = _i.next();
				final Collection<N> _reachables = getReachablesFrom(_src, forward);
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
	 * Processes the given node while creating a spanning tree.
	 * 
	 * @param grayNodes is the collection of nodes already visited or reached but not processed.
	 * @param workBag is the work bag that needs to be updates during processing.
	 * @param nodeToProcess is the node to be processed.
	 * @pre grayNodes != null and workBag != null and nodeToProcess != null
	 * @post processedNodes.containsAll(processedNodes$pre)
	 * @post reachedNodes.containsAll(reachedNodes$pre)
	 * @post processedNodes.containsAll(processedNodes$pre)
	 */
	private void processNodeForSpanningTree(@NonNull @NonNullContainer @Immutable final Collection<N> grayNodes,
			@NonNull @NonNullContainer final IWorkBag<Object> workBag, @NonNull @NonNullContainer final N nodeToProcess) {
		final Set<N> _temp = new HashSet<N>();
		spanningSuccs.put(nodeToProcess, _temp);

		for (final Iterator<N> _j = nodeToProcess.getSuccsOf().iterator(); _j.hasNext();) {
			final N _succ = _j.next();

			// edges to visited nodes can only be backedges or crossedges.
			if (grayNodes.contains(_succ)) {
				final int _destIndex = getIndexOfNode(_succ);
				final Pair<N, N> _edge = new Pair<N, N>(nodeToProcess, _succ);

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
