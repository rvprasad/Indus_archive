
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;


/**
 * This is the test suite used to run FA related tests using JUnit's text interface to the runner.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FARegressionTestSuite
  extends TestCase {
	///CLOVER:OFF

	/** 
	 * The name of the property via which the names of the classes to be used to drive the test is specified.
	 */
	public static final String FA_TEST_PROPERTIES_FILE = "indus.staticanalyses.flow.FATest.properties.file";

	/**
	 * This is the entry point via command-line.
	 *
	 * @param args are ignored.
	 */
	public static void main(final String[] args) {
		final String[] _suiteName = { FARegressionTestSuite.class.getName() };
		TestRunner.main(_suiteName);
	}

	///CLOVER:ON

	/**
	 * Provides the suite of tests in junit-style.  This sets up the tests based on the file specified via
	 * <code>FA_TEST_PROPERTIES_FILE</code> system property.  Refer to
	 * <code>edu.ksu.cis.indus.staticanalyses.flow.FATest.properties</code> for the format of the file.
	 *
	 * @return the suite of tests.
	 *
	 * @throws RuntimeException when <code>FA_TEST_PROPERTIES_FILE</code> property is unspecified.
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.fa");
		final String _propFileName = System.getProperty(FA_TEST_PROPERTIES_FILE);

		if (_propFileName == null) {
			throw new RuntimeException("Please provide a property file like FATest.properties via " + "-D"
				+ FA_TEST_PROPERTIES_FILE);
		}
		setupTests(_propFileName, _suite);
		return _suite;
	}

	/**
	 * Sets up the test fixture.
	 *
	 * @param propFileName is the name of the file with the data to setup the test fixture.
	 * @param suite will contain new tests based on the fixture data (upon return).
	 *
	 * @throws IllegalArgumentException when the fixture data is invalid.
	 *
	 * @pre propFileName != null and suite != null
	 */
	private static void setupTests(final String propFileName, final TestSuite suite) {
		final Properties _props = new Properties();

		try {
			_props.load(new FileInputStream(new File(propFileName)));

			final String[] _configs = _props.getProperty("configs").split(" ");

			for (int _i = 0; _i < _configs.length; _i++) {
				final String _config = _configs[_i];
				final String _classNames = _props.getProperty(_config + ".classNames");
				final String _classpath = _props.getProperty(_config + ".classpath");
				final String _jimpleXMLDumpDir = _props.getProperty(_config + ".jimpleXMLDumpDir");

				try {
					final TestSuite _temp = new TestSuite(_config);
					_temp.addTestSuite(FATest.class);
					TestHelper.appendSuiteNameToTestsIn(_temp, true);

					final FATestSetup _test = new FATestSetup(_temp, _classNames, _classpath);
					_test.setIdGenerator(new UniqueJimpleIDGenerator());
					_test.setJimpleXMLDumpLocation(_jimpleXMLDumpDir);
					suite.addTest(_test);
				} catch (IllegalArgumentException _e) {
					;
				}
			}
		} catch (IOException _e) {
			throw new IllegalArgumentException("Specified property file does not exist.");
		}
	}
}

// End of File
