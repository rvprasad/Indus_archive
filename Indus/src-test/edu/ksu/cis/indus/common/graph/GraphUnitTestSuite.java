
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

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.TestHelper;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;


/**
 * This is the test suite for <code>DirectedGraph</code> and <code>SimpleNodeGraph</code>.  Any new test cases should add to
 * this suite.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class GraphUnitTestSuite {
	///CLOVER:OFF

	/**
	 * Creates a new GraphUnitTestSuite object.
	 */
	private GraphUnitTestSuite() {
	}

	/**
	 * Executes the test case.
	 *
	 * @param s is ignored.
	 */
	public static void main(final String[] s) {
		final String[] _suiteName = { GraphUnitTestSuite.class.getName() };
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
		_suite.addTestSuite(SimpleNodeGraphTest.class);
		_suite.addTestSuite(SimpleNodeGraphNoCycleTest.class);
		_suite.addTestSuite(SimpleNodeGraphTest2.class);
		_suite.addTestSuite(SimpleNodeGraphTest3.class);
		_suite.addTestSuite(JikesBasedDirectedAndSimpleNodeGraphTest.class);
		_suite.addTestSuite(JavacBasedDirectedAndSimpleNodeGraphTest.class);
		//$JUnit-END$
		TestHelper.appendSuiteNameToTestsIn(_suite, true);
		_suite.setName(GraphUnitTestSuite.class.getName());
		return _suite;
	}
}

// End of File
