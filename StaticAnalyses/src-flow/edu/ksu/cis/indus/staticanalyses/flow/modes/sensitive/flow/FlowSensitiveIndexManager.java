
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.OneContextInfoIndex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	private static final Logger LOGGER = LoggerFactory.getLogger(FlowSensitiveIndexManager.class);

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
	protected IIndex createIndex(final Object o, final Context c) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting index for " + o + " in " + c);
		}

		return new OneContextInfoIndex(o, c.getProgramPoint());
	}
}

// End of File
