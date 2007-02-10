
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

import java.util.HashSet;


/**
 * This class tests <code>HistoryAwareLIFOWorkBag</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class HistoryAwareLIFOWorkBagTest
  extends AbstractHistoryAwareWorkBagTest {
	/**
	 * Tests <code>addWork</code> method.
	 */
	public final void testAddWork() {
		final Object _o1 = "test1";
		final Object _o2 = "test1";
		wb.addWork(_o1);
		wb.addWork(_o2);
		assertTrue(wb.getWork() == _o1);
		assertFalse(wb.hasWork());
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		wb = new HistoryAwareLIFOWorkBag(new HashSet());
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		wb.clear();
		wb = null;
	}
}

// End of File
