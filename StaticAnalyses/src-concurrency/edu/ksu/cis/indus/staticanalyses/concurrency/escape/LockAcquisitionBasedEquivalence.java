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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.Local;
import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

/**
 * This class calculates equivalence classes of locking statements that may acquire same locks. In simple words, two lock
 * acquisition statements (enter-monitor or synchronized method invocation) belong to the same equivalence class if they may
 * acquire the lock on the same object.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class LockAcquisitionBasedEquivalence
		extends AbstractProcessor {

	/**
	 * This provides the call graph.
	 */
	private ICallGraphInfo cgi;

	/**
	 * This is a collection of enter monitor statements.
	 */
	private final Collection<Pair<EnterMonitorStmt, SootMethod>> enterMonitorStmts;

	/**
	 * This is a collection of invocation statements.
	 */
	private final Collection<Pair<InvokeStmt, SootMethod>> invokeStmts;

	/**
	 * This provides locking information.
	 */
	private final IEscapeInfo locking;

	/**
	 * This maps a lock acquisition statement to the collection of lock acquisition statements that are in the same
	 * equivalence class as the key.
	 *
	 * @invariant locking2lockings.oclIsKindOf(Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt, SootMethod))))
	 * @invariant locking2lockings.keySet()->forall(o | o.getFirst().oclIsKindOf(EnterMonitorStmt) or
	 *            o.getFirst().containsInvokeExpr())
	 */
	private final Map<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>> locking2lockings;

	/**
	 * Creates a new LockAcquisitionBasedEquivalence object.
	 *
	 * @param escapeInfo to be used.
	 * @param callgraph to be used.
	 * @pre escapeInfo != null and callgraph != null
	 */
	public LockAcquisitionBasedEquivalence(final IEscapeInfo escapeInfo, final ICallGraphInfo callgraph) {
		locking = escapeInfo;
		cgi = callgraph;
		locking2lockings = new HashMap<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>>();
		enterMonitorStmts = new HashSet<Pair<EnterMonitorStmt, SootMethod>>();
		invokeStmts = new HashSet<Pair<InvokeStmt, SootMethod>>();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	@Override public void callback(final SootMethod method) {
		if (method.isSynchronized()) {
			final Pair<EnterMonitorStmt, SootMethod> _p = new Pair<EnterMonitorStmt, SootMethod>(null, method);
			enterMonitorStmts.add(_p);
			processLocal(null, method, _p);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, Context)
	 */
	@Override public void callback(final Stmt stmt, final Context context) {
		final SootMethod _method = context.getCurrentMethod();

		if (stmt instanceof InvokeStmt) {
			if (Util.isWaitInvocation((InvokeStmt) stmt, _method, cgi)) {
				final InvokeStmt _s = (InvokeStmt) stmt;
				final Pair<InvokeStmt, SootMethod> _p = new Pair<InvokeStmt, SootMethod>(_s, _method);
				final Local _l = (Local) ((VirtualInvokeExpr) _s.getInvokeExpr()).getBase();
				invokeStmts.add(_p);
				processLocal(_l, _method, _p);
			}
		} else {
			final EnterMonitorStmt _n = (EnterMonitorStmt) stmt;
			final Pair<EnterMonitorStmt, SootMethod> _p = new Pair<EnterMonitorStmt, SootMethod>(_n, _method);
			final Local _l = (Local) _n.getOp();
			enterMonitorStmts.add(_p);
			processLocal(_l, _method, _p);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	@Override public void consolidate() {
		super.consolidate();
		enterMonitorStmts.clear();
		invokeStmts.clear();
	}

	/**
	 * Retrieves the lock acquisitions that belong to the same equivalence class as the given lock acquisition.
	 *
	 * @param pair of interest.
	 * @return a collection of lock acquisition.
	 * @pre pair.oclIsKindOf(Pair(InvokeStmt, SootMethod)) or pair.oclIsKindOf(Pair(EnterMonitorStmt, SootMethod))
	 * @post result != null
	 * @post
	 * @post result->forall(o | o.oclIsKindOf(Pair(InvokeStmt, SootMethod)) or o.oclIsKindOf(Pair(EnterMonitorStmt,
	 *       SootMethod)))
	 */
	public Collection<Pair<? extends Stmt, SootMethod>> getLockAcquisitionsInEquivalenceClassOf(
			final Pair<Stmt, SootMethod> pair) {
		return Collections.unmodifiableCollection(MapUtils.getEmptyCollectionFromMap(locking2lockings, pair));
	}

	/**
	 * Retrieves the lock acquisitions that belong to a non-singleton equivalence class.
	 *
	 * @return a collection of lock acquisition.
	 * @post result != null
	 * @post result->forall(o | o.oclIsKindOf(Pair(InvokeStmt, SootMethod)) or o.oclIsKindOf(Pair(EnterMonitorStmt,
	 *       SootMethod)))
	 */
	public Collection<Pair<? extends Stmt, SootMethod>> getLockAcquisitionsInNonSingletonEquivalenceClass() {
		final Collection<Pair<? extends Stmt, SootMethod>> _r = new HashSet<Pair<? extends Stmt, SootMethod>>();
		final Iterator<Map.Entry<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>>> _i;
		_i = locking2lockings.entrySet().iterator();
		final int _iEnd = locking2lockings.entrySet().size();
		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry<Pair<? extends Stmt, SootMethod>, Collection<Pair<? extends Stmt, SootMethod>>> _e = _i.next();
			if (_e.getValue().size() > 1) {
				_r.add(_e.getKey());
			}
		}
		return _r;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(EnterMonitorStmt.class, this);
		ppc.register(InvokeStmt.class, this);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("monitorStmts", this.enterMonitorStmts).append(
				"invokeStmts", this.invokeStmts).append("locking2lockings", this.locking2lockings).toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(EnterMonitorStmt.class, this);
		ppc.unregister(InvokeStmt.class, this);
	}

	/**
	 * Processes the given local in the given method to calculate if it is related to the given lock acquisition.
	 *
	 * @param local of interest.
	 * @param method in which <code>local</code> occurs.
	 * @param p is a lock acquisition.
	 * @pre method != null and p != null and p.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	private void processLocal(final Local local, final SootMethod method, final Pair<? extends Stmt, SootMethod> p) {
		final Iterator<Pair<InvokeStmt, SootMethod>> _i = invokeStmts.iterator();
		final int _iEnd = invokeStmts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair<InvokeStmt, SootMethod> _p2 = _i.next();
			final InvokeStmt _s = _p2.getFirst();
			final SootMethod _sm = _p2.getSecond();
			final Local _l2 = _s == null ? null : (Local) ((VirtualInvokeExpr) _s.getInvokeExpr()).getBase();

			if (locking.areCoupledViaLocking(local, method, _l2, _sm)) {
				MapUtils.putIntoSetInMap(locking2lockings, _p2, p);
				MapUtils.putIntoSetInMap(locking2lockings, p, _p2);
			}
		}

		final Iterator<Pair<EnterMonitorStmt, SootMethod>> _j = enterMonitorStmts.iterator();
		final int _jEnd = enterMonitorStmts.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Pair<EnterMonitorStmt, SootMethod> _p2 = _j.next();
			final EnterMonitorStmt _s = _p2.getFirst();
			final SootMethod _sm = _p2.getSecond();
			final Local _l2 = _s == null ? null : (Local) _s.getOp();

			if (locking.areCoupledViaLocking(local, method, _l2, _sm)) {
				MapUtils.putIntoSetInMap(locking2lockings, _p2, p);
				MapUtils.putIntoSetInMap(locking2lockings, p, _p2);
			}
		}
	}
}

// End of File
