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

package edu.ksu.cis.indus.staticanalyses.impl;

import edu.ksu.cis.indus.interfaces.AbstractCallingContextRetriever;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;

import soot.jimple.ConcreteRef;
import soot.jimple.DefinitionStmt;
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
	@Override protected final boolean considerProgramPoint(final Context programPointContext) {
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

		ConcreteRef _curRef = null;
		ConcreteRef _srcRef = null;

		if (_curStmt.containsArrayRef() && _srcStmt.containsArrayRef()) {
			_curRef = _curStmt.getArrayRef();
			_srcRef = _srcStmt.getArrayRef();
		} else if (_curStmt.containsFieldRef() && _srcStmt.containsFieldRef()) {
			_curRef = _curStmt.getFieldRef();
			_srcRef = _srcStmt.getFieldRef();
		}

		boolean _result = _curRef != null && _curStmt instanceof DefinitionStmt && _srcStmt instanceof DefinitionStmt;

		if (_result) {
			final DefinitionStmt _curDefStmt = (DefinitionStmt) _curStmt;
			final DefinitionStmt _srcDefStmt = (DefinitionStmt) _srcStmt;
			final boolean _sameMethod = _curMethod.equals(getInfoFor(Identifiers.SRC_METHOD));

			if (_curRef == _curDefStmt.getRightOp() && _srcRef == _srcDefStmt.getLeftOp()) {
				if (_sameMethod) {
					_result = analysis.doesControlFlowPathExistsBetween(_srcDefStmt, _curDefStmt, _curMethod);
				} else {
					_result = analysis.isReachableViaInterProceduralControlFlow(
							(SootMethod) getInfoFor(Identifiers.SRC_METHOD), _srcDefStmt, _curMethod, _curDefStmt, tgi);
				}
			} else {
				// if (_curRef == _srcDefStmt.getRightOp() && _srcRef == _curDefStmt.getLeftOp())
				if (_sameMethod) {
					_result = analysis.doesControlFlowPathExistsBetween(_curDefStmt, _srcDefStmt, _curMethod);
				} else {
					_result = analysis.isReachableViaInterProceduralControlFlow(_curMethod, _curDefStmt,
							(SootMethod) getInfoFor(Identifiers.SRC_METHOD), _srcDefStmt, tgi);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint() - END - return value = " + _result);
		}
		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#getCallerSideToken(Object, SootMethod, ICallGraphInfo.CallTriple)
	 */
	@Override protected Object getCallerSideToken(final Object token, final SootMethod callee, final CallTriple callsite) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallerSideToken(Object token = " + token + ", SootMethod callee = " + callee
					+ ", CallTriple callsite = " + callsite + ") - BEGIN");
		}

		Object _result = Tokens.DISCARD_CONTEXT_TOKEN;

		final SootMethod _caller = callsite.getMethod();
		final Collection _ancestors = (Collection) token;

		if (_ancestors.contains(_caller)) {
			final Collection _col = CollectionUtils.intersection(getCallGraph().getMethodsReachableFrom(_caller, false),
					_ancestors);
			_col.remove(callee);

			if (!_col.isEmpty()) {
				_result = _col;
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallerSideToken() - END - return value = " + _result);
		}

		return _result;
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

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint() - END - return value = " + _result);
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
	protected final Collection<SootMethod> retrieveAncestors(final SootMethod defMethod, final SootMethod useMethod,
			final DefinitionStmt defStmt, final DefinitionStmt useStmt, final SootMethod curMethod) {
		final Collection<SootMethod> _ancestors;
		final ICallGraphInfo _callGraph = getCallGraph();

		if (defMethod.equals(useMethod) && analysis.doesControlFlowPathExistsBetween(defStmt, useStmt, defMethod)) {
			final Collection<SootMethod> _methodsReachableFrom = _callGraph.getMethodsReachableFrom(defMethod, false);

			if (_methodsReachableFrom.isEmpty()) {
				_ancestors = null;
			} else {
				_ancestors = new HashSet<SootMethod>(_methodsReachableFrom);
			}
		} else {
			_ancestors = new HashSet<SootMethod>();

			if (analysis.doesControlFlowPathExistsBetween(defMethod, defStmt, useMethod, true, true)) {
				_ancestors.addAll(_callGraph.getCommonMethodsReachableFrom(defMethod, true, useMethod, false));
			}

			if (analysis.doesControlFlowPathExistsBetween(useMethod, useStmt, defMethod, false, true)) {
				_ancestors.addAll(_callGraph.getCommonMethodsReachableFrom(useMethod, true, defMethod, false));
			}

			final Collection<SootMethod> _callersOfCurrMethod = _callGraph.getMethodsReachableFrom(curMethod, false);

			if (!_callersOfCurrMethod.isEmpty()) {
				final Collection<SootMethod> _commonAncestors = _callGraph.getConnectivityCallersFor(defMethod, useMethod);

				for (final Iterator<SootMethod> _i = _commonAncestors.iterator(); _i.hasNext();) {
					final SootMethod _sm = _i.next();
					final Collection<SootMethod> _methodsReachableFrom = _callGraph.getMethodsReachableFrom(_sm, true);
					_ancestors.addAll(CollectionUtils.intersection(_methodsReachableFrom, _callersOfCurrMethod));
				}
				_ancestors.addAll(_commonAncestors);
			}
		}

		return _ancestors;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param defMethod DOCUMENT ME!
	 * @param defStmt DOCUMENT ME!
	 * @param useMethod DOCUMENT ME!
	 * @param useStmt DOCUMENT ME!
	 * @param curMethod is the method in which the context will be rooted.
	 * @return DOCUMENT ME!
	 * @pre defMethod != null and useMethod != null and defStmt != null and useStmt != null and curMethod != null
	 * @post result != null
	 */
	protected Object retrieveToken(final SootMethod defMethod, final DefinitionStmt defStmt, final SootMethod useMethod,
			final DefinitionStmt useStmt, final SootMethod curMethod) {
		final Object _result;
		final Object _t = retrieveAncestors(defMethod, useMethod, defStmt, useStmt, curMethod);
		if (_t == null) {
			_result = Tokens.DISCARD_CONTEXT_TOKEN;
		} else {
			_result = _t;
		}
		return _result;
	}
}

// End of File
