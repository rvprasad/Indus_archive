
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

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;


/**
 * This implementation of <code>CallGraphInfo.ICallInfoProvider</code> generates call info for a system based on the 
 * information available from object flow information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class OFABasedCallInfoProvider
  extends AbstractValueAnalyzerBasedProcessor
  implements CallGraphInfo.ICallInfoProvider {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(OFABasedCallInfoProvider.class);

	/** 
	 * The FA instance which implements object flow analysis. This instance is used to calculate call graphCache information.
	 *
	 * @invariant analyzer.oclIsKindOf(OFAnalyzer)
	 */
	private IValueAnalyzer analyzer;

	/** 
	 * The collection of methods from which the system can be started. Although an instance of a class can be created and a
	 * method can be invoked on it from the environment, this method will not be considered as a <i>head method </i>.
	 * However, our definition of head methods are those methods (excluding those invoked via <code>invokespecial</code>
	 * bytecode) with no caller method that belongs to the system.
	 *
	 * @invariant head != null and heads.oclIsKindOf(Set(SootMethod))
	 */
	private Collection heads = new HashSet();

	/** 
	 * The collection of methods that are reachble in the system.
	 *
	 * @invariant reachables.oclIsKindOf(Set(SootMethod))
	 */
	private Collection reachables = new HashSet();

	/** 
	 * This maps callees to callers.
	 *
	 * @invariant callee2callers.oclIsKindOf(Map(SootMethod, Set(CallTriple)))
	 */
	private Map callee2callers = new HashMap();

	/** 
	 * This maps callers to callees.
	 *
	 * @invariant caller2callees.oclIsKindOf(Map(SootMethod, Set(CallTriple)))
	 */
	private Map caller2callees = new HashMap();

	/** 
     * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
     */
    public void reset() {
        heads.clear();
        reachables.clear();
        callee2callers.clear();
        caller2callees.clear();
    }

    /**
	 * Sets the analyzer to be used to calculate call graph information upon call back.
	 *
	 * @param objFlowAnalyzer that provides the information to create the call graph.
	 *
	 * @pre objFlowAnalyzer != null and objFlowAnalyzer.oclIsKindOf(OFAnalyzer)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer objFlowAnalyzer) {
		analyzer = objFlowAnalyzer;
	}

	/**
	 * @see CallGraphInfo.ICallInfoProvider#getCallee2CallersMap()
	 */
	public Map getCallee2CallersMap() {
		return callee2callers;
	}

	/**
	 * @see CallGraphInfo.ICallInfoProvider#getCaller2CalleesMap()
	 */
	public Map getCaller2CalleesMap() {
		return caller2callees;
	}

	/**
	 * @see CallGraphInfo.ICallInfoProvider#getHeads()
	 */
	public Collection getHeads() {
		return heads;
	}

	/**
	 * @see CallGraphInfo.ICallInfoProvider#getReachableMethods()
	 */
	public Collection getReachableMethods() {
		return reachables;
	}

	/**
	 * Called by the post process controller when it walks a jimple value AST node.
	 *
	 * @param vBox is the AST node to be processed.
	 * @param context in which value should be processed.
	 *
	 * @pre context != null and vBox != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(ValueBox,Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		SootMethod _callee = null;
		Set _callees;
		Set _callers;
		CallTriple _triple;
		final Value _value = vBox.getValue();

		if (_value instanceof StaticInvokeExpr) {
			final InvokeExpr _invokeExpr = (InvokeExpr) _value;
			_callee = _invokeExpr.getMethod();

			if (caller2callees.containsKey(_caller)) {
				_callees = (Set) caller2callees.get(_caller);
			} else {
				_callees = new HashSet();
				caller2callees.put(_caller, _callees);
			}
			_triple = new CallTriple(_callee, _stmt, _invokeExpr);
			_callees.add(_triple);

			if (callee2callers.containsKey(_callee)) {
				_callers = (Set) callee2callers.get(_callee);
			} else {
				_callers = new HashSet();
				callee2callers.put(_callee, _callers);
			}
			_triple = new CallTriple(_caller, _stmt, _invokeExpr);
			_callers.add(_triple);
		} else if (_value instanceof InterfaceInvokeExpr
			  || _value instanceof VirtualInvokeExpr
			  || _value instanceof SpecialInvokeExpr) {
			callBackOnInstanceInvokeExpr(context, (InstanceInvokeExpr) _value);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		// all method marked by the object flow analyses are reachable.
		reachables.add(method);

		if (method.getName().equals("<clinit>")) {
			heads.add(method);
		}
	}

	/**
	 * This calculates information such as heads, tails, and such.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#consolidate()
	 */
	public void consolidate() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: call graph consolidation");
		}
		heads.addAll(analyzer.getEnvironment().getRoots());

		// populate the caller2callees with head information in cases there are
		// no calls in the system.
		if (caller2callees.isEmpty()) {
			for (final Iterator _i = heads.iterator(); _i.hasNext();) {
				final Object _head = _i.next();
				caller2callees.put(_head, Collections.EMPTY_LIST);
			}
		}

		
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		unstable();
		ppc.register(VirtualInvokeExpr.class, this);
		ppc.register(InterfaceInvokeExpr.class, this);
		ppc.register(StaticInvokeExpr.class, this);
		ppc.register(SpecialInvokeExpr.class, this);
		ppc.register(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(VirtualInvokeExpr.class, this);
		ppc.unregister(InterfaceInvokeExpr.class, this);
		ppc.unregister(StaticInvokeExpr.class, this);
		ppc.unregister(SpecialInvokeExpr.class, this);
		ppc.unregister(this);
		stable();
	}

	/**
	 * Called as a result of callback durign processing the AST when instance invoke expression is encountered.
	 *
	 * @param context in which expression should be processed.
	 * @param expr is the expression.
	 *
	 * @pre context != null and stmt != null and caller != null and expr != null
	 */
	private void callBackOnInstanceInvokeExpr(final Context context, final InstanceInvokeExpr expr) {
		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		final SootMethod _calleeMethod = expr.getMethod();
		context.setProgramPoint(expr.getBaseBox());

		final Collection _values = analyzer.getValues(expr.getBase(), context);

		if (!_values.isEmpty()) {
			final Set _callees;

			if (caller2callees.containsKey(_caller)) {
				_callees = (Set) caller2callees.get(_caller);
			} else {
				_callees = new HashSet();
				caller2callees.put(_caller, _callees);
			}

			final CallTriple _ctrp = new CallTriple(_caller, _stmt, expr);

			for (final Iterator _i = _values.iterator(); _i.hasNext();) {
				final Object _t = _i.next();

				if (!(_t instanceof NewExpr || _t instanceof StringConstant)) {
					continue;
				}

				SootClass _accessClass = null;

				if (expr instanceof SpecialInvokeExpr && _calleeMethod.getName().equals("<init>")) {
					_accessClass = _calleeMethod.getDeclaringClass();
				} else if (_t instanceof NewExpr) {
					final NewExpr _newExpr = (NewExpr) _t;
					_accessClass = analyzer.getEnvironment().getClass(_newExpr.getBaseType().getClassName());
				} else if (_t instanceof StringConstant) {
					_accessClass = analyzer.getEnvironment().getClass("java.lang.String");
				}

				final String _methodName = _calleeMethod.getName();
				final List _parameterTypes = _calleeMethod.getParameterTypes();
				final Type _returnType = _calleeMethod.getReturnType();
				final SootMethod _callee =
					Util.findMethodImplementation(_accessClass, _methodName, _parameterTypes, _returnType);
				final CallTriple _triple = new CallTriple(_callee, _stmt, expr);
				_callees.add(_triple);

				final Set _callers;

				if (callee2callers.containsKey(_callee)) {
					_callers = (Set) callee2callers.get(_callee);
				} else {
					_callers = new HashSet();
					callee2callers.put(_callee, _callers);
				}
				_callers.add(_ctrp);
			}
		}
	}
}

// End of File
