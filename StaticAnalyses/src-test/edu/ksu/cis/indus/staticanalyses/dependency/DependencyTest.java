
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

import junit.framework.Test;
import junit.framework.TestSuite;

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
		TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.dependency");
		String propFileName = System.getProperty("indus.dependencytest.properties.file");

		if (propFileName == null) {
			throw new RuntimeException("Please provide a property file like DependencyTest.properties via"
				+ "-Dindus.dependencytest.properties.file");
		}
		setupTests(propFileName, suite);
		return suite;
	}

	/**
	 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
	 */
	public void testDA() {
		List das = xmlizer.getDAs();
		String xmlOutDir = xmlizer.getXmlOutDir();

		for (Iterator i = xmlizer.getRootMethods().iterator(); i.hasNext();) {
			SootMethod root = (SootMethod) i.next();
			String rootName = root.getSignature();

			for (Iterator iter = das.iterator(); iter.hasNext();) {
				DependencyAnalysis da = (DependencyAnalysis) iter.next();

				try {
					Reader current =
						new FileReader(new File(xmlOutDir + File.separator + da.getId() + "_" + das.indexOf(da) + "_"
								+ rootName.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml"));
					Reader previous =
						new FileReader(new File(xmlInDir + File.separator + da.getId() + "_" + das.indexOf(da) + "_"
								+ rootName.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml"));
					assertXMLEqual(previous, current);
				} catch (IOException e) {
					LOGGER.error("Failed to write the xml file based on " + da.getClass() + " for system rooted at method "
						+ rootName, e);
				} catch (SAXException e) {
					LOGGER.error("Exception while parsing XML", e);
				} catch (ParserConfigurationException e) {
					LOGGER.error("XML parser configuration related exception", e);
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
	private static final void setupTests(final String propFileName, final TestSuite suite) {
		Properties props = new Properties();
		IJimpleIDGenerator generator = new UniqueJimpleIDGenerator();

		try {
			props.load(new FileInputStream(new File(propFileName)));

			String[] configs = props.getProperty("configs").split(" ");

			for (int i = 0; i < configs.length; i++) {
				String config = configs[i];
				String[] temp = props.getProperty(config + ".classNames").split(" ");
				String xmlOutputDir = props.getProperty(config + ".xmlOutputDir");
				String xmlInputDir = props.getProperty(config + ".xmlInputDir");
				String classpath = props.getProperty(config + ".classpath");
				File f = new File(xmlInputDir);

				if (!f.exists() || !f.canRead()) {
					LOGGER.error("Input directory " + xmlInputDir + " does not exists. Bailing on " + config);
					continue;
				}
				f = new File(xmlOutputDir);

				if (!f.exists() || !f.canWrite()) {
					LOGGER.error("Output directory " + xmlInputDir + " does not exists. Bailing on " + config);
					continue;
				}

				DependencyTest test;

				try {
					test = new DependencyTest(new DependencyXMLizer(true), xmlInputDir);
					test.xmlizer.setClassNames(temp);
					test.xmlizer.setXMLOutputDir(xmlOutputDir);
					test.xmlizer.setGenerator(generator);
					test.xmlizer.populateDAs();

					if (classpath != null) {
						test.xmlizer.addToSootClassPath(classpath);
					}
				} catch (IllegalArgumentException e) {
					test = null;
				}

				if (test != null) {
					suite.addTest(test);
				}
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Specified property file does not exist.");
		}
	}
}

/*
   ChangeLog:
   $Log$
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
