
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

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.SootMethod;


/**
 * This class represents a slice criterion.  This class has support builtin for object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
abstract class AbstractSliceCriterion
  extends AbstractPoolable
  implements ISliceCriterion {
	/** 
	 * The method in which <code>stmt</code> occurs.
	 */
	protected SootMethod method;

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
	 * Checks if the given object is "equal" to this object.
	 *
	 * @param o is the object to be compared.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise.
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o instanceof AbstractSliceCriterion) {
			_result =
				((AbstractSliceCriterion) o).method.equals(method)
				  && ((AbstractSliceCriterion) o).considerExecution == considerExecution;

			if (_result) {
				if (callStack != null) {
					_result = callStack.equals(((AbstractSliceCriterion) o).callStack);
				} else {
					_result = callStack == ((AbstractSliceCriterion) o).callStack;
				}
			}
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int _hash = 17;
		_hash = _hash * 37 + Boolean.valueOf(considerExecution).hashCode();
		_hash = _hash * 37 + method.hashCode();

		if (callStack != null) {
			_hash = _hash * 37 + callStack.hashCode();
		}
		return _hash;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IPoolable#returnToPool()
	 */
	public void returnToPool() {
		callStack = null;
		super.returnToPool();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this, CustomToStringStyle.HASHCODE_AT_END_STYLE).append("considerExecution",
			this.considerExecution).append("method", this.method).append("callStack", this.callStack).toString();
	}

	/**
	 * Provides the method in which criterion occurs.
	 *
	 * @return the method in which the slice statement occurs.
	 *
	 * @post result != null
	 */
	protected final SootMethod getOccurringMethod() {
		return method;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	public final Object clone()
	  throws CloneNotSupportedException {
		return super.clone();
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
	 * Returns the stored criterion object.
	 *
	 * @return Object representing the criterion.
	 *
	 * @post result != null
	 */
	abstract Object getCriterion();

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
