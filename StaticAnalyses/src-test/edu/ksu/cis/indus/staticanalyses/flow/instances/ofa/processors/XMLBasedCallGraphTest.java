
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

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.IProcessor;

import edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

import soot.Scene;
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
  extends AbstractXMLBasedTest
  implements IFAProcessorTest {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(XMLBasedCallGraphTest.class);

	/**
	 * This provides the information for the test.  This drives the analyses and this object tests the output.
	 */
	private CallGraphXMLizer xmlizer;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ICallGraphInfo cgi;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Map info = new HashMap();

	/**
	 * Creates a new XMLBasedCallGraphTest object.
	 *
	 * @pre xmlInputDir != null
	 */
	public XMLBasedCallGraphTest() {
		xmlizer = new CallGraphXMLizer();
		xmlizer.setGenerator(new UniqueJimpleIDGenerator());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest#setFA(edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer)
	 */
	public void setFA(IValueAnalyzer valueAnalyzer) {
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest#setProcessor(edu.ksu.cis.indus.processing.IProcessor)
	 */
	public void setProcessor(IProcessor processor) {
		cgi = (ICallGraphInfo) processor;
		info.put(ICallGraphInfo.ID, cgi);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest#setScene(soot.Scene)
	 */
	public void setScene(Scene scene) {
	}

	/**
	 * Tests the inforamtion generated from the associated fixture. This uses <i>XMLUnit</i>.
	 */
	public void testDA() {
		final String _xmlOutDir = getXmlOutputDir();
		final String _xmlInDir = getXmlInputDir();

		for (final Iterator _i = cgi.getHeads().iterator(); _i.hasNext();) {
			final SootMethod _root = (SootMethod) _i.next();
			final String _rootName = _root.getSignature();

			try {
				final Reader _current =
					new FileReader(new File(_xmlOutDir + File.separator + "callgraph_"
							+ _rootName.replaceAll("[\\[\\]\\(\\)\\<\\>: ,\\.]", "") + ".xml"));
				final Reader _previous =
					new FileReader(new File(_xmlInDir + File.separator + "callgraph_"
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
		xmlizer.setXmlOutputDir(getXmlOutputDir());

		for (final Iterator _i = cgi.getHeads().iterator(); _i.hasNext();) {
			SootMethod _rootname = (SootMethod) _i.next();
			xmlizer.writeXML(_rootname.getSignature(), info);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/02/09 02:00:14  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.1  2004/02/09 01:20:10  venku
   - coding convention.
   - added a new abstract class contain the logic required for xml-based
     testing.  (AbstractXMLBasedTest)
   - added a new xml-based call graph testing class.
 */
