
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

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/04/21 02:24:04  venku
   - test clean up code was added.

   Revision 1.2  2004/04/05 22:30:45  venku
   - renamed HistoryAwareAbstractWorkBag to AbstractHistoryAwareWorkBag.
   - ripple effect on test cases.
   - Now PoolAwareWorkBag does not assume all work is poolable.

   Revision 1.1  2004/03/29 01:55:15  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.

 */
