
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

import org.znerd.xmlenc.XMLOutputter;

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


/**
 * This class provides the logic to xmlize Jimple statements.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class JimpleStmtXMLizer
  extends AbstractStmtSwitch {
	/**
	 * The xmlizer used to xmlize jimple <code>Value</code> types.
	 *
	 * @invariant valueXMLizer != null
	 */
	JimpleValueXMLizer valueXMLizer;

	/**
	 * This provides the id to be used during xmlization.
	 *
	 * @invariant idGenerator != null
	 */
	private final IJimpleIDGenerator idGenerator;

	/**
	 * This is the method whose statements are being processed.
	 */
	private SootMethod currMethod;

	/**
	 * The instance used to write xml data.
	 */
	private XMLOutputter xmlWriter;

	/**
	 * Creates a new JimpleStmtXMLizer object.
	 *
	 * @param theValueXMLizer to be used for value xmlization.
	 * @param generator to be used for id generation.
	 *
	 * @pre theValueXMLizer != null and generator != null
	 */
	JimpleStmtXMLizer(final JimpleValueXMLizer theValueXMLizer, final IJimpleIDGenerator generator) {
		valueXMLizer = theValueXMLizer;
		idGenerator = generator;
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
	 */
	public void caseAssignStmt(final AssignStmt v) {
		try {
			final boolean _notEmpty = !v.getBoxesPointingToThis().isEmpty();
			xmlWriter.startTag("assign_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(_notEmpty));
			xmlWriter.startTag("lhs");
			valueXMLizer.apply(v.getLeftOpBox());
			xmlWriter.endTag();
			xmlWriter.startTag("rhs");
			valueXMLizer.apply(v.getRightOpBox());
			xmlWriter.endTag();
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
	 */
	public void caseBreakpointStmt(final BreakpointStmt v) {
		try {
			xmlWriter.startTag("breakpoint_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
	 */
	public void caseEnterMonitorStmt(final EnterMonitorStmt v) {
		try {
			xmlWriter.startTag("entermonitor_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			valueXMLizer.apply(v.getOpBox());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
	 */
	public void caseExitMonitorStmt(final ExitMonitorStmt v) {
		try {
			xmlWriter.startTag("exitmonitor_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			valueXMLizer.apply(v.getOpBox());
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
	 */
	public void caseGotoStmt(final GotoStmt v) {
		try {
			xmlWriter.startTag("goto_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("target", idGenerator.getIdForStmt((Stmt) v.getTarget(), currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
	 */
	public void caseIdentityStmt(final IdentityStmt v) {
		try {
			xmlWriter.startTag("identity_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.startTag("lhs");
			valueXMLizer.apply(v.getLeftOpBox());
			xmlWriter.endTag();
			xmlWriter.startTag("rhs");
			valueXMLizer.apply(v.getRightOpBox());
			xmlWriter.endTag();
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
	 */
	public void caseIfStmt(final IfStmt v) {
		try {
			xmlWriter.startTag("if_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.attribute("truTargetId", idGenerator.getIdForStmt(v.getTarget(), currMethod));
			xmlWriter.startTag("condition");
			valueXMLizer.apply(v.getConditionBox());
			xmlWriter.endTag();
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
	 */
	public void caseInvokeStmt(final InvokeStmt v) {
		try {
			xmlWriter.startTag("invoke_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			valueXMLizer.apply(v.getInvokeExprBox());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
	 */
	public void caseLookupSwitchStmt(final LookupSwitchStmt v) {
		try {
			xmlWriter.startTag("lookupswitch_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.attribute("defaultTargetId", idGenerator.getIdForStmt((Stmt) v.getDefaultTarget(), currMethod));
			xmlWriter.startTag("key");
			valueXMLizer.apply(v.getKeyBox());
			xmlWriter.endTag();

			for (int _i = 0; _i < v.getTargetCount(); _i++) {
				xmlWriter.startTag("case");
				xmlWriter.attribute("value", String.valueOf(v.getLookupValue(_i)));
				xmlWriter.attribute("targetId", idGenerator.getIdForStmt((Stmt) v.getTarget(_i), currMethod));
				xmlWriter.endTag();
			}
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
	 */
	public void caseNopStmt(final NopStmt v) {
		try {
			xmlWriter.startTag("nop_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
	 */
	public void caseRetStmt(final RetStmt v) {
		try {
			xmlWriter.startTag("ret_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			valueXMLizer.apply(v.getStmtAddressBox());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
	 */
	public void caseReturnStmt(final ReturnStmt v) {
		try {
			xmlWriter.startTag("return_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			valueXMLizer.apply(v.getOpBox());
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
	 */
	public void caseReturnVoidStmt(final ReturnVoidStmt v) {
		try {
			xmlWriter.startTag("returnvoid_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
	 */
	public void caseTableSwitchStmt(final TableSwitchStmt v) {
		try {
			xmlWriter.startTag("tableswitch_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.attribute("defaultTargetId", idGenerator.getIdForStmt((Stmt) v.getDefaultTarget(), currMethod));
			xmlWriter.startTag("key");
			valueXMLizer.apply(v.getKeyBox());
			xmlWriter.endTag();

			for (int _i = 0; _i < v.getHighIndex() - v.getLowIndex(); _i++) {
				xmlWriter.startTag("case");
				xmlWriter.attribute("value", String.valueOf(_i));
				xmlWriter.attribute("targetId", idGenerator.getIdForStmt((Stmt) v.getTarget(_i), currMethod));
				xmlWriter.endTag();
			}
			xmlWriter.endTag();
		} catch (final IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
	 */
	public void caseThrowStmt(final ThrowStmt v) {
		try {
			xmlWriter.startTag("throw_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			valueXMLizer.apply(v.getOpBox());
			xmlWriter.endTag();
		} catch (IOException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Sets the method in whose statemetnts will be processed.
	 *
	 * @param method containing the statements to be processed.
	 *
	 * @pre method != null
	 */
	void setMethod(final SootMethod method) {
		currMethod = method;
		valueXMLizer.setMethod(method);
	}

	/**
	 * Sets the outputter into which xml data will be written into.
	 *
	 * @param outputter via which xml data will be written into.
	 *
	 * @pre stream != null
	 */
	void setWriter(final XMLOutputter outputter) {
		xmlWriter = outputter;
		valueXMLizer.setWriter(outputter);
	}

	/**
	 * Processes the given statement.
	 *
	 * @param stmt is the statement to be processed.
	 */
	void apply(final Stmt stmt) {
		valueXMLizer.setStmt(stmt);
		stmt.apply(this);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/05/09 08:24:08  venku
   - all xmlizers use xmlenc to write xml data.
   Revision 1.2  2004/05/06 09:31:00  venku
   - used xmlenc library to write xml instead of manual tag generation.
   Revision 1.1  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.10  2003/12/02 11:36:16  venku
   - coding convention.
   Revision 1.9  2003/12/02 09:42:24  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.8  2003/12/02 01:30:58  venku
   - coding conventions and formatting.
   Revision 1.7  2003/11/28 09:39:22  venku
   - added support to indicate labels.
   Revision 1.6  2003/11/24 06:45:23  venku
   - corrected xml encoding errors along with tag name emission errors.
   Revision 1.5  2003/11/24 06:27:18  venku
   - closing tag of assign element was incorrent. FIXED.
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
