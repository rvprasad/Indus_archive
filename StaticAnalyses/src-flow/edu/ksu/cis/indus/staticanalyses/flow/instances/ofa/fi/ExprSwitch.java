
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.fi;

import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.indus.staticanalyses.flow.ArrayVariant;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.TypeBasedFilter;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.ArrayAccessExprWork;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.FGAccessNode;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.FieldAccessExprWork;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.InvokeExprWork;

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
 * The expression visitor used in flow insensitive mode of object flow analysis.  Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ExprSwitch
  extends AbstractExprSwitch {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ExprSwitch.class);

	/**
	 * Creates a new <code>ExprSwitch</code> instance.
	 *
	 * @param stmtSwitchParam the statement visitor which uses this object.
	 * @param nodeConnector the connector to be used to connect ast and non-ast flow graph node.
	 */
	public ExprSwitch(final AbstractStmtSwitch stmtSwitchParam, final IFGNodeConnector nodeConnector) {
		super(stmtSwitchParam, nodeConnector);
	}

	/**
	 * Returns a new instance of the this class.
	 *
	 * @param o the statement visitor which uses the new instance.
	 *
	 * @return the new instance of this class.
	 *
	 * @pre o != null and o.oclIsKindOf(StmtSwitch)
	 * @post result != null and result.oclIsKindOf(ExprSwitch)
	 */
	public Object getClone(final Object o) {
		return new ExprSwitch((StmtSwitch) o, connector);
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

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(e.getBaseBox());
		}

		IFGNode baseNode = (IFGNode) getResult();
		IFGNode ast = method.getASTNode(e);
		AbstractWork work = new ArrayAccessExprWork(method, context, ast, connector);
		FGAccessNode temp = new FGAccessNode(work, getWorkList());
		baseNode.addSucc(temp);
		work.setFGNode(temp);
		process(e.getIndexBox());
		setResult(ast);
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
			IFGNode base = (IFGNode) getResult();
			IFGNode cast = method.getASTNode(e);
			cast.setFilter(new TypeBasedFilter(e.getCastType(), fa));
			base.addSucc(cast);
			setResult(cast);
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
		IFGNode node = method.getASTNode(e);
		setResult(node);
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

		IFGNode baseNode = (IFGNode) getResult();
		IFGNode ast = method.getASTNode(e);
		AbstractWork work = new FieldAccessExprWork(method, context, ast, connector);
		FGAccessNode temp = new FGAccessNode(work, getWorkList());
		baseNode.addSucc(temp);
		work.setFGNode(temp);
		setResult(ast);
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
		IFGNode node = method.getASTNode(e);
		setResult(node);
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

		IFGNode ast = method.getASTNode(e);
		fa.getArrayVariant((ArrayType) e.getType(), context);
		ast.addValue(e);
		setResult(ast);
	}

	/**
	 * Processes the new expression.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseNewExpr(final NewExpr e) {
		IFGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
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
		ArrayType arrayType = e.getBaseType();
		Type baseType = arrayType.baseType;
		int sizes = e.getSizeCount();

		for (int i = arrayType.numDimensions; i > 0; i--, sizes--) {
			arrayType = ArrayType.v(baseType, i);

			ArrayVariant array = fa.getArrayVariant(arrayType, context);

			if (sizes > 0) {
				array.getFGNode().addValue(e);
			}
		}

		IFGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	/**
	 * Processes <code>null</code>.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 *
	 * @pre e != null
	 */
	public void caseNullConstant(final NullConstant e) {
		IFGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
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
		final SootMethod callee = e.getMethod();

		if (callee.getName().equals("<init>")) {
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
		SootField field = e.getField();

		IFGNode ast = method.getASTNode(e);
		IFGNode nonast = fa.getFieldVariant(field).getFGNode();
		connector.connect(ast, nonast);
		setResult(ast);
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
		IFGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
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
		Value v = (Value) o;

		if (v instanceof BinopExpr) {
			BinopExpr temp = (BinopExpr) v;
			process(temp.getOp1Box());
			process(temp.getOp2Box());
		} else if (v instanceof UnopExpr) {
			UnopExpr temp = (UnopExpr) v;
			process(temp.getOpBox());
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

		IFGNode temp = (IFGNode) getResult();

		for (int i = 0; i < e.getArgCount(); i++) {
			process(e.getArgBox(i));
		}

		if (Util.isReferenceType(e.getMethod().getReturnType())) {
			setResult(method.getASTNode(e));
		} else {
			setResult(null);
		}

		AbstractWork work = new InvokeExprWork(method, context, this);
		FGAccessNode baseNode = new FGAccessNode(work, getWorkList());
		work.setFGNode(baseNode);
		temp.addSucc(baseNode);

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

		MethodVariant callee = fa.getMethodVariant(e.getMethod(), context);

		if (e instanceof SpecialInvokeExpr) {
			SpecialInvokeExpr expr = (SpecialInvokeExpr) e;
			IFGNode thisNode = callee.queryThisNode();
			process(expr.getBaseBox());

			IFGNode thisArgNode = (IFGNode) getResult();
			thisArgNode.addSucc(thisNode);
		}

		for (int i = 0; i < e.getArgCount(); i++) {
			if (Util.isReferenceType(e.getArg(i).getType())) {
				process(e.getArgBox(i));

				IFGNode argNode = (IFGNode) getResult();
				argNode.addSucc(callee.queryParameterNode(i));
			}
		}

		if (Util.isReferenceType(e.getMethod().getReturnType())) {
			IFGNode ast = method.getASTNode(e);
			callee.queryReturnNode().addSucc(ast);
			setResult(ast);
		} else {
			setResult(null);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: processed " + e);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.13  2003/12/16 00:19:25  venku
   - specialinvoke was handled incorrectly.  FIXED
     It behaves like virtual in cases when a non-instance
     initialization method is invoked.  Otherwise, it acts
     like static invocation. We deal with the first case
     by treating it as virtual invocation and the second
     case as static invoke expr but only with a primary.

   Revision 1.12  2003/12/07 08:40:29  venku
   - declared class was not being processed in case of
     virtual invoke.  FIXED.
   Revision 1.11  2003/12/07 03:23:21  venku
   - interfaces and classes involved in interface/special invokes
     are not being processed.  FIXED.
   Revision 1.10  2003/12/05 02:27:20  venku
   - unnecessary methods and fields were removed. Like
       getCurrentProgramPoint()
       getCurrentStmt()
   - context holds current information and only it must be used
     to retrieve this information.  No auxiliary arguments. FIXED.
   Revision 1.9  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.8  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.7  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/08/26 17:55:45  venku
   Well, we used typing info for triggering static field expression.  However,
   this was incorrect as the flow to the primary is cut off.  FIXED.
   Revision 1.5  2003/08/26 17:53:55  venku
   Actually we can use the types to cut down the number of edges
   between the flow nodes. The current fix uses a method in OFAnalyzer
   to check for reference types, only if the type matches the given expression
   is processed.  However, this does not apply for staticfield, instancefield, and
   array access expressions.
   Revision 1.4  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.3  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/15 02:54:06  venku
   Spruced up specification and documentation for flow-insensitive classes.
   Changed names in AbstractExprSwitch.
   Ripple effect of above change.
   Formatting changes to IPrototype.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
 */
