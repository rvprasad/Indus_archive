
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import org.apache.commons.pool.ObjectPool;


/**
 * This is a generic interface to be implemented by poolable objects.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IPoolable {
	/**
	 * Sets the pool to which this object should be returned to.
	 *
	 * @param thePool to which this object should be returned.
	 *
	 * @pre thePool != null
	 */
	void setPool(final ObjectPool thePool);

	/**
	 * Returns the object to it's pool.
	 */
	void returnToPool();
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/04 09:45:29  venku
   - added interface and it's abstract implementation to
     support pooling of objects.
 */
