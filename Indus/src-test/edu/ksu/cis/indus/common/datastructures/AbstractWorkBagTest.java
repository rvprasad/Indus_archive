
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;


/**
 * This class tests <code>AbstractWorkBag</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractWorkBagTest
  extends TestCase {
	/**
	 * The workbag.
	 */
	protected IWorkBag wb;

	/**
	 * Tests <code>addAllWork</code>.
	 */
	public void testAddAllWork() {
		final List _workPieces = getWorkPieces();
		wb.addAllWork(_workPieces);

		while (wb.hasWork()) {
			assertTrue(_workPieces.contains(wb.getWork()));
		}
	}

	/**
	 * Tests <code>addAllWorkNoDuplicates</code>.
	 */
	public void testAddAllWorkNoDuplicates() {
		final List _workPieces = getWorkPieces();
		wb.addAllWork(_workPieces);

		final int _two = 2;
		final int _five = 5;

		final List _subList = new ArrayList(_workPieces.subList(_two, _five));
		_subList.add(new Object());

		final Collection _temp = wb.addAllWorkNoDuplicates(_subList);
		assertTrue(_subList.containsAll(_temp));
		assertTrue(!_temp.containsAll(_subList));
	}

	/**
	 * Tests <code>addWorkNoDuplicate</code>.
	 */
	public void testAddWorkNoDuplicates() {
		final List _workPieces = getWorkPieces();
		wb.addAllWork(_workPieces);
		assertFalse(wb.addWorkNoDuplicates(_workPieces.get(0)));
		assertTrue(wb.addWorkNoDuplicates(new Object()));
	}

	/**
	 * Tests <code>clear</code>.
	 */
	public final void testClear() {
		wb.clear();
		assertFalse(wb.hasWork());

		try {
			wb.getWork();
			///CLOVER:OFF            
			fail("This is incorrect behavior.");
			///CLOVER:ON
		} catch (IllegalStateException _e) {
			_e.printStackTrace();
		}
	}

	/**
	 * Tests <code>getWork</code> and <code>hasWork</code>.
	 */
	public void testGetWorkandHasWork() {
		final Object _object = new Object();
		wb.addWork(_object);
		assertTrue(wb.hasWork());

		final Object _temp = wb.getWork();
		assertTrue(_temp != null);
		assertTrue(_temp == _object);
	}

	/**
	 * Provides work pieces.
	 *
	 * @return a collection of work pieces.
	 */
	private List getWorkPieces() {
		final List _result = new ArrayList();
		final int _ten = 10;

		for (int _i = 1; _i <= _ten; _i++) {
			_result.add(String.valueOf(_i));
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/01/28 22:44:16  venku
   empty message.
   Revision 1.2  2004/01/28 22:42:27  venku
   empty log message
   Revision 1.1  2004/01/28 00:18:45  venku
   - added unit tests for classes in data structures package.
 */
