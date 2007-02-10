
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.ErringTestCase;
import edu.ksu.cis.indus.IXMLBasedTest;
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
 * This is the test suite used to run slicer related tests using JUnit's text interface to the runner.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerRegressionTestSuite
  extends TestCase {
	/** 
	 * The properties file containing the test configuration is specified via this property.
	 */
	private static final String SLICER_TEST_PROPERTIES_FILE = "indus.slicer.XMLBasedSlicerTest.properties.file";

	///CLOVER:OFF

	/**
	 * This is the entry point via command-line.
	 *
	 * @param args are ignored.
	 */
	public static void main(final String[] args) {
		final String[] _suiteName = { SlicerRegressionTestSuite.class.getName() };
		TestRunner.main(_suiteName);
	}

	///CLOVER:ON

	/**
	 * Provides the suite of tests in junit-style.
	 *
	 * @return the suite of tests.
	 *
	 * @throws RuntimeException when <code>indus.slicertest.properties.file</code> property is unspecified.
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.tools.slicer");
		final String _propFileName = System.getProperty(SLICER_TEST_PROPERTIES_FILE);

		if (_propFileName == null) {
			throw new RuntimeException("Please provide a property file like XMLBasedSlicerTest.properties via" + "-D"
				+ SLICER_TEST_PROPERTIES_FILE);
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
			final FileInputStream _propFile = new FileInputStream(new File(propFileName));
			_props.load(_propFile);
			_propFile.close();

			final String[] _configs = _props.getProperty("configs").split(" ");

			for (int _i = 0; _i < _configs.length; _i++) {
				final String _config = _configs[_i];
				_props.setProperty("name", _config);

				final String[] _temp = _props.getProperty(_config + ".classNames").split(" ");
				final String _xmlTestDir = _props.getProperty(_config + IXMLBasedTest.XML_TEST_DIR_PROP_SUFFIX);
				final String _xmlControlDir = _props.getProperty(_config + IXMLBasedTest.XML_CONTROL_DIR_PROP_SUFFIX);
				final String _classpath = _props.getProperty(_config + ".classpath");
				final String _jimpleXMLDumpDir = _props.getProperty(_config + ".jimpleXMLDumpDir");
				final String _str = TestHelper.checkXMLBasedTestExecutability(_config, _xmlTestDir, _xmlControlDir);
				Test _test;

				try {
					if (_str.length() > 0) {
						_test = new ErringTestCase(_str);
					} else {
						final TestSuite _suite = new TestSuite(_config);
						final SlicerTestSetup _sTestSetup =
							new SlicerTestSetup(_suite, _temp, _xmlTestDir, _xmlControlDir, _classpath);
						_sTestSetup.setJimpleXMLDumpLocation(_jimpleXMLDumpDir);
						_sTestSetup.setIdGenerator(new UniqueJimpleIDGenerator());

						final XMLBasedSlicerTest _xmlTest = new XMLBasedSlicerTest(_sTestSetup);
						_suite.addTest(_xmlTest);
						_test = _sTestSetup;
					}
				} catch (IllegalArgumentException _e) {
					_test = null;
				}

				if (_test != null) {
					suite.addTest(_test);
				}
			}
		} catch (IOException _e) {
			throw new IllegalArgumentException("Specified property file does not exist.");
		}
	}
}

// End of File
