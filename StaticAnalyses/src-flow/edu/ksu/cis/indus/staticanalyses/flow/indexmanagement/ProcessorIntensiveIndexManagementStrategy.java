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

import edu.ksu.cis.indus.common.collections.RetrievableSet;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;

/**
 * This strategy tries to reuse indices. It returns the same object for identical indices passed as arguments to
 * <code>getEquivalentIndex</code>. There by, it will be processor-intensive while trying to being memory non-intensive.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <I> DOCUMENT ME!
 */
public class ProcessorIntensiveIndexManagementStrategy<I extends IIndex<I>>
		implements IIndexManagementStrategy<I> {

	/**
	 * The collection of indices managed by this object.
	 * 
	 * @invariant indices != null
	 */
	private final RetrievableSet<I> indices = new RetrievableSet<I>();

	/**
	 * {@inheritDoc}
	 * 
	 * @see IIndexManagementStrategy#getEquivalentIndex(edu.ksu.cis.indus.staticanalyses.flow.IIndex)
	 */
	public I getEquivalentIndex(final I index) {
		final I _result;

		if (indices.contains(index)) {
			_result = indices.get(index);
		} else {
			_result = index;
			indices.add(index);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.indexmanagement.IIndexManagementStrategy#reset()
	 */
	public void reset() {
		indices.clear();
	}
}

// End of File
