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

package edu.ksu.cis.indus.tools.slicer.contextualizers;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

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
	 * This is a dummy contextualizer that does not inject any context. Hence, the criteria are most pessimistic, i.e, all
	 * calling contexts are considered.
	 */
	ISliceCriteriaContextualizer DUMMY_CONTEXTUALIZER = new ISliceCriteriaContextualizer() {

		public void processCriteriaBasedOnProgramPoint(@SuppressWarnings("unused") final Context programPoint,
				@SuppressWarnings("unused") final Collection<ISliceCriterion> baseCriteria) {
		}

		public void processCriteriaBasedOnThis(@SuppressWarnings("unused") final SootMethod method,
				@SuppressWarnings("unused") final Collection<ISliceCriterion> baseCriteria) {
		}

		public void setSlicerTool(@SuppressWarnings("unused") final SlicerTool slicer) {
		}
	};

	/**
	 * Injects context into the given criteria based on the program point defined by <code>context</code>.
	 * 
	 * @param programPoint that defines the program point as the basis of the context that will be injected.
	 * @param baseCriteria is a collection of criteria to be injected with context. Implementations should populate this
	 *            collection with contextualized criteria upon return. The implementations may reuse or dispose the provided
	 *            criteria.
	 * @pre programPoint != null and baseCriteria != null
	 */
	void processCriteriaBasedOnProgramPoint(Context programPoint, Collection<ISliceCriterion> baseCriteria);

	/**
	 * Injects context into the given criteria based on the context provided by <code>method</code>.
	 * 
	 * @param method that defines the enclosing context as the basis of the context that will be injected.
	 * @param baseCriteria is a collection of criteria to be injected with context. Implementations should populate this
	 *            collection with contextualized criteria upon return. The implementations may reuse or dispose the provided
	 *            criteria.
	 * @pre method != null and baseCriteria != null
	 */
	void processCriteriaBasedOnThis(SootMethod method, Collection<ISliceCriterion> baseCriteria);

	/**
	 * Sets the slicer tool in conjunction of which this filter is being used.
	 * 
	 * @param slicer is the tool to be used.
	 */
	void setSlicerTool(SlicerTool slicer);
}

// End of File
