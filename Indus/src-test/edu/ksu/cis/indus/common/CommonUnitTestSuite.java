
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

package edu.ksu.cis.indus.common;

import edu.ksu.cis.indus.TestHelper;

import junit.framework.Test;
import junit.framework.TestSuite;

import junit.textui.TestRunner;


/**
 * test suite to tests classes in <code>common</code> package.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CommonUnitTestSuite {
	///CLOVER:OFF

	/**
	 * Creates a new DataStructuresUnitTestSuite object.
	 */
	private CommonUnitTestSuite() {
	}

	/**
	 * Executes the test case.
	 *
	 * @param s is ignored.
	 */
	public static void main(final String[] s) {
		final String[] _suiteName = { CommonUnitTestSuite.class.getName() };
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
		_suite.addTestSuite(CollectionsUtilitiesTest.class);
		_suite.addTestSuite(FilteredMapTest.class);
		_suite.addTestSuite(FilteredCollectionTestCase.class);
		_suite.addTestSuite(FilteredSetTestCase.class);
		_suite.addTestSuite(FilteredListTestCase.class);
		_suite.addTestSuite(FilteredListIteratorTestCase.class);
		//$JUnit-END$
		TestHelper.appendSuiteNameToTestsIn(_suite, true);
		_suite.setName(CommonUnitTestSuite.class.getName());
		return _suite;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2004/05/21 22:11:48  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.

   Revision 1.7  2004/04/05 23:16:37  venku
   - textui.TestRunner cannot be run via start(). FIXED.

   Revision 1.6  2004/04/05 22:26:30  venku
   - used textui.TestRunner instead of swingui.TestRunner.

   Revision 1.5  2004/04/01 22:33:45  venku
   - test suite name was incorrect.

   Revision 1.4  2004/02/09 01:39:50  venku
   - changed test naming for report purposes.

   Revision 1.3  2004/02/09 00:39:50  venku
   - output formatting.
   - UnitTestSuites alter the name of the test instances
     via appendSuiteTestName().
   Revision 1.2  2004/02/08 20:52:22  venku
   - changed the way unit test suites can be run as applications.
   - renamed GraphUnitestSuite to GraphUnitTestSuite.
   Revision 1.1  2004/02/08 03:34:30  venku
   - renamed NoArgTestSuite to UnitTestSuite
   Revision 1.1  2004/02/08 01:04:13  venku
   - renamed TestSuite classes to NoArgTestSuite classes.
   Revision 1.3  2004/02/05 18:16:21  venku
   - coding convention.
   Revision 1.2  2004/01/28 23:03:09  venku
   - added clover source code directives.
   Revision 1.1  2004/01/28 22:55:23  venku
   - added test suites for classes in common package.
 */
