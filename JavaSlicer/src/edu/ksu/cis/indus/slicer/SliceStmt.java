
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

import soot.jimple.Stmt;


/**
 * This class represents a statement as a slice criterion.  This class supports object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class SliceStmt
  extends AbstractSliceCriterion {
	/** 
	 * A pool of <code>SliceStmt</code> criterion objects.
	 *
	 * @invariant STMT_POOL.borrowObject().oclIsKindOf(SliceStmt)
	 */
	static final ObjectPool STMT_POOL =
		new SoftReferenceObjectPool(new BasePoolableObjectFactory() {
				/**
				 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
				 */
				public final Object makeObject() {
					final SliceStmt _result = new SliceStmt();
					_result.setPool(STMT_POOL);
					return _result;
				}
			});

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceStmt.class);

	/** 
	 * The statement associated with this criterion.
	 */
	protected Stmt stmt;

	/**
	 * Checks if the given object is "equal" to this object.
	 *
	 * @param o is the object to be compared.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise.
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o instanceof SliceStmt) {
			final SliceStmt _temp = (SliceStmt) o;
			_result = _temp.stmt == stmt && super.equals(o);
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int _hash = super.hashCode();
		_hash = 37 * _hash + stmt.hashCode();
		return _hash;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this, CustomToStringStyle.HASHCODE_AT_END_STYLE).appendSuper(super.toString())
																					 .append("stmt", this.stmt).toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the statement(<code>Stmt</code>) associated with this criterion.
	 *
	 * @post result != null and result.oclIsKindOf(jimple.Stmt)
	 *
	 * @see AbstractSliceCriterion#getCriterion()
	 */
	protected Object getCriterion() {
		return stmt;
	}

	/**
	 * Retrieves a statement-level slicing criterion object.
	 *
	 * @return a statement-level slicing criterion object.
	 *
	 * @throws RuntimeException if an object could not be retrieved from the pool.
	 *
	 * @post result != null
	 */
	static SliceStmt getSliceStmt() {
		SliceStmt _result;

		try {
			_result = (SliceStmt) STMT_POOL.borrowObject();
		} catch (Exception _e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("How can this happen?", _e);
			}
			throw new RuntimeException(_e);
		}
		return _result;
	}

	/**
	 * Initializes this object.
	 *
	 * @param occurringMethod in which the slice criterion occurs.
	 * @param criterion is the slice criterion.
	 *
	 * @pre method != null and stmt != null
	 */
	void initialize(final SootMethod occurringMethod, final Stmt criterion) {
		super.initialize(occurringMethod);
		this.stmt = criterion;
	}
}

// End of File
