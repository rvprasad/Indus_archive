
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;

import soot.SootMethod;


/**
 * This is the interface used to inject context into slice criteria.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISliceCriteriaContextualizer {
	/** 
	 * This is a dummy contextualizer that does not inject any context.  Hence, the criteria are most pessimistic, i.e, all
	 * calling contexts are considered.
	 */
	ISliceCriteriaContextualizer DUMMY_CONTEXTUALIZER =
		new ISliceCriteriaContextualizer() {
			public void setSlicerTool(final SlicerTool slicer) {
			}

			public void processCriteriaBasedOnProgramPoint(final Context context, final Collection baseCriteria) {
			}

			public void processCriteriaBasedOnThis(final SootMethod method, final Collection baseCriteria) {
			}
		};

	/**
	 * Sets the slicer tool in conjunction of which this filter is being used.
	 *
	 * @param slicer is the tool to be used.
	 */
	void setSlicerTool(SlicerTool slicer);

	/**
	 * Injects context into the given criteria based on the program point defined by <code>context</code>.
	 *
	 * @param context that defines the program point as the basis of the context that will be injected.
	 * @param baseCriteria is a collection of criteria to be injected with context.  Implementations should dispose the  base
	 * 		  criteria and populate this collection with contextualized criteria upon return.
	 *
	 * @pre context != null and baseCriteria != null
	 * @pre baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	void processCriteriaBasedOnProgramPoint(Context context, Collection baseCriteria);

	/**
	 * Injects context into the given criteria based on the context provided by <code>method</code>.
	 *
	 * @param method that defines the enclosing context as the basis of the context that will be injected.
	 * @param baseCriteria is a collection of criteria to be injected with context. Implementations should dispose the  base
	 * 		  criteria and populate this collection with contextualized criteria upon return.
	 *
	 * @pre method != null and baseCriteria != null
	 * @pre baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	void processCriteriaBasedOnThis(SootMethod method, Collection baseCriteria);
}

// End of File
