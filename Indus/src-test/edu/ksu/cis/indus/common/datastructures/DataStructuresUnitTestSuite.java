
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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.TestHelper;
import junit.framework.Test;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;


/**
 * The test suite to tests classes in datastructures package.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DataStructuresUnitTestSuite {
	///CLOVER:OFF

	/**
	 * Creates a new DataStructuresUnitTestSuite object.
	 */
	private DataStructuresUnitTestSuite() {
	}

	/**
	 * Executes the test case.
	 *
	 * @param s is ignored.
	 */
	public static void main(final String[] s) {
        final TestRunner _runner = new TestRunner();
        final String[] _suiteName = { "edu.ksu.cis.indus.common.datastructures.DataStructuresUnitTestSuite" };
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
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.common.datastructures");

		//$JUnit-BEGIN$
		_suite.addTestSuite(FastUnionFindElementTest.class);
		_suite.addTestSuite(LIFOWorkBagTest.class);
		_suite.addTestSuite(FIFOWorkBagTest.class);
		_suite.addTestSuite(PoolAwareWorkBagTest.class);
		_suite.addTestSuite(PairTest.class);
		_suite.addTestSuite(TripleTest.class);
		_suite.addTestSuite(QuadrapleTest.class);
        _suite.addTestSuite(MarkerTest.class);
		//$JUnit-END$
        TestHelper.appendSuiteNameToTestsIn(_suite, true);
		return _suite;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/02/08 20:58:58  venku
   - class loading during testing was fixed.

   Revision 1.2  2004/02/08 20:52:22  venku
   - changed the way unit test suites can be run as applications.
   - renamed GraphUnitestSuite to GraphUnitTestSuite.

   Revision 1.1  2004/02/08 03:34:30  venku
   - renamed NoArgTestSuite to UnitTestSuite

   Revision 1.1  2004/02/08 01:04:13  venku
   - renamed TestSuite classes to NoArgTestSuite classes.

   Revision 1.3  2004/01/28 00:18:45  venku
   - added unit tests for classes in data structures package.

   Revision 1.2  2004/01/13 23:26:32  venku
   - documentation.
   Revision 1.1  2004/01/06 15:06:23  venku
   - started to add test case for data structures.
 */
