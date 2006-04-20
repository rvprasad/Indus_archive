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

import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.interfaces.IPoolable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A worklist implementation.
 * <p>
 * Created: Tue Jan 22 02:43:16 2002
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Date$
 * @param <T> The type of work handled by this work bag.
 */
public final class WorkList<T> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(WorkList.class);

	/**
	 * The backend workbag object which holds the work piece.
	 */
	@NonNull @NonNullContainer private final IWorkBag<T> workbag;

	/**
	 * Creates a new <code>WorkList</code> instance.
	 * 
	 * @param container that will contain the work pieces.
	 */
	public WorkList(@NonNull @NonNullContainer final IWorkBag<T> container) {
		workbag = container;
	}

	/**
	 * Removes any work in the work list without processing them.
	 */
	public void clear() {
		workbag.clear();
	}

	/**
	 * Executes the work pieces in the worklist. This method returns when the worklist is empty, i.e., all the work peices
	 * have been executed.
	 * 
	 * @return the number of work pieces processed in this cycle.
	 * @post resutl &gt;= 0
	 */
	public int process() {
		int _result = 0;

		while (workbag.hasWork()) {
			final T _o = workbag.getWork();

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
			_result++;
		}
		return _result;
	}
}

// End of File
