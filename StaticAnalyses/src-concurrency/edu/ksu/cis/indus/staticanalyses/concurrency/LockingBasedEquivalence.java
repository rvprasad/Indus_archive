
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
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class LockingBasedEquivalence
  extends AbstractProcessor {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Collection invokeStmts = new HashSet();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Collection monitorStmts = new HashSet();

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final IEscapeInfo escape;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ICallGraphInfo cgi;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Map locking2lockings = new HashMap();

	/**
	 * Creates a new LockingBasedEquivalence object.
	 *
	 * @param escapeInfo DOCUMENT ME!
	 * @param callgraph DOCUMENT ME!
	 */
	public LockingBasedEquivalence(final IEscapeInfo escapeInfo, final ICallGraphInfo callgraph) {
		escape = escapeInfo;
		cgi = callgraph;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param pair DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getLockingBasedEquivalentsFor(final Pair pair) {
		return Collections.unmodifiableCollection((Collection) MapUtils.getObject(locking2lockings, pair,
				Collections.EMPTY_SET));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getNonSingularLockingBasedEquivalents() {
		return Collections.unmodifiableCollection(locking2lockings.keySet());
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		if (method.isSynchronized()) {
			final Pair _p = new Pair(null, method);
			processLocal(null, method, _p);
			monitorStmts.add(_p);
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
			monitorStmts.add(_p);
		}
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
		return new ToStringBuilder(this).appendSuper(super.toString()).append("monitorStmts", this.monitorStmts)
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
	 * DOCUMENT ME!
	 *
	 * @param local DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 * @param p DOCUMENT ME!
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

			if (escape.areCoupledViaLocking(local, method, _l2, _sm)) {
				CollectionsUtilities.putIntoSetInMap(locking2lockings, _p2, p);
				CollectionsUtilities.putIntoSetInMap(locking2lockings, p, _p2);
			}
		}

		final Iterator _j = monitorStmts.iterator();
		final int _jEnd = monitorStmts.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Pair _p2 = (Pair) _j.next();
			final EnterMonitorStmt _s = (EnterMonitorStmt) _p2.getFirst();
			final SootMethod _sm = (SootMethod) _p2.getSecond();
			final Local _l2 = _s == null ? null
										 : (Local) _s.getOp();

			if (escape.areCoupledViaLocking(local, method, _l2, _sm)) {
				CollectionsUtilities.putIntoSetInMap(locking2lockings, _p2, p);
				CollectionsUtilities.putIntoSetInMap(locking2lockings, p, _p2);
			}
		}
	}
}

// End of File
