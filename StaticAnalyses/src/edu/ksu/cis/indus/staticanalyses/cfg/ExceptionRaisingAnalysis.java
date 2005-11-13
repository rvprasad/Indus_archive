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

import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
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
	private static final String JAVA_LANG_ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = "java.lang.ArrayIndexOutOfBoundsException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_ARRAY_STORE_EXCEPTION = "java.lang.ArrayStoreException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_CLASS_CAST_EXCEPTION = "java.lang.ClassCastException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_ILLEGAL_ACCESS_ERROR = "java.lang.IllegalAccessError";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_INSTANTIATION_ERROR = "java.lang.InstantiationError";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_NEGATIVE_ARRAY_SIZE_EXCEPTION = "java.lang.NegativeArraySizeException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_NO_SUCH_METHOD_EXCEPTION = "java.lang.NoSuchMethodException";

	/**
	 * The name of an java exception/error class.
	 */
	private static final String JAVA_LANG_NULL_POINTER_EXCEPTION = "java.lang.NullPointerException";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionRaisingAnalysis.class);

	/**
	 * This maps ast node type to a collection of FQN of thrown exception types.
	 */
	private final Map<Class<?>, Collection<String>> astNodeType2thrownTypeNames = new HashMap<Class<?>, Collection<String>>();

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
	 */
	private final Map<SootMethod, Map<Stmt, Collection<SootClass>>> method2stmt2exceptions = new HashMap<SootMethod, Map<Stmt, Collection<SootClass>>>();

	/**
	 * This maps methods to the statements to a collection of uncaught exception types.
	 */
	private final Map<SootMethod, Map<Stmt, Collection<SootClass>>> method2stmt2uncaughtExceptions = new HashMap<SootMethod, Map<Stmt, Collection<SootClass>>>();

	/**
	 * The statement graph factory to use.
	 */
	private IStmtGraphFactory<?> stmtGraphFactory;

	/**
	 * A workbag.
	 */
	private final IWorkBag<Triple<Stmt, SootMethod, SootClass>> workbagCache;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param factory to retrieve the statement graphs.
	 * @param callgraph to be used.
	 * @param environment to be analyzed.
	 * @pre factory != null and callgraph != null and env != null
	 */
	public ExceptionRaisingAnalysis(final IStmtGraphFactory factory, final ICallGraphInfo callgraph,
			final IEnvironment environment) {
		stmtGraphFactory = factory;
		cgi = callgraph;
		workbagCache = new HistoryAwareFIFOWorkBag<Triple<Stmt, SootMethod, SootClass>>(
				new ArrayList<Triple<Stmt, SootMethod, SootClass>>());
		env = environment;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, Context)
	 */
	@Override public void callback(final Stmt stmt, final Context context) {
		final SootMethod _method = context.getCurrentMethod();
		final Map<Stmt, Collection<SootClass>> _stmt2exceptions = MapUtils.getMapFromMap(method2stmt2exceptions,
				_method);
		final Collection<SootClass> _thrownTypes = MapUtils.getCollectionFromMap(_stmt2exceptions, stmt);

		if (stmt instanceof ThrowStmt) {
			final SootClass _thrownType = ((RefType) ((ThrowStmt) stmt).getOp().getType()).getSootClass();
			processStmt(stmt, _method, _thrownTypes, _thrownType);
		} else if (stmt.containsInvokeExpr()) {
			final Collection<SootMethod> _callees = cgi.getCallees(stmt.getInvokeExpr(), context);
			final Iterator<SootMethod> _i = _callees.iterator();
			final int _iEnd = _callees.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootMethod _callee = _i.next();
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
	@Override public void callback(final ValueBox vb, final Context context) {
		final Value _v = vb.getValue();
		final Stmt _stmt = context.getStmt();
		final SootMethod _method = context.getCurrentMethod();
		final Collection<String> _c = getExceptionTypeNamesFor(_v.getClass());
		final Iterator<String> _i = _c.iterator();
		final int _iEnd = _c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final String _typeName = _i.next();
			final SootClass _sc = env.getClass(_typeName);

			if (isThrownExceptionNotCaught(_stmt, _method, _sc)) {
				workbagCache.addWorkNoDuplicates(new Triple<Stmt, SootMethod, SootClass>(_stmt, _method, _sc));
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	@Override public void consolidate() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - BEGIN");
		}

		super.consolidate();

		while (workbagCache.hasWork()) {
			final Triple<Stmt, SootMethod, SootClass> _triple = workbagCache.getWork();
			final Stmt _stmt = _triple.getFirst();
			final SootMethod _method = _triple.getSecond();
			final SootClass _thrownType = _triple.getThird();
			final Collection<CallTriple> _callers = cgi.getCallers(_method);
			final Iterator<CallTriple> _j = _callers.iterator();
			final int _jEnd = _callers.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final CallTriple _ctrp = _j.next();
				final SootMethod _caller = _ctrp.getMethod();
				final Stmt _callingStmt = _ctrp.getStmt();

				if (isThrownExceptionNotCaught(_callingStmt, _caller, _thrownType)) {
					workbagCache.addWorkNoDuplicates(new Triple<Stmt, SootMethod, SootClass>(_callingStmt, _caller, _thrownType));
				}
			}
			MapUtils.putIntoCollectionInMap(MapUtils.getMapFromMap(method2stmt2uncaughtExceptions, _method), 
					_stmt, _thrownType);
		}

		workbagCache.clear();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - END - "
					+ MapUtils.verbosePrint("method-to-stmt-to-throwntypes", method2stmt2uncaughtExceptions));
		}
	}

	/**
	 * @see IExceptionRaisingInfo#doesStmtThrowUncaughtException(soot.jimple.Stmt, soot.SootMethod)
	 */
	public boolean doesStmtThrowUncaughtException(final Stmt stmt, final SootMethod method) {
		final Map<Stmt, Collection<SootClass>> _map = MapUtils.queryObject(method2stmt2uncaughtExceptions, method,
				Collections.<Stmt, Collection<SootClass>>emptyMap());
		return _map.containsKey(stmt);
	}

	/**
	 * @see IExceptionRaisingInfo#getExceptionsThrownBy(soot.jimple.Stmt, soot.SootMethod)
	 */
	public Collection<SootClass> getExceptionsThrownBy(final Stmt stmt, final SootMethod method) {
		final Map<Stmt, Collection<SootClass>> _map = MapUtils.queryObject(method2stmt2exceptions, method,
				Collections.<Stmt, Collection<SootClass>>emptyMap());
		final Collection<SootClass> _col1 = MapUtils.queryObject(_map, stmt, Collections.<SootClass>emptySet());
		final Collection<SootClass> _col2 = getUncaughtExceptionsThrownBy(stmt, method);
		return SetUtils.union(_col1, _col2);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection<? extends Comparable<? extends Object>> getIds() {
		return Collections.singleton(ID);
	}

	/**
	 * @see IExceptionRaisingInfo#getUncaughtExceptionsThrownBy(soot.jimple.Stmt, soot.SootMethod)
	 */
	public Collection<SootClass> getUncaughtExceptionsThrownBy(final Stmt stmt, final SootMethod method) {
		final Map<Stmt, Collection<SootClass>> _map = MapUtils.queryObject(method2stmt2uncaughtExceptions, method,
				Collections.<Stmt, Collection<SootClass>>emptyMap());
		return MapUtils.queryObject(_map, stmt, Collections.<SootClass>emptySet());
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.registerForAllStmts(this);

		final Collection<Class<?>> _c = astNodeType2thrownTypeNames.keySet();
		final Iterator<Class<?>> _i = _c.iterator();
		final int _iEnd = _c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			ppc.register(_i.next(), this);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	@Override public void reset() {
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
	 * Toggles the tracking of the named exception type for the given ast node type.
	 * 
	 * @param astNodeType is a concrete jimple class that represents a <code>Value</code>, e.g, InstanceFieldRef.class
	 * @param exceptionName is the name of the exception that needs to be tracked at the given ast node.
	 * @param consider <code>true</code> indicates that the exception needs to be tracked; <code>false</code> indicates
	 *            that the exception should not be tracked.
	 * @pre astNodeType != null and exceptionName != null
	 */
	public void toggleExceptionsToTrack(final Class astNodeType, final String exceptionName, final boolean consider) {
		if (consider) {
			MapUtils.putIntoCollectionInMap(astNodeType2thrownTypeNames, astNodeType, exceptionName);
		} else {
			final Collection<String> _typeNames = MapUtils.queryObject(astNodeType2thrownTypeNames, astNodeType,
					Collections.<String>emptySet());
			_typeNames.remove(exceptionName);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		final StringBuffer _sb = new StringBuffer();
		_sb.append(MapUtils.verbosePrint("Caught+Uncaught Exception info", method2stmt2exceptions));
		_sb.append(MapUtils.verbosePrint("Uncaught exception info", method2stmt2uncaughtExceptions));
		return _sb.toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregisterForAllStmts(this);

		final Collection<Class<?>> _c = astNodeType2thrownTypeNames.keySet();
		final Iterator<Class<?>> _i = _c.iterator();
		final int _iEnd = _c.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			ppc.unregister(_i.next(), this);
		}
	}

	/**
	 * Retrieves the exception type names for the given ast node type.
	 * 
	 * @param c is the ast node type.
	 * @return a collection of exception type names.
	 * @pre c != null
	 * @post result != null and result.oclIsKindOf(Collection(String))
	 */
	private Collection<String> getExceptionTypeNamesFor(final Class<? extends Value> c) {
		Collection<String> _result = Collections.emptySet();
		final Set<Class<?>> _keySet = astNodeType2thrownTypeNames.keySet();
		final Iterator<Class<?>> _i = _keySet.iterator();
		final int _iEnd = _keySet.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Class<?> _nodeType = _i.next();

			if (_nodeType.isAssignableFrom(c)) {
				_result = astNodeType2thrownTypeNames.get(_nodeType);
				break;
			}
		}
		return _result;
	}

	/**
	 * Checks if the given exception type thrown from the given statement in the given method is not caught in the given
	 * method.
	 * 
	 * @param stmt at which the exception occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @param thrownType is the type of the exception that is thrown.
	 * @return <code>true</code> if the thrown exception is caught; <code>false</code>, otherwise.
	 * @pre stmt != null and method != null and thrownType != null
	 */
	private boolean isThrownExceptionNotCaught(final Stmt stmt, final SootMethod method, final SootClass thrownType) {
		final UnitGraph _graph = stmtGraphFactory.getStmtGraph(method);
		final List<Stmt> _succs = _graph.getSuccsOf(stmt);
		boolean _result = true;

		if (!_succs.isEmpty()) {
			boolean _isCaught = false;
			final Body _body = _graph.getBody();
			final Iterator<Stmt> _i = _succs.iterator();
			final int _iEnd = _succs.size();

			for (int _iIndex = 0; _iIndex < _iEnd && !_isCaught; _iIndex++) {
				final Stmt _handler = _i.next();
				final Iterator<RefType> _j = TrapManager.getExceptionTypesOf(_handler, _body).iterator();
				final int _jEnd = TrapManager.getExceptionTypesOf(_handler, _body).size();

				for (int _jIndex = 0; _jIndex < _jEnd && !_isCaught; _jIndex++) {
					final RefType _caughtType = _j.next();
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
	 * @param stmt that may cause the exception.
	 * @param method contains <code>stmt</code>.
	 * @param thrownTypes is the collection to which <code>thrownType</code> should be added if it is caught.
	 * @param thrownType is the exception to be checked for at <code>stmt</code>.
	 * @pre stmt != null and method != null and thrownTypes != null and thrownType != null
	 */
	private void processStmt(final Stmt stmt, final SootMethod method, final Collection<SootClass> thrownTypes,
			final SootClass thrownType) {
		if (isThrownExceptionNotCaught(stmt, method, thrownType)) {
			workbagCache.addWorkNoDuplicates(new Triple<Stmt, SootMethod, SootClass>(stmt, method, thrownType));
		} else {
			thrownTypes.add(thrownType);
		}
	}
}

// End of File
