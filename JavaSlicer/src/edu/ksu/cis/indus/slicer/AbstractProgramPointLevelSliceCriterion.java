
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

package edu.ksu.cis.indus.slicer;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This class encapsulates properties of a slice criterion pertaining to program points.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
abstract class AbstractProgramPointLevelSliceCriterion
  extends MethodLevelSliceCriterion {
	/** 
	 * This indicates if the effect of executing the criterion should be considered for slicing.  By default it takes on  the
	 * value <code>false</code> to indicate execution should not be considered.
	 */
	private boolean considerExecution;

	/**
	 * @see edu.ksu.cis.indus.slicer.MethodLevelSliceCriterion#equals(java.lang.Object)
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o instanceof AbstractProgramPointLevelSliceCriterion) {
			_result = super.equals(o) && ((AbstractProgramPointLevelSliceCriterion) o).considerExecution == considerExecution;
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.MethodLevelSliceCriterion#hashCode()
	 */
	public int hashCode() {
		return super.hashCode() * 37 + Boolean.valueOf(considerExecution).hashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("considerExecution", this.considerExecution)
										  .toString();
	}

	/**
	 * Returns the stored criterion object.
	 *
	 * @return Object representing the criterion.
	 *
	 * @post result != null
	 */
	abstract Object getCriterion();

	/**
	 * Sets the flag to indicate if the execution of the criterion should be considered during slicing.
	 *
	 * @param shouldConsiderExecution <code>true</code> indicates that the effect of executing this criterion should be
	 * 		  considered while slicing.  This also means all the subexpressions of the associated expression are also
	 * 		  considered as slice criteria. <code>false</code> indicates that just the mere effect of the control reaching
	 * 		  this criterion should be considered while slicing.  This means none of the subexpressions of the associated
	 * 		  expression are considered as slice criteria.
	 */
	final void setConsiderExecution(final boolean shouldConsiderExecution) {
		considerExecution = shouldConsiderExecution;
	}

	/**
	 * Indicates if the effect of execution of criterion should be considered.
	 *
	 * @return <code>true</code> if the effect of execution should be considered; <code>false</code>, otherwise.
	 */
	final boolean isConsiderExecution() {
		return considerExecution;
	}
}

// End of File
