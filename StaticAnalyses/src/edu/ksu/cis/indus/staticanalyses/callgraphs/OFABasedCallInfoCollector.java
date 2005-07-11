
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
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
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
 * This implementation of <code>CallGraphInfo.ICallInfo</code> generates call info for a system based on the information
 * available from object flow information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class OFABasedCallInfoCollector
  extends AbstractValueAnalyzerBasedProcessor
  implements ICallInfoCollector {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(OFABasedCallInfoCollector.class);

	/** 
	 * The FA instance which implements object flow analysis. This instance is used to calculate call graphCache information.
	 *
	 * @invariant analyzer.oclIsKindOf(OFAnalyzer)
	 */
	private IValueAnalyzer analyzer;

	/** 
	 * This holds call information.
	 */
	private final CallInfo callInfoHolder = new CallInfo();

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		// all method marked by the object flow analyses are reachable.
		callInfoHolder.addReachable(method);
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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback(ValueBox vBox = " + vBox + ", Context context = " + context + ") - BEGIN");
		}

		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		final Value _value = vBox.getValue();
		final InvokeExpr _invokeExpr = (InvokeExpr) _value;
        final SootMethod _callee = _invokeExpr.getMethod();

		if (_value instanceof StaticInvokeExpr || _value instanceof SpecialInvokeExpr) {
            final Set _callees = CollectionsUtilities.getSetFromMap(callInfoHolder.caller2callees, _caller);
            final CallTriple _triple1 = new CallTriple(_callee, _stmt, _invokeExpr);
			_callees.add(_triple1);

            final Set _callers = CollectionsUtilities.getSetFromMap(callInfoHolder.callee2callers, _callee);
            final CallTriple _triple2 = new CallTriple(_caller, _stmt, _invokeExpr);
			_callers.add(_triple2);			
		} else {
			callBackOnInstanceInvokeExpr(context, (InstanceInvokeExpr) _value);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callback() - END");
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

		callInfoHolder.fixupMethodsHavingZeroCallersAndCallees();
		
		stable();
	}

	/**
	 * @see ICallInfoCollector#getCallInfo()
	 */
	public CallGraphInfo.ICallInfo getCallInfo() {
		return callInfoHolder;
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
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#processingBegins()
	 */
	public void processingBegins() {
		unstable();
	}
	
	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#reset()
	 */
	public void reset() {
		callInfoHolder.reset();
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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callBackOnInstanceInvokeExpr(Context context = " + context + ", InstanceInvokeExpr expr = " + expr
				+ ") - BEGIN");
		}

		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		final SootMethod _calleeMethod = expr.getMethod();
		context.setProgramPoint(expr.getBaseBox());

		final Collection _values = analyzer.getValues(expr.getBase(), context);

		if (!_values.isEmpty()) {
			final Map _callee2callers = callInfoHolder.callee2callers;
			final Map _caller2callees = callInfoHolder.caller2callees;
			final Set _callees = CollectionsUtilities.getSetFromMap(_caller2callees, _caller);
			final CallTriple _ctrp = new CallTriple(_caller, _stmt, expr);

			for (final Iterator _i = _values.iterator(); _i.hasNext();) {
				final Object _t = _i.next();

				if (!(_t instanceof NewExpr || _t instanceof StringConstant)) {
					continue;
				}

				SootClass _accessClass = null;

				if (_t instanceof NewExpr) {
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

				final Set _callers = CollectionsUtilities.getSetFromMap(_callee2callers, _callee);
				_callers.add(_ctrp);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("callBackOnInstanceInvokeExpr() - END");
		}
	}
}

// End of File
