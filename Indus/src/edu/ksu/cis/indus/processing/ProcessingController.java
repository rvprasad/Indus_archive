
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

package edu.ksu.cis.indus.processing;

import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AddExpr;
import soot.jimple.AndExpr;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.BreakpointStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.DivExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.EqExpr;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GotoStmt;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LeExpr;
import soot.jimple.LengthExpr;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.LtExpr;
import soot.jimple.MulExpr;
import soot.jimple.NeExpr;
import soot.jimple.NegExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.OrExpr;
import soot.jimple.ParameterRef;
import soot.jimple.RemExpr;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.ShlExpr;
import soot.jimple.ShrExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.SubExpr;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.UnopExpr;
import soot.jimple.UshrExpr;
import soot.jimple.VirtualInvokeExpr;
import soot.jimple.XorExpr;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class controls the post processing for an analysis.  The analyses such as FA are very low-level.  The information is
 * raw.  This needs to be massaged via post processing.  Each post processor can registered interest in particular types of
 * AST chunks.  The controller will walk over the analyzed system and call the registered post processors. The post
 * processors then collect information from the analysis in form which is more accessible to the other applications. This
 * visitor will notify the interested post processors with the given AST node and then visit it's children.
 * 
 * <p>
 * Please note that the processor should be registered/unregistered separately for interface-level (class/method)  processing
 * and functional (method-body) processing.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ProcessingController {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ProcessingController.class);

	/**
	 * A collection of all possible Jimple statement types for which a processor can register interest.
	 */
	public static final Collection STMT_CLASSES;

	static {
		Collection t = new HashSet();
		t.add(AssignStmt.class);
		t.add(BreakpointStmt.class);
		t.add(EnterMonitorStmt.class);
		t.add(ExitMonitorStmt.class);
		t.add(GotoStmt.class);
		t.add(IdentityStmt.class);
		t.add(IfStmt.class);
		t.add(InvokeStmt.class);
		t.add(LookupSwitchStmt.class);
		t.add(NopStmt.class);
		t.add(RetStmt.class);
		t.add(ReturnVoidStmt.class);
		t.add(ReturnStmt.class);
		t.add(TableSwitchStmt.class);
		t.add(ThrowStmt.class);

		STMT_CLASSES = Collections.unmodifiableCollection(t);

		t = new HashSet();
		t.add(AddExpr.class);
		t.add(AndExpr.class);
		t.add(ArrayRef.class);
		t.add(CastExpr.class);
		t.add(CaughtExceptionRef.class);
		t.add(CmpExpr.class);
		t.add(CmpgExpr.class);
		t.add(CmplExpr.class);
		t.add(DivExpr.class);
		t.add(DoubleConstant.class);
		t.add(EqExpr.class);
		t.add(FloatConstant.class);
		t.add(GeExpr.class);
		t.add(GtExpr.class);
		t.add(InstanceFieldRef.class);
		t.add(InstanceOfExpr.class);
		t.add(IntConstant.class);
		t.add(InterfaceInvokeExpr.class);
		t.add(LeExpr.class);
		t.add(LengthExpr.class);
		t.add(Local.class);
		t.add(LongConstant.class);
		t.add(LtExpr.class);
		t.add(MulExpr.class);
		t.add(NeExpr.class);
		t.add(NegExpr.class);
		t.add(NewArrayExpr.class);
		t.add(NewExpr.class);
		t.add(NewMultiArrayExpr.class);
		t.add(NullConstant.class);
		t.add(OrExpr.class);
		t.add(ParameterRef.class);
		t.add(RemExpr.class);
		t.add(ShlExpr.class);
		t.add(ShrExpr.class);
		t.add(SpecialInvokeExpr.class);
		t.add(StaticFieldRef.class);
		t.add(StaticInvokeExpr.class);
		t.add(StringConstant.class);
		t.add(SubExpr.class);
		t.add(ThisRef.class);
		t.add(UshrExpr.class);
		t.add(VirtualInvokeExpr.class);
		t.add(XorExpr.class);

		VALUE_CLASSES = Collections.unmodifiableCollection(t);
	}

	/**
	 * A collection of all possible Jimple value types for which a processor can register interest.
	 */
	public static final Collection VALUE_CLASSES;

	/**
	 * The collection of processors registered with this controller to process interfaces (class/method).   This maintains
	 * the insertion order.
	 *
	 * @invariant interfaceProcessors->forall(o | o.isOclKindOf(IProcessor))
	 */
	protected final Collection interfaceProcessors = new ArrayList();

	/**
	 * The context in which the AST chunk is visited during post processing.
	 */
	protected Context context = new Context();

	/**
	 * This maps a class to the post processors interested in processing the analysis information pertaining to AST nodes of
	 * class type.
	 *
	 * @invariant class2processors.oclIsKindOf(Map(Class, Set(IProcessors)))
	 */
	protected final Map class2processors = new HashMap();

	/**
	 * This walks over the statements for processing.
	 */
	protected final StmtSwitcher stmtSwitcher = new StmtSwitcher(new ValueSwitcher());

	/**
	 * This indicates if statements are being processed.
	 */
	boolean processStmts;

	/**
	 * This indicates if values are being processed.
	 */
	boolean processValues;

	/**
	 * This defines the environment in which the processing runs.
	 */
	private IEnvironment env;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IProcessingFilter processingFilter;

	/**
	 * This class visits the statements of the methods and calls the call-back methods of the registered processors.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class StmtSwitcher
	  extends AbstractStmtSwitch {
		/**
		 * This walks expressions in the statement.
		 */
		private final ValueSwitcher valueSwitcher;

		/**
		 * Creates a new StmtSwitcher object.
		 *
		 * @param vs is the expressions processor.
		 */
		StmtSwitcher(final ValueSwitcher vs) {
			this.valueSwitcher = vs;
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			if (processStmts) {
				defaultCase(AssignStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getLeftOpBox());
				stmt.getLeftOp().apply(valueSwitcher);
				context.setProgramPoint(stmt.getRightOpBox());
				stmt.getRightOp().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
		 */
		public void caseBreakpointStmt(final BreakpointStmt stmt) {
			if (processStmts) {
				defaultCase(BreakpointStmt.class, stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
		 */
		public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
			if (processStmts) {
				defaultCase(EnterMonitorStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getOpBox());
				stmt.getOp().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
		 */
		public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
			if (processStmts) {
				defaultCase(ExitMonitorStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getOpBox());
				stmt.getOp().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
		 */
		public void caseGotoStmt(final GotoStmt stmt) {
			if (processStmts) {
				defaultCase(GotoStmt.class, stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			if (processStmts) {
				defaultCase(IdentityStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getLeftOpBox());
				stmt.getLeftOp().apply(valueSwitcher);
				context.setProgramPoint(stmt.getRightOpBox());
				stmt.getRightOp().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
		 */
		public void caseIfStmt(final IfStmt stmt) {
			if (processStmts) {
				defaultCase(IfStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getConditionBox());
				stmt.getCondition().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			if (processStmts) {
				defaultCase(InvokeStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getInvokeExprBox());
				stmt.getInvokeExpr().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
		 */
		public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
			if (processStmts) {
				defaultCase(LookupSwitchStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getKeyBox());
				stmt.getKey().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
		 */
		public void caseNopStmt(final NopStmt stmt) {
			if (processStmts) {
				defaultCase(NopStmt.class, stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
		 */
		public void caseRetStmt(final RetStmt stmt) {
			if (processStmts) {
				defaultCase(RetStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getStmtAddressBox());
				stmt.getStmtAddress().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			if (processStmts) {
				defaultCase(ReturnStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getOpBox());
				stmt.getOp().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
		 */
		public void caseReturnVoidStmt(final ReturnVoidStmt stmt) {
			if (processStmts) {
				defaultCase(ReturnVoidStmt.class, stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
		 */
		public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
			if (processStmts) {
				defaultCase(TableSwitchStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getKeyBox());
				stmt.getKey().apply(valueSwitcher);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			if (processStmts) {
				defaultCase(ThrowStmt.class, stmt);
			}

			if (processValues) {
				context.setProgramPoint(stmt.getOpBox());
				stmt.getOp().apply(valueSwitcher);
			}
		}

		/**
		 * Calls the processors interested in processing objects of type <code>objClass</code>.
		 *
		 * @param objClass is the type of <code>o</code>.
		 * @param o the AST INode to be processed.
		 */
		public void defaultCase(final Class objClass, final Object o) {
			Collection temp = (Collection) class2processors.get(objClass);

			if (temp != null) {
				Stmt stmt = (Stmt) o;

				for (Iterator i = temp.iterator(); i.hasNext();) {
					IProcessor pp = (IProcessor) i.next();
					pp.callback(stmt, context);
				}
			}
		}
	}


	/**
	 * This class walks the expressions and calls the call-methods of the registered processors.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class ValueSwitcher
	  extends AbstractJimpleValueSwitch {
		/**
		 * @see soot.jimple.ExprSwitch#caseAddExpr(soot.jimple.AddExpr)
		 */
		public void caseAddExpr(AddExpr v) {
			defaultCase(AddExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseAndExpr(soot.jimple.AndExpr)
		 */
		public void caseAndExpr(AndExpr v) {
			defaultCase(AndExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
		 */
		public void caseArrayRef(ArrayRef v) {
			defaultCase(ArrayRef.class, v);
			context.setProgramPoint(v.getBaseBox());
			v.getBase().apply(this);
			context.setProgramPoint(v.getIndexBox());
			v.getIndex().apply(this);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
		 */
		public void caseCastExpr(CastExpr v) {
			defaultCase(CastExpr.class, v);
			context.setProgramPoint(v.getOpBox());
			v.getOp().apply(this);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef)
		 */
		public void caseCaughtExceptionRef(CaughtExceptionRef v) {
			defaultCase(CaughtExceptionRef.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmpExpr(soot.jimple.CmpExpr)
		 */
		public void caseCmpExpr(CmpExpr v) {
			defaultCase(CmpExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmpgExpr(soot.jimple.CmpgExpr)
		 */
		public void caseCmpgExpr(CmpgExpr v) {
			defaultCase(CmpgExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmplExpr(soot.jimple.CmplExpr)
		 */
		public void caseCmplExpr(CmplExpr v) {
			defaultCase(CmplExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseDivExpr(soot.jimple.DivExpr)
		 */
		public void caseDivExpr(DivExpr v) {
			defaultCase(DivExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
		 */
		public void caseDoubleConstant(DoubleConstant v) {
			defaultCase(DoubleConstant.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseEqExpr(soot.jimple.EqExpr)
		 */
		public void caseEqExpr(EqExpr v) {
			defaultCase(EqExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
		 */
		public void caseFloatConstant(FloatConstant v) {
			defaultCase(FloatConstant.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseGeExpr(soot.jimple.GeExpr)
		 */
		public void caseGeExpr(GeExpr v) {
			defaultCase(GeExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseGtExpr(soot.jimple.GtExpr)
		 */
		public void caseGtExpr(GtExpr v) {
			defaultCase(GtExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(InstanceFieldRef v) {
			defaultCase(InstanceFieldRef.class, v);
			context.setProgramPoint(v.getBaseBox());
			v.getBase().apply(this);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
		 */
		public void caseInstanceOfExpr(InstanceOfExpr v) {
			defaultCase(InstanceOfExpr.class, v);
			context.setProgramPoint(v.getOpBox());
			v.getOp().apply(this);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
		 */
		public void caseIntConstant(IntConstant v) {
			defaultCase(IntConstant.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
		 */
		public void caseInterfaceInvokeExpr(InterfaceInvokeExpr v) {
			defaultCase(InterfaceInvokeExpr.class, v);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLeExpr(soot.jimple.LeExpr)
		 */
		public void caseLeExpr(LeExpr v) {
			defaultCase(LeExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
		 */
		public void caseLengthExpr(LengthExpr v) {
			defaultCase(LengthExpr.class, v);
			processUnaryExpr(v);
		}

		/**
		 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
		 */
		public void caseLocal(Local v) {
			defaultCase(Local.class, v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
		 */
		public void caseLongConstant(LongConstant v) {
			defaultCase(LongConstant.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLtExpr(soot.jimple.LtExpr)
		 */
		public void caseLtExpr(LtExpr v) {
			defaultCase(LtExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseMulExpr(soot.jimple.MulExpr)
		 */
		public void caseMulExpr(MulExpr v) {
			defaultCase(MulExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNeExpr(soot.jimple.NeExpr)
		 */
		public void caseNeExpr(NeExpr v) {
			defaultCase(NeExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNegExpr(soot.jimple.NegExpr)
		 */
		public void caseNegExpr(NegExpr v) {
			defaultCase(NegExpr.class, v);
			processUnaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
		 */
		public void caseNewArrayExpr(NewArrayExpr v) {
			defaultCase(NewArrayExpr.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
		 */
		public void caseNewExpr(NewExpr v) {
			defaultCase(NewExpr.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
		 */
		public void caseNewMultiArrayExpr(NewMultiArrayExpr v) {
			defaultCase(NewMultiArrayExpr.class, v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
		 */
		public void caseNullConstant(NullConstant v) {
			defaultCase(NullConstant.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseOrExpr(soot.jimple.OrExpr)
		 */
		public void caseOrExpr(OrExpr v) {
			defaultCase(OrExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(ParameterRef v) {
			defaultCase(ParameterRef.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseRemExpr(soot.jimple.RemExpr)
		 */
		public void caseRemExpr(RemExpr v) {
			defaultCase(RemExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseShlExpr(soot.jimple.ShlExpr)
		 */
		public void caseShlExpr(ShlExpr v) {
			defaultCase(ShlExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseShrExpr(soot.jimple.ShrExpr)
		 */
		public void caseShrExpr(ShrExpr v) {
			defaultCase(ShrExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(SpecialInvokeExpr v) {
			defaultCase(SpecialInvokeExpr.class, v);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
		 */
		public void caseStaticFieldRef(StaticFieldRef v) {
			defaultCase(StaticFieldRef.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
		 */
		public void caseStaticInvokeExpr(StaticInvokeExpr v) {
			defaultCase(StaticInvokeExpr.class, v);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
		 */
		public void caseStringConstant(StringConstant v) {
			defaultCase(StringConstant.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSubExpr(soot.jimple.SubExpr)
		 */
		public void caseSubExpr(SubExpr v) {
			defaultCase(SubExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
		 */
		public void caseThisRef(ThisRef v) {
			defaultCase(ThisRef.class, v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseUshrExpr(soot.jimple.UshrExpr)
		 */
		public void caseUshrExpr(UshrExpr v) {
			defaultCase(UshrExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
		 */
		public void caseVirtualInvokeExpr(VirtualInvokeExpr v) {
			defaultCase(VirtualInvokeExpr.class, v);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseXorExpr(soot.jimple.XorExpr)
		 */
		public void caseXorExpr(XorExpr v) {
			defaultCase(XorExpr.class, v);
			processBinaryExpr(v);
		}

		/**
		 * Calls the processors interested in processing object of type <code>objClass</code>.
		 *
		 * @param objClass is the type of <code>o</code>
		 * @param o the AST node to be processed.
		 */
		public void defaultCase(final Class objClass, final Object o) {
			Collection temp = (Collection) class2processors.get(objClass);

			if (temp != null) {
				for (Iterator i = temp.iterator(); i.hasNext();) {
					IProcessor pp = (IProcessor) i.next();
					pp.callback(context.getProgramPoint(), context);
				}
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param v DOCUMENT ME!
		 */
		private void processBinaryExpr(BinopExpr v) {
			context.setProgramPoint(v.getOp1Box());
			v.getOp1().apply(this);
			context.setProgramPoint(v.getOp1Box());
			v.getOp2().apply(this);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 *
		 * @param v DOCUMENT ME!
		 */
		private void processInvokeExpr(InvokeExpr v) {
			if (v instanceof InstanceInvokeExpr) {
				context.setProgramPoint(((InstanceInvokeExpr) v).getBaseBox());
				((InstanceInvokeExpr) v).getBase().apply(this);
			}

			for (int i = 0; i < v.getArgCount(); i++) {
				context.setProgramPoint(v.getArgBox(i));
				v.getArg(i).apply(this);
			}
		}

		/**
		 * DOCUMENT ME! <p></p>
		 *
		 * @param v DOCUMENT ME!
		 */
		private void processUnaryExpr(UnopExpr v) {
			context.setProgramPoint(v.getOpBox());
			v.getOp().apply(this);
		}
	}

	/**
	 * Sets the environment which provides
	 *
	 * @param environment an instance of the FA.
	 */
	public void setEnvironment(final IEnvironment environment) {
		env = environment;
	}

	/**
	 * Controls the processing activity.
	 */
	public final void process() {
		Collection processors = new HashSet();
		processors.addAll(interfaceProcessors);

		for (Iterator i = class2processors.values().iterator(); i.hasNext();) {
			processors.addAll((Collection) i.next());
		}

		initializeProcessors(processors);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: processing classes");
		}

		for (Iterator i = processors.iterator(); i.hasNext();) {
			((IProcessor) i.next()).processingBegins();
		}

		processStmts = !CollectionUtils.intersection(class2processors.keySet(), STMT_CLASSES).isEmpty();
		processValues = !CollectionUtils.intersection(class2processors.keySet(), VALUE_CLASSES).isEmpty();
		processClasses(env.getClasses());

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: processing classes");
			LOGGER.info("BEGIN: consolidation");
		}

		for (Iterator i = processors.iterator(); i.hasNext();) {
			((IProcessor) i.next()).consolidate();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: consolidation");
		}
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param theFilter ME!
	 */
	public void setProcessingFilter(final IProcessingFilter theFilter) {
		processingFilter = theFilter;
	}

	/**
	 * Registers the processor.  It indicates that the processor is interested in processing AST chunk of type
	 * <code>interest</code>.
	 *
	 * @param interest the class of AST node in which the <code>processor</code> is interested.
	 * @param processor the instance of processor.
	 */
	public void register(final Class interest, final IProcessor processor) {
		Set temp = (Set) class2processors.get(interest);

		if (temp == null) {
			temp = new HashSet();
			class2processors.put(interest, temp);
		}
		temp.add(processor);
	}

	/**
	 * Registers the processor for class, fields, and method interface processing only.
	 *
	 * @param processor the instance of processor.
	 */
	public void register(final IProcessor processor) {
		if (!interfaceProcessors.contains(processor)) {
			interfaceProcessors.add(processor);
		}
	}

	/**
	 * Registers the processor.  It indicates that the processor is interested in processing AST chunk of statement type.
	 * Please refer to <code>STMT_CLASSES</code> for the actual types.
	 *
	 * @param processor the instance of processor.
	 */
	public void registerForAllStmts(IProcessor processor) {
		for (Iterator i = ProcessingController.STMT_CLASSES.iterator(); i.hasNext();) {
			register((Class) i.next(), processor);
		}
	}

	/**
	 * Registers the processor.  It indicates that the processor is interested in processing AST chunk of value type. Please
	 * refer to <code>VALUE_CLASSES</code> for the actual types.
	 *
	 * @param processor the instance of processor.
	 */
	public void registerForAllValues(IProcessor processor) {
		for (Iterator i = ProcessingController.VALUE_CLASSES.iterator(); i.hasNext();) {
			register((Class) i.next(), processor);
		}
	}

	/**
	 * Unregisters the processor.  It indicates that the processor is no longer interested in processing AST chunk of type
	 * <code>interest</code>.
	 *
	 * @param interest the class of AST node in which the <code>processor</code> is interested.
	 * @param processor the instance of processor.
	 *
	 * @throws IllegalArgumentException when there are no processors who have registered to process <code>interest</code>.
	 */
	public void unregister(final Class interest, final IProcessor processor) {
		Set temp = (Set) class2processors.get(interest);

		if (temp == null) {
			throw new IllegalArgumentException("There are no processors registered  for " + interest.getName());
		}
		temp.remove(processor);
	}

	/**
	 * Unregisters the processor for class and method interface processing only.
	 *
	 * @param processor the instance of processor.
	 */
	public void unregister(final IProcessor processor) {
		interfaceProcessors.remove(processor);
	}

	/**
	 * Unregisters the processor. It indicates that the processor is not interested in processing the statement types. Please
	 * refer to <code>STMT_CLASSES</code> for the actual types.
	 *
	 * @param processor the instance of processor.
	 */
	public void unregisterForAllStmts(IProcessor processor) {
		for (Iterator i = ProcessingController.STMT_CLASSES.iterator(); i.hasNext();) {
			unregister((Class) i.next(), processor);
		}
	}

	/**
	 * Unregisters the processor. It indicates that the processor is not interested in processing the value types. Please
	 * refer to <code>VALUE_CLASSES</code> for the actual types.
	 *
	 * @param processor the instance of processor.
	 */
	public void unregisterForAllValues(IProcessor processor) {
		for (Iterator i = ProcessingController.VALUE_CLASSES.iterator(); i.hasNext();) {
			unregister((Class) i.next(), processor);
		}
	}

	/**
	 * Initializes the processors before processing the system.
	 *
	 * @param processors to be initialized.
	 *
	 * @pre processors != null
	 */
	protected void initializeProcessors(final Collection processors) {
	}

	/**
	 * Controls the processing of class level entities.
	 *
	 * @param theClasses to be processed.
	 *
	 * @pre theClasses != null and theClasses.oclIsKindOf(Collection(SootClass))
	 */
	protected void processClasses(final Collection theClasses) {
		Collection classes;

		if (processingFilter == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Performance may be hit as processing filter is not set.");
			}
			classes = theClasses;
		} else {
			classes = processingFilter.filterClasses(theClasses);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Classes to be processed:\n" + classes);
		}

		for (Iterator i = classes.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing class " + sc);
			}

			for (Iterator k = interfaceProcessors.iterator(); k.hasNext();) {
				IProcessor pp = (IProcessor) k.next();
				pp.callback(sc);

				for (Iterator j = sc.getFields().iterator(); j.hasNext();) {
					SootField field = (SootField) j.next();
					pp.callback(field);
				}
			}
			processMethods(sc.getMethods());
		}
	}

	/**
	 * Controls the processing of methods and their bodies.
	 *
	 * @param theMethods to be processed.
	 *
	 * @pre theMethods != null and theMethods.oclIsKindOf(Collection(SootMethod))
	 */
	protected void processMethods(final Collection theMethods) {
		Collection methods;

		if (processingFilter == null) {
			methods = theMethods;
		} else {
			methods = processingFilter.filterMethods(theMethods);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Methods to be processed:\n" + methods);
		}

		List sl = new ArrayList();

		for (Iterator j = methods.iterator(); j.hasNext();) {
			SootMethod sm = (SootMethod) j.next();
			context.setRootMethod(sm);

			for (Iterator k = interfaceProcessors.iterator(); k.hasNext();) {
				((IProcessor) k.next()).callback(sm);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing method " + sm);
			}

			if (processStmts || processValues) {
				if (sm.isConcrete()) {
					try {
						sl.clear();
						sl.addAll(sm.retrieveActiveBody().getUnits());

						for (Iterator k = sl.iterator(); k.hasNext();) {
							Stmt stmt = (Stmt) k.next();
							context.setStmt(stmt);
							stmt.apply(stmtSwitcher);
						}
					} catch (RuntimeException e) {
						LOGGER.warn("Well, exception while processing statements of a method may mean the processor does not"
							+ " recognize the given method or it's parts or method has not stored in jimple representation. : "
							+ sm.getSignature(), e);
					}
				} else {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info(sm + " is not a concrete method.  Hence, it's body could not be retrieved.");
					}
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.15  2003/11/30 00:21:11  venku
   - methodFilter fields was removed.
   - error in logic while filtering methods. FIXED.
   Revision 1.14  2003/11/30 00:10:17  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.13  2003/11/17 15:58:58  venku
   - coding conventions.
   Revision 1.12  2003/11/17 15:42:49  venku
   - changed the signature of callback(Value,..) to callback(ValueBox,..)
   Revision 1.11  2003/11/17 01:44:01  venku
   - documentation.
   Revision 1.10  2003/11/15 21:19:36  venku
   - added methods to register/unregister for all value types.
   Revision 1.9  2003/11/10 08:09:02  venku
   - documentation.
   Revision 1.8  2003/11/10 07:56:20  venku
   - calls processingBegins() on processors.
   Revision 1.7  2003/11/10 02:41:30  venku
   - added a utility method to register for all statements.
   Revision 1.6  2003/11/07 09:24:42  venku
   - exposed the collection of statement and value classes
     to the public.
   Revision 1.5  2003/11/06 08:33:36  venku
   - previous optimization had subtle bugs. FIXED.
   Revision 1.4  2003/11/06 07:57:10  venku
   - optimized processing depending on the processors.
   Revision 1.3  2003/11/06 06:22:12  venku
   - documentation.
   Revision 1.2  2003/11/06 05:31:08  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.1  2003/11/06 05:15:05  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.8  2003/10/21 08:41:04  venku
   - Changed the methods/classes get filtered.
   Revision 1.7  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/09/08 02:21:16  venku
   - processors will need to register separately for functional procesing
     and inteface processing.
   Revision 1.5  2003/08/25 08:36:27  venku
   Coding convention.
   Revision 1.4  2003/08/25 08:07:26  venku
   Extracts the classes for processing from the environment.
   It now has support to be driven by the environment alone.
   Revision 1.3  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/11 06:38:25  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
