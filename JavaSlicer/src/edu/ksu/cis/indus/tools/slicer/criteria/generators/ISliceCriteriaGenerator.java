
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

package edu.ksu.cis.indus.tools.slicer.criteria.generators;

import edu.ksu.cis.indus.tools.slicer.SlicerTool;
import edu.ksu.cis.indus.tools.slicer.contextualizers.ISliceCriteriaContextualizer;
import edu.ksu.cis.indus.tools.slicer.criteria.predicates.ISliceCriteriaPredicate;

import java.util.Collection;


/**
 * This interface is used by the slicer tool to generate slicing criteria.  An implementation of this interface can be used
 * to generate criteria that is based on property of the program points rather than hand-picked by the user. The user can
 * control the criteria via the filter and make them context-sensitive via contextualizer.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISliceCriteriaGenerator {
	/**
	 * Retrieves the slicing criteria.
	 *
	 * @param slicer that uses the criteria.
	 *
	 * @return a collection of criteria.
	 *
	 * @pre slicer != null
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	Collection getCriteria(SlicerTool slicer);

	/**
	 * Sets the criteria contextualizer.
	 *
	 * @param contextualizer to used.
	 *
	 * @pre contextualizer != null
	 */
	void setCriteriaContextualizer(ISliceCriteriaContextualizer contextualizer);

	/**
	 * Sets the criteria filter.
	 *
	 * @param criteriaFilter to be used.
	 *
	 * @pre criteriaFilter != null
	 */
	void setCriteriaFilter(ISliceCriteriaPredicate criteriaFilter);
}

// End of File
