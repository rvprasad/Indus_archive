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
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.impl.DataAliasBasedCallingContextRetriever;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Value;

/**
 * This implementation provides program-point-relative intra-thread calling contexts based on equivalence-class based
 * information.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DataAliasBasedCallingContextRetrieverV2
		extends DataAliasBasedCallingContextRetriever {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DataAliasBasedCallingContextRetrieverV2.class);

	/**
	 * This guides calling context construction.
	 */
	protected EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Creates an instance of this instance.
	 * 
	 * @param callingContextLengthLimit <i>refer to the constructor of the super class</i>.
	 */
	public DataAliasBasedCallingContextRetrieverV2(final int callingContextLengthLimit) {
		super(callingContextLengthLimit);
	}

	/**
	 * Sets the object that guides calling context construction.
	 * 
	 * @param oracle to be used.
	 * @pre oracle != null
	 */
	public void setECBA(final EquivalenceClassBasedEscapeAnalysis oracle) {
		ecba = oracle;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DataAliasBasedCallingContextRetriever#considerProgramPoint(edu.ksu.cis.indus.processing.Context)
	 */
	@Override protected final boolean considerProgramPoint(final Context programPointContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint(Context programPointContext = " + programPointContext + ") - BEGIN");
		}

		boolean _result = super.considerProgramPoint(programPointContext);

		if (_result) {
			final Value _value = programPointContext.getProgramPoint().getValue();
			final AliasSet _as = ecba.queryAliasSetFor(_value, programPointContext.getCurrentMethod());

			if (_as != null) {
				final Collection<Object> _o = _as.getIntraProcRefEntities();
				if (_o != null) {
					_result = shouldConsiderCallerSideToken(_o);
				}
			} else {
				_result = !EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(_value.getType());
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint() - END - return value = " + _result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DataAliasBasedCallingContextRetriever#getCallerSideToken(java.lang.Object, soot.SootMethod,
	 *      edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple, edu.ksu.cis.indus.common.collections.Stack)
	 */
	@Override protected Object getCallerSideToken(final Object token, final SootMethod callee, final CallTriple callsite,
			@SuppressWarnings("unused") final Stack<CallTriple> calleeCallStack) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallerSideToken(Object token = " + token + ", SootMethod callee = " + callee
					+ ", CallTriple callsite = " + callsite + ") - BEGIN");
		}

		Object _result = Tokens.DISCARD_CONTEXT_TOKEN;

		if (!Util.isStartMethod(callee)) {
			final AliasSet _as = ecba.getCallerSideAliasSet((AliasSet) token, callee, callsite);
			if (_as != null) {
				final Collection<Object> _c1 = ((AliasSet) token).getIntraProcRefEntities();
				final Collection<Object> _c2 = _as.getIntraProcRefEntities();
				if (_c1 != null && _c2 != null && CollectionUtils.containsAny(_c1, _c2) && shouldConsiderCallerSideToken(_c2)
						&& !getCallSitesThatCanReachSource(callsite.getMethod()).isEmpty()) {
					_result = _as;
				}
			} else {
				_result = Tokens.ACCEPT_TERMINAL_CONTEXT_TOKEN;
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallerSideToken() - END - return value = " + _result);
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DataAliasBasedCallingContextRetriever#getTokenForProgramPoint(edu.ksu.cis.indus.processing.Context)
	 */
	@Override protected Object getTokenForProgramPoint(final Context programPointContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint(Context programPointContext = " + programPointContext + ") - BEGIN");
		}

		final Value _value = programPointContext.getProgramPoint().getValue();
		final AliasSet _as = ecba.queryAliasSetFor(_value, programPointContext.getCurrentMethod());
		final Object _result;
		if (_as != null) {
			_result = _as;
		} else {
			if (EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(_value.getType())) {
				_result = Tokens.DISCARD_CONTEXT_TOKEN;
			} else {
				_result = Tokens.CONSIDER_ALL_CONTEXTS_TOKEN;
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint() - END - return value = " + _result);
		}
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param callerSideEntities DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	protected boolean shouldConsiderCallerSideToken(final Collection<Object> callerSideEntities) {
		return !callerSideEntities.isEmpty();
	}
}

// End of File
