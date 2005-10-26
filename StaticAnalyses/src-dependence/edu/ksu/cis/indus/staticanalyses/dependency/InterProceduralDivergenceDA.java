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

import edu.ksu.cis.indus.common.collections.ListUtils;
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
 * This class provides pure inter-procedural divergence dependency information. This implementation refers to the technical
 * report <a href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal tudy of Slicing for Multi-threaded
 * Program with JVM Concurrency Primitives"</a>.
 * <p>
 * The information from <code>NonTerminationSensitiveEntryControlDA</code> or an appropriately configured
 * <code>ExitControlDA</code> instance along with that from an instance of this class can be combined to obtain a intra- and
 * inter-procedural divergence dependence information.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class InterProceduralDivergenceDA
		extends
		AbstractDependencyAnalysis<Stmt, SootMethod, Stmt, SootMethod, Map<Stmt, Collection<Stmt>>, Stmt, SootMethod, Stmt, SootMethod, List<Collection<Stmt>>> {

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
	private static final Logger LOGGER = LoggerFactory.getLogger(InterProceduralDivergenceDA.class);

	/**
	 * This provides the call graph information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * This provides direction-sensitive information to make the analysis direction sensitive.
	 */
	private final IDirectionSensitiveInfo directionSensInfo;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param directionSensitiveInfo that controls the direction.
	 * @param direction of the analysis
	 * @pre info != null and direction != null
	 */
	private InterProceduralDivergenceDA(final IDirectionSensitiveInfo directionSensitiveInfo, final Direction direction) {
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
	 * @pre direction.equals(IDependencyAnalysis.FORWARD_DIRECTION) or
	 *      direction.equals(IDependencyAnalysis.BACKWARD_DIRECTION)
	 */
	public static InterProceduralDivergenceDA getDivergenceDA(final Direction direction) {
		final InterProceduralDivergenceDA _result;

		if (direction.equals(Direction.FORWARD_DIRECTION)) {
			_result = new InterProceduralDivergenceDA(new ForwardDirectionSensitiveInfo(), direction);
		} else if (direction.equals(Direction.BACKWARD_DIRECTION)) {
			_result = new InterProceduralDivergenceDA(new BackwardDirectionSensitiveInfo(), direction);
		} else {
			final String _msg = "Argument should be either 'IDependencyAnalysis.FORWARD_DIRECTION' or "
					+ "'IDependencyAnalysis.BACKWARD_DIRECTION'. Provided argument was " + direction.toString();
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

		findDivergencePoints(_method2preDivPoints);

		for (final Iterator<Map.Entry<SootMethod, Collection<Stmt>>> _i = _method2preDivPoints.entrySet().iterator(); _i
				.hasNext();) {
			final Map.Entry<SootMethod, Collection<Stmt>> _entry = _i.next();
			final SootMethod _method = _entry.getKey();
			final Collection<Stmt> _divergentStmts = _entry.getValue();
			final Collection<BasicBlock> _succsOfDivergentBBs = calculateIntraBBDependence(_method, _divergentStmts);
			calculateInterBBDependence(_method, _succsOfDivergentBBs, _divergentStmts);
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
		Collection<Stmt> _result = Collections.emptyList();
		final List<Collection<Stmt>> _list = dependent2dependee.get(method);

		if (_list != null) {
			final int _indexOf = getStmtList(method).indexOf(dependentStmt);

			if (_indexOf > -1) {
				_result = ListUtils.getAtIndexFromList(_list, _indexOf, Collections.<Stmt> emptyList());
			}
		}

		return _result;
	}

	/**
	 * Returns the statements which depend on the given statement in the given method.
	 * 
	 * @param dependeeStmt is the statement for which dependents are requested.
	 * @param method in which <code>dependeeStmt</code> occurs.
	 * @return a collection of statements.
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependentStmt.isOclKindOf(Stmt)
	 * @post result->forall( o | o.isOclKindOf(Stmt))
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependents(java.lang.Object,
	 *      java.lang.Object)
	 */
	public Collection<Stmt> getDependents(final Stmt dependeeStmt, final SootMethod method) {
		final Map<Stmt, Collection<Stmt>> _stmt2List = MapUtils.getFromMap(dependee2dependent, method, Collections
				.<Stmt, Collection<Stmt>> emptyMap());
		final Collection<Stmt> _result = MapUtils.getFromMap(_stmt2List, dependeeStmt, Collections.<Stmt> emptyList());
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection<Comparable> getIds() {
		return Collections.singleton(IDependencyAnalysis.DIVERGENCE_DA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	@Override public void reset() {
		super.reset();
	}

	// /CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis. The representation includes the results of the analysis.
	 * 
	 * @return a stringized representation of this object.
	 */
	@Override public String toString() {
		final StringBuffer _result = new StringBuffer("Statistics for divergence dependence as calculated by "
				+ getClass().getName() + "[" + hashCode() + "]\n");

		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependent2dependee.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final SootMethod _method = (SootMethod) _entry.getKey();
			_localEdgeCount = 0;

			final List _stmts = getStmtList(_method);
			final List _dependees = (List) _entry.getValue();

			for (int _j = 0; _j < _stmts.size(); _j++) {
				final Collection _c = (Collection) _dependees.get(_j);

				if (_c != null) {
					_temp.append("\t\t" + _stmts.get(_j) + " --> " + _c + "\n");
					_localEdgeCount += _c.size();
				}
			}

			_result.append("\tFor " + _method + " there are " + _localEdgeCount + " divergence dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}

		_result.append("A total of " + _edgeCount + " divergence dependence edges exist.");

		return _result.toString();
	}

	// /CLOVER:ON

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependenceRetriever()
	 */
	@Override protected StmtRetriever getDependenceRetriever() {
		return new StmtRetriever();
	}

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
	 * @param succsOfDivergentBBs are the successor basic blocks of divergent basic blocks.
	 * @param divergentStmts is the basic blocks which are the pre-divergent points.
	 * @pre method != null and succsOfDivergentBBs != null and divergentStmts != null
	 * @pre succsOfDivergentBBs.oclIsKindOf(Collection(BasicBlock))
	 * @pre divergentStmts.oclIsKindOf(Collection(Stmt))
	 * @pre succsOfDivergentBBs->forall(o | divergentStmts->exists(p | o.getPredsOf().contains(p)))
	 */
	private void calculateInterBBDependence(final SootMethod method, final Collection<BasicBlock> succsOfDivergentBBs,
			final Collection<Stmt> divergentStmts) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Processing method " + method + " with divergent blocks: " + succsOfDivergentBBs);
		}

		final List<Stmt> _sl = getStmtList(method);
		final IWorkBag<BasicBlock> _wb = new FIFOWorkBag<BasicBlock>();
		final Collection<Stmt> _dents = new HashSet<Stmt>();
		_wb.addAllWork(succsOfDivergentBBs);

		while (_wb.hasWork()) {
			final BasicBlock _bb = _wb.getWork();
			final Stmt _firstStmt = directionSensInfo.getFirstStmtInBB(_bb);
			final int _firstStmtIndex = _sl.indexOf(_firstStmt);
			final Collection<Stmt> _dees = dependent2dependee.get(method).get(_firstStmtIndex);
			_dents.clear();

			if (!divergentStmts.contains(_firstStmt)) {
				final Collection _bbStmts = directionSensInfo.getIntraBBDependents(_bb, _firstStmt);

				for (final Iterator _i = _bbStmts.iterator(); _i.hasNext();) {
					final Stmt _stmt = (Stmt) _i.next();
					_dents.add(_stmt);

					if (divergentStmts.contains(_stmt)) {
						break;
					}
				}
			}

			if (!succsOfDivergentBBs.contains(_bb)) {
				final Collection<BasicBlock> _succs = recordDepAcrossBB(method, divergentStmts, _dees, _dents,
						directionSensInfo.getFollowersOfBB(_bb));
				_wb.addAllWorkNoDuplicates(_succs);
			} else {
				recordDependenceInfoInBB(_dees, method, _dents);
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
	 * @param divergencePoints are the basic blocks that contain divergent points.
	 * @return a collection of basic blocks that follow the blocks in <code>divergencePoints</code>.
	 * @pre method != null and divergencePoints != null
	 * @pre divergencePoints.oclIsKindOf(Collection(BasicBlock))
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
	 * @post result->forall(o | divergencePoints->exists(p | o.getPredsOf().contains(p)))
	 */
	private Collection<BasicBlock> calculateIntraBBDependence(final SootMethod method, final Collection<Stmt> divergencePoints) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Processing method " + method + " with divergent points: " + divergencePoints);
		}

		final Collection<BasicBlock> _result = new HashSet<BasicBlock>();
		final List<Stmt> _sl = getStmtList(method);
		final Collection<Stmt> _dependents = new HashSet<Stmt>();
		final BasicBlockGraph _bbg = getBasicBlockGraph(method);
		final List<Collection<Stmt>> _dees = MapUtils.getFromMapUsingFactory(dependent2dependee, method, ListUtils
				.<Collection<Stmt>> getFactory());
		ListUtils.ensureSize(_dees, _sl.size(), null);

		for (final Iterator<Stmt> _i = divergencePoints.iterator(); _i.hasNext();) {
			Stmt _divPoint = _i.next();
			final BasicBlock _bb = _bbg.getEnclosingBlock(_divPoint);

			for (final Iterator _j = _bb.getStmtsOf().iterator(); _j.hasNext();) {
				_divPoint = (Stmt) _j.next();

				if (divergencePoints.contains(_divPoint)) {
					break;
				}
			}
			_dependents.clear();

			final Collection<Stmt> _bbStmts = directionSensInfo.getIntraBBDependents(_bb, _divPoint);

			for (final Iterator<Stmt> _j = _bbStmts.iterator(); _j.hasNext();) {
				final Stmt _stmt = _j.next();
				_dependents.add(_stmt);

				if (divergencePoints.contains(_stmt)) {
					recordDependenceInfoInBB(Collections.singleton(_divPoint), method, _dependents);
					_divPoint = _stmt;
					_dependents.clear();
				}
			}

			final Collection<BasicBlock> _temp = recordDepAcrossBB(method, divergencePoints,
					Collections.singleton(_divPoint), _dependents, _bb.getSuccsOf());
			_result.addAll(_temp);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Processing method " + method);
		}

		return _result;
	}

	/**
	 * Finds the divergence points in terms of divergent invocation statements and populates the given map.
	 * 
	 * @param method2divPoints maps a method to the set of divergent statements in it. This is an out parameter.
	 * @pre method2divPoints != null
	 * @post method2preDivPoints.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private void findDivergencePoints(final Map<SootMethod, Collection<Stmt>> method2divPoints) {
		// Pass 1.1: find divergence methods
		final Collection<SootMethod> _temp = findDivergentMethods();

		// Pass 1.2: In case of interprocedural analysis, filter out call-sites which do not lead to pre-divergent methods.
		final IWorkBag<SootMethod> _divMethods = new HistoryAwareLIFOWorkBag<SootMethod>(new HashSet<SootMethod>());
		_divMethods.addAllWork(_temp);

		while (_divMethods.hasWork()) {
			final SootMethod _callee = _divMethods.getWork();

			for (final Iterator<CallTriple> _j = callgraph.getCallers(_callee).iterator(); _j.hasNext();) {
				final CallTriple _ctrp = _j.next();
				final SootMethod _caller = _ctrp.getMethod();
				final Collection<Stmt> _c = MapUtils.getFromMapUsingFactory(method2divPoints, _caller, ListUtils
						.<Stmt> getFactory());
				final Stmt _stmt = _ctrp.getStmt();
				_c.add(_stmt);
				_divMethods.addWork(_caller);
			}
		}
	}

	/**
	 * Finds methods that contain divergent/looping code.
	 * 
	 * @return the collection of divergent methods.
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	private Collection<SootMethod> findDivergentMethods() {
		final Collection<SootMethod> _result = new ArrayList<SootMethod>();

		for (final Iterator _i = callgraph.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _method = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = getBasicBlockGraph(_method);
			final Collection _sccs = _bbg.getSCCs(true);
			boolean _doesNotContainLoops = true;

			for (final Iterator _j = _sccs.iterator(); _j.hasNext() && _doesNotContainLoops;) {
				final Collection _scc = (Collection) _j.next();
				final BasicBlock _bb = (BasicBlock) _scc.iterator().next();
				_doesNotContainLoops = _scc.size() == 1 && !_bb.getSuccsOf().contains(_bb);
			}

			if (!_doesNotContainLoops) {
				_result.add(_method);
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
	 * @pre preDivPoints.oclIsKindOf(Collection(BasicBlock))
	 * @pre dependees.oclIsKindOf(Collection(Stmt))
	 * @pre dependents.oclIsKindOf(Collection(Stmt))
	 * @pre succs.oclIsKindOf(Collection(BasicBlock))
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
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
	 * @pre dependees != null and dependees.oclIsKindOf(Collection(Stmt))
	 * @pre dependents != null and dependents.oclIsKindOf(Collection(Stmt))
	 * @pre method != null
	 */
	private void recordDependenceInfoInBB(final Collection<Stmt> dependees, final SootMethod method,
			final Collection<Stmt> dependents) {
		final List<Collection<Stmt>> _de = dependent2dependee.get(method);
		final List _sl = getStmtList(method);

		for (final Iterator<Stmt> _i = dependents.iterator(); _i.hasNext();) {
			final Stmt _dependent = _i.next();
			final Collection<Stmt> _dees = ListUtils.getAtIndexFromListUsingFactory(_de, _sl.indexOf(_dependent), ListUtils
					.<Stmt> getFactory());
			_dees.addAll(dependees);
		}

		final Map<Stmt, Collection<Stmt>> _stmt2List = MapUtils.getFromMapUsingFactory(dependee2dependent, method, MapUtils
				.<Stmt, Collection<Stmt>> getFactory());

		for (final Iterator<Stmt> _i = dependees.iterator(); _i.hasNext();) {
			final Stmt _dependee = _i.next();
			final Collection<Stmt> _dents = MapUtils.getFromMapUsingFactory(_stmt2List, _dependee, ListUtils
					.<Stmt> getFactory());
			_dents.addAll(dependents);
		}
	}
}

// End of File
