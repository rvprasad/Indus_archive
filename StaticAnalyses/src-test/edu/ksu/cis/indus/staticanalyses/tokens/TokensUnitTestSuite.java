
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import edu.ksu.cis.indus.TestHelper;

import junit.extensions.TestSetup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

import soot.Scene;


/**
 * This tests classes in tokens package in Indus module.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TokensUnitTestSuite
  extends TestCase {
	///CLOVER:OFF

	/**
	 * Creates a new TokensUnitTestSuite object.
	 */
	private TokensUnitTestSuite() {
	}

	/**
	 * Executes the test case.
	 *
	 * @param s is ignored.
	 */
	public static void main(final String[] s) {
		final String[] _suiteName = { TokensUnitTestSuite.class.getName() };
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
		final TestSuite _temp = new TestSuite();
		_temp.addTestSuite(BitSetTokenManagerTest.class);
		_temp.addTestSuite(CollectionTokenManagerTest.class);
		_temp.addTestSuite(IntegerTokenManagerTest.class);

		final TestSetup _setup =
			new TestSetup(_temp) {
				public void setUp() {
					final Scene _sc = Scene.v();
					_sc.loadClassAndSupport("java.lang.String");
				}
			};
		_suite.addTest(_setup);

		//$JUnit-END$
		TestHelper.appendSuiteNameToTestsIn(_suite, true);
		_suite.setName(TokensUnitTestSuite.class.getName());
		return _suite;
	}
}

// End of File
