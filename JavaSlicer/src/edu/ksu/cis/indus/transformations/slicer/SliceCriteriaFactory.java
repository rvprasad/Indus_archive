
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

package edu.ksu.cis.indus.slicer;

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
	 * @param inclusive <code>true</code> indicates include the criterion in the slice; <code>false</code> indicates
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
				result.add(new SliceExpr(method, stmt, (ValueBox) i.next(), inclusive));
			}
		}
		result.add(new SliceStmt(method, stmt, inclusive));
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
				result.add(new SliceExpr(method, stmt, (ValueBox) i.next(), inclusive));
			}
		}
		result.add(new SliceStmt(method, stmt, inclusive));
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
						result.add(new SliceExpr(method, stmt, vBox, true));
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
   
   Revision 1.4  2003/05/22 22:23:49  venku
   Changed interface names to start with a "I".
   Formatting.
 */
