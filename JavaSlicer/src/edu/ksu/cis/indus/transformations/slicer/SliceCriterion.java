
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

/**
 * <p>
 * This class represents a slice criterion.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class SliceCriterion {
	/**
	 * <p>
	 * This indicates if this criterion is included in the slice or not.
	 * </p>
	 */
	protected boolean inclusive;

	/**
	 * <p>
	 * Creates a new SliceCriterion object.
	 * </p>
	 *
	 * @param inclusive <code>true</code> indicates this criterion should be included in the slice; <code>false</code>,
	 * 		  otherwise.
	 */
	protected SliceCriterion(boolean inclusive) {
		this.inclusive = inclusive;
	}

	/**
	 * <p>
	 * Returns the stored criterion object.
	 * </p>
	 *
	 * @return Object representing the criterion.
	 */

	/*!
	 * @post result != null
	 */
	public abstract Object getCriterion();

	/**
	 * <p>
	 * Indicates if this criterion is included in the slice or not.
	 * </p>
	 *
	 * @return <code>true</code> if this criterion is included in the slice; <code>false</code>, otherwise.
	 */
	public boolean isIncluded() {
		return inclusive;
	}

	/**
	 * <p>Checks if the given object is "equal" to this object.</p>
	 *
	 * @param o is the object to be compared.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise. 
	 */
	public boolean equals(Object o) {
		boolean result = false;

		if(o instanceof SliceCriterion) {
			result = ((SliceCriterion) o).inclusive == inclusive;
		}
		return result;
	}

	/**
	 * <p>Returns the hashcode for this object.</p>
	 *
	 * @return the hashcode for this object.
	 */
	public int hashCode() {
		int result;

		if(inclusive) {
			result = Boolean.TRUE.hashCode();
		} else {
			result = Boolean.FALSE.hashCode();
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.1.1.1  2003/02/17 23:59:51  venku
Placing JavaSlicer under version control.


*****/
