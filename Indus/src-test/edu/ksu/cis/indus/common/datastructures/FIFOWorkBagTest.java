
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

package edu.ksu.cis.indus.common.datastructures;

/**
 * This class tests <code>FIFOWorkBag</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FIFOWorkBagTest
  extends LIFOWorkBagTest {
	/**
	 * Tests <code>addWork</code> method.
	 */
	public final void testAddWork() {
		final Object _o1 = new Object();
		final Object _o2 = new Object();
		wb.addWork(_o1);
		wb.addWork(_o2);
		assertTrue(wb.getWork() == _o1);
		assertTrue(wb.getWork() == _o2);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		wb = new FIFOWorkBag();
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		wb = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/01/28 00:18:45  venku
   - added unit tests for classes in data structures package.
 */
