
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

import soot.G;
import soot.SootMethod;

import javax.xml.parsers.ParserConfigurationException;

import edu.ksu.cis.indus.staticanalyses.dependency.xmlizer.DependencyXMLizer;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.custommonkey.xmlunit.XMLTestCase;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * This provides junit-style regression test support to test dependency.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DependencyTest
  extends DependencyXMLizer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(DependencyTest.class);

	/**
	 * Creates a new DependencyTest object.
	 */
	private DependencyTest() {
		super(true);
	}

	/**
	 * This is the junit test case that tests dependency information.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	static class TestEntity
	  extends XMLTestCase {
		/**
		 * The logger used by instances of this class to log messages.
		 */
		private static final Log LOCAL_LOGGER = LogFactory.getLog(TestEntity.class);

		/**
		 * This provides the information for the test.  This drives the analyses and this object tests the output.
		 */
		private DependencyTest xmlizer;

		/**
		 * This is the directory in which the regression test inputs are stored.
		 */
		private String xmlInDir;

		/**
		 * Creates a new TestEntity object.
		 *
		 * @param dt may be called as the fixture which creates the test data for regression testing.
		 * @param xmlInputDir is the directory in which the base test data of regression test exists.
		 *
		 * @pre dt != null and xmlInputDir != null
		 */
		public TestEntity(final DependencyTest dt, final String xmlInputDir) {
			xmlizer = dt;
			xmlInDir = xmlInputDir;
		}

		/**
		 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
		 */
		public void testDA() {
			List das = xmlizer.getDAs();
			String xmlOutDir = xmlizer.getXmlOutputDir();

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
						LOCAL_LOGGER.error("Failed to write the xml file based on " + da.getClass()
							+ " for system rooted at " + root, e);
					} catch (SAXException e) {
						LOCAL_LOGGER.error("Exception while parsing XML", e);
					} catch (ParserConfigurationException e) {
						LOCAL_LOGGER.error("XML parser configuration related exception", e);
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
	 * Retrieves the dependency analyses available in the fixture.
	 *
	 * @return a collection of analyses.
	 *
	 * @post result != null and resutl.oclIsKindOf(Collection(DependencyAnalysis))
	 */
	private List getDAs() {
		return das;
	}

	/**
	 * Retrieves the methods in the fixture that act as entry point to the system.
	 *
	 * @return a collection of methods.
	 *
	 * @post result != null and resutl.oclIsKindOf(Collection(SootMethod))
	 */
	private Collection getRootMethods() {
		return rootMethods;
	}

	/**
	 * Retrieves the directory into which xml output should be written into.
	 *
	 * @return the directory for xml output.
	 *
	 * @post result != null
	 */
	private String getXmlOutputDir() {
		return xmlOutDir;
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

				TestEntity test;

				try {
					DependencyTest dt = new DependencyTest();
					test = new TestEntity(dt, xmlInputDir);
					dt.setClassNames(temp);
					dt.setXMLOutputDir(xmlOutputDir);
					dt.setGenerator(generator);
					dt.setProperties(PROPERTIES);
					dt.populateDAs();

					if (classpath != null) {
						dt.addToSootClassPath(classpath);
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
