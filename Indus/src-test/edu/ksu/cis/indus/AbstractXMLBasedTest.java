
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

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.XMLTestCase;

import org.xml.sax.SAXException;


/**
 * This class provides abstract implementation that can be used to do xml data based testing.
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
	 * The xmlizer used to xmlize analysis info before testing.
	 */
	protected AbstractXMLizer xmlizer;

	/**
	 * The map of interface id to interface implementation instances.
	 */
	protected final Map info = new HashMap();

	/**
	 * The directory in which one of the xml-based testing input is read from.
	 */
	protected String xmlFirstInputDir;

	/**
	 * The directory in which the other xml-based tesing input is read from.
	 */
	protected String xmlSecondInputDir;

	/**
	 * The statement graph (CFG) factory used during testing.
	 */
	private IStmtGraphFactory stmtGraphFactory;

	/**
	 * The name of the method being run.
	 */
	private String testMethodName = "";

	/**
	 * The name of the test case instance.
	 */
	private String testName = "";

	/**
	 * @see IXMLBasedTest#setFirstXmlInputDir(String)
	 */
	public void setFirstXmlInputDir(final String xmlInDir) {
		xmlFirstInputDir = xmlInDir;
	}

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
		return testName;
	}

	/**
	 * @see IXMLBasedTest#setSecondXmlInputDir(String)
	 */
	public void setSecondXmlInputDir(final String xmlInDir) {
		xmlSecondInputDir = xmlInDir;
	}

	/**
	 * Sets the CFG factory to be used during testing.
	 *
	 * @param cfgFactory is the factory to be used.
	 */
	public void setStmtGraphFactory(final IStmtGraphFactory cfgFactory) {
		stmtGraphFactory = cfgFactory;
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
	 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
	 */
	public void testXMLSimilarity() {
		final String _outfileName = xmlSecondInputDir + File.separator + getFileName();

		try {
			final Reader _current = new FileReader(new File(_outfileName));
			final Reader _previous = new FileReader(new File(xmlFirstInputDir + File.separator + getFileName()));
			final Diff _diff = new Diff(_previous, _current);
			_diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());
			assertXMLEqual(_diff, true);
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
	 * Retrieves the name of the file that contains the test data.  The default implementation constructs a name from the
	 * xmlizer.
	 *
	 * @return the name of the file.
	 */
	protected String getFileName() {
		return xmlizer.getFileName(getName());
	}

	/**
	 * Retrieve the id generator to use during xmlizing. The default implementation returns a
	 * <code>UniqueJimpleIDGenerator</code> instance.
	 *
	 * @return the id generator.
	 */
	protected IJimpleIDGenerator getIDGenerator() {
		return new UniqueJimpleIDGenerator();
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	protected final void setUp()
	  throws Exception {
		xmlizer = getXMLizer();
		xmlizer.setXmlOutputDir(xmlSecondInputDir);
		xmlizer.setGenerator(getIDGenerator());
		localSetup();
		xmlizer.writeXML(info);
	}

	/**
	 * Retrieve the xmlizer to be used to generate the xml data for testing purpose.
	 *
	 * @return the xmlizer.
	 *
	 * @post result != null
	 */
	protected abstract AbstractXMLizer getXMLizer();

	/**
	 * Local test setup to be provided by subclasses.  Default implementation will add the name obtained via
	 * <code>getName()</code> into the <code>info</code> map against the key <code>AbstractXMLizer.FILE_NAME_ID</code> along
	 * with a <code>IStmtGraphFactory.ID</code> to <code>stmtGraphFactory</code> mapping.
	 *
	 * @throws Exception <i>not thrown by this implementation.</i>
	 */
	protected void localSetup()
	  throws Exception {
		info.put(AbstractXMLizer.FILE_NAME_ID, getName());
		info.put(IStmtGraphFactory.ID, stmtGraphFactory);
	}

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
   Revision 1.10  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.9  2004/03/09 18:40:06  venku
   - refactoring.
   - moved methods common to XMLBased Test into AbstractXMLBasedTest.
   Revision 1.8  2004/03/07 20:29:53  venku
   - refactoring.  Moved xmlizing support into this class.
   Revision 1.7  2004/03/05 11:59:40  venku
   - documentation.
   Revision 1.6  2004/02/14 23:16:49  venku
   - coding convention.
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
