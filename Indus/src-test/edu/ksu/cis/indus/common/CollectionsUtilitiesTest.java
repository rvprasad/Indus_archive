
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
		CollectionsUtilities.putAllIntoCollectionInMap(map, "first", _temp, CollectionsUtilities.ARRAY_LIST_FACTORY);
		assertNotNull(map.get("first"));
		assertTrue(((Collection) map.get("first")).contains("second"));
	}

	/**
	 * Tests <code>putIntoCollectionInMap</code>.
	 */
	public final void testPutIntoCollectionInMap() {
		final Collection _temp = new ArrayList();
		_temp.add("second");
		CollectionsUtilities.putIntoCollectionInMap(map, "first", "second", CollectionsUtilities.ARRAY_LIST_FACTORY);
		assertNotNull(map.get("first"));
		assertTrue(((Collection) map.get("first")).contains("second"));
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		map = new HashMap();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		map.clear();
		map = null;
	}
}

// End of File
