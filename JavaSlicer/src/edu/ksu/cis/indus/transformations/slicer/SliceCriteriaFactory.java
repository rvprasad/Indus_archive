
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.transformations.slicer;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.Stmt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This class hides the work involved in the creation of slice criteria from the environment.
 * 
 * <p>
 * The interesting issue while  creating slicing criteria is the inclusion information.  Please refer to {@link Slicer
 * Slicer}  for the sort of slices we discuss here.  In case of expression-level slice criterion, inclusion would mean that
 * the entire statement containing the expression needs to be included independent of the slice type, i.e., backward or
 * complete.  The same applies to statement-level slice criterion.
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
 * statement-level slice criterion which  refers to the statement that contains the expression.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SliceCriteriaFactory {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceCriteriaFactory.class);

	/**
	 * Creates slice criteria from the given value.  Every syntactic constructs related to storage at the program point are
	 * considered as slice criterion.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt in which the criterion occurs.
	 * @param vBox is the criterion.
	 * @param inclusive <code>true</code> indicates to include the criterion in the slice; <code>false</code> indicates
	 * 		  otherwise.
	 *
	 * @return a collection of slice criterion objects corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null and vBox != null
	 * @post result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriterion(final SootMethod method, final Stmt stmt, final ValueBox vBox, final boolean inclusive) {
		Collection result = new HashSet();
		Value value = vBox.getValue();

		Collection temp = value.getUseBoxes();

		if (temp.size() > 0) {
			for (Iterator i = temp.iterator(); i.hasNext();) {
				SliceExpr exprCriterion = SliceExpr.getSliceExpr();
				exprCriterion.initialize(method, stmt, vBox, inclusive);
				result.add(exprCriterion);
			}
		}

		SliceStmt stmtCriterion = SliceStmt.getSliceStmt();
		stmtCriterion.initialize(method, stmt, inclusive);
		result.add(stmtCriterion);
		return result;
	}

	/**
	 * Creates slice criteria from the given statement.  Every syntactic constructs related to storage in the statement are
	 * considered as slice criterion.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt is the criterion.
	 * @param inclusive <code>true</code> indicates that the criterion should be included in the slice; <code>false</code>
	 * 		  indicates otherwise.
	 *
	 * @return a collection of slice criterion corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null
	 * @post result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriterion(final SootMethod method, final Stmt stmt, final boolean inclusive) {
		Collection result = new HashSet();
		Collection temp = stmt.getUseAndDefBoxes();

		if (temp.size() > 0) {
			for (Iterator i = temp.iterator(); i.hasNext();) {
				SliceExpr exprCriterion = SliceExpr.getSliceExpr();
				exprCriterion.initialize(method, stmt, (ValueBox) i.next(), inclusive);
				result.add(exprCriterion);
			}
		}

		SliceStmt stmtCriterion = SliceStmt.getSliceStmt();
		stmtCriterion.initialize(method, stmt, inclusive);
		result.add(stmtCriterion);
		return result;
	}

	/**
	 * Returns a collection of criteria which include all occurrences of the given local in the given method.
	 *
	 * @param method in which the <code>local</code> occurs.
	 * @param local is the local variable whose all occurrences in <code>method</code> should be captured as slice criterion
	 *
	 * @return a collection of slice criteria.
	 *
	 * @post result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriterion(final SootMethod method, final Local local) {
		Collection result = Collections.EMPTY_LIST;

		Body body = method.getActiveBody();

		if (body != null) {
			result = new HashSet();

			for (Iterator i = body.getUnits().iterator(); i.hasNext();) {
				Stmt stmt = (Stmt) i.next();

				for (Iterator j = stmt.getUseAndDefBoxes().iterator(); j.hasNext();) {
					ValueBox vBox = (ValueBox) j.next();

					if (vBox.getValue().equals(local)) {
						SliceExpr exprCriterion = SliceExpr.getSliceExpr();
						exprCriterion.initialize(method, stmt, vBox, true);
						result.add(exprCriterion);
					}
				}
			}
		} else {
			LOGGER.warn(method.getSignature() + " does not have a body.");
		}

		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/08/18 05:01:45  venku
   Committing package name change in source after they were moved.
   Revision 1.5  2003/08/18 04:56:47  venku
   Spruced up Documentation and specification.
   But committing before moving slicer under transformation umbrella of Indus.
   Revision 1.4  2003/05/22 22:23:49  venku
   Changed interface names to start with a "I".
   Formatting.
 */
