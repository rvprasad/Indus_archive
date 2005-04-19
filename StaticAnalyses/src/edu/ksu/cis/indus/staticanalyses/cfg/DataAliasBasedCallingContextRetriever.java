
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

package edu.ksu.cis.indus.staticanalyses.cfg;

import edu.ksu.cis.indus.interfaces.AbstractCallingContextRetriever;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
	private static final Log LOGGER = LogFactory.getLog(DataAliasBasedCallingContextRetriever.class);

	/** 
	 * The CFG analysis to be used.
	 */
	private CFGAnalysis analysis;

	/** 
	 * The thread graph to be used.
	 */
	private IThreadGraphInfo tgi;

	/**
	 * Creates a new DataAliasBasedCallingContextRetriever object.
	 */
	public DataAliasBasedCallingContextRetriever() {
	}

	/**
	 * Sets the CFG based analysis to be used.
	 *
	 * @param cfgAnalysis to be used.
	 *
	 * @pre cfgAnalysis != null
	 */
	public final void setCfgAnalysis(final CFGAnalysis cfgAnalysis) {
		analysis = cfgAnalysis;
	}

	/**
	 * Sets the thread graph information provided.
	 *
	 * @param threadgraph provides thread graph information.
	 *
	 * @pre threadgraph != null
	 */
	public final void setThreadGraph(final IThreadGraphInfo threadgraph) {
		tgi = threadgraph;
	}

	/**
	 * @see AbstractCallingContextRetriever#getCallerSideToken(Object, SootMethod, CallTriple)
	 */
	protected Object getCallerSideToken(final Object token, final SootMethod callee, final CallTriple callsite) {
		final Object _result;

		if (((Collection) token).contains(callsite.getMethod())) {
			_result = token;
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#getTokenForProgramPoint(Context)
	 */
	protected Object getTokenForProgramPoint(final Context programPointContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint(Context programPointContext = " + programPointContext + ") - BEGIN");
		}

		final SootMethod _defMethod;
		final SootMethod _useMethod;
		final DefinitionStmt _curDefStmt = (DefinitionStmt) programPointContext.getStmt();
		final DefinitionStmt _srcDefStmt = (DefinitionStmt) ((Stmt) getInfoFor(SRC_ENTITY));
		ConcreteRef _curRef;
		ConcreteRef _srcRef;

		if (_curDefStmt.containsArrayRef() && _srcDefStmt.containsArrayRef()) {
			_curRef = _curDefStmt.getArrayRef();
			_srcRef = _srcDefStmt.getArrayRef();
		} else if (_curDefStmt.containsFieldRef() && _srcDefStmt.containsFieldRef()) {
			_curRef = _curDefStmt.getFieldRef();
			_srcRef = _srcDefStmt.getFieldRef();
		} else {
			_curRef = null;
			_srcRef = null;
		}

		if (_curRef == _curDefStmt.getRightOp() && _srcRef == _srcDefStmt.getLeftOp()) {
			_defMethod = (SootMethod) getInfoFor(SRC_METHOD);
			_useMethod = programPointContext.getCurrentMethod();
		} else {  //if (_curRef == _srcDefStmt.getRightOp() && _srcRef == _curDefStmt.getLeftOp())
			_useMethod = (SootMethod) getInfoFor(SRC_METHOD);
			_defMethod = programPointContext.getCurrentMethod();
		}

		final Collection _result = new HashSet();
		final ICallGraphInfo _callGraph = getCallGraph();
		final Collection _commonAncestors = _callGraph.getCommonMethodsReachableFrom(_defMethod, false, _useMethod, false);

		for (final Iterator _i = _commonAncestors.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			if (analysis.doesMethodLiesOnTheDataFlowPathBetween(_sm, _defMethod, _useMethod)) {
				_result.add(_sm);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getTokenForProgramPoint() - END - return value = " + _result);
		}
		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#considerProgramPoint(Context)
	 */
	protected boolean considerProgramPoint(final Context programPointContext) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint(Context programPointContext = " + programPointContext + ") - BEGIN");
		}

		final Stmt _srcStmt = (Stmt) getInfoFor(SRC_ENTITY);
		final Stmt _curStmt = programPointContext.getStmt();
		boolean _result = _curStmt instanceof DefinitionStmt && _srcStmt instanceof DefinitionStmt;

		if (_result) {
			final DefinitionStmt _curDefStmt = (DefinitionStmt) _curStmt;
			final DefinitionStmt _srcDefStmt = (DefinitionStmt) _srcStmt;
			ConcreteRef _curRef = null;
			ConcreteRef _srcRef = null;

			if (_curDefStmt.containsArrayRef() && _srcDefStmt.containsArrayRef()) {
				_curRef = _curDefStmt.getArrayRef();
				_srcRef = _srcDefStmt.getArrayRef();
			} else if (_curDefStmt.containsFieldRef() && _srcDefStmt.containsFieldRef()) {
				_curRef = _curDefStmt.getFieldRef();
				_srcRef = _srcDefStmt.getFieldRef();
			}

			_result = _curRef != null && _srcRef != null;

			if (_result) {
				if (_curRef == _curDefStmt.getRightOp() && _srcRef == _srcDefStmt.getLeftOp()) {
					_result =
						analysis.isReachableViaInterProceduralControlFlow((SootMethod) getInfoFor(SRC_METHOD), _srcDefStmt,
							programPointContext.getCurrentMethod(), _curDefStmt, tgi);
				} else {  //if (_curRef == _srcDefStmt.getRightOp() && _srcRef == _curDefStmt.getLeftOp())
					_result =
						analysis.isReachableViaInterProceduralControlFlow(programPointContext.getCurrentMethod(),
							_curDefStmt, (SootMethod) getInfoFor(SRC_METHOD), _srcDefStmt, tgi);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint() - END - return value = " + _result);
		}
		return _result;
	}

	/**
	 * @see AbstractCallingContextRetriever#shouldConsiderUnextensibleStacksAt(Object, SootMethod, CallTriple)
	 */
	protected boolean shouldConsiderUnextensibleStacksAt(final Object calleeToken, final SootMethod callee,
		final CallTriple callSite) {
		// we would have already declared the stacks are unextensible.  So, we should be at a method that lies on the 
		// data flow path between the def/use sites.  Hence, we need to check for this condition.
		return ((Collection) calleeToken).contains(callee);
	}
}

// End of File
