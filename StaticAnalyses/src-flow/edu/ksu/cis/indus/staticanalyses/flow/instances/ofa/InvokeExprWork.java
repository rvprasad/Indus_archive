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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.NullConstant;

/**
 * This class represents a peice of work that plugin new fragments of flow graph as new types which provide new
 * implementations flow into the receiver at the associated call-site.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T>  is the type of the token set object.
 */
class InvokeExprWork<T extends ITokens<T, Value>>
		extends AbstractAccessExprWork<T> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(InvokeExprWork.class);

	/**
	 * Indicates if the method represented by this object returns a value of with reference-like type.
	 * 
	 * @invariant returnsRefLikeType != null
	 */
	protected final boolean returnsRefLikeType;

	/**
	 * The collection of variants already processed/installed at the given access expression. We do not want to process
	 * variants again and again.
	 * 
	 * @invariant installedVariants != null
	 */
	private final Collection<IMethodVariant<OFAFGNode<T>>> installedVariants = new HashSet<IMethodVariant<OFAFGNode<T>>>();

	/**
	 * Creates a new <code>InvokeExprWork</code> instance.
	 * 
	 * @param callerMethod the method in which the call occurs.
	 * @param callContext the context in which the invocation occurs.
	 * @param tokenSet used to store the tokens that trigger the execution of this work peice.
	 * @throws IllegalArgumentException when <code>accessExprBox</code> does not wrap an <code>InstanceInvokeExpr</code>
	 *             object.
	 * @pre callerMethod != null and callContext != null and tokenSet != null
	 */
	public InvokeExprWork(final IMethodVariant<OFAFGNode<T>> callerMethod, final Context callContext, final T tokenSet) {
		super(callerMethod, callContext, tokenSet);

		final ValueBox _invocationExpr = callContext.getProgramPoint();

		if (!(_invocationExpr.getValue() instanceof InstanceInvokeExpr)) {
			throw new IllegalArgumentException("accessExprBox has to contain a InstanceInvokeExpr object as value.");
		}

		final InstanceInvokeExpr _ie = (InstanceInvokeExpr) _invocationExpr.getValue();
		this.returnsRefLikeType = _ie.getMethod().getReturnType() instanceof RefLikeType;
	}

	/**
	 * Checks if any of the <code>values</code> provide a new method implementation. If so, plugs in the flow graph for the
	 * new implementation at the method invocation site connecting the nodes suitably. It plugs in call-backs resulting from
	 * native method calls.
	 */
	public synchronized void execute() {
		final InstanceInvokeExpr _e = (InstanceInvokeExpr) accessExprBox.getValue();
		final ValueBox _vb = context.getProgramPoint();
		final Collection<Value> _values = tokens.getValues();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(_values + " values arrived at base node of " + accessExprBox.getValue() + " in " + context);
		}

		for (final Iterator<Value> _i = _values.iterator(); _i.hasNext();) {
			final Value _v = _i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Value: " + _v);
			}

			if (_v instanceof NullConstant) {
				continue;
			}

			try {
				processExprAgainstReceiver(_e, _v);
			} catch (final IllegalStateException _excp) {
				context.setProgramPoint(_vb);
				throw _excp;
			}
		}
		context.setProgramPoint(_vb);
		tokens.clear();
	}

	/**
	 * Returns a stringized representation of this object.
	 * 
	 * @return the stringized representation of this object.
	 * @post result != null
	 */
	@Override public String toString() {
		return "InvokeExprWork: " + caller.getMethod() + "@" + accessExprBox.getValue();
	}

	/**
	 * Processes the given invoke expression for the given receiver object.
	 * 
	 * @param expr is the invoke expr.
	 * @param receiver is the receiver object.
	 * @pre expr != null and receiver != null
	 */
	private void processExprAgainstReceiver(final InstanceInvokeExpr expr, final Value receiver) {
		final Type _t = receiver.getType();
		final SootClass _sc;
		final FA<?, ?, OFAFGNode<T>, ?> _fa = caller.getFA();

		if (_t instanceof RefType) {
			_sc = _fa.getClass(((RefType) receiver.getType()).getClassName());
		} else if (_t instanceof ArrayType) {
			_sc = _fa.getClass("java.lang.Object");
		} else {
			final IllegalStateException _excp = new IllegalStateException(
					"Non-reference/array type flowing into invocation site.");

			if (LOGGER.isErrorEnabled()) {
				LOGGER.error(expr.toString());
			}
			throw _excp;
		}

		final SootMethod _sm;

		try {
			_sm = Util.findDeclaringMethod(_sc, expr.getMethod());
		} catch (final IllegalStateException _excp) {
			LOGGER.error(_sc + ":" + context.getCurrentMethod() + "@" + expr, _excp);
			throw _excp;
		}

		final IMethodVariant<OFAFGNode<T>> _mv = _fa.getMethodVariant(_sm, context);

		if (!installedVariants.contains(_mv)) {
			for (int _j = 0; _j < _sm.getParameterCount(); _j++) {
				if (_sm.getParameterType(_j) instanceof RefLikeType) {
					final OFAFGNode<T> _param = _mv.queryParameterNode(_j);
					context.setProgramPoint(expr.getArgBox(_j));
					final OFAFGNode<T> _arg = caller.queryASTNode(expr.getArg(_j), context);
					_arg.addSucc(_param);
				}
			}

			final OFAFGNode<T> _thisNode = _mv.queryThisNode();
			context.setProgramPoint(expr.getBaseBox());
			final OFAFGNode<T> _receiverNode = caller.queryASTNode(expr.getBase(), context);
			_receiverNode.addSucc(_thisNode);

			final OFAFGNode<T> _thrownNode = _mv.queryThrownNode();
			context.setProgramPoint(accessExprBox);
			final OFAFGNode<T> _receivingNode = caller.queryThrowNode(expr, context);
			_thrownNode.addSucc(_receivingNode);

			if (returnsRefLikeType) {
				final OFAFGNode<T> _returnNode = _mv.queryReturnNode();
				final OFAFGNode<T> _returnValueNode = caller.queryASTNode(expr, context);
				_returnNode.addSucc(_returnValueNode);
			}
			installedVariants.add(_mv);
		}
	}
}

// End of File
