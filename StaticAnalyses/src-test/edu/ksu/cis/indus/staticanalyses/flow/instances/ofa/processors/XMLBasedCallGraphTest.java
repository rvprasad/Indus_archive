
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

import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;
import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.util.HashMap;
import java.util.Map;


/**
 * This class tests call graphs based on their xmlized representation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class XMLBasedCallGraphTest
  extends AbstractXMLBasedTest
  implements IFAProcessorTest {
	/**
	 * The map of interface id to interface implementation instances.
	 */
	private final Map info = new HashMap();

	/**
	 * The xmlizer used to xmlize the call graph before testing.
	 */
	private CallGraphXMLizer xmlizer;

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer valueAnalyzer) {
	}

	/**
	 * @see IFAProcessorTest#setFA(FA)
	 */
	public void setFA(final FA fa) {
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setFATagName(java.lang.String)
	 */
	public void setFATagName(final String tagName) {
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest#setProcessor(edu.ksu.cis.indus.processing.IProcessor)
	 */
	public void setProcessor(final IProcessor processor) {
		if (processor instanceof ICallGraphInfo) {
			info.put(ICallGraphInfo.ID, processor);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#getFileName()
	 */
	protected String getFileName() {
		return xmlizer.getFileName(getName());
	}

	/**
	 * Initializes and drives the fixture.
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		xmlizer = new CallGraphXMLizer();
		xmlizer.setXmlOutputDir(xmlSecondInputDir);
		xmlizer.setGenerator(new UniqueJimpleIDGenerator());
		info.put(AbstractXMLizer.FILE_NAME_ID, getName());
		xmlizer.writeXML(info);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2004/02/11 09:37:18  venku
   - large refactoring of code based  on testing :-)
   - processing filters can now be chained.
   - ofa xmlizer was implemented.
   - xml-based ofa tester was implemented.
   Revision 1.8  2004/02/09 17:42:25  venku
   - refactoring.
   Revision 1.7  2004/02/09 07:34:31  venku
    - formatting.
   Revision 1.6  2004/02/09 07:33:37  venku
   - id generator was not set on the xmlizer.  FIXED.
   Revision 1.5  2004/02/09 06:49:02  venku
   - deleted dependency xmlization and test classes.
   Revision 1.4  2004/02/09 04:39:36  venku
   - refactoring test classes still..
   - need to make xmlizer classes independent of their purpose.
     Hence, they need to be highly configurable.
   - For each concept, test setup should be in TestSetup
     rather than in the XMLizer.
   Revision 1.3  2004/02/09 02:19:05  venku
    - first stab at refactoring xmlizer framework to be amenable
     to testing and standalone execution.
   Revision 1.2  2004/02/09 02:00:14  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.1  2004/02/09 01:20:10  venku
   - coding convention.
   - added a new abstract class contain the logic required for xml-based
     testing.  (AbstractXMLBasedTest)
   - added a new xml-based call graph testing class.
 */
