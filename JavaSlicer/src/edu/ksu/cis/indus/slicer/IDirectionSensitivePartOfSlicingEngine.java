
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

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
interface IDirectionSensitivePartOfSlicingEngine
  extends DependenceExtractor.IDependenceRetriver {
	/**
	 * DOCUMENT ME!
	 *
	 * @param callee
	 * @param caller
	 * @param callStmt
	 */
	void generateCriteriaForTheCallToMethod(SootMethod callee, SootMethod caller, Stmt callStmt);

	/**
	 * DOCUMENT ME!
	 *
	 * @param stmt DOCUMENT ME!
	 * @param caller DOCUMENT ME!
	 * @param callees
	 */
	void generateCriteriaToIncludeCallees(Stmt stmt, SootMethod caller, Collection callees);

	/**
	 * DOCUMENT ME!
	 *
	 * @param local
	 * @param stmt
	 * @param method
	 */
	void processLocalAt(ValueBox local, Stmt stmt, SootMethod method);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	void processNewExpr(final Stmt stmt, final SootMethod method);

	/**
	 * DOCUMENT ME!
	 *
	 * @param box
	 * @param method
	 */
	void processParameterRef(ValueBox box, SootMethod method);

	/**
	 * DOCUMENT ME!
	 *
	 * @param valueBox
	 *
	 * @return
	 */
	Collection retrieveValueBoxesToTransformExpr(ValueBox valueBox);

	/**
	 * DOCUMENT ME!
	 *
	 * @param stmt
	 *
	 * @return
	 */
	Collection retrieveValueBoxesToTransformStmt(Stmt stmt);
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/08/18 09:54:49  venku
   - adding first cut classes from refactoring for feature 427.  This is not included in v0.3.2.
 */
