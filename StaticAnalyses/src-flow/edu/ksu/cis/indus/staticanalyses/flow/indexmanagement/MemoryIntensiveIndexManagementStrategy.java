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

package edu.ksu.cis.indus.staticanalyses.flow.indexmanagement;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;

/**
 * This strategy does not reuse indices. It returns the incoming argument passed to <code>getEquivalentIndex</code>. There
 * by, it will be memory-intensive while trying to being processor non-intensive.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <I> is the type of the index.
 */
public final class MemoryIntensiveIndexManagementStrategy<I extends IIndex<I>>
		implements IIndexManagementStrategy<I> {

	/**
	 * {@inheritDoc}
	 */
	@NonNull public I getEquivalentIndex(@NonNull final I index) {
		return index;
	}

	/**
	 * {@inheritDoc}
	 */
	@Empty public void reset() {
		// does nothing
	}
}

// End of File
