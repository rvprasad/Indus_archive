
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.fi;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.BaseType;
import ca.mcgill.sable.soot.SootField;

import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.BinopExpr;
import ca.mcgill.sable.soot.jimple.CastExpr;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.InstanceOfExpr;
import ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.NewArrayExpr;
import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.NewMultiArrayExpr;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.ParameterRef;
import ca.mcgill.sable.soot.jimple.SpecialInvokeExpr;
import ca.mcgill.sable.soot.jimple.StaticFieldRef;
import ca.mcgill.sable.soot.jimple.StaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.StringConstant;
import ca.mcgill.sable.soot.jimple.ThisRef;
import ca.mcgill.sable.soot.jimple.UnopExpr;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;

import edu.ksu.cis.bandera.jext.ChooseExpr;
import edu.ksu.cis.bandera.jext.ComplementExpr;
import edu.ksu.cis.bandera.jext.InExpr;
import edu.ksu.cis.bandera.jext.LocalExpr;
import edu.ksu.cis.bandera.jext.LogicalAndExpr;
import edu.ksu.cis.bandera.jext.LogicalOrExpr;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractExprSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractStmtSwitch;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.bandera.staticanalyses.flow.ArrayVariant;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNodeConnector;
import edu.ksu.cis.bandera.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.ArrayAccessExprWork;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.FGAccessNode;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.FieldAccessExprWork;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.InvokeExprWork;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * The expression visitor used in flow insensitive mode of object flow analysis.  Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ExprSwitch
  extends AbstractExprSwitch {
	/**
	 * An instance of <code>Logger</code> used for logging purpose.
	 */
	private static final Logger logger = LogManager.getLogger(ExprSwitch.class);

	/**
	 * Creates a new <code>ExprSwitch</code> instance.
	 *
	 * @param stmt the statement visitor which uses this object.
	 * @param connector the connector to be used to connect ast and non-ast flow graph node.
	 */
	public ExprSwitch(AbstractStmtSwitch stmt, FGNodeConnector connector) {
		super(stmt, connector);
	}

	/**
	 * Processes array access expressions.  Current implementation processes the primary and connects a node associated with
	 * the primary to a <code>FGAccessNode</code> which monitors this access expressions for new values in the primary.
	 *
	 * @param e the array access expressions.
	 */
	public void caseArrayRef(ArrayRef e) {
		process(e.getBaseBox());
		logger.debug(e.getBaseBox());

		FGNode baseNode = (FGNode) getResult();
		FGNode ast = method.getASTNode(e);
		AbstractWork work = new ArrayAccessExprWork(method, getCurrentProgramPoint(), context, ast, connector);
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
	 */
	public void caseCastExpr(CastExpr e) {
		process(e.getOpBox());
	}

	/**
	 * Describe <code>caseChooseExpr</code> method here.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseChooseExpr(ChooseExpr e) {
		logger.error("What are the choices?  Are they jimple Values or what?");
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseComplementExpr(ComplementExpr e) {
		process(e.getOpBox());
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseInExpr(InExpr e) {
		process(e.getOp1Box());
		process(e.getOp2Box());
	}

	/**
	 * Processes the field expression in a fashion similar to array access expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseInstanceFieldRef(InstanceFieldRef e) {
		process(e.getBaseBox());

		FGNode baseNode = (FGNode) getResult();
		FGNode ast = method.getASTNode(e);
		AbstractWork work = new FieldAccessExprWork(method, getCurrentProgramPoint(), context, ast, connector);
		FGAccessNode temp = new FGAccessNode(work, getWorkList());
		baseNode.addSucc(temp);
		work.setFGNode(temp);
		setResult(ast);
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseInstanceOfExpr(InstanceOfExpr e) {
		process(e.getOpBox());
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr e) {
		processNonStaticInvokeExpr(e);
	}

	/**
	 * Processes the local expression.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseLocal(Local e) {
		setResult(method.getASTNode(e));
		logger.debug("Local " + e + " - " + getResult() + "\n" + context);
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseLocalExpr(LocalExpr e) {
		e.getLocal().apply(this);
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseLogicalAndExpr(LogicalAndExpr e) {
		process(e.getOp1Box());
		process(e.getOp2Box());
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseLogicalOrExpr(LogicalOrExpr e) {
		process(e.getOp1Box());
		process(e.getOp2Box());
	}

	/**
	 * Processes the new array expression.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseNewArrayExpr(NewArrayExpr e) {
		process(e.getSizeBox());

		FGNode ast = method.getASTNode(e);
		bfa.getArrayVariant((ArrayType) e.getType(), context);
		ast.addValue(e);
		setResult(ast);
	}

	/**
	 * Processes the new expression.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseNewExpr(NewExpr e) {
		FGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	/**
	 * Processes the new array expression.  This injects values into the flow graph for each dimension for which the size is
	 * specified.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseNewMultiArrayExpr(NewMultiArrayExpr e) {
		ArrayType arrayType = e.getBaseType();
		BaseType baseType = arrayType.baseType;
		int sizes = e.getSizeCount();

		for(int i = arrayType.numDimensions; i > 0; i--, sizes--) {
			arrayType = ArrayType.v(baseType, i);

			ArrayVariant array = bfa.getArrayVariant(arrayType, context);

			if(sizes > 0) {
				array.getFGNode().addValue(e);
			}
		}

		FGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	/**
	 * Processes <code>null</code>.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseNullConstant(NullConstant e) {
		FGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	/**
	 * Processes parameter reference expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseParameterRef(ParameterRef e) {
		setResult(method.queryParameterNode(e.getIndex()));
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseSpecialInvokeExpr(SpecialInvokeExpr e) {
		processNonStaticInvokeExpr(e);
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseStaticFieldRef(StaticFieldRef e) {
		SootField field = e.getField();
		FGNode ast = method.getASTNode(e);
		FGNode nonast = bfa.getFieldVariant(field).getFGNode();
		connector.connect(ast, nonast);
		setResult(ast);
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseStaticInvokeExpr(StaticInvokeExpr e) {
		MethodVariant callee = bfa.getMethodVariant(e.getMethod(), context);
		FGNode argNode;

		for(int i = 0; i < e.getArgCount(); i++) {
			process(e.getArgBox(i));
			argNode = (FGNode) getResult();
			argNode.addSucc(callee.queryParameterNode(i));
		}

		if(isNonVoid(e.getMethod())) {
			FGNode ast = method.getASTNode(e);
			callee.queryReturnNode().addSucc(ast);
			setResult(ast);
		} else {
			setResult(null);
		}
	}

	/**
	 * Processes a string constant.  This injects a value into the flow graph.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseStringConstant(StringConstant e) {
		FGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	/**
	 * Processes the <code>this</code> variable.  Current implementation returns the node associated with the enclosing
	 * method.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseThisRef(ThisRef e) {
		setResult(method.queryThisNode());
	}

	/**
	 * Processes the embedded expressions.
	 *
	 * @param e the expression to be processed.
	 */
	public void caseVirtualInvokeExpr(VirtualInvokeExpr e) {
		processNonStaticInvokeExpr(e);
	}

	/**
	 * Processes cases which are not dealt by this visitor methods or delegates to suitable methods depending on the type.
	 *
	 * @param o the expression to be processed.
	 */
	public void defaultCase(Object o) {
		Value v = (Value) o;

		if(v instanceof BinopExpr) {
			BinopExpr temp = (BinopExpr) v;
			process(temp.getOp1Box());
			process(temp.getOp2Box());
		} else if(v instanceof UnopExpr) {
			UnopExpr temp = (UnopExpr) v;
			process(temp.getOpBox());
		} else {
			super.defaultCase(o);
		}
	}

	/**
	 * Returns a new instance of the this class.
	 *
	 * @param o the statement visitor which uses the new instance.
	 *
	 * @return the new instance of this class.
	 */
	public Object prototype(Object o) {
		return new ExprSwitch((StmtSwitch) o, connector);
	}

	/**
	 * Processes the invoke expressions by creating nodes to various data components present at the call-site and making them
	 * available to be connected when new method implementations are plugged in.
	 *
	 * @param e the invoke expression to be processed.
	 */
	protected void processNonStaticInvokeExpr(NonStaticInvokeExpr e) {
		process(e.getBaseBox());

		FGNode temp = (FGNode) getResult();

		for(int i = 0; i < e.getArgCount(); i++) {
			process(e.getArgBox(i));
		}

		if(isNonVoid(e.getMethod())) {
			setResult(method.getASTNode(e));
		} else {
			setResult(null);
		}

		AbstractWork work = new InvokeExprWork(method, getCurrentProgramPoint(), (Context) context.clone(), this);
		FGAccessNode baseNode = new FGAccessNode(work, getWorkList());
		work.setFGNode(baseNode);
		temp.addSucc(baseNode);
	}
}

/*****
 ChangeLog:

$Log$

*****/
