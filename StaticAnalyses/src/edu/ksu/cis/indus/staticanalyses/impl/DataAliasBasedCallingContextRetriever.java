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

package edu.ksu.cis.indus.staticanalyses.impl;

import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.AbstractCallingContextRetriever;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.ConcreteRef;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;

/**
 * This implementation provides program-point-relative intra-thread calling contexts.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DataAliasBasedCallingContextRetriever
		extends AbstractCallingContextRetriever {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DataAliasBasedCallingContextRetriever.class);

	/**
	 * The CFG analysis to be used.
	 */
	private CFGAnalysis analysis;

	/**
	 * This stores the call sites visited while constructing the call stack.
	 */
	private final Collection<CallTriple> previous = new HashSet<CallTriple>();

	/**
	 * The thread graph to be used.
	 */
	private IThreadGraphInfo tgi;

	/**
	 * Creates an instance of this instance.
	 * 
	 * @param callingContextLengthLimit <i>refer to the constructor of the super class</i>.
	 */
	public DataAliasBasedCallingContextRetriever(final int callingContextLengthLimit) {
		super(callingContextLengthLimit);
	}

	/**
	 * Sets the CFG based analysis to be used.
	 * 
	 * @param cfgAnalysis to be used.
	 * @pre cfgAnalysis != null
	 */
	public final void setCfgAnalysis(final CFGAnalysis cfgAnalysis) {
		analysis = cfgAnalysis;
	}

	/**
	 * Sets the thread graph information provided.
	 * 
	 * @param threadgraph provides thread graph information.
	 * @pre threadgraph != null
	 */
	public final void setThreadGraph(final IThreadGraphInfo threadgraph) {
		tgi = threadgraph;
	}

	/**
	 * @see AbstractCallingContextRetriever#considerProgramPoint(Context)
	 */
	@Override protected boolean considerProgramPoint(final Context programPointContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint(Context programPointContext = " + programPointContext + ") - BEGIN");
		}

		final Stmt _srcStmt = (Stmt) getInfoFor(Identifiers.SRC_ENTITY);
		final Stmt _curStmt = programPointContext.getStmt();
		final SootMethod _curMethod = programPointContext.getCurrentMethod();

		// there may be call path from static initializers to method invocations. Hence, we return true.
		if (_curMethod.isStatic() && _curMethod.getName().equals("<clinit>")) {
			return true;
		}
		final boolean _sameMethod = _curMethod.equals(getInfoFor(Identifiers.SRC_METHOD));

		ConcreteRef _curRef = null;
		ConcreteRef _srcRef = null;

		if (_curStmt.containsArrayRef() && _srcStmt.containsArrayRef()) {
			_curRef = _curStmt.getArrayRef();
			_srcRef = _srcStmt.getArrayRef();
		} else if (_curStmt.containsFieldRef() && _srcStmt.containsFieldRef()) {
			_curRef = _curStmt.getFieldRef();
			_srcRef = _srcStmt.getFieldRef();

			if (((FieldRef) _curRef).getField().isFinal()) {
				return true;
			}
		}

		boolean _result = _curRef != null && _curStmt instanceof DefinitionStmt && _srcStmt instanceof DefinitionStmt;

		if (_result) {
			final DefinitionStmt _curDefStmt = (DefinitionStmt) _curStmt;
			final DefinitionStmt _srcDefStmt = (DefinitionStmt) _srcStmt;

			if (_curRef == _curDefStmt.getRightOp() && _srcRef == _srcDefStmt.getLeftOp()) {
				if (_sameMethod) {
					_result = analysis.doesControlFlowPathExistBetween(_srcDefStmt, _curDefStmt, _curMethod);
				} else {
					_result = analysis
							.isReachableViaInterProceduralControlFlow((SootMethod) getInfoFor(Identifiers.SRC_METHOD),
									_srcDefStmt, _curMethod, _curDefStmt, tgi, false);
				}
			} else {
				// if (_curRef == _srcDefStmt.getRightOp() && _srcRef == _curDefStmt.getLeftOp())
				if (_sameMethod) {
					_result = analysis.doesControlFlowPathExistBetween(_curDefStmt, _srcDefStmt, _curMethod);
				} else {
					_result = analysis.isReachableViaInterProceduralControlFlow(_curMethod, _curDefStmt,
							(SootMethod) getInfoFor(Identifiers.SRC_METHOD), _srcDefStmt, tgi, false);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint() - END - return value = " + _result);
		}
		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#getCallerSideToken(Object, SootMethod, ICallGraphInfo.CallTriple, Stack)
	 */
	@Override protected Object getCallerSideToken(final Object token, final SootMethod callee, final CallTriple callsite,
			@SuppressWarnings("unused") final Stack<CallTriple> calleeCallStack) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallerSideToken(Object token = " + token + ", SootMethod callee = " + callee
					+ ", CallTriple callsite = " + callsite + ") - BEGIN");
		}

		Object _result = Tokens.DISCARD_CONTEXT_TOKEN;

		@SuppressWarnings("unchecked") final Collection<CallTriple> _ancestors = (Collection) token;
		if (!Util.isStartMethod(callee) && _ancestors.contains(callsite)) {
			final Collection<CallTriple> _col = new HashSet<CallTriple>();
			previous.add(callsite);
			for (final Iterator<CallTriple> _i = getCallGraph().getCallers(callsite.getMethod()).iterator(); _i.hasNext();) {
				final CallTriple _ctrp = _i.next();
				if (getCallSitesThatCanReachSource(_ctrp, false)) {
					_col.add(_ctrp);
				}
			}
			_col.removeAll(previous);

			if (!_col.isEmpty()) {
				_result = _col;
			} else {
				_result = Tokens.ACCEPT_TERMINAL_CONTEXT_TOKEN;
			}
		} else if (_ancestors.contains(null)) {
			_result = Tokens.ACCEPT_TERMINAL_CONTEXT_TOKEN;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallerSideToken() - END - return value = " + _result);
		}

		return _result;
	}

	/**
	 * Retrieves the call sites that can reach the source of data dependence.
	 * 
	 * @param callsite of interest.
	 * @param exclusive <code>true</code> indicates that <code>callsite</code> should not be considered during existence
	 *            check; <code>false</code>, otherwise. Unless the client is sure, this should be <code>false</code>.
	 * @return <code>true</code> if such a path exists; <code>false</code>, otherwise.
	 */
	protected final boolean getCallSitesThatCanReachSource(final CallTriple callsite, final boolean exclusive) {
		final Stmt _stmt = (Stmt) getInfoFor(Identifiers.SRC_ENTITY);
		final SootMethod _method = (SootMethod) getInfoFor(Identifiers.SRC_METHOD);
		final boolean _flag = isSourceADefSite();
		return (_flag && analysis.isReachableViaInterProceduralControlFlow(_method, _stmt, callsite.getMethod(), callsite
				.getStmt(), tgi, exclusive))
				|| (!_flag && analysis.isReachableViaInterProceduralControlFlow(callsite.getMethod(), callsite.getStmt(),
						_method, _stmt, tgi, exclusive));
	}

	/**
	 * @see AbstractCallingContextRetriever#getTokenForProgramPoint(Context)
	 */
	@Override protected Object getTokenForProgramPoint(final Context programPointContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint(Context programPointContext = " + programPointContext + ") - BEGIN");
		}

		final SootMethod _curMethod = programPointContext.getCurrentMethod();

		// there may be call path from static initializers to method invocations. Hence, we return true.
		if (_curMethod.isStatic() && _curMethod.getName().equals("<clinit>")) {
			return Tokens.CONSIDER_ALL_CONTEXTS_TOKEN;
		}

		final DefinitionStmt _curDefStmt = (DefinitionStmt) programPointContext.getStmt();
		final DefinitionStmt _srcDefStmt = (DefinitionStmt) ((Stmt) getInfoFor(Identifiers.SRC_ENTITY));
		ConcreteRef _curRef = null;
		ConcreteRef _srcRef = null;

		if (_curDefStmt.containsArrayRef() && _srcDefStmt.containsArrayRef()) {
			_curRef = _curDefStmt.getArrayRef();
			_srcRef = _srcDefStmt.getArrayRef();
		} else if (_curDefStmt.containsFieldRef() && _srcDefStmt.containsFieldRef()) {
			_curRef = _curDefStmt.getFieldRef();
			_srcRef = _srcDefStmt.getFieldRef();

			if (((FieldRef) _curRef).getField().isFinal()) {
				return Tokens.CONSIDER_ALL_CONTEXTS_TOKEN;
			}
		}

		final Object _result;

		if (_curRef == null) {
			_result = Collections.emptySet();
		} else {
			final SootMethod _defMethod;
			final SootMethod _useMethod;
			final DefinitionStmt _defStmt;
			final DefinitionStmt _useStmt;
			final SootMethod _srcMethod = (SootMethod) getInfoFor(Identifiers.SRC_METHOD);

			if (_curRef == _curDefStmt.getRightOp() && _srcRef == _srcDefStmt.getLeftOp()) {
				_defStmt = _srcDefStmt;
				_defMethod = _srcMethod;
				_useStmt = _curDefStmt;
				_useMethod = _curMethod;
			} else {
				// if (_curRef == _srcDefStmt.getRightOp() && _srcRef == _curDefStmt.getLeftOp()) {
				_defStmt = _curDefStmt;
				_defMethod = _curMethod;
				_useStmt = _srcDefStmt;
				_useMethod = _srcMethod;
			}

			_result = retrieveToken(_defMethod, _defStmt, _useMethod, _useStmt, _curMethod);
		}

		previous.clear();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint() - END - return value = " + _result);
		}
		return _result;
	}

	/**
	 * /** Checks for rechability of the callers to/from the given program point and updates the ancestors with callers that
	 * satisfy reachability condition.
	 * 
	 * @param stmt of interest.
	 * @param method in which <code>stmt</code> occurs.
	 * @param ancestors is the out parameter that should be populated with reachable callers.
	 * @param callers of interest. The callers added to ancestors are removed.
	 * @param isSource <code>true</code> if the program point is the source; <code>false</code> if the program point is
	 *            the destination.
	 * @pre stmt != null and method != null and ancestors != null and callers != null
	 * @post (ancestors - ancestors$pre).containsAll(callers$pre - callers)
	 * @post (callers$pre - callers).containsAll(ancestors - ancestors$pre)
	 */
	private void checkReachabilityFromCallersAndUpdateAncestors(final Stmt stmt, final SootMethod method,
			final Collection<CallTriple> ancestors, final Collection<CallTriple> callers, final boolean isSource) {
		for (final Iterator<CallTriple> _i = callers.iterator(); _i.hasNext();) {
			final CallTriple _destCallTriple = _i.next();
			final SootMethod _destMethod = _destCallTriple.getMethod();
			final Stmt _destStmt = _destCallTriple.getStmt();
			if ((isSource && analysis.isReachableViaInterProceduralControlFlow(method, stmt, _destMethod, _destStmt, tgi,
					true))
					|| (!isSource && analysis.isReachableViaInterProceduralControlFlow(_destMethod, _destStmt, method, stmt,
							tgi, true))) {
				ancestors.add(_destCallTriple);
				_i.remove();
			}
		}
	}

	/**
	 * Checks if the source triggering the context retrival is a def site.
	 * 
	 * @return <code>true</code> if the source triggering the context retrival is a def site; <code>false</code>,
	 *         otherwise.
	 */
	protected final boolean isSourceADefSite() {
		final DefinitionStmt _stmt = (DefinitionStmt) getInfoFor(Identifiers.SRC_ENTITY);
		final boolean _result;
		final Value _leftOp = _stmt.getLeftOp();
		if (_leftOp instanceof ArrayRef || _leftOp instanceof FieldRef) {
			_result = true;
		} else {
			_result = false;
		}
		return _result;
	}

	/**
	 * Retrieves the ancestors.
	 * 
	 * @param defMethod is the method in which the definition occurs.
	 * @param useMethod is the method in which the use occurs.
	 * @param defStmt is the definition statement.
	 * @param useStmt is the use statement.
	 * @param curMethod is the method in which the context will be rooted.
	 * @return a token object.
	 * @pre defMethod != null and useMethod != null and defStmt != null and useStmt != null and curMethod != null
	 */
	private Collection<CallTriple> retrieveAncestors(final SootMethod defMethod, final SootMethod useMethod,
			final DefinitionStmt defStmt, final DefinitionStmt useStmt, final SootMethod curMethod) {
		final Collection<CallTriple> _ancestors = new HashSet<CallTriple>();
		final ICallGraphInfo _callGraph = getCallGraph();
		@SuppressWarnings("unchecked") final Stack<CallTriple> _stack = (Stack) getInfoFor(Identifiers.SRC_CALLING_CONTEXT);
		final Collection<CallTriple> _callers = new HashSet<CallTriple>(_callGraph.getCallers(curMethod));
		final boolean _curMethodIsUseMethod;

		if (useMethod == curMethod) {
			_curMethodIsUseMethod = true;
		} else {
			// if (defMethod == curMethod)
			_curMethodIsUseMethod = false;
		}

		if (_stack != null && !_stack.isEmpty()) {
			for (final Iterator<CallTriple> _i = _stack.iterator(); _i.hasNext();) {
				final CallTriple _srcCallTriple = _i.next();
				if (_srcCallTriple != null) {
					final Stmt _srcStmt = _srcCallTriple.getStmt();
					final SootMethod _srcMethod = _srcCallTriple.getMethod();
					checkReachabilityFromCallersAndUpdateAncestors(_srcStmt, _srcMethod, _ancestors, _callers,
							_curMethodIsUseMethod);
				}

				if (defMethod.equals(useMethod) && analysis.doesControlFlowPathExistBetween(defStmt, useStmt, defMethod)) {
					_ancestors.add(_srcCallTriple);
				}
			}
		} else {
			if (defMethod.equals(useMethod) && analysis.doesControlFlowPathExistBetween(defStmt, useStmt, defMethod)) {
				_ancestors.addAll(_callers);
			} else {
				final SootMethod _method;
				final Stmt _stmt;
				if (_curMethodIsUseMethod) {
					_stmt = defStmt;
					_method = defMethod;
				} else {
					_stmt = useStmt;
					_method = useMethod;
				}
				checkReachabilityFromCallersAndUpdateAncestors(_stmt, _method, _ancestors, _callers, _curMethodIsUseMethod);
			}
		}

		return _ancestors;
	}

	/**
	 * Retrieves the token.
	 * 
	 * @param defMethod is the method in which definition occurs.
	 * @param defStmt is the statement in which definition occurs.
	 * @param useMethod is the method in which use occurs.
	 * @param useStmt is the statement in which use occurs.
	 * @param curMethod is the method in which the context will be rooted.
	 * @return the token
	 * @pre defMethod != null and useMethod != null and defStmt != null and useStmt != null and curMethod != null
	 * @post result != null
	 */
	private Object retrieveToken(final SootMethod defMethod, final DefinitionStmt defStmt, final SootMethod useMethod,
			final DefinitionStmt useStmt, final SootMethod curMethod) {
		final Object _result;
		final Collection<?> _t = retrieveAncestors(defMethod, useMethod, defStmt, useStmt, curMethod);
		if (_t.isEmpty()) {
			_result = Tokens.DISCARD_CONTEXT_TOKEN;
		} else {
			_result = _t;
		}
		return _result;
	}
}

// End of File
