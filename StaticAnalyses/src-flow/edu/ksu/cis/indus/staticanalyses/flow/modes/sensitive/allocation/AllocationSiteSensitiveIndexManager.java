
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.OneContextInfoIndex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class manages indices associated with fields and array components  in allocation-site sensitive mode.  In reality, it
 * provides the implementation to create new indices.  Created: Tue Mar  5 14:08:18 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class AllocationSiteSensitiveIndexManager
  extends AbstractIndexManager {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AllocationSiteSensitiveIndexManager.class);

	/**
	 * Returns a new instance of this class.
	 *
	 * @return a new instance of this class.
	 */
	public Object getClone() {
		return new AllocationSiteSensitiveIndexManager();
	}

	/**
	 * Returns an index corresponding to the given entity and context.
	 *
	 * @param o the entity for which the index in required.  Although it is not enforced, this should be of type
	 * 		  <code>FielRef</code> or <code>ArrayRef</code>.
	 * @param c the context in which information pertaining to <code>o</code> needs to be captured.
	 *
	 * @return the index that uniquely identifies <code>o</code> in context, <code>c</code>.
	 *
	 * @pre o != null and c != null and
	 * 		c.oclIsTypeOf(edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext)
	 */
	protected IIndex getIndex(final Object o, final Context c) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting index for " + o + " in " + c);
		}

		AllocationContext ctxt = (AllocationContext) c;

		return new OneContextInfoIndex(o, ctxt.getAllocationSite());
	}
}

/*
   ChangeLog:

   $Log$
   Revision 1.3  2003/08/13 08:02:28  venku
   Fixed Checkstyle formatting errors.
   Revision 1.2  2003/08/12 18:47:50  venku
   Spruced up documentation and specification.
   Changed equals() and hashCode() in AllocationContext.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 1.5  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
