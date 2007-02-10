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

package edu.ksu.cis.indus.tools.slicer.contextualizers;

import edu.ksu.cis.indus.annotations.Empty;
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

		@Empty public void processCriteriaBasedOnProgramPoint(@SuppressWarnings("unused") final Context programPoint,
				@SuppressWarnings("unused") final Collection<ISliceCriterion> baseCriteria) {
			// does nothing
		}

		@Empty public void processCriteriaBasedOnThis(@SuppressWarnings("unused") final SootMethod method,
				@SuppressWarnings("unused") final Collection<ISliceCriterion> baseCriteria) {
			// does nothing
		}

		@Empty public void setSlicerTool(@SuppressWarnings("unused") final SlicerTool<?> slicer) {
			// does nothing
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
	void setSlicerTool(SlicerTool<?> slicer);
}

// End of File
