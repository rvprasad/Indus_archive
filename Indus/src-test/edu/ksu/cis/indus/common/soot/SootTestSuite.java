
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
public final class SootTestSuite {
	///CLOVER:OFF

	/**
	 * Creates a new GraphTestSuite object.
	 */
	private SootTestSuite() {
	}

	/**
	 * Executes the test case.
	 *
	 * @param s is ignored.
	 */
	public static void main(final String[] s) {
		final TestRunner _runner = new TestRunner();
		_runner.setLoading(false);
		_runner.start(new String[0]);
		_runner.startTest(suite());
		_runner.runSuite();
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
 */
