
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.AbstractXMLBasedTest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

import soot.SootMethod;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class XMLBasedCallGraphTest
  extends AbstractXMLBasedTest {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(XMLBasedCallGraphTest.class);

	/**
	 * This provides the information for the test.  This drives the analyses and this object tests the output.
	 */
	CallGraphXMLizer xmlizer;

	/**
	 * This is the directory in which the regression test inputs are stored.
	 */
	private String xmlInDir;

	/**
	 * Creates a new XMLBasedCallGraphTest object.
	 *
	 * @param dt is the xmlizer used in this test case.
	 * @param xmlInputDir is the directory in which the base test data of regression test exists.
	 *
	 * @pre dt != null and xmlInputDir != null
	 */
	public XMLBasedCallGraphTest(final CallGraphXMLizer dt, final String xmlInputDir) {
		xmlizer = dt;
		xmlInDir = xmlInputDir;
	}

	/**
	 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
	 */
	public void testDA() {
		final String _xmlOutDir = xmlizer.getXmlOutDir();

		for (final Iterator _i = xmlizer.getRootMethods().iterator(); _i.hasNext();) {
			final SootMethod _root = (SootMethod) _i.next();
			final String _rootName = _root.getSignature();

			try {
				final Reader _current =
					new FileReader(new File(_xmlOutDir + File.separator + "callgraph_"
							+ _rootName.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml"));
				final Reader _previous =
					new FileReader(new File(xmlInDir + File.separator + "callgraph_"
							+ _rootName.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml"));
				assertXMLEqual(_previous, _current);
			} catch (IOException _e) {
				LOGGER.error("Failed to write the xml file for system rooted at method " + _rootName, _e);
			} catch (SAXException _e) {
				LOGGER.error("Exception while parsing XML", _e);
			} catch (ParserConfigurationException _e) {
				LOGGER.error("XML parser configuration related exception", _e);
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
}

/*
   ChangeLog:
   $Log$
 */
