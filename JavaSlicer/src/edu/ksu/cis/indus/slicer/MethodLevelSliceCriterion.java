
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;

import org.apache.commons.pool.impl.SoftReferenceObjectPool;



/**
 * This class represents method-level slice criterion.  This class has support builtin for object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class MethodLevelSliceCriterion
  extends AbstractProgramPointLevelSliceCriterion {
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
	 * @see edu.ksu.cis.indus.interfaces.IPoolable#returnToPool()
	 */
	public void returnToPool() {
		setCallStack(null);
		super.returnToPool();
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
