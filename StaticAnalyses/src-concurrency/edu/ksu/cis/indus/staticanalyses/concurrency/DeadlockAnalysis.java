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

package edu.ksu.cis.indus.staticanalyses.concurrency;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.graph.SimpleNode;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraphBuilder;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.IMonitorInfo;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.LockAcquisitionBasedEquivalence;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import soot.SootMethod;
import soot.Value;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.Stmt;

/**
 * This analysis performs a conservative static analysis to detect statements that may participate in deadlocks.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class DeadlockAnalysis {

	/**
	 * This provides monitor information.
	 */
	private final IMonitorInfo<?> monitorInfo;

	/**
	 * This provides object flow information.
	 */
	private final IValueAnalyzer<Value> ofa;

	/**
	 * This provides lock based equivalence information.
	 */
	private final LockAcquisitionBasedEquivalence lbe;

	/**
	 * This provides escape information.
	 */
	private final IEscapeInfo escapeInfo;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param m to be used.
	 * @param va to be used.
	 * @param l to be used.
	 * @param e to be used.
	 * @pre m != null and va != null
	 */
	public DeadlockAnalysis(final IMonitorInfo<?> m, final IValueAnalyzer<Value> va, final LockAcquisitionBasedEquivalence l,
			final IEscapeInfo e) {
		monitorInfo = m;
		ofa = va;
		lbe = l;
		escapeInfo = e;
	}

	/**
	 * Retrieves the collection of monitors that may participate in deadlock.
	 * 
	 * @return a collection of monitors.
	 */
	public Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> getDeadlockingMonitors() {
		final SimpleNodeGraphBuilder<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _sng = new SimpleNodeGraphBuilder<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _result = new HashSet<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _monitors = monitorInfo.getMonitorTriples();
		final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _processed = new ArrayList<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>();
		final IWorkBag<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _wb = new HistoryAwareFIFOWorkBag<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>(
				_processed);

		_sng.createGraph();

		for (final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _m : _monitors) {
			_wb.addWork(_m);
			while (_wb.hasWork()) {
				final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _e = _wb.getWork();
				final Collection<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _enclosingMonitorTriples = monitorInfo
						.getEnclosingMonitorTriples(_m.getFirst(), _m.getThird(), false);
				_sng.addEdgeFromTo(_e, _enclosingMonitorTriples);
				_wb.addAllWork(_enclosingMonitorTriples);
			}
		}

		for (final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _m1 : _processed) {
			for (final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> _m2 : _processed) {
				if (_m1 != _m2 && areMonitorsRelated(_m1, _m2)) {
					_sng.addEdgeFromTo(_m1, _m2);
				}
			}
		}

		_sng.finishBuilding();
		final SimpleNodeGraph<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _sn = _sng.getBuiltGraph();
		for (final List<SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>>> _nodes : _sn.getSCCs(true)) {
			for (final SimpleNode<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> _node : _nodes) {
				_result.add(_sn.getObjectExtractor().transform(_node));
			}
		}
		return _result;
	}

	/**
	 * Checks if the given monitors are related.
	 * 
	 * @param m1 is a monitor of interest.
	 * @param m2 is another monitor of interest.
	 * @return <code>true</code> if the monitors are related; <code>false</code>, otherwise.
	 */
	private boolean areMonitorsRelated(final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> m1,
			final Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod> m2) {
		boolean _result = false;
		final SootMethod _sm1 = m1.getThird();
		final SootMethod _sm2 = m2.getThird();
		final Pair<Stmt, SootMethod> _p1 = new Pair<Stmt, SootMethod>(m1.getSecond(), m1.getThird());
		final Pair<Stmt, SootMethod> _p2 = new Pair<Stmt, SootMethod>(m2.getSecond(), m2.getThird());

		if (lbe == null || lbe.getLockAcquisitionsInEquivalenceClassOf(_p1).contains(_p2)) {
			final Context _ctxt = new Context();
			if (m1.getFirst() == null && m2.getFirst() == null) {
				if (_sm1.isStatic() && _sm2.isStatic()) {
					_result = _sm1.getDeclaringClass().equals(_sm2.getDeclaringClass());
				} else if (!_sm1.isStatic() && !_sm2.isStatic()) {
					_ctxt.setRootMethod(_sm1);
					final Collection<Value> _c1 = ofa.getValuesForThis(_ctxt);
					_ctxt.setRootMethod(_sm2);
					final Collection<Value> _c2 = ofa.getValuesForThis(_ctxt);
					_result = CollectionUtils.containsAny(_c1, _c2)
							&& (escapeInfo == null || (escapeInfo.escapes(_sm2.getDeclaringClass(), _sm2) && escapeInfo
									.escapes(_sm1.getDeclaringClass(), _sm1)));
				}
			} else {
				final Collection<Value> _c1;
				boolean _flag1 = true;
				boolean _flag2 = true;
				if (m1.getFirst() == null) {
					if (_sm1.isStatic()) {
						_c1 = Collections.emptySet();
					} else {
						_ctxt.setRootMethod(_sm1);
						_c1 = ofa.getValuesForThis(_ctxt);
						_flag1 = escapeInfo == null || escapeInfo.escapes(_sm1.getDeclaringClass(), _sm1);
					}
				} else {
					_ctxt.setRootMethod(_sm1);
					_ctxt.setStmt(m1.getFirst());
					_ctxt.setProgramPoint(m1.getFirst().getOpBox());
					_c1 = ofa.getValues(m1.getFirst().getOp(), _ctxt);
					_flag1 = escapeInfo == null || escapeInfo.escapes(m1.getFirst().getOp(), _sm1);
				}

				final Collection<Value> _c2;
				if (m2.getFirst() == null) {
					if (_sm2.isStatic()) {
						_c2 = Collections.emptySet();
					} else {
						_ctxt.setRootMethod(_sm2);
						_c2 = ofa.getValuesForThis(_ctxt);
						_flag2 = escapeInfo == null || escapeInfo.escapes(_sm2.getDeclaringClass(), _sm2);
					}
				} else {
					_ctxt.setRootMethod(_sm2);
					_ctxt.setStmt(m2.getFirst());
					_ctxt.setProgramPoint(m2.getFirst().getOpBox());
					_c2 = ofa.getValues(m2.getFirst().getOp(), _ctxt);
					_flag2 = escapeInfo == null || escapeInfo.escapes(m2.getFirst().getOp(), _sm2);
				}
				_result = _flag1 && _flag2 && CollectionUtils.containsAny(_c1, _c2);
			}
		}
		return _result;
	}
}

// End of File
