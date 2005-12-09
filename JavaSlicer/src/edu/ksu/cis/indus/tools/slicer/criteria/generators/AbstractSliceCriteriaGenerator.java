
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
 * @param <T1> DOCUMENT ME!
 * @param <T2> DOCUMENT ME!
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
	 * @see ISliceCriteriaGenerator#getCriteria(edu.ksu.cis.indus.tools.slicer.SlicerTool)
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
	 * @see ISliceCriteriaGenerator#setCriteriaContextualizer(ISliceCriteriaContextualizer)
	 */
	public final void setCriteriaContextualizer(final ISliceCriteriaContextualizer theContextualizer) {
		contextualizer = theContextualizer;
	}

	/**
	 * @see ISliceCriteriaGenerator#setCriteriaFilterPredicate(ISliceCriteriaPredicate)
	 */
	public final void setCriteriaFilterPredicate(final ISliceCriteriaPredicate<T2> thePredicate) {
		criteriaPredicate = thePredicate;
	}

	/**
	 * @see ISliceCriteriaGenerator#setSiteSelectionPredicate(IPredicate)
	 */
	public final void setSiteSelectionPredicate(final IPredicate<T1> thePredicate) {
		siteSelectionPredicate = thePredicate;
	}

	/**
	 * Contextualizes the given criteria.
	 *
	 * @param context in which <code>baseCriteria</code> were generated.
	 * @param baseCriteria is the collection of criteria to be contextualized.  This collection will be modified upon return
	 * 		  with contextualized criteria.
	 *
	 * @pre context != null and baseCriteria != null
	 * @pre baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 * @post baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	protected final void contextualizeCriteriaBasedOnProgramPoint(final Context context, final Collection<ISliceCriterion> baseCriteria) {
		if (contextualizer != null) {
			contextualizer.processCriteriaBasedOnProgramPoint(context, baseCriteria);
		}
	}

	/**
	 * Contextualizes the given criteria.
	 *
	 * @param method in which <code>baseCriteria</code> were generated.
	 * @param baseCriteria is the collection of criteria to be contextualized.  This collection will be modified upon return
	 * 		  with contextualized criteria.
	 *
	 * @pre context != null and baseCriteria != null
	 * @pre baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 * @post baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	protected final void contextualizeCriteriaBasedOnThis(final SootMethod method, final Collection<ISliceCriterion> baseCriteria) {
		if (contextualizer != null) {
			contextualizer.processCriteriaBasedOnThis(method, baseCriteria);
		}
	}

	/**
	 * Checks if the given site should be considered for criteria generation.
	 *
	 * @param site to be checked.
	 *
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
	 *
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	protected abstract Collection<ISliceCriterion> getCriteriaTemplateMethod();

	/**
	 * Retrieves the value in <code>slicerTool</code>.
	 *
	 * @return the value in <code>slicerTool</code>.
	 */
	protected SlicerTool<?> getSlicerTool() {
		return slicerTool;
	}

	/**
	 * Checks if  criteria should be generated based on the given entity.
	 *
	 * @param entity forms the base for the criteria.
	 *
	 * @return <code>true</code>
	 *
	 * @pre entity != null and slicer != null
	 */
	protected final boolean shouldGenerateCriteriaFrom(final T2 entity) {
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
