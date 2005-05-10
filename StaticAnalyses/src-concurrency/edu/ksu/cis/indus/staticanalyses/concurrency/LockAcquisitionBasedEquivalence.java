
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

package edu.ksu.cis.indus.staticanalyses.concurrency;

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
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

import org.apache.commons.collections.MapUtils;

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.Local;
import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class calculates equivalence classes of locking statements that may acquire same locks.  In simple words, two lock
 * acquisition statements (enter-monitor or synchronized method invocation) belong to the same equivalence  class if they
 * may acquire the lock on the same object.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class LockAcquisitionBasedEquivalence
  extends AbstractProcessor {
	/** 
	 * This is a collection of enter monitor statements.
	 *
	 * @invariant monitorStmts.oclIsKindOf(Collection(EnterMonitorStmt))
	 */
	private final Collection enterMonitorStmts = new HashSet();

	/** 
	 * This is a collection of invocation statements.
	 *
	 * @invariant invokeStmts.oclIsKindOf(Collection(Stmt))
	 * @invariant invokeStmts->forall(o | o.containsInvokeExpr())
	 */
	private final Collection invokeStmts = new HashSet();

	/** 
	 * This provides locking information.
	 */
	private final IEscapeInfo locking;

	/** 
	 * This provides the call graph.
	 */
	private ICallGraphInfo cgi;

	/** 
	 * This maps a lock acquisition statements to the collection of lock acquisition statements that are in the same
	 * equivalence class as the key.
	 *
	 * @invariant locking2lockings.oclIsKindOf(Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt, SootMethod))))
	 * @invariant locking2lockings.keySet()->forall(o | o.getFirst().oclIsKindOf(EnterMonitorStmt) or
	 * 			  o.getFirst().containsInvokeExpr())
	 */
	private final Map locking2lockings = new HashMap();

	/**
	 * Creates a new LockAcquisitionBasedEquivalence object.
	 *
	 * @param escapeInfo to be used.
	 * @param callgraph to be used.
	 *
	 * @pre escapeInfo != null and callgraph != null
	 */
	public LockAcquisitionBasedEquivalence(final IEscapeInfo escapeInfo, final ICallGraphInfo callgraph) {
		locking = escapeInfo;
		cgi = callgraph;
	}

	/**
	 * Retrieves the lock acquisitions that belong to the same equivalence class as the given lock acquisition.
	 *
	 * @param pair of interest.
	 *
	 * @return a collection of lock acquisition.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
	 */
	public Collection getLockAcquisitionsInEquivalenceClassOf(final Pair pair) {
		return Collections.unmodifiableCollection((Collection) MapUtils.getObject(locking2lockings, pair,
				Collections.EMPTY_SET));
	}

	/**
	 * Retrieves the lock acquisitions that belong to a non-singleton equivalence class.
	 *
	 * @return a collection of lock acquisition.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(Pair(Stmt, SootMethod)))
	 */
	public Collection getLockAcquisitionsInNonSingletonEquivalenceClass() {
		return Collections.unmodifiableCollection(locking2lockings.keySet());
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		if (method.isSynchronized()) {
			final Pair _p = new Pair(null, method);
			processLocal(null, method, _p);
			enterMonitorStmts.add(_p);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final SootMethod _method = context.getCurrentMethod();

		if (stmt instanceof InvokeStmt) {
			if (Util.isWaitInvocation((InvokeStmt) stmt, _method, cgi)) {
				final InvokeStmt _s = (InvokeStmt) stmt;
				final Pair _p = new Pair(_s, _method);
				final Local _l = (Local) ((VirtualInvokeExpr) _s.getInvokeExpr()).getBase();
				processLocal(_l, _method, _p);
				invokeStmts.add(_p);
			}
		} else {
			final EnterMonitorStmt _n = (EnterMonitorStmt) stmt;
			final Pair _p = new Pair(_n, _method);
			final Local _l = (Local) _n.getOp();
			processLocal(_l, _method, _p);
			enterMonitorStmts.add(_p);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	public void consolidate() {
		super.consolidate();
		enterMonitorStmts.clear();
		invokeStmts.clear();
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
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("monitorStmts", this.enterMonitorStmts)
										  .append("invokeStmts", this.invokeStmts)
										  .append("locking2lockings", this.locking2lockings).toString();
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
	 *
	 * @pre method != null and p != null and p.oclIsKindOf(Pair(Stmt, SootMethod))
	 */
	private void processLocal(final Local local, final SootMethod method, final Pair p) {
		final Iterator _i = invokeStmts.iterator();
		final int _iEnd = invokeStmts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair _p2 = (Pair) _i.next();
			final InvokeStmt _s = (InvokeStmt) _p2.getFirst();
			final SootMethod _sm = (SootMethod) _p2.getSecond();
			final Local _l2 = _s == null ? null
										 : (Local) ((VirtualInvokeExpr) _s.getInvokeExpr()).getBase();

			if (locking.areCoupledViaLocking(local, method, _l2, _sm)) {
				CollectionsUtilities.putIntoSetInMap(locking2lockings, _p2, p);
				CollectionsUtilities.putIntoSetInMap(locking2lockings, p, _p2);
			}
		}

		final Iterator _j = enterMonitorStmts.iterator();
		final int _jEnd = enterMonitorStmts.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Pair _p2 = (Pair) _j.next();
			final EnterMonitorStmt _s = (EnterMonitorStmt) _p2.getFirst();
			final SootMethod _sm = (SootMethod) _p2.getSecond();
			final Local _l2 = _s == null ? null
										 : (Local) _s.getOp();

			if (locking.areCoupledViaLocking(local, method, _l2, _sm)) {
				CollectionsUtilities.putIntoSetInMap(locking2lockings, _p2, p);
				CollectionsUtilities.putIntoSetInMap(locking2lockings, p, _p2);
			}
		}
	}
}

// End of File
