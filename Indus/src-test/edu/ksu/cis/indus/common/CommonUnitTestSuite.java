
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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
import edu.ksu.cis.indus.common.collections.CollectionsUtilitiesTest;
import edu.ksu.cis.indus.common.collections.FilteredCollectionTestCase;
import edu.ksu.cis.indus.common.collections.FilteredListIteratorTestCase;
import edu.ksu.cis.indus.common.collections.FilteredListTestCase;
import edu.ksu.cis.indus.common.collections.FilteredMapTest;

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
		_suite.addTestSuite(FilteredListTestCase.class);
		_suite.addTestSuite(FilteredListIteratorTestCase.class);
		//$JUnit-END$
		TestHelper.appendSuiteNameToTestsIn(_suite, true);
		_suite.setName(CommonUnitTestSuite.class.getName());
		return _suite;
	}
}

// End of File
