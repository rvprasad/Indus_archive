
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

import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

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
public class StmtXMLizer
  extends AbstractStmtSwitch {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	ValueXMLizer valueXMLizer;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private IJimpleIDGenerator idGenerator;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private Object newId;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private StringWriter out;

	/**
	 * Creates a new StmtXMLizer object.
	 *
	 * @param valueXML DOCUMENT ME!
	 */
	StmtXMLizer(final ValueXMLizer valueXML) {
		valueXMLizer = valueXML;
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
	 */
	public final void caseAssignStmt(AssignStmt v) {
		out.write("<assign_stmt  id=\"" + newId + "\">");
		out.write("<lhs>");
		v.getLeftOp().apply(valueXMLizer);
		out.write("</lhs><rhs>");
		v.getRightOp().apply(valueXMLizer);
		out.write("</rhs>");
		out.write("</assignstmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
	 */
	public final void caseBreakpointStmt(BreakpointStmt v) {
		out.write("<breakpoint_stmt id=\"" + newId + "\">");
		v.apply(valueXMLizer);
		out.write("</breakpoint_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
	 */
	public final void caseEnterMonitorStmt(EnterMonitorStmt v) {
		out.write("<entermonitor_stmt id=\"" + newId + "\">");
		v.getOp().apply(valueXMLizer);
		out.write("</entermonitor_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
	 */
	public final void caseExitMonitorStmt(ExitMonitorStmt v) {
		out.write("<exitmonitor_stmt id=\"" + newId + "\">");
		v.getOp().apply(valueXMLizer);
		out.write("</exitmonitor_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
	 */
	public final void caseGotoStmt(GotoStmt v) {
		out.write("<goto_stmt id=\"" + newId + "\" target=\"" + idGenerator.getIdForStmt((Stmt) v.getTarget()) + "\"/>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
	 */
	public final void caseIdentityStmt(IdentityStmt v) {
		out.write("<identity_stmt  id=\"" + newId + "\">");
		out.write("<lhs>");
		v.getLeftOp().apply(valueXMLizer);
		out.write("</lhs><rhs>");
		v.getRightOp().apply(valueXMLizer);
		out.write("</rhs>");
		out.write("</identity_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
	 */
	public final void caseIfStmt(IfStmt v) {
		out.write("<if_stmt id=\"" + newId + " true_target id=\"" + idGenerator.getIdForStmt(v.getTarget())
			+ "\" false_target id=\"" + idGenerator.getIdForNextStmt() + "\"/>");
		out.write("<condition>");
		v.getCondition().apply(valueXMLizer);
		out.write("</condition>");
		out.write("</if_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
	 */
	public final void caseInvokeStmt(InvokeStmt v) {
		out.write("<invoke_stmt id=\"" + newId + "\">");
		v.getInvokeExpr().apply(valueXMLizer);
		out.write("</invoke_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
	 */
	public final void caseLookupSwitchStmt(LookupSwitchStmt v) {
		out.write("<lookupswitch_stmt id=\"" + newId + "\" defaultTargetId=\""
			+ idGenerator.getIdForStmt((Stmt) v.getDefaultTarget()) + "\">");
		out.write("<key>");
		v.getKey().apply(valueXMLizer);
		out.write("</key>");

		for (int i = 0; i <= v.getTargetCount(); i++) {
			out.write("<case value=\"" + v.getLookupValue(i) + "\" targetId=\""
				+ idGenerator.getIdForStmt((Stmt) v.getTarget(i)) + "\"/>");
		}
		out.write("</lookupswitch_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
	 */
	public final void caseNopStmt(NopStmt v) {
		out.write("<nop_stmt id=\"" + newId + "\"/>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
	 */
	public final void caseRetStmt(RetStmt v) {
		out.write("<ret_stmt id=\"" + newId + "\">");
		v.getStmtAddress().apply(valueXMLizer);
		out.write("</ret_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
	 */
	public final void caseReturnStmt(ReturnStmt v) {
		out.write("<return_stmt id=\"" + newId + "\">");
		v.getOp().apply(valueXMLizer);
		out.write("</return_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
	 */
	public final void caseReturnVoidStmt(ReturnVoidStmt v) {
		out.write("<returnvoid_stmt id=\"" + newId + "\"/>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
	 */
	public final void caseTableSwitchStmt(TableSwitchStmt v) {
		out.write("<tableswitch_stmt id=\"" + newId + "\" defaultTargetId=\""
			+ idGenerator.getIdForStmt((Stmt) v.getDefaultTarget()) + "\">");
		out.write("<key>");
		v.getKey().apply(valueXMLizer);
		out.write("</key>");

		for (int i = v.getLowIndex(); i <= v.getHighIndex(); i++) {
			out.write("<case value=\"" + i + "\" targetId=\"" + idGenerator.getIdForStmt((Stmt) v.getTarget(i)) + "\"/>");
		}
		out.write("</tableswitch_stmt>");
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
	 */
	public final void caseThrowStmt(ThrowStmt v) {
		out.write("<throw_stmt id=\"" + newId + "\">");
		v.getOp().apply(valueXMLizer);
		out.write("</throw_stmt>");
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param generator
	 */
	void setGenerator(final IJimpleIDGenerator generator) {
		idGenerator = generator;
	}

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
	 * @param stmt DOCUMENT ME!
	 */
	void apply(final Stmt stmt) {
		newId = idGenerator.getNewStmtId();
		stmt.apply(this);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/06 10:01:25  venku
   - created support for xmlizing Jimple in a customizable manner.
 */
