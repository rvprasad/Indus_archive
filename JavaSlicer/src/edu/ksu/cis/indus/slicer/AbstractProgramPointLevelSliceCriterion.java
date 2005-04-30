
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

import edu.ksu.cis.indus.common.CustomToStringStyle;

import edu.ksu.cis.indus.interfaces.AbstractPoolable;

import java.util.Stack;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import soot.SootMethod;


/**
 * This class encapsulates properties of a slice criterion pertaining to program points.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
abstract class AbstractProgramPointLevelSliceCriterion
  extends AbstractPoolable
  implements ISliceCriterion {
	/** 
	 * The method which is syntactically part of the slice criteria interest..
	 */
	private SootMethod method;

	/** 
	 * This captures the call sequence that caused this criterion in the callee to occur.  So, when slicing, if this field is
	 * non-null, we only will return to the call-site instead of all possible call-sites (which is what happend if this
	 * field is null).
	 */
	private Stack callStack;

	/** 
	 * This indicates if the effect of executing the criterion should be considered for slicing.  By default it takes on  the
	 * value <code>false</code> to indicate execution should not be considered.
	 */
	private boolean considerExecution;

	/**
	 * @see ISliceCriterion#setCallStack(Stack)
	 */
	public final void setCallStack(final Stack theCallStack) {
		callStack = theCallStack;
	}

	/**
	 * @see ISliceCriterion#getCallStack()
	 */
	public final Stack getCallStack() {
		return callStack;
	}

	/**
	 * @see ISliceCriterion#getOccurringMethod()
	 */
	public final SootMethod getOccurringMethod() {
		return method;
	}

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof AbstractProgramPointLevelSliceCriterion)) {
			return false;
		}

		final AbstractProgramPointLevelSliceCriterion _rhs = (AbstractProgramPointLevelSliceCriterion) object;
		return new EqualsBuilder().append(this.considerExecution, _rhs.considerExecution)
									.append(this.callStack, _rhs.callStack).append(this.method, _rhs.method).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(this.considerExecution).append(this.callStack).append(this.method)
											.toHashCode();
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IPoolable#returnToPool()
	 */
	public void returnToPool() {
		setCallStack(null);
		super.returnToPool();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this, CustomToStringStyle.HASHCODE_AT_END_STYLE).appendSuper(super.toString())
																					 .append("considerExecution",
			  this.considerExecution).append("callStack", this.callStack).append("method", this.method).toString();
	}

	/**
	 * Initializes this object.
	 *
	 * @param occurringMethod in which the slice criterion occurs.
	 */
	protected final void initialize(final SootMethod occurringMethod) {
		method = occurringMethod;
	}

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

	/**
	 * Returns the stored criterion object.
	 *
	 * @return Object representing the criterion.
	 *
	 * @throws UnsupportedOperationException when this implementation is invoked.
	 *
	 * @post result != null
	 */
	Object getCriterion() {
		throw new UnsupportedOperationException("This operation is not supported.");
	}
}

// End of File
