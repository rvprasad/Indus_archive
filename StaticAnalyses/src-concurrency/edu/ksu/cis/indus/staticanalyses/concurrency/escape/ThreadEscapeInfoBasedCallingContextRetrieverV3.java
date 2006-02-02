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

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.soot.Util;
import soot.SootMethod;
import soot.Value;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.MonitorStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

/**
 * This implementation facilitates the extraction of calling-contexts based on multithread data sharing and context coupling.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ThreadEscapeInfoBasedCallingContextRetrieverV3
		extends ThreadEscapeInfoBasedCallingContextRetrieverV2 {

	/**
	 * Creates an instance of this instance.
	 * 
	 * @param callContextLenLimit <i>refer to the constructor of the super class</i>.
	 * @param dependenceID id of the dependence type for which this retriever is used.
	 * @pre depenenceID != null
	 * @pre dependenceId.equals(IDependencyAnalysis.DependenceSort.READY_DA) or
	 *      dependenceId.equals(IDependencyAnalysis.DependenceSort.INTERFERENCE_DA)
	 */
	public ThreadEscapeInfoBasedCallingContextRetrieverV3(final int callContextLenLimit, final Object dependenceID) {
		super(callContextLenLimit, dependenceID);
	}

	/**
	 * @see ThreadEscapeInfoBasedCallingContextRetrieverV2#shouldCallerSideTokenBeDiscarded(AliasSet, AliasSet)
	 */
	@Override protected boolean shouldCallerSideTokenBeDiscarded(final AliasSet callerSideToken,
			final AliasSet calleeSideToken) {
		final Stmt _stmt = (Stmt) getInfoFor(Identifiers.SRC_ENTITY);
		boolean _result = super.shouldCallerSideTokenBeDiscarded(callerSideToken, calleeSideToken);
		if (!_result) {
			if (_stmt != null) {
				_result = process1(callerSideToken, _stmt, _result);
			} else {
				_result = process2(callerSideToken, _result);
			}
		}

		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param callerSideToken DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 * @param initialResult DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private boolean process1(final AliasSet callerSideToken, final Stmt stmt, final boolean initialResult) {
		Value _value = null;
		if (interferenceBased) {
			if (stmt.containsArrayRef()) {
				_value = stmt.getArrayRef().getBase();
			} else {
				final FieldRef _fr = stmt.getFieldRef();
				if (_fr instanceof StaticFieldRef) {
					_value = _fr;
				} else {
					_value = ((InstanceFieldRef) _fr).getBase();
				}
			}
		} else if (readyBased) {
			if (stmt instanceof MonitorStmt) {
				_value = ((MonitorStmt) stmt).getOp();
			} else if (stmt.containsInvokeExpr()) {
				final InvokeExpr _ex = stmt.getInvokeExpr();
				final SootMethod _invokedMethod = _ex.getMethod();

				if (_ex instanceof VirtualInvokeExpr
						&& (Util.isWaitMethod(_invokedMethod) || Util.isNotifyMethod(_invokedMethod))) {
					_value = ((VirtualInvokeExpr) _ex).getBase();
				}
			}
		}

		final AliasSet _as = ecba.queryAliasSetFor(_value, (SootMethod) getInfoFor(Identifiers.SRC_METHOD));
		boolean _result = initialResult;
		if (_as != null) {
			if (interferenceBased) {
				_result = CollectionUtils.containsAny(callerSideToken.getReadWriteShareEntities(), _as
						.getReadWriteShareEntities());
			} else if (readyBased) {
				_result = CollectionUtils.containsAny(callerSideToken.getReadyEntities(), _as.getReadyEntities())
						|| CollectionUtils.containsAny(callerSideToken.getLockEntities(), _as.getLockEntities());
			}
		} else {
			_result = true;
		}
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param callerSideToken DOCUMENT ME!
	 * @param initialResult DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	private boolean process2(final AliasSet callerSideToken, final boolean initialResult) {
		final AliasSet _as;
		final SootMethod _sm = (SootMethod) getInfoFor(Identifiers.SRC_METHOD);
		if (_sm.isStatic()) {
			_as = ecba.queryAliasSetFor(_sm.getDeclaringClass());
		} else {
			_as = ecba.queryAliasSetForThis(_sm);
		}

		boolean _r = initialResult;
		if (_as != null) {
			if (readyBased) {
				_r = CollectionUtils.containsAny(callerSideToken.getReadyEntities(), _as.getReadyEntities())
						|| CollectionUtils.containsAny(callerSideToken.getLockEntities(), _as.getLockEntities());
			}
		} else {
			_r = true;
		}
		return _r;
	}

}

// End of File
