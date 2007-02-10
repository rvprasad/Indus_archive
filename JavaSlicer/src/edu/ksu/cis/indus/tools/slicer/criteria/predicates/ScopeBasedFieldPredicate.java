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

package edu.ksu.cis.indus.tools.slicer.criteria.predicates;

import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import soot.SootField;

/**
 * This class provides facility to filter field based slice criteria based on scope specification.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ScopeBasedFieldPredicate
		extends AbstractSliceCriteriaPredicate<SootField> {

	/**
	 * The specification-based matcher to be used.
	 */
	private SpecificationBasedScopeDefinition matcher;

	/**
	 * Checks if criteria should be generated from the given entity.
	 *
	 * @param entity that serves as the basis for the slice criteria.
	 * @return <code>true</code> if criteria should be generated; <code>false</code>, otherwise.
	 * @pre entity.oclIsKindOf(SootField)
	 */
	public <T1 extends SootField> boolean evaluate(final T1 entity) {
		return matcher.isInScope(entity, getSlicerTool().getSystem());
	}

	/**
	 * Sets the value of <code>matcher</code>. This method should be called before using this generator.
	 *
	 * @param theMatcher the new value of <code>matcher</code>.
	 * @pre theMatcher != null
	 */
	public void setMatcher(final SpecificationBasedScopeDefinition theMatcher) {
		matcher = theMatcher;
	}
}

// End of File
