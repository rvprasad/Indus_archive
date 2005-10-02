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

package edu.ksu.cis.indus.processing;

import edu.ksu.cis.indus.interfaces.IActivePart;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

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
import soot.jimple.DefinitionStmt;
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

/**
 * This class controls the post processing for an analysis. The analyses such as FA are very low-level. The information is
 * raw. This needs to be massaged via post processing. Each post processor can registered interest in particular types of AST
 * chunks. The controller will walk over the analyzed system and call the registered post processors. The post processors then
 * collect information from the analysis in form which is more accessible to the other applications. This visitor will notify
 * the interested post processors with the given AST node and then visit it's children.
 * <p>
 * This class will control the processing of statements in methods. If the clients want to process the statements based on
 * their reachability local to the method, then the clients should call <code>setStmtGraphFactory()</code> before using an
 * instance of this class for processing.
 * </p>
 * <p>
 * Please note that the processor should be registered/unregistered separately for interface-level (class/method) processing
 * and functional (method-body) processing.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ProcessingController {

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
		 * Creates an instance of this class.
		 */
		public StmtSwitcher() {
			super();
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		@Override public void caseAssignStmt(final AssignStmt stmt) {
			if (processStmts) {
				defaultCase(AssignStmt.class, stmt);
			}

			if (processValues) {
				processValuesBoxesInDefStmt(stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseBreakpointStmt(soot.jimple.BreakpointStmt)
		 */
		@Override public void caseBreakpointStmt(final BreakpointStmt stmt) {
			if (processStmts) {
				defaultCase(BreakpointStmt.class, stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseEnterMonitorStmt(soot.jimple.EnterMonitorStmt)
		 */
		@Override public void caseEnterMonitorStmt(final EnterMonitorStmt stmt) {
			if (processStmts) {
				defaultCase(EnterMonitorStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getOpBox()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseExitMonitorStmt(soot.jimple.ExitMonitorStmt)
		 */
		@Override public void caseExitMonitorStmt(final ExitMonitorStmt stmt) {
			if (processStmts) {
				defaultCase(ExitMonitorStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getOpBox()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseGotoStmt(soot.jimple.GotoStmt)
		 */
		@Override public void caseGotoStmt(final GotoStmt stmt) {
			if (processStmts) {
				defaultCase(GotoStmt.class, stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		@Override public void caseIdentityStmt(final IdentityStmt stmt) {
			if (processStmts) {
				defaultCase(IdentityStmt.class, stmt);
			}

			if (processValues) {
				processValuesBoxesInDefStmt(stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIfStmt(soot.jimple.IfStmt)
		 */
		@Override public void caseIfStmt(final IfStmt stmt) {
			if (processStmts) {
				defaultCase(IfStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getConditionBox()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		@Override public void caseInvokeStmt(final InvokeStmt stmt) {
			if (processStmts) {
				defaultCase(InvokeStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getInvokeExprBox()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
		 */
		@Override public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
			if (processStmts) {
				defaultCase(LookupSwitchStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getKeyBox()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseNopStmt(soot.jimple.NopStmt)
		 */
		@Override public void caseNopStmt(final NopStmt stmt) {
			if (processStmts) {
				defaultCase(NopStmt.class, stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseRetStmt(soot.jimple.RetStmt)
		 */
		@Override public void caseRetStmt(final RetStmt stmt) {
			if (processStmts) {
				defaultCase(RetStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getStmtAddressBox()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		@Override public void caseReturnStmt(final ReturnStmt stmt) {
			if (processStmts) {
				defaultCase(ReturnStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getOpBox()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnVoidStmt(soot.jimple.ReturnVoidStmt)
		 */
		@Override public void caseReturnVoidStmt(final ReturnVoidStmt stmt) {
			if (processStmts) {
				defaultCase(ReturnVoidStmt.class, stmt);
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
		 */
		@Override public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
			if (processStmts) {
				defaultCase(TableSwitchStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getKeyBox()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		@Override public void caseThrowStmt(final ThrowStmt stmt) {
			if (processStmts) {
				defaultCase(ThrowStmt.class, stmt);
			}

			if (processValues) {
				processValueBoxes(Collections.singletonList(stmt.getOpBox()));
			}
		}

		/**
		 * Calls the processors interested in processing objects of type <code>objClass</code>.
		 * 
		 * @param objClass is the type of <code>o</code>.
		 * @param o the AST INode to be processed.
		 */
		public void defaultCase(final Class objClass, final Object o) {
			final Collection _temp = class2processors.get(objClass);

			if (_temp != null) {
				final Stmt _stmt = (Stmt) o;

				for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
					final IProcessor _pp = (IProcessor) _i.next();
					_pp.callback(_stmt, context);
				}
			}
		}

		/**
		 * Processes the value boxes in the given definition statement.
		 * 
		 * @param stmt to be processed.
		 * @pre stmt != null
		 */
		private void processValuesBoxesInDefStmt(final DefinitionStmt stmt) {
			final Collection<ValueBox> _boxes = new ArrayList<ValueBox>();
			_boxes.add(stmt.getLeftOpBox());
			_boxes.add(stmt.getRightOpBox());
			processValueBoxes(_boxes);
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
		 * Creates an instance of this class.
		 */
		public ValueSwitcher() {
			super();
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseAddExpr(soot.jimple.AddExpr)
		 */
		@Override public void caseAddExpr(final AddExpr v) {
			defaultCase(AddExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseAndExpr(soot.jimple.AndExpr)
		 */
		@Override public void caseAndExpr(final AndExpr v) {
			defaultCase(AndExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseArrayRef(soot.jimple.ArrayRef)
		 */
		@Override public void caseArrayRef(final ArrayRef v) {
			defaultCase(ArrayRef.class);

			final Collection<ValueBox> _boxes = new ArrayList<ValueBox>();
			_boxes.add(v.getBaseBox());
			_boxes.add(v.getIndexBox());
			processValueBoxes(_boxes);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCastExpr(soot.jimple.CastExpr)
		 */
		@Override public void caseCastExpr(final CastExpr v) {
			defaultCase(CastExpr.class);
			processValueBoxes(Collections.singletonList(v.getOpBox()));
		}

		/**
		 * @see soot.jimple.RefSwitch#caseCaughtExceptionRef(soot.jimple.CaughtExceptionRef)
		 */
		@Override public void caseCaughtExceptionRef(@SuppressWarnings("unused") final CaughtExceptionRef v) {
			defaultCase(CaughtExceptionRef.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmpExpr(soot.jimple.CmpExpr)
		 */
		@Override public void caseCmpExpr(final CmpExpr v) {
			defaultCase(CmpExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmpgExpr(soot.jimple.CmpgExpr)
		 */
		@Override public void caseCmpgExpr(final CmpgExpr v) {
			defaultCase(CmpgExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseCmplExpr(soot.jimple.CmplExpr)
		 */
		@Override public void caseCmplExpr(final CmplExpr v) {
			defaultCase(CmplExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseDivExpr(soot.jimple.DivExpr)
		 */
		@Override public void caseDivExpr(final DivExpr v) {
			defaultCase(DivExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseDoubleConstant(soot.jimple.DoubleConstant)
		 */
		@Override public void caseDoubleConstant(@SuppressWarnings("unused") final DoubleConstant v) {
			defaultCase(DoubleConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseEqExpr(soot.jimple.EqExpr)
		 */
		@Override public void caseEqExpr(final EqExpr v) {
			defaultCase(EqExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseFloatConstant(soot.jimple.FloatConstant)
		 */
		@Override public void caseFloatConstant(@SuppressWarnings("unused") final FloatConstant v) {
			defaultCase(FloatConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseGeExpr(soot.jimple.GeExpr)
		 */
		@Override public void caseGeExpr(final GeExpr v) {
			defaultCase(GeExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseGtExpr(soot.jimple.GtExpr)
		 */
		@Override public void caseGtExpr(final GtExpr v) {
			defaultCase(GtExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseInstanceFieldRef(soot.jimple.InstanceFieldRef)
		 */
		@Override public void caseInstanceFieldRef(final InstanceFieldRef v) {
			defaultCase(InstanceFieldRef.class);
			processValueBoxes(Collections.singletonList(v.getBaseBox()));
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInstanceOfExpr(soot.jimple.InstanceOfExpr)
		 */
		@Override public void caseInstanceOfExpr(final InstanceOfExpr v) {
			defaultCase(InstanceOfExpr.class);
			processValueBoxes(Collections.singletonList(v.getOpBox()));
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseIntConstant(soot.jimple.IntConstant)
		 */
		@Override public void caseIntConstant(@SuppressWarnings("unused") final IntConstant v) {
			defaultCase(IntConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseInterfaceInvokeExpr(soot.jimple.InterfaceInvokeExpr)
		 */
		@Override public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
			defaultCase(InterfaceInvokeExpr.class);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLeExpr(soot.jimple.LeExpr)
		 */
		@Override public void caseLeExpr(final LeExpr v) {
			defaultCase(LeExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLengthExpr(soot.jimple.LengthExpr)
		 */
		@Override public void caseLengthExpr(final LengthExpr v) {
			defaultCase(LengthExpr.class);
			processUnaryExpr(v);
		}

		/**
		 * @see soot.jimple.JimpleValueSwitch#caseLocal(soot.Local)
		 */
		@Override public void caseLocal(@SuppressWarnings("unused") final Local v) {
			defaultCase(Local.class);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseLongConstant(soot.jimple.LongConstant)
		 */
		@Override public void caseLongConstant(@SuppressWarnings("unused") final LongConstant v) {
			defaultCase(LongConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseLtExpr(soot.jimple.LtExpr)
		 */
		@Override public void caseLtExpr(final LtExpr v) {
			defaultCase(LtExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseMulExpr(soot.jimple.MulExpr)
		 */
		@Override public void caseMulExpr(final MulExpr v) {
			defaultCase(MulExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNeExpr(soot.jimple.NeExpr)
		 */
		@Override public void caseNeExpr(final NeExpr v) {
			defaultCase(NeExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNegExpr(soot.jimple.NegExpr)
		 */
		@Override public void caseNegExpr(final NegExpr v) {
			defaultCase(NegExpr.class);
			processUnaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewArrayExpr(soot.jimple.NewArrayExpr)
		 */
		@Override public void caseNewArrayExpr(final NewArrayExpr v) {
			defaultCase(NewArrayExpr.class);
			processValueBoxes(Collections.singletonList(v.getSizeBox()));
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewExpr(soot.jimple.NewExpr)
		 */
		@Override public void caseNewExpr(@SuppressWarnings("unused") final NewExpr v) {
			defaultCase(NewExpr.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseNewMultiArrayExpr(soot.jimple.NewMultiArrayExpr)
		 */
		@Override public void caseNewMultiArrayExpr(final NewMultiArrayExpr v) {
			defaultCase(NewMultiArrayExpr.class);

			final Collection<ValueBox> _boxes = new ArrayList<ValueBox>();

			for (int _i = 0; _i < v.getSizeCount(); _i++) {
				_boxes.add(v.getSizeBox(_i));
			}
			processValueBoxes(_boxes);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseNullConstant(soot.jimple.NullConstant)
		 */
		@Override public void caseNullConstant(@SuppressWarnings("unused") final NullConstant v) {
			defaultCase(NullConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseOrExpr(soot.jimple.OrExpr)
		 */
		@Override public void caseOrExpr(final OrExpr v) {
			defaultCase(OrExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseParameterRef(soot.jimple.ParameterRef)
		 */
		@Override public void caseParameterRef(@SuppressWarnings("unused") final ParameterRef v) {
			defaultCase(ParameterRef.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseRemExpr(soot.jimple.RemExpr)
		 */
		@Override public void caseRemExpr(final RemExpr v) {
			defaultCase(RemExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseShlExpr(soot.jimple.ShlExpr)
		 */
		@Override public void caseShlExpr(final ShlExpr v) {
			defaultCase(ShlExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseShrExpr(soot.jimple.ShrExpr)
		 */
		@Override public void caseShrExpr(final ShrExpr v) {
			defaultCase(ShrExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSpecialInvokeExpr(soot.jimple.SpecialInvokeExpr)
		 */
		@Override public void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
			defaultCase(SpecialInvokeExpr.class);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseStaticFieldRef(soot.jimple.StaticFieldRef)
		 */
		@Override public void caseStaticFieldRef(@SuppressWarnings("unused") final StaticFieldRef v) {
			defaultCase(StaticFieldRef.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseStaticInvokeExpr(soot.jimple.StaticInvokeExpr)
		 */
		@Override public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
			defaultCase(StaticInvokeExpr.class);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ConstantSwitch#caseStringConstant(soot.jimple.StringConstant)
		 */
		@Override public void caseStringConstant(@SuppressWarnings("unused") final StringConstant v) {
			defaultCase(StringConstant.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseSubExpr(soot.jimple.SubExpr)
		 */
		@Override public void caseSubExpr(final SubExpr v) {

			defaultCase(SubExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.RefSwitch#caseThisRef(soot.jimple.ThisRef)
		 */
		@Override public void caseThisRef(@SuppressWarnings("unused") final ThisRef v) {
			defaultCase(ThisRef.class);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseUshrExpr(soot.jimple.UshrExpr)
		 */
		@Override public void caseUshrExpr(final UshrExpr v) {
			defaultCase(UshrExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
		 */
		@Override public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
			defaultCase(VirtualInvokeExpr.class);
			processInvokeExpr(v);
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseXorExpr(soot.jimple.XorExpr)
		 */
		@Override public void caseXorExpr(final XorExpr v) {
			defaultCase(XorExpr.class);
			processBinaryExpr(v);
		}

		/**
		 * Calls the processors interested in processing object of type <code>objClass</code>.
		 * 
		 * @param objClass is the type of <code>o</code>
		 */
		public void defaultCase(final Class objClass) {
			final Collection _temp = class2processors.get(objClass);

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
		 * @pre v != null
		 */
		private void processBinaryExpr(final BinopExpr v) {
			final Collection<ValueBox> _boxes = new ArrayList<ValueBox>();
			_boxes.add(v.getOp1Box());
			_boxes.add(v.getOp2Box());
			processValueBoxes(_boxes);
		}

		/**
		 * Process method invocation expression.
		 * 
		 * @param v is the invocation expression.
		 * @pre v != null
		 */
		private void processInvokeExpr(final InvokeExpr v) {
			final Collection<ValueBox> _boxes = new ArrayList<ValueBox>();
			if (v instanceof InstanceInvokeExpr) {
				_boxes.add(((InstanceInvokeExpr) v).getBaseBox());
			}

			for (int _i = 0; _i < v.getArgCount(); _i++) {
				_boxes.add(v.getArgBox(_i));
			}

			processValueBoxes(_boxes);
		}

		/**
		 * Processes expressions with unary operator.
		 * 
		 * @param v is the expression with unary operator.
		 * @pre v != null
		 */
		private void processUnaryExpr(final UnopExpr v) {
			processValueBoxes(Collections.singletonList(v.getOpBox()));
		}
	}

	/**
	 * A collection of all possible Jimple statement types for which a processor can register interest.
	 */
	public static final Collection<Class> STMT_CLASSES;

	/**
	 * A collection of all possible Jimple value types for which a processor can register interest.
	 */
	public static final Collection<Class> VALUE_CLASSES;

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingController.class);

	static {
		Collection<Class> _t = new HashSet<Class>();
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

		_t = new HashSet<Class>();
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
	 * This maps a class to the post processors interested in processing the analysis information pertaining to AST nodes of
	 * class type.
	 * 
	 * @invariant class2processors.oclIsKindOf(Map(Class, Set(IProcessors)))
	 */
	protected final Map<Class, Set<IProcessor>> class2processors = new HashMap<Class, Set<IProcessor>>();

	/**
	 * The context in which the AST chunk is visited during post processing.
	 */
	protected Context context = new Context();

	/**
	 * The collection of processors registered with this controller to process interfaces (class/method). This maintains the
	 * insertion order.
	 * 
	 * @invariant interfaceProcessors->forall(o | o.oclIsKindOf(IProcessor))
	 */
	protected final Collection<IProcessor> interfaceProcessors = new ArrayList<IProcessor>();

	/**
	 * The collection of processors registered with this controller to process method local variables. This maintains the
	 * insertion order.
	 * 
	 * @invariant localsProcessors->forall(o | o.oclIsKindOf(IProcessor))
	 */
	protected final Collection<IProcessor> localsProcessors = new ArrayList<IProcessor>();

	/**
	 * This indicates if statements are being processed.
	 */
	boolean processStmts;

	/**
	 * This indicates if values are being processed.
	 */
	boolean processValues;

	/**
	 * The object used to realize the "active" part of this object.
	 */
	private final IActivePart.ActivePart activePart = new IActivePart.ActivePart();

	/**
	 * This defines the environment in which the processing runs.
	 */
	private IEnvironment env;

	/**
	 * This caches the processed locals while processing each method body.
	 */
	private final Collection<Value> processedLocals = new HashSet<Value>();

	/**
	 * The filter used to filter the classes that select the classes and methods to be processed.
	 */
	private IProcessingFilter processingFilter;

	/**
	 * The object that controls the order in which the statements should be processed. This should be set before process() or
	 * driveProcessors() is called.
	 */
	private IStmtSequencesRetriever stmtSequencesRetriever;

	/**
	 * This walks over the statements for processing.
	 */
	private final StmtSwitcher stmtSwitcher = new StmtSwitcher();

	/**
	 * This walks over the value for processing.
	 */
	private final ValueSwitcher valueSwitcher = new ValueSwitcher();

	/**
	 * Creates an instance of this class.
	 */
	public ProcessingController() {
		super();
	}

	/**
	 * Drive the given processors by the given controller. This is helpful to batch pre/post-processors.
	 * 
	 * @param processors is the collection of processors.
	 * @pre processors != null
	 */
	public final void driveProcessors(final Collection<IProcessor> processors) {
		for (final Iterator<IProcessor> _i = processors.iterator(); _i.hasNext();) {
			final IProcessor _processor = _i.next();

			_processor.hookup(this);
		}
		process();

		for (final Iterator<IProcessor> _i = processors.iterator(); _i.hasNext();) {
			final IProcessor _processor = _i.next();

			_processor.unhook(this);
		}
	}

	/**
	 * Returns the active part of this object.
	 * 
	 * @return the active part.
	 */
	public IActivePart getActivePart() {
		return activePart;
	}

	/**
	 * Controls the processing activity.
	 * <p>
	 * If statements in the system should be processed, then
	 * <ul>
	 * <li> calling <code>setStmtGraphFactory()</code> before this method will cause the processing of the statements based
	 * on reachability of the statements determined by the graph retrieved by the provided factory. </li>
	 * <li> not calling <code>setStmtGraphFactory()</code> before this method will cause the processing of all statements of
	 * a method if it's body is available </li>
	 * </ul>
	 * </p>
	 */
	public final void process() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: processing classes");
		}

		initializeProcessors();

		final Collection<IProcessor> _processors = new HashSet<IProcessor>();
		_processors.addAll(interfaceProcessors);

		for (final Iterator<Set<IProcessor>> _i = class2processors.values().iterator(); _i.hasNext();) {
			_processors.addAll(_i.next());
		}

		for (final Iterator<IProcessor> _i = _processors.iterator(); _i.hasNext();) {
			_i.next().processingBegins();
		}

		processStmts = !CollectionUtils.intersection(class2processors.keySet(), STMT_CLASSES).isEmpty();
		processValues = !CollectionUtils.intersection(class2processors.keySet(), VALUE_CLASSES).isEmpty();
		processClasses(env.getClasses());

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: processing classes");
			LOGGER.info("BEGIN: consolidation");
		}

		for (final Iterator<IProcessor> _i = _processors.iterator(); _i.hasNext();) {
			_i.next().consolidate();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: consolidation");
		}
	}

	/**
	 * Registers the processor. It indicates that the processor is interested in processing AST chunk of type
	 * <code>interest</code>.
	 * 
	 * @param interest the class of AST node in which the <code>processor</code> is interested.
	 * @param processor the instance of processor.
	 */
	public final void register(final Class interest, final IProcessor processor) {
		Set<IProcessor> _temp = class2processors.get(interest);

		if (_temp == null) {
			_temp = new HashSet<IProcessor>();
			class2processors.put(interest, _temp);
		}
		_temp.add(processor);
	}

	/**
	 * Registers the processor for class, fields, and method interface processing only. The processors are invoked in the
	 * order that they register.
	 * 
	 * @param processor the instance of processor.
	 */
	public final void register(final IProcessor processor) {
		if (!interfaceProcessors.contains(processor)) {
			interfaceProcessors.add(processor);
		}
	}

	/**
	 * Registers the processor. It indicates that the processor is interested in processing AST chunk of statement type.
	 * Please refer to <code>STMT_CLASSES</code> for the actual types. The processors are invoked in the order that they
	 * register.
	 * 
	 * @param processor the instance of processor.
	 */
	public final void registerForAllStmts(final IProcessor processor) {
		for (final Iterator<Class> _i = ProcessingController.STMT_CLASSES.iterator(); _i.hasNext();) {
			register(_i.next(), processor);
		}
	}

	/**
	 * Registers the processor. It indicates that the processor is interested in processing AST chunk of value type. Please
	 * refer to <code>VALUE_CLASSES</code> for the actual types. The processors are invoked in the order that they register.
	 * 
	 * @param processor the instance of processor.
	 */
	public final void registerForAllValues(final IProcessor processor) {
		for (final Iterator<Class> _i = ProcessingController.VALUE_CLASSES.iterator(); _i.hasNext();) {
			register(_i.next(), processor);
		}
	}

	/**
	 * Registers the processor for method local variable processing only. The processors are invoked in the order that they
	 * register.
	 * 
	 * @param processor the instance of processor.
	 */
	public final void registerForLocals(final IProcessor processor) {
		if (!localsProcessors.contains(processor)) {
			localsProcessors.add(processor);
		}
	}

	/**
	 * Clears internal data structures. It does not reset values set via set methods.
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
		activePart.activate();
	}

	/**
	 * Sets the environment which provides the system to be processed.
	 * 
	 * @param environment an instance of the FA.
	 * @pre environment != null
	 */
	public final void setEnvironment(final IEnvironment environment) {
		env = environment;
	}

	/**
	 * Sets the filter to be used to pick the classes and methods to be processed.
	 * 
	 * @param theFilter to be used.
	 * @pre theFilter != null
	 */
	public final void setProcessingFilter(final IProcessingFilter theFilter) {
		processingFilter = theFilter;
	}

	/**
	 * Sets the object that controls the order in which the statements are processed. This should be called before calling
	 * <code>process()</code> or <code>driverProcessors()</code>.
	 * 
	 * @param retriever controls the statement processing order.
	 * @pre retriever != null
	 */
	public void setStmtSequencesRetriever(final IStmtSequencesRetriever retriever) {
		stmtSequencesRetriever = retriever;
	}

	/**
	 * Unregisters the processor. It indicates that the processor is no longer interested in processing AST chunk of type
	 * <code>interest</code>.
	 * 
	 * @param interest the class of AST node in which the <code>processor</code> is interested.
	 * @param processor the instance of processor.
	 * @throws IllegalArgumentException when there are no processors who have registered to process <code>interest</code>.
	 */
	public final void unregister(final Class interest, final IProcessor processor) {
		final Set _temp = class2processors.get(interest);

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
		for (final Iterator<Class> _i = ProcessingController.STMT_CLASSES.iterator(); _i.hasNext();) {
			unregister(_i.next(), processor);
		}
	}

	/**
	 * Unregisters the processor. It indicates that the processor is not interested in processing the value types. Please
	 * refer to <code>VALUE_CLASSES</code> for the actual types.
	 * 
	 * @param processor the instance of processor.
	 */
	public final void unregisterForAllValues(final IProcessor processor) {
		for (final Iterator<Class> _i = ProcessingController.VALUE_CLASSES.iterator(); _i.hasNext();) {
			unregister(_i.next(), processor);
		}
	}

	/**
	 * Registers the processor for method local variable processing only.
	 * 
	 * @param processor the instance of processor.
	 */
	public final void unregisterForLocals(final IProcessor processor) {
		localsProcessors.remove(processor);
	}

	/**
	 * Initializes the processors before processing the system.
	 */
	protected void initializeProcessors() {
		// does nothing.
	}

	/**
	 * Processes the given value boxes.
	 * 
	 * @param boxes to be processed.
	 * @pre boxes != null and boxes.oclIsKindOf(Collection(ValueBox))
	 */
	void processValueBoxes(final Collection<ValueBox> boxes) {
		if (processingFilter != null) {
			processingFilter.filterValueBoxes(boxes);
		}

		final Iterator<ValueBox> _i = boxes.iterator();
		final int _iEnd = boxes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final ValueBox _vb = _i.next();
			context.setProgramPoint(_vb);
			_vb.getValue().apply(valueSwitcher);
		}
	}

	/**
	 * Controls the processing of class level entities.
	 * 
	 * @param theClasses to be processed.
	 * @pre theClasses != null and theClasses.oclIsKindOf(Collection(SootClass))
	 */
	private void processClasses(final Collection<SootClass> theClasses) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("processClasses(Collection) - BEGIN");
		}

		Collection<SootClass> _classes;

		if (processingFilter == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Performance may be hit as processing filter is not set.");
			}
			_classes = theClasses;
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing filter class: " + processingFilter);
			}
			_classes = processingFilter.filterClasses(theClasses);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Classes NOT to be processed:\n" + CollectionUtils.subtract(theClasses, _classes));
			LOGGER.debug("Classes to be processed:\n" + _classes);
		}

		for (final Iterator<SootClass> _i = _classes.iterator(); _i.hasNext() && activePart.canProceed();) {
			final SootClass _sc = _i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing class " + _sc);
			}

			for (final Iterator<IProcessor> _k = interfaceProcessors.iterator(); _k.hasNext();) {
				final IProcessor _pp = _k.next();
				_pp.callback(_sc);

				final Collection<SootField> _fields;

				if (processingFilter == null) {
					_fields = _sc.getFields();
				} else {
					_fields = processingFilter.filterFields(_sc.getFields());
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Fields NOT to be processed:\n" + CollectionUtils.subtract(_sc.getFields(), _fields));
					LOGGER.debug("Fields to be processed:\n" + _fields);
				}

				for (final Iterator<SootField> _j = _fields.iterator(); _j.hasNext();) {
					_pp.callback(_j.next());
				}
			}
			processMethods(_sc.getMethods());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("processClasses(Collection) - END");
		}
	}

	/**
	 * Processes the method body.
	 * 
	 * @param stmt in which the locals need to be processed.
	 * @param method in which <code>stmt</code> occurs.
	 * @pre method != null
	 */
	private void processLocals(final Stmt stmt, final SootMethod method) {
		@SuppressWarnings("unchecked") final Iterator<ValueBox> _i = stmt.getUseAndDefBoxes().iterator();
		final int _iEnd = stmt.getUseAndDefBoxes().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final ValueBox _vb = _i.next();
			final Value _value = _vb.getValue();

			if (_value instanceof Local && !processedLocals.contains(_value)) {
				processedLocals.add(_value);

				final Iterator<IProcessor> _j = localsProcessors.iterator();
				final int _jEnd = localsProcessors.size();

				for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
					final IProcessor _processor = _j.next();
					_processor.callback((Local) _value, method);
				}
			}
		}
	}

	/**
	 * Processes the method body.
	 * 
	 * @param method whose body needs to be processed.
	 * @throws IllegalStateException when <code>setStmtSequenceRetriever()</code> is not called with a non-null argument
	 *             before calling this method.
	 * @pre method != null
	 */
	private void processMethodBody(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Processing method " + method);
		}

		if (stmtSequencesRetriever == null) {
			final String _msg = "Please call setStmtSequenceRetriever() with a non-null argument "
					+ "before executing the controller.";
			LOGGER.error(_msg);
			throw new IllegalStateException(_msg);
		}

		try {
			processedLocals.clear();

			final Collection<List<Stmt>> _col1 = stmtSequencesRetriever.retreiveStmtSequences(method);
			final Iterator<List<Stmt>> _j = _col1.iterator();
			final int _jEnd = _col1.size();

			for (int _jIndex = 0; _jIndex < _jEnd && activePart.canProceed(); _jIndex++) {
				final Collection<Stmt> _seq;

				if (processingFilter != null) {
					_seq = processingFilter.filterStmts(_j.next());
				} else {
					_seq = _j.next();
				}

				final Iterator<Stmt> _i = _seq.iterator();
				final int _iEnd = _seq.size();

				for (int _iIndex = 0; _iIndex < _iEnd && activePart.canProceed(); _iIndex++) {
					final Stmt _stmt = _i.next();
					processLocals(_stmt, method);
					context.setStmt(_stmt);
					_stmt.apply(stmtSwitcher);
				}
			}
		} catch (final RuntimeException _e) {
			LOGGER.error("Well, exception while processing statements of a method may mean the processor does not"
					+ " recognize the given method or it's parts or method has not stored in jimple " + "representation. : "
					+ method.getSignature(), _e);
		}
	}

	/**
	 * Controls the processing of methods and their bodies.
	 * 
	 * @param theMethods to be processed.
	 * @pre theMethods != null
	 */
	private void processMethods(final Collection<SootMethod> theMethods) {
		Collection<SootMethod> _methods;

		if (processingFilter == null) {
			_methods = theMethods;
		} else {
			_methods = processingFilter.filterMethods(theMethods);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Methods NOT to be processed:\n" + CollectionUtils.subtract(theMethods, _methods));
			LOGGER.debug("Methods to be processed:\n" + _methods);
		}

		final boolean _processBody = processStmts || processValues;

		for (final Iterator<SootMethod> _j = _methods.iterator(); _j.hasNext() && activePart.canProceed();) {
			final SootMethod _sm = _j.next();
			context.setRootMethod(_sm);

			for (final Iterator<IProcessor> _k = interfaceProcessors.iterator(); _k.hasNext();) {
				_k.next().callback(_sm);
			}

			if (_processBody && _sm.isConcrete()) {
				processMethodBody(_sm);
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(_sm + " is not a concrete method.  Hence, it's body could not be retrieved.");
			}
		}
	}
}

// End of File
