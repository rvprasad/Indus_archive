
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

import edu.ksu.cis.indus.TestHelper;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;


/**
 * This is the suite of tests to test classes in <code>edu.ksu.cis.indus.common.soot</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SootUnitTestSuite {
	///CLOVER:OFF

	/**
	 * Creates a new GraphUnitTestSuite object.
	 */
	private SootUnitTestSuite() {
	}

	/**
	 * Executes the test case.
	 *
	 * @param s is ignored.
	 */
	public static void main(final String[] s) {
		final TestRunner _runner = new TestRunner();
		final String[] _suiteName = { SootUnitTestSuite.class.getName() };
		_runner.setLoading(false);
		_runner.start(_suiteName);
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
		_suite.addTestSuite(UtilTest.class);
		_suite.addTestSuite(UnitGraphFactoryTest.class);
		_suite.addTestSuite(NamedTagTest.class);
		//$JUnit-END$
		TestHelper.appendSuiteNameToTestsIn(_suite, true);
		_suite.setName(SootUnitTestSuite.class.getName());
		return _suite;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/02/09 01:39:50  venku
   - changed test naming for report purposes.

   Revision 1.4  2004/02/09 00:39:50  venku
   - output formatting.
   - UnitTestSuites alter the name of the test instances
     via appendSuiteTestName().
   Revision 1.3  2004/02/08 20:58:58  venku
   - class loading during testing was fixed.
   Revision 1.2  2004/02/08 20:52:22  venku
   - changed the way unit test suites can be run as applications.
   - renamed GraphUnitestSuite to GraphUnitTestSuite.
   Revision 1.1  2004/02/08 03:34:30  venku
   - renamed NoArgTestSuite to UnitTestSuite
   Revision 1.1  2004/02/08 01:04:12  venku
   - renamed TestSuite classes to NoArgTestSuite classes.
   Revision 1.1  2004/01/28 22:45:07  venku
   - added new test cases for testing classes in soot package.
 */
