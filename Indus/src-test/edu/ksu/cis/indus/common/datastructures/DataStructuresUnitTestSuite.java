
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

import junit.textui.TestRunner;


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
		final String[] _suiteName = { DataStructuresUnitTestSuite.class.getName() };
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
		_suite.addTestSuite(FastUnionFindElementTest.class);
		_suite.addTestSuite(LIFOWorkBagTest.class);
		_suite.addTestSuite(FIFOWorkBagTest.class);
		_suite.addTestSuite(PoolAwareWorkBagTest.class);
		_suite.addTestSuite(PairTest.class);
		_suite.addTestSuite(TripleTest.class);
		_suite.addTestSuite(QuadrapleTest.class);
		_suite.addTestSuite(MarkerTest.class);
		_suite.addTestSuite(HistoryAwareFIFOWorkBagTest.class);
		_suite.addTestSuite(HistoryAwareLIFOWorkBagTest.class);
		//$JUnit-END$
		TestHelper.appendSuiteNameToTestsIn(_suite, true);
		_suite.setName(DataStructuresUnitTestSuite.class.getName());
		return _suite;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/04/01 22:33:45  venku
   - test suite name was incorrect.

   Revision 1.6  2004/03/29 01:55:15  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.

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
   Revision 1.1  2004/02/08 01:04:13  venku
   - renamed TestSuite classes to NoArgTestSuite classes.
   Revision 1.3  2004/01/28 00:18:45  venku
   - added unit tests for classes in data structures package.
   Revision 1.2  2004/01/13 23:26:32  venku
   - documentation.
   Revision 1.1  2004/01/06 15:06:23  venku
   - started to add test case for data structures.
 */
