
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import soot.Local;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This class hides the work involved in the creation of slice criteria from the environment.
 * 
 * <p>
 * The interesting issue while  creating slicing criteria is the inclusion information.  Please refer to {@link SlicingEngine
 * SlicingEngine}  for the sort of slices we discuss here.  In case of expression-level slice criterion, inclusion would
 * mean  that the entire statement containing the expression needs to be included independent of the slice type, i.e.,
 * backward or complete.  The same applies to statement-level slice criterion.
 * </p>
 * 
 * <p>
 * On the otherhand, non-inclusion in the case complete slicing does not make sense.  However, in case of backward slicing
 * considering expresison-level slice criterion would mean that the expression should not be included in the sliced system
 * which only makes sense, but this introduces a dependency between the expression and the containing statement.  In
 * particular, the expression is control dependent on the statement, and so the statement should be included for capturing
 * control dependency.  This is exactly what  our implementation does.
 * </p>
 * 
 * <p>
 * On mentioning a statement as a non-inclusive slice criterion, we interpret that as all artifacts that affect the reaching
 * of that statement. On mentioning  an expression as a non-inclusive slice criterion, we interpret that as all artifacts
 * leading to the value of the expression in the statement  it occurs in, hence we will include the statement containing the
 * expression in a non-inclusive manner to capture control dependency leading to that statement.
 * </p>
 * 
 * <p>
 * As we are interested in meaningful slices, inclusive expression-level slice criterion is the same as inclusive
 * statement-level slice criterion which refers to the statement that contains the expression.
 * </p>
 * 
 * <p>
 * When an expression/statement is specified as a criteria, does this mean that
 * 
 * <ul>
 * <li>
 * the value of the expression should be preserved (the expression is executed), or
 * </li>
 * <li>
 * the control reaching the expression should be preserved (the expression is not executed)?
 * </li>
 * </ul>
 * 
 * To enable the user to specify this information, each factory method includes a <code>considerExecution</code> parameter
 * that incides if the execution of the expression/statement should be captured or not.  If you think that this is  overly
 * fine, I would be interested in a discussion.
 * </p>
 * 
 * <p>
 * Please note that <code>ISliceCriterion</code> extends <code>IPoolable</code>.  This means that all criteria that were
 * created should be returned to the pool.  Hence, the user is responsible to call <code>returnToPool()</code> on all the
 * criterion created via <code>getCriterion()</code> methods in this class.
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

	///CLOVER:OFF

	/**
	 * Creates a new SliceCriteriaFactory object.
	 */
	private SliceCriteriaFactory() {
	}

	///CLOVER:ON

	/**
	 * Retrieves the factory object.
	 *
	 * @return the factory object.
	 *
	 * @post result != null
	 */
	public static SliceCriteriaFactory getFactory() {
		return SINGLETON;
	}

	/**
	 * Creates slice criteria from the given value.  This is equivalent to <code>getCriterion(method, stmt, expression,
	 * false, considerExecution)</code>.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt in which the criterion occurs.
	 * @param expression is the criterion.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching the
	 * 		  end of criterion is required; <code>false</code> indicates the result of the control reaching the  criterion
	 * 		  is required.
	 *
	 * @return a collection of slice criterion objects corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null and expression != null
	 * @post result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	public Collection getCriteria(final SootMethod method, final Stmt stmt, final ValueBox expression,
		final boolean considerExecution) {
		return getCriteria(method, stmt, expression, false, considerExecution);
	}

	/**
	 * Creates slice criteria from the given value.  If <code>descend</code> is <code>true</code>, then a criterion is
	 * created  for each use site in the given excpression.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt in which the criterion occurs.
	 * @param expr is the criterion.
	 * @param descend <code>true</code> indicates if a criterion should be generated for each use site in the expresion;
	 * 		  <code>false</code>, otherwise.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching the
	 * 		  end of criterion is required; <code>false</code> indicates the result of the control reaching the  criterion
	 * 		  is required.
	 *
	 * @return a collection of slice criterion objects corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null and expr != null
	 * @post result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	public Collection getCriteria(final SootMethod method, final Stmt stmt, final ValueBox expr, final boolean descend,
		final boolean considerExecution) {
		final Collection _result = new HashSet();
		final SliceExpr _exprCriterion = SliceExpr.getSliceExpr();
		_exprCriterion.initialize(method, stmt, expr);
		_exprCriterion.setConsiderExecution(considerExecution);
		_result.add(_exprCriterion);

		if (descend) {
			for (final Iterator _i = expr.getValue().getUseBoxes().iterator(); _i.hasNext();) {
				final ValueBox _useBox = (ValueBox) _i.next();
				final SliceExpr _temp = SliceExpr.getSliceExpr();
				_temp.initialize(method, stmt, _useBox);
				_temp.setConsiderExecution(considerExecution);
				_result.add(_temp);
			}
		}
		return _result;
	}

	/**
	 * Creates slice criteria from the given statement only.  This is equivalent to <code>getCriterion(method, stmt,
	 * false, considerExecution)</code>.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt is the criterion.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching the
	 * 		  end of criterion is required; <code>false</code> indicates the result of the control reaching the  criterion
	 * 		  is required.
	 *
	 * @return a collection of slice criterion corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null
	 * @post result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriteria(final SootMethod method, final Stmt stmt, final boolean considerExecution) {
		return getCriteria(method, stmt, false, considerExecution);
	}

	/**
	 * Creates slice criteria from the given statement.  If <code>descend</code> is <code>true</code>, then a criterion is
	 * created for each use/def site in the given excpression.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt is the criterion.
	 * @param descend <code>true</code> indicates if a criterion should be generated for each def/use site in the statement;
	 * 		  <code>false</code>, otherwise.
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching the
	 * 		  end of criterion is required; <code>false</code> indicates the result of the control reaching the  criterion
	 * 		  is required.
	 *
	 * @return a collection of slice criterion corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null
	 * @post result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriteria(final SootMethod method, final Stmt stmt, final boolean descend,
		final boolean considerExecution) {
		final Collection _result = new HashSet();
		final SliceStmt _stmtCriterion = SliceStmt.getSliceStmt();
		_stmtCriterion.initialize(method, stmt);
		_stmtCriterion.setConsiderExecution(considerExecution);
		_result.add(_stmtCriterion);

		if (descend) {
			for (final Iterator _i = stmt.getUseAndDefBoxes().iterator(); _i.hasNext();) {
				final ValueBox _vBox = (ValueBox) _i.next();
				final SliceExpr _temp = SliceExpr.getSliceExpr();
				_temp.initialize(method, stmt, _vBox);
				_temp.setConsiderExecution(considerExecution);
				_result.add(_temp);
			}
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
	 * @param considerExecution <code>true</code> indicates the result of executing the criterion or the control reaching the
	 * 		  end of criterion is required; <code>false</code> indicates the result of the control reaching the  criterion
	 * 		  is required.
	 *
	 * @return a collection of slice criteria.
	 *
	 * @pre stmts != null and local != null and method != null
	 * @pre stmt.oclIsKindOf(Collection(Stmt)) and method.retrieveActiveBody().getUnits().containsAll(stmts)
	 * @post result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	public Collection getCriteria(final Local local, final Collection stmts, final SootMethod method,
		final boolean considerExecution) {
		final Collection _result = new HashSet();

		for (final Iterator _i = stmts.iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();

			for (final Iterator _j = _stmt.getUseAndDefBoxes().iterator(); _j.hasNext();) {
				final ValueBox _vBox = (ValueBox) _j.next();

				if (_vBox.getValue().equals(local)) {
					final SliceExpr _exprCriterion = SliceExpr.getSliceExpr();
					_exprCriterion.initialize(method, _stmt, _vBox);
					_exprCriterion.setConsiderExecution(considerExecution);
					_result.add(_exprCriterion);
				}
			}
		}

		return _result;
	}

	/**
	 * Checks if the given object is a slicing criterion.
	 *
	 * @param o to be checked.
	 *
	 * @return <code>true</code> if <code>o</code> is a slicing criterion; <code>false</code>, otherwise.
	 */
	public static boolean isSlicingCriterion(final Object o) {
		boolean _result = false;

		if (o != null) {
			_result = o instanceof AbstractSliceCriterion;
		}
		return _result;
	}
}

// End of File
