
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

import java.util.HashMap;
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
	public final void testEntrySet() {
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
	 * Tests the constructor.
	 */
	public final void testFilteredMap() {
		final Map _o1 =
			new FilteredMap(map,
				new Predicate() {
					public boolean evaluate(final Object o) {
						return o instanceof Integer;
					}
				}, null);
		assertNotNull(_o1);
		assertTrue(_o1.size() == 1);

		final Map.Entry _e1 = (Map.Entry) _o1.entrySet().iterator().next();
		assertTrue(_e1.getKey() instanceof Integer && _e1.getValue() instanceof String);

		final Map _o2 =
			new FilteredMap(map, null,
				new Predicate() {
					public boolean evaluate(final Object o) {
						return o instanceof Integer;
					}
				});
		assertNotNull(_o2);
		assertTrue(_o2.size() == 1);

		final Map.Entry _e2 = (Map.Entry) _o2.entrySet().iterator().next();
		assertTrue(_e2.getKey() instanceof String && _e2.getValue() instanceof Integer);
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

/*
   ChangeLog:
   $Log$
 */
