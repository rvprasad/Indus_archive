
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
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.custommonkey.xmlunit.XMLTestCase;

import org.xml.sax.SAXException;

import soot.G;
import soot.SootMethod;


/**
 * This provides junit-style regression test support to test dependency.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DependencyTest
  extends XMLTestCase {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DependencyTest.class);

	/**
	 * This provides the information for the test.  This drives the analyses and this object tests the output.
	 */
	private DependencyXMLizer xmlizer;

	/**
	 * This is the directory in which the regression test inputs are stored.
	 */
	private String xmlInDir;

	/**
	 * Creates a new DependencyTest object.
	 *
	 * @param dt is the xmlizer used in this test case.
	 * @param xmlInputDir is the directory in which the base test data of regression test exists.
	 *
	 * @pre dt != null and xmlInputDir != null
	 */
	public DependencyTest(final DependencyXMLizer dt, final String xmlInputDir) {
		xmlizer = dt;
		xmlInDir = xmlInputDir;
	}

	/**
	 * Provides the suite of tests in junit-style.
	 *
	 * @return the suite of tests.
	 *
	 * @throws RuntimeException when <code>indus.dependencytest.properties.file</code> property is unspecified.
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.dependency");
		final String _propFileName = System.getProperty("indus.dependencytest.properties.file");

		if (_propFileName == null) {
			throw new RuntimeException("Please provide a property file like DependencyTest.properties via"
				+ "-Dindus.dependencytest.properties.file");
		}
		setupTests(_propFileName, _suite);
		return _suite;
	}

	/**
	 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
	 */
	public void testDA() {
		final List _das = xmlizer.getDAs();
		final String _xmlOutDir = xmlizer.getXmlOutDir();

		for (final Iterator _i = xmlizer.getRootMethods().iterator(); _i.hasNext();) {
			final SootMethod _root = (SootMethod) _i.next();
			final String _rootName = _root.getSignature();

			for (final Iterator _j = _das.iterator(); _j.hasNext();) {
				final DependencyAnalysis _da = (DependencyAnalysis) _j.next();

				try {
					final Reader _current =
						new FileReader(new File(_xmlOutDir + File.separator + _da.getId() + "_" + _das.indexOf(_da) + "_"
								+ _rootName.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml"));
					final Reader _previous =
						new FileReader(new File(xmlInDir + File.separator + _da.getId() + "_" + _das.indexOf(_da) + "_"
								+ _rootName.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml"));
					assertXMLEqual(_previous, _current);
				} catch (IOException _e) {
					LOGGER.error("Failed to write the xml file based on " + _da.getClass() + " for system rooted at method "
						+ _rootName, _e);
				} catch (SAXException _e) {
					LOGGER.error("Exception while parsing XML", _e);
				} catch (ParserConfigurationException _e) {
					LOGGER.error("XML parser configuration related exception", _e);
				}
			}
		}
	}

	/**
	 * Initializes and drives the fixture.
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		xmlizer.initialize();
		xmlizer.execute();
	}

	/**
	 * Resets the underlying Soot framework and associated fixture.
	 *
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void teardown()
	  throws Exception {
		G.reset();
		xmlizer.reset();
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
   Revision 1.9  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.8  2003/11/17 16:58:15  venku
   - populateDAs() needs to be called from outside the constructor.
   - filterClasses() was called in CGBasedXMLizingController instead of filterMethods. FIXED.
   Revision 1.7  2003/11/17 03:22:59  venku
   - added junit test support for Slicing.
   - refactored code in test for dependency to make it more
     simple.
   Revision 1.6  2003/11/16 19:01:33  venku
   - documentation.
   Revision 1.5  2003/11/16 18:41:18  venku
   - renamed UniqueIDGenerator to UniqueJimpleIDGenerator.
   Revision 1.4  2003/11/12 10:45:36  venku
   - soot class path can be set in SootBasedDriver.
   - dependency tests are xmlunit based.
   Revision 1.3  2003/11/12 05:18:54  venku
   - moved xmlizing classes to a different class.
   Revision 1.2  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizer and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
   Revision 1.1  2003/11/11 10:11:27  venku
   - in the process of making XMLization a user
     application and at the same time a tester application.
 */
