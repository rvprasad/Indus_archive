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

import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.IStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.ITokenProcessingWork;
import edu.ksu.cis.indus.staticanalyses.flow.InvocationVariant;
import edu.ksu.cis.indus.staticanalyses.flow.ValuedVariant;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Local;
import soot.SootField;
import soot.Type;
import soot.Value;
import soot.jimple.ArrayRef;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.UnopExpr;
import soot.jimple.VirtualInvokeExpr;

/**
 * The expression visitor used in flow insensitive mode of object flow analysis.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T> is the type of the token set object.
 */
class FlowInsensitiveExprSwitch<T extends ITokens<T, Value>>
		extends AbstractExprSwitch<FlowInsensitiveExprSwitch<T>, Value, T, OFAFGNode<T>, Type> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FlowInsensitiveExprSwitch.class);

	/**
	 * This maps Java strings to Jimple string constant objects.
	 */
	private static final Map<String, StringConstant> STR_TO_CONST = new HashMap<String, StringConstant>();

	/**
	 * The token manager to be used.
	 */
	protected final ITokenManager<T, Value, Type> tokenMgr;

	/**
	 * This retrieves values for a given type.
	 */
	protected final Value2ValueMapper valueRetriever;

	/**
	 * Creates a new <code>FlowInsensitiveExprSwitch</code> instance.
	 * 
	 * @param statementSwitch the statement visitor which uses this object.
	 * @param nodeConnector the connector to be used to connect ast and non-ast flow graph node.
	 * @param type2valueMapper provides the values of a given type in the system.
	 */
	FlowInsensitiveExprSwitch(@NonNull final IFGNodeConnector<OFAFGNode<T>> nodeConnector,
			@NonNull final Value2ValueMapper type2valueMapper, @NonNull final IStmtSwitch statementSwitch) {
		super(statementSwitch, nodeConnector);

		if (fa != null) {
			tokenMgr = fa.getTokenManager();
		} else {
			tokenMgr = null;
		}

		valueRetriever = type2valueMapper;
	}

	/**
	 * Processes array access expressions. Current implementation processes the primary and connects a node associated with
	 * the primary to a <code>FGAccessNode</code> which monitors this access expressions for new values in the primary.
	 * 
	 * @param e the array access expressions.
	 * @pre e != null
	 */
	@Override public void caseArrayRef(final ArrayRef e) {
		process(e.getBaseBox());

		final OFAFGNode<T> _baseNode = getFlowNode();
		final OFAFGNode<T> _ast = method.getASTNode(e, context);
		MethodVariant.setFilterOfBasedOn(_ast, e.getType(), tokenMgr);

		final ITokenProcessingWork<T> _work = new ArrayAccessExprWork<T>(method, context, _ast, connector, tokenMgr
				.getNewTokenSet());
		final OFAFGNode<T> _temp = new FGAccessNode<T>(_work, fa, tokenMgr);
		MethodVariant.setFilterOfBasedOn(_temp, e.getBase().getType(), tokenMgr);
		_baseNode.addSucc(_temp);
		process(e.getIndexBox());
		setFlowNode(_ast);
	}

	/**
	 * Processes the cast expression. Current implementation processes the expression being cast.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseCastExpr(final CastExpr e) {
		process(e.getOpBox());

		if (Util.isReferenceType(e.getCastType())) {
			// NOTE: We need to filter expressions based on the cast type as casts result in type-conformant values at
			// run-time.
			final OFAFGNode<T> _base = getFlowNode();
			final OFAFGNode<T> _cast = method.getASTNode(e, context);
			MethodVariant.setFilterOfBasedOn(_cast, e.getType(), tokenMgr);
			_base.addSucc(_cast);
			setFlowNode(_cast);
		}
	}

	/**
	 * Processes the given exception reference expression. This is required to thread the flow of exception in the system.
	 * 
	 * @param e is the caught exception reference.
	 * @pre e != null
	 */
	@Override public void caseCaughtExceptionRef(final CaughtExceptionRef e) {
		final OFAFGNode<T> _node = method.getASTNode(e, context);
		MethodVariant.setFilterOfBasedOn(_node, e.getType(), tokenMgr);
		setFlowNode(_node);
	}

	/**
	 * Processes the field expression in a fashion similar to array access expressions.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseInstanceFieldRef(final InstanceFieldRef e) {
		process(e.getBaseBox());

		final OFAFGNode<T> _baseNode = getFlowNode();
		final OFAFGNode<T> _ast = method.getASTNode(e, context);
		MethodVariant.setFilterOfBasedOn(_ast, e.getType(), tokenMgr);

		final ITokenProcessingWork<T> _work = new FieldAccessExprWork<T>(method, context, _ast, connector, tokenMgr
				.getNewTokenSet());
		final FGAccessNode<T> _temp = new FGAccessNode<T>(_work, fa, tokenMgr);
		MethodVariant.setFilterOfBasedOn(_temp, e.getBase().getType(), tokenMgr);
		_baseNode.addSucc(_temp);
		setFlowNode(_ast);
	}

	/**
	 * Processes the embedded expressions.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseInstanceOfExpr(final InstanceOfExpr e) {
		process(e.getOpBox());
	}

	/**
	 * Processes the embedded expressions.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr e) {
		processInstanceInvokeExpr(e);
	}

	/**
	 * Processes the local expression.
	 * 
	 * @param e the expression to be processed.
	 * @pre != null
	 */
	@Override public void caseLocal(final Local e) {
		final OFAFGNode<T> _node = method.getASTNode(e, context);
		MethodVariant.setFilterOfBasedOn(_node, e.getType(), tokenMgr);
		setFlowNode(_node);
	}

	/**
	 * Processes the new array expression. This injects a value into the flow graph.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseNewArrayExpr(final NewArrayExpr e) {
		process(e.getSizeBox());

		Object _temp = null;

		final boolean _flag = context instanceof AllocationContext;

		if (_flag) {
			_temp = ((AllocationContext) context).setAllocationSite(e);
		}

		final OFAFGNode<T> _ast = method.getASTNode(e, context);
		MethodVariant.setFilterOfBasedOn(_ast, e.getType(), tokenMgr);
		fa.getArrayVariant((ArrayType) e.getType(), context);
		_ast.injectValue(valueRetriever.getValue(e));
		setFlowNode(_ast);

		if (_flag) {
			((AllocationContext) context).setAllocationSite(_temp);
		}
	}

	/**
	 * Processes the new expression. This injects a value into the flow graph.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseNewExpr(final NewExpr e) {
		final OFAFGNode<T> _ast = method.getASTNode(e, context);
		MethodVariant.setFilterOfBasedOn(_ast, e.getType(), tokenMgr);
		_ast.injectValue(valueRetriever.getValue(e));
		setFlowNode(_ast);
	}

	/**
	 * Processes the new array expression. This injects values into the flow graph for each dimension for which the size is
	 * specified.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseNewMultiArrayExpr(final NewMultiArrayExpr e) {
		final ArrayType _arrayType = e.getBaseType();
		final Type _baseType = _arrayType.baseType;

		Object _temp = null;

		final boolean _flag = context instanceof AllocationContext;

		if (_flag) {
			_temp = ((AllocationContext) context).setAllocationSite(e);
		}

		for (int _i = _arrayType.numDimensions, _sizes = e.getSizeCount(); _i > 0 && _sizes > 0; _i--, _sizes--) {
			final ArrayType _aType = ArrayType.v(_baseType, _i);
			final ValuedVariant<OFAFGNode<T>> _array = fa.getArrayVariant(_aType, context);
			process(e.getSizeBox(_sizes - 1));
			_array.getFGNode().injectValue(valueRetriever.getValue(e));
		}

		if (_flag) {
			((AllocationContext) context).setAllocationSite(_temp);
		}

		final OFAFGNode<T> _ast = method.getASTNode(e, context);
		MethodVariant.setFilterOfBasedOn(_ast, e.getType(), tokenMgr);
		_ast.injectValue(valueRetriever.getValue(e));
		setFlowNode(_ast);
	}

	/**
	 * Processes <code>null</code>. This injects a value into the flow graph.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseNullConstant(final NullConstant e) {
		final OFAFGNode<T> _ast = method.getASTNode(e, context);
		_ast.injectValue(e);
		setFlowNode(_ast);
	}

	/**
	 * Processes parameter reference expressions.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseParameterRef(final ParameterRef e) {
		final OFAFGNode<T> _node = method.queryParameterNode(e.getIndex());
		setFlowNode(_node);
	}

	/**
	 * Processes the embedded expressions.
	 * 
	 * @param e the expression to be processed.
	 */
	@Override public void caseSpecialInvokeExpr(final SpecialInvokeExpr e) {
		processInvokedMethod(e);
	}

	/**
	 * Processes the embedded expressions.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseStaticFieldRef(final StaticFieldRef e) {
		final SootField _field = e.getField();
		final OFAFGNode<T> _ast = method.getASTNode(e, context);
		MethodVariant.setFilterOfBasedOn(_ast, e.getType(), tokenMgr);

		final OFAFGNode<T> _nonast = fa.getFieldVariant(_field).getFGNode();
		MethodVariant.setFilterOfBasedOn(_nonast, _field.getType(), tokenMgr);
		connector.connect(_ast, _nonast);
		setFlowNode(_ast);
	}

	/**
	 * Processes the embedded expressions.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseStaticInvokeExpr(final StaticInvokeExpr e) {
		processInvokedMethod(e);
	}

	/**
	 * Processes a string constant. This injects a value into the flow graph.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseStringConstant(final StringConstant e) {
		final OFAFGNode<T> _ast = method.getASTNode(e, context);
		_ast.injectValue(getCanonicalStringConstant(e));
		setFlowNode(_ast);
	}

	/**
	 * Processes the <code>this</code> variable. Current implementation returns the node associated with the enclosing
	 * method.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseThisRef(@SuppressWarnings("unused") final ThisRef e) {
		setFlowNode(method.queryThisNode());
	}

	/**
	 * Processes the embedded expressions.
	 * 
	 * @param e the expression to be processed.
	 * @pre e != null
	 */
	@Override public void caseVirtualInvokeExpr(final VirtualInvokeExpr e) {
		processInstanceInvokeExpr(e);
	}

	/**
	 * Processes cases which are not dealt by this visitor methods or delegates to suitable methods depending on the type.
	 * 
	 * @param o the expression to be processed.
	 * @pre e != null
	 */
	@Override public void defaultCase(final Object o) {
		final Value _v = (Value) o;

		if (_v instanceof BinopExpr) {
			final BinopExpr _temp = (BinopExpr) _v;
			process(_temp.getOp1Box());
			process(_temp.getOp2Box());
		} else if (_v instanceof UnopExpr) {
			final UnopExpr _temp = (UnopExpr) _v;
			process(_temp.getOpBox());
		} else {
			super.defaultCase(o);
		}
	}

	/**
	 * Returns a new instance of the this class.
	 * 
	 * @param o the statement visitor which uses the new instance.
	 * @return the new instance of this class.
	 * @pre o != null and o[0].oclIsKindOf(IStmtSwitch)
	 * @post result != null
	 */
	@Override public FlowInsensitiveExprSwitch<T> getClone(final Object... o) {
		return new FlowInsensitiveExprSwitch<T>(connector, valueRetriever, (IStmtSwitch) o[0]);
	}

	/**
	 * Processes the invoke expressions that require resolution by creating nodes to various data components present at the
	 * call-site and making them available to be connected when new method implementations are plugged in.
	 * 
	 * @param e the invoke expression to be processed.
	 * @pre e != null
	 */
	protected void processInstanceInvokeExpr(final InstanceInvokeExpr e) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + e);
		}
		fa.processClass(e.getMethod().getDeclaringClass());
		process(e.getBaseBox());

		final OFAFGNode<T> _receiverNode = getFlowNode();
		MethodVariant.setFilterOfBasedOn(_receiverNode, e.getBase().getType(), tokenMgr);

		for (int _i = 0; _i < e.getArgCount(); _i++) {
			process(e.getArgBox(_i));
		}

		final InvocationVariant<OFAFGNode<T>> _iv = (InvocationVariant) method.getASTVariant(e, context);

		if (Util.isReferenceType(e.getType())) {
			final OFAFGNode<T> _ast = _iv.getFGNode();
			MethodVariant.setFilterOfBasedOn(_ast, e.getType(), tokenMgr);
			setFlowNode(_ast);
		} else {
			setFlowNode(null);
		}

		final ITokenProcessingWork<T> _work = new InvokeExprWork<T>(method, context, tokenMgr.getNewTokenSet());
		final FGAccessNode<T> _baseNode = new FGAccessNode<T>(_work, fa, tokenMgr);
		MethodVariant.setFilterOfBasedOn(_baseNode, e.getBase().getType(), tokenMgr);
		_receiverNode.addSucc(_baseNode);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + e);
		}
	}

	/**
	 * Retrieves the canonical string constant that represents the given string constant.
	 * 
	 * @param e is the string constant that needs to be canonicalized.
	 * @return the canonical string constant.
	 */
	private StringConstant getCanonicalStringConstant(final StringConstant e) {
		if (STR_TO_CONST.containsKey(e.value)) {
			return STR_TO_CONST.get(e.value);
		}
		STR_TO_CONST.put(e.value, e);
		return e;
	}

	/**
	 * Processes the invoke expressions that do not require resolution by creating nodes to various data components present at
	 * the call-site and making them available to be connected when new method implementations are plugged in.
	 * 
	 * @param e the invoke expression to be processed.
	 * @pre e != null
	 */
	private void processInvokedMethod(final InvokeExpr e) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + e);
		}

		final IMethodVariant<OFAFGNode<T>> _callee = fa.getMethodVariant(e.getMethod(), context);

		if (e instanceof SpecialInvokeExpr) {
			final SpecialInvokeExpr _expr = (SpecialInvokeExpr) e;
			final OFAFGNode<T> _thisNode = _callee.queryThisNode();
			process(_expr.getBaseBox());

			final OFAFGNode<T> _thisArgNode = getFlowNode();
			MethodVariant.setFilterOfBasedOn(_thisArgNode, _expr.getBase().getType(), tokenMgr);

			_thisArgNode.addSucc(_thisNode);
		}

		for (int _i = 0; _i < e.getArgCount(); _i++) {
			if (Util.isReferenceType(e.getArg(_i).getType())) {
				process(e.getArgBox(_i));

				final OFAFGNode<T> _argNode = getFlowNode();
				MethodVariant.setFilterOfBasedOn(_argNode, e.getArg(_i).getType(), tokenMgr);

				_argNode.addSucc(_callee.queryParameterNode(_i));
			}
		}

		final InvocationVariant<OFAFGNode<T>> _iv = (InvocationVariant) method.getASTVariant(e, context);
		final OFAFGNode<T> _throwNode = _iv.getThrowNode();
		MethodVariant.setFilterOfBasedOn(_throwNode, fa.getClass("java.lang.Throwable").getType(), tokenMgr);
		_callee.queryThrownNode().addSucc(_throwNode);

		if (Util.isReferenceType(e.getType())) {
			final OFAFGNode<T> _ast = _iv.getFGNode();
			MethodVariant.setFilterOfBasedOn(_ast, e.getType(), tokenMgr);
			_callee.queryReturnNode().addSucc(_ast);
			setFlowNode(_ast);
		} else {
			setFlowNode(null);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + e);
		}
	}
}

// End of File
