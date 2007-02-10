
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

package edu.ksu.cis.indus.interfaces;

import soot.SootMethod;

import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;


/**
 * This interface is used to extract the invocation site that calls the constructor on the created instance. In Jimple, one
 * can pick the new expression and pick the immediate following statement with <code>&lt;init&gt;</code> invocation
 * expression.  Note that both these statements should occur in the same method. However, this can be incorrect in some
 * cases.  A more sound approach is to this approach is to use a data-flow based  approach.
 */
public interface INewExpr2InitMapper {
	/**
	 * Retrieves the init invocation expression containing statement corresponding to the given new expression containing
	 * statement.
	 *
	 * @param newExprStmt is the statement with the new expression.
	 * @param method in which <code>newExprStmt</code> occurs.
	 *
	 * @return the statement in which the corresponding init invocation expression occurring statement
	 *
	 * @post result != null and result.getInvokeExpr().oclIsKindOf(SpecialInvokeExpr)
	 */
	InvokeStmt getInitCallStmtForNewExprStmt(final Stmt newExprStmt, final SootMethod method);
}

// End of File
