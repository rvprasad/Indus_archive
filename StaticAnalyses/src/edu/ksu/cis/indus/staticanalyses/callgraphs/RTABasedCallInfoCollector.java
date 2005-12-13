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

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.StaticFieldRef;

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
	 * The collection of classes that are instantiated.
	 */
	private final Collection<SootClass> instantiatedClasses = new HashSet<SootClass>();

	/**
	 * This maps method to the collection of classes that are instantiated whose effect has been considered on the key method.
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
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.Local, soot.SootMethod)
	 */
	@Override public void callback(final Local local, final SootMethod method) {
		final Type _type = local.getType();

		if (_type instanceof RefType) {
			capturePossibleRoots(((RefType) _type).getSootClass(), method);
		} else if (_type instanceof ArrayType) {
			final Type _baseType = ((ArrayType) _type).baseType;

			if (_baseType instanceof RefType) {
				capturePossibleRoots(((RefType) _baseType).getSootClass(), method);
			}
		}
	}

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

		if (_value instanceof NewExpr) {
			_type = ((NewExpr) _value).getType();
		} else if (_value instanceof InstanceFieldRef || _value instanceof StaticFieldRef) {
			final FieldRef _f = (FieldRef) _value;
			_type = _f.getType();
			MapUtils.putIntoCollectionInMap(method2instantiatedClasses, context.getCurrentMethod(), _f.getField()
					.getDeclaringClass());
		} else if (_value instanceof ArrayRef) {
			final ArrayRef _a = (ArrayRef) _value;
			_type = _a.getType();
		} else if (_value instanceof CastExpr) {
			final CastExpr _c = (CastExpr) _value;
			_type = _c.getType();
		} else if (_value instanceof NewArrayExpr) {
			final NewArrayExpr _n = (NewArrayExpr) _value;
			_type = _n.getBaseType();
		} else {
			// if (_value instanceof NewMultiArrayExpr)
			final NewMultiArrayExpr _n = (NewMultiArrayExpr) _value;
			_type = _n.getBaseType().baseType;
		}

		if (_type != null && _type instanceof RefType) {
			MapUtils.putIntoCollectionInMap(method2instantiatedClasses, context.getCurrentMethod(),
					((RefType) _type).getSootClass());
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	@Override public void consolidate() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("consolidate() - BEGIN");
		}

		final IWorkBag<SootMethod> _wb = new HistoryAwareFIFOWorkBag<SootMethod>(new HashSet());
		_wb.addAllWorkNoDuplicates(roots);

		while (_wb.hasWork()) {
			final SootMethod _sootMethod = _wb.getWork();
			callInfoHolder.addReachable(_sootMethod);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consolidate() - Processed method:  : " + _sootMethod);
			}

			processCallerAndAlreadyInstantiatedClasses(_sootMethod, _wb);

			final Collection<SootClass> _t = MapUtils.getCollectionFromMap(method2instantiatedClasses, _sootMethod);

			if (!instantiatedClasses.containsAll(_t)) {
				_t.removeAll(instantiatedClasses);
				instantiatedClasses.addAll(_t);
				processNewInstantiatedClasses(_t, _wb);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("consolidate() - Considered classes:  : " + _t);
				}
			}

			final Collection<SootMethod> _requiredClassInitializers = MapUtils.getCollectionFromMap(method2requiredCLInits,
					_sootMethod);
			_wb.addAllWorkNoDuplicates(_requiredClassInitializers);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("consolidate() - Required initializers:  : " + _requiredClassInitializers);
			}
		}

		callInfoHolder.fixupMethodsHavingZeroCallersAndCallees();

		instantiatedClasses.clear();
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
		ppc.registerForLocals(this);
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
		ppc.unregisterForLocals(this);
	}

	/**
	 * Captures the class initializers stemming from the given class that are needed to execute the given method.
	 *
	 * @param sootClass of interest.
	 * @param method of interest.
	 * @pre sootClass != null and method != null
	 */
	private void capturePossibleRoots(final SootClass sootClass, final SootMethod method) {
		final Collection<SootClass> _classes = new HashSet<SootClass>();
		_classes.add(sootClass);
		_classes.addAll(cha.getProperAncestorClassesOf(sootClass));
		_classes.addAll(cha.getProperAncestorInterfacesOf(sootClass));

		final Collection<SootMethod> _reqMethods = MapUtils.getCollectionFromMap(method2requiredCLInits, method);
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
	 * @pre caller != null and wb != null
	 */
	private void processCallerAndAlreadyInstantiatedClasses(final SootMethod caller, final IWorkBag<SootMethod> wb) {
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

			if (instantiatedClasses.contains(_calleeDeclaringClass)
					|| CollectionUtils.containsAny(instantiatedClasses, cha.getProperSubclassesOf(_calleeDeclaringClass))) {
				MapUtils.putIntoCollectionInMap(callInfoHolder.callee2callers, _callee, new CallTriple(caller,
						_calleeTriple.getStmt(), _calleeTriple.getExpr()));
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
	 * @pre newClasses.oclIsKindOf(Collection(SootClass))
	 */
	private void processNewInstantiatedClasses(final Collection<SootClass> newClasses, final IWorkBag<SootMethod> wb) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processNewInstantiatedClassesAndReachables(Collection newClasses = " + newClasses
					+ ", IWorkBag wb = " + wb + ") - BEGIN");
		}

		final Collection<SootMethod> _col = Util.getResolvedMethods(newClasses);
		final Map<SootMethod, Collection<CallTriple>> _chaCallee2Callers = chaCallInfo.getCallInfo().getCallee2CallersMap();
		final Collection<SootMethod> _reachables = callInfoHolder.getReachableMethods();
		final Iterator<SootMethod> _j = _col.iterator();
		final int _jEnd = _col.size();

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
					MapUtils.putIntoCollectionInMap(callInfoHolder.caller2callees, _caller, new CallTriple(
							_callee, _callerTriple.getStmt(), _callerTriple.getExpr()));
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
}

// End of File
