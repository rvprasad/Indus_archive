
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
import edu.ksu.cis.indus.staticanalyses.Constants;
import edu.ksu.cis.indus.staticanalyses.flow.indexmanagement.IIndexManagementStrategy;

/**
 * This class encapsulates the index creation logic.  It is abstract and it provides an interface through which new indices
 * can be obtained.  The sub classes should provide the logic for the actual creation of the indices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <I> is the type of the index.
 * @param <E> is the type of the indexed entity. 
 */
public abstract class AbstractIndexManager<I extends IIndex<I>, E>
  implements IIndexManager<I, E> {
	/**
	 * The strategy used to manage indices.
	 */
	private final IIndexManagementStrategy<I> strategizedIndexMgr;

	/**
	 * Creates a new AbstractIndexManager object.
	 */
	public AbstractIndexManager() {
		strategizedIndexMgr = Constants.getIndexManagementStrategy();
	}

	/**
	 * Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, a new index
	 * is created and returned.
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 *
	 * @return the index corresponding to the entity in the given context.
	 *
	 * @pre o != null and c != null
	 * @post result != null
	 */
	public final I getIndex(final E o, final Context c) {
		final I _temp = createIndex(o, c);
		return strategizedIndexMgr.getEquivalentIndex(_temp);
	}

	/**
	 * @see IIndexManager#reset()
	 */
	public void reset() {
		strategizedIndexMgr.reset();
	}

	/**
	 * Creates a new index corresponding to the given entity in the given context.
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 *
	 * @return the index corresponding to the entity in the given context.
	 *
	 * @pre o != null and c != null
	 * @post result != null
	 */
	protected abstract I createIndex(final E o, final Context c);
}

// End of File
