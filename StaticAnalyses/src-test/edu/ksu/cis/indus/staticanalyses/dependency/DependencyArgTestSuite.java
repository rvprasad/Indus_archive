
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.staticanalyses.dependency.xmlizer.DependencyXMLizer;

import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

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
 * This is the test suite used to run Dependency related tests using JUnit's swing interface to the runner.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DependencyArgTestSuite
  extends TestCase {
    /**
     * This is the property via which the dependence test accepts input.  Refer to DependenceTest.properties for format.
     */
    public static final String DEPENDENCE_TEST_PROPERTIES_FILE =
        "indus.staticanalyses.dependency.DependencyTest.properties.file";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DependencyArgTestSuite.class);

	//  /CLOVER:OFF

	/**
	 * This is the entry point via command-line.
	 *
	 * @param args are a list of space separated names of classes to be analyzed.
	 *
	 * @pre args != null
	 */
	public static void main(final String[] args) {
		final StringBuffer _sb = new StringBuffer();

		for (int _i = args.length - 1; _i >= 0; _i--) {
			_sb.append(args[_i] + " ");
		}

		System.setProperty(DEPENDENCE_TEST_PROPERTIES_FILE, _sb.toString());

		final TestRunner _runner = new TestRunner();
		_runner.setLoading(false);
		_runner.startTest(suite());
		_runner.runSuite();
	}

	///CLOVER:ON

	/**
	 * Provides the suite of tests in junit-style.  This sets up the tests based on the file specified via
	 * <code>indus.staticanalyses.dependency.DependencyTest.properties.file</code> system property.  Refer to
	 * <code>edu.ksu.cis.indus.staticanalyses.dependency.DependencyTest.properties</code> for the format of the file.
	 *
	 * @return the suite of tests.
	 *
	 * @throws RuntimeException when <code>indus.dependencytest.properties.file</code> property is unspecified.
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.dependency");
		final String _propFileName = System.getProperty(DEPENDENCE_TEST_PROPERTIES_FILE);

		if (_propFileName == null) {
			throw new RuntimeException("Please provide a property file like DependencyTest.properties via"
				+ "-Dindus.staticanalyses.dependency.DependencyTest.properties.file");
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
		final IJimpleIDGenerator _generator = new UniqueJimpleIDGenerator();

		try {
			_props.load(new FileInputStream(new File(propFileName)));

			final String[] _configs = _props.getProperty("configs").split(" ");

			for (int _i = 0; _i < _configs.length; _i++) {
				final String _config = _configs[_i];
				final String[] _temp = _props.getProperty(_config + ".classNames").split(" ");
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

				DependencyTest _test;

				try {
					_test = new DependencyTest(new DependencyXMLizer(true), _xmlInputDir);
					_test.xmlizer.setClassNames(_temp);
					_test.xmlizer.setXMLOutputDir(_xmlOutputDir);
					_test.xmlizer.setGenerator(_generator);
					_test.xmlizer.populateDAs();

					if (_classpath != null) {
						_test.xmlizer.addToSootClassPath(_classpath);
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

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/02/08 01:48:54  venku
   - documentation and coding convention.

   Revision 1.1  2004/02/08 01:14:33  venku
   - added clover source directives.
   - renamed DependencyTestSuite to DependencyArgTestSuite.

   Revision 1.1  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added DependencyArgTestSuite.
 */
