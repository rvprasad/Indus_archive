
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

import edu.ksu.cis.indus.common.datastructures.HistoryAwareLIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

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

import soot.toolkits.graph.UnitGraph;


/**
 * This class controls the post processing for an analysis.  The analyses such as FA are very low-level.  The information is
 * raw.  This needs to be massaged via post processing.  Each post processor can registered interest in particular types of
 * AST chunks.  The controller will walk over the analyzed system and call the registered post processors. The post
 * processors then collect information from the analysis in form which is more accessible to the other applications. This
 * visitor will notify the interested post processors with the given AST node and then visit it's children.
 * 
 * <p>
 * This class will control the processing of statements in methods.  If the clients want to process the statements based on
 * their reachability local to the method, then the clients should call <code>setStmtGraphFactory()</code> before using an
 * instance of this class for processing.
 * </p>
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
	 * A collection of all possible Jimple statement types for which a processor can register interest.
	 */
	public static final Collection STMT_CLASSES;

	/**
	 * A collection of all possible Jimple value types for which a processor can register interest.
	 */
	public static final Collection VALUE_CLASSES;

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ProcessingController.class);

	static {
		Collection _t = new HashSet();
		_t.add(AssignStmt.class);
		_t.add(BreakpointStmt.class);
		_t.add(EnterMonitorStmt.class);
		_t.add(ExitMonitorStmt.class);
		_t.add(GotoStmt.class);
		_t.add(IdentityStmt.class);
		_t.add(IfStmt.class);
		_t.add(InvokeStmt.class);
		_t.add(LookupSwitchStmt.class);
		_t.add(NopStmt.class);
		_t.add(RetStmt.class);
		_t.add(ReturnVoidStmt.class);
		_t.add(ReturnStmt.class);
		_t.add(TableSwitchStmt.class);
		_t.add(ThrowStmt.class);

		STMT_CLASSES = Collections.unmodifiableCollection(_t);

		_t = new HashSet();
		_t.add(AddExpr.class);
		_t.add(AndExpr.class);
		_t.add(ArrayRef.class);
		_t.add(CastExpr.class);
		_t.add(CaughtExceptionRef.class);
		_t.add(CmpExpr.class);
		_t.add(CmpgExpr.class);
		_t.add(CmplExpr.class);
		_t.add(DivExpr.class);
		_t.add(DoubleConstant.class);
		_t.add(EqExpr.class);
		_t.add(FloatConstant.class);
		_t.add(GeExpr.class);
		_t.add(GtExpr.class);
		_t.add(InstanceFieldRef.class);
		_t.add(InstanceOfExpr.class);
		_t.add(IntConstant.class);
		_t.add(InterfaceInvokeExpr.class);
		_t.add(LeExpr.class);
		_t.add(LengthExpr.class);
		_t.add(Local.class);
		_t.add(LongConstant.class);
		_t.add(LtExpr.class);
		_t.add(MulExpr.class);
		_t.add(NeExpr.class);
		_t.add(NegExpr.class);
		_t.add(NewArrayExpr.class);
		_t.add(NewExpr.class);
		_t.add(NewMultiArrayExpr.class);
		_t.add(NullConstant.class);
		_t.add(OrExpr.class);
		_t.add(ParameterRef.class);
		_t.add(RemExpr.class);
		_t.add(ShlExpr.class);
		_t.add(ShrExpr.class);
		_t.add(SpecialInvokeExpr.class);
		_t.add(StaticFieldRef.class);
		_t.add(StaticInvokeExpr.class);
		_t.add(StringConstant.class);
		_t.add(SubExpr.class);
		_t.add(ThisRef.class);
		_t.add(UshrExpr.class);
		_t.add(VirtualInvokeExpr.class);
		_t.add(XorExpr.class);

		VALUE_CLASSES = Collections.unmodifiableCollection(_t);
	}

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
	 * The filter used to filter the classes that select the classes and methods to be processed.
	 */
	private IProcessingFilter processingFilter;

	/**
	 * The factory that provides statement graphs (CFGs).  This should be set before process is called.
	 */
	private IStmtGraphFactory stmtGraphFactory;

	/**
	 * This class visits the statements of the methods and calls the call-back methods of the registered processors.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private final class StmtSwitcher
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
			final Collection _temp = (Collection) class2processors.get(objClass);

			if (_temp != null) {
				final Stmt _stmt = (Stmt) o;

				for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
					final IProcessor _pp = (IProcessor) _i.next();
					_pp.callback(_stmt, context);
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
	private final class ValueSwitcher
	  extends AbstractJimpleValueSwitch {
		/**
		 * @see soot.jimple.ExprSwitch#caseAddExpr(soot.jimple.AddExpr)
		 */
		public void caseAddExpr(final AddExpr v) {
			defaultCase(AddExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseAndExpr(soot.jimple.AndExpr)
		 */
		public void caseAndExpr(final AndExpr v) {
			defaultCase(AndExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
		 */
		public void caseArrayRef(final ArrayRef v) {
			defaultCase(ArrayRef.class);
			context.setProgramPoint(v.getBaseBox());
			v.getBase().apply(this);
			context.setProgramPoint(v.getIndexBox());
			v.getIndex().apply(this);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
		 */
		public void caseCastExpr(final CastExpr v) {
			defaultCase(CastExpr.class);
			context.setProgramPoint(v.getOpBox());
			v.getOp().apply(this);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef)
		 */
		public void caseCaughtExceptionRef(final CaughtExceptionRef v) {
			defaultCase(CaughtExceptionRef.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmpExpr(soot.jimple.CmpExpr)
		 */
		public void caseCmpExpr(final CmpExpr v) {
			defaultCase(CmpExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmpgExpr(soot.jimple.CmpgExpr)
		 */
		public void caseCmpgExpr(final CmpgExpr v) {
			defaultCase(CmpgExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmplExpr(soot.jimple.CmplExpr)
		 */
		public void caseCmplExpr(final CmplExpr v) {
			defaultCase(CmplExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseDivExpr(soot.jimple.DivExpr)
		 */
		public void caseDivExpr(final DivExpr v) {
			defaultCase(DivExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
		 */
		public void caseDoubleConstant(final DoubleConstant v) {
			defaultCase(DoubleConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseEqExpr(soot.jimple.EqExpr)
		 */
		public void caseEqExpr(final EqExpr v) {
			defaultCase(EqExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
		 */
		public void caseFloatConstant(final FloatConstant v) {
			defaultCase(FloatConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseGeExpr(soot.jimple.GeExpr)
		 */
		public void caseGeExpr(final GeExpr v) {
			defaultCase(GeExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseGtExpr(soot.jimple.GtExpr)
		 */
		public void caseGtExpr(final GtExpr v) {
			defaultCase(GtExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		public void caseInstanceFieldRef(final InstanceFieldRef v) {
			defaultCase(InstanceFieldRef.class);
			context.setProgramPoint(v.getBaseBox());
			v.getBase().apply(this);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
		 */
		public void caseInstanceOfExpr(final InstanceOfExpr v) {
			defaultCase(InstanceOfExpr.class);
			context.setProgramPoint(v.getOpBox());
			v.getOp().apply(this);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
		 */
		public void caseIntConstant(final IntConstant v) {
			defaultCase(IntConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
		 */
		public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
			defaultCase(InterfaceInvokeExpr.class);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLeExpr(soot.jimple.LeExpr)
		 */
		public void caseLeExpr(final LeExpr v) {
			defaultCase(LeExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
		 */
		public void caseLengthExpr(final LengthExpr v) {
			defaultCase(LengthExpr.class);
			processUnaryExpr(v);
		}

		/**
		 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
		 */
		public void caseLocal(final Local v) {
			defaultCase(Local.class);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
		 */
		public void caseLongConstant(final LongConstant v) {
			defaultCase(LongConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLtExpr(soot.jimple.LtExpr)
		 */
		public void caseLtExpr(final LtExpr v) {
			defaultCase(LtExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseMulExpr(soot.jimple.MulExpr)
		 */
		public void caseMulExpr(final MulExpr v) {
			defaultCase(MulExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNeExpr(soot.jimple.NeExpr)
		 */
		public void caseNeExpr(final NeExpr v) {
			defaultCase(NeExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNegExpr(soot.jimple.NegExpr)
		 */
		public void caseNegExpr(final NegExpr v) {
			defaultCase(NegExpr.class);
			processUnaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
		 */
		public void caseNewArrayExpr(final NewArrayExpr v) {
			defaultCase(NewArrayExpr.class);
			context.setProgramPoint(v.getSizeBox());
			v.getSize().apply(this);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
		 */
		public void caseNewExpr(final NewExpr v) {
			defaultCase(NewExpr.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
		 */
		public void caseNewMultiArrayExpr(final NewMultiArrayExpr v) {
			defaultCase(NewMultiArrayExpr.class);

			for (int _i = 0; _i < v.getSizeCount(); _i++) {
				context.setProgramPoint(v.getSizeBox(_i));
				v.getSize(_i).apply(this);
			}
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
		 */
		public void caseNullConstant(final NullConstant v) {
			defaultCase(NullConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseOrExpr(soot.jimple.OrExpr)
		 */
		public void caseOrExpr(final OrExpr v) {
			defaultCase(OrExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
		 */
		public void caseParameterRef(final ParameterRef v) {
			defaultCase(ParameterRef.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseRemExpr(soot.jimple.RemExpr)
		 */
		public void caseRemExpr(final RemExpr v) {
			defaultCase(RemExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseShlExpr(soot.jimple.ShlExpr)
		 */
		public void caseShlExpr(final ShlExpr v) {
			defaultCase(ShlExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseShrExpr(soot.jimple.ShrExpr)
		 */
		public void caseShrExpr(final ShrExpr v) {
			defaultCase(ShrExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
		 */
		public void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
			defaultCase(SpecialInvokeExpr.class);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
		 */
		public void caseStaticFieldRef(final StaticFieldRef v) {
			defaultCase(StaticFieldRef.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
		 */
		public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
			defaultCase(StaticInvokeExpr.class);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
		 */
		public void caseStringConstant(final StringConstant v) {
			defaultCase(StringConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSubExpr(soot.jimple.SubExpr)
		 */
		public void caseSubExpr(final SubExpr v) {
			defaultCase(SubExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
		 */
		public void caseThisRef(final ThisRef v) {
			defaultCase(ThisRef.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseUshrExpr(soot.jimple.UshrExpr)
		 */
		public void caseUshrExpr(final UshrExpr v) {
			defaultCase(UshrExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
		 */
		public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
			defaultCase(VirtualInvokeExpr.class);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseXorExpr(soot.jimple.XorExpr)
		 */
		public void caseXorExpr(final XorExpr v) {
			defaultCase(XorExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * Calls the processors interested in processing object of type <code>objClass</code>.
		 *
		 * @param objClass is the type of <code>o</code>
		 */
		public void defaultCase(final Class objClass) {
			final Collection _temp = (Collection) class2processors.get(objClass);

			if (_temp != null) {
				for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
					final IProcessor _pp = (IProcessor) _i.next();
					_pp.callback(context.getProgramPoint(), context);
				}
			}
		}

		/**
		 * Process expressions with binary operator.
		 *
		 * @param v is the expression with binary operator.
		 *
		 * @pre v != null
		 */
		private void processBinaryExpr(final BinopExpr v) {
			context.setProgramPoint(v.getOp1Box());
			v.getOp1().apply(this);
			context.setProgramPoint(v.getOp2Box());
			v.getOp2().apply(this);
		}

		/**
		 * Process method invocation expression.
		 *
		 * @param v is the invocation expression.
		 *
		 * @pre v != null
		 */
		private void processInvokeExpr(final InvokeExpr v) {
			if (v instanceof InstanceInvokeExpr) {
				context.setProgramPoint(((InstanceInvokeExpr) v).getBaseBox());
				((InstanceInvokeExpr) v).getBase().apply(this);
			}

			for (int _i = 0; _i < v.getArgCount(); _i++) {
				context.setProgramPoint(v.getArgBox(_i));
				v.getArg(_i).apply(this);
			}
		}

		/**
		 * Processes expressions with unary operator.
		 *
		 * @param v is the expression with unary operator.
		 *
		 * @pre v != null
		 */
		private void processUnaryExpr(final UnopExpr v) {
			context.setProgramPoint(v.getOpBox());
			v.getOp().apply(this);
		}
	}

	/**
	 * Sets the environment which provides the system to be processed.
	 *
	 * @param environment an instance of the FA.
	 */
	public final void setEnvironment(final IEnvironment environment) {
		env = environment;
	}

	/**
	 * Sets the filter to be used to pick the classes and methods to be processed.
	 *
	 * @param theFilter to be used.
	 *
	 * @pre theFilter != null
	 */
	public final void setProcessingFilter(final IProcessingFilter theFilter) {
		processingFilter = theFilter;
	}

	/**
	 * Sets the factory object that provides statement graphs (CFGs).
	 *
	 * @param cfgFactory is the factory object.
	 */
	public void setStmtGraphFactory(final IStmtGraphFactory cfgFactory) {
		stmtGraphFactory = cfgFactory;
	}

	/**
	 * Drive the given processors by the given controller.  This is helpful to batch pre/post-processors.
	 *
	 * @param processors is the collection of processors.
	 *
	 * @pre processors != null
	 * @pre processors.oclIsKindOf(Collection(IProcessor))
	 */
	public final void driveProcessors(final Collection processors) {
		for (final Iterator _i = processors.iterator(); _i.hasNext();) {
			final IProcessor _processor = (IProcessor) _i.next();

			_processor.hookup(this);
		}
		process();

		for (final Iterator _i = processors.iterator(); _i.hasNext();) {
			final IProcessor _processor = (IProcessor) _i.next();

			_processor.unhook(this);
		}
	}

	/**
	 * Controls the processing activity.
	 * 
	 * <p>
	 * If statements in the system should be processed, then
	 * 
	 * <ul>
	 * <li>
	 * calling <code>setStmtGraphFactory()</code> before this method will cause the processing of the statements based on
	 * reachability of the statements determined by the graph retrieved by the provided factory.
	 * </li>
	 * <li>
	 * not calling <code>setStmtGraphFactory()</code> before this method will cause the processing of all statements of a
	 * method if it's body is available
	 * </li>
	 * </ul>
	 * </p>
	 */
	public final void process() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: processing classes");
		}

		initializeProcessors();

		final Collection _processors = new HashSet();
		_processors.addAll(interfaceProcessors);

		for (final Iterator _i = class2processors.values().iterator(); _i.hasNext();) {
			_processors.addAll((Collection) _i.next());
		}

		for (final Iterator _i = _processors.iterator(); _i.hasNext();) {
			((IProcessor) _i.next()).processingBegins();
		}

		processStmts = !CollectionUtils.intersection(class2processors.keySet(), STMT_CLASSES).isEmpty();
		processValues = !CollectionUtils.intersection(class2processors.keySet(), VALUE_CLASSES).isEmpty();
		processClasses(env.getClasses());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debub("END: processing classes");
			LOGGER.debug("BEGIN: consolidation");
		}

		for (final Iterator _i = _processors.iterator(); _i.hasNext();) {
			((IProcessor) _i.next()).consolidate();
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: consolidation");
		}
	}

	/**
	 * Registers the processor.  It indicates that the processor is interested in processing AST chunk of type
	 * <code>interest</code>.
	 *
	 * @param interest the class of AST node in which the <code>processor</code> is interested.
	 * @param processor the instance of processor.
	 */
	public final void register(final Class interest, final IProcessor processor) {
		Set _temp = (Set) class2processors.get(interest);

		if (_temp == null) {
			_temp = new HashSet();
			class2processors.put(interest, _temp);
		}
		_temp.add(processor);
	}

	/**
	 * Registers the processor for class, fields, and method interface processing only.
	 *
	 * @param processor the instance of processor.
	 */
	public final void register(final IProcessor processor) {
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
	public final void registerForAllStmts(final IProcessor processor) {
		for (final Iterator _i = ProcessingController.STMT_CLASSES.iterator(); _i.hasNext();) {
			register((Class) _i.next(), processor);
		}
	}

	/**
	 * Registers the processor.  It indicates that the processor is interested in processing AST chunk of value type. Please
	 * refer to <code>VALUE_CLASSES</code> for the actual types.
	 *
	 * @param processor the instance of processor.
	 */
	public final void registerForAllValues(final IProcessor processor) {
		for (final Iterator _i = ProcessingController.VALUE_CLASSES.iterator(); _i.hasNext();) {
			register((Class) _i.next(), processor);
		}
	}

	/**
	 * Clears internal data structures.  It does not reset values set via set methods.
	 */
	public final void reset() {
		class2processors.clear();
		interfaceProcessors.clear();
		processStmts = false;
		processValues = false;
		context.setStmt(null);
		context.setProgramPoint(null);
		context.setRootMethod(null);
		context.returnFromCurrentMethod();
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
	public final void unregister(final Class interest, final IProcessor processor) {
		final Set _temp = (Set) class2processors.get(interest);

		if (_temp == null) {
			throw new IllegalArgumentException("There are no processors registered  for " + interest.getName());
		}
		_temp.remove(processor);
	}

	/**
	 * Unregisters the processor for class and method interface processing only.
	 *
	 * @param processor the instance of processor.
	 */
	public final void unregister(final IProcessor processor) {
		interfaceProcessors.remove(processor);
	}

	/**
	 * Unregisters the processor. It indicates that the processor is not interested in processing the statement types. Please
	 * refer to <code>STMT_CLASSES</code> for the actual types.
	 *
	 * @param processor the instance of processor.
	 */
	public final void unregisterForAllStmts(final IProcessor processor) {
		for (final Iterator _i = ProcessingController.STMT_CLASSES.iterator(); _i.hasNext();) {
			unregister((Class) _i.next(), processor);
		}
	}

	/**
	 * Unregisters the processor. It indicates that the processor is not interested in processing the value types. Please
	 * refer to <code>VALUE_CLASSES</code> for the actual types.
	 *
	 * @param processor the instance of processor.
	 */
	public final void unregisterForAllValues(final IProcessor processor) {
		for (final Iterator _i = ProcessingController.VALUE_CLASSES.iterator(); _i.hasNext();) {
			unregister((Class) _i.next(), processor);
		}
	}

	/**
	 * Initializes the processors before processing the system.
	 */
	protected void initializeProcessors() {
	}

	/**
	 * Controls the processing of class level entities.
	 *
	 * @param theClasses to be processed.
	 *
	 * @pre theClasses != null and theClasses.oclIsKindOf(Collection(SootClass))
	 */
	protected final void processClasses(final Collection theClasses) {
		Collection _classes;

		if (processingFilter == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Performance may be hit as processing filter is not set.");
			}
			_classes = theClasses;
		} else {
			_classes = processingFilter.filterClasses(theClasses);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Classes to be processed:\n" + _classes);
		}

		for (final Iterator _i = _classes.iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing class " + _sc);
			}

			for (final Iterator _k = interfaceProcessors.iterator(); _k.hasNext();) {
				final IProcessor _pp = (IProcessor) _k.next();
				_pp.callback(_sc);

				Collection _methods;

				if (processingFilter == null) {
					_methods = _sc.getFields();
				} else {
					_methods = processingFilter.filterFields(_sc.getFields());
				}

				for (final Iterator _j = _methods.iterator(); _j.hasNext();) {
					_pp.callback((SootField) _j.next());
				}
			}
			processMethods(_sc.getMethods());
		}
	}

	/**
	 * Controls the processing of methods and their bodies.
	 *
	 * @param theMethods to be processed.
	 *
	 * @pre theMethods != null and theMethods.oclIsKindOf(Collection(SootMethod))
	 */
	protected final void processMethods(final Collection theMethods) {
		Collection _methods;

		if (processingFilter == null) {
			_methods = theMethods;
		} else {
			_methods = processingFilter.filterMethods(theMethods);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Methods to be processed:\n" + _methods);
		}

		final boolean _processBody = processStmts || processValues;

		for (final Iterator _j = _methods.iterator(); _j.hasNext();) {
			final SootMethod _sm = (SootMethod) _j.next();
			context.setRootMethod(_sm);

			for (final Iterator _k = interfaceProcessors.iterator(); _k.hasNext();) {
				((IProcessor) _k.next()).callback(_sm);
			}

			if (_processBody && _sm.isConcrete()) {
				processMethodBody(_sm);
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(_sm + " is not a concrete method.  Hence, it's body could not be retrieved.");
			}
		}
	}

	/**
	 * Processes the method body.
	 *
	 * @param method whose body needs to be processed.
	 *
	 * @pre method != null
	 */
	private void processMethodBody(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing method " + method);
		}

		try {
			final IWorkBag _wb = new HistoryAwareLIFOWorkBag(new HashSet());

			if (stmtGraphFactory == null) {
				if (method.hasActiveBody()) {
					for (final Iterator _i = method.getActiveBody().getUnits().iterator(); _i.hasNext();) {
						final Stmt _stmt = (Stmt) _i.next();
						context.setStmt(_stmt);
						_stmt.apply(stmtSwitcher);
					}
				} else {
					LOGGER.error("Active body was not available for " + method.getSignature());
				}
			} else {
				final UnitGraph _stmtGraph = stmtGraphFactory.getStmtGraph(method);
				_wb.addAllWork(_stmtGraph.getHeads());

				while (_wb.hasWork()) {
					final Stmt _stmt = (Stmt) _wb.getWork();
					context.setStmt(_stmt);
					_stmt.apply(stmtSwitcher);
					_wb.addAllWorkNoDuplicates(_stmtGraph.getSuccsOf(_stmt));
				}
			}
		} catch (RuntimeException _e) {
			LOGGER.error("Well, exception while processing statements of a method may mean the processor does not"
				+ " recognize the given method or it's parts or method has not stored in jimple " + "representation. : "
				+ method.getSignature(), _e);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.33  2004/04/20 00:42:23  venku
   - Processing required a stmtGraphFactory to detect locally reachable statements
     However, this will fail in certain case.  Instead, one should be able to use this
     feature or just process all statements.  FIXED.

   Revision 1.32  2004/03/29 01:55:15  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.31  2004/02/09 04:39:40  venku
   - refactoring test classes still..
   - need to make xmlizer classes independent of their purpose.
     Hence, they need to be highly configurable.
   - For each concept, test setup should be in TestSetup
     rather than in the XMLizer.
   Revision 1.30  2003/12/28 00:40:37  venku
   - coding convention.
   Revision 1.29  2003/12/16 09:57:25  venku
   - removed redundant object and local.
   Revision 1.28  2003/12/15 02:16:47  venku
   - logging.
   Revision 1.27  2003/12/14 16:43:44  venku
   - extended ProcessingController to filter fields as well.
   - ripple effect.
   Revision 1.26  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.25  2003/12/08 09:17:55  venku
   - added a reset() method.
   Revision 1.24  2003/12/05 15:34:17  venku
   - logging and optimization.
   Revision 1.23  2003/12/05 03:10:07  venku
   - iteration in the wrong direction. FIXED.
   Revision 1.22  2003/12/05 03:08:56  venku
   - size expressions in new array and multi array were
     being ignored.  FIXED.
   Revision 1.21  2003/12/02 11:31:57  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.20  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.19  2003/12/02 01:30:58  venku
   - coding conventions and formatting.
   Revision 1.18  2003/12/01 11:34:28  venku
   - op1's box was used while processing op2 of binary expressions.  FIXED.
   Revision 1.17  2003/12/01 02:02:31  venku
   - coding convention.
   Revision 1.16  2003/11/30 13:20:51  venku
   - sub-expressions of complex expressions were not being
     processed.  FIXED.
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
