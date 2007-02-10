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

package edu.ksu.cis.indus.tools.slicer.criteria.generators;

import soot.SootField;
import soot.jimple.Stmt;

/**
 * This class can be used to generate fields-based slice criteria guided by a specification matcher. All the statements
 * considered for the criteria will contain a field reference in it. Instances of this class will provide only the fields for
 * filtering purposes.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class FieldBasedCriteriaGenerator
		extends AbstractStmtBasedSliceCriteriaGenerator<SootField> {

	/**
	 * @see AbstractStmtBasedSliceCriteriaGenerator#getEntityForCriteriaFiltering(soot.jimple.Stmt, soot.SootMethod)
	 */
	@Override protected SootField getEntityForCriteriaFiltering(final Stmt stmt) {
		return stmt.getFieldRef().getField();
	}

	/**
	 * @see AbstractStmtBasedSliceCriteriaGenerator#shouldConsiderStmt(soot.jimple.Stmt)
	 */
	@Override protected boolean shouldConsiderStmt(final Stmt stmt) {
		return stmt.containsFieldRef();
	}
}

// End of File
