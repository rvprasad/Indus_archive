
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

import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.AbstractStatus;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.RefType;
import soot.SootMethod;
import soot.Value;

import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class provides implementation of <code>IEscapeInfo</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class EscapeInfo
  extends AbstractStatus
  implements IEscapeInfo {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(EscapeInfo.class);

	/** 
	 * The creating/containing object.
	 */
	private final EquivalenceClassBasedEscapeAnalysis analysis;

	/**
	 * Creates an instance of this class.
	 *
	 * @param instance that creates this instance.
	 *
	 * @pre instance != null
	 */
	EscapeInfo(final EquivalenceClassBasedEscapeAnalysis instance) {
		this.analysis = instance;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IEscapeInfo.ID);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOf(soot.Local, soot.SootMethod)
	 */
	public Collection getReadingThreadsOf(final Local local, final SootMethod method) {
		Collection _result = Collections.EMPTY_SET;
		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);

		if (_triple != null) {
			final Map _local2as = (Map) _triple.getSecond();
			final AliasSet _as = (AliasSet) _local2as.get(local);

			if (_as != null) {
				_result = _as.getReadThreads();
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOf(int, soot.SootMethod)
	 */
	public Collection getReadingThreadsOf(final int paramIndex, final SootMethod method) {
		this.analysis.validate(paramIndex, method);

		final Collection _result;

		if (method.getParameterType(paramIndex) instanceof RefType) {
			final Triple _triple = (Triple) this.analysis.method2Triple.get(method);

			if (_triple != null) {
				final MethodContext _ctxt = (MethodContext) _triple.getFirst();
				_result = _ctxt.getParamAS(paramIndex).getReadThreads();
			} else {
				_result = Collections.EMPTY_SET;

				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No recorded information for " + method
						+ " is available.  Returning pessimistic (true) info.");
				}
			}
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOfThis(soot.SootMethod)
	 */
	public Collection getReadingThreadsOfThis(final SootMethod method) {
		this.analysis.validate(method);

		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);
		final Collection _result;

		if (_triple != null) {
			final MethodContext _ctxt = (MethodContext) _triple.getFirst();
			_result = Collections.unmodifiableCollection(_ctxt.thisAS.getReadThreads());
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method + " is available.  Returning pessimistic (true) info.");
			}
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getWritingThreadsOf(soot.Local, soot.SootMethod)
	 */
	public Collection getWritingThreadsOf(final Local local, final SootMethod method) {
		Collection _result = Collections.EMPTY_SET;
		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);

		if (_triple != null) {
			final Map _local2as = (Map) _triple.getSecond();
			final AliasSet _as = (AliasSet) _local2as.get(local);

			if (_as != null) {
				_result = _as.getWriteThreads();
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getWritingThreadsOf(int, soot.SootMethod)
	 */
	public Collection getWritingThreadsOf(final int paramIndex, final SootMethod method) {
		this.analysis.validate(paramIndex, method);

		final Collection _result;

		if (method.getParameterType(paramIndex) instanceof RefType) {
			final Triple _triple = (Triple) this.analysis.method2Triple.get(method);

			if (_triple != null) {
				final MethodContext _ctxt = (MethodContext) _triple.getFirst();
				_result = _ctxt.getParamAS(paramIndex).getWriteThreads();
			} else {
				_result = Collections.EMPTY_SET;

				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No recorded information for " + method
						+ " is available.  Returning pessimistic (true) info.");
				}
			}
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getWritingThreadsOfThis(soot.SootMethod)
	 */
	public Collection getWritingThreadsOfThis(final SootMethod method) {
		this.analysis.validate(method);

		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);
		final Collection _result;

		if (_triple != null) {
			final MethodContext _ctxt = (MethodContext) _triple.getFirst();
			_result = Collections.unmodifiableCollection(_ctxt.thisAS.getWriteThreads());
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method + " is available.  Returning pessimistic (true) info.");
			}
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#areCoupledViaLocking(soot.Local, soot.SootMethod, soot.Local, soot.SootMethod)
	 */
	public boolean areCoupledViaLocking(final Local local1, final SootMethod method1, final Local local2,
		final SootMethod method2) {
		final boolean _result;

		if (local1 == null && local2 == null && method1.isStatic() && method2.isStatic()) {
			_result = method1.getDeclaringClass().equals(method2.getDeclaringClass());
		} else if ((local1 == null && method1.isStatic()) ^ (local2 == null && method2.isStatic())) {
			_result = true;
		} else {
			final Triple _trp1 = (Triple) analysis.method2Triple.get(method1);

			if (_trp1 == null) {
				throw new IllegalArgumentException(method1 + " was not processed.");
			}

			final AliasSet _a1;

			if (local1 != null) {
				_a1 = (AliasSet) ((Map) _trp1.getSecond()).get(local1);
			} else {
				_a1 = ((MethodContext) _trp1.getFirst()).getThisAS();
			}

			if (_a1 == null) {
				throw new IllegalArgumentException(local1 + " in " + method1 + " was not processed.");
			}

			final Triple _trp2 = (Triple) analysis.method2Triple.get(method2);

			if (_trp2 == null) {
				throw new IllegalArgumentException(method2 + " was not processed.");
			}

			final AliasSet _a2;

			if (local2 != null) {
				_a2 = (AliasSet) ((Map) _trp2.getSecond()).get(local2);
			} else {
				_a2 = ((MethodContext) _trp2.getFirst()).getThisAS();
			}

			if (_a2 == null) {
				throw new IllegalArgumentException(local2 + " in " + method2 + " was not processed.");
			}

			final Collection _a1LockEntities = _a1.getLockEntities();
			final Collection _a2LockEntities = _a2.getLockEntities();
			_result =
				_a1LockEntities != null && _a2LockEntities != null
				  && CollectionUtils.containsAny(_a1LockEntities, _a2LockEntities);
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#areMonitorsCoupled(MonitorStmt, SootMethod, MonitorStmt, SootMethod)
	 */
	public boolean areMonitorsCoupled(final MonitorStmt enter, final SootMethod enterMethod, final MonitorStmt exit,
		final SootMethod exitMethod) {
		final boolean _result;

		if (enterMethod.isStatic() || exitMethod.isStatic()) {
			_result = true;
		} else {
			final Triple _trp1 = (Triple) analysis.method2Triple.get(enterMethod);

			if (_trp1 == null) {
				throw new IllegalArgumentException(enterMethod + " was not processed.");
			}

			final Triple _trp2 = (Triple) analysis.method2Triple.get(exitMethod);

			if (_trp2 == null) {
				throw new IllegalArgumentException(exitMethod + " was not processed.");
			}

			final AliasSet _n;

			if (enter == null) {
				_n = ((MethodContext) _trp1.getFirst()).getThisAS();
			} else {
				_n = (AliasSet) ((Map) _trp1.getSecond()).get(enter.getOp());
			}

			final AliasSet _x;

			if (exit == null) {
				_x = ((MethodContext) _trp2.getFirst()).getThisAS();
			} else {
				_x = (AliasSet) ((Map) _trp2.getSecond()).get(exit.getOp());
			}

			final Collection _xLockEntities = _x.getLockEntities();
			final Collection _nLockEntities = _n.getLockEntities();
			_result =
				_xLockEntities != null && _nLockEntities != null
				  && CollectionUtils.containsAny(_nLockEntities, _xLockEntities);
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#areWaitAndNotifyCoupled(InvokeStmt, SootMethod, InvokeStmt, SootMethod)
	 */
	public boolean areWaitAndNotifyCoupled(final InvokeStmt wait, final SootMethod waitMethod, final InvokeStmt notify,
		final SootMethod notifyMethod) {
		final Triple _trp1 = (Triple) analysis.method2Triple.get(waitMethod);

		if (_trp1 == null) {
			throw new IllegalArgumentException(waitMethod + " was not processed.");
		}

		final Triple _trp2 = (Triple) analysis.method2Triple.get(notifyMethod);

		if (_trp2 == null) {
			throw new IllegalArgumentException(notifyMethod + " was not processed.");
		}

		final InvokeExpr _wi = wait.getInvokeExpr();
		final InvokeExpr _ni = notify.getInvokeExpr();
		boolean _result = false;

		if (_wi instanceof VirtualInvokeExpr && _ni instanceof VirtualInvokeExpr) {
			final VirtualInvokeExpr _wTemp = (VirtualInvokeExpr) _wi;
			final VirtualInvokeExpr _nTemp = (VirtualInvokeExpr) _ni;
			final SootMethod _wSM = _wTemp.getMethod();
			final SootMethod _nSM = _nTemp.getMethod();

			if (Util.isWaitMethod(_wSM) && Util.isNotifyMethod(_nSM)) {
				final AliasSet _as1 = (AliasSet) ((Map) _trp1.getSecond()).get(_wTemp.getBase());
				final AliasSet _as2 = (AliasSet) ((Map) _trp2.getSecond()).get(_nTemp.getBase());

				if ((_as1.getReadyEntities() != null) && (_as2.getReadyEntities() != null)) {
					_result = CollectionUtils.containsAny(_as1.getReadyEntities(), _as2.getReadyEntities());
				} else {
					/*
					 * This is the case where a start site has wait and notify called on a reference.
					 * In such cases, wait and notify fields are set on the alias set but there is not alias set
					 * with set values to trigger the change of Entity field.
					 * Only if the start site is loop enclosed should these cases flag dependency by setting Entity.
					 */
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn(
							"There are wait()s and/or notify()s in this program without corresponding notify()s and/or "
							+ "wait()s that occur in different threads - " + wait + "@" + waitMethod + " " + notify + "@"
							+ notifyMethod);
					}
				}
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#escapes(Value, SootMethod)
	 */
	public boolean escapes(final Value v, final SootMethod sm) {
		boolean _result = this.analysis.escapesDefaultValue;

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = this.analysis.getAliasSetFor(v, sm).escapes();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#fieldAccessShared(Value, SootMethod, Value, SootMethod)
	 */
	public boolean fieldAccessShared(final Value v1, final SootMethod sm1, final Value v2, final SootMethod sm2) {
		boolean _result = fieldAccessShared(v1, sm1) && fieldAccessShared(v2, sm2);

		if (_result && !(v1 instanceof StaticFieldRef) && !(v2 instanceof StaticFieldRef)) {
			try {
				final Collection _o1 = analysis.getAliasSetFor(v1, sm1).getShareEntities();
				final Collection _o2 = analysis.getAliasSetFor(v2, sm2).getShareEntities();
				_result = (_o1 != null) && (_o2 != null) && CollectionUtils.containsAny(_o1, _o2);
			} catch (final NullPointerException _e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("There is no information about " + v1 + "/" + v2 + " occurring in " + sm1 + "/" + sm2
						+ ".  So, providing pessimistic info (true).", _e);
				}
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#fieldAccessShared(soot.Value, soot.SootMethod)
	 */
	public boolean fieldAccessShared(final Value v, final SootMethod sm) {
		boolean _result = this.analysis.escapesDefaultValue;

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = this.analysis.getAliasSetFor(v, sm).readWriteShared();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

    /**
     * @see IEscapeInfo#fieldAccessShared(soot.Value, soot.SootMethod, String)
     */
    public boolean fieldAccessShared(final Value v, final SootMethod sm, final String fieldSignature) {
        boolean _result = this.analysis.escapesDefaultValue;

        try {
            if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
                _result = this.analysis.getAliasSetFor(v, sm).readWriteShared(fieldSignature);
            } else {
                _result = false;
            }
        } catch (final NullPointerException _e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("There is no information about " + v + " occurring in " + sm
                    + ".  So, providing default value - " + _result, _e);
            }
        }

        return _result;
    }
    
    
	/**
	 * @see IEscapeInfo#lockUnlockShared(soot.Value, soot.SootMethod)
	 */
	public boolean lockUnlockShared(final Value v, final SootMethod sm) {
		boolean _result = this.analysis.escapesDefaultValue;

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = this.analysis.getAliasSetFor(v, sm).lockUnlockShared();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#thisEscapes(SootMethod)
	 */
	public boolean thisEscapes(final SootMethod method) {
		boolean _result = this.analysis.escapesDefaultValue;
		;

		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);

		if (_triple == null && LOGGER.isDebugEnabled()) {
			LOGGER.debug("There is no information about " + method + ".  So, providing default value - " + _result);
		} else {
			final AliasSet _as1 = ((MethodContext) _triple.getFirst()).getThisAS();

			// if non-static query the alias set of "this" variable.  If static, just return true assuming that the 
			// application to decide wisely :-)
			if (_as1 != null) {
				_result = _as1.escapes();
			}
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#thisFieldAccessShared(SootMethod)
	 */
	public boolean thisFieldAccessShared(final SootMethod method) {
		boolean _result = this.analysis.escapesDefaultValue;

		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);

		if (_triple == null && LOGGER.isDebugEnabled()) {
			LOGGER.debug("There is no information about " + method + ".  So, providing default value - " + _result);
		} else {
			final AliasSet _as1 = ((MethodContext) _triple.getFirst()).getThisAS();

			// if non-static query the alias set of "this" variable.  If static, just return true assuming that the 
			// application to decide wisely :-)
			if (_as1 != null) {
				_result = _as1.readWriteShared();
			}
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#thisLockUnlockShared(SootMethod)
	 */
	public boolean thisLockUnlockShared(final SootMethod method) {
		boolean _result = this.analysis.escapesDefaultValue;
		;

		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);

		if (_triple == null && LOGGER.isDebugEnabled()) {
			LOGGER.debug("There is no information about " + method + ".  So, providing default value - " + _result);
		} else {
			final AliasSet _as1 = ((MethodContext) _triple.getFirst()).getThisAS();

			// if non-static query the alias set of "this" variable.  If static, just return true assuming that the 
			// application to decide wisely :-)
			if (_as1 != null) {
				_result = _as1.lockUnlockShared();
			}
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#thisWaitNotifyShared(SootMethod)
	 */
	public boolean thisWaitNotifyShared(final SootMethod method) {
		boolean _result = this.analysis.escapesDefaultValue;
		;

		final Triple _triple = (Triple) this.analysis.method2Triple.get(method);

		if (_triple == null && LOGGER.isDebugEnabled()) {
			LOGGER.debug("There is no information about " + method + ".  So, providing default value - " + _result);
		} else {
			final AliasSet _as1 = ((MethodContext) _triple.getFirst()).getThisAS();

			// if non-static query the alias set of "this" variable.  If static, just return true assuming that the 
			// application to decide wisely :-)
			if (_as1 != null) {
				_result = _as1.waitNotifyShared();
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#waitNotifyShared(soot.Value, soot.SootMethod)
	 */
	public boolean waitNotifyShared(final Value v, final SootMethod sm) {
		boolean _result = this.analysis.escapesDefaultValue;

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = this.analysis.getAliasSetFor(v, sm).waitNotifyShared();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("There is no information about " + v + " occurring in " + sm
					+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * This exposes <code>super.stable</code>.
	 */
	void stableAdapter() {
		super.stable();
	}

	/**
	 * This exposes <code>super.unstable</code>.
	 */
	void unstableAdapter() {
		super.unstable();
	}
}

// End of File
