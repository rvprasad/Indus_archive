
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

import edu.ksu.cis.indus.staticanalyses.dependency.DependencyXMLizer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.Iterator;
import java.util.List;

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
	DependencyXMLizer xmlizer;

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
	 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
	 */
	public void testDA() {
		final List _das = xmlizer.getDAs();
		final String _xmlOutDir = xmlizer.getXmlOutputDir();

		for (final Iterator _i = xmlizer.sootBasedDriver.getRootMethods().iterator(); _i.hasNext();) {
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
					fail(_e.getMessage());
				} catch (SAXException _e) {
					LOGGER.error("Exception while parsing XML", _e);
					fail(_e.getMessage());
				} catch (ParserConfigurationException _e) {
					LOGGER.error("XML parser configuration related exception", _e);
					fail(_e.getMessage());
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

/*
   ChangeLog:
   $Log$
   Revision 1.14  2004/02/09 02:00:14  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.13  2004/02/08 04:53:11  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.
   Revision 1.12  2004/02/08 01:10:33  venku
   - renamed TestSuite classes to ArgTestSuite classes.
   - added DependencyArgTestSuite.
   Revision 1.11  2004/01/03 21:07:07  venku
   - changed the system property name.
   - documentation.
   Revision 1.10  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
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
