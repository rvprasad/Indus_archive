
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

import edu.ksu.cis.indus.common.soot.NamedTag;


/**
 * This is a marker class for the purpose of slicing.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicingTag
  extends NamedTag {
	/**
	 * Creates a new SlicingTag object.
	 *
	 * @param theName of the tag.
	 */
	public SlicingTag(final String theName) {
		super(theName);
	}

}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.

   Revision 1.5  2003/12/09 04:22:14  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.

   Revision 1.4  2003/12/04 12:10:12  venku
   - changes that take a stab at interprocedural slicing.

   Revision 1.3  2003/12/02 09:42:17  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.2  2003/11/30 00:47:01  venku
   - added a new tag which can be identified by name.
   - ripple effect.
   Revision 1.1  2003/11/24 16:47:31  venku
   - moved inner classes as external class.
   - made SliceCollector package private.
   - removed inheritance based dependence on ITransformer
     for SliceCollector.
 */
