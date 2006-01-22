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

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.indus.annotations.Experimental;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.LockAcquisitionBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv1;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.SootMethod;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.toolkits.graph.UnitGraph;

/**
 * This class calculates the dependence information that is more precise than it's parent class.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
@Experimental class DependenceAndMayFollowInfoCalculatorV2
		extends DependenceAndMayFollowInfoCalculator {

	/**
	 * DOCUMENT ME!
	 * <p>
	 * </p>
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	final class Info
			implements Cloneable {

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Collection<Pair<Stmt, SootMethod>> aref;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Stack<Pair<Stmt, SootMethod>> callstack;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Stmt currStmt;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Collection<Pair<Stmt, SootMethod>> fref;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Collection<Pair<Stmt, SootMethod>> lacq;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		SootMethod method;

		final SootMethod entryPoint;

		/**
		 * Creates an instance of this class.
		 * 
		 * @param sm DOCUMENT ME!
		 * @param stmt DOCUMENT ME!
		 */
		public Info(final SootMethod sm, final Stmt stmt, final Stack<Pair<Stmt, SootMethod>> callChain,
				final Collection<Pair<Stmt, SootMethod>> lockCol, final Collection<Pair<Stmt, SootMethod>> arefCol,
				final Collection<Pair<Stmt, SootMethod>> frefCol, final SootMethod entry) {
			method = sm;
			callstack = callChain;
			currStmt = stmt;
			lacq = lockCol;
			aref = arefCol;
			fref = frefCol;
			entryPoint = entry;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @return DOCUMENT ME!
		 */
		@Override public Info clone() {
			final Info _result;

			try {
				_result = (Info) super.clone();
				_result.aref = (Collection) ((HashSet<Pair<Stmt, SootMethod>>) aref).clone();
				_result.fref = (Collection) ((HashSet<Pair<Stmt, SootMethod>>) fref).clone();
				_result.lacq = (Collection) ((HashSet<Pair<Stmt, SootMethod>>) lacq).clone();
				_result.callstack = callstack.clone();
			} catch (final CloneNotSupportedException _e) {
				final IllegalStateException _r = new IllegalStateException();
				_r.initCause(_e);
				throw _r;
			}
			return _result;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	enum SuccessorType {

		/**
		 * DOCUMENT ME!
		 */
		EXCEPTIONAL_RETURN,
		/**
		 * DOCUMENT ME!
		 */
		INTRAPROCEDURAL,
		/**
		 * DOCUMENT ME!
		 */
		NORMAL_RETURN
	}

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final IStmtGraphFactory<?> stmtGraphFactory;

	/**
	 * DOCUMENT ME!
	 * 
	 * @param theTool DOCUMENT ME!
	 * @param ida DOCUMENT ME!
	 * @param lbe DOCUMENT ME!
	 * @param threadGraph DOCUMENT ME!
	 * @param callGraph DOCUMENT ME!
	 * @param cfgAnalysis DOCUMENT ME!
	 * @param graphFactory DOCUMENT ME!
	 * @see DependenceAndMayFollowInfoCalculator#DependenceAndMayFollowInfoCalculator(RelativeDependenceInfoTool,
	 *      InterferenceDAv1, LockAcquisitionBasedEquivalence, ICallGraphInfo, IThreadGraphInfo, CFGAnalysis)
	 */
	DependenceAndMayFollowInfoCalculatorV2(final RelativeDependenceInfoTool theTool, final InterferenceDAv1 ida,
			final LockAcquisitionBasedEquivalence lbe, final ICallGraphInfo callGraph, final IThreadGraphInfo threadGraph,
			final CFGAnalysis cfgAnalysis, final IStmtGraphFactory<?> graphFactory) {
		super(theTool, ida, lbe, callGraph, threadGraph, cfgAnalysis);
		stmtGraphFactory = graphFactory;
	}

	private final Map<SootMethod, Collection<Pair<Stmt, SootMethod>>> entryPoint2reachableARef = new HashMap<SootMethod, Collection<Pair<Stmt, SootMethod>>>();

	private final Map<SootMethod, Collection<Pair<Stmt, SootMethod>>> entryPoint2reachableFRef = new HashMap<SootMethod, Collection<Pair<Stmt, SootMethod>>>();

	private final Map<SootMethod, Collection<Pair<Stmt, SootMethod>>> entryPoint2reachableLock = new HashMap<SootMethod, Collection<Pair<Stmt, SootMethod>>>();

	/**
	 * @see edu.ksu.cis.indus.toolkits.bandera.DependenceAndMayFollowInfoCalculator#translateAndPopulateMayFollowRelation()
	 */
	@Override protected void translateAndPopulateMayFollowRelation() {
		final Iterator<SootMethod> _i = tgi.getThreadEntryPoints().iterator();
		final int _iEnd = tgi.getThreadEntryPoints().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _sm = _i.next();
			final IWorkBag<Info> _wb = new LIFOWorkBag<Info>();
			final Info _t = new Info(_sm, null, new Stack<Pair<Stmt, SootMethod>>(), new HashSet<Pair<Stmt, SootMethod>>(),
					new HashSet<Pair<Stmt, SootMethod>>(), new HashSet<Pair<Stmt, SootMethod>>(), _sm);
			_wb.addWork(_t);

			while (_wb.hasWork()) {
				final Info _info = _wb.getWork();

				if (handleRecursion(_info, _wb)) {
					continue;
				}

				final Stmt _stmt = _info.currStmt;

				if (_stmt == null) {
					_info.currStmt = (Stmt) stmtGraphFactory.getStmtGraph(_info.method).getBody().getUnits().getFirst();
					_wb.addWork(_info);
				} else if (_stmt instanceof EnterMonitorStmt) {
					final Pair<Stmt, SootMethod> _pair = new Pair<Stmt, SootMethod>(_stmt, _info.method);
					recordMayFollow(_pair, _info.lacq);
					processSuccs(_info, _pair, _wb, SuccessorType.INTRAPROCEDURAL);
				} else if (_stmt.containsArrayRef()) {
					final Pair<Stmt, SootMethod> _pair = new Pair<Stmt, SootMethod>(_stmt, _info.method);
					recordMayFollow(_pair, _info.aref);
					processSuccs(_info, _pair, _wb, SuccessorType.INTRAPROCEDURAL);
				} else if (_stmt.containsFieldRef()) {
					final Pair<Stmt, SootMethod> _pair = new Pair<Stmt, SootMethod>(_stmt, _info.method);
					recordMayFollow(_pair, _info.fref);
					processSuccs(_info, _pair, _wb, SuccessorType.INTRAPROCEDURAL);
				} else if (_stmt.containsInvokeExpr()) {
					final Pair<Stmt, SootMethod> _pair = new Pair<Stmt, SootMethod>(_stmt, _info.method);
					_info.lacq.add(_pair);
					_info.aref.add(_pair);
					_info.fref.add(_pair);
					_info.currStmt = null;

					final Context _context = new Context();
					_context.setStmt(_stmt);
					_context.setRootMethod(_info.method);

					final Collection<SootMethod> _callees = cgi.getCallees(_stmt.getInvokeExpr(), _context);
					final Iterator<SootMethod> _j = _callees.iterator();
					final int _jEnd = _callees.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Info _clone = _info.clone();
						_clone.callstack.push(_pair);
						_info.method = _j.next();
						_wb.addWork(_clone);
					}
				} else if (_stmt instanceof ReturnVoidStmt || _stmt instanceof ReturnStmt) {
					final Pair<Stmt, SootMethod> _pair = new Pair<Stmt, SootMethod>(_stmt, _info.method);

					if (!_info.callstack.isEmpty()) {
						final Pair<Stmt, SootMethod> _caller = _info.callstack.pop();
						_info.currStmt = _caller.getFirst();
						_info.method = _caller.getSecond();
						processSuccs(_info, _pair, _wb, SuccessorType.NORMAL_RETURN);
					}
				} else if (_stmt instanceof ThrowStmt) {
					final Pair<Stmt, SootMethod> _pair = new Pair<Stmt, SootMethod>(_stmt, _info.method);

					if (!_info.callstack.isEmpty()) {
						final Pair<Stmt, SootMethod> _caller = _info.callstack.pop();
						_info.currStmt = _caller.getFirst();
						_info.method = _caller.getSecond();
						processSuccs(_info, _pair, _wb, SuccessorType.EXCEPTIONAL_RETURN);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param info DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private boolean handleRecursion(final Info info, final IWorkBag<Info> wb) {
		final Pair<Stmt, SootMethod> _pair = new Pair<Stmt, SootMethod>(info.currStmt, info.method);
		boolean _result = info.callstack.contains(_pair);
		if (_result) {
			final Collection<Pair<Stmt, SootMethod>> _aref = MapUtils.getCollectionFromMap(entryPoint2reachableARef,
					info.entryPoint);
			_aref.addAll(info.aref);
			info.aref = _aref;
			final Collection<Pair<Stmt, SootMethod>> _fref = MapUtils.getCollectionFromMap(entryPoint2reachableFRef,
					info.entryPoint);
			_fref.addAll(info.fref);
			info.fref = _fref;
			final Collection<Pair<Stmt, SootMethod>> _lacq = MapUtils.getCollectionFromMap(entryPoint2reachableLock,
					info.entryPoint);
			_lacq.addAll(info.lacq);
			info.lacq = _lacq;
			final Pair<Stmt, SootMethod> _caller = info.callstack.pop();
			info.currStmt = _caller.getFirst();
			info.method = _caller.getSecond();
			processSuccs(info, _pair, wb, SuccessorType.NORMAL_RETURN);
			processSuccs(info, _pair, wb, SuccessorType.EXCEPTIONAL_RETURN);
		}
		return _result;
	};

	/**
	 * DOCUMENT ME!
	 * 
	 * @param info DOCUMENT ME!
	 * @param pair DOCUMENT ME!
	 * @param wb DOCUMENT ME!
	 */
	private void processSuccs(final Info info, final Pair<Stmt, SootMethod> pair, final IWorkBag<Info> wb,
			final SuccessorType retType) {
		info.lacq.add(pair);
		info.aref.add(pair);
		info.fref.add(pair);

		final Stmt _stmt = info.currStmt;
		final UnitGraph _graph = stmtGraphFactory.getStmtGraph(info.method);
		final List<Stmt> _succsOf = _graph.getSuccsOf(_stmt);
		final Iterator<Stmt> _i = _succsOf.iterator();
		final int _iEnd = _succsOf.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Stmt _succ = _i.next();
			if ((retType == SuccessorType.INTRAPROCEDURAL)
					|| (retType == SuccessorType.NORMAL_RETURN && (!(_succ instanceof DefinitionStmt) || !(((DefinitionStmt) _succ)
							.getRightOp() instanceof CaughtExceptionRef)))
					|| (retType == SuccessorType.EXCEPTIONAL_RETURN && _succ instanceof DefinitionStmt && ((DefinitionStmt) _succ)
							.getRightOp() instanceof CaughtExceptionRef)) {
				final Info _clone = info.clone();
				_clone.currStmt = _succ;
				wb.addWork(_clone);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param pair DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 */
	private void recordMayFollow(final Pair<Stmt, SootMethod> pair, final Collection<Pair<Stmt, SootMethod>> col) {
		final Iterator<Pair<Stmt, SootMethod>> _i = col.iterator();
		final int _iEnd = col.size();
		final Collection<String> _birLocs = tool.generateBIRRep(pair, false);
		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Collection<String> _pString = tool.generateBIRRep(_i.next(), false);
			for (final String _birLoc : _pString) {
				MapUtils.putAllIntoCollectionInMap(tool.mayFollow, _birLoc, _birLocs);
			}
		}
		col.clear();
	}
}

// End of File
