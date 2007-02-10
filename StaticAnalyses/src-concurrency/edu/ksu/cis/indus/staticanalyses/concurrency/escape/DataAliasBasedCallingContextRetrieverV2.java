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
						&& getCallSitesThatCanReachSource(callsite, true)) {
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
	 * Checks if the given caller site entities should be considered for further processing. 
	 * 
	 * @param callerSideEntities to be judged.
	 * @return <code>true</code> if they are to be considered; <code>false</code>, otherwise.
	 */
	protected boolean shouldConsiderCallerSideToken(final Collection<Object> callerSideEntities) {
		return !callerSideEntities.isEmpty();
	}
}

// End of File
