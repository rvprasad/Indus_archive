
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

package edu.ksu.cis.indus.slicer;

import soot.tagkit.Tag;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicingTag
  implements Tag {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final String name;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final boolean seed;

	/**
	 * Creates a new SlicingTag object.
	 *
	 * @param theName DOCUMENT ME!
	 * @param isSeed DOCUMENT ME!
	 */
	public SlicingTag(final String theName, final boolean isSeed) {
		name = theName;
		seed = isSeed;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public String getName() {
		return name;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean isSeed() {
		return seed;
	}

	/**
	 * @see soot.tagkit.Tag#getValue()
	 */
	public byte[] getValue() {
		return name.getBytes();
	}
}

/*
   ChangeLog:
   $Log$
 */
