
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IClassHierarchy;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.ValueBox;

import soot.jimple.NewExpr;


/**
 * This implementation collects call information using rapid-type analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class RTABasedCallInfoCollector
  extends AbstractProcessor {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(RTABasedCallInfoCollector.class);

	/** 
	 * This holds call information.
	 */
	private final CallInfoProvider callInfoHolder = new CallInfoProvider();

	/** 
	 * This holds CHA based call information.
	 */
	private CHABasedCallInfoCollector chaCallInfo;

	/** 
	 * The collection of classes that are instantiated.
	 *
	 * @invariant instantiatedClasses.oclIsKindOf(Collection(SootClass))
	 */
	private final Collection instantiatedClasses = new HashSet();

	/** 
	 * The collection of methods that serve as root methods during the analysis.
	 *
	 * @invariant roots.oclIsKindOf(Collection(SootMethod))
	 */
	private final Collection roots = new HashSet();

	/** 
	 * The instance of class hierarchy to be used for analysis.
	 */
	private IClassHierarchy cha;

	/** 
	 * This maps method to the collection of classes that are instantiated whose effect has been considered on the key
	 * method.
	 *
	 * @invariant method2instantiatedClasses.oclIsKindOf(Map(SootMethod, Collection(SootClass)))
	 */
	private final Map method2instantiatedClasses = new HashMap();

	/** 
	 * This maps method to the collection of class initializers that need to be hooked into the call graph.
	 *
	 * @invariant method2requiredCLInits.oclIsKindOf(Map(SootMethod, Collection(SootMethod)))
	 */
	private final Map method2requiredCLInits = new HashMap();

	/**
	 * Retrieves the call info calculated by this class.
	 *
	 * @return the call info.
	 *
	 * @post result != null
	 */
	public CallGraphInfo.ICallInfoProvider getCallInfoProvider() {
		return callInfoHolder;
	}

	/**
	 * Sets the root methods for the analysis.
	 *
	 * @param rootMethods of course.
	 *
	 * @pre rootMethods.oclIsKindOf(Collection(SootMethod))
	 */
	public void setRootMethods(final Collection rootMethods) {
		roots.clear();
		roots.addAll(rootMethods);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		final Collection _exceptions = method.getExceptions();
		final Iterator _i = _exceptions.iterator();
		final int _iEnd = _exceptions.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			capturePossibleRoots((SootClass) _i.next(), method);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.Local, soot.SootMethod)
	 */
	public void callback(final Local local, final SootMethod method) {
		final Type _type = local.getType();

		if (_type instanceof RefType) {
			capturePossibleRoots(((RefType) _type).getSootClass(), method);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		CollectionsUtilities.putIntoSetInMap(method2instantiatedClasses, context.getCurrentMethod(),
			((RefType) ((NewExpr) vBox.getValue()).getType()).getSootClass());
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	public void consolidate() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - BEGIN");
		}

		final Collection _temp = new HashSet();
		final IWorkBag _wb = new FIFOWorkBag();
		_wb.addAllWorkNoDuplicates(roots);

		while (_wb.hasWork()) {
			final Object _o = _wb.getWork();
			final SootMethod _sootMethod = (SootMethod) _o;
			callInfoHolder.reachables.add(_o);
			processCallerAndAlreadyInstantiatedClasses(_sootMethod, _wb);

			final Collection _t = CollectionsUtilities.getSetFromMap(method2instantiatedClasses, _o);

			if (!instantiatedClasses.containsAll(_t)) {
				_t.removeAll(instantiatedClasses);
				instantiatedClasses.addAll(_t);
				processNewInstantiatedClasses(_t, _wb);
			}

			final Set _s = CollectionsUtilities.getSetFromMap(method2requiredCLInits, _sootMethod);
			_wb.addAllWorkNoDuplicates(CollectionUtils.subtract(_s, _temp));
			_temp.addAll(_s);
		}

		CHABasedCallInfoCollector.calculateHeads(callInfoHolder);
		method2instantiatedClasses.clear();
		method2requiredCLInits.clear();
		roots.clear();

		stable();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("consolidate() - END");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.register(NewExpr.class, this);
		ppc.registerForLocals(this);
	}

	/**
	 * Initializes the collector.
	 *
	 * @param chaBasedCallInfo is the CHA based call info collector.
	 * @param analysis is an instance of class hierarchy to use.
	 *
	 * @pre chaBasedCallInfo != null and analysis != null
	 */
	public void initialize(final CHABasedCallInfoCollector chaBasedCallInfo, final IClassHierarchy analysis) {
		this.chaCallInfo = chaBasedCallInfo;
		this.cha = analysis;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#processingBegins()
	 */
	public void processingBegins() {
		unstable();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	public void reset() {
		cha = null;
		chaCallInfo = null;
		roots.clear();
		callInfoHolder.reset();
		method2instantiatedClasses.clear();
		method2requiredCLInits.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregister(NewExpr.class, this);
		ppc.unregisterForLocals(this);
	}

	/**
	 * Captures the class initializers stemming from the given class that are needed to execute the given method.
	 *
	 * @param sootClass of interest.
	 * @param method of interest.
	 *
	 * @pre sootClass != null and method != null
	 */
	private void capturePossibleRoots(final SootClass sootClass, final SootMethod method) {
		final Collection _classes = new HashSet();
		_classes.add(sootClass);
		_classes.addAll(cha.properAncestorClassesOf(sootClass));
		_classes.addAll(cha.properAncestorInterfacesOf(sootClass));

		final Collection _reqMethods = CollectionsUtilities.getSetFromMap(method2requiredCLInits, method);
		final Iterator _i = _classes.iterator();
		final int _iEnd = _classes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = (SootClass) _i.next();

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
	 *
	 * @pre caller != null and wb != null
	 */
	private void processCallerAndAlreadyInstantiatedClasses(final SootMethod caller, final IWorkBag wb) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processCallerAndAlreadyInstantiatedClasses(SootMethod caller = " + caller + ", IWorkBag wb = " + wb
				+ ") - BEGIN");
		}

		final Collection _chaCallees =
			(Collection) MapUtils.getObject(chaCallInfo.getCallInfoProvider().getCaller2CalleesMap(), caller,
				Collections.EMPTY_SET);
		final Map _callee2callers = callInfoHolder.callee2callers;
		final Map _caller2callees = callInfoHolder.caller2callees;
		final Iterator _i = _chaCallees.iterator();
		final int _iEnd = _chaCallees.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final CallTriple _calleeTriple = (CallTriple) _i.next();
			final SootMethod _callee = _calleeTriple.getMethod();
			final SootClass _calleeDeclaringClass = _callee.getDeclaringClass();

			if (instantiatedClasses.contains(_calleeDeclaringClass)
				  || CollectionUtils.containsAny(instantiatedClasses, cha.properSubclassesOf(_calleeDeclaringClass))) {
				CollectionsUtilities.putIntoSetInMap(_callee2callers, _callee,
					new CallTriple(caller, _calleeTriple.getStmt(), _calleeTriple.getExpr()));
				CollectionsUtilities.putIntoSetInMap(_caller2callees, caller, _calleeTriple);
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
	 *
	 * @pre newClasses != null and wb != null
	 * @pre newClasses.oclIsKindOf(Collection(SootClass))
	 */
	private void processNewInstantiatedClasses(final Collection newClasses, final IWorkBag wb) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processNewInstantiatedClassesAndReachables(Collection newClasses = " + newClasses
				+ ", IWorkBag wb = " + wb + ") - BEGIN");
		}

		final Map _chaCallee2Callers = chaCallInfo.getCallInfoProvider().getCallee2CallersMap();
		final Set _entrySet = _chaCallee2Callers.entrySet();
		final Map _caller2callees = callInfoHolder.caller2callees;
		final Map _callee2callers = callInfoHolder.callee2callers;
		final Collection _reachables = callInfoHolder.reachables;
		final Iterator _j = _entrySet.iterator();
		final int _jEnd = _entrySet.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Map.Entry _entry = (Map.Entry) _j.next();
			final SootMethod _callee = (SootMethod) _entry.getKey();
			final SootClass _calleeDeclaringClass = _callee.getDeclaringClass();

			if (newClasses.contains(_calleeDeclaringClass)
				  || CollectionUtils.containsAny(newClasses, cha.properSubclassesOf(_calleeDeclaringClass))) {
				final Collection _callers = (Collection) _entry.getValue();
				final Iterator _k = _callers.iterator();
				final int _kEnd = _callers.size();
				boolean _flag = false;

				for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
					final CallTriple _callerTriple = (CallTriple) _k.next();
					final SootMethod _caller = _callerTriple.getMethod();

					if (_reachables.contains(_caller)) {
						_flag |= true;
						CollectionsUtilities.putIntoSetInMap(_callee2callers, _callee, _callerTriple);
						CollectionsUtilities.putIntoSetInMap(_caller2callees, _caller,
							new CallTriple(_callee, _callerTriple.getStmt(), _callerTriple.getExpr()));
					}
				}

				if (_flag) {
					wb.addWorkNoDuplicates(_callee);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("processNewInstantiatedClassesAndReachables() - END");
		}
	}
}

// End of File
