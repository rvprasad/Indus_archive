
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

import soot.ArrayType;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.BinopExpr;
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
import soot.jimple.InstanceInvokeExpr;
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
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.ThisRef;
import soot.jimple.UnopExpr;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

import java.io.IOException;
import java.io.Writer;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ValueXMLizer
  extends AbstractJimpleValueSwitch {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final IJimpleIDGenerator idGenerator;

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
	private SootMethod currMethod;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Stmt currStmt;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Writer out;

	/**
	 * Creates a new ValueXMLizer object.
	 *
	 * @param generator DOCUMENT ME!
	 */
	ValueXMLizer(final IJimpleIDGenerator generator) {
		idGenerator = generator;
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseAddExpr(soot.jimple.AddExpr)
	 */
	public final void caseAddExpr(AddExpr v) {
		writeBinaryExpr("add", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseAndExpr(soot.jimple.AndExpr)
	 */
	public final void caseAndExpr(AndExpr v) {
		writeBinaryExpr("binary_and", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
	 */
	public final void caseArrayRef(ArrayRef v) {
		try {
			out.write("<array_ref id=\"" + newId + "\">");
			writeBase(v.getBase());
			out.write("<index>");
			apply(v.getIndex());
			out.write("</index>");
			out.write("</array_ref>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
	 */
	public final void caseCastExpr(CastExpr v) {
		try {
			out.write("<cast id=\"" + newId + "\" " + idGenerator.getIdForType(v.getCastType()) + " \">");
			apply(v.getOp());
			out.write("</cast >");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef)
	 */
	public final void caseCaughtExceptionRef(CaughtExceptionRef v) {
		try {
			out.write("<caught_exception_ref id=\"" + newId + " exceptionTypeId=\"" + idGenerator.getIdForType(v.getType())
				+ "\">");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmpExpr(soot.jimple.CmpExpr)
	 */
	public final void caseCmpExpr(CmpExpr v) {
		writeBinaryExpr("compare", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmpgExpr(soot.jimple.CmpgExpr)
	 */
	public final void caseCmpgExpr(CmpgExpr v) {
		writeBinaryExpr("compare greater", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmplExpr(soot.jimple.CmplExpr)
	 */
	public final void caseCmplExpr(CmplExpr v) {
		writeBinaryExpr("compare lesser", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseDivExpr(soot.jimple.DivExpr)
	 */
	public final void caseDivExpr(DivExpr v) {
		writeBinaryExpr("divide", v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
	 */
	public final void caseDoubleConstant(DoubleConstant v) {
		try {
			out.write("<double id=\"" + newId + "\" value=\"" + v.value + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseEqExpr(soot.jimple.EqExpr)
	 */
	public final void caseEqExpr(EqExpr v) {
		writeBinaryExpr("equal", v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
	 */
	public final void caseFloatConstant(FloatConstant v) {
		try {
			out.write("<float id=\"" + newId + "\" value=\"" + v.value + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseGeExpr(soot.jimple.GeExpr)
	 */
	public final void caseGeExpr(GeExpr v) {
		writeBinaryExpr("greater than or equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseGtExpr(soot.jimple.GtExpr)
	 */
	public final void caseGtExpr(GtExpr v) {
		writeBinaryExpr("greater than", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
	 */
	public final void caseInstanceFieldRef(InstanceFieldRef v) {
		try {
			out.write("<instance_field_ref id=\"" + newId + "\">");
			writeBase(v.getBase());
			writeField(v.getField());
			out.write("</instance_field_ref>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
	 */
	public final void caseInstanceOfExpr(InstanceOfExpr v) {
		try {
			out.write("<instanceof id=\"" + newId + "\" typeId=\"" + idGenerator.getIdForType(v.getCheckType()) + "\">");
			apply(v.getOp());
			out.write("</instanceof>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
	 */
	public final void caseIntConstant(IntConstant v) {
		try {
			out.write("<integer id=\"" + newId + "\" value=\"" + v.value + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
	 */
	public final void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
		writeInstanceInvokeExpr("interface", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLeExpr(soot.jimple.LeExpr)
	 */
	public final void caseLeExpr(LeExpr v) {
		writeBinaryExpr("less than or equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
	 */
	public final void caseLengthExpr(LengthExpr v) {
		writeUnaryExpr("length", v);
	}

	/**
	 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
	 */
	public final void caseLocal(Local v) {
		try {
			out.write("<local id=\"" + newId + "\" localId=\"" + idGenerator.getIdForLocal(v, currMethod) + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
	 */
	public final void caseLongConstant(LongConstant v) {
		try {
			out.write("<long id=\"" + newId + "\" value=\"" + v.value + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLtExpr(soot.jimple.LtExpr)
	 */
	public final void caseLtExpr(LtExpr v) {
		writeBinaryExpr("less than", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseMulExpr(soot.jimple.MulExpr)
	 */
	public final void caseMulExpr(MulExpr v) {
		writeBinaryExpr("multiply", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNeExpr(soot.jimple.NeExpr)
	 */
	public final void caseNeExpr(NeExpr v) {
		writeBinaryExpr("not equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNegExpr(soot.jimple.NegExpr)
	 */
	public final void caseNegExpr(NegExpr v) {
		writeUnaryExpr("binary negation", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
	 */
	public final void caseNewArrayExpr(NewArrayExpr v) {
		try {
			out.write("<new_array id=\"" + newId + "\" baseTypeId=\"" + idGenerator.getIdForType(v.getBaseType()) + "\">");
			writeDimensionSize(1, v.getSize());
			out.write("</new_array>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
	 */
	public final void caseNewExpr(NewExpr v) {
		try {
			out.write("<new id=\"" + newId + "\" typeId=\"" + idGenerator.getIdForType(v.getType()) + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
	 */
	public final void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
		ArrayType type = v.getBaseType();

		try {
			out.write("<new_multi_array id=\"" + newId + "\" baseTypeId=\"" + idGenerator.getIdForType(type.baseType)
				+ "\" dimension=\"" + type.numDimensions + "\">");

			for (int i = 0; i < type.numDimensions; i++) {
				writeDimensionSize(i + 1, v.getSize(i));
			}
			out.write("</new_multi_array>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
	 */
	public final void caseNullConstant(NullConstant v) {
		try {
			out.write("<null/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseOrExpr(soot.jimple.OrExpr)
	 */
	public final void caseOrExpr(OrExpr v) {
		writeBinaryExpr("binary or", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
	 */
	public final void caseParameterRef(ParameterRef v) {
		try {
			out.write("<parameter_ref id=\"" + newId + "\" position=\"" + v.getIndex() + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseRemExpr(soot.jimple.RemExpr)
	 */
	public final void caseRemExpr(RemExpr v) {
		writeBinaryExpr("reminder", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseShlExpr(soot.jimple.ShlExpr)
	 */
	public final void caseShlExpr(ShlExpr v) {
		writeBinaryExpr("shift left", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseShrExpr(soot.jimple.ShrExpr)
	 */
	public final void caseShrExpr(ShrExpr v) {
		writeBinaryExpr("shift right", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
	 */
	public final void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
		writeInstanceInvokeExpr("special", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
	 */
	public final void caseStaticFieldRef(StaticFieldRef v) {
		try {
			out.write("<static_field_ref id=\"" + newId + "\">");
			writeField(v.getField());
			out.write("</static_field_ref>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
	 */
	public final void caseStaticInvokeExpr(StaticInvokeExpr v) {
		try {
			out.write("<invoke_expr name=\"static\" id=\"" + newId + "\">");
			out.write("<method id=\"" + idGenerator.getIdForMethod(v.getMethod()) + "\"/>");
			out.write("<arguments>");

			for (int i = 0; i < v.getArgCount(); i++) {
				apply(v.getArg(i));
			}
			out.write("</arguments>");
			out.write("</invoke_expr>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
	 */
	public final void caseStringConstant(StringConstant v) {
		try {
			out.write("<string id=\"" + newId + "\" value=\"" + v.value + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseSubExpr(soot.jimple.SubExpr)
	 */
	public final void caseSubExpr(SubExpr v) {
		writeBinaryExpr("subtract", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
	 */
	public final void caseThisRef(ThisRef v) {
		try {
			out.write("<this id=\"" + newId + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseUshrExpr(soot.jimple.UshrExpr)
	 */
	public final void caseUshrExpr(UshrExpr v) {
		writeBinaryExpr("unsigned shift right", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
	 */
	public final void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
		writeInstanceInvokeExpr("virtual", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseXorExpr(soot.jimple.XorExpr)
	 */
	public final void caseXorExpr(XorExpr v) {
		writeBinaryExpr("binary xor", v);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param method DOCUMENT ME!
	 */
	void setMethod(final SootMethod method) {
		currMethod = method;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 */
	void setStmt(final Stmt stmt) {
		currStmt = stmt;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stream DOCUMENT ME!
	 */
	void setWriter(final Writer stream) {
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
		newId = idGenerator.getNewValueId(currStmt, currMethod);
		value.apply(this);
		newId = temp;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param v
	 */
	private void writeBase(Value v) {
		try {
			out.write("<base>");
			apply(v);
			out.write("</base>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param operatorName
	 * @param v
	 */
	private void writeBinaryExpr(String operatorName, BinopExpr v) {
		try {
			out.write("<binary_expr id=\"" + newId + "\" op=\"" + operatorName + "\">");
			out.write("<left_op>");
			apply(v.getOp1());
			out.write("</left_op>");
			out.write("<right_op>");
			apply(v.getOp1());
			out.write("</right_op>");
			out.write("</binary_expr>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param i DOCUMENT ME!
	 * @param v DOCUMENT ME!
	 */
	private void writeDimensionSize(int i, Value v) {
		try {
			out.write("<size dimension=\"" + i + "\">");
			apply(v);
			out.write("</size>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param field
	 */
	private void writeField(SootField field) {
		try {
			out.write("<field id=\"" + idGenerator.getIdForField(field) + "\" signature=\"" + field.getSubSignature() + "\">");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param name
	 * @param v
	 */
	private void writeInstanceInvokeExpr(String name, InstanceInvokeExpr v) {
		try {
			out.write("<invoke_expr name=\"" + name + "\" id=\"" + newId + "\">");

			SootMethod method = v.getMethod();
			out.write("<method id=\"" + idGenerator.getIdForMethod(method) + "\"/>");
			writeBase(v.getBase());
			out.write("<arguments>");

			for (int i = 0; i < v.getArgCount(); i++) {
				apply(v.getArg(i));
			}
			out.write("</arguments>");
			out.write("</invoke_expr>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param operatorName
	 * @param value
	 */
	private void writeUnaryExpr(String operatorName, UnopExpr value) {
		try {
			out.write("<unary_expr name=\"" + operatorName + "\" id=\"" + newId + "\">");
			apply(value.getOp());
			out.write("</" + operatorName + ">");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/07 06:27:03  venku
   - Made the XMLizer classes concrete by moving out the
     id generation logic outside.
   - Added an interface which provides the id required for
     xmlizing Jimple.
   Revision 1.1  2003/11/06 10:01:25  venku
   - created support for xmlizing Jimple in a customizable manner.
 */
