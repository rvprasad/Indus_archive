
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

package edu.ksu.cis.indus.common;

import edu.ksu.cis.indus.IndusTestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * This class tests <code>CollectionsUtilities</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CollectionsUtilitiesTest
  extends IndusTestCase {
	/**
	 * The map.
	 */
	private Map map;

	/**
	 * Tests <code>putAllIntoCollectionInMap</code>.
	 */
	public final void testPutAllIntoCollectionInMap() {
		final Collection _temp = new ArrayList();
		_temp.add("second");

		final Collection _t = new ArrayList();
		CollectionsUtilities.putAllIntoCollectionInMap(map, "first", _temp, _t);
		assertNotNull(map.get("first"));
		assertSame(_t, map.get("first"));
		assertTrue(((Collection) map.get("first")).contains("second"));
	}

	/**
	 * Tests <code>putIntoCollectionInMap</code>.
	 */
	public final void testPutIntoCollectionInMap() {
		final Collection _temp = new ArrayList();
		_temp.add("second");

		final Collection _t = new ArrayList();
		CollectionsUtilities.putIntoCollectionInMap(map, "first", "second", _t);
		assertNotNull(map.get("first"));
		assertSame(_t, map.get("first"));
		assertTrue(((Collection) map.get("first")).contains("second"));
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		map = new HashMap();
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
        map.clear();
		map = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2004/04/21 02:24:04  venku
   - test clean up code was added.

   Revision 1.3  2004/02/09 00:28:33  venku
   - added a new class, IndusTestCase, that extends TestCase
     to differentiate between the test method name and the
     test instance name.
   - all test cases in indus extends IndusTestCase.
   - added a new method TestHelper to append container's name
     to the test cases.

   Revision 1.2  2004/02/07 16:13:29  venku
   - coding conventions.

   Revision 1.1  2004/01/28 22:55:23  venku
   - added test suites for classes in common package.
 */
