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

package edu.ksu.cis.indus.staticanalyses.callgraphs;

import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IClassHierarchy;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

/**
 * This implementation collects call information using rapid-type analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class RTABasedCallInfoCollector
		extends AbstractProcessor
		implements ICallInfoCollector {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RTABasedCallInfoCollector.class);

	/**
	 * This holds call information.
	 */
	private final CallInfo callInfoHolder = new CallInfo();

	/**
	 * The instance of class hierarchy to be used for analysis.
	 */
	private IClassHierarchy cha;

	/**
	 * This holds CHA based call information.
	 */
	private CHABasedCallInfoCollector chaCallInfo;

	/**
	 * DOCUMENT ME!
	 */
	private IEnvironment env;

	/**
	 * This maps method to the collection of classes whose instances are created in the key method.
	 */
	private final Map<SootMethod, Collection<SootClass>> method2instantiatedClasses = new HashMap<SootMethod, Collection<SootClass>>();

	/**
	 * This maps method to the collection of class initializers that need to be hooked into the call graph.
	 */
	private final Map<SootMethod, Collection<SootMethod>> method2requiredCLInits = new HashMap<SootMethod, Collection<SootMethod>>();

	/**
	 * The collection of methods that serve as root methods during the analysis.
	 */
	private final Collection<SootMethod> roots = new HashSet<SootMethod>();

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootMethod)
	 */
	@Override public void callback(final SootMethod method) {
		final Collection<SootClass> _temp = new HashSet<SootClass>();
		_temp.addAll(method.getExceptions());
		_temp.add(method.getDeclaringClass());

		for (int _j = method.getParameterCount() - 1; _j >= 0; _j--) {
			final Type _type = method.getParameterType(_j);

			if (_type instanceof RefType) {
				_temp.add(((RefType) _type).getSootClass());
			}
		}

		final Type _type = method.getReturnType();

		if (_type != null && _type instanceof RefType) {
			_temp.add(((RefType) _type).getSootClass());
		}

		final Iterator<SootClass> _i = _temp.iterator();
		final int _iEnd = _temp.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			capturePossibleRoots(_i.next(), method);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	@Override public void callback(final ValueBox vBox, final Context context) {
		final Value _value = vBox.getValue();
		Type _type = null;

		final SootMethod _currentMethod = context.getCurrentMethod();
		if (_value instanceof NewExpr || _value instanceof InstanceFieldRef || _value instanceof StaticFieldRef
				|| _value instanceof ArrayRef || _value instanceof CastExpr || _value instanceof Local) {
			_type = _value.getType();
		} else if (_value instanceof NewArrayExpr) {
			final NewArrayExpr _n = (NewArrayExpr) _value;
			_type = _n.getBaseType();
		} else if (_value instanceof StringConstant) {
			_type = env.getClass("java.lang.String").getType();
		} else if (_value instanceof StaticInvokeExpr) {
			recordCall(context, (InvokeExpr) _value);
		} else if (_value instanceof SpecialInvokeExpr) {
			final SootMethod _invoked = ((SpecialInvokeExpr) _value).getMethod();
			if (_invoked.getName().equals("<init>") || _invoked.isPrivate()) {
				recordCall(context, (InvokeExpr) _value);
			}
		} else if (_value instanceof NewMultiArrayExpr) {
			final NewMultiArrayExpr _n = (NewMultiArrayExpr) _value;
			_type = _n.getBaseType().baseType;
		}

		if (_type != null && _type instanceof RefType) {
			MapUtils.putIntoCollectionInMap(method2instantiatedClasses, _currentMethod, ((RefType) _type).getSootClass());
			capturePossibleRoots(((RefType) _type).getSootClass(), _currentMethod);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	@Override public void consolidate() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("consolidate() - BEGIN");
		}

		final Collection<SootClass> _instantiatedClasses = new HashSet<SootClass>();
		final IWorkBag<SootMethod> _wb = new HistoryAwareFIFOWorkBag<SootMethod>(new HashSet<SootMethod>());
		_wb.addAllWorkNoDuplicates(roots);

		while (_wb.hasWork()) {
			final SootMethod _sootMethod = _wb.getWork();
			callInfoHolder.addReachable(_sootMethod);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consolidate() - Processed method: " + _sootMethod);
			}

			processCallerAndAlreadyInstantiatedClasses(_sootMethod, _wb, _instantiatedClasses);

			final Collection<SootClass> _t = MapUtils.getCollectionFromMap(method2instantiatedClasses, _sootMethod);

			if (!_instantiatedClasses.containsAll(_t)) {
				_t.removeAll(_instantiatedClasses);
				_instantiatedClasses.addAll(_t);
				processNewInstantiatedClasses(_t, _wb);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("consolidate() - Considered classes: " + _t);
				}
			}

			final Collection<SootMethod> _requiredClassInitializers = MapUtils.getCollectionFromMap(method2requiredCLInits,
					_sootMethod);
			_wb.addAllWorkNoDuplicates(_requiredClassInitializers);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consolidate() - Required initializers: " + _requiredClassInitializers);
			}
		}

		callInfoHolder.fixupMethodsHavingZeroCallersAndCallees();

		method2instantiatedClasses.clear();
		method2requiredCLInits.clear();
		roots.clear();

		stable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("consolidate() - END");
		}
	}

	/**
	 * @see ICallInfoCollector#getCallInfo()
	 */
	public CallGraphInfo.ICallInfo getCallInfo() {
		return callInfoHolder;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.register(NewExpr.class, this);
		ppc.register(InstanceFieldRef.class, this);
		ppc.register(StaticFieldRef.class, this);
		ppc.register(ArrayRef.class, this);
		ppc.register(CastExpr.class, this);
		ppc.register(Local.class, this);
		ppc.register(SpecialInvokeExpr.class, this);
		ppc.register(StaticInvokeExpr.class, this);
		ppc.register(StringConstant.class, this);
		env = ppc.getEnvironment();
	}

	/**
	 * Initializes the collector.
	 *
	 * @param chaBasedCallInfo is the CHA based call info collector.
	 * @param analysis is an instance of class hierarchy to use.
	 * @pre chaBasedCallInfo != null and analysis != null
	 */
	public void initialize(final CHABasedCallInfoCollector chaBasedCallInfo, final IClassHierarchy analysis) {
		this.chaCallInfo = chaBasedCallInfo;
		this.cha = analysis;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#processingBegins()
	 */
	@Override public void processingBegins() {
		unstable();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	@Override public void reset() {
		cha = null;
		chaCallInfo = null;
		roots.clear();
		callInfoHolder.reset();
		method2instantiatedClasses.clear();
		method2requiredCLInits.clear();
	}

	/**
	 * Sets the root methods for the analysis.
	 *
	 * @param rootMethods of course.
	 */
	public void setRootMethods(final Collection<SootMethod> rootMethods) {
		roots.clear();
		roots.addAll(rootMethods);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(InstanceFieldRef.class, this);
		ppc.unregister(StaticFieldRef.class, this);
		ppc.unregister(ArrayRef.class, this);
		ppc.unregister(CastExpr.class, this);
		ppc.unregister(Local.class, this);
		ppc.unregister(SpecialInvokeExpr.class, this);
		ppc.unregister(StaticInvokeExpr.class, this);
		ppc.unregister(StringConstant.class, this);
		env = null;
	}

	/**
	 * Captures the class initializers stemming from the given class that are needed to execute the given method.
	 *
	 * @param sootClass of interest.
	 * @param method of interest.
	 * @pre sootClass != null and method != null
	 */
	private void capturePossibleRoots(final SootClass sootClass, final SootMethod method) {
		final Collection<SootMethod> _reqMethods = MapUtils.getCollectionFromMap(method2requiredCLInits, method);
		final Collection<SootClass> _classes = new HashSet<SootClass>();
		_classes.add(sootClass);
		_classes.addAll(cha.getProperAncestorClassesOf(sootClass));
		_classes.addAll(cha.getProperAncestorInterfacesOf(sootClass));

		final Iterator<SootClass> _i = _classes.iterator();
		final int _iEnd = _classes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = _i.next();

			if (_sc.declaresMethodByName("<clinit>")) {
				final SootMethod _sm = _sc.getMethodByName("<clinit>");
				_reqMethods.add(_sm);
			}
		}
	}

	/**
	 * Process the given caller against previously processed class instantiations.
	 *
	 * @param caller of interest.
	 * @param wb to be populated with work pertaining to expansion of call info.
	 * @param instantiatedClasses is the collection of classes that have been instantiated.
	 * @pre caller != null and wb != null and instantiatedClasses != null
	 */
	private void processCallerAndAlreadyInstantiatedClasses(final SootMethod caller, final IWorkBag<SootMethod> wb,
			final Collection<SootClass> instantiatedClasses) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processCallerAndAlreadyInstantiatedClasses(SootMethod caller = " + caller + ", IWorkBag wb = " + wb
					+ ") - BEGIN");
		}

		final Collection<CallTriple> _chaCallees = new HashSet<CallTriple>();
		_chaCallees.addAll(MapUtils.queryCollection(chaCallInfo.getCallInfo().getCaller2CalleesMap(), caller));
		_chaCallees.removeAll(MapUtils.queryCollection(callInfoHolder.getCaller2CalleesMap(), caller));

		final Iterator<CallTriple> _i = _chaCallees.iterator();
		final int _iEnd = _chaCallees.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final CallTriple _calleeTriple = _i.next();
			final SootMethod _callee = _calleeTriple.getMethod();
			final SootClass _calleeDeclaringClass = _callee.getDeclaringClass();

			if (!_callee.isAbstract()
					&& (instantiatedClasses.contains(_calleeDeclaringClass)
							|| CollectionUtils.containsAny(instantiatedClasses, cha
									.getProperSubclassesOf(_calleeDeclaringClass)) || _callee.isStatic() || (_calleeTriple
							.getExpr() instanceof SpecialInvokeExpr)
							&& _callee.getName().equals("<init>"))) {
				MapUtils.putIntoCollectionInMap(callInfoHolder.callee2callers, _callee, new CallTriple(caller, _calleeTriple
						.getStmt(), _calleeTriple.getExpr()));
				MapUtils.putIntoCollectionInMap(callInfoHolder.caller2callees, caller, _calleeTriple);
				wb.addWorkNoDuplicates(_callee);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processCallerAndAlreadyInstantiatedClasses() - END");
		}
	}

	/**
	 * Process the newly instantiated classes to expand the call info.
	 *
	 * @param newClasses that were instantiated.
	 * @param wb that will be populated with work pertaining to expansion of call info.
	 * @pre newClasses != null and wb != null
	 */
	private void processNewInstantiatedClasses(final Collection<SootClass> newClasses, final IWorkBag<SootMethod> wb) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processNewInstantiatedClassesAndReachables(Collection newClasses = " + newClasses
					+ ", IWorkBag wb = " + wb + ") - BEGIN");
		}

		final Map<SootMethod, Collection<CallTriple>> _chaCallee2Callers = chaCallInfo.getCallInfo().getCallee2CallersMap();
		final Collection<SootMethod> _reachables = callInfoHolder.getReachableMethods();
		final Collection<SootMethod> _methodsInNewClasses = Util.getResolvedMethods(newClasses);
		final Iterator<SootMethod> _j = _methodsInNewClasses.iterator();
		final int _jEnd = _methodsInNewClasses.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final SootMethod _callee = _j.next();
			final Collection<CallTriple> _callers = MapUtils.queryCollection(_chaCallee2Callers, _callee);
			final Iterator<CallTriple> _k = _callers.iterator();
			final int _kEnd = _callers.size();
			boolean _flag = false;

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final CallTriple _callerTriple = _k.next();
				final SootMethod _caller = _callerTriple.getMethod();

				if (_reachables.contains(_caller)) {
					_flag |= true;
					MapUtils.putIntoCollectionInMap(callInfoHolder.callee2callers, _callee, _callerTriple);
					MapUtils.putIntoCollectionInMap(callInfoHolder.caller2callees, _caller, new CallTriple(_callee,
							_callerTriple.getStmt(), _callerTriple.getExpr()));
				}
			}

			if (_flag) {
				wb.addWorkNoDuplicates(_callee);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processNewInstantiatedClassesAndReachables() - END");
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param context DOCUMENT ME!
	 * @param expr DOCUMENT ME!
	 */
	private void recordCall(final Context context, final InvokeExpr expr) {
		final SootMethod _invokedMethod = expr.getMethod();
		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		final CallTriple _callerTriple = new CallTriple(_caller, _stmt, expr);
		MapUtils.putIntoCollectionInMap(callInfoHolder.callee2callers, _invokedMethod, _callerTriple);
		MapUtils.putIntoCollectionInMap(callInfoHolder.caller2callees, _caller, new CallTriple(_invokedMethod, _stmt, expr));
		callInfoHolder.addReachable(_caller);
		callInfoHolder.addReachable(_invokedMethod);
	}
}

// End of File
