
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.indus.staticanalyses.flow.IMethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.ITokenProcessingWork;
import edu.ksu.cis.indus.staticanalyses.flow.InvocationVariant;
import edu.ksu.cis.indus.staticanalyses.flow.ValuedVariant;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.IType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
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
 */
class FlowInsensitiveExprSwitch
  extends AbstractExprSwitch {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FlowInsensitiveExprSwitch.class);

	/** 
	 * The token manager to be used.
	 */
	protected final ITokenManager tokenMgr;

	/**
	 * Creates a new <code>FlowInsensitiveExprSwitch</code> instance.
	 *
	 * @param statementSwitch the statement visitor which uses this object.
	 * @param nodeConnector the connector to be used to connect ast and non-ast flow graph node.
	 *
	 * @pre statementSwitch != null and nodeConnector != null
	 */
	public FlowInsensitiveExprSwitch(final AbstractStmtSwitch statementSwitch, final IFGNodeConnector nodeConnector) {
		super(statementSwitch, nodeConnector);

		if (fa != null) {
			tokenMgr = fa.getTokenManager();
		} else {
			tokenMgr = null;
		}
	}

	/**
	 * Returns a new instance of the this class.
	 *
	 * @param o the statement visitor which uses the new instance.
	 *
	 * @return the new instance of this class.
	 *
	 * @pre o != null and o.oclIsKindOf(StmtSwitch)
	 * @post result != null and result.oclIsKindOf(FlowInsensitiveExprSwitch)
	 */
	public Object getClone(final Object o) {
		return new FlowInsensitiveExprSwitch((StmtSwitch) o, connector);
	}

	/**
	 * Processes array access expressions.  Current implementation processes the primary and connects a node associated with
	 * the primary to a <code>FGAccessNode</code> which monitors this access expressions for new values in the primary.
	 *
	 * @param e the array access expressions.
	 *
	 * @pre e != null
	 */
	public void caseArrayRef(final ArrayRef e) {
		process(e.getBaseBox());

		final IFGNode _baseNode = (IFGNode) getResult();
		final IFGNode _ast = method.getASTNode(e);
		MethodVariant.setOutFilterOfBasedOn(_ast, e.getType(), tokenMgr);

		final ITokenProcessingWork _work =
			new ArrayAccessExprWork(method, context, _ast, connector, tokenMgr.getNewTokenSet());
		final FGAccessNode _temp = new FGAccessNode(_work, fa, tokenMgr);
		_baseNode.addSucc(_temp);
		process(e.getIndexBox());
		setResult(_ast);
	}

	/**
	 * Processes the cast expression. Current implementation processes the expression being cast.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseCastExpr(final CastExpr e) {
		process(e.getOpBox());

		if (Util.isReferenceType(e.getCastType())) {
			// NOTE: We need to filter expressions based on the cast type as casts result in type-conformant values at 
			// run-time.
			final IFGNode _base = (IFGNode) getResult();
			final IFGNode _cast = method.getASTNode(e);
			MethodVariant.setOutFilterOfBasedOn(_cast, e.getType(), tokenMgr);
			_base.addSucc(_cast);
			setResult(_cast);
		}
	}

	/**
	 * Processes the given exception reference expression.  This is required to thread the flow of exception in the system.
	 *
	 * @param e is the caught exception reference.
	 *
	 * @pre e != null
	 */
	public void caseCaughtExceptionRef(final CaughtExceptionRef e) {
		final IFGNode _node = method.getASTNode(e);
		MethodVariant.setOutFilterOfBasedOn(_node, e.getType(), tokenMgr);
		setResult(_node);
	}

	/**
	 * Processes the field expression in a fashion similar to array access expressions.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseInstanceFieldRef(final InstanceFieldRef e) {
		process(e.getBaseBox());

		final IFGNode _baseNode = (IFGNode) getResult();
		final IFGNode _ast = method.getASTNode(e);
		MethodVariant.setOutFilterOfBasedOn(_ast, e.getType(), tokenMgr);

		final ITokenProcessingWork _work =
			new FieldAccessExprWork(method, context, _ast, connector, tokenMgr.getNewTokenSet());
		final FGAccessNode _temp = new FGAccessNode(_work, fa, tokenMgr);
		_baseNode.addSucc(_temp);
		setResult(_ast);
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseInstanceOfExpr(final InstanceOfExpr e) {
		process(e.getOpBox());
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr e) {
		processInstanceInvokeExpr(e);
	}

	/**
	 * Processes the local expression.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre != null
	 */
	public void caseLocal(final Local e) {
		final IFGNode _node = method.getASTNode(e);
		MethodVariant.setOutFilterOfBasedOn(_node, e.getType(), tokenMgr);
		setResult(_node);
	}

	/**
	 * Processes the new array expression.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseNewArrayExpr(final NewArrayExpr e) {
		process(e.getSizeBox());

		Object _temp = null;

		final boolean _flag = context instanceof AllocationContext;

		if (_flag) {
			_temp = ((AllocationContext) context).setAllocationSite(e);
		}

		final IFGNode _ast = method.getASTNode(e);
		MethodVariant.setOutFilterOfBasedOn(_ast, e.getType(), tokenMgr);
		fa.getArrayVariant((ArrayType) e.getType(), context);
		_ast.injectValue(e);
		setResult(_ast);

		if (_flag) {
			((AllocationContext) context).setAllocationSite(_temp);
		}
	}

	/**
	 * Processes the new expression.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseNewExpr(final NewExpr e) {
		final IFGNode _ast = method.getASTNode(e);
		_ast.injectValue(e);
		setResult(_ast);
	}

	/**
	 * Processes the new array expression.  This injects values into the flow graph for each dimension for which the size is
	 * specified.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseNewMultiArrayExpr(final NewMultiArrayExpr e) {
		final ArrayType _arrayType = e.getBaseType();
		final Type _baseType = _arrayType.baseType;

		Object _temp = null;

		final boolean _flag = context instanceof AllocationContext;

		if (_flag) {
			_temp = ((AllocationContext) context).setAllocationSite(e);
		}

		for (int _i = _arrayType.numDimensions, _sizes = e.getSizeCount(); _i > 0 && _sizes > 0; _i--, _sizes--) {
			final ArrayType _aType = ArrayType.v(_baseType, _i);
			final ValuedVariant _array = fa.getArrayVariant(_aType, context);
			process(e.getSizeBox(_sizes - 1));
			_array.getFGNode().injectValue(e);
		}

		if (_flag) {
			((AllocationContext) context).setAllocationSite(_temp);
		}

		final IFGNode _ast = method.getASTNode(e);
		_ast.injectValue(e);
		setResult(_ast);
	}

	/**
	 * Processes <code>null</code>.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseNullConstant(final NullConstant e) {
		final IFGNode _ast = method.getASTNode(e);
		_ast.injectValue(e);
		setResult(_ast);
	}

	/**
	 * Processes parameter reference expressions.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseParameterRef(final ParameterRef e) {
		setResult(method.queryParameterNode(e.getIndex()));
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseSpecialInvokeExpr(final SpecialInvokeExpr e) {
		final SootMethod _callee = e.getMethod();

		if (_callee.getName().equals("<init>")) {
			processInvokedMethod(e);
		} else {
			processInstanceInvokeExpr(e);
		}
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseStaticFieldRef(final StaticFieldRef e) {
		final SootField _field = e.getField();
		final IFGNode _ast = method.getASTNode(e);
		MethodVariant.setOutFilterOfBasedOn(_ast, e.getType(), tokenMgr);

		final IFGNode _nonast = fa.getFieldVariant(_field).getFGNode();
		connector.connect(_ast, _nonast);
		setResult(_ast);
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseStaticInvokeExpr(final StaticInvokeExpr e) {
		processInvokedMethod(e);
	}

	/**
	 * Processes a string constant.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseStringConstant(final StringConstant e) {
		final IFGNode _ast = method.getASTNode(e);
		_ast.injectValue(e);
		setResult(_ast);
	}

	/**
	 * Processes the <code>this</code> variable.  Current implementation returns the node associated with the enclosing
	 * method.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseThisRef(final ThisRef e) {
		setResult(method.queryThisNode());
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseVirtualInvokeExpr(final VirtualInvokeExpr e) {
		processInstanceInvokeExpr(e);
	}

	/**
	 * Processes cases which are not dealt by this visitor methods or delegates to suitable methods depending on the type.
	 *
	 * @param o the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void defaultCase(final Object o) {
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
	 * Processes the invoke expressions that require resolution by creating nodes to various data components present at the
	 * call-site and making them available to be connected when new method implementations are plugged in.
	 *
	 * @param e the invoke expression to be processed.
	 *
	 * @pre e != null
	 */
	protected void processInstanceInvokeExpr(final InstanceInvokeExpr e) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + e);
		}
		fa.processClass(e.getMethod().getDeclaringClass());
		process(e.getBaseBox());

		final IFGNode _receiverNode = (IFGNode) getResult();
		MethodVariant.setOutFilterOfBasedOn(_receiverNode, e.getBase().getType(), tokenMgr);

		for (int _i = 0; _i < e.getArgCount(); _i++) {
			process(e.getArgBox(_i));
		}

		final InvocationVariant _iv = (InvocationVariant) method.getASTVariant(e, context);
		final IFGNode _ast = _iv.getFGNode();
		final IType _baseType =
			tokenMgr.getTypeManager().getTokenTypeForRepType(fa.getClass("java.lang.Throwable").getType());
		final ITokenFilter _baseFilter = tokenMgr.getTypeBasedFilter(_baseType);
		_ast.setOutFilter(_baseFilter);

		if (Util.isReferenceType(e.getMethod().getReturnType())) {
			setResult(_ast);
		} else {
			setResult(null);
		}

		final IType _tokenTypeForRepType = tokenMgr.getTypeManager().getTokenTypeForRepType(e.getBase().getType());
		final ITokenFilter _typeBasedFilter = tokenMgr.getTypeBasedFilter(_tokenTypeForRepType);
		final ITokenProcessingWork _work = new InvokeExprWork(method, context, tokenMgr.getNewTokenSet());
		final FGAccessNode _baseNode = new FGAccessNode(_work, fa, tokenMgr);
		_baseNode.setInFilter(_typeBasedFilter);
		_receiverNode.addSucc(_baseNode);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + e);
		}
	}

	/**
	 * Processes the invoke expressions that do not require resolution by creating nodes to various data components present
	 * at the call-site and making them available to be connected when new method implementations are plugged in.
	 *
	 * @param e the invoke expression to be processed.
	 *
	 * @pre e != null
	 */
	private void processInvokedMethod(final InvokeExpr e) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing " + e);
		}

		final IMethodVariant _callee = fa.getMethodVariant(e.getMethod(), context);

		if (e instanceof SpecialInvokeExpr) {
			final SpecialInvokeExpr _expr = (SpecialInvokeExpr) e;
			final IFGNode _thisNode = _callee.queryThisNode();
			process(_expr.getBaseBox());

			final IFGNode _thisArgNode = (IFGNode) getResult();
			MethodVariant.setOutFilterOfBasedOn(_thisArgNode, _expr.getBase().getType(), tokenMgr);

			_thisArgNode.addSucc(_thisNode);
		}

		for (int _i = 0; _i < e.getArgCount(); _i++) {
			if (Util.isReferenceType(e.getArg(_i).getType())) {
				process(e.getArgBox(_i));

				final IFGNode _argNode = (IFGNode) getResult();
				MethodVariant.setOutFilterOfBasedOn(_argNode, e.getArg(_i).getType(), tokenMgr);

				_argNode.addSucc(_callee.queryParameterNode(_i));
			}
		}

		final InvocationVariant _iv = (InvocationVariant) method.getASTVariant(e, context);
        final IFGNode _throwNode = _iv.getThrowNode();		 
		final IType _baseType =
			tokenMgr.getTypeManager().getTokenTypeForRepType(fa.getClass("java.lang.Throwable").getType());
		final ITokenFilter _baseFilter = tokenMgr.getTypeBasedFilter(_baseType);
		_throwNode.setOutFilter(_baseFilter);
        _callee.queryThrownNode().addSucc(_throwNode);

		if (Util.isReferenceType(e.getMethod().getReturnType())) {
            final IFGNode _ast = _iv.getFGNode();
			MethodVariant.setOutFilterOfBasedOn(_ast, e.getType(), tokenMgr);
			_callee.queryReturnNode().addSucc(_ast);
			setResult(_ast);
		} else {
			setResult(null);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + e);
		}
	}
}

// End of File
