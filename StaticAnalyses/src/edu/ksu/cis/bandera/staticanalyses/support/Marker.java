
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

package edu.ksu.cis.bandera.staticanalyses.support;

/**
 * This is a dummy class used to mark locations in call stack during recursion root calculation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Marker {
	/**
	 * <p>
	 * Any content to be stored in the marker.
	 * </p>
	 */
	public final Object _CONTENT;

	/**
	 * Creates a new Marker object with <code>content</code> set to <code>null</code>.
	 */
	public Marker() {
		_CONTENT = null;
	}

	/**
	 * Creates a new Marker object.
	 *
	 * @param o is any content to be stored in the marker.
	 */
	public Marker(Object o) {
		_CONTENT = o;
	}

	/**
	 * Checks if <code>o</code> is equal to this object.  If the <code>content</code> field of both the objects are equal to
	 * <code>null</code>, then <i>referential equality</i> is used.  If not, the equality of <code>content</code> fields is
	 * considered as the equality between this object and <code>o</code>.
	 *
	 * @param o is the object to be checked for equality.
	 *
	 * @return <code>true</code> if <code>o</code> equals this object; <code>false</code>, otherwise.
	 */
	public boolean equals(Object o) {
		boolean result = false;

		if(o instanceof Marker) {
			Object temp = ((Marker) o)._CONTENT;

			if(temp == null && _CONTENT == null) {
				result = this == o;
			} else if(_CONTENT != null && temp != null) {
				result = _CONTENT.equals(temp);
			}
		}
		return result;
	}

	/**
	 * Returns the hash code of the object.  <code>content</code> field is used if it is non-null.
	 *
	 * @return the hash code of the object.
	 */
	public int hashCode() {
		int result = super.hashCode();

		if(_CONTENT != null) {
			result = _CONTENT.hashCode();
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$

*****/
