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

import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * This is the interface used to filter the basis of criteria during automatic criteria generation.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <T>
 */
public interface ISliceCriteriaPredicate<T>
		extends IPredicate<T> {

	/**
	 * A dummy filter that allows every basis to be considered for criteria generation.
	 */
	ISliceCriteriaPredicate DUMMY_FILTER = new ISliceCriteriaPredicate<Object>() {

		public boolean evaluate(final Object entity) {
			return true;
		}

		public void setSlicerTool(final SlicerTool slicer) {
		}
	};

	/**
	 * Checks if the entity should be generated based on the given entity.
	 * 
	 * @param entity forms the base for the criteria.
	 * @return <code>true</code> if
	 * @pre entity != null and slicer != null
	 */
	boolean evaluate(final T entity);

	/**
	 * Sets the slicer tool in conjunction of which this filter is being used.
	 * 
	 * @param slicer is the tool to be used.
	 */
	void setSlicerTool(SlicerTool slicer);
}

// End of File
