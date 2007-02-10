
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
  extends AbstractSliceCriteriaGenerator<Object, Object> {
	/**
	 * The collection of statically provided criteria.
	 */
	private final Collection<ISliceCriterion> staticCriteria;

	/**
	 * Creates an instance of this class.
	 *
	 * @param criteria is the collection of statically provided criteria.
	 *
	 * @pre criteria != null
	 */
	public StaticSliceCriteriaGenerator(final Collection<ISliceCriterion> criteria) {
		super();
		staticCriteria = Collections.unmodifiableCollection(new ArrayList<ISliceCriterion>(criteria));
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
	@Override protected Collection<ISliceCriterion> getCriteriaTemplateMethod() {
		final Collection<ISliceCriterion> _result = new HashSet<ISliceCriterion>();
		final Collection<ISliceCriterion> _subResult = new ArrayList<ISliceCriterion>();

		for (final Iterator<ISliceCriterion> _i = staticCriteria.iterator(); _i.hasNext();) {
			_subResult.clear();

			final ISliceCriterion _criterion = _i.next();
			_subResult.add(_criterion);
			contextualizeCriteriaBasedOnThis(_criterion.getOccurringMethod(), _subResult);
			_result.addAll(_subResult);
		}

		return _result;
	}
}

// End of File
