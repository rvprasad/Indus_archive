
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.TrapManager;
import soot.Value;
import soot.ValueBox;

import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;

import soot.toolkits.graph.UnitGraph;


/**
 * This provides interprocedural analysis that calculates the propogation of uncaught exceptions. 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class UncheckedExceptionAnalysis
  extends AbstractProcessor {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(UncheckedExceptionAnalysis.class);

	/** 
	 * The call graph to be used.
	 */
	private final ICallGraphInfo cgi;

	/** 
	 * The environment to be analyzed.
	 */
	private final IEnvironment env;

	/** 
	 * This maps methods to the statements to a collection of uncaught exception types.
	 *
	 * @invariant method2stmt2thrownTypes.oclIsKindOf(Map(SootMethod, Map(Stmt, Collection(SootClass))))
	 */
	private final Map method2stmt2thrownTypes = new HashMap();

	/** 
	 * The statement graph factory to use.
	 */
	private IStmtGraphFactory stmtGraphFactory;

	/** 
	 * A workbag.
	 */
	private final IWorkBag workbagCache;

	/** 
	 * This maps ast node type to a collection of FQN of thrown exception types.
     * @invariant astNodeType2thrownTypeNames.oclIsKindOf(Map(Class, Collection(String)))
	 */
	private final Map astNodeType2thrownTypeNames = new HashMap();

	/**
	 * Creates an instance of this class.
	 *
	 * @param factory to retrieve the statement graphs.
	 * @param callgraph to be used.
	 * @param environment to be analyzed.
     * @pre factory != null and callgraph != null and env != null
	 */
	public UncheckedExceptionAnalysis(final IStmtGraphFactory factory, final ICallGraphInfo callgraph, final IEnvironment environment) {
		stmtGraphFactory = factory;
		cgi = callgraph;
		workbagCache = new HistoryAwareFIFOWorkBag(new ArrayList());
		env = environment;
	}

	/**
	 * Retrieves the uncaught exceptions thrown by the given statement in the given method.
	 *
	 * @param stmt of interest.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of exception type names.
     * @pre stmt != null and method != null
	 */
	public Collection getUncaughtExceptionsThrownBy(final Stmt stmt, final SootMethod method) {
		final Map _map = (Map) MapUtils.getObject(method2stmt2thrownTypes, method, Collections.EMPTY_MAP);
		return (Collection) MapUtils.getObject(_map, stmt, Collections.EMPTY_SET);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final SootClass _thrownType = ((RefType) ((ThrowStmt) stmt).getOp().getType()).getSootClass();
		final SootMethod _method = context.getCurrentMethod();

		if (isThrownExceptionNotCaught(stmt, _method, _thrownType)) {
			workbagCache.addWorkNoDuplicates(new Triple(stmt, _method, _thrownType));
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, Context)
	 */
	public void callback(final ValueBox vb, final Context context) {
		final Value _v = vb.getValue();
		final Stmt _stmt = context.getStmt();
		final SootMethod _method = context.getCurrentMethod();
		final Collection _c = (Collection) astNodeType2thrownTypeNames.get(_v.getClass());
		final Iterator _i = _c.iterator();
		final int _iEnd = _c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final String _typeName = (String) _i.next();
			final SootClass _sc = env.getClass(_typeName);

			if (isThrownExceptionNotCaught(_stmt, _method, _sc)) {
				workbagCache.addWorkNoDuplicates(new Triple(_stmt, _method, _sc));
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	public void consolidate() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - BEGIN");
		}

		super.consolidate();

		while (workbagCache.hasWork()) {
			final Triple _triple = (Triple) workbagCache.getWork();
			final Stmt _stmt = (Stmt) _triple.getFirst();
			final SootMethod _method = (SootMethod) _triple.getSecond();
			final SootClass _thrownType = (SootClass) _triple.getThird();
			final Collection _callers = cgi.getCallers(_method);
			final Iterator _j = _callers.iterator();
			final int _jEnd = _callers.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final ICallGraphInfo.CallTriple _ctrp = (ICallGraphInfo.CallTriple) _j.next();
				final SootMethod _caller = _ctrp.getMethod();

				if (isThrownExceptionNotCaught(_ctrp.getStmt(), _caller, _thrownType)) {
					workbagCache.addWorkNoDuplicates(new Pair(_caller, _thrownType));
				}
			}
			CollectionsUtilities.putIntoSetInMap(CollectionsUtilities.getMapFromMap(method2stmt2thrownTypes, _method), _stmt,
				_thrownType);
		}

		workbagCache.clear();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - END - "
				+ CollectionsUtilities.prettyPrint("method-to-stmt-to-throwntypes", method2stmt2thrownTypes));
		}
	}

	/**
	 * Checks if the given statement throws an uncaught exception in the given method. 
	 *
	 * @param stmt of interest.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return <code>true</code> if an uncaught exception will be thrown; <code>false</code>, otherwise.
     * @pre stmt != null and method != null
	 */
	public boolean doesStmtThrowException(final Stmt stmt, final SootMethod method) {
		final Map _map = (Map) MapUtils.getObject(method2stmt2thrownTypes, method, Collections.EMPTY_MAP);
		return _map.containsKey(stmt);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(ThrowStmt.class, this);

		final Collection _c = astNodeType2thrownTypeNames.keySet();
		final Iterator _i = _c.iterator();
		final int _iEnd = _c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			ppc.register((Class) _i.next(), this);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	public void reset() {
		super.reset();
		method2stmt2thrownTypes.clear();
		astNodeType2thrownTypeNames.clear();
	}

	/**
	 * Toggles the tracking of the named exception type for the given ast node type. 
	 *
	 * @param astNodeType is a concrete jimple class that represents a <code>Value</code>, e.g, InstanceFieldRef.class
	 * @param exceptionName is the name of the exception that needs to be tracked at the given ast node.
	 * @param consider <code>true</code> indicates that the exception needs to be tracked; <code>false</code> indicates that
     * the exception should not be tracked.
     * @pre astNodeType != null and exceptionName != null
	 */
	public void toggleExceptionsToTrack(final Class astNodeType, final String exceptionName, final boolean consider) {
		if (consider) {
			CollectionsUtilities.putIntoSetInMap(astNodeType2thrownTypeNames, astNodeType, exceptionName);
		} else {
			final Collection _typeNames =
				(Collection) MapUtils.getObject(astNodeType2thrownTypeNames, astNodeType, Collections.EMPTY_SET);
			_typeNames.remove(exceptionName);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(ThrowStmt.class, this);

		final Collection _c = astNodeType2thrownTypeNames.keySet();
		final Iterator _i = _c.iterator();
		final int _iEnd = _c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			ppc.register((Class) _i.next(), this);
		}
	}

	/**
	 * Checks if the given exception type thrown from the given statement in the given method is not caught in the given 
     * method.
	 *
	 * @param stmt at which the exception occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @param thrownType is the type of the exception that is thrown.
	 *
	 * @return <code>true</code> if the thrown exception is caught; <code>false</code>, otherwise.
     * @pre stmt != null and method != null and thrownType != null
	 */
	private boolean isThrownExceptionNotCaught(final Stmt stmt, final SootMethod method, final SootClass thrownType) {
		final UnitGraph _graph = stmtGraphFactory.getStmtGraph(method);
		final List _succs = _graph.getSuccsOf(stmt);
		boolean _result = true;

		if (!_succs.isEmpty()) {
			boolean _isCaught = false;
			final Body _body = _graph.getBody();
			final Iterator _i = _succs.iterator();
			final int _iEnd = _succs.size();

			for (int _iIndex = 0; _iIndex < _iEnd && !_isCaught; _iIndex++) {
				final Stmt _handler = (Stmt) _i.next();
				final Iterator _j = TrapManager.getExceptionTypesOf(_handler, _body).iterator();
				final int _jEnd = TrapManager.getExceptionTypesOf(_handler, _body).size();

				for (int _jIndex = 0; _jIndex < _jEnd && !_isCaught; _jIndex++) {
					final SootClass _caughtType = (SootClass) _j.next();
					_isCaught |= Util.isDescendentOf(thrownType, _caughtType);
				}
			}
			_result = !_isCaught;
		}
		return _result;
	}
}

// End of File
