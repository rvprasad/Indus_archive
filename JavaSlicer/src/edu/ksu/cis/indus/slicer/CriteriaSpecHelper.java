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
import soot.ValueBox;
import soot.jimple.Stmt;

/**
 * This is a helper class to generate criteria specicfication. This is intended for internal use.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CriteriaSpecHelper {

	// /CLOVER:OFF

	/**
	 * Creates a new CriteriaSpecHelper object.
	 */
	@Empty private CriteriaSpecHelper() {
		// does nothing
	}

	// /CLOVER:ON

	/**
	 * Retrieves the criterion expression.
	 * 
	 * @param criterion of interest.
	 * @return the criterion expression, if available; <code>null</code>, otherwise.
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
	 * @return the statement in which the criterion occurs.
	 * @throws IllegalArgumentException if the criterion is not a valid criterion type.
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

	/**
	 * Checks if the criterion's execution is considered during slicing.
	 * 
	 * @param criterion of interest.
	 * @return <code>true</code> if the criterion's execution is considered during slicing; <code>false</code>,
	 *         otherwise.
	 * @pre criterion != null
	 */
	public static boolean isConsiderExecution(final ISliceCriterion criterion) {
		return ((AbstractSliceCriterion) criterion).isConsiderExecution();
	}
}

// End of File
