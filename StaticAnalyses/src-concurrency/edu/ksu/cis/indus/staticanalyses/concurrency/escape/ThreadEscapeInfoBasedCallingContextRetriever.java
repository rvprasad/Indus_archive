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

import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.AbstractCallingContextRetriever;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ThreadEscapeInfoBasedCallingContextRetriever
		extends AbstractCallingContextRetriever {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadEscapeInfoBasedCallingContextRetriever.class);

	/**
	 * This provides escapes information according to interface.
	 */
	protected IEscapeInfo escapesInfo;

	/**
	 * This guides calling context construction.
	 */
	protected EquivalenceClassBasedEscapeAnalysis ecba;

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
	 * @pre oracle != null
	 */
	public void setECBA(final EquivalenceClassBasedEscapeAnalysis oracle) {
		ecba = oracle;
	}

	/**
	 * Sets the escape analysis.
	 * 
	 * @param info to be used.
	 * @pre oracle != null
	 */
	public void setEscapeInfo(final IEscapeInfo info) {
		escapesInfo = info;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractCallingContextRetriever#considerProgramPoint(edu.ksu.cis.indus.processing.Context)
	 */
	@Override protected boolean considerProgramPoint(final Context context) {
		final Value _value = context.getProgramPoint().getValue();
		final SootMethod _currentMethod = context.getCurrentMethod();
		final boolean _result;
		if (_value instanceof StaticFieldRef) {
			_result = escapesInfo.escapes(((StaticFieldRef) _value).getField().getDeclaringClass(), _currentMethod);
		} else {
			_result = escapesInfo.escapes(_value, _currentMethod);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint() - result =" + _result);
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractCallingContextRetriever#considerThis(Context)
	 */
	@Override protected boolean considerThis(final Context methodContext) {
		final SootMethod _method = methodContext.getCurrentMethod();
		final boolean _result;

		if (_method.isStatic()) {
			_result = escapesInfo.escapes(_method.getDeclaringClass(), _method);
		} else {
			_result = escapesInfo.thisEscapes(_method);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerThis() -  : _result = " + _result);
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractCallingContextRetriever#getCallerSideToken(Object, SootMethod,
	 *      edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple, Stack)
	 */
	@Override protected Object getCallerSideToken(final Object token, final SootMethod callee, final CallTriple callsite,
			@SuppressWarnings("unused") final Stack<CallTriple> calleeCallStack) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallerSideToken(callee = " + callee + ", callsite = " + callsite + ")");
		}

		final AliasSet _as = ecba.getCallerSideAliasSet((AliasSet) token, callee, callsite);
		final Object _result;

		if (_as != null && _as.escapes()) {
			_result = _as.find();
		} else if (Util.isStartMethod(callee)) {
			_result = Tokens.ACCEPT_NON_TERMINAL_CONTEXT_TOKEN;
		} else {
			_result = Tokens.DISCARD_CONTEXT_TOKEN;
		}
		return _result;
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
	 * {@inheritDoc}
	 * 
	 * @see AbstractCallingContextRetriever#getTokenForProgramPoint(edu.ksu.cis.indus.processing.Context)
	 */
	@Override protected Object getTokenForProgramPoint(final Context context) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint(context = " + context + ")");
		}

		final Value _value = context.getProgramPoint().getValue();
		final AliasSet _as;

		if (_value instanceof StaticFieldRef) {
			_as = ecba.queryAliasSetFor(((StaticFieldRef) _value).getField().getDeclaringClass());
		} else {
			_as = ecba.queryAliasSetFor(_value, context.getCurrentMethod());
		}

		return prepareToken(_as);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see AbstractCallingContextRetriever#getTokenForThis(Context)
	 */
	@Override protected Object getTokenForThis(final Context methodContext) {
		final SootMethod _method = methodContext.getCurrentMethod();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForThis(method = " + _method + ")");
		}

		final AliasSet _as;
		if (_method.isStatic()) {
			_as = ecba.queryAliasSetFor(_method.getDeclaringClass());
		} else {
			_as = ecba.queryAliasSetForThis(_method);
		}
		return prepareToken(_as);
	}

	/**
	 * Prepare a token based on the given alias set.
	 * 
	 * @param as is the alias set of interest.
	 * @return the token based on the given alias set.
	 * @pre as != null
	 */
	private Object prepareToken(final AliasSet as) {
		final Object _result;

		if (as != null) {
			_result = as.find();
		} else {
			_result = Tokens.DISCARD_CONTEXT_TOKEN;
		}
		return _result;
	}
}

// End of File
