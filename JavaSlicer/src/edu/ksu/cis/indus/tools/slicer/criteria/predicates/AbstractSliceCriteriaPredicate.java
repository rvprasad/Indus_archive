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

package edu.ksu.cis.indus.tools.slicer.criteria.predicates;

import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * This is an abstract implementation of slice criteria filter.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the type of the input objects to the predicate.
 */
public abstract class AbstractSliceCriteriaPredicate<T>
		implements ISliceCriteriaPredicate<T> {

	/**
	 * This is the slicer tool provides the context in which filtering occurs.
	 */
	private SlicerTool<?> slicerTool;

	/**
	 * {@inheritDoc}
	 */
	public void setSlicerTool(final SlicerTool<?> slicer) {
		slicerTool = slicer;
	}

	/**
	 * Retrieves the value in <code>slicerTool</code>.
	 * 
	 * @return the value in <code>slicerTool</code>.
	 */
	protected final SlicerTool<?> getSlicerTool() {
		return slicerTool;
	}
}

// End of File
