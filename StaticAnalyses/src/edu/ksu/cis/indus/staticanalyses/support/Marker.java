
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

package edu.ksu.cis.indus.staticanalyses.support;

/**
 * This class serves as a marker in sequences of data.  The stringized representation of this object is dependent on  the
 * stringized representation of it's constituents.  Hence, the stringized representation of this object will change if  that
 * of the constituents change.  A similar dependency exists for hashCode too.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Marker {
	/**
	 * Any content to be stored in the marker.
	 */
	public final Object _content;

	/**
	 * Creates a new Marker object with <code>content</code> set to <code>null</code>.
	 */
	public Marker() {
		_content = null;
	}

	/**
	 * Creates a new Marker object.
	 *
	 * @param o is any content to be stored in the marker.
	 */
	public Marker(final Object o) {
		_content = o;
	}

	/**
	 * Checks if the given object is equal to this object.
	 *
	 * @param o is the object to be checked for equality.
	 *
	 * @return <code>true</code> if <code>o</code> equals this object; <code>false</code>, otherwise.
	 *
	 * @post result == true implies o.oclTypeOf(Marker) and (o._content.equals(_content) or o._content == _content)
	 * @post result == false implies (not o.oclTypeOf(Marker)) or not (o._content.equals(_content) or o._content == _content)
	 */
	public boolean equals(final Object o) {
		boolean result = false;

		if (o instanceof Marker) {
			Object temp = ((Marker) o)._content;

			if (temp == null && _content == null) {
				result = this == o;
			} else if (_content != null && temp != null) {
				result = _content.equals(temp);
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
		int result;

		if (_content != null) {
			result = _content.hashCode();
		} else {
			result = super.hashCode();
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.4  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
