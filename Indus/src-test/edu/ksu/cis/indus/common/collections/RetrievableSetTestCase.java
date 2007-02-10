
/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.collections;

import java.util.Collections;
import java.util.NoSuchElementException;

import junit.framework.TestCase;


/**
 * This class tests <code>RetrievableSet</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class RetrievableSetTestCase
  extends TestCase {
	/** 
	 * The instance to be tested.
	 */
	private RetrievableSet set;

	/**
	 * Class under test for <code>boolean add(Object)</code>.
	 */
	public final void testAddObject() {
		set.add(null);
		assertTrue(set.contains(null));
		final String _hiString = "Hi";
        assertFalse(set.contains(_hiString));

		set.add(_hiString);
		assertTrue(set.contains(null));
		assertTrue(set.contains(_hiString));

        final String _string = new String("Hi");
		set.add(_string);
		assertTrue(set.contains(null));
		assertTrue(set.contains(_string));
		assertTrue(set.size() == 2);
        assertSame(_hiString, set.get(_string));
        assertNotSame(_string, set.get(_string));
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
        
        final String _hiString = "Hi";
        set.add(_hiString);
        final String _string = new String("Hi");
        assertEquals(_hiString, set.get(_string));
        assertSame(_hiString, set.get(_string));
        assertNotSame(_hiString, _string);
        
        set.remove(_hiString);
        try {
            set.get(_hiString);
            fail("Element was not removed.");
        } catch (final NoSuchElementException _e) {
            ;
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
