package edu.ksu.cis.bandera.bfa.analysis.ofa.fi;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.BaseType;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.BinopExpr;
import ca.mcgill.sable.soot.jimple.CastExpr;
import ca.mcgill.sable.soot.jimple.FieldRef;
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
import ca.mcgill.sable.soot.jimple.ValueBox;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;
import edu.ksu.cis.bandera.bfa.AbstractExprSwitch;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.AbstractStmtSwitch;
import edu.ksu.cis.bandera.bfa.AbstractValuedVariant;
import edu.ksu.cis.bandera.bfa.AbstractWork;
import edu.ksu.cis.bandera.bfa.ArrayVariant;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import edu.ksu.cis.bandera.bfa.FieldVariant;
import edu.ksu.cis.bandera.bfa.MethodVariant;
import edu.ksu.cis.bandera.bfa.analysis.ofa.ArrayAccessExprWork;
import edu.ksu.cis.bandera.bfa.analysis.ofa.FGAccessNode;
import edu.ksu.cis.bandera.bfa.analysis.ofa.FieldAccessExprWork;
import edu.ksu.cis.bandera.bfa.analysis.ofa.InvokeExprWork;
import edu.ksu.cis.bandera.jext.ChooseExpr;
import edu.ksu.cis.bandera.jext.ComplementExpr;
import edu.ksu.cis.bandera.jext.InExpr;
import edu.ksu.cis.bandera.jext.LocalExpr;
import edu.ksu.cis.bandera.jext.LogicalAndExpr;
import edu.ksu.cis.bandera.jext.LogicalOrExpr;
import java.util.Collection;
import java.util.Iterator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * ExprSwitch.java
 *
 *
 * Created: Sun Jan 27 14:29:14 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ExprSwitch extends AbstractExprSwitch {

	private static final Logger logger = LogManager.getLogger(ExprSwitch.class);

	public ExprSwitch (AbstractStmtSwitch stmt, FGNodeConnector connector){
		super(stmt, connector);
	}

	public void caseArrayRef(ArrayRef e) {
        process(e.getBaseBox());
		logger.debug(e.getBaseBox());
		FGNode baseNode = (FGNode)getResult();
		FGNode ast = method.getASTNode(e);
		AbstractWork work = new ArrayAccessExprWork(method, e, context, ast, connector);
		FGAccessNode temp = new FGAccessNode(work, getWorkList());
		baseNode.addSucc(temp);
		work.setFGNode(temp);
		logger.debug("Temp node  " + temp);

		process(e.getIndexBox());
		setResult(ast);
	}

	public void caseCastExpr(CastExpr e) {
		process(e.getOpBox());
	}

	public void caseChooseExpr(ChooseExpr e) {
		logger.error("What are the choices?  Are they jimple Values or what?");
	}

	public void caseComplementExpr(ComplementExpr e) {
		process(e.getOpBox());
	}

	public void caseInExpr(InExpr e) {
		process(e.getOp1Box());
		process(e.getOp2Box());
	}

	public void caseInstanceFieldRef(InstanceFieldRef e) {
		process(e.getBaseBox());
		FGNode baseNode = (FGNode)getResult();
		FGNode ast = method.getASTNode(e);
		AbstractWork work = new FieldAccessExprWork(method, e, context, ast, connector);
		FGAccessNode temp = new FGAccessNode(work, getWorkList());
		baseNode.addSucc(temp);
		work.setFGNode(temp);
		setResult(ast);
	}

	public void caseInstanceOfExpr(InstanceOfExpr e) {
		process(e.getOpBox());
	}

	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr e) {
		processNonStaticInvokeExpr(e);
	}

	public void caseLocal(Local e) {
		setResult(method.getASTNode(e));
		logger.debug("Local " + e + " - " + getResult() + "\n" + context);
	}

	public void caseLocalExpr(LocalExpr e) {
		e.getLocal().apply(this);
	}

	public void caseLogicalAndExpr(LogicalAndExpr e) {
		process(e.getOp1Box());
		process(e.getOp2Box());
	}

	public void caseLogicalOrExpr(LogicalOrExpr e) {
		process(e.getOp1Box());
		process(e.getOp2Box());
	}

	public void caseNewExpr(NewExpr e) {
		FGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	public void caseNewArrayExpr(NewArrayExpr e ) {
        process(e.getSizeBox());
		FGNode ast = method.getASTNode(e);
		bfa.getArrayVariant((ArrayType)e.getType(), context);
		ast.addValue(e);
		setResult(ast);
	}

	public void caseNewMultiArrayExpr(NewMultiArrayExpr e) {
		ArrayType arrayType = e.getBaseType();
		BaseType baseType = arrayType.baseType;
		int sizes = e.getSizeCount();

		for (int i = arrayType.numDimensions; i > 0; i--, sizes--) {
			arrayType = arrayType.v(baseType, i);
			ArrayVariant array = bfa.getArrayVariant(arrayType, context);
			if (sizes > 0) {
				array.getFGNode().addValue(e);
			} // end of if (sizes > 0)
		} // end of for (int i = 0; i < e.getSizeCount(); i++)

		FGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	public void caseNullConstant(NullConstant e) {
		FGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	public void caseParameterRef(ParameterRef e) {
		setResult(method.getParameterNode(e.getIndex()));
	}

	public void caseSpecialInvokeExpr(SpecialInvokeExpr e) {
		processNonStaticInvokeExpr(e);
	}

	public void caseStaticFieldRef(StaticFieldRef e) {
		SootField field = e.getField();
		FGNode ast = method.getASTNode(e);
		FGNode nonast =  bfa.getFieldVariant(field).getFGNode();
		connector.connect(ast, nonast);
		setResult(ast);
	}

	public void caseStaticInvokeExpr(StaticInvokeExpr e) {
		MethodVariant callee = bfa.getMethodVariant(e.getMethod(), context);
		FGNode argNode;

		for (int i = 0; i < e.getArgCount(); i++) {
			process(e.getArgBox(i));
			argNode = (FGNode)getResult();
			argNode.addSucc(callee.getParameterNode(i));
		}

		if (isNonVoid(e.getMethod())) {
			FGNode ast = method.getASTNode(e);
			callee.getReturnNode().addSucc(ast);
			setResult(ast);
		} else {
			setResult(null);
		} // end of else
	}

	public void caseStringConstant(StringConstant e) {
		FGNode ast = method.getASTNode(e);
		ast.addValue(e);
		setResult(ast);
	}

	public void caseThisRef(ThisRef e) {
		setResult(method.getThisNode());
	}

	public void caseVirtualInvokeExpr(VirtualInvokeExpr e) {
		processNonStaticInvokeExpr(e);
	}

	public void defaultCase(Object o) {
		Value v = (Value)o;
		if (v instanceof BinopExpr) {
			BinopExpr temp = (BinopExpr)v;
			process(temp.getOp1Box());
			process(temp.getOp2Box());
		} // end of if (o instanceof BinOpExpr)
		else if (v instanceof UnopExpr) {
			UnopExpr temp = (UnopExpr)v;
			process(temp.getOpBox());
		} // end of if (o instanceof UnopExpr)
		else {
			super.defaultCase(o);
		} // end of else
	}

	protected void processNonStaticInvokeExpr(NonStaticInvokeExpr e) {
		process(e.getBaseBox());
		FGNode temp = (FGNode)getResult();

		AbstractWork work = new InvokeExprWork(method, e, (Context)context.clone(), this);
		FGAccessNode baseNode = new FGAccessNode(work, getWorkList());
		work.setFGNode(baseNode);
		temp.addSucc(baseNode);

		for (int i = 0; i < e.getArgCount(); i++) {
			process(e.getArgBox(i));
		} // end of for (int i = 0; i < e.getArgCount(); i++)

		if (isNonVoid(e.getMethod())) {
			setResult(method.getASTNode(e));
		} else {
			setResult(null);
		} // end of else
	}

	public Object prototype(Object o) {
		return new ExprSwitch((StmtSwitch)o, connector);
	}

}// ExprSwitch
