
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.slicer;

import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.CaughtExceptionRef;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.Ref;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;


/**
 * <p>
 * This class hides the work involved in the creation of slice criteria from the environment.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SliceCriteriaFactory {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceCriteriaFactory.class);

	/**
	 * <p>
	 * This provides the classes that will be sliced.
	 * </p>
	 */
	private SootClassManager classManager;

	/**
	 * <p>
	 * Creates a new SliceCriteriaFactory object.
	 * </p>
	 *
	 * @param classManager that provides the classes that will be sliced.
	 *
	 * @pre classManager != null
	 */
	protected SliceCriteriaFactory(SootClassManager classManager) {
		this.classManager = classManager;
	}

	/**
	 * <p>
	 * Creates <code>SliceCriteria</code> objects from the given value criterion.  Every syntactic constructs related to
	 * storage in the statement are considered as slice criterion.
	 * </p>
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt in which the criterion occurs.
	 * @param vBox is the criterion.
	 * @param inclusive <code>true</code> indicates include the criterion in the slice; <code>false</code> indicates
	 *           otherwise.
	 *
	 * @return a collection of <code>SliceCriterion</code> objects corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null and vBox != null
	 * @post (result.size() > 1) implies (inclusive = true)
	 */
	public Collection getCriterion(SootMethod method, Stmt stmt, ValueBox vBox, boolean inclusive) {
		Collection result = new HashSet();
		Value value = vBox.getValue();

		ca.mcgill.sable.util.Collection temp = value.getUseBoxes();

		if (temp.size() > 0) {
			for (ca.mcgill.sable.util.Iterator i = temp.iterator(); i.hasNext();) {
				ValueBox cBox = (ValueBox) i.next();
				Value containee = cBox.getValue();

				if (containee instanceof Local || (containee instanceof Ref && !(containee instanceof CaughtExceptionRef))) {
					result.add(new SliceExpr(method, stmt, cBox, inclusive));
				}
			}
		}
		result.add(new SliceStmt(method, stmt, inclusive));
		return result;
	}

	/**
	 * <p>
	 * Creates <code>SliceCriteria</code> objects from the given statement criterion.  Every syntactic constructs related to
	 * storage in the statement are considered as slice criterion.
	 * </p>
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt is the criterion.
	 * @param inclusive <code>true</code> indicates include the criterion in the slice; <code>false</code> indicates
	 *           otherwise.
	 *
	 * @return a collection of <code>SliceCriterion</code> objects corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null
	 * @post (result.size() > 1) implies (inclusive = true)
	 */
	public Collection getCriterion(SootMethod method, Stmt stmt, boolean inclusive) {
		Collection result = new HashSet();
		ca.mcgill.sable.util.Collection temp = stmt.getUseAndDefBoxes();

		if (temp.size() > 0) {
			for (ca.mcgill.sable.util.Iterator i = temp.iterator(); i.hasNext();) {
				ValueBox cBox = (ValueBox) i.next();
				Value containee = cBox.getValue();

				if (containee instanceof Local || (containee instanceof Ref && !(containee instanceof CaughtExceptionRef))) {
					result.add(new SliceExpr(method, stmt, cBox, inclusive));
				}
			}
		}
		result.add(new SliceStmt(method, stmt, inclusive));
		return result;
	}

	/**
	 * <p>
	 * Returns a collection of criteria which include all occurrences of the given local in the given method.
	 * </p>
	 *
	 * @param method in which the <code>local</code> occurs.
	 * @param local is the local variable whose all occurrences in <code>method</code> should be captured as slice criterion
	 *
	 * @return a collection of <code>SliceCriterion</code> objects.
	 */
	public Collection getCriterion(SootMethod method, Local local) {
		Collection result = Collections.EMPTY_LIST;

		JimpleBody body = (JimpleBody) method.getBody(Jimple.v());

		if (body != null) {
			result = new HashSet();

			for (ca.mcgill.sable.util.Iterator i = body.getStmtList().iterator(); i.hasNext();) {
				Stmt stmt = (Stmt) i.next();

				for (ca.mcgill.sable.util.Iterator j = stmt.getUseAndDefBoxes().iterator(); j.hasNext();) {
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

/*****
 ChangeLog:

$Log$

*****/
