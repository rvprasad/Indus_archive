
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
        final String[] _suiteName = { "edu.ksu.cis.indus.common.soot.SootUnitTestSuite" };
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
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.common.soot");

		//$JUnit-BEGIN$
		_suite.addTestSuite(UtilTest.class);
		_suite.addTestSuite(UnitGraphFactoryTest.class);
		_suite.addTestSuite(NamedTagTest.class);
		//$JUnit-END$
		return _suite;
	}
}

/*
   ChangeLog:
   $Log$
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
