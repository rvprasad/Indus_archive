
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

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;


/**
 * This class provides the logic to detect parts of a forward slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ForwardSlicingPart
  implements IDirectionSensitivePartOfSlicingEngine {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ForwardSlicingPart.class);

	/** 
	 * The engine with which this part is a part of.
	 */
	private final SlicingEngine engine;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theEngine of which this part is a part of.
	 *
	 * @pre theEngine != null
	 */
	ForwardSlicingPart(final SlicingEngine theEngine) {
		engine = theEngine;
	}

	/**
	 * @see DependenceExtractor.IDependenceRetriver#getDependences(IDependencyAnalysis, Object, Object)
	 */
	public Collection getDependences(final IDependencyAnalysis analysis, final Object entity, final Object context) {
		final Collection _result = new HashSet();
		final Object _direction = analysis.getDirection();

		if (_direction.equals(IDependencyAnalysis.FORWARD_DIRECTION) || _direction.equals(IDependencyAnalysis.DIRECTIONLESS)) {
			_result.addAll(analysis.getDependees(entity, context));
		} else if (_direction.equals(IDependencyAnalysis.BI_DIRECTIONAL)) {
			_result.addAll(analysis.getDependents(entity, context));
		} else if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("Trying to retrieve FORWARD dependence from a dependence analysis that is BACKWARD direction.");
		}

		return _result;
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#generateCriteriaForTheCallToMethod(soot.SootMethod,     soot.SootMethod,
	 * 		soot.jimple.Stmt)
	 */
	public void generateCriteriaForTheCallToMethod(final SootMethod callee, final SootMethod caller, final Stmt callStmt) {
		/*
		 * _stmt may be an assignment statement.  Hence, we want the control to reach the statement but not leave
		 * it.  However, the execution of the invoke expression should be considered as it is requied to reach the
		 * callee.  Likewise, we want to include the expression but not all arguments.  We rely on the reachable
		 * parameters to suck in the arguments.  So, we generate criteria only for the invocation expression and
		 * not the arguments.  Refer to transformAndGenerateToNewCriteriaForXXXX for information about how
		 * invoke expressions are handled differently.
		 */
		engine.generateSliceStmtCriterion(callStmt, caller, true);
		engine.getCollector().includeInSlice(callStmt.getInvokeExprBox());

		if (!callee.isStatic()) {
			final ValueBox _vBox = ((InstanceInvokeExpr) callStmt.getInvokeExpr()).getBaseBox();
			engine.generateSliceExprCriterion(_vBox, callStmt, caller, false);
		}

		if (callStmt instanceof AssignStmt) {
			final AssignStmt _defStmt = (AssignStmt) callStmt;
			engine.generateSliceExprCriterion(_defStmt.getLeftOpBox(), callStmt, caller, false);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generateCriteriaToIncludeCallees(soot.jimple.Stmt,
	 * 		soot.SootMethod, java.util.Collection)
	 */
	public void generateCriteriaToIncludeCallees(final Stmt stmt, final SootMethod caller, final Collection callees) {
		final InvokeExpr _expr = stmt.getInvokeExpr();

		if (_expr instanceof InstanceInvokeExpr) {
			final Iterator _i = callees.iterator();
			final int _iEnd = callees.size();

			if (engine.getCallStackCache() == null) {
				engine.setCallStackCache(new Stack());
			}
			engine.getCallStackCache().push(new CallTriple(caller, stmt, _expr));

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootMethod _callee = (SootMethod) _i.next();
				final Collection _units = engine.getBasicBlockGraphManager().getStmtList(_callee);

				for (final Iterator _j = _units.iterator(); _j.hasNext();) {
					final Stmt _stmt = (Stmt) _j.next();

					if (_stmt instanceof IdentityStmt) {
						final IdentityStmt _idStmt = (IdentityStmt) _stmt;
						final Value _rightOp = _idStmt.getRightOp();

						if (_rightOp instanceof ThisRef) {
							engine.generateSliceStmtCriterion(_idStmt, _callee, false);
							break;
						}
					}
				}
			}
			engine.getCallStackCache().pop();

			if (engine.getCallStackCache().isEmpty()) {
				engine.setCallStackCache(null);
			}
		}
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#processLocalAt(Local, Stmt, SootMethod)
	 */
	public void processLocalAt(final Local local, final Stmt stmt, final SootMethod method) {
		if (stmt.containsArrayRef()) {
			final ArrayRef _ref = stmt.getArrayRef();
			final Collection _useBoxes = _ref.getUseBoxes();
			final Iterator _i = _useBoxes.iterator();
			final int _iEnd = _useBoxes.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final ValueBox _vb = (ValueBox) _i.next();

				if (_vb.getValue().equals(local)) {
					engine.generateSliceStmtCriterion(stmt, method, false);
					break;
				}
			}
		} else if (stmt.containsFieldRef()) {
			final FieldRef _ref = stmt.getFieldRef();
			final Collection _useBoxes = _ref.getUseBoxes();
			final Iterator _i = _useBoxes.iterator();
			final int _iEnd = _useBoxes.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final ValueBox _vb = (ValueBox) _i.next();

				if (_vb.getValue().equals(local)) {
					engine.generateSliceStmtCriterion(stmt, method, false);
					break;
				}
			}
		} else if (stmt.containsInvokeExpr()) {
			final Collection _useBoxes = stmt.getInvokeExpr().getUseBoxes();
			final Iterator _j = _useBoxes.iterator();
			final int _jEnd = _useBoxes.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final ValueBox _vb = (ValueBox) _j.next();

				if (_vb.getValue().equals(local)) {
					engine.generateSliceExprCriterion(_vb, stmt, method, true);
				}
			}

			final InvokeExpr _invokeExpr = stmt.getInvokeExpr();
			final List _args = _invokeExpr.getArgs();
			final int _argIndex = _args.indexOf(local);

			if (_argIndex > -1) {
				final Context _ctxt = new Context();
				_ctxt.setRootMethod(method);
				_ctxt.setStmt(stmt);

				final Collection _callees = engine.getCgi().getCallees(_invokeExpr, _ctxt);

				if (engine.getCallStackCache() == null) {
					engine.setCallStackCache(new Stack());
				}
				engine.getCallStackCache().push(new CallTriple(method, stmt, stmt.getInvokeExpr()));

				generateCriteriaToIncludeArgumentReadStmts(_argIndex, _callees);
				engine.getCallStackCache().pop();

				if (engine.getCallStackCache().isEmpty()) {
					engine.setCallStackCache(null);
				}
			}
		}
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#processNewExpr(Stmt, SootMethod)
	 */
	public void processNewExpr(final Stmt stmt, final SootMethod method) {
		// DOES NOTHING
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#processParameterRef(ValueBox, SootMethod)
	 */
	public void processParameterRef(final ValueBox box, final SootMethod method) {
		// DOES NOTHING
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformExpr(ValueBox, Stmt)
	 */
	public Collection retrieveValueBoxesToTransformExpr(final ValueBox valueBox, final Stmt stmt) {
		final Collection _valueBoxes = new HashSet();
		_valueBoxes.add(valueBox);

		final Value _value = valueBox.getValue();

		//if it is an invocation expression, we do not want to include the arguments/sub-expressions. 
		// in case of instance invocation, we do want to include the receiver position expression.
		if (_value instanceof InvokeExpr) {
			_valueBoxes.addAll(_value.getUseBoxes());

			if (_value instanceof InstanceInvokeExpr) {
				_valueBoxes.add(((InstanceInvokeExpr) _value).getBaseBox());
			}
		}

		/*
		 *  Note that l-position is the lhs of an assignment statement whereas an l-value is value that occurs the l-position
		 * and is defined.  In a[i] = v; a is the l-value whereas i is a r-value in the l-position and v is r-value in the
		 * r-position.
		 */

		// we include the unincluded l-values in case the given value box appears in the r-position. 
		if (stmt instanceof DefinitionStmt && ((DefinitionStmt) stmt).getLeftOp().getUseBoxes().contains(valueBox)) {
			_valueBoxes.addAll(engine.collector.getUncollected(stmt.getDefBoxes()));
		}

		return _valueBoxes;
	}

	/**
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformStmt(Stmt)
	 */
	public Collection retrieveValueBoxesToTransformStmt(final Stmt stmt) {
		return new HashSet(stmt.getUseBoxes());
	}

	/**
	 * Generates criteria to include statements that pop the arguments of the call stack in the callees.
	 *
	 * @param argIndex is the index of the argument whose read should be included.
	 * @param callees are the methods called.
	 *
	 * @pre argIndex != null and callees != null
	 * @pre callees.oclIsKindOf(Collection(SootMethod))
	 * @pre callees->forall(o | o.getParameterCount() > argIndex)
	 */
	private void generateCriteriaToIncludeArgumentReadStmts(final int argIndex, final Collection callees) {
		final Iterator _i = callees.iterator();
		final int _iEnd = callees.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _callee = (SootMethod) _i.next();
			final Collection _units = engine.getBasicBlockGraphManager().getStmtList(_callee);

			for (final Iterator _j = _units.iterator(); _j.hasNext();) {
				final Stmt _stmt = (Stmt) _j.next();

				if (_stmt instanceof IdentityStmt) {
					final IdentityStmt _idStmt = (IdentityStmt) _stmt;
					final Value _rightOp = _idStmt.getRightOp();

					if (_rightOp instanceof ParameterRef && ((ParameterRef) _rightOp).getIndex() == argIndex) {
						engine.generateSliceStmtCriterion(_idStmt, _callee, false);
						break;
					}
				}
			}
		}
	}
}

// End of File
