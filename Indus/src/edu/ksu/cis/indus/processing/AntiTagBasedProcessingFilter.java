
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

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.tagkit.Host;
import soot.tagkit.Tag;


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
     * The logger used by instances of this class to log messages.
     * 
     */
    private static final Log LOGGER = LogFactory.getLog(AntiTagBasedProcessingFilter.class);

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
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Host " + host + " is tagged:" + host.hasTag(tagName) + " [" + tagName + "]");

			if (!host.getTags().isEmpty()) {
				LOGGER.debug("Host " + host + " has the following tags: ");

				for (final Iterator _i = host.getTags().iterator(); _i.hasNext();) {
					final Tag _tag = (Tag) _i.next();

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(_tag.getName());
					}
				}
			}
		}
		return !host.hasTag(tagName);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/12/16 00:11:09  venku
   - logging.

   Revision 1.3  2003/12/15 16:30:12  venku
   - logging.
   Revision 1.2  2003/12/14 20:36:05  venku
   - the filtering methods were incorrect in TagBased... FIXED.
   Revision 1.1  2003/12/14 15:53:31  venku
   - added a new class AntiTagBasedProcessingFilter that
     does the opposite of TagBasedProcessingFilter.
 */
