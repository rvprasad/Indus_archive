
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

package edu.ksu.cis.indus.staticanalyses.flow;

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
 * This is the test suite used to run FA related tests using JUnit's swing interface to the runner.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FARegressionTestSuite
  extends TestCase {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FARegressionTestSuite.class);

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
		final TestRunner _runner = new TestRunner();
		_runner.setLoading(false);
		_runner.startTest(suite());
		_runner.runSuite();
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
            throw new RuntimeException("Please provide a property file like FATest.properties via "
                + "-D" + FA_TEST_PROPERTIES_FILE);
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
	protected static void setupTests(final String propFileName, final TestSuite suite) {
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

				FATestSetup _test;

				try {
					final TestSuite _temp = new TestSuite();
					_temp.setName(_config);
                    _temp.addTestSuite(FATest.class);
					_test = new FATestSetup(_temp, _classNames, _classpath);
					_test.setXMLOutputDir(_xmlOutputDir);
					_test.setXMLInputDir(_xmlInputDir);
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

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/02/08 04:53:11  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.
   Revision 1.3  2004/02/08 01:14:33  venku
   - added clover source directives.
   - renamed DependencyTestSuite to DependencyArgTestSuite.
   Revision 1.2  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added DependencyArgTestSuite.
   Revision 1.1  2004/01/03 19:52:54  venku
   - renamed CallGraphInfoTest to CallGraphTest
   - all tests of a kind have to be exposed via a suite like
     FATestSuite or OFAProcessorArgTestSuite.  This is to enable
     automated testing.
   - all properties should start with indus and not edu.ksu.cis.indus...
 */
