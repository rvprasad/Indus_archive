
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

package edu.ksu.cis.indus.tools.slicer.criteria.generators;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class can be used to generate fields-based slice criteria guided by a specification matcher.  All the statements
 * considered for the criteria will contain a field reference in it.  Instances of this class will provide only the fields
 * for filtering purposes.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class FieldBasedCriteriaGenerator
  extends AbstractStmtBasedSliceCriteriaGenerator {
	/**
	 * @see AbstractStmtBasedSliceCriteriaGenerator#getEntityForCriteriaFiltering(soot.jimple.Stmt, soot.SootMethod)
	 */
	protected Object getEntityForCriteriaFiltering(final Stmt stmt, final SootMethod sm) {
		return stmt.getFieldRef().getField();
	}

	/**
	 * @see AbstractStmtBasedSliceCriteriaGenerator#shouldConsiderStmt(soot.jimple.Stmt)
	 */
	protected boolean shouldConsiderStmt(final Stmt stmt) {
		return stmt.containsFieldRef();
	}
}

// End of File
