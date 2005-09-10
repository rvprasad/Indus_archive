
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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
import edu.ksu.cis.indus.xmlizer.IXMLizer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXMLBasedTest.class);

	/** 
	 * The xmlizer used to xmlize analysis info before testing.
	 */
	protected IXMLizer xmlizer;

	/** 
	 * The map of interface id to interface implementation instances.
	 */
	protected final Map info = new HashMap();

	/** 
	 * The directory in which the test control input is read from.
	 */
	protected String xmlControlDir;

	/** 
	 * The directory in which the test input is read from.
	 */
	protected String xmlTestDir;

	/** 
	 * ID generator used while xmlizing documents which will often be the case (xmlize and test the xmlized data).
	 */
	private IJimpleIDGenerator idGenerator;

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
	 * @see IXMLBasedTest#setIdGenerator(IJimpleIDGenerator)
	 */
	public final void setIdGenerator(final IJimpleIDGenerator generator) {
		idGenerator = generator;
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
	 * @see IXMLBasedTest#setXMLControlDir(String)
	 */
	public void setXMLControlDir(final String xmlInDir) {
		xmlControlDir = xmlInDir;
	}

	/**
	 * @see IXMLBasedTest#setXMLTestDir(String)
	 */
	public void setXMLTestDir(final String xmlInDir) {
		xmlTestDir = xmlInDir;
	}

	/**
	 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
	 */
	public void testXMLSimilarity() {
		final String _outfileName = xmlTestDir + File.separator + getFileName();

		try {
			final Reader _current = new FileReader(new File(_outfileName));
			final Reader _previous = new FileReader(new File(xmlControlDir + File.separator + getFileName()));

			final Diff _diff = new Diff(_previous, _current);
			_diff.overrideElementQualifier(new ElementNameAndAttributeQualifier());

			final boolean _verdict = _diff.similar();
			final String _difference = _diff.toString();
			_current.close();
			_previous.close();
			assertTrue(_difference, _verdict);
		} catch (IOException _e) {
			LOGGER.error("Failed to read the xml file " + _outfileName, _e);
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
	 * @see junit.framework.TestCase#setUp()
	 */
	protected final void setUp()
	  throws Exception {
		xmlizer = getXMLizer();
		xmlizer.setXmlOutputDir(xmlTestDir);
		xmlizer.setGenerator(idGenerator);
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
	protected abstract IXMLizer getXMLizer();

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

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		info.clear();

		if (stmtGraphFactory != null) {
			stmtGraphFactory.reset();
			stmtGraphFactory = null;
		}
		xmlizer = null;
		super.tearDown();
	}
}

// End of File
