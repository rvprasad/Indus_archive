
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.instances.AbstractXMLBasedValueAnalysisTest;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import edu.ksu.cis.indus.xmlizer.IXMLizer;


/**
 * This is a XML based test for object flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class XMLBasedOFATest
  extends AbstractXMLBasedValueAnalysisTest {    
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer valueAnalyzer) {
		if (valueAnalyzer instanceof OFAnalyzer) {
			info.put(IValueAnalyzer.ID, valueAnalyzer);
		}
	}

	/**
	 * Retrieve the xmlizer to be used to generate the xml data for testing purpose.
	 *
	 * @return the xmlizer.
	 *
	 * @post result != null
	 */
	protected IXMLizer getXMLizer() {
		return new OFAXMLizer();
	}
}

// End of File
