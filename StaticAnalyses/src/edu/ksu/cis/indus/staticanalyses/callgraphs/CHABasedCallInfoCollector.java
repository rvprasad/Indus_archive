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

package edu.ksu.cis.indus.staticanalyses.callgraphs;

import edu.ksu.cis.indus.common.collections.MapUtils;
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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		extends AbstractProcessor
		implements ICallInfoCollector {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(CHABasedCallInfoCollector.class);

	/**
	 * This holds call information.
	 */
	private final CallInfo callInfoHolder = new CallInfo();

	/**
	 * The class hierarchy analysis to be used.
	 */
	private IClassHierarchy cha;

	/**
	 * This maps invoked methods (not resolved) to caller-site triple.
	 */
	private final Map<SootMethod, Collection<CallTriple>> invokedMethod2callerTriple = new HashMap<SootMethod, Collection<CallTriple>>();

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootMethod)
	 */
	@Override public void callback(final SootMethod method) {
		callInfoHolder.addReachable(method);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	@Override public void callback(final ValueBox vBox, final Context context) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback(ValueBox vBox = " + vBox + ", Context context = " + context + ") - BEGIN");
		}

		final Value _val = vBox.getValue();
		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		final InvokeExpr _expr = (InvokeExpr) _val;
		final SootMethod _callee = _expr.getMethod();
		final Collection<CallTriple> _callees = MapUtils.getCollectionFromMap(callInfoHolder.caller2callees, _caller);
		final CallTriple _callerTriple = new CallTriple(_caller, _stmt, _expr);

		if (_expr instanceof StaticInvokeExpr
				|| (_expr instanceof SpecialInvokeExpr && (_callee.getName().equals("<init>") || _callee.isPrivate()))) {
			final Collection<CallTriple> _callers = MapUtils.getCollectionFromMap(callInfoHolder.callee2callers, _callee);
			_callers.add(_callerTriple);
			_callees.add(new CallTriple(_callee, _stmt, _expr));
		} else {
			MapUtils.putIntoCollectionInMap(invokedMethod2callerTriple, _callee, _callerTriple);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback() - END");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#consolidate()
	 */
	@Override public void consolidate() {
		final Collection<SootMethod> _temp = new HashSet<SootMethod>();
		final Collection<SootClass> _implClasses = new HashSet<SootClass>();
		final Set<Map.Entry<SootMethod, Collection<CallTriple>>> _entrySet = invokedMethod2callerTriple.entrySet();
		final Iterator<Map.Entry<SootMethod, Collection<CallTriple>>> _j = _entrySet.iterator();
		final int _jEnd = _entrySet.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final Map.Entry<SootMethod, Collection<CallTriple>> _entry = _j.next();
			final SootMethod _invokedMethod = _entry.getKey();
			final Collection<CallTriple> _callerTriples = _entry.getValue();
			final String _invokedMethodSubSignature = _invokedMethod.getSubSignature();
			final SootClass _declInterface = _invokedMethod.getDeclaringClass();
			_implClasses.clear();
			_implClasses.addAll(cha.getProperSubclassesOf(_declInterface));
			_implClasses.add(_declInterface);

			final Iterator<SootClass> _i = _implClasses.iterator();
			final int _iEnd = _implClasses.size();

			_temp.clear();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootClass _impl = _i.next();

				if (_impl.declaresMethod(_invokedMethodSubSignature)
						&& !_impl.getMethod(_invokedMethodSubSignature).isAbstract()) {
					final SootMethod _implMethod = _impl.getMethod(_invokedMethodSubSignature);
					MapUtils.putAllIntoCollectionInMap(callInfoHolder.callee2callers, _implMethod, _callerTriples);
					_temp.add(_implMethod);
				}
			}

			final Iterator<CallTriple> _k = _callerTriples.iterator();
			final int _kEnd = _callerTriples.size();

			for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
				final CallTriple _callerTriple = _k.next();
				final SootMethod _caller = _callerTriple.getMethod();
				final Stmt _stmt = _callerTriple.getStmt();
				final InvokeExpr _expr = _callerTriple.getExpr();
				final Collection<CallTriple> _callees = MapUtils.getCollectionFromMap(callInfoHolder.caller2callees, _caller);
				final Iterator<SootMethod> _l = _temp.iterator();
				final int _lEnd = _temp.size();

				for (int _lIndex = 0; _lIndex < _lEnd; _lIndex++) {
					final SootMethod _callee = _l.next();
					_callees.add(new CallTriple(_callee, _stmt, _expr));
				}
			}
		}

		callInfoHolder.fixupMethodsHavingZeroCallersAndCallees();

		invokedMethod2callerTriple.clear();

		stable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END - Call info collection");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.callgraphs.ICallInfoCollector#getCallInfo()
	 */
	public CallGraphInfo.ICallInfo getCallInfo() {
		return callInfoHolder;
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
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#processingBegins()
	 */
	@Override public void processingBegins() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN - Call info collection");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	@Override public void reset() {
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
}

// End of File
