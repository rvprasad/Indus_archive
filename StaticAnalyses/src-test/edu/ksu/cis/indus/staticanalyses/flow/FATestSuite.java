
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

package edu.ksu.cis.indus.staticanalyses.flow;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;


/**
 * This is the test suite used to run FA related tests using JUnit's swing interface to the runner.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FATestSuite
  extends TestCase {
	/**
	 * This is the entry point via command-line.
	 *
	 * @param args are a list of space separated names of classes to be analyzed.
	 *
	 * @pre args != null
	 */
	public static void main(final String[] args) {
		final StringBuffer _sb = new StringBuffer();

		for (int _i = args.length - 1; _i >= 0; _i--) {
			_sb.append(args[_i] + " ");
		}

		System.setProperty(FATestSetup.CLASSES_PROPERTY, _sb.toString());

		final TestRunner _runner = new TestRunner();
		_runner.setLoading(false);
		_runner.startTest(suite());
		_runner.runSuite();
	}

	/**
	 * Retrieves the test suite of the tests in this class.
	 *
	 * @return a test suite.
	 *
	 * @post result != null
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.FATest");

		//$JUnit-BEGIN$
		_suite.addTestSuite(FATest.class);
		//$JUnit-END$
		return new FATestSetup(_suite);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/01/03 19:52:54  venku
   - renamed CallGraphInfoTest to CallGraphTest
   - all tests of a kind have to be exposed via a suite like
     FATestSuite or OFAProcessorArgTestSuite.  This is to enable
     automated testing.
   - all properties should start with indus and not edu.ksu.cis.indus...
 */
