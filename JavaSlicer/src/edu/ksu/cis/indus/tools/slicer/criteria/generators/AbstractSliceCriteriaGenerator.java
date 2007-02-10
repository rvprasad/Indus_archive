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
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;
import edu.ksu.cis.indus.tools.slicer.contextualizers.ISliceCriteriaContextualizer;
import edu.ksu.cis.indus.tools.slicer.criteria.predicates.ISliceCriteriaPredicate;

import java.util.Collection;

import soot.SootMethod;

/**
 * This is an abstract implementation of <code>ISliceCriteriaGenerator</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T1> is the type of objects that are selected in expectation of containing slice criteria.
 * @param <T2> is the type of objects that will be considered for being used as slice criteria.
 */
public abstract class AbstractSliceCriteriaGenerator<T1, T2>
		implements ISliceCriteriaGenerator<T1, T2> {

	/**
	 * The contextualizer to use.
	 */
	private ISliceCriteriaContextualizer contextualizer;

	/**
	 * The filter to use.
	 */
	private ISliceCriteriaPredicate<T2> criteriaPredicate;

	/**
	 * The predicate to select sites in which the criteria may occur.
	 */
	private IPredicate<T1> siteSelectionPredicate;

	/**
	 * The slicer that defines the context in which generator functions.
	 */
	private SlicerTool<?> slicerTool;

	/**
	 * {@inheritDoc}
	 */
	public final Collection<ISliceCriterion> getCriteria(final SlicerTool<?> slicer) {
		slicerTool = slicer;

		if (contextualizer != null) {
			contextualizer.setSlicerTool(slicerTool);
		}

		if (criteriaPredicate != null) {
			criteriaPredicate.setSlicerTool(slicerTool);
		}
		return getCriteriaTemplateMethod();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setCriteriaContextualizer(final ISliceCriteriaContextualizer theContextualizer) {
		contextualizer = theContextualizer;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setCriteriaFilterPredicate(final ISliceCriteriaPredicate<T2> thePredicate) {
		criteriaPredicate = thePredicate;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setSiteSelectionPredicate(final IPredicate<T1> thePredicate) {
		siteSelectionPredicate = thePredicate;
	}

	/**
	 * Contextualizes the given criteria.
	 * 
	 * @param context in which <code>baseCriteria</code> were generated.
	 * @param baseCriteria is the collection of criteria to be contextualized. This collection will be modified upon return
	 *            with contextualized criteria.
	 */
	protected final void contextualizeCriteriaBasedOnProgramPoint(@NonNull final Context context,
			@NonNull @NonNullContainer final Collection<ISliceCriterion> baseCriteria) {
		if (contextualizer != null) {
			contextualizer.processCriteriaBasedOnProgramPoint(context, baseCriteria);
		}
	}

	/**
	 * Contextualizes the given criteria.
	 * 
	 * @param method in which <code>baseCriteria</code> were generated.
	 * @param baseCriteria is the collection of criteria to be contextualized. This collection will be modified upon return
	 *            with contextualized criteria.
	 */
	protected final void contextualizeCriteriaBasedOnThis(@NonNull final SootMethod method,
			@NonNull @NonNullContainer final Collection<ISliceCriterion> baseCriteria) {
		if (contextualizer != null) {
			contextualizer.processCriteriaBasedOnThis(method, baseCriteria);
		}
	}

	/**
	 * Checks if the given site should be considered for criteria generation.
	 * 
	 * @param site to be checked.
	 * @return <code>true</code> if site should be considered; <code>false</code>, otherwise.
	 */
	protected final boolean shouldConsiderSite(final T1 site) {
		final boolean _result;

		if (siteSelectionPredicate != null) {
			_result = siteSelectionPredicate.evaluate(site);
		} else {
			_result = true;
		}
		return _result;
	}

	/**
	 * This is a template method that the subclasses should implement to generate the criteria.
	 * 
	 * @return a collection of criteria.
	 */
	@NonNull @NonNullContainer protected abstract Collection<ISliceCriterion> getCriteriaTemplateMethod();

	/**
	 * Retrieves the value in <code>slicerTool</code>.
	 * 
	 * @return the value in <code>slicerTool</code>.
	 */
	protected SlicerTool<?> getSlicerTool() {
		return slicerTool;
	}

	/**
	 * Checks if criteria should be generated based on the given entity.
	 * 
	 * @param entity forms the base for the criteria.
	 * @return <code>true</code>
	 */
	protected final boolean shouldGenerateCriteriaFrom(@NonNull final T2 entity) {
		final boolean _result;

		if (criteriaPredicate != null) {
			_result = criteriaPredicate.evaluate(entity);
		} else {
			_result = true;
		}
		return _result;
	}
}

// End of File
