
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

import java.io.IOException;
import java.io.Writer;

import soot.ArrayType;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;

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
import soot.jimple.InvokeExpr;
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
	StringBuffer tabs = new StringBuffer("\t\t\t");

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
	public final void caseAddExpr(final AddExpr v) {
		writeBinaryExpr("add", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseAndExpr(soot.jimple.AndExpr)
	 */
	public final void caseAndExpr(final AndExpr v) {
		writeBinaryExpr("binary and", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
	 */
	public final void caseArrayRef(final ArrayRef v) {
		try {
			out.write(tabs + "<array_ref id=\"" + newId + "\">\n");
			writeBase(v.getBaseBox());
			incrementTabs();
			out.write(tabs + "<index>\n");
			apply(v.getIndexBox());
			out.write(tabs + "</index>\n");
			decrementTabs();
			out.write(tabs + "</array_ref>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
	 */
	public final void caseCastExpr(final CastExpr v) {
		try {
			out.write(tabs + "<cast id=\"" + newId + "\" typeId=\"" + idGenerator.getIdForType(v.getCastType()) + "\">\n");
			apply(v.getOpBox());
			out.write(tabs + "</cast >\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef)
	 */
	public final void caseCaughtExceptionRef(final CaughtExceptionRef v) {
		try {
			out.write(tabs + "<caught_exception_ref id=\"" + newId + "\" exceptionTypeId=\""
				+ idGenerator.getIdForType(v.getType()) + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmpExpr(soot.jimple.CmpExpr)
	 */
	public final void caseCmpExpr(final CmpExpr v) {
		writeBinaryExpr("compare", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmpgExpr(soot.jimple.CmpgExpr)
	 */
	public final void caseCmpgExpr(final CmpgExpr v) {
		writeBinaryExpr("compare greater", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmplExpr(soot.jimple.CmplExpr)
	 */
	public final void caseCmplExpr(final CmplExpr v) {
		writeBinaryExpr("compare lesser", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseDivExpr(soot.jimple.DivExpr)
	 */
	public final void caseDivExpr(final DivExpr v) {
		writeBinaryExpr("divide", v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
	 */
	public final void caseDoubleConstant(final DoubleConstant v) {
		try {
			out.write(tabs + "<double id=\"" + newId + "\" value=\"" + v.value + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseEqExpr(soot.jimple.EqExpr)
	 */
	public final void caseEqExpr(final EqExpr v) {
		writeBinaryExpr("equal", v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
	 */
	public final void caseFloatConstant(final FloatConstant v) {
		try {
			out.write(tabs + "<float id=\"" + newId + "\" value=\"" + v.value + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseGeExpr(soot.jimple.GeExpr)
	 */
	public final void caseGeExpr(final GeExpr v) {
		writeBinaryExpr("greater than or equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseGtExpr(soot.jimple.GtExpr)
	 */
	public final void caseGtExpr(final GtExpr v) {
		writeBinaryExpr("greater than", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
	 */
	public final void caseInstanceFieldRef(final InstanceFieldRef v) {
		try {
			out.write(tabs + "<instance_field_ref id=\"" + newId + "\">\n");
			writeBase(v.getBaseBox());
			writeField(v.getField());
			out.write(tabs + "</instance_field_ref>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
	 */
	public final void caseInstanceOfExpr(final InstanceOfExpr v) {
		try {
			out.write(tabs + "<instanceof id=\"" + newId + "\" typeId=\"" + idGenerator.getIdForType(v.getCheckType())
				+ "\">\n");
			apply(v.getOpBox());
			out.write(tabs + "</instanceof>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
	 */
	public final void caseIntConstant(final IntConstant v) {
		try {
			out.write(tabs + "<integer id=\"" + newId + "\" value=\"" + v.value + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
	 */
	public final void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
		writeInvokeExpr("interface", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLeExpr(soot.jimple.LeExpr)
	 */
	public final void caseLeExpr(final LeExpr v) {
		writeBinaryExpr("less than or equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
	 */
	public final void caseLengthExpr(final LengthExpr v) {
		writeUnaryExpr("length", v);
	}

	/**
	 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
	 */
	public final void caseLocal(final Local v) {
		try {
			out.write(tabs + "<local_ref id=\"" + newId + "\" localId=\"" + idGenerator.getIdForLocal(v, currMethod)
				+ "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
	 */
	public final void caseLongConstant(final LongConstant v) {
		try {
			out.write(tabs + "<long id=\"" + newId + "\" value=\"" + v.value + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLtExpr(soot.jimple.LtExpr)
	 */
	public final void caseLtExpr(final LtExpr v) {
		writeBinaryExpr("less than", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseMulExpr(soot.jimple.MulExpr)
	 */
	public final void caseMulExpr(final MulExpr v) {
		writeBinaryExpr("multiply", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNeExpr(soot.jimple.NeExpr)
	 */
	public final void caseNeExpr(final NeExpr v) {
		writeBinaryExpr("not equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNegExpr(soot.jimple.NegExpr)
	 */
	public final void caseNegExpr(final NegExpr v) {
		writeUnaryExpr("negation", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
	 */
	public final void caseNewArrayExpr(final NewArrayExpr v) {
		try {
			out.write(tabs + "<new_array id=\"" + newId + "\" typeId=\"" + idGenerator.getIdForType(v.getBaseType())
				+ "\">\n");
			writeDimensionSize(1, v.getSizeBox());
			out.write(tabs + "</new_array>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
	 */
	public final void caseNewExpr(final NewExpr v) {
		try {
			out.write(tabs + "<new id=\"" + newId + "\" typeId=\"" + idGenerator.getIdForType(v.getType()) + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
	 */
	public final void caseNewMultiArrayExpr(final NewMultiArrayExpr v) {
		final ArrayType _type = v.getBaseType();

		try {
			out.write(tabs + "<new_multi_array id=\"" + newId + "\" typeId=\"" + idGenerator.getIdForType(_type.baseType)
				+ "\" dimension=\"" + _type.numDimensions + "\">\n");

			for (int i = 0; i < v.getSizeCount(); i++) {
				writeDimensionSize(i + 1, v.getSizeBox(i));
			}
			out.write(tabs + "</new_multi_array>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
	 */
	public final void caseNullConstant(final NullConstant v) {
		try {
			out.write(tabs + "<null/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseOrExpr(soot.jimple.OrExpr)
	 */
	public final void caseOrExpr(final OrExpr v) {
		writeBinaryExpr("binary or", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
	 */
	public final void caseParameterRef(final ParameterRef v) {
		try {
			out.write(tabs + "<parameter_ref id=\"" + newId + "\" position=\"" + v.getIndex() + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseRemExpr(soot.jimple.RemExpr)
	 */
	public final void caseRemExpr(final RemExpr v) {
		writeBinaryExpr("reminder", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseShlExpr(soot.jimple.ShlExpr)
	 */
	public final void caseShlExpr(final ShlExpr v) {
		writeBinaryExpr("shift left", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseShrExpr(soot.jimple.ShrExpr)
	 */
	public final void caseShrExpr(final ShrExpr v) {
		writeBinaryExpr("shift right", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
	 */
	public final void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
		writeInvokeExpr("special", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
	 */
	public final void caseStaticFieldRef(final StaticFieldRef v) {
		try {
			out.write(tabs + "<static_field_ref id=\"" + newId + "\">\n");
			writeField(v.getField());
			out.write(tabs + "</static_field_ref>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
	 */
	public final void caseStaticInvokeExpr(final StaticInvokeExpr v) {
		writeInvokeExpr("static", v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
	 */
	public final void caseStringConstant(final StringConstant v) {
		try {
			out.write(tabs + "<string id=\"" + newId + "\" value=\"" + v.value + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseSubExpr(soot.jimple.SubExpr)
	 */
	public final void caseSubExpr(final SubExpr v) {
		writeBinaryExpr("subtract", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
	 */
	public final void caseThisRef(final ThisRef v) {
		try {
			out.write(tabs + "<this id=\"" + newId + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseUshrExpr(soot.jimple.UshrExpr)
	 */
	public final void caseUshrExpr(final UshrExpr v) {
		writeBinaryExpr("unsigned shift right", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
	 */
	public final void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
		writeInvokeExpr("virtual", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseXorExpr(soot.jimple.XorExpr)
	 */
	public final void caseXorExpr(final XorExpr v) {
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
	 * @param vBox DOCUMENT ME!
	 */
	void apply(final ValueBox vBox) {
		Object temp = newId;
		newId = idGenerator.getIdForValueBox(vBox, currStmt, currMethod);
		incrementTabs();
		vBox.getValue().apply(this);
		decrementTabs();
		newId = temp;
	}

	/**
	 *
	 */
	private void decrementTabs() {
		tabs.deleteCharAt(0);
	}

	/**
	 *
	 */
	private void incrementTabs() {
		tabs.append("\t");
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param v DOCUMENT ME!
	 */
	private void writeBase(final ValueBox v) {
		try {
			incrementTabs();
			out.write(tabs + "<base>\n");
			apply(v);
			out.write(tabs + "</base>\n");
			decrementTabs();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param operatorName DOCUMENT ME!
	 * @param v DOCUMENT ME!
	 */
	private void writeBinaryExpr(final String operatorName, final BinopExpr v) {
		try {
			out.write(tabs + "<binary_expr id=\"" + newId + "\" op=\"" + operatorName + "\">\n");
			incrementTabs();
			out.write(tabs + "<left_op>\n");
			apply(v.getOp1Box());
			out.write(tabs + "</left_op>\n");
			out.write(tabs + "<right_op>\n");
			apply(v.getOp2Box());
			out.write(tabs + "</right_op>\n");
			decrementTabs();
			out.write(tabs + "</binary_expr>\n");
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
	private void writeDimensionSize(final int i, final ValueBox v) {
		try {
			incrementTabs();
			out.write(tabs + "<size dimension=\"" + i + "\">\n");
			apply(v);
			out.write(tabs + "</size>\n");
			decrementTabs();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param field DOCUMENT ME!
	 */
	private void writeField(final SootField field) {
		try {
			incrementTabs();
			out.write(tabs + "<field_ref fieldId=\"" + idGenerator.getIdForField(field) + "\"/>\n");
			decrementTabs();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 * @param v DOCUMENT ME!
	 */
	private void writeInvokeExpr(final String name, final InvokeExpr v) {
		try {
			out.write(tabs + "<invoke_expr name=\"" + name + "\" id=\"" + newId + "\">\n");

			final SootMethod _method = v.getMethod();
			incrementTabs();
			out.write(tabs + "<method_ref methodId=\"" + idGenerator.getIdForMethod(_method) + "\"/>\n");
			decrementTabs();

			if (v instanceof InstanceInvokeExpr) {
				writeBase(((InstanceInvokeExpr) v).getBaseBox());
			}

			if (v.getArgCount() > 0) {
				out.write(tabs + "\t<arguments>\n");

				for (int i = 0; i < v.getArgCount(); i++) {
					apply(v.getArgBox(i));
				}
				out.write(tabs + "\t</arguments>\n");
			}
			out.write(tabs + "</invoke_expr>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param operatorName DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 */
	private void writeUnaryExpr(final String operatorName, final UnopExpr value) {
		try {
			incrementTabs();
			out.write(tabs + "<unary_expr op=\"" + operatorName + "\" id=\"" + newId + "\">\n");
			apply(value.getOpBox());
			out.write(tabs + "</unary_expr>\n");
			decrementTabs();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.11  2003/12/02 01:30:58  venku
   - coding conventions and formatting.
   Revision 1.10  2003/11/30 12:49:28  venku
   - tabbing in the output.
   Revision 1.9  2003/11/30 09:44:53  venku
   - renamed getIdForValue to getIdForValueBox.
   Revision 1.8  2003/11/28 09:41:49  venku
   - tags and attribute names were changed.
   Revision 1.7  2003/11/26 18:26:08  venku
   - capture a whole lot more information for classes and methods.
   - removed unnecessary info from the attributes.
   Revision 1.6  2003/11/24 06:45:23  venku
   - corrected xml encoding errors along with tag name emission errors.
   Revision 1.5  2003/11/24 06:28:04  venku
   - static invoke expr is also routed through writeInvokeExpr().
   Revision 1.4  2003/11/24 01:20:27  venku
   - enhanced output formatting.
   Revision 1.3  2003/11/17 15:57:03  venku
   - removed support to retrieve new statement ids.
   - added support to retrieve id for value boxes.
   Revision 1.2  2003/11/07 11:14:44  venku
   - Added generator class for xmlizing purpose.
   - XMLizing of Jimple works, but takes long.
     Probably, reachable method dump should fix it.  Another rainy day problem.
   Revision 1.1  2003/11/07 06:27:03  venku
   - Made the XMLizer classes concrete by moving out the
     id generation logic outside.
   - Added an interface which provides the id required for
     xmlizing Jimple.
   Revision 1.1  2003/11/06 10:01:25  venku
   - created support for xmlizing Jimple in a customizable manner.
 */
