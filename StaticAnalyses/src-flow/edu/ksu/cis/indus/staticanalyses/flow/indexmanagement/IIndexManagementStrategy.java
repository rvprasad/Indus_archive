
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

import edu.ksu.cis.indus.staticanalyses.flow.IIndex;

/**
 * This interface enables to plug in index management strategy.  
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <I> DOCUMENT ME!
 */
public interface IIndexManagementStrategy <I extends IIndex<I>> {
	/**
	 * Retrieves an index equivalent to the given index.
	 *
	 * @param index of interest.
	 *
	 * @return the equivalent index.
	 *
	 * @pre index != null
	 */
	I getEquivalentIndex(I index);

	/**
	 * Resets internal data structures.
	 */
	void reset();
}

// End of File
