
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.AbstractXMLBasedTest;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.xmlizer.IXMLizer;


/**
 * This provides junit-style regression test support to test dependency.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class XMLBasedSlicerTest
  extends AbstractXMLBasedTest {
	/** 
	 * The test setup in which this test runs.
	 */
	private final SlicerTestSetup setup;

	/**
	 * Creates an instance of this class.
	 *
	 * @param testSetup in which this test case instance will executed.
	 *
	 * @pre testSetup != null
	 */
	public XMLBasedSlicerTest(final SlicerTestSetup testSetup) {
		setup = testSetup;
		setName("testXMLSimilarity");
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#getXMLizer()
	 */
	protected IXMLizer getXMLizer() {
		return setup.driver.getXMLizer();
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#localSetup()
	 */
	protected void localSetup()
	  throws Exception {
		super.localSetup();
		info.put(IEnvironment.ID, setup.driver.slicer.getSystem());
	}
}

// End of File
