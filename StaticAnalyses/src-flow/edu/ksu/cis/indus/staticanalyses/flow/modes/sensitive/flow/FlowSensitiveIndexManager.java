
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.OneContextInfoIndex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class manages indices associated with entities in flow sensitive mode.  In reality, it provides the implementation to
 * create new indices.
 * 
 * <p>
 * Created: Tue Mar  5 14:08:18 2002.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FlowSensitiveIndexManager
  extends AbstractIndexManager {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FlowSensitiveIndexManager.class);

	/**
	 * Returns a new instance of this class.
	 *
	 * @return a new instance of this class.
	 *
	 * @post result != null
	 */
	public Object getClone() {
		return new FlowSensitiveIndexManager();
	}

	/**
	 * Returns an index corresponding to the given entity and context.  The index is dependent on the program point stored in
	 * the context.
	 *
	 * @param o the entity for which the index in required.
	 * @param c the context which captures program point needed to generate the index.
	 *
	 * @return the index that uniquely identifies <code>o</code> at the program point captured in <code>c</code>.
	 *
	 * @pre o != null and c != null
	 */
	protected IIndex getIndex(final Object o, final Context c) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting index for " + o + " in " + c);
		}

		return new OneContextInfoIndex(o, c.getProgramPoint());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/12 18:39:15  venku
   Spruced up documentation and specification.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.4  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
