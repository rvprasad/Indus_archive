
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

package edu.ksu.cis.indus.tools.slicer.criteria.predicates;

import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;

import soot.SootField;


/**
 * This class provides facility to filter slice criteria based on scope specification.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ScopeBasedPredicate
  extends AbstractSliceCriteriaPredicate {
	/** 
	 * The specification-based matcher to be used.
	 */
	private SpecificationBasedScopeDefinition matcher;

	/**
	 * Sets the value of <code>matcher</code>.  This method should be called before using this generator.
	 *
	 * @param theMatcher the new value of <code>matcher</code>.
	 *
	 * @pre theMatcher != null
	 */
	public void setMatcher(final SpecificationBasedScopeDefinition theMatcher) {
		matcher = theMatcher;
	}

	/**
	 * Checks if criteria should be generated from the given entity.
	 *
	 * @param entity that serves as the basis for the slice criteria.
	 *
	 * @return <code>true</code> if criteria should be generated; <code>false</code>, otherwise.
	 *
	 * @pre entity.oclIsKindOf(SootField)
	 *
	 * @see ISliceCriteriaPredicate#shouldGenerateCriteriaFrom(java.lang.Object)
	 */
	public boolean evaluate(final Object entity) {
		return matcher.isInScope((SootField) entity, getSlicerTool().getSystem());
	}
}

// End of File
