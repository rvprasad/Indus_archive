/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import soot.Body;
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
	 * This is the method whose statements are being processed.
	 */
	private SootMethod currMethod;

	/**
	 * The body of the method currently being processed.
	 */
	private Body currMethodBody;

	/**
	 * This provides the id to be used during xmlization.
	 * 
	 * @invariant idGenerator != null
	 */
	private final IJimpleIDGenerator idGenerator;

	/**
	 * The instance used to write xml data.
	 */
	private XMLOutputter xmlWriter;

	/**
	 * Creates a new JimpleStmtXMLizer object.
	 * 
	 * @param theValueXMLizer to be used for value xmlization.
	 * @param generator to be used for id generation.
	 * @pre theValueXMLizer != null and generator != null
	 */
	JimpleStmtXMLizer(final JimpleValueXMLizer theValueXMLizer, final IJimpleIDGenerator generator) {
		valueXMLizer = theValueXMLizer;
		idGenerator = generator;
	}

	/**
	 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
	 */
	@Override public void caseAssignStmt(final AssignStmt v) {
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
	@Override public void caseBreakpointStmt(final BreakpointStmt v) {
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
	@Override public void caseEnterMonitorStmt(final EnterMonitorStmt v) {
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
	@Override public void caseExitMonitorStmt(final ExitMonitorStmt v) {
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
	@Override public void caseGotoStmt(final GotoStmt v) {
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
	@Override public void caseIdentityStmt(final IdentityStmt v) {
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
	@Override public void caseIfStmt(final IfStmt v) {
		try {
			xmlWriter.startTag("if_stmt");
			xmlWriter.attribute("id", idGenerator.getIdForStmt(v, currMethod));
			xmlWriter.attribute("label", String.valueOf(!v.getBoxesPointingToThis().isEmpty()));
			xmlWriter.attribute("trueTargetId", idGenerator.getIdForStmt(v.getTarget(), currMethod));
			xmlWriter.attribute("falseTargetId", idGenerator.getIdForStmt((Stmt) currMethodBody.getUnits().getSuccOf(v),
					currMethod));
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
	@Override public void caseInvokeStmt(final InvokeStmt v) {
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
	@Override public void caseLookupSwitchStmt(final LookupSwitchStmt v) {
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
	@Override public void caseNopStmt(final NopStmt v) {
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
	@Override public void caseRetStmt(final RetStmt v) {
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
	@Override public void caseReturnStmt(final ReturnStmt v) {
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
	@Override public void caseReturnVoidStmt(final ReturnVoidStmt v) {
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
	@Override public void caseTableSwitchStmt(final TableSwitchStmt v) {
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
	@Override public void caseThrowStmt(final ThrowStmt v) {
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
	 * Processes the given statement.
	 * 
	 * @param stmt is the statement to be processed.
	 */
	void apply(final Stmt stmt) {
		valueXMLizer.setStmt(stmt);
		stmt.apply(this);
	}

	/**
	 * Sets the method in whose statemetnts will be processed.
	 * 
	 * @param method containing the statements to be processed.
	 * @pre method != null
	 */
	void setMethod(final SootMethod method) {
		currMethod = method;

		if (method.isConcrete()) {
			currMethodBody = method.retrieveActiveBody();
		} else {
			currMethodBody = null;
		}
		valueXMLizer.setMethod(method);
	}

	/**
	 * Sets the outputter into which xml data will be written into.
	 * 
	 * @param outputter via which xml data will be written into.
	 * @pre stream != null
	 */
	void setWriter(final XMLOutputter outputter) {
		xmlWriter = outputter;
		valueXMLizer.setWriter(outputter);
	}
}

// End of File
