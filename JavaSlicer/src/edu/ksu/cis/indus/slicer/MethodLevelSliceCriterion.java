
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;

import org.apache.commons.pool.impl.SoftReferenceObjectPool;

import soot.SootMethod;


/**
 * This class represents method-level slice criterion.  This class has support builtin for object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class MethodLevelSliceCriterion
  extends AbstractPoolable
  implements ISliceCriterion {
	/** 
	 * A pool of <code>StmtLevelSliceCriterion</code> criterion objects.
	 *
	 * @invariant STMT_POOL.borrowObject().oclIsKindOf(StmtLevelSliceCriterion)
	 */
	static final ObjectPool METHOD_POOL =
		new SoftReferenceObjectPool(new BasePoolableObjectFactory() {
				/**
				 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
				 */
				public final Object makeObject() {
					final MethodLevelSliceCriterion _result = new MethodLevelSliceCriterion();
					_result.setPool(METHOD_POOL);
					return _result;
				}
			});

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(MethodLevelSliceCriterion.class);

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
	 * Checks if the given object is "equal" to this object.
	 *
	 * @param o is the object to be compared.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise.
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o instanceof MethodLevelSliceCriterion) {
			final MethodLevelSliceCriterion _methodLevelSliceCriterion = (MethodLevelSliceCriterion) o;
			_result = _methodLevelSliceCriterion.method.equals(method);

			if (_result) {
				if (callStack != null) {
					_result = callStack.equals(_methodLevelSliceCriterion.callStack);
				} else {
					_result = callStack == _methodLevelSliceCriterion.callStack;
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
		return new ToStringBuilder(this, CustomToStringStyle.HASHCODE_AT_END_STYLE).append("method", this.method)
																					 .append("callStack", this.callStack)
																					 .toString();
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
	 * Retrieves a method-level slicing criterion object.
	 *
	 * @return a method-level slicing criterion object.
	 *
	 * @throws RuntimeException if an object could not be retrieved from the pool.
	 *
	 * @post result != null
	 */
	static MethodLevelSliceCriterion getMethodLevelSliceCriterion() {
		MethodLevelSliceCriterion _result;

		try {
			_result = (MethodLevelSliceCriterion) METHOD_POOL.borrowObject();
		} catch (final Exception _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("How can this happen?", _e);
			}
			throw new RuntimeException(_e);
		}
		return _result;
	}
}

// End of File
