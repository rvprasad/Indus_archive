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
