
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

package edu.ksu.cis.indus.common.collections;

import java.util.Collections;
import java.util.NoSuchElementException;

import junit.framework.TestCase;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class RetrievableSetTestCase
  extends TestCase {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private RetrievableSet set;

	/**
	 * Class under test for <code>boolean add(Object)</code>.
	 */
	public final void testAddObject() {
		set.add(null);
		assertTrue(set.contains(null));
		assertFalse(set.contains("Hi"));

		set.add("Hi");
		assertTrue(set.contains(null));
		assertTrue(set.contains("Hi"));

		set.add("Hi");
		assertTrue(set.contains(null));
		assertTrue(set.contains("Hi"));
		assertTrue(set.size() == 2);
	}

	/**
	 * Class under test for <code>void clear()</code>.
	 */
	public final void testClear() {
		set.add(null);
		set.add("Hi");
		assertTrue(set.size() == 2);

		set.clear();
		assertTrue(set.size() == 0);
	}

	/**
	 * Class under test for <code>boolean contains(Object)</code>.
	 */
	public final void testContainsObject() {
		set.add(null);
        assertTrue(set.contains(null));
        
        set.add("Hi");
        assertTrue(set.contains("Hi"));
        assertTrue(set.contains(null));
	}

	/**
	 * Class under test for <code>Object get(Object)</code>.
	 */
	public final void testGet() {
		set.add(null);
        assertNull(set.get(null));
        
        set.add("Hi");
        assertEquals("Hi", set.get("Hi"));
        
        set.remove("Hi");
        try {
            set.get("Hi");
            fail("Element was not removed.");
        } catch (final NoSuchElementException _e) {
            
        }
	}

	/**
	 * Class under test for <code>boolean remove(Object)</code>.
	 */
	public final void testRemoveObject() {
		set.add(null);
        set.add("Hi");
        assertTrue(set.size() == 2);
        
        set.remove(null);
        assertFalse(set.contains(null));
        
        set.remove("Hi");
        assertFalse(set.contains("Hi"));
        
        assertTrue(set.isEmpty());
	}

	/**
	 * Class under test for <code>int size()</code>.
	 */
	public final void testSize() {
		set.add(null);
		assertTrue(set.size() == 1);

		set.add("Hi");
		assertTrue(set.size() == 2);

		set.addAll(Collections.EMPTY_SET);
		assertTrue(set.size() == 2);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		set = new RetrievableSet();
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		set = null;
	}
}

// End of File
