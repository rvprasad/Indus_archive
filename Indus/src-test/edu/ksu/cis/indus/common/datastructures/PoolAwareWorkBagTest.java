
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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.interfaces.IPoolable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.pool.ObjectPool;


/**
 * This class tests <code>PoolAwareWorkBag</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class PoolAwareWorkBagTest
  extends LIFOWorkBagTest {
	/**
	 * A dummy poolable object.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class Poolable
	  implements IPoolable {
		/**
		 * @see edu.ksu.cis.indus.interfaces.IPoolable#setPool(org.apache.commons.pool.ObjectPool)
		 */
		public void setPool(final ObjectPool thePool) {
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IPoolable#returnToPool()
		 */
		public void returnToPool() {
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.AbstractWorkBagTest#testAddAllWorkNoDuplicates()
	 */
	public void testAddAllWorkNoDuplicates() {
		final Collection _poolables = getPoolables();
		assertTrue(wb.addAllWorkNoDuplicates(_poolables).isEmpty());
		assertTrue(wb.addAllWorkNoDuplicates(_poolables).isEmpty());
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.AbstractWorkBagTest#testAddWorkNoDuplicates()
	 */
	public void testAddWorkNoDuplicates() {
		final List _poolables = getPoolables();
		assertTrue(wb.addWorkNoDuplicates(_poolables.get(0)));
		assertFalse(wb.addWorkNoDuplicates(_poolables.get(0)));
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		wb = new PoolAwareWorkBag(new LIFOWorkBag());
	}

	/**
	 * Retrieve a collection of poolable objects.
	 *
	 * @return a collection of <code>IPoolable</code> objects.
	 */
	private List getPoolables() {
		final List _result = new ArrayList();
		final int _ten = 10;

		for (int _i = 0; _i <= _ten; _i++) {
			_result.add(new Poolable());
		}
		return _result;
	}
}

// End of File
