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

package edu.ksu.cis.indus.staticanalyses.flow.indexmanagement;

import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.collections.RetrievableSet;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;

/**
 * This strategy tries to reuse indices. It returns the same object for identical indices passed as arguments to
 * <code>getEquivalentIndex</code>. There by, it will be processor-intensive while trying to being memory non-intensive.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <I> is the type of the index.
 */
public class ProcessorIntensiveIndexManagementStrategy<I extends IIndex<I>>
		implements IIndexManagementStrategy<I> {

	/**
	 * The collection of indices managed by this object.
	 * 
	 */
	@NonNullContainer private final RetrievableSet<I> indices = new RetrievableSet<I>();

	/**
	 * {@inheritDoc}
	 * 
	 */
	@NonNull public I getEquivalentIndex(@NonNull final I index) {
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
	 * {@inheritDoc}
	 */
	public void reset() {
		indices.clear();
	}
}

// End of File
