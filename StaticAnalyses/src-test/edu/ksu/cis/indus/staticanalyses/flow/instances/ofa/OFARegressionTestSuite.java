
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.staticanalyses.flow.FATest;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraphTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is the test suite used to run OFA related tests using JUnit's swing interface to the runner.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class OFARegressionTestSuite
  extends TestCase {
	/**
	 * This is the property via which the ofa test accepts input.  Refer to OFATest.properties for format.
	 */
	public static final String OFA_TEST_PROPERTIES_FILE = "indus.staticanalyses.flow.instances.ofa.OFATest.properties.file";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(OFARegressionTestSuite.class);

	///CLOVER:OFF

	/**
	 * This is the entry point via command-line.
	 *
	 * @param args are ignored.
	 */
	public static void main(final String[] args) {
		final TestRunner _runner = new TestRunner();
		final String[] _suiteName = { "edu.ksu.cis.indus.staticanalysis.flow.instances.ofa.OFARegressionTestSuite" };
		_runner.setLoading(false);
		_runner.start(_suiteName);
	}

	///CLOVER:ON

	/**
	 * Provides the suite of tests in junit-style.  This sets up the tests based on the file specified via
	 * <code>OFA_TEST_PROPERTIES_FILE</code> system property.  Refer to
	 * <code>edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFATest.properties</code> for the format of the file.
	 *
	 * @return the suite of tests.
	 *
	 * @throws RuntimeException when <code>OFA_TEST_PROPERTIES_FILE</code> property is unspecified.
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.ofa");
		final String _propFileName = System.getProperty(OFA_TEST_PROPERTIES_FILE);

		if (_propFileName == null) {
			throw new RuntimeException("Please provide a property file like OFATest.properties via " + "-D"
				+ OFA_TEST_PROPERTIES_FILE);
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
				final String _xmlOutputDir = _props.getProperty(_config + ".xmlOutputDir");
				final String _xmlInputDir = _props.getProperty(_config + ".xmlInputDir");
				final String _classpath = _props.getProperty(_config + ".classpath");
				File _f = new File(_xmlInputDir);

				if (!_f.exists() || !_f.canRead()) {
					LOGGER.error("Input directory " + _xmlInputDir + " does not exists. Bailing on " + _config);
					continue;
				}
				_f = new File(_xmlOutputDir);

				if (!_f.exists() || !_f.canWrite()) {
					LOGGER.error("Output directory " + _xmlInputDir + " does not exists. Bailing on " + _config);
					continue;
				}

				try {
					final TestSuite _temp = new TestSuite(_config);
					_temp.addTestSuite(CallGraphTest.class);
					_temp.addTestSuite(FATest.class);
					TestHelper.appendSuiteNameToTestsIn(_temp, true);

					OFATestSetup _test = new OFATestSetup(_temp, _classNames, _classpath);
					_test.setXMLOutputDir(_xmlOutputDir);
					_test.setXMLInputDir(_xmlInputDir);
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

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/02/08 21:31:41  venku
   - test refactoring to enable same test case to be used as
     unit test case and regression test case
   Revision 1.2  2004/02/08 19:17:19  venku
   - test refactoring for regression testing.
   Revision 1.1  2004/02/08 04:53:10  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.
   Revision 1.2  2004/02/08 01:48:54  venku
   - documentation and coding convention.
   Revision 1.1  2004/02/08 01:14:33  venku
   - added clover source directives.
   - renamed DependencyTestSuite to OFAProcessorArgTestSuite.
   Revision 1.1  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added OFAProcessorArgTestSuite.
 */
