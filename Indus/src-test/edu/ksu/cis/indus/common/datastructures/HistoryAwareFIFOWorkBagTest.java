
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
 * This class tests <code>HistoryAwareFIFOWorkBag</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class HistoryAwareFIFOWorkBagTest
  extends HistoryAwareLIFOWorkBagTest {
	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		wb = new HistoryAwareFIFOWorkBag(new HashSet());
	}

	/**
	 * @see TestCase#tearDown()
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
   Revision 1.1  2004/03/29 01:55:15  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.

 */
