
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive;

import edu.ksu.cis.indus.staticanalyses.flow.IIndex;


/**
 * This class represents an index which can be differentiated based on a context of unit size.  We consider any peice of
 * information which can be used to divide summary sets, as context information.  So, a context can be made up of many such
 * peices of information.  This class can encapsulate only one such peice of context information.  For example, an instance
 * can encapsulate either the  calling stack or the program point as the context information, but not both.
 * 
 * <p>
 * Created: Fri Jan 25 13:11:19 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class OneContextInfoIndex
  implements IIndex {
	/**
	 * The context in which <code>value</code> needs to be differentiated.
	 */
	private final Object contextInfo;

	/**
	 * This index is used in association with <code>value</code>.  This value is not available for retrieval, but rather adds
	 * to improve the performance of <code>hashCode()</code> and <code>equals(Object)</code>.
	 */
	private final Object v;

	/**
	 * Creates a new <code>OneContextInfoIndex</code> instance.
	 *
	 * @param value the value whose variant is identified by this index.
	 * @param c the context in which <code>value</code>'s variant is identified by this index.
	 */
	public OneContextInfoIndex(final Object value, final Object c) {
		this.v = value;
		this.contextInfo = c;
	}

	/**
	 * Compares this index with a given index.  The objects are equal when the <code>value</code> and <code>context</code>
	 * are equal.
	 *
	 * @param index the index to be compared with.
	 *
	 * @return <code>true</code> if this index is equal to <code>index</code>; <code>false</code> otherwise.
	 */
	public boolean equals(final Object index) {
		boolean result = false;

		if (index != null && index instanceof OneContextInfoIndex) {
			OneContextInfoIndex d = (OneContextInfoIndex) index;

			if (v != null) {
				result = v.equals(d.v);
			} else {
				result = v == d.v;
			}

			if (result) {
				if (contextInfo != null) {
					result = contextInfo.equals(d.contextInfo);
				} else {
					result = contextInfo == d.contextInfo;
				}
			}
		}
		return result;
	}

	/**
	 * Generates a hash code for this object.
	 *
	 * @return the hash code for this object.
	 */
	public int hashCode() {
		int result = 17;

		if (v != null) {
			result = 37 * result + v.hashCode();
		}

		if (contextInfo != null) {
			result = 37 * result + contextInfo.hashCode();
		}
		return result;
	}

	/**
	 * Returns the stringized form of this object.
	 *
	 * @return returns the stringized form of this object.
	 */
	public String toString() {
		return v + " " + contextInfo;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/12 18:55:51  venku
   Spruced up documentation and specification.
   Changed equals() and hashCode() in AllocationContext.
   Removed cached versions of hashCode() and equals().
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.8  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
