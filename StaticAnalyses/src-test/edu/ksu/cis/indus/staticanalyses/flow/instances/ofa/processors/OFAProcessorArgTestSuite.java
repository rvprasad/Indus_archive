
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.staticanalyses.flow.FATest;
import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;


/**
 * This is the test suite used to run FA related processor tests using JUnit's swing interface to the runner.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class OFAProcessorArgTestSuite {
	///CLOVER:OFF

	/**
	 * This is the entry point via the command-line.
	 *
	 * @param args is the command line arguments.
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

	///CLOVER:OFF

	/**
	 * Retrieves the test suite that encapsulates the tests defined in this class.
	 *
	 * @return a test suite.
	 *
	 * @post result != null
	 */
	public static Test suite() {
		final TestSuite _suite =
			new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph");

		//$JUnit-BEGIN$
		_suite.addTestSuite(CallGraphTest.class);
		_suite.addTestSuite(FATest.class);
		//$JUnit-END$
		return new OFAProcessorTestSetup(_suite);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/02/08 01:14:33  venku
   - added clover source directives.
   - renamed DependencyTestSuite to DependencyArgTestSuite.

   Revision 1.1  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added DependencyArgTestSuite.
   Revision 1.1  2004/01/03 19:52:54  venku
   - renamed CallGraphInfoTest to CallGraphTest
   - all tests of a kind have to be exposed via a suite like
     FATestSuite or OFAProcessorArgTestSuite.  This is to enable
     automated testing.
   - all properties should start with indus and not edu.ksu.cis.indus...
 */
