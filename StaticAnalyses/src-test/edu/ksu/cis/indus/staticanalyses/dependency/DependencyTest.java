
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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
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
	 * DOCUMENT ME!
	 *
	 * @author venku To change this generated comment go to  Window>Preferences>Java>Code Generation>Code Template
	 */
	static class TestEntity
	  extends XMLTestCase {
		/**
		 * The logger used by instances of this class to log messages.
		 */
		private static final Log LOCAL_LOGGER = LogFactory.getLog(TestEntity.class);

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private DependencyTest xmlizer;

		/**
		 * <p>
		 * DOCUMENT ME!
		 * </p>
		 */
		private String xmlInDir;

		/**
		 * Creates a new TestEntity object.
		 *
		 * @param dt DOCUMENT ME!
		 * @param xmlInputDir DOCUMENT ME!
		 */
		public TestEntity(final DependencyTest dt, final String xmlInputDir) {
			xmlizer = dt;
			xmlInDir = xmlInputDir;
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		public void testDA() {
			List das = xmlizer.das;
			String xmlOutDir = xmlizer.xmlOutDir;

			for (Iterator i = xmlizer.rootMethods.iterator(); i.hasNext();) {
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
		 * @see junit.framework.TestCase#setUp()
		 */
		protected void setUp()
		  throws Exception {
			xmlizer.initialize();
			xmlizer.execute();
		}

		/**
		 * @see junit.framework.TestCase#tearDown()
		 */
		protected void teardown()
		  throws Exception {
			G.reset();
			xmlizer.reset();
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws RuntimeException DOCUMENT ME!
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
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param propFileName DOCUMENT ME!
	 * @param suite DOCUMENT ME!
	 *
	 * @throws IllegalArgumentException DOCUMENT ME!
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
