
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

import edu.ksu.cis.indus.IndusTestCase;


/**
 * This class tests <code>Marker</code> class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class MarkerTest
  extends IndusTestCase {
	/** 
	 * The marker object.
	 */
	private Marker marker;

	/** 
	 * The object in the marker.
	 */
	private Object object;

	/**
	 * Tests <code>getContent</code>.
	 */
	public final void testGetContent() {
		assertTrue(marker.getContent() == object);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		object = new Object();
		marker = new Marker(object);
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		marker = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/01/28 00:18:45  venku
   - added unit tests for classes in data structures package.

 */
