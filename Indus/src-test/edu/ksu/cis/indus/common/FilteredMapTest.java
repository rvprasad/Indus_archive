
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.collections.Predicate;


/**
 * This class tests <code>FilteredMap</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FilteredMapTest
  extends TestCase {
	/** 
	 * The map used for testing.
	 */
	private Map map;

	/**
	 * Tests <code>entrySet()</code>.
	 */
	public final void testEntrySet1() {
		final Map _o1 =
			new FilteredMap(map,
				new Predicate() {
					public boolean evaluate(final Object o) {
						return o instanceof Integer;
					}
				}, null);
		final Set _entrySet = _o1.entrySet();
		assertTrue(_entrySet.size() == 1);

		final Map.Entry _entry = (Map.Entry) _entrySet.iterator().next();
		assertEquals(_entry.getKey(), new Integer(1));
		assertEquals(_entry.getValue(), new String("Hi"));
	}

	/**
	 * Tests <code>entrySet()</code>.
	 */
	public final void testEntrySet2() {
		final Map _o1 =
			new FilteredMap(map, null,
				new Predicate() {
					public boolean evaluate(final Object o) {
						return o instanceof Integer;
					}
				});
		final Set _entrySet = _o1.entrySet();
		assertTrue(_entrySet.size() == 1);

		final Iterator _iterator = _entrySet.iterator();
		final Map.Entry _entry = (Map.Entry) _iterator.next();
		assertEquals(_entry.getKey(), new String("Hello"));
		assertEquals(_entry.getValue(), new Integer(2));
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		map = new HashMap();
		map.put(new Integer(1), new String("Hi"));
		map.put(new String("Hello"), new Integer(2));
		map.put(new String("GDay"), new String("bye"));
		super.setUp();
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		map = null;
		super.tearDown();
	}
}

// End of File
