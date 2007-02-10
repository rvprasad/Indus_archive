/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import soot.Local;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

/**
 * This class hides the work involved in the creation of slice criteria from the environment.
 * <p>
 * The interesting issue while creating slicing criteria is the inclusion information. Please refer to {@link SlicingEngine
 * SlicingEngine} for the sort of slices we discuss here. In case of expression-level slice criterion, inclusion would mean
 * that the entire statement containing the expression needs to be included independent of the slice type, i.e., backward or
 * complete. The same applies to statement-level slice criterion.
 * </p>
 * <p>
 * On the otherhand, non-inclusion in the case complete slicing does not make sense. However, in case of backward slicing
 * considering expresison-level slice criterion would mean that the expression should not be included in the sliced system
 * which only makes sense, but this introduces a dependency between the expression and the containing statement. In
 * particular, the expression is control dependent on the statement, and so the statement should be included for capturing
 * control dependency. This is exactly what our implementation does.
 * </p>
 * <p>
 * On mentioning a statement as a non-inclusive slice criterion, we interpret that as all artifacts that affect the reaching
 * of that statement. On mentioning an expression as a non-inclusive slice criterion, we interpret that as all artifacts
 * leading to the value of the expression in the statement it occurs in, hence we will include the statement containing the
 * expression in a non-inclusive manner to capture control dependency leading to that statement.
 * </p>
 * <p>
 * As we are interested in meaningful slices, inclusive expression-level slice criterion is the same as inclusive
 * statement-level slice criterion which refers to the statement that contains the expression.
 * </p>
 * <p>
 * When an expression/statement is specified as a criteria, does this mean that
 * <ul>
 * <li> the value of the expression should be preserved (the expression is executed), or </li>
 * <li> the control reaching the expression should be preserved (the expression is not executed)? </li>
 * </ul>
 * To enable the user to specify this information, each factory method includes a <code>considerExecution</code> parameter
 * that incides if the execution of the expression/statement should be captured or not. If you think that this is overly fine,
 * I would be interested in a discussion.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class SliceCriteriaFactory {

	/**
	 * This stores a reference to the factory object.
	 */
	private static final SliceCriteriaFactory SINGLETON = new SliceCriteriaFactory();

	// /CLOVER:OFF

	/**
	 * Creates a new SliceCriteriaFactory object.
	 */
	@Empty private SliceCriteriaFactory() {
		// does nothing
	}

	// /CLOVER:ON

	/**
	 * Retrieves the factory object.
	 *
	 * @return the factory object.
	 * @post result != null
	 */
	public static SliceCriteriaFactory getFactory() {
		return SINGLETON;
	}

	/**
	 * Checks if the given object is a slicing criterion.
	 *
	 * @param o to be checked.
	 * @return <code>true</code> if <code>o</code> is a slicing criterion; <code>false</code>, otherwise.
	 */
	public static boolean isSlicingCriterion(final Object o) {
		boolean _result = false;

		if (o != null) {
			_result = o instanceof ISliceCriterion;
		}
		return _result;
	}

	/**
	 * Clones the given criterion.
	 *
	 * @param criterion to be cloned.
	 * @return the clone.
	 * @throws IllegalArgumentException if criterion is not of type <code>ISliceCriterion</code>.
	 * @pre criterion != null
	 */
	public ISliceCriterion clone(final ISliceCriterion criterion) {
		final ISliceCriterion _result;

		if (criterion instanceof StmtLevelSliceCriterion) {
			final StmtLevelSliceCriterion _t = (StmtLevelSliceCriterion) criterion;
			_result = getStmtCriteria(_t.getOccurringMethod(), (Stmt) _t.getCriterion(), _t.isConsiderExecution());

			final Stack<CallTriple> _callStack = _t.getCallStack();

			if (_callStack != null) {
				_result.setCallStack(_callStack.clone());
			}
		} else if (criterion instanceof ExprLevelSliceCriterion) {
			final ExprLevelSliceCriterion _t = (ExprLevelSliceCriterion) criterion;
			_result = getExprCriteria(_t.getOccurringMethod(), _t.getOccurringStmt(), (ValueBox) _t.getCriterion(), _t
					.isConsiderExecution());

			final Stack<CallTriple> _callStack = _t.getCallStack();

			if (_callStack != null) {
				_result.setCallStack(_callStack.clone());
			}
		} else if (criterion instanceof MethodLevelSliceCriterion) {
			final MethodLevelSliceCriterion _m = (MethodLevelSliceCriterion) criterion;
			_result = getMethodCriteria(_m.getOccurringMethod(), _m.isConsiderExecution());
			final Stack<CallTriple> _callStack = _m.getCallStack();

			if (_callStack != null) {
				_result.setCallStack(_callStack.clone());
			}
		} else {
			throw new IllegalArgumentException("criterion's type " + criterion.getClass() + " is unknown.");
		}
		return _result;
	}

	/**
	 * Returns a collection of criteria which include all occurrences of the given local in the given collection of
	 * statements.
	 *
	 * @param local is the local variable whose all occurrences in <code>method</code> should be captured as slice criterion
	 * @param stmts in which the occurrences of the <code>local</code> should be captured.
	 * @param method in which the given statements occur.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching
	 *            the end of criterion is required; <code>false</code> indicates the result of the control reaching the
	 *            criterion is required.
	 * @return a collection of slice criteria.
	 * @pre stmts != null and local != null and method != null
	 * @pre method.retrieveActiveBody().getUnits().containsAll(stmts)
	 */
	public Collection<ISliceCriterion> getCriteria(final Local local, final Collection<Stmt> stmts, final SootMethod method,
			final boolean considerExecution) {
		final Collection<ISliceCriterion> _result = new HashSet<ISliceCriterion>();

		for (final Iterator<Stmt> _i = stmts.iterator(); _i.hasNext();) {
			final Stmt _stmt = _i.next();

			for (final Iterator<ValueBox> _j = _stmt.getUseAndDefBoxes().iterator(); _j.hasNext();) {
				final ValueBox _vBox = _j.next();

				if (_vBox.getValue().equals(local)) {
					final ExprLevelSliceCriterion _exprCriterion = getExprCriteria(method, _stmt, _vBox, considerExecution);
					_result.add(_exprCriterion);
				}
			}
		}

		return _result;
	}

	/**
	 * Retrieves a slice criterion for the given method.
	 *
	 * @param method of interest.
	 * @return a slice criterion.
	 * @pre method != null
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	public Collection<ISliceCriterion> getCriteria(final SootMethod method) {
		final MethodLevelSliceCriterion _criterion = new MethodLevelSliceCriterion();
		_criterion.initialize(method);
		return Collections.singleton((ISliceCriterion) _criterion);
	}

	/**
	 * Creates slice criteria from the given statement only. This is equivalent to <code>getCriterion(method, stmt, false,
	 * considerExecution)</code>.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt is the criterion.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching
	 *            the end of criterion is required; <code>false</code> indicates the result of the control reaching the
	 *            criterion is required.
	 * @return a collection of slice criterion corresponding to the given criterion.
	 * @pre method != null and stmt != null
	 */
	public Collection<ISliceCriterion> getCriteria(final SootMethod method, final Stmt stmt, final boolean considerExecution) {
		return getCriteria(method, stmt, false, considerExecution);
	}

	/**
	 * Creates slice criteria from the given statement. If <code>descend</code> is <code>true</code>, then a criterion is
	 * created for each use/def site in the given excpression.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt is the criterion.
	 * @param descend <code>true</code> indicates if a criterion should be generated for each def/use site in the statement;
	 *            <code>false</code>, otherwise.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching
	 *            the end of criterion is required; <code>false</code> indicates the result of the control reaching the
	 *            criterion is required.
	 * @return a collection of slice criterion corresponding to the given criterion.
	 * @pre method != null and stmt != null
	 */
	public Collection<ISliceCriterion> getCriteria(final SootMethod method, final Stmt stmt, final boolean descend,
			final boolean considerExecution) {
		final Collection<ISliceCriterion> _result = new HashSet<ISliceCriterion>();
		final StmtLevelSliceCriterion _stmtCriterion = getStmtCriteria(method, stmt, considerExecution);
		_result.add(_stmtCriterion);

		if (descend) {
			for (final Iterator<ValueBox> _i = stmt.getUseAndDefBoxes().iterator(); _i.hasNext();) {
				final ValueBox _vBox = _i.next();
				final ExprLevelSliceCriterion _temp = getExprCriteria(method, stmt, _vBox, considerExecution);
				_result.add(_temp);
			}
		}

		return _result;
	}

	/**
	 * Creates slice criteria from the given value. This is equivalent to <code>getCriterion(method, stmt, expression,
	 * false, considerExecution)</code>.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt in which the criterion occurs.
	 * @param expression is the criterion.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching
	 *            the end of criterion is required; <code>false</code> indicates the result of the control reaching the
	 *            criterion is required.
	 * @return a collection of slice criterion objects corresponding to the given criterion.
	 * @pre method != null and stmt != null and expression != null
	 */
	public Collection<ISliceCriterion> getCriteria(final SootMethod method, final Stmt stmt, final ValueBox expression,
			final boolean considerExecution) {
		return getCriteria(method, stmt, expression, false, considerExecution);
	}

	/**
	 * Creates slice criteria from the given value. If <code>descend</code> is <code>true</code>, then a criterion is
	 * created for each use site in the given excpression.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt in which the criterion occurs.
	 * @param expr is the criterion.
	 * @param descend <code>true</code> indicates if a criterion should be generated for each use site in the expresion;
	 *            <code>false</code>, otherwise.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching
	 *            the end of criterion is required; <code>false</code> indicates the result of the control reaching the
	 *            criterion is required.
	 * @return a collection of slice criterion objects corresponding to the given criterion.
	 * @pre method != null and stmt != null and expr != null
	 */
	public Collection<ISliceCriterion> getCriteria(final SootMethod method, final Stmt stmt, final ValueBox expr,
			final boolean descend, final boolean considerExecution) {
		final Collection<ISliceCriterion> _result = new HashSet<ISliceCriterion>();
		final ExprLevelSliceCriterion _exprCriterion = getExprCriteria(method, stmt, expr, considerExecution);
		_result.add(_exprCriterion);

		if (descend) {
			for (final Iterator<ValueBox> _i = expr.getValue().getUseBoxes().iterator(); _i.hasNext();) {
				final ValueBox _useBox = _i.next();
				final ExprLevelSliceCriterion _temp = getExprCriteria(method, stmt, _useBox, considerExecution);
				_result.add(_temp);
			}
		}
		return _result;
	}

	/**
	 * Retrieves a slice criterion for the given program point.
	 *
	 * @param method containing <code>stmt</code>.
	 * @param stmt containing <code>valueBox</code>.
	 * @param valueBox is the slice criterion.
	 * @param considerExecution indicates if the execution of the statement should be considered or just the control reaching
	 *            it.
	 * @return a slice criterion.
	 * @pre method != null and stmt != null and vlaueBox != null
	 * @post result != null
	 */
	private ExprLevelSliceCriterion getExprCriteria(final SootMethod method, final Stmt stmt, final ValueBox valueBox,
			final boolean considerExecution) {
		final ExprLevelSliceCriterion _temp = new ExprLevelSliceCriterion();
		_temp.initialize(method, stmt, valueBox);
		_temp.setConsiderExecution(considerExecution);
		return _temp;
	}

	/**
	 * Retrieves a slice criterion for the given method.
	 *
	 * @param method of interest.
	 * @param considerExecution <i>place holder for future use</i>.
	 * @return a slice criterion.
	 * @pre method != null
	 * @post result != null
	 */
	private MethodLevelSliceCriterion getMethodCriteria(final SootMethod method, final boolean considerExecution) {
		final MethodLevelSliceCriterion _temp = new MethodLevelSliceCriterion();
		_temp.initialize(method);
		_temp.setConsiderExecution(considerExecution);
		return _temp;
	}

	/**
	 * Retrieves a slice criterion for the given statement.
	 *
	 * @param method containing <code>stmt</code>.
	 * @param stmt is the slice criterion.
	 * @param considerExecution indicates if the execution of the statement should be considered or just the control reaching
	 *            it.
	 * @return a slice criterion.
	 * @pre method != null and stmt != null
	 * @post result != null
	 */
	private StmtLevelSliceCriterion getStmtCriteria(final SootMethod method, final Stmt stmt, final boolean considerExecution) {
		final StmtLevelSliceCriterion _stmtCriterion = new StmtLevelSliceCriterion();
		_stmtCriterion.initialize(method, stmt);
		_stmtCriterion.setConsiderExecution(considerExecution);
		return _stmtCriterion;
	}
}

// End of File
