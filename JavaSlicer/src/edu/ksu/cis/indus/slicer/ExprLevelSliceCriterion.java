
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

import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;

import org.apache.commons.pool.impl.SoftReferenceObjectPool;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This class represents expression-level slice criterion.  This class supports object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class ExprLevelSliceCriterion
  extends AbstractProgramPointLevelSliceCriterion {
	/** 
	 * A pool of <code>ExprLevelSliceCriterion</code> criterion objects.
	 *
	 * @invariant EXPR_POOL.borrowObject().oclIsKindOf(ExprLevelSliceCriterion)
	 */
	static final ObjectPool EXPR_POOL =
		new SoftReferenceObjectPool(new BasePoolableObjectFactory() {
				/**
				 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
				 */
				public final Object makeObject() {
					final ExprLevelSliceCriterion _result = new ExprLevelSliceCriterion();
					_result.setPool(EXPR_POOL);
					return _result;
				}
			});

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ExprLevelSliceCriterion.class);

	/** 
	 * The statement associated with this criterion.
	 */
	protected Stmt stmt;

	/** 
	 * The expression associated with this criterion.
	 */
	protected ValueBox expr;

	/**
	 * Checks if the given object is "equal" to this object.
	 *
	 * @param o is the object to be compared.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise.
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o instanceof ExprLevelSliceCriterion) {
			final ExprLevelSliceCriterion _temp = (ExprLevelSliceCriterion) o;
			_result = _temp.expr == expr && _temp.stmt == stmt && super.equals(o);
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int _hash = super.hashCode();
		_hash = 37 * _hash + stmt.hashCode();
		_hash = 37 * _hash + expr.hashCode();
		return _hash;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this, CustomToStringStyle.HASHCODE_AT_END_STYLE).appendSuper(super.toString())
																					 .append("expr", this.expr).toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the expression(<code>ValueBox</code>) associated with criterion.
	 *
	 * @post result != null and result.oclIsKindOf(ValueBox)
	 *
	 * @see AbstractProgramPointLevelSliceCriterion#getCriterion()
	 */
	protected Object getCriterion() {
		return expr;
	}

	/**
	 * Provides the statement in which the slice expression occurs.
	 *
	 * @return the statement in which the slice expression occurs.
	 *
	 * @post result != null
	 */
	protected final Stmt getOccurringStmt() {
		return stmt;
	}

	/**
	 * Retrieves an expression-level slicing criterion object.
	 *
	 * @return an expression-level slicing criterion object.
	 *
	 * @throws RuntimeException if an object could not be retrieved from the pool.
	 *
	 * @post result != null
	 */
	static ExprLevelSliceCriterion getExprLevelSliceCriterion() {
		try {
			final ExprLevelSliceCriterion _result = (ExprLevelSliceCriterion) EXPR_POOL.borrowObject();
			return _result;
		} catch (final Exception _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("How can this happen?", _e);
			}
			throw new RuntimeException(_e);
		}
	}

	/**
	 * Initializes this object.
	 *
	 * @param occurringMethod in which the criterion containing statement occurs.
	 * @param occurringStmt in which the criterion containing expression occurs.
	 * @param criterion is the slicing criterion.
	 *
	 * @pre expr != null and stmt != null and method != null
	 */
	void initialize(final SootMethod occurringMethod, final Stmt occurringStmt, final ValueBox criterion) {
		super.initialize(occurringMethod);
		this.stmt = occurringStmt;
		this.expr = criterion;
	}
}

// End of File
