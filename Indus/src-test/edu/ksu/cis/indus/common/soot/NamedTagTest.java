
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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.IndusTestCase;


/**
 * This class tests <code>NamedTag</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class NamedTagTest
  extends IndusTestCase {
	/**
	 * The tag.
	 */
	private NamedTag tag;

	/**
	 * Tests <code>equals</code>.
	 */
	public final void testEqualsObject() {
		assertTrue(tag.equals(new NamedTag("Name")));
		assertFalse(tag.equals(new Object()));
	}

	/**
	 * Tests <code>getName</code>.
	 */
	public final void testGetName() {
		assertTrue(tag.getName().equals("Name"));
	}

	/**
	 * Tests <code>getValue</code>.
	 */
	public final void testGetValue() {
		assertNotNull(tag.getValue());
	}

	/**
	 * Tests <code>hashCode</code>.
	 */
	public final void testHashCode() {
		final Object _temp = new NamedTag("Name");
		assertTrue(tag.hashCode() == _temp.hashCode());
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		tag = new NamedTag("Name");
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		tag = null;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/02/09 00:28:33  venku
   - added a new class, IndusTestCase, that extends TestCase
     to differentiate between the test method name and the
     test instance name.
   - all test cases in indus extends IndusTestCase.
   - added a new method TestHelper to append container's name
     to the test cases.

   Revision 1.1  2004/01/28 22:45:07  venku
   - added new test cases for testing classes in soot package.

 */
