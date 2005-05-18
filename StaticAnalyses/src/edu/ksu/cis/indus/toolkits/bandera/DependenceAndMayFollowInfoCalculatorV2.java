
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import soot.SootMethod;

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
class DependenceAndMayFollowInfoCalculatorV2
  extends DependenceAndMayFollowInfoCalculator {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final IStmtGraphFactory stmtGraphFactory;

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
	 *
	 * @see DependenceAndMayFollowInfoCalculator#DependenceAndMayFollowInfoCalculator(RelativeDependenceInfoTool,
	 * 		InterferenceDAv1, LockAcquisitionBasedEquivalence, ICallGraphInfo,
	 * 		IThreadGraphInfo, CFGAnalysis)
	 */
	DependenceAndMayFollowInfoCalculatorV2(final RelativeDependenceInfoTool theTool, final InterferenceDAv1 ida,
		final LockAcquisitionBasedEquivalence lbe,
		final IThreadGraphInfo threadGraph, final ICallGraphInfo callGraph, final CFGAnalysis cfgAnalysis,
		final IStmtGraphFactory graphFactory) {
		super(theTool, ida, lbe, callGraph, threadGraph, cfgAnalysis);
		stmtGraphFactory = graphFactory;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
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
		Collection aref;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Collection fref;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Collection lacq;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		SootMethod method;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Stack callstack;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Stack path;

		/** 
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		Stmt currStmt;

		/**
		 * Creates an instance of this class.
		 *
		 * @param sm
		 * @param stmt
		 * @param stack1
		 * @param stack2
		 * @param stack3
		 * @param stack4
		 * @param stack5
		 */
		public Info(SootMethod sm, Stmt stmt, Stack stack1, Collection stack2, Collection stack3, Collection stack4,
			Stack stack5) {
			method = sm;
			callstack = stack1;
			currStmt = stmt;
			lacq = stack2;
			aref = stack3;
			fref = stack4;
			path = stack5;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @return DOCUMENT ME!
		 */
		public Object clone() {
			final Info _result;

			try {
				_result = (Info) super.clone();
				_result.aref = (Collection) ((HashSet) aref).clone();
				_result.fref = (Collection) ((HashSet) fref).clone();
				_result.lacq = (Collection) ((HashSet) lacq).clone();
				_result.path = (Stack) path.clone();
				_result.callstack = (Stack) callstack.clone();
			} catch (final CloneNotSupportedException _e) {
				final IllegalStateException _r = new IllegalStateException();
				_r.initCause(_e);
				throw _r;
			}
			return _result;
		}
	}

	/**
	 * @see edu.ksu.cis.indus.toolkits.bandera.DependenceAndMayFollowInfoCalculator#calculatedMayFollowRelation()
	 */
	protected void calculatedMayFollowRelation() {
		final Iterator _i = tgi.getThreadEntryPoints().iterator();
		final int _iEnd = tgi.getThreadEntryPoints().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _sm = (SootMethod) _i.next();
			final IWorkBag _wb = new LIFOWorkBag();
			final Info _t = new Info(_sm, null, new Stack(), new HashSet(), new HashSet(), new HashSet(), new Stack());
			_wb.addWork(_t);

			while (_wb.hasWork()) {
				final Info _info = (Info) _wb.getWork();

				if (handleRecursion(_info)) {
					continue;
				}

				final Stmt _stmt = _info.currStmt;

				if (_stmt == null) {
					_info.currStmt = (Stmt) _info.method.retrieveActiveBody().getUnits().getFirst();
					_wb.addWork(_info);
				} else if (_stmt instanceof EnterMonitorStmt) {
					final Pair _pair = new Pair(_stmt, _info.method);
					recordMayFollow(_pair, _info.lacq);
					processSuccs(_info, _pair, _wb);
				} else if (_stmt.containsArrayRef()) {
					final Pair _pair = new Pair(_stmt, _info.method);
					recordMayFollow(_pair, _info.aref);
					processSuccs(_info, _pair, _wb);
				} else if (_stmt.containsFieldRef()) {
					final Pair _pair = new Pair(_stmt, _info.method);
					recordMayFollow(_pair, _info.fref);
					processSuccs(_info, _pair, _wb);
				} else if (_stmt.containsInvokeExpr()) {
					final Pair _pair = new Pair(_stmt, _info.method);
					_info.path.push(_pair);
					_info.lacq.add(_pair);
					_info.aref.add(_pair);
					_info.fref.add(_pair);
					_info.currStmt = null;

					final Context _context = new Context();
					_context.setStmt(_stmt);
					_context.setRootMethod(_info.method);

					final Collection _callees = cgi.getCallees(_stmt.getInvokeExpr(), _context);
					final Iterator _j = _callees.iterator();
					final int _jEnd = _callees.size();

					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final Info _clone = (Info) _info.clone();
						_clone.callstack.push(_pair);
						_info.method = (SootMethod) _j.next();
						_wb.addWork(_clone);
					}
				} else if (_stmt instanceof ReturnVoidStmt || _stmt instanceof ReturnStmt) {
					final Pair _pair = new Pair(_stmt, _info.method);

					if (!_info.callstack.isEmpty()) {
						final Pair _caller = (Pair) _info.callstack.pop();
						_info.currStmt = (Stmt) _caller.getFirst();
						_info.method = (SootMethod) _caller.getSecond();
						// TODO: we need to process only the successor reachable upon normal return.
						processSuccs(_info, _pair, _wb);
					}
				} else if (_stmt instanceof ThrowStmt) {
					final Pair _pair = new Pair(_stmt, _info.method);

					if (!_info.callstack.isEmpty()) {
						final Pair _caller = (Pair) _info.callstack.pop();
						_info.currStmt = (Stmt) _caller.getFirst();
						_info.method = (SootMethod) _caller.getSecond();
						// TODO: we need to process only the handlers of the given exception.
						processSuccs(_info, _pair, _wb);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param info DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	private boolean handleRecursion(Info info) {
		final Pair _pair = new Pair(info.currStmt, info.method);
		boolean _result = info.path.contains(_pair);
		final Iterator _i = info.lacq.iterator();
		final int _iEnd = info.lacq.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair _p = (Pair) _i.next();

			// TODO: logic please
		}
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param info DOCUMENT ME!
	 * @param pair DOCUMENT ME!
	 * @param wb DOCUMENT ME!
	 */
	private void processSuccs(final Info info, final Pair pair, final IWorkBag wb) {
		info.path.push(pair);
		info.lacq.add(pair);
		info.aref.add(pair);
		info.fref.add(pair);

		final Stmt _stmt = info.currStmt;
		final UnitGraph _graph = stmtGraphFactory.getStmtGraph(info.method);
		final List _succsOf = _graph.getSuccsOf(_stmt);
		final Iterator _i = _succsOf.iterator();
		final int _iEnd = _succsOf.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Stmt _succ = (Stmt) _i.next();
			final Info _clone = (Info) info.clone();
			_clone.currStmt = _succ;
			wb.addWork(_clone);
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param pair DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 */
	private void recordMayFollow(Pair pair, Collection col) {
		final Iterator _i = col.iterator();
		final int _iEnd = col.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair _p = (Pair) _i.next();
			CollectionsUtilities.putIntoListInMap(tool.mayFollow, _p, pair);
		}
		col.clear();
	}
}

// End of File
