
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

package edu.ksu.cis.indus;

import junit.framework.Test;
import junit.framework.TestCase;


/**
 * This extends JUnit TestCase to differentiate between test case instance name and the test method name.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class IndusTestCase
  extends TestCase
  implements Test {
	/** 
	 * The name of the method being run.
	 */
	private String testMethodName = "";

	/** 
	 * The name of the test case instance.
	 */
	private String testName = "";

	/**
	 * @see junit.framework.TestCase#setName(java.lang.String)
	 */
	public void setName(final String name) {
		testMethodName = name;
	}

	/**
	 * @see junit.framework.TestCase#getName()
	 */
	public String getName() {
		final String _result;

		if (!testName.equals("")) {
			_result = testName;
		} else {
			_result = testMethodName;
		}
		return _result;
	}

	/**
	 * Returns the name of the method being tested.
	 *
	 * @return the name of the method being tested.
	 */
	public String getTestMethodName() {
		return testMethodName;
	}

	/**
	 * Sets the name of the test instance.
	 *
	 * @param name of the test instance.
	 *
	 * @pre name != null
	 */
	public void setTestName(final String name) {
		testName = name;
		super.setName(name);
	}

	/**
	 * @see junit.framework.TestCase#runTest()
	 */
	protected void runTest()
	  throws Throwable {
		super.setName(testMethodName);
		super.runTest();
		super.setName(testName);
	}
}

// End of File
