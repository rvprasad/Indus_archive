
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

package edu.ksu.cis.indus.processing;

import soot.tagkit.Host;


/**
 * This class filters out classes and methods that do have a tag of the given name.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AntiTagBasedProcessingFilter
  extends TagBasedProcessingFilter {
	/**
	 * Creates a new TagBasedProcessingFilter object.
	 *
	 * @param theTagName is the name of the tag used during filtering.
	 *
	 * @pre theTagName != null
	 */
	public AntiTagBasedProcessingFilter(final String theTagName) {
		super(theTagName);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.TagBasedProcessingFilter#isFilterate(soot.tagkit.Host)
	 */
	protected boolean isFilterate(final Host host) {
		return !host.hasTag(tagName);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/14 15:53:31  venku
   - added a new class AntiTagBasedProcessingFilter that
     does the opposite of TagBasedProcessingFilter.

 */
