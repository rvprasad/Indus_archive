
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

package edu.ksu.cis.indus.common.datastructures;

import java.util.List;


/**
 * This class tests <code>AbstractHistoryAwareWorkBag</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractHistoryAwareWorkBagTest
  extends AbstractWorkBagTest {
	/**
	 * Tests <code>addAllWork</code>.
	 */
	public void testAddAllWork() {
		super.testAddAllWork();

		final List _workPieces = getWorkPieces();
		wb.addAllWork(_workPieces);
		assertFalse(wb.hasWork());
	}
}

// End of File
