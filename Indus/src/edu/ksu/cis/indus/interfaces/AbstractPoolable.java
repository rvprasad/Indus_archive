
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

package edu.ksu.cis.indus.interfaces;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.pool.ObjectPool;


/**
 * This provides an abstract implementation of <code>IPoolable</code>.  Concrete implementation should extend this class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractPoolable
  implements IPoolable {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractPoolable.class);

	/** 
	 * The pool to which this object should be returned to.
	 */
	private ObjectPool pool;

	/**
	 * {@inheritDoc}
	 */
	public final void setPool(final ObjectPool thePool) {
		if (thePool == null) {
			throw new IllegalArgumentException("Invalid argument: null");
		}
		pool = thePool;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws RuntimeException if the returning of the object to it's pool failed.
	 */
	public void returnToPool() {
		if (pool != null) {
			try {
				pool.returnObject(this);
			} catch (final Exception _e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("How can this happen?", _e);
				}
				throw new RuntimeException(_e);
			}
		}
	}
}

// End of File
