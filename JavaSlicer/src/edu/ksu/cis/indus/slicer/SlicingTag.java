
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

import edu.ksu.cis.indus.common.NamedTag;


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
  extends NamedTag {
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
		super(theName);
		seed = isSeed;
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
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/11/30 00:47:01  venku
   - added a new tag which can be identified by name.
   - ripple effect.
   Revision 1.1  2003/11/24 16:47:31  venku
   - moved inner classes as external class.
   - made TaggingBasedSliceCollector package private.
   - removed inheritance based dependence on ITransformer
     for TaggingBasedSliceCollector.
 */
