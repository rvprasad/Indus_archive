
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

package edu.ksu.cis.bandera.slicer;

import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.AbstractJimpleValueSwitch;
import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.BreakpointStmt;
import ca.mcgill.sable.soot.jimple.CastExpr;
import ca.mcgill.sable.soot.jimple.CaughtExceptionRef;
import ca.mcgill.sable.soot.jimple.DoubleConstant;
import ca.mcgill.sable.soot.jimple.EnterMonitorStmt;
import ca.mcgill.sable.soot.jimple.ExitMonitorStmt;
import ca.mcgill.sable.soot.jimple.FloatConstant;
import ca.mcgill.sable.soot.jimple.GotoStmt;
import ca.mcgill.sable.soot.jimple.IdentityStmt;
import ca.mcgill.sable.soot.jimple.IfStmt;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.InstanceOfExpr;
import ca.mcgill.sable.soot.jimple.IntConstant;
import ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.LengthExpr;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.LongConstant;
import ca.mcgill.sable.soot.jimple.LookupSwitchStmt;
import ca.mcgill.sable.soot.jimple.NewArrayExpr;
import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.NewInvokeExpr;
import ca.mcgill.sable.soot.jimple.NewMultiArrayExpr;
import ca.mcgill.sable.soot.jimple.NopStmt;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.ParameterRef;
import ca.mcgill.sable.soot.jimple.RetStmt;
import ca.mcgill.sable.soot.jimple.ReturnStmt;
import ca.mcgill.sable.soot.jimple.ReturnVoidStmt;
import ca.mcgill.sable.soot.jimple.SpecialInvokeExpr;
import ca.mcgill.sable.soot.jimple.StaticFieldRef;
import ca.mcgill.sable.soot.jimple.StaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtSwitch;
import ca.mcgill.sable.soot.jimple.StringConstant;
import ca.mcgill.sable.soot.jimple.TableSwitchStmt;
import ca.mcgill.sable.soot.jimple.ThisRef;
import ca.mcgill.sable.soot.jimple.ThrowStmt;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;


