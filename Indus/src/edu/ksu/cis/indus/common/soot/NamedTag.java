
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.common.soot;

import soot.tagkit.Tag;


/**
 * This is a tag implementation which can be differentiated by the name given at instantiation time.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class NamedTag
  implements Tag {
	/**
	 * The name of this tag.
	 */
	protected final String name;

	/**
	 * Creates a new NamedTag object.
	 *
	 * @param theName is the name associated with this tag.
	 *
	 * @pre theName != null
	 */
	public NamedTag(final String theName) {
		name = theName;
	}

	/**
	 * Returns the name of this tag as provided at it's creation.
	 *
	 * @return the name of this tag.
	 *
	 * @post result != null
	 */
	public final String getName() {
		return name;
	}

	/**
	 * @see soot.tagkit.Tag#getValue()
	 */
	public final byte[] getValue() {
		return name.getBytes();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(final Object o) {
		boolean result = false;

		if (o instanceof NamedTag) {
			final NamedTag _t = (NamedTag) o;
			result = name == _t.name || _t.name.equals(name);
		}
		return result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int result = 17;
		result = result * 37 + name.hashCode();
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/12/03 21:44:20  venku
   - added support for equals() and hashCode() methods.

   Revision 1.3  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.1  2003/11/30 00:46:58  venku
   - added a new tag which can be identified by name.
   - ripple effect.
 */
