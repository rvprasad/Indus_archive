
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

import soot.Local;
import soot.SootMethod;
import soot.ValueBox;

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
	 * Generates new criteria to capture the call to the given method <code>callee</code>.
	 *
	 * @param callee obviously.
	 * @param caller obviously.
	 * @param callStmt in the <code>caller</code> at which <code>callee</code> was called.
	 *
	 * @pre callee != null and caller != null and callStmt != null
	 * @pre callStmt.containsInvokeExpr()
	 */
	void generateCriteriaForTheCallToMethod(SootMethod callee, SootMethod caller, Stmt callStmt);

	/**
	 * Generate new criteria to include the given called methods at given statement in the caller.
	 *
	 * @param callStmt at which the invocation occurs.
	 * @param caller in which the invocation occurs.
	 * @param callees that are invoked.
	 *
	 * @pre callStmt != null and caller != null and callees != null
	 * @pre callStmt.containsInvokeExpr()
	 * @pre callees.oclIsKindOf(Collection(SootMethod))
	 */
	void generateCriteriaToIncludeCallees(Stmt callStmt, SootMethod caller, Collection callees);

	/**
	 * Process the local at program point <code>local</code> for inclusion in the slice.
	 *
	 * @param local obviosly.
	 * @param stmt in which <code>local</code> occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre local != null and stmt != null and method != null
	 */
	void processLocalAt(Local local, Stmt stmt, SootMethod method);

	/**
	 * Process the new expression that occurs in the given statement and method for inclusion in the slice.
	 *
	 * @param stmt containing the new expression.
	 * @param method containing <code>stamt</code>.
	 *
	 * @pre stmt != null and method != null
	 * @pre stmt.getUseBoxes()->exists(o | o.getValue().oclIsKindOf(NewExpr))
	 */
	void processNewExpr(final Stmt stmt, final SootMethod method);

	/**
	 * Process the parameter reference in <code>paramRef</code> for inclusion in the slice.
	 *
	 * @param paramRef is the program point that contains the parameter ref to be processed.
	 * @param method containting <code>paramRef</code>.
	 *
	 * @pre paramRef != null and method != null
	 * @pre paramRef.getValue().oclIsKindOf(ParameterRef))
	 */
	void processParameterRef(ValueBox paramRef, SootMethod method);

	/**
	 * Retrieves the value boxes reachable from the given program point (via containment relationship) that should be
	 * considered while transforming the given program point.
	 *
	 * @param valueBox is the program point being transformed.
	 *
	 * @return a collection of value boxes.
	 *
	 * @pre valueBox != null
	 * @post result != null and result.oclIsKindOf(Collection(ValueBox))
	 */
	Collection retrieveValueBoxesToTransformExpr(ValueBox valueBox);

	/**
	 * Retrieves the value boxes reachable from the given statement (via containment relationship) that should be considered
	 * while transforming the given statement.
	 *
	 * @param stmt is the statement being transformed.
	 *
	 * @return a collection of value boxes.
	 *
	 * @pre stmt != null
	 * @post result != null and result.oclIsKindOf(Collection(ValueBox))
	 */
	Collection retrieveValueBoxesToTransformStmt(Stmt stmt);
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/08/23 03:46:08  venku
   - documentation.

   Revision 1.2  2004/08/20 02:13:05  venku
   - refactored slicer based on slicing direction.
   Revision 1.1  2004/08/18 09:54:49  venku
   - adding first cut classes from refactoring for feature 427.  This is not included in v0.3.2.
 */
