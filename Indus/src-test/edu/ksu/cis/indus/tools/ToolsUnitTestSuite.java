
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

package edu.ksu.cis.indus.tools;

import edu.ksu.cis.indus.TestHelper;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;


/**
 * test suite to tests classes in <code>tools</code> package.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ToolsUnitTestSuite {
	///CLOVER:OFF

	/**
	 * Creates a new DataStructuresUnitTestSuite object.
	 */
	private ToolsUnitTestSuite() {
	}

	/**
	 * Executes the test case.
	 *
	 * @param s is ignored.
	 */
	public static void main(final String[] s) {
		final String[] _suiteName = { ToolsUnitTestSuite.class.getName() };
		TestRunner.main(_suiteName);
	}

	///CLOVER:ON

	/**
	 * Creates the test suite.
	 *
	 * @return the created test suite.
	 *
	 * @post result != null
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite();

		//$JUnit-BEGIN$
		_suite.addTestSuite(AbstractToolTest.class);
		//$JUnit-END$
		TestHelper.appendSuiteNameToTestsIn(_suite, true);
		_suite.setName(ToolsUnitTestSuite.class.getName());
		return _suite;
	}
}

// End of File
