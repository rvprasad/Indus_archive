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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.processing.Context;

/**
 * This interface is used to manage indices.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <I> DOCUMENT ME!
 * @param <O> DOCUMENT ME!
 */
public interface IIndexManager<I extends IIndex<I>, O> {

	/**
	 * Returns the index corresponding to the given entity in the given context, if one exists. If none exist, a new index is
	 * created and returned.
	 * 
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 * @return the index corresponding to the entity in the given context.
	 * @pre o != null and c != null
	 * @post result != null
	 */
	I getIndex(final O o, final Context c);

	/**
	 * Reset the manager. Flush all the internal data structures.
	 */
	void reset();
}

// End of File
