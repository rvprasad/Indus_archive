
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

package edu.ksu.cis.indus.xmlizer;

import soot.Local;
import soot.Value;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.EqExpr;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GtExpr;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

import java.io.StringWriter;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractValueXMLizer
  extends AbstractJimpleValueSwitch {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Object newId;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private StringWriter out;

	/**
	 * @see soot.jimple.ExprSwitch#caseAddExpr(soot.jimple.AddExpr)
	 */
	public final void caseAddExpr(AddExpr v) {
		out.write("<add_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</add_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseAndExpr(soot.jimple.AndExpr)
	 */
	public final void caseAndExpr(AndExpr v) {
		out.write("<and_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</and_expr>");
	}

	/**
	 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
	 */
	public final void caseArrayRef(ArrayRef v) {
		// TODO: Auto-generated method stub
		super.caseArrayRef(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
	 */
	public final void caseCastExpr(CastExpr v) {
		// TODO: Auto-generated method stub
		super.caseCastExpr(v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef)
	 */
	public final void caseCaughtExceptionRef(CaughtExceptionRef v) {
		// TODO: Auto-generated method stub
		super.caseCaughtExceptionRef(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmpExpr(soot.jimple.CmpExpr)
	 */
	public final void caseCmpExpr(CmpExpr v) {
		out.write("<cmp_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</cmp_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmpgExpr(soot.jimple.CmpgExpr)
	 */
	public final void caseCmpgExpr(CmpgExpr v) {
		out.write("<cmpg_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</cmpg_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmplExpr(soot.jimple.CmplExpr)
	 */
	public final void caseCmplExpr(CmplExpr v) {
		out.write("<cmpl_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</cmpl_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseDivExpr(soot.jimple.DivExpr)
	 */
	public final void caseDivExpr(DivExpr v) {
		out.write("<div_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</div_expr>");
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
	 */
	public final void caseDoubleConstant(DoubleConstant v) {
		// TODO: Auto-generated method stub
		super.caseDoubleConstant(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseEqExpr(soot.jimple.EqExpr)
	 */
	public final void caseEqExpr(EqExpr v) {
		out.write("<equal_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</equal_expr>");
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
	 */
	public final void caseFloatConstant(FloatConstant v) {
		// TODO: Auto-generated method stub
		super.caseFloatConstant(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseGeExpr(soot.jimple.GeExpr)
	 */
	public final void caseGeExpr(GeExpr v) {
		out.write("<ge_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</ge_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseGtExpr(soot.jimple.GtExpr)
	 */
	public final void caseGtExpr(GtExpr v) {
		out.write("<gt_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</gt_expr>");
	}

	/**
	 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
	 */
	public final void caseInstanceFieldRef(InstanceFieldRef v) {
		// TODO: Auto-generated method stub
		super.caseInstanceFieldRef(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
	 */
	public final void caseInstanceOfExpr(InstanceOfExpr v) {
		// TODO: Auto-generated method stub
		super.caseInstanceOfExpr(v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
	 */
	public final void caseIntConstant(IntConstant v) {
		// TODO: Auto-generated method stub
		super.caseIntConstant(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
	 */
	public final void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		// TODO: Auto-generated method stub
		super.caseInterfaceInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLeExpr(soot.jimple.LeExpr)
	 */
	public final void caseLeExpr(LeExpr v) {
		out.write("<le_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</le_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
	 */
	public final void caseLengthExpr(LengthExpr v) {
		// TODO: Auto-generated method stub
		super.caseLengthExpr(v);
	}

	/**
	 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
	 */
	public final void caseLocal(Local v) {
		out.write("<local id=\"" + newId + "\" name=\"" + v.getName() + "\"/>");
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
	 */
	public final void caseLongConstant(LongConstant v) {
		// TODO: Auto-generated method stub
		super.caseLongConstant(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLtExpr(soot.jimple.LtExpr)
	 */
	public final void caseLtExpr(LtExpr v) {
		out.write("<lt_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</lt_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseMulExpr(soot.jimple.MulExpr)
	 */
	public final void caseMulExpr(MulExpr v) {
		out.write("<mul_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</mul_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNeExpr(soot.jimple.NeExpr)
	 */
	public final void caseNeExpr(NeExpr v) {
		out.write("<ne_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</ne_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNegExpr(soot.jimple.NegExpr)
	 */
	public final void caseNegExpr(NegExpr v) {
		// TODO: Auto-generated method stub
		super.caseNegExpr(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
	 */
	public final void caseNewArrayExpr(NewArrayExpr v) {
		// TODO: Auto-generated method stub
		super.caseNewArrayExpr(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
	 */
	public final void caseNewExpr(NewExpr v) {
		// TODO: Auto-generated method stub
		super.caseNewExpr(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
	 */
	public final void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		// TODO: Auto-generated method stub
		super.caseNewMultiArrayExpr(v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
	 */
	public final void caseNullConstant(NullConstant v) {
		out.write("<null/>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseOrExpr(soot.jimple.OrExpr)
	 */
	public final void caseOrExpr(OrExpr v) {
		out.write("<or_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</or_expr>");
	}

	/**
	 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
	 */
	public final void caseParameterRef(ParameterRef v) {
		// TODO: Auto-generated method stub
		super.caseParameterRef(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseRemExpr(soot.jimple.RemExpr)
	 */
	public final void caseRemExpr(RemExpr v) {
		out.write("<rem_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</rem_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseShlExpr(soot.jimple.ShlExpr)
	 */
	public final void caseShlExpr(ShlExpr v) {
		out.write("<shl_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</shl_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseShrExpr(soot.jimple.ShrExpr)
	 */
	public final void caseShrExpr(ShrExpr v) {
		out.write("<shr_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</shr_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
	 */
	public final void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		// TODO: Auto-generated method stub
		super.caseSpecialInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
	 */
	public final void caseStaticFieldRef(StaticFieldRef v) {
		// TODO: Auto-generated method stub
		super.caseStaticFieldRef(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
	 */
	public final void caseStaticInvokeExpr(StaticInvokeExpr v) {
		// TODO: Auto-generated method stub
		super.caseStaticInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
	 */
	public final void caseStringConstant(StringConstant v) {
		// TODO: Auto-generated method stub
		super.caseStringConstant(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseSubExpr(soot.jimple.SubExpr)
	 */
	public final void caseSubExpr(SubExpr v) {
		out.write("<sub_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</sub_expr>");
	}

	/**
	 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
	 */
	public final void caseThisRef(ThisRef v) {
		// TODO: Auto-generated method stub
		super.caseThisRef(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseUshrExpr(soot.jimple.UshrExpr)
	 */
	public final void caseUshrExpr(UshrExpr v) {
		out.write("<ushr_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</ushr_expr>");
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
	 */
	public final void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		// TODO: Auto-generated method stub
		super.caseVirtualInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseXorExpr(soot.jimple.XorExpr)
	 */
	public final void caseXorExpr(XorExpr v) {
		out.write("<xor_expr id=\"" + newId + "\">");
		out.write("<left_op>");
		v.getOp1().apply(this);
		out.write("</left_op>");
		out.write("<right_op>");
		v.getOp2().apply(this);
		out.write("</right_op>");
		out.write("</xor_expr>");
	}

	/**
	 * @see soot.jimple.RefSwitch#defaultCase(java.lang.Object)
	 */
	public final void defaultCase(Object v) {
		// TODO: Auto-generated method stub
		super.defaultCase(v);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	protected abstract String getNewId();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected abstract void newStmt(final Object stmtId);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stream DOCUMENT ME!
	 */
	void setOutputStream(final StringWriter stream) {
		out = stream;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param value DOCUMENT ME!
	 */
	void apply(final Value value) {
		Object temp = newId;
		newId = getNewId();
		value.apply(this);
		newId = temp;
	}
}

/*
   ChangeLog:
   $Log$
 */
