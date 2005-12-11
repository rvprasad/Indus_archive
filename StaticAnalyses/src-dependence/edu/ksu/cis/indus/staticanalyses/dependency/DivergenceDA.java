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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.BackwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.ForwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.IDirectionSensitiveInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;

import soot.jimple.Stmt;

/**
 * This class provides divergence dependency information. This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal Study of Slicing for Multi-threaded Program with
 * JVM Concurrency Primitives"</a>.
 * <p>
 * This implementation by default does not consider call-sites for dependency calculation.
 * </p>
 * <p>
 * This implementation does not capture intraprocedural dependence within loops. Hence, if there is a loop inside a loop, then
 * the statements in the outer loop are not flagged as being dependent on the inner loop.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class DivergenceDA
		extends
		AbstractDependencyAnalysis<Stmt, SootMethod, Stmt, SootMethod, Map<Stmt, Collection<Stmt>>, Stmt, SootMethod, Stmt, SootMethod, Map<Stmt, Collection<Stmt>>> {

	/*
	 * The dependence information is stored as follows: For each method, a sequence of collection of statements is maintained.
	 * The length of the sequence is equal to the number of statements in the method. The statement collection at a location
	 * in this sequence corresponds to statements related via dependency to the statement at the same location in the
	 * statement list of the method. The collection is a singleton in case of dependee information.
	 */
	/*
	 * This implementation does not capture intraprocedural dependence within loops. Hence, if there is a loop inside a loop,
	 * then the statements in the outer loop are not flagged as being dependent on the inner loop. This can be remedied by -
	 * improving the precision of pre-divergence point identification. - using loop information in getValidSuccs() to control
	 * which successors should be considered for dependence.
	 */

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DivergenceDA.class);

	/**
	 * This provides the call graph information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * This indicates if call-sites that invoke methods containing pre-divergence points should be considered as
	 * pre-divergence points.
	 */
	private boolean considerCallSites;

	/**
	 * This provides direction-sensitive information to make the analysis direction sensitive.
	 */
	private final IDirectionSensitiveInfo directionSensInfo;

	/**
	 * This maps methods to the inter-procedural divergence points they contain.
	 *
	 * @invariant method2interProcDivPoints.values()->forall(o | o->forall(p | p.containsInvokeExpr()))
	 */
	private final Map<SootMethod, Collection<Stmt>> method2interProcDivPoints = new HashMap<SootMethod, Collection<Stmt>>();

	/**
	 * Creates an instance of this class.
	 *
	 * @param directionSensitiveInfo that controls the direction.
	 * @param direction of the analysis
	 * @pre info != null and direction != null
	 */
	private DivergenceDA(final IDirectionSensitiveInfo directionSensitiveInfo, final Direction direction) {
		super(direction);
		directionSensInfo = directionSensitiveInfo;
	}

	/**
	 * Retrieves an instance of divergence dependence analysis that calculates information in the specified direction.
	 *
	 * @param direction of the dependence information.
	 * @return an instance of divergence dependence.
	 * @throws IllegalArgumentException if direction does not satisfy the preconditions.
	 * @post result != null
	 * @pre direction.equals(IDependencyAnalysis.Direction.FORWARD_DIRECTION) or
	 *      direction.equals(IDependencyAnalysis.Direction..BACKWARD_DIRECTION)
	 */
	public static DivergenceDA getDivergenceDA(final Direction direction) {
		final DivergenceDA _result;

		if (direction.equals(Direction.FORWARD_DIRECTION)) {
			_result = new DivergenceDA(new ForwardDirectionSensitiveInfo(), direction);
		} else if (direction.equals(Direction.BACKWARD_DIRECTION)) {
			_result = new DivergenceDA(new BackwardDirectionSensitiveInfo(), direction);
		} else {
			final String _msg = "Argument should be either 'IDependencyAnalysis.Direction.FORWARD_DIRECTION' or "
					+ "'IDependencyAnalysis.Direction.BACKWARD_DIRECTION'. Provided argument was " + direction.toString();
			LOGGER.error("getForwardDivergenceDA()");
			throw new IllegalArgumentException(_msg);
		}
		return _result;
	}

	/**
	 * Calculates the divergence dependency in the methods.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	@Override public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Divergence Dependence processing");
		}

		final Map<SootMethod, Collection<Stmt>> _method2preDivPoints = new HashMap<SootMethod, Collection<Stmt>>();

		findPreDivPoints(_method2preDivPoints);

		for (final Iterator<Map.Entry<SootMethod, Collection<Stmt>>> _i = _method2preDivPoints.entrySet().iterator(); _i
				.hasNext();) {
			final Map.Entry<SootMethod, Collection<Stmt>> _entry = _i.next();
			final SootMethod _method = _entry.getKey();
			final Collection<Stmt> _preDivPoints = _entry.getValue();
			final Collection<BasicBlock> _succsOfPreDivBBs = calculateIntraBBDependence(_method, _preDivPoints);
			calculateInterBBDependence(_method, _succsOfPreDivBBs, _preDivPoints);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this.toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Divergence Dependence processing");
		}
		stable();
	}

	/**
	 * Returns the statements on which the given statement depends on in the given method.
	 *
	 * @param dependentStmt is the statement for which dependees are requested.
	 * @param method in which <code>dependentStmt</code> occurs.
	 * @return a collection of statements. However, in this case the collection contains only one statement as the .
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 *      java.lang.Object)
	 */
	public Collection<Stmt> getDependees(final Stmt dependentStmt, final SootMethod method) {
		final Map<Stmt, Collection<Stmt>> _map = MapUtils.queryMap(dependent2dependee, method);
		final Collection<Stmt> _queryCollection = MapUtils.queryCollection(_map, dependentStmt);
		return Collections.unmodifiableCollection(_queryCollection);
	}

	/**
	 * Returns the statements which depend on the given statement in the given method.
	 *
	 * @param dependeeStmt is the statement for which dependents are requested.
	 * @param method in which <code>dependeeStmt</code> occurs.
	 * @return a collection of statements.
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependents(java.lang.Object,
	 *      java.lang.Object)
	 */
	public Collection<Stmt> getDependents(final Stmt dependeeStmt, final SootMethod method) {
		final Map<Stmt, Collection<Stmt>> _stmt2List = MapUtils.queryMap(dependee2dependent, method);
		final Collection<Stmt> _result = MapUtils.queryCollection(_stmt2List, dependeeStmt);
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection<IDependencyAnalysis.DependenceSort> getIds() {
		return Collections.singleton(IDependencyAnalysis.DependenceSort.DIVERGENCE_DA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	@Override public void reset() {
		super.reset();
		method2interProcDivPoints.clear();
	}

	/**
	 * Sets if the analyses should consider the effects of method calls. This method may change the preprocessing requirements
	 * of this analysis. Hence, it should be called
	 *
	 * @param consider <code>true</code> indicates call-sites that invoke methods containing pre-divergence points should be
	 *            considered as pre-divergence points; <code>false</code>, otherwise.
	 */
	public void setConsiderCallSites(final boolean consider) {
		considerCallSites = consider;
	}

	/**
	 * Returns a stringized representation of this analysis. The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	@Override public String toString() {
		final StringBuffer _result = new StringBuffer("Statistics for divergence dependence as calculated by "
				+ getClass().getName() + "[" + hashCode() + "]\n");
		_result.append("The analyses setup was : \n \tinterprocedural: " + considerCallSites + "\n");

		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator<Map.Entry<SootMethod, Map<Stmt, Collection<Stmt>>>> _i = dependent2dependee.entrySet().iterator(); _i
				.hasNext();) {
			final Map.Entry<SootMethod, Map<Stmt, Collection<Stmt>>> _entry = _i.next();
			final SootMethod _method = _entry.getKey();
			_localEdgeCount = 0;

			final Map<Stmt, Collection<Stmt>> _dependees = _entry.getValue();

			for (final Stmt _dt : _dependees.keySet()) {
				final Collection<?> _c = _dependees.get(_dt);
				_temp.append("\t\t" + _dt + " --> " + _c + "\n");
				_localEdgeCount += _c.size();
			}

			_result.append("\tFor " + _method + " there are " + _localEdgeCount + " divergence dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}

		_result.append("A total of " + _edgeCount + " divergence dependence edges exist.");

		return _result.toString();
	}

	// /CLOVER:OFF

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependenceRetriever()
	 */
	@Override protected IDependenceRetriever<Stmt, SootMethod, Stmt, Stmt, SootMethod, Stmt> getDependenceRetriever() {
		return new StmtRetriever();
	}

	// /CLOVER:ON

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	@Override protected void setup() throws InitializationException {
		super.setup();
		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
		}
	}

	/**
	 * Calculates inter-basic block divergence dependence.
	 *
	 * @param method in which the basic blocks occur.
	 * @param succsOfPreDivPoints are the successor basic blocks of pre-divergent basic blocks.
	 * @param preDivPoints is the basic blocks which are the pre-divergent points.
	 * @pre method != null and succsOfPreDivPoints != null and preDivPoints != null
	 * @pre succsOfPreDivPoints->forall(o | preDivPoints->exists(p | o.getPredsOf().contains(p)))
	 */
	private void calculateInterBBDependence(final SootMethod method, final Collection<BasicBlock> succsOfPreDivPoints,
			final Collection<Stmt> preDivPoints) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Processing method " + method + " with divergent blocks: " + succsOfPreDivPoints);
		}

		final IWorkBag<BasicBlock> _wb = new FIFOWorkBag<BasicBlock>();
		final Collection<Stmt> _dependents = new HashSet<Stmt>();
		_wb.addAllWork(succsOfPreDivPoints);

		while (_wb.hasWork()) {
			final BasicBlock _bb = _wb.getWork();
			final Stmt _firstStmt = directionSensInfo.getFirstStmtInBB(_bb);
			final Collection<Stmt> _dependees = dependent2dependee.get(method).get(_firstStmt);
			_dependents.clear();

			if (!preDivPoints.contains(_firstStmt)) {
				final Collection<Stmt> _bbStmts = directionSensInfo.getIntraBBDependents(_bb, _firstStmt);

				for (final Iterator<Stmt> _i = _bbStmts.iterator(); _i.hasNext();) {
					final Stmt _stmt = _i.next();
					_dependents.add(_stmt);

					if (preDivPoints.contains(_stmt)) {
						break;
					}
				}
			}

			if (!succsOfPreDivPoints.contains(_bb)) {
				final Collection<BasicBlock> _succs = recordDepAcrossBB(method, preDivPoints, _dependees, _dependents,
						directionSensInfo.getFollowersOfBB(_bb));
				_wb.addAllWorkNoDuplicates(_succs);
			} else {
				recordDependenceInfoInBB(_dependees, method, _dependents);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Processing method " + method);
		}
	}

	/**
	 * Calculates intra-basic block divergence dependence.
	 *
	 * @param method in which the basic blocks occur.
	 * @param preDivPoints are the basic blocks that contain pre-divergent points.
	 * @return a collection of basic blocks that follow the blocks in <code>preDivPoints</code>.
	 * @pre method != null and preDivPoints != null
	 * @post result != null
	 * @post result->forall(o | preDivPoints->exists(p | o.getPredsOf().contains(p)))
	 */
	private Collection<BasicBlock> calculateIntraBBDependence(final SootMethod method, final Collection<Stmt> preDivPoints) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Processing method " + method + " with divergent points: " + preDivPoints);
		}

		final Collection<BasicBlock> _result = new HashSet<BasicBlock>();
		final Collection<Stmt> _dependents = new HashSet<Stmt>();
		final BasicBlockGraph _bbg = getBasicBlockGraph(method);

		for (final Iterator<Stmt> _i = preDivPoints.iterator(); _i.hasNext();) {
			Stmt _divPoint = _i.next();
			final BasicBlock _bb = _bbg.getEnclosingBlock(_divPoint);

			for (final Iterator _j = _bb.getStmtsOf().iterator(); _j.hasNext();) {
				_divPoint = (Stmt) _j.next();

				if (preDivPoints.contains(_divPoint)) {
					break;
				}
			}
			_dependents.clear();

			final Collection<Stmt> _bbStmts = directionSensInfo.getIntraBBDependents(_bb, _divPoint);

			for (final Iterator<Stmt> _j = _bbStmts.iterator(); _j.hasNext();) {
				final Stmt _stmt = _j.next();
				_dependents.add(_stmt);

				if (preDivPoints.contains(_stmt)) {
					recordDependenceInfoInBB(Collections.singleton(_divPoint), method, _dependents);
					_divPoint = _stmt;
					_dependents.clear();
				}
			}

			final Collection<BasicBlock> _validSuccs = getValidSuccs(_divPoint, _bb, _bbg, method);
			final Collection<BasicBlock> _temp = recordDepAcrossBB(method, preDivPoints, Collections.singleton(_divPoint),
					_dependents, _validSuccs);
			_result.addAll(_temp);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Processing method " + method);
		}

		return _result;
	}

	/**
	 * Finds the pre-divergent points in terms of pre-divergent statements and populates the given map. It also captures the
	 * methods in which pre-divergent points occur.
	 *
	 * @param method2preDivPoints maps methods (of interest) to a set of pre-divergent points. This is an out parameter.
	 * @return the collection of pre-divergent methods.
	 * @pre method2preDivPoints != null
	 * @post result != null
	 */
	private Collection<SootMethod> findIntraproceduralPreDivPoints(final Map<SootMethod, Collection<Stmt>> method2preDivPoints) {
		final Collection<Stmt> _preDivPoints = new HashSet<Stmt>();
		final Collection<SootMethod> _temp;

		if (considerCallSites) {
			_temp = new HashSet<SootMethod>();
		} else {
			_temp = null;
		}

		for (final Iterator<SootMethod> _i = callgraph.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _method = _i.next();
			final BasicBlockGraph _bbg = getBasicBlockGraph(_method);
			final List<List<BasicBlock>> _sccs = _bbg.getSCCs(true);

			for (final Iterator<List<BasicBlock>> _j = _sccs.iterator(); _j.hasNext();) {
				final List<BasicBlock> _scc = _j.next();

				if (_scc.size() > 1) {
					for (final Iterator<BasicBlock> _k = _scc.iterator(); _k.hasNext();) {
						final BasicBlock _bb = _k.next();

						if (!_scc.containsAll(_bb.getSuccsOf())) {
							_preDivPoints.add(_bb.getTrailerStmt());
						}
					}
				} else {
					final BasicBlock _bb = _scc.iterator().next();
					final Collection<BasicBlock> _succs = _bb.getSuccsOf();

					if (_succs.size() > 1 && _succs.contains(_bb)) {
						_preDivPoints.add(_bb.getTrailerStmt());
					}
				}
			}

			if (!_preDivPoints.isEmpty()) {
				method2preDivPoints.put(_method, new ArrayList<Stmt>(_preDivPoints));

				if (considerCallSites) {
					_temp.add(_method);
				}
				_preDivPoints.clear();
			}
		}
		return _temp;
	}

	/**
	 * Finds the pre-divergent points in terms of pre-divergent statements and populates the given map.
	 *
	 * @param method2preDivPoints maps a method to the set of pre-divergent statements in it. This is an out parameter.
	 * @pre method2preDivPoints != null
	 */
	private void findPreDivPoints(final Map<SootMethod, Collection<Stmt>> method2preDivPoints) {
		// Pass 1: Calculate pre-divergence points
		// Pass 1.1: Calculate intraprocedural pre-divergence points
		final Collection<SootMethod> _temp = findIntraproceduralPreDivPoints(method2preDivPoints);

		// Pass 1.2: In case of interprocedural analysis, filter out call-sites which do not lead to pre-divergent methods.
		if (considerCallSites) {
			final IWorkBag<SootMethod> _preDivMethods = new HistoryAwareLIFOWorkBag<SootMethod>(new HashSet<SootMethod>());
			_preDivMethods.addAllWork(_temp);

			while (_preDivMethods.hasWork()) {
				final SootMethod _callee = _preDivMethods.getWork();

				for (final Iterator<CallTriple> _j = callgraph.getCallers(_callee).iterator(); _j.hasNext();) {
					final CallTriple _ctrp = _j.next();
					final SootMethod _caller = _ctrp.getMethod();
					final Collection<Stmt> _c = MapUtils.getCollectionFromMap(method2preDivPoints, _caller);
					final Collection<Stmt> _d = MapUtils.getCollectionFromMap(method2interProcDivPoints, _caller);
					final Stmt _stmt = _ctrp.getStmt();
					_c.add(_stmt);
					_d.add(_stmt);
					_preDivMethods.addWork(_caller);
				}
			}
		}
	}

	/**
	 * Retrieves the valid successors of <code>bb</code> occurring in <code>bbg</code> of <code>method</code> w.r.t to
	 * <code>divPoint</code>. Given divergence point is not an interprocedural divergence point, then the only the
	 * successors of <code>bb</code> that do not occur in the SCC of <code>bb</code> in <code>bbg</code> are considered
	 * valid successors.
	 *
	 * @param divPoint of interest.
	 * @param bb in which <code>divPoint</code> occurs.
	 * @param bbg in which <code>bb</code> occurs.
	 * @param method in which <code>divPoint</code> occurs and <code>bbg</code> corresponds to.
	 * @return a collection of successor basic blocks.
	 * @pre divPoint != null and bb != null and bbg != null and method != null
	 * @post result != null
	 */
	private Collection<BasicBlock> getValidSuccs(final Stmt divPoint, final BasicBlock bb, final BasicBlockGraph bbg,
			final SootMethod method) {
		final Collection<BasicBlock> _result = new HashSet<BasicBlock>(directionSensInfo.getFollowersOfBB(bb));

		if (!MapUtils.getEmptyCollectionFromMap(method2interProcDivPoints, method).contains(divPoint)) {
			final List<List<BasicBlock>> _sccs = bbg.getSCCs(true);

			for (final Iterator<List<BasicBlock>> _i = _sccs.iterator(); _i.hasNext();) {
				final List _scc = _i.next();

				if (_scc.contains(bb)) {
					_result.removeAll(_scc);
					break;
				}
			}
		}
		return _result;
	}

	/**
	 * Records dependence information across basic blocks while picking up basic blocks for further processing.
	 *
	 * @param method in which the dependence occurs.
	 * @param preDivPoints are the pre-divergent basic blocks.
	 * @param dependees are the dependees involved in depedence.
	 * @param dependents are the dependents involved in depedence.
	 * @param succs are the successor basic blocks whose leader statement should also be picked up as dependents.
	 * @return a collection of basic blocks whose dependence information changed.
	 * @pre method != null and preDivPoints != null and dependees != null and dependents != null and succs != null
	 * @post result != null
	 * @post succs.containsAll(result)
	 */
	private Collection<BasicBlock> recordDepAcrossBB(final SootMethod method, final Collection<Stmt> preDivPoints,
			final Collection<Stmt> dependees, final Collection<Stmt> dependents, final Collection<BasicBlock> succs) {
		final Collection<BasicBlock> _result = new ArrayList<BasicBlock>();

		for (final Iterator<BasicBlock> _j = succs.iterator(); _j.hasNext();) {
			final BasicBlock _succ = _j.next();
			final Stmt _firstStmt = directionSensInfo.getFirstStmtInBB(_succ);
			dependents.add(_firstStmt);

			if (!preDivPoints.contains(_firstStmt)) {
				_result.add(_succ);
			}
		}
		recordDependenceInfoInBB(dependees, method, dependents);
		return _result;
	}

	/**
	 * Records dependence information.
	 *
	 * @param dependees of course.
	 * @param method in which the dependence occurs.
	 * @param dependents of course.
	 * @pre dependees != null
	 * @pre dependents != null
	 * @pre method != null
	 */
	private void recordDependenceInfoInBB(final Collection<Stmt> dependees, final SootMethod method,
			final Collection<Stmt> dependents) {
		final Map<Stmt, Collection<Stmt>> _de = dependent2dependee.get(method);

		for (final Iterator<Stmt> _i = dependents.iterator(); _i.hasNext();) {
			final Stmt _dependent = _i.next();
			final Collection<Stmt> _dees = MapUtils.getCollectionFromMap(_de, _dependent);
			_dees.addAll(dependees);
		}

		final Map<Stmt, Collection<Stmt>> _dt = MapUtils.getMapFromMap(dependee2dependent, method);

		for (final Iterator<Stmt> _i = dependees.iterator(); _i.hasNext();) {
			final Stmt _dependee = _i.next();
			final Collection<Stmt> _dents = MapUtils.getCollectionFromMap(_dt, _dependee);
			_dents.addAll(dependents);
		}
	}
}

// End of File
