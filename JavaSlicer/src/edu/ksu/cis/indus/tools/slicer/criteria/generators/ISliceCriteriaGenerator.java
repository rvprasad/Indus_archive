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

import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.collections.IPredicate;
import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;
import edu.ksu.cis.indus.tools.slicer.contextualizers.ISliceCriteriaContextualizer;
import edu.ksu.cis.indus.tools.slicer.criteria.predicates.ISliceCriteriaPredicate;

import java.util.Collection;

/**
 * This interface is used by the slicer tool to generate slicing criteria. An implementation of this interface can be used to
 * generate criteria that is based on property of the program points rather than hand-picked by the user. The user can control
 * the criteria via the filter and make them context-sensitive via contextualizer.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ *
 * @param <T1> is the type of objects that are selected in expectation of containing slice criteria.
 * @param <T2> is the type of objects that will be considered for being used as slice criteria.
 */
public interface ISliceCriteriaGenerator<T1, T2> {

	/**
	 * Retrieves the slicing criteria.
	 * 
	 * @param slicer that uses the criteria.
	 * @return a collection of criteria.
	 */
	@NonNull @NonNullContainer Collection<ISliceCriterion> getCriteria(@NonNull SlicerTool<?> slicer);

	/**
	 * Sets the criteria contextualizer.
	 * 
	 * @param contextualizer to used.
	 */
	void setCriteriaContextualizer(@NonNull ISliceCriteriaContextualizer contextualizer);

	/**
	 * Sets the predicate to filter criteria.
	 * 
	 * @param predicate to be used.
	 */
	void setCriteriaFilterPredicate(@NonNull ISliceCriteriaPredicate<T2> predicate);

	/**
	 * Sets the predicate to filter out criteria sites.
	 * 
	 * @param predicate to be used.
	 * @pre predicate != null
	 */
	void setSiteSelectionPredicate(IPredicate<T1> predicate);
}

// End of File
