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
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IExceptionRaisingInfo;

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
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.TrapManager;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;

import soot.toolkits.graph.UnitGraph;

/**
 * This provides interprocedural analysis that calculates the propogation of uncaught exceptions.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ExceptionRaisingAnalysis
		extends AbstractProcessor
		implements IExceptionRaisingInfo {

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_ARRAY_STORE_EXCEPTION = "java.lang.ArrayStoreException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = "java.lang.ArrayIndexOutOfBoundsException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_CLASS_CAST_EXCEPTION = "java.lang.ClassCastException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_NEGATIVE_ARRAY_SIZE_EXCEPTION = "java.lang.NegativeArraySizeException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_INSTANTIATION_ERROR = "java.lang.InstantiationError";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_NULL_POINTER_EXCEPTION = "java.lang.NullPointerException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_NO_SUCH_METHOD_EXCEPTION = "java.lang.NoSuchMethodException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_ILLEGAL_ACCESS_ERROR = "java.lang.IllegalAccessError";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionRaisingAnalysis.class);

	/**
	 * The call graph to be used.
	 */
	private final ICallGraphInfo cgi;

	/**
	 * The environment to be analyzed.
	 */
	private final IEnvironment env;

	/**
	 * This maps methods to statements to thrown exception types.
	 * 
	 * @invariant method22stmt2exceptions.oclIsKindOf(Map(SootMethod, Map(Stmt, Collection(SootClass))))
	 */
	private final Map method2stmt2exceptions = new HashMap();

	/**
	 * This maps methods to the statements to a collection of uncaught exception types.
	 * 
	 * @invariant method2stmt2thrownTypes.oclIsKindOf(Map(SootMethod, Map(Stmt, Collection(SootClass))))
	 */
	private final Map method2stmt2uncaughtExceptions = new HashMap();

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
	 * 
	 * @invariant astNodeType2thrownTypeNames.oclIsKindOf(Map(Class, Collection(String)))
	 */
	private final Map astNodeType2thrownTypeNames = new HashMap();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param factory
	 *            to retrieve the statement graphs.
	 * @param callgraph
	 *            to be used.
	 * @param environment
	 *            to be analyzed.
	 * @pre factory != null and callgraph != null and env != null
	 */
	public ExceptionRaisingAnalysis(final IStmtGraphFactory factory, final ICallGraphInfo callgraph,
			final IEnvironment environment) {
		stmtGraphFactory = factory;
		cgi = callgraph;
		workbagCache = new HistoryAwareFIFOWorkBag(new ArrayList());
		env = environment;
	}

	/**
	 * @see IExceptionRaisingInfo#getExceptionsThrownBy(soot.jimple.Stmt, soot.SootMethod)
	 */
	public Collection getExceptionsThrownBy(final Stmt stmt, final SootMethod method) {
		final Map _map = (Map) MapUtils.getObject(method2stmt2exceptions, method, Collections.EMPTY_MAP);
		final Collection _col1 = (Collection) MapUtils.getObject(_map, stmt, Collections.EMPTY_SET);
		final Collection _col2 = getUncaughtExceptionsThrownBy(stmt, method);
		return CollectionUtils.union(_col1, _col2);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(ID);
	}

	/**
	 * @see IExceptionRaisingInfo#getUncaughtExceptionsThrownBy(soot.jimple.Stmt, soot.SootMethod)
	 */
	public Collection getUncaughtExceptionsThrownBy(final Stmt stmt, final SootMethod method) {
		final Map _map = (Map) MapUtils.getObject(method2stmt2uncaughtExceptions, method, Collections.EMPTY_MAP);
		return (Collection) MapUtils.getObject(_map, stmt, Collections.EMPTY_SET);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		final SootMethod _method = context.getCurrentMethod();
		final Map _stmt2exceptions = CollectionsUtilities.getMapFromMap(method2stmt2exceptions, _method);
		final Collection _thrownTypes = CollectionsUtilities.getSetFromMap(_stmt2exceptions, stmt);

		if (stmt instanceof ThrowStmt) {
			final SootClass _thrownType = ((RefType) ((ThrowStmt) stmt).getOp().getType()).getSootClass();
			processStmt(stmt, _method, _thrownTypes, _thrownType);
		} else if (stmt.containsInvokeExpr()) {
			final Collection _callees = cgi.getCallees(stmt.getInvokeExpr(), context);
			final Iterator _i = _callees.iterator();
			final int _iEnd = _callees.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootMethod _callee = (SootMethod) _i.next();
				final Iterator _j = _callee.getExceptions().iterator();
				final int _jEnd = _callee.getExceptions().size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final SootClass _thrownType = (SootClass) _j.next();
					processStmt(stmt, _method, _thrownTypes, _thrownType);
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, Context)
	 */
	public void callback(final ValueBox vb, final Context context) {
		final Value _v = vb.getValue();
		final Stmt _stmt = context.getStmt();
		final SootMethod _method = context.getCurrentMethod();
		final Collection _c = getExceptionTypeNamesFor(_v.getClass());
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
				final Stmt _callingStmt = _ctrp.getStmt();

				if (isThrownExceptionNotCaught(_callingStmt, _caller, _thrownType)) {
					workbagCache.addWorkNoDuplicates(new Triple(_callingStmt, _caller, _thrownType));
				}
			}
			CollectionsUtilities.putIntoSetInMap(CollectionsUtilities.getMapFromMap(method2stmt2uncaughtExceptions, _method),
					_stmt, _thrownType);
		}

		workbagCache.clear();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - END - "
					+ CollectionsUtilities.prettyPrint("method-to-stmt-to-throwntypes", method2stmt2uncaughtExceptions));
		}
	}

	/**
	 * @see IExceptionRaisingInfo#doesStmtThrowUncaughtException(soot.jimple.Stmt, soot.SootMethod)
	 */
	public boolean doesStmtThrowUncaughtException(final Stmt stmt, final SootMethod method) {
		final Map _map = (Map) MapUtils.getObject(method2stmt2uncaughtExceptions, method, Collections.EMPTY_MAP);
		return _map.containsKey(stmt);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.registerForAllStmts(this);

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
		method2stmt2uncaughtExceptions.clear();
		method2stmt2exceptions.clear();
		astNodeType2thrownTypeNames.clear();
	}

	/**
	 * Sets up this object to consider common unchecked exceptions.
	 */
	public void setupForCommonUncheckedExceptions() {
		toggleExceptionsToTrack(ArrayRef.class, JAVA_LANG_ARRAY_STORE_EXCEPTION, true);
		toggleExceptionsToTrack(ArrayRef.class, JAVA_LANG_ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION, true);
		toggleExceptionsToTrack(ArrayRef.class, JAVA_LANG_NULL_POINTER_EXCEPTION, true);
		toggleExceptionsToTrack(NewExpr.class, JAVA_LANG_INSTANTIATION_ERROR, true);
		toggleExceptionsToTrack(NewArrayExpr.class, JAVA_LANG_NEGATIVE_ARRAY_SIZE_EXCEPTION, true);
		toggleExceptionsToTrack(NewArrayExpr.class, JAVA_LANG_INSTANTIATION_ERROR, true);
		toggleExceptionsToTrack(NewMultiArrayExpr.class, JAVA_LANG_NEGATIVE_ARRAY_SIZE_EXCEPTION, true);
		toggleExceptionsToTrack(NewMultiArrayExpr.class, JAVA_LANG_INSTANTIATION_ERROR, true);
		toggleExceptionsToTrack(InstanceFieldRef.class, JAVA_LANG_NULL_POINTER_EXCEPTION, true);
		toggleExceptionsToTrack(InstanceFieldRef.class, JAVA_LANG_ILLEGAL_ACCESS_ERROR, true);
		toggleExceptionsToTrack(StaticFieldRef.class, JAVA_LANG_ILLEGAL_ACCESS_ERROR, true);
		toggleExceptionsToTrack(CastExpr.class, JAVA_LANG_CLASS_CAST_EXCEPTION, true);
		toggleExceptionsToTrack(VirtualInvokeExpr.class, JAVA_LANG_NULL_POINTER_EXCEPTION, true);
		toggleExceptionsToTrack(VirtualInvokeExpr.class, JAVA_LANG_NO_SUCH_METHOD_EXCEPTION, true);
		toggleExceptionsToTrack(VirtualInvokeExpr.class, JAVA_LANG_ILLEGAL_ACCESS_ERROR, true);
		toggleExceptionsToTrack(SpecialInvokeExpr.class, JAVA_LANG_NULL_POINTER_EXCEPTION, true);
		toggleExceptionsToTrack(SpecialInvokeExpr.class, JAVA_LANG_NO_SUCH_METHOD_EXCEPTION, true);
		toggleExceptionsToTrack(SpecialInvokeExpr.class, JAVA_LANG_ILLEGAL_ACCESS_ERROR, true);
		toggleExceptionsToTrack(InterfaceInvokeExpr.class, JAVA_LANG_NULL_POINTER_EXCEPTION, true);
		toggleExceptionsToTrack(InterfaceInvokeExpr.class, JAVA_LANG_NO_SUCH_METHOD_EXCEPTION, true);
		toggleExceptionsToTrack(InterfaceInvokeExpr.class, JAVA_LANG_ILLEGAL_ACCESS_ERROR, true);
		toggleExceptionsToTrack(StaticInvokeExpr.class, JAVA_LANG_NO_SUCH_METHOD_EXCEPTION, true);
		toggleExceptionsToTrack(StaticInvokeExpr.class, JAVA_LANG_ILLEGAL_ACCESS_ERROR, true);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		final StringBuffer _sb = new StringBuffer();
		_sb.append(CollectionsUtilities.prettyPrint("Caught+Uncaught Exception info", method2stmt2exceptions));
		_sb.append(CollectionsUtilities.prettyPrint("Uncaught exception info", method2stmt2uncaughtExceptions));
		return _sb.toString();
	}

	/**
	 * Toggles the tracking of the named exception type for the given ast node type.
	 * 
	 * @param astNodeType
	 *            is a concrete jimple class that represents a <code>Value</code>, e.g, InstanceFieldRef.class
	 * @param exceptionName
	 *            is the name of the exception that needs to be tracked at the given ast node.
	 * @param consider
	 *            <code>true</code> indicates that the exception needs to be tracked; <code>false</code> indicates that
	 *            the exception should not be tracked.
	 * @pre astNodeType != null and exceptionName != null
	 */
	public void toggleExceptionsToTrack(final Class astNodeType, final String exceptionName, final boolean consider) {
		if (consider) {
			CollectionsUtilities.putIntoSetInMap(astNodeType2thrownTypeNames, astNodeType, exceptionName);
		} else {
			final Collection _typeNames = (Collection) MapUtils.getObject(astNodeType2thrownTypeNames, astNodeType,
					Collections.EMPTY_SET);
			_typeNames.remove(exceptionName);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregisterForAllStmts(this);

		final Collection _c = astNodeType2thrownTypeNames.keySet();
		final Iterator _i = _c.iterator();
		final int _iEnd = _c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			ppc.unregister((Class) _i.next(), this);
		}
	}

	/**
	 * Retrieves the exception type names for the given ast node type.
	 * 
	 * @param c
	 *            is the ast node type.
	 * @return a collection of exception type names.
	 * @pre c != null
	 * @post result != null and result.oclIsKindOf(Collection(String))
	 */
	private Collection getExceptionTypeNamesFor(final Class c) {
		Collection _result = Collections.EMPTY_SET;
		final Set _keySet = astNodeType2thrownTypeNames.keySet();
		final Iterator _i = _keySet.iterator();
		final int _iEnd = _keySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Class _nodeType = (Class) _i.next();

			if (_nodeType.isAssignableFrom(c)) {
				_result = (Collection) astNodeType2thrownTypeNames.get(_nodeType);
				break;
			}
		}
		return _result;
	}

	/**
	 * Checks if the given exception type thrown from the given statement in the given method is not caught in the given
	 * method.
	 * 
	 * @param stmt
	 *            at which the exception occurs.
	 * @param method
	 *            in which <code>stmt</code> occurs.
	 * @param thrownType
	 *            is the type of the exception that is thrown.
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
					final RefType _caughtType = (RefType) _j.next();
					_isCaught |= Util.isDescendentOf(thrownType, _caughtType.getSootClass());
				}
			}
			_result = !_isCaught;
		}
		return _result;
	}

	/**
	 * Process the given statement and method against the given exception type and updates the given collection.
	 * 
	 * @param stmt
	 *            that may cause the exception.
	 * @param method
	 *            contains <code>stmt</code>.
	 * @param thrownTypes
	 *            is the collection to which <code>thrownType</code> should be added if it is caught.
	 * @param thrownType
	 *            is the exception to be checked for at <code>stmt</code>.
	 * @pre stmt != null and method != null and thrownTypes != null and thrownType != null
	 */
	private void processStmt(final Stmt stmt, final SootMethod method, final Collection thrownTypes,
			final SootClass thrownType) {
		if (isThrownExceptionNotCaught(stmt, method, thrownType)) {
			workbagCache.addWorkNoDuplicates(new Triple(stmt, method, thrownType));
		} else {
			thrownTypes.add(thrownType);
		}
	}
}

// End of File
