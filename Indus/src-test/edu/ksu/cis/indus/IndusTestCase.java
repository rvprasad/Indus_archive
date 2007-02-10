
/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
