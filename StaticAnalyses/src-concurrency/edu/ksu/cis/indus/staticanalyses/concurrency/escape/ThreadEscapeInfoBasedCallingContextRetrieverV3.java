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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.soot.Util;

import java.util.Collection;

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
	 * @param preserveReady <i>refer to the constructor of the super class</i>.
	 * @param preserveInterference <i>refer to the constructor of the super class</i>.
	 */
	public ThreadEscapeInfoBasedCallingContextRetrieverV3(final int callContextLenLimit, final boolean preserveReady,
			final boolean preserveInterference) {
		super(callContextLenLimit, preserveReady, preserveInterference);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected boolean shouldCallerSideTokenBeDiscarded(final AliasSet callerSideToken,
			final AliasSet calleeSideToken) {
		final Stmt _stmt = (Stmt) getInfoFor(Identifiers.SRC_ENTITY);
		boolean _result = super.shouldCallerSideTokenBeDiscarded(callerSideToken, calleeSideToken);
		if (!_result) {
			if (_stmt != null) {
				_result = shouldCallerSideTokenBeDiscardedWhenSrcStmtIsNonNull(callerSideToken, _stmt, _result);
			} else {
				_result = shouldCallerSideTokenBeDiscardedWhenSrcStmtIsNull(callerSideToken, _result);
			}
		}

		return _result;
	}

	/**
	 * Checks if the caller side token should be discarded.
	 * 
	 * @param callerSideToken of interest.
	 * @param stmt that lead to this context.
	 * @param initialResult is the collection that needs to be updated.
	 * @return <code>true</code> if the token should be discarded; <code>false</code>, otherwise.
	 */
	private boolean shouldCallerSideTokenBeDiscardedWhenSrcStmtIsNonNull(final AliasSet callerSideToken, final Stmt stmt,
			final boolean initialResult) {
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
				final Collection<Object> _callerRWEntities = callerSideToken.getReadWriteShareEntities();
				final Collection<Object> _srcRWEntities = _as.getReadWriteShareEntities();
				_result = _callerRWEntities != null && _srcRWEntities != null
						&& CollectionUtils.containsAny(_callerRWEntities, _srcRWEntities);
			} else if (readyBased) {
				final Collection<Object> _callerReadyEntities = callerSideToken.getReadyEntities();
				final Collection<Object> _srcReadyEntities = _as.getReadyEntities();
				final Collection<Object> _callerLockEntities = callerSideToken.getLockEntities();
				final Collection<Object> _srcLockEntities = _as.getLockEntities();
				_result = (_callerReadyEntities != null && _srcReadyEntities != null && CollectionUtils.containsAny(
						_callerReadyEntities, _srcReadyEntities))
						|| (_callerLockEntities != null && _srcLockEntities != null && CollectionUtils.containsAny(
								_callerLockEntities, _srcLockEntities));
			}
		} else {
			_result = false;
		}
		return !_result;
	}

	/**
	 * Checks if the caller side token should be discarded.
	 * 
	 * @param callerSideToken of interest.
	 * @param initialResult is the collection that needs to be updated.
	 * @return <code>true</code> if the token should be discarded; <code>false</code>, otherwise.
	 */
	private boolean shouldCallerSideTokenBeDiscardedWhenSrcStmtIsNull(final AliasSet callerSideToken,
			final boolean initialResult) {
		final AliasSet _as;
		final SootMethod _sm = (SootMethod) getInfoFor(Identifiers.SRC_METHOD);
		if (_sm.isStatic()) {
			_as = ecba.queryAliasSetFor(_sm.getDeclaringClass());
		} else {
			_as = ecba.queryAliasSetForThis(_sm);
		}

		boolean _result = initialResult;
		if (_as != null) {
			if (readyBased) {
				final Collection<Object> _callerReadyEntities = callerSideToken.getReadyEntities();
				final Collection<Object> _srcReadyEntities = _as.getReadyEntities();
				final Collection<Object> _callerLockEntities = callerSideToken.getLockEntities();
				final Collection<Object> _srcLockEntities = _as.getLockEntities();
				_result = (_callerReadyEntities != null && _srcReadyEntities != null && CollectionUtils.containsAny(
						_callerReadyEntities, _srcReadyEntities))
						|| (_callerLockEntities != null && _srcLockEntities != null && CollectionUtils.containsAny(
								_callerLockEntities, _srcLockEntities));
			}
		} else {
			_result = false;
		}
		return !_result;
	}

}

// End of File
