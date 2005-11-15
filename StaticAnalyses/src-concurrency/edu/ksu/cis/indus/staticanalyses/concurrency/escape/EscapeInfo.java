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
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(EscapeInfo.class);

	/**
	 * This is the default verdict for escapes queries.
	 */
	boolean escapesDefaultValue;

	/**
	 * This is the default verdict for lock-unlock sharing.
	 */
	boolean lockunlockDefaultValue;

	/**
	 * This is the default verdict for read-write sharing.
	 */
	boolean readwriteDefaultValue;

	/**
	 * This is the default verdict for wait-notify sharing.
	 */
	boolean waitnotifyDefaultValue;

	/**
	 * This is the default verdict for write-write sharing.
	 */
	boolean writewriteDefaultValue;

	/**
	 * The creating/containing object.
	 */
	private final EquivalenceClassBasedEscapeAnalysis analysis;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param instance that creates this instance.
	 * @pre instance != null
	 */
	EscapeInfo(final EquivalenceClassBasedEscapeAnalysis instance) {
		this.analysis = instance;
		escapesDefaultValue = true;
		readwriteDefaultValue = true;
		writewriteDefaultValue = true;
		lockunlockDefaultValue = true;
		waitnotifyDefaultValue = true;
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
			final AliasSet _a1 = getAliasSetForIn(local1, method1);
			final AliasSet _a2 = getAliasSetForIn(local2, method2);
			final Collection _a1LockEntities = _a1.getLockEntities();
			final Collection _a2LockEntities = _a2.getLockEntities();
			_result = _a1LockEntities != null && _a2LockEntities != null
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
			final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _trp1 = analysis.method2Triple
					.get(enterMethod);

			if (_trp1 == null) {
				throw new IllegalArgumentException(enterMethod + " was not processed.");
			}

			final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _trp2 = analysis.method2Triple
					.get(exitMethod);

			if (_trp2 == null) {
				throw new IllegalArgumentException(exitMethod + " was not processed.");
			}

			final AliasSet _n;

			if (enter == null) {
				_n = _trp1.getFirst().getThisAS();
			} else {
				_n = _trp1.getSecond().get(enter.getOp());
			}

			final AliasSet _x;

			if (exit == null) {
				_x = _trp2.getFirst().getThisAS();
			} else {
				_x = _trp2.getSecond().get(exit.getOp());
			}

			final Collection _xLockEntities = _x.getLockEntities();
			final Collection _nLockEntities = _n.getLockEntities();
			_result = _xLockEntities != null && _nLockEntities != null
					&& CollectionUtils.containsAny(_nLockEntities, _xLockEntities);
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#areWaitAndNotifyCoupled(InvokeStmt, SootMethod, InvokeStmt, SootMethod)
	 */
	public boolean areWaitAndNotifyCoupled(final InvokeStmt wait, final SootMethod waitMethod, final InvokeStmt notify,
			final SootMethod notifyMethod) {
		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _trp1 = analysis.method2Triple
				.get(waitMethod);

		if (_trp1 == null) {
			throw new IllegalArgumentException(waitMethod + " was not processed.");
		}

		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _trp2 = analysis.method2Triple
				.get(notifyMethod);

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
				final AliasSet _as1 = _trp1.getSecond().get(_wTemp.getBase());
				final AliasSet _as2 = _trp2.getSecond().get(_nTemp.getBase());

				if ((_as1.getReadyEntities() != null) && (_as2.getReadyEntities() != null)) {
					_result = CollectionUtils.containsAny(_as1.getReadyEntities(), _as2.getReadyEntities());
				} else {
					/*
					 * This is the case where a start site has wait and notify called on a reference. In such cases, wait and
					 * notify fields are set on the alias set but there is not alias set with set values to trigger the change
					 * of Entity field. Only if the start site is loop enclosed should these cases flag dependency by setting
					 * Entity.
					 */
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("There are wait()s and/or notify()s in this program without "
								+ "corresponding notify()s and/or wait()s that occur in different threads - " + wait + "@"
								+ waitMethod + " " + notify + "@" + notifyMethod);
					}
				}
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#escapes(SootClass, SootMethod)
	 */
	public boolean escapes(final SootClass sc, final SootMethod sm) {
		boolean _result = escapesDefaultValue;

		try {
			final AliasSet _as = this.analysis.queryAliasSetFor(sc);

			if (_as != null) {
				_result = _as.escapes();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + sc + " occurring in " + sm
						+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#escapes(Value, SootMethod)
	 */
	public boolean escapes(final Value v, final SootMethod sm) {
		boolean _result = escapesDefaultValue;

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = this.analysis.queryAliasSetFor(v, sm).escapes();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + v + " occurring in " + sm
						+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#fieldAccessShared(soot.Value, soot.SootMethod, Object)
	 */
	public boolean fieldAccessShared(final Value v, final SootMethod sm, final Object sharedAccessSort) {
		boolean _result = getDefaultValueForSharedAccessSort(sharedAccessSort);

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				if (sharedAccessSort.equals(IEscapeInfo.READ_WRITE_SHARED_ACCESS)) {
					_result = this.analysis.queryAliasSetFor(v, sm).readWriteShared();
				} else if (sharedAccessSort.equals(IEscapeInfo.WRITE_WRITE_SHARED_ACCESS)) {
					_result = this.analysis.queryAliasSetFor(v, sm).writeWriteShared();
				} else {
					throw new IllegalArgumentException("sharedAccessSort has to be either "
							+ "IEscapeInfo.WRITE_WRITE_SHARED_ACCESS or IEscapeInfo.READ_WRITE_SHARED_ACCESS");
				}
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + v + " occurring in " + sm
						+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#fieldAccessShared(soot.Value, soot.SootMethod, String, Object)
	 */
	public boolean fieldAccessShared(final Value v, final SootMethod sm, final String fieldSignature,
			final Object sharedAccessSort) {
		boolean _result = getDefaultValueForSharedAccessSort(sharedAccessSort);

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				if (sharedAccessSort.equals(IEscapeInfo.READ_WRITE_SHARED_ACCESS)) {
					_result = this.analysis.queryAliasSetFor(v, sm).readWriteShared(fieldSignature);
				} else if (sharedAccessSort.equals(IEscapeInfo.WRITE_WRITE_SHARED_ACCESS)) {
					_result = this.analysis.queryAliasSetFor(v, sm).writeWriteShared(fieldSignature);
				} else {
					throw new IllegalArgumentException("sharedAccessSort has to be either "
							+ "IEscapeInfo.WRITE_WRITE_SHARED_ACCESS or IEscapeInfo.READ_WRITE_SHARED_ACCESS");
				}
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + v + " occurring in " + sm
						+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#fieldAccessShared(Value, SootMethod, Value, SootMethod, Object)
	 */
	public boolean fieldAccessShared(final Value v1, final SootMethod sm1, final Value v2, final SootMethod sm2,
			final Object sharedAccessSort) {
		boolean _result = fieldAccessShared(v1, sm1, sharedAccessSort) && fieldAccessShared(v2, sm2, sharedAccessSort);

		if (_result) {
			try {
				if (sharedAccessSort.equals(IEscapeInfo.READ_WRITE_SHARED_ACCESS)) {
					final Collection _o1 = analysis.queryAliasSetFor(v1, sm1).getReadWriteShareEntities();
					final Collection _o2 = analysis.queryAliasSetFor(v2, sm2).getReadWriteShareEntities();
					_result = (_o1 != null) && (_o2 != null) && CollectionUtils.containsAny(_o1, _o2);
				} else if (sharedAccessSort.equals(IEscapeInfo.WRITE_WRITE_SHARED_ACCESS)) {
					final Collection _o1 = analysis.queryAliasSetFor(v1, sm1).getWriteWriteShareEntities();
					final Collection _o2 = analysis.queryAliasSetFor(v2, sm2).getWriteWriteShareEntities();
					_result = (_o1 != null) && (_o2 != null) && CollectionUtils.containsAny(_o1, _o2);
				} else {
					throw new IllegalArgumentException("sharedAccessSort has to be either "
							+ "IEscapeInfo.WRITE_WRITE_SHARED_ACCESS or IEscapeInfo.READ_WRITE_SHARED_ACCESS");
				}
			} catch (final NullPointerException _e) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("There is no information about " + v1 + "/" + v2 + " occurring in " + sm1 + "/" + sm2
							+ ".  So, providing pessimistic info (true).", _e);
				}
			}
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection<? extends Comparable<?>> getIds() {
		return Collections.singleton(IEscapeInfo.ID);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOf(int, soot.SootMethod)
	 */
	public Collection getReadingThreadsOf(final int paramIndex, final SootMethod method) {
		this.analysis.validate(paramIndex, method);

		final Collection _result;

		if (method.getParameterType(paramIndex) instanceof RefType) {
			final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
			_triple = this.analysis.method2Triple.get(method);

			if (_triple != null) {
				final MethodContext _ctxt = _triple.getFirst();
				_result = _ctxt.getParamAS(paramIndex).getReadThreads();
			} else {
				_result = Collections.emptySet();

				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No recorded information for " + method
							+ " is available.  Returning pessimistic (true) info.");
				}
			}
		} else {
			_result = Collections.emptySet();
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOf(soot.Local, soot.SootMethod)
	 */
	public Collection getReadingThreadsOf(final Local local, final SootMethod method) {
		Collection _result = Collections.emptySet();
		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);

		if (_triple != null) {
			final Map<Local, AliasSet> _local2as = _triple.getSecond();
			final AliasSet _as = _local2as.get(local);

			if (_as != null) {
				_result = _as.getReadThreads();
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getReadingThreadsOfThis(soot.SootMethod)
	 */
	public Collection getReadingThreadsOfThis(final SootMethod method) {
		this.analysis.validate(method);

		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);
		final Collection _result;

		if (_triple != null) {
			final MethodContext _ctxt = _triple.getFirst();
			_result = Collections.unmodifiableCollection(_ctxt.thisAS.getReadThreads());
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method + " is available.  Returning pessimistic (true) info.");
			}
			_result = Collections.emptySet();
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
			final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
			_triple = this.analysis.method2Triple.get(method);

			if (_triple != null) {
				final MethodContext _ctxt = _triple.getFirst();
				_result = _ctxt.getParamAS(paramIndex).getWriteThreads();
			} else {
				_result = Collections.emptySet();

				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No recorded information for " + method
							+ " is available.  Returning pessimistic (true) info.");
				}
			}
		} else {
			_result = Collections.emptySet();
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getWritingThreadsOf(soot.Local, soot.SootMethod)
	 */
	public Collection getWritingThreadsOf(final Local local, final SootMethod method) {
		Collection _result = Collections.emptySet();
		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);

		if (_triple != null) {
			final Map<Local, AliasSet> _local2as = _triple.getSecond();
			final AliasSet _as = _local2as.get(local);

			if (_as != null) {
				_result = _as.getWriteThreads();
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#getWritingThreadsOfThis(soot.SootMethod)
	 */
	public Collection getWritingThreadsOfThis(final SootMethod method) {
		this.analysis.validate(method);

		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);
		final Collection _result;

		if (_triple != null) {
			final MethodContext _ctxt = _triple.getFirst();
			_result = Collections.unmodifiableCollection(_ctxt.thisAS.getWriteThreads());
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No recorded information for " + method + " is available.  Returning pessimistic (true) info.");
			}
			_result = Collections.emptySet();
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#lockUnlockShared(soot.Value, soot.SootMethod)
	 */
	public boolean lockUnlockShared(final Value v, final SootMethod sm) {
		boolean _result = lockunlockDefaultValue;

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = this.analysis.queryAliasSetFor(v, sm).lockUnlockShared();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + v + " occurring in " + sm
						+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#staticfieldAccessShared(soot.SootClass, soot.SootMethod, Object)
	 */
	public boolean staticfieldAccessShared(final SootClass sc, final SootMethod sm, final Object sharedAccessSort) {
		boolean _result = getDefaultValueForSharedAccessSort(sharedAccessSort);

		try {
			final AliasSet _as = this.analysis.queryAliasSetFor(sc);

			if (_as != null) {
				if (sharedAccessSort.equals(IEscapeInfo.READ_WRITE_SHARED_ACCESS)) {
					_result = _as.readWriteShared();
				} else if (sharedAccessSort.equals(IEscapeInfo.WRITE_WRITE_SHARED_ACCESS)) {
					_result = _as.writeWriteShared();
				} else {
					throw new IllegalArgumentException("sharedAccessSort has to be either "
							+ "IEscapeInfo.WRITE_WRITE_SHARED_ACCESS or IEscapeInfo.READ_WRITE_SHARED_ACCESS");
				}
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + sc + " occurring in " + sm
						+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IEscapeInfo#staticfieldAccessShared(soot.SootClass, soot.SootMethod,
	 *      java.lang.String, Object)
	 */
	public boolean staticfieldAccessShared(final SootClass sc, final SootMethod sm, final String signature,
			final Object sharedAccessSort) {
		boolean _result = getDefaultValueForSharedAccessSort(sharedAccessSort);

		try {
			final AliasSet _as = this.analysis.queryAliasSetFor(sc);

			if (_as != null) {
				if (sharedAccessSort.equals(IEscapeInfo.READ_WRITE_SHARED_ACCESS)) {
					_result = _as.readWriteShared(signature);
				} else if (sharedAccessSort.equals(IEscapeInfo.WRITE_WRITE_SHARED_ACCESS)) {
					_result = _as.writeWriteShared(signature);
				} else {
					throw new IllegalArgumentException("sharedAccessSort has to be either "
							+ "IEscapeInfo.WRITE_WRITE_SHARED_ACCESS or IEscapeInfo.READ_WRITE_SHARED_ACCESS");
				}
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + sc + " occurring in " + sm
						+ ".  So, providing default value - " + _result, _e);
			}
		}

		return _result;
	}

	/**
	 * @see IEscapeInfo#thisEscapes(SootMethod)
	 */
	public boolean thisEscapes(final SootMethod method) {
		boolean _result = escapesDefaultValue;

		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);

		if (_triple == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + method + ".  So, providing default value - " + _result);
			}
		} else {
			final AliasSet _as = _triple.getFirst().getThisAS();

			// if non-static query the alias set of "this" variable. If static, just return true assuming that the
			// application to decide wisely :-)
			if (_as != null) {
				_result = _as.escapes();
			}
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#thisFieldAccessShared(SootMethod, Object)
	 */
	public boolean thisFieldAccessShared(final SootMethod method, final Object sharedAccessSort) {
		boolean _result = getDefaultValueForSharedAccessSort(sharedAccessSort);

		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);

		if (_triple == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + method + ".  So, providing default value - " + _result);
			}
		} else {
			final AliasSet _as = _triple.getFirst().getThisAS();

			// if non-static query the alias set of "this" variable. If static, just return true assuming that the
			// application to decide wisely :-)
			if (_as != null) {
				if (sharedAccessSort.equals(IEscapeInfo.READ_WRITE_SHARED_ACCESS)) {
					_result = _as.readWriteShared();
				} else if (sharedAccessSort.equals(IEscapeInfo.WRITE_WRITE_SHARED_ACCESS)) {
					_result = _as.writeWriteShared();
				} else {
					throw new IllegalArgumentException("sharedAccessSort has to be either "
							+ "IEscapeInfo.WRITE_WRITE_SHARED_ACCESS or IEscapeInfo.READ_WRITE_SHARED_ACCESS");
				}
			}
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#thisFieldAccessShared(SootMethod, String, Object)
	 */
	public boolean thisFieldAccessShared(final SootMethod method, final String signature, final Object sharedAccessSort) {
		boolean _result = getDefaultValueForSharedAccessSort(sharedAccessSort);

		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);

		if (_triple == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + method + ".  So, providing default value - " + _result);
			}
		} else {
			final AliasSet _as = _triple.getFirst().getThisAS();

			// if non-static query the alias set of "this" variable. If static, just return true assuming that the
			// application to decide wisely :-)
			if (_as != null) {
				if (sharedAccessSort.equals(IEscapeInfo.READ_WRITE_SHARED_ACCESS)) {
					_result = _as.readWriteShared(signature);
				} else if (sharedAccessSort.equals(IEscapeInfo.WRITE_WRITE_SHARED_ACCESS)) {
					_result = _as.writeWriteShared(signature);
				} else {
					throw new IllegalArgumentException("sharedAccessSort has to be either "
							+ "IEscapeInfo.WRITE_WRITE_SHARED_ACCESS or IEscapeInfo.READ_WRITE_SHARED_ACCESS");
				}
			}
		}
		return _result;
	}

	/**
	 * @see IEscapeInfo#thisLockUnlockShared(SootMethod)
	 */
	public boolean thisLockUnlockShared(final SootMethod method) {
		boolean _result = lockunlockDefaultValue;

		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);

		if (_triple == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + method + ".  So, providing default value - " + _result);
			}
		} else {
			final AliasSet _as1 = _triple.getFirst().getThisAS();

			// if non-static query the alias set of "this" variable. If static, just return true assuming that the
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
		boolean _result = waitnotifyDefaultValue;

		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _triple;
		_triple = this.analysis.method2Triple.get(method);

		if (_triple == null) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + method + ".  So, providing default value - " + _result);
			}
		} else {
			final AliasSet _as1 = _triple.getFirst().getThisAS();

			// if non-static query the alias set of "this" variable. If static, just return true assuming that the
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
		boolean _result = waitnotifyDefaultValue;

		try {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(v.getType())) {
				_result = this.analysis.queryAliasSetFor(v, sm).waitNotifyShared();
			} else {
				_result = false;
			}
		} catch (final NullPointerException _e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There is no information about " + v + " occurring in " + sm
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

	/**
	 * Retrieves the alias set for the given local in the given method.
	 * 
	 * @param local of interest.
	 * @param method in which <code>local</code> occurs.
	 * @return the alias set if it exists.
	 * @throws IllegalArgumentException when either the method or the local was not processed.
	 * @pre local != null and method != null
	 */
	private AliasSet getAliasSetForIn(final Local local, final SootMethod method) throws IllegalArgumentException {
		final Triple<MethodContext, Map<Local, AliasSet>, Map<CallTriple, MethodContext>> _trp1 = analysis.method2Triple
				.get(method);

		if (_trp1 == null) {
			throw new IllegalArgumentException(method + " was not processed.");
		}

		final AliasSet _a1;

		if (local != null) {
			_a1 = _trp1.getSecond().get(local);
		} else {
			_a1 = _trp1.getFirst().getThisAS();
		}

		if (_a1 == null) {
			throw new IllegalArgumentException(local + " in " + method + " was not processed.");
		}
		return _a1;
	}

	/**
	 * Retrieves the default value for the given sort of shared access.
	 * 
	 * @param sharedAccessSort of interest.
	 * @return the default value.
	 * @pre shareadAccessSort != null
	 */
	private boolean getDefaultValueForSharedAccessSort(final Object sharedAccessSort) {
		if (sharedAccessSort.equals(IEscapeInfo.READ_WRITE_SHARED_ACCESS)) {
			return readwriteDefaultValue;
		} else if (sharedAccessSort.equals(IEscapeInfo.WRITE_WRITE_SHARED_ACCESS)) {
			return writewriteDefaultValue;
		} else {
			throw new IllegalArgumentException("sharedAccessSort has to be either "
					+ "IEscapeInfo.WRITE_WRITE_SHARED_ACCESS or IEscapeInfo.READ_WRITE_SHARED_ACCESS but was "
					+ sharedAccessSort);
		}
	}
}

// End of File
