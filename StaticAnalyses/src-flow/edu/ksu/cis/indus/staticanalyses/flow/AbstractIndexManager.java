
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
