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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis.Direction;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(ForwardSlicingPart.class);

	/**
	 * The engine with which this part is a part of.
	 */
	private final SlicingEngine engine;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param theEngine of which this part is a part of.
	 * @pre theEngine != null
	 */
	ForwardSlicingPart(final SlicingEngine theEngine) {
		engine = theEngine;
	}

	/**
	 * This implementation always returns <code>false</code>. {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#continueProcessing()
	 */
	@Empty public boolean continueProcessing() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#generateCriteriaForTheCallToMethod(soot.SootMethod, soot.SootMethod,
	 *      soot.jimple.Stmt)
	 */
	public void generateCriteriaForTheCallToMethod(final SootMethod callee, final SootMethod caller, final Stmt callStmt) {
		/*
		 * _stmt may be an assignment statement. Hence, we want the control to reach the statement but not leave it. However,
		 * the execution of the invoke expression should be considered as it is requied to reach the callee. Likewise, we want
		 * to include the expression but not all arguments. We rely on the reachable parameters to suck in the arguments. So,
		 * we generate criteria only for the invocation expression and not the arguments. Refer to
		 * transformAndGenerateToNewCriteriaForXXXX for information about how invoke expressions are handled differently.
		 */
		engine.generateStmtLevelSliceCriterion(callStmt, caller, true);
		engine.includeInSlice(callStmt.getInvokeExprBox());

		if (!callee.isStatic()) {
			final ValueBox _vBox = ((InstanceInvokeExpr) callStmt.getInvokeExpr()).getBaseBox();
			engine.generateExprLevelSliceCriterion(_vBox, callStmt, caller, false);
		}

		if (callStmt instanceof AssignStmt) {
			final AssignStmt _defStmt = (AssignStmt) callStmt;
			engine.generateExprLevelSliceCriterion(_defStmt.getLeftOpBox(), callStmt, caller, true);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#generateCriteriaToIncludeCallees(soot.jimple.Stmt,
	 *      soot.SootMethod, java.util.Collection)
	 */
	public void generateCriteriaToIncludeCallees(final Stmt stmt, final SootMethod caller,
			final Collection<SootMethod> callees) {
		final InvokeExpr _expr = stmt.getInvokeExpr();

		if (_expr instanceof InstanceInvokeExpr) {
			final Iterator<SootMethod> _i = callees.iterator();
			final int _iEnd = callees.size();

			engine.enterMethod(new CallTriple(caller, stmt, _expr));

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final SootMethod _callee = _i.next();
				final Collection<Stmt> _units = engine.getBasicBlockGraphManager().getStmtList(_callee);

				for (final Iterator<Stmt> _j = _units.iterator(); _j.hasNext();) {
					final Stmt _stmt = _j.next();

					if (_stmt instanceof IdentityStmt) {
						final IdentityStmt _idStmt = (IdentityStmt) _stmt;
						final Value _rightOp = _idStmt.getRightOp();

						if (_rightOp instanceof ThisRef) {
							engine.generateStmtLevelSliceCriterion(_idStmt, _callee, false);
							break;
						}
					}
				}
			}
			engine.returnFromMethod();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DependenceExtractor.IDependenceRetriver#getDependences(IDependencyAnalysis, Object, SootMethod )
	 */
	public Collection<Object> getDependences(final IDependencyAnalysis analysis, final Object entity, final SootMethod method) {
		final Collection<Object> _result = new HashSet<Object>();
		final Object _dir = analysis.getDirection();

		if (_dir.equals(Direction.FORWARD_DIRECTION)) {
			_result.addAll(analysis.getDependees(entity, method));
		} else if (_dir.equals(Direction.BI_DIRECTIONAL)) {
			_result.addAll(analysis.getDependents(entity, method));
		} else if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("Trying to retrieve FORWARD dependence from a dependence analysis that is BACKWARD in direction. -- "
					+ analysis.getClass() + " - " + _dir);
		}

		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see DependenceExtractor.IDependenceRetriver#getEntityForIdentifierBasedDataDA(soot.Local, soot.jimple.Stmt)
	 */
	public Object getEntityForIdentifierBasedDataDA(@SuppressWarnings("unused") final Local local, final Stmt stmt) {
		final Object _result;
		if (stmt instanceof DefinitionStmt) {
			_result = stmt;
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#processLocalAt(Local, Stmt, SootMethod)
	 */
	public void processLocalAt(final Local local, final Stmt stmt, final SootMethod method) {
		engine.generateStmtLevelSliceCriterion(stmt, method, true);

		if (stmt instanceof DefinitionStmt) {
			final Collection<ValueBox> _boxes = ((DefinitionStmt) stmt).getRightOp().getUseBoxes();
			for (final ValueBox _box : _boxes) {
				final Value _v = _box.getValue();
				if (_v == local) {
					engine.generateExprLevelSliceCriterion(_box, stmt, method, false);
					break;
				}
			}
		}

		if (stmt.containsInvokeExpr()) {
			final Collection<ValueBox> _useBoxes = stmt.getInvokeExpr().getUseBoxes();
			final Iterator<ValueBox> _j = _useBoxes.iterator();
			final int _jEnd = _useBoxes.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final ValueBox _vb = _j.next();

				if (_vb.getValue().equals(local)) {
					engine.generateExprLevelSliceCriterion(_vb, stmt, method, true);
				}
			}

			final InvokeExpr _invokeExpr = stmt.getInvokeExpr();
			final List<Value> _args = _invokeExpr.getArgs();
			final int _argIndex = _args.indexOf(local);

			if (_argIndex > -1) {
				final Context _ctxt = new Context();
				_ctxt.setRootMethod(method);
				_ctxt.setStmt(stmt);

				final Collection<SootMethod> _callees = engine.getCgi().getCallees(_invokeExpr, _ctxt);

				engine.enterMethod(new CallTriple(method, stmt, stmt.getInvokeExpr()));
				generateCriteriaToIncludeArgumentReadStmts(_argIndex, _callees);
				engine.returnFromMethod();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#processNewExpr(Stmt, SootMethod)
	 */
	public void processNewExpr(final Stmt stmt, final SootMethod method) {
		if (stmt instanceof AssignStmt) {
			final AssignStmt _as = (AssignStmt) stmt;
			engine.generateExprLevelSliceCriterion(_as.getLeftOpBox(), stmt, method, false);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#processParameterRef(IdentityStmt, SootMethod)
	 */
	public void processParameterRef(final IdentityStmt stmt, final SootMethod method) {
		engine.generateExprLevelSliceCriterion(stmt.getLeftOpBox(), stmt, method, false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.slicer.IDirectionSensitivePartOfSlicingEngine#reset()
	 */
	@Empty public void reset() {
		// DOES NOTHING.
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformExpr(ValueBox, Stmt)
	 */
	public Collection<ValueBox> retrieveValueBoxesToTransformExpr(final ValueBox valueBox, final Stmt stmt) {
		final Collection<ValueBox> _valueBoxes = new HashSet<ValueBox>();
		_valueBoxes.add(valueBox);

		final Value _value = valueBox.getValue();

		// if it is an invocation expression, we do not want to include the arguments/sub-expressions.
		// in case of instance invocation, we do want to include the receiver position expression.
		if (_value instanceof InvokeExpr) {
			_valueBoxes.addAll(_value.getUseBoxes());

			if (_value instanceof InstanceInvokeExpr) {
				_valueBoxes.add(((InstanceInvokeExpr) _value).getBaseBox());
			}
		}

		/*
		 * Note that l-position is the lhs of an assignment statement whereas an l-value is value that occurs the l-position
		 * and is defined. In a[i] = v; a is the l-value whereas i is a r-value in the l-position and v is r-value in the
		 * r-position.
		 */

		// we include the unincluded l-values in case the given value box appears in the r-position.
		if (stmt instanceof DefinitionStmt && ((DefinitionStmt) stmt).getLeftOp().getUseBoxes().contains(valueBox)) {
			_valueBoxes.addAll(engine.getCollector().getUncollected(stmt.getDefBoxes()));
		}

		return _valueBoxes;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see IDirectionSensitivePartOfSlicingEngine#retrieveValueBoxesToTransformStmt(Stmt)
	 */
	public Collection<ValueBox> retrieveValueBoxesToTransformStmt(final Stmt stmt) {
		return new HashSet<ValueBox>(stmt.getUseBoxes());
	}

	/**
	 * Generates criteria to include statements that pop the arguments of the call stack in the callees.
	 * 
	 * @param argIndex is the index of the argument whose read should be included.
	 * @param callees are the methods called.
	 * @pre argIndex != null and callees != null
	 * @pre callees.oclIsKindOf(Collection(SootMethod))
	 * @pre callees->forall(o | o.getParameterCount() > argIndex)
	 */
	private void generateCriteriaToIncludeArgumentReadStmts(final int argIndex, final Collection<SootMethod> callees) {
		final Iterator<SootMethod> _i = callees.iterator();
		final int _iEnd = callees.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _callee = _i.next();
			final Collection<Stmt> _units = engine.getBasicBlockGraphManager().getStmtList(_callee);

			for (final Iterator<Stmt> _j = _units.iterator(); _j.hasNext();) {
				final Stmt _stmt = _j.next();

				if (_stmt instanceof IdentityStmt) {
					final IdentityStmt _idStmt = (IdentityStmt) _stmt;
					final Value _rightOp = _idStmt.getRightOp();

					if (_rightOp instanceof ParameterRef && ((ParameterRef) _rightOp).getIndex() == argIndex) {
						engine.generateStmtLevelSliceCriterion(_idStmt, _callee, false);
						break;
					}
				}
			}
		}
	}
}

// End of File