/**
 * <p>
 * This class "clones" a given Soot Jimple AST chunk (statement and expression).  Our interpretation of clone here is an
 * semantically identical copy of the given AST chunk.  This is used during construction of the new body after slicing.  The
 * methods of this class walkover the given AST using <i>visitor pattern</i> to cloning parts of the given AST chunk.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ASTCloner
  extends AbstractJimpleValueSwitch
  implements StmtSwitch {
	/**
	 * <p>
	 * The static reference to the singleton object used to create Jimple AST chunks.
	 * </p>
	 */
	private static final Jimple jimple = Jimple.v();

	/**
	 * <p>
	 * This provides information about parts of the clone(d) system that is required during cloning chunks involving fields
	 * and such.
	 * </p>
	 */
	private final ASTClonerHelper helper;

	/**
	 * <p>
	 * The method in which the cloned statement occurs.
	 * </p>
	 */
	private SootMethod method;

	/**
	 * <p>
	 * The clone statement.
	 * </p>
	 */
	private Stmt resStmt;

	/**
	 * <p>
	 * The cloned statement.
	 * </p>
	 */
	private Stmt stmt;

	/**
	 * <p>
	 * The clone value/expression.
	 * </p>
	 */
	private Value resValue;

	/**
	 * <p>
	 * The cloned value/expression.
	 * </p>
	 */
	private Value value;

	/**
	 * <p>
	 * Creates a new ASTCloner object.
	 * </p>
	 *
	 * @param helper is the instance that provides information about parts of the system such as fields during cloning.
	 */
	public ASTCloner(ASTClonerHelper helper) {
		this.helper = helper;
	}

	/**
	 * <p>
	 * This interface can be used to obtain information about parts (class, methods, and fields) of the original system being
	 * cloned.
	 * </p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public interface ASTClonerHelper {
		/**
		 * <p>
		 * Returns the clone of the given method, if it exists.  Otherwise, <code>null</code> is returned.
		 * </p>
		 *
		 * @param method in the cloned system.
		 *
		 * @return the clone of <code>method</code>.
		 */
		public SootMethod getCloneOf(SootMethod method);

		/**
		 * <p>
		 * Returns the clone of the given field, if it exists.  Otherwise, <code>null</code> is returned.
		 * </p>
		 *
		 * @param field in the cloned system.
		 *
		 * @return the clone of <code>field</code>.
		 */
		public SootField getCloneOf(SootField field);

		/**
		 * <p>
		 * Returns the clone of the given class, if it exists.  Otherwise, <code>null</code> is returned.
		 * </p>
		 *
		 * @param clazz is the class in the cloned system.
		 *
		 * @return the clone of <code>clazz</code>.
		 */
		public SootClass getCloneOf(SootClass clazz);

		/**
		 * <p>
		 * Returns the clone of the given class, if it exists.  Otherwise, <code>null</code> is returned.
		 * </p>
		 *
		 * @param clazz is the fully qualified name of the class in the cloned system.
		 *
		 * @return the clone of a class named <code>clazz</code>.
		 */
		public SootClass getCloneOfSootClass(String clazz);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseArrayRef(ca.mcgill.sable.soot.jimple.ArrayRef)
	 */
	public void caseArrayRef(ArrayRef v) {
		v.getBase().apply(this);

		Value base = resValue;
		v.getIndex().apply(this);
		resValue = jimple.newArrayRef(base, resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseAssignStmt(ca.mcgill.sable.soot.jimple.AssignStmt)
	 */
	public void caseAssignStmt(AssignStmt stmt) {
		stmt.getLeftOp().apply(this);

		Value left = resValue;
		stmt.getRightOp().apply(this);

		Value right = resValue;
		resStmt = jimple.newAssignStmt(left, right);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseBreakpointStmt(ca.mcgill.sable.soot.jimple.BreakpointStmt)
	 */
	public void caseBreakpointStmt(BreakpointStmt stmt) {
		resStmt = jimple.newBreakpointStmt();
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseCastExpr(ca.mcgill.sable.soot.jimple.CastExpr)
	 */
	public void caseCastExpr(CastExpr v) {
		v.getOp().apply(this);
		resValue = jimple.newCastExpr(resValue, v.getType());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseCaughtExceptionRef(ca.mcgill.sable.soot.jimple.CaughtExceptionRef)
	 */
	public void caseCaughtExceptionRef(CaughtExceptionRef v) {
		resValue = jimple.newCaughtExceptionRef((JimpleBody) method.getBody(jimple));
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ConstantSwitch#caseDoubleConstant(ca.mcgill.sable.soot.jimple.DoubleConstant)
	 */
	public void caseDoubleConstant(DoubleConstant v) {
		resValue = DoubleConstant.v(v.value);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseEnterMonitorStmt(ca.mcgill.sable.soot.jimple.EnterMonitorStmt)
	 */
	public void caseEnterMonitorStmt(EnterMonitorStmt stmt) {
		stmt.getOp().apply(this);
		resStmt = jimple.newEnterMonitorStmt(resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseExitMonitorStmt(ca.mcgill.sable.soot.jimple.ExitMonitorStmt)
	 */
	public void caseExitMonitorStmt(ExitMonitorStmt stmt) {
		stmt.getOp().apply(this);
		resStmt = jimple.newExitMonitorStmt(resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ConstantSwitch#caseFloatConstant(ca.mcgill.sable.soot.jimple.FloatConstant)
	 */
	public void caseFloatConstant(FloatConstant v) {
		resValue = FloatConstant.v(v.value);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseGotoStmt(ca.mcgill.sable.soot.jimple.GotoStmt)
	 */
	public void caseGotoStmt(GotoStmt stmt) {
		resStmt = jimple.newGotoStmt(stmt.getTarget());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseIdentityStmt(ca.mcgill.sable.soot.jimple.IdentityStmt)
	 */
	public void caseIdentityStmt(IdentityStmt stmt) {
		stmt.getLeftOp().apply(this);

		Value left = resValue;
		stmt.getRightOp().apply(this);

		Value right = resValue;
		resStmt = jimple.newIdentityStmt(left, right);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseIfStmt(ca.mcgill.sable.soot.jimple.IfStmt)
	 */
	public void caseIfStmt(IfStmt stmt) {
		stmt.getCondition().apply(this);
		resStmt = jimple.newIfStmt(resValue, stmt.getTarget());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseInstanceFieldRef(ca.mcgill.sable.soot.jimple.InstanceFieldRef)
	 */
	public void caseInstanceFieldRef(InstanceFieldRef v) {
		v.getBase().apply(this);
		resValue = jimple.newInstanceFieldRef(resValue, v.getField());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseInstanceOfExpr(ca.mcgill.sable.soot.jimple.InstanceOfExpr)
	 */
	public void caseInstanceOfExpr(InstanceOfExpr v) {
		v.getOp().apply(this);
		resValue = jimple.newInstanceOfExpr(resValue, v.getCheckType());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ConstantSwitch#caseIntConstant(ca.mcgill.sable.soot.jimple.IntConstant)
	 */
	public void caseIntConstant(IntConstant v) {
		resValue = IntConstant.v(v.value);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr)
	 */
	public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		v.getBase().apply(this);

		Local base = (Local) resValue;
		resValue = jimple.newInterfaceInvokeExpr(base, helper.getCloneOf(v.getMethod()), getArgs(v));
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseInvokeStmt(ca.mcgill.sable.soot.jimple.InvokeStmt)
	 */
	public void caseInvokeStmt(InvokeStmt stmt) {
		stmt.getInvokeExpr().apply(this);
		resStmt = jimple.newInvokeStmt(resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseLengthExpr(ca.mcgill.sable.soot.jimple.LengthExpr)
	 */
	public void caseLengthExpr(LengthExpr v) {
		v.getOp().apply(this);
		resValue = jimple.newLengthExpr(resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.JimpleValueSwitch#caseLocal(ca.mcgill.sable.soot.jimple.Local)
	 */
	public void caseLocal(Local l) {
		resValue = jimple.newLocal(l.getName(), l.getType());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ConstantSwitch#caseLongConstant(ca.mcgill.sable.soot.jimple.LongConstant)
	 */
	public void caseLongConstant(LongConstant v) {
		resValue = LongConstant.v(v.value);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseLookupSwitchStmt(ca.mcgill.sable.soot.jimple.LookupSwitchStmt)
	 */
	public void caseLookupSwitchStmt(LookupSwitchStmt stmt) {
		stmt.getKey().apply(this);
		resStmt = jimple.newLookupSwitchStmt(resValue, stmt.getLookupValues(), stmt.getTargets(), stmt.getDefaultTarget());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseNewArrayExpr(ca.mcgill.sable.soot.jimple.NewArrayExpr)
	 */
	public void caseNewArrayExpr(NewArrayExpr v) {
		v.getSize().apply(this);
		resValue = jimple.newNewArrayExpr(v.getBaseType(), resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseNewExpr(ca.mcgill.sable.soot.jimple.NewExpr)
	 */
	public void caseNewExpr(NewExpr v) {
		resValue = jimple.newNewExpr(v.getBaseType());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseNewInvokeExpr(ca.mcgill.sable.soot.jimple.NewInvokeExpr)
	 */
	public void caseNewInvokeExpr(NewInvokeExpr v) {
		throw new IllegalArgumentException("Control should never reach here.  "
			+ "An instance of NewInvokeExpr cannot be created in Jimple.");
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseNewMultiArrayExpr(ca.mcgill.sable.soot.jimple.NewMultiArrayExpr)
	 */
	public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		ca.mcgill.sable.util.List sizes = new ca.mcgill.sable.util.ArrayList();

		for(int i = sizes.size() - 1; i >= 0; i--) {
			v.getSize(i).apply(this);
			sizes.add(i, resValue);
		}
		resValue = jimple.newNewMultiArrayExpr(v.getBaseType(), sizes);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseNopStmt(ca.mcgill.sable.soot.jimple.NopStmt)
	 */
	public void caseNopStmt(NopStmt stmt) {
		resStmt = jimple.newNopStmt();
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ConstantSwitch#caseNullConstant(ca.mcgill.sable.soot.jimple.NullConstant)
	 */
	public void caseNullConstant(NullConstant v) {
		resValue = NullConstant.v();
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseParameterRef(ca.mcgill.sable.soot.jimple.ParameterRef)
	 */
	public void caseParameterRef(ParameterRef v) {
		resValue = jimple.newParameterRef(helper.getCloneOf(v.getMethod()), v.getIndex());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseRetStmt(ca.mcgill.sable.soot.jimple.RetStmt)
	 */
	public void caseRetStmt(RetStmt stmt) {
		stmt.getStmtAddress().apply(this);
		resStmt = jimple.newRetStmt(resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseReturnStmt(ca.mcgill.sable.soot.jimple.ReturnStmt)
	 */
	public void caseReturnStmt(ReturnStmt stmt) {
		stmt.getReturnValue().apply(this);
		resStmt = jimple.newReturnStmt(resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseReturnVoidStmt(ca.mcgill.sable.soot.jimple.ReturnVoidStmt)
	 */
	public void caseReturnVoidStmt(ReturnVoidStmt stmt) {
		resStmt = jimple.newReturnVoidStmt();
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseSpecialInvokeExpr(ca.mcgill.sable.soot.jimple.SpecialInvokeExpr)
	 */
	public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		v.getBase().apply(this);

		Local base = (Local) resValue;
		resValue = jimple.newSpecialInvokeExpr(base, helper.getCloneOf(v.getMethod()), getArgs(v));
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseStaticFieldRef(ca.mcgill.sable.soot.jimple.StaticFieldRef)
	 */
	public void caseStaticFieldRef(StaticFieldRef v) {
		resValue = jimple.newStaticFieldRef(helper.getCloneOf(v.getField()));
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseStaticInvokeExpr(ca.mcgill.sable.soot.jimple.StaticInvokeExpr)
	 */
	public void caseStaticInvokeExpr(StaticInvokeExpr v) {
		resValue = jimple.newStaticInvokeExpr(helper.getCloneOf(v.getMethod()), getArgs(v));
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ConstantSwitch#caseStringConstant(ca.mcgill.sable.soot.jimple.StringConstant)
	 */
	public void caseStringConstant(StringConstant v) {
		resValue = StringConstant.v(v.value);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseTableSwitchStmt(ca.mcgill.sable.soot.jimple.TableSwitchStmt)
	 */
	public void caseTableSwitchStmt(TableSwitchStmt stmt) {
		stmt.getKey().apply(this);
		resStmt =
			jimple.newTableSwitchStmt(resValue, stmt.getLowIndex(), stmt.getHighIndex(), stmt.getTargets(),
				stmt.getDefaultTarget());
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.RefSwitch#caseThisRef(ca.mcgill.sable.soot.jimple.ThisRef)
	 */
	public void caseThisRef(ThisRef v) {
		resValue = jimple.newThisRef(helper.getCloneOfSootClass(((RefType) v.getType()).className));
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.StmtSwitch#caseThrowStmt(ca.mcgill.sable.soot.jimple.ThrowStmt)
	 */
	public void caseThrowStmt(ThrowStmt stmt) {
		stmt.getOp().apply(this);
		resStmt = jimple.newThrowStmt(resValue);
	}

	/**
	 * @see ca.mcgill.sable.soot.jimple.ExprSwitch#caseVirtualInvokeExpr(ca.mcgill.sable.soot.jimple.VirtualInvokeExpr)
	 */
	public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		super.caseVirtualInvokeExpr(v);
	}

	/**
	 * <p>
	 * Clones a given Jimple statement that occurs in the given method.
	 * </p>
	 *
	 * @param stmt is the Jimple statement to be cloned.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return the clone of <code>stmt</code>.
	 */
	public Stmt cloneASTFragment(Stmt stmt, SootMethod method) {
		this.stmt = stmt;
		this.method = method;
		stmt.apply(this);
		return resStmt;
	}

	/**
	 * <p>
	 * Clones a given Jimple value that occurs in the given method.
	 * </p>
	 *
	 * @param value is the Jimple value to be cloned.
	 * @param method in which <code>value</code> occurs.
	 *
	 * @return the clone of <code>value</code>.
	 */
	public Value cloneASTFragment(Value value, SootMethod method) {
		this.value = value;
		this.method = method;
		value.apply(this);
		return resValue;
	}

	/**
	 * <p>
	 * Returns the AST chunks(Jimple <code>Value</code>s) corresponding to the arguments to the given invoke expression.
	 * </p>
	 *
	 * @param v is the method invocation of interest.
	 *
	 * @return a collection of AST chunks.
	 */
	/*!
	 * @post result.size() = v.getArgCount()
	 */
	private ca.mcgill.sable.util.List getArgs(InvokeExpr v) {
		ca.mcgill.sable.util.List result = new ca.mcgill.sable.util.ArrayList();

		for(int i = result.size() - 1; i >= 0; i--) {
			v.getArg(i).apply(this);
			result.add(i, resValue);
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.1.1.1  2003/02/17 23:59:51  venku
Placing JavaSlicer under version control.


*****/
