
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

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.AbstractCallingContextRetriever;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import edu.ksu.cis.indus.processing.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * This class provides facilities to retrieve context based on thread escape information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ThreadEscapeInfoBasedCallingContextRetriever
  extends AbstractCallingContextRetriever {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ThreadEscapeInfoBasedCallingContextRetriever.class);

	/** 
	 * This provides escapes information according to interface.
	 */
	protected IEscapeInfo escapesInfo;

	/** 
	 * This guides calling context construction.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Creates an instance of this instance.
	 *
	 * @param callContextLenLimit <i>refer to the constructor of the super class</i>.
	 */
	public ThreadEscapeInfoBasedCallingContextRetriever(final int callContextLenLimit) {
		super(callContextLenLimit);
	}

	/**
	 * Sets the object that guides calling context construction.
	 *
	 * @param oracle to be used.
	 *
	 * @pre oracle != null
	 */
	public void setECBA(final EquivalenceClassBasedEscapeAnalysis oracle) {
		ecba = oracle;
	}

	/**
	 * Sets the escape analysis.
	 *
	 * @param info to be used.
	 *
	 * @pre oracle != null
	 */
	public void setEscapeInfo(final IEscapeInfo info) {
		escapesInfo = info;
	}

	/**
	 * Retrieves the escape analysis.
	 *
	 * @return the escape analysis.
	 */
	protected final IEscapeInfo getEscapeInfo() {
		return escapesInfo;
	}

	/**
	 * @see AbstractCallingContextRetriever#getCallerSideToken(Object, SootMethod, CallTriple)
	 */
	protected Object getCallerSideToken(final Object token, final SootMethod callee, final CallTriple callsite) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallerSideToken(callee = " + callee + ", callsite = " + callsite + ")");
		}

		final AliasSet _as = ecba.getCallerSideAliasSet((AliasSet) token, callee, callsite);
		final AliasSet _result;

		if (_as != null && _as.escapes()) {
			_result = (AliasSet) _as.find();
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#getTokenForProgramPoint(edu.ksu.cis.indus.processing.Context)
	 */
	protected Object getTokenForProgramPoint(final Context context) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint(context = " + context + ")");
		}

		final AliasSet _as = ecba.getAliasSetFor(context.getProgramPoint().getValue(), context.getCurrentMethod());
		final Object _result;

		if (_as != null && !_as.isGlobal()) {
			_result = (AliasSet) _as.find();
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#getTokenForThis(Context)
	 */
	protected Object getTokenForThis(final Context methodContext) {
		final SootMethod _method = methodContext.getCurrentMethod();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForThis(method = " + _method + ")");
		}

		final AliasSet _as = ecba.getAliasSetForThis(_method);
        final Object _result;
        if (_as != null) {
            _result = _as.find();
        } else {
            _result = null;
        }
        return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#considerProgramPoint(edu.ksu.cis.indus.processing.Context)
	 */
	protected boolean considerProgramPoint(final Context context) {
		final Value _value = context.getProgramPoint().getValue();
		final Stmt _stmt = context.getStmt();
		Value _r = null;
		final boolean _result;

		if (_stmt.containsFieldRef()) {
            final FieldRef _fr = _stmt.getFieldRef();
            if ((_fr instanceof InstanceFieldRef && ((InstanceFieldRef) _value).getBase() == _value) || 
                    (_fr instanceof StaticFieldRef && _fr == _value)) {
                _r = _value;
            }
		} else if (_stmt.containsArrayRef() && _stmt.getArrayRef().getBase() == _value) {
			_r = _value;
		} else if (_stmt instanceof MonitorStmt && ((MonitorStmt) _stmt).getOp() == _value) {
			_r = _value;
		} else if (_stmt.containsInvokeExpr()) {
		    final InvokeExpr _ex = _stmt.getInvokeExpr();
			  if(_ex instanceof InstanceFieldRef && (Util.isWaitMethod(_ex.getMethod())
			  || Util.isNotifyMethod(_ex.getMethod())) && ((VirtualInvokeExpr) _ex).getBase() == _value) 
			_r = _value;
		}

		if (_r == null) {
			_result = false;
		} else {
			_result = escapesInfo.escapes(_r, context.getCurrentMethod());
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint() - result =" + _result);
		}

		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#considerThis(Context)
	 */
	protected boolean considerThis(final Context methodContext) {
		final SootMethod _method = methodContext.getCurrentMethod();
		final boolean _result = _method.isStatic() || escapesInfo.thisEscapes(_method);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerThis() -  : _result = " + _result);
		}

		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#shouldConsiderUnextensibleStacksAt(Object, SootMethod, CallTriple)
	 */
	protected boolean shouldConsiderUnextensibleStacksAt(final Object calleeToken, final SootMethod callee,
		final CallTriple callSite) {
		return ((AliasSet) calleeToken).isGlobal();
	}
}

// End of File
