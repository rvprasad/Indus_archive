
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

import edu.ksu.cis.indus.common.CollectionsUtilities;
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

import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class provides divergence dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.
 * 
 * <p>
 * This implementation by default does not consider call-sites for dependency calculation.
 * </p>
 * 
 * <p>
 * This implementation does not capture intraprocedural dependence within loops.  Hence, if there is a loop inside a loop,
 * then the statements in the outer loop are not flagged as being dependent on the inner loop.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependee2dependent.oclIsKindOf(Map(SootMethod, Map(Stmt, Collection(Stmt))))
 * @invariant dependee2dependent.values()->forall(o | o.getValue().size = o.getKey().getActiveBody().getUnits().size())
 * @invariant dependent2dependee.oclIsKindOf(Map(SootMethod, Sequence(Collection(Stmt))))
 * @invariant dependent2dependee.values()->forall(o | o.getValue().size = o.getKey().getActiveBody().getUnits().size())
 */
public final class DivergenceDA
  extends AbstractDependencyAnalysis {
	/*
	 * The dependence information is stored as follows: For each method, a sequence of collection of statements is maintained.
	 * The length of the sequence is equal to the number of statements in the method.  The statement collection at a location
	 * in this sequence corresponds to statements related via dependency to the statement at the same location in the
	 * statement list of the method. The collection is a singleton in case of dependee information.
	 */
	/*
	 * This implementation does not capture intraprocedural dependence within loops.  Hence, if there is a loop inside a loop,
	 * then the statements in the outer loop are not flagged as being dependent on the inner loop. This can be remedied by
	 *  - improving the precision of pre-divergence point identification.
	 *  - using loop information in getValidSuccs() to control which successors should be considered for dependence.
	 */

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DivergenceDA.class);

	/** 
	 * This provides the call graph information.
	 */
	private ICallGraphInfo callgraph;

	/** 
	 * This provides direction-sensitive information to make the analysis direction sensitive.
	 */
	private final IDirectionSensitiveInfo directionSensInfo;

	/** 
	 * This maps methods to the inter-procedural divergence points they contain.
	 *
	 * @invariant method2interProcDivPoints.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 * @invariant method2interProcDivPoints.values()->forall(o | o->forall(p | p.containsInvokeExpr()))
	 */
	private final Map method2interProcDivPoints = new HashMap();

	/** 
	 * The direction of the analysis.
	 */
	private final Object theDirection;

	/** 
	 * This indicates if call-sites that invoke methods containing pre-divergence points should be considered as
	 * pre-divergence points.
	 */
	private boolean considerCallSites;

	/**
	 * Creates an instance of this class.
	 *
	 * @param directionSensitiveInfo that controls the direction.
	 * @param direction of the analysis
	 *
	 * @pre info != null and direction != null
	 */
	private DivergenceDA(final IDirectionSensitiveInfo directionSensitiveInfo, final Object direction) {
		directionSensInfo = directionSensitiveInfo;
		theDirection = direction;
	}

	/**
	 * Retrieves an instance of divergence dependence analysis that calculates information in backward direction.
	 *
	 * @return an instance of divergence dependence.
	 *
	 * @post result != null
	 */
	public static DivergenceDA getBackwardDivergenceDA() {
		return new DivergenceDA(new BackwardDirectionSensitiveInfo(), BACKWARD_DIRECTION);
	}

	/**
	 * Sets if the analyses should consider the effects of method calls.  This method may change the preprocessing
	 * requirements of this analysis.  Hence, it should be called
	 *
	 * @param consider <code>true</code> indicates call-sites that invoke methods containing pre-divergence points should be
	 * 		  considered as pre-divergence points; <code>false</code>, otherwise.
	 */
	public void setConsiderCallSites(final boolean consider) {
		considerCallSites = consider;
	}

	/**
	 * Returns the statements on which the given statement depends on in the given method.
	 *
	 * @param dependentStmt is the statement for which dependees are requested.
	 * @param method in which <code>dependentStmt</code> occurs.
	 *
	 * @return a collection of statements.  However, in this case the collection contains only one statement as the .
	 *
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependentStmt.isOclKindOf(Stmt)
	 * @post result->forall( o | o.isOclKindOf(Stmt))
	 * @post result.size == 1
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependees(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependees(final Object dependentStmt, final Object method) {
		Collection _result = Collections.EMPTY_LIST;
		final List _list = (List) dependent2dependee.get(method);

		if (_list != null) {
			_result =
				(Collection) CollectionsUtilities.getAtIndexFromList(_list,
					getStmtList((SootMethod) method).indexOf(dependentStmt), CollectionsUtilities.EMPTY_LIST_FACTORY);
		}

		return _result;
	}

	/**
	 * Returns the statements which depend on the given statement in the given method.
	 *
	 * @param dependeeStmt is the statement for which dependents are requested.
	 * @param method in which <code>dependeeStmt</code> occurs.
	 *
	 * @return a collection of statements.
	 *
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @pre dependentStmt.isOclKindOf(Stmt)
	 * @post result->forall( o | o.isOclKindOf(Stmt))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getDependents(java.lang.Object,
	 * 		java.lang.Object)
	 */
	public Collection getDependents(final Object dependeeStmt, final Object method) {
		final Map _stmt2List = (Map) MapUtils.getObject(dependee2dependent, method, Collections.EMPTY_MAP);
		final Collection _result = (Collection) MapUtils.getObject(_stmt2List, dependeeStmt, Collections.EMPTY_LIST);
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * Retrieves an instance of divergence dependence analysis that calculates information in forward direction.
	 *
	 * @return an instance of divergence dependence.
	 *
	 * @post result != null
	 */
	public static DivergenceDA getForwardDivergenceDA() {
		return new DivergenceDA(new ForwardDirectionSensitiveInfo(), FORWARD_DIRECTION);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getDirection()
	 */
	public Object getDirection() {
		return theDirection;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IDependencyAnalysis.DIVERGENCE_DA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis getIndirectVersionOfDependence() {
		return new IndirectDependenceAnalysis(this, IDependenceRetriever.STMT_DEP_RETRIEVER);
	}

	/**
	 * Calculates the divergence dependency in the methods.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Divergence Dependence processing");
		}

		final Map _method2preDivPoints = new HashMap();

		findPreDivPoints(_method2preDivPoints);

		for (final Iterator _i = _method2preDivPoints.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			final SootMethod _method = (SootMethod) _entry.getKey();
			final Collection _preDivPoints = (Collection) _entry.getValue();
			final Collection _succsOfPreDivBBs = calculateIntraBBDependence(_method, _preDivPoints);
			calculateInterBBDependence(_method, _succsOfPreDivBBs, _preDivPoints);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Divergence Dependence processing");
		}
		stable();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		method2interProcDivPoints.clear();
	}

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for divergence dependence as calculated by " + getClass().getName() + "["
				+ hashCode() + "]\n");
		_result.append("The analyses setup was : \n \tinterprocedural: " + considerCallSites + "\n");

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

	///CLOVER:ON

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 *
	 * @pre info.get(ICallGraphInfo.ID) != null and    info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();
		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
		}
	}

	/**
	 * Retrieves the valid successors of <code>bb</code> occurring in <code>bbg</code> of <code>method</code> w.r.t to
	 * <code>divPoint</code>. Given divergence point is not an interprocedural divergence point, then the only the
	 * successors of <code>bb</code> that do not occur in the SCC of  <code>bb</code> in <code>bbg</code> are considered
	 * valid successors.
	 *
	 * @param divPoint of interest.
	 * @param bb in which <code>divPoint</code> occurs.
	 * @param bbg in which <code>bb</code> occurs.
	 * @param method in which <code>divPoint</code> occurs and <code>bbg</code> corresponds to.
	 *
	 * @return a collection of successor basic blocks.
	 *
	 * @pre divPoint != null and bb != null and bbg != null and method != null
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
	 */
	private Collection getValidSuccs(final Stmt divPoint, final BasicBlock bb, final BasicBlockGraph bbg,
		final SootMethod method) {
		final Collection _result = new HashSet(directionSensInfo.getFollowersOfBB(bb));

		if (!((Collection) CollectionsUtilities.getFromMap(method2interProcDivPoints, method,
				  CollectionsUtilities.EMPTY_LIST_FACTORY)).contains(divPoint)) {
			final Collection _sccs = bbg.getSCCs(true);

			for (final Iterator _i = _sccs.iterator(); _i.hasNext();) {
				final Collection _scc = (Collection) _i.next();

				if (_scc.contains(bb)) {
					_result.removeAll(_scc);
					break;
				}
			}
		}
		return _result;
	}

	/**
	 * Calculates inter-basic block divergence dependence.
	 *
	 * @param method in which the basic blocks occur.
	 * @param succsOfPreDivPoints are the successor basic blocks of pre-divergent basic blocks.
	 * @param preDivPoints is the basic blocks which are the pre-divergent points.
	 *
	 * @pre method != null and succsOfPreDivPoints != null and preDivPoints != null
	 * @pre succsOfPreDivPoints.oclIsKindOf(Collection(BasicBlock))
	 * @pre preDivPoints.oclIsKindOf(Collection(BasicBlock))
	 * @pre succsOfPreDivPoints->forall(o | preDivPoints->exists(p | o.getPredsOf().contains(p)))
	 */
	private void calculateInterBBDependence(final SootMethod method, final Collection succsOfPreDivPoints,
		final Collection preDivPoints) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Processing method " + method + " with divergent blocks: " + succsOfPreDivPoints);
		}

		final List _sl = getStmtList(method);
		final IWorkBag _wb = new FIFOWorkBag();
		final Collection _dependents = new HashSet();
		_wb.addAllWork(succsOfPreDivPoints);

		while (_wb.hasWork()) {
			final BasicBlock _bb = (BasicBlock) _wb.getWork();
			final Stmt _firstStmt = directionSensInfo.getFirstStmtInBB(_bb);
			final int _firstStmtIndex = _sl.indexOf(_firstStmt);
			final Collection _dependees = (Collection) ((List) dependent2dependee.get(method)).get(_firstStmtIndex);
			_dependents.clear();

			if (!preDivPoints.contains(_firstStmt)) {
				final List _bbStmts = directionSensInfo.getIntraBBDependents(_bb, _firstStmt);

				for (final Iterator _i = _bbStmts.iterator(); _i.hasNext();) {
					final Stmt _stmt = (Stmt) _i.next();
					_dependents.add(_stmt);

					if (preDivPoints.contains(_stmt)) {
						break;
					}
				}
			}

			if ((!succsOfPreDivPoints.contains(_bb))) {
				final Collection _succs =
					recordDepAcrossBB(method, preDivPoints, _dependees, _dependents, directionSensInfo.getFollowersOfBB(_bb));
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
	 *
	 * @return a collection of basic blocks that follow the blocks in <code>preDivPoints</code>.
	 *
	 * @pre method != null and  preDivPoints != null
	 * @pre preDivPoints.oclIsKindOf(Collection(BasicBlock))
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
	 * @post result->forall(o | preDivPoints->exists(p | o.getPredsOf().contains(p)))
	 */
	private Collection calculateIntraBBDependence(final SootMethod method, final Collection preDivPoints) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Processing method " + method + " with divergent points: " + preDivPoints);
		}

		final Collection _result = new HashSet();
		final List _sl = getStmtList(method);
		final Collection _dependents = new HashSet();
		final BasicBlockGraph _bbg = getBasicBlockGraph(method);
		final List _dees = CollectionsUtilities.getListFromMap(dependent2dependee, method);
		CollectionsUtilities.ensureSize(_dees, _sl.size(), null);

		for (final Iterator _i = preDivPoints.iterator(); _i.hasNext();) {
			Stmt _divPoint = (Stmt) _i.next();
			final BasicBlock _bb = _bbg.getEnclosingBlock(_divPoint);

			for (final Iterator _j = _bb.getStmtsOf().iterator(); _j.hasNext();) {
				_divPoint = (Stmt) _j.next();

				if (preDivPoints.contains(_divPoint)) {
					break;
				}
			}
			_dependents.clear();

			final List _bbStmts = directionSensInfo.getIntraBBDependents(_bb, _divPoint);

			for (final Iterator _j = _bbStmts.iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();
				_dependents.add(_stmt);

				if (preDivPoints.contains(_stmt)) {
					recordDependenceInfoInBB(Collections.singleton(_divPoint), method, _dependents);
					_divPoint = _stmt;
					_dependents.clear();
				}
			}

			final Collection _validSuccs = getValidSuccs(_divPoint, _bb, _bbg, method);
			final Collection _temp =
				recordDepAcrossBB(method, preDivPoints, Collections.singleton(_divPoint), _dependents, _validSuccs);
			_result.addAll(_temp);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Processing method " + method);
		}

		return _result;
	}

	/**
	 * Finds the pre-divergent points in terms of pre-divergent statements and populates the given map.  It also captures the
	 * methods in which pre-divergent points occur.
	 *
	 * @param method2preDivPoints maps methods (of interest) to a set of pre-divergent points. This is an out parameter.
	 *
	 * @return the collection of pre-divergent methods.
	 *
	 * @pre method2preDivPoints != null
	 * @post method2preDivPoints.oclIsKindOf(Map(SootMethod, Collection))
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 * @post result.keySet()->forall(o | method2preDivPoints.get(o) != null &&
	 * 		 method2preDivPoints.get(o).oclIsKindOf(Collection(Stmt)))
	 */
	private Collection findIntraproceduralPreDivPoints(final Map method2preDivPoints) {
		final Collection _preDivPoints = new HashSet();
		final Collection _temp;

		if (considerCallSites) {
			_temp = new HashSet();
		} else {
			_temp = null;
		}

		for (final Iterator _i = callgraph.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _method = (SootMethod) _i.next();
			final BasicBlockGraph _bbg = getBasicBlockGraph(_method);
			final Collection _sccs = _bbg.getSCCs(true);

			for (final Iterator _j = _sccs.iterator(); _j.hasNext();) {
				final Collection _scc = (Collection) _j.next();

				if (_scc.size() > 1) {
					for (final Iterator _k = _scc.iterator(); _k.hasNext();) {
						final BasicBlock _bb = (BasicBlock) _k.next();

						if (!_scc.containsAll(_bb.getSuccsOf())) {
							_preDivPoints.add(_bb.getTrailerStmt());
						}
					}
				} else {
					final BasicBlock _bb = (BasicBlock) _scc.iterator().next();
					final Collection _succs = _bb.getSuccsOf();

					if (_succs.size() > 1 && _succs.contains(_bb)) {
						_preDivPoints.add(_bb.getTrailerStmt());
					}
				}
			}

			if (!_preDivPoints.isEmpty()) {
				method2preDivPoints.put(_method, new ArrayList(_preDivPoints));

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
	 * @param method2preDivPoints maps a method to the set of pre-divergent statements in it.  This is an out parameter.
	 *
	 * @pre method2preDivPoints != null
	 * @post method2preDivPoints.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 */
	private void findPreDivPoints(final Map method2preDivPoints) {
		// Pass 1: Calculate pre-divergence points
		// Pass 1.1: Calculate intraprocedural pre-divergence points
		final Collection _temp = findIntraproceduralPreDivPoints(method2preDivPoints);

		// Pass 1.2: In case of interprocedural analysis, filter out call-sites which do not lead to pre-divergent methods.
		if (considerCallSites) {
			final IWorkBag _preDivMethods = new HistoryAwareLIFOWorkBag(new HashSet());
			_preDivMethods.addAllWork(_temp);

			while (_preDivMethods.hasWork()) {
				final SootMethod _callee = (SootMethod) _preDivMethods.getWork();

				for (final Iterator _j = callgraph.getCallers(_callee).iterator(); _j.hasNext();) {
					final CallTriple _ctrp = (CallTriple) _j.next();
					final SootMethod _caller = _ctrp.getMethod();
					final Collection _c = CollectionsUtilities.getListFromMap(method2preDivPoints, _caller);
					final Collection _d = CollectionsUtilities.getListFromMap(method2interProcDivPoints, _caller);
					final Stmt _stmt = _ctrp.getStmt();
					_c.add(_stmt);
					_d.add(_stmt);
					_preDivMethods.addWork(_caller);
				}
			}
		}
	}

	/**
	 * Records dependence information across basic blocks while picking up basic blocks for further processing.
	 *
	 * @param method in which the dependence occurs.
	 * @param preDivPoints are the pre-divergent basic blocks.
	 * @param dependees are the dependees involved in depedence.
	 * @param dependents are the dependents involved in depedence.
	 * @param succs are the successor basic blocks whose leader statement should also be picked up as dependents.
	 *
	 * @return a collection of basic blocks whose dependence information changed.
	 *
	 * @pre method != null and preDivPoints != null and dependees != null and dependents != null and succs != null
	 * @pre preDivPoints.oclIsKindOf(Collection(BasicBlock))
	 * @pre dependees.oclIsKindOf(Collection(Stmt))
	 * @pre dependents.oclIsKindOf(Collection(Stmt))
	 * @pre succs.oclIsKindOf(Collection(BasicBlock))
	 * @post result != null and result.oclIsKindOf(Collection(BasicBlock))
	 * @post succs.containsAll(result)
	 */
	private Collection recordDepAcrossBB(final SootMethod method, final Collection preDivPoints, final Collection dependees,
		final Collection dependents, final Collection succs) {
		final Collection _result = new ArrayList();

		for (final Iterator _j = succs.iterator(); _j.hasNext();) {
			final BasicBlock _succ = (BasicBlock) _j.next();
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
	 *
	 * @pre dependees != null and dependees.oclIsKindOf(Collection(Stmt))
	 * @pre dependents != null and dependents.oclIsKindOf(Collection(Stmt))
	 * @pre method != null
	 */
	private void recordDependenceInfoInBB(final Collection dependees, final SootMethod method, final Collection dependents) {
		final List _de = (List) dependent2dependee.get(method);
		final List _sl = getStmtList(method);

		for (final Iterator _i = dependents.iterator(); _i.hasNext();) {
			final Stmt _dependent = (Stmt) _i.next();
			final Collection _dees = (Collection) CollectionsUtilities.getListAtIndexFromList(_de, _sl.indexOf(_dependent));
			_dees.addAll(dependees);
		}

		final Map _stmt2List = CollectionsUtilities.getMapFromMap(dependee2dependent, method);

		for (final Iterator _i = dependees.iterator(); _i.hasNext();) {
			final Stmt _dependee = (Stmt) _i.next();
			final Collection _dents = CollectionsUtilities.getListFromMap(_stmt2List, _dependee);
			_dents.addAll(dependents);
		}
	}
}

// End of File
