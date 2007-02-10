
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

// End of File
