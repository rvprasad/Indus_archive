
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

import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This implementation calculates call information based on class-hierarchy information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CHABasedCallInfoCollector
  extends AbstractProcessor {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CHABasedCallInfoCollector.class);

	/** 
	 * This holds call information.
	 */
	private final CallInfoProvider callInfoHolder = new CallInfoProvider();

	/** 
	 * The class hierarchy analysis to be used.
	 */
	private IClassHierarchy cha;

	/** 
	 * This maps invoked methods (not resolved) to caller-site triple.
	 *
	 * @invariant invokedMethod2callerTriple.oclIsKindOf(Map(SootMethod, CallTriple))
	 */
	private final Map invokedMethod2callerTriple = new HashMap();

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
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		callInfoHolder.callee2callers.get(method);
		callInfoHolder.caller2callees.get(method);
		callInfoHolder.reachables.add(method);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback(ValueBox vBox = " + vBox + ", Context context = " + context + ") - BEGIN");
		}

		final Value _val = vBox.getValue();
		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		final InvokeExpr _expr = (InvokeExpr) _val;
		final SootMethod _callee = _expr.getMethod();
		final Collection _callees = CollectionsUtilities.getSetFromMap(callInfoHolder.caller2callees, _caller);
		final CallTriple _callerTriple = new CallTriple(_caller, _stmt, _expr);

		if (_expr instanceof StaticInvokeExpr
			  || (_expr instanceof SpecialInvokeExpr && (_callee.getName().equals("<init>") || _callee.isPrivate()))) {
			final Collection _callers = CollectionsUtilities.getSetFromMap(callInfoHolder.callee2callers, _callee);
			_callers.add(_callerTriple);
			_callees.add(new CallTriple(_callee, _stmt, _expr));
		} else {
			CollectionsUtilities.putIntoSetInMap(invokedMethod2callerTriple, _callee, _callerTriple);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback() - END");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	public void consolidate() {
		final Collection _temp = new HashSet();
		final Collection _implClasses = new HashSet();
		final Set _entrySet = invokedMethod2callerTriple.entrySet();
		final Iterator _j = _entrySet.iterator();
		final int _jEnd = _entrySet.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Map.Entry _entry = (Map.Entry) _j.next();
			final SootMethod _invokedMethod = (SootMethod) _entry.getKey();
			final Collection _callerTriples = (Collection) _entry.getValue();
			final String _invokedMethodSubSignature = _invokedMethod.getSubSignature();
			final SootClass _declInterface = _invokedMethod.getDeclaringClass();
			_implClasses.clear();
			_implClasses.addAll(cha.properSubclassesOf(_declInterface));
			_implClasses.add(_declInterface);

			final Iterator _i = _implClasses.iterator();
			final int _iEnd = _implClasses.size();

			_temp.clear();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootClass _impl = (SootClass) _i.next();

				if (_impl.declaresMethod(_invokedMethodSubSignature)
					  && !_impl.getMethod(_invokedMethodSubSignature).isAbstract()) {
					final SootMethod _implMethod = _impl.getMethod(_invokedMethodSubSignature);
					CollectionsUtilities.putAllIntoSetInMap(callInfoHolder.callee2callers, _implMethod, _callerTriples);
					_temp.add(_implMethod);
				}
			}

			final Iterator _k = _callerTriples.iterator();
			final int _kEnd = _callerTriples.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final CallTriple _callerTriple = (CallTriple) _k.next();
				final SootMethod _caller = _callerTriple.getMethod();
				final Stmt _stmt = _callerTriple.getStmt();
				final InvokeExpr _expr = _callerTriple.getExpr();
				final Collection _callees = CollectionsUtilities.getSetFromMap(callInfoHolder.caller2callees, _caller);
				final Iterator _l = _temp.iterator();
				final int _lEnd = _temp.size();

				for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
					final SootMethod _callee = (SootMethod) _l.next();
					_callees.add(new CallTriple(_callee, _stmt, _expr));
				}
			}
		}

		calculateHeads(callInfoHolder);

		invokedMethod2callerTriple.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.register(InterfaceInvokeExpr.class, this);
		ppc.register(SpecialInvokeExpr.class, this);
		ppc.register(StaticInvokeExpr.class, this);
		ppc.register(VirtualInvokeExpr.class, this);
	}

	/**
	 * Creates an instance of this class.
	 *
	 * @param analysis to be used.
	 */
	public void initialize(final IClassHierarchy analysis) {
		this.cha = analysis;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	public void reset() {
		super.reset();
		callInfoHolder.reset();
		cha = null;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregister(InterfaceInvokeExpr.class, this);
		ppc.unregister(SpecialInvokeExpr.class, this);
		ppc.unregister(StaticInvokeExpr.class, this);
		ppc.unregister(VirtualInvokeExpr.class, this);
	}

	/**
	 * Calculates head methods.
	 *
	 * @param callInfoHolder for which the heads need to be calculated.
	 *
	 * @pre callInfoHolder != null
	 */
	static void calculateHeads(final CallInfoProvider callInfoHolder) {
		final Set _keySet = callInfoHolder.callee2callers.keySet();
		final Iterator _i = _keySet.iterator();
		final int _iEnd = _keySet.size();
		final Collection _heads = callInfoHolder.heads;
		_heads.addAll(callInfoHolder.reachables);

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _method = (SootMethod) _i.next();

			if (!((Collection) MapUtils.getObject(callInfoHolder.callee2callers, _method, Collections.EMPTY_SET)).isEmpty()) {
				_heads.remove(_method);
			}
		}
	}
}

// End of File
