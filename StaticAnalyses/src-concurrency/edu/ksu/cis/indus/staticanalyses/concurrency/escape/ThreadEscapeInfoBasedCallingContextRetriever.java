
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

import edu.ksu.cis.indus.interfaces.AbstractCallingContextRetriever;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Value;

import soot.jimple.StaticFieldRef;


/**
 * This class provides facilities to retrieve context based on thread escape information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ThreadEscapeInfoBasedCallingContextRetriever
  extends AbstractCallingContextRetriever {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ThreadEscapeInfoBasedCallingContextRetriever.class);

	/** 
	 * This provides escape information.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Sets the escape information provider.
	 *
	 * @param escapeAnalysis to be used.
	 *
	 * @pre escapeAnalysis != null
	 */
	public void setECBA(final EquivalenceClassBasedEscapeAnalysis escapeAnalysis) {
		ecba = escapeAnalysis;
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
			_result = _as;
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
		final AliasSet _result;

		if (_as != null && !_as.isGlobal()) {
			_result = (AliasSet) _as.find();
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#getTokenForThis(soot.SootMethod)
	 */
	protected Object getTokenForThis(final SootMethod method) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getTokenForThis(method = " + method + ")");
        }

		return ecba.getAliasSetForThis(method).find();
	}

	/**
	 * @see AbstractCallingContextRetriever#considerProgramPoint(edu.ksu.cis.indus.processing.Context)
	 */
	protected boolean considerProgramPoint(final Context context) {
		final Value _value = context.getProgramPoint().getValue();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint() - result =" + ecba.escapes(_value, context.getCurrentMethod()));
		}

		return (_value instanceof StaticFieldRef) || (ecba.escapes(_value, context.getCurrentMethod()));
	}

	/**
	 * @see AbstractCallingContextRetriever#considerThis(soot.SootMethod)
	 */
	protected boolean considerThis(final SootMethod method) {
		return !method.isStatic() && ecba.thisEscapes(method);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.AbstractCallingContextRetriever#shouldUnextendedStacksBeConsidered()
	 */
	protected boolean shouldUnextendedStacksBeConsidered() {
		return true;
	}
}

// End of File
