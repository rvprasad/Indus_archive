
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


/**
 * This implementation can be used to cater a set of statically determined criteria.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class StaticSliceCriteriaGenerator
  extends AbstractSliceCriteriaGenerator {
	/** 
	 * The collection of statically provided criteria.
	 *
	 * @invariant staticCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	private final Collection staticCriteria;

	/**
	 * Creates an instance of this class.
	 *
	 * @param criteria is the collection of statically provided criteria.
	 *
	 * @pre criteria != null and criteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	public StaticSliceCriteriaGenerator(final Collection criteria) {
		super();
		staticCriteria = Collections.unmodifiableCollection(new ArrayList(criteria));
	}

	/**
	 * Returns the provided static criteria.
	 *
	 * @return a collection of criteria.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriterion))
	 *
	 * @see AbstractSliceCriteriaGenerator#getCriteriaTemplateMethod()
	 */
	protected Collection getCriteriaTemplateMethod() {
		return staticCriteria;
	}
}

// End of File
