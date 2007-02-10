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

package edu.ksu.cis.indus.xmlizer;

import java.io.IOException;

import org.znerd.xmlenc.XMLOutputter;

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
 * This class provides logic xmlize Jimple Values.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class JimpleValueXMLizer
		extends AbstractJimpleValueSwitch {

	/**
	 * This is used to maintain tabbing the output xml.
	 * 
	 * @invariant tabs != null
	 */
	final StringBuffer tabs = new StringBuffer("\t\t\t");

	/**
	 * The current method being processed.
	 */
	private SootMethod currMethod;

	/**
	 * The current statement being processed.
	 */
	private Stmt currStmt;

	/**
	 * This is used generate ids used during xmlization.
	 * 
	 * @pre idGenerator != null
	 */
	private final IJimpleIDGenerator idGenerator;

	/**
	 * This caches the id of the current value/expression/program point being processed.
	 */
	private Object newId;

	/**
	 * This is the outputter to be used to write xml data.
	 */
	private XMLOutputter xmlWriter;

	/**
	 * Creates a new JimpleValueXMLizer object.
	 * 
	 * @param generator to be used generate ids during xmlization.
	 * @pre generator != null
	 */
	JimpleValueXMLizer(final IJimpleIDGenerator generator) {
		idGenerator = generator;
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseAddExpr(soot.jimple.AddExpr)
	 */
	@Override public final void caseAddExpr(final AddExpr v) {
		writeBinaryExpr("add", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseAndExpr(soot.jimple.AndExpr)
	 */
	@Override public final void caseAndExpr(final AndExpr v) {
		writeBinaryExpr("binary and", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
	 */
	@Override public final void caseArrayRef(final ArrayRef v) {
		try {
			xmlWriter.startTag("array_ref");
			xmlWriter.attribute("id", newId.toString());
			writeBase(v.getBaseBox());
			xmlWriter.startTag("index");
			apply(v.getIndexBox());
			xmlWriter.endTag();
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
	 */
	@Override public final void caseCastExpr(final CastExpr v) {
		try {
			xmlWriter.startTag("cast");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("typeId", idGenerator.getIdForType(v.getCastType()));
			apply(v.getOpBox());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef)
	 */
	@Override public final void caseCaughtExceptionRef(final CaughtExceptionRef v) {
		try {
			xmlWriter.startTag("caught_exception_ref");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("exceptionTypeId", idGenerator.getIdForType(v.getType()));
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmpExpr(soot.jimple.CmpExpr)
	 */
	@Override public final void caseCmpExpr(final CmpExpr v) {
		writeBinaryExpr("compare", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmpgExpr(soot.jimple.CmpgExpr)
	 */
	@Override public final void caseCmpgExpr(final CmpgExpr v) {
		writeBinaryExpr("compare greater", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseCmplExpr(soot.jimple.CmplExpr)
	 */
	@Override public final void caseCmplExpr(final CmplExpr v) {
		writeBinaryExpr("compare lesser", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseDivExpr(soot.jimple.DivExpr)
	 */
	@Override public final void caseDivExpr(final DivExpr v) {
		writeBinaryExpr("divide", v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
	 */
	@Override public final void caseDoubleConstant(final DoubleConstant v) {
		try {
			xmlWriter.startTag("double");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("value", String.valueOf(v.value));
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseEqExpr(soot.jimple.EqExpr)
	 */
	@Override public final void caseEqExpr(final EqExpr v) {
		writeBinaryExpr("equal", v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
	 */
	@Override public final void caseFloatConstant(final FloatConstant v) {
		try {
			xmlWriter.startTag("float");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("value", String.valueOf(v.value));
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseGeExpr(soot.jimple.GeExpr)
	 */
	@Override public final void caseGeExpr(final GeExpr v) {
		writeBinaryExpr("greater than or equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseGtExpr(soot.jimple.GtExpr)
	 */
	@Override public final void caseGtExpr(final GtExpr v) {
		writeBinaryExpr("greater than", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
	 */
	@Override public final void caseInstanceFieldRef(final InstanceFieldRef v) {
		try {
			xmlWriter.startTag("instance_field_ref");
			xmlWriter.attribute("id", newId.toString());
			writeBase(v.getBaseBox());
			writeField(v.getField());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
	 */
	@Override public final void caseInstanceOfExpr(final InstanceOfExpr v) {
		try {
			xmlWriter.startTag("instanceof");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("typeId", idGenerator.getIdForType(v.getCheckType()));
			apply(v.getOpBox());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
	 */
	@Override public final void caseIntConstant(final IntConstant v) {
		try {
			xmlWriter.startTag("integer");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("value", String.valueOf(v.value));
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
	 */
	@Override public final void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
		writeInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLeExpr(soot.jimple.LeExpr)
	 */
	@Override public final void caseLeExpr(final LeExpr v) {
		writeBinaryExpr("less than or equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
	 */
	@Override public final void caseLengthExpr(final LengthExpr v) {
		writeUnaryExpr("length", v);
	}

	/**
	 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
	 */
	@Override public final void caseLocal(final Local v) {
		try {
			xmlWriter.startTag("local_ref");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("localId", idGenerator.getIdForLocal(v, currMethod));
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
	 */
	@Override public final void caseLongConstant(final LongConstant v) {
		try {
			xmlWriter.startTag("long");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("value", String.valueOf(v.value));
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseLtExpr(soot.jimple.LtExpr)
	 */
	@Override public final void caseLtExpr(final LtExpr v) {
		writeBinaryExpr("less than", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseMulExpr(soot.jimple.MulExpr)
	 */
	@Override public final void caseMulExpr(final MulExpr v) {
		writeBinaryExpr("multiply", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNeExpr(soot.jimple.NeExpr)
	 */
	@Override public final void caseNeExpr(final NeExpr v) {
		writeBinaryExpr("not equal", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNegExpr(soot.jimple.NegExpr)
	 */
	@Override public final void caseNegExpr(final NegExpr v) {
		writeUnaryExpr("negation", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
	 */
	@Override public final void caseNewArrayExpr(final NewArrayExpr v) {
		try {
			xmlWriter.startTag("new_array");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("typeId", idGenerator.getIdForType(v.getBaseType()));
			writeDimensionSize(1, v.getSizeBox());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
	 */
	@Override public final void caseNewExpr(final NewExpr v) {
		try {
			xmlWriter.startTag("new");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("typeId", idGenerator.getIdForType(v.getType()));
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
	 */
	@Override public final void caseNewMultiArrayExpr(final NewMultiArrayExpr v) {
		final ArrayType _type = v.getBaseType();

		try {
			xmlWriter.startTag("new_multi_array");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("typeId", idGenerator.getIdForType(_type.baseType));
			xmlWriter.attribute("dimension", String.valueOf(_type.numDimensions));

			for (int _i = 0; _i < v.getSizeCount(); _i++) {
				writeDimensionSize(_i + 1, v.getSizeBox(_i));
			}
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
	 */
	@Override public final void caseNullConstant(@SuppressWarnings("unused") final NullConstant v) {
		try {
			xmlWriter.startTag("null");
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseOrExpr(soot.jimple.OrExpr)
	 */
	@Override public final void caseOrExpr(final OrExpr v) {
		writeBinaryExpr("binary or", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
	 */
	@Override public final void caseParameterRef(final ParameterRef v) {
		try {
			xmlWriter.startTag("parameter_ref");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("position", String.valueOf(v.getIndex()));
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseRemExpr(soot.jimple.RemExpr)
	 */
	@Override public final void caseRemExpr(final RemExpr v) {
		writeBinaryExpr("reminder", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseShlExpr(soot.jimple.ShlExpr)
	 */
	@Override public final void caseShlExpr(final ShlExpr v) {
		writeBinaryExpr("shift left", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseShrExpr(soot.jimple.ShrExpr)
	 */
	@Override public final void caseShrExpr(final ShrExpr v) {
		writeBinaryExpr("shift right", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
	 */
	@Override public final void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
		writeInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
	 */
	@Override public final void caseStaticFieldRef(final StaticFieldRef v) {
		try {
			xmlWriter.startTag("static_field_ref");
			xmlWriter.attribute("id", newId.toString());
			writeField(v.getField());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
	 */
	@Override public final void caseStaticInvokeExpr(final StaticInvokeExpr v) {
		writeInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
	 */
	@Override public final void caseStringConstant(final StringConstant v) {
		try {
			xmlWriter.startTag("string");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.cdata(AbstractXMLizer.encode(String.valueOf(v.value), xmlWriter.getEncoding()));
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseSubExpr(soot.jimple.SubExpr)
	 */
	@Override public final void caseSubExpr(final SubExpr v) {
		writeBinaryExpr("subtract", v);
	}

	/**
	 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
	 */
	@Override public final void caseThisRef(@SuppressWarnings("unused") final ThisRef v) {
		try {
			xmlWriter.startTag("this");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseUshrExpr(soot.jimple.UshrExpr)
	 */
	@Override public final void caseUshrExpr(final UshrExpr v) {
		writeBinaryExpr("unsigned shift right", v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
	 */
	@Override public final void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
		writeInvokeExpr(v);
	}

	/**
	 * @see soot.jimple.ExprSwitch#caseXorExpr(soot.jimple.XorExpr)
	 */
	@Override public final void caseXorExpr(final XorExpr v) {
		writeBinaryExpr("binary xor", v);
	}

	/**
	 * Process the given program point.
	 * 
	 * @param programPoint to be processed.
	 * @pre programPoint != null
	 */
	final void apply(final ValueBox programPoint) {
		final Object _temp = newId;
		newId = idGenerator.getIdForValueBox(programPoint, currStmt, currMethod);
		incrementTabs();
		programPoint.getValue().apply(this);
		decrementTabs();
		newId = _temp;
	}

	/**
	 * Sets the method whose values will be processed.
	 * 
	 * @param method whose values will be processed.
	 * @pre method != null
	 */
	final void setMethod(final SootMethod method) {
		currMethod = method;
	}

	/**
	 * Sets the statement whose values will be processed.
	 * 
	 * @param stmt whose values will be processed.
	 * @pre stmt != null
	 */
	final void setStmt(final Stmt stmt) {
		currStmt = stmt;
	}

	/**
	 * Sets the outputter into which xml data will be written into.
	 * 
	 * @param outputter via which xml data will be written into.
	 * @pre outputter != null
	 */
	final void setWriter(final XMLOutputter outputter) {
		xmlWriter = outputter;
	}

	/**
	 * Decrements the indentation.
	 */
	private void decrementTabs() {
		tabs.deleteCharAt(0);
	}

	/**
	 * Incrementst the indentation.
	 */
	private void incrementTabs() {
		tabs.append("\t");
	}

	/**
	 * Writes base information in XML. Base are primaries in field access, array access, and invocation expressions.
	 * 
	 * @param base to be xmlized.
	 * @pre base != null
	 */
	private void writeBase(final ValueBox base) {
		try {
			xmlWriter.startTag("base");
			apply(base);
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Writes a binary expression in XML.
	 * 
	 * @param operatorName is the name of the binary operator occurring in the expresssion.
	 * @param v is the expression.
	 * @pre operatorName != null and v != null
	 */
	private void writeBinaryExpr(final String operatorName, final BinopExpr v) {
		try {
			xmlWriter.startTag("binary_expr");
			xmlWriter.attribute("id", newId.toString());
			xmlWriter.attribute("op", operatorName);
			xmlWriter.startTag("left_op");
			apply(v.getOp1Box());
			xmlWriter.endTag();
			xmlWriter.startTag("right_op");
			apply(v.getOp2Box());
			xmlWriter.endTag();
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Writes array dimension size specification in a new array expression.
	 * 
	 * @param i is the dimension being specified.
	 * @param v is the size of the dimension.
	 */
	private void writeDimensionSize(final int i, final ValueBox v) {
		try {
			xmlWriter.startTag("size");
			xmlWriter.attribute("dimension", String.valueOf(i));
			apply(v);
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Writes the field in XML.
	 * 
	 * @param field to be xmlized.
	 * @pre field != null
	 */
	private void writeField(final SootField field) {
		try {
			xmlWriter.startTag("field_ref");
			xmlWriter.attribute("fieldId", idGenerator.getIdForField(field));
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Writes the invocation expression in XML.
	 * 
	 * @param v is the invocation expression.
	 * @throws IllegalArgumentException when input argument is not of any of these types: VirtualInvokeExpr, StaticInvokeExpr,
	 *             SpecialInvokeExpr, or InterfaceInvokeExpr.
	 * @pre name != null and v != null
	 */
	private void writeInvokeExpr(final InvokeExpr v) {
		String _name = "";

		if (v instanceof InterfaceInvokeExpr) {
			_name = "interface";
		} else if (v instanceof SpecialInvokeExpr) {
			_name = "special";
		} else if (v instanceof StaticInvokeExpr) {
			_name = "static";
		} else if (v instanceof VirtualInvokeExpr) {
			_name = "virtual";
		} else {
			throw new IllegalArgumentException(
					"v has to be of VirtualInvokeExpr, StaticInvokeExpr, SpecialInvokeExpr, or InterfaceInvokeExpr.");
		}

		try {
			xmlWriter.startTag("invoke_expr");
			xmlWriter.attribute("name", _name);
			xmlWriter.attribute("id", newId.toString());

			final SootMethod _method = v.getMethod();
			xmlWriter.startTag("method_ref");
			xmlWriter.attribute("methodId", idGenerator.getIdForMethod(_method));
			xmlWriter.endTag();

			if (v instanceof InstanceInvokeExpr) {
				writeBase(((InstanceInvokeExpr) v).getBaseBox());
			}

			if (v.getArgCount() > 0) {
				xmlWriter.startTag("arguments");

				for (int _i = 0; _i < v.getArgCount(); _i++) {
					apply(v.getArgBox(_i));
				}
				xmlWriter.endTag();
			}
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Writes a unary expression as XML.
	 * 
	 * @param operatorName is the name of the unary operator in the expression.
	 * @param value is the unary expression.
	 * @pre operatorName != null and value != null
	 */
	private void writeUnaryExpr(final String operatorName, final UnopExpr value) {
		try {
			xmlWriter.startTag("unary_expr");
			xmlWriter.attribute("op", operatorName);
			xmlWriter.attribute("id", newId.toString());
			apply(value.getOpBox());
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}
}

// End of File
