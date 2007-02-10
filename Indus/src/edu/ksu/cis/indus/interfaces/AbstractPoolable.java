
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

package edu.ksu.cis.indus.interfaces;

import org.apache.commons.pool.ObjectPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPoolable.class);

	/** 
	 * The pool to which this object should be returned to.
	 */
	private ObjectPool pool;

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

	/**
	 * {@inheritDoc}
	 */
	public final void setPool(final ObjectPool thePool) {
		if (thePool == null) {
			throw new IllegalArgumentException("Invalid argument: null");
		}
		pool = thePool;
	}
}

// End of File
