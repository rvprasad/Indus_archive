
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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.interfaces.IPoolable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A worklist implementation.
 * 
 * <p>
 * Created: Tue Jan 22 02:43:16 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public final class WorkList {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(WorkList.class);

	/** 
	 * The backend workbag object which holds the work piece.
	 */
	private final IWorkBag workbag;

	/**
	 * Creates a new <code>WorkList</code> instance.
	 *
	 * @param container that will contain the work pieces.
	 *
	 * @pre container != null
	 */
	public WorkList(final IWorkBag container) {
		workbag = container;
	}

	/**
	 * Removes any work in the work list without processing them.
	 */
	public void clear() {
		workbag.clear();
	}

	/**
	 * Executes the work pieces in the worklist.  This method returns when the worklist is empty, i.e., all the work peices
	 * have been executed.
	 */
	public void process() {
		while (workbag.hasWork()) {
			final Object _o = workbag.getWork();

			if (_o instanceof IWork) {
				final IWork _w = (IWork) _o;

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Processing work:" + _w);
				}
				_w.execute();
			}

			if (_o instanceof IPoolable) {
				((IPoolable) _o).returnToPool();
			}
		}
	}
}

// End of File
