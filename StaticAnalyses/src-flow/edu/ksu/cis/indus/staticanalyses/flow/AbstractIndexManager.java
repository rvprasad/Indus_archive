
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
 * This class encapsulates the index creation logic.  It is abstract and it provides an interface through which new indices
 * can be obtained.  The sub classes should provide the logic for the actual creation of the indices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractIndexManager
  implements IIndexManager {
	/** 
	 * The value of <code>INDEX_MANAGEMENT_STRATEGY_PROPERTY</code> to use memory intensive index management strategy.
	 */
	public static final String MEMORY_INTENSIVE_INDEX_MANAGEMENT = "MEMORY_INTENSIVE_INDEX_MANAGEMENT";

	/** 
	 * The value of <code>INDEX_MANAGEMENT_STRATEGY_PROPERTY</code> to use processor intensive index management strategy.
	 */
	public static final String PROCESSOR_INTENSIVE_INDEX_MANAGEMENT = "PROCESSOR_INTENSIVE_INDEX_MANAGEMENT";

	/** 
	 * The name of the property via which index management strategy can be altered. 
	 */
	public static final String INDEX_MANAGEMENT_STRATEGY =
		"edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager.indexManagementStrategy";

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private final IIndexManagementStrategy strategizedIndexMgr;

	/**
	 * Creates a new AbstractIndexManager object.
	 */
	public AbstractIndexManager() {
		final String _prop = System.getProperty(INDEX_MANAGEMENT_STRATEGY);

		if (_prop != null && _prop.equals(MEMORY_INTENSIVE_INDEX_MANAGEMENT)) {
			strategizedIndexMgr = new MemoryIntensiveStrategy();
		} else {
			strategizedIndexMgr = new ProcessorIntensiveStrategy();
		}
	}

	/**
	 * This operation is unsupported.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * This operation is unsupported.
	 *
	 * @param o is ignored.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public Object getClone(final Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
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
	public final IIndex getIndex(final Object o, final Context c) {
		final IIndex _temp = createIndex(o, c);
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
	protected abstract IIndex createIndex(final Object o, final Context c);
}

// End of File
