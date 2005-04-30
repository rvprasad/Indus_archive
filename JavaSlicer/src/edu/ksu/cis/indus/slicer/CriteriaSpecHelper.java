
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

import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This is a helper class to generate criteria specicfication.  This is intended for internal use.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CriteriaSpecHelper {
	///CLOVER:OFF

	/**
	 * Creates a new CriteriaSpecHelper object.
	 */
	private CriteriaSpecHelper() {
	}

	///CLOVER:ON

	/**
	 * Checks if the criterion's execution is considered during slicing.
	 *
	 * @param criterion of interest.
	 *
	 * @return <code>true</code> if the criterion's execution is considered during slicing; <code>false</code>, otherwise.
	 *
	 * @pre criterion != null
	 */
	public static boolean isConsiderExecution(final ISliceCriterion criterion) {
		return ((AbstractSliceCriterion) criterion).isConsiderExecution();
	}

	/**
	 * Retrieves the criterion expression.
	 *
	 * @param criterion of interest.
	 *
	 * @return the criterion expression, if available; <code>null</code>, otherwise.
	 *
	 * @pre criterion != null
	 */
	public static ValueBox getOccurringExpr(final ISliceCriterion criterion) {
		final ValueBox _result;

		if (criterion instanceof ExprLevelSliceCriterion) {
			_result = (ValueBox) ((ExprLevelSliceCriterion) criterion).getCriterion();
		} else {
			_result = null;
		}
		return _result;
	}

	/**
	 * Retrieves the statement in which the criterion occurs.
	 *
	 * @param criterion of interest.
	 *
	 * @return the statement in which the criterion occurs.
	 *
	 * @throws IllegalArgumentException if the criterion is not a valid criterion type.
	 *
	 * @pre criterion != null
	 * @post result != null
	 */
	public static Stmt getOccurringStmt(final ISliceCriterion criterion) {
		final Stmt _result;

		if (criterion instanceof ExprLevelSliceCriterion) {
			_result = ((ExprLevelSliceCriterion) criterion).getOccurringStmt();
		} else if (criterion instanceof StmtLevelSliceCriterion) {
			_result = (Stmt) ((StmtLevelSliceCriterion) criterion).getCriterion();
		} else {
			throw new IllegalArgumentException(
				"The type of \"criterion\" has to be one of StmtLevelSliceCriterion or ExprLevelSliceCriterion.");
		}
		return _result;
	}
}

// End of File
