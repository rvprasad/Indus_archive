
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
		name = theName.intern();
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
		boolean _result = false;

		if (o instanceof NamedTag) {
			final NamedTag _t = (NamedTag) o;
			_result = name == _t.name;
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int _result = 17;
		_result = _result * 37 + name.hashCode();
		return _result;
	}
}

// End of File
