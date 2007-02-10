/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
