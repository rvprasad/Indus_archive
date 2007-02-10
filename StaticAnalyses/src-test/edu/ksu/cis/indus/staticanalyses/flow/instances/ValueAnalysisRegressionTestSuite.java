
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

package edu.ksu.cis.indus.staticanalyses.flow.instances;

import edu.ksu.cis.indus.ErringTestCase;
import edu.ksu.cis.indus.IXMLBasedTest;
import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallGraphTest;
import edu.ksu.cis.indus.staticanalyses.callgraphs.XMLBasedCallGraphTest;
import edu.ksu.cis.indus.staticanalyses.flow.FATest;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.XMLBasedOFATest;

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
 * This is the test suite used to run FA based value analysis related tests using JUnit's text interface to the runner.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ValueAnalysisRegressionTestSuite
  extends TestCase {
	/** 
	 * This is the property via which the ofa test accepts input.  Refer to ValueAnalysisTest.properties for format.
	 */
	public static final String VALUE_ANALYSIS_TEST_PROPERTIES_FILE =
		"indus.staticanalyses.flow.instances.ValueAnalysisTest.properties.file";

	///CLOVER:OFF

	/**
	 * This is the entry point via command-line.
	 *
	 * @param args are ignored.
	 */
	public static void main(final String[] args) {
		final String[] _suiteName = { ValueAnalysisRegressionTestSuite.class.getName() };
		TestRunner.main(_suiteName);
	}

	///CLOVER:ON

	/**
	 * Provides the suite of tests in junit-style.  This sets up the tests based on the file specified via
	 * <code>VALUE_ANALYSIS_TEST_PROPERTIES_FILE</code> system property.  Refer to
	 * <code>edu.ksu.cis.indus.staticanalyses.flow.instances.ValueAnalysisTest.properties</code> for the format of the file.
	 *
	 * @return the suite of tests.
	 *
	 * @throws RuntimeException when <code>VALUE_ANALYSIS_TEST_PROPERTIES_FILE</code> property is unspecified.
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.instances");
		final String _propFileName = System.getProperty(VALUE_ANALYSIS_TEST_PROPERTIES_FILE);

		if (_propFileName == null) {
			throw new RuntimeException("Please provide a property file like ValueAnalysisTest.properties via " + "-D"
				+ VALUE_ANALYSIS_TEST_PROPERTIES_FILE);
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
			final IStmtGraphFactory _stmtGraphFactory = new CompleteStmtGraphFactory();

			for (int _i = 0; _i < _configs.length; _i++) {
				final String _config = _configs[_i];
				final String _classNames = _props.getProperty(_config + ".classNames");
				final String _xmlTestDir = _props.getProperty(_config + IXMLBasedTest.XML_TEST_DIR_PROP_SUFFIX);
				final String _xmlControlDir = _props.getProperty(_config + IXMLBasedTest.XML_CONTROL_DIR_PROP_SUFFIX);
				final String _jimpleDumpDir = _props.getProperty(_config + ".jimpleXMLDumpDir");
				final String _classpath = _props.getProperty(_config + ".classpath");
				final String _str = TestHelper.checkXMLBasedTestExecutability(_config, _xmlTestDir, _xmlControlDir);

				try {
					final TestSuite _temp = new TestSuite(_config);

					if (_str.length() > 0) {
						_temp.addTest(new ErringTestCase(_str));
						TestHelper.appendSuiteNameToTestsIn(_temp, true);
						suite.addTest(_temp);
					} else {
						_temp.addTestSuite(XMLBasedCallGraphTest.class);
                        _temp.addTestSuite(OFABasedCallGraphTest.class);
						_temp.addTestSuite(XMLBasedOFATest.class);
						_temp.addTestSuite(FATest.class);
						TestHelper.appendSuiteNameToTestsIn(_temp, true);

						final ValueAnalysisTestSetup _test = new ValueAnalysisTestSetup(_temp, _classNames, _classpath);
						_test.setIdGenerator(new UniqueJimpleIDGenerator());
						_test.setJimpleXMLDumpLocation(_jimpleDumpDir);
						_test.setStmtGraphFactory(_stmtGraphFactory);
						_test.setXMLTestDir(_xmlTestDir);
						_test.setXMLControlDir(_xmlControlDir);
						suite.addTest(_test);
					}
				} catch (IllegalArgumentException _e) {
					;
				}
			}
		} catch (final IOException _e) {
			throw new IllegalArgumentException("Specified property file does not exist.");
		}
	}
}

// End of File
