
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
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
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.transformations.common;

import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.DoubleConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FloatConstant;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StmtSwitch;
import soot.jimple.StringConstant;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class "clones" a given Soot Jimple AST chunk (statement and expression).  Our interpretation of clone here is an
 * semantically identical copy of the given AST chunk.  This is used during construction of the new body after transforming.
 * The methods of this class walkover the given AST using <i>visitor pattern</i> to clone the given AST chunk.  Note that
 * we cannot use <code>clone()</code> on the all of the chunks as we have to get the references to the chunks created in the
 * clone rather than the clonee.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ASTCloner
  extends AbstractJimpleValueSwitch
  implements StmtSwitch {
	/**
	 * The static reference to the singleton object used to create Jimple AST chunks.
	 *
	 * @invariant JIMPLE != null
	 */
	private static final Jimple JIMPLE = Jimple.v();

	/**
	 * This provides information about parts of the clone(d) system that is required during cloning chunks involving fields
	 * and such.
	 *
	 * @invariant helper != null
	 */
	private final IASTClonerHelper helper;

	/**
	 * The method in which the cloned statement occurs.
	 */
	private SootMethod method;

	/**
	 * The clone statement.
	 */
	private Stmt cloneStmt;

	/**
	 * The clone value/expression.
	 */
	private Value cloneValue;

	/**
	 * Creates a new ASTCloner object.
	 *
	 * @param cloningHelper is the instance that provides information about parts of the system such as fields during
	 * 		  cloning.
	 *
	 * @pre cloningHelper != null
	 */
	public ASTCloner(final IASTClonerHelper cloningHelper) {
		this.helper = cloningHelper;
	}

	/**
	 * This interface can be used to obtain information about parts (class, methods, and fields) of the original system being
	 * cloned.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public interface IASTClonerHelper {
		/**
		 * Returns the clone of the given method, if it exists.  Otherwise, <code>null</code> is returned.
		 *
		 * @param method in the cloned system.
		 *
		 * @return the clone of <code>method</code>.
		 */
		SootMethod getCloneOf(SootMethod method);

		/**
		 * Returns the clone of the given field, if it exists.  Otherwise, <code>null</code> is returned.
		 *
		 * @param field in the cloned system.
		 *
		 * @return the clone of <code>field</code>.
		 */
		SootField getCloneOf(SootField field);

		/**
		 * Returns the clone of the given class, if it exists.  Otherwise, <code>null</code> is returned.
		 *
		 * @param clazz is the class in the cloned system.
		 *
		 * @return the clone of <code>clazz</code>.
		 */
		SootClass getCloneOf(SootClass clazz);

		/**
		 * Returns the clone of the given class, if it exists.  Otherwise, <code>null</code> is returned.
		 *
		 * @param clazz is the fully qualified name of the class in the cloned system.
		 *
		 * @return the clone of a class named <code>clazz</code>.
		 */
		SootClass getCloneOf(String clazz);

		/**
		 * Returns the local corresponding to the given local in the given method.
		 *
		 * @param local of interest.
		 * @param method in which the local occurs.
		 *
		 * @return the corresponding local.
		 */
		Local getLocal(Local local, SootMethod method);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
	 */
	public void caseArrayRef(final ArrayRef v) {
		v.getBase().apply(this);

		Value base = cloneValue;
		v.getIndex().apply(this);
		cloneValue = JIMPLE.newArrayRef(base, cloneValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
	 */
	public void caseAssignStmt(final AssignStmt stmt) {
		stmt.getLeftOp().apply(this);

		Value left = cloneValue;
		stmt.getRightOp().apply(this);

		Value right = cloneValue;
		cloneStmt = JIMPLE.newAssignStmt(left, right);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
	 */
	public void caseBreakpointStmt(final BreakpointStmt stmt) {
		cloneStmt = (BreakpointStmt) stmt.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
	 */
	public void caseCastExpr(final CastExpr v) {
		v.getOp().apply(this);
		cloneValue = JIMPLE.newCastExpr(cloneValue, v.getType());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef)
	 */
	public void caseCaughtExceptionRef(final CaughtExceptionRef v) {
		cloneValue = (CaughtExceptionRef) v.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
	 */
	public void caseDoubleConstant(final DoubleConstant v) {
		cloneValue = (DoubleConstant) v.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
	 */
	public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
		stmt.getOp().apply(this);
		cloneStmt = JIMPLE.newEnterMonitorStmt(cloneValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
	 */
	public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
		stmt.getOp().apply(this);
		cloneStmt = JIMPLE.newExitMonitorStmt(cloneValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
	 */
	public void caseFloatConstant(final FloatConstant v) {
		cloneValue = (FloatConstant) v.clone();
	}

	/**
	 * Clones the given goto statement.  However, the cloning is broken purposefully.  At the time of cloning it is
	 * impossible to know if the target exists in the clone.  Hence, we create a clone which points to the original target.
	 * This way the application can fix the target information once the entire method's body has been cloned.
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
	 */
	public void caseGotoStmt(final GotoStmt stmt) {
		cloneStmt = JIMPLE.newGotoStmt(stmt.getTarget());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
	 */
	public void caseIdentityStmt(final IdentityStmt stmt) {
		stmt.getLeftOp().apply(this);

		Value left = cloneValue;
		stmt.getRightOp().apply(this);

		Value right = cloneValue;
		cloneStmt = JIMPLE.newIdentityStmt(left, right);
	}

	/**
	 * Clones the given if statement.  Please refer to {@link #caseGotoStmt(GotoStmt) caseGotoStmt} for details about how the
	 * target is handled.
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
	 */
	public void caseIfStmt(final IfStmt stmt) {
		stmt.getCondition().apply(this);
		cloneStmt = JIMPLE.newIfStmt(cloneValue, stmt.getTarget());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
	 */
	public void caseInstanceFieldRef(final InstanceFieldRef v) {
		v.getBase().apply(this);
		cloneValue = JIMPLE.newInstanceFieldRef(cloneValue, helper.getCloneOf(v.getField()));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
	 */
	public void caseInstanceOfExpr(final InstanceOfExpr v) {
		v.getOp().apply(this);
		cloneValue = JIMPLE.newInstanceOfExpr(cloneValue, v.getCheckType());
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
	 */
	public void caseIntConstant(final IntConstant v) {
		cloneValue = (IntConstant) v.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
	 */
	public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
		v.getBase().apply(this);

		Local base = (Local) cloneValue;
		cloneValue = JIMPLE.newInterfaceInvokeExpr(base, helper.getCloneOf(v.getMethod()), getArgs(v));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
	 */
	public void caseInvokeStmt(final InvokeStmt stmt) {
		stmt.getInvokeExpr().apply(this);
		cloneStmt = JIMPLE.newInvokeStmt(cloneValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
	 */
	public void caseLengthExpr(final LengthExpr v) {
		v.getOp().apply(this);
		cloneValue = JIMPLE.newLengthExpr(cloneValue);
	}

	/**
	 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.jimple.Local)
	 */
	public void caseLocal(final Local l) {
		cloneValue = helper.getLocal(l, method);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
	 */
	public void caseLongConstant(final LongConstant v) {
		cloneValue = (LongConstant) v.clone();
	}

	/**
	 * Clones the given lookup statement.  The lookup values are cloned but the targets are not.  So, the clone statement
	 * refers to the targets in the clonee statement.  This happens as at the time of processing of the lookup statement the
	 * targets may not exists.  Hence, it is the responsibility of the application to later fix up the targets suitably.
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt is the statement to be cloned.
	 *
	 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
	 */
	public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
		stmt.getKey().apply(this);

		List temp = new ArrayList();

		for (Iterator i = stmt.getLookupValues().iterator(); i.hasNext();) {
			Value v = (Value) i.next();
			v.apply(this);
			temp.add(cloneValue);
		}
		cloneStmt = JIMPLE.newLookupSwitchStmt(cloneValue, temp, stmt.getTargets(), stmt.getDefaultTarget());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
	 */
	public void caseNewArrayExpr(final NewArrayExpr v) {
		v.getSize().apply(this);
		cloneValue = JIMPLE.newNewArrayExpr(v.getBaseType(), cloneValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
	 */
	public void caseNewExpr(final NewExpr v) {
		cloneValue = JIMPLE.newNewExpr(v.getBaseType());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
	 */
	public void caseNewMultiArrayExpr(final NewMultiArrayExpr v) {
		List sizes = new ArrayList();

		for (int i = sizes.size() - 1; i >= 0; i--) {
			v.getSize(i).apply(this);
			sizes.add(i, cloneValue);
		}
		cloneValue = JIMPLE.newNewMultiArrayExpr(v.getBaseType(), sizes);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
	 */
	public void caseNopStmt(final NopStmt stmt) {
		cloneStmt = (NopStmt) stmt.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
	 */
	public void caseNullConstant(final NullConstant v) {
		cloneValue = (NullConstant) v.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
	 */
	public void caseParameterRef(final ParameterRef v) {
		cloneValue = (ParameterRef) v.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
	 */
	public void caseRetStmt(final RetStmt stmt) {
		stmt.getStmtAddress().apply(this);
		cloneStmt = JIMPLE.newRetStmt(cloneValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
	 */
	public void caseReturnStmt(final ReturnStmt stmt) {
		stmt.getOp().apply(this);
		cloneStmt = JIMPLE.newReturnStmt(cloneValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
	 */
	public void caseReturnVoidStmt(final ReturnVoidStmt stmt) {
		cloneStmt = (ReturnVoidStmt) stmt.clone();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
	 */
	public void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
		v.getBase().apply(this);

		Local base = (Local) cloneValue;
		cloneValue = JIMPLE.newSpecialInvokeExpr(base, helper.getCloneOf(v.getMethod()), getArgs(v));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
	 */
	public void caseStaticFieldRef(final StaticFieldRef v) {
		cloneValue = JIMPLE.newStaticFieldRef(helper.getCloneOf(v.getField()));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
	 */
	public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
		cloneValue = JIMPLE.newStaticInvokeExpr(helper.getCloneOf(v.getMethod()), getArgs(v));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
	 */
	public void caseStringConstant(final StringConstant v) {
		cloneValue = (StringConstant) v.clone();
	}

	/**
	 * Clones the given statement. Please refer to {@link #caseLookupSwitchStmt(LookupSwitchStmt) caseLookupSwitchStmt} for
	 * details about how targets are handled.
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
	 */
	public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
		stmt.getKey().apply(this);
		cloneStmt =
			JIMPLE.newTableSwitchStmt(cloneValue, stmt.getLowIndex(), stmt.getHighIndex(), stmt.getTargets(),
				stmt.getDefaultTarget());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
	 */
	public void caseThisRef(final ThisRef v) {
		cloneValue = JIMPLE.newThisRef((RefType) v.getType());
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param stmt is the statement to be cloned.
	 *
	 * @pre stmt != null
	 *
	 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
	 */
	public void caseThrowStmt(final ThrowStmt stmt) {
		stmt.getOp().apply(this);
		cloneStmt = JIMPLE.newThrowStmt(cloneValue);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param v is the value to be cloned.
	 *
	 * @pre v != null
	 *
	 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
	 */
	public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
		v.getBase().apply(this);

		Local base = (Local) cloneValue;
		cloneValue = JIMPLE.newVirtualInvokeExpr(base, helper.getCloneOf(v.getMethod()), getArgs(v));
	}

	/**
	 * Clones a given Jimple statement that occurs in the given method.
	 *
	 * @param stmt is the Jimple statement to be cloned.
	 * @param cloneeMethod in which <code>stmt</code> occurs.
	 *
	 * @return the clone of <code>stmt</code>.
	 *
	 * @pre stmt != null and cloneeMethod != null
	 * @post result != null and result.oclIsTypeOf(stmt.evaluationType())
	 */
	public Stmt cloneASTFragment(final Stmt stmt, final SootMethod cloneeMethod) {
		this.method = cloneeMethod;
		stmt.apply(this);
		return cloneStmt;
	}

	/**
	 * Clones a given Jimple value that occurs in the given method.
	 *
	 * @param value is the Jimple value to be cloned.
	 * @param cloneeMethod in which <code>value</code> occurs.
	 *
	 * @return the clone of <code>value</code>.
	 *
	 * @pre value != null and cloneeMethod != null
	 * @post result != null and result.oclIsTypeOf(stmt.evaluationType())
	 */
	public Value cloneASTFragment(final Value value, final SootMethod cloneeMethod) {
		this.method = cloneeMethod;
		value.apply(this);
		return cloneValue;
	}

	/**
	 * Returns the AST chunks(Jimple <code>Value</code>s) corresponding to the arguments to the given invoke expression.
	 *
	 * @param v is the method invocation of interest.
	 *
	 * @return a collection of AST chunks.
	 *
	 * @pre v != null
	 * @post result != null
	 * @post result.oclIsKindOf(Sequence(Value)) and result.size() == v.getArgCount()
	 * @post v.getArgs()->forall(o | result->at(v.getArgs().indexOf(o)).oclIsKindOf(o.evaluationType()))
	 */
	private List getArgs(final InvokeExpr v) {
		List result = new ArrayList();

		for (int i = result.size() - 1; i >= 0; i--) {
			v.getArg(i).apply(this);
			result.add(i, cloneValue);
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2003/08/19 11:58:53  venku
   Remove any reference to slicing from the documentation.

   
   Revision 1.7  2003/08/18 04:45:31  venku
   Moved the code such that code common to transformations are in one location
   and independent of any specific transformation.
   
   Revision 1.6  2003/08/18 04:01:52  venku
   Major changes:
    - Teased apart cloning logic in the slicer.  Made it transformation independent.
    - Moved it under transformation common location under indus.
    
   Revision 1.5  2003/05/22 22:23:49  venku
   Changed interface names to start with a "I".
   Formatting.
 */
