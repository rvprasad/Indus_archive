
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

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/04/20 06:53:17  venku
   - documentation.

   Revision 1.1  2004/04/18 00:17:58  venku
   - added testcases for token manager logic.
 */
