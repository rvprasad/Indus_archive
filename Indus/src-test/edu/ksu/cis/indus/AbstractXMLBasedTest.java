
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

package edu.ksu.cis.indus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.custommonkey.xmlunit.XMLTestCase;

import org.xml.sax.SAXException;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractXMLBasedTest
  extends XMLTestCase
  implements IXMLBasedTest {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractXMLBasedTest.class);

	/**
	 * The directory in which xml-based testing input is read from.
	 */
	protected String xmlInputDir;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected String xmlOutputDir;

	/**
	 * The name of the method being run.
	 */
	private String testMethodName = "";

	/**
	 * The name of the test case instance.
	 */
	private String testName = "";

	/**
	 * @see junit.framework.TestCase#setName(java.lang.String)
	 */
	public void setName(final String name) {
		testMethodName = name;
	}

	/**
	 * @see junit.framework.TestCase#getName()
	 */
	public String getName() {
		final String _result;

		if (!testName.equals("")) {
			_result = testName;
		} else {
			_result = testMethodName;
		}
		return _result;
	}

	/**
	 * Returns the name of the method being tested.
	 *
	 * @return the name of the method being tested.
	 */
	public String getTestMethodName() {
		return testMethodName;
	}

	/**
	 * Sets the name of the test instance.
	 *
	 * @param name of the test instance.
	 *
	 * @pre name != null
	 */
	public void setTestName(final String name) {
		testName = name;
		super.setName(name);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param xmlInDir ME!
	 */
	public void setXmlInputDir(final String xmlInDir) {
		xmlInputDir = xmlInDir;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param xmlOutDir DOCUMENT ME!
	 */
	public void setXmlOutputDir(final String xmlOutDir) {
		xmlOutputDir = xmlOutDir;
	}

	/**
	 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
	 */
	public void testXMLSimilarity() {
		final String _outfileName = xmlOutputDir + File.separator + getFileName();

		try {
			final Reader _current = new FileReader(new File(_outfileName));
			final Reader _previous = new FileReader(new File(xmlInputDir + File.separator + getFileName()));
			assertXMLEqual(_previous, _current);
		} catch (IOException _e) {
			LOGGER.error("Failed to read/write the xml file " + _outfileName, _e);
			fail(_e.getMessage());
		} catch (SAXException _e) {
			LOGGER.error("Exception while parsing XML", _e);
			fail(_e.getMessage());
		} catch (ParserConfigurationException _e) {
			LOGGER.error("XML parser configuration related exception", _e);
			fail(_e.getMessage());
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	protected abstract String getFileName();

	/**
	 * @see junit.framework.TestCase#runTest()
	 */
	protected void runTest()
	  throws Throwable {
		super.setName(testMethodName);
		super.runTest();
		super.setName(testName);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/02/09 07:32:41  venku
   - added support to differentiate test method name and test name.
   - added logic to change name of AbstractXMLBasedTest tests as well.

   Revision 1.4  2004/02/09 06:49:05  venku
   - deleted dependency xmlization and test classes.
   Revision 1.3  2004/02/09 04:39:40  venku
   - refactoring test classes still..
   - need to make xmlizer classes independent of their purpose.
     Hence, they need to be highly configurable.
   - For each concept, test setup should be in TestSetup
     rather than in the XMLizer.
   Revision 1.2  2004/02/09 02:00:11  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.1  2004/02/09 01:20:06  venku
   - coding convention.
   - added a new abstract class contain the logic required for xml-based
     testing.  (AbstractXMLBasedTest)
   - added a new xml-based call graph testing class.
 */
