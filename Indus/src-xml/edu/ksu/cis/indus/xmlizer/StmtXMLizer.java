
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
			out.write("\t\t\t<assign_stmt  id=\"" + newId + "\">\n");
			out.write("\t\t\t\t<lhs>\n");
			valueXMLizer.apply(v.getLeftOpBox());
			out.write("\t\t\t\t</lhs>\n\t\t\t\t<rhs>\n");
			valueXMLizer.apply(v.getRightOpBox());
			out.write("\t\t\t\t</rhs>\n");
			out.write("\t\t\t</assign_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
	 */
	public final void caseBreakpointStmt(BreakpointStmt v) {
		try {
			out.write("\t\t\t<breakpoint_stmt id=\"" + newId + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
	 */
	public final void caseEnterMonitorStmt(EnterMonitorStmt v) {
		try {
			out.write("\t\t\t<entermonitor_stmt id=\"" + newId + "\">\n");
			valueXMLizer.apply(v.getOpBox());
			out.write("\t\t\t</entermonitor_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
	 */
	public final void caseExitMonitorStmt(ExitMonitorStmt v) {
		try {
			out.write("\t\t\t<exitmonitor_stmt id=\"" + newId + "\">\n");
			valueXMLizer.apply(v.getOpBox());
			out.write("\t\t\t</exitmonitor_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
	 */
	public final void caseGotoStmt(GotoStmt v) {
		try {
			out.write("\t\t\t<goto_stmt id=\"" + newId + "\" target=\""
				+ idGenerator.getIdForStmt((Stmt) v.getTarget(), currMethod) + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
	 */
	public final void caseIdentityStmt(IdentityStmt v) {
		try {
			out.write("\t\t\t<identity_stmt  id=\"" + newId + "\">\n");
			out.write("\t\t\t\t<lhs>\n");
			valueXMLizer.apply(v.getLeftOpBox());
			out.write("\t\t\t\t</lhs>\n\t\t\t\t<rhs>\n");
			valueXMLizer.apply(v.getRightOpBox());
			out.write("\t\t\t\t</rhs>\n");
			out.write("\t\t\t</identity_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
	 */
	public final void caseIfStmt(IfStmt v) {
		try {
			out.write("\t\t\t<if_stmt id=\"" + newId + " true_target id=\"" + idGenerator.getIdForStmt(v.getTarget(), currMethod)
				+ "\"/>\n");
			out.write("\t\t\t\t<condition>\n");
			valueXMLizer.apply(v.getConditionBox());
			out.write("\t\t\t\t</condition>\n");
			out.write("\t\t\t</if_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
	 */
	public final void caseInvokeStmt(InvokeStmt v) {
		try {
			out.write("\t\t\t<invoke_stmt id=\"" + newId + "\">\n");
			valueXMLizer.apply(v.getInvokeExprBox());
			out.write("\t\t\t</invoke_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
	 */
	public final void caseLookupSwitchStmt(LookupSwitchStmt v) {
		try {
			out.write("\t\t\t<lookupswitch_stmt id=\"" + newId + "\" defaultTargetId=\""
				+ idGenerator.getIdForStmt((Stmt) v.getDefaultTarget(), currMethod) + "\">\n");
			out.write("\t\t\t\t<key>\n");
			valueXMLizer.apply(v.getKeyBox());
			out.write("\t\t\t\t</key>\n");

			for (int i = 0; i < v.getTargetCount(); i++) {
				out.write("\t\t\t\t<case value=\"" + v.getLookupValue(i) + "\" targetId=\""
					+ idGenerator.getIdForStmt((Stmt) v.getTarget(i), currMethod) + "\"/>\n");
			}
			out.write("\t\t\t</lookupswitch_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
	 */
	public final void caseNopStmt(NopStmt v) {
		try {
			out.write("\t\t\t<nop_stmt id=\"" + newId + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
	 */
	public final void caseRetStmt(RetStmt v) {
		try {
			out.write("\t\t\t<ret_stmt id=\"" + newId + "\">\n");
			valueXMLizer.apply(v.getStmtAddressBox());
			out.write("\t\t\t</ret_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
	 */
	public final void caseReturnStmt(ReturnStmt v) {
		try {
			out.write("\t\t\t<return_stmt id=\"" + newId + "\">\n");
			valueXMLizer.apply(v.getOpBox());
			out.write("\t\t\t</return_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
	 */
	public final void caseReturnVoidStmt(ReturnVoidStmt v) {
		try {
			out.write("\t\t\t<returnvoid_stmt id=\"" + newId + "\"/>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
	 */
	public final void caseTableSwitchStmt(TableSwitchStmt v) {
		try {
			out.write("\t\t\t<tableswitch_stmt id=\"" + newId + "\" defaultTargetId=\""
				+ idGenerator.getIdForStmt((Stmt) v.getDefaultTarget(), currMethod) + "\"\n");
			out.write("\t\t\t\t<key>\n");
			valueXMLizer.apply(v.getKeyBox());
			out.write("\t\t\t\t</key>\n");

			for (int i = 0; i < v.getHighIndex() - v.getLowIndex(); i++) {
				out.write("\t\t\t\t<case value=\"" + i + "\" targetId=\""
					+ idGenerator.getIdForStmt((Stmt) v.getTarget(i), currMethod) + "\"/>\n");
			}
			out.write("\t\t\t</tableswitch_stmt>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
	 */
	public final void caseThrowStmt(ThrowStmt v) {
		try {
			out.write("\t\t\t<throw_stmt id=\"" + newId + "\">\n");
			valueXMLizer.apply(v.getOpBox());
			out.write("\t\t\t</throw_stmt>\n");
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
		newId = idGenerator.getIdForStmt(stmt, currMethod);
		valueXMLizer.setStmt(stmt);
		stmt.apply(this);
	}
}

/*
   ChangeLog:
   $Log$
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
