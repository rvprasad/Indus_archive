
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

import edu.ksu.cis.indus.slicer.ISliceCriterion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This implementation can be used to cater a set of statically determined criteria.
 * 
 * <p>
 * This implementation is intended to be used with <code>StaticSliceCriteriaCallStackContextualizer</code>.
 * </p>
 * 
 * <p>
 * As the generator is unaware of the entity that each criterion is based on, the contextualization will be based on the
 * method in which the criterion occurs.
 * </p>
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
		final Collection _result = new HashSet();
		final Collection _subResult = new ArrayList();

		for (final Iterator _i = staticCriteria.iterator(); _i.hasNext();) {
			_subResult.clear();

			final ISliceCriterion _criterion = (ISliceCriterion) _i.next();
			_subResult.add(_criterion);
			contextualizeCriteriaBasedOnThis(_criterion.getOccurringMethod(), _subResult);
			_result.addAll(_subResult);
		}

		return _result;
	}
}

// End of File
