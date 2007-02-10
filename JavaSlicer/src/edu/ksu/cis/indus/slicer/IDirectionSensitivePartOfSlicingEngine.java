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

import java.util.Collection;

import soot.Local;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.IdentityStmt;
import soot.jimple.Stmt;

/**
 * This interface provides methods to control the direction of slicing.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
interface IDirectionSensitivePartOfSlicingEngine
		extends DependenceExtractor.IDependenceRetriver {

	/**
	 * Controls if processing of slice criteria can end. This is involed by the engine when there are no criteria/work left to
	 * process. This method would be a good place to inject new criteria/work based on some caching mechanism. So, if the
	 * implementation adds new criteria/work then it should return <code>true</code> as the return value.
	 * 
	 * @return <code>true</code> if the processing should continue; <code>false</code>, otherwise.
	 */
	boolean continueProcessing();

	/**
	 * Generates new criteria to capture the call to the given method <code>callee</code>.
	 * <p>
	 * This should be called from within the caller's context (callStack containing the call to the caller as TOS).
	 * </p>
	 * 
	 * @param callee obviously.
	 * @param caller obviously.
	 * @param callStmt in the <code>caller</code> at which <code>callee</code> was called.
	 * @pre callee != null and caller != null and callStmt != null
	 * @pre callStmt.containsInvokeExpr()
	 */
	void generateCriteriaForTheCallToMethod(SootMethod callee, SootMethod caller, Stmt callStmt);

	/**
	 * Generate new criteria to include the given called methods at given statement in the caller.
	 * <p>
	 * This should be called from within the caller's context (callStack does not contain the call to the callee as TOS).
	 * </p>
	 * 
	 * @param callStmt at which the invocation occurs.
	 * @param caller in which the invocation occurs.
	 * @param callees that are invoked.
	 * @pre callStmt != null and caller != null and callees != null
	 * @pre callStmt.containsInvokeExpr()
	 */
	void generateCriteriaToIncludeCallees(Stmt callStmt, SootMethod caller, Collection<SootMethod> callees);

	/**
	 * Process the local in the given statement for inclusion in the slice.
	 * 
	 * @param local obviosly.
	 * @param depStmt in which <code>local</code> occurs.
	 * @param method in which <code>stmt</code> occurs.
	 * @pre local != null and depStmt != null and method != null
	 */
	void processLocalAt(Local local, Stmt depStmt, SootMethod method);

	/**
	 * Process the new expression that occurs in the given statement and method for inclusion in the slice.
	 * <p>
	 * This should be called from within the callee's context (callStack containing the call to the callee as TOS).
	 * </p>
	 * 
	 * @param stmt containing the new expression.
	 * @param method containing <code>stamt</code>.
	 * @pre stmt != null and method != null
	 * @pre stmt.getUseBoxes()->exists(o | o.getValue().oclIsKindOf(NewExpr))
	 */
	void processNewExpr(final Stmt stmt, final SootMethod method);

	/**
	 * Process the parameter reference in <code>stmt</code> for inclusion in the slice.
	 * <p>
	 * This should be called from within the method's context (callStack containing the call to method as TOS).
	 * </p>
	 * 
	 * @param stmt in which the parameter is referenced.
	 * @param method containting <code>stmt</code>.
	 * @pre method != null and stmt != null
	 */
	void processParameterRef(IdentityStmt stmt, SootMethod method);

	/**
	 * Reset the part.
	 */
	void reset();

	/**
	 * Retrieves the value boxes at the given given program point that should be considered while transforming the given
	 * program point. Other value boxes occurring in the statement may also be included in the result.
	 * 
	 * @param valueBox is the program point being transformed.
	 * @param stmt in which the program point occurs.
	 * @return a collection of value boxes.
	 * @pre valueBox != null and stmt != null
	 * @post result != null
	 */
	Collection<ValueBox> retrieveValueBoxesToTransformExpr(ValueBox valueBox, Stmt stmt);

	/**
	 * Retrieves the value boxes reachable from the given statement (via containment relationship) that should be considered
	 * while transforming the given statement.
	 * 
	 * @param stmt is the statement being transformed.
	 * @return a collection of value boxes.
	 * @pre stmt != null
	 * @post result != null
	 */
	Collection<ValueBox> retrieveValueBoxesToTransformStmt(Stmt stmt);
}

// End of File
