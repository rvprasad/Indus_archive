
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

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.Stmt;


/**
 * This class represents a statement as a slice criterion.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SliceStmt
  extends SliceCriterion {
	/**
	 * The method in which <code>stmt</code> occurs.
	 */
	protected SootMethod method;

	/**
	 * The statement associated with this criterion.
	 */
	protected Stmt stmt;

	/**
	 * Creates a new SliceStmt object.
	 *
	 * @param method in which the slice criterion occurs.
	 * @param stmt is the slice criterion.
	 * @param inclusive <code>true</code> if the slice criterion should be included in the slice; <code>false</code>,
	 * 		  otherwise.
	 *
	 * @pre method != null and stmt != null
	 */
	protected SliceStmt(SootMethod method, Stmt stmt, boolean inclusive) {
		super(inclusive);
		this.method = method;
		this.stmt = stmt;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the statement(<code>Stmt</code>) associated with this criterion.
	 *
	 * @post result = stmt and result.oclType = ca.mcgill.sable.jimple.Stmt
	 *
	 * @see edu.ksu.cis.bandera.slicer.SliceCriterion#getCriterion()
	 */
	public Object getCriterion() {
		return stmt;
	}

	/**
	 * Provides the method in which criterion occurs.
	 *
	 * @return the method in which the slice statement occurs.
	 *
	 * @post result = method
	 */
	public SootMethod getOccurringMethod() {
		return method;
	}

	/**
	 * Checks if the given object is "equal" to this object.
	 *
	 * @param o is the object to be compared.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise.
	 */
	public boolean equals(Object o) {
		boolean result = false;

		if(o instanceof SliceStmt) {
			SliceStmt temp = (SliceStmt) o;
			result = temp.stmt.equals(stmt) && temp.method.equals(method) && super.equals(temp);
		}
		return result;
	}

	/**
	 * Returns the hashcode for this object.
	 *
	 * @return the hashcode for this object.
	 */
	public int hashCode() {
		return (stmt.toString() + method.toString() + super.toString()).hashCode();
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.1.1.1  2003/02/17 23:59:51  venku
Placing JavaSlicer under version control.


*****/
