
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

import soot.SootMethod;

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
public class StmtXMLizer
  extends AbstractStmtSwitch {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	ValueXMLizer valueXMLizer;

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
	private Writer out;

	/**
	 * Creates a new StmtXMLizer object.
	 *
	 * @param valueXML DOCUMENT ME!
	 * @param generator DOCUMENT ME!
	 */
	StmtXMLizer(final ValueXMLizer valueXML, final IJimpleIDGenerator generator) {
		valueXMLizer = valueXML;
		idGenerator = generator;
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
	 */
	public final void caseAssignStmt(AssignStmt v) {
		try {
			out.write("<assign_stmt  id=\"" + newId + "\">");
			out.write("<lhs>");
			valueXMLizer.apply(v.getLeftOp());
			out.write("</lhs><rhs>");
			valueXMLizer.apply(v.getRightOp());
			out.write("</rhs>");
			out.write("</assignstmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
	 */
	public final void caseBreakpointStmt(BreakpointStmt v) {
		try {
			out.write("<breakpoint_stmt id=\"" + newId + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
	 */
	public final void caseEnterMonitorStmt(EnterMonitorStmt v) {
		try {
			out.write("<entermonitor_stmt id=\"" + newId + "\">");
			valueXMLizer.apply(v.getOp());
			out.write("</entermonitor_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
	 */
	public final void caseExitMonitorStmt(ExitMonitorStmt v) {
		try {
			out.write("<exitmonitor_stmt id=\"" + newId + "\">");
			valueXMLizer.apply(v.getOp());
			out.write("</exitmonitor_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
	 */
	public final void caseGotoStmt(GotoStmt v) {
		try {
			out.write("<goto_stmt id=\"" + newId + "\" target=\""
				+ idGenerator.getIdForStmt((Stmt) v.getTarget(), currMethod) + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
	 */
	public final void caseIdentityStmt(IdentityStmt v) {
		try {
			out.write("<identity_stmt  id=\"" + newId + "\">");
			out.write("<lhs>");
			valueXMLizer.apply(v.getLeftOp());
			out.write("</lhs><rhs>");
			valueXMLizer.apply(v.getRightOp());
			out.write("</rhs>");
			out.write("</identity_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
	 */
	public final void caseIfStmt(IfStmt v) {
		try {
			out.write("<if_stmt id=\"" + newId + " true_target id=\"" + idGenerator.getIdForStmt(v.getTarget(), currMethod)
				+ "\"/>");
			out.write("<condition>");
			valueXMLizer.apply(v.getCondition());
			out.write("</condition>");
			out.write("</if_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
	 */
	public final void caseInvokeStmt(InvokeStmt v) {
		try {
			out.write("<invoke_stmt id=\"" + newId + "\">");
			valueXMLizer.apply(v.getInvokeExpr());
			out.write("</invoke_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
	 */
	public final void caseLookupSwitchStmt(LookupSwitchStmt v) {
		try {
			out.write("<lookupswitch_stmt id=\"" + newId + "\" defaultTargetId=\""
				+ idGenerator.getIdForStmt((Stmt) v.getDefaultTarget(), currMethod) + "\">");
			out.write("<key>");
			valueXMLizer.apply(v.getKey());
			out.write("</key>");

			for (int i = 0; i < v.getTargetCount(); i++) {
				out.write("<case value=\"" + v.getLookupValue(i) + "\" targetId=\""
					+ idGenerator.getIdForStmt((Stmt) v.getTarget(i), currMethod) + "\"/>");
			}
			out.write("</lookupswitch_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
	 */
	public final void caseNopStmt(NopStmt v) {
		try {
			out.write("<nop_stmt id=\"" + newId + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
	 */
	public final void caseRetStmt(RetStmt v) {
		try {
			out.write("<ret_stmt id=\"" + newId + "\">");
			valueXMLizer.apply(v.getStmtAddress());
			out.write("</ret_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
	 */
	public final void caseReturnStmt(ReturnStmt v) {
		try {
			out.write("<return_stmt id=\"" + newId + "\">");
			valueXMLizer.apply(v.getOp());
			out.write("</return_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
	 */
	public final void caseReturnVoidStmt(ReturnVoidStmt v) {
		try {
			out.write("<returnvoid_stmt id=\"" + newId + "\"/>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
	 */
	public final void caseTableSwitchStmt(TableSwitchStmt v) {
		try {
			out.write("<tableswitch_stmt id=\"" + newId + "\" defaultTargetId=\""
				+ idGenerator.getIdForStmt((Stmt) v.getDefaultTarget(), currMethod) + "\">");
			out.write("<key>");
			valueXMLizer.apply(v.getKey());
			out.write("</key>");

			for (int i = 0; i < v.getHighIndex() - v.getLowIndex(); i++) {
				out.write("<case value=\"" + i + "\" targetId=\""
					+ idGenerator.getIdForStmt((Stmt) v.getTarget(i), currMethod) + "\"/>");
			}
			out.write("</tableswitch_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
	 */
	public final void caseThrowStmt(ThrowStmt v) {
		try {
			out.write("<throw_stmt id=\"" + newId + "\">");
			valueXMLizer.apply(v.getOp());
			out.write("</throw_stmt>");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		valueXMLizer.setMethod(method);
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
		valueXMLizer.setWriter(stream);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 */
	void apply(final Stmt stmt) {
		newId = idGenerator.getNewStmtId(currMethod);
		idGenerator.resetValueCounter();
		valueXMLizer.setStmt(stmt);
		stmt.apply(this);
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
