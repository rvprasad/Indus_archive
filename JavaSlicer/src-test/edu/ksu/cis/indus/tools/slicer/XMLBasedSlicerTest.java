
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.AbstractXMLBasedTest;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Environment;

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
